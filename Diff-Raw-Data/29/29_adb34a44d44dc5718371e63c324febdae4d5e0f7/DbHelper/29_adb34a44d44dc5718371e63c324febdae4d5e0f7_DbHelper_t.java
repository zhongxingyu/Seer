 package com.quizz.core.db;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Locale;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DbHelper extends SQLiteOpenHelper {
 
 	// Database Version
 	private static final int DATABASE_VERSION = 1;
 
 	// TABLES
 	public static final String TABLE_SECTIONS = "sections";
 	public static final String TABLE_LEVELS = "levels";
 	public static final String TABLE_HINTS = "hints";
 	public static final String TABLE_USERDATA = "user_data";
 
 	// COLUMNS
 	public static final String COLUMN_ID = "id";
 	public static final String COLUMN_UNLOCKED = "unlocked";
 	public static final String COLUMN_NUMBER = "number";
 	public static final String COLUMN_LEVEL = "level";
 	public static final String COLUMN_IMAGE = "image";
 	public static final String COLUMN_INDICATION = "indication";
 	public static final String COLUMN_DIFFICULTY = "difficulty";
 	public static final String COLUMN_LINK = "link";
 	public static final String COLUMN_HINT = "hint";
 	public static final String COLUMN_HINT_TYPE = "hint_type";
 	public static final String COLUMN_RESPONSE = "response";
	public static final String COLUMN_COPYRIGHT = "copyright";
 	public static final String COLUMN_PARTIAL_RESPONSE = "partial_response";
 	public static final String COLUMN_FK_SECTION = "fk_section_id";
 	public static final String COLUMN_FK_LEVEL = "fk_level_id";
 
 	public static final String COLUMN_STATUS = "status";
 	public static final String COLUMN_REF_FROM_TABLE = "ref_from_table";
 	public static final String COLUMN_REF = "ref";
 	
 	private static final int LANG_FR = 0;
 	private static final int LANG_EN = 1;
 	
 	private String mGamedataDBName;	
 	private String mUserdataDBName;
 
 	public String mAppName;
 	
 	private int mDBLang;
 	
 	private Context mContext;
 	
 	private String mGamedataDBFullPath;
 	private String mUserdataDBFullPath;
 	
 	private boolean mUserdataDBExists;
 	private boolean mGamedataDBExists;
 	
 	private boolean mIsDbHelperInitDone = false;
 	
 	public DbHelper(Context ctx) {
 		super(ctx,
 				ctx.getPackageName().substring(
 						ctx.getPackageName().lastIndexOf('.') + 1)
 				  + "_userdata" + ".db",
 				null,
 				DATABASE_VERSION);
 
 		String gamedataDirectoryPath = android.os.Environment.getExternalStorageDirectory().toString()
 				+ "/WorldTourQuiz/gamedata/";
 		
 		mContext = ctx;
 		mAppName = ctx.getPackageName().substring(ctx.getPackageName().lastIndexOf('.') + 1);
 
 		mUserdataDBName = mAppName + "_userdata.db";
 		mGamedataDBName = mAppName + ".db";
 		
 		mDBLang = (Locale.getDefault().getLanguage().equalsIgnoreCase("fr")) ? LANG_FR : LANG_EN;
 		
 		mUserdataDBExists = ctx.getDatabasePath(mUserdataDBName).exists();
 		mGamedataDBExists = (new File(gamedataDirectoryPath + mGamedataDBName)).exists();
 		
 		mUserdataDBFullPath = super.getReadableDatabase().getPath();
 		mGamedataDBFullPath = gamedataDirectoryPath + mGamedataDBName;
 		this.createGamedataDirOnSDCard(gamedataDirectoryPath);
 		
 		mIsDbHelperInitDone = true;
 	}
 	
 	private void createGamedataDirOnSDCard(String gamedataDirectoryPath)
 	{
 		File dbFile = new File(mGamedataDBFullPath);
 		if (!dbFile.exists()) {
 			dbFile.getParentFile().mkdirs();
 			try {
 				dbFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void createDatabases() throws IOException
 	{
 		try {
 			if (!mUserdataDBExists)
 			{
 				String assetDBFilename = mAppName + "_userdata.db";
 				this.copyDatabase(assetDBFilename, mUserdataDBFullPath);
 				mUserdataDBExists = true;
 			}
 			if (!mGamedataDBExists)
 			{
 				String assetDBFilename = mAppName + ((mDBLang == LANG_FR) ? "_fr" : "_en") + ".db";
 				this.copyDatabase(assetDBFilename, mGamedataDBFullPath);
 				mGamedataDBExists = true;
 			}
 		} catch (IOException e) {
 			throw new Error("Error copying database");
 		}
 	}
 
 	public SQLiteDatabase getReadableDatabase() {
 		if (this.mIsDbHelperInitDone)
 			return SQLiteDatabase.openDatabase(mGamedataDBFullPath, null, SQLiteDatabase.OPEN_READONLY);
 		return super.getReadableDatabase();
 	}
 	
 	public SQLiteDatabase getReadableUserdataDatabase() {
 		return super.getReadableDatabase();
 	}
 	
 	public SQLiteDatabase getWritableUserdataDatabase() {
 		return super.getWritableDatabase();
 	}
 	
 	public SQLiteDatabase getWritableDatabase() {
 		if (this.mIsDbHelperInitDone)
 			return SQLiteDatabase.openDatabase(mGamedataDBFullPath, null, SQLiteDatabase.OPEN_READWRITE);
 		return super.getWritableDatabase();
 	}  
 	
 	private void copyDatabase(String assetDBFilename, String dbFullPath) throws IOException {
 
 		int length;
 		byte[] buffer = new byte[1024];
 		InputStream input = mContext.getAssets().open("databases/" + assetDBFilename);
 		String outputFilename = dbFullPath;
 		
 		try {
 			OutputStream output = new FileOutputStream(outputFilename);
 			while ((length = input.read(buffer)) > 0)
 				output.write(buffer, 0, length);
 			output.flush();
 			output.close();
 			input.close();
 		} catch (FileNotFoundException e) {
 			Log.d("ERROR COPYING DB", "FILE NOT FOUND EXCEPTION : " + outputFilename);
 		}
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 	}
 
 	// Upgrading database
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 
 	public String getGamedataDBName() {
 		return mGamedataDBName;
 	}
 
 	public String getUserdataDBName() {
 		return mUserdataDBName;
 	}
 
 	public String getGamedataDBFullPath() {
 		return mGamedataDBFullPath;
 	}
 
 	public String getUserdataDBFullPath() {
 		return mUserdataDBFullPath;
 	}
 
 }
