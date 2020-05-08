 package csci422.final_project;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class MiniMapActivity extends MapActivity {
 	MapView mapView;
 	MapController mapController;
 
 	int DEFAULT_ZOOM_LEVEL = 18;
 	int KAFADAR_MICRO_LAT = 39751265;
 	int KAFADAR_MICRO_LNG = -105221450;
 	int CH_MICRO_LAT = 39751702;
 	int CH_MICRO_LNG = -105222700;
 	int A_POINT_MICRO_LAT = 39751202;
 	int A_POINT_MICRO_LNG = -105220700;
 	int BROWN_MICRO_LAT = 39749784;
 	int BROWN_MICRO_LNG = -105221247;
 	
 	private static final String DEFAULT_SERVER_URL = "http://inside.mines.edu/~rolsen/";
 	private static final String REPORT_FLARE_ACTION = "cgi-bin/flareReport.cgi";
 	private static final String FLARE_LIST = "cgi-bin/flareList.cgi";
 	
 	private static final String GENERAL_ERROR = "Something went wrong.";
 	
 	class FlareOverlay extends com.google.android.maps.Overlay
 	{
 		GeoPoint point;
 		
 		public FlareOverlay(GeoPoint gp) {
 			point = gp;
 		}
 		
 		public boolean draw(Canvas canvas, MapView mapView, 
 				boolean shadow, long when)
 		{
 			if (point == null) {
 				System.out.println("Error in draw flare, GeoPoint is null");
 				return false;
 			}
 			
 			super.draw(canvas, mapView, shadow);
 
 			// Translate the GeoPoint to screen pixels
 			Point screenPts = new Point();
 			mapView.getProjection().toPixels(point, screenPts);
 
 			// Add the flare
 			Bitmap bmp = BitmapFactory.decodeResource(
 					getResources(), R.drawable.flare_t);
 			canvas.drawBitmap(bmp, screenPts.x, screenPts.y, null);
 			return true;
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_LEFT_ICON);
 		setContentView(R.layout.mini_map);
 		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_hvz);
 
 		mapView = (MapView) findViewById(R.id.mapView);  
 		mapView.setBuiltInZoomControls(true);
 		mapView.displayZoomControls(true);
 		mapView.setSatellite(true);
 
 		mapController = mapView.getController();
 
 		
 		//GeoPoint brown = new GeoPoint(BROWN_LAT,BROWN_LNG); // TODO: make this the user location
 		GeoPoint brown = geoPointFromLocation(getUserLocation());
 		
 		mapController.animateTo(brown);
 		mapController.setZoom(DEFAULT_ZOOM_LEVEL);
 		
 		displayFlares();
 
         final Button button = (Button) findViewById(R.id.flare);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 shootFlare();
             }
         });
 
         Bundle b = getIntent().getExtras();
         if ((b != null) && b.getBoolean("shootFlare")) {
         	shootFlare();
         }
         
         System.out.println("MiniMapActivity has finished onCreate");
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	public GeoPoint geoPointFromLocation(Location l) {
 		int lat = (int) (l.getLatitude() * 1e6);
 		int lng = (int) (l.getLongitude() * 1e6);
 		GeoPoint gp = new GeoPoint(lat, lng);
 		
 		// System.out.println(String.format("The lat: was %f now %d, the lng: was %f now %d", l.getLatitude(), lat, l.getLongitude(), lng));
 		return gp;
 	}
 
 	public Location getUserLocation() {
 		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		try {
 			// Acquire a reference to the system Location Manager
 			System.out.println("getUserLocation stub, setting user location to Brown Building");
 	
 			// TODO: This just sets the location for testing purposes to be in Kafadar (CSM)
 			// GPS_PROVIDER is necessary for setTestProviderLocation and getLastKnowLocation to work together
 			// Begin TEMP
 			Location userLocation = new Location(LocationManager.GPS_PROVIDER);
 			userLocation.setLatitude(BROWN_MICRO_LAT / 1e6);
 			userLocation.setLongitude(BROWN_MICRO_LNG / 1e6);
 			locationManager.setTestProviderLocation (LocationManager.GPS_PROVIDER, userLocation);
 			// End TEMP
 	
 			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		}
 		catch (IllegalArgumentException e) {
 			System.out.println("getUserLocation detects emulator instead of real device");
 			
 			Location userLocation = new Location(LocationManager.GPS_PROVIDER);
 			userLocation.setLatitude(BROWN_MICRO_LAT / 1e6);
 			userLocation.setLongitude(BROWN_MICRO_LNG / 1e6);
 			
 			return userLocation;
 		}
 	}
 	
 	public void displayFlares() {
 		List<GeoPoint> list = getFlareLocations();
 		List<Overlay> listOfOverlays = mapView.getOverlays();
 		listOfOverlays.clear();
 		
 		if (list != null) {
 			for (GeoPoint point : list) {
 				FlareOverlay mapOverlay = new FlareOverlay(point);
 				listOfOverlays.add(mapOverlay);
 			}
 			System.out.println("list is not null");
 		}
 		mapView.invalidate(); // Calls onDraw()
 	}
 	
 	// May return a null List
 	public List<GeoPoint> getFlareLocations() {
 		List<GeoPoint> list = new ArrayList<GeoPoint>();
 		
 		// Begin TEMP
 //		GeoPoint near_ch = new GeoPoint(CH_MICRO_LAT,CH_MICRO_LNG);
 //		list.add(near_ch);
 //		
 //		GeoPoint a_point = new GeoPoint(A_POINT_MICRO_LAT,A_POINT_MICRO_LNG);
 //		list.add(a_point);
 		// End TEMP
 		
 		try {
 			URL flareListURL = new URL(getFlareListURL());
 			
 			BufferedReader in = new BufferedReader(
 					new InputStreamReader(flareListURL.openStream()));
 
 			String inputLine;
			String pipeDelimiter = "\\|";
 			int lat, lng;
 			
 			inputLine = in.readLine();
 			while (inputLine != null) {
 				// inputLine = inputLine.replace("\n", "");
 				if (inputLine == "" || inputLine == " " || inputLine == "\n") {
 					System.out.println("got empty line");
 					return list = null;
 				}
 				System.out.printf("LINE: %s\n", inputLine);
 				
				String[] tokens  = inputLine.split(pipeDelimiter);
 				
				if (tokens.length < 3) {
 					return list;
 				}
 				
 				System.out.printf("line: %s, %s\n", tokens[0], tokens[1]);
 				lat = Integer.parseInt(tokens[0]);
 				lng = Integer.parseInt(tokens[1]);
 				list.add(new GeoPoint(lat, lng));
 				
 				inputLine = in.readLine();
 			}
 			
 			in.close();
 		} catch (MalformedURLException e) {
 			Toast.makeText(getApplicationContext(), GENERAL_ERROR, Toast.LENGTH_LONG).show();
 		} catch (IOException e) {
 			Toast.makeText(getApplicationContext(), GENERAL_ERROR, Toast.LENGTH_LONG).show();
 		}
 		
 		return list;
 	}
 	
 	public void shootFlare() {
 		// TODO: send Flare data to server, update screen
 		System.out.println("Placeholder flare");
 		
 		List<GeoPoint> list = getFlareLocations();
 		List<Overlay> listOfOverlays = mapView.getOverlays();
 		listOfOverlays.clear();
 		
 		for (GeoPoint point : list) {
 			FlareOverlay mapOverlay = new FlareOverlay(point);
 			listOfOverlays.add(mapOverlay);
 		}
 		
 		GeoPoint brown_point = new GeoPoint(BROWN_MICRO_LAT, BROWN_MICRO_LNG);
 		FlareOverlay mapOverlay = new FlareOverlay(brown_point);
 		listOfOverlays.add(mapOverlay);
 		
 		mapView.invalidate(); // Calls onDraw()
 		
 		Toast t = Toast.makeText(getApplicationContext(), "You have fired your flare gun!", Toast.LENGTH_LONG);
 		t.show();
 	}
 	
 	public String getFlareReportURL() {
 		return DEFAULT_SERVER_URL + REPORT_FLARE_ACTION;
 	}
 	
 	public String getFlareListURL() {
 		return DEFAULT_SERVER_URL + FLARE_LIST;
 	}
 }
