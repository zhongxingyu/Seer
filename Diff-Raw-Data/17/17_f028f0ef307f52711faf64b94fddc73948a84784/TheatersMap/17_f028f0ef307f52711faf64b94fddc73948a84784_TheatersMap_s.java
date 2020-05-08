 package ua.in.leopard.androidCoocooAfisha;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Point;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.text.Html;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 public class TheatersMap extends MapActivity implements OnClickListener {
 	private MapView mapView;
 	private MyLocationOverlay me;
 	private MapController mapController;
 	private List<Overlay> mapOverlays;
 	private TheatersItemizedOverlay theatersItemizedOverlay;
 	
 	private LinearLayout theater_info_block;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.theaters_map);
 	    
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         mapView.setClickable(true);
         mapView.setEnabled(true);
         mapController = mapView.getController();
         mapOverlays = mapView.getOverlays();
         
         me = new MyLocationOverlay(this, mapView);
         mapOverlays.add(me);
         
         Bundle extras = getIntent().getExtras();
         int get_theater_id = 0;
         if(extras != null) {
         	get_theater_id = extras.getInt("theater_id", 0);
         }
         
         if (get_theater_id != 0){
         	moveToTheater(get_theater_id);
         } else {
         	moveToCityLocation();
         }
         
         
         initMapPanel();
         
         theatersItemizedOverlay = new TheatersItemizedOverlay(
         		getResources().getDrawable(R.drawable.theater_map),
         		getResources().getDrawable(R.drawable.theater_map_selected));
         
         theatersItemizedOverlay.setViews(theater_info_block, (TextView)findViewById(R.id.theaters_map_title));
         
         initMapTheaters();
         
         if(!isGpsDeviceOn()){
         	View myLocationButton = findViewById(R.id.theaters_map_my_location);
         	myLocationButton.setEnabled(false);
         	AlertDialog.Builder gpsWarningDialog = new AlertDialog.Builder(this);
             gpsWarningDialog.setTitle(getString(R.string.gps_off_warning_title));
             gpsWarningDialog.setMessage(getString(R.string.gps_off_warning));
             gpsWarningDialog.setIcon(android.R.drawable.stat_sys_warning);
             gpsWarningDialog.setNeutralButton(getString(R.string.gps_off_warning_button), new DialogInterface.OnClickListener() {
             	public void onClick(DialogInterface dialog, int which) {
             	}
             });
             gpsWarningDialog.show();
         }
 	}
 	
 	private void initMapTheaters(){
 		DatabaseHelper DatabaseHelperObject = new DatabaseHelper(this);
         List<TheaterDB> theaters = DatabaseHelperObject.getTheaters(false);
         for (int i = 0; i < theaters.size(); i++){
         	TheaterDB theater_row = theaters.get(i);
         	if (theater_row.getLatitude() != null && theater_row.getLatitude().length() != 0 &&
         			theater_row.getLongitude() != null && theater_row.getLongitude().length() != 0){
         		float latitude = Float.valueOf(theater_row.getLatitude()).floatValue();
         		float longitude = Float.valueOf(theater_row.getLongitude()).floatValue();
 	        	GeoPoint point = new GeoPoint((int)(latitude*1e6),(int)(longitude*1e6));
 	            TheaterOverlayItem overlayitem = new TheaterOverlayItem(point, theater_row.getTitle(), theater_row.getAddress());
 	            overlayitem.setTheaterObj(theater_row);
 	            theatersItemizedOverlay.addOverlay(overlayitem);
         	}
         }
         mapOverlays.add(theatersItemizedOverlay);
 	}
 	
 	private void initMapPanel(){
 		TextView current_city = (TextView)findViewById(R.id.theaters_map_city);
         current_city.setText(Html.fromHtml(getString(R.string.theaters_map_city_title) + " <b>" + EditPreferences.getCity(this) + "</b>"));
         
         theater_info_block = (LinearLayout) findViewById(R.id.theaters_map_block_info);
         View myLocationButton = findViewById(R.id.theaters_map_my_location);
         myLocationButton.setOnClickListener(this);
         View theatersLocationButton = findViewById(R.id.theaters_map_show_city_location);
         theatersLocationButton.setOnClickListener(this);
         View theatersSeancesButton = findViewById(R.id.theaters_map_show_seances);
         theatersSeancesButton.setOnClickListener(this);
         View theatersPhoneButton = findViewById(R.id.theaters_map_call_phone);
         theatersPhoneButton.setOnClickListener(this);
         View theatersHideButton = findViewById(R.id.theaters_map_close_info);
         theatersHideButton.setOnClickListener(this);
 	}
 	
 	private void moveToCityLocation(){
 		Geocoder geoCoder = new Geocoder(this);
         try {
 			List<Address> foundAdresses = geoCoder.getFromLocationName(EditPreferences.getCity(this) + getString(R.string.theaters_map_city_syfix), 1);
 			if (foundAdresses.size() > 0){
 				double lat = foundAdresses.get(0).getLatitude();
 				double lon = foundAdresses.get(0).getLongitude();
 				GeoPoint point = new GeoPoint((int)(lat*1e6),(int)(lon*1e6));
 				mapController.animateTo(point);
 				mapController.setZoom(12);
 			} else {
 				Toast.makeText(this, getString(R.string.theaters_map_city_found_error), Toast.LENGTH_LONG).show();
 			}
 		} catch (IOException e) {
 			//e.printStackTrace();
 			Toast.makeText(this, getString(R.string.theaters_map_city_found_error), Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	private void moveToTheater(int theater_id){
 		DatabaseHelper DatabaseHelperObject = new DatabaseHelper(this);
         TheaterDB theater = DatabaseHelperObject.getTheater(theater_id);
         if (theater != null){
         	if (theater.getLatitude() != null && theater.getLatitude().length() != 0 &&
         			theater.getLongitude() != null && theater.getLongitude().length() != 0){
         		float latitude = Float.valueOf(theater.getLatitude()).floatValue();
         		float longitude = Float.valueOf(theater.getLongitude()).floatValue();
 	        	GeoPoint point = new GeoPoint((int)(latitude*1e6),(int)(longitude*1e6));
 	        	mapController.animateTo(point);
 	        	mapController.setZoom(16);
         	} else {
         		moveToCityLocation();
         	}
         } else {
         	moveToCityLocation();
         }
 	}
 	
 	public GeoPoint getMyLocation(){
		/*
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location loc = lm.getLastKnownLocation("gps");
		if (loc != null){
			return new GeoPoint((int)(loc.getLatitude()*1e6),(int)(loc.getLongitude()*1e6));
 		} else {
 			return null;
 		}
		*/
		return me.getMyLocation();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		this.me.enableCompass();
 		this.me.enableMyLocation();
 	}
 	@Override
 	public void onPause() {
 		super.onPause();
 		this.me.disableMyLocation();
 		this.me.disableCompass();
 	}
 	
 	@Override
 	public void onDestroy() {
 		if (this.me != null){
 			this.me.disableMyLocation();
 			this.me.disableCompass();
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	public void onClick(View v) {
 		TheaterDB selected_theater = theatersItemizedOverlay.getSelectedTheater();
 		switch (v.getId()) {
 		  case R.id.theaters_map_my_location:
 			 //GeoPoint user_location = me.getMyLocation();
 			 GeoPoint user_location = getMyLocation();
 			 if (user_location != null){
 				 mapController.animateTo(user_location);
 			 } else {
 				 Toast.makeText(this, getString(R.string.theaters_map_me_found_error), Toast.LENGTH_LONG).show();
 			 }
 	         break;
 		  case R.id.theaters_map_show_seances:
 			  if (selected_theater != null){
 				Intent intent = new Intent(this, Theater.class);
 				Bundle bundle = new Bundle();
 				bundle.putInt("theater_id", selected_theater.getId());
 				intent.putExtras(bundle);
 				startActivity(intent);
 			  }
 		      break;
 		  case R.id.theaters_map_call_phone:
 			  if (selected_theater != null){
 				  String toDial="tel:" + selected_theater.getCallPhone();
 				  startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(toDial)));
 			  }
 		      break;
 		  case R.id.theaters_map_show_city_location:
 			  moveToCityLocation();
 		      break;
 		  case R.id.theaters_map_close_info:
 			  theater_info_block.setVisibility(View.GONE);
 		      break;
 	      }
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean supRetVal = super.onCreateOptionsMenu(menu);
 		menu.add(Menu.NONE, 0, Menu.NONE, R.string.theaters_map_zoom_in);
 		menu.add(Menu.NONE, 1, Menu.NONE, R.string.theaters_map_zoom_out);
 		menu.add(Menu.NONE, 2, Menu.NONE, R.string.theaters_map_satellit);
 		menu.add(Menu.NONE, 3, Menu.NONE, R.string.theaters_map_streets);
 		menu.add(Menu.NONE, 4, Menu.NONE, R.string.theaters_map_traffic);
 		menu.add(Menu.NONE, 5, Menu.NONE, R.string.theaters_map_hotkey_help);
 		return supRetVal;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		    case 0:
 		    	mapController.zoomIn();
 		        return true;
 		    case 1:
 		    	mapController.zoomOut();
 		        return true;
 		    case 2:
 		    	mapView.setSatellite(!mapView.isSatellite());
 		        return true;
 		    case 3:
 		    	mapView.setStreetView(!mapView.isStreetView());
 		        return true;
 		    case 4:
 		    	mapView.setTraffic(!mapView.isTraffic());
 		        return true;
 		    case 5:
 		    	startActivity(new Intent(this, AboutMap.class));
 		        return true;
 		    default:
 		        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
 	public boolean onKeyDown(int KeyCode, KeyEvent event) {
 		super.onKeyDown(KeyCode, event);
 		switch(KeyCode){
 			case KeyEvent.KEYCODE_DPAD_UP:
 				moveUp();
                 return true;
 			case KeyEvent.KEYCODE_DPAD_DOWN:
 				moveDown();
                 return true;
 			case KeyEvent.KEYCODE_DPAD_LEFT:
 				moveLeft();
                 return true;
 			case KeyEvent.KEYCODE_DPAD_RIGHT:
 				moveRight();
                 return true;
 			case KeyEvent.KEYCODE_DPAD_CENTER:
 			case KeyEvent.KEYCODE_2:
 				mapController.zoomIn();
 		        return true;
 			case KeyEvent.KEYCODE_8:
 				mapController.zoomOut();
 		        return true;
 			case KeyEvent.KEYCODE_BACK:
 				finish();
 		        return true;
 			case KeyEvent.KEYCODE_4:
 				mapView.setSatellite(!mapView.isSatellite());
 		        return true;
 			case KeyEvent.KEYCODE_5:
 				mapView.setStreetView(!mapView.isStreetView());
 		        return true;
 			case KeyEvent.KEYCODE_6:
 				mapView.setTraffic(!mapView.isTraffic());
 		        return true;
 		}
 		mapView.invalidate();
         return false;
 	}
 	
 	public boolean isGpsDeviceOn(){
         String checkGpsDevice = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
         if(checkGpsDevice.length()==0 || checkGpsDevice==null || (!checkGpsDevice.contains(LocationManager.GPS_PROVIDER) && !checkGpsDevice.contains(LocationManager.NETWORK_PROVIDER))){
                 return false;
         }
         return true;
     }
 	
 	public void moveUp(){
         GeoPoint centerGeoPoint = mapView.getMapCenter();
         Point centerPoint = new Point();
         mapView.getProjection().toPixels(centerGeoPoint, centerPoint);
         GeoPoint newCenterGeoPoint = mapView.getProjection().fromPixels(centerPoint.x, centerPoint.y - mapView.getHeight()/4);
         mapController.animateTo(newCenterGeoPoint);
 	}
 	
 	public void moveDown(){
         GeoPoint centerGeoPoint = mapView.getMapCenter();
         Point centerPoint = new Point();
         mapView.getProjection().toPixels(centerGeoPoint, centerPoint);
         GeoPoint newCenterGeoPoint = mapView.getProjection().fromPixels(centerPoint.x, centerPoint.y + mapView.getHeight()/4);
         mapController.animateTo(newCenterGeoPoint);
 	}
 	
 	public void moveLeft(){
         GeoPoint centerGeoPoint = mapView.getMapCenter();
         Point centerPoint = new Point();
         mapView.getProjection().toPixels(centerGeoPoint, centerPoint);
         GeoPoint newCenterGeoPoint = mapView.getProjection().fromPixels(centerPoint.x - mapView.getWidth()/4, centerPoint.y);
         mapController.animateTo(newCenterGeoPoint);
 	}
 	
 	public void moveRight(){
         GeoPoint centerGeoPoint = mapView.getMapCenter();
         Point centerPoint = new Point();
         mapView.getProjection().toPixels(centerGeoPoint, centerPoint);
         GeoPoint newCenterGeoPoint = mapView.getProjection().fromPixels(centerPoint.x + mapView.getHeight()/4, centerPoint.y);
         mapController.animateTo(newCenterGeoPoint);
 	}
 	
 }
