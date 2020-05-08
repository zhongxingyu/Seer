 package se.team05.view;
 
 import se.team05.R;
 import se.team05.overlay.CheckPoint;
 import se.team05.overlay.CheckPointOverlay;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.widget.Toast;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 public class EditRouteMapView extends MapView {
 	
 	private int minTimeForLongClick = 800;
 	private long timerIfLongClick = 0;
 	private float xScreenCoordinateForLongClick;
 	private float yScreenCoordinateForLongClick;
 	private float tolerance=10;//pixels that your finger can move but still be a long press
 	private MapActivity mapActivity;
 	Drawable drawable = this.getResources().getDrawable(R.drawable.green_markerc);
     CheckPointOverlay checkPointOverlay; 
     
     
 	public EditRouteMapView(Context arg0, String arg1) {
 		super(arg0, arg1);
 		// TODO Auto-generated constructor stub
 	}
 
 	public EditRouteMapView(Context arg0, AttributeSet arg1) {
 		super(arg0, arg1);
 		// TODO Auto-generated constructor stub
 	}
 
 	public EditRouteMapView(Context arg0, AttributeSet arg1, int arg2) {
 		super(arg0, arg1, arg2);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		switch (event.getAction())
 		{
 			case MotionEvent.ACTION_DOWN:
 				timerIfLongClick=event.getEventTime();
 	            xScreenCoordinateForLongClick=event.getX();
 	            yScreenCoordinateForLongClick=event.getY();
 				break;
 			case MotionEvent.ACTION_MOVE:
                float dY = Math.abs(event.getY()-yScreenCoordinateForLongClick);
                float dX = Math.abs(event.getX()-xScreenCoordinateForLongClick);
                if(dY>tolerance || dX>tolerance){
                     timerIfLongClick=0;
                 }
 				break;
 			case MotionEvent.ACTION_UP:
 				long eventTime = event.getEventTime();
 	            long downTime = event.getDownTime(); 
 	            if (timerIfLongClick==downTime){          
 	                if ((eventTime-timerIfLongClick)>minTimeForLongClick){ 
 	                    	GeoPoint p = this.getProjection().fromPixels(
 	                                (int) event.getX(),
 	                                (int) event.getY());
 	                                Toast.makeText(mapActivity.getBaseContext(),                             
 	                                p.getLatitudeE6() / 1E6 + "," + 
 	                                p.getLongitudeE6() /1E6 ,                             
 	                                Toast.LENGTH_SHORT).show();
 	                                setCheckPoint(p);
 	       	        }
 				break;
 	            }
 		}
 		return super.onTouchEvent(event);
 		
 	}
 
 	
 	public void setMapActivity(MapActivity mapActivity) {
 		this.mapActivity = mapActivity;
 		checkPointOverlay = new CheckPointOverlay(drawable, mapActivity);
 		this.getOverlays().add(checkPointOverlay);
 	}
 	public void setCheckPoint(GeoPoint geoPoint)
 	{
 		CheckPoint checkPoint = new CheckPoint(geoPoint, "CheckPoint", 30);
         checkPointOverlay.addCheckPoint(checkPoint);
         postInvalidate();
 	}
 	
 
 }
