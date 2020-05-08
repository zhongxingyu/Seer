 /*
  * 	This file is part of ASage.
  *
  *    ASage is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    ASage is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with ASage.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   Copyright 2012 Giovanni Visentini 
  */
 
 package com.wise.gui;
 
 import java.io.ByteArrayOutputStream;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.transform.Result;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import com.wise.R;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 /**
  * class that show the rss content
  * @author Giovanni Visentini
  * */
 public class RssViewFragment extends OnlineFragment {
 
 	private final static String TAG="RssView";
 	public final static String RSS_URL = "url";
 
 	private URL feedXml;
 	private Transformer mRss2Html;
 	private WebView browser;
 	
 	/**
 	 * @see android.app.Fragment#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		try {
 			mRss2Html = TransformerFactory.newInstance().newTransformer(
 					new StreamSource(this.getResources().openRawResource(
 							R.raw.rss2html)));
 		} catch (TransformerConfigurationException e) {
 			Log.d(TAG, e.toString());
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			Log.d(TAG, e.toString());
 			e.printStackTrace();
 		}//try-catch
 
 		if (savedInstanceState != null){
 			viewRss(savedInstanceState.getString(RSS_URL));
 		}
 		
 		
 	}// onCreate
 
 	/**
 	 * @see android.app.Fragment#onStart()
 	 */
 	/*we use onStart for be secure that the webView is initialized */
 	public void onStart() {
 		super.onStart();
 		SharedPreferences pref =PreferenceManager.getDefaultSharedPreferences(getActivity());
 		browser.setWebViewClient(
 				new WebClientManager(pref.getBoolean("useExternalBrowser",true)));
 	}
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.rss_view, container, false);
         
         browser = (WebView) v.findViewById(R.id.browser);
         //enable pic-to zoom for the pages
         browser.getSettings().setBuiltInZoomControls(true);
         showDefaultContentBrowser(); 
         return v;
     }
 
     public void showError(int messageId, Object... paramiter){
     	super.showError(messageId, paramiter);
     	showDefaultContentBrowser();
     }
 
     public void showDefaultContentBrowser(){
     	/*Log.d(TAG, getResources().get.getString(R.raw.feed_icon));
     	browser.load
     	browser.loadData(getResources().getString(R.raw.feed_icon),"image/svg+xml","UTF-8");*/
     }
     
     
     public void viewRss(String url){
     	if(isOnline()){
 	    	try {
 				feedXml = new URL(url);
 				new LoadRss().execute(feedXml);
 			} catch (MalformedURLException e) {
 				showError(ERROR_URL,url);
 			}//try-catch
     	}else{
     		showError(ERROR_NETWORK);
 		}//if-else
 	}//viewRss
     
 	/**
 	 * @see android.app.Fragment#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// save the feed
 		if(feedXml!=null)
 			outState.putString(RSS_URL, feedXml.toString());
 	}
 
 	/**
 	 *class that load the xml and convert it into an html file
 	 */
 	private class LoadRss extends AsyncTask<URL, Void, Boolean> {
 
 		//buffer for the downloaded page
 		private ByteArrayOutputStream page = new ByteArrayOutputStream();
 		
 		@Override
 		protected Boolean doInBackground(URL... rssUrl) {
 			
 			if(!isOnline()){
 				showError(ERROR_NETWORK);
 			}
 			
 			Result html = new StreamResult(page);
 			
 			try {
 				HttpURLConnection urlConnection = (HttpURLConnection) rssUrl[0].openConnection();	
 				urlConnection.addRequestProperty("Cache-Control", "max-age=600"); // 10min
 				StreamSource s = new StreamSource(urlConnection.getInputStream());
 				mRss2Html.transform(s, html);
 			} catch (IOException e) {
 				showError(ERROR_URL,feedXml.toString());
 				return false;
 			} catch (TransformerException e) {
 				showError(ERROR_XML,feedXml.toString());
 				return false;
 			}			
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean loaded) {
 			Log.d(TAG, "Page Loaded:" + loaded);
 			
 			if(loaded){
 				String html = page.toString();
 				if(!html.isEmpty()){
 					browser.loadDataWithBaseURL("file:///android_asset/",page.toString(), "text/html", "utf8",null);
 
 				}else
 					showError(ERROR_XML,feedXml.toString());
 			}
 		}
 
 	}
 
 	/**
 	 *class that manage the browser 
 	 *used for open a link into the default browser instead of using the fragment
 	 */
 	private class WebClientManager extends WebViewClient {
 		
 		private boolean useExternalBrowser;
 		
 		public WebClientManager(boolean useExternalBrowser){
 			this.useExternalBrowser=useExternalBrowser;
 		}
 		
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			
			if(!useExternalBrowser){
 				browser.loadUrl(url);
 			}else{
 				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 				view.getContext().startActivity(i);
 			}
 			return true;
 		}
 
 	}
 
 }
