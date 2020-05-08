 package fr.utc.assos.uvweb;
 
 import android.app.Activity;
 import android.content.res.Configuration;
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
 import com.actionbarsherlock.widget.SearchView;
 import fr.utc.assos.uvweb.adapters.UVListAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 
 /**
  * A list fragment representing a list of UVs. This fragment also supports
  * tablet devices by allowing list items to be given an 'activated' state upon
  * selection. This helps indicate which item is currently being viewed in a
  * {@link UVDetailFragment}.
  * <p/>
  * Activities containing this fragment MUST implement the {@link Callbacks} interface.
  */
 public class UVListFragment extends SherlockFragment implements AdapterView.OnItemClickListener {
 	private static final String TAG = "UVListFragment";
 	/**
 	 * Special mUVDisplayed case where no UV is actually displayed.
 	 */
 	private static final String NO_UV_DISPLAYED = "no_uv_displayed";
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * activated item position. Only used on tablets.
 	 */
 	private static final String STATE_ACTIVATED_POSITION = "activated_position";
 	/**
 	 * A dummy implementation of the {@link Callbacks} interface that does
 	 * nothing. Used only when this fragment is not attached to an activity.
 	 */
 	private static Callbacks sDummyCallbacks = new Callbacks() {
 		@Override
 		public void onItemSelected(String id) {
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
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public UVListFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Fragment configuration
 		setHasOptionsMenu(true);
 		setRetainInstance(true);
 
 		// Adapter setup
 		mAdapter = new UVListAdapter(getSherlockActivity());
 		mAdapter.updateUVs(UVwebContent.UVS);
 
 		// Restore the previously serialized activated item position.
 		if (savedInstanceState != null
 				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
 			setActivatedPosition(savedInstanceState
 					.getInt(STATE_ACTIVATED_POSITION));
 		} else {
 			mShowDefaultDetailFragment = true;
 		}
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
 	public void onDetach() {
 		super.onDetach();
 
 		// Reset the active callbacks interface to the dummy implementation.
 		mCallbacks = sDummyCallbacks;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 							long id) {
 
 		// Notify the active callbacks interface (the activity, if the
 		// fragment is attached to one) that an item has been selected.
 		final String toBeDisplayed = UVwebContent.UVS.get(position).getName();
 		if (!toBeDisplayed.equalsIgnoreCase(mDisplayedUVName)) {
 			mCallbacks.onItemSelected(toBeDisplayed);
 			mDisplayedUVName = toBeDisplayed;
 			mActivatedPosition = position;
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (mActivatedPosition != ListView.INVALID_POSITION) {
 			// Serialize and persist the activated item position.
 			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
 		}
 	}
 
 	/**
 	 * Turns on activate-on-click mode. When this mode is on, list items will be
 	 * given the 'activated' state when touched.
	 * Carefull, this method is only called in two-pane mode (i.e. tablet)
 	 */
 	public void configureListView() {
 		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		mListView.setVerticalScrollbarPosition(ListView.SCROLLBAR_POSITION_LEFT);
 		if (mShowDefaultDetailFragment) {
 			mCallbacks.showDefaultDetailFragment();
 			mShowDefaultDetailFragment = false;
 		}
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
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_uv_list,
 				container, false);
 
 		// ListView setup
 		mListView = (FastscrollThemedStickyListHeadersListView) rootView.findViewById(android.R.id.list);
 		mListView.setOnItemClickListener(this);
 		mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
 		mListView.setAdapter(mAdapter);
 
 		return rootView;
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
 		inflater.inflate(R.menu.fragment_uv_list, menu);
 
 		// SearchView configuration
 		final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
 		searchView.setQueryHint(getResources().getString(R.string.search_uv_hint));
 		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				return false;
 			}
 
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				if (TextUtils.isEmpty(newText)) {
 					mListView.clearTextFilter();
 					mListView.setFastScrollEnabled(true);
 				} else {
 					mListView.setFastScrollEnabled(false); // Workaround to avoid broken fastScroll
 					// when in search mode
					mAdapter.getFilter().filter(newText);
 				}
 				return true;
 			}
 		});
		// TODO: workaround onCloseListener not working in order to setFastScrollEnabled(true) again
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
 		public void onItemSelected(String id);
 
 		/**
 		 * Callback to display the default DetailFragment.
 		 */
 		public void showDefaultDetailFragment();
 	}
 }
