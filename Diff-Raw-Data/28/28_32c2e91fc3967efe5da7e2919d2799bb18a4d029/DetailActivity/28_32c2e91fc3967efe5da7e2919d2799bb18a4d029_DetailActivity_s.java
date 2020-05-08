 package com.barview;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.concurrent.CountDownLatch;
 
import android.R;
 import android.app.Activity;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.barview.base64.Base64;
 import com.barview.constants.BarviewConstants;
 import com.barview.mobile.BarviewMobileUtility;
 import com.barview.rest.RestClient;
 import com.barview.rest.RestClient.RequestMethod;
 import com.barview.utilities.BarviewUtilities;
 import com.barview.utilities.FacebookUtility;
 
 public class DetailActivity extends Activity {
 	
 	private ImageView imageView;
 	
 	private Button favesButton;
 	
 	private DetailActivity me = this;
 	
 	private String barId;
 	private String barName;
 	
 	private CountDownLatch latch;
 	
 	/**
 	 * Flag that dictates whether or not the BILoop thread is running.
 	 */
 	private boolean runLoop = true;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(com.barview.R.layout.detail);
 	}
 	
 	/**
 	 * Override the onStop() method to set the loop flag to false, killing it.
 	 */
 	public void onStop() {
 		super.onStop();
 		
 		runLoop = false;
 	}
 	
 	/**
 	 * Override the onDestroy() method to set the loop flag to false, killing it.
 	 */
 	public void onDestroy() {
 		super.onDestroy();
 		
 		runLoop = false;
 	}
 	
 	public void onResume() {
 		super.onResume();
 		
 		Log.i("DetailActivity", "in onResume");
 		
 		runLoop = true;
 		
 		Bundle extras = getIntent().getExtras();
 		barId = extras.getString(BarviewConstants.BAR_ID);
 		barName = extras.getString(BarviewConstants.BAR_NAME);
 		
 		/*
 		 * We don't want the "Add to Favorites" button to be visible if
 		 * this bar is already a favorite.
 		 */
 		favesButton = (Button) findViewById(com.barview.R.id.button1);
 		if( (!FacebookUtility.isLoggedIn() && !BarviewMobileUtility.isLoggedIn()) || 
 				((FacebookUtility.isLoggedIn() || BarviewMobileUtility.isLoggedIn()) && FavoritesActivity.isFavorite(barId)) ) {
 			favesButton.setVisibility(View.INVISIBLE);
 		}
 		else
 			favesButton.setVisibility(View.VISIBLE);
 		
 		/*
 		 * Create an OnClickListener for the "Add to Favorites" button so
 		 * it calls out to the REST service and adds this bar as a favorite
 		 * for the logged-in user.
 		 */
 		favesButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				RestClient client = new RestClient(BarviewUtilities.getFavoriteURLForRunMode() + "/" + barId);
				client.AddHeader(BarviewConstants.REST_USER_ID, FacebookUtility.getRESTUserId());
 				
 				try {
 					client.Execute(RequestMethod.POST);
 					String response = client.getResponse();
 					
 					/*
 					 * The POST call for the favorites service should return an empty response to
 					 * us indicating success.  If we get something back in the response (HTML for
 					 * a PHP error) then we know the call failed.  Either way, we will fill in the
 					 * Toast accordingly based on the response.
 					 */
 					Toast toast;
 					if(!response.equals(""))
 						toast = Toast.makeText(me, "Unable to add " + barName + " to favorites.  Please try again later.", Toast.LENGTH_SHORT);
 					else {
 						favesButton.setVisibility(View.INVISIBLE);
 						toast = Toast.makeText(me, "Added " + barName + " to favorites!", Toast.LENGTH_SHORT);
 					}
 					
 					toast.show();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			
 		});
 		
 		TextView tv = (TextView) findViewById(com.barview.R.id.detailTitle);
 		tv.setText(barName);
 		
 //		BarImageFetcher bif = new BarImageFetcher();
 //		bif.execute(barId);
 		BILoop loop = new BILoop();
 		loop.execute(barId);
 	}
 	
 	/**
 	 * This is a helper class that spawns a thread to run the BarImageFetcher thread
 	 * every 5 seconds (or whatever BARIMAGE_REFRESH_TIME is configured for).  It
 	 * runs as long as the runLoop variable is true, which is set to true upon entering
 	 * onResume() and set to false on onStop();
 	 * 
 	 * @author dmaclean
 	 *
 	 */
 	class BILoop extends AsyncTask<String, Integer, String> {
 
 		@Override
 		protected String doInBackground(String... params) {
 			while(runLoop) {
 				BarImageFetcher bif = new BarImageFetcher();
 				bif.execute(barId);
 				
 				try {
 					Thread.sleep(BarviewConstants.BARIMAGE_REFRESH_TIME);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			return null;
 		}
 		
 	}
 	
 	/**
 	 * An AsyncTask class that contacts the Barview server with the user id to
 	 * get their favorites as an XML string.  Once retrieved, this string will
 	 * be parsed by a SAX parser into individual Favorite objects.  Finally,
 	 * we'll grab the ArrayList of Favorite objects so it can be put into the list.
 	 * 
 	 * @author dmaclean
 	 *
 	 */
 	class BarImageFetcher extends AsyncTask<String, Integer, String> {
 
 		String response = "";
 		
 		@Override
 		protected String doInBackground(String... params) {
 			RestClient client = new RestClient(BarviewUtilities.getBarImageURLForRunMode() + "binary/" + params[0]);
 			if(FacebookUtility.isLoggedIn())
 				client.AddHeader(BarviewConstants.REST_USER_ID, FacebookUtility.getRESTUserId());
 			else if(BarviewMobileUtility.isLoggedIn())
 				client.AddHeader(BarviewConstants.REST_USER_ID, BarviewMobileUtility.getUser().getUserId());
 			
 			Log.i(BarImageFetcher.class.getName(), "Starting GET request for bar image");
 			long start = System.currentTimeMillis();
 			try {
 				client.Execute(RequestMethod.GET);
 				response = client.getResponse();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			long end = System.currentTimeMillis();
 			Log.i(BarImageFetcher.class.getName(), "GET request took " + (end-start)/1000.0 + " seconds");
 			
 			return response;
 		}
 		
 		protected void onPostExecute(String result) {
 			long start = System.currentTimeMillis();
 			InputStream is = null;
 			try {
 				is = new ByteArrayInputStream(Base64.decode(response.getBytes()));
 			}
 			catch(Exception e) {
 				Log.e(BarImageFetcher.class.getName(), e.getMessage());
 				e.printStackTrace();
 			}
 			long end = System.currentTimeMillis();
 			Log.i(BarImageFetcher.class.getName(), "Bar image decoding took " + (end-start)/1000.0 + " seconds.");
 			
 			imageView = (ImageView) findViewById(com.barview.R.id.detailImage);
 			imageView.setImageDrawable(Drawable.createFromStream(is, ""));
 			
 			if(latch != null)
 				latch.countDown();
 		}
 	}
 
 	public void setLatch(CountDownLatch latch) {
 		this.latch = latch;
 	}
 }
