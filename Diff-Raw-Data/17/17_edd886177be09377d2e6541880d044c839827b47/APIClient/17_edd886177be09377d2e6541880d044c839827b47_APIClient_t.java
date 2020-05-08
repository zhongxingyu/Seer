 package controllers;
 
 import java.io.IOException;
 import java.net.URI;
import api.requests.*;
import api.responses.*;
 import javax.naming.OperationNotSupportedException;
 
 import oauth.signpost.http.HttpRequest;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 /*import org.json.JSONObject;*/
 

 import api.helpers.GsonHelper;
 
 import play.Play;
 import play.mvc.*;
 import play.mvc.Http.StatusCode;
 
 public class APIClient extends Controller {
 	
 	public static APIClient singleton;
 	public static HttpHost host;
 	static {
 		host = new HttpHost(Play.configuration.getProperty("openarms.service_host"), Integer.parseInt(Play.configuration.getProperty("openarms.service_port")));
 	}
     
 	public DefaultHttpClient client;
 	/**
 	 * Constructing a new API client, try to reuse these.
 	 */
 	public APIClient() {
 		client = new DefaultHttpClient();
 	}
 	
 	public static APIClient getInstance() {
 		if(singleton == null) {
 			singleton = new APIClient();
 		}
 		return singleton;
 	}
 	
	private HttpRequestBase getBaseRequest(api.requests.Request request) throws Exception {
 		HttpRequestBase httpRequest;
 		if(request.method == Request.Method.GET) {
 			httpRequest = new HttpGet();
 		} else if(request.method == Request.Method.POST) {
 			httpRequest = new HttpPost();
 		} else if(request.method == Request.Method.PUT) {
 			httpRequest = new HttpPut();
 		} else if(request.method == Request.Method.DELETE) {
 			httpRequest = new HttpDelete();
 		} else {
 			throw new Exception("Unknown HTTP-method of the API-request.");
 		}
 		return httpRequest;
 	}
 	
	private Response sendRequest(Request request) throws Exception {
 		String json = GsonHelper.toJson(request);
 		HttpRequestBase httpRequest = getBaseRequest(request);
 		ByteArrayEntity bae = new ByteArrayEntity(json.getBytes());
 		
 		// Adding the payload
 		if(httpRequest instanceof HttpPost) {
 			((HttpPost) httpRequest).setEntity(bae);
 		} else if(httpRequest instanceof HttpPut) {
 			((HttpPut) httpRequest).setEntity(bae);
 		}
 		// Setting the URI.
 		httpRequest.setURI(URI.create(request.getURL()));
 		
 		// TODO: Remember to set the encoding of the request.
 		
 		HttpResponse httpResponse = client.execute(host, httpRequest);
 		HttpEntity httpResponseEntity = httpResponse.getEntity();
 		// Check the response content-type.
 		if(httpResponseEntity.getContentType().getValue().startsWith("application/json")) {
 			return GsonHelper.fromJson(httpResponseEntity.getContent(), request.EXPECTED_RESPONSE);
 		} else {
 			throw new Exception("Http response didn't have the application/json content-type.");
 		}
 	}
 	
	public static Response send(Request request) throws Exception {
 		return getInstance().sendRequest(request);
 	}
 }
