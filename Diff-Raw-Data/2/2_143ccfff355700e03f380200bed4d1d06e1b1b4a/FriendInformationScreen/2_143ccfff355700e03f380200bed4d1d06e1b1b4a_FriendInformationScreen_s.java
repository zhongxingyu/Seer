 /**
  * 
  */
 package com.victorpantoja.mss.screen;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.victorpantoja.mss.R;
 import com.victorpantoja.mss.util.MD5Util;
 import com.victorpantoja.mss.util.Util;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author victor.pantoja
  *
  */
 public class FriendInformationScreen extends Activity implements OnClickListener {
 	
 	String auth;
 	Button btnAddRemove;
 	Boolean isFriend, isInvite;
 	String username;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.user_information);
         
         Bundle extras = getIntent().getExtras();
         auth = extras.getString("auth");
         isFriend = extras.getBoolean("isFriend");
         username = extras.getString("username");
         isInvite = extras.getBoolean("isInvite");
                 
 		String url = Util.url_get_user+"?username="+username+"&auth="+auth;
 		
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
 				btnAddRemove = (Button)findViewById(R.id.addRemoveFriendButtom);
 								
 				firstName.setText(json.getJSONObject("user").getString("first_name"));
 				user_name.setText(json.getJSONObject("user").getString("username"));
 				lastName.setText(json.getJSONObject("user").getString("last_name"));
 				
 				if(!isFriend && !isInvite){
 					btnAddRemove.setText("Follow");
 				}
 				
 				if(isInvite){
 					btnAddRemove.setText("Accept");
 				}
 				
 				btnAddRemove.setVisibility(View.VISIBLE);
 				btnAddRemove.setOnClickListener(this);
 				
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
 
     @Override
 	public void onClick(View v) {
 		
     	if(!isInvite){
         	if(isFriend){
         		addRemoveFriend("remove");
         	}
         	else{
         		addRemoveFriend("add");
         	}	
     	}
     	else{
     		acceptInvite();
     	}
 	}
     
     private void acceptInvite() {
     	
 		String url = Util.url_accept_invite+"?username="+username+"&auth="+auth;
 		String result = Util.queryRESTurl(url);
 		
 		if(result.equals(""))
 		{
 			Toast.makeText(getApplicationContext(), "Internal Error.", Toast.LENGTH_SHORT).show();
 		}
 		else{
 			try{
 				JSONObject json = new JSONObject(result);
 				
 				Toast.makeText(getApplicationContext(), json.getString("msg"), Toast.LENGTH_SHORT).show();
 										
 				if (json.getString("status").equals("ok"))
 				{	
 			    	isFriend = true;
 			    	isInvite = false;
 			    	btnAddRemove.setText("Unfollow");
 				}
 			}  
 			catch (JSONException e) {  
 				Log.e("JSON", "There was an error parsing the JSON", e);  
 			}
 		}
     }
 
 	private void addRemoveFriend(String type) {
 		
 		String url = (type=="add")?Util.url_send_invite:Util.url_remove_friendship;
 				
 		url = url+"?username="+username+"&auth="+auth;
 		String result = Util.queryRESTurl(url);
 		
 		if(result.equals(""))
 		{
 			Toast.makeText(getApplicationContext(), "Internal Error.", Toast.LENGTH_SHORT).show();
 		}
 		else{
 			try{
 				JSONObject json = new JSONObject(result);
 				
 				Toast.makeText(getApplicationContext(), json.getString("msg"), Toast.LENGTH_SHORT).show();
 										
 				if (json.getString("status").equals("ok"))
 				{	
 					if(!isFriend){
 			    		btnAddRemove.setClickable(false);
 			    		btnAddRemove.setEnabled(false);
 					}
 					else{
 			    		isFriend = false;
 			    		btnAddRemove.setText("Follow");
 					}
 				}
 			}  
 			catch (JSONException e) {  
 				Log.e("JSON", "There was an error parsing the JSON", e);  
 			}
 		}
 	}
 }
