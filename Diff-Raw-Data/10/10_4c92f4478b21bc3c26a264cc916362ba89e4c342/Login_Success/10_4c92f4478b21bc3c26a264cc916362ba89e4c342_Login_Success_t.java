 package com.cookcook.main.login;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.cookcook.main.R;
 import com.cookcook.main.http.RestClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 
 
 
 public class Login_Success extends Activity {
	TextView txt_username,txt_email,txt_token;
 	ImageView img_avatar;
 	static final int SIGN_UP_REQUEST = 1; // The request code
 	static final int HOME_REQUEST = 2; 
 	String xuser;
 	Button btn_followings, btn_followers;
 	URL img_avatar_url = null;
 //SharedPreferences spProfile;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.profile);
		txt_token= (TextView) findViewById(R.id.txt_token);
 		txt_username= (TextView) findViewById(R.id.txt_username);
 		txt_email = (TextView) findViewById(R.id.txt_email);
 		img_avatar=(ImageView)findViewById(R.id.img_avatar);
 		btn_followers = (Button)findViewById(R.id.btn_follwer);
 		btn_followings = (Button)findViewById(R.id.btn_followings);
 		
 		
 		
 		//String xusername = editorprofile.getString("name","khongco");
 		Intent home_intent = getIntent();
 		txt_username.setText(home_intent.getStringExtra("name"));
		txt_token.setText("Token: "+home_intent.getStringExtra("token"));
		
 
 		
 		String android_id = Secure.getString(getBaseContext()
 				.getContentResolver(), Secure.ANDROID_ID);
 		Log.v("token receive:",home_intent.getStringExtra("token"));
 		Login_Preference preference = Login_Preference.getLogin(this);
 		preference.deleteAllKey();
 		preference.putString("name", home_intent.getStringExtra("name"));
 		preference.putString("token", home_intent.getStringExtra("token"));
 		preference.putString("device", android_id);
 		RequestParams params = new RequestParams();
 		params.put("name", home_intent.getStringExtra("name"));
 		params.put("device", android_id);
 		params.put("token", home_intent.getStringExtra("token"));
 		params.put("name_get", home_intent.getStringExtra("name"));
 		Log.v("token set:",preference.getString("token", "0"));
 		RestClient.post("auth/getinfouser", params,
 				new JsonHttpResponseHandler() {
 					@Override
 					public void onSuccess(JSONObject result) {
 						Toast.makeText(getApplicationContext(),
 								result.toString(),
 								Toast.LENGTH_LONG).show();
 						//img_avatar.setImageDrawable(result.getString("avatar"));
 						try {
 							btn_followers.setText(result.getInt("followers_count" )+ " followers");
 							btn_followings.setText(result.getInt("following_count" )+ " followings");

 							txt_username.setText("Screen Name: "+result.getString("screen_name"));
 							txt_email.setText("Email: "+result.getString("email"));
 							img_avatar_url= new URL(result.getString("avatar"));
 							Bitmap bmp_avatar = BitmapFactory.decodeStream(img_avatar_url.openConnection().getInputStream());
 							img_avatar.setImageBitmap(bmp_avatar);
 						} catch (JSONException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (MalformedURLException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					});
 	}
 	
 	
 	
 	
 	
 
 }
