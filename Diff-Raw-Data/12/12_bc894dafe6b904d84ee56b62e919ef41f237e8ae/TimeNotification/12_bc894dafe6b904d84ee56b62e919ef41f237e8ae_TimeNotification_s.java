 package com.corsework.notepad.activity;
 
 import com.corsework.notepad.activity.R;
 import com.corsework.notepad.application.NotePadApplication;
 import com.corsework.notepad.entities.dao.BellDao;
 import com.corsework.notepad.entities.dao.ReminderDao;
 import com.corsework.notepad.entities.program.Bell;
 import com.corsework.notepad.entities.program.Reminder;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class TimeNotification extends BroadcastReceiver {
 public static final String ACTION_TIME_NOIFICATION = "com.corsework.notepad.activity.ACTION_TIME_NOIFICATION";
 	private NotificationManager nm;
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
 		String s="Reminder",st = "See Reminder";
 		Long pos = 0L,pos2 = 0l ;
 		if (intent.hasExtra(NotePadApplication.KEY_ROWID)){
 			s = intent.getExtras().getString(NotePadApplication.KEY_TITLE);
 			st = intent.getExtras().getString(NotePadApplication.KEY_BODY);
 			pos = intent.getExtras().getLong(NotePadApplication.KEY_ROWID);
 			pos2 = intent.getExtras().getLong(NotePadApplication.KEY_SRTD);
 		}

 		((NotePadApplication)context.getApplicationContext()).startNotify(pos2);
 		
 		Notification notification = new Notification(R.drawable.redhat,s,System.currentTimeMillis());
 		int p = Integer.parseInt(""+pos2,10);
 		Intent intentTl = new Intent(context,SeeReminderActivity.class);
 		intentTl.putExtra(NotePadApplication.KEY_ROWID,pos);
 		notification.setLatestEventInfo(context,s,st,
 				PendingIntent.getActivity(context, 0, intentTl, PendingIntent.FLAG_CANCEL_CURRENT));
 		notification.flags = Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.FLAG_AUTO_CANCEL;
 		nm.notify(p,notification);
 	}
 }
