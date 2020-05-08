 package br.com.tealdi.httpclient;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import br.com.tealdi.httpclient.wrapper.IHttpConnectorWrapper;
 
 public class HttpClient implements IHttpClient {
 
	private final IHttpConnectorWrapper httpConnectorWrapperMocked;
 
	public HttpClient(IHttpConnectorWrapper httpConnectorWrapperMocked) {
		this.httpConnectorWrapperMocked = httpConnectorWrapperMocked;
 	}
 
 	@Override
 	public Response doGet(Request request) throws MalformedURLException, IOException {
		return httpConnectorWrapperMocked.connectTo(request, HttpVerb.GET);
 	}
 
 }
