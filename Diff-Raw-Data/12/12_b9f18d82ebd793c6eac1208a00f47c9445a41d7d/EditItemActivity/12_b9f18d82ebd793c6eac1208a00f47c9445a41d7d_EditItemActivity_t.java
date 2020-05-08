 package com.swap;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Switch;
 import android.widget.Toast;
 
 public class EditItemActivity extends Activity  implements DBAccessDelegate{
 
	public static final String IMAGES_FOLDER = "http://purple.dotgeek.org/scaled-image.php?src=";
 	public static final String ARG_ITEM_DATA = "item_data"; 
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	public static final int MEDIA_TYPE_VIDEO = 2;
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	
 	Item itemData;
 	private Uri fileUri;
 	private customImage selectedImage;
 	static LinearLayout itemGallery;
 	LocationManager locationManager;
 	LocationProvider provider;
 	LinearLayout linlaHeaderProgress;
 	private static LocationListener locationListener;
 	private Location LastLocation = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sell);
 		getActionBar().setDisplayShowTitleEnabled(false);
 		
 		itemData = (Item) getIntent().getSerializableExtra(ARG_ITEM_DATA);
 		
 		linlaHeaderProgress = (LinearLayout) findViewById(R.id.linearProgress);
 		itemGallery = (LinearLayout)findViewById(R.id.itemGallery);
 		
 		EditText txtTitle = (EditText)findViewById(R.id.txtTitle);
 		txtTitle.setText(itemData.title);
 		
 		EditText txtDescription = (EditText)findViewById(R.id.txtDescription);
 		txtDescription.setText(itemData.description);
 		
 		EditText txtPrice = (EditText)findViewById(R.id.txtPrice);
 		txtPrice.setText(String.valueOf(itemData.price));
 		
 		//CheckBox featured = (CheckBox)findViewById(R.id.checkFeatured);
 		Switch featured = (Switch)findViewById(R.id.toggleFeatured);
 		featured.setChecked(itemData.featured);
 		
 		ImageDownloader mDownload = ImageDownloader.getInstance();
 		for (int i=0; i<itemData.imagesnum;i++)
 		{
 			customImage imageView = new customImage(getApplicationContext());
 			
 			Display display = getWindowManager().getDefaultDisplay();
 			Point size = new Point();
 			display.getSize(size);
 			int width = size.x;
 			
 			imageView.setLayoutParams(new LayoutParams(width,LayoutParams.MATCH_PARENT));
//			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
 			final BitmapFactory.Options options = new BitmapFactory.Options();
 		   
 		    options.inSampleSize = 4;
 		    
 			registerForContextMenu(imageView);
 			
 			mDownload.download(IMAGES_FOLDER + Integer.toString(itemData.id) + "_"+String.valueOf(i+1)+".jpg", imageView);
 			
 			itemGallery.addView(imageView);
 		}
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
 //	    	    Toast msg = Toast.makeText(getApplicationContext(), String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT);
 //	    	    msg.show();
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
 	public void sendButtonClicked(View view)
 	{
 		//NOT IMPLEMENTED CORRECTLY
 //		if(true){
 //			Toast.makeText(this, "NOT IMPLEMENTED YET!", Toast.LENGTH_LONG).show();
 //			return;
 //		}
 		
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
 		linlaHeaderProgress.bringToFront();
 		
 		newItem.id = itemData.id;
 		newItem.date = itemData.date;
 		newItem.title = tmpTitle.getText().toString();
 		newItem.price = Float.parseFloat(tmpPrice.getText().toString());
 		newItem.description = tmpDesc.getText().toString();
 		
 		//CheckBox featured = (CheckBox)findViewById(R.id.checkFeatured);
 		Switch featured = (Switch)findViewById(R.id.toggleFeatured);
 		newItem.featured = featured.isChecked();
 		
 		newItem.rating = 5;
 		
 		newItem.location = itemData.location;
 		/*
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
 		*/
 		newItem.available = true;
 		newItem.sellerid = itemData.sellerid;
 		newItem.imagesnum = itemGallery.getChildCount();
 		
 		
 		DBAccess.updateItem(this, newItem);
 		
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
 	    else
 	    {
 	    	num = "5555555555";
 	    }
 	    return num;
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 	                                ContextMenuInfo menuInfo) {
 	    super.onCreateContextMenu(menu, v, menuInfo);
 	    MenuInflater inflater = getMenuInflater();
 	    selectedImage = (customImage) v;
 	    inflater.inflate(R.menu.context_menu_images, menu);
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
 	           	customImage imageView = new customImage(getApplicationContext());
 				
 				Display display = getWindowManager().getDefaultDisplay();
 				Point size = new Point();
 				display.getSize(size);
 				int width = size.x;
 				
 				imageView.setLayoutParams(new LayoutParams(width,LayoutParams.MATCH_PARENT));
 				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
 				final BitmapFactory.Options options = new BitmapFactory.Options();
 			  
 			    options.inSampleSize = 4;
 	
 			    imageView.setImageBitmap(BitmapFactory.decodeFile(fileUri.getPath(), options));
 				imageView.path = fileUri.toString();
 
 				registerForContextMenu(imageView);
 				
 				itemGallery.addView(imageView);
 				
 	        } else if (resultCode == RESULT_CANCELED) {
 	            // User cancelled the image capture
 	        } else {
 	            // Image capture failed, advise user
 	        }
 	    }
 	 }
 
 	private static Uri getOutputMediaFileUri(int type){
 	      return Uri.fromFile(getOutputMediaFile(type));
 	}
 
 	/** Create a File for saving an image or video */
 	private static File getOutputMediaFile(int type){
 
 	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
 	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
 
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
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_edit_item, menu);
 		return true;
 	}
 
 	@Override
 	public void downloadedResult(List<Item> data) {
 		
 		Toast msg = Toast.makeText(getApplicationContext(), String.valueOf("ITEM UPDATED"), Toast.LENGTH_LONG);
 	    msg.show();
 	    linlaHeaderProgress.setVisibility(View.GONE);
 	    setResult(RESULT_FIRST_USER);
 //	    finish();
 	}
 
 }
