 package edu.uiuc.whosinline.fragments;
 
 import java.util.List;
 
 import edu.uiuc.whosinline.R;
 import edu.uiuc.whosinline.data.Venue;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class FavoriteFragment extends BaseFragment {
 	
 	private int tableNum = 2;
 	
 	private void insertTestData() {
 		Venue venue;
 		
		venue = new Venue(1, "Chipotle Mexican Grill", "", R.drawable.chipotle,
 				"Restaurant", R.drawable.stars_5, 0.12f, 10);
 		dbAccessObj.insertVenue(tableNum, venue);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		List<Venue> venues = dbAccessObj.getAllVenues(tableNum);
 		for (Venue venue : venues) {
 			dbAccessObj.deleteVenue(tableNum, venue);
 		}
 		insertTestData();
 		
 		fillData(tableNum);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		// Inflate the layout for this fragment.
 		return inflater.inflate(R.layout.fragment_favorite, container, false);
 	}
 }
