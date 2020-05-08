 /**
  * 
  */
 package eu.fbk.dycapo.services;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import eu.fbk.dycapo.exceptions.DycapoException;
 import eu.fbk.dycapo.util.StreamConverter;
 
 /**
  * @author riccardo
  *	HEAD 		Asks for the response identical to the one that would correspond to a GET request, but without the response body. This is useful for retrieving meta-information written in response headers, without having to transport the entire content.
  *	GET			Requests a representation of the specified resource. Note that GET should not be used for operations that cause side-effects, such as using it for taking actions in web applications. One reason for this is that GET may be used arbitrarily by robots or crawlers, which should not need to consider the side effects that a request should cause. See safe methods below.
  *	POST		Submits data to be processed (e.g., from an HTML form) to the identified resource. The data is included in the body of the request. This may result in the creation of a new resource or the updates of existing resources or both.
  *	PUT			Uploads a representation of the specified resource.
  *	DELETE		Deletes the specified resource.
  */
 public abstract class DycapoServiceClient {
 	public static final String URL_BASIS = "http://test.dycapo.org/api/";
 	
 	private static final String TAG = "DycapoServiceClient";
 	
 	
 	/**
 	 * @param code
 	 * @return
 	 * @throws DycapoException 
 	 */
 	public static final void translateStatusCode(int code,boolean log) throws DycapoException
 	{
 		boolean eThrow = false;
 		String msg = null;
 		switch(code){
 		case 200:
 			msg = "Ok!";
 			break;
 		case 201:
 			msg = "Resource Created";
 			break;
 		case 204:
 			msg = "Resource Deleted";
 			break;
 		case 401:
 			msg = "Unauthorized! Invalid Credentials";
 			eThrow = true;
 			break;
 		case 403:
 			msg = "Forbidden";
 			eThrow = true;
 			break;
 		case 404:
 			msg = "Resource Not Found";
 			eThrow = true;
 			break;
 		case 415:
 			msg = "Unsupported Media Type";
 			eThrow = true;
 			break;
 		}
 		
 		if (log == true)
 			Log.d(TAG + ".translateStatusCode", "status code: "+code + "message: " + msg);
		if (eThrow == true){
 			Log.e(TAG, msg);
 			throw new DycapoException ("status code : " + String.valueOf(code));
		}
 	}
 	
 	
 	public static final String MESSAGE = "message";
 	public static final int HEAD = 0;
 	public static final int GET = 1;
 	public static final int POST = 2;
 	public static final int PUT = 3;
 	public static final int DELETE = 4;
 	
 	
 	public static final String callDycapo(int method,String uri,JSONObject jsonObject,String username,String password) throws DycapoException, JSONException{
 		Log.d(TAG, jsonObject.toString());
 		HttpResponse response = doJSONRequest(method,uri,jsonObject,username,password);
 
 		try {
 			String stringResp = StreamConverter.convertStreamToString(response.getEntity().getContent());
 			try{
 				translateStatusCode(response.getStatusLine().getStatusCode(),true);
 			}catch(DycapoException e){
 				throw new DycapoException(e.getMessage() + " cause : " + stringResp);
 			}
 			return stringResp;
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			Log.e(TAG, e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e(TAG, e.getMessage());
 		}
 		return null;
 	}
 	
 	private static final HttpResponse doJSONRequest(int method,String uri,JSONObject jsonObject,String username,String password){
 		DefaultHttpClient httpclient = new DefaultHttpClient();
 		StringBuilder sb =new StringBuilder();
 		sb.append(URL_BASIS);
 		sb.append(uri);
 		sb.append("/");
 		URI uriF;
 		try {
 			uriF = new URI(sb.toString());
 			if (username instanceof String || password instanceof String)
 				httpclient.getCredentialsProvider().setCredentials(
 						new AuthScope(uriF.getHost(), uriF.getPort(),AuthScope.ANY_REALM),
 						new UsernamePasswordCredentials(username, password));
 			HttpResponse response = null;
 
 			switch(method){
 			case HEAD:
				
 				break;
 			case GET:
				
 				break;
 			case POST:
 				StringEntity se;
 				se = new StringEntity(jsonObject.toString());
 				
 				HttpPost requestPost = new HttpPost(uriF);
 				requestPost.setHeader("Accept", "application/json");
 				requestPost.setHeader("Content-type", "application/json");
 				requestPost.setEntity(se);
 				response = (HttpResponse) httpclient.execute(requestPost);
 				break;
 			case PUT:
			
 				break;
 			case DELETE:
				
 				break;
 			}
 			return response;
 		} catch (ClientProtocolException e) {
 	
 			e.printStackTrace();
 			Log.e(TAG, e.getMessage());
 		} catch (IOException e) {
 			
 			e.printStackTrace();
 			Log.e(TAG, e.getMessage());
 		} catch (URISyntaxException e1) {
			
 			e1.printStackTrace();
 			Log.e(TAG, e1.getMessage());
 		}
 
 		return null;
 	}
 }
