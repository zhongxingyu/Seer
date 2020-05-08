 package edu.pitt.designs1635.ParkIt;
 
 import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 
 public class TouchLocationOverlay extends Overlay {
 
 	private Context m_context;
 	private GeoPoint m_point;
 	
 	private TapListener m_tapListener = null;
 	
 	public TouchLocationOverlay(Context context)
 	{
 		m_context = context;
 	}
 
 	
 /*    @Override
     public boolean onTouchEvent(MotionEvent event, MapView mapView) 
     {
     	
         //---when user lifts his finger---
         if (event.getAction() == 1) {                
         	m_point = mapView.getProjection().fromPixels( (int) event.getX(),
             												 (int) event.getY());
         	
         	Toast.makeText(mapView.getContext(), "onTouchEvent " + m_point.getLatitudeE6() + ", " +
 					m_point.getLongitudeE6(),	Toast.LENGTH_LONG).show();
         }                            
         return false;
 		
     	
     }*/
     
     public boolean onTap(GeoPoint p, MapView mapView)
     {
     	
     	//m_point = mapView.getProjection().fromPixels( (int) event.getX(),
 		//		 (int) event.getY());
     	m_point = p;
     	
     	if(m_tapListener != null)
     	{
     		m_tapListener.onTap(p);
     	}
     	
     	//Toast.makeText(mapView.getContext(), "onTap " + p.getLatitudeE6() + ", " +
 		//		p.getLongitudeE6(),	Toast.LENGTH_LONG).show();
 		return true;
     	
     }
         
     public GeoPoint getGeoPoint()
     {
 		return m_point;
     }
     
     
 	/*public ParkingLocationOverlay(Context context, ParkingLocation loc)
 	{
 		m_context = context;
 		m_info = loc;	
 	}
 	
 	
 	
     @Override
     public boolean draw(Canvas canvas, 
     					MapView mapView,
     					boolean shadow, 
     					long when) 
     {
         super.draw(canvas, mapView, shadow);                   
 
         //---translate the GeoPoint to screen pixels---
         Point screenPts = new Point();
         mapView.getProjection().toPixels(m_info.getGeoPoint(), screenPts);    
 
         Bitmap bmp = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.g_icon); 
         
         canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 50, null);   
 
         return true;
         
     }*/
     
     /**
      * Interface for firing an event when the Overlay's onTap event is fired. (both displayed and erased) has changed.
      * @author Thaddeus
      *
      */
     public interface TapListener
     {
     	public abstract void onTap(GeoPoint p);
     }
     
     /**
      * Allow the TapListener to be set.
      * 
      * @param listener - the user implemented interface.
      */
     public void setTapListener(TapListener listener)
     {
     	m_tapListener = listener;
     }
 }
