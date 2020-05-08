 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities;
 
 import android.content.Context;
 import android.os.Bundle;
 import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import org.mapsforge.android.maps.IMapActivity;
 import org.mapsforge.android.maps.MapView;
 import org.mapsforge.core.GeoPoint;
 
 public class MapActivity extends SherlockActivity implements IMapActivity {
 	private MapView mapView;
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		outState.putSerializable("Center", mapView.getMapPosition().getMapCenter());
 		outState.putByte("ZoomLvl", mapView.getMapPosition().getZoomLevel());
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_mapscreen);

 		if (savedInstanceState != null) {
 			mapView.setCenter((GeoPoint) savedInstanceState.get("Center"));
 			// TODO restore zoom
 		}
 	}
 

 	@Override
 	protected void onDestroy() {
 		mapView.destroy();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onPause() {
 		mapView.onPause();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mapView.onResume();
 	}
 
 	@Override
 	public Context getContext() {
 		return this;
 	}
 
 	@Override
 	public int getMapViewId() {
 		return 0;
 	}
 
 	@Override
 	public void registerMapView(MapView mapView) {
 		this.mapView = mapView;
 	}
 }
