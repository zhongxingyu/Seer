 package com.yinheli.sxtcm.activity;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.yinheli.sxtcm.Constants;
 import com.yinheli.sxtcm.R;
 import com.yinheli.sxtcm.utils.ActivityUtil;
 
 /**
  * Webview activity
  * 
  * @author yinheli <yinheli@gmail.com>
  *
  */
 public class MainActivity extends Activity {
 	
 	Button home;
 	
 	Button forward;
 	
 	Button back;
 	
 	WebView webView;
 	
 	TextView title;
 	
 	static final String HOME_URL = "http://m.sxtcm.com/";
 	
 	ProgressDialog loadingDialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		// view items
 		home = (Button) findViewById(R.id.home);
 		forward = (Button) findViewById(R.id.forward);
 		back = (Button) findViewById(R.id.back);
 		
 		title = (TextView) findViewById(R.id.title);
 		
 		webView = (WebView) findViewById(R.id.webview);
 		
 		home.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				loadHomePage();
 			}
 		});
 		
 		back.setEnabled(false);
 		forward.setEnabled(false);
 		
 		forward.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				webView.goForward();
 			}
 		});
 		
 		back.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				webView.goBack();
 			}
 		});
 		
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.getSettings().setDatabaseEnabled(true);
		webView.getSettings().setSavePassword(false);
		webView.getSettings().setSaveFormData(false);
		webView.getSettings().setLightTouchEnabled(true);
 		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
 		
 		if (ActivityUtil.isNewworkAvailable(this)) {
 			webView.clearCache(true);
 		} else {
 			Toast.makeText(this, R.string.off_line_tip, Toast.LENGTH_LONG).show();
 		}
 		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
 		
 		webView.setWebChromeClient(new WebChromeClient() {
 			@Override
 			public void onProgressChanged(WebView view, int newProgress) {
 				super.onProgressChanged(view, newProgress);
 			}
 		});
 		
 		webView.setWebViewClient(new WebViewClient() {
 			
 			private boolean homeSaved;
 			
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				super.onPageFinished(view, url);
 				if (loadingDialog != null && loadingDialog.isShowing()) {
 					loadingDialog.dismiss();
 				}
 				title.setText(getWebviewTitle());
 				
 				if (webView.canGoBack()) {
 					back.setEnabled(true);
 				} else {
 					back.setEnabled(false);
 				}
 				
 				if (webView.canGoForward()) {
 					forward.setEnabled(true);
 				} else {
 					forward.setEnabled(false);
 				}
 				
 				if (!homeSaved && url != null) {
 					homeSaved = true;
 					Editor e = getPreferences(MODE_WORLD_WRITEABLE).edit();
 					e.putString("home", url);
 					e.putLong("exp", new Date().getTime() + 3600 * 10 * 1000);
 					e.commit();
 					Log.d(Constants.TAG, "保存首页地址：" + url);
 				}
 			}
 			
 			@Override
 			public void onReceivedError(WebView view, int errorCode,
 					String description, String failingUrl) {
 				if (errorCode == WebViewClient.ERROR_CONNECT) {
 					webView.loadUrl("file:///android_asset/html/connect_error.html");
 					return;
 				} else if (errorCode == WebViewClient.ERROR_FILE_NOT_FOUND) {
 					webView.loadUrl("file:///android_asset/html/404.html");
 					return;
 				}
 				super.onReceivedError(view, errorCode, description, failingUrl);
 			}
 			
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				view.loadUrl(url);
 				if (loadingDialog != null && loadingDialog.isShowing()) {
 					// ignore
 				} else {
 					startLoading(R.string.loading_message);
 				}
 				return true;
 			}
 		});
 		
 		loadHomePage();
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu_main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 		switch (id) {
 			case R.id.menu_refresh:
 				if (ActivityUtil.isNewworkAvailable(this)) {
 					webView.clearCache(true);
 					webView.reload();
 				} else {
 					Toast.makeText(this, R.string.off_line_refresh_tip, Toast.LENGTH_SHORT).show();
 				}
 				break;
 			case R.id.menu_share:
 				Intent i = new Intent(Intent.ACTION_SEND);
 				String title = getResources().getString(R.string.share);
 				String url = webView.getUrl();
 				if (url != null) {
 					url = url.replace("/wap", "/web");
 				} else {
 					url = "";
 				}
 				i.setType("text/*");
 				i.putExtra(Intent.EXTRA_SUBJECT, title);
 				i.putExtra(Intent.EXTRA_TEXT, getWebviewTitle() + "  " + url);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(Intent.createChooser(i, title));
 				break;
 			case R.id.menu_exit:
 				finish();
 				break;
 			default:
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if (webView.canGoBack()) {
 			webView.goBack();
 		} else {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setCancelable(true);
 			builder.setTitle(R.string.exit_title);
 			builder.setMessage(R.string.exit_message);
 			builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					System.exit(0);
 				}
 			});
 			builder.setNegativeButton(R.string.cancel, null);
 			builder.create().show();
 		}
 	}
 	
 	private void loadHomePage() {
 		SharedPreferences sharedPreferences = getPreferences(MODE_WORLD_READABLE);
 		String sHome = sharedPreferences.getString("home", null);
 		long sExp = sharedPreferences.getLong("exp", -1L);
 		if (sHome != null && sExp > new Date().getTime()) {
 			Log.d(Constants.TAG, "历史首页地址：" + sHome);
 			webView.loadUrl(sHome);
 		} else {
 			webView.loadUrl(HOME_URL);
 		}
 	}
 	
 	private void startLoading(String message) {
 		loadingDialog = ProgressDialog.show(this, null, message, true, true, new  DialogInterface.OnCancelListener() {
 			
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				webView.stopLoading();
 			}
 		});
 	}
 	
 	private void startLoading(int id) {
 		startLoading(getResources().getString(id));
 	}
 	
 	private String getWebviewTitle() {
 		String t = webView.getTitle();
 		String defaultTitle = getResources().getString(R.string.default_title);
 		return (t != null) ? t.trim() : defaultTitle;
 	}
 
 }
