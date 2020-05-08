 package org.kvj.quebec4.widgets;
 
 import org.kvj.quebec4.R;
 import org.kvj.quebec4.service.Q4App;
 import org.kvj.quebec4.ui.DrawingPane;
 import org.kvj.quebec4.ui.NewTask;
 import org.kvj.quebec4.ui.TaskList;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class CapturePanel extends AppWidgetProvider {
 
 	private static final String TAG = "CapturePanel";
 
 	private PendingIntent createCaptureIntent(Context context, int id,
 			String type) {
 		Intent intent = new Intent(context, NewTask.class);
 		intent.putExtra("type", type);
 		PendingIntent pendingIntent = PendingIntent.getActivity(context, id,
 				intent, PendingIntent.FLAG_CANCEL_CURRENT);
 		return pendingIntent;
 	}
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		for (int i = 0; i < appWidgetIds.length; i++) {
 			int id = appWidgetIds[i];
 			SharedPreferences prefs = Q4App.getInstance().getWidgetConfig(id,
 					"capture");
 			if (null == prefs) {
 				Log.w(TAG, "No config for " + id);
 				continue;
 			}
 			Log.i(TAG, "Update widget: " + id);
 			RemoteViews views = new RemoteViews(context.getPackageName(),
 					R.layout.widget);
 			int bg = R.drawable.widget_bg;
 			switch (Q4App.getInstance().getIntPreference(R.string.widgetBG,
 					R.string.widgetBGDefault)) {
 			case 0:
 				bg = android.R.color.transparent;
 				break;
 			case 10:
 				bg = R.drawable.widget_bg10;
 				break;
 			case 50:
 				bg = R.drawable.widget_bg50;
 				break;
 			}
 			views.setInt(R.id.w_root, "setBackgroundResource", bg);
 			views.setOnClickPendingIntent(R.id.w_launcher, PendingIntent
 					.getActivity(context, 0,
 							new Intent(context, TaskList.class),
 							PendingIntent.FLAG_CANCEL_CURRENT));
 			views.setOnClickPendingIntent(R.id.w_drawing, PendingIntent
					.getActivity(context, 1, new Intent(context,
 							DrawingPane.class),
 							PendingIntent.FLAG_CANCEL_CURRENT));
 			views.setOnClickPendingIntent(R.id.w_note,
					createCaptureIntent(context, 2, "none"));
 			views.setOnClickPendingIntent(R.id.w_point,
					createCaptureIntent(context, 3, "point"));
 			views.setOnClickPendingIntent(R.id.w_camera,
					createCaptureIntent(context, 4, "camera"));
 			views.setOnClickPendingIntent(R.id.w_path,
					createCaptureIntent(context, 5, "path"));
 			appWidgetManager.updateAppWidget(id, views);
 		}
 	}
 }
