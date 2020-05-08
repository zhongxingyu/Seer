 package com.palmap.jinse.exhibition.activity;
 
 import com.macrowen.macromap.draw.Map;
 import com.macrowen.macromap.utils.MapService;
 import com.macrowen.macromap.utils.MapService.MapLoadStatus;
 import com.macrowen.macromap.utils.MapService.MapLoadStatusListener;
 import com.palmap.main.activity.HomeActivity;
 
 import android.content.Intent;
 
 import android.os.Handler;
 
 import roboguice.inject.ContentView;
 
 import android.os.Bundle;
 
 import roboguice.activity.RoboActivity;
 
 @ContentView(R.layout.activity_launch)
 public class LaunchActivity extends RoboActivity implements MapLoadStatusListener {
 
   private Handler mHandler = new Handler() {
     @Override
     public void handleMessage(android.os.Message msg) {
       goHome();
     };
   };
 
   private MapService mMapService = MapService.getInstance();
 
   @Override
   public void onMapLoadStatusEvent(MapLoadStatus mapLoadStatus, Map map) {
     loadDataTime();
   }
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     mMapService.setOnMapLoadStatusListener(this);
    mMapService.initMapData("5", "文渊楼");
   }
 
   private void goHome() {
     Intent intent = new Intent();
     intent.setClass(this, HomeActivity.class);
     startActivity(intent);
     this.finish();
   }
 
   private void loadDataTime() {
     new Thread() {
       @Override
       public void run() {
         mHandler.sendEmptyMessageDelayed(1, 1000);
       };
     }.start();
   }
 }
