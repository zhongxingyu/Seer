 package edu.illinois.geosight;
 
 import java.io.IOException;
 
 import org.apache.http.ParseException;
 import org.json.JSONException;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import edu.illinois.geosight.servercom.GeosightException;
 import edu.illinois.geosight.servercom.Sight;
 
 public class SightListActivity extends ListActivity {
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.sights);
 		
 		try {
 			setListAdapter( new ArrayAdapter<Sight>(this, android.R.layout.simple_list_item_1, Sight.getAllSights()));
 		} catch (GeosightException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	protected void onListItemClick (ListView l, View v, int position, long id){
 		Sight current = (Sight)l.getItemAtPosition(position);
 		Intent intent = new Intent(SightListActivity.this, GoogleMapActivity.class);
 		
 		intent.putExtra("latitude", current.getLatitudeE6() );
 		intent.putExtra("longitude", current.getLongitudeE6() );
 		
 		startActivity(intent);
 	}
 
 
 }
