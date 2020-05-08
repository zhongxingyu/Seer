 package com.clover.sdk.impl;
 
 import android.R;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import com.clover.sdk.CloverOrder;
 import com.clover.sdk.CloverOrderRequest;
 import com.clover.sdk.CloverUserInfo;
 import com.clover.sdk.CloverOrderListener;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class CloverOverlay extends Dialog {
 
   private final String TAG = CloverOverlay.class.getSimpleName();
 
   // static vars
   private static final ViewGroup.LayoutParams FILL_FILL =
           new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.FILL_PARENT);
 
   private static final String BASE_URL = "https://www.clover.com/";
   private static final String WEB_VIEW_URL = BASE_URL + "static/sdk-overlay.html#protocol=AndroidJavascriptBridge";
 
   private static final Map<String,String> sdkVersionInfo = sdkVersion();
 
   // instance vars
   private Handler handler = new Handler();
   private final String url;
   private ProgressDialog spinner;
   private FrameLayout contentLayout;
   private WebView webView;
   private ImageView cancelImage;
 
   private final CloverOrderRequest orderRequest;
   private final CloverUserInfo userInfo;
   final CloverOrderListener listener;
 
 
   public CloverOverlay(Context context, CloverOrderRequest orderRequest, CloverUserInfo userInfo, CloverOrderListener listener) {
     super(context, R.style.Theme_Translucent_NoTitleBar);
     this.orderRequest = orderRequest;
     this.userInfo = userInfo == null ? new CloverUserInfo() : userInfo;
     this.listener = listener;
     this.url = WEB_VIEW_URL;
   }
 
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     spinner = new ProgressDialog(getContext());
     spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
     spinner.setMessage("Loading...");
 
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     contentLayout = new FrameLayout(getContext());
 
     cancelImage = new ImageView(getContext());
     cancelImage.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
         CloverOverlay.this.dismiss();
         listener.onCancel();
       }
     });
 
     cancelImage.setImageDrawable(getContext().getResources().getDrawable(com.clover.sdk.R.drawable.cancel));
     cancelImage.setVisibility(View.INVISIBLE);
 
     setUpWebView(cancelImage.getDrawable().getIntrinsicWidth()/2);
 
     contentLayout.addView(cancelImage, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
     addContentView(contentLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
   }
 
   private void setUpWebView(int margin) {
     LinearLayout webViewLayout = new LinearLayout(getContext());
 
     webView = new WebView(getContext());
     webView.setVerticalScrollBarEnabled(false);
     webView.setHorizontalScrollBarEnabled(false);
     webView.getSettings().setJavaScriptEnabled(true);
     webView.setWebViewClient(new WebViewClient() {
 
       @Override
       public boolean shouldOverrideUrlLoading(WebView view, String url) {
         Log.d(TAG, "loading url " + url);
         return true;
       }
 
       @Override
       public void onReceivedError(WebView view, int errorCode,
                                   String description, String failingUrl) {
         super.onReceivedError(view, errorCode, description, failingUrl);
         Log.d(TAG, "onReceived error " + description + " failing url " + failingUrl);
         dismiss();
 //        // Dispatch the error to the caller
 //        listener.onFailure(new RuntimeException("Failed to load " + failingUrl));
       }
 
       @Override
       public void onPageStarted(WebView view, String url, Bitmap favicon) {
         super.onPageStarted(view, url, favicon);
         Log.d(TAG, "on page started " + url);
         spinner.show();
       }
 
       @Override
       public void onPageFinished(WebView view, String url) {
         super.onPageFinished(view, url);
         Log.d(TAG, "on page finished " + url);
         spinner.dismiss();
         contentLayout.setBackgroundColor(Color.TRANSPARENT);
         webView.setVisibility(View.VISIBLE);
         cancelImage.setVisibility(View.VISIBLE);
       }
     });
     webView.addJavascriptInterface(new CloverJSInterface(getContext()), "CloverAndroidSDK");
     webView.setLayoutParams(FILL_FILL);
     webView.setVisibility(View.INVISIBLE);
     webView.loadUrl(url);
     webViewLayout.setPadding(margin + 2, margin + 2, margin + 2, margin + 2);
     webViewLayout.addView(webView);
     contentLayout.addView(webViewLayout);
   }
 
   public class CloverJSInterface {
     Context context;
 
     public CloverJSInterface(Context c) {
       context = c;
     }
 
     public void receivedMessage(String type, String dataJson) {
       // type == "OverlayReady", "OpenCloverApp" or "HideOverlay" or "OrderAuthorized". dataJson we'll have to decide on
       Log.d(TAG, "got data " + dataJson + " type " + type);
       // when its overlayready send checkout json
       if ("OverlayReady".equals(type)) {
         sendOrder();
       } else if ("OrderAuthorized".equals(type)) {
         sendResult(dataJson);
       } else if ("HideCloseButton".equals(type)) {
        cancelImage.setVisibilty(View.GONE);
       }
     }
   }
 
   private void sendOrder() {
       JSONObject dataJson = orderRequest.toJson();
       final JSONObject payload = new JSONObject();
 
       try {
         dataJson.put("sdkInfo", new JSONObject(sdkVersionInfo));
         if (userInfo != null) userInfo.toJson(dataJson);
         payload.put("type", "Checkout");
         payload.put("data", dataJson);
         Log.d(TAG, "sending " + payload.toString(2));
       } catch (JSONException e) {
         Log.e(TAG, "Exception creating json ", e);
         dismiss();
         listener.onFailure(e);
         return;
       }
     try {
       handler.post(new Runnable() {
         @Override
         public void run() {
           webView.loadUrl("javascript:gBridge._onMessage(" + payload.toString() + ");void(0);");
         }
       });
     } catch (Exception ex) {
       dismiss();
       listener.onFailure(ex);
     }
   }
 
   public void sendResult(final String dataJson) {
     dismiss();
     handler.post(new Runnable() {
       @Override
       public void run() {
         try {
           CloverOrder order = Utils.parseCloverOrder(dataJson);
           listener.onOrderAuthorized(order);
         } catch (JSONException e) {
           listener.onFailure(e);
         } catch (Exception e) {
           listener.onFailure(e);
         }
       }
     });
   }
 
   private static Map<String,String> sdkVersion() {
     Map<String,String> sdkInfo = new HashMap<String,String>(2);
     sdkInfo.put("platform", "android");
     sdkInfo.put("version", "0.0.1");
     return sdkInfo;
   }
 }
