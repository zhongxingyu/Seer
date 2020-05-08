 package ua.in.leopard.androidCoocooAfisha;
 
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.widget.RemoteViews;
 
 public class AfishaWidgetProvider extends AppWidgetProvider {
	public static String FORCE_WIDGET_UPDATE = "ua.in.leopard.androidCoocooAfisha.AfishaWidgetProvider.FORCE_WIDGET_UPDATE";
 	private List<CinemaDB> cinemas_list = null;
 	private Context myContext = null;
 	private AppWidgetManager myAppWidgetManager = null;
 
 	private HashMap<Integer, Timer> timers = new HashMap<Integer, Timer>();
 	private HashMap<Integer, Integer> cinemas_iterators = new HashMap<Integer, Integer>();
 	
 	public class WidgetTimerTask extends TimerTask{
 		private int id;
 		
 		public WidgetTimerTask(int id){
 			this.id = id;
 		}
 		
 		@Override
 		public void run() {
 			updatePoster(this.id);
 		}
 		
 	}
 	
 	
 	
 	@Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] app_widget_ids) {
 		this.myContext = context;
 		this.myAppWidgetManager = appWidgetManager;
 		DatabaseHelper DatabaseHelperObject = new DatabaseHelper(context);
 		this.cinemas_list = DatabaseHelperObject.getTodayCinemas();
 	    
 		final int count = app_widget_ids.length;
         for (int i=0; i< count; i++) {
             updateAppWidget(context, appWidgetManager, app_widget_ids[i]);
         }
 	}
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		super.onReceive(context, intent);
 		if (FORCE_WIDGET_UPDATE.equals(intent.getAction())) {
 			// TODO Update widget UI.
 		}
 	}
 	
     @Override
     public void onEnabled(Context context) {
         PackageManager pm = context.getPackageManager();
         pm.setComponentEnabledSetting(
                 new ComponentName("ua.in.leopard.androidCoocooAfisha", ".AfishaWidgetBroadcastReceiver"),
                 PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                 PackageManager.DONT_KILL_APP);
     }
     @Override
     public void onDisabled(Context context) {
         PackageManager pm = context.getPackageManager();
         pm.setComponentEnabledSetting(
                 new ComponentName("ua.in.leopard.androidCoocooAfisha", ".AfishaWidgetBroadcastReceiver"),
                 PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                 PackageManager.DONT_KILL_APP);
     }
     @Override
     public void onDeleted(Context context, int[] app_widget_ids) {
     	for (int i=0; i< app_widget_ids.length; i++) {
     		Timer timer = timers.get(i);
     		if (timer != null){
     			timer.cancel();
     		}
         }
     }
     
     public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
             int app_widget_id) {
     	timers.put(app_widget_id, new Timer());
     	cinemas_iterators.put(app_widget_id, 0);
     	
     	RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.afisha_widget_provider);
     	view.setImageViewResource(R.id.cinema_poster, R.drawable.poster);
     	
         startTimer(app_widget_id);
         
         //Intent form = new Intent(context, HelloAndroid.class);
         //PendingIntent main = PendingIntent.getActivity(context, 0, form, 0);
         //views.setOnClickPendingIntent(R.id.start_program, main);
 
         // Tell the widget manager
         appWidgetManager.updateAppWidget(app_widget_id, view);
     }
     
     private void startTimer(int app_widget_id){
     	Timer timer = timers.get(app_widget_id);
     	timer.scheduleAtFixedRate(
 		new WidgetTimerTask(app_widget_id), 
 		0, 1000 * 1);
 	}
     
     private void updatePoster(int id){
         if (cinemas_list.size() > 0){
         	RemoteViews view = new RemoteViews(this.myContext.getPackageName(), R.layout.afisha_widget_provider);
         	int cinemas_iterator = cinemas_iterators.get(id);
         	if (cinemas_list.size() <= cinemas_iterator){
         		cinemas_iterator = 0;
     		}
         	Bitmap poster = cinemas_list.get(cinemas_iterator).getPosterImg();
     		if (poster != null){
     			view.setImageViewBitmap(R.id.cinema_poster, poster);
     		}
     		
     		cinemas_iterator++;
     		if (cinemas_list.size() <= cinemas_iterator){
     			cinemas_iterator = 0;
     		}
     		cinemas_iterators.put(id, cinemas_iterator);
 
     		myAppWidgetManager.updateAppWidget(id, view);
         }
     }
 }
