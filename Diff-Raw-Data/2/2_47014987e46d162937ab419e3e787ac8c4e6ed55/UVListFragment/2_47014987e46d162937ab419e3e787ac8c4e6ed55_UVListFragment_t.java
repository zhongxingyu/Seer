 package fr.utc.assos.uvweb;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.github.espiandev.showcaseview.ShowcaseView;
 import fr.utc.assos.uvweb.adapters.UVListAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 import fr.utc.assos.uvweb.util.ConfigHelper;
 
 import java.util.List;
 
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
 	private static final String TAG = "UVListFragment";
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
 	private boolean mIsLoadingUV = false;
 	private String mQuery;
	private static final ShowcaseView.ConfigOptions mOptions = new ShowcaseView.ConfigOptions();
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public UVListFragment() {
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
 		setRetainInstance(true);
 
 		// ShowcaseView options
 		mOptions.shotType = ShowcaseView.TYPE_ONE_SHOT;
 
 		// Restore the previously serialized activated item position.
 		if (savedInstanceState == null) {
 			mShowDefaultDetailFragment = true;
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_uv_list,
 				container, false);
 
 		// ListView setup
 		mListView = (FastscrollThemedStickyListHeadersListView) rootView.findViewById(android.R.id.list);
 		mListView.setOnItemClickListener(this);
 		mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
 
 		if (mTwoPane) {
 			setupTwoPaneUi();
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		// Adapter setup
 		mAdapter = new UVListAdapter(getSherlockActivity());
 		mAdapter.setSearchCallbacks(this);
 		mAdapter.updateUVs(UVwebContent.UVS);
 		mListView.setAdapter(mAdapter);
 	}
 
 	@Override
 	public void onDestroyView() {
 		// Resources cleanup
 		mListView = null;
 		mAdapter = null;
 
 		super.onDestroyView();
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
 		final UVwebContent.UV UV = mAdapter.getItem(position);
 		final String toBeDisplayed = UV.getName();
 
 		if (!mTwoPane || ConfigHelper.hasSeveralFragmentConfigurations(getSherlockActivity(),
 				Configuration.ORIENTATION_PORTRAIT) || !TextUtils.equals(toBeDisplayed, mDisplayedUVName)) {
 			// If in tablet mode and the dislayed UV is not the same as the UV clicked, or in phone mode
 			// Lazy load the selected UV
 			mIsLoadingUV = true;
 
 			mCallbacks.onItemSelected(toBeDisplayed);
 			mDisplayedUVName = toBeDisplayed;
 			mActivatedPosition = position;
 		}
 	}
 
 	public void setIsTwoPane(boolean twoPane) {
 		mTwoPane = twoPane;
 	}
 
 	/**
 	 * Turns on activate-on-click mode. When this mode is on, list items will be
 	 * given the 'activated' state when touched.
 	 * Careful, this method should only be called in two-pane mode (i.e. tablet)
 	 */
 	private void setupTwoPaneUi() {
 		// In two-pane mode, list items should be given the
 		// 'activated' state when touched.
 		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		mListView.setVerticalScrollbarPosition(ListView.SCROLLBAR_POSITION_LEFT);
 		if (mShowDefaultDetailFragment) {
 			mCallbacks.showDefaultDetailFragment();
 			mShowDefaultDetailFragment = false;
 		}
 		//getSherlockActivity().getWindow().setBackgroundDrawable(null);
 	}
 
 	private void setActivatedPosition(int position) {
 		if (position == ListView.INVALID_POSITION) {
 			mListView.setItemChecked(mActivatedPosition, false);
 		} else {
 			mListView.setItemChecked(position, true);
 		}
 
 		mActivatedPosition = position;
 	}
 
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		if (ConfigHelper.hasSeveralFragmentConfigurations(getSherlockActivity(),
 				Configuration.ORIENTATION_PORTRAIT)) {
 			// Workaround: on a device like the Nexus 7 which has two different fragment configurations,
 			// we need to manually remove the items when changing orientation
 			menu.removeItem(R.id.menu_refresh);
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		Log.d(TAG, "onCreateOptionsMenu");
 		inflater.inflate(R.menu.fragment_uv_list, menu);
 
 		// SearchView configuration
 		final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
 		final UVwebSearchView searchView = (UVwebSearchView) searchMenuItem.getActionView();
 		if (mQuery != null && !TextUtils.isEmpty(mQuery)) {
 			//searchView.onActionViewExpanded();
 			searchView.setQuery(mQuery, false);
 		}
 		searchView.setIsLoadingUV(mIsLoadingUV);
 		searchView.setOnQueryTextListener(this);
 		Log.d(TAG, "mQuery == " + mQuery);
 
 		// We can't call onCloseListener() since it's broken on ICS+
 		searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
 			@Override
 			public boolean onMenuItemActionExpand(MenuItem item) {
 				return true;
 			}
 
 			@Override
 			public boolean onMenuItemActionCollapse(MenuItem item) {
 				mListView.setFastScrollEnabled(true);
 				mAdapter.getFilter().filter(null);
 				if (mIsLoadingUV) {
 					mIsLoadingUV = false;
 					searchView.setIsLoadingUV(mIsLoadingUV);
 				}
 				return true;
 			}
 		});
 
 		// ShowcaseView configuration
 		ShowcaseView.insertShowcaseViewWithType(
 				ShowcaseView.ITEM_ACTION_ITEM,
 				R.id.menu_search,
 				getSherlockActivity(),
 				"Rechercher des UVs ici",
 				null,
 				mOptions);
 	}
 
 	/**
 	 * {@link UVListAdapter} interface callbacks for search implementation
 	 */
 	@Override
 	public void onItemsFound(final List<UVwebContent.UV> results) {
 		if (mTwoPane && results.size() == 1) {
 			performClick(0);
 		} else {
 			//setActivatedPosition();
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
 		if (TextUtils.isEmpty(newText)) {
 			mListView.setFastScrollEnabled(true);
 		} else {
 			mListView.setFastScrollEnabled(false); // Workaround to avoid broken fastScroll
 			// when in search mode
 		}
 		mQuery = newText;
 		mAdapter.getFilter().filter(newText);
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
