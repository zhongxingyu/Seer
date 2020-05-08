 package edu.channel4.mm.db.android.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.ConsoleMessage;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import edu.channel4.mm.db.android.R;
 import edu.channel4.mm.db.android.model.request.GraphRequest;
 import edu.channel4.mm.db.android.network.SalesforceConn;
 import edu.channel4.mm.db.android.util.Keys;
 import edu.channel4.mm.db.android.util.Logger;
 
 public class GraphViewerActivity extends Activity {
 
 	private WebView webView;
 	private String requestUrlParameters;
 	private GraphRequest graphRequest;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_graph_viewer);
 
 		// Retrieve the GraphRequest from the Intent
 		graphRequest = (GraphRequest) getIntent().getExtras().getSerializable(
 				Keys.GRAPH_REQUEST_TYPE);
 		
 		//get the url parameters that were put into intent by the graphrequest
 		requestUrlParameters = getIntent().getStringExtra(Keys.REQUEST_URL_PARAMETERS);
 
 		// Setup the WebView
 		webView = (WebView) findViewById(R.id.webViewGraphViewer);
 		webView.setWebViewClient(new WebViewClient());
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.setWebChromeClient(new WebChromeClient() {
 			public boolean onConsoleMessage(ConsoleMessage cm) {
 				Log.d("MyApplication",
 						cm.message() + " -- From line " + cm.lineNumber()
 								+ " of " + cm.sourceId());
 				return true;
 			}
 		});
 
 		Logger.i("Sending graphRequest: " + graphRequest);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		sendGraphRequest();
 	}
 
 	private void sendGraphRequest() {
 		// get graph URL
 		SalesforceConn sfConn = SalesforceConn.getInstance(getApplicationContext());
 		String url = sfConn.getGraphViewingURL();
 
 		// construct URL parameters
 		//url = addParamsToUrl(url);
		url += "&" + requestUrlParameters;
 		
 		Log.d("MyApplication", "URL: " + url);
 
 		// point WebView to Graph URL with correct params
 		webView.loadUrl(url);
 	}
 
 	protected String addParamsToUrl(String url) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		//Should be "custom" "SessionOverTime", "EventOverTime", etc.
 		params.add(new BasicNameValuePair(Keys.REQUEST_TYPE, graphRequest.getRestRequestType()));
 		
 		//TOOD(mlerner): Get the URL parameters from the GraphRequest and add those to the url
 		
 		
 		// TODO FIXME: App Name is hardcoded right now
 		params.add(new BasicNameValuePair(Keys.APP_NAME, "a04E00000034dlH"));
 
 		String paramString = URLEncodedUtils.format(params, "utf-8");
 
 		url += paramString;
 		return url;
 	}
 }
