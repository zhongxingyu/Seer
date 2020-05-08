 package se.kjellstrand.colorclock.provider;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import se.kjellstrand.colorclock.R;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class ColorClockAppWidgetProvider extends AppWidgetProvider {
 
     private static final String TAG = ColorClockAppWidgetProvider.class
             .getName();
 
     int[] mAppWidgetIds;
     AppWidgetManager widgetManager;
     static Timer timer = null;
 
     private final int[] mDigitsColor = new int[10];
 
     private final static int[] DIGIT_VIEWS_INDEX = new int[] {
             R.id.digit_0, R.id.digit_1, R.id.digit_2, R.id.digit_3,
             R.id.digit_4, R.id.digit_5, R.id.digit_6, R.id.digit_7,
             R.id.digit_8, R.id.digit_9
     };
 
     private RemoteViews views;
 
     private int mSSColor = 0xff0000cc;
     private int mSColor = 0xff000099;
 
     private int mMMColor = 0xff00cc00;
     private int mMColor = 0xff009900;
 
     private int mHHColor = 0xffcc0000;
     private int mHColor = 0xff990000;
 
     public ColorClockAppWidgetProvider() {
         Log.d(TAG, "ColorClockAppWidgetProvider");

     }
 
     public void update(Context context) {
         views = new RemoteViews(context.getPackageName(), R.layout.color_clock);
 
         Calendar c = Calendar.getInstance();
 
         int s = c.get(Calendar.SECOND) % 10;
         int ss = c.get(Calendar.SECOND) / 10;
         int m = c.get(Calendar.MINUTE) % 10;
         int mm = c.get(Calendar.MINUTE) / 10;
         int h = c.get(Calendar.HOUR_OF_DAY) % 10;
         int hh = c.get(Calendar.HOUR_OF_DAY) / 10;
 
         for (int i = 0; i <= 9; i++) {
             mDigitsColor[i] = 0;
         }
 
         mDigitsColor[s] = additiveBlendTwoColors(mDigitsColor[s], mSColor);
         mDigitsColor[ss] = additiveBlendTwoColors(mDigitsColor[ss], mSSColor);
 
         mDigitsColor[m] = additiveBlendTwoColors(mDigitsColor[m], mMColor);
         mDigitsColor[mm] = additiveBlendTwoColors(mDigitsColor[mm], mMMColor);
 
         mDigitsColor[h] = additiveBlendTwoColors(mDigitsColor[h], mHColor);
         mDigitsColor[hh] = additiveBlendTwoColors(mDigitsColor[hh], mHHColor);
 
        views.setTextViewText(R.id.digit_0, mm + "" + m + ":" + ss + "" + s);
 
         // Set the colors to the views.
         for (int i = 0; i <= 9; i++) {
             views.setInt(DIGIT_VIEWS_INDEX[i], "setBackgroundColor",
                     mDigitsColor[i]);
            //Log.d(TAG, "mDigitsColor["+i+"]"+mDigitsColor[i]);
         }
 
        Log.d(TAG, "Time: " + (System.currentTimeMillis() / 10000) % 6 + ":"
                + (System.currentTimeMillis() / 1000) % 10);
 
         widgetManager.updateAppWidget(mAppWidgetIds, views);
 
     }
 
     private int additiveBlendTwoColors(int c1, int c2) {
         int r = Math.min(((c1 & 0xff0000) + (c2 & 0xff0000)), 0xff0000);
         int g = Math.min(((c1 & 0xff00) + (c2 & 0xff00)), 0xff00);
         int b = Math.min(((c1 & 0xff) + (c2 & 0xff)), 0xff);
         int alpha = 0xff000000;
         int c = alpha + r + g + b;
         return c;
     }
 
     public void onDeleted(Context context, int[] appWidgetIds) {
         CharSequence text = "SHUTDOWN ";
         int duration = Toast.LENGTH_LONG;
         Toast toast = Toast.makeText(context, text, duration);
         toast.show();
         timer.cancel();
     }
 
     public void onUpdate(Context context, AppWidgetManager appWidgetManager,
             int[] appWidgetIds) {
         // lets copy our stuff
         mAppWidgetIds = new int[appWidgetIds.length];
         for (int a = 0; a < appWidgetIds.length; a++)
             mAppWidgetIds[a] = appWidgetIds[a];
         widgetManager = appWidgetManager;
 
         update(context);
 
         if (timer == null) {
             CharSequence text = "INSTALLING";
             int duration = Toast.LENGTH_SHORT;
             Toast toast = Toast.makeText(context, text, duration);
             toast.show();
 
             timer = new Timer();
             Calendar cal = Calendar.getInstance();
             cal.add(Calendar.SECOND, 1);
             cal.set(Calendar.MILLISECOND, 500);
 
             timer.scheduleAtFixedRate(new MyTime(context, this), cal.getTime(),
                     1000);
         }
 
     }
 
     private class MyTime extends TimerTask {
         AppWidgetManager appWidgetManager;
         ColorClockAppWidgetProvider parent;
         Context context;
 
         public MyTime(Context context, ColorClockAppWidgetProvider parent) {
             this.parent = parent;
             this.context = context;
         }
 
         @Override
         public void run() {
             try {
                 parent.update(context);
             } catch (Exception e) {
                 CharSequence text = "tim_excp : " + e.getMessage();
                 int duration = Toast.LENGTH_SHORT;
                 Toast toast = Toast.makeText(context, text, duration);
                 toast.show();
             }
         }
     }
 
 }
