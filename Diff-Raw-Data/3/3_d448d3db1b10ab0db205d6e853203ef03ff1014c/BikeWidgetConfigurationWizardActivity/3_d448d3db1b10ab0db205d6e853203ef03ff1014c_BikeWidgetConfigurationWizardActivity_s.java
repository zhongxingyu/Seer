 package fr.itinerennes.ui.widget;
 
 import java.util.Calendar;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.FilterQueryProvider;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RemoteViews;
 
 import fr.itinerennes.R;
 import fr.itinerennes.TypeConstants;
 import fr.itinerennes.ui.activity.FullScreenImageActivity;
 import fr.itinerennes.ui.activity.WizardActivity;
 
 /**
  * A configuration wizard for the bus stop widget.
  * 
  * @author Jérémie Huchet
  */
 public final class BikeWidgetConfigurationWizardActivity extends WizardActivity {
 
     /** The event logger. */
     private static final Logger LOGGER = LoggerFactory
             .getLogger(BikeWidgetConfigurationWizardActivity.class);
 
     /** Widget id. */
     private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
 
     /** Name of bike widget shared preferences. */
     private static final String PREFS_NAME = "fr.itinerennes.widget.BikeWidgetProvider";
 
     /** Prefix for preferences key. */
     private static final String PREF_PREFIX_KEY = "STOP_ID_WIDGET_";
 
     /** Widget refresh interval. */
     private static final long WIDGET_REFRESH_INTERVAL = 10 * 60 * 1000;
 
     /** Adapter for the bike stations list view. */
     private BikeStopListAdapter adapter;
 
     /** The edit text for list filtering. */
     private EditText filterText;
 
     /** The list view for bike stations. */
     private ListView listview;
 
     @Override
     protected void onCreateWizard(final Bundle savedInstanceState) {
 
         // Set the result to CANCELED. This will cause the widget host to cancel
         // out of the widget placement if they press the back button.
         setResult(RESULT_CANCELED);
 
         // Find the widget id from the intent.
         final Intent intent = getIntent();
         final Bundle extras = intent.getExtras();
         if (extras != null) {
             appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                     AppWidgetManager.INVALID_APPWIDGET_ID);
         }
 
         // If they gave us an intent without the widget id, just bail.
         if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
             finish();
         }
 
         addStep(new Step1());
         addStep(new Step2());
     }
 
     /**
      * Step displaying some explanations on how the bike widget works.
      * 
      * @author Olivier Boudet
      */
     private class Step1 extends BaseWizardStepAdapter {
 
         @Override
         public View onShow() {
 
             final View step = getLayoutInflater().inflate(R.layout.wzd_wgt_bike_1_instructions,
                     null);
 
             final ImageView preview = (ImageView) step.findViewById(R.id.widget_preview);
             preview.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(final View v) {
 
                     final Intent imagePreview = FullScreenImageActivity.createIntent(
                            BikeWidgetConfigurationWizardActivity.this, R.drawable.pv_widget_bike);
                     startActivity(imagePreview);
                 }
             });
 
             return step;
         }
     }
 
     /**
      * Step displaying the list of bike stations giving to the user the ability to select those he
      * want to follow.
      * 
      * @author Olivier Boudet
      */
     private class Step2 extends BaseWizardStepAdapter {
 
         @Override
         public View onShow() {
 
             final View step = getLayoutInflater().inflate(
                     R.layout.wzd_wgt_bike_2_station_selection, null);
 
             // TOBO mettre une erreur si la liste des markers en base est vide
 
             listview = (ListView) step.findViewById(R.act_widget_bike.list);
             listview.setTextFilterEnabled(true);
 
             adapter = new BikeStopListAdapter(getBaseContext(), getApplicationContext()
                     .getMarkerDao().getMarkers(TypeConstants.TYPE_BIKE, null, null));
 
             adapter.setFilterQueryProvider(new FilterQueryProvider() {
 
                 @Override
                 public Cursor runQuery(final CharSequence filter) {
 
                     final Cursor c = getApplicationContext().getMarkerDao().getMarkers(
                             TypeConstants.TYPE_BIKE, filter.toString(), adapter.getSelectedIds());
                     return c;
                 }
 
             });
             listview.setAdapter(adapter);
 
             filterText = (EditText) step.findViewById(R.act_widget_bike.filter);
             filterText.addTextChangedListener(filterTextWatcher);
 
             return step;
         }
 
         @Override
         public void onFinish() {
 
             savePref(BikeWidgetConfigurationWizardActivity.this, appWidgetId,
                     adapter.getSelectedIds());
 
             // initialize the widget view with a progress bar
             final RemoteViews views = new RemoteViews(getPackageName(), R.layout.wgt_container);
             views.addView(R.widget.container, new RemoteViews(getPackageName(),
                     R.layout.misc_view_is_loading));
             AppWidgetManager.getInstance(getBaseContext()).updateAppWidget(appWidgetId, views);
 
             // prepare Alarm Service
             final Intent intent = new Intent(BikeWidgetProvider.WIDGET_UPDATE);
             final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                     BikeWidgetConfigurationWizardActivity.this, 0, intent, 0);
             final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
             final Calendar calendar = Calendar.getInstance();
             calendar.setTimeInMillis(System.currentTimeMillis());
             calendar.add(Calendar.SECOND, 1);
             alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                     WIDGET_REFRESH_INTERVAL, pendingIntent);
 
             BikeWidgetProvider.savePendingIntent(pendingIntent);
 
             final Intent resultValue = new Intent();
             resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
             setResult(RESULT_OK, resultValue);
 
             finish();
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onDestroy()
      */
     @Override
     protected final void onDestroy() {
 
         super.onDestroy();
         if (null != filterText) {
             filterText.removeTextChangedListener(filterTextWatcher);
         }
     }
 
     /**
      * Saves in preferences the list of checked bike stations.
      * 
      * @param context
      *            the context
      * @param appWidgetId
      *            the widget id
      * @param checkedIds
      *            the list of checked bike stations
      */
     private static void savePref(final Context context, final int appWidgetId,
             final Set<String> checkedIds) {
 
         // prior to API Level 11 it is not possible to store a set of string in preferences. a
         // workaround used here is to concatenate all station is in a unique string, separated by a
         // pipe
 
         final StringBuffer sb = new StringBuffer();
         for (final String id : checkedIds) {
             if (sb.length() > 0) {
                 sb.append(";");
             }
             sb.append(id);
 
         }
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Saving widget preferences for bike stations {} in widget id {}",
                     sb.toString(), appWidgetId);
         }
 
         final SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
         prefs.putString(PREF_PREFIX_KEY + appWidgetId, sb.toString());
         prefs.commit();
     }
 
     /** A text watcher for the stations filter. */
     private final TextWatcher filterTextWatcher = new TextWatcher() {
 
         /**
          * {@inheritDoc}
          * 
          * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
          */
         @Override
         public void afterTextChanged(final Editable s) {
 
         }
 
         /**
          * {@inheritDoc}
          * 
          * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
          */
         @Override
         public void beforeTextChanged(final CharSequence s, final int start, final int count,
                 final int after) {
 
         }
 
         /**
          * {@inheritDoc}
          * 
          * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
          */
         @Override
         public void onTextChanged(final CharSequence s, final int start, final int before,
                 final int count) {
 
             adapter.getFilter().filter(s);
         }
 
     };
 
     /**
      * Deletes preferences for the given widget id.
      * 
      * @param context
      *            the context
      * @param appWidgetId
      *            the widget id
      */
     public static void deletePref(final Context context, final int appWidgetId) {
 
         final SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
         prefs.remove(PREF_PREFIX_KEY + appWidgetId);
         prefs.commit();
     }
 
     /**
      * Load preferences for the given widget id. Returns a semicolon separated string of bike
      * station ids to display.
      * 
      * @param context
      *            the context
      * @param appWidgetId
      *            the widget id
      * @return the semicolon separated string of bike stations id
      */
     public static String loadPref(final Context context, final int appWidgetId) {
 
         final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
         return prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
     }
 }
