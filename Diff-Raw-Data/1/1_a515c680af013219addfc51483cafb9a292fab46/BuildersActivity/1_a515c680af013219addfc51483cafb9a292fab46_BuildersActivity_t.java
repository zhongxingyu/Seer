 package com.buildbotwatcher;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.buildbotwatcher.worker.Builder;
 import com.buildbotwatcher.worker.JsonParser;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class BuildersActivity extends ListActivity {
 	static final int DIALOG_NET_ISSUE_ID = 0;
 
 	private BuildersAdapter	_adapter;
 	private Menu			_menu;
 	private GetBuilders		_async;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		_async = null;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.builders_list_loading);
		setTitle(getResources().getString(R.string.btn_builders));
 		firstTimeWizard();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		JsonParser p = new JsonParser(prefs.getString("host", "http://buildbot.buildbot.net"), Integer.valueOf("0" + prefs.getString("port", "80")), prefs.getBoolean("auth", false), prefs.getString("auth_login", null), prefs.getString("auth_password", null));
 		_adapter = new BuildersAdapter(this);
 		setListAdapter(_adapter);
 
 		@SuppressWarnings("unchecked")
 		final List<Builder> data = (List<Builder>) getLastNonConfigurationInstance();
 		if (data == null) {
 			_async = new GetBuilders();
 			_async.execute(p);
 		} else {
 			setContentView(R.layout.builders_list);
 			for (Builder b: data) {
 				_adapter.addBuilder(b);
 			}
 		}
 	}
 
 	private void startSettings() {
 		if (_async != null)
 			_async.cancel(true);
 		
 		if (_menu != null)
 			_menu.findItem(R.id.menu_refresh).setEnabled(true);
 		
 		Intent i = new Intent();
 		i.setClass(BuildersActivity.this, SettingsActivity.class);
 		startActivity(i);
 	}
 
 	private void firstTimeWizard() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		String host = prefs.getString("host", "");
 		if (host.equals("")) {
 			AlertDialog.Builder popup = new AlertDialog.Builder(this);
 			popup.setTitle(R.string.ftw_title)
 			.setMessage(R.string.ftw_message)
 			.setCancelable(false)
 			.setPositiveButton(R.string.ftw_now, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					startSettings();
 				}
 			})
 			.setNegativeButton(R.string.ftw_later, null)
 			.create()
 			.show();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		_menu = menu;
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.builders, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 			refresh();
 			return true;
 
 		case R.id.menu_settings:
 			startSettings();
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		final List<Builder> data = _adapter.getBuilders();
 		return data;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		if (_async != null)
 			_async.cancel(true);
 		
 		Builder builder = (Builder) getListAdapter().getItem(position);
 		Intent i = new Intent();
 		i.setClass(BuildersActivity.this, BuilderActivity.class);
 		//TODO: use android.os.Parcelable instead of java.io.Serializable
 		i.putExtra("builder", builder);
 		startActivity(i);
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		switch(id) {
 		case DIALOG_NET_ISSUE_ID:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(R.string.dlg_cnx_issue)
 			.setTitle(R.string.dlg_cnx_issue_title)
 			.setCancelable(false)
 			.setNeutralButton(R.string.dlg_cnx_issue_btn, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					startSettings();
 				}
 			});
 			dialog = builder.create();
 			break;
 		default:
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	private void refresh() {
 		_menu.findItem(R.id.menu_refresh).setEnabled(false);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		JsonParser p = new JsonParser(prefs.getString("host", "http://buildbot.buildbot.net"), Integer.valueOf(prefs.getString("port", "80")), prefs.getBoolean("auth", false), prefs.getString("auth_login", null), prefs.getString("auth_password", null));
 		setContentView(R.layout.builders_list_loading);
 		_adapter.clearBuilders();
 		if (_async != null)
 			_async.cancel(true);
 		
 		_async = new GetBuilders();
 		_async.execute(p);
 	}
 
 	private class BuildersAdapter extends ArrayAdapter<Builder> {
 		private final Activity	_context;
 		private List<Builder>	_builders;
 
 		public BuildersAdapter(Activity context) {
 			super(context, R.layout.builders_row);
 			_context = context;
 			_builders = new ArrayList<Builder>();
 		}
 
 		public List<Builder> getBuilders() {
 			return _builders;
 		}
 		
 		public void clearBuilders() {
 			_builders.clear();
 			clear();
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater inflater = _context.getLayoutInflater();
 				v = inflater.inflate(R.layout.builders_row, null, true);
 			}
 			TextView textView = (TextView) v.findViewById(R.id.label);
 			String s = _builders.get(position).getName();
 			textView.setText(s);
 
 			int id;
 			if (_builders.get(position).getLastBuild() != null) {
 				if (_builders.get(position).getLastBuild().isSuccessful())
 					id = R.drawable.success;
 				else
 					id = R.drawable.failure;
 			} else {
 				id = R.drawable.none;
 			}
 			textView.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0);
 
 			return v;
 		}
 
 		public void addBuilder(Builder b) {
 			_builders.add(b);
 			add(b);
 		}
 	}
 
 	private class GetBuilders extends AsyncTask<JsonParser, Integer, List<Builder>> {
 		protected List<Builder> doInBackground(JsonParser... p) {
 			return p[0].getBuilders();
 		}
 
 		protected void onProgressUpdate(Integer... progress) {
 			// TODO
 		}
 		
 		protected void onCancelled(List<Builder> result) {
 			_async = null;
 			setContentView(R.layout.builders_list);
 		}
 
 		protected void onPostExecute(List<Builder> result) {
 			_async = null;
 			setContentView(R.layout.builders_list);
 			if (_menu != null)
 				_menu.findItem(R.id.menu_refresh).setEnabled(true);
 			if (result != null) {
 				for (Builder b: result) {
 					_adapter.addBuilder(b);
 				}
 			} else {
 				showDialog(DIALOG_NET_ISSUE_ID);
 			}
 		}
 	}
 }
