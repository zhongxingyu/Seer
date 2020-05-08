 package dk.bigherman.android.hello;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class HelloItemizedOverlay extends ItemizedOverlay<OverlayItem>
 {
 	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 	private Context mContext;
 	public HelloItemizedOverlay(Drawable defaultMarker) 
 	{
 		super(boundCenterBottom(defaultMarker));
 		populate();
 	}
 	
 	public HelloItemizedOverlay(Drawable defaultMarker, Context context) 
 	{
 		  super(boundCenterBottom(defaultMarker));
 		  mContext = context;
 		  populate();
 	}
 	
 	public void addOverlay(OverlayItem overlay) 
 	{
 	    mOverlays.add(overlay);
 	    this.populate();
 	}
 	
 	@Override
 	protected OverlayItem createItem(int i) 
 	{
 	  return mOverlays.get(i);
 	}
 	
 	@Override
 	public int size() 
 	{
 	  return mOverlays.size();
 	}
 	
 	@Override
 	protected boolean onTap(int index) 
 	{
 	  
 		System.out.println(index);
 		OverlayItem item = mOverlays.get(index);
 		//System.out.println(item);
 		//System.out.println(mContext);
 		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
 		
 		//System.out.println(item.getTitle());
 		//System.out.println(item.getSnippet());
 		dialog.setTitle(item.getTitle());
 		dialog.setMessage(item.getSnippet());
 		
 		//dialog.setTitle("This is a test!");
 		dialog.show();
 		
 		return true;
 	}
 	
     @SuppressWarnings("unchecked")
    @Override
 	public boolean onTouchEvent(MotionEvent event, MapView mapView)
     {
    	super.onTouchEvent(event, mapView);
     	if (event.getAction() == MotionEvent.ACTION_UP) {
     		GeoPoint geoPoint = mapView.getProjection().fromPixels(0,0);
     		double minLong = (double)geoPoint.getLongitudeE6()/(double)1E6;
     		double maxLat  = (double)geoPoint.getLatitudeE6()/(double)1E6;
     		double maxLong = minLong+(double)mapView.getLongitudeSpan()/(double)1E6;
     		double minLat  = maxLat-(double)mapView.getLatitudeSpan()/(double)1E6;
 
     		int minGridLong = (int)(Math.floor(minLong+180));
     		int minGridLat  = (int)(Math.floor(minLat+90));
     		int maxGridLong = (int)(Math.ceil(maxLong+180));
     		int maxGridLat  = (int)(Math.ceil(maxLat+90));
     		
     		MapArea mapArea = new MapArea(minGridLat, maxGridLat, minGridLong, maxGridLong);
 
     		Log.i("Map", "minLong=" + minLong + ",maxLong=" + maxLong + ",minLat=" + minLat + ",maxLat=" + maxLat);
     		Log.i("Map - Grid", "minGridLong=" + minGridLong + ",maxGridLong=" + maxGridLong + ",minGridLat=" + minGridLat + ",maxGridLat=" + maxGridLat);
     		
     		ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
     		
     		new PopulateMapArea(mapView.getContext(), mapArea).execute(overlayItemList);
  
     		for (OverlayItem item : overlayItemList) {
     			this.addOverlay(item);
     		}

     	    mapView.invalidate();
     	    
     	    Log.i("airfields","Map Invalidated");
         	
        }
 
     	
 		return false;
     }
 }
