 package com.theidiomsbook.controller;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 import java.util.Locale;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.theidiomsbook.R;
 
 public class DictionaryDatabase {
 	public static final String COLUMN_ID = "_id";
 	public static final String COLUMN_SPANISH = "spanish";
 	public static final String COLUMN_ENGLISH = "english";
 	public static final String COLUMN_SPANISH_DESCRIPTION = "spanish_description";
 	public static final String COLUMN_ENGLISH_DESCRIPTION = "english_description";
 	public static final String COLUMN_EXAMPLES = "examples";
 
 	private static DictionaryDatabase dictionary;
 	private static Context context;
 	private static final String TAG = "DictionaryDatabase";
 	private static final String DATABASE_NAME = "tib";
 	private static final String DICTIONARY_TABLE = "dictionary";
 	private static final int DATABASE_VERSION = 4;
 	private static DictionaryOpenHelper mDatabaseOpenHelper;
 	private static SQLiteDatabase mDatabase;
 	private static int NUM_IDIOMS = 0;
 
 	private DictionaryDatabase() {
 		super();
 	}
 
 	public static DictionaryDatabase load(Context ctx) {
 		context = ctx;
 
 		if (dictionary == null)
 			dictionary = new DictionaryDatabase();
 
 		if (mDatabase == null) {
 			mDatabaseOpenHelper = new DictionaryOpenHelper(context);
 			mDatabase = mDatabaseOpenHelper.getWritableDatabase();
 		}
 		dictionary.setNumIdioms();
 
 		return dictionary;
 	}
 
 	public Cursor findIdioms(String language, String expression) {
 		String query = "SELECT _id, spanish, english FROM dictionary WHERE " + language + " like '%"
		    + expression.trim().toLowerCase(Locale.getDefault()) + "%'";
 		return mDatabase.rawQuery(query, null);
 	}
 
 	public Cursor getIdiomsByID(String[] aIDs) {
 		if (aIDs == null)
 			return null;
 
 		StringBuilder sbQuery = new StringBuilder("SELECT * FROM Dictionary WHERE _id IN (");
 		for (int i = 0; i < aIDs.length; i++) {
 			if (i > 0)
 				sbQuery.append(",");
 			sbQuery.append(aIDs[i]);
 		}
 		sbQuery.append(")");
 		return mDatabase.rawQuery(sbQuery.toString(), null);
 	}
 
 	public Cursor getAllIdioms(String sourceLanguage, String targetLanguage) {
 		if (sourceLanguage == null) {
 			sourceLanguage = COLUMN_ENGLISH;
 			targetLanguage = COLUMN_SPANISH;
 		}
 
 		return mDatabase.query(DICTIONARY_TABLE,
 		    new String[] { "_id", sourceLanguage, targetLanguage }, null, null, null, null,
 		    sourceLanguage, null);
 	}
 
 	public int getIdiomsCount() {
 		return NUM_IDIOMS;
 	}
 
 	private void setNumIdioms() {
 		if (mDatabase == null)
 			return;
 		Cursor cursor = mDatabase.rawQuery("SELECT _id FROM dictionary", null);
 		NUM_IDIOMS = cursor.getCount();
 	}
 
 	public static String getColumnNameByLanguage(String sLanguage) {
 		return (sLanguage.equals("spanish")) ? COLUMN_SPANISH : COLUMN_ENGLISH;
 	}
 
 	/**
 	 * Starts a thread to load the database table with idioms
 	 */
 	private void loadDictionary() {
 
 		new AsyncTask<Integer, Integer, Boolean>() {
 			ProgressDialog progressDialog;
 
 			@Override
 			protected void onPreExecute() {
 				progressDialog = ProgressDialog.show(context,
 				    context.getResources().getString(R.string.lbl_installing_database), context
 				        .getResources().getString(R.string.msg_dialog_installing_database));
 			}
 
 			@Override
 			protected Boolean doInBackground(Integer... params) {
 				if (params == null) {
 					return false;
 				}
 				try {
 					Thread.sleep(params[0]);
 					loadIdioms();
 				} catch (Exception e) {
 					Log.e(TAG, e.getMessage());
 					return false;
 				}
 
 				return true;
 			}
 
 			@Override
 			protected void onPostExecute(Boolean result) {
 				progressDialog.dismiss();
 
 				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
 				dialogBuilder.setTitle(android.R.string.dialog_alert_title);
 				if (result) {
 					dialogBuilder.setMessage(context.getString(R.string.msg_database_installation_completed));
 				} else {
 					dialogBuilder.setMessage(R.string.msg_database_installation_error);
 				}
 				dialogBuilder.setPositiveButton(context.getString(android.R.string.ok),
 				    new DialogInterface.OnClickListener() {
 					    @Override
 					    public void onClick(DialogInterface dlg, int arg1) {
 						    dlg.dismiss();
 					    }
 				    });
 				dialogBuilder.create().show();
 			}
 		}.execute(2000);
 
 		dictionary.setNumIdioms();
 
 	}
 
 	private void loadIdioms() throws IOException {
 		Log.d(TAG, "Loading idioms...");
 		final Resources resources = context.getResources();
 		InputStream inputStream = resources.openRawResource(R.raw.tib);
 		BufferedReader reader;
 
 		try {
 			inputStream = resources.openRawResource(R.raw.tib);
 			JSONParser parser = new JSONParser();
 			reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
 			Object obj = parser.parse(reader);
 			JSONArray JSONRoot = (JSONArray) obj;
 			Iterator<?> iterator = JSONRoot.iterator();
 			while (iterator.hasNext()) {
 				JSONObject JSONIdiom = (JSONObject) iterator.next();
 				String id = (String) JSONIdiom.get("nid");
 				String spanish = (String) JSONIdiom.get("spanish");
 				String english = (String) JSONIdiom.get("english");
 				String spanish_description = (String) JSONIdiom.get("spanish_description");
 				String english_description = (String) JSONIdiom.get("english_description");
 				String examples = (String) JSONIdiom.get("examples");
 				if (addIdiom(id, spanish, english, spanish_description, english_description, examples) < 0)
 					Log.e(TAG, "Error adding idiom: " + id + " " + spanish);
 				else
 					Log.d(TAG, "Idiom added: " + id + " " + spanish);
 			}
 
 		} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			inputStream.close();
 		}
 
 		Log.d(TAG, "DONE loading idioms.");
 	}
 
 	/**
 	 * Add a word to the dictionary.
 	 * 
 	 * @return rowId or -1 if failed
 	 */
 	private long addIdiom(String idiomID, String spanish, String english, String spanish_description,
 	    String english_description, String examples) {
 		ContentValues initialValues = new ContentValues();
 		// initialValues.put(COLUMN_ID, idiomID);
 		initialValues.put(COLUMN_SPANISH, spanish);
 		initialValues.put(COLUMN_ENGLISH, english);
 		initialValues.put(COLUMN_SPANISH_DESCRIPTION, spanish_description);
 		initialValues.put(COLUMN_ENGLISH_DESCRIPTION, english_description);
 		initialValues.put(COLUMN_EXAMPLES, examples);
 
 		return mDatabase.insert(DICTIONARY_TABLE, null, initialValues);
 	}
 
 	/**
 	 * Private class to create/open the database.
 	 */
 	private static class DictionaryOpenHelper extends SQLiteOpenHelper {
 
 		private SQLiteDatabase mDatabase;
 
 		private static final String TABLE_CREATE = "CREATE TABLE " + DICTIONARY_TABLE
 		    + " ('_id' INTEGER PRIMARY KEY  NOT NULL , " + COLUMN_SPANISH + " VARCHAR NOT NULL , "
 		    + COLUMN_ENGLISH + " VARCHAR NOT NULL , " + COLUMN_SPANISH_DESCRIPTION + " VARCHAR(1000), "
 		    + COLUMN_ENGLISH_DESCRIPTION + " VARCHAR(1000), " + COLUMN_EXAMPLES + " VARCHAR(1000))";
 
 		DictionaryOpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			mDatabase = db;
 			mDatabase.execSQL(TABLE_CREATE);
 			dictionary.loadDictionary();
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
 			    + ", which will destroy all old data");
 			db.execSQL("DROP TABLE IF EXISTS " + DICTIONARY_TABLE);
 			onCreate(db);
 		}
 
 	}
 
 }
