 package info.liuqy.adc.aroundme;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class StarOverlay extends ItemizedOverlay<OverlayItem> {
 	private ArrayList<OverlayItem> stars = new ArrayList<OverlayItem>();
 	private Context context;
 	CouchDbAdapter couchdb;
 
 	public StarOverlay(Context context, Drawable defaultMarker) {
 		super(boundCenterBottom(defaultMarker)); // adjust (0,0) to center bottom
 		this.context = context;
 
 		/*
 		 * populate the empty overlay.
 		 * http://code.google.com/p/android/issues/detail?id=11666
 		 */
 		populate();
 	}
 
 	@Override
 	protected OverlayItem createItem(int i) {
 		return stars.get(i);
 	}
 
 	@Override
 	public int size() {
 		return stars.size();
 	}
 
 	@Override
 	protected boolean onTap(int index) {
 		OverlayItem item = stars.get(index);
 		// start chat
 		Intent i = new Intent(context, ChatActivity.class);
 		i.putExtra(ChatActivity.EXTRA_ID, item.getTitle());
 		i.putExtra(ChatActivity.EXTRA_ATTRIBUTES, item.getSnippet());
 		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //bring it up or create it
 		context.startActivity(i);
 		return true;
 	}
 
 	// run this in UI thread
 	public void renewCouchDbAdapter() {
 		couchdb = new CouchDbAdapter(handler);
 	}
 	
 	// run this in any thread
 	public void loadStarsAroundMe(MapView mapView) {
 		GeoPoint p = mapView.getMapCenter();
 
 		int lat1 = p.getLatitudeE6() - mapView.getLatitudeSpan() / 2;
 		int lat2 = p.getLatitudeE6() + mapView.getLatitudeSpan() / 2;
 		int long1 = p.getLongitudeE6() - mapView.getLongitudeSpan() / 2;
 		int long2 = p.getLongitudeE6() + mapView.getLongitudeSpan() / 2;
 		double lat1d = (double) lat1 / 1e6;
 		double long1d = (double) long1 / 1e6;
 		double lat2d = (double) lat2 / 1e6;
 		double long2d = (double) long2 / 1e6;
 
 		String extraPath = "/_design/stars/_view/by_loc";
		String query = "view/by_loc?startkey=[" + long1d + "," + lat1d
 				+ "]&endkey=[" + long2d + "," + lat2d + "]";
 		couchdb.doGet(extraPath, query);
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Bundle data;
 			switch (msg.what) {
 			case CouchDbAdapter.RESULT:
 				data = msg.getData();
 				String page = data.getString("result");
 				Log.d("XXX", "Loaded stars: " + page);
 				String jsonText = page.substring(4); // 200:{jsonText}
 				try {
 					showMarkers(jsonText);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					Toast.makeText(context,
 							"Load star failed. Wrong json format: " + jsonText,
 							Toast.LENGTH_LONG).show();
 				}
 				break;
 			case CouchDbAdapter.START:
 				Log.d("XXX", "Loading stars...");
 				break;
 			default:
 				// TODO
 			}
 		}
 
 		private void showMarkers(String jsonText) throws JSONException {
 			JSONObject json = new JSONObject(jsonText);
 			JSONArray rows = json.getJSONArray("rows");
 			for (int i = 0; i < rows.length(); i++) {
 				JSONObject star = rows.getJSONObject(i).getJSONObject("value");
 				String id = star.getString("_id");
 				String name = star.getString("name");
 				long until = star.getLong("until");
 				double long0 = star.getDouble("long");
 				double lat0 = star.getDouble("lat");
 				GeoPoint p0 = new GeoPoint((int) (lat0 * 1e6),
 						(int) (long0 * 1e6));
 				OverlayItem item = new OverlayItem(p0, id, name + "," + until);
 				stars.add(item);
 			}
 
 			StarOverlay.this.populate(); // draw the stars
 		}
 	};
 }
