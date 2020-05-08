 package com.aragaer.reminder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.graphics.PixelFormat;
 import android.os.Build;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.DisplayMetrics;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.RemoteViews;
 
 public class ReminderService extends Service implements View.OnTouchListener {
 	private static final boolean multiple_intents = Build.VERSION.SDK_INT > 10; // Honeycomb+
 	private static final boolean need_collapse = Build.VERSION.SDK_INT > 13; // ICS+
 	private static final int intent_flags = Intent.FLAG_ACTIVITY_NEW_TASK
 			| Intent.FLAG_ACTIVITY_CLEAR_TOP
 			| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
 	static boolean window_created = false;
 
 	public static final String settings_changed = "com.aragaer.reminder.SETTINGS_CHANGE";
 
 	public IBinder onBind(Intent i) {
 		return null;
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		handleCommand(intent);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		handleCommand(intent);
 		return START_STICKY;
 	}
 
 	private static View click_catcher;
 	static float x = -1, size;
 	private void handleCommand(Intent command) {
 		if (!multiple_intents && !window_created) {
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1,
					1, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
					WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
							| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
							| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
 					PixelFormat.TRANSLUCENT);
 			lp.gravity = Gravity.LEFT | Gravity.TOP;
 			lp.x = 0;
 			click_catcher = new View(this);
 			click_catcher.setOnTouchListener(this);
 			((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(click_catcher, lp);
 			window_created = true;
 		}
 
 		IntentFilter filter = new IntentFilter(ReminderProvider.UPDATE_ACTION);
 		filter.addAction(settings_changed);
 		registerReceiver(update, filter);
 
 		registerReceiver(catcher, new IntentFilter(catcher_action));
 		startForeground(1, buildNotification(this));
 	}
 
 	public static List<Glyph2Intent> list = new ArrayList<Glyph2Intent>();
 
 	private static final String PKG_NAME = ReminderService.class.getPackage().getName(); 
 	private static Notification buildNotification(Context ctx) {
 		boolean invert = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("notification_invert", !multiple_intents);
 		Resources r = ctx.getResources();
 		int height = r.getDimensionPixelSize(R.dimen.notification_height);
 		int padding = r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
 		int size = height - padding * 2;
 
 		DisplayMetrics dm = r.getDisplayMetrics();
 		int num = dm.widthPixels / height;
 
 		Cursor cursor = ctx.getContentResolver()
 				.query(ReminderProvider.content_uri, null, null, null, null);
 		List<ReminderItem> items = ReminderProvider.getAllSublist(cursor, num - 2);
 		int lost = cursor.getCount() - items.size();
 		cursor.close();
 
 		list.clear();
 
 		for (ReminderItem item : items) {
 			Intent intent = new Intent(ctx, ReminderViewActivity.class)
 				.putExtra("reminder_id", item._id)
 				.setAction("View " + item._id);
 			list.add(new Glyph2Intent(Bitmaps.memo_bmp(ctx, item, invert), intent));
 		}
 		items.clear();
 
 		Notification n = new Notification(
 				list.isEmpty()
 					? R.drawable.notify
 					: R.drawable.notify_reminder,
 				ctx.getString(R.string.app_name),
 				System.currentTimeMillis());
 
 		Intent intent = new Intent(ctx, ReminderListActivity.class);
 		intent.addFlags(intent_flags);
 		list.add(new Glyph2Intent(Bitmaps.list_bmp(ctx, lost, invert), intent));
 		intent = new Intent(ctx, ReminderCreateActivity.class);
 		intent.addFlags(intent_flags);
 		list.add(new Glyph2Intent(Bitmaps.add_new_bmp(ctx, invert), intent));
 
 		RemoteViews rv = new RemoteViews(PKG_NAME, R.layout.notification);
 		rv.removeAllViews(R.id.wrap);
 		if (multiple_intents) {
 			for (int i = 0; i < list.size(); i++) {
 				final Glyph2Intent g2i = list.get(i);
 				final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
 				final PendingIntent pi = PendingIntent.getBroadcast(ctx, i,
 						new Intent(catcher_action).putExtra("what", i), 0);
 				image.setOnClickPendingIntent(R.id.image, pi);
 				image.setImageViewBitmap(R.id.image, g2i.image);
 				rv.addView(R.id.wrap, image);
 			}
 		} else {
 			final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
 			Bitmap bmp = Bitmap.createBitmap(height * list.size() - padding * 2, size, Config.ARGB_8888);
 			Canvas c = new Canvas(bmp);
 			for (int i = 0; i < list.size(); i++) {
 				final Glyph2Intent item = list.get(i);
 				c.drawBitmap(item.image, i * height, 0, null);
 			}
 			image.setImageViewBitmap(R.id.image, bmp);
 			rv.addView(R.id.wrap, image);
 		}
 		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
 
 		n.contentView = rv;
 		n.contentIntent = PendingIntent.getBroadcast(ctx, list.size(), new Intent(catcher_action), 0);
 
 		return n;
 	}
 
 	private final BroadcastReceiver update = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, buildNotification(context));
 		}
 	};
 
 	public static final String catcher_action = "com.aragaer.reminder.CATCH_ACTION";
 	private final BroadcastReceiver catcher = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			int glyph_width = context.getResources().getDimensionPixelSize(R.dimen.notification_height);
			int position = multiple_intents ? intent.getIntExtra("what", 99) : (int) ReminderService.x / glyph_width;
 			Intent i = ReminderService.list == null
 					|| position >= ReminderService.list.size()
 				? new Intent(context, ReminderListActivity.class)
 				: ReminderService.list.get(position).intent;
 			if (need_collapse)
 				try {
 					Object obj = context.getSystemService("statusbar");
 				    Class.forName("android.app.StatusBarManager").getMethod("collapse", new Class[0]).invoke(obj, (Object[]) null);
 				} catch (Exception e) {
 					// do nothing, it's OK
 				}
 			context.startActivity(i.addFlags(intent_flags));
 		}
 	};
 
 	public void onDestroy() {
 		((WindowManager) getSystemService("window")).removeView(click_catcher);
 		unregisterReceiver(update);
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		x = event.getRawX();
 		return false;
 	}
 
 	public static final class Glyph2Intent {
 		public Bitmap image;
 		public Intent intent;
 		public Glyph2Intent (Bitmap b, Intent i) {
 			image = b;
 			intent = i;
 		}
 	}
 }
