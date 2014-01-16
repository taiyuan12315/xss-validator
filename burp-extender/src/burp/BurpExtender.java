package burp;
import burp.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;


public class BurpExtender implements IBurpExtender, IHttpListener, IIntruderPayloadGeneratorFactory, IIntruderPayloadProcessor	{
	public burp.IBurpExtenderCallbacks mCallbacks;
	private IExtensionHelpers helpers;
	private PrintWriter stdout;
    private PrintWriter stderr;
    
    private HttpClient client;
    
    private static String phantomServer = "http://127.0.0.1:8093";
	
	public static final byte[][] PAYLOADS = {
		"|".getBytes(),
		"<script>alert(1)</script>".getBytes()
	};
	
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		mCallbacks = callbacks;
		
		this.client = HttpClientBuilder.create().build();
		helpers = callbacks.getHelpers();
		callbacks.setExtensionName("XSS Auditor Payloads");
		stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(), true);
		callbacks.registerIntruderPayloadGeneratorFactory(this);
		callbacks.registerIntruderPayloadProcessor(this);
		callbacks.registerHttpListener(this);
	}
	
	@Override
	public String getGeneratorName() {
		return "XSS Auditor Payloads";
	}
	
	@Override
    public IIntruderPayloadGenerator createNewInstance(IIntruderAttack attack)
    {
        // return a new IIntruderPayloadGenerator to generate payloads for this attack
        return new IntruderPayloadGenerator();
    }

    //
    // implement IIntruderPayloadProcessor
    //
    
    @Override
    public String getProcessorName()
    {
        return "XSS Validator";
    }
    
    @Override
    public byte[] processPayload(byte[] currentPayload, byte[] originalPayload, byte[] baseValue) {
    	return helpers.stringToBytes(helpers.urlEncode(helpers.bytesToString(currentPayload)));
    }
    
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		stdout.println("HTTP Listener started...");
		
        if (toolFlag == 32 && messageIsRequest) {
        	stdout.println("HTTP Listener intruder http request match found...");
        	// Manipulate intruder request, if necessary
        } else if (toolFlag == 32 && ! messageIsRequest) {
        	stdout.println("Response Received");
        	HttpPost PhantomJs = new HttpPost(phantomServer);
        	
        	try {
	        	StringEntity input = new StringEntity("asdf");
	        	
	        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	        	nameValuePairs.add(new BasicNameValuePair("http-response", "CjxodG1sPgo8aGVhZD4KCjxzY3JpcHQgbGFuZ3VhZ2U9IkphdmFTY3JpcHQiPgovLyBDbGllbnRzIHdobyBoYXZlIGlmcmFtZXMgZW5hYmxlZCBydW4gaW50byBjcm9zcy1kb21haW4gamF2YXNjcmlwdCBzZWN1cml0eSBjaGVja3MgYW5kCi8vIGVtb3RpY29ucyBmYWlscyB0byBkaXNwbGF5IGluIHRoZSBwb3N0IGZvcm0uIFRoZSBmaXggaXMgdG8gc2V0IHRoZSBkb21haW4gbmFtZSB0byBtYXRjaCB0aGF0Ci8vb2YgdGhlIGNsaWVudCBlLmcuIHNldCB0aGUgZG9tYWluIG5hbWUgdG8gInR2Z3VpZGUuY29tIiBpbnN0ZWFkIG9mIHRoZSBkZWZhdWx0ICJjb21tdW5pdHkudHZndWlkZS5jb20iLgovLwovLyBCdXQgdGhpcyBuZWVkcyB0byBiZSB0ZXN0ZWQgd2hldGhlciB0aGUgZm9ybSBjYW4gYmUgZm91bmQgZmlyc3QgLSBiZWNhdXNlIGZvciBzb21lIGNsaWVudHMgdGhpcyAnZml4JyB3aWxsIGNhdXNlIHRoZSBwb3N0Zm9ybSB0byBub3QgYmUgZm91bmQKdHJ5IHsgCiAgdmFyIG15VGVzdEZpZWxkID0gd2luZG93Lm9wZW5lci5kb2N1bWVudC4iPjwvc2NyaXB0PjxzY3JpcHQ+YWxlcnQoJ1hTUycpPC9zY3JpcHQ+MHhBbGkgLSBYU1NFRDwhLS0uYm9keTsKfQpjYXRjaCAoZXJyKSB7CmlmIChkb2N1bWVudC5kb21haW4gIT0gImxvY2FsaG9zdCIpIHsKICAgdmFyIG15ZG9tYWluID0gZG9jdW1lbnQuZG9tYWluOwogICBteWRvbWFpbl9hcnIgPSBteWRvbWFpbi5zcGxpdCggIi4iKTsKICAgaWYgKG15ZG9tYWluX2Fyci5sZW5ndGggPiAyKSB7CiAgICAgICBteWRvbWFpbiA9IG15ZG9tYWluX2FycltteWRvbWFpbl9hcnIubGVuZ3RoIC0gMl0gKyAiLiIgKyBteWRvbWFpbl9hcnJbbXlkb21haW5fYXJyLmxlbmd0aCAgLSAxXTsKICAgICAgIGRvY3VtZW50LmRvbWFpbiA9IG15ZG9tYWluOwogICB9Cn0KfQo8IS0tCmZ1bmN0aW9uIHNlbGVjdCAocykgewogIHZhciBteUZpZWxkID0gd2luZG93Lm9wZW5lci5kb2N1bWVudC4iPjwvc2NyaXB0PjxzY3JpcHQ+YWxlcnQoJ1hTUycpPC9zY3JpcHQ+MHhBbGkgLSBYU1NFRDwhLS0uYm9keTsKICBpZiAod2luZG93Lm9wZW5lci5kb2N1bWVudC5zZWxlY3Rpb24pIHsKICAgIG15RmllbGQuZm9jdXMoKTsKICAgIHZhciBzZWwgPSB3aW5kb3cub3BlbmVyLmRvY3VtZW50LnNlbGVjdGlvbi5jcmVhdGVSYW5nZSgpOwogICAgc2VsLnRleHQgPSBzOwogIH0gZWxzZSBpZiAobXlGaWVsZC5zZWxlY3Rpb25TdGFydCB8fCBteUZpZWxkLnNlbGVjdGlvblN0YXJ0ID09ICcwJykgewogICAgdmFyIHN0YXJ0UG9zID0gbXlGaWVsZC5zZWxlY3Rpb25TdGFydDsKICAgIHZhciBlbmRQb3MgPSBteUZpZWxkLnNlbGVjdGlvbkVuZDsKICAgIG15RmllbGQudmFsdWUgPSBteUZpZWxkLnZhbHVlLnN1YnN0cmluZygwLCBzdGFydFBvcykgKyBzICsgbXlGaWVsZC52YWx1ZS5zdWJzdHJpbmcoZW5kUG9zLCBteUZpZWxkLnZhbHVlLmxlbmd0aCk7CiAgfSBlbHNlIHsKICAgIG15RmllbGQudmFsdWUgKz0gczsKICB9CiAgd2luZG93LmNsb3NlICgpOwp9CmZ1bmN0aW9uIGNhbmNlbCAoKSB7CiAgd2luZG93LmNsb3NlICgpOwp9Ci8vIC0tPgo8L3NjcmlwdD4KPC9oZWFkPgo8Ym9keT4KPHRhYmxlIHdpZHRoPSIxMDAlIiBib3JkZXI9IjAiIGNlbGxzcGFjaW5nPSIwIiBjZWxscGFkZGluZz0iMSI+Cjx0cj48dGQgYWxpZ249ImNlbnRlciIgY29sc3Bhbj0iNCI+PHNwYW4gY2xhc3M9Imx3LXRleHQiIHN0eWxlPSJmb250LXNpemU6IDEycHg7IGZvbnQtZmFtaWx5OiBhcmlhbCxoZWx2ZXRpY2Esc2Fucy1zZXJpZjsiPlNlbGVjdCBFbW90aWNvbnM6PC9zcGFuPjwvdGQ+PC90cj4KPHRyPjx0ZCB3aWR0aD0iMjUlIj48YSBocmVmPSIjIiBvbmNsaWNrPSJzZWxlY3QgKCc6LSknKTtyZXR1cm4gZmFsc2U7Ij48aW1nIHNyYz0iaW1hZ2VzL2Vtb3RpY29ucy9oYXBweS5naWYiIGJvcmRlcj0iMCI+PC9hPjwvdGQ+Cjx0ZCB3aWR0aD0iMjUlIj48YSBocmVmPSIjIiBvbmNsaWNrPSJzZWxlY3QgKCc6LSgnKTtyZXR1cm4gZmFsc2U7Ij48aW1nIHNyYz0iaW1hZ2VzL2Vtb3RpY29ucy9zYWQuZ2lmIiBib3JkZXI9IjAiPjwvYT48L3RkPgo8dGQgd2lkdGg9IjI1JSI+PGEgaHJlZj0iIyIgb25jbGljaz0ic2VsZWN0ICgnOi1EJyk7cmV0dXJuIGZhbHNlOyI+PGltZyBzcmM9ImltYWdlcy9lbW90aWNvbnMvZ3Jpbi5naWYiIGJvcmRlcj0iMCI+PC9hPjwvdGQ+Cjx0ZCB3aWR0aD0iMjUlIj48YSBocmVmPSIjIiBvbmNsaWNrPSJzZWxlY3QgKCc6LXgnKTtyZXR1cm4gZmFsc2U7Ij48aW1nIHNyYz0iaW1hZ2VzL2Vtb3RpY29ucy9sb3ZlLmdpZiIgYm9yZGVyPSIwIj48L2E+PC90ZD48L3RyPgo8dHI+PHRkIHdpZHRoPSIyNSUiPjxhIGhyZWY9IiMiIG9uY2xpY2s9InNlbGVjdCAoJzp8Jyk7cmV0dXJuIGZhbHNlOyI+PGltZyBzcmM9ImltYWdlcy9lbW90aWNvbnMvcGxhaW4uZ2lmIiBib3JkZXI9IjAiPjwvYT48L3RkPjwvdGQ+Cjx0ZCB3aWR0aD0iMjUlIj48YSBocmVmPSIjIiBvbmNsaWNrPSJzZWxlY3QgKCdCLSknKTtyZXR1cm4gZmFsc2U7Ij48aW1nIHNyYz0iaW1hZ2VzL2Vtb3RpY29ucy9jb29sLmdpZiIgYm9yZGVyPSIwIj48L2E+PC90ZD4KPHRkIHdpZHRoPSIyNSUiPjxhIGhyZWY9IiMiIG9uY2xpY2s9InNlbGVjdCAoJ106KScpO3JldHVybiBmYWxzZTsiPjxpbWcgc3JjPSJpbWFnZXMvZW1vdGljb25zL2RldmlsLmdpZiIgYm9yZGVyPSIwIj48L2E+PC90ZD4KPHRkIHdpZHRoPSIyNSUiPjxhIGhyZWY9IiMiIG9uY2xpY2s9InNlbGVjdCAoJzotcCcpO3JldHVybiBmYWxzZTsiPjxpbWcgc3JjPSJpbWFnZXMvZW1vdGljb25zL3NpbGx5LmdpZiIgYm9yZGVyPSIwIj48L2E+PC90ZD48L3RyPgo8dHI+PHRkIHdpZHRoPSIyNSUiPjxhIGhyZWY9IiMiIG9uY2xpY2s9InNlbGVjdCAoJ1gtKCcpO3JldHVybiBmYWxzZTsiPjxpbWcgc3JjPSJpbWFnZXMvZW1vdGljb25zL2FuZ3J5LmdpZiIgYm9yZGVyPSIwIj48L2E+PC90ZD4KPHRkIHdpZHRoPSIyNSUiPjxhIGhyZWY9IiMiIG9uY2xpY2s9InNlbGVjdCAoJzpeTycpO3JldHVybiBmYWxzZTsiPjxpbWcgc3JjPSJpbWFnZXMvZW1vdGljb25zL2xhdWdoLmdpZiIgYm9yZGVyPSIwIj48L2E+PC90ZD4KPHRkIHdpZHRoPSIyNSUiPjxhIGhyZWY9IiMiIG9uY2xpY2s9InNlbGVjdCAoJzstKScpO3JldHVybiBmYWxzZTsiPjxpbWcgc3JjPSJpbWFnZXMvZW1vdGljb25zL3dpbmsuZ2lmIiBib3JkZXI9IjAiPjwvYT48L3RkPgo8dGQgd2lkdGg9IjI1JSI+PGEgaHJlZj0iIyIgb25jbGljaz0ic2VsZWN0ICgnOjh9Jyk7cmV0dXJuIGZhbHNlOyI+PGltZyBzcmM9ImltYWdlcy9lbW90aWNvbnMvYmx1c2guZ2lmIiBib3JkZXI9IjAiPjwvYT48L3RkPjwvdHI+Cjx0cj48dGQgd2lkdGg9IjI1JSI+PGEgaHJlZj0iIyIgb25jbGljaz0ic2VsZWN0ICgnOl98Jyk7cmV0dXJuIGZhbHNlOyI+PGltZyBzcmM9ImltYWdlcy9lbW90aWNvbnMvY3J5LmdpZiIgYm9yZGVyPSIwIj48L2E+PC90ZD4KPHRkIHdpZHRoPSIyNSUiPjxhIGhyZWY9IiMiIG9uY2xpY2s9InNlbGVjdCAoJz86fCcpO3JldHVybiBmYWxzZTsiPjxpbWcgc3JjPSJpbWFnZXMvZW1vdGljb25zL2NvbmZ1c2VkLmdpZiIgYm9yZGVyPSIwIj48L2E+PC90ZD4KPHRkIHdpZHRoPSIyNSUiPjxhIGhyZWY9IiMiIG9uY2xpY2s9InNlbGVjdCAoJzpPJyk7cmV0dXJuIGZhbHNlOyI+PGltZyBzcmM9ImltYWdlcy9lbW90aWNvbnMvc2hvY2tlZC5naWYiIGJvcmRlcj0iMCI+PC9hPjwvdGQ+CjwvdHI+Cjx0cj48dGQgd2lkdGg9IjI1JSI+PGEgaHJlZj0iIyIgb25jbGljaz0iY2FuY2VsICgpO3JldHVybiBmYWxzZTsiPjxpbWcgc3JjPSJpbWFnZXMvYmFjay10by0xNngxNi5naWYiIGJvcmRlcj0iMCI+PC9hPjwvdGQ+PC90cj4KPC90YWJsZT4KPC9ib2R5Pgo8L2h0bWw+Cg=="));
	        	//PhantomJs.setEntity(nameValuePairs);
	        	PhantomJs.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        	HttpResponse response = client.execute(PhantomJs);
	        	String responseAsString = EntityUtils.toString(response.getEntity());
	            stdout.println(responseAsString);
        	} catch (Exception e) {
        		stderr.println(e.getMessage());
        	}
        }
        stdout.println("HTTP Listener finished...");
	}
		
	class IntruderPayloadGenerator implements IIntruderPayloadGenerator {
		int payloadIndex;
		
		@Override
		public boolean hasMorePayloads() {
			System.out.println("Checking for more payloadz");
			return payloadIndex < PAYLOADS.length;
		}
		
		@Override
		public byte[] getNextPayload(byte[] baseValue) {
			System.out.println("Getting next payload");
			
			byte[] payload = PAYLOADS[payloadIndex];
			payloadIndex++;
			return payload;
		}
		
		@Override
		public void reset() {
			payloadIndex = 0;
		}
		
	}
}
