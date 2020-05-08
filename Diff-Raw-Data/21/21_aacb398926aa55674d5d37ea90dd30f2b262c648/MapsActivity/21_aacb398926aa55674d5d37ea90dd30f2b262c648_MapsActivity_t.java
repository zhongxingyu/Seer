 package pl.pwr.smartkill.activities;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import pl.pwr.smartkill.LocationService;
 import pl.pwr.smartkill.R;
 import pl.pwr.smartkill.SKApplication;
 import pl.pwr.smartkill.obj.Match;
 import pl.pwr.smartkill.obj.Position;
 import pl.pwr.smartkill.obj.Positions;
 import pl.pwr.smartkill.obj.Profile;
 import pl.pwr.smartkill.tools.DistanceCalculator;
 import pl.pwr.smartkill.tools.FixedMyLocationOverlay;
 import pl.pwr.smartkill.tools.PlayersItemizedOverlay;
 import pl.pwr.smartkill.tools.WebserviceHandler;
 import pl.pwr.smartkill.tools.httpRequests.PostRequest;
 import android.graphics.drawable.Drawable;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.androidquery.AQuery;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.App;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import com.googlecode.androidannotations.annotations.UiThread;
 
 @EActivity
 public class MapsActivity extends MapActivity {
 	@Extra
 	Match match;
 
 	@App
 	SKApplication app;
 
 	HashMap<Number, Integer> killChecker;
	HashMap<Number,Profile> profiles;
 	boolean sent=true;
 	boolean profilesFetched=false;
 	private MapView mapView;
 	private MapController mc;
 	private MyLocationOverlay myLocationOverlay;
 	public LocationManager locManager;
 	public LocationService locService;
 	public static final double PRECISION = 1000000;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_maps);
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 
 		// create an overlay that shows our current location
 		myLocationOverlay = new FixedMyLocationOverlay(this, mapView);
 		myLocationOverlay.enableMyLocation();
 		// add this overlay to the MapView and refresh it
 		mapView.getOverlays().add(myLocationOverlay);
 		mapView.postInvalidate();
 		mc = mapView.getController();
 		mc.setZoom(15);
 
 		// call convenience method that zooms map on our location
 		// zoomToMyLocation();
 		//locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		//		locService = new LocationService(this);//TOOD wyłączone , to fix, android annotations dokumentacja sie kłania
 
 		//locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2, locListener);
 
 		// mapView.getOverlays().add(new CircleOverlay(this,
 		// StrToDbl(match.getLat())/PRECISION,
 		// (StrToDbl(match.getLat())/PRECISION), 300));
 
 		myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				getData();
 				start();
 				mc.animateTo(myLocationOverlay.getMyLocation());
 			}
 
 		});
 
 	}
 
 	public static double StrToDbl(String data) {
 		return (Double.parseDouble(data));
 	}
 
 	long delay = 4 * 1000; // delay in ms : 10 * 1000 ms = 10 sec.
 	Timer timer = new Timer("TaskName");
 
 	public void start() {
 		timer.cancel();
 		timer = new Timer("TaskName");
 		TimerTask task = new TimerTask() {
 			public void run() {
 				if(sent){
 					getData();
 				}
 				mc.animateTo(myLocationOverlay.getMyLocation());
 			}
 		};
 		Date executionDate = new Date();
 		timer.schedule(task, executionDate, delay);
 	}
 
 	@AfterViews
 	public void prepare() {
 	}
 
 	@Background
 	public void getData() {
 		sent=false;
 		String lat, lng;
 		if (myLocationOverlay != null
 				&& myLocationOverlay.getMyLocation() != null) {
 			lat = ((float) (myLocationOverlay.getMyLocation().getLatitudeE6()))
 					/ 1000000 + "";
 			lng = ((float) myLocationOverlay.getMyLocation().getLongitudeE6())
 					/ 1000000 + "";
 		} else {
 			lat = match.getLat();
 			lng = match.getLng();
 		}
 		HashMap<String, String> params = new HashMap<String, String>();
 		params.put("id", app.getSessionId());
 		params.put("match", match.getId() + "");
 		params.put("lat", lat);
 		params.put("lng", lng);
 		Positions m = new WebserviceHandler<Positions>().getAndParse(this,
 				new PostRequest(SKApplication.API_URL + "position", params),
 				new Positions());
 		if(!profilesFetched&&m.getPositions()!=null){
 			for(Position p:m.getPositions()){
				profiles.put(p.getUser(),getUserProfile(p.getUser()));
 			}
 			profilesFetched=true;
 		}
 		if(m.getPositions()!=null)
 			updateData(m);
 	}
 
 	@UiThread
 	public void updateData(Positions m) {
 		sent=true;
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		Drawable victim = getResources().getDrawable(R.drawable.victim);
 		Drawable hunter = getResources().getDrawable(
 				R.drawable.ic_launcher_icon);
 		myLocationOverlay = (FixedMyLocationOverlay) mapOverlays.get(0);
 		mapOverlays.clear();
 		mapOverlays.add(myLocationOverlay);
 		DistanceCalculator dc = new DistanceCalculator();
 		String myType = "prey"; // TODO pobranie typu użytkownika
 		String lat, lng;
 		for (Position p : m.getPositions()) {
 			if (p.getLat() != null) {
 				GeoPoint point = new GeoPoint((int) (Float.parseFloat(p
 						.getLat()) * 1000000), (int) (Float.parseFloat(p
 								.getLng()) * 1000000));
 				PlayersItemizedOverlay itemizedoverlay;
 				if (p.getType().equals("hunter")) {
 					itemizedoverlay = new PlayersItemizedOverlay(hunter, this,
 							point, p.getType());// TODO - p.getType() - powinno
 					// byc getName()
 					if (myType.equals("prey")) {
 						if (dc.CalculationByDistance(point,
 								myLocationOverlay.getMyLocation()) < 5) {
 							// kill
 							if(killChecker.containsKey(p.getUser())&&killChecker.get(p.getUser())>2){
 								HashMap<String, String> params = new HashMap<String, String>();
 								params.put("id", app.getSessionId());
 								params.put("match", match.getId() + "");
 								params.put("hunter", p.getUser() + "");
 								lat = ((float) (myLocationOverlay.getMyLocation()
 										.getLatitudeE6())) / 1000000 + "";
 								lng = ((float) myLocationOverlay.getMyLocation()
 										.getLongitudeE6()) / 1000000 + "";
 								params.put("lat", lat);
 								params.put("lng", lng);
 								KillData killed = new WebserviceHandler<KillData>()
 										.getAndParse(this, new PostRequest(
 												SKApplication.API_URL + "killUser",
 												params), new KillData());
 								Log.e("killed", killed + "");
 
 							}else{
 								if(!killChecker.containsKey(p.getUser())){
 									killChecker.put(p.getUser(), 1);
 								}else{
 									killChecker.put(p.getUser(), killChecker.get(p.getUser())+1);
 								}
 							}
 
 						}
 
 					}
 				} else
 					itemizedoverlay = new PlayersItemizedOverlay(victim, this,
 							point, p.getType());
 				mapOverlays.add(itemizedoverlay);
 
 				Log.e("type", "Position: " + p.getType());
 			}
 		}
 		// TODO
 	}
 
 	@Override
 	protected void onPause() {
 		try {
 			myLocationOverlay.disableMyLocation();
 		} catch (Exception e) {
 		}
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		try {
 			myLocationOverlay.enableMyLocation();
 		} catch (Exception e) {
 		}
 		super.onResume();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public class KillData {
 		private String id;
 		private String matchId;
 
 		public String getId() {
 			return this.id;
 		}
 
 		public void setId(String id) {
 			this.id = id;
 		}
 
 		public String getMatchId() {
 			return this.matchId;
 		}
 
 		public void setMatchId(String matchId) {
 			this.matchId = matchId;
 		}
 	}
 
	public Profile getUserProfile(Number id){
 		HashMap<String, String> params = new HashMap<String, String>();
 		params.put("id", app.getSessionId());
 		params.put("user", id+"");
 		Profile m = new WebserviceHandler<Profile>().getAndParse(this,
 				new PostRequest(SKApplication.API_URL + "profile", params),
 				new Profile());
 		return m;
 	}
 //TODO
 	public void downloadAvatar(String url){
 		AQuery aq = new AQuery(getParent());
 //		aq.id(R.id.image1).image(url);//TODO gdzie R.id.image1 to ID obrazka który ma byc ustawiony, zamiast tego obiekt ImageView można dać
 	}
 }
