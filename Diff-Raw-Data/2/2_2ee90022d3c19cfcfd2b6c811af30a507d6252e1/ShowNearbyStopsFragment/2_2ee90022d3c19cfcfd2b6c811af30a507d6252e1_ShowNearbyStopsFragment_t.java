 package ch.unige.tpgcrowd.ui.fragments;
 
 import com.google.android.gms.location.LocationClient;
 
 import ch.unige.tpgcrowd.google.location.LocationHandler;
 import ch.unige.tpgcrowd.manager.ITPGStops;
 import ch.unige.tpgcrowd.manager.TPGManager;
 import ch.unige.tpgcrowd.model.StopList;
 import ch.unige.tpgcrowd.net.listener.TPGObjectListener;
 import android.app.Fragment;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Location;
 
 public class ShowNearbyStopsFragment extends Fragment{
 	
 	float accuracyLimit = 400; //min accuracy to display nearby stops without user input
 	
 	Location currentLocation; // Location from google API
 	boolean userPoint = false; //True if user pointed in map for more accurate position
 	
 	private static final String ACTION_GET_LOCATION = "ch.unige.tpgcrowd.action.GET_LOCATION";
 	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			currentLocation = (Location) intent.getSerializableExtra(LocationClient.KEY_LOCATION_CHANGED);
 			updateMap(currentLocation);
 			if(currentLocation.hasAccuracy() && !userPoint){
 				if(currentLocation.getAccuracy()<=accuracyLimit){
 					updateNearbyStops(currentLocation, userPoint);
 				}else{
 					//TODO display something like 'waiting for accuracy <=accuracyLimit - current accuracy XX'
 				}
			}else if(!userPoint){
 					updateNearbyStops(currentLocation, userPoint);
 			}
 		}
 	};
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		getActivity().registerReceiver(locationReceiver, new IntentFilter(ACTION_GET_LOCATION));
 		// Create an explicit Intent
 				final Intent intent = new Intent(ACTION_GET_LOCATION);
 				/*
 				 * Return the PendingIntent
 				 */
 				PendingIntent penInt = PendingIntent.getBroadcast(
 						getActivity(),
 						0,
 						intent,
 						PendingIntent.FLAG_UPDATE_CURRENT);
 				
 				LocationHandler.startLocation(getActivity(), penInt);
 		
 	}
 	
 	public void updateMap(Location location){
 		//TODO update location on map
 	}
 
 	public void updateNearbyStops(Location location, boolean userPointHolder){
 		
 		userPoint = userPointHolder;
 		
 //		double latitude = 46.2022200;
 //		double longitude = 6.1456900;
 		double latitude = location.getLatitude();
 		double longitude = location.getLongitude();
 		
 		ITPGStops stopsManager = TPGManager.getStopsManager(getActivity());
 		stopsManager.getStopsByPosition(latitude, longitude, new TPGObjectListener<StopList>() {
 
 			@Override
 			public void onSuccess(StopList results) {
 				//TODO refresh view with stops				
 			}
 
 			@Override
 			public void onFailure() {
 				// TODO Maybe wait a bit and try again. LOG!!
 				
 			}
 	
 			
 			
 		});
 
 	}
 
 	
 }
