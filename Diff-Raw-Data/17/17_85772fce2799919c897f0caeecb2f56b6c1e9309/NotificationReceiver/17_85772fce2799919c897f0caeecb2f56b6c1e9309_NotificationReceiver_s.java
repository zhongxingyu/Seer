 package com.lnu.todomorrow.utils;
 
 import com.lnu.todomorrow.DialogFinish;
 import com.lnu.todomorrow.DialogPostpone;
 import com.lnu.todomorrow.R;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 public class NotificationReceiver extends BroadcastReceiver {
 	private static final String TAG = NotificationReceiver.class.getSimpleName();
 
 	private static int notificationID = 0;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Task task = (Task) intent.getSerializableExtra("task");
 
 		Intent in = new Intent(context, com.lnu.todomorrow.TaskList.class);
 		PendingIntent pendInt = PendingIntent.getActivity(context, 0, in, 0);
 
 		Log.d(TAG, "id in broadCastReceiver: " + task.getId());
 
 		showNotification(context, pendInt, task);
 	}
 
 	private void showNotification(Context context, PendingIntent pendInt, Task task) {
 		Log.d(TAG, "show notification for task: " + task);
 		Intent intPostpone = new Intent(context, DialogPostpone.class);
 		intPostpone.putExtra("task_id", task.getId());
 		intPostpone.putExtra("notif_id", notificationID);
 
 		Intent intFinish = new Intent(context, DialogFinish.class);
 		intFinish.putExtra("task_id", task.getId());
 		intFinish.putExtra("notif_id", notificationID);
 
 		NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(context)
 				.setSmallIcon(R.drawable.ic_launcher)
 				.setContentTitle(task.getName())
 				.setContentText(
 						task.getGoal().getName() + "    "
 								+ context.getResources().getText(R.string.notificationText) + " "
 								+ String.valueOf(task.getValue()))
 				.setContentIntent(pendInt)
 				.addAction(
 						R.drawable.ic_launcher,
 						"Finished",
 						PendingIntent.getActivity(context, notificationID, intFinish,
 								PendingIntent.FLAG_CANCEL_CURRENT))
 				.addAction(
 						R.drawable.ic_launcher,
 						"Postpone",
 						PendingIntent.getActivity(context, notificationID, intPostpone,
 								PendingIntent.FLAG_CANCEL_CURRENT));
 		NotificationManager notificationManager = (NotificationManager) context
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 		notificationManager.notify(notificationID, notBuilder.build());
 		notificationID++;
 
 	}
 
 }
