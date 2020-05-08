 package ua.in.leopard.androidCoocooAfisha;
 
 
 import java.util.List;
 import java.util.Timer;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.widget.RemoteViews;
 
 public class AfishaWidgetProvider extends AppWidgetProvider {
 	public static String FORCE_WIDGET_UPDATE = "ua.in.leopard.androidCoocooAfisha.FORCE_WIDGET_UPDATE";
 	
 	@Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] app_widget_ids) {
 		updateAllWidgets(context, appWidgetManager, app_widget_ids);
 	}
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		super.onReceive(context, intent);
 		if (FORCE_WIDGET_UPDATE.equals(intent.getAction()) || AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
 			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 	        ComponentName thisWidget = new ComponentName(context, AfishaWidgetProvider.class);
 	        int[] app_widget_ids = appWidgetManager.getAppWidgetIds(thisWidget);
 	        
 			updateAllWidgets(context, appWidgetManager, app_widget_ids);
 		}
 		
 	}
 	
     @Override
     public void onDeleted(Context context, int[] app_widget_ids) {
     	super.onDeleted(context, app_widget_ids);
     	for (int i=0; i< app_widget_ids.length; i++) {
     		Timer timer = SingletoneStorage.get_value_timers(app_widget_ids[i]);
     		if (timer != null){
     			timer.cancel();
     		}
     		AfishaWidgetConfigure.deleteTimerPref(context, app_widget_ids[i]);
         }
     }
     
 	private void updateAllWidgets(Context context, AppWidgetManager appWidgetManager, int[] app_widget_ids){
 		DatabaseHelper DatabaseHelperObject = new DatabaseHelper(context);
 		SingletoneStorage.set_cinemas(DatabaseHelperObject.getTodayCinemasForWidget());
 		
         final int count = app_widget_ids.length;
         for (int i=0; i< count; i++) {
             updateAppWidget(context, appWidgetManager, app_widget_ids[i]);
         }
 	}
     
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
             int app_widget_id) {
     	
     	if (SingletoneStorage.get_cinemas().size() == 0 || EditPreferences.isNoPosters(context) || 
     			!EditPreferences.isCachedPosters(context)){
     		
     		AfishaWidgetProvider.stopTimer(context, appWidgetManager, app_widget_id);
     		
     		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.afisha_widget_provider);
     		view.setImageViewResource(R.id.cinema_poster, R.drawable.poster);
     		view.setTextViewText(R.id.cinema_title, context.getString(R.string.widget_option_no_right));
     		appWidgetManager.updateAppWidget(app_widget_id, view);
     	} else {
     		AfishaWidgetProvider.startTimer(context, appWidgetManager, app_widget_id);
     	}
     }
     
     public static void stopTimer(Context context, AppWidgetManager appWidgetManager, int app_widget_id){
     	SingletoneStorage.put_cinemas_iterators(app_widget_id, 0);
     	
     	Timer timer = SingletoneStorage.get_value_timers(app_widget_id);
 		if (timer != null){
 			timer.cancel();
 		}
     }
     
     public static void startTimer(Context context, AppWidgetManager appWidgetManager, int app_widget_id){
     	SingletoneStorage.put_cinemas_iterators(app_widget_id, 0);
     	
     	Timer timer = SingletoneStorage.get_value_timers(app_widget_id);
 		if (timer != null){
 			timer.cancel();
 		}
 		
 		Timer new_timer = new Timer();
 		new_timer.scheduleAtFixedRate(
         		new WidgetTimerTask(context, appWidgetManager, app_widget_id), 
         		0, 1000 * AfishaWidgetConfigure.loadTimerPref(context, app_widget_id));
         
         SingletoneStorage.put_timers(app_widget_id, new_timer);
     }
     
     public static void updatePoster(Context context, AppWidgetManager appWidgetManager, int app_widget_id){ 
     	
     	List<CinemaDB> cinemas_list = SingletoneStorage.get_cinemas();
     	int cinemas_iterator = SingletoneStorage.get_value_cinemas_iterators(app_widget_id);
     	CinemaDB cinema_object = cinemas_list.get(cinemas_iterator);
     	
     	RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.afisha_widget_provider);
     	if (cinemas_list.size() <= cinemas_iterator){
     		cinemas_iterator = 0;
 		}
     	
     	Bitmap poster = cinema_object.getPosterImg();
 		if (poster != null){
 			view.setImageViewBitmap(R.id.cinema_poster, poster);
 			view.setTextViewText(R.id.cinema_title, cinema_object.getTitle());
 			// click
 	        Intent form = new Intent(context, Cinema.class);
 	        Bundle bundle = new Bundle();
 			bundle.putInt("cinema_id", cinema_object.getId());
 			form.putExtras(bundle);
 	        PendingIntent main = PendingIntent.getActivity(context, 0, form, PendingIntent.FLAG_UPDATE_CURRENT);
 	        view.setOnClickPendingIntent(R.id.cinema_widget_box, main);
 	        // update view
 	        appWidgetManager.updateAppWidget(app_widget_id, view);
 		}
 		
 		cinemas_iterator++;
 		if (cinemas_list.size() <= cinemas_iterator){
 			cinemas_iterator = 0;
 		}
 		SingletoneStorage.put_cinemas_iterators(app_widget_id, cinemas_iterator);
     }
 }
