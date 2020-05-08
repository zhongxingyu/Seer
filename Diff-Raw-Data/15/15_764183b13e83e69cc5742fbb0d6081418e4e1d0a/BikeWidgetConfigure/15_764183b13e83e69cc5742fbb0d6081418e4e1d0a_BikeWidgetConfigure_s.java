 package fr.itinerennes.ui.widget;
 
 import java.util.Calendar;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.impl.AndroidLoggerFactory;
 
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
 import android.widget.EditText;
 import android.widget.FilterQueryProvider;
 import android.widget.ListView;
 import android.widget.RemoteViews;
 
 import fr.itinerennes.R;
 import fr.itinerennes.TypeConstants;
 import fr.itinerennes.ui.activity.ItineRennesActivity;
 
 /**
  * Activity used to configure the bike widget.
  * 
  * @author Olivier Boudet
  */
 public class BikeWidgetConfigure extends ItineRennesActivity {
 
     /** The event logger. */
     private static final Logger LOGGER = AndroidLoggerFactory.getLogger(BikeWidgetConfigure.class);
 
     /** Widget id. */
     private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
 
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
 
     /**
      * Constructor.
      */
     public BikeWidgetConfigure() {
 
         super();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     public final void onCreate(final Bundle icicle) {
 
         super.onCreate(icicle);
 
         // Set the result to CANCELED. This will cause the widget host to cancel
         // out of the widget placement if they press the back button.
         setResult(RESULT_CANCELED);
 
         // Set the view layout resource to use.
        setContentView(R.layout.act_widget_bike_configure);
 
         // Bind the action for the save button.
         findViewById(R.act_widget_bike.savebutton).setOnClickListener(mOnClickSaveButtonListener);
 
         // TOBO mettre une erreur si la liste des markers en base est vide
 
         listview = (ListView) findViewById(R.act_widget_bike.list);
         listview.setTextFilterEnabled(true);
 
         adapter = new BikeStopListAdapter(getBaseContext(), getApplicationContext().getMarkerDao()
                 .getMarkers(TypeConstants.TYPE_BIKE, null));
 
         adapter.setFilterQueryProvider(new FilterQueryProvider() {
 
             @Override
             public Cursor runQuery(final CharSequence filter) {
 
                 final Cursor c = getApplicationContext().getMarkerDao().getMarkers(
                         TypeConstants.TYPE_BIKE, filter.toString());
                 return c;
             }
 
         });
 
         listview.setAdapter(adapter);
 
         filterText = (EditText) findViewById(R.act_widget_bike.filter);
         filterText.addTextChangedListener(filterTextWatcher);
 
         // Find the widget id from the intent.
         final Intent intent = getIntent();
         final Bundle extras = intent.getExtras();
         if (extras != null) {
             mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                     AppWidgetManager.INVALID_APPWIDGET_ID);
         }
 
         // If they gave us an intent without the widget id, just bail.
         if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
             finish();
         }
 
     }
 
     /** Listener to handle click event on the save button. */
     private final View.OnClickListener mOnClickSaveButtonListener = new View.OnClickListener() {
 
         @Override
         public void onClick(final View v) {
 
             savePref(BikeWidgetConfigure.this, mAppWidgetId, adapter.getSelectedIds());
 
             // initialize the widget view with a progress bar
            final RemoteViews views = new RemoteViews(getPackageName(), R.layout.vw_widget);
             views.addView(R.widget.container, new RemoteViews(getPackageName(),
                     R.layout.misc_view_is_loading));
             AppWidgetManager.getInstance(getBaseContext()).updateAppWidget(mAppWidgetId, views);
 
             // prepare Alarm Service
             final Intent intent = new Intent(BikeWidgetProvider.WIDGET_UPDATE);
             final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                     BikeWidgetConfigure.this, 0, intent, 0);
             final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
             final Calendar calendar = Calendar.getInstance();
             calendar.setTimeInMillis(System.currentTimeMillis());
             calendar.add(Calendar.SECOND, 1);
             alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                     WIDGET_REFRESH_INTERVAL, pendingIntent);
 
             BikeWidgetProvider.savePendingIntent(pendingIntent);
 
             final Intent resultValue = new Intent();
             resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
             setResult(RESULT_OK, resultValue);
 
             finish();
         }
     };
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onDestroy()
      */
     @Override
     protected final void onDestroy() {
 
         super.onDestroy();
         filterText.removeTextChangedListener(filterTextWatcher);
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
             final List<String> checkedIds) {
 
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
 
 }
