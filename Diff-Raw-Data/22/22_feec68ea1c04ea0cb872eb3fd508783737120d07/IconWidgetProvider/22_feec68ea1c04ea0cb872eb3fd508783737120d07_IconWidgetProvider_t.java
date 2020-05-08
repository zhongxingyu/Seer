 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.widget;
 
 import java.util.List;
 
 import javax.annotation.Nullable;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.text.format.Time;
import android.view.View;
 import android.widget.RemoteViews;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.annotations.ApplicationScope;
 import com.zapta.apps.maniana.main.MainActivity;
 import com.zapta.apps.maniana.model.AppModel;
 import com.zapta.apps.maniana.model.ItemModelReadOnly;
 
 /**
  * Implemnets the Maniana icon widgets.
  * 
  * @author Tal Dayan
  */
 @ApplicationScope
 public class IconWidgetProvider extends BaseWidgetProvider {
 
     public IconWidgetProvider() {
     }
 
     /** Called by the widget host. */
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         final Time timeNow = new Time();
         timeNow.setToNow();
         update(context, appWidgetManager, appWidgetIds, loadModelForWidgets(context, timeNow));
     }
 
     /** Internal widget update method. */
     private static final void update(Context context, AppWidgetManager appWidgetManager,
             int[] appWidgetIds, @Nullable AppModel model) {
         if (appWidgetIds.length == 0) {
             return;
         }
 
        @Nullable
        final String maybeLabel;
         if (model == null) {
            maybeLabel = "??";
         } else {
             // NOTE: we always exclude completed items from the count.
            final List<ItemModelReadOnly> items = WidgetUtil.selectTodaysItems(model, false);
            final int n = items.size();
            maybeLabel = (n > 0) ? Integer.toString(n) : null;
         }
 
         // Provides access to the remote view hosted by the home launcher.
         RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                 R.layout.widget_icon_layout);
 
         // Set widget on click to trigger the Manian app
         final Intent intent = new Intent(context, MainActivity.class);
         final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
         remoteViews.setOnClickPendingIntent(R.id.widget_icon_top_view, pendingIntent);
 
        if (maybeLabel == null) {
            remoteViews.setInt(R.id.widget_icon_label_view, "setVisibility", View.GONE);
        } else {
            remoteViews.setInt(R.id.widget_icon_label_view, "setVisibility", View.VISIBLE);
            remoteViews.setTextViewText(R.id.widget_icon_label_text_view, maybeLabel);
        }
 
         // Tell the app widget manager to replace the views with the new views. This is not a
         // partial update.
         appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
     }
 
     public static void updateAllIconWidgetsFromModel(Context context, @Nullable AppModel model) {
         // Get list of all widget ids
         final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         final int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                 IconWidgetProvider.class));
 
         // Update (ignores silently if widgetIds is empty)
         update(context, appWidgetManager, widgetIds, model);
     }
 }
