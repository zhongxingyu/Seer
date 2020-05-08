 package edu.mines.csci498.ybakos.lunchlist;
 
 import android.os.Bundle;
import android.widget.Toast;
 import android.graphics.drawable.Drawable;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class RestaurantMap extends MapActivity {
 	
 	public static final String EXTRA_LATITUDE = "edu.mines.csci498.ybakos.lunchlist.EXTRA_LATITUDE";
 	public static final String EXTRA_LONGITUDE = "edu.mines.csci498.ybakos.lunchlist.EXTRA_LONGITUDE";
 	public static final String EXTRA_NAME = "edu.mines.csci498.ybakos.lunchlist.EXTRA_NAME";
 
 	private MapView map;
 
 	private class RestaurantOverlay extends ItemizedOverlay<OverlayItem> {
 		
 		private OverlayItem item;
 		
 		public RestaurantOverlay(Drawable marker, GeoPoint point, String name) {
 			super(marker);
 			boundCenterBottom(marker);
 			item = new OverlayItem(point, name, name);
 			populate();
 		}
 		
 		@Override
		protected OverlayItem createItem(int index) {
 			return item;
 		}
 		
 		@Override
 		public int size() {
 			return 1;
 		}
 		
		@Override
		protected boolean onTap(int index) {
			Toast.makeText(RestaurantMap.this, item.getSnippet(), Toast.LENGTH_SHORT).show();
			return true;
		}
		
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
 		
 		double latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0);
 		double longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0);
 		map = (MapView) findViewById(R.id.map);
 		map.getController().setZoom(17);
 		GeoPoint status = new GeoPoint( (int)(latitude * 1000000.0), (int)(longitude * 1000000.0) );
 		map.getController().setCenter(status);
 		map.setBuiltInZoomControls(true);
 		
 		Drawable marker = getResources().getDrawable(R.drawable.marker);
 		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
 		map.getOverlays().add(new RestaurantOverlay(marker, status, getIntent().getStringExtra(EXTRA_NAME)));
 		
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 }
