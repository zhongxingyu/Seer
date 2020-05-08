 package ch.almana.android.stechkarte.model;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.net.Uri;
 import android.widget.Toast;
 import ch.almana.android.stechkarte.provider.IAccess;
 import ch.almana.android.stechkarte.provider.db.DB;
 import ch.almana.android.stechkarte.provider.db.DB.Timestamps;
 import ch.almana.android.stechkarte.utils.Settings;
 
 public class TimestampAccess implements IAccess {
 	
 	private final Context context;
 	
 	private static TimestampAccess instance;
 	
 	public static void initInstance(Context context) {
 		instance = new TimestampAccess(context);
 	}
 	
 	public static TimestampAccess getInstance() {
 		return instance;
 	}
 	
 	public TimestampAccess(Context context) {
 		super();
 		this.context = context;
 	}
 	
 	private class AlertDialogHandler implements OnClickListener {
 		
 		private static final int ACTION_ADD_INVERTED = 0;
 		private static final int ACTION_ADD = 1;
 		private static final int ACTION_ADDLAST_ADD = 2;
 		private static final int ACTION_CANCAL = 3;
 		private static final int ACTION_MAX = 4;
 		
 		private final Timestamp timestamp;
 		private final CharSequence[] actions;
 		
 		public AlertDialogHandler(Context context, Timestamp timestamp) {
 			this.timestamp = timestamp;
 			actions = new String[ACTION_MAX];
 			String invTsTAsString = Timestamp.getTimestampTypeAsString(context, Timestamp
 					.invertTimestampType(timestamp));
 			String tstAsString = timestamp.getTimestampTypeAsString(context);
 			actions[ACTION_ADD_INVERTED] = "Add " + invTsTAsString;
 			actions[ACTION_ADD] = "Add " + tstAsString;
 			actions[ACTION_ADDLAST_ADD] = "Add last " + invTsTAsString + " and add " + tstAsString;
 			actions[ACTION_CANCAL] = "Cancel";
 		}
 		
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			switch (which) {
 			case ACTION_ADD_INVERTED:
 				timestamp.setTimestampType(Timestamp.invertTimestampType(timestamp));
 				insert(timestamp);
 				break;
 			case ACTION_ADD:
 				insert(timestamp);
 				break;
 			case ACTION_CANCAL:
 				Toast.makeText(context, "Canceled action", Toast.LENGTH_SHORT).show();
 				break;
 			
 			case ACTION_ADDLAST_ADD:
 				insert(timestamp);
 				Intent i = new Intent(Intent.ACTION_INSERT, Timestamps.CONTENT_URI);
 				i.putExtra(Timestamps.NAME_TIMESTAMP_TYPE, Timestamp.invertTimestampType(timestamp));
 				context.startActivity(i);
 				break;
 			
 			default:
 				Toast.makeText(context, "Action not implemented.", Toast.LENGTH_SHORT).show();
 				break;
 			}
 			
 		}
 		
 		public CharSequence[] getActions() {
 			return actions;
 		}
 		
 	}
 	
 	public int delete(Cursor c) {
 		return delete(Timestamps.CONTENT_URI, DB.NAME_ID + "=" + c.getInt(DB.INDEX_ID), null);
 	}
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		return getContext().getContentResolver().delete(uri, selection, selectionArgs);
 	}
 	
 	private Context getContext() {
 		return context;
 	}
 	
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		return getContext().getContentResolver().insert(uri, values);
 	}
 	
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		return getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
 	}
 	
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		return getContext().getContentResolver().update(uri, values, selection, selectionArgs);
 	}
 	
 	public boolean addOutNow(Context ctx) {
 		return addOut(ctx, System.currentTimeMillis());
 	}
 	
 	public boolean addOut(Context ctx, long time) {
 		return processInOutAdd(ctx, time, Timestamp.TYPE_OUT);
 	}
 	
 	public boolean addInNow(Context ctx) {
 		return addIn(ctx, System.currentTimeMillis());
 	}
 	
 	public boolean addToggleTimestampNow(Context ctx) {
 		return processInOutAdd(ctx, System.currentTimeMillis(), Timestamp.TYPE_UNDEF);
 	}
 	
 	public boolean addIn(Context ctx, long time) {
 		return processInOutAdd(ctx, time, Timestamp.TYPE_IN);
 	}
 	
	private boolean processInOutAdd(Context context, long time, int timestampType) {
 		Timestamp lastTs = getLastTimestamp();
 			
 		if (timestampType == Timestamp.TYPE_UNDEF) {
 			timestampType = Timestamp.invertTimestampType(lastTs);
 		}
 
 		Timestamp timestamp = new Timestamp(time, timestampType);
 		if (lastTs != null) {
 			if (Math.abs(timestamp.getTimestamp() - lastTs.getTimestamp()) < Settings
 					.getInstance().getMinTimestampDiff()) {
 				String tsTime = timestamp.toString();
 				String ltsTime = lastTs.toString();
 				Toast.makeText(
						context,
 						"Difference betwenn current and last timestamp is too small!\n"
 								+ tsTime + "\n" + ltsTime
 								+ "\nIgnoring timestamp.", Toast.LENGTH_LONG)
 						.show();
 				return false;
 			}
 			if (timestamp.getTimestampType() == lastTs.getTimestampType()) {
 
				Builder alert = new AlertDialog.Builder(context);
				alert.setTitle("Same timestamp types: "
						+ timestamp.getTimestampTypeAsString(context));
				AlertDialogHandler alertDiaHandler = new TimestampAccess.AlertDialogHandler(
						context, timestamp);
 				alert.setItems(alertDiaHandler.getActions(), alertDiaHandler);
 				alert.create();
 				alert.show();
 				return false;
 			}
 		}
 		insert(timestamp);
 		
 		return true;
 	}
 	
 	public void insert(Timestamp timestamp) {
 		Toast.makeText(context, timestamp.getTimestampTypeAsString(context) + ": " + timestamp.toString(),
 				Toast.LENGTH_LONG).show();
 		Uri uri = insert(Timestamps.CONTENT_URI, timestamp.getValues());
 		long id = ContentUris.parseId(uri);
 		if (id > 0) {
 			timestamp.setId(id);
 		}
 		DayAccess.getInstance()
 				.recalculate(getContext(), timestamp.getDayRef());
 		// RebuildDaysTask.rebuildDays(getContext(), timestamp);
 	}
 	
 	public void update(Timestamp timestamp) {
 		update(Timestamps.CONTENT_URI, timestamp.getValues(), DB.NAME_ID + "=" + timestamp.getId(), null);
 		DayAccess.getInstance()
 				.recalculate(getContext(), timestamp.getDayRef());
 		// RebuildDaysTask.rebuildDays(getContext(), timestamp);
 	}
 	
 	public Cursor query(String selection) {
 		return query(selection, DB.Timestamps.DEFAUL_SORTORDER);
 	}
 	
 	public Cursor query(String selection, String sortOrder) {
 		return query(Timestamps.CONTENT_URI, Timestamps.DEFAULT_PROJECTION, selection, null, sortOrder);
 	}
 	
 	private Timestamp getLastTimestamp() {
 		Timestamp t = null;
 		Cursor cursor = query(Timestamps.NAME_TIMESTAMP + "<"
 				+ System.currentTimeMillis());
 		if (cursor != null && cursor.getCount() > 0) {
 			cursor.moveToFirst();
 			t = new Timestamp(cursor);
 			cursor.close();
 		}
 		return t;
 	}
 	
 	public Timestamp getTimestampById(long id) {
 		Cursor c = query(DB.NAME_ID + "=" + id);
 		if (c.moveToFirst()) {
 			Timestamp ts = new Timestamp(c);
 			c.close();
 			return ts;
 		} else {
 			throw new SQLException("No such id " + id);
 		}
 	}
 	
 }
