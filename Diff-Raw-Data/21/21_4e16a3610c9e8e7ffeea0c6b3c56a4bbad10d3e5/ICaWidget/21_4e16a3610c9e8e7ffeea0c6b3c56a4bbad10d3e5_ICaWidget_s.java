 package net.kuwalab.android.icareader;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 
 public class ICaWidget extends AppWidgetProvider {
 	public void onUpdate(Context context, AppWidgetManager awm, int[] ids) {
 		Intent intent = new Intent(context, ICaService.class);
 		context.startService(intent);
 	}
 }
