 package destinationsalinas.historictour;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 public class MapsActivity extends MapActivity {
 
 	private List<Overlay> mapOverlays;
 	private Drawable drawable;
 	private MapsItemizedOverlay itemizedOverlay;
 	private DestinationManager destinationManager;
 	private final int DEFAULT_ZOOM = 12;
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.maps);
 		
 		destinationManager = GlobalVariables.destinationManager;
 		MapView mapView;
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		
 		mapOverlays = mapView.getOverlays();
 		int counter = 0;
 		GeoPoint point;
 		OverlayItem overlayitem;
 		
 		for (int i = 0; i < destinationManager.getSize(); i++)
 		{
 			if (destinationManager.getDestination(i).getLocation().getLatitudeE6() != 0)
 			{
 				switch(counter)
 				{
 				case 0:
 					drawable = this.getResources().getDrawable(R.drawable.green_marker);
 					break;
 				case 1:
 					drawable = this.getResources().getDrawable(R.drawable.light_blue_marker);
 					break;
 				case 2:
 					drawable = this.getResources().getDrawable(R.drawable.pink_marker);
 					break;
 				case 3:
 					drawable = this.getResources().getDrawable(R.drawable.purple_marker);
 					break;
 				case 4:
 					drawable = this.getResources().getDrawable(R.drawable.red_marker);
 					break;
 				case 5:
 					drawable = this.getResources().getDrawable(R.drawable.yellow_marker);
 					counter = -1;
 					break;
 				}
 				
 				itemizedOverlay = new MapsItemizedOverlay(drawable);
 				point = destinationManager.getDestination(i).getLocation();
 				overlayitem = new OverlayItem(point, "" + destinationManager.getDestination(i).getNumber(), "");
 				itemizedOverlay.addOverlay(overlayitem);
 				mapOverlays.add(itemizedOverlay);
 				counter++;
 			}
 		}
 		
 		mapView.getController().setZoom(DEFAULT_ZOOM);
 		mapView.getController().setCenter(itemizedOverlay.getCenter());
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 	    return false;
 	}
 	
 	private class MapsItemizedOverlay extends ItemizedOverlay {
 
 		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 		
 		public MapsItemizedOverlay(Drawable arg0) {
 			super(boundCenterBottom(arg0));
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		protected OverlayItem createItem(int i) {
 			// TODO Auto-generated method stub
 			return mOverlays.get(i);
 		}
 
 		@Override
 		public int size() {
 			// TODO Auto-generated method stub
 			return mOverlays.size();
 		}
 		
 		public void addOverlay(OverlayItem overlay)
 		{
 			mOverlays.add(overlay);
 			populate();
 		}
 		
 		@Override 
 	    protected boolean onTap(int i) { 
 			
 			Intent intent = new Intent();
 			intent.setClass(getBaseContext(), DestinationActivity.class);
 			intent.putExtra("site.number", Integer.parseInt(mOverlays.get(i).getTitle()));
 			startActivity(intent);
             return true; 
 	    }
 	}
 }
