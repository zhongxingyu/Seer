 /**
  * 
  */
 package com.victorpantoja.mss.screen;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.WebView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.victorpantoja.mss.R;
 import com.victorpantoja.mss.util.MD5Util;
 import com.victorpantoja.mss.util.Util;
 
 /**
  * @author victor.pantoja
  *
  */
 public class MyInformationScreen extends Activity {
 	private String auth = "";
 	
     @Override
     public void onResume() {
         super.onResume();
         
         setContentView(R.layout.user_information);
         
         Bundle extras = getIntent().getExtras();
         
         auth = extras.getString("auth");
                 
 		String url = Util.url_get_user+"?auth="+auth;
 		
 		String result = Util.queryRESTurl(url);
 		
 		if(result.equals(""))
 		{
 			Toast.makeText(getApplicationContext(), "Internal Error.", Toast.LENGTH_SHORT).show();
 		}
 		else{
 			try{
 				JSONObject json = new JSONObject(result);
 				TextView firstName = (TextView)findViewById(R.id.textFirstName);
 				TextView user_name = (TextView)findViewById(R.id.textUsername);
 				TextView lastName = (TextView)findViewById(R.id.textLastName);
 								
 				firstName.setText(json.getJSONObject("user").getString("first_name"));
 				user_name.setText(json.getJSONObject("user").getString("username"));
 				lastName.setText(json.getJSONObject("user").getString("last_name"));
 				
				String hash = MD5Util.md5Hex(json.getJSONObject("user").getString("username"));
 				
 				WebView mWebView = (WebView) findViewById(R.id.webkitWebView1);
 				String summary = "<html><body><img src=\"http://www.gravatar.com/avatar/"+hash+"?s=100\"/></body></html>";
 			    mWebView.loadData(summary, "text/html", "utf-8");
 			}  
 			catch (JSONException e) {
 				Log.e("JSON", "There was an error parsing the JSON", e);  
 			}
 		}
     }
 }
