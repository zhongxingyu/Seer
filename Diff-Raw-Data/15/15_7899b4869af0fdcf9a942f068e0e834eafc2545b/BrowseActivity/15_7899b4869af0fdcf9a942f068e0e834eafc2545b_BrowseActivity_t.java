 package com.additt.browsing;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.additt.maple_android.AdCreationDialog;
 import com.additt.maple_android.MapleApplication;
 import com.additt.maple_android.MapleHttpClient;
import com.additt.maple_android.R;
 import com.additt.maple_android.User;
 import com.additt.maple_android.Utility;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 /**
  * This is the base class for the ad browsing tabs on the home screen
  * @author rkpandey
  *
  */
 public class BrowseActivity extends Activity {
 	private static final String TAG = "BrowseAds";
 	// the view we are using to display the ads
 	private GridView mGridview; 
 	// Contains file uri of photo being taken
 	protected Uri mFileUri;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	/**
 	 * Specify which layout to use (either popular ads or personal ads).
 	 * This is put in the parent class since the gridview shares common
 	 * functionality
 	 * @param layout
 	 */
 	public void setLayout(int layout) {
 		setContentView(layout);
 		mGridview = (GridView) findViewById(R.id.gridviewAds);
 
 		// On Click event for Single Gridview Item
         mGridview.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
             	Intent i = new Intent(getApplicationContext(), FullImageActivity.class);
                 DisplayAd displayAd = (DisplayAd) parent.getAdapter().getItem(position);
 				i.putExtra("url", displayAd.getUrl());
                 i.putExtra("title", displayAd.getTitle());
                 startActivity(i);
             }
         });
 	}
 	
 	public void requestUserAds(RequestParams params) {
 		MapleHttpClient.get("posts", params, new AsyncHttpResponseHandler(){
 			// Example json response: http://maplesyrup.herokuapp.com/posts?user_id=3
 			@Override
 			public void onSuccess(int statusCode, String response) {
 				try {
 					JSONArray jObjectAds = new JSONArray(response);
 					if (jObjectAds.length() == 0) {
 						mGridview = (GridView) findViewById(R.id.gridviewAds);
 						((RelativeLayout) mGridview.getParent()).removeView(mGridview);
 						TextView adsTitle = (TextView) findViewById(R.id.adsTitle);
 						adsTitle.setText("There are no ads to show; you should create one!");
 						// Still not centering, sigh...
 						LayoutParams p = (LayoutParams) mGridview.getLayoutParams();
 						p.addRule(RelativeLayout.CENTER_HORIZONTAL);
 						adsTitle.setLayoutParams(p);
 						adsTitle.setTextSize(22);
 						adsTitle.setTypeface(null, Typeface.BOLD);
 					} else {
 						MapleApplication mApp = (MapleApplication) getApplication();
 						User appUser = mApp.getUser();
 						mGridview.setAdapter(new ImageAdapter(getApplicationContext(), jObjectAds, appUser.getToken()));
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			@Override
 		    public void onFailure(Throwable error, String response) {
 				Toast.makeText(getApplicationContext(), "Sugar! We ran into a problem fetching user ads!", Toast.LENGTH_LONG).show();
 		    }
 		});
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	/**
 	 * Respond to each tab button
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return Utility.myOnOptionsItemSelected(this, item);
 	}
 	
 	/**
 	 * After a photo has been taken it gets routed to this function. This will retreive the
 	 * photo that was saved to disk and initialize a new AdCreationManager with it
 	 * 
 	 * @param requestCode The code that identifies the action as coming from the camera
 	 * @param resultCode The success or failure of the action
 	 * @param data Various data about the photo that Android supplies us.
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		MapleApplication app = ((MapleApplication) this.getApplication());
		if (resultCode == Activity.RESULT_CANCELED) {
			return;
		}
 		switch (requestCode) {

 		case AdCreationDialog.CAMERA_REQUEST:
 			if (resultCode == Activity.RESULT_OK) {
 				// Load bitmap into byteArray so that we can pass the data to the
 				// new Activity
 				
 				Bitmap currBitmap = Utility.retrieveBitmap(mFileUri, 240, 320);
 				
 				app.initAdCreationManager(currBitmap, mFileUri);		
 			}
 			break;
 		case AdCreationDialog.PICK_IMAGE:
 	        if(resultCode == Activity.RESULT_OK){  
 	            Uri selectedImage = data.getData();
 	            String[] filePathColumn = {MediaStore.Images.Media.DATA};
 
 	            Cursor cursor = getContentResolver().query(
 	                               selectedImage, filePathColumn, null, null, null);
 	            cursor.moveToFirst();
 
 	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
 	            String filePath = cursor.getString(columnIndex);
 	            cursor.close();
 
 	            app.initAdCreationManager(BitmapFactory.decodeFile(filePath), Uri.parse(filePath));
 	            
 	        }
 	        break;
 
 		}
 		// Reset companyTag from any previous ad creations
 		app.getAdCreationManager().nextStage(this, app.getAdCreationManager().getCurrentBitmap());
 	}
 	
 	public void setFileUri(Uri fileUri) {
 		mFileUri = fileUri;
 	}
 	
 	public Uri getFileUri() {
 		return mFileUri;
 	}
 	
 }
