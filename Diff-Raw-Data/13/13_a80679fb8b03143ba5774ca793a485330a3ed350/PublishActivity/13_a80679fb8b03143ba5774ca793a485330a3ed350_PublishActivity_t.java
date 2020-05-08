 package com.example.ad_creation;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 import com.example.custom_views.ProgressView;
 import com.example.maple_android.AdCreationManager;
 import com.example.maple_android.MainActivity;
 import com.example.maple_android.MapleApplication;
 import com.example.maple_android.MapleHttpClient;
 import com.example.maple_android.R;
 import com.example.maple_android.Utility;
 import com.facebook.Session;
 import com.larvalabs.svgandroid.SVG;
 import com.larvalabs.svgandroid.SVGParser;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PublishActivity extends FunnelActivity {
 	private MapleApplication mApp;
 	private AdCreationManager mAdCreationManager;
 	private ImageView mAdView;
 	private ProgressView mProgressBar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_publish);
 
 		// Init app
 		mApp = (MapleApplication) this.getApplication();
 		mAdCreationManager = mApp.getAdCreationManager();
 		
 		ImageButton help = (ImageButton) findViewById(R.id.helpButton);
 		SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.question);
 		help.setImageDrawable(svg.createPictureDrawable());
 		help.setBackgroundColor(Color.BLACK);
 
 		mAdCreationManager.setup(this);
 		// customize header text to show company name
		//TextView title = (TextView) findViewById(R.id.headerText);
		//title.setText("Publish Your " + mAdCreationManager.getCompanyName() + " Ad");
 	}
 
 	/**
 	 * Publish the ad to the website
 	 * @param view
 	 */
 	public void publish(View view) {
 		// get user's session details
 		//TODO: Handle session error edge cases?
 		Session session = Session.getActiveSession();
 		
 		Bitmap currBitmap = mApp.getAdCreationManager().getCurrentBitmap();
 		Uri fileUri = mApp.getAdCreationManager().getFileUri();
 		Utility.saveBitmap(fileUri, currBitmap, this);
 		
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		currBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
 		byte[] photoByteArray = stream.toByteArray();
 		
 		RequestParams params = new RequestParams();
 		params.put("post[image]", new ByteArrayInputStream(photoByteArray), fileUri.getPath());
 		params.put("post[title]", "Company: " + mAdCreationManager.getCompanyName());
 		params.put("token", session.getAccessToken());
 
 		MapleHttpClient.post("posts", params, new AsyncHttpResponseHandler(){
 			@Override
 			public void onSuccess(int statusCode, String response) {
 				Intent i = new Intent(PublishActivity.this, MainActivity.class);
 				i.putExtra("successMessage",
 						"Posted picture successfully! Go to the website to check it out.");
 				startActivity(i);
 			}
 			
 			@Override
 		    public void onFailure(Throwable error, String response) {
 				Toast.makeText(getApplicationContext(), "Sugar! We ran into a problem!", Toast.LENGTH_LONG).show();
 		    }
 		});
 	}
 
 	/**
 	 * Return to the previous stage without saving any changes
 	 * 
 	 * @param view
 	 */
 	public void prevStage(View view) {
 		mAdCreationManager.previousStage(this);
 	}
 
 	public void getHelp(View v) {
 		String message = "You're done! Congrats";
 		String title = "Step " + mAdCreationManager.getReadableCurrentStage() + " of " + mAdCreationManager.getNumStages();
 		Utility.createHelpDialog(this, message, title);
 	}
 	
 }
