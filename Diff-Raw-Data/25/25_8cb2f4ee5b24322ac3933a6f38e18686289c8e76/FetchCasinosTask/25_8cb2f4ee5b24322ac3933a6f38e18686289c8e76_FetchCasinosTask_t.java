 /**
  * 
  */
 package cz.mapyhazardu.android.task;
 
import java.util.ArrayList;
 import java.util.List;
 
 import com.google.android.maps.MapView;
 
 import android.os.AsyncTask;
 import android.util.Log;
import android.widget.Toast;
 import cz.mapyhazardu.android.CasinoOverlay;
 import cz.mapyhazardu.android.MapyHazarduServiceProvider;
import cz.mapyhazardu.android.R;
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
	private Toast errorToast;
 	
 	public FetchCasinosTask(CasinoOverlay casinoOverlay, MapView mapView) {
 		super();
 		this.casinoOverlay = casinoOverlay;
 		this.mapView = mapView;
 	}
 
 	@Override
 	protected List<Casino> doInBackground(GeographicCoordinate... param) {
 		GeographicCoordinate position = param[0];
 		
 		Log.i(TAG, "Loading casino list");
		MapyHazardu mapyHazarduApi = MapyHazarduServiceProvider.getService();
		
		List<Casino> casinos = new ArrayList<Casino>();
		try {
			casinos = mapyHazarduApi.findNearestCasinos(position);
		} catch (Throwable ex) {
			Log.e(FetchCasinosTask.class.getSimpleName(), "Error when fetching nearest casinos information from web service.", ex);
			errorToast.show();
		}
 		
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
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		errorToast = Toast.makeText(mapView.getContext(), R.string.message_error_when_obtaining_data_from_api, 10000);
	}
 }
