 package org.fr.ykatchou.paillardes;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	// The Android's default system path of your application database.
 	private static String DB_PATH = "/data/data/org.fr.ykatchou.paillardes/databases/";
 	private static String DB_NAME = "paillardes.db";
 	private SQLiteDatabase myDatabase;
 	private final Context myContext;
 
 	public DatabaseHelper(Context context) {
 		super(context, DB_NAME, null, 1);
 		this.myContext = context;
 	}
 
 	@Override
 	public synchronized void close() {
 		if (myDatabase != null)
 			myDatabase.close();
 		super.close();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 
 	public void createDataBase() throws IOException {
 		boolean dbExist = checkDatabase();
 		if (dbExist) {
 			// do nothing - database already exist
 		} else {
 
 			// By calling this method and empty database will be created into
 			// the default system path
 			// of your application so we are gonna be able to overwrite that
 			// database with our database.
 			this.getReadableDatabase();
 			try {
 
 				copyDataBase();
 
 			} catch (IOException e) {
 				throw new Error("Error copying database");
 			}
 		}
 	}
 
 	private boolean checkDatabase() {
 		SQLiteDatabase checkDB = null;
 		try {
 			String myPath = DB_PATH + DB_NAME;
 			checkDB = SQLiteDatabase.openDatabase(myPath, null,
 					SQLiteDatabase.OPEN_READONLY);
 		} catch (SQLiteException e) {
 
 			// database does't exist yet.
 		}
 		if (checkDB != null) {
 			checkDB.close();
 		}
 		return checkDB != null ? true : false;
 	}
 
 	private void copyDataBase() throws IOException {
 		// Open your local db as the input stream
 		InputStream myInput = myContext.getAssets().open(DB_NAME);
 		// Path to the just created empty db
 		String outFileName = DB_PATH + DB_NAME;
 		// Open the empty db as the output stream
 		OutputStream myOutput = new FileOutputStream(outFileName);
 		// transfer bytes from the inputfile to the outputfile
 		byte[] buffer = new byte[1024];
 		int length;
 		while ((length = myInput.read(buffer)) > 0) {
 			myOutput.write(buffer, 0, length);
 		}
 		// Close the streams
 		myOutput.flush();
 		myOutput.close();
 		myInput.close();
 	}
 
 	public void openDataBase() throws SQLException {
 		// Open the database
 		String myPath = DB_PATH + DB_NAME;
 		myDatabase = SQLiteDatabase.openDatabase(myPath, null,
 				SQLiteDatabase.OPEN_READONLY);
 	}
 
 	public void init() throws IOException {
 		createDataBase();
 		openDataBase();
 	}
 
 	public List<Chanson> getTitres() {
 		List<Chanson> data = new LinkedList<Chanson>();
 
 		String allTitresQuery = "select ch.id, ch.titre from chanson ch order by ch.titre";
 		Cursor d = myDatabase.rawQuery(allTitresQuery, null);
 
		while (d.moveToNext()) {
			Chanson c = new Chanson(d.getLong(0), d.getString(1));
			data.add(c);
 		}
 		return data;
 	}
 }
