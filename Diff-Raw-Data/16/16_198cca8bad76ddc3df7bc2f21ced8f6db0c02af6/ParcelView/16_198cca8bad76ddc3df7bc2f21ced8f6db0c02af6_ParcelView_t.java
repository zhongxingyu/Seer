 /*
  * Copyright (C) 2013 Joakim Andersson
  * Copyright (C) 2013 blunden
  * 
  * This file is part of PactrackDroid, an Android application to keep
  * track of parcels sent with the Swedish mail service (Posten).
  * 
  * PactrackDroid is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * PactrackDroid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package nu.firetech.android.pactrack.frontend;
 
 import nu.firetech.android.pactrack.R;
 import nu.firetech.android.pactrack.backend.ParcelDbAdapter;
 import nu.firetech.android.pactrack.backend.ParcelUpdater;
 import nu.firetech.android.pactrack.backend.Preferences;
 import nu.firetech.android.pactrack.common.ContextListener;
 import nu.firetech.android.pactrack.common.Error;
 import nu.firetech.android.pactrack.common.RefreshContext;
 import android.app.AlertDialog;
 import android.app.LoaderManager;
 import android.app.NotificationManager;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Loader;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
import android.os.Message;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class ParcelView extends DialogAwareListActivity implements
 		RefreshContext, ParcelOptionsMenu.UpdateableView, LoaderManager.LoaderCallbacks<Cursor> {
 	private static final int PARCEL_LOADER_ID = 1;
 	private static final int EVENTS_LOADER_ID = 2;
 	private static final int REFRESH_LOADER_ID = 3;
 
 	private static final String TAG = "<PactrackDroid> ParcelView";
 	
 	public static final String FORCE_REFRESH = "force_update";
 
 	private static final String KEY_EXTENDED = "extended_view";
 	private static final String KEY_ERROR_SHOWN = "error_shown";
 
 	private static final int DELETE_ID = Menu.FIRST;
 	private static final int RENAME_ID = Menu.FIRST + 1;
 	private static final int REFRESH_ID = Menu.FIRST + 2;
 
 	private Long mRowId;
 	private ParcelDbAdapter mDbAdapter;
 	private LinearLayout mExtended;
 	private Button mToggleButton;
 	private boolean mExtendedShowing;
 	private int errorShown;
 	private SimpleCursorAdapter mEventsAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.parcel_view);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		mExtended = (LinearLayout)findViewById(R.id.extended);
 		mToggleButton = (Button)findViewById(R.id.extended_toggle);
 
 		mToggleButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				LinearLayout extended = (LinearLayout)findViewById(R.id.extended);
 				mExtendedShowing = !mExtendedShowing;
 				if (mExtendedShowing) {
 					mExtended.setVisibility(View.VISIBLE);
 					mToggleButton.setText(R.string.hide_extended);
 				} else {
 					extended.setVisibility(View.GONE);
 					mToggleButton.setText(R.string.show_extended);
 				}
 			}
 		});
 
 		mDbAdapter = new ParcelDbAdapter(this);
 		mDbAdapter.open();
 
 		mRowId = savedInstanceState != null ? savedInstanceState.getLong(ParcelDbAdapter.KEY_ROWID) 
 				: null;
 
 		mExtendedShowing = false;
 		if (savedInstanceState != null) {
 			if (savedInstanceState.getBoolean(KEY_EXTENDED)) {
 				mExtended.setVisibility(View.VISIBLE);
 				mToggleButton.setText(R.string.hide_extended);
 				mExtendedShowing = true;
 			}
 			errorShown = savedInstanceState.getInt(KEY_ERROR_SHOWN);
 		} else {
 			errorShown = Error.NONE;
 		}
 
 
 		String[] from = new String[]{ParcelDbAdapter.KEY_CUSTOM, ParcelDbAdapter.KEY_DESC, ParcelDbAdapter.KEY_ERREV};
 		int[] to = new int[]{android.R.id.title, android.R.id.text1, android.R.id.text2};
 		
 		mEventsAdapter = new SimpleCursorAdapter(this, R.layout.event_row, null, from, to, 0);
 		mEventsAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 			@Override
 			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 				if (view instanceof TextView && view.getId() == android.R.id.text2) {
 					if (cursor.getInt(columnIndex) == 1) {
 						((TextView)view).setVisibility(View.VISIBLE);
 						((TextView)view).setText(getString(R.string.error_event));
 					} else {
 						((TextView)view).setVisibility(View.GONE);
 					}
 					return true;
 				} else {
 					return false;
 				}
 			}
 		});
 		setListAdapter(mEventsAdapter);
 		
 		Bundle extras = null;
 		if (mRowId == null) {
 			extras = getIntent().getExtras();            
 			mRowId = extras != null ? extras.getLong(ParcelDbAdapter.KEY_ROWID) 
 					: null;
 		}
 		if (extras != null && extras.containsKey(FORCE_REFRESH)) {
 			doRefresh();
 		}
 		
 		getLoaderManager(); // Workaround for a bug regarding LoaderManager life cycle when rotating the screen.
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshDone();
 	}
 	
 	@Override
 	public void refreshDone() {
 		int[] loaders = { PARCEL_LOADER_ID, EVENTS_LOADER_ID };
 		
 		LoaderManager lm = getLoaderManager();
 		for (int loader_id : loaders) {
 			if (lm.getLoader(loader_id) == null) {
 				lm.initLoader(loader_id, null, this);
 			} else {
 				lm.restartLoader(loader_id, null, this);
 			}
 		}
 	}
 	
 	public void doRefresh() {
 		LoaderManager lm = getLoaderManager();
 		if (lm.getLoader(REFRESH_LOADER_ID) == null) {
 			lm.initLoader(REFRESH_LOADER_ID, null, this);
 		} else {
 			lm.restartLoader(REFRESH_LOADER_ID, null, this);
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putLong(ParcelDbAdapter.KEY_ROWID, mRowId);
 		outState.putBoolean(KEY_EXTENDED, mExtendedShowing);
 		outState.putInt(KEY_ERROR_SHOWN, errorShown);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		mDbAdapter.close();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, DELETE_ID, 0, R.string.menu_delete)
 			.setIcon(R.drawable.ic_action_discard)
 			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		menu.add(0, RENAME_ID, 0, R.string.menu_rename)
 			.setIcon(R.drawable.ic_action_edit)
 			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		new ParcelOptionsMenu(this, menu, mRowId, R.id.status_icon, mDbAdapter, this);
 		menu.add(0, REFRESH_ID, 0, R.string.menu_refresh)
 			.setIcon(R.drawable.ic_action_refresh)
 			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId()) {
 		case DELETE_ID:
 			MainWindow.deleteParcel(mRowId, this, mDbAdapter, new Runnable() {
 				@Override
 				public void run() {
 					finish();					
 				}
 			});
 			return true;
 		case RENAME_ID:
 			ParcelIdDialog.show(this, mRowId);
 			return true;
 		case REFRESH_ID:
 			errorShown = Error.NONE;
 			doRefresh();
 			return true;
 		}
 
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case android.R.id.home:
 	        NavUtils.navigateUpFromSameTask(this);
 	        return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public Handler getProgressHandler() {
 		return ((RefreshDialog)getDialogByClass(RefreshDialog.class)).getProgressHandler();
 	}
 
 	@Override
 	public void startRefreshProgress(int maxValue, ContextListener contextListener) {
 		RefreshDialog.show(this, maxValue);
 		addContextListener(contextListener);
 	}
 
 	@Override
 	public boolean showsNews() {
 		return true;
 	}
 
 	private void updateView(Cursor parcel) {
 		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(mRowId.hashCode());
 		
 		try {
 			int error = parcel.getInt(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_ERROR));
 			
 			String status = parcel.getString(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_STATUS));
 			
 			if (error != Error.NONE && errorShown != error) {
 				switch(error) {
 				case Error.NOT_FOUND:
 					status = getString(R.string.parcel_error_not_found);
 					break;
 				case Error.MULTI_PARCEL:
 					status = getString(R.string.parcel_error_multi_parcel);
 					break;
 				case Error.SERVER:
 					status = getString(R.string.parcel_error_server);
 					break;
 				default:
 					status = getString(R.string.parcel_error_unknown, error);
 				}
 				
 				new AlertDialog.Builder(this)
 				.setTitle(R.string.parcel_problem)
 				.setMessage(getString(R.string.parcel_error_message, status))
 				.setIconAttribute(android.R.attr.alertDialogIcon)
 				.setPositiveButton(R.string.yes, new OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				})
 				.setNegativeButton(R.string.no, new OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 						ParcelView.this.finish();
 					}
 				})
 				.create()
 				.show();
 			}
 			
 			errorShown = error;
 
 			String parcelId = parcel.getString(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_PARCEL));
 			String parcelName = parcel.getString(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_NAME));
 			if (parcelName == null) {
 				parcelName = getString(R.string.generic_parcel_name, parcelId);
 			}
 
 			setTitle(parcelName);
 			String lastUpdate = null, lastOkUpdate = null;
 			int lastUpdateIndex = parcel.getColumnIndex(ParcelDbAdapter.KEY_UPDATE);
 			if (lastUpdateIndex >= 0) {
 				lastUpdate = parcel.getString(lastUpdateIndex);
 			}
 			int lastOkUpdateIndex = parcel.getColumnIndex(ParcelDbAdapter.KEY_OK_UPDATE);
 			if (lastOkUpdateIndex >= 0) {
 				lastOkUpdate = parcel.getString(lastOkUpdateIndex);
 			}
 			if (lastUpdate != null && lastUpdate.equals(lastOkUpdate)) {
 				lastOkUpdate = getString(R.string.same_time);
 			}
 
 			findTextView(R.id.customer).setText(parcel.getString(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_CUSTOMER)));
 			findTextView(R.id.sent).setText(parcel.getString(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_SENT)));
 			findTextView(R.id.status).setText(status);
 			findTextView(R.id.update_info).setText(getString(R.string.update_info_syntax, 
 					(lastUpdate == null ? getString(R.string.never) : lastUpdate),
 					(lastOkUpdate == null ? getString(R.string.never) : lastOkUpdate)));
 			findTextView(R.id.weight).setText(parcel.getString(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_WEIGHT)));
 			findTextView(R.id.postal).setText(parcel.getString(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_POSTAL)));
 			findTextView(R.id.service).setText(parcel.getString(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_SERVICE)));
 			
 			((ImageView)findViewById(R.id.status_icon)).setImageResource(
 					MainWindow.getStatusImage(parcel, parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_STATUSCODE)));
 			updateAutoUpdateView(R.id.status_icon, Preferences.getPreferences(this).getCheckInterval() == 0 ||
 					parcel.getInt(parcel.getColumnIndexOrThrow(ParcelDbAdapter.KEY_AUTO)) == 1);
 		} catch (Exception e) {
 			Log.d(TAG, "Database error", e);
 			MainWindow.dbErrorDialog(this);
 		}
 	}
 
 	private TextView findTextView(int resId) {
 		return (TextView)findViewById(resId);
 	}
 
 	@Override
 	public void updateAutoUpdateView(int position, boolean value) {
 		ImageView icon = (ImageView)findViewById(position);
 		icon.getDrawable().setAlpha((value ? 255 : 70));
 		icon.invalidate();
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 	    switch (id) {
 		case PARCEL_LOADER_ID:
	    case REFRESH_LOADER_ID:
 			return mDbAdapter.getParcelLoader(mRowId);
 	    case EVENTS_LOADER_ID:
 			return mDbAdapter.getEventsLoader(mRowId);
 	    }
 	    return null;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		switch (loader.getId()) {
 		case PARCEL_LOADER_ID:
 			updateView(cursor);
 			break;
 		case EVENTS_LOADER_ID:
 			mEventsAdapter.swapCursor(cursor);
 			break;
 		case REFRESH_LOADER_ID:
			//TODO This can probably be done better...
			final Cursor fCursor = cursor;
			new Handler(getMainLooper()){
				@Override
				public void handleMessage(Message m) {
					ParcelUpdater.update(ParcelView.this, fCursor);
				}
			}.sendEmptyMessage(0);
 			break;
 	    }
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		switch (loader.getId()) {
 		case EVENTS_LOADER_ID:
 			mEventsAdapter.swapCursor(null);
 			break;
 		}
 	}
 }
