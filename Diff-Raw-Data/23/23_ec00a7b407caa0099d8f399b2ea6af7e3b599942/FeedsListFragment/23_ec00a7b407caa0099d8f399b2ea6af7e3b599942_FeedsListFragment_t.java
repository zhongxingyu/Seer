 package pwr.rss.reader.fragments;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import pwr.rss.reader.ApplicationObject;
 import pwr.rss.reader.FeedDetailsActivity;
 import pwr.rss.reader.FeedsListActivity;
 import pwr.rss.reader.OnMenuListActionListener;
 import pwr.rss.reader.R;
 import pwr.rss.reader.database.adapters.FeedCursorAdapter;
 import pwr.rss.reader.database.adapters.SQLiteCursorLoader;
 import pwr.rss.reader.database.dao.Feed;
 import pwr.rss.reader.utils.AlertDialogBuilder;
 import pwr.rss.reader.utils.UndoableCollection;
 import pwr.rss.reader.utils.UndoableCollection.Action;
 import pwr.rss.reader.web.DownloadService;
 import pwr.rss.reader.web.WebEventReceiver;
 import squixy.sln.swipetodismiss.OnDismissCallback;
 import squixy.sln.swipetodismiss.SwipeDismissList;
 import squixy.sln.swipetodismiss.Undoable;
 import undobar.controller.library.UndoBarController;
 import undobar.controller.library.UndoBarListener;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
