 package net.trajano.gasprices;
 
 import android.app.ListActivity;
 import android.appwidget.AppWidgetManager;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.RemoteViews;
 
 public class GasPricesWidgetConfigurationActivity extends ListActivity {
 
 	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
 
 	/**
 	 * Preference data, stored in memory until destruction.
 	 */
 	private PreferenceAdaptor preferences;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (Build.PRODUCT.endsWith("sdk")
 				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
 			StrictMode.enableDefaults();
 		}
 		setResult(RESULT_CANCELED);
 
 		preferences = new PreferenceAdaptor(this);
 		setListAdapter(new CityListAdapter(this));
 
 		// listView.set
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
 
 	}
 
 	/**
 	 * When the item is selected, it updates the selected city preference rather
 	 * than sending the data back. It is this activity that is setting what it
 	 * wants
 	 */
 	@Override
 	protected void onListItemClick(final ListView l, final View v,
 			final int position, final long id) {
 		final PreferenceAdaptorEditor editor = preferences.edit();
 		editor.saveWidgetCityId(appWidgetId, id);
 		// Commit is used to make sure the data is put in before we request the
 		// update.
 		editor.commit();
 
 		final RemoteViews remoteViews = new RemoteViews(getPackageName(),
 				R.layout.widget_layout);
		if (!preferences.isUpdateNeeded()) {
			GasPricesWidgetProvider.updateAppWidget(this,
					AppWidgetManager.getInstance(this), appWidgetId,
					preferences, remoteViews);
		}
 		{
 			// Make sure we pass back the original appWidgetId
 			final Intent intent = new Intent();
 			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 			setResult(RESULT_OK, intent);
 		}
 		finish();
 
 	}
 }
