 package com.csci5115.group2.planmymeal;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 /**
  * A fragment representing a single Meal detail screen. This fragment is either
  * contained in a {@link MealListActivity} in two-pane mode (on tablets) or a
  * {@link MealDetailActivity} on handsets.
  */
 public class MealDetailFragment extends Fragment {
 	/**
 	 * The fragment argument representing the item ID that this fragment
 	 * represents.
 	 */
 	public static final String ARG_ITEM_ID = "item_id";
 
 	/**
 	 * The dummy content this fragment is presenting.
 	 */
 	private Meal meal;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public MealDetailFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getArguments().containsKey(ARG_ITEM_ID)) {
 			// Load the dummy content specified by the fragment
 			// arguments. In a real-world scenario, use a Loader
 			// to load content from a content provider.
			Log.v("FRAG", ARG_ITEM_ID);
			meal = GlobalData.findUserMealByName(ARG_ITEM_ID);
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_meal_detail,
 				container, false);
 
 		// Show the dummy content as text in a TextView.
 		if (meal != null) {
 			((TextView) rootView.findViewById(R.id.fragment_meal_title))
 					.setText(meal.getName());
 		}
 
 		return rootView;
 	}
 }
