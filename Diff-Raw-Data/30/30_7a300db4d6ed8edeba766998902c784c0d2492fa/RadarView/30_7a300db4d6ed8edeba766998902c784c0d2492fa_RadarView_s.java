 package com.samsandberg.nofi;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.location.Location;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.TextView;
 
 public class RadarView extends View implements OnTouchListener {
 
 	protected final String TAG = "NoFi_RadarView";
 
 	static final float DEFAULT_REAL_RADIUS_LENGTH = 300;
 	static final float MAX_BEARING_CHANGE = 30;
 	
 	static final boolean SMOOTH_LOCATION_BY_AVERAGING = true;
 	static final int NUMBER_OF_LOCATIONS_TO_AVERAGE = 3;
 	
 	private Context context;
 	private Location myLocation;
 	private List<Location> myLocations;
 	private List<Hotspot> hotspots;
	private Paint mPaintGreen, mPaintRed, mPaintYellow;
 	private float width, height, radiusPixels, radiusMeters, myBearing, myLastBearing;
 	
 
 	public RadarView(Context context, List<Hotspot> hotspots) {
 		super(context);
 		this.context = context;
 		this.hotspots = hotspots;
 
         mPaintGreen = new Paint(Paint.ANTI_ALIAS_FLAG);
         mPaintGreen.setColor(Color.GREEN);
 
         mPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
         mPaintRed.setColor(Color.RED);
 
         mPaintYellow = new Paint(Paint.ANTI_ALIAS_FLAG);
         mPaintYellow.setColor(Color.YELLOW);
         
         setOnTouchListener(this);
         
         myLocations = new ArrayList<Location>();
         myBearing = 0;
         myLastBearing = -1;
 	}
 	
 	private void drawInit() {
         width = getWidth();
         height = getHeight();
 
         float usableWidth = width * 0.9f;
         float usableHeight = height * 0.9f;
         
         radiusPixels = Math.min(usableWidth, usableHeight) / 2;
         
         radiusMeters = DEFAULT_REAL_RADIUS_LENGTH;
         /* Or do it dynamically:
         for (Hotspot hotspot : hotspots) {
         	float dist = myLocation.distanceTo(hotspot);
         	if (dist > radiusMeters) {
         		radiusMeters = dist;
         	}
         }
         */
 
         // TODO: it IS an instance of Activity, be more elegant
         if (context instanceof Activity) {
         	Activity activity = (Activity) context;
         	
         	TextView tvRadarNumUpdates = (TextView) activity.findViewById(R.id.tv_radar_numupdates);
         	if (tvRadarNumUpdates != null) {
         		tvRadarNumUpdates.setText("Num Updates: " + myLocations.size());
         	}
         	
     		TextView tvRadarGridRadius = (TextView) activity.findViewById(R.id.tv_radar_gridradius);
     		if (tvRadarGridRadius != null) {
     			tvRadarGridRadius.setText("Grid Radius Size: " + radiusMeters + "m");
     		}
         }
 
         //Log.d(TAG, "width=" + width + " usableWidth=" + usableWidth);
         //Log.d(TAG, "height=" + height + " usableHeight=" + usableHeight);
         //Log.d(TAG, "radiusPixels=" + radiusPixels);
         //Log.d(TAG, "radiusMeters=" + radiusMeters);
 	}
 
 	private FloatPoint prepareRelativeLocation(Location fromLocation, Location toLocation) {
 		float bearing = fromLocation.bearingTo(toLocation);
 		bearing -= myBearing;
 		if (bearing < 0) {
 			bearing += 360;
 		}
 		
 		float dist = fromLocation.distanceTo(toLocation);
 		dist = radiusPixels * dist / radiusMeters;
 		
 		float x, y;
 		
 		if (bearing <= 90) {
 			double bearingRadians = Math.toRadians(bearing);
 			x = width/2 + (float) Math.sin(bearingRadians) * dist;
 			y = height/2 - ((float) Math.cos(bearingRadians) * dist);
 		} else if (bearing <= 180) {
 			bearing -= 90;
 			double bearingRadians = Math.toRadians(bearing);
 			x = width/2 + (float) Math.cos(bearingRadians) * dist;
 			y = height/2 + (float) Math.sin(bearingRadians) * dist;
 		} else if (bearing <= 270) {
 			bearing -= 180;
 			double bearingRadians = Math.toRadians(bearing);
 			x = width/2 - (float) Math.sin(bearingRadians) * dist;
 			y = height/2 + (float) Math.cos(bearingRadians) * dist;
 		} else {
 			bearing -= 270;
 			double bearingRadians = Math.toRadians(bearing);
 			x = width/2 - (float) Math.cos(bearingRadians) * dist;
 			y = height/2 - (float) Math.sin(bearingRadians) * dist;
 		}
 		
 		return new FloatPoint(x, y);
 	}
 	
 	private void drawAxes(Canvas canvas) {
 		//Log.d(TAG, "drawAxes()");
         canvas.drawLine(width/2 - radiusPixels, height/2, width/2 + radiusPixels, height/2, mPaintGreen);
         canvas.drawLine(width/2, height/2 - radiusPixels, width/2, height/2 + radiusPixels, mPaintGreen);
 	}
 	
 	private void drawMyPath(Canvas canvas) {
 		//Log.d(TAG, "drawMyPath()");
 		
 		FloatPoint oldPoint = null;
 		for (Location location : myLocations) {
 			FloatPoint newPoint = prepareRelativeLocation(myLocation, location);
 			if (oldPoint != null) {
 				canvas.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y, mPaintYellow);
 			}
 			oldPoint = newPoint;
 		}
 	}
 	
 	/* TODO: do we really need to draw ourselves?
 	private void drawMe(Canvas canvas) {
 		//Log.d(TAG, "drawMe()");
 		canvas.drawCircle(width/2, height/2, Hotspot.DEFAULT_RADIUS_LENGTH, mPaintRed);
 	}
 	*/
 	
 	private void drawNorth(Canvas canvas) {
 		//Log.d(TAG, "drawNorth()");
 		
 		// Use real north pole?
 		Hotspot northPole = new Hotspot(90, 0);
 		FloatPoint northPolePoint = prepareRelativeLocation(myLocation, northPole);
 		float x = northPolePoint.x;
 		float y = northPolePoint.y;
 		
 		/*
 		// Or true north is always up!
 		float x = width/2;
 		float y = height/2 - radiusPixels;
 		*/
 		
         canvas.drawLine(width/2, height/2, x, y, mPaintRed);
 	}
 	
 	private void drawHotspots(Canvas canvas) {
 		//Log.d(TAG, "drawHotspots()");
 		
 		for (Hotspot hotspot : hotspots) {
 			//Log.d(TAG, hotspot.toString());
 			
 			FloatPoint relativeLocation = prepareRelativeLocation(myLocation, hotspot);
 			hotspot.x = relativeLocation.x;
 			hotspot.y = relativeLocation.y;
 			hotspot.draw(canvas);
 		}
 	}
 	
 	private void drawDirectionTriangle(Canvas canvas) {
 		//Log.d(TAG, "drawDirectionTriangle()");
 		// TODO: fill in this
 	}
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         
         drawInit();
 
         drawAxes(canvas);
         drawMyPath(canvas);
         //drawMe(canvas);
         drawNorth(canvas);
         drawHotspots(canvas);
         
         if (myLocations.size() > 1) {
         	drawDirectionTriangle(canvas);
         }
     }
     
     public void updateHotspots(List<Hotspot> hotspots) {
     	this.hotspots = hotspots;
     	invalidate();
     }
     
     public void updateMyLocation(Location location) {
 		// TODO: this bearing stuff
 //    	if (myLocation != null) {
 //    		myBearing = myLocation.bearingTo(location);
 //    		if (myBearing < 0) {
 //    			myBearing += 360;
 //    		}
 //
 //    		// TODO: Soften the bearing change
 //    		if (myLastBearing != -1) {
 //    			float bearingClockwise = myLastBearing - myBearing;
 //    			if (bearingClockwise < 0) {
 //    				bearingClockwise += 360;
 //    			}
 //    			float bearingCounterClockwise = myBearing - myLastBearing;
 //    			if (bearingCounterClockwise < 0) {
 //    				bearingCounterClockwise += 360;
 //    			}
 //    		}
 //    		myLastBearing = myBearing;
 //    	}
     	
     	// Average the last few location lat/lon points to try and smooth it out
     	if (SMOOTH_LOCATION_BY_AVERAGING) {
 	    	double avgLatitude = location.getLatitude();
 	    	double avgLongitude = location.getLongitude();
 	    	int numAveraged = 1;
 	    	for (int i = myLocations.size() - 1; i >= 0 && numAveraged < NUMBER_OF_LOCATIONS_TO_AVERAGE; i--) {
 	    		avgLatitude += myLocations.get(i).getLatitude();
 	    		avgLongitude += myLocations.get(i).getLongitude();
 	    		numAveraged++;
 	    	}
 	    	location.setLatitude(avgLatitude / NUMBER_OF_LOCATIONS_TO_AVERAGE);
 	    	location.setLongitude(avgLongitude / NUMBER_OF_LOCATIONS_TO_AVERAGE);
     	}
     	
     	myLocation = location;
     	myLocations.add(location);
     	
     	invalidate();
     }
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		//Log.d(TAG, "onTouch()");
 		if (event.getAction() != MotionEvent.ACTION_DOWN) {
 			return false;
 		}
 		//Log.d(TAG, "onTouch() action is DOWN!");
 
 		for (Hotspot hotspot: hotspots) {
 			if (hotspot.clickInsideCircle(event.getX(), event.getY())) {
 				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 				alertDialogBuilder.setTitle(hotspot.ssid);
 				String message = "";
 				if (hotspot.passwordProtected) {
 					message += "Password: " + hotspot.password + "\n";
 				} else {
 					message += "Open (no password)\n";
 				}
 				
 				if (! hotspot.foursquareId.equals("")) {
 					message += "4Sq Venue Name: " + hotspot.foursquareName + "\n";
 					message += "4Sq Venue Type: " + hotspot.foursquareType + "\n";
 				}
 				
 				message += "Distance: " + myLocation.distanceTo(hotspot) + "m\n";
 				float bearing = myLocation.bearingTo(hotspot);
 				bearing -= myBearing;
 				if (bearing < 0) {
 					bearing += 360;
 				}
 				message += "Bearing: " + bearing + " degrees\n";
 				
				for (String note : hotspot.notes) {
					message += "\n" + note + "\n";
 				}
 				
 				alertDialogBuilder.setMessage(message);
 				AlertDialog alertDialog = alertDialogBuilder.create();
 				alertDialog.show();
 				return true;
 			}
 		}
 		return false;
 	}
 }
