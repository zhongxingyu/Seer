 // Copyright 2012 Google Inc. All Rights Reserved.
 
 package com.google.android.apps.cloudprint;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Base64;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 /** Cloud Print dialog to select a printer to print to. */
 public class CloudPrintDialog extends Activity {
   private static final String PRINT_DIALOG_URL = "https://www.google.com/cloudprint/dialog.html";
   private static final String JS_INTERFACE = "AndroidPrintDialog";
 
   /**
    * Post message that is sent by Print Dialog web page when the printing dialog
    * needs to be closed.
    */
   private static final String CLOSE_POST_MESSAGE_NAME = "cp-dialog-on-close";
 
   /** Web view element to show the printing dialog in. */
   private WebView dialogWebView;
   
   /** Intent that started the action. */
   Intent cloudPrintIntent;
 
   final class PrintDialogJavaScriptInterface {
 	  public String getType() {
 	    return "dataUrl";
 	  }
 
 	  public String getTitle() {
 	    return cloudPrintIntent.getExtras().getString("title");
 	  }
 
 	  public String getContent() {
 	    try {
 	      ContentResolver contentResolver = getContentResolver();
 	      InputStream is = contentResolver.openInputStream(cloudPrintIntent.getData());
 	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
 	        
 	      byte[] buffer = new byte[4096];
 	      int n = is.read(buffer);
 	      while (n >= 0) {
 	        baos.write(buffer, 0, n);
 	        n = is.read(buffer);
 	      }
 	      is.close();
 	      baos.flush();
 	        
 	      String contentBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
 	      return "data:" + cloudPrintIntent.getType() + ";base64," + contentBase64;
 	    } catch (Exception e) {
 	      e.printStackTrace();
 	    }
 	    return "";
 	  }
 
 	  public void onPostMessage(String message) {
 	    if (message.startsWith(CLOSE_POST_MESSAGE_NAME)) {
 	      finish();
 	    }
 	  }
   }
 
   @Override
   public void onCreate(Bundle icicle) {
     super.onCreate(icicle);

     setContentView(R.layout.print_dialog);
     dialogWebView = (WebView) findViewById(R.id.webview);
     cloudPrintIntent = this.getIntent();
 
     WebSettings settings = dialogWebView.getSettings();
     settings.setJavaScriptEnabled(true);
     
     dialogWebView.setWebViewClient(new PrintDialogWebClient());
     dialogWebView.addJavascriptInterface(
        new PrintDialogJavaScriptInterface(), JS_INTERFACE);
 
     dialogWebView.loadUrl(PRINT_DIALOG_URL);
   }
 
   private final class PrintDialogWebClient extends WebViewClient {
     @Override
     public boolean shouldOverrideUrlLoading(WebView view, String url) {
       view.loadUrl(url);
       return false;
     }
 
     @Override
     public void onPageFinished(WebView view, String url) {
       if (PRINT_DIALOG_URL.equals(url)) {
         // Submit print document.
         view.loadUrl("javascript:printDialog.setPrintDocument(printDialog.createPrintDocument("
            + "window." + JS_INTERFACE + ".getType(),window." + JS_INTERFACE + ".getTitle(),"
            + "window." + JS_INTERFACE + ".getContent()))");
 
         // Add post messages listener.
         view.loadUrl("javascript:window.addEventListener('message',"
             + "function(evt){window." + JS_INTERFACE + ".onPostMessage(evt.data)}, false)");
       }
     }
   }
 }
