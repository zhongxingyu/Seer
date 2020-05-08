 
 package uk.co.samhogy.metroappwidget;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
import android.view.View;
 import android.widget.RemoteViews;
 
 import uk.co.samhogy.metroappwidget.data.DataSource;
 import uk.co.samhogy.metroappwidget.model.Station;
 
 public class MetroTimeProvider extends AppWidgetProvider {
 
     private DataSource source;
     private static int intent_counter = 0;
     private static final String TAG = "uk.co.samhogy.metroappwidget";
 
     @Override
     public void onEnabled(Context context) {
         Log.d(TAG, "onEnabled");
         super.onEnabled(context);
         if (source == null) {
             Log.d(TAG, "SOURCE CREATED in onEnabled");
             source = new DataSource(context);
             source.open();
         }
     }
 
     @Override
     public void onDisabled(Context context) {
         Log.d(TAG, "onDisabled");
         super.onDisabled(context);
         Log.d(TAG, "SOURCE DELETED in onDisabled");
         if (source != null) {
             source.close();
         }
     }
 
     // Called every updatePeriodMillis
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager,
             int[] appWidgetIds) {
         Log.d(TAG, "onUpdate");
         final int N = appWidgetIds.length;
 
         if (source == null) {
             Log.d(TAG, "SOURCE CREATED in onUpdate");
             source = new DataSource(context);
             source.open();
         }
 
         for (int i = 0; i < N; i++) {
             int appWidgetId = appWidgetIds[i];
             int stationId = MetroTimeConfiguration.loadStationId(context, appWidgetId);
             if (stationId != -1) {
                 updateAppWidget(context, appWidgetManager, appWidgetId,
                         source.getStation(stationId));
             }
         }
     }
 
     static void updateAppWidget(Context context,
             AppWidgetManager manager, int appWidgetId, Station station) {
         RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
         views.setEmptyView(R.id.widget_list, R.id.empty_view);
        views.setViewVisibility(R.id.empty_view, View.GONE);
         views.setTextViewText(R.id.widget_title, station.getName());
 
         switch (station.getLines()) {
             case GREEN:
                 views.setImageViewResource(R.id.widget_lines, R.color.line_green);
                 break;
             case YELLOW:
                 views.setImageViewResource(R.id.widget_lines, R.color.line_yellow);
                 break;
             case ALL:
             default:
                 views.setImageViewResource(R.id.widget_lines, R.drawable.shape_all_lines);
                 break;
         }
 
         Intent intent = new Intent(context, DeparturesService.class);
         intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         intent.putExtra("random", intent_counter);
         intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
         views.setRemoteAdapter(R.id.widget_list, intent);
         intent_counter++;
 
         manager.updateAppWidget(appWidgetId, views);
         manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
     }
 
     @Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         final int N = appWidgetIds.length;
 
         for (int i = 0; i < N; i++) {
             MetroTimeConfiguration.deleteStationId(context, appWidgetIds[i]);
         }
     }
 
 }
