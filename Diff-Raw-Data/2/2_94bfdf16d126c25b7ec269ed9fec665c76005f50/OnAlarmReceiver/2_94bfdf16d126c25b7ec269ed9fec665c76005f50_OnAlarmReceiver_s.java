 package csci498.lunchlist;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 public class OnAlarmReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Intent i = new Intent(context, AlarmActivity.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
 	}
 }