import android.os.Handler;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.TextUtils;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
 import com.actionbarsherlock.widget.SearchView;
 import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 
 public class FeedsListFragment extends SherlockListFragment implements OnRefreshListener2<ListView>,
 	LoaderManager.LoaderCallbacks<Cursor>, UndoBarListener, OnMenuListActionListener, OnDismissCallback,
 	OnItemLongClickListener {
 	
 	public static final String FLAG_POSITION = "FLAG_POSITION";
 	public static final String ACTION_REFRESH = "ACTION_REFRESH";
 	
 	private static final int LOADER_ID = 0x213;
 	private static int COLOR_BLUE;
 	private final Intent downloadIntent = new Intent(DownloadService.ACTION_START_DOWNLOAD);
 	
 	private FeedsListActivity activity;
 	private UndoBarController undoBarController;
 	private LoaderManager loaderManager;
 	private ApplicationObject applicationObject;
 	private FeedCursorAdapter feedCursorAdapter;
 	private PullToRefreshListView pullToRefreshListView;
 	private ListView listView;
 	private SwipeDismissList swipeDismissList;
 	private LocalBroadcastManager localBroadcastManager;
 	private SparseArray<View> checkedItems;
 	private ActionMode actionMode;
 	private boolean isInActionMode;
 	private FragmentManager fragmentManager;
 	private DialogFragmentInternetConnection dialogFragmentInternetConnection;
 	private BroadcastReceiver downloadFinishedReceiver;
 	private Resources resources;
	private Handler handler;
	private Runnable hireRefreshListHeaderRunnable;
 	
 	/***************************************
 	 ********** LIFECYCLE METHODS **********
 	 ***************************************/
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		this.setHasOptionsMenu(true);
 		
 		this.checkedItems = new SparseArray<View>();
 		this.isInActionMode = false;
 		this.resources = getResources();
 		this.activity = (FeedsListActivity) getSherlockActivity();
 		this.loaderManager = getLoaderManager();
 		this.feedCursorAdapter = new FeedCursorAdapter(activity, null);
 		this.applicationObject = (ApplicationObject) activity.getApplication();
 		this.localBroadcastManager = LocalBroadcastManager.getInstance(activity);
 		this.downloadFinishedReceiver = new WebEventReceiver(this);
		this.handler = new Handler();
		this.hireRefreshListHeaderRunnable = new Runnable() {
			@Override
			public void run() {
				pullToRefreshListView.onRefreshComplete();
			}
		};
 		
 		COLOR_BLUE = getResources().getColor(R.color.blue_selected);
 	}
 	
 	/**
 	 * Replaces android standard ListView of android.R.id.list ID in
 	 * SherlockListFragment to PullToRefreshListView
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		pullToRefreshListView = (PullToRefreshListView) inflater.inflate(R.layout.list_main, container);
 		pullToRefreshListView.setOnRefreshListener(this);
 		pullToRefreshListView.setEmptyView(inflater.inflate(R.layout.empty_list_view, container));
 		
 		View layout = super.onCreateView(inflater, container, savedInstanceState);
 		ListView listView = (ListView) layout.findViewById(android.R.id.list);
 		ViewGroup parent = (ViewGroup) listView.getParent();
 		
 		int listViewIndex = parent.indexOfChild(listView);
 		parent.removeViewAt(listViewIndex);
 		parent.addView(pullToRefreshListView, listViewIndex, listView.getLayoutParams());
 		
 		return layout;
 	}
 	
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		loaderManager.initLoader(LOADER_ID, null, this);
 		setListAdapter(feedCursorAdapter);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		ListView listView = getConfiguredListView();
 		this.swipeDismissList = new SwipeDismissList(listView, this, R.string.undobar_message_feed_dismissed);
 		this.undoBarController = new UndoBarController(listView);
 		this.undoBarController.registerUndoBarListener(this);
 		this.localBroadcastManager.registerReceiver(downloadFinishedReceiver, getIntentFilterForBroadcastReceiver());
 		this.dialogFragmentInternetConnection = new DialogFragmentInternetConnection();
 		this.fragmentManager = getFragmentManager();
 	}
 	
 	private IntentFilter getIntentFilterForBroadcastReceiver() {
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_COMPLETED);
 		intentFilter.addAction(DownloadService.ACTION_DEVICE_OFFLINE);
 		intentFilter.addAction(ACTION_REFRESH);
 		return intentFilter;
 	}
 	
 	@SuppressLint("InlinedApi")
 	private ListView getConfiguredListView() {
 		listView = pullToRefreshListView.getRefreshableView();
 		listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
 		listView.setOnItemLongClickListener(this);
 		return listView;
 	}
 	
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		loaderManager.destroyLoader(LOADER_ID);
 		undoBarController.unregisterUndoBarListener(this);
 		undoBarController.hideUndoBar();
 		swipeDismissList.discardUndo();
 		this.localBroadcastManager.unregisterReceiver(downloadFinishedReceiver);
 	}
 	
 	/***************************************
 	 ************* UNDO ACTIONS ************
 	 ***************************************/
 	
 	@Override
 	public void onUndo(Serializable token) {
 		UndoableCollection undoableCollection = (UndoableCollection) token;
 		
 		if (UndoableCollection.Action.READ == undoableCollection.action) {
 			undoReadingFeeds(undoableCollection);
 		}
 		else if (UndoableCollection.Action.DISMISSED == undoableCollection.action) {
 			undoDismissingFeeds(undoableCollection);
 		}
 		
 		restartLoader();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void undoDismissingFeeds(UndoableCollection undoableCollection) {
 		ArrayList<Feed> readFeeds = (ArrayList<Feed>) undoableCollection.list;
 		applicationObject.addFeeds(readFeeds);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void undoReadingFeeds(UndoableCollection undoableCollection) {
 		ArrayList<Long> readFeedsIDs = (ArrayList<Long>) undoableCollection.list;
 		applicationObject.markAsUnread(readFeedsIDs);
 	}
 	
 	@Override
 	public Undoable onDismiss(android.widget.AbsListView listView, int position) {
 		final Feed deletedFeed = dismissFeedAndReturn(position);
 		restartLoader();
 		
 		return new Undoable() {
 			@Override
 			public void onDismissUndo() {
 				applicationObject.addFeed(deletedFeed);
 				restartLoader();
 			}
 		};
 	}
 	
 	/***************************************
 	 ********* DATABASE OPERATIONS *********
 	 ***************************************/
 	private void markAllAsRead() {
 		ArrayList<Long> feedsMarkedAsReadIDs = applicationObject.markAllAsRead();
 		if (!feedsMarkedAsReadIDs.isEmpty()) {
 			createUndoAction(feedsMarkedAsReadIDs, R.string.undobar_message_read_all, UndoableCollection.Action.READ);
 		}
 		restartLoader();
 	}
 	
 	private Feed dismissFeedAndReturn(int position) {
 		Feed deletedFeed = getFeedFromList(position);
 		Long feedID = deletedFeed.ID;
 		applicationObject.deleteFeed(feedID);
 		return deletedFeed;
 	}
 	
 	private Feed markFeedAsReadAndReturn(int position) {
 		Feed currentFeed = getFeedFromList(position);
 		applicationObject.markFeedAsRead(currentFeed);
 		return currentFeed;
 	}
 	
 	private Feed getFeedFromList(int position) {
 		Cursor feedCursor = (Cursor) feedCursorAdapter.getItem(position);
 		Feed currentFeed = applicationObject.getFeed(feedCursor);
 		return currentFeed;
 	}
 	
 	@Override
 	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
 		refresh();
 	}
 	
 	private void refresh() {
 		if (applicationObject.isConnectedToInternet()) {
 			activity.sendBroadcast(downloadIntent);
 		}
 		else {
 			showAlertDeviceOffline();
			hireRefreshListHeader();
 		}
 		activity.setSupportProgressBarIndeterminateVisibility(false);
 	}
 	
	private void hireRefreshListHeader() {
		handler.postDelayed(hireRefreshListHeaderRunnable, 100);
	}
	
 	public void showAlertDeviceOffline() {
 		if (FeedsListActivity.NEW_API()) {
 			if (!dialogFragmentInternetConnection.isAdded()) {
 				dialogFragmentInternetConnection.show(fragmentManager, "DialogFragmentInternetConnection");
 			}
 		}
 		else {
 			AlertDialogBuilder.getAlertDialog(activity).show();
 		}
 	}
 	
 	@Override
 	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
 		markAllAsRead();
 	}
 	
 	/***************************************
 	 ************ MENU EVENTS **************
 	 ***************************************/
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		
 		inflater.inflate(R.menu.list_menu, menu);
 		configureSearchAction(menu);
 	}
 	
 	private void configureSearchAction(Menu menu) {
 		final MenuItem moreItem = menu.findItem(R.id.menu_list_moreover);
 		final MenuItem searchItem = menu.findItem(R.id.menu_search);
 		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
 			@Override
 			public boolean onMenuItemActionExpand(MenuItem item) {
 				moreItem.setVisible(false);
 				applicationObject.setFilterQuery("");
 				return true;
 			}
 			
 			@Override
 			public boolean onMenuItemActionCollapse(MenuItem item) {
 				moreItem.setVisible(true);
 				return true;
 			}
 		});
 		
 		final SearchView searchView = (SearchView) searchItem.getActionView();
 		searchView.setSubmitButtonEnabled(true);
 		searchView.setOnQueryTextListener(onQueryTextListener);
 	}
 	
 	private final OnQueryTextListener onQueryTextListener = new OnQueryTextListener() {
 		private String currentFilter = "";
 		
 		@Override
 		public boolean onQueryTextSubmit(String query) {
 			restartLoaderIfFilterHasChanged(query);
 			return true;
 		}
 		
 		private void restartLoaderIfFilterHasChanged(String query) {
 			if (hasFilterChanged(query)) {
 				applicationObject.setFilterQuery(query);
 				currentFilter = query;
 				restartLoader();
 			}
 		}
 		
 		private boolean hasFilterChanged(String newFilter) {
 			return !TextUtils.equals(currentFilter, newFilter);
 		}
 		
 		@Override
 		public boolean onQueryTextChange(String newText) {
 			restartLoaderIfFilterIsEmpty(newText);
 			return true;
 		}
 		
 		private void restartLoaderIfFilterIsEmpty(String newText) {
 			if (isEmpty(newText) && hasFilterChanged(newText)) {
 				applicationObject.setFilterQuery("");
 				restartLoader();
 			}
 		}
 		
 		private boolean isEmpty(String newText) {
 			return TextUtils.isEmpty(newText);
 		}
 	};
 	
 	@Override
 	public void notifyMenuRefresh() {
 		applicationObject.showBottomToast(R.string.message_refreshing_feeds_list);
 		refresh();
 	}
 	
 	@Override
 	public void notifyMenuMarkAllAsRead() {
 		markAllAsRead();
 	}
 	
 	/***************************************
 	 ********** LOADER MANAGEMENT **********
 	 ***************************************/
 	
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		return new SQLiteCursorLoader(activity);
 	}
 	
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		hireRefreshListHeader();
 		feedCursorAdapter.swapCursor(cursor);
 	}
 	
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		feedCursorAdapter.swapCursor(null);
 	}
 	
 	public void restartLoader() {
 		activity.updateUnreadCount();
 		if (loaderManager != null) {
 			loaderManager.restartLoader(LOADER_ID, null, this);
 		}
 	}
 	
 	/***************************************
 	 ******** ACTION MODES HANDLING ********
 	 ***************************************/
 	
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
 		switchActionMode();
 		
 		if (isInActionMode) {
 			startActionMode();
 			checkItem(position, v);
 		}
 		else {
 			uncolorAndClearAllItems();
 			finishActionMode();
 		}
 		
 		return true;
 	}
 	
 	private void switchActionMode() {
 		isInActionMode ^= true;
 	}
 	
 	private void checkItem(int position, View v) {
 		boolean isChecked = isItemChecked(position);
 		setItemChecked(position, v, !isChecked);
 		colorItem(v, !isChecked);
 	}
 	
 	private boolean isItemChecked(int position) {
 		boolean isChecked = false;
 		View v = checkedItems.get(position, null);
 		if (v != null) {
 			isChecked = true;
 		}
 		return isChecked;
 	}
 	
 	private void startActionMode() {
 		actionMode = activity.startActionMode(new ActionModeCallback());
 	}
 	
 	private void finishActionMode() {
 		actionMode.finish();
 		actionMode = null;
 		activity.getSupportActionBar().setDisplayShowCustomEnabled(true);
 	}
 	
 	private void setItemChecked(int position, View v, boolean isChecked) {
 		if (isChecked) {
 			checkedItems.put(position, v);
 		}
 		else {
 			checkedItems.delete(position);
 		}
 		
 		int count = checkedItems.size();
 		String countSelected = resources.getQuantityString(R.plurals.action_mode_selected, count, count);
 		actionMode.setTitle(countSelected);
 	}
 	
 	private void colorItem(View v, boolean isChecked) {
 		int color;
 		if (isChecked) {
 			color = COLOR_BLUE;
 		}
 		else {
 			color = Color.TRANSPARENT;
 		}
 		v.setBackgroundColor(color);
 	}
 	
 	private final class ActionModeCallback implements ActionMode.Callback {
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			activity.getSupportMenuInflater().inflate(R.menu.list_action_menu, menu);
 			return true;
 		}
 		
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			isInActionMode = true;
 			swipeDismissList.pause();
 			return true;
 		}
 		
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			int itemsSize = checkedItems.size();
 			
 			switch (item.getItemId()) {
 				case R.id.menu_dismiss:
 					dismissFeeds(itemsSize);
 					break;
 				case R.id.menu_mark_as_read:
 					markFeedsAsRead(itemsSize);
 					break;
 			}
 			
 			restartLoaderIfNecessary(itemsSize);
 			mode.finish();
 			return true;
 		}
 		
 		private void dismissFeeds(int itemsSize) {
 			ArrayList<Feed> feeds = new ArrayList<Feed>();
 			for (int i = itemsSize - 1; i >= 0; i--) {
 				int position = checkedItems.keyAt(i);
 				Feed dismissedFeed = dismissFeedAndReturn(position);
 				feeds.add(dismissedFeed);
 			}
 			createUndoAction(feeds, R.string.undobar_message_deleted_selected, Action.DISMISSED);
 		}
 		
 		private void markFeedsAsRead(int itemsSize) {
 			ArrayList<Long> feeds = new ArrayList<Long>();
 			for (int i = itemsSize - 1; i >= 0; i--) {
 				int position = checkedItems.keyAt(i);
 				Feed readFeed = markFeedAsReadAndReturn(position);
 				feeds.add(readFeed.ID);
 			}
 			createUndoAction(feeds, R.string.undobar_message_read_selected, Action.READ);
 		}
 		
 		private void restartLoaderIfNecessary(int itemsSize) {
 			if (itemsWillChange(itemsSize)) {
 				restartLoader();
 			}
 		}
 		
 		private boolean itemsWillChange(int itemsSize) {
 			return itemsSize != 0;
 		}
 		
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			isInActionMode = false;
 			swipeDismissList.resume();
 			uncolorAndClearAllItems();
 		}
 	}
 	
 	private void createUndoAction(ArrayList<? extends Serializable> feedsIDs, int messageId, Action actionType) {
 		UndoableCollection undoableAction = new UndoableCollection(feedsIDs, actionType);
 		undoBarController.showUndoBar(undoableAction, messageId);
 	}
 	
 	private void uncolorAndClearAllItems() {
 		int size = checkedItems.size();
 		for (int i = size - 1; i >= 0; i--) {
 			View view = checkedItems.valueAt(i);
 			colorItem(view, false);
 		}
 		checkedItems.clear();
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		
 		if (isInActionMode) {
 			checkItem(position, v);
 			clearCheckedItemsIfNoSelection();
 		}
 		else {
 			openFeedDetails(position);
 		}
 	}
 	
 	private void openFeedDetails(int position) {
 		startActivity(getFeedDetailsIntent(position - 1));
 		activity.overridePendingTransition(0, 0);
 	}
 	
 	private Intent getFeedDetailsIntent(int position) {
 		Intent openFeedDetails = new Intent(activity, FeedDetailsActivity.class);
 		openFeedDetails.putExtra(FLAG_POSITION, position);
 		return openFeedDetails;
 	}
 	
 	private void clearCheckedItemsIfNoSelection() {
 		if (noItemSelected()) {
 			checkedItems.clear();
 			finishActionMode();
 		}
 	}
 	
 	private boolean noItemSelected() {
 		return checkedItems.size() == 0;
 	}
 }
