 package com.tobbetu.en4s.helpers;
 
 import android.os.AsyncTask;
 
 public abstract class BetterAsyncTask<Params, Result> extends
         AsyncTask<Params, Void, Boolean> {
 
     private Result value;
     private Exception error;
 
     protected abstract Result task(Params... arg0) throws Exception;
 
     protected abstract void onSuccess(Result result);
 
     protected abstract void onFailure(Exception error);
 
     @Override
     protected Boolean doInBackground(Params... arg0) {
         try {
             value = task(arg0);
             return true;
         } catch (Exception e) {
             error = e;
             cancel(true);
             return false;
         }
     }
 
     @Override
     protected final void onPostExecute(Boolean result) {
        onSuccess(value);
     }
 
     @Override
    protected final void onCancelled(Boolean result) {
         onFailure(error);
     }
 }
