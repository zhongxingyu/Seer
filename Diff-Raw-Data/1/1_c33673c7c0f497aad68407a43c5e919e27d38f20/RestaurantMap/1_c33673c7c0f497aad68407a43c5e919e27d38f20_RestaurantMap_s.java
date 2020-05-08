 package csci498.csmyth.lunchlist;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.Toast;

 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class RestaurantMap extends MapActivity {
 	private MapView map = null;
 	private GeoPoint status = null;
 	double lat;
 	double lon;
 	
 	public static final String EXTRA_LATITUDE = "csci498.csmyth.lunchlist.EXTRA_LATITUDE";
 	public static final String EXTRA_LONGITUDE = "csci498.csmyth.lunchlist.EXTRA_LONGITUDE";
 	public static final String EXTRA_NAME = "csci498.csmyth.lunchlist.EXTRA_NAME";
 	public static final Double LAT_LON_DEFAULT = 0.0;
 	public static final Integer DEFAULT_ZOOM = 17;
 	public static final Double MICRODEGREE_CONVERSION_FACTOR = 1000000.0;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		
 		lat = getIntent().getDoubleExtra(EXTRA_LATITUDE, LAT_LON_DEFAULT);
 		lon = getIntent().getDoubleExtra(EXTRA_LONGITUDE, LAT_LON_DEFAULT);
 		map = (MapView)findViewById(R.id.map);
 		
 		map.getController().setZoom(DEFAULT_ZOOM);
 		status = new GeoPoint((int)(lat * MICRODEGREE_CONVERSION_FACTOR), (int)(lon * MICRODEGREE_CONVERSION_FACTOR));
 		map.getController().setCenter(status);
 		map.setBuiltInZoomControls(true);
 		
 		Drawable marker = getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
 		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
 		map
 			.getOverlays()
 			.add(new RestaurantOverlay(marker, status, getIntent().getStringExtra(EXTRA_NAME)));
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	private class RestaurantOverlay extends ItemizedOverlay<OverlayItem> {
 		private OverlayItem item = null;
 		
 		private final Integer NUM_POINTS_TO_DRAW = 1;
 		
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
 			return NUM_POINTS_TO_DRAW;
 		}
 		
 		@Override
 		protected boolean onTap(int i) {
 			Toast.makeText(RestaurantMap.this, item.getSnippet(), Toast.LENGTH_SHORT).show();
 			return true;
 		}
 	}
 
 }
