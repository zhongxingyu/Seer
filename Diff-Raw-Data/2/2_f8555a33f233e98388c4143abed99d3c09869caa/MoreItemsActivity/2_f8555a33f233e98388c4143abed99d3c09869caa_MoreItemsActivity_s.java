 package cmuHCI.WalkyScotty;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import cmuHCI.WalkyScotty.entities.*;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ExpandableListView.OnChildClickListener;
 
 
 public class MoreItemsActivity extends WSActivity implements OnItemClickListener{
 	private String[] places = new String[0];
 	private Location[] LOCATIONS;
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		ArrayList<? extends Location> locations;
 
 		LocationType locType = LocationType.valueOf(getIntent().getStringExtra("which"));
 		
         super.onCreate(savedInstanceState);
         setContentView(R.layout.moreitems);
         
         locations = getLocationList(locType);
         
         ListView brlv = (ListView) findViewById(R.id.more_items_list);
 		brlv.setTextFilterEnabled(true);
 		brlv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, places));
 		brlv.setOnItemClickListener(this);
 
 		
 		TextView bc = (TextView) findViewById(R.id.moreitems_breadcrumb_category);
         bc.setText(locType.toString().charAt(0) + locType.toString().substring(1).toLowerCase()); //<-- this is so dumb...
         
         TextView title = (TextView) findViewById(R.id.more_items_title);
        title.setText(locType.toString());
 
     }
 	
 	@Override
 	public void onItemClick(AdapterView arg0,View v,int position, long arg3) {
 			navigateDetailsPage(LOCATIONS[position]);
 	}
 	
 	private ArrayList<? extends Location> getLocationList(LocationType type){
 		ArrayList<? extends Location> locs = new ArrayList<Location>();
 		
 		DBAdapter adp = new DBAdapter(this);
 		try {
 			adp.createDataBase();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		adp.openDataBase();
 		
 		switch(type){
 			case BUILDINGS :
 				locs = adp.getBuildings();
 				break;
 			case ROOMS :
 				locs = adp.getRooms();
 				break;
 			case RESTAURANTS :
 				locs = adp.getRestaurants();
 				break;
 			case SERVICES:
 				locs = adp.getServices();
 				break;
 			case OTHER:
 				locs = adp.getOther();
 				break;
 			default:
 				locs = adp.getAllLocations();
 				break;
 		}
 		
 		adp.close();
 		
 		places = new String[locs.size()];
 		LOCATIONS = new Location[locs.size()];
 		int i = 0;
 		for(Location l:locs){
 			places[i] = l.getName();
 			LOCATIONS[i] = l;
 			i++;
 		}
 		return locs;
 	}
 	
 	private void navigateDetailsPage(Location loc){
 		if(loc.getId() <= 0)
 			throw new RuntimeException("Bad Location ID");
 		
 		Intent i;
 		switch(loc.getlType()){
 			case RESTAURANTS:
 				i = new Intent(this, FoodInfo.class);
 				break;
 			case SHUTTLES:
 				i = new Intent(this, ShuttleInfo.class);
 				break;
 			case ESCORTS:
 				i = new Intent(this, EscortInfo.class);
 				break;
 			case ROOMS:
 				i = new Intent(this, RoomInfo.class);
 				break;
 			case SERVICES:
 				i = new Intent(this, OtherInfo.class);
 				break;
 			default:
 				i = new Intent(this, BakerInfo.class);
 		}
 		
 		i.putExtra("lID", loc.getId());
 		this.startActivity(i);
 	}
 	
 }
