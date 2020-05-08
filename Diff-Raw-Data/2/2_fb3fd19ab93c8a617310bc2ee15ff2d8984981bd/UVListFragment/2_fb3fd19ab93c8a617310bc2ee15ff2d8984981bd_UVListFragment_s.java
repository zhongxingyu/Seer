 package fr.utc.assos.uvweb;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import fr.utc.assos.uvweb.adapters.UVListAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 
 import java.util.List;
 
 import static fr.utc.assos.uvweb.util.LogUtils.makeLogTag;
 
 /**
  * A list fragment representing a list of {@link UVwebContent.UV}s. This fragment also supports
  * tablet devices by allowing list items to be given an 'activated' state upon
  * selection. This helps indicate which item is currently being viewed in a
  * {@link UVDetailFragment}.
  * <p/>
  * Activities containing this fragment MUST implement the {@link Callbacks} interface.
  */
 public class UVListFragment extends SherlockFragment implements AdapterView.OnItemClickListener,
 		UVListAdapter.SearchCallbacks, UVwebSearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * activated item position. Only used on tablets.
 	 */
 	public static final String STATE_DISPLAYED_UV = "displayed_uv";
 	public static final String STATE_SEARCH_QUERY = "search_query";
 	/**
 	 * The fragment argument representing whether the layout is in twoPane mode or not.
 	 */
 	private static final String STATE_TWO_PANE = "two_pane";
 	private static final String TAG = makeLogTag(UVListFragment.class);
 	/**
 	 * A dummy implementation of the {@link Callbacks} interface that does
 	 * nothing. Used only when this fragment is not attached to an activity.
 	 */
 	private static final Callbacks sDummyCallbacks = new Callbacks() {
 		@Override
 		public void onItemSelected(final String id) {
 		}
 
 		@Override
 		public void showDefaultDetailFragment() {
 		}
 	};
 	/**
 	 * The fragment's current callback object, which is notified of list item
 	 * clicks.
 	 */
 	private Callbacks mCallbacks = sDummyCallbacks;
 	/**
 	 * The associated ListView object
 	 */
 	private FastscrollThemedStickyListHeadersListView mListView;
 	/**
 	 * The {@link fr.utc.assos.uvweb.adapters.UVAdapter} ListAdapter instance
 	 */
 	private UVListAdapter mAdapter;
 	/**
 	 * The displayed UV name
 	 */
 	private String mDisplayedUVName;
 	private boolean mTwoPane;
 	private UVwebSearchView mSearchView;
 	private String mSearchQuery;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public UVListFragment() {
 	}
 
 	/**
 	 * Create a new instance of {@link UVListFragment} that will be initialized
 	 * with the given arguments.
 	 */
 	public static UVListFragment newInstance(final String displayedUVName,
 											 final String searchQuery, final boolean twoPane) {
 		final Bundle arguments = new Bundle();
 		if (displayedUVName != null) {
 			arguments.putString(STATE_DISPLAYED_UV, displayedUVName);
 		}
 		if (searchQuery != null) {
 			arguments.putString(STATE_SEARCH_QUERY, searchQuery);
 		}
 		arguments.putBoolean(STATE_TWO_PANE, twoPane);
 		final UVListFragment f = new UVListFragment();
 		f.setArguments(arguments);
 		return f;
 	}
 
 	public static UVListFragment newInstance(final boolean twoPane) {
 		return newInstance(null, null, twoPane);
 	}
 
 	public static UVListFragment newInstance() {
 		return newInstance(false);
 	}
 
 	// Fragment Lifecycle management
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
 
 		// Fragment configuration
 		setHasOptionsMenu(true);
 
 		final Bundle arguments = getArguments();
 		mTwoPane = arguments.getBoolean(STATE_TWO_PANE, false);
 		if (arguments.containsKey(STATE_SEARCH_QUERY)) {
 			mDisplayedUVName = arguments.getString(STATE_DISPLAYED_UV);
 		}
 		if (arguments.containsKey(STATE_SEARCH_QUERY)) {
 			mSearchQuery = arguments.getString(STATE_SEARCH_QUERY);
 		}
 
 		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SEARCH_QUERY) && !mTwoPane) {
 			mSearchQuery = savedInstanceState.getString(STATE_SEARCH_QUERY);
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		final View rootView = inflater.inflate(R.layout.fragment_uv_list,
 				container, false);
 
 		// ListView setup
 		mListView = (FastscrollThemedStickyListHeadersListView) rootView.findViewById(android.R.id.list);
 		mListView.setOnItemClickListener(this);
 		mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
 
 		return rootView;
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		// Adapter setup
 		mAdapter = new UVListAdapter(getSherlockActivity());
 		mAdapter.setSearchCallbacks(this);
 		mAdapter.updateUVs(UVwebContent.UVS);
 		mListView.setAdapter(mAdapter);
 
 		setupTwoPaneUi();
 	}
 
 	@Override
 	public void onDetach() {
 		// Reset the active callbacks interface to the dummy implementation.
 		mCallbacks = sDummyCallbacks;
 
 		super.onDetach();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		// Notify the active callbacks interface (the activity, if the
 		// fragment is attached to one) that an item has been selected.
 		performClick(position);
 	}
 
 	private void performClick(final int position) {
 		final String toBeDisplayed = mAdapter.getItem(position).getName();
 
 		if (!mTwoPane || !TextUtils.equals(toBeDisplayed, mDisplayedUVName)) {
 			// If in tablet mode and the dislayed UV is not the same as the UV clicked, or in phone mode
 			// Lazy load the selected UV
 			mCallbacks.onItemSelected(toBeDisplayed);
 			mDisplayedUVName = toBeDisplayed;
 		}
 	}
 
 	public String getSearchQuery() {
 		return mSearchQuery;
 	}
 
 	public void setSearchQuery(String searchQuery) {
 		if (!TextUtils.equals(searchQuery, mSearchQuery)) {
 			mSearchQuery = searchQuery;
 		}
 	}
 
 	public String getDisplayedUVName() {
 		return mDisplayedUVName;
 	}
 
 	public void setDisplayedUV(String uvName) {
 		if (!TextUtils.equals(uvName, mDisplayedUVName)) {
 			mDisplayedUVName = uvName;
 		}
 	}
 
 	/**
 	 * Turns on activate-on-click mode. When this mode is on, list items will be
 	 * given the 'activated' state when touched.
 	 */
 	private void setupTwoPaneUi() {
 		if (mTwoPane) {
 			// In two-pane mode, list items should be given the
 			// 'activated' state when touched.
 			mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			mListView.setVerticalScrollbarPosition(ListView.SCROLLBAR_POSITION_LEFT);
 			if (mDisplayedUVName == null) {
 				mCallbacks.showDefaultDetailFragment();
 			}
			//getSherlockActivity().getWindow().setBackgroundDrawable(null); // TODO: Reduce overdraw on tablets
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.fragment_uv_list, menu);
 
 		// SearchView configuration
 		final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
 
 		// We can't call onCloseListener() since it's broken on ICS+
 		searchMenuItem.setOnActionExpandListener(this);
 
 		mSearchView = (UVwebSearchView) searchMenuItem.getActionView();
 		mSearchView.setOnQueryTextListener(this);
 		if (mSearchQuery != null) {
 			mSearchView.setQuery(mSearchQuery, false);
 			searchMenuItem.expandActionView();
 			mSearchView.requestFocus();
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_search:
 				mSearchView.setIsLoadingUV(false);
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	/**
 	 * {@link UVListAdapter} interface callbacks for search implementation
 	 */
 	@Override
 	public void onItemsFound(final List<UVwebContent.UV> results) {
 		if (mTwoPane && results.size() == 1) {
 			final String toBeDisplayed = results.get(0).getName();
 			if (!TextUtils.equals(toBeDisplayed, mDisplayedUVName)) {
 				mListView.setItemChecked(0, true);
 				performClick(0);
 			}
 		}
 	}
 
 	@Override
 	public void onNothingFound() {
 		if (mTwoPane && mDisplayedUVName != null) {
 			mCallbacks.showDefaultDetailFragment();
 		}
 		mDisplayedUVName = null;
 	}
 
 	/**
 	 * {@link UVwebSearchView} interface callbacks for text submission
 	 */
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 		return false;
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 		if (mListView != null) {
 			mListView.setFastScrollEnabled(TextUtils.isEmpty(newText)); // Workaround to avoid broken fastScroll
 			// when in search mode
 			mAdapter.getFilter().filter(newText);
 			mSearchQuery = newText;
 		}
 		return true;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		if (!mTwoPane) {
 			if (mSearchQuery != null) {
 				outState.putString(STATE_SEARCH_QUERY, mSearchQuery);
 			}
 			if (mDisplayedUVName != null) {
 				outState.putString(STATE_DISPLAYED_UV, mDisplayedUVName);
 			}
 		}
 	}
 
 	/**
 	 * {@link MenuItem.OnActionExpandListener} interface callbacks
 	 */
 	@Override
 	public boolean onMenuItemActionExpand(MenuItem item) {
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemActionCollapse(MenuItem item) {
 		mSearchView.setQuery(null, false);
 		mListView.setFastScrollEnabled(true);
 		mSearchQuery = null;
 		return true;
 	}
 
 	/**
 	 * A callback interface that all activities containing this fragment must
 	 * implement. This mechanism allows activities to be notified of item
 	 * selections.
 	 */
 	public interface Callbacks {
 		/**
 		 * Callback for when an item has been selected.
 		 */
 		public void onItemSelected(final String id);
 
 		/**
 		 * Callback to display the default DetailFragment.
 		 */
 		public void showDefaultDetailFragment();
 	}
 }
