 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.RemoteViews;
 
 public class WifiFixerAppWidgetProvider extends AppWidgetProvider {
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 	    int[] appWidgetIds) {
 	final int N = appWidgetIds.length;
	final int FLAG_CANCEL_CURRENT = 268435456;
 
 	// Perform this loop procedure for each App Widget that belongs to this
 	// provider
 	for (int i = 0; i < N; i++) {
 	    int appWidgetId = appWidgetIds[i];
 
 	    // Create an Intent to launch the service
 	    Intent intent = new Intent(context, WifiFixerService.class);
 	    intent.putExtra(WifiFixerService.FIXWIFI, true);
 	    PendingIntent pendingIntent = PendingIntent.getService(context
		    .getApplicationContext(), 0, intent, FLAG_CANCEL_CURRENT);
 
 	    // Get the layout for the App Widget and attach an on-click listener
 	    // to the button
 	    RemoteViews views = new RemoteViews(context.getPackageName(),
 		    R.layout.widget);
 	    views.setOnClickPendingIntent(R.id.Button, pendingIntent);
 
 	    // Tell the AppWidgetManager to perform an update on the current App
 	    // Widget
 	    appWidgetManager.updateAppWidget(appWidgetId, views);
 	}
 
     }
 }
