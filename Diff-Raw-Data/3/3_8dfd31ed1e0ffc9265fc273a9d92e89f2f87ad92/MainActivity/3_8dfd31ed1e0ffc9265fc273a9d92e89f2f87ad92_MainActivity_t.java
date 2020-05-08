 package com.gregpaton.friendfinder;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainActivity extends MapActivity {
 //public class MainActivity extends Activity {
 
 	private final int numFriends = 4;
 	
 	// layout
 	TextView _tvLocation;
 	TextView _tvFriend1;
 	TextView _tvFriend2;
 	TextView _tvFriend3;
 	TextView _tvFriend4;
 	ImageView _ivFriend1;
 	ImageView _ivFriend2;
 	ImageView _ivFriend3;
 	ImageView _ivFriend4;
 	TextView _tvFriendLoc1;
 	TextView _tvFriendLoc2;
 	TextView _tvFriendLoc3;
 	TextView _tvFriendLoc4;
 	Button _btnRefresh;
 	Button _btnExit;
 	MapView _mvFriends;
 	// friends
 	public Friend friends[] = new Friend[numFriends];
 	
 	public FriendItemizedOverlay itemizedoverlays[] = new FriendItemizedOverlay[numFriends];
 	public List<Overlay> mapOverlays;
 	
 	MapController mapCon;
 	
 	public double latitude;
 	public double longitude;
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 	    return false;
 	}	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         latitude = 0;
         longitude = 0;
         
         _tvLocation = (TextView) findViewById(R.id.tvLocation);
         _tvFriend1 = (TextView) findViewById(R.id.tvFriend1);
         _tvFriend2 = (TextView) findViewById(R.id.tvFriend2);
         _tvFriend3 = (TextView) findViewById(R.id.tvFriend3);
         _tvFriend4 = (TextView) findViewById(R.id.tvFriend4);
         _ivFriend1 = (ImageView) findViewById(R.id.ivFriend1);
         _ivFriend2 = (ImageView) findViewById(R.id.ivFriend2);
         _ivFriend3 = (ImageView) findViewById(R.id.ivFriend3);
         _ivFriend4 = (ImageView) findViewById(R.id.ivFriend4);
         _tvFriendLoc1 = (TextView) findViewById(R.id.tvFriendLoc1);
         _tvFriendLoc2 = (TextView) findViewById(R.id.tvFriendLoc2);
         _tvFriendLoc3 = (TextView) findViewById(R.id.tvFriendLoc3);
         _tvFriendLoc4 = (TextView) findViewById(R.id.tvFriendLoc4);
         _btnRefresh = (Button) findViewById(R.id.btnRefresh);
         _mvFriends = (MapView) findViewById(R.id.mapview);
         _mvFriends.setBuiltInZoomControls(true);
         mapCon = _mvFriends.getController();
 
         _btnRefresh.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 new DownloadLocationTask().execute("http://winlab.rutgers.edu/~shubhamj/locations.txt");
             }
         });
         
 
         friends[0] = new Friend("Mickey", _tvFriend1, _tvFriendLoc1, _ivFriend1, "http://winlab.rutgers.edu/~shubhamj/mickey.png");
         friends[1] = new Friend("Donald", _tvFriend2, _tvFriendLoc2, _ivFriend2, "http://winlab.rutgers.edu/~shubhamj/donald.jpg");
         friends[2] = new Friend("Goofy", _tvFriend3, _tvFriendLoc3, _ivFriend3, "http://winlab.rutgers.edu/~shubhamj/goofy.png");
         friends[3] = new Friend("Garfield", _tvFriend4, _tvFriendLoc4, _ivFriend4, "http://winlab.rutgers.edu/~shubhamj/garfield.jpg");
         
         // set up map overlay
         mapOverlays = _mvFriends.getOverlays();
         for (int i = 0; i < numFriends; ++i) {
             Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
             itemizedoverlays[i] = new FriendItemizedOverlay(drawable, this);
         }
         
         
         // download and display friend images
 		new DownloadImageTask().execute(friends[0], friends[1], friends[2], friends[3]);
 		
 		// download friend location data and update distances
         new DownloadLocationTask().execute("http://winlab.rutgers.edu/~shubhamj/locations.txt");
         
         //Acquire a reference to the system Location Manager
         LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
         // Define a listener that responds to location updates
         LocationListener locationListener = new LocationListener() {
             public void onLocationChanged(Location location) {
             	if (location != null) {
             	    setMapImages();
 	            	latitude = location.getLatitude();
 	            	longitude = location.getLongitude();
 	            	_tvLocation.setText(String.format("Lat: %.1f Long: %.1f", latitude, longitude));
             	    GeoPoint point = new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
             	    mapCon.setZoom(12);
             	    mapCon.setCenter(point);
             	    _mvFriends.invalidate();
             	}
             }
 
             public void onStatusChanged(String provider, int status, Bundle extras) {
     	    }
 
             public void onProviderEnabled(String provider) {
             }
 
             public void onProviderDisabled(String provider) {
             }
         };
         // Register the listener with the Location Manager to receive location updates
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
         
         // set up daemon to update distances every 30 seconds
         Timer locationDaemon = new Timer(true);
         locationDaemon.schedule(new updateLocationTask(), 0, 30000);
         
     }
     
     public void setMapImages() {
         for (int i = 0; i < numFriends; ++i) {
             if (friends[i].bitmap != null) {
                 //Drawable drawable = new BitmapDrawable(getResources(), friends[i].bitmap).getConstantState().newDrawable();
                 //itemizedoverlays[i] = new FriendItemizedOverlay(drawable, this);
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     // class to handle downloading friend images. Allows at most two
     // concurrent downloads
 	private class DownloadImageTask extends AsyncTask<Friend, Void, Friend> {
 		@Override
 		protected Friend doInBackground(Friend... friend) {
 			int length = friend.length;
 			// If downloading one image, process request when possible
 			if (length == 1) {
 				Friend.waitForOthers();
 				friend[0].setWorking(true);
 				friend[0].bitmap = loadImageFromNetwork(friend[0].url);
 				return friend[0];
 			}
 			// if downloading two images, create second thread and 
 			// process first request when possible
 			if (length == 2) {	
 				new DownloadImageTask().execute(friend[1]);
 				Friend.waitForOthers();
 				friend[0].setWorking(true);
 				friend[0].bitmap = loadImageFromNetwork(friend[0].url);
 				return friend[0];
 			}
 			// if downloading more than two images, create threads for 
 			// first all images
 			if (length > 2) {
 				for (int i = 0; i < length; i+=2) {
 					if (i+1 < length)
 						new DownloadImageTask().execute(friend[i], friend[i+1]);
 					else
 						new DownloadImageTask().execute(friend[i]);
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Friend friend) {
 			if (friend == null)
 				return;
 			friend.imageView.setImageBitmap(friend.bitmap);
 			friend.setWorking(false);
 		}
 	}
 
 	private Bitmap loadImageFromNetwork(String url) {
 		Bitmap bitmap = null;
 		try {
 			bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return bitmap;
 	}
 	
 	// class to download friend location data and update friend distance
 	private class DownloadLocationTask extends AsyncTask<String, Void, String> {
 		@Override
 		protected String doInBackground(String... url) {
             return loadLocationFromNetwork(url[0]);
 		}
 
 		@Override
 		protected void onPostExecute(String locations) {
 			// parse and set locations of friends
 			String[] tokens = locations.split(" ");
 			for (int i = 0; i < tokens.length; ++i) {
 				for (int j = 0; j < numFriends; ++j) {
 					if (tokens[i].equalsIgnoreCase(friends[j].name)) {
 						++i;
 						friends[j].latitude = Double.valueOf(tokens[i]);
 						++i;
 						friends[j].longitude = Double.valueOf(tokens[i]);
 						friends[j].updateDistance(latitude, longitude);
 			            GeoPoint point = new GeoPoint((int)(friends[j].latitude * 1E6), (int)(friends[j].longitude * 1E6));
 			            OverlayItem overlayitem = new OverlayItem(point, friends[j].name, String.format("Distance: %.1f", friends[j].distance));
 			            if (itemizedoverlays[j] != null) {
     			            itemizedoverlays[j].addOverlay(overlayitem);
     			            mapOverlays.add(itemizedoverlays[j]);
 			            }
 			            else
 			                Log.i("", "null: " + j);
 						break;
 					}						
 				}
 			}
 		}
 	}
 	
 
 	private String loadLocationFromNetwork(String url) {
 		String text = "";
 		try {
 			   URL textUrl = new URL(url);
 			   BufferedReader bufferReader = new BufferedReader(new InputStreamReader(textUrl.openStream()));
 			   String buffer;
 			   while ((buffer = bufferReader.readLine()) != null) {
 				   text += buffer;
 				   text += " ";
 			   }
 		}
 		catch(IOException e) {
 			e.printStackTrace(); 
 			return null;
 		}
 		return text;
 	}
 	
 	private class updateLocationTask extends TimerTask {
 		@Override
 		public void run() {
 	        new DownloadLocationTask().execute("http://winlab.rutgers.edu/~shubhamj/locations.txt");
 		}
 		
 	}
 }
 
 
