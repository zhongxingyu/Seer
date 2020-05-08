 package com.music.fmv.tasks;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import com.music.fmv.api.Api;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lebed
  * Date: 7/14/13
  * Time: 8:49 AM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class BaseAsyncTask<Result> extends AsyncTask<Void, Void, Result>{
     protected Context context;
     protected Api api = new Api();
 
     protected BaseAsyncTask(Context context) {
         this.context = context;
     }
 
    protected void onError(boolean isCaptcha){};
 }
