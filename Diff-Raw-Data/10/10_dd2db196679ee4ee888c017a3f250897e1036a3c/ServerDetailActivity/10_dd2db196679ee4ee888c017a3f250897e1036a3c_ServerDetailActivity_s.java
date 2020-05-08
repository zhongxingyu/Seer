 package com.saikali.android_skwissh;
 
 import org.json.JSONArray;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 import com.saikali.android_skwissh.adapters.SensorsAdapter;
 import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent;
 import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent.SkwisshGraphTypeItem;
 import com.saikali.android_skwissh.objects.SkwisshMeasureItem;
 import com.saikali.android_skwissh.objects.SkwisshSensorItem;
 import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;
 import com.saikali.android_skwissh.objects.SkwisshServerGroupContent;
 import com.saikali.android_skwissh.utils.Constants;
 import com.saikali.android_skwissh.utils.SkwisshAjaxHelper;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshBase;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshBase.OnRefreshListener;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshExpandableListView;
 
 public class ServerDetailActivity extends Activity {
 
 	private PullToRefreshExpandableListView expandableList = null;
 	private SensorsAdapter adapter;
 	private SkwisshServerItem server;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
 		this.setContentView(R.layout.activity_serverdetail_list);
 
 		String server_id = this.getIntent().getExtras().getString("server_id");
 		String group_id = this.getIntent().getExtras().getString("group_id");
 		this.server = SkwisshServerGroupContent.ITEM_MAP.get(group_id).getServer(server_id);
 
 		this.expandableList = (PullToRefreshExpandableListView) this.findViewById(R.id.pull_to_refresh_sensorslistview);
 		this.expandableList.getRefreshableView().setGroupIndicator(null);
 		this.expandableList.setShowIndicator(false);
		this.expandableList.setRefreshingLabel("Loading Skwissh measures for server " + ServerDetailActivity.this.server.getHostname() + "...");
 		this.expandableList.setOnRefreshListener(new OnRefreshListener<ExpandableListView>() {
 			@Override
 			public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
 				new SensorsLoader().execute();
 			}
 		});
 
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
 		String period = sharedPrefs.getString("default_period", "day");
 		this.setTitle(this.server.getHostname() + " activity on last " + period + ".");
 
 		TextView headerTitleText = (TextView) this.findViewById(R.id.headerTitleText);
 		headerTitleText.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView headerTitleTextSSH = (TextView) this.findViewById(R.id.headerTitleTextSSH);
 		headerTitleTextSSH.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView headerSubtitle = (TextView) this.findViewById(R.id.headerSubtitle);
 		headerSubtitle.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView pullToRefresh = (TextView) this.findViewById(R.id.pull_to_refresh_text);
 		pullToRefresh.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Oxygen.otf"));
 
 		this.adapter = new SensorsAdapter(this, this.server);
 		this.expandableList.getRefreshableView().setAdapter(this.adapter);
 		this.expandableList.setRefreshing();
 		new SensorsLoader().execute();
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		this.getMenuInflater().inflate(R.menu.activity_servers_list, menu);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
 		String period = sharedPrefs.getString("default_period", "day");
 		this.setTitle(this.server.getHostname() + " activity on last " + period + ".");
 		this.expandableList.setRefreshing();
 		new SensorsLoader().execute();
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			this.startActivityForResult(new Intent(this, SettingsActivity.class), 0);
 			return true;
 		case R.id.menu_refresh:
 			this.expandableList.setRefreshing();
 			new SensorsLoader().execute();
 			return true;
 		}
 		return false;
 	}
 
 	public class SensorsLoader extends AsyncTask<String, String, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 			try {
 				ServerDetailActivity.this.server.clearSensors();
 				SkwisshAjaxHelper saj = new SkwisshAjaxHelper(ServerDetailActivity.this.adapter.context);
 
 				JSONArray jsonGraphTypes = saj.getJSONGraphTypes();
 				for (int i = 0; i < jsonGraphTypes.length(); i++) {
 					SkwisshGraphTypeContent.addItem(new SkwisshGraphTypeItem(jsonGraphTypes.getJSONObject(i)));
 				}
 
 				JSONArray jsonSensors = saj.getJSONSensors(ServerDetailActivity.this.adapter.getServer().getId());
 				for (int j = 0; j < jsonSensors.length(); j++) {
 					SkwisshSensorItem sensor = new SkwisshSensorItem(jsonSensors.getJSONObject(j), ServerDetailActivity.this.adapter.getServer());
 					sensor.setGraphTypeName(SkwisshGraphTypeContent.ITEM_MAP.get(sensor.getGraphTypeId()).getName());
 					ServerDetailActivity.this.adapter.getServer().addSensor(sensor);
 				}
 
 				for (int j = 0; j < ServerDetailActivity.this.adapter.getServer().getSensors().size(); j++) {
 					SkwisshSensorItem sensor = ServerDetailActivity.this.adapter.getServer().getSensors().get(j);
 					JSONArray jsonMeasures = saj.getJSONMeasures(ServerDetailActivity.this.adapter.getServer(), sensor);
 					for (int k = 0; k < jsonMeasures.length(); k++) {
 						sensor.addMeasure(new SkwisshMeasureItem(jsonMeasures.getJSONObject(k)));
 					}
 				}
 
 				saj.skwisshLogout();
 				return true;
 			} catch (Exception e) {
 				Log.e(Constants.SKWISSH_TAG, "SensorsLoader", e);
 				return false;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Boolean success) {
 			if (success) {
 				ServerDetailActivity.this.adapter.updateEntries();
 				for (int i = 0; i < ServerDetailActivity.this.adapter.getGroupCount(); i++) {
 					ServerDetailActivity.this.expandableList.getRefreshableView().expandGroup(i);
 				}
 			} else {
 				AlertDialog alertDialog = new AlertDialog.Builder(ServerDetailActivity.this.adapter.context).create();
 				alertDialog.setTitle("Error");
 				alertDialog.setMessage("An error occured while loading Skwissh sensors for server " + ServerDetailActivity.this.adapter.getServer().getHostname() + ".\nPlease try to reload or check your Skwissh settings...");
 				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				});
 				alertDialog.show();
 			}
 			ServerDetailActivity.this.expandableList.onRefreshComplete();
 			super.onPostExecute(true);
 		}
 	}
 
 }
