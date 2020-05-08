 package com.example.ad_creation;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.example.browsing.PopularAdsActivity;
 import com.example.custom_views.ProgressView;
 import com.example.maple_android.AdCreationManager;
 import com.example.maple_android.MapleApplication;
 import com.example.maple_android.MapleHttpClient;
 import com.example.maple_android.R;
 import com.example.maple_android.Utility;
 import com.facebook.Session;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 public class PublishActivity extends FunnelActivity {
 	private ImageView mAdView;
 	private ProgressView mProgressBar;
 	private RelativeLayout mLoading;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setCustomContent(R.layout.activity_publish);
 	
 		mConfig.put(Config.HELP_MESSAGE, "You're done! Congrats");
 		mConfig.put(Config.NAME, "Publish");
 		
 		setNextBtn(R.drawable.check, R.drawable.check_pressed);
 
 		mAdCreationManager.setup(this);
 		mLoading = (RelativeLayout) findViewById(R.id.ad_loading);
 	}
 
 	public void publish(View view) {
 		// get user's session details
 		//TODO: Handle session error edge cases?
 		Session session = Session.getActiveSession();
 		
 		EditText contentView = (EditText) findViewById(R.id.ad_content);
 		EditText titleView = (EditText) findViewById(R.id.ad_title);
 
 		
 		Bitmap currBitmap = mApp.getAdCreationManager().getCurrentBitmap();
 		Uri fileUri = mApp.getAdCreationManager().getFileUri();
 		Utility.saveBitmap(fileUri, currBitmap, this);
 		
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		currBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
 		byte[] photoByteArray = stream.toByteArray();
 		
 		RequestParams params = new RequestParams();
 		params.put("post[image]", new ByteArrayInputStream(photoByteArray), fileUri.getPath());
 		params.put("post[title]", titleView.getText().toString());
 		params.put("post[content]", contentView.getText().toString());
		//TODO Eventually switch this to actual company_id when we have access to it
		params.put("post[company_id]", "1");
 		params.put("token", session.getAccessToken());
 		mLoading.setVisibility(View.VISIBLE);
 		MapleHttpClient.post("posts", params, new AsyncHttpResponseHandler(){
 			@Override
 			public void onSuccess(int statusCode, String response) {
 				Intent i = new Intent(PublishActivity.this, PopularAdsActivity.class);
 				i.putExtra("successMessage",
 						"Posted picture successfully! Go to the website to check it out.");
 				startActivity(i);
 				mLoading.setVisibility(View.GONE);
 
 				
 			}
 			
 			@Override
 		    public void onFailure(Throwable error, String response) {
 				Toast.makeText(getApplicationContext(), "Sugar! We ran into a problem!", Toast.LENGTH_LONG).show();
 				mLoading.setVisibility(View.GONE);
 			}
 		});
 	}
 	/**
 	 * Publish the ad to the website
 	 * @param view
 	 */
 	public void nextStage(View view) {
 		selectNext();
 		publish(view);
 	}
 
 	/**
 	 * Return to the previous stage without saving any changes
 	 * 
 	 * @param view
 	 */
 	public void prevStage(View view) {
 		selectPrev();
 		mAdCreationManager.previousStage(this);
 	}
 	
 }
