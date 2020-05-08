 /**
  *	Copyright 2010-2012 Norio bvba
  *
  *	This program is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *	
  *	This program is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *	
  *	You should have received a copy of the GNU General Public License
  *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.norio.twunch.android.ui;
 
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.graphics.drawable.AnimationDrawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import be.norio.twunch.android.R;
 import be.norio.twunch.android.TwunchActivity;
 import be.norio.twunch.android.TwunchApplication;
 import be.norio.twunch.android.TwunchesMapActivity;
 import be.norio.twunch.android.provider.TwunchContract.Twunches;
 import be.norio.twunch.android.service.SyncService;
 import be.norio.twunch.android.util.PrefsUtils;
 import be.norio.twunch.android.util.Util;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.apps.iosched.util.DetachableResultReceiver;
 
 public class TwunchListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
 
 	private CursorAdapter mAdapter;
 
 	private DetachableResultReceiver resultReceiver;
 
 	LocationManager locationManager;
 	LocationListener locationListener;
 
 	MenuItem refreshMenuItem;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setHasOptionsMenu(true);
 
 		// Acquire a reference to the system Location Manager
 		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 		locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				Cursor cursor = ((CursorAdapter) getListAdapter()).getCursor();
 				if (cursor != null) {
 					cursor.requery();
 				}
 			}
 
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				// Do nothing
 			}
 
 			public void onProviderEnabled(String provider) {
 				// Do nothing
 			}
 
 			public void onProviderDisabled(String provider) {
 				// Do nothing
 			}
 		};
 
 		resultReceiver = new DetachableResultReceiver(new Handler());
 		resultReceiver.setReceiver(new SyncResultReceiver());
 	}
 
	private final ContentObserver mChangesObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			Cursor cursor = ((CursorAdapter) getListAdapter()).getCursor();
			if (cursor != null) {
				cursor.requery();
			}
		}
	};

 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		mAdapter = new TwunchAdapter(getActivity());
 		setListAdapter(mAdapter);
 
 		setListShown(false);
 
 		getLoaderManager().initLoader(TwunchesQuery._TOKEN, getArguments(), this);
 	}
 
 	private interface TwunchesQuery {
 		int _TOKEN = 0x1;
 
 		String[] PROJECTION = { BaseColumns._ID, Twunches.TITLE, Twunches.ADDRESS, Twunches.DATE, Twunches.NUMPARTICIPANTS,
 				Twunches.LATITUDE, Twunches.LONGITUDE, Twunches.NEW };
 
 		int _ID = 0;
 		int NAME = 1;
 		int ADDRESS = 2;
 		int DATE = 3;
 		int NUMPARTICIPANTS = 4;
 		int LATITUDE = 5;
 		int LONGITUDE = 6;
 		int NEW = 7;
 	}
 
 	private static class ViewHolder {
 		public View rootView;
 		public TextView title;
 		public TextView address;
 		public TextView distance;
 		public TextView date;
 		public TextView days;
 
 		public ViewHolder(View view) {
 			rootView = view;
 			title = (TextView) view.findViewById(R.id.twunchTitle);
 			address = (TextView) view.findViewById(R.id.twunchAddress);
 			distance = (TextView) view.findViewById(R.id.twunchDistance);
 			date = (TextView) view.findViewById(R.id.twunchDate);
 			days = (TextView) view.findViewById(R.id.twunchDays);
 
 			view.setTag(this);
 		}
 	}
 
 	class TwunchAdapter extends CursorAdapter {
 
 		public TwunchAdapter(Context context) {
 			super(context, null);
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 
 			ViewHolder vh = (ViewHolder) view.getTag();
 			// Title
 			vh.title.setText(cursor.getString(TwunchesQuery.NAME));
 			// Address
 			vh.address.setText(cursor.getString(TwunchesQuery.ADDRESS));
 			vh.address.setTypeface(null, cursor.getInt(TwunchesQuery.NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);
 			// Distance
 			Float distance = Util.getDistanceToTwunch(view.getContext(), cursor.getFloat(TwunchesQuery.LATITUDE),
 					cursor.getFloat(TwunchesQuery.LONGITUDE));
 			vh.distance.setText(String.format(view.getContext().getString(R.string.distance), distance));
 			vh.distance.setVisibility(distance == null ? View.INVISIBLE : View.VISIBLE);
 			// Date
 			vh.date.setText(String.format(
 					view.getContext().getString(R.string.date),
 					DateUtils.formatDateTime(view.getContext(), cursor.getLong(TwunchesQuery.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
 							| DateUtils.FORMAT_SHOW_DATE),
 					DateUtils.formatDateTime(view.getContext(), cursor.getLong(TwunchesQuery.DATE), DateUtils.FORMAT_SHOW_TIME)));
 			vh.date.setTypeface(null, cursor.getInt(TwunchesQuery.NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);
 			// Days
 			final long msInDay = 86400000;
 			int days = (int) (cursor.getLong(TwunchesQuery.DATE) / msInDay - new Date().getTime() / msInDay);
 			vh.days.setText(days == 0 ? getString(R.string.today) : String.format(
 					getResources().getQuantityString(R.plurals.days_to_twunch, days), days));
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			View view = LayoutInflater.from(context).inflate(R.layout.twunch_list_item, parent, false);
 			new ViewHolder(view);
 			return view;
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
		getActivity().getContentResolver().registerContentObserver(Twunches.CONTENT_URI, true, mChangesObserver);
 		refreshTwunches(false);
 		// Start listening for location updates
 		String provider = locationManager.getBestProvider(new Criteria(), true);
 		if (provider != null) {
 			locationManager.requestLocationUpdates(provider, 300000, 500, locationListener);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
		getActivity().getContentResolver().unregisterContentObserver(mChangesObserver);
 		locationManager.removeUpdates(locationListener);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Intent intent = new Intent();
 		intent.setComponent(new ComponentName(v.getContext(), TwunchActivity.class));
 		intent.putExtra(TwunchActivity.PARAMETER_ID, ((Cursor) mAdapter.getItem(position)).getInt(TwunchesQuery._ID));
 		startActivity(intent);
 	}
 
 	public void refreshTwunches(boolean force) {
 		long lastSync = PrefsUtils.getLastUpdate();
 		long now = (new Date()).getTime();
 		long oneDay = 1000 * 60 * 60 * 24;
 		if (!force && lastSync != 0 && (now - lastSync < oneDay)) {
 			Log.d(TwunchApplication.LOG_TAG, "Not refreshing twunches");
 			return;
 		}
 		if (refreshMenuItem != null) {
 			refreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
 			((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable()).start();
 		}
 		Log.d(TwunchApplication.LOG_TAG, "Refreshing twunches");
 		Intent intent = new Intent(getActivity(), SyncService.class);
 		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, resultReceiver);
 		getActivity().startService(intent);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.fragment_twunchlist, menu);
 		refreshMenuItem = menu.findItem(R.id.menuRefresh);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuRefresh:
 			refreshTwunches(true);
 			return true;
 		case R.id.menuMap:
 			startActivity(new Intent(getActivity(), TwunchesMapActivity.class));
 			return true;
 		}
 		return false;
 	}
 
 	private class SyncResultReceiver implements DetachableResultReceiver.Receiver {
 
 		@Override
 		public void onReceiveResult(int resultCode, Bundle resultData) {
 			switch (resultCode) {
 			case SyncService.STATUS_RUNNING: {
 				break;
 			}
 			case SyncService.STATUS_FINISHED: {
 				((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable()).stop();
 				refreshMenuItem.setActionView(null);
 				Toast.makeText(getActivity(), getString(R.string.download_done), Toast.LENGTH_SHORT).show();
 				break;
 			}
 			case SyncService.STATUS_ERROR: {
 				((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable()).stop();
 				refreshMenuItem.setActionView(null);
 				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 				builder.setMessage(R.string.download_error);
 				builder.setCancelable(false);
 				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// Do nothing
 					}
 				});
 				builder.create().show();
 				break;
 			}
 			}
 
 		}
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		return new CursorLoader(getActivity(), Twunches.CONTENT_URI, TwunchesQuery.PROJECTION, null, null, Twunches.DEFAULT_SORT);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		mAdapter.swapCursor(cursor);
 		if (isResumed()) {
 			setListShown(true);
 		} else {
 			setListShownNoAnimation(true);
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		mAdapter.swapCursor(null);
 	}
 }
