 package com.islamsharabash.magic_app;
 
 import android.appwidget.AppWidgetProvider;
 import android.content.Intent;
 import android.content.Context;
 import android.appwidget.AppWidgetManager;
 
 public class MagicWidget extends AppWidgetProvider
 {
   /** Called when the activity is first created. */
  @Override
    public void onReceive(Context context, Intent intent)
    {
      //super.onCreate(savedInstanceState);
      //setContentView(R.layout.main);
    }
 
   @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
     {
       super.onUpdate(context, appWidgetManager, appWidgetIds);
     }
 
 }
