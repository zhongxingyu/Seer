 package com.swap;
 
 import java.util.ArrayList;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Point;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 
 public class ItemDetailActivity extends Activity {
 	
	public static final String IMAGES_FOLDER = "http://purple.dotgeek.org/swapimages/";
 	public static final String ARG_ITEM_DATA = "item_data"; 
 	
 	private Item itemData;
 	
 	LocationListener locationListener;
 	LocationManager locationManager;
 	LocationProvider locationProvider;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_item_detail);
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setDisplayShowTitleEnabled(false);
 		//getActionBar().hide();
 		
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int width = size.x;		
 		
 		itemData = (Item) getIntent().getSerializableExtra(ARG_ITEM_DATA);
 		
 		TextView distanceView = (TextView) findViewById(R.id.distanceView);
 		distanceView.setText(" ");
 		
 		TextView priceView = (TextView) findViewById(R.id.priceView);
 		priceView.setText("$" + ItemAdapter.dollarFormat(itemData.price));
 		
 		TextView titleView = (TextView) findViewById(R.id.titleView);
 		titleView.setText(itemData.title);
 		
 		TextView descriptionView = (TextView) findViewById(R.id.descriptionView);
 		descriptionView.setText(itemData.description);
 		
 		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
 		viewPager.getLayoutParams().width = width;
 		viewPager.getLayoutParams().height = width;
 		
 	    ImagePagerAdapter adapter = new ImagePagerAdapter();
 	    viewPager.setAdapter(adapter);
 	    
 	    this.startListeningGPS();
 	}
 	
 	private void startListeningGPS() {
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
 		
 		final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
 	    if (!gpsEnabled) {
 	        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 	        startActivity(settingsIntent);
 	    }
 	    
 	    locationListener = new LocationListener() {
 	    	@Override  
 	    	public void onLocationChanged(Location location) {
 	    		Location itemLoc = new Location(location);
 	    		
 	    		double longitude = -117.23755;
 				double latitude = 32.88106;
 				 
 				if (!itemData.location.equals("0.0 0.0"))
 				{
 					String[] longLatArray = itemData.location.split(" ");
 					latitude = Double.valueOf(longLatArray[0]);
 					longitude = Double.valueOf(longLatArray[1]);
 				}
 				
 				itemLoc.setLatitude(latitude);
 	    		itemLoc.setLongitude(longitude);
 	    		
 	    		float dist = (float) (location.distanceTo(itemLoc) * 0.00062137);
 	    		
 	    		String distanceToDisplay = ItemAdapter.dollarFormat(dist);
 	    		
 	    		TextView distanceView = (TextView) findViewById(R.id.distanceView);
 	    		distanceView.setText(distanceToDisplay + " miles");
 	    	}
 	    	
 	    	@Override
 	    	public void onProviderEnabled(String provider){}
 			@Override
 			public void onProviderDisabled(String provider) {}
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
 	    };
 	    
 	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_item_detail, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void contactButtonTap(View view) {
 		Intent intent = new Intent(Intent.ACTION_SENDTO);
 
 		intent.setData(Uri.parse("sms:" + itemData.sellerid));
 		intent.putExtra("sms_body", "Hello I'm interested in your " + itemData.title);
         intent.putExtra("compose_mode", true);
 		startActivity(intent);
 	}
 	
 	public void locationButtonTap(View view) {				
 		ArrayList<String> locationArrayList = new ArrayList<String>(1);
 		locationArrayList.add(itemData.location);
 		
 		Intent intent = new Intent(this, MapActivity.class);
 		intent.putStringArrayListExtra(MapActivity.ARG_ITEMS_DATA, locationArrayList);
 		startActivity(intent);
 	}
 
 	
 	private class ImagePagerAdapter extends PagerAdapter {
 
 		@Override
 		public int getCount() {
 			return itemData.imagesnum;
 		}
 
 	    @Override
 	    public boolean isViewFromObject(View view, Object object) {
 	    	return view == ((ImageView) object);
 	    }
 
 	    @Override
 	    public Object instantiateItem(ViewGroup container, int position) {
 	    	Context context = ItemDetailActivity.this;
 	    	
 	    	ImageView imageView = new ImageView(context);
 	    	imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 	    				
 			ImageDownloader mDownload = ImageDownloader.getInstance();
 			mDownload.download(IMAGES_FOLDER + Integer.toString(itemData.id) + "_" + Integer.toString(position + 1) + ".jpg", imageView);
 	    	
 	    	((ViewPager) container).addView(imageView, 0);
 	    	return imageView;
 	    }
 
 	    @Override
 	    public void destroyItem(ViewGroup container, int position, Object object) {
 	    	((ViewPager) container).removeView((ImageView) object);
 	    }
 	}
 }
