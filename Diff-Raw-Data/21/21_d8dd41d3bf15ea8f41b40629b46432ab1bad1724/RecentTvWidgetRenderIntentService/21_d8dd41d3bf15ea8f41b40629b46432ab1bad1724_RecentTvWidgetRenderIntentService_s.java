 package com.anderspersson.xbmcwidget.recenttv;
 
 import java.util.List;
 
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Handler;
 import android.view.View;
 import android.widget.RemoteViews;
 
 import com.anderspersson.xbmcwidget.R;
 import com.anderspersson.xbmcwidget.common.BitmapCache;
 import com.anderspersson.xbmcwidget.common.XbmcWidgetApplication;
 import com.anderspersson.xbmcwidget.xbmc.TvShowEpisode;
 import com.anderspersson.xbmcwidget.xbmc.XbmcService;
 
 
 public class RecentTvWidgetRenderIntentService extends IntentService {
 	
 	private static final String NAVIGATE = "com.anderspersson.xbmcwidget.recenttv.NAVIGATE";
 	private Handler handler;
 	
 	public RecentTvWidgetRenderIntentService() {
 		super("RecentTvWidgetRenderIntentService");
 	}
 	
 	@Override 
 	public void onCreate() {
 		super.onCreate();
 		handler = new Handler();
 	}
 	
    @Override
    protected void onHandleIntent(Intent intent) {
 		String action = intent.getAction();
 	   
 	   if(action.equals(RecentTvWidget.RECENT_TV_UPDATE_WIDGET)) {
 		   createAppWidget(intent.getIntExtra("widgetId", 0));
 		   return;
 	   }
 	   
 	   if(action.equals(RecentTvRefreshedIntent.REFRESHED)){
 		   updateShows();
 		   return;
 	   }
 	   
 	   if(action.equals(FanArtDownloaderIntentService.FANART_DOWNLOADED)) {
 		   refreshCurrent();
 	   }
 	   
 	   if(action.equals(NAVIGATE)) {
 		   moveTo(intent.getIntExtra("toIndex", -1));
 		   return;
 	   }
 	   
 	   if(action.equals(XbmcService.PLAY_EPISODE_ACTION)) {
 	        handlePlayClick(intent.getStringExtra(XbmcService.EXTRA_ITEM));
 		   return;
 	   }
    }
 
 	private void createAppWidget(int widgetId) {
 		createAndUpdateView(-1, -1, null);	
 	}
 	
 	private void updateShows() {
 		List<TvShowEpisode> episodes = getEpisodes();
 		
 		if(episodes.size() == 0) {
 			return;
 		}
 		
 		createAndUpdateView(0, episodes.size(), episodes.get(0));
 	}
 
 	private void refreshCurrent() {
 		moveTo(getWidgetApplication().getCurrentEpisodeIndex());
 	}
 	
 	private void moveTo(int toIndex) {
 		List<TvShowEpisode> episodes = getEpisodes();
 		
 		if(episodes == null || toIndex < 0 || toIndex >= episodes.size()) {
 			return;
 		}
 		
 		createAndUpdateView(toIndex, episodes.size()-1, episodes.get(toIndex));
 	}
 	
 	private void handlePlayClick(String filePath) {
 		handler.post(new SentToXBMCToast(this));
 		
 		Intent playIntent = new Intent(this, XbmcService.class);
 		playIntent.setAction(XbmcService.PLAY_EPISODE_ACTION);
 		playIntent.putExtra(XbmcService.EXTRA_ITEM, filePath);
 		
 		startService(playIntent);	
 	}
 	
 	private void createAndUpdateView(int episodeIndex, int maxIndex, TvShowEpisode episode) {
 		RemoteViews rv = new RemoteViews( this.getPackageName(), R.layout.recent_tv_widget );
 		ComponentName recentTvWidget = new ComponentName( this, RecentTvWidget.class );
 	    
 		if(episode != null) {
 			setupViewData(episodeIndex, maxIndex, episode, rv);
 			setCurrentEpisodeIndex(episodeIndex);
 		}
 		
 		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
 	    appWidgetManager.updateAppWidget( recentTvWidget, rv );
 	}
 
 	private void setupViewData(int episodeIndex, int maxIndex, TvShowEpisode episode, RemoteViews rv) {
 		Boolean leftArrowEnabled = episodeIndex != 0;
 		Boolean rightArrowEnabled = episodeIndex != maxIndex;
 		
 		setupNavigationButton(rv, R.id.next_show, episodeIndex+1,  rightArrowEnabled);
 		setupNavigationButton(rv, R.id.prev_show, episodeIndex-1, leftArrowEnabled);
 		
 		rv.setTextViewText(R.id.default_header, episode.getTvShowTitle());
 		rv.setTextViewText(R.id.item_header, episode.getTvShowTitle());
 		rv.setTextViewText(R.id.item_subheader, episode.getFullEpisodeTitle());
 		rv.setTextViewText(R.id.age, "Aired\n" + episode.getAge());
 		rv.setViewVisibility(R.id.new_icon, episode.hasBeenSeen() ?  View.INVISIBLE : View.VISIBLE);
 		
         setupClick(episode, rv);
 		setupFanArt(rv, episode);
 	}
 
 	private void setupClick(TvShowEpisode episode, RemoteViews rv) {
 		Intent playIntent = new Intent(this, RecentTvWidgetRenderIntentService.class);
 		playIntent.setAction(XbmcService.PLAY_EPISODE_ACTION);
 		PendingIntent toastPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.play_recenttv_button, toastPendingIntent);
 	}
 	
 	private void setupFanArt(RemoteViews rv, TvShowEpisode episode) {
 		BitmapCache fanArtCache = getBitmapCache();
 		String fanArtPath = episode.getFanArtPath();
 		
 		if(fanArtCache.has(fanArtPath)) {
 			rv.setViewVisibility(R.id.default_header, View.INVISIBLE);
 			rv.setImageViewBitmap(R.id.fanArt, fanArtCache.get(fanArtPath));
 			return;
 		}
 				
 		rv.setImageViewResource(R.id.fanArt, R.drawable.default_fan_art);
 		rv.setViewVisibility(R.id.default_header, View.VISIBLE);
 		Intent fanArtDownloadIntent = new Intent(this, FanArtDownloaderIntentService.class);
 		fanArtDownloadIntent.putExtra("path", fanArtPath);
 		startService(fanArtDownloadIntent);
 	}
 
 	private void setupNavigationButton(RemoteViews remoteViews, int viewId, int toIndex, Boolean enabled) {
 		
 		if(enabled == false) {
 			remoteViews.setBoolean(viewId, "setEnabled", false);
 			return;
 		}
 		
 		Intent navigateIntent = new Intent(this, RecentTvWidgetRenderIntentService.class);
 		navigateIntent.setAction(NAVIGATE);
 		navigateIntent.putExtra("toIndex", toIndex);
 		
 		// To make it unique - due to a bug the following line must be here
 		navigateIntent.setData(Uri.parse(navigateIntent.toUri(Intent.URI_INTENT_SCHEME))); 
         PendingIntent pendingIntent = PendingIntent.getService(this, 0, navigateIntent, 0);
         
         remoteViews.setBoolean(viewId, "setEnabled", true);
         remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
 	}
 	
 	private void setCurrentEpisodeIndex(int episodeIndex) {
 		getWidgetApplication().setCurrentEpisodeIndex(episodeIndex);
 	}
 	
 	private BitmapCache getBitmapCache() {
 		XbmcWidgetApplication app = getWidgetApplication();
 		return app.getBitmapCache();
 	}
 	
 	private List<TvShowEpisode> getEpisodes() {
 		XbmcWidgetApplication app = getWidgetApplication();
 		return app.getLastDownloadedEpisodes();
 	}
 
 	private XbmcWidgetApplication getWidgetApplication() {
 		return (XbmcWidgetApplication)getApplicationContext();
 	}
 }
