 package com.formicite;
 
 
 import android.view.SurfaceView;
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
 import com.google.android.maps.GeoPoint;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 import android.widget.RadioGroup;
 import android.location.LocationManager;
 import android.widget.Spinner;
 import android.hardware.Camera;
 import java.io.FileOutputStream;
 
 public class RobomagelleanActivity extends MapActivity implements GPS.GPSListener, CameraPreview.Callback {
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
 	private TextView satilliteView;
 	private ListView courseList;
 	private RadioGroup courseAction;
 	private Course course;
 	private boolean alive;
 	private SerialInterface serial;
 	private CourseAction currentAction;
 	private GPS gps;
 	private CameraPreview camera;
 	private Bitmap image;
 	
 	public void startStartPointCapture(View view) {
 		GPS gps = this.gps;
 		if (gps != null) {
 			gps.startStartPointCapture();
 		}
 	}
 	
 	public void onProcessedImage(Bitmap i) {
 		Log.i("RobomagelleanActivity", "Got image from camera: " + i);
 		this.image = i;
 	}
 	
 	public void captureStart(View view) {
 		if (addPoint(CoursePoint.Type.START, true, 0)) {
 			gps.startStartPointCapture();
 		} else {
 			//FIXME: error
 		}
 	}
 	
 	public void captureCone(View view) {
 		addPoint(CoursePoint.Type.CONE, false, -1); //FIXME: error
 	}
 	
 	public void captureWaypoint(View view) {
 		addPoint(CoursePoint.Type.WAYPOINT, false, -1); //FIXME: error
 	}
 	
 	private boolean addPoint(CoursePoint.Type type, boolean removeStartPoint, int addr) {
 		GPS gps = this.gps;
 		if (gps == null) {
 			return false;
 		}
 		if (gps.getLocation() == null) {
 			return false;
 		}
 		CoursePoint g = new CoursePoint(type,
 			new GeoPoint(gps.getLocation().getLatitudeE6(), 
 						 gps.getLocation().getLongitudeE6()));
 		if (removeStartPoint) {
 			while (course.getStartPoint() != null) {
 				course.remove(course.getStartPoint());
 			}
 		}
 		if (addr > 0) {
 			course.add(addr, g);
 		} else {
 			course.add(g);
 		}
 		saveCourse();
 		return true;
 	}
 	
 	private void updateGPSStart() {
 		GPS gps = this.gps;
 		if (course.getStartPoint() != null) { 
 			gps.setCourseStartPoint(course.getStartPoint().getLocation());
 		} else {
 			gps.setCourseStartPoint(null);
 		}
 	}
 	
 	public void saveCourse() {
 		course.save(currentCourse);
 		updateGPSStart();
 	}
 	
 	public void loadCourse() {
 		course.load(currentCourse);
 		updateGPSStart();
 	}
 	
 	public void selectPreviousPoint(View view) {
 		course.setSelectedPoint(course.getPreviousPoint(course.getSelectedPoint()));
 		saveCourse();
 	}
 	public void selectNextPoint(View view) {
 		course.setSelectedPoint(course.getNextPoint(course.getSelectedPoint()));
 		saveCourse();
 	}
 	
 	public void saveCameraPicture(View view) {
 		RobomagellanController controller = this.controller;
 		if (controller instanceof ScreenController) {
 			((ScreenController)controller).savePicture();
 		}
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
 	
 	public void onGpsStatusChanged(int st, int max) {
 		String alert = "";
 		if (gps.getStartPoint() == null || gps.getStartPointCountdown() != gps.getStartPointMax()) {
 			alert = alert + "no run start point (" + gps.getStartPointCountdown() + "/" + gps.getStartPointMax() + ")";
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
 		locationOverlay.setLocation(gps.getLocation());
 		if (controller != null) {
 			controller.setGPS(gps.getLocation());
 		}
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
 		gps = new GPS((LocationManager)getSystemService(Context.LOCATION_SERVICE));
 		gps.addListener(this);
 		updateGPSStart();
 		
 		camera = new CameraPreview((SurfaceView)findViewById(R.id.camera_view));
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		//Start propbridge
 		alive = true;
 		serial = new SerialInterface();
 		serial.start();
 		new Thread(new ComThread()).start();
 		courseChooser.setVisibility(courseChooser.VISIBLE);
 		
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		serial.halt();
 		alive = false;
 		if (controller != null) {
 			controller.terminate();
 		}
 		gps.halt();
 		hideAll();
 		camera.stop();
 	}
 	
 	private class ComThread implements Runnable {
 		public void run() {
 			int newMotor = RobomagellanController.MOTOR_ZERO;
 			int newSteering = RobomagellanController.STEERING_ZERO;
 			boolean newPowerDraw = false;
 			while (alive) {
 				try {
 					Thread.sleep(10L);
 				} catch (Exception ex) {
 					alive = false;
 					break;
 				}
 				final RobomagellanController controller = RobomagelleanActivity.this.controller;
 				if (controller != null) {
 					newMotor = controller.getMotor();
 					newSteering = controller.getSteering();
 					newPowerDraw = controller.getPowerDraw();
 					controller.setSonar(serial.getSonar());
 					String err = "";
 					if (!serial.serialAlive()) {
 						err = "<Serial Error>\n";
 					}
 					if (gps != null) {
 						if (gps.getLocation() != null) {
 							err += "GPS: " + gps.getLocation().getLatitudeE6() +
 									" " + gps.getLocation().getLongitudeE6() + "\nSonar:";
 						} else {
 							err += "GPS: no location\nSonar:";
 						}
 					} else {
 						err += "GPS: none\nSonar:";
 					}
 					if (serial.getSonar() != null) {
 						for (int i : serial.getSonar()) {
 							err += " " + i;
 						}
 						err += "\n";
 					} else {
 						err += " none\n";
 					}
 					err += "Motor: " + serial.getMotor() + "\n";
 					err += "Steering: " + serial.getSteering() + "\n";
 					err += "Power: " + serial.getPower() + "\n";
 					
 					final String data = err + controller.getMessage();
 					RobomagelleanActivity.this.runOnUiThread(new Runnable() {
 						public void run() {
 							((TextView)findViewById(R.id.messages)).setText(data); }});
 					
 					final Bitmap image = RobomagelleanActivity.this.image;
 					if (image != null) {
 						Log.i("RobomagelleanActivity", "Got image");
 						RobomagelleanActivity.this.image = null;
 						final VisionAlgo algo = controller.getVisionAlgorithm();
 						if (algo != null) {
 							controller.setVisionResults(algo.process(image));
 							(new Thread(new Runnable() {
 								public void run() {
 									
 								}})).start();
 						} else {
 							Log.e("RobomagelleanActivity", "Null vision algorithm");
 						}
 					}
 					if (controller.pictureRequested()) {
 						RobomagelleanActivity.this.image = null;
 						camera.takePicture();
 					}
 				}
 				if (newMotor != serial.getMotor()) {
 					serial.setMotor(newMotor);
 				}
 				if (newSteering != serial.getSteering()) {
 					serial.setSteering(newSteering);
 				}
 				if (newPowerDraw != serial.getPower()) {
 					serial.setPowerDraw(newPowerDraw);
 				}
 			}
 		}
 	}
 	
 	class ScreenController extends RobomagellanController {
 		class FileSaverVisionAlgo extends VisionAlgo {
 			File directory;
 			int number;
 			public FileSaverVisionAlgo() {
 				File root = Environment.getExternalStorageDirectory();
 				directory = new File(root + "/robomagellan/images");
 				directory.mkdirs();
 				
 				final File files[] = directory.listFiles();
 				number = 0;
 				for (File f : files) {
					String[] name = f.getName().split(".");
 					if (name.length > 0) {
 						String base = name[0];
 						int c = new Integer(base).intValue();
 						if (c > number) {
 							number = c;
 						}
 					}
 				}
 				number++;
 			}
 			
 			public int getImageNumber() {
 				return number;
 			}
 		
 			@Override public Result[] process(Bitmap image) {
 				try {
 					String filename = directory + "/" + number + ".png";
 					Log.i("RobomagelleanActivity", "Saving: " + filename);
 					FileOutputStream out = new FileOutputStream(filename);
 					image.compress(Bitmap.CompressFormat.PNG, 100, out);
 					out.close();
 					number++;
 				} catch (Exception e) {
 					Log.e("RobomagelleanActivity", "Error saving image");
 					e.printStackTrace();
 				}
 				return new Result[0];
 			}
 
 		}
 		private FileSaverVisionAlgo visionAlgo;
 		public ScreenController(Course course) {
 			super(course);
 			setVisionAlgorithm(new FileSaverVisionAlgo());
 			Spinner motor = (Spinner)findViewById(R.id.motor_speed);
 			final Integer speeds[] = new Integer[(SERVO_MAX - SERVO_MIN) / 1000 + 1];
 			speeds[0] = SERVO_MIN;
 			int mZeroPos = 0;
 			for (int i = 1; i < speeds.length; i++) {
 				speeds[i] = speeds[i - 1] + 1000;
 				if (speeds[i - 1] < MOTOR_ZERO && speeds[i] > MOTOR_ZERO) {
 					speeds[i] = MOTOR_ZERO;
 					mZeroPos = i;
 					speeds[i + 1] = speeds[i];
 					i++;
 				}
 				if (speeds[i] == MOTOR_ZERO) {
 					mZeroPos = i;
 				}
 			}
 			motor.setAdapter(new ArrayAdapter<Integer>(RobomagelleanActivity.this, android.R.layout.simple_spinner_item, speeds));
 			motor.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
 					@Override public void onNothingSelected(AdapterView<?> parent) {
 						ScreenController.this.setMotor(MOTOR_ZERO);
 					}
 					@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 						ScreenController.this.setMotor(speeds[position]);
 					}
 				});
 			motor.setSelection(mZeroPos);
 			
 			Spinner steering = (Spinner)findViewById(R.id.steering_position);
 			final Integer positions[] = new Integer[(SERVO_MAX - SERVO_MIN) / 1000 + 1];
 			int sZeroPos = 0;
 			positions[0] = SERVO_MIN;
 			for (int i = 1; i < positions.length; i++) {
 				positions[i] = positions[i - 1] + 1000;
 				if (positions[i - 1] < STEERING_ZERO && positions[i] > STEERING_ZERO) {
 					positions[i] = STEERING_ZERO;
 					sZeroPos = i;
 					positions[i + 1] = positions[i];
 					i++;
 				}
 				if (positions[i] == STEERING_ZERO) {
 					sZeroPos = i;
 				}
 			}
 			steering.setAdapter(new ArrayAdapter<Integer>(RobomagelleanActivity.this, android.R.layout.simple_spinner_item, positions));
 			steering.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
 					@Override public void onNothingSelected(AdapterView<?> parent) {
 						ScreenController.this.setSteering(STEERING_ZERO);
 					}
 					@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 						ScreenController.this.setSteering(positions[position]);
 					}
 				});
 			steering.setSelection(sZeroPos);
 			
 			String settings[] = {"Off", "On"};
 			Spinner power_draw = (Spinner)findViewById(R.id.power_draw);
 			power_draw.setAdapter(new ArrayAdapter<String>(RobomagelleanActivity.this, android.R.layout.simple_spinner_item, settings));
 			power_draw.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
 					@Override public void onNothingSelected(AdapterView<?> parent) {
 						ScreenController.this.deactivatePowerDraw();
 					}
 					@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 						if (position != 0) {
 							ScreenController.this.activatePowerDraw();
 						} else {
 							ScreenController.this.deactivatePowerDraw();
 						}
 					}
 				});
 			power_draw.setSelection(0);
 		}
 		
 		public void savePicture() {
 			requestPicture();
 		}
 				
 		public void run() {
 			while (!isTerminated()) {
 				String data = "";
 				data += "\nMotor: " + getMotor();
 				data += "\nSteering: " + getSteering();
 				data += "\nImage: " + ((FileSaverVisionAlgo)getVisionAlgorithm()).getImageNumber();
 				setMessage(data);
 				sleep(10);
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
 					gps.startStartPointCapture();
 					camera.start();
 					camera.addCallback(RobomagelleanActivity.this);
 					controller = new ScreenController(course);
 					controller.start();
 				}
 			}
 		});
 
 	}
 }
 
 
 
 
