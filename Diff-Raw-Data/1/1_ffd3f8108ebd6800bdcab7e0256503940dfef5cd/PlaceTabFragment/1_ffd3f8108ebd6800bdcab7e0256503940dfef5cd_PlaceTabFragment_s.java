 package edu.vanderbilt.vm.guide.ui;
 
 /**
  * @author Athran, Nick
  * This Fragment shows the categories of places and the user's current location
  */
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.annotation.TargetApi;
 import android.app.Fragment;
 import android.database.Cursor;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.vanderbilt.vm.guide.R;
 import edu.vanderbilt.vm.guide.container.Place;
 import edu.vanderbilt.vm.guide.db.GuideDBConstants;
 import edu.vanderbilt.vm.guide.db.GuideDBOpenHelper;
 import edu.vanderbilt.vm.guide.ui.adapter.PlaceCursorAdapter;
 import edu.vanderbilt.vm.guide.ui.listener.GeomancerListener;
 import edu.vanderbilt.vm.guide.ui.listener.PlaceListClickListener;
 import edu.vanderbilt.vm.guide.util.DBUtils;
 import edu.vanderbilt.vm.guide.util.Geomancer;
 import edu.vanderbilt.vm.guide.util.GlobalState;
 import edu.vanderbilt.vm.guide.util.GuideConstants;
 
 @TargetApi(16)
 public class PlaceTabFragment extends Fragment implements OnClickListener,
 		GeomancerListener {
 	private final int DESCRIPTION_LENGTH = 100;
 
 	private ListView mListView;
 	private TextView mCurrPlaceName;
 	private TextView mCurrPlaceDesc;
 	private EditText mSearchBox;
 	private LinearLayout mCurrentPlaceBar;
 	private Place mCurrPlace;
 	private ImageView ivCurrent;
 	private Timer mSearchFaerie;
 	
 	private Cursor mAllPlacesCursor; // A cursor holding all places in the db
 
 	private static final int SEARCH_DELAY = 5000;
 
 	@SuppressWarnings("unused")
 	private static final Logger logger = LoggerFactory
 			.getLogger("ui.PlaceTabFragment");
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 		return inflater.inflate(R.layout.fragment_place_list, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		setupUI();
 
 		// Query for places and setup ListView
 		GuideDBOpenHelper helper = new GuideDBOpenHelper(getActivity());
 		String[] columns = { GuideDBConstants.PlaceTable.NAME_COL,
 				GuideDBConstants.PlaceTable.CATEGORY_COL,
 				GuideDBConstants.PlaceTable.LATITUDE_COL,
 				GuideDBConstants.PlaceTable.LONGITUDE_COL,
 				GuideDBConstants.PlaceTable.ID_COL,
 				GuideDBConstants.PlaceTable.DESCRIPTION_COL,
 				GuideDBConstants.PlaceTable.IMAGE_LOC_COL};
 		mAllPlacesCursor = DBUtils.getAllPlaces(columns,
 				helper.getReadableDatabase());
 		mListView.setAdapter(new PlaceCursorAdapter(getActivity(), mAllPlacesCursor));
 		mListView.setOnItemClickListener(new PlaceListClickListener(
 				getActivity()));
 		helper.close();
 
 		// Tells you what is the closest building to your location right now
 		Location loc = Geomancer.getDeviceLocation();
 		findAndSetClosestPlace(loc);
 
 		// Prevent the soft keyboard from popping up at startup.
 		getActivity().getWindow().setSoftInputMode(
 				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
 
 		setHasOptionsMenu(true);
 
 	}
 
 	private void setupUI() {
 		mCurrPlaceName = (TextView) getActivity().findViewById(
 				R.id.currentPlaceName);
 		mCurrPlaceDesc = (TextView) getActivity().findViewById(
 				R.id.currentPlaceDesc);
 		ivCurrent = (ImageView) getActivity().findViewById(
 				R.id.currentPlaceThumbnail);
 
 		mListView = (ListView) getActivity()
 				.findViewById(R.id.placeTablistView);
 
 		mSearchBox = (EditText) getActivity().findViewById(
 				R.id.placeTabSearchEdit);
 		mSearchBox.setOnClickListener(this);
 
 		mCurrentPlaceBar = (LinearLayout) getActivity().findViewById(
 				R.id.current_place_bar);
 		mCurrentPlaceBar.setOnClickListener(this);
 
 		// setBackground() throws NoSuchMethodError
 		// Had to use this one instead
 		mCurrentPlaceBar.setBackgroundDrawable(GuideConstants.LIGHT_GOLD);
 	}
 
 	private class SearchLogic extends TimerTask {
 
 		@Override
 		public void run() {
 			// String query = mSearchBox.getText().toString();
 
 			// TODO
 		}
 
 	}
 
 	// this method of setting up OnClickListener seems to be necessary when you
 	// want to access class variables
 	@Override
 	public void onClick(View v) {
 		if (v == mCurrPlaceDesc || v == mCurrPlaceName || v == mCurrentPlaceBar) {
 			PlaceDetailer.open(getActivity(), mCurrPlace.getUniqueId());
 		} else if (v == mSearchBox) {
 			// mSearchBox.setFocusable(true);
 		}
 
 	}
 
 	public void setCurrentPlace(Place plc) {
 		if(plc == null) {
 			return;
 		}
 		
 		mCurrPlace = plc;
 		mCurrPlaceName.setText(mCurrPlace.getName());
 
 		String desc = mCurrPlace.getDescription();
 		if (desc != null && desc.length() > DESCRIPTION_LENGTH) {
 			mCurrPlaceDesc.setText(desc.substring(0, DESCRIPTION_LENGTH)
 					+ "...");
 		} else if(desc == null) {
 			mCurrPlaceDesc.setText("No description available");
 		} else {
 			mCurrPlaceDesc.setText(desc + "...");
 		}
 
 		ivCurrent.setImageBitmap(GlobalState.getBitmapForPlace(mCurrPlace));
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 			// TODO
 			Toast.makeText(getActivity(), "Place list refreshed",
 					Toast.LENGTH_SHORT).show();
 			return true;
 
 		case R.id.menu_sort_alphabetic:
 			// TODO
 			Toast.makeText(getActivity(),
 					"PlacesList is sorted alphabetically", Toast.LENGTH_SHORT)
 					.show();
 			return true;
 
 		case R.id.menu_sort_distance:
 			// TODO
 			Toast.makeText(getActivity(), "PlacesList is sorted by distance",
 					Toast.LENGTH_SHORT).show();
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Geomancer.registerGeomancerListener(this);
 		mSearchFaerie = new Timer();
 		mSearchFaerie.schedule(new SearchLogic(), 0, SEARCH_DELAY);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Geomancer.removeGeomancerListener(this);
 		mSearchFaerie.cancel();
 	}
 
 	@Override
 	public void updateLocation(Location loc) {
 		findAndSetClosestPlace(loc);
 	}
 	
 	private void findAndSetClosestPlace(Location loc) {
 		if(loc == null) {
 			return;
 		}
 		
 		int closestIx = Geomancer.findClosestPlace(loc, mAllPlacesCursor);
 		if(closestIx == -1) {
 			Toast.makeText(getActivity(), "Couldn't find closest place", Toast.LENGTH_LONG).show();
 		} else {
 			mAllPlacesCursor.moveToPosition(closestIx);
 			setCurrentPlace(DBUtils.getPlaceFromCursor(mAllPlacesCursor));
 		}
 	}
 
 }
