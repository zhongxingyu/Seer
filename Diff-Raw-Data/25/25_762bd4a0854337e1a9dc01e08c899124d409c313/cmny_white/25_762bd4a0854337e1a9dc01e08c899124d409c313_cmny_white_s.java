 package carnero.cmny;
 
import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class cmny_white extends AppWidgetProvider {
 	private final base cmnyBase = new base();
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
 		try {
 			cmnyBase.refresh(base.WHITE, context, ids);
 		} catch (Exception e) {
 			// nothing
 		}
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (intent.getAction().equalsIgnoreCase("cmnyNewMessage") == true) {
 			final Bundle bundle = intent.getExtras();
 			final String text = bundle.getString("text");
 
 			(new refresh(context, text)).start();
 		} else if (intent.getAction().equalsIgnoreCase("cmnyUpdate") == true) {
 			(new refresh(context)).start();
 		} else if (intent.getAction().equalsIgnoreCase("cmnyTouch") == true) {
 			(new refresh(context)).start();
 		}
 
 		super.onReceive(context, intent);
 	}
 
 	private class refresh extends Thread {
 		private Context ctx = null;
 		private String txt = null;
 
 		public refresh(Context context) {
 			ctx = context;
 		}
 
 		public refresh(Context context, String text) {
 			ctx = context;
 			txt = text;
 		}
 
 		@Override
 		public void run() {
 			if (txt == null) {
 				cmnyBase.refresh(base.WHITE, ctx);
 			} else {
 				cmnyBase.refresh(base.WHITE, ctx, txt);
 			}
 		}
 	}
 }
