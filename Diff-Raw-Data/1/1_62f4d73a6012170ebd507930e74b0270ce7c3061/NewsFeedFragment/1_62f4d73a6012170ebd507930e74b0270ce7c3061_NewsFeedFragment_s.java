 package fr.utc.assos.uvweb.ui;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.utc.assos.uvweb.R;
 import fr.utc.assos.uvweb.adapters.NewsFeedEntryAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 import fr.utc.assos.uvweb.io.NewsfeedTaskFragment;
 import fr.utc.assos.uvweb.io.base.BaseTaskFragment;
 import fr.utc.assos.uvweb.ui.base.UVwebFragment;
 import fr.utc.assos.uvweb.util.AnimationUtils;
 import fr.utc.assos.uvweb.util.ConnectionUtils;
 
 import static fr.utc.assos.uvweb.util.LogUtils.makeLogTag;
 
 /**
  * A list fragment representing a list of {@link UVwebContent.NewsFeedEntry}s.
  */
 public class NewsFeedFragment extends UVwebFragment implements
 		NewsfeedTaskFragment.Callbacks<List<UVwebContent.NewsFeedEntry>> {
 	private static final String TAG = makeLogTag(NewsFeedFragment.class);
 	private static final String STATE_NEWSFEED_ENTRIES = "newsfeed_entries";
 	private NewsFeedEntryAdapter mAdapter;
 	private MenuItem mRefreshMenuItem;
 	private ProgressBar mProgressBar;
 	private ListView mListView;
 	private boolean mNetworkError;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public NewsFeedFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if (container == null) {
 			return null;
 		}
 
 		final View rootView = inflater.inflate(R.layout.fragment_newsfeed, container, false);
 
 		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
 
 		mListView = (ListView) rootView.findViewById(android.R.id.list);
 		mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
 		// TODO: scale better on N10 using margin point (see IO @7h)
 
 		mAdapter = new NewsFeedEntryAdapter(getSherlockActivity());
 
 		final SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter
 				(mAdapter, AnimationUtils.CARD_ANIMATION_DELAY_MILLIS, AnimationUtils.CARD_ANIMATION_DURATION_MILLIS);
 		swingBottomInAnimationAdapter.setListView(mListView);
 
 		mListView.setAdapter(swingBottomInAnimationAdapter);
 
 		if (savedInstanceState != null) {
 			if (savedInstanceState.containsKey(STATE_NEWSFEED_ENTRIES)) {
 				final ArrayList<UVwebContent.NewsFeedEntry> savedNewsfeedEntries = savedInstanceState
 						.getParcelableArrayList(STATE_NEWSFEED_ENTRIES);
 				mAdapter.updateNewsFeedEntries(savedNewsfeedEntries);
 			} else {
 				// In this case, we have a configuration change
 				final SherlockFragmentActivity context = getSherlockActivity();
 				final NewsfeedTaskFragment newsfeedTaskFragment =
 						NewsfeedTaskFragment.get(context.getSupportFragmentManager(), this);
 				if (savedInstanceState.containsKey(STATE_NETWORK_ERROR)) {
 					if (!ConnectionUtils.isOnline(context)) {
 						handleNetworkError(context);
 					} else {
 						// If we previously had a network error, we can try and reload the list
 						newsfeedTaskFragment.startNewTask(BaseTaskFragment.THREAD_POOL_EXECUTOR_POLICY);
 					}
 				} else {
 					if (!ConnectionUtils.isOnline(context)) {
 						handleNetworkError(context);
 					} else {
 						if (!newsfeedTaskFragment.isRunning()) {
 							newsfeedTaskFragment.startNewTask(BaseTaskFragment.THREAD_POOL_EXECUTOR_POLICY);
 						} else {
 							// The task wasn't complete and is still running, we need to show the ProgressBar again
 							onPreExecute();
 						}
 					}
 				}
 			}
 		} else {
 			final SherlockFragmentActivity context = getSherlockActivity();
 			if (!ConnectionUtils.isOnline(context)) {
 				handleNetworkError(context);
 			} else {
 				NewsfeedTaskFragment.get(context.getSupportFragmentManager(), this)
 						.startNewTask(BaseTaskFragment.THREAD_POOL_EXECUTOR_POLICY);
 			}
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 
 		final MenuItem refreshItem = menu.findItem(R.id.menu_refresh_uvdetail);
 		if (refreshItem != null) {
 			refreshItem.setVisible(false);
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 
 		inflater.inflate(R.menu.fragment_newsfeed, menu);
 
 		mRefreshMenuItem = menu.findItem(R.id.menu_refresh_newsfeed);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_refresh_newsfeed:
 				final SherlockFragmentActivity context = getSherlockActivity();
 				if (!ConnectionUtils.isOnline(context)) {
 					handleNetworkError(context);
 				} else {
 					final NewsfeedTaskFragment newsfeedTaskFragment =
 							NewsfeedTaskFragment.get(context.getSupportFragmentManager(), this);
 					if (!newsfeedTaskFragment.isRunning()) {
 						newsfeedTaskFragment.startNewTask();
 					}
 				}
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		if (!mAdapter.isEmpty()) {
 			outState.putParcelableArrayList(STATE_NEWSFEED_ENTRIES, (ArrayList) mAdapter.getNewsfeedEntries());
 		}
 		if (mNetworkError) {
 			outState.putBoolean(STATE_NETWORK_ERROR, true);
 		}
 	}
 
 	@Override
 	public void onPreExecute() {
 		mListView.getEmptyView().setVisibility(View.GONE);
 		if (mRefreshMenuItem != null) {
 			mRefreshMenuItem.setActionView(R.layout.progressbar);
 		} else {
 			mProgressBar.setVisibility(View.VISIBLE);
 		}
 	}
 
 	@Override
 	public void onPostExecute(List<UVwebContent.NewsFeedEntry> entries) {
 		mAdapter.updateNewsFeedEntries(entries);
 		mListView.getEmptyView().setVisibility(View.VISIBLE);
 		if (mRefreshMenuItem != null && mRefreshMenuItem.getActionView() != null) {
 			mRefreshMenuItem.setActionView(null);
 		}
 		if (mProgressBar.getVisibility() == View.VISIBLE) {
 			mProgressBar.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public void onError() {
 		mNetworkError = true;
 		handleNetworkError();
 	}
 }
