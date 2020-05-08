 package com.android.task.web;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 public class MyWebClient extends WebViewClient{
 	private Activity a;
 	private ProgressDialog mLoadingBar;
 	public MyWebClient(Activity a)
 	{
 		super();
 		this.a  = a;
 	}
 	public boolean shouldOverrideUrlLoading(WebView view, String url) {
 		if (url.endsWith(".3gp")){
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setDataAndType(Uri.parse(url),"video/3gp");
 			a.startActivity(intent);
 			return true;
 		}else if(url.endsWith(".mp4")){
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setDataAndType(Uri.parse(url),"video/mp4");
 			a.startActivity(intent);
 			return true;
 		}
         view.loadUrl(url);
         return true;
     }
 	
 	@Override
 	public void onPageStarted(WebView view, String url, Bitmap favicon) 
 	{
 		//mLoadingBar=ProgressDialog.show(this.a, null, "ڼء");
		Toast.makeText(this.a, "ʷ...", Toast.LENGTH_SHORT).show();
 	}
 	
 	@Override
 	public void onPageFinished(WebView view, String url) {
	Toast.makeText(this.a, "", Toast.LENGTH_SHORT).show();
 		super.onPageFinished(view, url);
 	}
 	@Override
     public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
         Toast.makeText(this.a, "", Toast.LENGTH_SHORT).show();
         final AlertDialog alertDialog = new AlertDialog.Builder(this.a).create();
         alertDialog.setTitle("");
         alertDialog.setMessage(description);
         alertDialog.setButton("OK", new DialogInterface.OnClickListener(){
             public void onClick(DialogInterface dialog, int which) {
             
             	alertDialog.dismiss();
             }
         });
         alertDialog.show();
     }
 }
