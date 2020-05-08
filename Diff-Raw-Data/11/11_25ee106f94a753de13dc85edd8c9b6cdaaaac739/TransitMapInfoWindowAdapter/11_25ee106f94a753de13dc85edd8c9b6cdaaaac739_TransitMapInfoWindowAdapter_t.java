 package com.appirio.mobile.aau.nativemap;
 
 import java.util.ArrayList;
 
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.View;
 import android.widget.TextView;
 
 import com.appirio.aau.R;
 import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.model.Marker;
 
 public class TransitMapInfoWindowAdapter implements InfoWindowAdapter, OnInfoWindowClickListener {
 
 	private Context ctx;
 	private MapManager mapManager;
 	
 	public TransitMapInfoWindowAdapter(Context ctx, MapManager mapManager) {
 		this.ctx = ctx;
 		this.mapManager = mapManager;
 	}
 	
 	@Override
 	public View getInfoContents(Marker marker) {
 		try {
 			JSONObject markerInfo = new JSONObject(marker.getTitle());
 			
 			if(markerInfo.has("type") && markerInfo.getString("type").equals("stop")) {
 				View result = ((Activity)ctx).getLayoutInflater().inflate(R.layout.bus_stop_info_window, null);
 				
 				((TextView)result.findViewById(R.id.txtRoutes)).setText(markerInfo.getString("routes"));
 				((TextView)result.findViewById(R.id.txtStopName)).setText(markerInfo.getString("stopName"));
 
 				View imgvwOpenStopDetails = result.findViewById(R.id.imgvwOpenStopDetails);
 				imgvwOpenStopDetails.setClickable(true);
 				
 				return result;
 			} else if (markerInfo.has("type") && markerInfo.getString("type").equals("bus")) {
 				View result = ((Activity)ctx).getLayoutInflater().inflate(R.layout.bus_info_window, null);
 				
 				((TextView)result.findViewById(R.id.txtBusRoute)).setText(markerInfo.getString("route"));
 				
 				return result;
 			}
 			
 			return null;
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			
 			return null;
 		}
 	}
 
 	@Override
 	public View getInfoWindow(Marker arg0) {
 		return null;
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		try {
 			if(!StopScheduleActivity.isActive()) {
 				JSONObject markerInfo = new JSONObject(marker.getTitle());
 
 				if(markerInfo.has("type") && markerInfo.getString("type").equals("stop")) {
					StopScheduleActivity.setActive(true);

 					String stopName = markerInfo.getString("stopName");
 
 					StopScheduleActivity.setMapManager(this.mapManager);
 
 					//ArrayList<RouteStopSchedule> schedule = this.mapManager.getSchedule(stopName);
 	
 					Intent intent = new Intent(this.ctx, StopScheduleActivity.class);
 					
 					//intent.putExtra("schedule", schedule);
 					intent.putExtra("stopName", stopName);
 					
 					this.ctx.startActivity(intent);
 					
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 	}
 
 }
