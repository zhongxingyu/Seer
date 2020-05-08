 package br.com.tealdi.httpclient;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import br.com.tealdi.httpclient.wrapper.IHttpConnectorWrapper;
 
 public class HttpClient implements IHttpClient {
 
	private final IHttpConnectorWrapper connector;
 
	public HttpClient(IHttpConnectorWrapper connector) {
		this.connector = connector;
 	}
 
 	@Override
 	public Response doGet(Request request) throws MalformedURLException, IOException {
		return connector.connectTo(request, HttpVerb.GET);
 	}
 
 }
