 
 package net.jnwd.origamiData;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.jnwd.origamiFinder.R;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class ModelTable {
     private static final String Tag = "ModelDatabase";
 
     private static final String KEY_ROWID = "_id";
 
     public static final String[] allColumns = {
             KEY_ROWID,
             Model.COL_MODEL_NAME,
             Model.COL_MODEL_TYPE,
             Model.COL_CREATOR,
             Model.COL_BOOK_TITLE,
             Model.COL_ISBN,
             Model.COL_ON_PAGE,
             Model.COL_DIFFICULTY,
             Model.COL_PAPER,
             Model.COL_PIECES,
             Model.COL_GLUE,
             Model.COL_CUTS
     };
 
     public static final String[] listColumns = {
             KEY_ROWID,
             Model.COL_MODEL_NAME,
             Model.COL_CREATOR,
             Model.COL_BOOK_TITLE,
             Model.COL_DIFFICULTY
     };
 
     private static final String DATABASE_NAME = "MODELS";
     private static final String FTS_VIRTUAL_TABLE = "FTS";
     private static final int DATABASE_VERSION = 3;
 
     private final Context mContext;
 
     private DatabaseOpenHelper mDatabaseOpenHelper;
     private SQLiteDatabase mDb;
 
     public ModelTable(Context context) {
         super();
 
         mContext = context;
     }
 
     public ModelTable open() throws SQLException {
         Log.i(Tag, "Inside the open routine of the database handler...");
 
         Log.i(Tag, "Establishing the connection to the database...");
 
         mDatabaseOpenHelper = new DatabaseOpenHelper(mContext);
 
         Log.i(Tag, "Getting a writeable database instance...");
 
         mDb = mDatabaseOpenHelper.getWritableDatabase();
 
         Log.i(Tag, "Checking the database..." + (mDb == null ? "Null!!!!" : "Database Okay!"));
 
         return this;
     }
 
     public void close() {
         if (mDatabaseOpenHelper != null) {
             mDatabaseOpenHelper.close();
         }
     }
 
     public Cursor fetchAllModels() {
         Cursor mCursor = mDb.query(FTS_VIRTUAL_TABLE, listColumns, null, null, null, null, null);
 
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
 
         return mCursor;
     }
 
     public Cursor getModelMatches(String query) {
         return getModelMatches(query, listColumns);
     }
 
     public Cursor getModelMatches(String query, String[] columns) {
         String selection = Model.COL_MODEL_NAME + " MATCH ?";
 
         String[] selectionArgs = new String[] {
                 query + "*"
         };
 
         return query(selection, selectionArgs, columns);
     }
 
     private Cursor query(String selection, String[] selectionArgs, String[] columns) {
         SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
 
         builder.setTables(FTS_VIRTUAL_TABLE);
 
         Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns,
                 selection, selectionArgs, null, null, Model.COL_MODEL_NAME);
 
         if (cursor == null) {
             return null;
         } else if (!cursor.moveToFirst()) {
             cursor.close();
 
             return null;
         }
 
         return cursor;
     }
 
     private static class DatabaseOpenHelper extends SQLiteOpenHelper {
         private final String Tag = "DatabaseOpenHelper";
 
         private static String DB_Path = "";
        private static String DB_Name = FTS_VIRTUAL_TABLE;
 
         private final Context mHelperContext;
         private SQLiteDatabase mDatabase;
 
         private static final String FTS_TABLE_CREATE = "" +
                 "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE + " " +
                 "USING fts3 (" +
                 KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                 Model.COL_MODEL_NAME + ", " +
                 Model.COL_MODEL_TYPE + ", " +
                 Model.COL_CREATOR + ", " +
                 Model.COL_BOOK_TITLE + ", " +
                 Model.COL_ISBN + ", " +
                 Model.COL_ON_PAGE + ", " +
                 Model.COL_DIFFICULTY + ", " +
                 Model.COL_PAPER + ", " +
                 Model.COL_PIECES + ", " +
                 Model.COL_GLUE + ", " +
                 Model.COL_CUTS + ")";
 
         DatabaseOpenHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
 
             Log.i(Tag, "Trying to create the database instance...");
 
            DB_Path = context.getFilesDir().getPath() + "/../databases";
 
             mHelperContext = context;
 
             try {
                 createDataBase();
             } catch (IOException ioe) {
                 Toast.makeText(mHelperContext,
                         "Unable to create database. Please contact App Creator.",
                         Toast.LENGTH_LONG).show();
 
                 Log.e(Tag, "Exception copying the database: " + ioe.getMessage());
             }
         }
 
         private void createDataBase() throws IOException {
             if (checkDataBase()) {
                 return;
             }
 
             this.getReadableDatabase();
             this.close();
 
             try {
                 copyDataBase();
 
                 Log.e(Tag, "createDatabase: Database created!");
             } catch (IOException mIOException) {
                 throw new Error("ErrorCopyingDataBase");
             }
         }
 
         private boolean checkDataBase() {
            File dbFile = new File(DB_Path + DB_Name);
 
             return dbFile.exists();
         }
 
         private void copyDataBase() throws IOException {
             InputStream mInput = mHelperContext.getAssets().open(DB_Name);
 
             String outFileName = DB_Path + DB_Name;
 
             OutputStream mOutput = new FileOutputStream(outFileName);
 
             byte[] mBuffer = new byte[1024];
             int mLength;
 
             while ((mLength = mInput.read(mBuffer)) > 0) {
                 mOutput.write(mBuffer, 0, mLength);
             }
 
             mOutput.flush();
             mOutput.close();
             mInput.close();
         }
 
         @Override
         public synchronized void close() {
             if (mDatabase != null) {
                 mDatabase.close();
             }
 
             super.close();
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             boolean bypass = true;
 
             if (bypass) {
                 return;
             }
 
             Log.i(Tag, "About to perform the onCreate method...");
 
             Log.i(Tag, "The database instance coming in is: " + db);
 
             mDatabase = db;
 
             Log.i(Tag, "Executing the create table script...");
 
             Log.i(Tag, "Command: " + FTS_TABLE_CREATE);
 
             mDatabase.execSQL(FTS_TABLE_CREATE);
 
             Log.i(Tag, "Load the data into the database...");
 
             loadDatabase();
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             boolean bypass = true;
 
             if (bypass) {
                 return;
             }
 
             Log.w(Tag, "Upgrading database from version " + oldVersion + " to " + newVersion
                     + ", which will destroy all old data");
 
             db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
 
             Log.i(Tag, "Now...recreate the database...");
 
             onCreate(db);
         }
 
         private void loadDatabase() {
             Log.i(Tag, "Start the load database thread...");
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         loadModels();
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 }
             }).start();
         }
 
         private void loadModels() throws IOException {
             Log.i(Tag, "Open up the raw data file...");
             Log.i(Tag, "Open a reference to the app resources...");
 
             final Resources resources = mHelperContext.getResources();
 
             Log.i(Tag, "Open the raw data file...Get an inputStream...");
 
             InputStream inputStream = resources.openRawResource(R.raw.model);
 
             Log.i(Tag, "Set up a buffered reader...");
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 
             try {
                 String line;
 
                 List<Model> allModels = new ArrayList<Model>();
 
                 Log.i(Tag, "Spin through the file...Only grab one copy of each model!");
 
                 while ((line = reader.readLine()) != null) {
                     Model model = new Model(line);
 
                     if (!allModels.contains(model)) {
                         allModels.add(model);
 
                         long id = addModel(model);
 
                         if (id < 0) {
                             Log.e(Tag, "unable to add model: " + model.toString());
                         }
                     }
                 }
 
                 Log.i(Tag, "Finished building model list...");
             } finally {
                 reader.close();
             }
 
             Log.i(Tag, "Finished loading the raw file into the database...");
         }
 
         public long addModel(Model model) {
             ContentValues initialValues = new ContentValues();
 
             initialValues.put("_id", model.id);
             initialValues.put(Model.COL_MODEL_NAME, model.name);
             initialValues.put(Model.COL_MODEL_TYPE, model.modelType);
             initialValues.put(Model.COL_CREATOR, model.creator);
             initialValues.put(Model.COL_BOOK_TITLE, model.bookTitle);
             initialValues.put(Model.COL_ISBN, model.ISBN);
             initialValues.put(Model.COL_ON_PAGE, model.page);
             initialValues.put(Model.COL_DIFFICULTY, model.difficulty);
             initialValues.put(Model.COL_PAPER, model.paper);
             initialValues.put(Model.COL_PIECES, model.pieces);
             initialValues.put(Model.COL_GLUE, model.glue);
             initialValues.put(Model.COL_CUTS, model.cuts);
 
             return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
         }
     }
 }
