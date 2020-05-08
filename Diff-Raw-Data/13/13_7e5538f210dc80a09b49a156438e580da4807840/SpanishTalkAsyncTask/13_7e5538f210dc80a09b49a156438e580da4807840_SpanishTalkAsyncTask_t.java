 package com.example.logic;
 
 import org.apache.http.HttpResponse;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
 import android.widget.ProgressBar;
 
 import com.example.lib.HttpPack;
 import com.example.lib.SessionManagement;
 import com.example.spanishtalk.R;
 import com.example.spanishtalk.SpanishTalkApplication;
 
 
 
 
 public class SpanishTalkAsyncTask<TParams> extends AsyncTask<TParams, Void, HttpResponse> {
 	private Context context = SpanishTalkApplication.context;
 	private ProgressBar progressBar;
 	
 	public SpanishTalkAsyncTask() {
     }
 	
 	
     public SpanishTalkAsyncTask(ProgressBar v) {
     	progressBar = v;
     }
     
 	@Override
 	protected HttpResponse doInBackground(TParams... arg0) {
 		
 		HttpResponse response = doPost();
 		
 		if (response == null) {
 			cancel(true);
 			return null;
 		}
 		
 		return response;
 	}
 	
 	@Override
 	protected void onPreExecute() {
		super.onPreExecute();
 		showNoticeView();
 				
 		if (!HttpPack.hasConnected()) {
 			BaseAction.showFormNotice(context.getString(R.string.network_error));
 			cancel(true);
 			return;
 		}
 	}
 
 	@Override
 	protected void onCancelled() {
 		hideNoticeView();
 		BaseAction.showFormNotice(context.getString(R.string.server_connection_error));
 	}
 
 	@Override
 	protected void onPostExecute(HttpResponse response) {
 		hideNoticeView();
 		
 		Integer statusCode = response.getStatusLine().getStatusCode();
 		switch (statusCode) {
         	case 200:  
         		onSuccess(response);
         		break;
         	case 401:
         		loginRequired();
         		break;
         	case 404:
         		BaseAction.showFormNotice(context.getString(R.string.server_connection_error));
         		break;
         	case 500:
         		BaseAction.showFormNotice(context.getString(R.string.server_connection_error));
         		break;
         	default:
         		loginRequired();
         		break;
 		}
 		
 		super.onPostExecute(response);
 	}
 	
 	protected HttpResponse doPost() {
 		HttpResponse response = null;
 		return response;
 	}
 	
 	protected void showNoticeView() {
 		progressBar.setVisibility(View.VISIBLE);
 	}
 	
 	protected void hideNoticeView() {
 		progressBar.setVisibility(View.GONE);
 	}
 	
 	protected void onSuccess(HttpResponse response) {
 		
 	}
 	
 	protected void loginRequired() {
 		
 	}
 
 }
