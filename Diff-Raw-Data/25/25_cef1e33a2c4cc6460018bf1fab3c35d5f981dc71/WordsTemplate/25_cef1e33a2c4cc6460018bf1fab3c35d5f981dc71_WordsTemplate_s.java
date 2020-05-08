 package edu.rit.se.agile.data;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import edu.rit.se.agile.randominsultapp.GenericActivity;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class WordsTemplate extends SQLiteOpenHelper {
 
 	public static final String TABLE_NAME = "words";
 	public static final String COLUMN_ID = "_id";
 	public static final String COLUMN_WORD_TYPE = "word_type";
 	public static final String COLUMN_WORD = "word";
 	public static final String COLUMN_CATEGORY = "category";
 
 	private static final String DATABASE_NAME = "words.db";
 	private static final int DATABASE_VERSION = 1;
 
 	private static boolean tableInitialized = true;
 
 	// Database creation sql statement
 	private static final String DATABASE_CREATE = "create table "
 			+ TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, " 
 			+ COLUMN_WORD_TYPE + " text not null, "
 			+ COLUMN_CATEGORY + " text not null, "
 			+ COLUMN_WORD + " text not null);";
 
 	//The seperator string for the csv file to seperate entries.
 	private static final String SEPERATOR = ",";
 
 	/*
 	 * The filename for the words csv file.
 	 * 
 	 * Format of the file needs to one entry per line. each entry is as follows
 	 * 
 	 * id , word_type , category , word
 	 * 
 	 * 
 	 */
 	private static final String IMPORT_FILE_NAME = "words.csv";	
 	private Context ctx;
 
 	public WordsTemplate(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		ctx = context;
 	}
 
 	public boolean getDatabaseInitialized() {
 		return tableInitialized;
 	}
 
 	public void initializeDatabase() {
 		InputStream iStream = null;
 		try {
 			AssetManager am = ctx.getAssets();
 			iStream = am.open(IMPORT_FILE_NAME);
 			BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
 
 			String content;
 			while((content = bReader.readLine()) != null) {
 				Log.println(Log.VERBOSE, "Testing", "" + content);
 				String[] splitStr = content.split(SEPERATOR) ;
				GenericActivity.wordDAO.createWord(splitStr[1], splitStr[2], splitStr[3]);
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if(iStream != null) {
 				try {
 					iStream.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		tableInitialized = true;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(DATABASE_CREATE);
 		tableInitialized = false;
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 	}
 
 
 
 }
