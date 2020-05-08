 package ch.almana.android.stechkarte.model.calc;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.widget.Toast;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.model.Day;
 import ch.almana.android.stechkarte.model.DayAccess;
 import ch.almana.android.stechkarte.model.Timestamp;
 import ch.almana.android.stechkarte.utils.IProgressWrapper;
 import ch.almana.android.stechkarte.utils.ProgressWrapperActivity;
 import ch.almana.android.stechkarte.utils.ProgressWrapperDialog;
 import ch.almana.android.stechkarte.utils.Settings;
 
 public class RebuildDaysTask extends AsyncTask<Timestamp, Object, Object> {
 
 	public static final String PREF_KEY_LAST_UPDATE = "prefKeyRebuildDaysLastUpdate";
 
 	private IProgressWrapper progressWrapper;
 
 	private Timestamp timestamp;
 
 	private Context ctx;
 
 	private static boolean rebuilding = false;
 
 	protected RebuildDaysTask() {
 		super();
 	}
 
 	protected RebuildDaysTask(Context ctx) {
 		this(ctx, null);
 	}
 
 	protected RebuildDaysTask(Context ctx, Timestamp timestamp) {
 		this.ctx = ctx;
 		this.timestamp = timestamp;
 		if (ctx instanceof FragmentActivity) {
 			FragmentActivity act = (FragmentActivity) ctx;
 			try {
 				this.progressWrapper = new ProgressWrapperActivity(act);
 			} catch (Throwable e) {
 				Log.w(Logger.TAG, "Cannot create titlebar progess", e);
 			}
 		}
 		if (ctx instanceof Activity) {
 			Activity act = (Activity) ctx;
 			try {
 				this.progressWrapper = new ProgressWrapperActivity(act);
 			} catch (Throwable e) {
 				Log.w(Logger.TAG, "Cannot create titlebar progess", e);
 			}
 		}
 		if (progressWrapper == null) {
 			this.progressWrapper = new ProgressWrapperDialog(ctx);
 		}
 	}
 
 	@Override
 	protected void onPreExecute() {
 		progressWrapper.setTitle("Rebuilding...");
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
 		RebuildDays rebuildDays = RebuildDays.create(ctx);
 		rebuildDays.recalculateDays(timestamp, progressWrapper);
 		return null;
 	}
 
 	public static void rebuildDays(Context ctx, Timestamp timestamp) {
 		Logger.logStacktrace("RebuildDays is called");
 		if (rebuilding) {
 			Log.w(Logger.TAG, "Allready rebuilding, returning");
 			Toast.makeText(ctx, "A rebuild task is allready running.  Not starting again...", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		rebuilding = true;
 		RebuildDaysTask rebuildDaysTask = new RebuildDaysTask(ctx, timestamp);
 		try {
 			rebuildDaysTask.execute((Timestamp[]) null);
 		} catch (Exception e) {
 			Log.w(Logger.TAG, "Cannot rebuild", e);
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
				if (ts.getTimestamp() > lastDaysRebuild) {
					long luDay = day.getLastUpdated();
					String lastDaysRebuildStr = SimpleDateFormat.getInstance().format(new Date(lastDaysRebuild));
					String luDayStr = SimpleDateFormat.getInstance().format(new Date(luDay));
					Log.i(Logger.TAG, "Rebuild days: starting from " + day.getDayString() + " ( last update " + luDayStr + " last global rebuild " + lastDaysRebuildStr + ")");
					RebuildDaysTask.rebuildDays(ctx, ts);
				}
 			}
 		}
 
 	}
 
 	public static boolean isRebuilding() {
 		return rebuilding;
 	}
 
 }
