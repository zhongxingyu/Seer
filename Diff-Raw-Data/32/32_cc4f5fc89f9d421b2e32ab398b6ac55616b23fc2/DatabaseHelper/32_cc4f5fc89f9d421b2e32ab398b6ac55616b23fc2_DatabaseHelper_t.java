 package carnero.netmap.database;
 
 import java.io.*;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
 import android.util.Log;
 
 import carnero.netmap.App;
 import carnero.netmap.common.Constants;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	protected OperatorDatabase mOD;
 	// consts
     public static final String DB_NAME = "carnero.netmap";
 	public static final int DB_VERSION = 3;
 
     public DatabaseHelper(Context context) {
         super(context, DB_NAME, null, DB_VERSION);
	    init();
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
 	    final String tableBTS = getBTSTableName(App.getOperatorID());
 	    final String tableSectors = getSectorsTableName(App.getOperatorID());
 
 	    // first-time; it's called before init()
 	    db.execSQL(DatabaseStructure.SQL.createBts(tableBTS));
 	    db.execSQL(DatabaseStructure.SQL.createBtsIndex(tableBTS));
 	    db.execSQL(DatabaseStructure.SQL.createSector(tableSectors));
 	    db.execSQL(DatabaseStructure.SQL.createSectorIndex(tableSectors));
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int versionOld, int versionNew) {
 	    // empty
     }
 
 	protected void init() {
 		if (mOD == null) {
 			mOD = new OperatorDatabase();
 			mOD.operatorID = App.getOperatorID();
 			mOD.database = getWritableDatabase();
 
 			if (mOD.database.inTransaction()) {
 				mOD.database.endTransaction();
 			}
 
 			// check tables
 			ArrayList<String> tables = new ArrayList<>();
 			Cursor cursor = mOD.database.rawQuery("select name from sqlite_master where type='table'", null);
 			if (cursor != null) {
 				int index = cursor.getColumnIndex("name");
 
 				cursor.moveToFirst();
 				do {
 					String table = cursor.getString(index);
 					tables.add(table);
 
 					Log.d(Constants.TAG, "table: " + table);
 				} while (cursor.moveToNext());
 			}
 
 			final String tableBTS = getBTSTableName(App.getOperatorID());
 			final String tableSectors = getSectorsTableName(App.getOperatorID());
 
 			if (tables.contains(DatabaseStructure.TABLE.BTS) || tables.contains(DatabaseStructure.TABLE.SECTOR)) {
 				// move old tables
 				mOD.database.beginTransaction();
 				try {
 					if (tables.contains(DatabaseStructure.TABLE.BTS)) {
 						mOD.database.execSQL("alter table '" + DatabaseStructure.TABLE.BTS + "' rename to '" + tableBTS + "'");
 					}
 					if (tables.contains(DatabaseStructure.TABLE.SECTOR)) {
 						mOD.database.execSQL("alter table '" + DatabaseStructure.TABLE.SECTOR + "' rename to '" + tableSectors + "'");
 					}
 
 					mOD.database.setTransactionSuccessful();
 					Log.i(Constants.TAG, "Old tables moved...");
 				} catch (Exception e) {
 					Log.e(Constants.TAG, "Failed to move old tables! " + e.toString());
 				} finally {
 					mOD.database.endTransaction();
 				}
 			} else if (!tables.contains(tableBTS) || !tables.contains(tableSectors)) {
 				// create new operator-related tables
 				mOD.database.beginTransaction();
 				try {
 					if (!tables.contains(tableBTS)) {
 						mOD.database.execSQL(DatabaseStructure.SQL.createBts(tableBTS));
 						mOD.database.execSQL(DatabaseStructure.SQL.createBtsIndex(tableBTS));
 					}
 					if (!tables.contains(tableSectors)) {
 						mOD.database.execSQL(DatabaseStructure.SQL.createSector(tableSectors));
 						mOD.database.execSQL(DatabaseStructure.SQL.createSectorIndex(tableSectors));
 					}

					mOD.database.setTransactionSuccessful();
					Log.i(Constants.TAG, "Tables for " + App.getOperatorID() + " created...");
 				} catch (Exception e) {
 					Log.e(Constants.TAG, "Failed to create new tables! " + e.toString());
 				} finally {
 					mOD.database.endTransaction();
 				}
 			}
 		}
 	}
 
 	public static String getBTSTableName(final String operatorID) {
 		return DatabaseStructure.TABLE.BTS_PREFIX + operatorID;
 	}
 
 	public static String getSectorsTableName(final String operatorID) {
 		return DatabaseStructure.TABLE.SECTOR_PREFIX + operatorID;
 	}
 
 	public OperatorDatabase getDatabase(final String operatorID) {
 		if (mOD == null) {
 			init();
 		}
 
 		return mOD;
 	}
 
     public void release() {
 	    if (mOD != null) {
 		    if (mOD.database.inTransaction()) {
 			    mOD.database.endTransaction();
 		    }
 
 		    mOD.database.close();
 		    mOD = null;
 
             SQLiteDatabase.releaseMemory();
         }
     }
 
 	public static String getImportFileName() {
 		return DatabaseHelper.DB_NAME + ".import.sqlite";
 	}
 
 	public static File getImportFile() {
 		File sd = Environment.getExternalStorageDirectory();
 		String path = DatabaseHelper.DB_NAME + ".import.sqlite";
 
 		if (!sd.canWrite()) {
 			return null;
 		}
 
 		return new File(sd, path);
 	}
 
 	public static long getImportFileDate() {
 		File file = getImportFile();
 		if (file.exists()) {
 			return file.lastModified();
 		} else {
 			return -1;
 		}
 	}
 
 	public static File getExportFile() {
 		File sd = Environment.getExternalStorageDirectory();
 		String path = DatabaseHelper.DB_NAME + ".backup.sqlite";
 
 		if (!sd.canWrite()) {
 			return null;
 		}
 
 		return new File(sd, path);
 	}
 
 	public void importDB() {
 		exportDB();
 
 		File data = Environment.getDataDirectory();
 
 		String pathApp = "/data/carnero.netmap/databases/" + DatabaseHelper.DB_NAME;
 		File databaseApp = new File(data, pathApp);
 		File databaseExport = getImportFile();
 
 		if (databaseExport == null || !databaseExport.exists()) {
 			Log.e(Constants.TAG, "Unable to export database");
 			return;
 		}
 
 		try {
 			if (mOD != null) {
 				mOD.database.close();
 				mOD = null;
 			}
 			if (databaseApp.exists()) {
 				databaseApp.delete();
 			}
 			databaseApp.createNewFile();
 
 			FileChannel source = new FileInputStream(databaseExport).getChannel();
 			FileChannel destination = new FileOutputStream(databaseApp).getChannel();
 
 			destination.transferFrom(source, 0, source.size());
 
 			destination.close();
 			source.close();
 
 			databaseExport.delete();
 		} catch (FileNotFoundException fnfe) {
 			fnfe.printStackTrace();
 			Log.e(Constants.TAG, "Failed to import database (FNFE)");
 		} catch (IOException ioe) {
 			Log.e(Constants.TAG, "Failed to import database (IOE)");
 		}
 	}
 
 	public static void tryExportDB() {
 		File exported = getExportFile();
 
 		if (!exported.exists() || exported.lastModified() < (System.currentTimeMillis() - (24 * 60 * 60 * 1000))) { // 24 hrs
 			exportDB();
 		}
 	}
 
 	public static void exportDB() {
 		File data = Environment.getDataDirectory();
 
 		String pathApp = "/data/carnero.netmap/databases/" + DatabaseHelper.DB_NAME;
 		File databaseApp = new File(data, pathApp);
 		File databaseExport = getExportFile();
 
 		if (databaseExport == null) {
 			Log.e(Constants.TAG, "Unable to export database");
 			return;
 		}
 
 		try {
 			if (databaseExport.exists()) {
 				databaseExport.delete();
 			}
 			databaseExport.createNewFile();
 
 			FileChannel source = new FileInputStream(databaseApp).getChannel();
 			FileChannel destination = new FileOutputStream(databaseExport).getChannel();
 
 			destination.transferFrom(source, 0, source.size());
 
 			source.close();
 			destination.close();
 		} catch (FileNotFoundException fnfe) {
 			Log.e(Constants.TAG, "Failed to export database (FNFE)");
 		} catch (IOException ioe) {
 			Log.e(Constants.TAG, "Failed to export database (IOE)");
 		}
 	}
 }
