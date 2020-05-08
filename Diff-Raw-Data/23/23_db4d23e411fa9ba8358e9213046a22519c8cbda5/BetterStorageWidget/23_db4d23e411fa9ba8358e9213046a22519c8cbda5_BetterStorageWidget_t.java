 package com.tempura.storagewidget;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Environment;
 import android.os.StatFs;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class BetterStorageWidget extends AppWidgetProvider {
 	public static String ACTION_WIDGET_CLICKED = "ActionWidgetClicked";
 	
 	public static void updateAppWidget(Context paramContext, AppWidgetManager paramAppWidgetManager, int paramInt) {
 		RemoteViews localRemoteViews = new RemoteViews(paramContext.getPackageName(), R.layout.storage_widget);

		// Internal data directory
		// StatFs localStatFs1 = new StatFs(Environment.getDataDirectory().getPath());
		// SD card
		StatFs localStatFs1 = new StatFs(Environment.getExternalStorageDirectory().getPath());
	    long totalMB = (long)localStatFs1.getBlockCount() * (long)localStatFs1.getBlockSize()/1048576L;
	    long freeMB = (long)localStatFs1.getAvailableBlocks() * (long)localStatFs1.getBlockSize()/1048576L;
 	    	    
 	    // Set to the remote view's widget
	    long freePercent = (freeMB * 100L) / totalMB; 
	    String text = freePercent + "% free " + "(" + freeMB + "/" + totalMB + ")";

 	    localRemoteViews.setProgressBar(R.id.progressBar1, (int)100, (int)(100 - freePercent), false);
 	    localRemoteViews.setTextViewText(R.id.details_percentage, text);
 	    	    
 	    paramAppWidgetManager.updateAppWidget(paramInt, localRemoteViews);
 	    return;
 	}
 	
 	public void onEnabled(Context paramContext) {
 		super.onEnabled(paramContext);
 	}
 
 	public void onReceive(Context paramContext, Intent paramIntent) {
 		super.onReceive(paramContext, paramIntent);
 	    AppWidgetManager localAppWidgetManager = AppWidgetManager.getInstance(paramContext);
 	    int[] arrayOfInt = localAppWidgetManager.getAppWidgetIds(new ComponentName(paramContext.getPackageName(), BetterStorageWidget.class.getName()));
 	    if (paramIntent.getAction().equals(ACTION_WIDGET_CLICKED))
 	    {
 	    	onUpdate(paramContext, localAppWidgetManager, arrayOfInt);
 	    	Toast.makeText(paramContext, "Updating storage info...", Toast.LENGTH_SHORT).show();
 	    }		
 	}
 	
 	public void onUpdate(Context paramContext, AppWidgetManager paramAppWidgetManager, int[] paramArrayOfInt) {
 	    int[] arrayOfInt = paramAppWidgetManager.getAppWidgetIds(new ComponentName(paramContext, BetterStorageWidget.class));
 	    int i = arrayOfInt.length;
 	    for (int j = 0; j < i; j++)
 	    {	      
 	    	updateAppWidget(paramContext, paramAppWidgetManager, arrayOfInt[j]);
 	    }		
 	}
 }
