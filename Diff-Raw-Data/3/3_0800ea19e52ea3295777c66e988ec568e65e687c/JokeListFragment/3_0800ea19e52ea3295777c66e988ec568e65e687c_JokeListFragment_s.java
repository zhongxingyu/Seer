 package com.wxk.jokeandroidapp.ui.fragment.app;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
 import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
 import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 
 import com.wxk.jokeandroidapp.App;
 import com.wxk.jokeandroidapp.R;
 import com.wxk.jokeandroidapp.bean.JokeBean;
 import com.wxk.jokeandroidapp.ui.activity.app.DetailActivity;
 import com.wxk.jokeandroidapp.ui.activity.app.MainActivity;
 import com.wxk.jokeandroidapp.ui.adapter.JokeAdapter;
 import com.wxk.jokeandroidapp.ui.fragment.BaseListFragment;
 import com.wxk.jokeandroidapp.services.JokeService;
 import com.wxk.util.LogUtil;
 
 public class JokeListFragment extends BaseListFragment implements
 		OnScrollListener, OnRefreshListener {
 	public static final String ARG_JOKE_TOPIC = "52lxh:joke_topic";
 	private static final String TAG = "52lxh:JokeListFragment";
 	private BaseAdapter mAdapter;
 	private List<JokeBean> mJokeItems;
 	private JokeListReceiver mJokeListReceiver;
 	private int mPage = 1;
 	private int mTopic = 0;
 	private boolean mIsLoading = false;
 	PullToRefreshLayout mPullToRefreshLayout;
 
 	private class JokeListReceiver extends BroadcastReceiver {
 		private static final String TAG = "52lxh:JokeListFragment>>JokeListReceiver";
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.i(TAG, "::onReceive()");
 			mPullToRefreshLayout.setRefreshComplete();
 			if (isDetached() || isRemoving()) {
 				return;
 			}
 
 			final boolean isCached = intent.getBooleanExtra(
 					JokeService.EXTRA_CACHED, false);
 			final boolean isError = intent.getBooleanExtra(
 					JokeService.EXTRA_SERVER_ERROR, false);
 			final boolean isRefresh = intent.getBooleanExtra(
 					JokeService.EXTRA_REFRESH, false);
 			final boolean isNoData = intent.getBooleanExtra(
 					JokeService.EXTRA_NO_DATA, false);
 			final int topic = intent.getIntExtra(JokeService.ARG_JOKE_TOPIC, 0);
 			final int page = intent.getIntExtra(JokeService.ARG_JOKE_PAGE, 0);
 			List<JokeBean> fetched = JokeService.loadJokeFromCache(
 					getActivity(), mTopic, mPage);
 			if (isError) {
 				Toast.makeText(getActivity(),
 						getString(R.string.toast_error_network),
 						Toast.LENGTH_SHORT).show();
 			}
 
 			if (fetched != null && fetched.size() > 0) {
 
 				if ((mAdapter != null && !isRefresh) || page > 1) {
 					Log.d(TAG, "$$$+++++%%%--- append");
 					mJokeItems = new ArrayList<JokeBean>();
 					mJokeItems.addAll(fetched);
 
 					((JokeAdapter) mAdapter).appendWithItems(mJokeItems);
 					mAdapter.notifyDataSetChanged();
 
 				} else if (isRefresh || mAdapter == null
 						|| mAdapter.getCount() < 1) {
 					// UI refresh
 					Log.d(TAG, "$$$+++++%%%--- refresh");
 					mAdapter = new JokeAdapter(getActivity());
 					mJokeItems = new ArrayList<JokeBean>();
 					mJokeItems.addAll(fetched);
 					((JokeAdapter) mAdapter).fillWithItems(mJokeItems);
 					mAdapter.notifyDataSetChanged();
 					setListAdapter(mAdapter);
 				}
 
 			}
 			mIsLoading = false;
 
 			if (isCached) {
 				startPullDataService(false);
 				if (fetched != null)
 					Log.i(TAG, String.format("Cached data items:%s , topic=%s",
 							fetched.size(), topic));
 				else
 					Log.i(TAG, String.format("Cached no data !", topic));
 			}
 			if (isNoData) {
 				// Toast.makeText(getActivity(),
 				// getString(R.string.toast_no_refresh_data),
 				// Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	public static JokeListFragment newInstance(int topicId) {
 		JokeListFragment f = new JokeListFragment();
 		Bundle b = new Bundle();
 		b.putInt(ARG_JOKE_TOPIC, topicId);
 		f.setArguments(b);
 		return f;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.i(TAG, "::onCreate()");
 		super.onCreate(savedInstanceState);
 		// setRetainInstance(true);
 		final Bundle args = getArguments();
 		if (args != null) {
 			mTopic = args.getInt(ARG_JOKE_TOPIC);
 		}
 
 		registerReceiver();
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		Log.i(TAG, "::onActivityCreated()");
 		super.onActivityCreated(savedInstanceState);
 		if (MainActivity.class.isInstance(getActivity())) {
 		}
 		getListView().setOnScrollListener(this);
 		startPullDataService(true);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		// getLoaderManager().restartLoader(mTopic, null, this);
 		Log.i(TAG, "::onStart()");
 	}
 
 	private void registerReceiver() {
 		if (mJokeListReceiver == null) {
 			mJokeListReceiver = new JokeListReceiver();
 		}
 		IntentFilter refreshFilter = new IntentFilter(
 				JokeService.REFRESH_JOKE_UI_INTENT + mTopic);
 		getActivity().registerReceiver(mJokeListReceiver, refreshFilter);
 	}
 
 	private void startPullDataService(boolean isCached) {
 
 		final Intent startService = new Intent(getActivity(), JokeService.class);
 		startService.putExtra(JokeService.ARG_JOKE_TOPIC, mTopic);
 		startService.putExtra(JokeService.ARG_JOKE_PAGE, mPage);
 		startService.setAction(JokeService.GET_JOKE_DATA_INTENT);
 
 		getActivity().startService(startService);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		Log.i(TAG, "::onCreateView()");
 		// return super.onCreateView(inflater, container, savedInstanceState);
 		View view = inflater.inflate(R.layout.fragment_joke_list, null);
 		return view;
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		ViewGroup viewGroup = (ViewGroup) view;
 
 		// As we're using a ListFragment we create a PullToRefreshLayout
 		// manually
 		mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
 
 		// We can now setup the PullToRefreshLayout
 		ActionBarPullToRefresh
 				.from(getActivity())
 				// We need to insert the PullToRefreshLayout into the Fragment's
 				// ViewGroup
 				.insertLayoutInto(viewGroup)
 				// Here we mark just the ListView and it's Empty View as
 				// pullable
 				.theseChildrenArePullable(android.R.id.list, android.R.id.empty)
 				.listener(this).setup(mPullToRefreshLayout);
 	}
 
 	@Override
 	public void onPause() {
 		Log.i(TAG, "::onPause()");
 		super.onPause();
 
 	}
 
 	@Override
 	public void onStop() {
 		Log.i(TAG, "::onStop()");
 		super.onStop();
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.i(TAG, "::onDestroy()");
 		try {
 			if (mJokeListReceiver != null) {
 				getActivity().unregisterReceiver(mJokeListReceiver);
 			}
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Log.i(TAG, String.format("position=%s, id=%s", position, id));
 		Intent intentDetail = new Intent();
 		intentDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		intentDetail.putExtra(DetailActivity.EXTRA_JOKE_ID,
 				mAdapter.getItemId(position));
 
 		intentDetail.setClass(App.context, DetailActivity.class);
 		App.context.startActivity(intentDetail);
 	}
 
 	private void loadingMore() {
 		if (!mIsLoading) {
 			mIsLoading = true;
 			mPage = mPage + 1;
 			startPullDataService(true);
 		}
 	}
 
 	@Override
 	public void onRefreshStarted(View view) {
 		mPage = 1;
 		startPullDataService(false);
 	}
 
 	private int mLastItem;
 	private int mFirstItem;
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		mFirstItem = firstVisibleItem;// - (isFooter ? 1 : 0) - (isHeader ? 1 :
 										// 0);
 		mLastItem = firstVisibleItem + visibleItemCount;
 
 		// LogUtil.i(TAG, "firstItem=" + firstItem + " ,lastItem=" + lastItem);
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
 		if (scrollState == SCROLL_STATE_IDLE
 				&& mLastItem >= mAdapter.getCount() - 4) {
 			LogUtil.i(TAG, "loading more ...");
 			loadingMore();
 		}
 		if (scrollState == SCROLL_STATE_IDLE && mFirstItem == 0) {
 		}
 	}
 
 }
