 package ch.unige.tpgcrowd.ui.fragments;
 
 import java.io.Serializable;
 
 import com.google.android.gms.location.LocationClient;
 
 import ch.unige.tpgcrowd.R;
 import ch.unige.tpgcrowd.google.location.LocationHandler;
 import ch.unige.tpgcrowd.manager.ITPGStops;
 import ch.unige.tpgcrowd.manager.TPGManager;
 import ch.unige.tpgcrowd.model.Stop;
 import ch.unige.tpgcrowd.model.StopList;
 import ch.unige.tpgcrowd.net.listener.TPGObjectListener;
 import ch.unige.tpgcrowd.ui.component.HorizontalStickyScrollView;
 import ch.unige.tpgcrowd.ui.component.HorizontalStickyScrollView.StopSelectedListener;
 import ch.unige.tpgcrowd.ui.component.StopViewItem;
 import android.support.v4.app.Fragment;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Camera.PreviewCallback;
 import android.location.Location;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
public class ShowNearbyStopsFragment extends Fragment implements StopSelectedListener {
 	public static final String TAG = "nearbyStops";
 	private LinearLayout layoutLeft;
 	private LinearLayout layoutRight;
 	private HorizontalStickyScrollView scrollView;
 	
 	float accuracyLimit = 400; //min accuracy to display nearby stops without user input
 	float distMin = 50;
 	float[] dist;
 	int locationAttemptsMax = 10; //number of attempts before stopping
 	int locationAttempts;
 	
 	Location curLocation; // Location from google API
 	Location curLocationUsed;// Holds the location from google API currently being used for nearby stops call
 	
 	boolean userPoint = false; //True if user pointed in map for more accurate position
 	
 	private PendingIntent penInt;
 	
 	private static final String ACTION_GET_LOCATION = "ch.unige.tpgcrowd.action.GET_LOCATION";
 
 	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			curLocation = (Location) intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);
 			updateMap(curLocation);
 			Location.distanceBetween(curLocationUsed.getLatitude(), curLocationUsed.getLongitude(), curLocation.getLatitude(),curLocation.getLongitude(), dist);
 			
 			if(!userPoint){
 				if(curLocation.hasAccuracy()){
 					if(curLocation.getAccuracy()<=accuracyLimit && (curLocation.getAccuracy()<curLocationUsed.getAccuracy() || dist[0]>=distMin)){
 						updateNearbyStops(curLocation, userPoint);
 						locationAttempts = 0;
 						curLocationUsed = curLocation;
 					}else{
 						//TODO display something like 'waiting for accuracy <=accuracyLimit - current accuracy XX'
 						locationAttempts+=1;
 					}
 				}
 				
 				if(locationAttempts>locationAttemptsMax){
 //					LocationHandler.stopLocation(getActivity(), penInt); //NOTE:(do not stop yet!! Need it to update the map. Stop it when user has selected a stop!)
 					//TODO prompt user to select a manual location
 				}
 			}
 			
 		}
 	};
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		Log.d("Create", "CREATE");
 		return inflater.inflate(R.layout.nearby_stops_fragment, container, false);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		getActivity().registerReceiver(locationReceiver, new IntentFilter(ACTION_GET_LOCATION));
 		// Create an explicit Intent
 		final Intent intent = new Intent(ACTION_GET_LOCATION);
 		/*
 		 * Return the PendingIntent
 		 */
 		penInt = PendingIntent.getBroadcast(
 				getActivity(),
 				0,
 				intent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		
 		LocationHandler.startLocation(getActivity(), penInt);
 		
 		
 		layoutLeft = (LinearLayout)getView().findViewById(R.id.layout_left);
 		layoutRight = (LinearLayout)getView().findViewById(R.id.layout_right);
 		
 		scrollView = (HorizontalStickyScrollView)getView().findViewById(R.id.horizontalScrollView);
 		
 		scrollView.setStopSelectedListener(this);
 		
 		layoutLeft.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				scrollView.move(false);
 			}
 		});
 		
 		layoutRight.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				scrollView.move(true);
 			}
 		});
 		
 	}
 
 	@Override
 	public void setSelectedStop(Stop stop) {
 		Log.d(TAG, "Stop selected : " + stop.getStopName());
 	}
 
 	public void updateMap(Location location){
 		//TODO update location on map
 	}
 
 	public void updateNearbyStops(Location location, boolean userPointHolder){
 		Log.d(TAG, "Update nearby stops!");
 		
 		userPoint = userPointHolder;
 		
 //		double latitude = 46.2022200;
 //		double longitude = 6.1456900;
 		double latitude = location.getLatitude();
 		double longitude = location.getLongitude();
 		
 		ITPGStops stopsManager = TPGManager.getStopsManager(getActivity());
 		stopsManager.getStopsByPosition(latitude, longitude, new TPGObjectListener<StopList>() {
 
 			@Override
 			public void onSuccess(StopList results) {
 				scrollView.setNearbyStops(results.getStops());		
 			}
 
 			@Override
 			public void onFailure() {
 				// TODO Maybe wait a bit and try again. LOG!!
 				
 			}
 		});
 
 	}
 
 	
 }
