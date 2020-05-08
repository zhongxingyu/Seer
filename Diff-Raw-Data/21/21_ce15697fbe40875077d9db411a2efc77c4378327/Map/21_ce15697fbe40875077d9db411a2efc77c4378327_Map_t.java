 package com.Blue.Map;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.graphics.RectF;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.FloatMath;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class Map extends Activity implements OnTouchListener {
 
 	// These matrices will be used to move and zoom image
 	Matrix matrix = new Matrix();
 	Matrix savedMatrix = new Matrix();
 
 	// We can be in one of these 3 states
 	static final int NONE = 0;
 	static final int DRAG = 1;
 	static final int ZOOM = 2;
 	int mode = NONE;
 
 	// Remember some things for zooming
 	PointF start = new PointF();
 	PointF mid = new PointF();
 	float oldDist = 1f;
 
 	// Map and screen values
 	private ImageView view;
 	private float[] matrixValues = new float[9];
 	private float maxZoom;
 	private float minZoom;
 	private float mapHeight;
 	private float mapWidth;
 	private RectF viewRect;
 
 	// Values for grid
 	private float currentX;
 	private float currentY;
 	private float displayPixelsX;
 	private float displayPixelsY;
 	private float currentScale;
 	DatabaseHelper myDBHelper;
 
 	// new
 	// variables---------------------------------------------------------------------------------------------------------------------------------
 	private GPS mLocationListener;
 	private LocationManager mLocationManager;
 	double latitude;
 	double longitude;
 
 	// new code --> creates the menu
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}

 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {

 		// Handle item selection

 		switch (item.getItemId()) {
 		case R.id.menu:
 			// alot of this stuff can probably go in GPS.java
 			mLocationListener = new GPS();
 			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 			Location location = mLocationManager
 					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}
 			mLocationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
 			LatLongPixelConversion convertGPSToPixels = new LatLongPixelConversion();
 			float[] xyPixels = convertGPSToPixels.calcXYPixels(latitude,
 					longitude);
 			centerMapWithPixels(xyPixels[0], xyPixels[1]);
 			return true;
 			// add cases for more menu items
 		default:
 			return super.onOptionsItemSelected(item);
 
 		}
 
 	}
 
 	@Override
 	protected void onDestroy() {
 
 		// TODO Auto-generated method stub
 
 		super.onDestroy();
 		// Turns off GPS before the user closes the app
 		mLocationManager.removeUpdates((LocationListener) mLocationListener);
 
 	}
 
 	// end new code
 	// ------------------------------------------------------------------------------------------------
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);

 		view = (ImageView) findViewById(R.id.imageView);
 		// turn on onTouchListener to map
 		view.setOnTouchListener(this);
 
 		// Upload database from assets folder
 		// if not already uploaded to '/data/data/com.Blue.Map/databases/'
 		myDBHelper = new DatabaseHelper(this);
 		try {
 			myDBHelper.createDatabase();
 		} catch (IOException ioe) {
 			throw new Error("Unable to create database");
 		}
 		try {
 			myDBHelper.openDatabase();
 		} catch (SQLException sqle) {
 			throw sqle;
 		}
 
 		// define user interface widgets
 		AutoCompleteTextView searchAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.searchAutoCompleteTextView);
 		Spinner mainSpinner = (Spinner) findViewById(R.id.mainSpinner);
 
 		// set-up adapters for widgets to display
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mainSpinner.setAdapter(adapter);
 		searchAutoCompleteTextView.setAdapter(adapter);
 
 		// display prompt title
 		mainSpinner.setPrompt("Select Building");
 		// query for all buildings to be displayed in spinner
 		myDBHelper.getAllBuildings(adapter);
 
 		// ------------------------------------------------//
 		// The following overriding methods return latitude//
 		// and longitude value to be displayed in //
 		// a pop-up when selected from spinner. //
 		// ------------------------------------------------//
 
 		// this only works properly when all buildings are displayed in the
 		// spinner
 		// the solution to the issue is described here:
 		// http://www.outofwhatbox.com/blog/2010/11/android-autocompletetextview-sqlite-and-dependent-fields/
 		mainSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parentView,
 					View selectedItemView, int position, long id) {
 				Cursor c = myDBHelper.getGPSFromSpinner(parentView
 						.getItemAtPosition(position).toString());
 				double latValue = c.getDouble(0);
 				double lonValue = c.getDouble(1);
 
 				Toast toast = Toast.makeText(getApplicationContext(), "LAT: "
 						+ latValue + " LON: " + lonValue, Toast.LENGTH_LONG);
 				toast.setGravity(Gravity.BOTTOM, 0, 0);
 				toast.show();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parentView) {
 				// Do nothing
 			}
 
 		});
 	}
 
 	// update
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		if (hasFocus) {
 			// get map dimensions
 			mapHeight = view.getDrawable().getIntrinsicHeight();
 			mapWidth = view.getDrawable().getIntrinsicWidth();
 			// set zoom levels
 			maxZoom = 4;
 			minZoom = getMinZoom();
 			// set rectangle to screen perimeter
 			viewRect = new RectF(0, 0, view.getWidth(), view.getHeight());
 
 			setGridValues();
 			Toast toast = Toast.makeText(getApplicationContext(), "("
 					+ currentX + "," + currentY + ")", Toast.LENGTH_SHORT);
 			Toast toast2 = Toast.makeText(getApplicationContext(),
 					displayPixelsX + "," + displayPixelsY, Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.BOTTOM, 0, 0);
 			toast2.setGravity(Gravity.BOTTOM, 100, 0);
 			toast.show();
 			toast2.show();
 		}
 	}
 
 	// -------------------//
 	// Define touch events//
 	// -------------------//
 
 	@Override
 	public boolean onTouch(View v, MotionEvent rawEvent) {
 		WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
 		ImageView view = (ImageView) v;
 
 		// Handle touch events
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
 
 		// One finger touch
 		case MotionEvent.ACTION_DOWN:
 			// reset matrix
 			savedMatrix.set(matrix);
 			// get location of touch
 			start.set(event.getX(), event.getY());
 			mode = DRAG;
 			break;
 
 		// Two finger touch
 		case MotionEvent.ACTION_POINTER_DOWN:
 			oldDist = spacing(event);
 			// make sure that there are actually two fingers because it's
 			// possible to to have a misread from MotionEvent
 			if (oldDist > 10f) {
 				// reset matrix
 				savedMatrix.set(matrix);
 				// get midpoint between fingers on touch-down
 				midPoint(mid, event);
 				mode = ZOOM;
 			}
 			break;
 
 		// One finger lifted
 		case MotionEvent.ACTION_UP:
 			mode = NONE;
 			setGridValues();
 			Toast toast = Toast.makeText(getApplicationContext(), "("
 					+ currentX + "," + currentY + ")", Toast.LENGTH_SHORT);
 			Toast toast2 = Toast.makeText(getApplicationContext(),
 					displayPixelsX + "," + displayPixelsY, Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.BOTTOM, 0, 0);
 			toast2.setGravity(Gravity.BOTTOM, 100, 0);
 			toast.show();
 			toast2.show();
 			break;
 
 		// Two fingers lifted
 		case MotionEvent.ACTION_POINTER_UP:
 			mode = NONE;
 			setGridValues();
 			Toast toast3 = Toast.makeText(getApplicationContext(), "("
 					+ currentX + "," + currentY + ")", Toast.LENGTH_SHORT);
 			Toast toast4 = Toast.makeText(getApplicationContext(),
 					displayPixelsX + "," + displayPixelsY, Toast.LENGTH_SHORT);
 			toast3.setGravity(Gravity.BOTTOM, 0, 0);
 			toast4.setGravity(Gravity.BOTTOM, 100, 0);
 			toast3.show();
 			toast4.show();
 			break;
 
 		// One or two fingers moved
 		case MotionEvent.ACTION_MOVE:
 			// for drag event
 			if (mode == DRAG) {
 				matrix.set(savedMatrix);
 
 				// ------------//
 				// limit scroll//
 				// ------------//
 
 				setGridValues();
 				float currentHeight = mapHeight * currentScale;
 				float currentWidth = mapWidth * currentScale;
 				// calculate change in x and y values
 				float dx = event.getX() - start.x;
 				float dy = event.getY() - start.y;
 				// get new placement
 				float newX = currentX + dx;
 				float newY = currentY + dy;
 
 				// rectangle for new position
 				RectF drawingRect = new RectF(newX, newY, newX + currentWidth,
 						newY + currentHeight);
 				// calculate distance that drawingRect is past map limits
 				float diffUp = Math.min(viewRect.bottom - drawingRect.bottom,
 						viewRect.top - drawingRect.top);
 				float diffDown = Math.max(viewRect.bottom - drawingRect.bottom,
 						viewRect.top - drawingRect.top);
 				float diffLeft = Math.min(viewRect.left - drawingRect.left,
 						viewRect.right - drawingRect.right);
 				float diffRight = Math.max(viewRect.left - drawingRect.left,
 						viewRect.right - drawingRect.right);
 				// push map back into view
 				if (diffUp > 0) {
 					dy += diffUp;
 				}
 				if (diffDown < 0) {
 					dy += diffDown;
 				}
 				if (diffLeft > 0) {
 					dx += diffLeft;
 				}
 				if (diffRight < 0) {
 					dx += diffRight;
 				}
 				// set matrix for map to be scrolled
 				matrix.postTranslate(dx, dy);
 
 				// for zoom event
 			} else if (mode == ZOOM) {
 				// calculate new distance between fingers
 				float newDist = spacing(event);
 				// make sure that there are actually two fingers because it's
 				// possible to to have a misread from MotionEvent
 				if (newDist > 10f) {
 					// reset matrix
 					matrix.set(savedMatrix);
 					// calculate scale to be changed
 					float scale = newDist / oldDist;
 
 					matrix.getValues(matrixValues);
 					currentScale = matrixValues[Matrix.MSCALE_Y];
 
 					// ----------//
 					// limit zoom//
 					// ----------//
 
 					if (scale * currentScale > maxZoom) {
 						scale = maxZoom / currentScale;
 					} else if (scale * currentScale < minZoom) {
 						scale = minZoom / currentScale;
 					}
 					// set matrix to new scale, and center map to midpoint of
 					// fingers
 					matrix.postScale(scale, scale, mid.x, mid.y);
 				}
 			}
 			break;
 		}
 
 		view.setImageMatrix(matrix);
 		return true; // indicate event was handled
 	}
 
 	// Determine the space between the two fingers
 	private float spacing(WrapMotionEvent event) {
 		float x = event.getX(0) - event.getX(1);
 		float y = event.getY(0) - event.getY(1);
 		return FloatMath.sqrt(x * x + y * y);
 	}
 
 	// Calculate the mid point of the first two fingers
 	private void midPoint(PointF point, WrapMotionEvent event) {
 		float x = event.getX(0) + event.getX(1);
 		float y = event.getY(0) + event.getY(1);
 		point.set(x / 2, y / 2);
 	}
 
 	// return minimum zoom level to constrain boundaries
 	private float getMinZoom() {
 		// get screen orientation
 		Display getOrientation = getWindowManager().getDefaultDisplay();
 		int orientation = getOrientation.getOrientation();
 
 		if (orientation == 1) {// set zoom if portrait
 			return view.getHeight() / mapHeight;
 		} else {// set zoom if landscape
 			return view.getWidth() / mapWidth;
 		}
 	}
 
 	public void centerMapWithPixels(float x, float y) {
 
 		// calculate new top left corner
 		float newX = x + displayPixelsX / 2;
 		float newY = y + displayPixelsY / 2;
 		// gets change in x and change in y
 		float dx = newX - currentX;
 		float dy = newY - currentY;
 
 		float currentHeight = mapHeight * currentScale;
 		float currentWidth = mapWidth * currentScale;
 
 		RectF drawingRect = new RectF(newX, newY, newX + currentWidth, newY
 				+ currentHeight);
 		float diffUp = Math.min(viewRect.bottom - drawingRect.bottom,
 				viewRect.top - drawingRect.top);
 		float diffDown = Math.max(viewRect.bottom - drawingRect.bottom,
 				viewRect.top - drawingRect.top);
 		float diffLeft = Math.min(viewRect.left - drawingRect.left,
 				viewRect.right - drawingRect.right);
 		float diffRight = Math.max(viewRect.left - drawingRect.left,
 				viewRect.right - drawingRect.right);
 		// push map back into view
 		if (diffUp > 0) {
 			dy += diffUp;
 		}
 		if (diffDown < 0) {
 			dy += diffDown;
 		}
 		if (diffLeft > 0) {
 			dx += diffLeft;
 		}
 		if (diffRight < 0) {
 			dx += diffRight;
 		}
 		matrix.postTranslate(dx, dy);
 		view.setImageMatrix(matrix);
 	}
 
 	private void setGridValues() {
 		matrix.getValues(matrixValues);
 		currentY = matrixValues[Matrix.MTRANS_Y];
 		currentX = matrixValues[Matrix.MTRANS_X];
 		currentScale = matrixValues[Matrix.MSCALE_Y];
 		displayPixelsY = view.getHeight() / currentScale;
 		displayPixelsX = view.getWidth() / currentScale;
 	}
 
 }
