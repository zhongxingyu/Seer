 package com.datakom;
 
 import java.util.ArrayList;
 
 import com.datakom.POIObjects.HaggleConnector;
 import com.datakom.POIObjects.POIObject;
 import com.google.android.maps.GeoPoint;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class TabTrace extends Activity implements OnClickListener {
 
 	private String title;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tabtrace);
 		
 		// First we need to get the data stored in the intent
 		Bundle extras = getIntent().getExtras();
 		if(extras != null) {
 			
 			// Get the value of the title and set it as topic
			title = extras.getString("TRACE_OBJECTS");
 			setTitle(title);
 			
			ArrayList<POIObject> objects = HaggleConnector.getInstance().getPOIObjectsByName(title);
			ArrayList<GeoPoint> geopoints = null;
			for(POIObject a : objects)
			{
				geopoints = a.getCoordsExchange();
			}
			
			// We need to get the amount of times this object have been exchanged
			setDescription(geopoints.size());
 			
 			Button showOnMap = (Button) findViewById(R.id.show_on_map);
 			// Set the button to listen for clicks
 			showOnMap.setOnClickListener(this);
 
 		} else {
 			setDescription(0);
 		}
 	}
 	
 	protected void setTitle(String title) {
 		TextView texttitle = (TextView) findViewById(R.id.info_title);
 		texttitle.setText(title);
 	}
 	
 	protected void setDescription(int exchanges)
 	{
 		String title = "This object have been exchanged " + Integer.toString(exchanges) + " times\n\n\n\n";
 		TextView description = (TextView) findViewById(R.id.label_info);
 		description.setText(title);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.show_on_map) {
 			// This is called when we clicked the button
 			Bundle coordinates = new Bundle();
 			// We send the object to the map so it can parse the value itself
 			coordinates.putString(com.datakom.POIObjects.HaggleConnector.SEARCH_TITLE , title);
 			coordinates.putString(com.datakom.POIObjects.HaggleConnector.SHOW_TRACE, "true");
 			
 			Intent mapview = new Intent(TabTrace.this, TabMap.class);
 			mapview.putExtra("com.datakom.TabMap", coordinates);
 			TabTrace.this.startActivity(mapview);
 		}
 		
 	}
 }
