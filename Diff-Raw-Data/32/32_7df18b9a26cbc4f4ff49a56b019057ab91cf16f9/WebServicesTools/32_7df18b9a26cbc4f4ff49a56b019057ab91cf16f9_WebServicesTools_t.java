 package fr.upsilon.inventirap;
 
 import java.io.IOException;
 
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 
 public class WebServicesTools {
 	public static JSONObject jsonObj;
 	private static String WEBSERVICE_ADDRESS = "/ServicesWeb/materiel/";
 
	public static String getXML(Context context, String url, String content) throws IOException, IllegalArgumentException {
 		String line = null;
 		
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		
 		Log.d("", "WebService call : " + url+WEBSERVICE_ADDRESS+content);
 		HttpPost httpPost = new HttpPost(url+WEBSERVICE_ADDRESS+content);
 		HttpResponse httpResponse;
 
 		httpResponse = httpClient.execute(httpPost);
 		HttpEntity httpEntity = httpResponse.getEntity();
 		line = EntityUtils.toString(httpEntity);
 		
 		return line;
 	}
 	
 	public static boolean JSONFromString(String json) {
 		try {
 			jsonObj = new JSONObject(json);
 			//jsonObj = jsonObj.optJSONObject("materials");
 		} catch (JSONException e) {
 			return false;
 		}
 		return true;
 	}
 }
