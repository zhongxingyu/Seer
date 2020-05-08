 package csci498.gnanda.lunchList;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class RestaurantMap extends MapActivity {
 	
 	public static final String EXTRA_LATITUDE = "csci498.gnanda.lunchList.EXTRA_LATITUDE";
 	public static final String EXTRA_LONGITUDE = "csci498.gnanda.lunchList.EXTRA_LONGITUDE";
 	public static final String EXTRA_NAME = "csci498.gnanda.lunchList.EXTRA_NAME";
 	private MapView map;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 

 		double lat = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0);
 		double lon = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0);
 		
 		map = (MapView) findViewById(R.id.map);
 		map.getController().setZoom(17);
 		
 		GeoPoint status = new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0));
 		
 		map.getController().setCenter(status);
 		map.setBuiltInZoomControls(true);
 		
 		Drawable marker = getResources().getDrawable(R.drawable.marker);
 		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
 		
 		map.getOverlays().add(new RestaurantOverlay(marker, status, getIntent().getStringExtra(EXTRA_NAME)));
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	private class RestaurantOverlay extends ItemizedOverlay<OverlayItem> {
 		
 		private OverlayItem item;
 
 		public RestaurantOverlay(Drawable marker, GeoPoint point, String name) {
 			super(marker);
 			boundCenterBottom(marker);
 			item = new OverlayItem(point, name, name); 
 			populate();
 		}
 
 		@Override
 		protected OverlayItem createItem(int i) {
 			return item;
 		}
 
 		@Override
 		public int size() {
 			return 1;
 		}
 		
 		@Override
 		protected boolean onTap(int i) {
 			Toast.makeText(RestaurantMap.this, item.getSnippet(), Toast.LENGTH_SHORT).show();
 			return true;
 		}
 		
 	}
 
 }
