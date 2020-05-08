 package com.aragaer.reminder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Pair;
 import android.widget.RemoteViews;
 
 public class ReminderService extends Service {
 	private static final int intent_flags = Intent.FLAG_ACTIVITY_NEW_TASK
 			| Intent.FLAG_ACTIVITY_CLEAR_TOP
 			| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
 
 	public static final String settings_changed = "com.aragaer.reminder.SETTINGS_CHANGE";
 
 	public IBinder onBind(Intent i) {
 		return null;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		handleCommand(intent);
 		return START_STICKY;
 	}
 
 	private void handleCommand(Intent command) {
 		getContentResolver().registerContentObserver(ReminderProvider.content_uri, false, observer);
 		startForeground(1, buildNotification(this));
 	}
 
 	private static final String PKG_NAME = ReminderService.class.getPackage().getName();
 
 	private static Notification buildNotification(Context ctx) {
 		List<Pair<Bitmap, PendingIntent>> list = new ArrayList<Pair<Bitmap, PendingIntent>>();
 		Resources r = ctx.getResources();
 		int height = r.getDimensionPixelSize(R.dimen.notification_height);
 		int margin = r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
 		int size = height - 2 * margin;
 
 		int num = r.getDisplayMetrics().widthPixels / height;
 		if (num > 7) // Hardcoded value, yo!
 			num = 7;
 
 		Cursor cursor = ctx.getContentResolver().query(
 				ReminderProvider.content_uri, null, null, null, null);
 		List<ReminderItem> items = ReminderProvider.getAllSublist(cursor, num - 2);
 		int lost = cursor.getCount() - items.size();
 		cursor.close();
 
 		for (ReminderItem item : items)
 			list.add(Pair.create(Bitmaps.memo_bmp(ctx, item, size),
 					PendingIntent.getActivity(ctx, (int) item._id,
 							new Intent(ctx, ReminderViewActivity.class)
 								.putExtra("reminder_id", item._id), 0)));
 		items.clear();
 
 		Pair<Bitmap, PendingIntent> list_btn = Pair.create(Bitmaps.list_bmp(
 				ctx, lost), PendingIntent.getActivity(ctx, 0, new Intent(ctx,
 						ReminderListActivity.class).addFlags(intent_flags), 0));
 		Pair<Bitmap, PendingIntent> new_btn = Pair.create(Bitmaps
 				.add_new_bmp(ctx), PendingIntent.getActivity(ctx, 0,
 						new Intent(ctx, ReminderCreateActivity.class)
 							.addFlags(intent_flags), 0));
 		int n_sym = list.size();
 		list.add(list_btn);
 		list.add(new_btn);
 
 		RemoteViews rv = new RemoteViews(PKG_NAME, R.layout.notification);
 		rv.removeAllViews(R.id.wrap);
 		rv.removeAllViews(R.id.wrap2);
 		for (int i = 0; i < list.size(); i++) {
 			final Pair<Bitmap, PendingIntent> g2i = list.get(i);
 			final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
 			image.setOnClickPendingIntent(R.id.image, g2i.second);
 			image.setImageViewBitmap(R.id.image, g2i.first);
 			if (i < n_sym)
 				rv.addView(R.id.wrap, image);
 			else
 				rv.addView(R.id.wrap2, image);
 		}
 		list.clear();
 
 		Notification n = new Notification.Builder(ctx).setSmallIcon(
				R.drawable.notify, list.size()).setContent(rv).getNotification();
 		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR
 				| Notification.FLAG_ONLY_ALERT_ONCE;
 
 		return n;
 	}
 
 	ContentObserver observer = new ContentObserver(new Handler()) {
 		@SuppressLint("NewApi")
 		public void onChange(boolean selfChange) {
 			this.onChange(selfChange, null);
 		}
 
 		public void onChange(boolean selfChange, Uri uri) {
 			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
 					.notify(1, buildNotification(ReminderService.this));
 		}
 	};
 
 	public void onDestroy() {
 		getContentResolver().unregisterContentObserver(observer);
 	}
 }
