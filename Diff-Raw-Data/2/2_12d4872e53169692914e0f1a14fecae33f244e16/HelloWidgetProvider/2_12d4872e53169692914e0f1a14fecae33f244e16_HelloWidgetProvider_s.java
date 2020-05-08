 package org.xo.demo.helloworld.hellowidget;
 
 import java.util.Date;
 
 import org.xo.demo.R;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.widget.RemoteViews;
 
 public class HelloWidgetProvider extends AppWidgetProvider {
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager,
             int[] appWidgetIds) {
         final int N = appWidgetIds.length;
         for (int i = 0; i < N; i++) {
             int appWidgetId = appWidgetIds[i];
             RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hello_widget_layout);
             java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm:ss");
            views.setTextViewText(R.id.text, "ǰʱ䣺" + df.format(new Date()));
             appWidgetManager.updateAppWidget(appWidgetId, views);
         }
     }
 }
