 package com.example.webphotos;
 
 import java.io.BufferedReader;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.ref.WeakReference;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.json.*;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.support.v4.util.LruCache;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 
 import android.provider.Settings.Secure;
 
 public class MainWebActivity extends Activity {
 	private String currentLocation = "";
 	SeekBar seekbar;
 	TextView txt;
 
 
 
 	private String getURL(int rad) {
 
 		// Acquire a reference to the system Location Manager
 		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 		LocationListener locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location provider.
 				currentLocation = String.valueOf(location.getLatitude()) + "+" + String.valueOf(location.getLongitude());
 			}
 
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 			public void onProviderEnabled(String provider) {}
 
 			public void onProviderDisabled(String provider) {}
 		};
 
 		// Register the listener with the Location Manager to receive location updates
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 
 		final String android_id = Secure.getString(getBaseContext().getContentResolver(),
 				Secure.ANDROID_ID);
 		//final String user = "aaa"; //idk how to deal with making users unique right now
 		String url = "http://18.238.2.68/cuisinestream/phonedata.cgi?user="+android_id+"&location="+currentLocation+"&radius="+rad;
 		return url;
 	}
 
 	private class ListOfRestaurants
 	{
 		public List<RestaurantData> restaurants;
 
 		public ListOfRestaurants()
 		{
 			restaurants = new ArrayList<RestaurantData>();
 		}
 
 		public ListOfRestaurants(List<RestaurantData> data)
 		{
 			restaurants = data;
 		}
 
 		public RestaurantData[] getRestaurantArray()
 		{			
 			RestaurantData[] result;
 			result = new RestaurantData[restaurants.size()];
 			for(int i = 0; i < restaurants.size(); i++)
 			{
 				result[i] = restaurants.get(i);
 			}
 			return result;
 		}
 	}
 
 	protected class RestaurantData
 	{
 		public String id;
 		public String name;
 		public double rating;
 		public double lat;
 		public double lng;
 		public double distance;
 		public List<String> photos;
 
 		/**
 		 * 
 		 * @param ID
 		 * @param Name
 		 * @param Lat
 		 * @param Lng
 		 * @param Rating
 		 * @param Photos
 		 */
 		public RestaurantData(String ID, String Name, double Lat, double Lng, double Rating, double Distance, List<String> Photos)
 		{
 			id = ID;
 			name = Name;
 			lat = Lat;
 			lng = Lng;
 			rating = Rating;
 			distance = Distance;
 			photos = Photos;
 		}
 
 		@Override
 		public String toString()
 		{
 			return "id: " + id + ", name: " + name + ", #photos: " + String.valueOf(photos.size());
 		}
 	}
 
 	private class RestaurantInfoTask extends AsyncTask<String, Void, String> {
 
 		public RestaurantInfoTask() {}
 
 		@Override
 		protected String doInBackground(String... arg0) {
 			String rawData="";
 			try {
 				URL url = new URL(arg0[0]);
 				//Log.d("reader", "1");
 				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 				//Log.d("reader", "2");
 				String inputLine;
 				while((inputLine=in.readLine())!=null) {rawData=inputLine;}
 				//Log.d("reader", "3");
 				in.close();
 				//Log.d("reader", "rawData = "+rawData);
 			} catch (Exception e) {
 				Log.e("Error", e.getMessage());
 				e.printStackTrace();
 			}
 			return rawData;
 		}
 
 		@Override
 		protected void onPostExecute(String raw) {
 			try {
 				JSONTokener tokener = new JSONTokener(raw);
 				JSONObject obj1 = (JSONObject) tokener.nextValue();
 				JSONArray keys = obj1.names();
 				ListOfRestaurants restData = new ListOfRestaurants();
 				for(int i = 0; i < keys.length(); i++)
 				{
 					String id = keys.get(i).toString();
 					JSONObject restaurant = (JSONObject)obj1.get(id);
 					double lat = restaurant.getDouble("lat");
 					double lng = restaurant.getDouble("lng");
 					double dist = restaurant.getDouble("distance");
 					String name = restaurant.getString("name");
 					double rating = Double.NaN;
 					if(restaurant.has("rating"))
 					{
 						rating = restaurant.getDouble("rating");
 					}
 					JSONArray photosArray = restaurant.getJSONArray("photos");
 					List<String> photos = new ArrayList<String>();
 					for(int j = 0; j < photosArray.length(); j++)
 					{
 						photos.add(photosArray.getString(i));
 					}
 					restData.restaurants.add(new RestaurantData(id, name, lat, lng, rating, dist, photos));
 				}
 				Log.d("parseJSON", String.valueOf(restData.restaurants.size()));
 				for(RestaurantData data : restData.restaurants)
 				{
 					Log.v("printJSON", data.toString());
 				}
 			}
 			catch (JSONException e)
 			{
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	private LruCache<String, Bitmap> mMemoryCache; //instantiate cache
 
 	//test data
 	String[][] testData = {{"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
 		"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
 		"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
 		"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
 		"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg",
 	"http://images4.fanpop.com/image/photos/18400000/pickle-delivery-pickles-18401263-373-500.jpg"},
 	{"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRFknsdZDTESSTWImcDDcx_-NR0WXriUbW7VSxLkatioTwm3tWe",
 		"http://images.all-free-download.com/images/graphicmedium/fast_food_04_vector_156273.jpg",
 		"http://images.all-free-download.com/images/graphicmedium/fast_food_06_vector_156271.jpg",
 		"http://www.camillesdish.com/wp-content/uploads/et_temp/IMG_1721-455568_200x200.jpg",
 		"http://www.stopfoodborneillness.org/sites/default/files/images/1food.jpg",
 	"http://neworleanslocal.com/wp-content/uploads/2012/05/fresh-produce-200x200.jpg"}};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_main_web);
 
 		//		jsonTestRead();
 		new RestaurantInfoTask().execute("http://18.238.2.68/cuisinestream/phonedata.cgi?user=jes&location=42.358506+-71.060142&radius=2000");
 		//activity_main_web has a vertical linearlayout base view
 		LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
 
 		//set up banner
 		Resources res = getResources();
 		ImageView banner = (ImageView)findViewById(R.id.bannerSpace);
 		banner.setImageDrawable(res.getDrawable(R.drawable.csbanner));
 
 		//listen to the distance slider
 		seekbar = (SeekBar)findViewById(R.id.distanceSlide);
 		txt = (TextView)findViewById(R.id.radius);
 		txt.setText("Set search radius: 100 ft");
 		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int prog,
 					boolean fromUser) {
 				String st="you fucked up";
 				if (prog<=2) {st = "100 ft";}
 				else if (prog>2 && prog<=4) {st = "200 ft";}
 				else if (prog>4 && prog<=6) {st = "350 ft";}
 				else if (prog>6 && prog<=8) {st = "500 ft";}
 				else if (prog>8 && prog<=10) {st = ".1 mile";}
 				else if (prog>10 && prog<=15) {st = ".25 mile";}
 				else if (prog>15 && prog<=20) {st = ".5 mile";}
 				else if (prog>15 && prog<=25) {st = ".75 mile";}
 				else if (prog>25 && prog<=30) {st = "1 mile";}
 				else if (prog>30 && prog<=40) {st = "1.5 miles";}
 				else if (prog>40 && prog<=50) {st = "2 miles";}
 				else if (prog>50 && prog<=60) {st = "2.5 miles";}
 				else if (prog>60 && prog<=72) {st = "3 miles";}
 				else if (prog>72 && prog<=85) {st = "4 miles";}
 				else {st = "5 miles";}
 				//this is pretty ugly but oh well
 				txt.setText("Set search radius: "+st);
 			}
 		});
 
 		//vertical scroll in layout containing a vertical linearlayout containing the restaurant scrolls
 		ScrollView nearbyRestaurants = (ScrollView)findViewById(R.id.nearbyRestaurants);
 		LinearLayout restaurantsFrame = new LinearLayout(this);
 		restaurantsFrame.setOrientation(1);
 		View parent = (View) restaurantsFrame.getParent();
 		Log.d("find error", "resFrame parent: "+parent);
 		nearbyRestaurants.addView(restaurantsFrame);
		layout.addView(nearbyRestaurants);
 
 		//made each horizontalscrollview clickable instead
 //		//adding a button to test swipe view
 //		Button pressme = new Button(this);
 //		layout.addView(pressme);
 //		pressme.setOnClickListener(new Button.OnClickListener() {
 //			public void onClick(View v) {
 //				Intent toPage = new Intent(MainWebActivity.this, GalleryActivity.class);
 //				startActivity(toPage);
 //			}
 //		});
 
 		//set up horizontal restaurant scrolls
 		final int PREVIEW_HEIGHT = 200; //height of each restaurant preview in scroll
 		LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, PREVIEW_HEIGHT);
 
 		//need to differentiate frames. create an ArrayList array. lol
 		//number of restaurants defined by testData.length. need that many copies of horizontalscrollview
 		LinearLayout[] imgFramesList = new LinearLayout[testData.length];
 		for (int i=0; i<testData.length; i++) {
 			HorizontalScrollView restaurant = new HorizontalScrollView(this); //create scroll
 			restaurant.setLayoutParams(hp); //set height
 			restaurant.setClickable(true);
 			restaurant.setOnClickListener(new Button.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent toPage = new Intent(MainWebActivity.this, GalleryActivity.class);
 //					toPage.putExtra(restaurantData); //send restaurantdata object to next activity
 					toPage.putExtra("tester", "message");
 					startActivity(toPage);
 				}
 			});
 			restaurantsFrame.addView(restaurant); //add scroll to scroll container
 			imgFramesList[i] = new LinearLayout(this); //make frame for scroll
 			restaurant.addView(imgFramesList[i]); //add frame to scroll
 		}
 
 		//define pic size. should be unnecessary if pictures come in at a specified size
 		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 200);
 
 		//get an Array or ArrayList of urls. An array would be faster but it might be harder to make
 		//depending on how the server sends restaurant data.
 		//TO-DO tie these together in one loop
 		for (int i=0; i<testData[0].length; i++) {
 			ImageView fillin = new ImageView(this);
 			fillin.setLayoutParams(lp);
 			imgFramesList[0].addView(fillin);
 			new BitmapWorkerTask(fillin).execute(testData[0][i]);
 		}
 
 		//another set of urls to test lag for uncached images. seems not bad
 		for (int i=0; i<testData[1].length; i++) {
 			ImageView fillin = new ImageView(this);
 			fillin.setLayoutParams(lp);
 			imgFramesList[1].addView(fillin);
 			new BitmapWorkerTask(fillin).execute(testData[1][i]);
 		}
 
 		//setup for the cache
 		final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
 		final int cacheSize = maxMemory / 8;
 		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
 			@Override
 			protected int sizeOf(String key, Bitmap bitmap) {
 				return bitmap.getByteCount() / 1024;
 			}
 		};
 	}
 
 	//cache methods
 	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
 		if (getBitmapFromMemCache(key) == null) {
 			mMemoryCache.put(key, bitmap);
 		}
 	}
 
 	public Bitmap getBitmapFromMemCache(String key) {
 		return mMemoryCache.get(key);
 	}
 
 	//this is the AsyncTask class that handles background download of images
 	private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
 		private final WeakReference<ImageView> imageViewRef;
 
 		public BitmapWorkerTask(ImageView imgV) {
 			// Use a WeakReference to ensure the IV can be garbage collected
 			imageViewRef = new WeakReference<ImageView>(imgV);
 		}
 
 		// Decode image in background
 		@Override
 		protected Bitmap doInBackground(String... urls) {
 			String urldisplay = urls[0];
 			Bitmap mIcon11 = null;
 			try {
 				InputStream in = new java.net.URL(urldisplay).openStream();
 				mIcon11 = BitmapFactory.decodeStream(in);
 			} catch (Exception e) {
 				Log.e("Error", e.getMessage());
 				e.printStackTrace();
 			}
 			if (mIcon11 != null) addBitmapToMemoryCache(urls[0], mIcon11);
 			return mIcon11;
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap bitmap) {
 			if (imageViewRef != null && bitmap != null) {
 				final ImageView imageView = imageViewRef.get();
 				if (imageView != null) {
 					imageView.setImageBitmap(bitmap);
 				}
 			}
 		}
 	}
 
 	public void loadBitmap(String url, ImageView imgV) {
 		final String imgKey = url;
 		final Bitmap bitmap = getBitmapFromMemCache(imgKey);
 		if (bitmap != null) imgV.setImageBitmap(bitmap);
 		else {
 			//			imgV.setImageResource(R.drawable.image_placeholder);
 			//TO-DO pick a placeholder for images that are downloading slowly
 			BitmapWorkerTask task = new BitmapWorkerTask(imgV);
 			task.execute(url);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main_web, menu);
 		return true;
 	}
 }
