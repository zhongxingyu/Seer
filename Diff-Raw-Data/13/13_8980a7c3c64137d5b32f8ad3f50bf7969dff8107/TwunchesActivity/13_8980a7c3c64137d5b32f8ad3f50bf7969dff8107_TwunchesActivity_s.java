 /**
  *	Copyright 2010 Norio bvba
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
 
 package be.norio.twunch.android;
 
 import greendroid.app.GDActivity;
 import greendroid.widget.ActionBarItem;
 import greendroid.widget.LoaderActionBarItem;
 
 import java.util.List;
 
 import android.app.AlertDialog;
import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.format.DateUtils;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import be.norio.twunch.android.core.Twunch;
 
 import com.cyrilmottier.android.greendroid.R;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class TwunchesActivity extends GDActivity {
 
 	private final static int MENU_ABOUT = 0;
 	private final static int MENU_REFRESH = 1;
 
 	ListView mListView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		GoogleAnalyticsTracker.getInstance().start(TwunchApplication.TRACKER_ID, 60, this);
 		GoogleAnalyticsTracker.getInstance().trackPageView("Twunches");
 		setActionBarContentView(R.layout.twunch_list);
 		addActionBarItem(greendroid.widget.ActionBarItem.Type.Refresh);
 		mListView = (ListView) findViewById(R.id.twunchesList);
 		mListView.setEmptyView(findViewById(R.id.noTwunches));
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView l, View v, int position, long id) {
 				Intent intent = new Intent();
 				intent.setComponent(new ComponentName(v.getContext(), TwunchActivity.class));
 				intent.putExtra(TwunchActivity.PARAMETER_INDEX, position);
 				startActivity(intent);
 			}
 		});
 		refreshTwunches(false);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		GoogleAnalyticsTracker.getInstance().dispatch();
 		GoogleAnalyticsTracker.getInstance().stop();
 	}
 
 	static class TwunchArrayAdapter extends ArrayAdapter<Twunch> {
 
 		private final List<Twunch> twunches;
 		private final Context context;
 
 		/**
 		 * @param context
 		 * @param resource
 		 * @param textViewResourceId
 		 * @param objects
 		 */
 		public TwunchArrayAdapter(Context context, int resource, int textViewResourceId, List<Twunch> twunches) {
 			super(context, resource, textViewResourceId, twunches);
 			this.context = context;
 			this.twunches = twunches;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.widget.Adapter#getView(int, android.view.View,
 		 * android.view.ViewGroup)
 		 */
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = inflater.inflate(R.layout.twunch_list_item, null);
 			}
 			Twunch twunch = twunches.get(position);
 			((TextView) convertView.findViewById(R.id.twunchTitle)).setText(twunch.getTitle());
 			StringBuffer address = new StringBuffer();
 			String distance = TwunchManager.getInstance().getDistanceToTwunch(context, twunch);
 			if (distance != null) {
 				address.append(distance);
 				address.append(" - ");
 			}
 			address.append(twunch.getAddress());
 			((TextView) convertView.findViewById(R.id.twunchAddress)).setText(address);
 			((TextView) convertView.findViewById(R.id.twunchDate)).setText(String.format(
 					context.getString(R.string.date),
 					DateUtils.formatDateTime(context, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_WEEKDAY
 							| DateUtils.FORMAT_SHOW_DATE),
 					DateUtils.formatDateTime(context, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_TIME)));
 			((TextView) convertView.findViewById(R.id.twunchNumberParticipants)).setText(String.format(context.getResources()
 					.getQuantityString(R.plurals.numberOfParticipants, twunch.getNumberOfParticipants()), twunch
 					.getNumberOfParticipants()));
 			return convertView;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_REFRESH, 0, R.string.menu_refresh).setIcon(R.drawable.ic_menu_refresh);
 		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_ABOUT:
 			Intent intent = new Intent();
 			intent.setComponent(new ComponentName(this, AboutActivity.class));
 			startActivity(intent);
 			return true;
 		case MENU_REFRESH:
 			refreshTwunches(true);
 			return true;
 		}
 		return false;
 	}
 
 	public void refreshTwunches(boolean force) {
 		if (!force && TwunchManager.getInstance().isTwunchListCurrent()) {
 			mListView.setAdapter(new TwunchArrayAdapter(this, R.layout.twunch_list_item, R.id.twunchTitle, TwunchManager
 					.getInstance().getTwunchList()));
 			((LoaderActionBarItem) getActionBar().getItem(0)).setLoading(false);
 			return;
 		}
		final ProgressDialog progress = ProgressDialog.show(this, "", getString(R.string.download), true);
 		final GDActivity thisActivity = this;
 		final Handler handler = new Handler();
 		final Runnable onDownloadSuccess = new Runnable() {
 			@Override
 			public void run() {
 				mListView.setAdapter(new TwunchArrayAdapter(thisActivity, R.layout.twunch_list_item, R.id.twunchTitle, TwunchManager
 						.getInstance().getTwunchList()));
				progress.dismiss();
 				((LoaderActionBarItem) getActionBar().getItem(0)).setLoading(false);
 			}
 		};
 		final Runnable onDownloadFailure = new Runnable() {
 			@Override
 			public void run() {
				progress.dismiss();
 				((LoaderActionBarItem) getActionBar().getItem(0)).setLoading(false);
 				AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
 				builder.setMessage(R.string.download_error);
 				builder.setCancelable(false);
 				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// Do nothing
 					}
 				});
 				builder.create().show();
 			}
 		};
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					TwunchManager.getInstance().loadTwunches();
 					handler.post(onDownloadSuccess);
 				} catch (Exception e) {
 					handler.post(onDownloadFailure);
 				}
 			}
 		}.start();
 	}
 
 	@Override
 	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
 		if (position == 0) {
 			refreshTwunches(true);
 			return true;
 		}
 		return false;
 	}
 
 }
