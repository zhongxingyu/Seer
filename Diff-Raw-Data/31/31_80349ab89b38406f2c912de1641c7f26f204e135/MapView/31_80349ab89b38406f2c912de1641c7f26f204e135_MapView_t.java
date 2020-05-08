 package org.misera.android.cavenav;
 
 import java.util.ArrayList;
 
 import android.content.*;
 import android.graphics.*;
 import android.hardware.*;
 import android.os.*;
 import android.util.*;
 import android.view.*;
 
 import org.misera.android.cavenav.graph.AStar;
 import org.misera.android.cavenav.graph.Edge;
 import org.misera.android.cavenav.graph.Graph;
 import org.misera.android.cavenav.graph.Vertex;
 
 public class MapView extends View {
 	
 	private Map map;
     private Rect screen = new Rect();
     private ScaleGestureDetector mScaleDetector;
     private float mScaleFactor = 1.f;
 	private boolean scaling = false;
 	private float mLastTouchY;
 	private float mLastTouchX;
 	private float mPosX;
 	private float mPosY;
 	private float angle = 0.f;
 	private float heading = 0.f;
 	private float prevAngle = 0.f;
 	private Display mDisplay;
 
 	private RayCastRendererView rayCastRenderer;	
 	private boolean hasRayCaster = false;
 	
 	private boolean clickStepping = false;
 	private boolean showPaths = false;
 	private Graph graph;
 	private Matrix mapToScreenMatrix;
 	private Matrix screenToMapMatrix;
 	private int centerX;
 	private int centerY;
 	ArrayList<Edge> route = new ArrayList<Edge>();
 	private double routeLength = 0;
 
 	public MapView(Context context, Bitmap pic, Graph graph) {
 		super(context);
 
 		this.map = new Map(pic);
 		this.graph = graph;
 		this.mapToScreenMatrix = new Matrix();
 		this.screenToMapMatrix = new Matrix();
 		
 		screen.bottom = this.getHeight();
 		screen.right = this.getWidth();
 	    
 	    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
 		SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
         Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
 	    mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
 		
 		this.setOnClickListener(clickListener);
 	    
 	    this.setOnLongClickListener(longClickListener);
 	    
 	    WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 	    mDisplay = mWindowManager.getDefaultDisplay();
 	}  
 	
 
 	protected void onCreate(Bundle savedValues) {
 	}
 	
 	public void setRayCaster(RayCastRendererView r){
 		this.rayCastRenderer = r;
 		hasRayCaster = true;
 	}
 	
 	public void toggleClickStepping() {
 		this.clickStepping = !this.clickStepping;
 	}
 	
 	public void togglePaths() {
 		this.showPaths = !this.showPaths ;
 		invalidate();
 	}
 	
     public void clearMarkers() {
 		this.map.clearMarkers();
 		this.route.clear();
 		routeLength = 0;
 		invalidate();
     }
 
 	public void route() {
 		if (map.markers.size() > 1) {
			graph.clearReferrences();
 			AStar aStar = new AStar(graph);
 			Point p = map.markers.get(0);
 			Vertex start = graph.nearestVertex(p.x, p.y);
 			p = map.markers.get(1);
 			Vertex goal = graph.nearestVertex(p.x, p.y);
 			route = aStar.getRoute(start, goal);
 			for (Edge e : route) {
 				routeLength  += e.length;
 			}
			invalidate();
 		}
 	}
     
     private void updateMapToScreenMatrix() {
     	mapToScreenMatrix.reset();
 	    
 		screen.bottom = this.getHeight();
 		screen.right = this.getWidth();
 
     	centerX = screen.right/2;
     	centerY = screen.bottom/2;
 
 		float x = mPosX;
 		float y = mPosY;
 
 		/* 
 			Convert the point that is C in the viewport coordinate system
 			to map coordinate system and move it to (0,0)
 		*/
 		mapToScreenMatrix.setTranslate(-(centerX + x), -(centerY + y));
 		
 		/* 
 			Now rotate the map around point (0,0) which 
 			now corresponds to the viewport center
 		*/
 		mapToScreenMatrix.postRotate(angle, 0,0);		
 		
 		/* 
 			Move map back so that only the (x,y) offset remains
 		*/
 		mapToScreenMatrix.postTranslate((centerX), (centerY));
 
 		/*
 		 	Scale around the C
 		 */
 		mapToScreenMatrix.postScale(mScaleFactor, mScaleFactor, centerX, centerY);
     }
     
 	
 	@Override
     public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 	    canvas.save();
 	    
 	    updateMapToScreenMatrix();
         
         map.draw(canvas, mapToScreenMatrix, mScaleFactor);
 		
         // bullseye at center
 	    Paint paint = new Paint();
 	    paint.setColor(Color.RED);
 	    canvas.drawCircle(centerX, centerY, 1, paint);
 	    paint.setStyle(Paint.Style.STROKE);
 	    canvas.drawCircle(centerX, centerY, 3, paint);
 	    canvas.drawCircle(centerX, centerY, 5, paint);
 	    
 	    // debug overlay
 	    String debug = String.format("angle: %.2f, zoom: %.3f (x,y): (%.1f,%.1f), route length: %.1fm", angle, mScaleFactor, mPosX, mPosY, routeLength/2);
 	    if(scaling){
 			debug += "[SCALING] ";
 		}
 		canvas.drawText(debug, 10, 10, paint);
 		
 		// draw graph
 		if (showPaths) {
 			// vertices
 			paint.setColor(Color.GREEN);
 			paint.setStyle(Paint.Style.FILL_AND_STROKE);
 			for (Integer key : graph.vertices.keySet()) {
 				Vertex v = graph.vertices.get(key);
 				float[] coords = {v.x, v.y};
 				mapToScreenMatrix.mapPoints(coords);
 				canvas.drawCircle(coords[0], coords[1], mScaleFactor, paint);
 			}
 			// edges
 			for (Integer key : graph.edges.keySet()) {
 				Edge e = graph.edges.get(key);
 				Vertex startVertex = e.startVertex;
 				float[] start = {startVertex.x, startVertex.y};
 				mapToScreenMatrix.mapPoints(start);
 				Vertex endVertex = e.endVertex;
 				float[] end = {endVertex.x, endVertex.y};
 				mapToScreenMatrix.mapPoints(end);
 				canvas.drawLine(start[0], start[1], end[0], end[1], paint);
 			}
 		}
 		
 		// route
 		for (Edge e : route) {
 			paint.setColor(Color.YELLOW);
 			Vertex startVertex = e.startVertex;
 			float[] start = {startVertex.x, startVertex.y};
 			mapToScreenMatrix.mapPoints(start);
 			Vertex endVertex = e.endVertex;
 			float[] end = {endVertex.x, endVertex.y};
 			mapToScreenMatrix.mapPoints(end);
 			canvas.drawLine(start[0], start[1], end[0], end[1], paint);
 		}
 		
 		if(hasRayCaster){
 			RayCaster rayCaster = rayCastRenderer.rayCaster;
 			
 			rayCaster.playerPos[0] = (int) Math.floor(mPosX + centerX);
 			rayCaster.playerPos[1] = (int) Math.floor(mPosY + centerY);
 			rayCaster.viewingAngle =  angle + 90;
 			
 			rayCastRenderer.invalidate();
 			
 		}
 	    canvas.restore();
     }
 
 	private float[] rotateMapToScreenCoords(float angle, float xMap,float yMap){
 		
 		float x = xMap;
 		float y = yMap;
 		
 		// Convert x,y to polar coords
 
 		double r = Math.sqrt(x*x + y*y);
 		double theta = Math.atan2(y,x);
 
 		// Rotate by angle
 		double rotation = angle * Math.PI / 180;
 		theta -= rotation;
 
 		// Convert back to cartesian coords
 
 		x = (float) (r * Math.cos(theta));
 		y = (float) (r * Math.sin(theta));
 		
 		float[] returnArray = {x,y};
 		return returnArray;
 	}
 	
 	
 	private float[] screenToMapCoords(float xScreen, float yScreen) {
 		mapToScreenMatrix.invert(screenToMapMatrix);
 		float[] out = {xScreen, yScreen};
 		screenToMapMatrix.mapPoints(out);
 		return out;
 	}
 	
 	private float[] mapToScreenCoords(float xMap, float yMap) {
 		float[] out = {xMap, yMap};
 		mapToScreenMatrix.mapPoints(out);
 		return out;
 	}
 		
 	public boolean onTouchEvent(MotionEvent  ev) {
 		mScaleDetector.onTouchEvent(ev);
 		final int action = ev.getAction();
 		switch (action) {
 			case MotionEvent.ACTION_UP: {
 				if(scaling){
 					scaling = false;
 				}
 		        break;
 		    }
 		    case MotionEvent.ACTION_DOWN: {
 				if(!scaling){
 					final float x = ev.getX();
 					final float y = ev.getY();
 
 					// Remember where we started
 					mLastTouchX = x;
 					mLastTouchY = y;	
 				}
 		        break;
 		    }
 		        
 		    case MotionEvent.ACTION_MOVE: {
 				if(!scaling){
 					final float x = ev.getX();
 					final float y = ev.getY();
 					
 					// Calculate the distance moved
 					final float dx = (x - mLastTouchX);
 					final float dy = (y - mLastTouchY);
 
 					float[] mapCoords = rotateMapToScreenCoords(angle, dx, dy);
 					
 					// Move the object
 					mPosX -= mapCoords[0] / mScaleFactor;
 					mPosY -= mapCoords[1] / mScaleFactor;
 
 					// Remember this touch position for the next move event
 					mLastTouchX = x;
 					mLastTouchY = y;
 					
 					// Invalidate to request a redraw
 					invalidate();	
 				}
 				break;
 		        
 		    }
 	    }
 		return super.onTouchEvent(ev);
 	}
 	
 	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
 	    @Override
 	    public boolean onScale(ScaleGestureDetector detector) {
 			scaling = true;
 	        mScaleFactor *= detector.getScaleFactor();
 
 	        // Don't let the object get too small or too large.
 	        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
 
 	        invalidate();
 	        return true;
 	    }
 	}
 	
 	private final SensorEventListener mListener = new SensorEventListener() {
         public void onSensorChanged(SensorEvent event) {
         	float _heading = event.values[0];
         	// make the heading compatible to the transform matrix:
         	// 1. invert sign of heading to turn the other way
         	// 2. take into account screen orientation
         	float angleNew = -_heading - 90*mDisplay.getOrientation();
         	// to smooth the rotation, only rotate if angle changes significantly
         	if (Math.abs(angle - angleNew) > 0.3) {
         		angle = angleNew;
         		heading = _heading;
                 invalidate();
         	}
         }
 
         public void onAccuracyChanged(Sensor sensor, int accuracy) {
         }
     };
     
     
     private OnLongClickListener longClickListener = new OnLongClickListener() {
         public boolean onLongClick(View v) {
         	if (map.markers.size() > 1) {
         		return true;
         	}
 			float[] coords = screenToMapCoords(mLastTouchX, mLastTouchY);
         	Vertex vertex = graph.nearestVertex((int)coords[0], (int)coords[1]);
     		//map.addMarker((int)coords[0], (int)coords[1]);
         	if (vertex != null) {
         		map.addMarker(vertex.x, vertex.y);
         		invalidate();
         	}
 			return false;
         }
     };
     	
 	private OnClickListener clickListener = new OnClickListener(){
 		public void onClick(View v){
 			if (clickStepping) {
 				float stepLength = 0.75f;
 				float pixelLength = 0.5f;
 				
 				float dx = 0;
 				float dy = -(stepLength / pixelLength);
 				
 				float[] transformed = rotateMapToScreenCoords(angle, dx,dy);
 				mPosX += transformed[0];
 				mPosY += transformed[1];
 				
 				map.addMarker((int) (mPosX + centerX), (int) (mPosY + centerY));
 				//mPosX -= dx;
 				Log.i("clickAdvance", "PosX = " + mPosX);
 				invalidate();
 			}
 			//return false;
 		}
 	};
 
 }
