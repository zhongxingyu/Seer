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
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import be.norio.twunch.android.core.Twunch;
 
 public class TwunchesActivity extends ListActivity {
 
 	private final static int MENU_ABOUT = 0;
 	private final static int MENU_REFRESH = 1;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.twunches);
 		setListAdapter(new TwunchArrayAdapter(this, R.layout.twunchheadline, R.id.twunchTitle,
 				((TwunchApplication) getApplication()).getTwunches()));
 		setTitle(R.string.activity_twunches);
 	}
 
 	class TwunchArrayAdapter extends ArrayAdapter<Twunch> {
 
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
 				convertView = inflater.inflate(R.layout.twunchheadline, null);
 			}
 			Twunch twunch = twunches.get(position);
 			((TextView) convertView.findViewById(R.id.twunchTitle)).setText(twunch.getTitle());
 			((TextView) convertView.findViewById(R.id.twunchDate)).setText(String.format(getString(R.string.date), DateUtils
 					.formatDateTime(context, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE),
 					DateUtils.formatDateTime(context, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_TIME)));
 			return convertView;
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent intent = new Intent();
 		intent.setComponent(new ComponentName(v.getContext(), TwunchActivity.class));
 		intent.putExtra(TwunchActivity.PARAMETER_INDEX, position);
 		startActivity(intent);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_REFRESH, 0, R.string.menu_refresh).setIcon(R.drawable.ic_menu_refresh);
 		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(R.drawable.ic_menu_info_details);
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
 			showAbout();
 			return true;
 		case MENU_REFRESH:
 			try {
 				((TwunchApplication) getApplication()).loadTwunches();
 			} catch (Exception e) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage(R.string.error_network);
 				builder.setCancelable(false);
 				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// Do nothing
 					}
 				});
 				builder.create().show();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Shows the about dialog.
 	 */
 	private void showAbout() {
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		String version = "";
 		try {
 			version = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionName;
 		} catch (NameNotFoundException e) {
 			// Do nothing
 		}
 		builder.setTitle(R.string.app_name);
 		builder.setCancelable(true);
		builder.setIcon(R.drawable.ic_dialog_info);
 		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				// Do nothing
 			}
 		});
 		builder.setMessage(String.format(getString(R.string.about_text), version));
 		builder.create().show();
 	}
 
 }
