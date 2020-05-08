 package com.sl.mimc;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.TextView;
 
 public class EntryActivity extends Activity implements DialogInterface.OnCancelListener {
 
	final Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 
 			Bundle data = msg.getData();
 			if (data.containsKey("onPageFinished"))	{		
 				if (progressDialog.isShowing())
 					progressDialog.dismiss();
 				return;
 			}
 		
 			if (data.containsKey("navigate"))
 			{
 				entryTag = data.getString("newTag");
 				title = data.getString("title");
 				date = data.getString("newDate");
 				entryBackTag = data.getString("newBack");
 				entryForwardTag = data.getString("newForward");
 				permalink = data.getString("newLink");
 			}
 
 			titleTextView.setText(data.getString("title"));
 			dateTextView.setText(data.getString("date"));
 			webView.loadUrl(data.getString("link"));
 
 			if (data.getString("title").length() == 0) {
 				ErrorNotification.noConnection(EntryActivity.this);
 			}
 		}
 	};
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.layout.entrymenu, menu);
 	    return true;
 	}
 	
 	public boolean onPrepareOptionsMenu (Menu menu) {
 		if (requestThread != null &&
 				requestThread.isAlive())
 	    {
 	    	for (int i = 0; i < menu.size(); ++i)
 	    		menu.getItem(i).setEnabled(false);
 	    }
 	    else
 	    {
 	    	MenuItem item = menu.findItem(R.id.back);
 	    	item.setEnabled(entryBackTag != null && entryBackTag.length() > 0);
 	    	
 	    	item = menu.findItem(R.id.forward);
 	    	item.setEnabled(entryForwardTag != null && entryForwardTag.length() > 0);
 	    }
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.back:
 	        {
 	            goBack();
 	            return true;
 	        }
 	        case R.id.forward:
 	        {
 	        	goForward();
 	            return true;
 	        }
 	        case R.id.share:
 	        {
 	        	Intent intent = new Intent(android.content.Intent.ACTION_SEND);
 	        	intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
 	        	intent.putExtra(android.content.Intent.EXTRA_TEXT, permalink);
 	        	intent.setType("text/plain");
 	        	intent = Intent.createChooser(intent,
 	        			getText(R.string.share));
 	        	
 	        	startActivity(intent);
 	            return true;
 	        }
 	        case R.id.comment:
 	        {
 	        	Intent intent = new Intent(android.content.Intent.ACTION_SEND);
 	        	intent.setType("plain/text");
 	        	intent.putExtra(android.content.Intent.EXTRA_EMAIL,
 	        			new String[] { "sleuthold@beuth-hochschule.de" });
 	        	intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 	        			title);
 	        	intent = Intent.createChooser(intent,
 	        			getText(R.string.sendMail));
 	        	startActivity(intent);
 
 	        	return true;
 	        }
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	public void onStop() {
 		super.onStop();
 		
 		if (requestThread != null &&
 				requestThread.isAlive())
 			requestThread.interrupt();
 			
 		Message msg = handler.obtainMessage();
 		Bundle data = new Bundle();
 		data.putBoolean("onPageFinished", true);
 		msg.setData(data);
 		handler.sendMessage(msg);
 	}
 	
 	public void onCancel (DialogInterface dialog) {
 		finish();
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.entry);
 		
 		requestThread = null;
 
 		progressDialog = ProgressDialog.show(this,
 				getText(R.string.progressTitle), getText(R.string.progressMsg), true, true, this);
 
 		titleTextView = (TextView) findViewById(R.id.textview);
 		dateTextView = (TextView) findViewById(R.id.date);
 
 		webView = (WebView) findViewById(R.id.webview);
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.getSettings().setSupportZoom(true);
 		
 		webView.setWebViewClient(new WebViewClient() {
 			public void onPageFinished(WebView view, String url)
 			{
 				Message msg = handler.obtainMessage();
 				Bundle data = new Bundle();
 				data.putBoolean("onPageFinished", true);
 				msg.setData(data);
 				handler.sendMessage(msg);
 			}
 		});
 
		

 		Intent intent = getIntent();
 		latest = intent.getExtras().getBoolean("latest", false);
 		category = "all";
 
 		if (latest) {
 			entryTag = "";
 			title = "";
 			date = "";
 		} else {
 			entryTag = intent.getExtras().getString("entryTag");
 			title = intent.getExtras().getString("entryTitle");
 			date = intent.getExtras().getString("entryDate");
 			entryBackTag = intent.getExtras().getString("entryBack");
 			entryForwardTag = intent.getExtras().getString("entryForward");
 			permalink = intent.getExtras().getString("entryLink");
 			category = intent.getExtras().getString("entryCategory");
 		}
 
 		requestThread = new Thread(new Runnable() {
 			public void run() {
 
 				NBAPIResponse nbapi = new NBAPIResponse();
 
 				if (latest) {
 
 					String response = nbapi
							.getText("https://cgi.beuth-hochschule.de/~sleuthold/nb_api/nb_api.cgi?q=latest");
 
 					JSONObject json = null;
 					try {
 						if (response != null)
 							json = new JSONObject(response);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 
 					if (json != null) {
 						try {
 							entryTag = json.getString("tag");
 							json = json.getJSONObject("data");
 							title = json.getString("TITLE");
 							date = json.getString("DATE");
 							permalink = json.getString("PERMALINK");
 							entryBackTag = json.getString("BACK");
 							entryForwardTag = json.getString("NEXT");
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 				Message msg = handler.obtainMessage();
 				Bundle data = new Bundle();
 				data.putString("title", title);
 				data.putString("date", date);
 				data.putString("link", link + entryTag);
 				msg.setData(data);
 				handler.sendMessage(msg);
 			}
 		});
 		requestThread.start();
 	}
 	
 	private void navigate(final String tag)
 	{
 		progressDialog = ProgressDialog.show(this,
 				getText(R.string.progressTitle), getText(R.string.progressMsg), true, true, this);
 		
 		webView.clearView();
 		webView.invalidate();
 		
 		requestThread = new Thread(new Runnable() {
 			public void run() {
 
 				NBAPIResponse nbapi = new NBAPIResponse();
 
 				String categoryQuery = "";
 				if (category.startsWith("20"))
 					categoryQuery += "&year=" + category;
 				else if (category != "all")
 					categoryQuery += "&category=" + category;
 
 				String response = nbapi
 				.getText("https://cgi.beuth-hochschule.de/~sleuthold/nb_api/nb_api.cgi?q=details" 
 						+ categoryQuery
 						+ "&tag=" + tag);
 
 				JSONObject json = null;
 				try {
 					if (response != null)
 						json = new JSONObject(response);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 				String newTag = null;
 				String newTitle = null;
 				String newDate = null;
 				String newLink = null;
 				String newBack = null;
 				String newForward = null;
 				
 				if (json != null) {
 					try {
 						newTag = json.getString("tag");
 						json = json.getJSONObject("data");
 						newTitle = json.getString("TITLE");
 						newDate = json.getString("DATE");
 						newLink = json.getString("PERMALINK");
 						newBack = json.getString("BACK");
 						newForward = json.getString("NEXT");
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 
 				Message msg = handler.obtainMessage();
 				Bundle data = new Bundle();
 				data.putBoolean("navigate", true);
 				data.putString("newTag", newTag);
 				data.putString("title", newTitle);
 				data.putString("date", newDate);
 				data.putString("link", link + newTag);
 				data.putString("permalink", newLink);
 				data.putString("newBack", newBack);
 				data.putString("newForward", newForward);
 				msg.setData(data);
 				handler.sendMessage(msg);
 			}
 		});
 		requestThread.start();
 	}
 	
 	private void goBack()
 	{
 		navigate(entryBackTag);
 	}
 	
 	private void goForward()
 	{
 		navigate(entryForwardTag);
 	}
 
 	private ProgressDialog progressDialog;
 	private TextView titleTextView;
 	private TextView dateTextView;
 	private WebView webView;
 	
 	private Thread requestThread;
 
 	private boolean latest;
 	private String entryTag;
 	private String entryBackTag;
 	private String entryForwardTag;
 	private String title;
 	private String date;
 	private String permalink;
 	private String category;
 	
 	private final String link = "https://cgi.beuth-hochschule.de/~sleuthold/nb_api/nb_api.cgi?q=entry&tag=";
 }
