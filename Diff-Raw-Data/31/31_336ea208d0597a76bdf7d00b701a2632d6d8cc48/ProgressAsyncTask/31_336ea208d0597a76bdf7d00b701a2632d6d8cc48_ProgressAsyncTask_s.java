 package com.github.mobileartisans.bawall.component;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.widget.Toast;
 import com.github.mobileartisans.bawall.LoginActivity;
 import com.github.mobileartisans.bawall.domain.HttpClientException;
 
 public abstract class ProgressAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
     private ProgressDialog dialog;
     protected Context context;
     private HttpClientException e;
 
     protected ProgressAsyncTask(Context context) {
         this.context = context;
     }
 
     @Override
     protected void onPreExecute() {
         dialog = new ProgressDialog(context);
         dialog.show();
     }
 
     @Override
     protected final Result doInBackground(Params... params) {
         try {
             return process(params);
         } catch (HttpClientException e) {
             this.e = e;
         }
         return null;
     }
 
     protected void onUnAuthorized() {
         Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show();
         context.startActivity(new Intent(context, LoginActivity.class));
     }
 
     protected void onNotFound() {
         Toast.makeText(context, "Not found", Toast.LENGTH_SHORT).show();
     }
 
     protected void onError() {
         Toast.makeText(context, "Whoops! Something went wrong.", Toast.LENGTH_SHORT).show();
     }
 
     protected void onSuccess(Result result) {
     }
 
     protected abstract Result process(Params... params);
 
     @Override
     protected final void onPostExecute(Result result) {
         super.onPostExecute(result);
        if (result != null) {
             onSuccess(result);
        } else if (e.error.equals(HttpClientException.ClientError.UNKNOWN)) {
             onError();
        } else if (e.error.equals(HttpClientException.ClientError.NOT_FOUND)) {
             onNotFound();
        } else if (e.error.equals(HttpClientException.ClientError.UNAUTHORIZED)) {
             onUnAuthorized();
         }
         dialog.dismiss();
     }
 }
