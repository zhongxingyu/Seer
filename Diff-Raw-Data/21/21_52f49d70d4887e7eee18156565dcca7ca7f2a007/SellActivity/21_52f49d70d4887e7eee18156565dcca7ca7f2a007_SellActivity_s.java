 package com.swap;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.app.FragmentManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Switch;
 import android.widget.Toast;
 
 public class SellActivity extends Activity implements DBAccessDelegate {
 
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	public static final int MEDIA_TYPE_VIDEO = 2;
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	
 	private Uri fileUri;
 	private Location LastLocation = null;
 	private customImage selectedImage;
 	
 	private static LocationListener locationListener;
 	
 	static LinearLayout itemGallery;
 	
 	LocationManager locationManager;
 	LocationProvider provider;
 	LinearLayout linlaHeaderProgress;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sell);
 		getActionBar().setDisplayShowTitleEnabled(false);
 		
 		linlaHeaderProgress = (LinearLayout) findViewById(R.id.linearProgress);
 		itemGallery = (LinearLayout)findViewById(R.id.itemGallery);
 		
 //		ImageView imageView = new ImageView(getApplicationContext());
 //		imageView.setLayoutParams(new LayoutParams(640,LayoutParams.MATCH_PARENT));
 //		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
 //		imageView.setImageResource(R.drawable.ic_launcher);
 //
 //	//	layout.addView(imageView);
 //
 //		itemGallery.addView(imageView);
 		
 		
 		
 		//DialogFragment newFragment = new progressBarDialogFragment();
 	  //  newFragment.show(getFragmentManager(), "missiles");
 	    
 	}
 	@Override
 	protected void onPause() {
         super.onPause();
 
         locationManager.removeUpdates(locationListener);
         locationListener = null;
     }
 
 	@Override
 	protected void onResume() {
         super.onResume();
         
         startListeningGPS();
     }
 
 //	@Override
 //	public void onBackPressed() {
 ////		Intent sellSummary = new Intent(this, SellSummaryActivity.class);
 ////        startActivity(sellSummary);
 ////        finish();
 //    }
 	
 	private void startListeningGPS() {
 		locationManager =
 		        (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		
 		provider =
 		        locationManager.getProvider(LocationManager.GPS_PROVIDER);
 		
 		final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 	    if (!gpsEnabled) {
 	        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 	        startActivity(settingsIntent);
 	    }
 	    
 	    locationListener = new LocationListener() {
 	    	@Override  
 	    	public void onLocationChanged(Location location) {
	    	    Toast msg = Toast.makeText(getApplicationContext(), String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT);
	    	    msg.show();
 	    		LastLocation = location;
 	    	  }
 	    	@Override
 	    	public void onProviderEnabled(String provider)
 	    	{}
 			@Override
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 				// TODO Auto-generated method stub
 				
 			}
 	    };
 	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_sell, menu);
 		return true;
 	}
 
 	public void sendButtonClicked(View view)
 	{
 		Item newItem = new Item();
 		
 		EditText tmpTitle = (EditText)findViewById(R.id.txtTitle);
 		EditText tmpPrice = (EditText)findViewById(R.id.txtPrice);
 		EditText tmpDesc = (EditText)findViewById(R.id.txtDescription);
 		
 		if(tmpTitle.getText().toString().length()==0 || tmpPrice.getText().toString().length()==0 || tmpDesc.getText().toString().length()==0 )
 		{
 			Toast.makeText(this, "Please enter all the required fields", Toast.LENGTH_LONG).show();
 			return;
 		}
 		
 		
 		
 		linlaHeaderProgress.setVisibility(View.VISIBLE);
 		linlaHeaderProgress.requestFocus();
 		//Button btn = (Button)findViewById(R.id.btnSend);
 		//btn.setClickable(false);
 		//EditText title = (EditText)findViewById(R.id.txtTitle);
 		//title.setEnabled(false);
 		linlaHeaderProgress.bringToFront();
 		
 		newItem.title = tmpTitle.getText().toString();
 		newItem.price = Float.parseFloat(tmpPrice.getText().toString());
 		newItem.description = tmpDesc.getText().toString();
 		
 		//CheckBox featured = (CheckBox)findViewById(R.id.checkFeatured);
 		Switch featured = (Switch)findViewById(R.id.toggleFeatured);
 		newItem.featured = featured.isChecked();
 		
 		// TODO: UI for these properties
 		newItem.rating = 5;
 		
 		if(LastLocation != null)
 		{
 			LastLocation = locationManager.getLastKnownLocation(provider.getName());
 			
 			if(LastLocation != null)
 			{
 				newItem.location = String.valueOf(LastLocation.getLatitude()) + " " + String.valueOf(LastLocation.getLongitude());
 			}
 			else
 			{
 				newItem.location = "0.0 0.0";
 			}
 		}
 		else
 		{
 			newItem.location = "0.0 0.0";
 		}
 		
 		newItem.available = true;
 		newItem.sellerid = getPhoneNumber(10);
 		newItem.imagesnum = itemGallery.getChildCount();
 		
 		
 		DBAccess.createItem(this, newItem);
 		
 	}
 	
 	private String getPhoneNumber(int digitCount) {
 
 		   TelephonyManager mTelephonyMgr;
 	    mTelephonyMgr = (TelephonyManager)
 	        getSystemService(Context.TELEPHONY_SERVICE); 
 	    String num = mTelephonyMgr.getLine1Number();
 	    if(num!=null && num.length()>digitCount)
 	    {
 	    	num= num.substring(num.length()-digitCount);
 	    }
 	    return num;
 	}
 
 	public void downloadedResult(List<Item> data)
 	{
 		Item newItem = data.get(0);
 		
 		if (newItem.id != -1)
 		{
 			Toast.makeText(this, "Item created with ID=" +  String.valueOf(newItem.id), Toast.LENGTH_LONG).show();
 			//Intent sellSummary = new Intent(this, SellSummaryActivity.class);
 	        //startActivity(sellSummary);
 			setResult(RESULT_FIRST_USER);
 	        finish();
 		}
 	}
 	
 	public void launchCamera(View view)
 	{
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
 	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
 	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
 
 	    // start the image capture Intent
 	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	    
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 	        if (resultCode == RESULT_OK) {
 	            // Image captured and saved to fileUri specified in the Intent
 	            //Toast.makeText(this, "Image saved to:\n" +  data.getData(), Toast.LENGTH_LONG).show();
 //	            ImageView image= (ImageView)findViewById(R.id.imageView1);
 //	    	    image.setImageURI(fileUri);
 	    	    
 //	    	    LinearLayout layout = new LinearLayout(getApplicationContext());
 //				layout.setLayoutParams(new LayoutParams(600,LayoutParams.MATCH_PARENT));
 //				layout.setGravity(Gravity.CENTER);
 	        	
 
 				customImage imageView = new customImage(getApplicationContext());
 				
 				Display display = getWindowManager().getDefaultDisplay();
 				Point size = new Point();
 				display.getSize(size);
 				int width = size.x;
 				
 				imageView.setLayoutParams(new LayoutParams(width,LayoutParams.MATCH_PARENT));
 				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
 				final BitmapFactory.Options options = new BitmapFactory.Options();
 			   // options.inJustDecodeBounds = true;
 			    options.inSampleSize = 4;
 			    
 			    
 			    // Calculate inSampleSize
 			    
 				//imageView.setImageURI(fileUri);
 			    imageView.setImageBitmap(BitmapFactory.decodeFile(fileUri.getPath(), options));
 				imageView.path = fileUri.toString();
 				//imageView.setId(itemGallery.getChildCount());
 
 				registerForContextMenu(imageView);
 				
 				//layout.addView(imageView);
 				itemGallery.addView(imageView);
 				
 	        } else if (resultCode == RESULT_CANCELED) {
 	            // User cancelled the image capture
 	        } else {
 	            // Image capture failed, advise user
 	        }
 	    }
 	 }
 
 	/** Create a file Uri for saving an image or video */
 	private static Uri getOutputMediaFileUri(int type){
 	      return Uri.fromFile(getOutputMediaFile(type));
 	}
 
 	/** Create a File for saving an image or video */
 	private static File getOutputMediaFile(int type){
 	    // To be safe, you should check that the SDCard is mounted
 	    // using Environment.getExternalStorageState() before doing this.
 
 	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
 	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
 	    // This location works best if you want the created images to be shared
 	    // between applications and persist after your app has been uninstalled.
 
 	    // Create the storage directory if it does not exist
 	    if (! mediaStorageDir.isDirectory()){
 	        if (! mediaStorageDir.mkdirs()){
 	            Log.d("MyCameraApp", "failed to create directory");
 	            return null;
 	        }
 	    }
 
 	    // Create a media file name
 	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 	    File mediaFile;
 	    if (type == MEDIA_TYPE_IMAGE){
 	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
 	        "IMG_"+ timeStamp + ".jpg");
 	    } else if(type == MEDIA_TYPE_VIDEO) {
 	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
 	        "VID_"+ timeStamp + ".mp4");
 	    } else {
 	        return null;
 	    }
 
 	    return mediaFile;
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 	                                ContextMenuInfo menuInfo) {
 	    super.onCreateContextMenu(menu, v, menuInfo);
 	    MenuInflater inflater = getMenuInflater();
 	    selectedImage = (customImage) v;
 	    inflater.inflate(R.menu.context_menu_images, menu);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 	    switch (item.getItemId()) {
 	        case R.id.menu_delete:
 	        	itemGallery.removeView(selectedImage);
 	        	File toBeRemovedFile = new File(selectedImage.path.substring(7),"" );
 	        	
 	        	Toast msg = Toast.makeText(getApplicationContext(), toBeRemovedFile.getPath(), Toast.LENGTH_SHORT);
 	    	    msg.show();
 	    	    
 	        	if(toBeRemovedFile.exists())
 	        	{
 	        		Boolean removed  = toBeRemovedFile.delete();
 	        	}
 	        	
 	           // editNote(info.id);
 	            return true;
 	        case R.id.menu_rotate:
 	            //deleteNote(info.id);
 	            return true;
 	        default:
 	            return super.onContextItemSelected(item);
 	    }
 	}
 }
