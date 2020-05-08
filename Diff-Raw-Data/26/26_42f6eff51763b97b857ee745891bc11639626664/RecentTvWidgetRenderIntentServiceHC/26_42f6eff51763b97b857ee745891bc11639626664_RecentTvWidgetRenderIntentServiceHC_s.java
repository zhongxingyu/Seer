 package com.anderspersson.xbmcwidget.recenttv;
 
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 import com.anderspersson.xbmcwidget.R;
 import com.anderspersson.xbmcwidget.xbmc.XbmcService;
 
 public class RecentTvWidgetRenderIntentServiceHC extends IntentService {
 	public RecentTvWidgetRenderIntentServiceHC() {
 		super("RecentTvWidgetRenderIntentServiceHC");
 	}
 	
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		String action = intent.getAction();
 		Context context = getBaseContext();
 		
 		if(action.equals(RecentTvWidget.RECENT_TV_UPDATE_WIDGET)) {
 			createWidget(context, intent.getIntExtra("widgetId", 0));
 		}
		else if(action.equals(RecentTvWidget.RECENT_TV_REFRESH)) {
 			refreshWidgets(context);
 		}
 		else if(action.equals(XbmcService.PLAY_EPISODE_ACTION)) {
             handlePlayClick(context, intent.getStringExtra(XbmcService.EXTRA_ITEM));
         }
 	}
 
 	private void refreshWidgets(Context context) {
 		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
         AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
         appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view);
 	}
 	
 	private void createWidget(Context context, int appWidgetId) {
 		updateRemoteView(context, AppWidgetManager.getInstance(context), appWidgetId);
 		
 		triggerRefreshRecentTv(context);
 	}
 
 	private void handlePlayClick(Context context, String filePath) {
 		Toast.makeText(context, "Sent to XBMC", Toast.LENGTH_SHORT).show();
 		
 		Intent playIntent = new Intent(context, XbmcService.class);
 		playIntent.setAction(XbmcService.PLAY_EPISODE_ACTION);
 		playIntent.putExtra(XbmcService.EXTRA_ITEM, filePath);
 		
 		context.startService(playIntent);	
 	}
 	
 	private void triggerRefreshRecentTv(Context context) {
 		Intent refreshEpisodesIntent = new Intent(context, RefreshRecentTvIntentService.class);
 		context.startService(refreshEpisodesIntent);
 	}
 
 	private void updateRemoteView(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
 		Intent intent = new Intent(context, RecentTvRemoteViewsService.class);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
 		
 		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.recent_tv_widget);
 		rv.setRemoteAdapter(appWidgetId, R.id.stack_view, intent);
 		rv.setEmptyView(R.id.stack_view, R.id.empty_view);
 
 		Intent playIntent = new Intent(context, RecentTvWidget.class);
 		playIntent.setAction(XbmcService.PLAY_EPISODE_ACTION);
 		playIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
 		PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 		rv.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent);
 
 		appWidgetManager.updateAppWidget(appWidgetId, rv);
 	}
 }
