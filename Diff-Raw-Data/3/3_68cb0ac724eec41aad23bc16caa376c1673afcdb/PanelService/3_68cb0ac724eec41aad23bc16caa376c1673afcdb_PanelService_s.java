 /*
  * Copyright 2013 Erwin Goslawski
  *
  * This file is part of SystemWidePanel.
  *
  * SystemWidePanel is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SystemWidePanel is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SystemWidePanel.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.cygery.systemwidepanel;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.PixelFormat;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 // Glitches:
 // - orientation change: usual system rotate animation
 public class PanelService extends Service {
     private static final String BC_CONFIG_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";
     private static final int DELAY_HANDLER = 50; // determined via trial&error
 
     private Handler handler;
     private BroadcastReceiver broadcastReceiver;
     private StatusBarVisibilityCheckerThread statusBarVisibilityCheckerThread;
     private OnStatusBarVisibilityChangeListener onStatusBarVisibilityChangeListener;
     private PanelViewOnTouchListener panelViewOnTouchListener; // for drag&drop
 
     private RelativeLayout panelView;
     // View covering whole screen (except status/navigation bar) to show dummy during drag&drop
     private View panelDummyView;
     // View covering whole screen (except navigation bar) to detect status bar presence
     private View fullscreenDummyView;
 
     private WindowManager windowManager;
     private WindowManager.LayoutParams paramsPanel;
     private WindowManager.LayoutParams paramsDummy;
     private WindowManager.LayoutParams paramsFullscreenDummy;
 
     private int globalPanelX, globalPanelY;
     private int panelViewHeight, panelViewWidth;
 
     private boolean statusBarVisible;
     private int statusBarHeight;
     private int lastOrientation;
 
     // TODO EDIT HERE
     private void registerPanelListeners() {
         panelView.findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Toast.makeText(PanelService.this, "bla", Toast.LENGTH_SHORT).show();
             }
         });
         panelView.findViewById(R.id.button3).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 stopSelf();
             }
         });
         panelView.findViewById(R.id.button2).setOnTouchListener(panelViewOnTouchListener);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (intent != null) {
             // read panel start coordinates
             globalPanelX = intent.getIntExtra(getPackageName() + ".x", 0);
             globalPanelY = intent.getIntExtra(getPackageName() + ".y", 0);
         }
 
         return START_STICKY;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         // INIT FIELDS
 
         statusBarVisible = true; // app starts in non-fullscreen
         statusBarHeight = getStatusBarHeight(); // is constant
 
         lastOrientation = getResources().getConfiguration().orientation;
 
         handler = new Handler();
 
         panelViewOnTouchListener = new PanelViewOnTouchListener();
         statusBarVisibilityCheckerThread = new StatusBarVisibilityCheckerThread();
 
         final IntentFilter filter = new IntentFilter();
         filter.addAction(BC_CONFIG_CHANGED);
 
         windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
 
         paramsPanel = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                 WindowManager.LayoutParams.WRAP_CONTENT,
                 WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
         paramsPanel.gravity = Gravity.BOTTOM | Gravity.LEFT;
 
         paramsDummy = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                 WindowManager.LayoutParams.MATCH_PARENT,
                 WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
 
         paramsFullscreenDummy = new WindowManager.LayoutParams(
                 WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                 WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
 
         // INIT VIEWS
 
         panelDummyView = LayoutInflater.from(this).inflate(R.layout.panel_dummy, null);
         fullscreenDummyView = LayoutInflater.from(this).inflate(R.layout.fullscreen_dummy, null);
 
         // measure panel's dimensions to allow clipping & updating its coords before displaying it
         updatePanelView();
         panelView.setLayoutParams(paramsPanel);
         panelView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                 MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
         panelViewHeight = panelView.getMeasuredHeight();
         panelViewWidth = panelView.getMeasuredWidth();
 
         // ADD VIEWS
 
         windowManager.addView(fullscreenDummyView, paramsFullscreenDummy);
         windowManager.addView(panelDummyView, paramsDummy);
 
         // remaining INIT
 
         // IMPORTANT: for all updates: wait DELAY_HANDLER ms to ensure dummyPanelView is refreshed,
         // else updatePanelParams() and clipPanelParams() (both called by updatePanelView()) might
         // get a wrong width/height of dummyPanelView => relative/global coords broken
 
         this.setOnStatusBarVisibilityChangeListener(new OnStatusBarVisibilityChangeListener() {
             @Override
             public void onChange() {
                 // Only update panel position by clipping its coordinates if it'd else be underneath
                 // the now shown status bar. Else, all coordinates can be kept.
                 if (isPanelBelowStatusBar()) {
                     handler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             clipPanelParams();
                             updateGlobalCoords();
                             windowManager.updateViewLayout(panelView, paramsPanel);
                         }
                     }, DELAY_HANDLER);
                 }
             }
         });
 
         broadcastReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (intent.getAction().equals(BC_CONFIG_CHANGED)) {
                     Configuration config = getResources().getConfiguration();
 
                     if (config.orientation != lastOrientation) {
                         lastOrientation = config.orientation;
 
                         // store panel dimensions before it is destroyed
                         // swap width/height s.t. they correspond to the new (rotated) layout
                         panelViewWidth = panelView.getHeight();
                         panelViewHeight = panelView.getWidth();
 
                         handler.postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                 windowManager.removeView(panelView);
                                 updatePanelView();
                                 updateGlobalCoords();
                                 windowManager.addView(panelView, paramsPanel);
                             }
                         }, DELAY_HANDLER);
                     }
                 }
             }
         };
 
         // wait until dummy views are shown (else the Thread "detects" two changes at start)
         handler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 // now we can get width/height of dummyView -> correct clipping
                 updatePanelView();
                 updateGlobalCoords();
                 windowManager.addView(panelView, paramsPanel);
 
                 registerReceiver(broadcastReceiver, filter);
                 statusBarVisibilityCheckerThread.start();
             }
         }, DELAY_HANDLER);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         statusBarVisibilityCheckerThread.stopThread();
 
         windowManager.removeView(panelView);
         windowManager.removeView(panelDummyView);
         windowManager.removeView(fullscreenDummyView);
 
         unregisterReceiver(broadcastReceiver);
     }
 
     private int getStatusBarHeight() {
         int resID = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
 
         return (resID > 0 ? Resources.getSystem().getDimensionPixelSize(resID) : 0); // 0 on error
     }
 
     // IMPORTANT: not constant! depends on orientation (mind the "landscape" in the identifier)
     private int getNavigationBarHeight() {
         int resID;
 
         if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
             resID = Resources.getSystem().getIdentifier("navigation_bar_height_landscape", "dimen",
                     "android");
         else
             resID = Resources.getSystem()
                     .getIdentifier("navigation_bar_height", "dimen", "android");
 
         return (resID > 0 ? Resources.getSystem().getDimensionPixelSize(resID) : 0); // 0 on error
     }
 
     private void updatePanelView() {
         // inflate the panel layout for the _current_ orientation
         panelView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.panel, null);
 
         registerPanelListeners();
 
         panelView.setDrawingCacheEnabled(true); // for dummy bitmap during drag&drop
 
         updatePanelParams();
         clipPanelParams();
     }
 
     /**
      * computes the relative panel coordinates (panelParams.x/y) based on global coordinates,
      * orientation & status bar visibility (relative to bottom left corner)
      */
     private void updatePanelParams() {
         if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
             paramsPanel.x = globalPanelX;
             paramsPanel.y = globalPanelY - getNavigationBarHeight();
         } else {
             paramsPanel.x = globalPanelY;
             paramsPanel.y = fullscreenDummyView.getHeight() - globalPanelX - panelViewHeight;
         }
     }
 
     private void clipPanelParams() {
         if (paramsPanel.x < 0)
             paramsPanel.x = 0;
         if (paramsPanel.y < 0)
             paramsPanel.y = 0;
 
         if (paramsPanel.x > panelDummyView.getWidth() - panelViewWidth)
             paramsPanel.x = panelDummyView.getWidth() - panelViewWidth;
         if (paramsPanel.y > panelDummyView.getHeight() - panelViewHeight)
             paramsPanel.y = panelDummyView.getHeight() - panelViewHeight;
     }
 
     /**
      * computes the global coordinates of the panel using its relative coordinates, screen
      * orientation & status bar visibility Origin: Bottom left corner of landscape mode
      */
     private void updateGlobalCoords() {
         if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
             globalPanelX = paramsPanel.x;
             globalPanelY = paramsPanel.y + getNavigationBarHeight();
         } else {
             globalPanelX = fullscreenDummyView.getHeight() - paramsPanel.y - panelViewHeight;
             globalPanelY = paramsPanel.x;
         }
     }
 
     private boolean isPanelBelowStatusBar() {
         if (statusBarVisible) {
             return (paramsPanel.y + panelViewHeight > panelDummyView.getHeight());
         } else {
             return false;
         }
     }
 
     private interface OnStatusBarVisibilityChangeListener {
         public void onChange();
     }
 
     public void setOnStatusBarVisibilityChangeListener(OnStatusBarVisibilityChangeListener l) {
         onStatusBarVisibilityChangeListener = l;
     }
 
     /**
      * Listener which implements drag&drop functionality for the panel
      */
     private class PanelViewOnTouchListener implements OnTouchListener {
         private float startX, startY, endX, endY;
         private boolean isDragging = false;
 
         @Override
         public boolean onTouch(View v, MotionEvent event) {
             switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 isDragging = true;
 
                 startX = event.getX();
                 startY = event.getY();
 
                 // get dummy image
                 Bitmap bm = panelView.getDrawingCache();
 
                 // show dummy image on dummy overlay at panel's position
                 ((ImageView) panelDummyView.findViewById(R.id.imageView_dummy)).setImageBitmap(bm);
                 panelDummyView.findViewById(R.id.imageView_dummy).setX(paramsPanel.x);
                 panelDummyView.findViewById(R.id.imageView_dummy).setY(
                         panelDummyView.getHeight() - paramsPanel.y - panelViewHeight);
                 panelDummyView.findViewById(R.id.imageView_dummy).setVisibility(View.VISIBLE);
 
                 // hide real panel
                 panelView.setVisibility(View.INVISIBLE);
                 break;
             case MotionEvent.ACTION_UP:
                 if (isDragging) {
                     // update panel position
                     endX = event.getX();
                     endY = event.getY();
 
                     float deltaX = endX - startX;
                     float deltaY = endY - startY;
 
                     paramsPanel.x += deltaX;
                     paramsPanel.y -= deltaY;
 
                     // the dummy can be dragged such that it partly leaves the screen but the panel
                     // should remain complete within the screen
                     clipPanelParams();
 
                     // show panel
                     windowManager.updateViewLayout(panelView, paramsPanel);
                     panelView.setVisibility(View.VISIBLE);
 
                     // hide dummy. wait to ensure that real panel is visible beforehand. else
                     // flickering is possible when dummy is hidden before panel is visible again
                     handler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             panelDummyView.findViewById(R.id.imageView_dummy).setVisibility(
                                     View.INVISIBLE);
                         }
                     }, DELAY_HANDLER);
 
                     updateGlobalCoords();
                 }
 
                 isDragging = false;
                 break;
             case MotionEvent.ACTION_MOVE:
                 if (isDragging) {
                     // update dummy position
                     endX = event.getX();
                     endY = event.getY();
 
                     float deltaX = endX - startX;
                     float deltaY = endY - startY;
 
                     panelDummyView.findViewById(R.id.imageView_dummy).setX(paramsPanel.x + deltaX);
                     panelDummyView.findViewById(R.id.imageView_dummy).setY(
                             panelDummyView.getHeight() - paramsPanel.y - panelViewHeight + deltaY);
                 }
                 break;
             case MotionEvent.ACTION_CANCEL:
                 // cancel drag, leave panel at start position and show it again
                 isDragging = false;
                 panelView.setVisibility(View.VISIBLE);
 
                 // make dummy invisible, else canvas error because of reusing bitmap is possible
                 panelDummyView.findViewById(R.id.imageView_dummy).setVisibility(View.INVISIBLE);
                 break;
             default:
                 break;
             }
 
             return true;
         }
     };
 
     /**
      * simple Thread which monitors the height's of the dummy and fullscreenDummy overlays. Heights
      * match iff the status bar is not present, a difference of the status bar height corresponds to
      * a present status bar
      */
     private class StatusBarVisibilityCheckerThread extends Thread {
         private volatile boolean running;
 
         public void run() {
             running = true;
 
             while (running) {
                 int fullscreenDummyHeight = fullscreenDummyView.getHeight();
                 int panelDummyHeight = panelDummyView.getHeight();
 
                 boolean oldVis = statusBarVisible;
 
                 if (fullscreenDummyHeight - panelDummyHeight == statusBarHeight)
                     statusBarVisible = true;
                 else if (fullscreenDummyHeight == panelDummyHeight)
                     statusBarVisible = false;
                 // else, probably just one view changed orientation
 
                 if (oldVis != statusBarVisible)
                     onStatusBarVisibilityChangeListener.onChange();
 
                 try {
                     sleep(DELAY_HANDLER);
                 } catch (InterruptedException e) {
                     // usually because stopThread() was called
                 }
             }
         }
 
         public void stopThread() {
             running = false;
         }
     }
 }
