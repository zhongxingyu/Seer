 package com.bk.sunwidgt;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import com.bk.sunwidgt.activity.SunActivity;
 import com.bk.sunwidgt.lib.MoonCalculator.MoonriseMoonset;
 import com.bk.sunwidgt.lib.MoonCalculator;
 import com.bk.sunwidgt.lib.SunCalculator;
 import com.bk.sunwidgt.lib.SunCalculator.SunriseSunset;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.TextView;
 
 
 public class SunWidget extends AppWidgetProvider {
     private final static String TAG = SunWidget.class.getSimpleName();
     public final static SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm");
     private final static SimpleDateFormat fmtDate = new SimpleDateFormat("MM/dd");
     public final static String notimeString = "--:--";
     
     @Override
     public void onUpdate(Context context,AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         Log.d(TAG, "+onUpdate");
         RemoteViews updateViews = new RemoteViews( context.getPackageName(), com.bk.sunwidgt.R.layout.main);
         Calendar cal = Calendar.getInstance();
         
         LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
         Location coarseLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         
         double lat = null == coarseLocation ? 25.045792 : coarseLocation.getLatitude();
         double lng = null == coarseLocation ? 121.453857 : coarseLocation.getLongitude();
         Log.d(TAG, "lat=" + lat + " lng=" + lng);
         
         SunriseSunset sunAnswer = null;
         MoonriseMoonset moonAnswer = null;
         
         sunAnswer = SunCalculator.getSunriseSunset(cal,lat,lng,false);
         moonAnswer = MoonCalculator.getMoonriseMoonset(cal, lat, lng);
         fillSunTable(updateViews,sunAnswer,com.bk.sunwidgt.R.id.day1_title,com.bk.sunwidgt.R.id.day1_sunrise,com.bk.sunwidgt.R.id.day1_sunset);
         fillMoonTable(updateViews,moonAnswer,com.bk.sunwidgt.R.id.day1_title,com.bk.sunwidgt.R.id.day1_moonrise,com.bk.sunwidgt.R.id.day1_moonset);
         Log.d(TAG, sunAnswer.toString());
         Log.d(TAG, moonAnswer.toString());
         
         cal.add(Calendar.DAY_OF_MONTH, 1);
         sunAnswer = SunCalculator.getSunriseSunset(cal,lat,lng,false);
         moonAnswer = MoonCalculator.getMoonriseMoonset(cal, lat, lng);
         fillSunTable(updateViews,sunAnswer,com.bk.sunwidgt.R.id.day2_title,com.bk.sunwidgt.R.id.day2_sunrise,com.bk.sunwidgt.R.id.day2_sunset);
         fillMoonTable(updateViews,moonAnswer,com.bk.sunwidgt.R.id.day2_title,com.bk.sunwidgt.R.id.day2_moonrise,com.bk.sunwidgt.R.id.day2_moonset);
         Log.d(TAG, sunAnswer.toString());
         Log.d(TAG, moonAnswer.toString());
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
         sunAnswer = SunCalculator.getSunriseSunset(cal,lat,lng,false);
         moonAnswer = MoonCalculator.getMoonriseMoonset(cal, lat, lng);
         fillSunTable(updateViews,sunAnswer,com.bk.sunwidgt.R.id.day3_title,com.bk.sunwidgt.R.id.day3_sunrise,com.bk.sunwidgt.R.id.day3_sunset);
         fillMoonTable(updateViews,moonAnswer,com.bk.sunwidgt.R.id.day3_title,com.bk.sunwidgt.R.id.day3_moonrise,com.bk.sunwidgt.R.id.day3_moonset);
         Log.d(TAG, sunAnswer.toString());
         Log.d(TAG, moonAnswer.toString());
 
         
         final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context,SunActivity.class), 0);
         updateViews.setOnClickPendingIntent(com.bk.sunwidgt.R.id.mainlayout, pendingIntent);
 
         appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
         Log.d(TAG, "-onUpdate");
     }
     
     private void fillSunTable(RemoteViews view,SunriseSunset answer,int res_title,int res_sunrise,int res_sunset) {
         view.setTextViewText(res_title,  fmtDate.format(answer.sunrise));
         view.setTextViewText(res_sunrise,  fmtTime.format(answer.sunrise )  + " " + (int) answer.sunrise_azel);
         view.setTextViewText(res_sunset,  fmtTime.format(answer.sunset ) + " " + (int) answer.sunset_azel);
     }
     
     private void fillMoonTable(RemoteViews view,MoonriseMoonset answer,int res_title,int res_sunrise,int res_sunset) {
        view.setTextViewText(res_title,  fmtDate.format(answer.moonrise));
         if(answer.moonrise != null) {
             view.setTextViewText(res_sunrise,  fmtTime.format(answer.moonrise )  + " " + (int) answer.rise_az);
         }
         else {
             view.setTextViewText(res_sunrise,  notimeString);
         }
         
         if(answer.moonset != null) {
             view.setTextViewText(res_sunset,  fmtTime.format(answer.moonset ) + " " + (int) answer.set_sz);
         }
         else {
             view.setTextViewText(res_sunset,  notimeString);
         }
     }    
     
 }
