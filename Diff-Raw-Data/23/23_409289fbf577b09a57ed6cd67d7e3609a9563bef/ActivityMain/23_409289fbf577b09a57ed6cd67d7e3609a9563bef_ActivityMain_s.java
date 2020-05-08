 package com.RoboMobo;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class ActivityMain extends Activity implements View.OnTouchListener
 {
     /**
      * Called when the activity is first created.
      */
     public boolean flag = true;
     public GPSModule mlocListener;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         RMGR.init(this);
         setContentView(R.layout.main);
         Log.wtf("1", "1");
         LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         mlocListener = new GPSModule();
         mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
         mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
         ((MainSurfaceView) findViewById(R.id.view)).setOnTouchListener(this);
         RMR.init(this,mlocListener);
     }
 
     public void fixCoord()
     {
         if (flag)
         {
             RMR.currentMap.fixCorner1(mlocListener.last_latt, mlocListener.last_long);
             flag = false;
             Log.wtf("fix","1");
         }
         else
         {
             RMR.currentMap.fixCorner2(mlocListener.last_latt, mlocListener.last_long);
             flag = true;
             Log.wtf("fix","2");
         }
     }
 
    public void moveUp()
     {
        RMR.currentMap.player1.posY++;
     }
 
    public void moveDown()
     {
        RMR.currentMap.player1.posY--;
     }
 
    public void moveRight()
     {
        RMR.currentMap.player1.posX++;
     }
 
    public void moveLeft()
     {
        RMR.currentMap.player1.posX--;
     }
 
     @Override
     public boolean onTouch(View view, MotionEvent motionEvent)
     {
         if (view instanceof MainSurfaceView)
         {
             switch (motionEvent.getAction() & MotionEvent.ACTION_MASK)
             {
                 case MotionEvent.ACTION_POINTER_DOWN:
                     RMR.prevZoomDistance = (float) (Math.sqrt(Math.pow(Math.abs(motionEvent.getX(0) - motionEvent.getX(1)), 2f) + Math.pow(Math.abs(motionEvent.getY(0) - motionEvent.getY(1)), 2f)));
 
                     if (RMR.zoomDistance > 10f)
                     {
                         RMR.prevTransform.set(RMR.transform);
                         RMR.midPoint.set(Math.abs(motionEvent.getX(0) - motionEvent.getX(1)) / 2 + (motionEvent.getX(0) > motionEvent.getX(1) ? motionEvent.getX(1) : motionEvent.getX(0)), Math.abs(motionEvent.getY(0) - motionEvent.getY(1)) / 2 + (motionEvent.getY(0) > motionEvent.getY(1) ? motionEvent.getY(1) : motionEvent.getY(0)));
                         RMR.transformMode = RMR.ZOOM;
                     }
                     break;
 
                 case MotionEvent.ACTION_MOVE:
                     if (RMR.transformMode == RMR.ZOOM)
                     {
                         float newDist = (float) (Math.sqrt(Math.pow(Math.abs(motionEvent.getX(0) - motionEvent.getX(1)), 2f) + Math.pow(Math.abs(motionEvent.getY(0) - motionEvent.getY(1)), 2f)));
                         if (newDist > 10f)
                         {
                             RMR.transform.set(RMR.prevTransform);
                             float scale = newDist / RMR.prevZoomDistance;
                             RMR.transform.postScale(scale, scale, RMR.midPoint.x, RMR.midPoint.y);
                         }
                     }
                     break;
 
                 case MotionEvent.ACTION_UP:
                 case MotionEvent.ACTION_POINTER_UP:
                     RMR.transformMode = RMR.NONE;
                     break;
             }
         }
 
         return true;
     }
 }
