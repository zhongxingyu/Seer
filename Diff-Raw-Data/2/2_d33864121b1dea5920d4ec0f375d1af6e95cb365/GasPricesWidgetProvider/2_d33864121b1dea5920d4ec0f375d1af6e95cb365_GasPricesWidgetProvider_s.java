 package net.trajano.gasprices;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 import android.os.Build;
 import android.widget.RemoteViews;
 
 public class GasPricesWidgetProvider extends AppWidgetProvider {
 	private static Intent getLaunchIntent(final Context context,
 			final int appWidgetId) {
 		final PackageManager manager = context.getPackageManager();
 		final Intent intent = manager
 				.getLaunchIntentForPackage("net.trajano.gasprices");
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
 		intent.setData(new Uri.Builder().path(String.valueOf(appWidgetId))
 				.build());
 		return intent;
 
 	}
 
 	private static void setBlue(final RemoteViews remoteViews) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			remoteViews.setInt(R.id.thelayout, "setBackgroundResource",
 					R.drawable.myshape);
 		}
 	}
 
 	private static void setGreen(final RemoteViews remoteViews) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			remoteViews.setInt(R.id.thelayout, "setBackgroundResource",
 					R.drawable.myshape_green);
 		}
 	}
 
 	private static void setRed(final RemoteViews remoteViews) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			remoteViews.setInt(R.id.thelayout, "setBackgroundResource",
 					R.drawable.myshape_red);
 		}
 	}
 
 	/**
 	 * This will update the app widgets provided that an update is not needed.
 	 * Because if an update is neded then there isn't a point of changing the UI
 	 * until the updates have been completed.
 	 * 
 	 * @param context
 	 * @param appWidgetManager
 	 * @param appWidgetId
 	 * @param preferences
 	 * @param remoteViews
 	 */
 	public static void updateAppWidget(final Context context,
 			final AppWidgetManager appWidgetManager, final int appWidgetId,
 			final PreferenceAdaptor preferences, final RemoteViews remoteViews) {
		if (!preferences.isUpdateNeeded()) {
 			return;
 		}
 		final CityInfo city = preferences.getWidgetCityInfo(appWidgetId);
 		remoteViews.setTextViewText(R.id.widget_city, city.getName());
 		remoteViews.setTextViewText(
 				R.id.widget_price,
 				context.getResources().getString(R.string.widget_price_format,
 						city.getCurrentGasPrice()));
 		setBlue(remoteViews);
 		if (city.isTomorrowsGasPriceAvailable()) {
 			if (city.isTomorrowsGasPriceUp()) {
 				setRed(remoteViews);
 				remoteViews.setTextViewText(
 						R.id.widget_price_change,
 						context.getResources().getString(
 								R.string.widget_price_change_up_format,
 								city.getPriceDifferenceAbsoluteValue()));
 			} else if (city.isTomorrowsGasPriceDown()) {
 				setGreen(remoteViews);
 				remoteViews.setTextViewText(
 						R.id.widget_price_change,
 						context.getResources().getString(
 								R.string.widget_price_change_down_format,
 								city.getPriceDifferenceAbsoluteValue()));
 			} else {
 				remoteViews.setTextViewText(
 						R.id.widget_price_change,
 						context.getResources().getString(
 								R.string.widget_price_unchanged));
 			}
 		} else {
 			remoteViews.setTextViewText(R.id.widget_price_change, null);
 
 		}
 
 		final PendingIntent pendingIntent = PendingIntent.getActivity(context,
 				appWidgetId, getLaunchIntent(context, appWidgetId), 0);
 
 		remoteViews.setOnClickPendingIntent(R.id.thelayout, pendingIntent);
 		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
 
 	}
 
 	/**
 	 * This will remove all the preferences along with all the
 	 * {@link PendingIntent} associated with the widget.
 	 */
 	@Override
 	public void onDeleted(final Context context, final int[] appWidgetIds) {
 		super.onDeleted(context, appWidgetIds);
 		final PreferenceAdaptor preferences = new PreferenceAdaptor(context);
 		final PreferenceAdaptorEditor editor = preferences.edit();
 		editor.removeWidgetCityId(appWidgetIds);
 		editor.apply();
 		for (final int appWidgetId : appWidgetIds) {
 			final PendingIntent pendingIntent = PendingIntent.getActivity(
 					context, appWidgetId,
 					getLaunchIntent(context, appWidgetId),
 					PendingIntent.FLAG_NO_CREATE);
 			if (pendingIntent != null) {
 				pendingIntent.cancel();
 			}
 		}
 	}
 
 	@Override
 	public void onEnabled(final Context context) {
 		GasPricesUpdateService.scheduleUpdate(context);
 	}
 
 	@Override
 	public void onUpdate(final Context context,
 			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
 		final RemoteViews remoteViews = new RemoteViews(
 				context.getPackageName(), R.layout.widget_layout);
 
 		final PreferenceAdaptor preferences = new PreferenceAdaptor(context);
 
 		for (final int appWidgetId : appWidgetIds) {
 			updateAppWidget(context, appWidgetManager, appWidgetId,
 					preferences, remoteViews);
 		}
 		if (preferences.isUpdateNeeded()) {
 			// Build the intent to call the service
 			final Intent intent = new Intent(context.getApplicationContext(),
 					GasPricesUpdateService.class);
 			context.startService(intent);
 		}
 	}
 
 }
