 package eu.trentorise.smartcampus.jp.custom;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import android.os.AsyncTask;
 
 import com.google.android.maps.MapView;
 
 import eu.trentorise.smartcampus.jp.custom.map.StopsItemizedOverlay;
 import eu.trentorise.smartcampus.jp.helper.JPHelper;
 import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
 import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
 
 public class StopsAsyncTask extends AsyncTask<Object, SmartCheckStop, Boolean> {
 
 	public interface OnStopLoadingFinished {
 		public void onStopLoadingFinished();
 	}
 
 	private OnStopLoadingFinished mOnStopLoadingFinished;
 
	// private Collection<SmartCheckStop> list;
 	private Map<String, SmartCheckStop> smartCheckStopMap;
 	private StopsItemizedOverlay overlay;
 	private double diagonal;
 	private double[] location;
 	private MapView mapView;
 	private StopsItemizedOverlay old_overlay;
 
 	public StopsAsyncTask(Map<String, SmartCheckStop> smartCheckStopMap, StopsItemizedOverlay overlay, double[] location,
 			double diagonal, MapView mapView, OnStopLoadingFinished listener) {
 		super();
 		this.overlay = overlay;
 		this.mapView = mapView;
 		this.diagonal = diagonal;
 		this.location = location;
 		this.mOnStopLoadingFinished = listener;
		// this.list = new ArrayList<SmartCheckStop>();
 		this.smartCheckStopMap = smartCheckStopMap;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		old_overlay = overlay;
 
 	}
 
 	@Override
 	protected void onCancelled() {
 		super.onCancelled();
 		this.onPostExecute(false);
 	}
 
 	@Override
 	protected Boolean doInBackground(Object... params) {
 		// params[0]=location
 		// params[1]=radius
 		try {
 			List<SmartCheckStop> stops = JPHelper.getStops(RoutesHelper.AGENCYID_BUS + "", location, diagonal);
			// list.clear();
 			for (SmartCheckStop stop : stops) {
 				if (!smartCheckStopMap.containsKey(stop.getId())) {
 					smartCheckStopMap.put(stop.getId(), stop);
					// list.add(stop);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	protected void onProgressUpdate(SmartCheckStop... values) {
 		super.onProgressUpdate(values);
 		// overlay.addOverlay(values[0]);
 	}
 
 	@Override
 	protected void onPostExecute(Boolean result) {
 		super.onPostExecute(result);
 		if (result) {
			overlay.clearMarkers();
			// for (SmartCheckStop o : list) {
			for (SmartCheckStop o : smartCheckStopMap.values()) {
 				overlay.addOverlay(o);
 			}
 			overlay.populateAll();
 			mapView.invalidate();
			// list.clear();
 		} else {
 			overlay = old_overlay;
 		}
 
 		mOnStopLoadingFinished.onStopLoadingFinished();
 	}
 
 }
