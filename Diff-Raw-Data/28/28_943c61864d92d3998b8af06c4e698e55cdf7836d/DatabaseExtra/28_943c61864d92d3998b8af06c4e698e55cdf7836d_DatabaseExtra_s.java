 package com.LoLCompanionApp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.os.Environment;
 
 public class DatabaseExtra extends DatabaseHelper {
 
 	final String USER_COUNTER_TABLE = "usercounteredby";
 	final String DEFAULT_COUNTER_TABLE = "defaultcounteredby";
 	final String BACKUP_PATH, BACKUP_USER_FILE, BACKUP_DEFAULT_FILE;
 	final Context context;
 
 	public DatabaseExtra(Context context) {
 		super(context, "extrainfo.sqlite");
 		this.context = context;
 
 		// set backup variables
 		BACKUP_PATH = Environment.getExternalStorageDirectory().toString()
 				+ File.separator + "LoLCompanionAppBackup" + File.separator;
 		BACKUP_USER_FILE = "backup_" + USER_COUNTER_TABLE + ".txt";
 		BACKUP_DEFAULT_FILE = "backup_" + DEFAULT_COUNTER_TABLE + ".txt";
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		try {
 			// backup the counters
 			backupUserCounters();
 			backupDefaultCounters();
 			// upgrade the databases
 			super.onUpgrade(db, oldVersion, newVersion);
 			// import the user counters
 			importUserCounters();
 			importDefaultCounters();
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String[][] getCounteredByChampions(String champ) {
 
 		return getCounterChampions(champ, "champid", "counterid");
 	}
 
 	public String[][] getCounteringChampions(String champ) {
 
 		return getCounterChampions(champ, "counterid", "champid");
 	}
 
 	private String[][] getCounterChampions(String champ, String searchColumn,
 			String champColumn) {
 		// get champions that counter the chosen champion
 
 		String[][] result = null;
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// create cursor array to hold the two result sets (ne for default
 		// values and one for user values)
 		Cursor[] curArray = {
 				database.rawQuery(
 						"SELECT * FROM " + USER_COUNTER_TABLE + " WHERE "
 								+ searchColumn + "=\'"
 								+ champ.replace("'", "''") + "\'", null),
 				database.rawQuery(
 						"SELECT * FROM " + DEFAULT_COUNTER_TABLE + " WHERE "
 								+ searchColumn + "=\'"
 								+ champ.replace("'", "''")
 								+ "\' AND visible=\'true\'", null) };
 
 		// create a result array based on how many rows returned
 		result = new String[curArray[0].getCount() + curArray[1].getCount()][6];
 
 		// create a new result counter to keep track of number of results
 		int counter = 0;
 
 		// go through array of cursors and put in values.
 		for (int j = 0; j < curArray.length; j += 1) {
 			// go through the data and fill the array
 			if (curArray[j].moveToFirst()) {
 				for (int i = 0; i < curArray[j].getCount(); i += 1) {
 					// get the values
 					result[counter][0] = ""
 							+ curArray[j].getString(curArray[j]
 									.getColumnIndex(champColumn));
 					result[counter][1] = ""
 							+ curArray[j].getString(curArray[j]
 									.getColumnIndex("description"));
 					result[counter][2] = ""
 							+ curArray[j].getString(curArray[j]
 									.getColumnIndex("role"));
 					result[counter][3] = ""
 							+ curArray[j].getString(curArray[j]
 									.getColumnIndex("tips"));
 					result[counter][4] = ""
 							+ curArray[j].getString(curArray[j]
 									.getColumnIndex("id"));
 
 					// first pass is user table, second is default table.
 					if (j == 0) {
 						result[counter][5] = "user";
 					} else {
 						result[counter][5] = "default";
 					}
 
 					// move to next row
 					curArray[j].moveToNext();
 					// Increment the resultCounter
 					counter += 1;
 				}
 			}
 		}
 
 		database.close();
 
 		return result;
 	}
 
 	public void deleteAllUserCounters() throws SQLiteException {
 		SQLiteDatabase database = getWritableDatabase();
 
 		// clear all rows from table
 		database.delete(USER_COUNTER_TABLE, null, null);
 
 		database.close();
 	}
 
 	public void restoreDefaultCounters() throws SQLiteException {
 		// delete all user counters
 		deleteAllUserCounters();
 
 		SQLiteDatabase database = getWritableDatabase();
 
 		// create new values for default database
 		ContentValues values = new ContentValues();
 		values.put("visible", "true");
 
 		// update the database with new values. (make rows disappear)
 		database.update(DEFAULT_COUNTER_TABLE, values, null, null);
 
 		database.close();
 	}
 
 	public boolean deleteCounter(String id, String type) throws SQLiteException {
 		SQLiteDatabase database = getWritableDatabase();
 
 		// if deleting a row that is added by the user
 		if (type.equals("user")) {
 			try {
 				database.delete(USER_COUNTER_TABLE, "id='" + id + "'", null);
 				database.close();
 				return true;
 			} catch (SQLiteException e) {
 				e.printStackTrace();
 			}
 		}
 		// else deleting a default row. Only hide default rows.
 		else {
 			// create new values
 			ContentValues values = new ContentValues();
 			values.put("visible", "false");
 
 			try {
 				// update the database with new values. (make rows disappear)
 				database.update(DEFAULT_COUNTER_TABLE, values, "id='" + id
 						+ "'", null);
 				return true;
 			} catch (SQLiteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		database.close();
 		return false;
 	}
 
 	public void restoreDefaultToVisible(String id) {
 		SQLiteDatabase database = getWritableDatabase();
 
 		// create new values
 		ContentValues values = new ContentValues();
 		values.put("visible", "true");
 
 		try {
 			// update the database with new values. (make rows appear)
 			database.update(DEFAULT_COUNTER_TABLE, values, "id='" + id + "'",
 					null);
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 
 		database.close();
 	}
 
 	public void addNewCounter(String counter, String champ, String role,
 			String tips, String description) throws SQLiteException {
 		SQLiteDatabase database = getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put("champid", champ);
 		values.put("counterid", counter);
 		values.put("role", role);
 		values.put("tips", tips);
 		values.put("description", description);
 
 		// insert the new drink into the database
 		database.insert(USER_COUNTER_TABLE, null, values);
 
 		database.close();
 	}
 
 	public void backupUserCounters() throws IOException, SQLiteException {
 		// get path and create new backup file
 		File userPath = new File(BACKUP_PATH);
 		userPath.mkdirs();
 
 		File userFile = new File(BACKUP_PATH + BACKUP_USER_FILE);
 		userFile.createNewFile();
 
 		// new file writer
 		FileWriter fileUserWriter = new FileWriter(userFile);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		// get database cursors
 		Cursor curUser = database.rawQuery("SELECT * FROM "
 				+ USER_COUNTER_TABLE, null);
 
 		// write the user files
 		// write down the actual information written by the user into a file in
 		// order to restore it in the future
 		if (curUser.moveToFirst()) {
 			// get the names of columns in the database
 			String[] dbColumns = curUser.getColumnNames();
 			String row = "";
 			fileUserWriter.write("");
 
 			// get the rows to add into the database
 			// start at 1 to skip id column
 			for (int i = 1; i < dbColumns.length; i += 1) {
 				row += dbColumns[i];
 				// if i is not at the last pass, add a delimener
 				if ((i + 1) < dbColumns.length) {
 					row += "|";
 				}
 			}
 
 			// append the first row that has column names
 			fileUserWriter.append(row + "\n");
 
 			while (!curUser.isAfterLast()) {
 				// reset row string
 				row = "";
 
 				// populate string with data
 				for (int i = 1; i < dbColumns.length; i += 1) {
 					row += curUser.getString(curUser
 							.getColumnIndex(dbColumns[i]));
 					// if not at last pass, add deliminer
 					if ((i + 1) < dbColumns.length) {
 						row += "|";
 					}
 				}
 				// append to the file
 				fileUserWriter.append(row + "\n");
 
 				// go to next line
 				curUser.moveToNext();
 			}
 			// flush and close the writer
 			fileUserWriter.flush();
 			fileUserWriter.close();
 		}
 
 		database.close();
 	}
 
 	public void backupDefaultCounters() throws IOException, SQLiteException {
 		// get path and create new backup file
 		File defaultPath = new File(BACKUP_PATH);
 		defaultPath.mkdirs();
 
 		File defaultFile = new File(BACKUP_PATH + BACKUP_DEFAULT_FILE);
 		defaultFile.createNewFile();
 
 		// create file writers
 		FileWriter fileDefaultWriter = new FileWriter(defaultFile);
 
 		SQLiteDatabase database = getReadableDatabase();
 
 		Cursor curDefault = database.rawQuery("SELECT * FROM "
 				+ DEFAULT_COUNTER_TABLE + " WHERE visible='false'", null);
 
 		// write the the default file.
 		// write down all ids of the files that have been marked as
 		// "deleted by the user" to persist the changes in a new version
 		if (curDefault.moveToFirst()) {
 			String row;
 			fileDefaultWriter.write("");
 
 			while (!curDefault.isAfterLast()) {
 				// get the id of the row that is "deleted"
 				row = curDefault.getString(curDefault.getColumnIndex("id"));
 
 				// append id to file
 				fileDefaultWriter.append(row);
 
 				// if not the last row, append with deliminer ','
 				if (!curDefault.isLast()) {
 					fileDefaultWriter.append(",");
 				}
 
 				curDefault.moveToNext();
 			}
 			// flush and close the writer
 			fileDefaultWriter.flush();
 			fileDefaultWriter.close();
 		}
 
 		database.close();
 	}
 
 	public void importDefaultCounters() throws SQLiteException, IOException {
 		try {
 			File defaultFile = new File(BACKUP_PATH + BACKUP_DEFAULT_FILE);
 			BufferedReader buffreader = new BufferedReader(new FileReader(
 					defaultFile));
 
 			// read the data (all in one line because was appended)
 			String[] defaultIds = buffreader.readLine().split(",");
 
 			for (String id : defaultIds) {
 				restoreDefaultToVisible(id);
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void importUserCounters() throws IOException, SQLiteException {
 		try {
 			File userFile = new File(BACKUP_PATH + BACKUP_USER_FILE);
 			BufferedReader buffreader = new BufferedReader(new FileReader(
 					userFile));
 
 			// read first line (columns)
 			String line = buffreader.readLine();
 
 			// read the rest of the lines to get the data
 			line = buffreader.readLine();
 			while (!line.equals("")) {
 				String[] data = line.split("\\|");
 
 				// add the counter data into the database
 				addNewCounter(data[1], data[0], data[3], data[4], data[2]);
 
 				// read next line
 				line = buffreader.readLine();
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public String getBackupPath() {
 		return BACKUP_PATH;
 	}
 
 }
