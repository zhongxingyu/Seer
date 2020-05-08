 package com.zen.droidparts.ui.routing;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 import com.zen.droidparts.ui.activity.BaseActivity;
 
 /**
  * Created by zen on 11/16/13.
  */
 public class BaseRouter {
     private Activity activity;
 
     protected void startActivityOfClassWithoutParams(Class<? extends BaseActivity> activityClass) {
         startActivityOfClass(activityClass, null, -1);
     }
 
     protected void startActivityOfClassAndClearTop(Class<? extends BaseActivity> activityClass) {
        startActivityOfClassWithFlags(activityClass, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
     }
 
     protected void startActivityOfClassWithFlags(Class<? extends BaseActivity> activityClass, int flags) {
         startActivityOfClass(activityClass, null, flags);
     }
 
     protected void startActivityOfClass(Class<? extends BaseActivity> activityClass, Bundle params, int flags) {
         Intent intent = new Intent(this.activity, activityClass);
 
         if (params != null) {
             intent.getExtras().putBundle(BaseActivity.PARAMS, params);
         }
 
         if (flags > 0) {
             intent.setFlags(flags);
         }
 
         this.activity.startActivity(intent);
     }
 
     public class RouteEnd {
         public void andFinish() {
             activity.finish();
         }
     }
 
     RouteEnd routeEnd = new RouteEnd();
 
     protected RouteEnd routeEnd() {
         return routeEnd;
     }
 
     public void setActivity(Activity activity) {
         this.activity = activity;
     }
 }
