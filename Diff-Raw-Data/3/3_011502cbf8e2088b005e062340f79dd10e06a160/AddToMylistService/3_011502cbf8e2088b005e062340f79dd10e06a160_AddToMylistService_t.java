 package net.homelinux.md401.magus;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 
 public class AddToMylistService extends IntentService {
 	private static final FileHandler handler = new FileHandler();
 	private static final int SCIENTIST = 7;
 	public AddToMylistService(){
 		super("AddFilesToMylist");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		UsernamePasswordFile param = (UsernamePasswordFile) intent.getSerializableExtra(UsernamePasswordFile.USERNAME_PASSWORD_FILE);
 		NotificationManager notificationService = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		Intent i = new Intent(this, MagusActivity.class);
 		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
 		Notification notification = new Notification.Builder(this).setContentTitle("Hashing File").setSmallIcon(R.drawable.status_magi).getNotification();
 		notification.setLatestEventInfo(this, "Hashing File", "Hashing File", pi);
 		notification.flags |= Notification.FLAG_NO_CLEAR;
 		startForeground(SCIENTIST, notification);
 		String s;
 		try {
 			handler.addFile(param.username, param.password, true, param.file);
 			s = "Added " + param.file.getName() + " to mylist as watched.";
 		} catch (FailureException e) {
 			s = e.detail;
 		} catch (Exception e) {
 			s = e.getMessage();
 		}
		Notification finishedNotification = new Notification.Builder(this).setContentTitle(s).setContentText(s).setSmallIcon(R.drawable.status_magi).getNotification();
		notificationService.notify(SCIENTIST + 1, finishedNotification);
 	}
 
 }
