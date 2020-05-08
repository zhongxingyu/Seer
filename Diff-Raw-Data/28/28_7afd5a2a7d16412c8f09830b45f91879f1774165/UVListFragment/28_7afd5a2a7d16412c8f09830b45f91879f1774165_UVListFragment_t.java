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
 
 import static fr.utc.assos.uvweb.util.LogUtils.LOGD;
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
 		UVListAdapter.SearchCallbacks, UVwebSearchView.OnQueryTextListener {
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * activated item position. Only used on tablets.
 	 */
 	public static final String STATE_ACTIVATED_POSITION = "activated_position";
 	public static final String STATE_SEARCH_QUERY = "search_query";
 	private static final String TAG = makeLogTag(UVListFragment.class);
 	/**
 	 * Special mUVDisplayed case where no UV is actually displayed.
 	 */
 	private static final String NO_UV_DISPLAYED = "no_uv_displayed";
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
 	 * The current activated item position. Only used on tablets.
 	 */
 	private int mActivatedPosition = ListView.INVALID_POSITION;
 	/**
 	 * Indicates whether the default detail fragment has to be shown.
 	 */
 	private boolean mShowDefaultDetailFragment;
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
 	private String mDisplayedUVName = NO_UV_DISPLAYED;
 	private boolean mTwoPane = false;
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
 	public static UVListFragment newInstance(final int activatedPosition, final String searchQuery) {
 		final UVListFragment f = new UVListFragment();
 		if (activatedPosition == ListView.INVALID_POSITION) {
 			f.setShowDefaultDetailFragment(true);
 		} else {
 			f.setActivatedPosition(activatedPosition);
 		}
 		f.setSearchQuery(searchQuery);
 		return f;
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
 
 		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SEARCH_QUERY)) {
 			mSearchQuery = savedInstanceState.getString(STATE_SEARCH_QUERY);
 		}
 
 		setupTwoPaneUi();
 	}
 
 	@Override
 	public void onDetach() {
 		// Reset the active callbacks interface to the dummy implementation.
 		mCallbacks = sDummyCallbacks;
 
 		super.onDetach();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 							long id) {
 
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
 			mActivatedPosition = position;
 		}
 	}
 
 	public void setIsTwoPane(final boolean twoPane) {
 		mTwoPane = twoPane;
 	}
 
 	public int getActivatedPosition() {
 		return mActivatedPosition;
 	}
 
 	public void setActivatedPosition(final int position) {
 		mActivatedPosition = position;
 	}
 
 	public void setShowDefaultDetailFragment(final boolean showDefaultDetailFragment) {
 		mShowDefaultDetailFragment = showDefaultDetailFragment;
 	}
 
	public String getSearchQuery() {
		return mSearchQuery;
 	}
 
	public void setSearchQuery(final String searchQuery) {
		mSearchQuery = searchQuery;
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
 			if (mShowDefaultDetailFragment) {
 				mCallbacks.showDefaultDetailFragment();
 				mShowDefaultDetailFragment = false;
 			} else {
 				if (mActivatedPosition != ListView.INVALID_POSITION) {
 					mListView.setItemChecked(mActivatedPosition, true);
 				}
 			}
 			//getSherlockActivity().getWindow().setBackgroundDrawable(null); // Reduce overdraw on tablets
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		LOGD(TAG, "onCreateOptionsMenu");
 		inflater.inflate(R.menu.fragment_uv_list, menu);
 
 		// SearchView configuration
 		final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
 		mSearchView = (UVwebSearchView) searchMenuItem.getActionView();
 		mSearchView.setOnQueryTextListener(this);
 
 		// We can't call onCloseListener() since it's broken on ICS+
 		searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
 			@Override
 			public boolean onMenuItemActionExpand(MenuItem item) {
 				return true;
 			}
 
 			@Override
 			public boolean onMenuItemActionCollapse(MenuItem item) {
				mSearchView.setIsLoadingUV(false);
				mSearchView.setQuery(null, false);
 				mListView.setFastScrollEnabled(true);
 				mSearchQuery = null;
 				return true;
 			}
 		});
 
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
 		if (mTwoPane && results.size() == 1 && mActivatedPosition != 0) {
 			setActivatedPosition(0);
 			mListView.setItemChecked(0, true);
 			performClick(0);
 		}
 	}
 
 	@Override
 	public void onNothingFound() {
 		if (mTwoPane) {
 			mCallbacks.showDefaultDetailFragment();
 		}
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
 
 		if (mSearchQuery != null && !mTwoPane) {
 			outState.putString(STATE_SEARCH_QUERY, mSearchQuery);
 		}
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
