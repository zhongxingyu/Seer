 package com.aragaer.reminder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
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
 		registerReceiver(catcher, new IntentFilter(catcher_action));
 		startForeground(1, buildNotification(this));
 	}
 
 	private static final String PKG_NAME = ReminderService.class.getPackage().getName();
 
 	List<Pair<Bitmap, Intent>> list = new ArrayList<Pair<Bitmap, Intent>>();
 	private Notification buildNotification(Context ctx) {
 		Resources r = ctx.getResources();
 		int height = r.getDimensionPixelSize(R.dimen.notification_height);
 		int margin = r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
 		int size = height - 2 * margin;
 
 		int num = r.getDisplayMetrics().widthPixels / height;
 		if (num > 7) // Hardcoded value, yo!
 			num = 7;
 
 		list.clear();
 		Cursor cursor = ctx.getContentResolver().query(
 				ReminderProvider.content_uri, null, null, null, null);
 		int max = num - 2;
 		ReminderItem item = null;
		while (cursor.moveToNext() && --max > 0) {
 			item = ReminderProvider.getItem(cursor, item);
 			list.add(Pair.create(Bitmaps.memo_bmp(ctx, item, size),
 					new Intent(ctx, ReminderViewActivity.class)
 							.putExtra("reminder_id", item._id)));
 		}
 		int n_sym = list.size();
 		int lost = cursor.getCount() - n_sym;
 		cursor.close();
 
 		Pair<Bitmap, Intent> list_btn = Pair.create(
 				Bitmaps.list_bmp(ctx, lost),
 				new Intent(ctx, ReminderListActivity.class).addFlags(intent_flags));
 		Pair<Bitmap, Intent> new_btn = Pair.create(
 				Bitmaps.add_new_bmp(ctx),
 				new Intent(ctx, ReminderCreateActivity.class).addFlags(intent_flags));
 		list.add(list_btn);
 		list.add(new_btn);
 
 		RemoteViews rv = new RemoteViews(PKG_NAME, R.layout.notification);
 		rv.removeAllViews(R.id.wrap);
 		rv.removeAllViews(R.id.wrap2);
 		for (int i = 0; i < list.size(); i++) {
 			final Pair<Bitmap, Intent> g2i = list.get(i);
 			final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
 			image.setOnClickPendingIntent(R.id.image, PendingIntent.getBroadcast(ctx, i,
 					new Intent(catcher_action).putExtra("what", i), 0));
 			image.setImageViewBitmap(R.id.image, g2i.first);
 			if (i < n_sym)
 				rv.addView(R.id.wrap, image);
 			else
 				rv.addView(R.id.wrap2, image);
 		}
 
 		Notification n = new Notification.Builder(ctx).setSmallIcon(
 				R.drawable.notify, n_sym).setContent(rv).getNotification();
 		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR
 				| Notification.FLAG_ONLY_ALERT_ONCE;
 
 		return n;
 	}
 
 	public static final String catcher_action = "com.aragaer.reminder.CATCH_ACTION";
 	private static final String collapse_method = Build.VERSION.SDK_INT > 16 ? "collapsePanels" : "collapse";
 	private final BroadcastReceiver catcher = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			int position = intent.getIntExtra("what", 99);
 			Intent i = list == null || position >= list.size()
 					? new Intent(context, ReminderListActivity.class)
 					: list.get(position).second;
 
 			try {
 				Object obj = context.getSystemService("statusbar");
 				Class.forName("android.app.StatusBarManager")
 						.getMethod(collapse_method, new Class[0])
 						.invoke(obj, (Object[]) null);
 			} catch (Exception e) {
 				Log.e("STATUSBAR", "Failed to collapse status panel: "+e);
 				// do nothing, it's OK
 			}
 			context.startActivity(i.addFlags(intent_flags));
 		}
 	};
 
 	ContentObserver observer = new ContentObserver(new Handler()) {
 		@SuppressLint("NewApi")
 		public void onChange(boolean selfChange) {
 			this.onChange(selfChange, null);
 		}
 
 		@SuppressLint("Override")
 		public void onChange(boolean selfChange, Uri uri) {
 			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
 					.notify(1, buildNotification(ReminderService.this));
 		}
 	};
 
 	public void onDestroy() {
 		getContentResolver().unregisterContentObserver(observer);
 	}
 }
