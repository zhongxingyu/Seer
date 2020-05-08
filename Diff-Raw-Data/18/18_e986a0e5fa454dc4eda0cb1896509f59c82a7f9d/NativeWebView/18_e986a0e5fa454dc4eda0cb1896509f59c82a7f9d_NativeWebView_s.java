 package org.opentravelmate.widget.webview;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.opentravelmate.R;
 import org.opentravelmate.commons.ExceptionListener;
 import org.opentravelmate.commons.I18nException;
 import org.opentravelmate.commons.UIThreadExecutor;
 import org.opentravelmate.widget.HtmlLayout;
 import org.opentravelmate.widget.HtmlLayoutParams;
 import org.opentravelmate.widget.map.NativeMap;
 import org.opentravelmate.widget.menu.NativeMenu;
 
 import android.annotation.SuppressLint;
 import android.view.View;
 import android.webkit.ConsoleMessage;
 import android.webkit.JavascriptInterface;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 /**
  * Injected object.
  * 
  * @author marc.plouhinec@gmail.com (Marc Plouhinec)
  */
 public class NativeWebView {
 	
 	public static final String GLOBAL_OBJECT_NAME = "org_opentravelmate_native_widget_webview_nativeWebView";
 	public static final String SCRIPT_URL = "/native/widget/webview/nativeWebView.js";
 	private final ExceptionListener exceptionListener;
 	private final HtmlLayout htmlLayout;
 	private final String baseUrl;
 	private final NativeMenu nativeMenu;
 	private final NativeMap nativeMap;
 	
 	/**
 	 * Create a NativeWebView object.
 	 */
 	public NativeWebView(
 			ExceptionListener exceptionListener, HtmlLayout htmlLayout, String baseUrl, NativeMenu nativeMenu, NativeMap nativeMap) {
 		this.exceptionListener = exceptionListener;
 		this.htmlLayout = htmlLayout;
 		this.baseUrl = baseUrl;
 		this.nativeMenu = nativeMenu;
 		this.nativeMap = nativeMap;
 	}
 	
 	/**
 	 * Build the native view object for the current widget.
 	 * 
 	 * @param jsonLayoutParams
 	 */
 	@JavascriptInterface
 	public void buildView(final String jsonLayoutParams) {
 		UIThreadExecutor.execute(new Runnable() {
 			@Override public void run() {
 				try {
 					buildView(HtmlLayoutParams.fromJsonLayoutParams(new JSONObject(jsonLayoutParams)));
 				} catch (JSONException e) {
 					exceptionListener.onException(false, e);
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Update the native view object for the current widget.
 	 * 
 	 * @param jsonLayoutParams
 	 */
 	@JavascriptInterface
 	public void updateView(final String jsonLayoutParams) {
 		UIThreadExecutor.execute(new Runnable() {
 			@Override public void run() {
 				try {
 					HtmlLayoutParams layoutParams = HtmlLayoutParams.fromJsonLayoutParams(new JSONObject(jsonLayoutParams));
 					View view = htmlLayout.findViewByPlaceHolderId(layoutParams.id);
 					if (view != null) {
 						view.setLayoutParams(layoutParams);
 					}
 				} catch (JSONException e) {
 					exceptionListener.onException(false, e);
 				}
 				
 			}
 		});
 	}
 	
 	/**
 	 * Remove the native view object for the current widget.
 	 * 
 	 * @param id Place holder ID
 	 */
 	@JavascriptInterface
 	public void removeView(final String id) {
 		UIThreadExecutor.execute(new Runnable() {
 			@Override public void run() {
 				View view = htmlLayout.findViewByPlaceHolderId(id);
 				if (view != null) {
 					htmlLayout.removeView(view);
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Fire an event to a listener that is outside of the WebView.
 	 * 
 	 * @param webViewPlaceHolderId
 	 * @param eventName
 	 * @param jsonPayload
 	 */
 	@JavascriptInterface
 	public void fireExternalEvent(final String webViewPlaceHolderId, final String eventName, final String jsonPayload) {
 		UIThreadExecutor.execute(new Runnable() {
 			@Override public void run() {
 				WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
 				mainWebView.loadUrl("javascript:(function(){" +
 						"    require(['extensions/core/widget/Widget'], function (Widget) {" +
 						"        var subWebView = Widget.findById('" + webViewPlaceHolderId + "');" +
 						"        if (!subWebView) { return; };" +
						"        var payload = JSON.parse('" + jsonPayload.replace("\"", "\\\"") + "');" +
 						"        subWebView.fireEventFromInternal('" + eventName + "', payload);" +
 						"    });" +
 						"})();");
 			}
 		});
 	}
 	
 	/**
 	 * Fire an event to a listener that is inside the WebView.
 	 * 
 	 * @param webViewPlaceHolderId
 	 * @param eventName
 	 * @param jsonPayload
 	 */
 	@JavascriptInterface
 	public void fireInternalEvent(final String webViewPlaceHolderId, final String eventName, final String jsonPayload) {
 		UIThreadExecutor.execute(new Runnable() {
 			@Override public void run() {
 				WebView webView = (WebView)htmlLayout.findViewByPlaceHolderId(webViewPlaceHolderId);
 				webView.loadUrl("javascript:(function(){" +
 						"    require(['extensions/core/widget/webview/webview'], function (webview) {" +
						"        var payload = JSON.parse('" + jsonPayload.replace("\"", "\\\"") + "');" +
 						"        webview.fireEventFromExternal('" + eventName + "', payload);" +
 						"    });" +
 						"})();");
 			}
 		});
 	}
 	
 	/**
 	 * Build the native view object for the current widget.
 	 * 
 	 * @param layoutParams
 	 */
 	@SuppressLint("SetJavaScriptEnabled")
 	public void buildView(final HtmlLayoutParams layoutParams) {
 		// Create the web view
 		WebView webView = new WebView(htmlLayout.getContext());
 		webView.setLayoutParams(layoutParams);
 		webView.getSettings().setJavaScriptEnabled(true);
 		htmlLayout.addView(webView);
 		
 		// Register event handlers
 		webView.setWebViewClient(new WebViewClient() {
 			private boolean isFirstPage = true;
 			
 			@Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 				exceptionListener.onException(false, new I18nException(
 						R.string.webview_received_error, null, layoutParams.id, description));
 			}
 			@Override public void onPageFinished(WebView view, String url) {
 				if (isFirstPage) {
 					isFirstPage = false;
 					runStartupScript(view);
 				}
 			}
 		});
 		webView.setWebChromeClient(new WebChromeClient() {
 			@Override public boolean onConsoleMessage(ConsoleMessage cm) {
 				exceptionListener.onException(false, new I18nException(
 								R.string.webview_javascript_console_msg, null, layoutParams.id, cm.message(), cm.lineNumber(), cm.sourceId()));
 				return true;
 			}
 		});
 		
 		// Inject java objects
 		webView.addJavascriptInterface(this, NativeWebView.GLOBAL_OBJECT_NAME);
 		webView.addJavascriptInterface(this.nativeMenu, NativeMenu.GLOBAL_OBJECT_NAME);
 		webView.addJavascriptInterface(this.nativeMap, NativeMap.GLOBAL_OBJECT_NAME);
 		
 		// Set the URL
 		webView.loadUrl(this.baseUrl + layoutParams.additionalParameters.get("url"));
 	}
 	
 	/**
 	 * Inject the startup script when the web view is loading its first page.
 	 * 
 	 * @param webView
 	 */
 	private void runStartupScript(WebView webView) {
 		if (!(webView.getLayoutParams() instanceof HtmlLayoutParams)) {
 			return;
 		}
 		HtmlLayoutParams layoutParams = (HtmlLayoutParams) webView.getLayoutParams();
 		
 		// Set global constants and inject the scripts
 		webView.loadUrl("javascript:(function(){" +
 				"  window.org_opentravelmate_widget_webview_webviewId='" + layoutParams.id + "';" +
 				"  window.org_opentravelmate_widget_webview_webviewUrl='" + layoutParams.additionalParameters.get("url") + "';" +
 				"  window.org_opentravelmate_widget_webview_webviewEntrypoint='" + layoutParams.additionalParameters.get("entrypoint") + "';" +
 				"  window.org_opentravelmate_widget_webview_webviewBaseUrl='" + this.baseUrl + "';" +
 				"  window.org_opentravelmate_widget_webview_additionalParameters=" + layoutParams.getAdditionalParametersAsJson() + ";" +
 				"  var script = document.createElement('script');" +
 				"  script.src = '" + this.baseUrl + "extensions/core/lib/require.min.js';" +
 				"  script.setAttribute('data-main', '" + this.baseUrl + "extensions/core/widget/webview/startupScript');" +
 				"  document.body.appendChild(script);" +
 				"})();");
 	}
 
 }
