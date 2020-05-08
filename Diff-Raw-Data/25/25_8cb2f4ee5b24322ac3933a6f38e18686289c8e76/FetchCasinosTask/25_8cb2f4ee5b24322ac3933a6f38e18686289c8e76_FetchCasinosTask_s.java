 /**
  * 
  */
 package cz.mapyhazardu.android.task;
 
 import java.util.List;
 
 import com.google.android.maps.MapView;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import cz.mapyhazardu.android.CasinoOverlay;
 import cz.mapyhazardu.android.MapyHazarduServiceProvider;
 import cz.mapyhazardu.api.MapyHazardu;
 import cz.mapyhazardu.api.domain.Casino;
 import cz.mapyhazardu.api.domain.GeographicCoordinate;
 import cz.mapyhazardu.api.impl.MapyHazarduImpl;
 
 /**
  * @author vlasta
  *
  */
 public class FetchCasinosTask extends AsyncTask<GeographicCoordinate, Integer, List<Casino>> {
 
 	private final CasinoOverlay casinoOverlay;
 	private final MapView mapView;
 	private static final String TAG = "FetchCasinosTask";
 	
 	public FetchCasinosTask(CasinoOverlay casinoOverlay, MapView mapView) {
 		super();
 		this.casinoOverlay = casinoOverlay;
 		this.mapView = mapView;
 	}
 
 	@Override
 	protected List<Casino> doInBackground(GeographicCoordinate... param) {
 		GeographicCoordinate position = param[0];
 		
 		Log.i(TAG, "Loading casino list");
		List<Casino> casinos = MapyHazarduServiceProvider.getService().findNearestCasinos(position);
 		
 		return casinos;
 	}
 
 	@Override
 	protected void onPostExecute(List<Casino> result) {
 		Log.i(TAG, "Displaying casino list");
 		
 		for (Casino casino : result) {
 			casinoOverlay.addCasino(casino);
 		}
 		
 		mapView.postInvalidate();
 	}
 }
