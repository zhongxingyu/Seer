 package se.alexanderblom.apk;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.content.Context;
 import android.database.DatabaseErrorHandler;
 import android.database.DefaultDatabaseErrorHandler;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteException;
 import android.util.Log;
 
 import com.google.common.io.Files;
 import com.google.common.io.InputSupplier;
 
 public class AssetDBOpenHelper {
 	private static final String TAG = "AssetDBOpenHelper";
 	
 	private Context context;
 	
 	private String databaseName;
 	private String databasePath;
 	private CursorFactory factory;
	//private DatabaseErrorHandler errorHandler;
 	private int version;
 	
 	
 	private SQLiteDatabase database;

 	public AssetDBOpenHelper(Context context, String databaseName, String databasePath, CursorFactory factory,
			int version) {
			//, DatabaseErrorHandler errorHandler) {
 		this.context = context;
 		this.databaseName = databaseName;
 		this.databasePath = databasePath;
 		this.factory = factory;
		//this.errorHandler = errorHandler;
 		this.version = version;
 	}
 	
 	public synchronized SQLiteDatabase getWritableDatabase() throws SQLException {
 		if (database != null) {
 			if (!database.isOpen()) {
 				// Someone closed the database
 				database = null;
 			} else if (!database.isReadOnly()) {
 				return database;
 			}
 		}
 		
 		
 		if (!context.getDatabasePath(databaseName).exists()) {
 			// Database does not exist, copy it
 			try {
 				database = copyDatabase();
 			} catch (IOException e) {
 				Log.e(TAG, "Database could not be copied", e);
 				
 				throw new SQLiteException("Database could not be copied");
 			}
 		} else {
 			database = openDatabase();
 		}
 		
 		int version = database.getVersion();
 		if (this.version != version) {
 			// TODO: Handle upgrade
 		}
 		
 		return database;
 	}
 	
 	public synchronized SQLiteDatabase getReadableDatabase() throws SQLException {
 		return getWritableDatabase();
 	}
 	
 	private SQLiteDatabase openDatabase() {
		//return context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, factory, errorHandler);
		return context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, factory);
 	}
 	
 	private SQLiteDatabase copyDatabase() throws IOException {
 		Log.d(TAG, "Copying database: " + databaseName);
 		
 		InputStream in = context.getAssets().open(databasePath);
 		File out = context.getDatabasePath(databaseName);
 		
 		// Ensure directory exist
 		out.getParentFile().mkdirs();
 		
 		Files.copy(new Supplier(in), out);
 		
 		SQLiteDatabase db = openDatabase();
 		db.setVersion(version);
 		
 		// This is not need after api 4
 		//db.execSQL("CREATE TABLE \"android_metadata\" (\"locale\" TEXT DEFAULT 'en_US')");
 		//b.execSQL("INSERT INTO \"android_metadata\" VALUES ('en_US')");
 		
 		return db;
 	}
 	
 	private static class Supplier implements InputSupplier<InputStream> {
 		private InputStream is;
 		
 		public Supplier(InputStream is) {
 			this.is = is;
 		}
 
 		@Override
 		public InputStream getInput() throws IOException {
 			return is;
 		}
 		
 	}
 }
