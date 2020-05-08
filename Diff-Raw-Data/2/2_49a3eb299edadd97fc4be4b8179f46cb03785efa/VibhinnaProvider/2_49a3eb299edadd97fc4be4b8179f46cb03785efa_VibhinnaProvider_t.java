 package com.vibhinna.binoy;
 
 import java.io.File;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class VibhinnaProvider extends ContentProvider {
 	private DataBaseHelper mDataBaseHelper;
 	private SQLiteDatabase mDB;
 	private Context context;
	public static final String AUTHORITY = "com.vibhinna.binoy.VibhinnaProvider";
 	public static final int TUTORIALS = 0;
 	public static final int TUTORIAL_ID = 1;
 	private static final int TUTORIAL_LIST = 2;
 	private static final int TUTORIAL_DETAILS = 3;
 	//private static final int NEW_VFS = 4;
 	private static final UriMatcher sURIMatcher = new UriMatcher(
 			UriMatcher.NO_MATCH);
 	private static final String TAG = null;
 	private static final String TUTORIALS_BASE_PATH = "vfs";
 	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 			+ "/" + TUTORIALS_BASE_PATH );
 	public static final Uri LIST_DISPLAY_URI = Uri.parse("content://"
 			+ AUTHORITY + "/" + TUTORIALS_BASE_PATH+ "/list");
 	//public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
 	//		+ "/mt-vfs";
 	//public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
 	//		+ "/mt-vfs";
 
 	@Override
 	public int delete(Uri arg0, String arg1, String[] arg2) {
 		int count = 0;
 		switch (sURIMatcher.match(arg0)) {
 		case TUTORIALS:
 			count = mDB.delete(DataBaseHelper.VFS_DATABASE_TABLE, arg1, arg2);
 			break;
 		case TUTORIAL_ID:
 			count = mDB.delete(DataBaseHelper.VFS_DATABASE_TABLE,
 					BaseColumns._ID
 							+ " = "
 							+ arg0.getPathSegments().get(1)
 							+ (!TextUtils.isEmpty(arg1) ? " AND (" + arg1 + ')'
 									: ""), arg2);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + arg0);
 		}
 		getContext().getContentResolver().notifyChange(arg0, null);
 		return count;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		int uriType = sURIMatcher.match(uri);
 		switch (uriType) {
 		case TUTORIAL_ID:
 			return "vnd.android.cursor.item/vnd.vibhinna.vfs";
 		case TUTORIALS:
 			return "vnd.android.cursor.dir/vnd.vibhinna.vfsdir";
 		case TUTORIAL_LIST:
 			return "vnd.android.cursor.dir/vnd.vibhinna.vfslist";
 		case TUTORIAL_DETAILS:
 			return "vnd.android.cursor.item/vnd.vibhinna.vfsdetails";
 		default:
 			throw new IllegalArgumentException("Unknown URI");
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		long rowID = mDB
 				.insert(DataBaseHelper.VFS_DATABASE_TABLE, null, values);
 		if (rowID > 0) {
 			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
 			getContext().getContentResolver().notifyChange(_uri, null);
 			return _uri;
 		}
 		throw new SQLException("Failed to insert row into " + uri);
 	}
 
 	@Override
 	public boolean onCreate() {
 		context = getContext();
 		mDataBaseHelper = new DataBaseHelper(context);
 		mDB = mDataBaseHelper.getWritableDatabase();
 		return true;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		int count = 0;
 		switch (sURIMatcher.match(uri)) {
 		case TUTORIALS:
 			count = mDB.update(DataBaseHelper.VFS_DATABASE_TABLE, values,
 					selection, selectionArgs);
 			break;
 		case TUTORIAL_ID:
 			count = mDB.update(DataBaseHelper.VFS_DATABASE_TABLE, values,
 					BaseColumns._ID
 							+ " = "
 							+ uri.getPathSegments().get(1)
 							+ (!TextUtils.isEmpty(selection) ? " AND ("
 									+ selection + ')' : ""), selectionArgs);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 		queryBuilder.setTables(DataBaseHelper.VFS_DATABASE_TABLE);
 		Log.d(TAG, "uri :"+uri.toString());
 		int uriType = sURIMatcher.match(uri);
 		switch (uriType) {
 		case TUTORIAL_ID:
 			Log.d(TAG, "Get single db row");
 			queryBuilder.appendWhere(BaseColumns._ID + "="
 					+ uri.getLastPathSegment());
 			break;
 		case TUTORIALS:
 			Log.d(TAG, "get all db rows");
 			// no filter
 			break;
 		case TUTORIAL_LIST:
 			Log.d(TAG, "get name list");
 			Cursor c = query(CONTENT_URI, projection, selection, selectionArgs,
 					sortOrder);
 			MatrixCursor cursor = new MatrixCursor(
 					Constants.MATRIX_COLUMN_NAMES);
 			if (c.moveToFirst()) {
 				do {
 					File root = new File(c.getString(2));
 					for (int i = 0; i < c.getColumnCount(); i++) {
 						Log.d(TAG, "c.getString(" + i + ")" + c.getString(i));
 					}
 					if (root.canRead()) {
 						Object[] fsii = new Object[8];
 						String cache = null;
 						String data = null;
 						String system = null;
 						String vdstatus = "0";
 						File cacheimg = new File(root, "cache.img");
 						if (cacheimg.exists()) {
 							cache = cacheimg.length() / 1048576
 									+ context.getString(R.string.smiB);
 						} else
 							cache = context.getString(R.string.error);
 						File dataimg = new File(root, "data.img");
 						if (dataimg.exists()) {
 							data = dataimg.length() / 1048576
 									+ context.getString(R.string.smiB);
 						} else
 							data = context.getString(R.string.error);
 						File systemimg = new File(root, "system.img");
 						if (systemimg.exists()) {
 							system = systemimg.length() / 1048576
 									+ context.getString(R.string.smiB);
 						} else
 							system = context.getString(R.string.error);
 						if (systemimg.exists() && cacheimg.exists()
 								&& dataimg.exists()) {
 							vdstatus = "1";
 						} else
 							vdstatus = "0";
 						fsii[0] = Integer.parseInt(c.getString(0));
 						fsii[1] = c.getString(1);
 						fsii[2] = c.getString(4);
 						fsii[3] = null;
 						fsii[4] = c.getString(3);
 						fsii[5] = context.getString(R.string.caches) + cache
 								+ context.getString(R.string.datas) + data
 								+ context.getString(R.string.systems) + system;
 						fsii[6] = vdstatus;
 						fsii[7] = c.getString(2);
 						cursor.addRow(fsii);
 					}
 				} while (c.moveToNext());
 			}
 			c.close();
 			return cursor;
 		case TUTORIAL_DETAILS:
 			Log.d(TAG, "get vs details");
 			// getvsdata single row cursor to string.
 			ProcessManager processManager = new ProcessManager();
 			String[] vsinfo = new String[29];
 			Cursor dbcursor = query(uri, Constants.allColumns, null, null, null);
 			dbcursor.moveToFirst();
 			vsinfo[0] = dbcursor.getString(0);
 			vsinfo[1] = dbcursor.getString(1);
 			String vspath = dbcursor.getString(2);
 			File vsfolder = new File(vspath);
 			vsinfo[2] = vsfolder.getName();
 			vsinfo[3] = dbcursor.getString(3);
 			vsinfo[4] = dbcursor.getString(4);
 			dbcursor.close();
 			for (int i = 5; i < 29; i++) {
 				vsinfo[i] = context.getString(R.string.na);
 			}
 			for (int i = 7; i < 29; i = i + 8) {
 				vsinfo[i] = vsinfo[i] = context.getString(R.string.corrupted);
 			}
 			try {
 				String[] shellinput = {
 						"/data/data/com.manager.boot.free/bin/tune2fs -l ",
 						vspath, "/cache.img", "" };
 				String istr = processManager.inputStreamReader(shellinput, 40);
 				Scanner scanner = new Scanner(istr).useDelimiter("\\n");
 				scanner.findWithinHorizon(
 						Pattern.compile("Filesystem\\sUUID:\\s*(\\S+)"), 0);
 				String chuuid = scanner.match().group(1);
 				scanner.findWithinHorizon(Pattern
 						.compile("Filesystem\\smagic\\snumber:\\s*(\\S+)"), 0);
 				String chmagicnumber = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\scount:\\s*(\\d+)"), 0);
 				String chblockcount = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Free\\sblocks:\\s*(\\d+)"), 0);
 				String chfreeblocks = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\ssize:\\s*(\\d+)"), 0);
 				String chblocksize = scanner.match().group(1);
 				vsinfo[5] = chuuid;
 				vsinfo[6] = chmagicnumber;
 				if (chmagicnumber.equals("0xEF53")) {
 					vsinfo[7] = context.getString(R.string.healthy);
 				}
 				vsinfo[8] = Integer.parseInt(chblockcount)
 						* Integer.parseInt(chblocksize) / 1048576 + "";
 				vsinfo[9] = Integer.parseInt(chfreeblocks)
 						* Integer.parseInt(chblocksize) / 1048576 + "";
 				vsinfo[10] = chblockcount;
 				vsinfo[11] = chfreeblocks;
 				vsinfo[12] = chblocksize;
 			} catch (Exception e) {
 				Log.w("Exception", "exception in executing :"
 						+ "/data/data/com.manager.boot.free/bin/tune2fs -l "
 						+ vspath + "/cache.img");
 			}
 			try {
 				String[] shellinput = {
 						"/data/data/com.manager.boot.free/bin/tune2fs -l ",
 						vspath, "/data.img", "" };
 				String istr = processManager.inputStreamReader(shellinput, 40);
 				Scanner scanner = new Scanner(istr).useDelimiter("\\n");
 				scanner.findWithinHorizon(
 						Pattern.compile("Filesystem\\sUUID:\\s*(\\S+)"), 0);
 				String dauuid = scanner.match().group(1);
 				scanner.findWithinHorizon(Pattern
 						.compile("Filesystem\\smagic\\snumber:\\s*(\\S+)"), 0);
 				String damagicnumber = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\scount:\\s*(\\d+)"), 0);
 				String dablockcount = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Free\\sblocks:\\s*(\\d+)"), 0);
 				String dafreeblocks = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\ssize:\\s*(\\d+)"), 0);
 				String dablocksize = scanner.match().group(1);
 				vsinfo[13] = dauuid;
 				vsinfo[14] = damagicnumber;
 				if (damagicnumber.equals("0xEF53")) {
 					vsinfo[15] = context.getString(R.string.healthy);
 				}
 				vsinfo[16] = Integer.parseInt(dablockcount)
 						* Integer.parseInt(dablocksize) / 1048576 + "";
 				vsinfo[17] = Integer.parseInt(dafreeblocks)
 						* Integer.parseInt(dablocksize) / 1048576 + "";
 				vsinfo[18] = dablockcount;
 				vsinfo[19] = dafreeblocks;
 				vsinfo[20] = dablocksize;
 			} catch (Exception e) {
 				Log.w("Exception", "exception in executing :"
 						+ "/data/data/com.manager.boot.free/bin/tune2fs -l "
 						+ vspath + "/data.img");
 			}
 			try {
 				String[] shellinput = {
 						"/data/data/com.manager.boot.free/bin/tune2fs -l ",
 						vspath, "/system.img", "" };
 				String istr = processManager.inputStreamReader(shellinput, 40);
 				Scanner scanner = new Scanner(istr).useDelimiter("\\n");
 				scanner.findWithinHorizon(
 						Pattern.compile("Filesystem\\sUUID:\\s*(\\S+)"), 0);
 				String syuuid = scanner.match().group(1);
 				scanner.findWithinHorizon(Pattern
 						.compile("Filesystem\\smagic\\snumber:\\s*(\\S+)"), 0);
 				String symagicnumber = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\scount:\\s*(\\d+)"), 0);
 				String syblockcount = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Free\\sblocks:\\s*(\\d+)"), 0);
 				String syfreeblocks = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\ssize:\\s*(\\d+)"), 0);
 				String syblocksize = scanner.match().group(1);
 				vsinfo[21] = syuuid;
 				vsinfo[22] = symagicnumber;
 				if (symagicnumber.equals("0xEF53")) {
 					vsinfo[23] = context.getString(R.string.healthy);
 				}
 				vsinfo[24] = Integer.parseInt(syblockcount)
 						* Integer.parseInt(syblocksize) / 1048576 + "";
 				vsinfo[25] = Integer.parseInt(syfreeblocks)
 						* Integer.parseInt(syblocksize) / 1048576 + "";
 				vsinfo[26] = syblockcount;
 				vsinfo[27] = syfreeblocks;
 				vsinfo[28] = syblocksize;
 			} catch (Exception e) {
 				Log.w("Exception", "exception in executing :"
 						+ "/data/data/com.manager.boot.free/bin/tune2fs -l "
 						+ vspath + "/system.img");
 			}
 			String key[] = new String[29];
 			for (int i = 0; i < key.length; i++) {
 				key[i] = "key" + i;
 			}
 			MatrixCursor matcursor = new MatrixCursor(key);
 			matcursor.addRow(vsinfo);
 			return matcursor;
 		default:
 			throw new IllegalArgumentException("Unknown URI");
 		}
 
 		Cursor cursor = queryBuilder.query(
 				mDataBaseHelper.getReadableDatabase(), projection, selection,
 				selectionArgs, null, null, sortOrder);
 
 		cursor.setNotificationUri(context.getContentResolver(), uri);
 		return cursor;
 	}
 
 	static {
 		sURIMatcher.addURI(AUTHORITY, TUTORIALS_BASE_PATH, TUTORIALS);
 		sURIMatcher.addURI(AUTHORITY, TUTORIALS_BASE_PATH + "/#", TUTORIAL_ID);
 		sURIMatcher.addURI(AUTHORITY, TUTORIALS_BASE_PATH + "/list",
 				TUTORIAL_LIST);
 		sURIMatcher.addURI(AUTHORITY, TUTORIALS_BASE_PATH + "/details/#",
 				TUTORIAL_DETAILS);
 		sURIMatcher.addURI(AUTHORITY, TUTORIALS_BASE_PATH + "/new/*",
 				NEW_VFS);
 	}
 }
