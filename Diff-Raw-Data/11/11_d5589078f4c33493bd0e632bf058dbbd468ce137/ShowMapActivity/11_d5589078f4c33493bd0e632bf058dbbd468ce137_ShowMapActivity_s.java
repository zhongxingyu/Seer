 package com.jalinsuara.android.maps;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.ConsoleMessage;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebSettings.RenderPriority;
 import android.webkit.WebView;
 
 import com.actionbarsherlock.view.MenuItem;
 import com.jalinsuara.android.BaseFragmentActivity;
 import com.jalinsuara.android.R;
 import com.jalinsuara.android.helper.NetworkUtils;
 import com.jalinsuara.android.news.model.News;
 
 /**
  * 
  * Show maps activity
  * 
  * @author tonoman3g
  * 
  */
 public class ShowMapActivity extends BaseFragmentActivity {
 
 	/**
 	 * Web view for showing the maps. Map will be shown using leaflet
 	 */
 	private WebView mWebView;
 
 	private ArrayList<News> mList = new ArrayList<News>();
 	private News mNews = new News();
 	@Override
 	protected void onCreate(Bundle arg0) {
 		super.onCreate(arg0);
 
 		setContentView(R.layout.activity_show_map);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 		mWebView = (WebView) findViewById(R.id.activity_show_map_web_view);
 
 		mWebView.setWebChromeClient(new WebChromeClient() {
 
 			public boolean onConsoleMessage(ConsoleMessage cm) {
 				Log.d("JalinSuara Web View", cm.message() + " -- From line "
 						+ cm.lineNumber() + " of " + cm.sourceId());
 
 				return true;
 			}
 			
 			
 		});
 
 		WebSettings webSettings = mWebView.getSettings();
 		webSettings.setJavaScriptEnabled(true);
 		webSettings.setRenderPriority(RenderPriority.HIGH);
 		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
 		// multi-touch zoom
 		webSettings.setBuiltInZoomControls(true);
 		webSettings.setDisplayZoomControls(false);
 		
		//LoadNews task = new LoadNews();
		LoadOneNews task = new LoadOneNews();
 		task.execute();
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home: {
 			finish();
 			return true;
 		}
 
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private String readAsset(String fileName) {
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					getAssets().open(fileName), "UTF-8"));
 
 			// do reading, usually loop until end of file reading
 			StringBuilder sb = new StringBuilder();
 			String mLine = reader.readLine();
 			while (mLine != null) {
 				// process line
 				sb.append(mLine);
 				mLine = reader.readLine();
 			}
 
 			reader.close();
 			return sb.toString();
 		} catch (IOException e) {
 			// log the exception
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	/**
 	 * Load news
 	 * 
 	 * @author tonoman3g
 	 * 
 	 */
 	private class LoadNews extends AsyncTask<Object, Integer, Integer> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			resetStatus();
 			setStatusProgress(getString(R.string.loading), false);
 		}
 
 		@Override
 		protected Integer doInBackground(Object... params) {
 			mList = NetworkUtils.getPosts(0);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			super.onPostExecute(result);
 			if (mList != null) {
 				String data = readAsset("leaflet/index.html");
 				
 
 				StringBuilder sb = new StringBuilder();
 				int i = 0;
 				for (News news : mList) {
 					if (news.getLatitude() != 0 && news.getLongitude() != 0) {
 						if (i == 0) {
 							sb.append("{\"lat\":" + news.getLatitude());
 							sb.append(",\"lon\":" + news.getLongitude() + ", \"title\":\""+news.getTitle()+"\" "+", \"id\":\""+news.getId()+"\" "+"}");
 						} else {
 							sb.append(",{\"lat\":" + news.getLatitude());
 							sb.append(",\"lon\":" + news.getLongitude() + ", \"title\":\""+news.getTitle()+"\" "+", \"id\":\""+news.getId()+"\" "+"}");
 						}
 					}
 					i++;
 				}
 
 				if (i > 0) {
 					log.info("markers: "+sb.toString());
 					data = data.replace("{{posts}}", sb.toString());
 				} else {
 					data = data.replace("{{posts}}", "");
 										
 				}
 				log.info("html "+data);
 
 				mWebView.loadDataWithBaseURL("file:///android_asset/", data,
 						"text/html", "utf-8", "");
 				// or
 				// mWebView.loadUrl("file:///android_asset/leaflet/test.html");
 				
 				resetStatus();
 				setStatusShowContent();
 			}
 		}
 
 	}
 	
 	/**
 	 * Load A News
 	 * 
 	 * @author tonoman3g
 	 * 
 	 */
 	private class LoadOneNews extends AsyncTask<Object, Integer, Integer> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			resetStatus();
 			setStatusProgress(getString(R.string.loading), false);
 		}
 
 		@Override
 		protected Integer doInBackground(Object... params) {
 			mNews = NetworkUtils.getPosts(0).get(0);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			super.onPostExecute(result);
 			if (mNews != null) {
 				String data = readAsset("leaflet/index.html");
 				
 
 				StringBuilder sb = new StringBuilder();
 				int i = 0;
 	
 					if (mNews.getLatitude() != 0 && mNews.getLongitude() != 0) {
 						if (i == 0) {
 							sb.append("{\"lat\":" + mNews.getLatitude());
 							sb.append(",\"lon\":" + mNews.getLongitude() + ", \"title\":\""+mNews.getTitle()+"\" "+", \"id\":\""+mNews.getId()+"\" "+"}");
 						} else {
 							sb.append(",{\"lat\":" + mNews.getLatitude());
 							sb.append(",\"lon\":" + mNews.getLongitude() + ", \"title\":\""+mNews.getTitle()+"\" "+", \"id\":\""+mNews.getId()+"\" "+"}");
 						}
 					}
 					i++;
 				
 
 				if (i > 0) {
 					log.info("markers: "+sb.toString());
 					data = data.replace("{{posts}}", sb.toString());
 				} else {
 					data = data.replace("{{posts}}", "");
 										
 				}
 				log.info("html "+data);
 
 				mWebView.loadDataWithBaseURL("file:///android_asset/", data,
 						"text/html", "utf-8", "");
 				// or
 				// mWebView.loadUrl("file:///android_asset/leaflet/test.html");
 				
 				resetStatus();
 				setStatusShowContent();
 			}
 		}
 
 	}
 }
