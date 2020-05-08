 package edu.ncue.im;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.Facebook;
 import com.facebook.android.FacebookError;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import edu.ncue.test.jls.*;
 
 public class ContentDetailActivity extends Activity {
 	protected ImageView im;
 	protected TextView poiTitle;
 	protected ScrollView sV;
 	protected Button likeButton;
 	protected Bitmap bitmap;
 	private Facebook facebook = SettingsActivity.facebook;
 	String test;
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()){
 		case android.R.id.home:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		ActionBar actionBar = this.getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		
 		this.setContentView(R.layout.content_detail);
 		
 		final HashMap<String, String> poiMap = (HashMap<String, String>)this.getIntent().getSerializableExtra("SinglePOI");
 		im = (ImageView)findViewById(R.id.titleImageView);
		im.setImageResource(R.drawable.placeholder_history);
 
 		
 		if (poiMap.get("PICsURL") != null) {
 			new Thread(new Runnable(){
 
 				@Override
 				public void run() {
 					
 					String[] imageURLs = poiMap.get("PICsURL").split(";");
 					
 					Log.d("dev", imageURLs.toString());
 					try {
 						bitmap = BitmapFactory.decodeStream((InputStream) new URL(
 								imageURLs[0]).getContent());
 						
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					ContentDetailActivity.this.runOnUiThread(new Runnable(){
 						@Override
 						public void run(){
 							
 							if (bitmap != null)
 								im.setImageBitmap(bitmap);
 						}
 					});
 				}
 				
 			}).start();
 			
 		}
 		
 		poiTitle = (TextView)findViewById(R.id.descripe_of_POI);
 		poiTitle.setText(poiMap.get("POI_title"));
 		
 		likeButton = (Button)findViewById(R.id.likebutton);
 		likeButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				if(facebook.isSessionValid()){
 					test ="failed";
 					
 					AsyncFacebookRunner as = new AsyncFacebookRunner(facebook);
 					//as.request("me", new UserRequestListener());
 					Bundle params = new Bundle();
 					
 					JSONObject historic_monument = new JSONObject();
 					try {
 						historic_monument.put("title", "Test")
 						.put("image", "http://deh.csie.ncku.edu.tw/moe2/picture/407-000013-pic1.jpg")
 						.put("longitude", "120.651735")
 						.put("latitude", "24.160578")
 						.put("description", "test_description");
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					Log.d("facebook", historic_monument.toString());
 					
 					params.putString("object", historic_monument.toString());
 					//params.putString("og:title", "Test");
 					//params.putString("og:image", "http://deh.csie.ncku.edu.tw/moe2/picture/407-000013-pic1.jpg");
 					//params.putString("longitude", "120.651735");
 					//params.putString("latitude", "24.160578");
 					//params.putString("og:description", "description");
 					as.request("me/og.Likes", params, "POST", new UserRequestListener(), new Object());
 							
 							
 					
 					
 				}
 			}
 		});
 			
 		
 		sV = (ScrollView)findViewById(R.id.contentScrollView);
 		TextView poiContent = new TextView(this);
 		poiContent.setText(poiMap.get("POI_description"));
 		sV.addView(poiContent);
 		
 		
 		
 	}
 	
 	class UserRequestListener implements RequestListener{
 
 		@Override
 		public void onComplete(String response, Object state) {
 			// TODO Auto-generated method stub
 			test = response;
 			Log.d("fbtest",test);
 		}
 
 		@Override
 		public void onIOException(IOException e, Object state) {
 			// TODO Auto-generated method stub
 			e.printStackTrace();
 		}
 
 		@Override
 		public void onFileNotFoundException(FileNotFoundException e,
 				Object state) {
 			// TODO Auto-generated method stub
 			e.printStackTrace();
 		}
 
 		@Override
 		public void onMalformedURLException(MalformedURLException e,
 				Object state) {
 			// TODO Auto-generated method stub
 			e.printStackTrace();
 		}
 
 		@Override
 		public void onFacebookError(FacebookError e, Object state) {
 			// TODO Auto-generated method stub
 			e.printStackTrace();
 		}
 		
 	}
 }
