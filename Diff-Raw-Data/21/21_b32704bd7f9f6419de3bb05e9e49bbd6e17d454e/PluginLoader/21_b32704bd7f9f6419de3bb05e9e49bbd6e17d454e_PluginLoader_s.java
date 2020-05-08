 package com.eas.android.libraries.rapidjs.pluginmanager;
 
 
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.webkit.WebView;
 
 public class PluginLoader {
 
 	private AcceleratedWebViewRequests requests;
 
 	private WebView webView;
 
 	private PluginManager pluginManager;
 	
 	public PluginLoader(PluginManager pluginManager, WebView webView, AcceleratedWebViewRequests requests, JSTools jsTools, Handler handler){
 		this.webView = webView;
 		this.requests = requests;
 		this.pluginManager = pluginManager;
 	}
 
 	public void loadUrl(final String url){
 		final class ContentLoaderTask extends AsyncTask<String, Void, String> {
 			@Override
 			protected String doInBackground(String... strings) {
 				final String content = getContent(url);
 				return content;
 			}
 			@Override
 			protected void onPostExecute(String content) {
 				webView.loadDataWithBaseURL(url, content, "text/html", "utf-8", null);
 			}
 		}
 		ContentLoaderTask contentLoaderTask = new ContentLoaderTask();
 		contentLoaderTask.execute((String[])null);
 	}
 
 	public String getContent(String url){
 		String content = requests.getUrlData(url);
 		content = injectJSAtBeginning(content, pluginManager.getPluginsJS());
 		return content;
 	}
 
 	public String getBaseUrl(String url){
 		return url.substring(0,url.lastIndexOf("/")+1);
 	}
 
 	private String injectJSAtBeginning(String html, String js){
 		String script = "<script type=\"text/javascript\"> " + js + " </script>";
 		return script + html;
 	}
 
 }
