 package com.formicite;
 
 import java.lang.Thread;
 import java.lang.Runnable;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Environment;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import java.io.File;
 import java.io.IOException;
 import android.view.View;
 import android.util.Log;
 import java.util.ArrayList;
 import android.widget.TextView;
 import android.widget.ListView;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MapActivity;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 import com.google.android.maps.GeoPoint;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.GpsStatus;
 import android.location.GpsSatellite;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 import android.widget.RadioGroup;
 
 public class RobomagelleanActivity extends MapActivity implements GpsStatus.Listener {
 	enum CourseAction {
 		EDIT, DELETE, RUN
 	};
 	
 	private CourseOverlay locationOverlay;
 	private RobomagellanController controller;
 	private File courseDirectory;
 	private File currentCourse;
 	private LinearLayout courseChooser;
 	private LinearLayout courseEditor;
 	private MapView mapView;
 	private GeoPoint myGeoPoint;
 	private GeoPoint myStartPoint;
 	private long startLat, startLong;
 	private int startPointCountdown;
 	private int startPointN;
 	private TextView satilliteView;
 	private ListView courseList;
 	private RadioGroup courseAction;
 	private Course course;
 	private boolean alive;
 	private SerialInterface serial;
 	private CourseAction currentAction;
 	
 	
 	public void startStartPointCapture(View view) {
 		startStartPointCapture();
 	}
 	private void startStartPointCapture() {
 		myStartPoint = null;
 		startLat = 0;
 		startLong = 0;
 		startPointCountdown = 3;
 		startPointN = 0;
 		onGpsStatusChanged(0);
 	}
 	
 	public void captureStart(View view) {
 		if (myGeoPoint == null) {
 			//FIXME: error
 			return;
 		}
 		CoursePoint g = new CoursePoint(CoursePoint.Type.START,
 			new GeoPoint(myGeoPoint.getLatitudeE6(), myGeoPoint.getLongitudeE6()));
 		while (course.getStartPoint() != null) {
 			course.remove(course.getStartPoint());
 		}
 		course.add(0, g);
 		startStartPointCapture();
 		saveCourse();
 	}
 	
 	public void captureCone(View view) {
 		if (myGeoPoint == null) {
 			//FIXME: error
 			return;
 		}
 		course.add(new CoursePoint(CoursePoint.Type.CONE, 
 			new GeoPoint(myGeoPoint.getLatitudeE6(),
 				     myGeoPoint.getLongitudeE6())));
 		saveCourse();
 	}
 	
 	public void captureWaypoint(View view) {
 		if (myGeoPoint == null) {
 			//FIXME: error
 			return;
 		}
 		course.add(new CoursePoint(CoursePoint.Type.WAYPOINT, 
 			new GeoPoint(myGeoPoint.getLatitudeE6(),
 				     myGeoPoint.getLongitudeE6())));
 		saveCourse();
 	}
 	
 	public void saveCourse() {
 		course.save(currentCourse);
 	}
 	
 	public void loadCourse() {
 		course.load(currentCourse);
 	}
 	
 	public void selectPreviousPoint(View view) {
 		course.setSelectedPoint(course.getPreviousPoint(course.getSelectedPoint()));
 		saveCourse();
 	}
 	public void selectNextPoint(View view) {
 		course.setSelectedPoint(course.getNextPoint(course.getSelectedPoint()));
 		saveCourse();
 	}
 	
 	public void moveSelectUp(View view) {
 		if (course.getSelectedPoint() != null) {
 			course.getSelectedPoint().move(1, 0);
 			saveCourse();
 		}
 	}
 	public void moveSelectDown(View view) {
 		if (course.getSelectedPoint() != null) {
 			course.getSelectedPoint().move(-1, 0);
 			saveCourse();
 		}
 	}
 	public void moveSelectLeft(View view) {
 		if (course.getSelectedPoint() != null) {
 			course.getSelectedPoint().move(0, -1);
 			saveCourse();
 		}
 	}
 	public void moveSelectRight(View view) {
 		if (course.getSelectedPoint() != null) {
 			course.getSelectedPoint().move(0, 1);
 			saveCourse();
 		}
 	}
 	
 	public void moveSelectBackward(View view) {
 		if (course.getSelectedPoint() != null) {
 			course.reorderCoursePoint(course.getSelectedPoint(), -1);
 			saveCourse();
 		}
 	}
 	public void moveSelectForward(View view) {
 		if (course.getSelectedPoint() != null) {
 			course.reorderCoursePoint(course.getSelectedPoint(), 1);
 			saveCourse();
 		}
 	}
 	
 	public void onGpsStatusChanged(int event) {
 		GpsStatus stat = ((LocationManager)getSystemService(Context.LOCATION_SERVICE)).getGpsStatus(null);
 		int st = 0, max = 0;
 		for (GpsSatellite s : stat.getSatellites()) {
 			if (s.usedInFix()) {
 				st++;
 			}
 			max++;
 		}
 		
 		String alert = "";
 		if (myStartPoint == null || startPointCountdown != 0) {
 			alert = alert + "no run start point (" + (startPointCountdown) + "/" + (startPointCountdown + startPointN) + ")";
 		} else {
 			alert = alert + "run start point";
 		}
 		alert = alert + ", ";
 		if (course.getStartPoint() == null) {
 			alert = alert + "no course start point";
 		} else {
 			alert = alert + "course start point";
 		}
 		satilliteView.setText(st + "/" + max + " satillites, " + alert);
 	}
 	
 	private class MyLocationListener implements LocationListener {
 		public void onLocationChanged(Location argLocation) {
 			GeoPoint gp = new GeoPoint(
 				(int)(argLocation.getLatitude()*1000000),
 				(int)(argLocation.getLongitude()*1000000));
 			
 			if (startPointCountdown != 0) {
 				startLat += gp.getLatitudeE6();
 				startLong += gp.getLongitudeE6();
 				Log.i("MyLocationListener", "Me: " + gp.getLatitudeE6() + "," + gp.getLongitudeE6()
 						+ " My Start: " + startLat + "," + startLong + " " + startPointN);
 				startPointN++;
 				startPointCountdown--;
 				if (startPointCountdown == 0) {
 					myStartPoint = new GeoPoint((int)(startLat / startPointN),
 						                    (int)(startLong / startPointN));
 				}
 				onGpsStatusChanged(0);
 			}
 			
 			if (myStartPoint != null && course.getStartPoint() != null && startPointCountdown == 0) { //Do subtraction
 				Log.i("MyLocationListener", "Me: " + gp.getLatitudeE6() + "," + gp.getLongitudeE6()
 						+ " My Start: " + myStartPoint.getLatitudeE6() + "," + myStartPoint.getLongitudeE6()
 						+ " Course: " + course.getStartPoint().getLocation().getLatitudeE6() 
 						+ "," + course.getStartPoint().getLocation().getLongitudeE6());
 				myGeoPoint = new GeoPoint(gp.getLatitudeE6() - myStartPoint.getLatitudeE6() 
 											+ course.getStartPoint().getLocation().getLatitudeE6(),
 										  gp.getLongitudeE6() - myStartPoint.getLongitudeE6() 
 											+ course.getStartPoint().getLocation().getLongitudeE6());
 			} else {
 				myGeoPoint = gp;
 			}
 			
 			locationOverlay.setLocation(myGeoPoint);
 			if (controller != null) {
 				controller.setGPS(myGeoPoint);
 			}
 			//Log.i("MyLocationListener", "Lat: " + argLocation.getLatitude() + " Long: " + argLocation.getLongitude());
 		}
 		public void onProviderDisabled(String provider) {}
 		public void onProviderEnabled(String provider) {}
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 	}
 	
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		courseChooser = (LinearLayout)findViewById(R.id.course_chooser);
 		courseEditor = (LinearLayout)findViewById(R.id.course_editor);
 		satilliteView = (TextView)findViewById(R.id.satillites_count);
 		courseList = (ListView)findViewById(R.id.course_list);
 		courseAction = (RadioGroup)findViewById(R.id.course_action);
 		courseAction.setOnCheckedChangeListener(
 			new RadioGroup.OnCheckedChangeListener() {
 				public void onCheckedChanged(RadioGroup group, int checkedId) {
 					if (checkedId == R.id.radio_edit) { currentAction = CourseAction.EDIT; }
 					if (checkedId == R.id.radio_delete) { currentAction = CourseAction.DELETE; }
 					if (checkedId == R.id.radio_run) { currentAction = CourseAction.RUN; }
 					Log.i("RobomagelleanActivity", "Clicked: " + checkedId + " " + currentAction);
 				}});
 		
 		
 		
 		///Load course
 		course = new Course();
 		//FIXME: be smart about bad SD cards
 		File root = Environment.getExternalStorageDirectory();
 		courseDirectory = new File(root + "/robomagellan/courses");
 		courseDirectory.mkdirs();
 		currentCourse = null;
 		listCourses();
 		
 		//Create map view.
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);	
 		mapView.setSatellite(true);
 		locationOverlay = new CourseOverlay(this, course);
 		mapView.getOverlays().add(locationOverlay);
 		
 		//Change display
 		hideAll();
 		courseChooser.setVisibility(courseChooser.VISIBLE);
 		
 		//Start GPS
 		startStartPointCapture();
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		MyLocationListener ll = new MyLocationListener();
 		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
 		lm.addGpsStatusListener(this); 
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		//Start propbridge
 		alive = true;
 		serial = new SerialInterface();
 		serial.start();
 		new Thread(new ComThread()).start();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		serial.halt();
 		alive = false;
 		if (controller != null) {
 			controller.terminate();
 		}
 		hideAll();
 	}
 	
 	private class ComThread implements Runnable {
 		public void run() {
 			int motor = 1, steering = 1;
 			boolean powerDraw = true;
 			
 			//Values between 1000, -1000.
 			int newMotor = 0, newSteering = 0;
 			boolean newPowerDraw = false;
 			while (alive) {
 				try {
 					Thread.sleep(10L);
 				} catch (Exception ex) {
 					alive = false;
 					break;
 				}
 				if (controller != null) {
 					newMotor = controller.getMotor();
 					newSteering = controller.getSteering();
 					newPowerDraw = controller.getPowerDraw();
 					controller.setSonar(serial.getSonar());
 				}
 				if (newMotor != motor) {
 					serial.setMotor(newMotor);
 				}
 				if (newSteering != steering) {
 					serial.setSteering(newSteering);
 				}
 				if (newPowerDraw != powerDraw) {
 					serial.setPowerDraw(newPowerDraw);
 				}
 			}
 		}
 	}
 	
 	private void hideAll() {
 		courseChooser.setVisibility(courseChooser.GONE);
 		courseEditor.setVisibility(courseEditor.GONE);
 	}
 	
 	private void startNewCourse(String name) {
 		currentCourse = new File(courseDirectory + "/" + name);
 		hideAll();
 		courseEditor.setVisibility(courseEditor.VISIBLE);
 	}
 	
 	public void newCourse(View view) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
 		alert.setTitle("Create New Course");  
 		alert.setMessage("Enter Name:");
 		// Set an EditText view to get user input   
 		final EditText input = new EditText(this); 
 		alert.setView(input);
 		
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
 				public void onClick(DialogInterface dialog, int whichButton) {  
 					String value = input.getText().toString();
 					//FIXME: overwrite
 					startNewCourse(value);
 				}});
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 				}});
 		alert.show();
 	}
 	
 	private class FileWrap {
 		private File file;
 		public FileWrap(File f) {
 			file = f;
 		}
 		public File getFile() { return file; }
 		public String toString() {
 			return file.getName();
 		}
 	}
 	
 	private void listCourses() {
 		final File f[] = courseDirectory.listFiles();
 		final FileWrap s[] = new FileWrap[f.length];
 		for (int i = 0; i < f.length; i++) {
 			s[i] = new FileWrap(f[i]);
 		}
 		courseList.setAdapter(new ArrayAdapter<FileWrap>(this, android.R.layout.simple_list_item_1, s));
 		courseList.setOnItemClickListener(new ListView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				//Do whatever on file s[i].
 				currentCourse = s[position].getFile();
 				if (currentAction == CourseAction.EDIT) {
 					hideAll();
 					currentCourse = f[position];
 					loadCourse();
 					courseEditor.setVisibility(courseEditor.VISIBLE);
 				} else if (currentAction == CourseAction.DELETE) {
 					
 				} else if (currentAction == CourseAction.RUN) {
 					hideAll();
 					currentCourse = f[position];
 					loadCourse();
 					//controller = new RobomagellanController(course);
 				}
 			}
 		});
 
 	}
 }
 
 
 
 
