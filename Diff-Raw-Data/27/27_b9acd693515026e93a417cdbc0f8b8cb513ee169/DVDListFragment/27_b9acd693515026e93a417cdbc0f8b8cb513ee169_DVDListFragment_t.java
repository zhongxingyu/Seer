 package uk.co.dazcorp.android.upcomingdvds;
 
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.manuelpeinado.refreshactionitem.ProgressIndicatorType;
 import com.manuelpeinado.refreshactionitem.RefreshActionItem;
 import com.manuelpeinado.refreshactionitem.RefreshActionItem.RefreshActionListener;
 
 import java.util.ArrayList;
 
 /**
  * A list fragment representing a list of DVDs. This fragment also supports
  * tablet devices by allowing list items to be given an 'activated' state upon
  * selection. This helps indicate which item is currently being viewed in a
  * {@link DVDDetailFragment}.
  * <p>
  * Activities containing this fragment MUST implement the {@link Callbacks}
  * interface.
  */
 public class DVDListFragment extends ListFragment implements
 		RefreshActionListener, OnNavigationListener {
 
 	/**
 	 * A callback interface that all activities containing this fragment must
 	 * implement. This mechanism allows activities to be notified of item
 	 * selections.
 	 */
 	public interface Callbacks {
 		/**
 		 * Callback for when an item has been selected.
 		 */
 		public void onItemSelected(JSONObject item);
 	}
 
	public static final String ERROR = "error";

 	public class ResponseReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String result = intent.getExtras().getString(WebService.RESULT);
			if (result.equals(ERROR)) {
				return;
			}
 			JSONObject upcoming = null;
 			try {
 				upcoming = new JSONObject(result);
 				// Save some data
 				// TODO: save movie/dvd results seperatly
 				SharedPreferences.Editor ed = mPrefs.edit();
 				ed.putString(MOVIES_DATA, result);
 				ed.putLong(MOVIES_TIME, System.currentTimeMillis());
 				ed.commit();
 
 			} catch (JSONException e) {
 				Toast.makeText(getActivity(), "Parsing the feed went wrong!",
 						Toast.LENGTH_LONG).show();
 				e.printStackTrace();
 			}
			if (upcoming != null) {
				processResult(upcoming);
			}

 			mRefreshActionItem.showProgress(false);
 
 		}
 	}
 
 	private static final Long HOUR = (long) 3600000;
 
 	private static final String MOVIES_DATA = "movies_data";
 
 	private static final String PREFS = "dvdlistprefs";
 
 	// The type of content we are currently looking at
 	public int CURRENT_VIEW = 0;
 
 	/**
 	 * A dummy implementation of the {@link Callbacks} interface that does
 	 * nothing. Used only when this fragment is not attached to an activity.
 	 */
 	private static Callbacks sDummyCallbacks = new Callbacks() {
 		@Override
 		public void onItemSelected(JSONObject item) {
 		}
 	};
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * activated item position. Only used on tablets.
 	 */
 	private static final String STATE_ACTIVATED_POSITION = "activated_position";
 	/**
 	 * The current activated item position. Only used on tablets.
 	 */
 	private int mActivatedPosition = ListView.INVALID_POSITION;
 	private JSONArrayAdapter mAdapter;
 
 	/**
 	 * The fragment's current callback object, which is notified of list item
 	 * clicks.
 	 */
 	private Callbacks mCallbacks = sDummyCallbacks;
 
 	private final ArrayList<JSONObject> mMovies = new ArrayList<JSONObject>();
 	private final String MOVIES_TIME = "movies_timestamp";
 
 	private SharedPreferences mPrefs;
 
 	private ResponseReceiver mReceiver;
 
 	private RefreshActionItem mRefreshActionItem;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public DVDListFragment() {
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		// Activities containing this fragment must implement its callbacks.
 		if (!(activity instanceof Callbacks)) {
 			throw new IllegalStateException(
 					"Activity must implement fragment's callbacks.");
 		}
 
 		mCallbacks = (Callbacks) activity;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mReceiver = new ResponseReceiver();
 		mAdapter = new JSONArrayAdapter(this.getActivity(),
 				R.layout.row_dvd_list, mMovies, new String[] {
 						ApiDetails.Upcoming.Movies.TAG_TITLE,
 						ApiDetails.Upcoming.Movies.TAG_YEAR,
 						ApiDetails.Upcoming.Movies.Posters.TAG_PROFILE,
 						ApiDetails.Upcoming.Movies.ReleaseDates.TAG_DVDDATE
 
 				}, new int[] { R.id.list_item_title, R.id.list_item_year,
 						R.id.list_item_thumbnail, R.id.list_item_release_date });
 		this.setListAdapter(mAdapter);
 
 		mPrefs = getActivity()
 				.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
 		String movies = mPrefs.getString(MOVIES_DATA, null);
 		Long savedAt = mPrefs.getLong(MOVIES_TIME, 0);
 		if (movies != null) {
 			// Have saved movies, use them
 			processResult(movies);
 		}
 		if (System.currentTimeMillis() > savedAt + HOUR) {
 			refreshData();
 		}
 
 		Context context = this.getActivity().getActionBar().getThemedContext();
 		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
 				context, R.array.menu, android.R.layout.simple_list_item_1);
 		list.setDropDownViewResource(android.R.layout.simple_list_item_1);
 		this.getActivity().getActionBar()
 				.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		this.getActivity().getActionBar()
 				.setListNavigationCallbacks(list, this);
 
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
 		menuInflater.inflate(R.menu.list_menu, menu);
 		MenuItem item = menu.findItem(R.id.list_refresh);
 		mRefreshActionItem = (RefreshActionItem) item.getActionView();
 		mRefreshActionItem.setMenuItem(item);
 		mRefreshActionItem
 				.setProgressIndicatorType(ProgressIndicatorType.INDETERMINATE);
 		mRefreshActionItem.setRefreshActionListener(this);
 		refreshData();
 
 	}
 
 	@Override
 	public void onRefreshButtonClick(RefreshActionItem sender) {
 		refreshData();
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		return inflater.inflate(R.layout.fragment_dvd_list, null);
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 
 		// Reset the active callbacks interface to the dummy implementation.
 		mCallbacks = sDummyCallbacks;
 	}
 
 	@Override
 	public void onListItemClick(ListView listView, View view, int position,
 			long id) {
 		super.onListItemClick(listView, view, position, id);
 
 		// Notify the active callbacks interface (the activity, if the
 		// fragment is attached to one) that an item has been selected.
 		mCallbacks.onItemSelected(mAdapter.getItem(position));
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		this.getActivity().unregisterReceiver(mReceiver);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		IntentFilter filter = new IntentFilter(WebService.ACTION_RESP);
 		filter.addCategory(Intent.CATEGORY_DEFAULT);
 		this.getActivity().registerReceiver(mReceiver, filter);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (mActivatedPosition != AdapterView.INVALID_POSITION) {
 			// Serialize and persist the activated item position.
 			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
 		}
 		// TODO: Save the list of movies here so we don't refetch on orientation
 		// change
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		// Restore the previously serialized activated item position.
 		if (savedInstanceState != null
 				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
 			setActivatedPosition(savedInstanceState
 					.getInt(STATE_ACTIVATED_POSITION));
 		}
 	}
 
 	public void processResult(JSONObject upcoming) {
 		JSONArray movies;
 		if (upcoming != null) {
 			try {
 				mAdapter.clear();
 				movies = upcoming.getJSONArray(ApiDetails.Upcoming.TAG_MOVIES);
 				for (int i = 0; i < movies.length(); i++) {
 					mAdapter.add(movies.getJSONObject(i));
 				}
 				mAdapter.notifyDataSetChanged();
 
 			} catch (JSONException e) {
 				Toast.makeText(getActivity(), "Parsing the feed went wrong!",
 						Toast.LENGTH_LONG).show();
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	/**
 	 * Turns on activate-on-click mode. When this mode is on, list items will be
 	 * given the 'activated' state when touched.
 	 */
 	public void setActivateOnItemClick(boolean activateOnItemClick) {
 		// When setting CHOICE_MODE_SINGLE, ListView will automatically
 		// give items the 'activated' state when touched.
 		getListView().setChoiceMode(
 				activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE
 						: AbsListView.CHOICE_MODE_NONE);
 	}
 
 	private void processResult(String movies) {
 		try {
 			processResult(new JSONObject(movies));
 		} catch (JSONException e) {
 			Toast.makeText(getActivity(), "Parsing the feed went wrong!",
 					Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		}
 
 	}
 
 	private void setActivatedPosition(int position) {
 		if (position == AdapterView.INVALID_POSITION) {
 			getListView().setItemChecked(mActivatedPosition, false);
 		} else {
 			getListView().setItemChecked(position, true);
 		}
 
 		mActivatedPosition = position;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		if (CURRENT_VIEW != itemPosition) {
 			CURRENT_VIEW = itemPosition;
 			refreshData();
 		}
 
 		return true;
 	}
 
 	private void refreshData() {
 		if (mRefreshActionItem != null) {
 			mRefreshActionItem.showProgress(true);
 		}
 
 		DVDListActivity.refreshData(this.getActivity(), CURRENT_VIEW);
 	}
 }
