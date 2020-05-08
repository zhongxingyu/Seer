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
 
 import android.annotation.SuppressLint;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.PixelFormat;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.Surface;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 // Glitches:
 // - orientation change: usual system rotate animation
 public class PanelService extends Service {
     private static final int DELAY_HANDLER = 50; // determined via trial&error
 
     private Handler handler;
     private BroadcastReceiver broadcastReceiver;
     private SystemBarVisibilityCheckerThread systemBarVisibilityCheckerThread;
     private OnSystemBarVisibilityChangeListener onSystemBarVisibilityChangeListener;
     private PanelViewOnTouchListener panelViewOnTouchListener; // for drag&drop
 
     private RelativeLayout panelView;
     // View covering whole screen (except status/navigation bar) to show dummy during drag&drop
     private View panelDummyView;
     // View covering whole screen (except navigation bar) to detect status bar presence
     private View fullscreenDummyView;
 
     private ImageView dummyImageView;
 
     private WindowManager windowManager;
     private Display display;
     private WindowManager.LayoutParams paramsPanel;
     private WindowManager.LayoutParams paramsDummy;
     private WindowManager.LayoutParams paramsFullscreenDummy;
     private WindowManager.LayoutParams compatLP;
 
     private int globalPanelX, globalPanelY;
     private int panelViewHeight, panelViewWidth;
 
     private boolean statusBarVisible;
     private boolean navigationBarVisible;
     private int statusBarHeight;
     private int currentRotation;
 
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
 
         windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
         display = windowManager.getDefaultDisplay();
 
         statusBarVisible = true; // app starts in non-fullscreen
         statusBarHeight = getStatusBarHeight(); // is constant
 
         navigationBarVisible = true;
 
         currentRotation = display.getRotation();
 
         handler = new Handler();
 
         panelViewOnTouchListener = new PanelViewOnTouchListener();
         systemBarVisibilityCheckerThread = new SystemBarVisibilityCheckerThread();
 
         final IntentFilter filter = new IntentFilter();
         filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
 
         paramsPanel = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT,
                 LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                         | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
         paramsPanel.gravity = Gravity.TOP | Gravity.LEFT;
 
         paramsDummy = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT,
                 LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
 
         paramsFullscreenDummy = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT,
                 LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                         | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
 
         // INIT VIEWS
 
         panelDummyView = LayoutInflater.from(this).inflate(R.layout.dummy, null);
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
 
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             dummyImageView = (ImageView) fullscreenDummyView.findViewById(R.id.imageView_dummy);
         } else {
             dummyImageView = new ImageView(this);
             compatLP = new WindowManager.LayoutParams(
                     android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                     android.view.ViewGroup.LayoutParams.WRAP_CONTENT, paramsPanel.x, paramsPanel.y,
                     WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                     WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                             | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                     PixelFormat.TRANSLUCENT);
             compatLP.gravity = Gravity.TOP | Gravity.LEFT;
             windowManager.addView(dummyImageView, compatLP);
         }
 
         // remaining INIT
 
         // IMPORTANT: for all updates: wait DELAY_HANDLER ms to ensure dummyPanelView is refreshed,
         // else updatePanelParams() and clipPanelParams() (both called by updatePanelView()) might
         // get a wrong width/height of dummyPanelView => relative/global coords broken
 
         this.setOnSystemBarVisibilityChangeListener(new OnSystemBarVisibilityChangeListener() {
             @Override
             public void onChange() {
                 handler.postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         clipPanelParams();
                         updateGlobalCoords();
                         windowManager.updateViewLayout(panelView, paramsPanel);
                     }
                 }, DELAY_HANDLER);
             }
         });
 
         broadcastReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                     int newRotation = display.getRotation();
 
                     // @formatter:off
                     // usual rotation values: ("o" denotes the front camera on top)
                     // ROTATION_0 = 0  ROTATION_90 = 1  ROTATION_180 = 2  ROTATION_270 = 3
                     // .-----.                          .-----.                         
                     // | _o_ |         .------------.   | ___ |           .------------.
                     // ||   ||         | |¯¯¯¯¯¯¯¯| |   ||   ||           | |¯¯¯¯¯¯¯¯| |
                     // ||   ||         |o|        | |   ||   ||           | |        |o|
                     // ||___||         | |________| |   ||___||           | |________| |
                     // |_____|         `------------´   |__o__|           `------------´
                     // @formatter:on
                     if (newRotation != currentRotation) {
                         // detect if orientation (landscape/portrait) also changed (it's possible to
                         // directly switch from ROTATION_90 to ROTATION_270)
                         if ((currentRotation - newRotation) % 2 != 0) {
                             // store panel dimensions before it is destroyed
                             // swap width/height s.t. they correspond to the new (rotated) layout
                             panelViewWidth = panelView.getHeight();
                             panelViewHeight = panelView.getWidth();
                         }
 
                         currentRotation = newRotation;
 
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
                 systemBarVisibilityCheckerThread.start();
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
 
         systemBarVisibilityCheckerThread.stopThread();
 
         windowManager.removeView(panelView);
         windowManager.removeView(panelDummyView);
         windowManager.removeView(fullscreenDummyView);
 
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            windowManager.removeView(dummyImageView);

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
      * orientation & navigation bar visibility (relative to current top left corner of screen)
      */
     private void updatePanelParams() {
         if (currentRotation == Surface.ROTATION_0) {
             paramsPanel.x = fullscreenDummyView.getWidth() - globalPanelY - panelViewWidth;
             paramsPanel.y = globalPanelX;
         } else if (currentRotation == Surface.ROTATION_180) {
             paramsPanel.x = globalPanelY;
             if (navigationBarVisible)
                 paramsPanel.y = fullscreenDummyView.getHeight() + getNavigationBarHeight()
                         - globalPanelX - panelViewHeight;
             else
                 paramsPanel.y = fullscreenDummyView.getHeight() - globalPanelX - panelViewHeight;
         } else if (currentRotation == Surface.ROTATION_90) {
             paramsPanel.x = globalPanelX;
             paramsPanel.y = globalPanelY;
         } else {
             paramsPanel.x = fullscreenDummyView.getWidth() - globalPanelX - panelViewWidth;
             if (navigationBarVisible)
                 paramsPanel.y = fullscreenDummyView.getHeight() + getNavigationBarHeight()
                         - globalPanelY - panelViewHeight;
             else
                 paramsPanel.y = fullscreenDummyView.getHeight() - globalPanelY - panelViewHeight;
         }
     }
 
     /**
      * Clip the relative panel coordinates regarding the visibility of status/navigation bar
      */
     private void clipPanelParams() {
         // X coordinate
         if (paramsPanel.x < 0)
             paramsPanel.x = 0;
         if (paramsPanel.x > panelDummyView.getWidth() - panelViewWidth)
             paramsPanel.x = panelDummyView.getWidth() - panelViewWidth;
 
         // Y coordinate
         if (statusBarVisible) {
             if (paramsPanel.y < statusBarHeight)
                 paramsPanel.y = statusBarHeight;
         } else {
             if (paramsPanel.y < 0)
                 paramsPanel.y = 0;
         }
         // fullscreenDummyView always ignores status bar and always goes either down to the top of
         // the navigation bar or down to the bottom of the screen (if the navigation bar is
         // invisible)
         if (paramsPanel.y > fullscreenDummyView.getHeight() - panelViewHeight)
             paramsPanel.y = fullscreenDummyView.getHeight() - panelViewHeight;
     }
 
     /**
      * computes the global coordinates of the panel using its relative coordinates, screen
      * orientation & status bar visibility Origin: top left corner of landscape mode
      */
     private void updateGlobalCoords() {
         if (currentRotation == Surface.ROTATION_0) {
             globalPanelX = paramsPanel.y;
             globalPanelY = fullscreenDummyView.getWidth() - paramsPanel.x - panelViewWidth;
         } else if (currentRotation == Surface.ROTATION_180) {
             globalPanelX = fullscreenDummyView.getHeight() - paramsPanel.y - panelViewHeight;
             if (navigationBarVisible)
                 globalPanelX += getNavigationBarHeight();
             globalPanelY = paramsPanel.x;
         } else if (currentRotation == Surface.ROTATION_90) {
             globalPanelX = paramsPanel.x;
             globalPanelY = paramsPanel.y;
         } else {
             globalPanelX = fullscreenDummyView.getWidth() - paramsPanel.x - panelViewWidth;
             globalPanelY = fullscreenDummyView.getHeight() - paramsPanel.y - panelViewHeight;
             if (navigationBarVisible)
                 globalPanelY += getNavigationBarHeight();
         }
     }
 
     private interface OnSystemBarVisibilityChangeListener {
         public void onChange();
     }
 
     public void setOnSystemBarVisibilityChangeListener(OnSystemBarVisibilityChangeListener l) {
         onSystemBarVisibilityChangeListener = l;
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
 
                 // make sure height is uptodate (in very rare cases it wasn't)
                 panelViewHeight = panelView.getHeight();
 
                 startX = event.getX();
                 startY = event.getY();
 
                 // get dummy image
                 Bitmap bm = panelView.getDrawingCache();
 
                 // show dummy image on dummy overlay at panel's position
                 dummyImageView.setImageBitmap(bm);
                 updateDummyImagePosition(paramsPanel.x, paramsPanel.y);
                 dummyImageView.setVisibility(View.VISIBLE);
 
                 // hide real panel
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                     panelView.setVisibility(View.INVISIBLE);
                 } else {
                     handler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             panelView.setVisibility(View.INVISIBLE);
                         }
                     }, DELAY_HANDLER);
                 }
                 break;
             case MotionEvent.ACTION_UP:
                 if (isDragging) {
                     // update panel position
                     endX = event.getX();
                     endY = event.getY();
 
                     float deltaX = endX - startX;
                     float deltaY = endY - startY;
 
                     paramsPanel.x += deltaX;
                     paramsPanel.y += deltaY;
 
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
                             dummyImageView.setVisibility(View.INVISIBLE);
                             // a short click on the drag button results in ACTION_DOWN followed
                             // directly by ACTION_UP (=drop). On older devices it might happen that
                             // the first setVisibility on panelView here might be before it was set
                             // invisible in ACTION_DOWN -> panel would be invisible -> as a safety
                             // measure, make visible again here
                             panelView.setVisibility(View.VISIBLE);
                         }
                     }, 2 * DELAY_HANDLER);
 
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
 
                     updateDummyImagePosition(paramsPanel.x + deltaX, paramsPanel.y + deltaY);
                 }
                 break;
             case MotionEvent.ACTION_CANCEL:
                 // cancel drag, leave panel at start position and show it again
                 isDragging = false;
                 panelView.setVisibility(View.VISIBLE);
 
                 // make dummy invisible, else canvas error because of reusing bitmap is possible
                 dummyImageView.setVisibility(View.INVISIBLE);
                 break;
             default:
                 break;
             }
 
             return true;
         }
 
         @SuppressLint("NewApi")
         protected void updateDummyImagePosition(float x, float y) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                 dummyImageView.setX(x);
                 dummyImageView.setY(y);
             } else {
                 compatLP.x = (int) x;
                 compatLP.y = (int) y;
                 windowManager.updateViewLayout(dummyImageView, compatLP);
             }
         }
     };
 
     /**
      * simple Thread which monitors the height's of the dummy and fullscreenDummy overlays. Heights
      * match if the status bar is not present, a difference of the status bar height corresponds to
      * a present status bar. Also tries to detect navigation bar visibility depending on height of
      * the fullscreenDummy overlay. Note: On >=HC devices a OnSystemUiVisibilityChangeListener could
      * be used to detect the visibility of the navigation bar (sometimes it doesn't work for the
      * status bar).
      */
     private class SystemBarVisibilityCheckerThread extends Thread {
         private volatile boolean running;
 
         @Override
         public void run() {
             running = true;
 
             while (running) {
                 int fullscreenDummyHeight = fullscreenDummyView.getHeight();
                 int panelDummyHeight = panelDummyView.getHeight();
 
                 boolean oldStatusBarVis = statusBarVisible;
                 boolean oldNavigationBarVis = navigationBarVisible;
 
                 // hack: we assume, that
                 // (1) The physical screen dimensions are multiples of 10
                 // (2) fullscreenDummyHeight%10 == 0 => fullscreenDummyHeight == screen height
                 // (3) fullscreenDummyHeight == screen height => navigation (and status) bar hidden
                 // (4) fullscreenDummyHeight == panelDummyHeight => status bar hidden
                 // (5) fullscreenDummyHeight-panelDummyHeight==statusBarHeight => status bar shown
                 // (The main problem is that there's no official way to get the physical resolution
                 // before API17 and not even an unofficial before API14)
                 if (fullscreenDummyHeight == panelDummyHeight) {
                     statusBarVisible = false;
                     if ((fullscreenDummyHeight % 10) == 0)
                         navigationBarVisible = false;
                     else
                         navigationBarVisible = true;
                 } else if (fullscreenDummyHeight - panelDummyHeight == statusBarHeight) {
                     statusBarVisible = true;
                     if ((fullscreenDummyHeight % 10) == 0)
                         navigationBarVisible = false; // (unlikely case)
                     else
                         navigationBarVisible = true;
                 }
 
                 if (oldStatusBarVis != statusBarVisible
                         || oldNavigationBarVis != navigationBarVisible)
                     onSystemBarVisibilityChangeListener.onChange();
 
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
