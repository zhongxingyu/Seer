 package com.exiashio.batterystatussquare;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.widget.RemoteViews;
 
 public class UpdateService extends Service {
     private static final boolean DEBUG = false;
     private static final String TAG = "UpdateService";
 
     private static final int SQUARE_MIN = 0;
     private static final int SQUARE_MAX = 72;
     private static final int TEXT_SIZE = 22;
 
     private int mLevel;
     PendingIntent mPendingIntent;
 
     private static ComponentName mComponentName;
     private static AppWidgetManager mAppWidgetManager;
 
     BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (DEBUG) Log.v(TAG, "onReceive");
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_BATTERY_CHANGED) == false) {
                 // nothing to do.
                 return;
             }
 
             mLevel = intent.getIntExtra("level", 0);
             updateWidget();
         }
     };
 
     @Override
     public IBinder onBind(Intent arg0) {
         return null;
     }
 
     @Override
     public void onCreate() {
         if (DEBUG) Log.v(TAG, "onCreate");
 
         BatteryStatusSquarePreference.mRunning = true;
 
         mComponentName = new ComponentName(this, WidgetProvider.class);
         mAppWidgetManager = AppWidgetManager.getInstance(this);
 
         // receive ACTION_BATTERY_CHANGED.
         IntentFilter filter = new IntentFilter();
         filter.addAction(Intent.ACTION_BATTERY_CHANGED);
         registerReceiver(mBroadcastReceiver, filter);
 
         // set preference activity intent.
         Intent clickIntent = new Intent(this, BatteryStatusSquarePreference.class);
         mPendingIntent = PendingIntent.getActivity(this, 0, clickIntent, 0);
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
         if (DEBUG) Log.v(TAG, "onStart");
 
         updateWidget();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (DEBUG) Log.v(TAG, "onStartCommand");
 
         updateWidget();
 
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     @Override
     public void onDestroy() {
         if (DEBUG) Log.v(TAG, "onDestroy");
 
         unregisterReceiver(mBroadcastReceiver);
 
         BatteryStatusSquarePreference.mRunning = false;
     }
 
     private void updateWidget() {
         Bitmap bitmap = Bitmap.createBitmap(SQUARE_MAX, SQUARE_MAX, Bitmap.Config.ARGB_8888);
 
         drawBitmap(bitmap);
 
         // RemoteViews has array list of Bitmap. If use static object,
         // IPC data size is increase, and causes binder error.
         // Because create new object everytime.
         RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget);
         remoteViews.setImageViewBitmap(R.id.image, bitmap);
 
         remoteViews.setOnClickPendingIntent(R.id.widget_base, mPendingIntent);
 
         if (BatteryStatusSquarePreference.isTextVisible(this)) {
             remoteViews.setTextViewText(R.id.text, Integer.toString(mLevel) + "%");
             remoteViews.setFloat(R.id.text, "setTextSize", TEXT_SIZE);
             remoteViews.setTextColor(R.id.text,
                     BatteryStatusSquarePreference.getTextColor(this));
             remoteViews.setViewVisibility(R.id.text, View.VISIBLE);
         } else {
             remoteViews.setViewVisibility(R.id.text, View.GONE);
         }
 
         mAppWidgetManager.updateAppWidget(mComponentName, remoteViews);
     }
 
     private void drawBitmap(Bitmap bitmap) {
         Canvas c = new Canvas(bitmap);
         Paint p = new Paint();
         p.setAntiAlias(true);
         p.setColor(BatteryStatusSquarePreference.getSquareColor(this));
 
         int level = (int)(mLevel*SQUARE_MAX/100 + 0.5f);
         if (DEBUG) Log.v(TAG, "level : " + mLevel + " square : " + (SQUARE_MAX - level));
 
         c.drawRect(SQUARE_MIN, SQUARE_MAX - level, SQUARE_MAX, SQUARE_MAX, p);
     }
 }
