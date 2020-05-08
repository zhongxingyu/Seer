 package com.zeyomir.ocfun.gui;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.RadioGroup;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.zeyomir.ocfun.LocationProvider;
 import com.zeyomir.ocfun.LocationUser;
 import com.zeyomir.ocfun.R;
 import com.zeyomir.ocfun.controller.AddCaches;
 import com.zeyomir.ocfun.controller.helper.CacheDownloader;
 import com.zeyomir.ocfun.gui.addtabs.CustomFragment;
 import com.zeyomir.ocfun.gui.addtabs.NearFragment;
 import com.zeyomir.ocfun.gui.addtabs.StandardFragment;
 import com.zeyomir.ocfun.gui.addtabs.TabListener;
 import org.holoeverywhere.app.Activity;
 import org.holoeverywhere.app.AlertDialog;
 import org.holoeverywhere.app.Dialog;
 import org.holoeverywhere.widget.CheckBox;
 import org.holoeverywhere.widget.EditText;
 import org.holoeverywhere.widget.Toast;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Add extends Activity implements LocationUser {
 
 	private static final int fieldsDialog = 1;
 	private static final int internetDialog = 2;
 	private static final int locationDialog = 3;
 	private static final int tooMany = 4;
 	private static String[] _cachesToDownload;
 	private static CacheDownloader _cachesDownloader;
 	private static boolean waitingForLocation = false;
 
 	private static int currentFragmentTag;
 	private ActionBar actionBar;
 
 	public void setCurrentFragmentTag(int tag) {
 		currentFragmentTag = tag;
 		unregister();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		actionBar = getSupportActionBar();
 		setActionBarOptions();
 		addTabs();
 	}
 
 	private void setActionBarOptions() {
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayShowTitleEnabled(true);
 		actionBar.setTitle(R.string.add);
 		actionBar.setDisplayShowHomeEnabled(true);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setHomeButtonEnabled(true);
 	}
 
 	private void addTabs() {
 		ActionBar.Tab tab = actionBar
 				.newTab()
 				.setText(R.string.code_or_name)
 				.setTabListener(
 						new TabListener<StandardFragment>(this, "standard",
 								StandardFragment.class));
 		actionBar.addTab(tab);
 
 		tab = actionBar
 				.newTab()
 				.setText(R.string.near)
 				.setTabListener(
 						new TabListener<NearFragment>(this, "near",
 								NearFragment.class));
 		actionBar.addTab(tab);
 
 		tab = actionBar
 				.newTab()
 				.setText(R.string.custom)
 				.setTabListener(
 						new TabListener<CustomFragment>(this, "custom",
 								CustomFragment.class));
 		actionBar.addTab(tab);
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				Intent intent = new Intent(this, OCFun.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 				return true;
 			case R.id.menu_add:
 				if (!checkInternet() && currentFragmentTag != R.id.add_custom) {
 					showDialog(internetDialog);
 					break;
 				}
 				if (waitingForLocation) {
 					showDialog(locationDialog);
 					break;
 				}
 				Map<String, String> data = new HashMap<String, String>();
 				boolean allOk = collectData(data);
 				if (allOk) {
 					(new AddCaches(this)).add(currentFragmentTag, data);
 					return true;
 				} else {
 					showDialog(fieldsDialog);
 					break;
 				}
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private boolean checkInternet() {
 		NetworkInfo ni = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
 				.getActiveNetworkInfo();
 		if ((ni == null) || !(ni.isConnectedOrConnecting())) {
 			return false;
 		}
 		return true;
 	}
 
 	public void displayTooManyWarning(String[] codes, CacheDownloader downloader) {
 		Log.i("warning", "called");
 		_cachesToDownload = codes;
 		_cachesDownloader = downloader;
 		showDialog(tooMany);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		AlertDialog.Builder builder;
 		final Context c = this;
 		DialogInterface.OnClickListener cl = new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		};
 
 		switch (id) {
 			case fieldsDialog:
 				builder = new AlertDialog.Builder(this);
 				builder.setMessage(R.string.dialog_fill_fields);
 				builder.setPositiveButton(R.string.dialog_ok, cl);
 				dialog = builder.create();
 				break;
 			case locationDialog:
 				builder = new AlertDialog.Builder(this);
 				builder.setMessage(R.string.dialog_wait_for_location);
 				builder.setPositiveButton(R.string.dialog_ok, cl);
 				dialog = builder.create();
 				break;
 			case internetDialog:
 				builder = new AlertDialog.Builder(this);
 				builder.setMessage(R.string.dialog_no_internet);
 				builder.setPositiveButton(R.string.dialog_ok, cl);
 				dialog = builder.create();
 				break;
 			case tooMany:
 				Log.i("warning", "happened");
 				builder = new AlertDialog.Builder(this);
				builder.setMessage("Skrzynek do ściągnięcia: " + _cachesToDownload.length + ".\nCzy kontynuować?"); //TODO !!!
 				builder.setNegativeButton("Anuluj", cl);
				builder.setPositiveButton("Tak, ściągaj", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 						_cachesDownloader.run(_cachesToDownload);
 					}
 				});
 				dialog = builder.create();
 				break;
 			default:
 				dialog = null;
 		}
 		return dialog;
 	}
 
 	private boolean collectData(Map<String, String> data) {
 		String temp;
 		switch (currentFragmentTag) {
 			case R.id.add_standard:
 				temp = ((EditText) findViewById(R.id.editText1)).getText()
 						.toString();
 				if (isEmpty(temp))
 					return false;
 				data.put("text", temp);
 				data.put(
 						"option",
 						""
 								+ ((RadioGroup) findViewById(R.id.radioGroup1))
 								.getCheckedRadioButtonId());
 				break;
 			case R.id.add_near:
 				temp = ((EditText) findViewById(R.id.editText1)).getText()
 						.toString();
 				if (isEmpty(temp))
 					return false;
 				data.put("distance", temp);
 				temp = ((EditText) findViewById(R.id.editText2)).getText()
 						.toString();
 				if (isEmpty(temp) && findViewById(R.id.editText2).isEnabled())
 					return false;
 				data.put("text", temp);
 				data.put(
 						"option",
 						""
 								+ ((RadioGroup) findViewById(R.id.radioGroup1))
 								.getCheckedRadioButtonId());
 				break;
 			case R.id.add_custom:
 				temp = ((EditText) findViewById(R.id.editText1)).getText()
 						.toString();
 				if (isEmpty(temp))
 					return false;
 				data.put("name", temp);
 				temp = ((EditText) findViewById(R.id.editText2)).getText()
 						.toString();
 				if (isEmpty(temp) && findViewById(R.id.editText2).isEnabled())
 					return false;
 				data.put("coords", temp);
 				temp = ((EditText) findViewById(R.id.editText3)).getText()
 						.toString();
 
 				data.put("description", temp);
 				data.put("option",
 						"" + ((CheckBox) findViewById(R.id.switch1)).isChecked());
 				break;
 		}
 		return true;
 	}
 
 	private boolean isEmpty(String s) {
 		if (s == null || s.trim().equals(""))
 			return true;
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.add, menu);
 		return true;
 	}
 
 	public void onClick(View v) {
 		switch (currentFragmentTag) {
 			case R.id.add_standard:
 				if (v.getId() == R.id.radioButton1)
 					((EditText) findViewById(R.id.editText1))
 							.setHint(R.string.hint_codes);
 				else
 					((EditText) findViewById(R.id.editText1))
 							.setHint(R.string.hint_name);
 				unregister();
 				break;
 			case R.id.add_near:
 				if (v.getId() == R.id.radioButton1) {
 					((EditText) findViewById(R.id.editText2))
 							.setHint(R.string.hint_address);
 					findViewById(R.id.editText2).setEnabled(true);
 					unregister();
 				} else if (v.getId() == R.id.radioButton2) {
 					((EditText) findViewById(R.id.editText2))
 							.setHint(R.string.hint_cache);
 					findViewById(R.id.editText2).setEnabled(true);
 					unregister();
 				} else if (v.getId() == R.id.radioButton3) {
 					((EditText) findViewById(R.id.editText2))
 							.setHint(R.string.hint_coords);
 					findViewById(R.id.editText2).setEnabled(true);
 					unregister();
 				} else {
 					((EditText) findViewById(R.id.editText2))
 							.setHint(R.string.gps_wait);
 					((EditText) findViewById(R.id.editText2)).setText("");
 					findViewById(R.id.editText2).setEnabled(false);
 					register();
 				}
 				break;
 			case R.id.add_custom:
 				if (((CheckBox) findViewById(R.id.switch1)).isChecked()) {
 					((EditText) findViewById(R.id.editText2))
 							.setHint(R.string.gps_wait);
 					((EditText) findViewById(R.id.editText2)).setText("");
 					findViewById(R.id.editText2).setEnabled(false);
 					register();
 				} else {
 					((EditText) findViewById(R.id.editText2))
 							.setHint(R.string.hint_coords);
 					findViewById(R.id.editText2).setEnabled(true);
 					unregister();
 				}
 				break;
 		}
 	}
 
 	@Override
 	public void locationFound(Location l) {
 		if (waitingForLocation) {
 			((EditText) findViewById(R.id.editText2)).setText(l.getLatitude() + ", " + l.getLongitude());
 			Toast.makeText(this, "Błąd GPS: " + l.getAccuracy() + "m.", Toast.LENGTH_SHORT).show();
 		}
 		unregister();
 	}
 
 	private void unregister() {
 		waitingForLocation = false;
 		((LocationProvider) getApplicationContext()).unregister(this);
 		Log.i("location", "UNregister");
 	}
 
 	private void register() {
 		waitingForLocation = true;
 		((LocationProvider) getApplicationContext()).registerForFrequentlyLocationUpdates(this);
 		Log.i("location", "register");
 	}
 }
