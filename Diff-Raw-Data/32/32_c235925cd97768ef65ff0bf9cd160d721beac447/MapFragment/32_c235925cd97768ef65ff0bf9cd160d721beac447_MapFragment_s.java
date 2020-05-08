 package ee.ajapaik.android.fragment;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 import ee.ajapaik.android.DetailsActivity;
 import ee.ajapaik.android.R;
 import ee.ajapaik.android.loader.PhotoListLoader;
 import ee.ajapaik.android.map.PhotoItem;
 import ee.ajapaik.android.map.PhotoItemsOverlay;
 
 public class MapFragment extends Fragment implements LoaderCallbacks<PhotoItem[]>, PhotoItemsOverlay.Listener {
 	public static MapFragment newInstance() {
 		return new MapFragment();
 	}
 	
 	public static final String TAG = "MapFragment";
 	
 	private MapView map;
 	private MapController mapc;
 	private MyLocationOverlay myLoc;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.map, null);
 		map = (MapView) v.findViewById(R.id.map);
 		mapc = map.getController();
 		
 		if (savedInstanceState == null) {
 			mapc.setCenter(new GeoPoint(58383333, 26716667));
 			mapc.setZoom(18);
 		}
 		
 		myLoc = new MyLocationOverlay(getActivity(), map);
 		map.getOverlays().add(myLoc);
 		
 		getLoaderManager().initLoader(0, null, this);
 		
 		return v;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		myLoc.enableCompass();
 		myLoc.enableMyLocation();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		myLoc.disableCompass();
 		myLoc.disableMyLocation();
 	}
 
 	@Override
 	public Loader<PhotoItem[]> onCreateLoader(int id, Bundle args) {
 		return new PhotoListLoader(getActivity());
 	}
 
 	@Override
 	public void onLoadFinished(Loader<PhotoItem[]> loader, PhotoItem[] results) {
 		PhotoItemsOverlay overlay = new PhotoItemsOverlay(this, results);
 		
 		map.getOverlays().add(0, overlay);
 		map.invalidate();
 	}
 
 	@Override
 	public void onLoaderReset(Loader<PhotoItem[]> loader) {
 		// wut
 	}
 
 	@Override
 	public void onTapPhoto(PhotoItem item) {
 		Log.d(TAG, "tapped " + item.getId());
 		DetailsActivity.start(getActivity(), item.getId(), item.getDescription());
 	}
 }
