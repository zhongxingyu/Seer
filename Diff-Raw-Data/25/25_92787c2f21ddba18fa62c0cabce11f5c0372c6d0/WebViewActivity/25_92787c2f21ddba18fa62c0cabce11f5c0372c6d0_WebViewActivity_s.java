 package com.slobodastudio.discussions.ui.activities;
 
 import com.slobodastudio.discussions.R;
 import com.slobodastudio.discussions.utils.MyLog;
 import com.slobodastudio.discussions.utils.TextViewUtils;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.webkit.WebView;
 import android.webkit.WebView.HitTestResult;
 import android.webkit.WebViewClient;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class WebViewActivity extends BaseActivity implements OnMenuItemClickListener, OnLongClickListener {
 
 	private static final int ID_SAVE_IMAGE = 0x01;
 	private static final int ID_VIEW_IMAGE = 0x02;
 	private static final int ID_SAVE_LINK = 0x03;
 	private WebView mWebView;
 	private EditText mEditText;
 	private boolean loadingFinished = true;
 	private boolean redirect = false;
 	private String savedUrl = "";
 	private boolean imageSearch = false;
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_webview);
 		mWebView = (WebView) findViewById(R.id.webview);
 		mWebView.setOnLongClickListener(this);
 		mWebView.addJavascriptInterface(new JavaScriptInterface(), "JSI");
 		// registerForContextMenu(mWebView);
 		mWebView.getSettings().setJavaScriptEnabled(true);
 		mWebView.getSettings().setBuiltInZoomControls(true);
 		if (isTablet(this)) {
 			mWebView.getSettings().setUserAgentString(
 					"Mozilla/5.0 AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
 		}
 		mEditText = (EditText) findViewById(R.id.edittext_url);
 		mEditText.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
 
 				if (actionId == EditorInfo.IME_ACTION_GO) {
 					// search pressed and perform your functionality.
 					String enteredText = TextViewUtils.toString(mEditText);
 					if (!enteredText.startsWith("http")) {
 						enteredText = "http://www." + enteredText;
 						mEditText.setText(enteredText);
 					}
 					mWebView.loadUrl(enteredText);
 					return true;
 				}
 				return false;
 			}
 		});
 		mWebView.setWebViewClient(new WebViewClient() {
 
 			@Override
 			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
 
 				MyLog.tempv("[shouldOverrideUrlLoading] url: " + url);
 				if (!loadingFinished) {
 					redirect = true;
 				}
 				loadingFinished = false;
 				if (url.endsWith(".pdf") || url.endsWith(".jpg") || url.endsWith(".png")
 						|| url.endsWith(".jpeg")) {
 					showSaveConfirmationDialog(url);
 					redirect = false;
 					return false;
 				}
 				if ("about:blank".equals(url)) {
 					redirect = false;
 					return false;
 				}
 				MyLog.tempv("[shouldOverrideUrlLoading] load url");
 				mWebView.loadUrl(url);
 				mEditText.setText(url);
 				return true;
 			}
 
 			@Override
 			public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
 
 				super.onPageStarted(view, url, favicon);
 				loadingFinished = false;
 				// SHOW LOADING IF IT ISNT ALREADY VISIBLE
 				// setSupportProgressBarVisibility(true);
 				setSupportProgressBarIndeterminateVisibility(true);
 			}
 
 			@Override
 			public void onPageFinished(final WebView view, final String url) {
 
 				if (!redirect) {
 					loadingFinished = true;
 				}
 				if (loadingFinished && !redirect) {
 					// HIDE LOADING IT HAS FINISHED
 					// setSupportProgressBarVisibility(false);
 					setSupportProgressBarIndeterminateVisibility(false);
 				} else {
 					redirect = false;
 				}
 			}			
 		});
 		// mWebView.setWebChromeClient(new WebChromeClient() {
 		//
 		// @Override
 		// public void onProgressChanged(final WebView view, final int newProgress) {
 		//
 		// super.onProgressChanged(view, newProgress);
 		// setSupportProgress(newProgress * 100);
 		// }
 		// });
 		mWebView.requestFocus();
 	}
 
 	@Override
 	protected void onResume() {
 
 		super.onResume();
 		populateView();
 	}
 
 	private void populateView() {
 
 		Uri uri = getIntent().getData();
 		if (uri != null) {
 			String url = uri.toString();
 			mWebView.loadUrl(url);
 			mEditText.setText(url);			
 			if(url.contains("images.google.com")){
 				imageSearch = true;
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {
 
 		MenuInflater menuInflater = getSupportMenuInflater();
 		menuInflater.inflate(R.menu.actionbar_webview, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
 
 		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
 			mWebView.goBack();
 			mEditText.setText(mWebView.getUrl());
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onLongClick(final View arg0) {
 
 		HitTestResult result = mWebView.getHitTestResult();
 		if ((result.getType() == HitTestResult.IMAGE_TYPE)
 				|| (result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
 			// Menu options for an image.
 			// set the header title to the image url
 			String url = result.getExtra();
 			showSaveConfirmationDialog(url);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) {
 
 		switch (item.getItemId()) {
 			case R.id.menu_cancel:
 				setMyResult(RESULT_CANCELED, null);
 				finish();
 				break;
 			case R.id.menu_new_attachment:
 				onActionSave();
 				break;
 			case android.R.id.home:
 				finish();
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onControlServiceConnected() {
 
 		// TODO: this activity doesn't react with service
 	}
 
 	@Override
 	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
 
 		super.onCreateContextMenu(menu, v, menuInfo);
 		HitTestResult result = mWebView.getHitTestResult();
 		if ((result.getType() == HitTestResult.IMAGE_TYPE)
 				|| (result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
 			// Menu options for an image.
 			// set the header title to the image url
 			savedUrl = result.getExtra();
 			menu.setHeaderTitle(result.getExtra());
 			menu.add(0, ID_SAVE_IMAGE, 0, "Save Image").setOnMenuItemClickListener(this);
 			menu.add(0, ID_VIEW_IMAGE, 0, "View Image").setOnMenuItemClickListener(this);
 		} else if ((result.getType() == HitTestResult.ANCHOR_TYPE)
 				|| (result.getType() == HitTestResult.SRC_ANCHOR_TYPE)) {
 			// Menu options for a hyperlink.
 			// set the header title to the link url
 			savedUrl = result.getExtra();
 			menu.setHeaderTitle(result.getExtra());
 			menu.add(0, ID_SAVE_LINK, 0, "Save Link").setOnMenuItemClickListener(this);
 		}
 	}
 
 	@Override
 	public boolean onMenuItemClick(final android.view.MenuItem item) {
 
 		switch (item.getItemId()) {
 			case ID_SAVE_IMAGE:
 				onActionSave(savedUrl);
 				return true;
 			case ID_VIEW_IMAGE:
 				mWebView.loadUrl(savedUrl);
 				return true;
 			case ID_SAVE_LINK:
 				onActionSave(savedUrl);
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	private void onActionSave() {
 		if (!imageSearch) {
 			onActionSave(mWebView.getUrl());
 		} else {
 			getImage();
 		}
 	}
 
 	private void onActionSave(final String url) {
 
 		MyLog.tempv("[onActionSave] url: " + url);
 		Intent intent = getIntent();
 		Uri uri = Uri.parse(url);
 		intent.setData(uri);
 		setMyResult(RESULT_OK, intent);
 		finish();
 	}
 
 	private void setMyResult(final int code, final Intent intent) {
 
 		if (getParent() == null) {
 			setResult(code, intent);
 		} else {
 			getParent().setResult(code, intent);
 		}
 	}
 
 	private void showSaveConfirmationDialog(final String url) {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Save it?").setMessage(url).setCancelable(true).setPositiveButton(
 				android.R.string.ok, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(final DialogInterface dialog, final int id) {
 
 						onActionSave(url);
 					}
 				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(final DialogInterface dialog, final int id) {
 
 				dialog.cancel();
 				mWebView.loadUrl(url);
 			}
 		});
 		builder.create().show();
 	}
 
 	public boolean isTablet(final Context context) {
 
 		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
 		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
 		return (xlarge || large);
 	}
 	
 	private void getImage() {
 		//get the biggest image using javascript
 		String js = "javascript: ";
 		js += "var images = document.getElementsByTagName('img');";
 		js += "var tmpSize = 0; var index = 0; var ss = '';";
 		js += "for(var i = 0; i < images.length; i++){ ";
 		js += "var size = images[i].clientWidth*images[i].clientHeight;";
 		js += "if(tmpSize < size) {tmpSize = size; index = i;} ss+= size + '-' + i + ' ';}";
 		js += "window.JSI.onImageRecieved(images[index].src);";
 		mWebView.loadUrl(js);
 	}
 	
 	class JavaScriptInterface {
 		public void onImageRecieved(String url) {
 			MyLog.tempv("[onImageRecieved] " + url);
 			onActionSave(url);
 		}
 	}
 }
