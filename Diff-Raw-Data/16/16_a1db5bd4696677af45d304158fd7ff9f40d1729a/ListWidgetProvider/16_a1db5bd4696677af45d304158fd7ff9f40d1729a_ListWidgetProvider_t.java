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
 
 import static com.zapta.apps.maniana.util.Assertions.check;
 
 import java.util.List;
 
 import javax.annotation.Nullable;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.text.format.Time;
 import android.view.View;
 import android.widget.RemoteViews;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.main.MainActivity;
 import com.zapta.apps.maniana.main.ResumeAction;
 import com.zapta.apps.maniana.model.AppModel;
 import com.zapta.apps.maniana.model.ItemModelReadOnly;
 import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
 import com.zapta.apps.maniana.preferences.PreferencesTracker;
 import com.zapta.apps.maniana.preferences.WidgetBackgroundType;
 import com.zapta.apps.maniana.services.AppServices;
 import com.zapta.apps.maniana.util.LogUtil;
 
 /**
  * Base class for the task list widgets.
  * 
  * @author Tal Dayan
  */
 public abstract class ListWidgetProvider extends BaseWidgetProvider {
 
     /** List of all widget provider classes. */
     @SuppressWarnings("rawtypes")
     public static final Class[] LIST_WIDGET_PROVIDER_CLASSES = new Class[] {
             // TODO: split to two sets, for old and for new phones
             ListWidgetProvider4.class, ListWidgetProvider1.class, ListWidgetProvider2.class,
             ListWidgetProvider3.class, ListWidgetProviderX.class };
 
     public ListWidgetProvider() {
     }
 
     /** Called by the widget host. */
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         update(context, appWidgetManager, appWidgetIds, loadModel(context));
     }
 
     /** Internal widget update method that accepts the model as a parameter */
     private static final void update(Context context, AppWidgetManager appWidgetManager,
             int[] appWidgetIds, @Nullable AppModel model) {
 
         if (appWidgetIds.length == 0) {
             return;
         }
 
         final SharedPreferences sharedPreferences = PreferenceManager
                 .getDefaultSharedPreferences(context);
 
         // Provides access to the remote view hosted by the home launcher.
         RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                 R.layout.widget_list_layout);
 
         // Set onClick() actions
         setOnClickLaunch(context, remoteViews, R.id.widget_list_top_view, ResumeAction.NONE);
 
         // Set background
         final WidgetBackgroundType backgroundType = PreferencesTracker
                 .readWidgetBackgroundTypePreference(sharedPreferences);
         switch (backgroundType) {
             case PAPER:
                 remoteViews.setInt(R.id.widget_list_top_view, "setBackgroundResource",
                         R.drawable.widget_background);
                 break;
 
             default:
                 LogUtil.error("Unknown widget background type: %s", backgroundType);
                 // fall through to Solid
             case SOLID:
                 final int backgroundColor = PreferencesTracker
                         .readWidgetBackgroundColorPreference(sharedPreferences);
                 remoteViews
                         .setInt(R.id.widget_list_top_view, "setBackgroundColor", backgroundColor);
         }

         // Set toolbar
         final boolean toolbarEanbled = PreferencesTracker
                 .readWidgetShowToolbarPreference(sharedPreferences);
        final boolean showToolbarBackground = toolbarEanbled
                && (backgroundType != WidgetBackgroundType.PAPER);
         setToolbar(context, remoteViews, toolbarEanbled, showToolbarBackground);
 
         // Set item list
         remoteViews.removeAllViews(R.id.widget_list_item_list);
         final int textColor = PreferencesTracker.readWidgetTextColorPreference(sharedPreferences);
         populateItemList(context, remoteViews, model, textColor, sharedPreferences);
 
         // Tell the app widget manager to replace the views with the new views.
         // This is not a partial update.
         appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
     }
 
     private static final void populateItemList(Context context, RemoteViews remoteViews,
             AppModel model, int textColor, SharedPreferences sharedPreferences) {
         // For debugging
         final boolean debugTimestamp = false;
         if (debugTimestamp) {
             final String message = String.format("[%s]", SystemClock.elapsedRealtime() / 1000);
             addMessageItem(context, remoteViews, message, textColor);
         }
 
         if (model == null) {
             addMessageItem(context, remoteViews, "(Maniana data not found)", textColor);
         } else {
             final LockExpirationPeriod lockExpirationPeriod = PreferencesTracker
                     .readLockExpierationPeriodPreference(sharedPreferences);
             // TODO: reorganize the code. No need to read lock preference if
             // date now is same as the model
             Time now = new Time();
             now.setToNow();
 
             final List<ItemModelReadOnly> items = WidgetUtil.selectTodaysActiveItemsByTime(model,
                     now, lockExpirationPeriod);
             if (items.isEmpty()) {
                 addMessageItem(context, remoteViews, "(no active tasks)", textColor);
             } else {
 
                 final boolean singleLine = PreferencesTracker
                         .readWidgetSingleLinePreference(sharedPreferences);
                 for (ItemModelReadOnly item : items) {
                     final RemoteViews remoteItemViews = new RemoteViews(context.getPackageName(),
                             R.layout.widget_list_item_layout);
 
                     // NOTE: TextView has a bug that does not allows more than
                     // two lines when using ellipsize. Otherwise we would give the user more
                     // choices about the max number of lines. More details here:
                     // http://code.google.com/p/android/issues/detail?id=2254
                     if (!singleLine) {
                         remoteItemViews.setBoolean(R.id.widget_item_text_view, "setSingleLine",
                                 false);
                         // NOTE: on ICS (API 14) the text view behaves
                         // differently and does not limit the lines to two when ellipsize. For
                         // consistency, we limit it explicitly to two lines.
                         remoteItemViews.setInt(R.id.widget_item_text_view, "setMaxLines", 2);
                     }
 
                     remoteItemViews.setTextViewText(R.id.widget_item_text_view, item.getText());
                     remoteItemViews.setTextColor(R.id.widget_item_text_view, textColor);
 
                     // If color is NONE show a gray solid color to help visually
                     // grouping item text lines.
                     final int itemColor = item.getColor().isNone() ? 0xff808080 : item.getColor()
                             .getColor();
 
                     remoteItemViews.setInt(R.id.widget_item_color, "setBackgroundColor", itemColor);
 
                     // These are required for ICS. Otherwise text backdround is dark
                     remoteItemViews.setInt(R.id.widget_item_text_view, "setBackgroundColor",
                             0x00000000);
                     remoteItemViews.setInt(R.id.widget_item_view, "setBackgroundColor", 0x00000000);
 
                     remoteViews.addView(R.id.widget_list_item_list, remoteItemViews);
                 }
 
             }
         }
 
     }
 
     private static final void setToolbar(Context context, RemoteViews remoteViews,
             boolean toolbarEnabled, boolean showToolbarBackground) {
         if (!toolbarEnabled) {
             remoteViews.setInt(R.id.widget_list_toolbar, "setVisibility", View.GONE);
             return;
         }
 
         // Make toolbar visible
         remoteViews.setInt(R.id.widget_list_toolbar, "setVisibility", View.VISIBLE);
 
         // Show or hide toolbar background.
         if (showToolbarBackground) {
            remoteViews.setInt(R.id.widget_list_toolbar, "setBackgroundResource",
                    R.drawable.widget_toolbar_background);
         } else {
            remoteViews.setInt(R.id.widget_list_toolbar, "setBackgroundColor", 0x00000000);
         }

         // Set new task by text action.
         setOnClickLaunch(context, remoteViews, R.id.widget_list_toolbar_add_by_text,
                 ResumeAction.ADD_NEW_ITEM_BY_TEXT);
 
         // The voice recognition button is shown only if this device supports voice recognition.
         if (AppServices.isVoiceRecognitionSupported(context)) {
             remoteViews
                     .setInt(R.id.widget_list_toolbar_add_by_voice, "setVisibility", View.VISIBLE);
             setOnClickLaunch(context, remoteViews, R.id.widget_list_toolbar_add_by_voice,
                     ResumeAction.ADD_NEW_ITEM_BY_VOICE);
         } else {
             remoteViews.setInt(R.id.widget_list_toolbar_add_by_voice, "setVisibility", View.GONE);
         }
     }
 
     /** Set onClick() action of given remote view element to launch the app. */
     private static final void setOnClickLaunch(Context context, RemoteViews remoteViews,
             int viewId, ResumeAction resumeAction) {
         final Intent intent = new Intent(context, MainActivity.class);
         ResumeAction.setInIntent(intent, resumeAction);
         // Setting unique intent action and using FLAG_UPDATE_CURRENT to avoid cross
         // reuse of pending intents. See http://tinyurl.com/8axhrlp for more info.
         intent.setAction("maniana.list_widget." + resumeAction.toString());
         final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                 PendingIntent.FLAG_UPDATE_CURRENT);
         remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
     }
 
     private static final void addMessageItem(Context context, RemoteViews remoteViews,
             String message, int textColor) {
         final RemoteViews itemMessageViews = new RemoteViews(context.getPackageName(),
                 R.layout.widget_list_item_layout);
         final int textViewResourceId = R.id.widget_item_text_view;
         itemMessageViews.setBoolean(R.id.widget_item_text_view, "setSingleLine", false);
         itemMessageViews.setTextViewText(textViewResourceId, message);
         itemMessageViews.setTextColor(textViewResourceId, textColor);
         itemMessageViews.setInt(R.id.widget_item_color, "setVisibility", View.GONE);
         remoteViews.addView(R.id.widget_list_item_list, itemMessageViews);
     }
 
     // TODO: decice what we want to do with this.
     // An attempt to update all list widgtes by a direct call.
     public static void updateAllIconWidgetsFromModel(Context context, @Nullable AppModel model) {
         final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 
         // Get ids of all widgets of this type.
         @Nullable
         final int[] widgetIds = getWidgetIds(context, appWidgetManager);
 
         // Update
         if (widgetIds != null) {
             update(context, appWidgetManager, widgetIds, model);
         }
     }
 
     /** Return null if none, or an array if at least one. */
     private static int[] getWidgetIds(Context context, AppWidgetManager appWidgetManager) {
         final int[][] widgetIdLists = new int[LIST_WIDGET_PROVIDER_CLASSES.length][];
 
         int n = 0;
         for (int i = 0; i < LIST_WIDGET_PROVIDER_CLASSES.length; i++) {
             widgetIdLists[i] = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                     LIST_WIDGET_PROVIDER_CLASSES[i]));
             n += widgetIdLists[i].length;
         }
 
         if (n == 0) {
             return null;
         }
 
         final int[] widgetIds = new int[n];
         int m = 0;
         for (int[] src : widgetIdLists) {
             System.arraycopy(src, 0, widgetIds, m, src.length);
             m += src.length;
         }
         check(m == n, "%s vs %s", m, n);
 
         return widgetIds;
     }
 }
