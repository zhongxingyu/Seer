 package com.music.fmv.utils;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import com.music.fmv.activities.BandInfoActivity;
 import com.music.fmv.activities.PlayerActivity;
 import com.music.fmv.models.BandInfoModel;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lebed
  * Date: 24.05.13
  * Time: 0:47
  * To change this template use File | Settings | File Templates.
  */
 
 public class ActivityMediator {
     private Context context;
     private boolean isActivity = false;
 
     public void startBandActivity(BandInfoModel model){
         Bundle bnd = new Bundle();
         bnd.putSerializable(BandInfoActivity.BAND_KEY, model);
         startActivity(BandInfoActivity.class, bnd);
     }
 
     public ActivityMediator(Context context) {
         this.context = context;
         if (context instanceof Activity)
             isActivity = true;
     }
 
     private void startActivity(Intent intent) {
         if (!isActivity)
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         context.startActivity(intent);
     }
 
     private void startActivity(Class clz) {
         startActivity(clz, null, null);
     }
 
     private void startActivity(Class clz, Bundle bundle) {
         startActivity(clz, bundle, null);
     }
 
     private void startActivity(Class clz, int[] flags) {
         startActivity(clz, null, flags);
     }
 
     private void startActivity(Class clz, Bundle bundle, int... flags) {
         Intent intent = new Intent(context, clz);
         if (bundle != null)
             intent.putExtras(bundle);
         if (!isActivity)
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         if (flags != null && flags.length > 0) {
             for (int flag : flags) {
                 intent.addFlags(flag);
             }
         }
         context.startActivity(intent);
     }
 
     private void startActivityForresult(Class clz, Bundle bundle,int requestCode) {
         Intent intent = new Intent(context, clz);
         if (bundle != null)
             intent.putExtras(bundle);
         if (isActivity) {
             ((Activity) context).startActivityForResult(intent, requestCode);
         }
     }
 
     public void startPlayerActivity() {
         startActivity(PlayerActivity.class, new int[]{Intent.FLAG_ACTIVITY_SINGLE_TOP});
     }
 }
