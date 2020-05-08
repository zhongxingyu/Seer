 package com.zhideel.tapathon;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 import android.view.View;
 import android.view.animation.TranslateAnimation;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * Created by Adeel on 24/1/14.
  */
 public final class Config {
     public static Context context = null;
 
 
     public static boolean isAPConnected()
     {
         //Check if it is an Access Point
         Boolean apState = false;
         try {
             WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
             Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
             method.setAccessible(true);
 
             apState = (Boolean) method.invoke(wifiManager, (Object[]) null);
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         }
         catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
         return apState;
     }
 
     public static boolean isWifiConnected() {
         //Check for Wifi Connection
         final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         final NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
         //Check if it is an Access Point
         Boolean apState = false;
         try {
             WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
             Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
             method.setAccessible(true);
 
             apState = (Boolean) method.invoke(wifiManager, (Object[]) null);
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         }
         catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
 
 
         return (networkInfo != null && networkInfo.isConnected()) || apState;
     }
 
     // To animate view slide out from left to right
     public static void slideToRight(View view){
         TranslateAnimation animate = new TranslateAnimation(0,view.getWidth(),0,0);
         animate.setDuration(500);
         animate.setFillAfter(true);
         view.startAnimation(animate);
         view.setVisibility(View.GONE);
     }
     // To animate view slide out from right to left
     public static void slideToLeft(View view){
         TranslateAnimation animate = new TranslateAnimation(0,-view.getWidth(),0,0);
         animate.setDuration(500);
         animate.setFillAfter(true);
         view.startAnimation(animate);
         view.setVisibility(View.GONE);
     }
 
     // To animate view slide out from top to bottom
     public static void slideToBottom(View view){
         TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
         animate.setDuration(500);
         animate.setFillAfter(true);
         view.startAnimation(animate);
         view.setVisibility(View.GONE);
     }
 
     // To animate view slide out from bottom to top
     public static void slideToTop(View view){
         TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
         animate.setDuration(500);
         animate.setFillAfter(true);
         view.startAnimation(animate);
         view.setVisibility(View.GONE);
     }
 
     public static int currentTimeMillis() {
         return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
     }
 }
