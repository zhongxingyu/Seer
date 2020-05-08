 package mobi.monaca.framework.view;
 
 
 import mobi.monaca.framework.MonacaPageActivity;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.framework.util.UrlUtil;
 
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.CordovaWebViewClient;
 
 import android.annotation.SuppressLint;
 import android.graphics.Bitmap;
 import android.net.http.SslError;
 import android.os.Message;
 import android.webkit.HttpAuthHandler;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebView;
 
 /** This WebViewClient is used for an WebView DroidgapActivity has. */
 public class MonacaPageGingerbreadWebViewClient extends CordovaWebViewClient {
 
     private static final String TAG = MonacaPageGingerbreadWebViewClient.class.getSimpleName();
     protected MonacaPageActivity monacaPage;
     protected String currentUrl;
     protected boolean isRemote;
 
     public MonacaPageGingerbreadWebViewClient(String currentUrl,
             MonacaPageActivity monacaPage, CordovaWebView webView) {
     	super(monacaPage, webView);
         this.currentUrl = currentUrl;
         this.monacaPage = monacaPage;
     }
 
     @Override
     public boolean shouldOverrideUrlLoading(WebView view, String uri) {
     	MyLog.v(TAG, "shouldOverrideUrlLoading url:" + uri);
 
         if (uri != null) {
         	if (uri.contains("file:///android_res/raw/home")) {
         		MyLog.v(TAG, "Going home from 404");
         		monacaPage.goHomeAsync(null);
         		return true;
         	} else if (UrlUtil.isMonacaUri(monacaPage, uri)) {
//       		MyLog.d(TAG, "load as monaca application : " + uri);
 //        		monacaPage.loadUri(uri, false);
//              return true;

        		// MyLog.d(TAG, "update currentPageUri and UI : " + uri);
        		monacaPage.setCurrentUri(uri);
        		monacaPage.loadUiFile(monacaPage.getCurrentUriWithoutQuery());
             } else {
             	MyLog.d(TAG, "return super.shouldOverrideUrlLoading");
                 return super.shouldOverrideUrlLoading(view, uri);
             }
         }
         return false;
     }
 
     @Override
     public void doUpdateVisitedHistory(WebView view, String url,
             boolean isReload) {
         super.doUpdateVisitedHistory(view, url, isReload);
     }
 
     @Override
     public void onLoadResource(WebView view, String url) {
     	monacaPage.onLoadResource(view, url);
     	super.onLoadResource(view, url);
     }
 
     @Override
     public void onFormResubmission(WebView view, Message dontResend,
             Message resend) {
         super.onFormResubmission(view, dontResend, resend);
     }
 
     @Override
     public void onPageFinished(WebView view, String url) {
 //    	MyLog.d(TAG, "onPageFinished: " + url);
         monacaPage.onPageFinished(view, url);
         super.onPageFinished(view, url);
     }
 
     @Override
     public void onPageStarted(WebView view, String url, Bitmap favicon) {
 //    	MyLog.d(TAG, "onPageStarted: " + url);
         monacaPage.onPageStarted(view, url);
         super.onPageStarted(view, url, favicon);
     }
 
     @Override
     public void onReceivedError(WebView view, int errorCode,
             String description, String failingUrl) {
     	MyLog.d(TAG, "received error:");
     	MyLog.d(TAG, "  errorCode:" + errorCode);
     	MyLog.d(TAG, "  description:" + description);
     	MyLog.d(TAG, "  failingUrl:" + failingUrl);
         monacaPage.onReceivedError(view, errorCode, description, failingUrl);
 
         if(errorCode == ERROR_FILE_NOT_FOUND || errorCode == ERROR_UNKNOWN){
         	monacaPage.push404Page(failingUrl);
         } else {
         	 super.onReceivedError(view, errorCode, description, failingUrl);
         }
     }
 
     @SuppressLint("NewApi")
 	public void onReceivedSslError(WebView view, SslErrorHandler handler,
             SslError error) {
         MyLog.e(TAG, " received ss: error:" + error.toString());
         handler.proceed();
     }
 
     @Override
     public void onReceivedHttpAuthRequest(WebView webView,
             HttpAuthHandler handler, String host, String realm) {
     	MyLog.d(TAG, "onReceivedHttpAuthRequest: host => " + host + ", realm => "
                 + realm);
         String[] up = webView.getHttpAuthUsernamePassword(host, realm);
 
         if (up != null && up.length == 2 && up[0] != null && up[1] != null) {
             handler.proceed(up[0], up[1]);
         } else {
             super.onReceivedHttpAuthRequest(webView, handler, host, realm);
         }
     }
 }
