 package com.stiandrobak.FlashLight;
 
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.appwidget.AppWidgetProvider;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.app.PendingIntent;
 import android.content.Intent;
 
 /**
  * Created with IntelliJ IDEA.
  * Author: Stian Drøbak
  * Date: 01.03.13
  * Time: 12:16
  */
 
 
 public class FlashWidget extends AppWidgetProvider{
 
     private static Camera camera;
 
    static {
        camera = Camera.open();
    }

     public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds){
         RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget);
         Intent active = new Intent(context,FlashWidget.class);
         active.setAction(ACTION_WIDGET_RECEIVER);
         active.putExtra("msg","Message from button.");
         PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context,0,active,0);
         remoteViews.setOnClickPendingIntent(R.id.flashbutton,actionPendingIntent);
         appWidgetManager.updateAppWidget(appWidgetIds,remoteViews);
 
     }
 
 
     @Override
     public void onReceive(Context context,Intent intent){
         if(intent.getAction().equals(ACTION_WIDGET_RECEIVER)){
             if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                 FlashWidget.toggleValue();
             }
         }else{
             Log.d(null,"By Demons I am awesome!");
         }
         super.onReceive(context,intent);
     }
 
 
     public static void toggleValue(){
         Camera.Parameters p = camera.getParameters();
         if(!p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)){
             p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
             camera.setParameters(p);
             camera.startPreview();
         }else{
             p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
             camera.setParameters(p);
             camera.stopPreview();
         }
     }
 }
