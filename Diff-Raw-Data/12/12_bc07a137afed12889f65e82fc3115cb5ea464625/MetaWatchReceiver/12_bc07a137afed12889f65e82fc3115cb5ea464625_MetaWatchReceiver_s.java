 package uk.co.mentalspace.android.bustimes.displays.metawatch;
 
 import android.content.BroadcastReceiver;
 import uk.co.mentalspace.android.bustimes.BusTimeRefreshService;
 import uk.co.mentalspace.android.bustimes.R;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 public class MetaWatchReceiver extends BroadcastReceiver {
 	private static final String LOGNAME = "MetaWatchReceiver";
 
 	public static final String MW_DISCOVERY = "org.metawatch.manager.APPLICATION_DISCOVERY";
 	public static final String MW_ACTIVATED = "org.metawatch.manager.APPLICATION_ACTIVATE";
 	public static final String MW_DEACTIVATED = "org.metawatch.manager.APPLICATION_DEACTIVATE";
 	public static final String MW_BUTTON = "org.metawatch.manager.BUTTON_PRESS";
 	public static final String MW_ANNOUNCE = "org.metawatch.manager.APPLICATION_ANNOUCE";
 	public static final String MW_UPDATE = "org.metawatch.manager.APPLICATION_UPDATE";
 	public static final String MW_START = "org.metawatch.manager.APPLICATION_START";
 	public static final String MW_STOP = "org.metawatch.manager.APPLICATION_STOP";
 	public static final String MW_NOTIFICATION = "org.metawatch.manager.NOTIFICATION";
 	public static final String MW_VIBRATE = "org.metawatch.manager.VIBRATE";
 	public static final String MW_SILENTMODE = "org.metawatch.manager.SILENTMODE";
 	public static final String MW_WIDGET_UPDATE = "org.metawatch.manager.WIDGET_UPDATE";
 
 	public static final int BUTTON_TYPE_PRESS = 0;
 	public static final int BUTTON_TYPE_NO_HOLD = 1;
 	public static final int BUTTON_TYPE_SHORT_HOLD = 2;
 	public static final int BUTTON_TYPE_LONG_HOLD = 3;
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		final String action = intent.getAction();
 		Log.d(LOGNAME, "Received Intent.  Action: " + action);
 
 		if (MW_DISCOVERY.equals(action)) {
 			metaWatchAnnounce(context);
 		}
 		else if (MW_BUTTON.equals(action)) {
 //			String msg = "Btn Press: button ["+intent.getIntExtra("button", -1)+"] type ["+intent.getIntExtra("type", -1)+"]";
 //			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
 			Bundle b = intent.getExtras();
 			int btnId = b.getInt("button");
 			int btnType = b.getInt("type");
 			Log.d(LOGNAME, "Button press. btn id ["+btnId+"], type ["+btnType+"]");
 			if (BUTTON_TYPE_NO_HOLD == btnType) {
 				Intent service = new Intent(context, MetaWatchService.class);
 				service.setAction(action);
 				service.putExtra("button", btnId);
 				service.putExtra("type", btnType);
 				context.startService(service);
 			}
 		}
 		else if (MW_ACTIVATED.equals(action)) {
 			Intent service = new Intent(context, MetaWatchService.class);
 			service.setAction(action);
 			context.startService(service);
 		}
 		else if (MW_DEACTIVATED.equals(action)) {
 			Intent service = new Intent(context, MetaWatchService.class);
 			service.setAction(action);
 			context.startService(service);
 		}
 		else if (BusTimeRefreshService.ACTION_LATEST_BUS_TIMES.equals(action)) {
 			Intent service = new Intent(context, MetaWatchService.class);
 			service.setAction(BusTimeRefreshService.ACTION_LATEST_BUS_TIMES);
 			service.putExtras(intent.getExtras());
 			context.startService(service);
 		}
 		else {
 			Log.d(LOGNAME, "Unrecognised intent action: "+action);
 		}
 	}
 
	public void metaWatchAnnounce(Context ctx) {
 		Intent announce = new Intent(MW_ANNOUNCE);
 		Bundle b = new Bundle();
 		b.putString("id", ctx.getResources().getString(R.string.app_id));
 		b.putString("name", ctx.getResources().getString(R.string.app_name));
 		announce.putExtras(b);
 		ctx.sendBroadcast(announce);
 		Log.d(LOGNAME, "Sending Metawatch Announce");
 	}
 	
 }
