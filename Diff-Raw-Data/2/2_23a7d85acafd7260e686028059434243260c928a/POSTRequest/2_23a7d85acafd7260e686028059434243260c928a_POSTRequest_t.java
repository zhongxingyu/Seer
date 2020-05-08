 package org.letstalktech.aahw;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.BitmapFactory;
 
 public class POSTRequest extends HTTPRequest {
 	HttpPost post;
 	public POSTRequest(CookieStore cookieStore, Callback cb){
 		super(cookieStore,cb);
 	}
 	public POSTRequest(CookieStore cookieStore, Callback cb, Callback errorCallback){
 		super(cookieStore,cb,errorCallback);
 	}
 
 	public POSTRequest(){
 		super();
 	}
 
 	public POSTRequest(CookieStore cookieStore){
 		super(cookieStore);
 	}
 
 	@Override	
 	protected Result doInBackground(Parameters... parameters) {
 		Result result = new Result();
 		android.util.Log.v("POSTRequest","Entered the POSTRequest");
 		try {
 			String postURL = serverAddress+parameters[0].getPath();
 			post = new HttpPost(postURL); 
 			android.util.Log.e("POSTRequest",postURL);
 			
 			if(parameters[0].getUserAgent().length() > 0)
 				post.getParams().setParameter(CoreProtocolPNames.USER_AGENT, parameters[0].getUserAgent());
 			
 			//List<NameValuePair> params = new ArrayList<NameValuePair>();
 			MultipartEntity ent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 			Charset chars = Charset.forName("UTF-8");
 			
			for (Map.Entry<String, Object> e : parameters[0].getHeaders().entrySet())
 			{
 				post.addHeader(e.getKey(), e.getValue().toString());
 			}
 			
 			for (Map.Entry<String, Object> e : parameters[0].getParams().entrySet())
 			{
 			//	params.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
 				if(e.getValue().getClass().getSimpleName().contentEquals("File"))
 					ent.addPart(e.getKey(),new FileBody((File)e.getValue()));
 				else{
 				StringBody test = new StringBody(e.getValue().toString(),"text/plain",chars);
 				ent.addPart(e.getKey(),test);
 				}
 				
 			}
 
 			//UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
 			//post.addHeader("Content-Type", "multipart/form-data");
 			post.setEntity(ent);
 			response = httpclient.execute(post,localContext);  
 
 			result.setStatus(response.getStatusLine().getStatusCode());
 			android.util.Log.v("POSTRequest",String.valueOf(result.getStatus()));
 			HttpEntity resEntity = response.getEntity();
 			if (resEntity != null) {    
 				try{
 					android.util.Log.v("POSTRequest", response.getHeaders("Content-Type")[0].getValue());
 					if(response.getHeaders("Content-Type")[0].getValue().contains("image")){
 						android.util.Log.e("POSTRequest","I'm an image!");
 						BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(resEntity);
 						InputStream is = bufHttpEntity.getContent();
 						result.setResponse(BitmapFactory.decodeStream(is));
 					}
 					else
 						if(response.getHeaders("Content-Type")[0].getValue().contains("json")){
 							JSONObject json = null;
 							try {
 								String teste = EntityUtils.toString(resEntity);
 								android.util.Log.e("POSTRequest",teste);
 								json=new JSONObject(teste);
 							
 //								android.util.Log.e("POSTRequest",json.toString());
 							} catch (ParseException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							} catch (JSONException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							result.setResponse(json);
 						}
 						else
 							if(response.getHeaders("Content-Type")[0].getValue().contains("text/html")){
 								result.setResponse(EntityUtils.toString(resEntity));
 								android.util.Log.e("POSTRequest",(String) result.getResponse());
 							}
 					
 				}catch(NullPointerException e){
 					result.setResponse(new String("null"));
 				}
 			}
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 //	protected void onPostExecute(Result result){
 //		android.util.Log.v("POSTRequest",String.valueOf(result.getStatus()));
 //		if(callback != null)
 //			callback.run(result);
 //	}
 }
