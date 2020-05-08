 package com.johandahlberg.podr.ui;
 
 import com.johandahlberg.podr.BuildConfig;
 import com.johandahlberg.podr.R;
 import com.johandahlberg.podr.data.PodrContentProvider;
 import com.johandahlberg.podr.data.PodrDataHandler;
 import com.johandahlberg.podr.data.PodrOpenHelper;
 import com.johandahlberg.podr.data.Subscription;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.content.CursorLoader;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SubscriptionListFragment extends ListFragment implements
 		LoaderManager.LoaderCallbacks<Cursor> {
 	private static final String LOG_TAG = ".ui.SubscriptionListFragment";
 	private OnSubscriptionSelectedListener subscriptionSelectedListener;
 	private PodrDataHandler dataHandler;
 	private static final int REMOVE_SUBSCRIPTION_ID = 0;
 	private static final int ACTIVATE_AUTODOWNLOAD = 1;
 	private static final int DEACTIVATE_AUTODOWNLOAD = 2;
 
 	private SubscriptionAdapter mSubscriptionAdapter;
 
 	public interface OnSubscriptionSelectedListener {
 		public void onSubscriptionSelected(int subId);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		if (BuildConfig.DEBUG)
 			Log.d(LOG_TAG, "onActivityCreated()");
 		super.onActivityCreated(savedInstanceState);
 
 		getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
 		registerForContextMenu(getListView());
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		activity.getContentResolver().registerContentObserver(
 				PodrContentProvider.SUBSCRIPTION_CONTENT_URI, true, mObserver);
 
 		try {
 			subscriptionSelectedListener = (OnSubscriptionSelectedListener) activity;
 		} catch (ClassCastException e) {
 			Log.e(LOG_TAG, "Bad class", e);
 			throw new ClassCastException(activity.toString()
 					+ " must implement OnSubscriptionSelectedListener");
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG)
			Log.d(LOG_TAG, "onCreate()");
 		super.onCreate(savedInstanceState);
 
 		dataHandler = new PodrDataHandler(getActivity());
 		mSubscriptionAdapter = new SubscriptionAdapter(getActivity());
 		setListAdapter(mSubscriptionAdapter);
 		getLoaderManager().initLoader(0, null, this);
 
 		if (savedInstanceState == null) {
 			// mScrollToNow = true;
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
		if (BuildConfig.DEBUG)
			Log.d(LOG_TAG, "onCreateView()");
 		ViewGroup root = (ViewGroup) inflater.inflate(
 				R.layout.list_empty_container, container, false);
 		inflater.inflate(R.layout.list_loading,
 				(ViewGroup) root.findViewById(android.R.id.empty), true);
 		root.setBackgroundColor(Color.WHITE);
 		ListView listView = (ListView) root.findViewById(android.R.id.list);
 		listView.setItemsCanFocus(true);
 		listView.setCacheColorHint(Color.WHITE);
 		listView.setSelector(android.R.color.transparent);
 		return root;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		Subscription currentSubscription = dataHandler
 				.getSubscriptionById((int) info.id);
 		menu.add(0, REMOVE_SUBSCRIPTION_ID, 0, R.string.menu_remove);
 		if (currentSubscription.isAutoDownload()) {
 			menu.add(0, DEACTIVATE_AUTODOWNLOAD, 0,
 					R.string.menu_deactivate_autodownload);
 		} else {
 			menu.add(0, ACTIVATE_AUTODOWNLOAD, 0,
 					R.string.menu_activate_autodownload);
 		}
 
 	}
 
 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 
 		switch (item.getItemId()) {
 		case REMOVE_SUBSCRIPTION_ID: {
 			// Defines selection criteria for the rows you want to delete
 			String mSelectionClause = BaseColumns._ID + " LIKE ?";
 			String[] mSelectionArgs = { String.valueOf(info.id) };
 
 			// Deletes the words that match the selection criteria
 			int mRowsDeleted = getActivity().getContentResolver().delete(
 					PodrContentProvider.SUBSCRIPTION_CONTENT_URI,
 					mSelectionClause, mSelectionArgs);
 
 			if (mRowsDeleted > 0)
 				Toast.makeText(
 						getActivity().getApplicationContext(),
 						getString(R.string.remove_subscription) + ": "
 								+ String.valueOf(info.id), Toast.LENGTH_LONG)
 						.show();
 			return true;
 		}
 		case ACTIVATE_AUTODOWNLOAD: {
 			dataHandler.setAutodownload((int) info.id, true);
 			return true;
 		}
 		case DEACTIVATE_AUTODOWNLOAD: {
 			dataHandler.setAutodownload((int) info.id, false);
 			return true;
 		}
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		getActivity().getContentResolver().unregisterContentObserver(mObserver);
 	}
 
 	// LoaderManager.LoaderCallbacks<Cursor> methods
 
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		CursorLoader cursorLoader = new CursorLoader(getActivity(),
 				PodrContentProvider.SUBSCRIPTION_CONTENT_URI,
 				SubscriptionQuery.PROJECTION, null, null,
 				PodrOpenHelper.SUBSCRIPTION_COL_TITLE + " ASC");
 
 		return cursorLoader;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		String projection[] = { BaseColumns._ID };
 		Cursor catCursor = getActivity().getContentResolver().query(
 				Uri.withAppendedPath(
 						PodrContentProvider.SUBSCRIPTION_CONTENT_URI,
 						String.valueOf(id)), projection, null, null, null);
 
 		if (catCursor.moveToFirst()) {
 			subscriptionSelectedListener.onSubscriptionSelected(catCursor
 					.getInt(0));
 		}
 
 		catCursor.close();
 		l.setItemChecked(position, true);
 	}
 
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		mSubscriptionAdapter.changeCursor(cursor);
 	}
 
 	public void onLoaderReset(Loader<Cursor> loader) {
 		// mSubscriptionAdapter.swapCursor(null);
 	}
 
 	private final ContentObserver mObserver = new ContentObserver(new Handler()) {
 		@Override
 		public void onChange(boolean selfChange) {
 			if (getActivity() == null) {
 				return;
 			}
 
 			Loader<Cursor> loader = getLoaderManager().getLoader(0);
 			if (loader != null) {
 				loader.forceLoad();
 			}
 		}
 	};
 
 	private class SubscriptionAdapter extends CursorAdapter {
 
 		public SubscriptionAdapter(Context context, Cursor c) {
 			super(context, c, false);
 		}
 
 		public SubscriptionAdapter(Context context) {
 			super(context, null, false);
 		}
 
 		@Override
 		public void bindView(View view, Context context, final Cursor cursor) {
 			final String title = cursor
 					.getString(SubscriptionQuery.SUBSCRIPTION_TITLE);
 
 			final int autoDownload = cursor
 					.getInt(SubscriptionQuery.SUBSCRIPTION_AUTODOWNLOAD);
 
 			final TextView titleView = (TextView) view
 					.findViewById(R.id.subscription_title);
 			final TextView newEpisodesView = (TextView) view
 					.findViewById(R.id.subscription_newepisodes);
 			final TextView autoDownloadView = (TextView) view
 					.findViewById(R.id.subscription_autodownload);
 
 			titleView.setText(title);
 
 			int unreadEpisodes = dataHandler.getUnreadBySubscription(cursor
 					.getInt(SubscriptionQuery._ID));
 			if (unreadEpisodes == 0) {
 				newEpisodesView
 						.setText(getString(R.string.subscription_no_new_episode));
 			} else if (unreadEpisodes == 1) {
 				newEpisodesView
 						.setText(getString(R.string.subscription_one_new_episode));
 			} else {
 				newEpisodesView.setText(unreadEpisodes + " "
 						+ getString(R.string.subscription_x_new_episodes));
 			}
 
 			if (autoDownload == 1) {
 				autoDownloadView.setText("AUTO");
 			} else {
 				autoDownloadView.setText("");
 			}
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			return getActivity().getLayoutInflater().inflate(
 					R.layout.subscription_list_item, parent, false);
 		}
 	}
 
 	private interface SubscriptionQuery {
 		String[] PROJECTION = { BaseColumns._ID,
 				PodrOpenHelper.SUBSCRIPTION_COL_TITLE,
 				PodrOpenHelper.SUBSCRIPTION_COL_AUTODOWNLOAD };
 
 		int _ID = 0;
 		int SUBSCRIPTION_TITLE = 1;
 		int SUBSCRIPTION_AUTODOWNLOAD = 2;
 	}
 }
