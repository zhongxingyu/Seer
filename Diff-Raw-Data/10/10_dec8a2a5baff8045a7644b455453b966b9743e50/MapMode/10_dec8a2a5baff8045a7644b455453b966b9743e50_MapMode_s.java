 package second.prototype;
 
 import item.Backpack;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import sys.item.ItemSystem;
 
 import control.appearance.BackgroundMusic;
 import control.appearance.DrawableIndex;
 import control.stage.Stage;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MapMode extends Activity {
 	/** Members */
 	private Stage stage = ContainerBox.currentStage;
 	private Backpack backpack = ContainerBox.backback;
 	
 	private SensorManager manager;
 	private Sensor sensor;
 	private SensorEventListener listener;
 
 	private boolean called = false;
 
 	private MapView mapView;
 	private ListView pointListView;
 	private float myX = 0,myY = 0;
 
 	private ArrayList<HashMap<String, String>> pointList = new ArrayList<HashMap<String, String>>();
 	private SimpleAdapter adapter;
 	
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	
 	private View storyBox;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.mapmode);
 		
 		Log.e("Version","APL level = "+Build.VERSION.SDK_INT);
 		
 		mapView = (MapView) findViewById(R.id.mapView);
 		pointListView = (ListView) findViewById(R.id.listView);
 		pointListView.setBackgroundResource(DrawableIndex.LIST_BACK_GROUND);
 		
 		// set up sensors
 		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
 		ContainerBox.topManager = manager;
 		ContainerBox.topSensor = sensor;
 		
 		// set up location
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		
 		mapView.setViewCenter(this.getWindowManager().getDefaultDisplay().getWidth()*3/4
 				,this.getWindowManager().getDefaultDisplay().getHeight()*3/4);
 		
 		setTitle("Now Playing : "+stage.getName());
 	}
 	
 	/** System works */
 	@Override
 	public void onResume() {
 		super.onResume();
 		buildList();
 		
 		BackgroundMusic.play();
 		
 		// orientation sensor
 		if (sensor != null) {
 			listener = new SensorEventListener() {
 
 				@Override
 				public void onAccuracyChanged(Sensor sensor, int accuracy) {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void onSensorChanged(SensorEvent event) {
 					// TODO Auto-generated method stub
 					mapView.setOrientation(event.values[0]);
 					float para = ContainerBox.isTab?event.values[1]:event.values[2];
 					if (Math.abs(para) > 45 && !called ) {
 						// check for initial state
 						// check for repeating call (one intent allowed)
 						// check if playing (modify mode doesn't go camera)
 						called = true;
 						Intent intent = new Intent();
 						intent.setClass(MapMode.this,CameraMode.class);
 						ContainerBox.currentStage = stage;
 						stage.setInRangeList(myX, myY);
 						startActivity(intent);
 					}
 
 				}
 
 			};
 			manager.registerListener(listener, sensor,
 					SensorManager.SENSOR_DELAY_GAME);
 			called = false;
 		}
 		
 		// location manager
 		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 			locationListener = new LocationListener() {
 
 				@Override
 				public void onLocationChanged(Location location) {
 					// TODO Auto-generated method stub
 					myX = (float)location.getLongitude() - stage.getMapCenter("X");
 					myY = (float)location.getLatitude() - stage.getMapCenter("Y");
 					
 					myX = myX*ContainerBox.deg_index;
 					myY = myY*ContainerBox.deg_index;
 					
 					mapView.setCurrentLocation(myX,myY);
 				}
 
 				@Override
 				public void onProviderDisabled(String provider) {
 					// TODO Auto-generated method stub
 					Log.e("GPS something","Provider disabled");
 				}
 
 				@Override
 				public void onProviderEnabled(String provider) {
 					// TODO Auto-generated method stub
 					Log.e("GPS something","Provider enabled");
 				}
 
 				@Override
 				public void onStatusChanged(String provider, int status,
 						Bundle extras) {
 					// TODO Auto-generated method stub
 					Log.e("GPS something"," status changed");
 				}
 				
 			};
 			Log.e("GPS something"," Did requested");
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
 		} else {
 			if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 				Toast.makeText(this, "This Game Requrires GPS to Play", Toast.LENGTH_LONG);
 			}
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		BackgroundMusic.pause();
 		
 		manager.unregisterListener(listener);
 		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			locationManager.removeUpdates(locationListener);
 			
 		}
 		
 		saveList();
 		backpack.savePref();
 	}
 
 	/** Menu Control */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(1, 0, 0, "Check Up").setIcon(android.R.drawable.ic_menu_info_details);
 		menu.add(1, 2, 0, "Backpack").setIcon(android.R.drawable.ic_menu_manage);
 		menu.add(1, 3, 0, "Clear").setIcon(android.R.drawable.ic_menu_delete);
 		menu.add(1, 4, 0,"Master").setIcon(android.R.drawable.ic_menu_view);
 		if(stage.isCenterChangable()){
 			menu.add(1, 1, 0, "Set Center Point").setIcon(android.R.drawable.ic_menu_myplaces);
 		} else {
 			menu.add(1, 1, 0, "Show Center Point").setIcon(android.R.drawable.ic_menu_myplaces);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 0:
 			checkStatus();
 			break;
 		case 1:
 			if(stage.isCenterChangable()){
 				setCurrentPointCenter();
 			} else {
 				showCenter();
 			}
 			break;
 		case 2:
 			openBackpack();
 			break;
 		case 3:
 			clearProgress();
 			break;
 		case 4:
 			masterMode();
 			break;
 		default :
 		}
 		
 		return true;
 	}
 
 	
 	/** Menu operations */	
 	private void checkStatus() {
 		Toast.makeText(this, "Current Progress = "+stage.getProgress(), Toast.LENGTH_SHORT).show();
 	}
 	
 	private void openBackpack() {
 		Intent bag = new Intent();
 		bag.setClass(this, ItemSystem.class);
 		startActivity(bag);
 	}
 	
 	private void clearProgress() {
 		stage.clear();
 		backpack.clearBackpack();
 	}
 	
 	private void setCurrentPointCenter() {
 		float nowX,nowY;
 		Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if(loc!=null){
 			nowX = (float) loc.getLongitude();
 			nowY = (float) loc.getLatitude();
 		} else {
 			Log.e("GPS something","last known location not found");
 			nowX = (float) 0.0;
 			nowY = (float) 0.0;
 		}
 		
 		stage.setMapCenter(nowX, nowY);
 		mapView.setCurrentLocation(0, 0);
 	}
 	
 	private void showCenter() {
 		Uri dest = Uri.parse("geo:0,0?q="+stage.getMapCenter("Y")+","+stage.getMapCenter("X")+" (" + stage.getName() + ")");
		Intent netMap = new Intent(Intent.ACTION_WEB_SEARCH,dest);
 		startActivity(netMap);
 	}
 	
 	private void readStory(int number) {
 		LayoutInflater lf = LayoutInflater.from(this);
 		storyBox = lf.inflate(R.layout.storybox, null);
 		TextView story = (TextView) storyBox.findViewById(R.id.story);
 		story.setText(stage.getPointOf(number).getStory());
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		builder.setTitle(stage.getPointOf(number).getName());
 		builder.setIcon(android.R.drawable.ic_dialog_info);
 		builder.setView(storyBox);
 		
 		builder.setNeutralButton("OK", new DialogInterface.OnClickListener(){
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				System.gc();
 			}
 			
 		});
 		
 		builder.show();
 	}
 	
 	private void masterMode() {
 		stage.updateProgress();
 	}
 	
 	
 
 	/** Build up list */
 	private void buildList() {
 		
 		pointList.clear();
 		
 		for(int i=0;i<stage.length();i++){
 			if(stage.getPointOf(i).isVisible) {
 				HashMap<String,String> item = new HashMap<String,String>();
 				item.put("Name", stage.getPointOf(i).getName());
 				item.put("Brief", stage.getPointOf(i).getBrief());
 				pointList.add(item);
 			}
 		}
 		
 		adapter = new SimpleAdapter(this, pointList, R.layout.pointitem, new String[] {"Name","Brief"},new int[] {R.id.pointName,R.id.pointLocation});
 		pointListView.setAdapter(adapter);
 		
 		pointListView.setOnItemClickListener(new OnItemClickListener(){
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				readStory(arg2);
 			}
 			
 		});
 		
 	}
 
 	private void saveList() {
 		// store list
 		stage.commit();
 	}
 }
