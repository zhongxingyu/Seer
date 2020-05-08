 package csci498.lunchlist;
 
 import android.annotation.SuppressLint;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.widget.RemoteViews;
 
 public class AppWidget extends AppWidgetProvider {
 	@Override
 	public void onUpdate(Context ctxt, AppWidgetManager mgr, int[] appWidgetIds) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			onHCUpdate(ctxt, mgr, appWidgetIds);
 		}
 		else {
 			ctxt.startService(new Intent(ctxt, WidgetService.class));
 		}
 	}
 
 	@SuppressWarnings("deprecation")
	@SuppressLint({ "NewApi", "NewApi" })
 	private void onHCUpdate(Context ctxt, AppWidgetManager mgr, int[] appWidgetIds) {
 		for (int i = 0; i < appWidgetIds.length; ++i) {
 			Intent svcIntent = new Intent(ctxt, ListWidgetService.class);
 			
 			svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
 			
 			RemoteViews widget = new RemoteViews(ctxt.getPackageName(), R.layout.widget);
			widget.setRemoteAdapter(appWidgetIds[i], R.id.restaurants, svcIntent);
 			
 			Intent clickIntent = new Intent(ctxt, DetailForm.class);
 			PendingIntent clickPI = PendingIntent.getActivity(ctxt, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 			
 			widget.setPendingIntentTemplate(R.id.restaurants, clickPI);
			mgr.updateAppWidget(appWidgetIds[i], widget);
 		}
 		
 		super.onUpdate(ctxt, mgr, appWidgetIds);
 	}
 }
