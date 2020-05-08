 package fi.iki.dezgeg.matkakorttiwidget.gui;
 
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.TypedValue;
 import android.widget.RemoteViews;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import fi.iki.dezgeg.matkakorttiwidget.R;
 import fi.iki.dezgeg.matkakorttiwidget.matkakortti.Card;
 
 public class WidgetUpdaterService extends IntentService
 {
     public WidgetUpdaterService()
     {
         super("MatkakorttiWidgetUpdaterService");
     }
     @Override
     public void onHandleIntent(Intent source)
     {
         System.out.println("onHandleIntent: " + source.toString());
 
         try {
             updateWidgets(getApplicationContext(), MatkakorttiWidgetApp.getCardList());
         } catch (Exception e) {
             setWidgetText(getApplicationContext(), e.getMessage(), "", "", true);
         }
     }
 
     public static void updateWidgets(Context context, List<Card> cards) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 
         String name = null;
         String money = null;
         String period = "None";
         boolean anyCards = false;
         for (Card card : cards) {
             if (!prefs.getBoolean("cardSelected_" + card.getId(), false))
                 continue;
 
             name = card.getName();
             money = card.getMoney() + "";
             if (card.getPeriodExpiryDate() != null)
                 period = new SimpleDateFormat("dd.MM.").format(card.getPeriodExpiryDate()) + "";
             anyCards = true;
         }
 
         if (!anyCards)
             setWidgetText(context, "No cards selected!", "", "", true);
         else {
             setWidgetText(context, name, money, period, false);
         }
     }
 
     private static void setWidgetText(Context context, String name, String money, String period, boolean isError) {
         AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         ComponentName widget = new ComponentName(context, HomescreenWidgetProvider.class);
         int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);
 
        int layoutId = isError ? R.layout.homescreen_widget_warning : R.layout.homescreen_widget;
        int rootWidgetId = isError ? R.id.homescreen_warning_layout : R.id.homescreen_layout;
 
         for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
             if (isError) {
                remoteViews.setTextViewText(R.id.homescreen_warning_text, name);
             } else {
                 remoteViews.setTextViewText(R.id.homescreen_name, name);
                 remoteViews.setTextViewText(R.id.homescreen_money_text, money);
                 remoteViews.setTextViewText(R.id.homescreen_period_text, period);
             }
 
             Intent mainMenuIntent = new Intent(context, MainMenuActivity.class);
             PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainMenuIntent, 0);
 
            remoteViews.setOnClickPendingIntent(rootWidgetId, pendingIntent);
             appWidgetManager.updateAppWidget(widgetId, remoteViews);
         }
     }
 
     @Override
     public IBinder onBind(Intent intent)
     {
         return null;
     }
 
 }
