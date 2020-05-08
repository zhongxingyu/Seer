 package ch.almana.android.stechkarte.utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.model.Day;
 import ch.almana.android.stechkarte.model.DayAccess;
 import ch.almana.android.stechkarte.model.Timestamp;
 
 public class RebuildDaysTask extends AsyncTask<Timestamp, Object, Object> {
 
 	public static final String PREF_KEY_LAST_UPDATE = "prefKeyRebuildDaysLastUpdate";
 
 	private IProgressWrapper progressWrapper;
 
 	private Timestamp timestamp;
 
 	private static boolean rebuilding = false;
 
 	protected RebuildDaysTask() {
 		super();
 	}
 
 	protected RebuildDaysTask(Context ctx) {
 		this(ctx, null);
 	}
 
 	public RebuildDaysTask(Context ctx, Timestamp timestamp) {
 		if (ctx instanceof Activity) {
 			Activity act = (Activity) ctx;
 			try {
 				this.progressWrapper = new ProgressWrapperActivity(act);
 			} catch (Throwable e) {
 				Log.w(Logger.LOG_TAG, "Cannot create titlebar progess", e);
 			}
 		}
 		if (progressWrapper == null) {
 			this.progressWrapper = new ProgressWrapperDialog(ctx);
 		}
 		this.timestamp = timestamp;
 	}
 
 	@Override
 	protected void onPreExecute() {
		if (rebuilding) {
			// FIXME do not start
		}
 		rebuilding = true;
 		progressWrapper.setTitle("Rebuilding days");
 		progressWrapper.show();
 		super.onPreExecute();
 	}
 
 	@Override
 	protected void onPostExecute(Object result) {
 		Settings.getInstance().setLastDaysRebuild(System.currentTimeMillis());
 		rebuilding = false;
 		progressWrapper.dismiss();
 		super.onPostExecute(result);
 	}
 
 	@Override
 	protected Object doInBackground(Timestamp... timestamps) {
		DayAccess.getInstance().recalculateDayFromTimestamp(timestamp,
				progressWrapper);
 		return null;
 	}
 
 	public static void rebuildDays(Context ctx, Timestamp timestamp) {
 		if (rebuilding) {
 			Log.w(Logger.LOG_TAG, "Allready rebuilding days, returning");
 			Toast.makeText(ctx, "A rebuild days task is allready running.  Not starting again...", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		RebuildDaysTask rebuildDaysTask = new RebuildDaysTask(ctx, timestamp);
 		try {
 			rebuildDaysTask.execute((Timestamp[]) null);
 		} catch (Exception e) {
 			Log.w(Logger.LOG_TAG, "Cannot rebuild days", e);
 		}
 	}
 
 	public static long getLastUpdate() {
 		return Settings.getInstance().getLastDaysRebuild();
 	}
 
 	public static void rebuildDaysIfNeeded(Context ctx) {
 		long lastDaysRebuild = RebuildDaysTask.getLastUpdate();
 		Day day = DayAccess.getInstance().getOldestUpdatedDay(lastDaysRebuild);
 		if (day != null) {
 			Cursor tsCursor = day.getTimestamps();
 			if (tsCursor.moveToFirst()) {
 				Timestamp ts = new Timestamp(tsCursor);
 				if (true) { // FIXME ts.getTimestamp() > lastDaysRebuild) {
 					long luDay = day.getLastUpdated();
 					String lastDaysRebuildStr = SimpleDateFormat.getInstance().format(new Date(lastDaysRebuild));
 					String luDayStr = SimpleDateFormat.getInstance().format(new Date(luDay));
					Log.i(Logger.LOG_TAG, "Rebuild days: starting from " + day.getDayString() + " ( last update " + luDayStr
							+ " last global rebuild " + lastDaysRebuildStr + ")");
 					RebuildDaysTask.rebuildDays(ctx, ts);
 				}
 			}
 		}
 
 	}
 
 	public static boolean isRebuilding() {
 		return rebuilding;
 	}
 
 }
