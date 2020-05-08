 package com.RoboMobo;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class ActivityMain extends Activity// implements View.OnTouchListener
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
         //((MainSurfaceView) findViewById(R.id.view)).setOnTouchListener(this);
 
         RMR.sw = (MainSurfaceView) findViewById(R.id.view);
         RMR.init(this, mlocListener);
     }
 
    public void fixCoord(View view)
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
 
     public void moveUp(View view)
     {
         RMR.currentMap.player1.posX-=32;
     }
 
     public void moveDown(View view)
     {
         RMR.currentMap.player1.posX+=32;
     }
 
     public void moveRight(View view)
     {
         RMR.currentMap.player1.posY+=32;
     }
 
     public void moveLeft(View view)
     {
         RMR.currentMap.player1.posY-=32;
     }
 
     public void setPlayer(View view)
     {
         RMR.currentMap.player1.changePos(new int[] {16, 16});
     }
 
     /*
     @Override
     public boolean onTouch(View view, MotionEvent motionEvent)
     {
         if (view instanceof MainSurfaceView)
         {
             float[] values = new float[9];
             RMR.transform.getValues(values);
             float[] prevValues = new float[9];
             RMR.prevTransform.getValues(prevValues);
 
             switch (motionEvent.getAction() & MotionEvent.ACTION_MASK)
             {
                 case MotionEvent.ACTION_POINTER_DOWN:
                     RMR.prevZoomDistance = (float) (Math.sqrt(Math.pow(Math.abs(motionEvent.getX(0) - motionEvent.getX(1)), 2f) + Math.pow(Math.abs(motionEvent.getY(0) - motionEvent.getY(1)), 2f)));
                     RMR.zoomDistance = RMR.prevZoomDistance;
 
                     RMR.transformMode = RMR.NONE;
                     if (RMR.prevZoomDistance > 10f)
                     {
                         RMR.prevTransform.set(RMR.transform);
                         RMR.midPoint.set(Math.abs(motionEvent.getX(0) - motionEvent.getX(1)) / 2 + (motionEvent.getX(0) > motionEvent.getX(1) ? motionEvent.getX(1) : motionEvent.getX(0)), Math.abs(motionEvent.getY(0) - motionEvent.getY(1)) / 2 + (motionEvent.getY(0) > motionEvent.getY(1) ? motionEvent.getY(1) : motionEvent.getY(0)));
                         RMR.transformMode = RMR.ZOOM;
                     }
                     break;
 
                 case MotionEvent.ACTION_MOVE:
                     if (RMR.transformMode == RMR.ZOOM)
                     {
                         RMR.zoomDistance = (float) (Math.sqrt(Math.pow(Math.abs(motionEvent.getX(0) - motionEvent.getX(1)), 2f) + Math.pow(Math.abs(motionEvent.getY(0) - motionEvent.getY(1)), 2f)));
 
                         if (RMR.zoomDistance > 10f && (values[0] >= 0.2F || (values[0] < 0.2F && (RMR.zoomDistance / RMR.prevZoomDistance) >= 1)))
                         {
                             if(values[0] >= 0.2F && (RMR.zoomDistance / RMR.prevZoomDistance) >= 1) RMR.midPoint.set(Math.abs(motionEvent.getX(0) - motionEvent.getX(1)) / 2 + (motionEvent.getX(0) > motionEvent.getX(1) ? motionEvent.getX(1) : motionEvent.getX(0)), Math.abs(motionEvent.getY(0) - motionEvent.getY(1)) / 2 + (motionEvent.getY(0) > motionEvent.getY(1) ? motionEvent.getY(1) : motionEvent.getY(0)));
 
 
                             RMR.transform.set(RMR.prevTransform);
                             float scale = RMR.zoomDistance / RMR.prevZoomDistance;
                             RMR.transform.postScale(scale, scale, RMR.midPoint.x, RMR.midPoint.y);
                         }
 
                     }
 
                     if (RMR.transformMode == RMR.DRAG)
                     {
                         RMR.transform.set(RMR.prevTransform);
                         RMR.transform.postTranslate(motionEvent.getX() - RMR.midPoint.x, motionEvent.getY() - RMR.midPoint.y);
                     }
                     break;
 
                 case MotionEvent.ACTION_DOWN:
                     RMR.prevTransform.set(RMR.transform);
                     RMR.midPoint.set(motionEvent.getX(), motionEvent.getY());
                     RMR.transformMode = RMR.DRAG;
                     break;
 
                 case MotionEvent.ACTION_UP:
                 case MotionEvent.ACTION_POINTER_UP:
                     RMR.transformMode = RMR.NONE;
                     RMR.zoomDistance = 1;
                     RMR.prevZoomDistance = 1;
                     break;
             }
         }
 
         return true;
     }*/
 }
