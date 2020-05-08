 package ch.almana.android.stechkarte.view.fragment;
 
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import ch.almana.android.stechkarte.R;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.provider.DB;
 import ch.almana.android.stechkarte.provider.DB.Days;
 import ch.almana.android.stechkarte.utils.DeleteDayDialog;
 import ch.almana.android.stechkarte.utils.DialogCallback;
 import ch.almana.android.stechkarte.utils.MenuHelper;
 import ch.almana.android.stechkarte.view.adapter.DayItemAdapter;
 
 public class DaysListFragment extends ListFragment implements DialogCallback, LoaderCallbacks<Cursor> {
 
 	private DayItemAdapter adapter;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ListView listView = getListView();
 		View header = getLayoutInflater(savedInstanceState).inflate(R.layout.daylist_header, listView, false);
 		listView.addHeaderView(header);
 		// TODO make list header non clickable
 
 		adapter = new DayItemAdapter(getActivity(), null);
 		listView.setAdapter(adapter);
 
 		setListShown(false);
 		getLoaderManager().initLoader(0, null, this);
 
 		listView.setOnCreateContextMenuListener(this);
 		// dia.dismiss();
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.daylist_option, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return MenuHelper.handleCommonOptions(getActivity(), item);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		if (id < 0) {
 			return;
 		}
 		Uri uri = ContentUris.withAppendedId(Days.CONTENT_URI, id);
 		startActivity(new Intent(Intent.ACTION_EDIT, uri));
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		getActivity().getMenuInflater().inflate(R.menu.daylist_context, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		super.onContextItemSelected(item);
 
 		AdapterView.AdapterContextMenuInfo info;
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		} catch (ClassCastException e) {
 			Log.e(Logger.TAG, "bad menuInfo", e);
 			return false;
 		}
 
 		switch (item.getItemId()) {
 		case R.id.itemDeleteDay: {
 
 			DeleteDayDialog alert = new DeleteDayDialog(this, info.id);
			alert.setTitle(getString(R.string.title_delete_day));
 			alert.show();
 			return true;
 		}
 		}
 		return false;
 
 	}
 
 	@Override
 	public void finished(boolean success) {
 
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		return new CursorLoader(getActivity(), DB.Days.CONTENT_URI, DB.Days.DEFAULT_PROJECTION, null, null, Days.DEFAULT_SORTORDER);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
 		adapter.swapCursor(c);
 
 		// The list should now be shown.
 		if (isResumed()) {
 			setListShown(true);
 		} else {
 			setListShownNoAnimation(true);
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		adapter.swapCursor(null);
 	}
 
 	@Override
 	public Context getContext() {
 		return getActivity();
 	}
 
 }
