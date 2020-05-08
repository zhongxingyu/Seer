 package com.allplayers.android;
 
 import android.os.Bundle;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 public class EventDisplayActivity extends MapActivity
 {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.eventdetail);
 		
 		TextView eventInfo = (TextView)findViewById(R.id.eventInfo);
 		MapView map = (MapView)findViewById(R.id.eventMap);
 		
 		EventData event = Globals.currentEvent;
 		eventInfo.setText("Event Title: " + event.getTitle() + "\nDescription: " + 
 				event.getDescription() + "\nCategory: " + event.getCategory() + 
				"\nStart: " + event.getStart() + "\nEnd: " + event.getEnd());
 		
 		MapController mapController = map.getController();
 		String lat = event.getLatitude();
 		String lon = event.getLongitude();
 		GeoPoint point;
 		
 		if(lat.equals("") || lon.equals(""))
 		{
 			point = new GeoPoint(0,0);
 		}
 		else
 		{
 			point = new GeoPoint((int)(Float.parseFloat(lat) * 1000000), (int)(Float.parseFloat(lon) * 1000000));
 		}
 		
 		mapController.setCenter(point);
 		map.setStreetView(true);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed()
 	{
 		return false;
 	}
 }
