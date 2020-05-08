 package com.yuz.webview;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;

 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.TextView;
 import android.widget.Toast;
 
 @TargetApi(11)
 public class MainView extends Activity {
 	WebView webview;
 	//Boolean flag;
 	int check_run_times;   
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		webview = new WebView(this);
 		getWindow().requestFeature(Window.FEATURE_PROGRESS);
 		setContentView(webview);
 		webview.getSettings().setJavaScriptEnabled(true); // EnableJavaScript
 		webview.getSettings().setBuiltInZoomControls(true); // Enable Zooming
 															// with MultyTouch
 
 		// SharedPref for run estimate dialog
 		SharedPreferences settings = getPreferences(0);
 		//flag = settings.getBoolean("flag", false);    
 		check_run_times = settings.getInt("check_run_times", 0);
 		check_run_times++;
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putInt("check_run_times", check_run_times);
 		editor.commit();
 
 		// Let's display the progress in the activity title bar, like the
 		// browser app does.
 
 		final Activity activity = this;
 		webview.setWebChromeClient(new WebChromeClient() {
 			public void onProgressChanged(WebView view, int progress) {
 				// Activities and WebViews measure progress with different
 				// scales.
 				// The progress meter will automatically disappear when we reach
 				// 100%
 				activity.setTitle(R.string.activity_load_title);
 				activity.setProgress(progress * 100); // Make the bar disappear
 														// after URL is loaded
 				// Return the app name after finish loading
 				if (progress == 100)
 					activity.setTitle(R.string.app_name);
 			}
 		});
 		webview.setWebViewClient(new WebViewClient() {
 
 			// Check for errors
 			public void onReceivedError(WebView view, int errorCode,
 					String description, String failingUrl) {
 				createNoConnectDialog().show();
 			}
 		});
 
 		// webview.setWebViewClient(new WebViewClient());
 		webview.loadUrl(getString(R.string.main_url));
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// Check if the key event was the Back button and if there's history
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			if (webview.canGoBack() == true) {
 				webview.goBack();
 			} else {
 				CheckRunTimes(); // Exit or show Estimate Dialog
 			}
 
 			return true;
 		}
 		// If it wasn't the Back key or there's no web page history, bubble up
 		// to the default
 		// system behavior (probably exit the activity)
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.exit_item:
 			//CheckRunTimes();
 			// For debug
 			 String s = String.valueOf(check_run_times);
 			 Toast.makeText(this, s, Toast.LENGTH_LONG).show();
 			break;
 		case R.id.refresh_item:
 			webview.reload();
 			break;
 		default:
 			break;
 		}
 
 		return true;
 	}
 
 	private Dialog createNoConnectDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("No internet");
 		TextView textView = new TextView(this);
 		textView.setText(R.string.no_connection);
 		textView.setTypeface(Typeface.createFromAsset(getAssets(), "comic.ttf"));
 		textView.setPadding(10, 10, 10, 10);
 		textView.setTextSize(14);
 		builder.setView(textView);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 			}
 		});
 
 		// Create the AlertDialog object and return it
 		return builder.create();
 	}
 
 	private void CheckRunTimes() {
 		if (((check_run_times % 5) == 0) || (check_run_times == 1)) { // Every first and fifth times run Estimate
 			createEstimateDialogFragment().show();
 		} else {
 			createExitDialog().show();
 		}
 	}
 
 	private void Estimate() {
 		SharedPreferences settings = getPreferences(0);
 		SharedPreferences.Editor editor = settings.edit();
 	//	editor.putBoolean("flag", true);
 		editor.putInt("check_run_times", check_run_times);
 		editor.commit();
 		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
 				Uri.parse(getString(R.string.google_estimate_url)));
 		startActivity(browserIntent);
 		finish();
 	}
 
 	private Dialog createEstimateDialogFragment() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.estimate_dialog_text)
 				.setPositiveButton(R.string.estimate_dialog_button,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 							Estimate();
 							}
 						})
 				.setNegativeButton(R.string.exit,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								finish();
 
 							}
 						});
 		// Create the AlertDialog object and return it
 		return builder.create();
 	}
 
 	private Dialog createExitDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.exit_dialog_text)
 				.setPositiveButton(R.string.exit,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								finish();
 							}
 						})
 				.setNegativeButton(R.string.exit_dlg_cancel,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								// User cancelled the dialog
 
 							}
 						});
 		// Create the AlertDialog object and return it
 		return builder.create();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		webview.saveState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		webview.restoreState(savedInstanceState);
 	}
 
 }
