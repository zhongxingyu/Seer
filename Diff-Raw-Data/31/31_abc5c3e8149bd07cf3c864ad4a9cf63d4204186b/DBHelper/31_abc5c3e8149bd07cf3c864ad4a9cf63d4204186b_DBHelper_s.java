 package com.shvelo.guesslogo;
 
 import java.io.BufferedReader;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.Gson;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DBHelper extends SQLiteOpenHelper {
 
 	private SQLiteDatabase database;
 
 	private final Context context;
 
 	private static final String DATABASE_NAME = "brands.db3";
 	private static final int DATABASE_VERSION = 5;
 
 	/**
 	 * Constructor Takes and keeps a reference of the passed context in order to
 	 * access to the application assets and resources.
 	 * 
 	 * @param context
 	 */
 	public DBHelper(Context context) {
 
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 		
		database = getReadableDatabase();
 		if(checkJSON()) {
 			update(database, null, null);
 		}
 	}
 	
 	/**
 	 * Close the database
 	 */
 	@Override
 	public synchronized void close() {
 		if (database != null)
 			database.close();
 		
 		super.close();
 	}
 	
 	/**
 	 * Get the database
 	 * @return SQLiteDatabase
 	 */
 	public SQLiteDatabase get() {
 		return database;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		
 		database = db;
 		execSQLFile("schema.sql", db);
 		
 		loadDB(db);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		update(db, oldVersion, newVersion);
 	}
 	
 	public boolean checkJSON() {
 		try {
 			DigestInputStream is;
 			InputStream inputStream;
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			inputStream = context.getAssets().open("brands.json");
 			is = new DigestInputStream(inputStream, md);
			String digest = new String(md.digest());
 			is.close();
 			
 			Cursor cursor = database.rawQuery("SELECT value FROM userdata WHERE name='json_checksum'", null);
 			cursor.moveToFirst();
 			
			if(digest == cursor.getString(0)) {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public void update(SQLiteDatabase db, Integer oldVersion, Integer newVersion) {
 		if(oldVersion == null) oldVersion = DATABASE_VERSION;
 		if(newVersion == null) newVersion = DATABASE_VERSION;
 				
 		List<Integer> guessed = new ArrayList<Integer>();
 		
 		Cursor query = db.query("brands", null, null, null, null, null, "name asc", null);
 		for(int i = 0; i < query.getCount(); i++) {
         	query.moveToNext();
         	if(query.getInt(query.getColumnIndex("guessed")) == 1)
         		guessed.add(query.getInt(query.getColumnIndex("_id")));
 		}
 		
 		int score = 0;
 		
 		if(oldVersion >= 4) {
 			Cursor cursor = db.rawQuery("SELECT value FROM userdata WHERE name='score'", null);
 			cursor.moveToFirst();
 			score = cursor.getInt(0);
 		}
 		
 		execSQLFile("schema_drop.sql", db);
 		execSQLFile("schema.sql", db);
 		
 		loadDB(db);
 		
 		db.beginTransaction();
 		
 		for(int i = 0; i < guessed.size(); i++) {
 			db.execSQL("UPDATE brands SET guessed=1 WHERE _id=?", new String[] {
 					guessed.get(i).toString()
 			});
 		}
 		if(oldVersion >= 4 && score > 0) {
 			db.rawQuery("UPDATE userdata SET value=? WHERE name='score'", new String[]{
 				String.valueOf(score)
 			});
 		}
 		
 		db.setTransactionSuccessful();
 		db.endTransaction();
 	}
 	
 	private void loadDB(SQLiteDatabase db) {
 		try {
 			loadLogos(db);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void loadLogos(SQLiteDatabase db) throws IOException {
 		InputStream inputStream = context.getAssets().open("brands.json");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 
 		String line;
 		
 		StringBuilder jsonBuilder = new StringBuilder();
 		
 		while ((line = reader.readLine()) != null) {
 			jsonBuilder.append(line);
 		}
 		
 		Gson gson = new Gson();
 		
 		BrandEntry[] brands = gson.fromJson(jsonBuilder.toString(), BrandEntry[].class);
 
 		db.beginTransaction();
 		try {
 			for(int i = 0; i < brands.length; i++) {
 				BrandEntry brandEntry = brands[i];
 				db.execSQL(
 						"INSERT INTO brands(name,logo,variant1,variant2,variant3,variant4,correct,guessed) values(?,?,?,?,?,?,?,0);"
 						, new String[]{
 							brandEntry.name, //name
 							"@drawable/" + brandEntry.logo, //logo
 							brandEntry.variants[0], brandEntry.variants[1], brandEntry.variants[2] ,brandEntry.variants[3], //variants
 							String.valueOf(brandEntry.correct) //correct answer
 						});
 			}
 			db.setTransactionSuccessful();
 		} finally {
 			reader.close();
 			db.endTransaction();
 		}
 	}
 	
 	private String readFile(String name) throws IOException {
 		InputStream inputStream = context.getAssets().open(name);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 		String line;
 		StringBuilder builder = new StringBuilder();
 		
 		while ((line = reader.readLine()) != null) {
 			builder.append(line);
 		}
 		
 		return builder.toString();
 	}
 	
 	private void execSQLFile(String name, SQLiteDatabase db) {
 		try {
 			String sql = readFile(name);
 			
 			String[] lines = sql.split("(?!\".*?;.*?\")(?!'.*?;.*?');");
 			for(int i = 0; i < lines.length; i++) {
 				Log.d("guesslogo", lines[i]);
 				String statement = lines[i];
 				statement.replaceAll("[\\s\\n\\t ]","");
				if(!statement.isEmpty())
 					db.execSQL(statement);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
