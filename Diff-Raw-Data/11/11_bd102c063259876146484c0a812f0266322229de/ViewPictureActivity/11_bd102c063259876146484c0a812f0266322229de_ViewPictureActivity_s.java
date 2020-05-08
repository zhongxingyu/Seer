 package fr.mildlyusefulsoftware.cutekitty.activity;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 import fr.mildlyusefulsoftware.cutekitty.R;
 import fr.mildlyusefulsoftware.cutekitty.service.Picture;
 
 public class ViewPictureActivity extends Activity {
 
 	private static String TAG = "cutekitty";
 	private AdView adView;
 
 	/**
 	 * Called when the activity is first created.
 	 * 
 	 * @param savedInstanceState
 	 *            If the activity is being re-initialized after previously being
 	 *            shut down then this Bundle contains the data it most recently
 	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
 	 *            is null.</b>
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i(TAG, "onCreate");
 		requestWindowFeature(Window.FEATURE_LEFT_ICON);
 		setContentView(R.layout.view_picture_layout);
 		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
 		initAdBannerView();
 		Gallery pictureList = (Gallery) findViewById(R.id.pictureList);
 		loadImageFromPosition(0);
 		pictureList.setAdapter(new ImageAdapter(this));
 		pictureList.setOnItemClickListener(new OnItemClickListener() {
 	        @Override
 			public void onItemClick(AdapterView parent, View v, int position, long id) {
 	        	loadImageFromPosition(position);
 	        }
 	    });
 			if (savedInstanceState != null) {
 				int pos = savedInstanceState.getInt("POSITION");
				pictureList.setSelection(pos);
				loadImageFromPosition(pos);
 			}
 		
 		}
 
 
 	private void loadImageFromPosition(final int pos) {
 		final Activity currentActivity = this;
 		final ProgressDialog progressDialog = ProgressDialog.show(this,
 				"Loading", "please wait", true);
 		new AsyncTask<Void, Void, Picture>() {
 			@Override
 			protected Picture doInBackground(Void... params) {
 				return PicturePager.getInstance(currentActivity).getPictureAt(pos);
 			}
 			protected void onPostExecute(Picture picture) {
 				progressDialog.dismiss();
 				Bitmap b;
 				try {
 					b = Picture.getBitmapFromPicture(picture);
 					ImageView pictureView=(ImageView) findViewById(R.id.pictureView);
 					pictureView.setImageBitmap(b);
 				} catch (IOException e1) {
 					Log.e(TAG,e1.getMessage(),e1);
 				}
 			}
 
 		}.execute();
 
 		
 	}
 
 	
 	protected void initAdBannerView() {
 		final ViewGroup quoteLayout = (ViewGroup) findViewById(R.id.view_picutre_root_layout);
 		// Create the adView
 		adView = new AdView(this, AdSize.BANNER, "a14f6f84d0cc485");
 		LinearLayout adLayout = new LinearLayout(this);
 		adLayout.addView(adLayout);
 		// Add the adView to it
 		quoteLayout.addView(adView);
 		AdRequest ar=new AdRequest();
	//	ar.addTestDevice(AdRequest.TEST_EMULATOR);
 		// Initiate a generic request to load it with an ad
 		adView.loadAd(ar);
 
 	}
 	
 
 	@Override
 	protected void onDestroy() {
 		super.onPause();
 		if (adView != null) {
 			adView.destroy();
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		Gallery pictureList = (Gallery) findViewById(R.id.pictureList);
 		savedInstanceState.putInt("POSITION", pictureList.getSelectedItemPosition());
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 }
