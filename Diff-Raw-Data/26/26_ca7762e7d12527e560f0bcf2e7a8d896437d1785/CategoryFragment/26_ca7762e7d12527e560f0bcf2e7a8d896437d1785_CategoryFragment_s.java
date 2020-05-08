 package il.ac.huji.shoppit;
 
 import com.parse.ParseGeoPoint;
 import com.parse.ParseQuery;
 import com.parse.ParseQueryAdapter;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 
 public class CategoryFragment extends Fragment implements
 RadioGroup.OnCheckedChangeListener {
 
 	public static final String ARG_CATEGORY_NUMBER = "category_number";
 //	public static final String ARG_LATITUDE = "latitude";
 //	public static final String ARG_LONGITUDE = "longitude";
 	public static final String MAIN_CATEGORY = "mainCategory";
 	public static final String ALL = "All";
 
 	// FOR DEBUGGING
 	protected static final String TAG = "CAT_FRAG";
 
 	private RadioGroup radioGroupSortBy;
 	private ListView listView;
 
 	private ItemAdapter adapter;
 
 //	private double latitude;
 //	private double longitude;
 	private ParseGeoPoint currentLocation;
 	
 	private String category;
 
 	private int checkedRadioButtonId;
 
 	public CategoryFragment() {
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		final View rootView = inflater.inflate(R.layout.fragment_category, container, false);
 		final int i = getArguments().getInt(ARG_CATEGORY_NUMBER);
 
 		radioGroupSortBy = (RadioGroup) rootView.findViewById(R.id.radioGroupSortBy);
 		listView = (ListView) rootView.findViewById(R.id.homeListView);
 
 		// get location from MainActivity
 		double latitude = getArguments().getDouble(MainActivity.LATITUDE_EXTRA);
 		double longitude = getArguments().getDouble(MainActivity.LONGITUDE_EXTRA);
		ParseGeoPoint currentLocation = new ParseGeoPoint(latitude, longitude);
 		
 		category = getResources().getStringArray(R.array.categories_array)[i];
 
 		radioGroupSortBy.setOnCheckedChangeListener(this);
 
 		checkedRadioButtonId = radioGroupSortBy.getCheckedRadioButtonId();
 
 		loadItems();
 
 		return rootView;
 	}
 
 	private void loadItems() {
 
 		ParseQueryAdapter.QueryFactory<Item> queryFactory = new ParseQueryAdapter.QueryFactory<Item>() {
 			public ParseQuery<Item> create() {				
 
 				ParseQuery<Item> query = new ParseQuery<Item>("Item");
 								
 				if (!category.equals(ALL)) {
 					query.whereEqualTo(MAIN_CATEGORY, category);
 				}
 
 				switch (checkedRadioButtonId) {
 				case R.id.radioNearby:
 					query.whereNear("location", currentLocation);
 					query.whereWithinMiles("location", currentLocation, 40000);
 					break;
 				case R.id.radioCheapest:
 					// TODO make sure the item is within a certain distance too, or not?
 					// query.whereWithinMiles("location", currentLocation, 10);
 					query.orderByAscending("price");
 					break;
 				case R.id.radioMostLiked:
 					query.orderByDescending("likesCount");
 					break;
 				default:
 					break;
 				}
 
 				return query;
 			}
 		};
 
 		adapter = new ItemAdapter(getActivity(), queryFactory);
 		listView.setAdapter(adapter);
 		getActivity().setTitle(category);
 	}
 
 	@Override
 	public void onCheckedChanged(RadioGroup group, int checkedId) {
 		checkedRadioButtonId = checkedId;
 		loadItems();
 	}
 }
