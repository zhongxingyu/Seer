 package ch.almana.android.stechkarte.view.fragment;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import ch.almana.android.stechkarte.R;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.provider.DB;
 import ch.almana.android.stechkarte.provider.DB.Holidays;
 import ch.almana.android.stechkarte.provider.DB.holidayTypes;
 import ch.almana.android.stechkarte.utils.DialogCallback;
 import ch.almana.android.stechkarte.utils.MenuHelper;
 
 public class HolidaysListFragment extends ListFragment implements DialogCallback, LoaderCallbacks<Cursor> {
 
 	private SimpleCursorAdapter adapter;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		ListView listView = getListView();
 		//		View header = getLayoutInflater(savedInstanceState).inflate(R.layout.monthlist_header, listView, false);
 		//		listView.addHeaderView(header);
 
 		setListShown(false);
 		getLoaderManager().initLoader(0, null, this);
 		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2, null, new String[] { DB.Holidays.NAME_START, DB.Holidays.NAME_COMMENT },
 				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
 
 		adapter.setViewBinder(new ViewBinder() {
 			@Override
 			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 				if (cursor == null) {
 					return false;
 				}
 				if (columnIndex == DB.Holidays.INDEX_START) {
 					DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
 					StringBuilder sb = new StringBuilder();
 					long start = cursor.getLong(Holidays.INDEX_START);
 					sb.append(dateFormat.format(new Date(start)));
 					sb.append(" - ");
 					sb.append(dateFormat.format(new Date(cursor.getLong(Holidays.INDEX_END))));
 					sb.append(" ");
 					sb.append(Float.toString(cursor.getFloat(Holidays.INDEX_DAYS)));
 					sb.append(" days");
 					((TextView) view).setText(sb.toString());
 					return true;
 				}
 				if (columnIndex == DB.Holidays.INDEX_COMMENT) {
 					StringBuilder sb = new StringBuilder();
 					String comment = cursor.getString(Holidays.INDEX_COMMENT);
 					CursorLoader cursorLoader = new CursorLoader(getActivity(), holidayTypes.CONTENT_URI, holidayTypes.DEFAULT_PROJECTION, DB.SELECTION_BY_ID,
 							new String[] { Long.toString(cursor.getLong(Holidays.INDEX_TYPE)) }, null);
 					Cursor c = cursorLoader.loadInBackground();
 					if (c != null && c.moveToFirst()) {
 						sb.append(c.getString(holidayTypes.INDEX_NAME));
 					}
 					if (c != null) {
 						c.close();
 					}
 					if (!TextUtils.isEmpty(comment)) {
 						sb.append(", ");
 						sb.append(comment);
 					}
 					if (cursor.getInt(Holidays.INDEX_IS_HOLIDAY) == 1) {
 						sb.append(", ");
 						sb.append(getString(R.string.cbIsHoliday));
 					}
 					if (cursor.getInt(Holidays.INDEX_IS_PAID) == 1) {
 						sb.append(", ");
 						sb.append(getString(R.string.CheckBoxIsPayed));
 					}
 					if (cursor.getInt(Holidays.INDEX_YIELDS_OVERTIME) == 1) {
 						sb.append(", ");
 						sb.append(getString(R.string.cbYieldsOvertime));
 					}
 					if (cursor.getInt(Holidays.INDEX_IS_YEARLY) == 1) {
 						sb.append(", ");
 						sb.append(getString(R.string.cbIsYearly));
 					}
 					((TextView) view).setText(sb.toString());
 					return true;
 				}
 				return false;
 			}
 		});
 
 		getListView().setAdapter(adapter);
 		getListView().setOnCreateContextMenuListener(this);
 		// dia.dismiss();
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.monthlist_option, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return MenuHelper.handleCommonOptions(getContext(), item);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		if (id < 0) {
 			return;
 		}
 		Uri uri = ContentUris.withAppendedId(Holidays.CONTENT_URI, id);
 		startActivity(new Intent(Intent.ACTION_EDIT, uri));
 	}
 
 	@Override
 	public void finished(boolean success) {
 
 	}
 
 	@Override
 	public Context getContext() {
 		return getActivity();
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		return new CursorLoader(getActivity(), DB.Holidays.CONTENT_URI, DB.Holidays.DEFAULT_PROJECTION, null, null, Holidays.DEFAULT_SORTORDER);
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
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		getActivity().getMenuInflater().inflate(R.menu.holidaylist_context, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		super.onContextItemSelected(item);
 
 		final AdapterView.AdapterContextMenuInfo info;
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		} catch (ClassCastException e) {
 			Log.e(Logger.TAG, "bad menuInfo", e);
 			return false;
 		}
 
 		switch (item.getItemId()) {
 		case R.id.itemDeleteHoliday:
 			Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.title_delete_holidays);
			builder.setMessage(R.string.msg_delete_holidays);
 			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					getActivity().getContentResolver().delete(Holidays.CONTENT_URI, DB.SELECTION_BY_ID, new String[] { Long.toString(info.id) });
 				}
 			});
 			builder.setNegativeButton(android.R.string.no, null);
 			builder.create().show();
 			return true;
 		}
 		return false;
 
 	}
 }
