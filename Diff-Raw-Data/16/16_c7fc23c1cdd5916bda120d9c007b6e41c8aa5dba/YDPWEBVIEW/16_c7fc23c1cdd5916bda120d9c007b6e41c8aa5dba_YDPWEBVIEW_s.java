 package com.example.ydp;
 
 
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 
 import android.util.Log;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class YDPWEBVIEW extends Activity {
 	WebView wb;
 	private ProgressDialog progressBar;
 	TextView user,pass;
 	boolean loadingFinished = true;
 	boolean redirect = false;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.webview);
 	wb = (WebView) findViewById(R.id.webView1);
 	wb.getSettings().setJavaScriptEnabled(true);
 	
 	wb.setInitialScale(1);
 	wb.getSettings().setLoadWithOverviewMode(true);
 	wb.getSettings().setUseWideViewPort(true);
 	wb.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 	wb.setScrollbarFadingEnabled(false);
 	final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 
     progressBar = ProgressDialog.show(YDPWEBVIEW.this, "YDP page is ", "Loading...");
 	
 	wb.loadUrl("https://yourdoctorprogram.com/qhr/Login.aspx/");
 	
 
 	 wb.setWebViewClient(new WebViewClient() {
 
 		   @Override
 		   public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
 		       if (!loadingFinished) {
 		          redirect = true;
 		       }
 
 		   loadingFinished = false;
 		   wb.loadUrl(urlNewString);
 		   return true;
 		   }
 
 		   public void onPageStarted(WebView view, String url) {
 		        loadingFinished = false;
 		        
 		    }
 
 		   @Override
 		   public void onPageFinished(WebView view, String url) {
 			   Log.d("page loaded","page not loaded");
 			   if(!redirect)
 			   {
 				   
 	             loadingFinished = true;
 	             }
 
 		       if(loadingFinished && !redirect){
 		    	   progressBar.dismiss();
 		    	   Log.d("page loaded","page");
 		    	  // wb.loadUrl("javascript:document.getElementById('ContentPlaceHolder_ContentPlaceHolder1_UserName').setAttribute('value',user);");
 		    	  // wb.loadUrl("javascript:document.getElementById('ContentPlaceHolder_ContentPlaceHolder1_txt_Password').setAttribute('value',pass);");
 		    	  // wb.loadUrl("javascript:document.getElementsByName('ctl00$ctl00$ContentPlaceHolder$ContentPlaceHolder1$bt_Login')[0].click();");
		    	   String user =getIntent().getExtras().getString("username");
		    	   String pass =getIntent().getExtras().getString("username");
 		    	   Log.d(user, "username");
 		    	   Log.d(pass, "password");
 		    	   wb.loadUrl("javascript:document.getElementById('ContentPlaceHolder_ContentPlaceHolder1_UserName').value='"+user+"';");
 		    	   wb.loadUrl("javascript:document.getElementById('ContentPlaceHolder_ContentPlaceHolder1_txt_Password').value='"+pass+"';");
		    	  //wb.loadUrl(" javascript:document.getElementById(\"ContentPlaceHolder_ContentPlaceHolder1_bt_Login\").click();");
		       
 		       } else{
 		          redirect = false; 
 		       }
 
 		    }
 		});
 	
 	 
 	 
 	}
 
 }
