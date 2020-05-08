 package com.android.nitelights.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 import com.android.nitelights.R;
 
 
 
 /**
  * Venue fragment
  */
 public class VenuesFragment extends ListFragment{
 
 	static ArrayList<HashMap<String, String>> venueList;
 
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		View rootView = inflater.inflate(R.layout.list_venues,container, false);
 		return rootView;
 	}
 	
 	public void onViewCreated(View view, Bundle savedInstanceState){
 		super.onViewCreated(view, savedInstanceState);
 		
 		String[] title_venue = new String[] { "Light Ultra Club", "Stereo Night Club", "Club La Boom Montreal",
 		        "Altitude 737", "Bar Downtown", "Bains Douches", "Radio Lounge", "1234 Club",
 		        "Bar Salon Officiel", "Tokyo Bar" };
 		    ArrayAdapter<String> adapter_name = new ArrayAdapter<String>(getActivity(),
 		        R.layout.list_item_venues, R.id.title_venue, title_venue);
 		    setListAdapter(adapter_name);
 		    
 		    String[] title_venue_address = new String[] { "2020 Crescent Street, Montreal, QC, Canada", "858 Sainte-Catherine St E, Montreal, QC, Canada", "1254 Rue Stanley, Montreal, QC, Canada",
 			        "1 Place Ville Marie, Montreal, Canada", "1196 Sainte-Catherine West, Montreal, QC, Canada", "390 Saint-Jacques Old MTL, Montreal, QC, Canada", "3553 Saint Laurent Boulevard, Montreal, QC, Canada", "1234 Rue de la Montagne, Montreal, QC, Canada",
 			        "351 Rue Roy Est, Montreal, QC, Canada", "3709 Saint Laurent Boulevard, Montreal, QC, Canada" };
 		    ArrayAdapter<String> adapter_address = new ArrayAdapter<String>(getActivity(),
 			        R.layout.list_item_venues, R.id.title_venue_address, title_venue_address);
 			setListAdapter(adapter_address);
 	}
 
 }
