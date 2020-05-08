 package fr.univsavoie.src.s4.ltp;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class PoteClickListener implements OnClickListener {
 
 	private MapView view;
 	private JSONObject pote;
 	
 	public PoteClickListener(MapView v, JSONObject p) {
 		view = v;
 		pote = p;
 	}
 
	@Override
 	public void onClick(View arg0) {
 		try {
 			GeoPoint p = new GeoPoint((int)(pote.getDouble("@lat")*1e6), (int)(pote.getDouble("@lon")*1e6));
 			view.getController().animateTo(p);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
