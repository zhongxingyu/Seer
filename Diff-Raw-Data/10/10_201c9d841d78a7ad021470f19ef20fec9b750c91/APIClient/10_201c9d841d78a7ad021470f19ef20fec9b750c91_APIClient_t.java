 package controllers;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.HashMap;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import play.Logger;
 import play.Play;
 import play.libs.Codec;
 import play.libs.Crypto;
 import play.mvc.Controller;
 import play.mvc.Http.StatusCode;
 import api.entities.UserJSON;
 import api.helpers.GsonHelper;
 import api.requests.AuthenticateUserRequest;
 import api.requests.LoadTestDataRequest;
 import api.requests.Request;
 import api.responses.AuthenticateUserResponse;
 import api.responses.EmptyResponse;
 import api.responses.ExceptionResponse;
 import api.responses.Response;
 
 public class APIClient extends Controller {
 
 	/**
 	 * The host to use when contacting the service, this will be set by the constructor.
 	 */
 	public HttpHost host;
 	
 	/**
 	 * Constructing a new API client, try to reuse these.
 	 */
 	public APIClient() {
 		this(Play.configuration.getProperty("openarms.service_host"), Integer.valueOf(Play.configuration.getProperty("openarms.service_port")));
 	}
 	
 	/**
 	 * Constructs a new API client, from a set of try to reuse these.
 	 * 
 	 * @param hostname
 	 * @param port
 	 * @param userId
 	 * @param userSecret
 	 */
 	public APIClient(String hostname, int port) {
 		host = new HttpHost(hostname, port);
 	}
 	
 	public void setAuthentication(Long userId, String userSecret) {
 		session.put("user_id", userId);
 		session.put("user_secret", Crypto.decryptAES(userSecret));
 	}
 	
 	public static APIClient getInstance() {
 		return new APIClient();
 	}
 	
 	private HttpRequestBase getBaseRequest(api.requests.Request request) throws Exception {
 		HttpRequestBase httpRequest;
 		if(request.getHttpMethod().equals(Request.Method.GET)) {
 			httpRequest = new HttpGet();
 		} else if(request.getHttpMethod().equals(Request.Method.POST)) {
 			httpRequest = new HttpPost();
 		} else if(request.getHttpMethod().equals(Request.Method.PUT)) {
 			httpRequest = new HttpPut();
 		} else if(request.getHttpMethod().equals(Request.Method.DELETE)) {
 			httpRequest = new HttpDelete();
 		} else {
 			throw new Exception("Unknown HTTP-method of the API-request.");
 		}
 		return httpRequest;
 	}
 	
 	public Response sendRequest(Request request) throws Exception {
 		DefaultHttpClient client = new DefaultHttpClient();
 		
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
 		// Set the authentication header to match the user_secret known from the user session.
 		String encryptedUserSecret = session.get("user_secret");
 		String userId = session.get("user_id");
 		if(encryptedUserSecret != null && !encryptedUserSecret.isEmpty() && userId != null && !userId.isEmpty()) {
 			Logger.debug("encryptedUserSecret = "+encryptedUserSecret);
 			String decryptedUserSecret = Crypto.decryptAES(encryptedUserSecret);
 			String authenticationString = userId+":"+decryptedUserSecret;
 			String decryptedUserSecretBase64Encoded = Codec.encodeBASE64(authenticationString.getBytes());
 			httpRequest.addHeader("Authorization", "Basic "+decryptedUserSecretBase64Encoded);
 		}
 		
 		// TODO: Remember to set the encoding of the request.
 		Logger.debug("APIClient sends: %s to %s%s as %s", json, host.toString(), httpRequest.getURI(), httpRequest.getMethod());
 		
 		HttpResponse httpResponse = client.execute(host, httpRequest);
 		HttpEntity httpResponseEntity = httpResponse.getEntity();
 		// Check the response content-type.
 		if(httpResponseEntity.getContentType().getValue().startsWith("application/json")) {
 			BufferedReader br = new BufferedReader(new InputStreamReader(httpResponseEntity.getContent()));
 			String responseJson = "";
 			String line;
 			while ((line = br.readLine()) != null) {
 				responseJson += line;
 				Logger.debug("APIClient receives: %s", line);
 			}
 			Response response = GsonHelper.fromJson(responseJson, request.getExpectedResponseClass());
 			response.statusCode = httpResponse.getStatusLine().getStatusCode();
 			return response;
 		} else {
 			Logger.debug("APIClient receives something with the wrong content-type.");
 			//Logger.debug(httpResponseEntity.getContent());
 			BufferedReader br = new BufferedReader(new InputStreamReader(httpResponseEntity.getContent()));
 			String line;
 			while ((line = br.readLine()) != null) {
 				Logger.debug("APIClient receives: %s", line);
 			}
 			throw new Exception("Http response didn't have the application/json content-type, got: "+httpResponseEntity.getContentType().getValue());
 		}
 	}
 	
 	public static Response send(Request request) throws Exception {
 		return getInstance().sendRequest(request);
 	}
 
 	public static void tunnel(String url) {
 		try {
 			url = "/"+url; // As the initial slash is removed in routes.
 			
 			DefaultHttpClient client = new DefaultHttpClient();
 			
 			HttpRequestBase httpRequest;
 			if(request.method.equals("GET")) {
 				httpRequest = new HttpGet();
 			} else if(request.method.equals("POST")) {
 				httpRequest = new HttpPost();
 			} else if(request.method.equals("PUT")) {
 				httpRequest = new HttpPut();
 			} else if(request.method.equals("DELETE")) {
 				httpRequest = new HttpDelete();
 			} else {
 				System.err.println("Unknown HTTP-method of the API-request.");
 				throw new Exception("Unknown HTTP-method of the API-request.");
 			}
 			
 			httpRequest.setURI(URI.create(url));
 			// Set the authentication header to match the user_secret known from the user session.
 			String encryptedUserSecret = session.get("user_secret");
 			String userId = session.get("user_id");
 			if(encryptedUserSecret != null && !encryptedUserSecret.isEmpty() && userId != null && !userId.isEmpty()) {
 				Logger.debug("encryptedUserSecret = "+encryptedUserSecret);
 				String decryptedUserSecret = Crypto.decryptAES(encryptedUserSecret);
 				String authenticationString = userId+":"+decryptedUserSecret;
 				String decryptedUserSecretBase64Encoded = Codec.encodeBASE64(authenticationString.getBytes());
 				httpRequest.addHeader("Authorization", "Basic "+decryptedUserSecretBase64Encoded);
 			}
 
 			if(Logger.isDebugEnabled()) {
 				Logger.debug("APIClient tunnel sends: "+httpRequest.getMethod()+" for "+httpRequest.getURI()+" on "+getInstance().host);
 				for(org.apache.http.Header h: httpRequest.getAllHeaders()) {
 					Logger.debug(" - with the following headers ("+httpRequest.getAllHeaders().length+" in total): "+h.getName()+" = "+h.getValue());
 				}
 			}
 			
 			HttpResponse httpResponse = client.execute(getInstance().host, httpRequest);
 			
 			// Set the status code.
 			response.status = httpResponse.getStatusLine().getStatusCode();
 			// Tunnel through the headers ...
 			for(org.apache.http.Header h: httpResponse.getAllHeaders()) {
 				response.setHeader(h.getName(), h.getValue());
 			}
 			// Write the response.
 			httpResponse.getEntity().writeTo(response.out);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.status = StatusCode.INTERNAL_ERROR;
 	        response.contentType = "application/json";
 	        
 	        Response responseJson = new ExceptionResponse(e);
 			renderJSON(responseJson);
 		}
 	}
 	
 	public static EmptyResponse loadServiceData(String yamlDataFile) throws Exception {
 		LoadTestDataRequest req = new LoadTestDataRequest(yamlDataFile);
 		return (EmptyResponse) APIClient.send(req);
 	}
 
 	public static boolean authenticateSimple(String email, String password) throws Exception {
 		UserJSON u = new UserJSON();
 		u.email = email;
 		u.attributes = new HashMap<String,String>();
 		u.attributes.put("password", password);
 		AuthenticateUserRequest req = new AuthenticateUserRequest(u, "class models.SimpleUserAuthBinding");
 		AuthenticateUserResponse res = (AuthenticateUserResponse) APIClient.send(req);
 		if(StatusCode.success(res.statusCode) && res.user != null && res.user.id != null && res.user.secret != null) {
 			session.put("user_id", res.user.id);
 			session.put("user_secret", Crypto.encryptAES(res.user.secret));
 			return true;
 		} else {
			session.put("user_id", null);
			session.put("user_secret", null);
 			return false;
 		}
 	}
 }
