 package com.danielblakes.progressbook;
 
 import com.danielblakes.progressbook.R;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.webkit.*;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		boolean firstrun = getSharedPreferences(
 				"com.danielblakes.progressbook", MODE_PRIVATE).getBoolean(
 				"firstrun", true);
 		if (firstrun) {
			new AlertDialog.Builder(this).setTitle("First Run").show();
 			pickDistrict(this);
 			getSharedPreferences("com.danielblakes.progressbook", MODE_PRIVATE)
 					.edit().putBoolean("firstrun", false).commit();
 		}
 
//		else {
//			String saved_district = getSharedPreferences(
//					"com.danielblakes.progressbook", MODE_PRIVATE).getString(
//					"district", null);
//			startupWebView(saved_district);
//		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	// handles webview creation and basic stuff on startup
 	@SuppressLint("SetJavaScriptEnabled")
 	public void startupWebView(String i) {
 		// create main webview container
 		WebView mainWebView = (WebView) findViewById(R.id.webView);
 		// load progressbook homepage
 		mainWebView.loadUrl(i);
 		// allow javascript
 		WebSettings mainWebViewSettings = mainWebView.getSettings();
 		mainWebViewSettings.setJavaScriptEnabled(true);
 		// set links to open in webview
 		mainWebView.setWebViewClient(new WebViewClient());
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			startActivity(new Intent(this, SettingsMenu.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public Dialog pickDistrict(final Context context) {
 		AlertDialog.Builder districtalert = new AlertDialog.Builder(context);
 		districtalert
 				.setTitle(R.string.choose_district)
 				.setItems(R.array.districts,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int i) {
 								if (i == 0) {
 									String district_site = "https://parentaccess.ocps.net/General/District.aspx?From=Global";
 									startupWebView(district_site);
 									getSharedPreferences(
 											"com.danielblakes.progressbook",
 											MODE_PRIVATE)
 											.edit()
 											.putString("district",
 													district_site).commit();
 								} else if (i == 1) {
									AlertDialog.Builder customdistrict = new AlertDialog.Builder(context);
 									customdistrict
 											.setTitle(
 													R.string.custom_district_title)
 											.setMessage(
 													R.string.custom_district_message);
									final EditText input = new EditText(
											getParent());
 									customdistrict.setView(input);
 									customdistrict
 											.setPositiveButton(
 													"Ok",
 													new DialogInterface.OnClickListener() {
 														public void onClick(
 																DialogInterface dialog,
 																int which) {
 															String custom_url = input
 																	.getText()
 																	.toString();
 															getSharedPreferences(
 																	"com.danielblakes.progressbook",
 																	MODE_PRIVATE)
 																	.edit()
 																	.putString(
 																			"district",
 																			custom_url)
 																	.commit();
 														}
 													});
 									customdistrict
 											.setNegativeButton(
 													"Cancel",
 													new DialogInterface.OnClickListener() {
 														public void onClick(
 																DialogInterface dialog,
 																int which) {
 															return;
 														}
 													}).show();
 								}
 							}
 						}).show();
 		return districtalert.create();
 	}
 }
