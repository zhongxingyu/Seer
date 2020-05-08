 package com.alex.recipemanager.provider;
 
 import java.util.HashMap;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.util.Log;
 
 import com.alex.recipemanager.provider.RecipeContent.CaseHistoryColumn;
 import com.alex.recipemanager.provider.RecipeContent.MedicineColumn;
 import com.alex.recipemanager.provider.RecipeContent.MedicineNameColumn;
 import com.alex.recipemanager.provider.RecipeContent.NationColumn;
 import com.alex.recipemanager.provider.RecipeContent.PatientColumns;
 import com.alex.recipemanager.provider.RecipeContent.RecipeColumn;
 import com.alex.recipemanager.provider.RecipeContent.RecipeMedicineColumn;
 
 public class RecipeProvider extends ContentProvider {
     private static final String TAG = "RecipeProvider";
 
     static final String DATABASE_NAME = "RecipeManager.db";
 
    public static final int DATABASE_VERSION = 29;
 
     private static final String REFERENCE_PATIENT_ID_AS_FOREIGN_KEY =
             "references " + PatientColumns.TABLE_NAME
             + " ( " + PatientColumns._ID + " )";
 
     private static final String REFERENCE_MEDICINE_ID_AS_FOREIGN_KEY =
             "references " + MedicineColumn.TABLE_NAME
             + " ( " + MedicineColumn._ID + " )";
 
     private static final String REFERENCE_CASE_HISTORY_ID_AS_FOREIGN_KEY =
             "references "
             + CaseHistoryColumn.TABLE_NAME
             + " ( "
             + CaseHistoryColumn._ID
             + " )";
 
     private static final String REFERENCE_RECIPE_ID_AS_FOREIGN_KEY =
             "references "
            + RecipeColumn.TABLE_NAME
             + " ( "
            + RecipeColumn._ID + " )";
 
     private static HashMap<String, String> sMedicineJoinAliasProjectionMap;
     private static HashMap<String, String> sRecipeMedicineJoinMedicineNameMap;
 
     private static final int BASE_SHIFT = 12;
     private static final int PATIENT_BASE = 0;
     private static final int PATIENT = PATIENT_BASE;
     private static final int PATIENT_ID = PATIENT_BASE + 1;
 
     private static final int CASE_HISTORY_BASE = PATIENT_BASE + 0x1000;
     private static final int CASE_HISTORY = CASE_HISTORY_BASE;
     private static final int CASE_HISTORY_ID = CASE_HISTORY_BASE + 1;
 
     private static final int MEDICINE_BASE = CASE_HISTORY_BASE + 0x1000;
     private static final int MEDICINE = MEDICINE_BASE;
     private static final int MEDICINE_ID = MEDICINE_BASE + 1;
 
     private static final int RECIPE_BASE = MEDICINE_BASE + 0x1000;
     private static final int RECIPE = RECIPE_BASE;
     private static final int RECIPE_ID = RECIPE_BASE + 1;
 
     private static final int RECIPE_MEDICINE_BASE = RECIPE_BASE + 0x1000;
     private static final int RECIPE_MEDICINE = RECIPE_MEDICINE_BASE;
     private static final int RECIPE_MEDICINE_ID = RECIPE_MEDICINE_BASE + 1;
 
     private static final int MEDICINE_NAME_BASE = RECIPE_MEDICINE_BASE + 0x1000;
     private static final int MEDICINE_NAME = MEDICINE_NAME_BASE;
     private static final int MEDICINE_NAME_ID = MEDICINE_NAME_BASE + 1;
     private static final int MEDICINE_NAME_MEDICINE = MEDICINE_NAME_BASE + 2;
     private static final int MEDICINE_NAME_MEDICINE_ID = MEDICINE_NAME_BASE + 3;
 
     private static final int NATION_BASE = MEDICINE_NAME_BASE + 0x1000;
     private static final int NATION = NATION_BASE;
 
     private static final UriMatcher sURIMatcher = new UriMatcher(
             UriMatcher.NO_MATCH);
 
     private static final String TABLE_MEDICINE_JOINED_ALIAS_QUERY =
             MedicineNameColumn.TABLE_NAME
             + " left join "
             + MedicineColumn.TABLE_NAME
             + " on "
             + MedicineColumn.TABLE_NAME
             + "."
             + MedicineColumn._ID
             + "="
             + MedicineNameColumn.TABLE_NAME
             + "."
             + MedicineNameColumn.MEDICINE_KEY;
 
     private static final String TABLE_RECIPE_JOINED_MEDICINE_QUERY =
             MedicineNameColumn.TABLE_NAME
                 + " left join "
                 + RecipeMedicineColumn.TABLE_NAME
                 + " on "
                 + RecipeMedicineColumn.TABLE_NAME
                 + "."
                 + RecipeMedicineColumn.MEDICINE_KEY
                 + "="
                 + MedicineNameColumn.TABLE_NAME
                 + "."
                 + MedicineNameColumn.MEDICINE_KEY;
 
     static {
         // Email URI matching table
         UriMatcher matcher = sURIMatcher;
         // All patient
         matcher.addURI(RecipeContent.AUTHORITY, "patient", PATIENT);
         // A special patient
         matcher.addURI(RecipeContent.AUTHORITY, "patient/#", PATIENT_ID);
         // All case history
         matcher.addURI(RecipeContent.AUTHORITY, "case_history", CASE_HISTORY);
         // A special history
         matcher.addURI(RecipeContent.AUTHORITY, "case_history/#",
                 CASE_HISTORY_ID);
         // All medicine
         matcher.addURI(RecipeContent.AUTHORITY, "medicine", MEDICINE);
         // A special medicine
         matcher.addURI(RecipeContent.AUTHORITY, "medicine/#", MEDICINE_ID);
         // All medicine alias
         matcher.addURI(RecipeContent.AUTHORITY, "medicine_name", MEDICINE_NAME);
         // A special medicine alias
         matcher.addURI(RecipeContent.AUTHORITY, "medicine_name/#", MEDICINE_NAME_ID);
         // All medicine and all it's names
         matcher.addURI(RecipeContent.AUTHORITY, "medicine_alias/medicine",
                 MEDICINE_NAME_MEDICINE);
         // A special medicine and all it's names
         matcher.addURI(RecipeContent.AUTHORITY, "medicine_alias/medicine/#",
                 MEDICINE_NAME_MEDICINE_ID);
         // All recipe
         matcher.addURI(RecipeContent.AUTHORITY, "recipe", RECIPE);
         // A special recipe
         matcher.addURI(RecipeContent.AUTHORITY, "recipe/#", RECIPE_ID);
         // All recipe medicine
         matcher.addURI(RecipeContent.AUTHORITY, "recipe_medicine",
                 RECIPE_MEDICINE);
         // A special medicine
         matcher.addURI(RecipeContent.AUTHORITY, "recipe_medicine/#",
                 RECIPE_MEDICINE_ID);
         // All nations in China
         matcher.addURI(RecipeContent.AUTHORITY, "nation", NATION);
     }
 
     private static final String[] TABLE_NAMES = { PatientColumns.TABLE_NAME,
             CaseHistoryColumn.TABLE_NAME, MedicineColumn.TABLE_NAME,
             RecipeColumn.TABLE_NAME, RecipeMedicineColumn.TABLE_NAME,
             MedicineNameColumn.TABLE_NAME, NationColumn.TABLE_NAME };
 
     static void createPatientTable(SQLiteDatabase db) {
         String s = " (" + PatientColumns._ID
                 + " integer primary key autoincrement, "
                 + PatientColumns.ADDRESS + " text, "
                 + PatientColumns.FIRST_TIME + " integer, "
                 + PatientColumns.GENDER + " integer, "
                 + PatientColumns.NAME + " text, "
                 + PatientColumns.NAME_ABBR + " text, "
                 + PatientColumns.HISTORY + " text, "
                 + PatientColumns.AGE + " integer, "
                 + PatientColumns.NATION + " text, "
                 + PatientColumns.TIMESTAMP + " integer, "
                 + PatientColumns.TELEPHONE + " text" + ");";
         db.execSQL("create table " + PatientColumns.TABLE_NAME + s);
     }
 
     static void createCaseHistoryTable(SQLiteDatabase db) {
         String s = " (" + CaseHistoryColumn._ID
                 + " integer primary key autoincrement, "
                 + CaseHistoryColumn.PATIENT_KEY + " integer "
                 + REFERENCE_PATIENT_ID_AS_FOREIGN_KEY + " , "
                 + CaseHistoryColumn.DESCRIPTION + " text, "
                 + CaseHistoryColumn.SYMPTOM + " text, "
                 + CaseHistoryColumn.SYMPTOM_ABBR + " text, "
                 + CaseHistoryColumn.FIRST_TIME + " integer, "
                 + CaseHistoryColumn.TIMESTAMP + " integer" + ");";
         db.execSQL("create table " + CaseHistoryColumn.TABLE_NAME + s);
         db.execSQL(createIndex(CaseHistoryColumn.TABLE_NAME,
                 CaseHistoryColumn.PATIENT_KEY));
     }
 
     static void createMedicineTable(SQLiteDatabase db) {
         String s = " (" + MedicineColumn._ID
                 + " integer primary key autoincrement, "
                 + MedicineColumn.AMOUNT + " integer" + ");";
         db.execSQL("create table " + MedicineColumn.TABLE_NAME + s);
     }
 
     static void createMedicineNameTable(SQLiteDatabase db) {
         String s = " (" + MedicineNameColumn._ID
                 + " integer primary key autoincrement, "
                 + MedicineNameColumn.MEDICINE_KEY + " integer "
                 + REFERENCE_MEDICINE_ID_AS_FOREIGN_KEY + " , "
                 + MedicineNameColumn.MEDICINE_NAME_ABBR + " text, "
                 + MedicineNameColumn.MEDICINE_NAME + " text, " + "unique ("
                 + MedicineNameColumn.MEDICINE_NAME + ")" + ");";
         db.execSQL("create table " + MedicineNameColumn.TABLE_NAME + s);
         db.execSQL(createIndex(MedicineNameColumn.TABLE_NAME,
                 MedicineNameColumn.MEDICINE_KEY));
     }
 
     static void createRecipeTable(SQLiteDatabase db) {
         String s = " (" + RecipeColumn._ID
                 + " integer primary key autoincrement, "
                 + RecipeColumn.PATIENT_KEY + " integer "
                 + REFERENCE_PATIENT_ID_AS_FOREIGN_KEY + " , "
                 + RecipeColumn.CASE_HISTORY_KEY + " integer "
                 + REFERENCE_CASE_HISTORY_ID_AS_FOREIGN_KEY + " , "
                 + RecipeColumn.NAME + " text, "
                 + RecipeColumn.NAME_ABBR + " text, "
                 + RecipeColumn.NUMBER + " integer, "
                 + RecipeColumn.TIMESTAMP + " integer" + ");";
         db.execSQL("create table " + RecipeColumn.TABLE_NAME + s);
         db.execSQL(createIndex(RecipeColumn.TABLE_NAME,
                 RecipeColumn.PATIENT_KEY));
         db.execSQL(createIndex(RecipeColumn.TABLE_NAME,
                 RecipeColumn.CASE_HISTORY_KEY));
     }
 
     static void createRecipeMedicineTable(SQLiteDatabase db) {
         String s = " (" + RecipeMedicineColumn._ID
                 + " integer primary key autoincrement, "
                 + RecipeMedicineColumn.MEDICINE_KEY + " integer "
                 + REFERENCE_MEDICINE_ID_AS_FOREIGN_KEY + " , "
                 + RecipeMedicineColumn.RECIPE_KEY + " integer "
                 + REFERENCE_RECIPE_ID_AS_FOREIGN_KEY + " , "
                 + RecipeMedicineColumn.WEIGHT + " integer" + ");";
         db.execSQL("create table " + RecipeMedicineColumn.TABLE_NAME + s);
         db.execSQL(createIndex(RecipeMedicineColumn.TABLE_NAME,
                 RecipeMedicineColumn.MEDICINE_KEY));
         db.execSQL(createIndex(RecipeMedicineColumn.TABLE_NAME,
                 RecipeMedicineColumn.RECIPE_KEY));
     }
 
     static void createNationTable(SQLiteDatabase db) {
         String s = " (" + NationColumn._ID + " integer primary key, "
                 + NationColumn.NATION_NAME + " text" + ");";
         db.execSQL("create table " + NationColumn.TABLE_NAME + s);
         initializeNationTable(db);
     }
 
     private static void initializeNationTable(SQLiteDatabase db) {
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 1
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 2
                 + "','ɹ')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 3
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 4
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 5
                 + "','ά')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 6
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 7
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 8
                 + "','׳')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 9
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 10
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 11
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 12
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 13
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 14
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 15
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 16
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 17
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 18
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 19
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 20
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 21
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 22
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 23
                 + "','ɽ')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 24
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 25
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 26
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 27
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 28
                 + "','¶')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 29
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 30
                 + "','Ӷ')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 31
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 32
                 + "','Ǽ')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 33
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 34
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 35
                 + "','ë')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 36
                 + "','ˮ')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 37
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 38
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 39
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 40
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 41
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 42
                 + "','ŭ')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 43
                 + "','α')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 44
                 + "','˹')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 45
                 + "','¿')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 46
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 47
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 48
                 + "','ԣ')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 49
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 50
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 51
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 52
                 + "','״')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 53
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 54
                 + "','Ű')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 55
                 + "','')");
         db.execSQL("insert into " + NationColumn.TABLE_NAME + " values('" + 56
                 + "','ŵ')");
     }
 
     /*
      * Internal helper method for index creation. Example:
      * "create index message_" + MessageColumns.FLAG_READ + " on " +
      * Message.TABLE_NAME + " (" + MessageColumns.FLAG_READ + ");"
      */
     /* package */
     static String createIndex(String tableName, String columnName) {
         return "create index " + tableName.toLowerCase() + '_' + columnName
                 + " on " + tableName + " (" + columnName + ");";
     }
 
     private SQLiteDatabase mDatabase;
 
     public synchronized SQLiteDatabase getDatabase(Context context) {
         if (mDatabase != null) {
             return mDatabase;
         }
         DatabaseHelper helper = new DatabaseHelper(context);
         mDatabase = helper.getWritableDatabase();
         mDatabase.execSQL("PRAGMA foreign_keys = ON;");
 
         return mDatabase;
     }
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         public void onCreate(SQLiteDatabase db) {
             createPatientTable(db);
             createCaseHistoryTable(db);
             createMedicineNameTable(db);
             createMedicineTable(db);
             createRecipeMedicineTable(db);
             createRecipeTable(db);
             // Nation Table should not be modified.Only used to query.
             createNationTable(db);
//            createTriggers(db);
         }
 
         void createTriggers(SQLiteDatabase db) {
             db.execSQL("DROP TRIGGER IF EXISTS "
                     + MedicineNameColumn.TABLE_NAME + "_deleted;");
             db.execSQL("CREATE TRIGGER "
                     + MedicineNameColumn.TABLE_NAME + "_deleted "
                     + "   BEFORE DELETE ON " + MedicineNameColumn.TABLE_NAME
                     + " BEGIN "
                     + "   DELETE FROM " + MedicineColumn.TABLE_NAME
                     + "     WHERE " + MedicineColumn._ID
                                     + "=OLD." + MedicineNameColumn.MEDICINE_KEY
                                     + " AND (SELECT COUNT(*) FROM "
                                     + MedicineNameColumn.TABLE_NAME
                                     + " WHERE " + MedicineNameColumn.MEDICINE_KEY
                                     + " =OLD." + MedicineNameColumn.MEDICINE_KEY
                                     + ")=1;"
                     + " END");
 
             //patient delete trigger
             db.execSQL("DROP TRIGGER IF EXISTS "
                     + PatientColumns.TABLE_NAME + "_deleted;");
             db.execSQL("CREATE TRIGGER "
                     + PatientColumns.TABLE_NAME + "_deleted "
                     + "   BEFORE DELETE ON " + PatientColumns.TABLE_NAME
                     + " BEGIN "
                     + "   DELETE FROM " + CaseHistoryColumn.TABLE_NAME
                     + "     WHERE " + CaseHistoryColumn.PATIENT_KEY
                                     + "=OLD." + PatientColumns._ID
                                     + ";"
                     + " END");
 
             //case_history delete trigger
             db.execSQL("DROP TRIGGER IF EXISTS "
                     + CaseHistoryColumn.TABLE_NAME + "_deleted;");
             db.execSQL("CREATE TRIGGER "
                     + CaseHistoryColumn.TABLE_NAME + "_deleted "
                     + "   BEFORE DELETE ON " + CaseHistoryColumn.TABLE_NAME
                     + " BEGIN "
                     + "   DELETE FROM " + RecipeColumn.TABLE_NAME
                     + "     WHERE " + RecipeColumn.CASE_HISTORY_KEY
                                     + "=OLD." + CaseHistoryColumn._ID
                                     + ";"
                     + " END");
 
             //recipe delete trigger
             db.execSQL("DROP TRIGGER IF EXISTS "
                     + RecipeColumn.TABLE_NAME + "_deleted;");
             db.execSQL("CREATE TRIGGER "
                     + RecipeColumn.TABLE_NAME + "_deleted "
                     + "   BEFORE DELETE ON " + RecipeColumn.TABLE_NAME
                     + " BEGIN "
                     + "   DELETE FROM " + RecipeMedicineColumn.TABLE_NAME
                     + "     WHERE " + RecipeMedicineColumn.RECIPE_KEY
                                     + "=OLD." + RecipeColumn._ID
                                     + ";"
                     + " END");
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             db.execSQL("DROP TABLE IF EXISTS "
                     + RecipeMedicineColumn.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + PatientColumns.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + CaseHistoryColumn.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + MedicineNameColumn.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + MedicineColumn.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + RecipeColumn.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + NationColumn.TABLE_NAME);
             onCreate(db);
//            createTriggers(db);
         }
     }
 
     @Override
     public boolean onCreate() {
         return true;
     }
 
     private String whereWithId(String id, String selection) {
         StringBuilder sb = new StringBuilder(256);
         sb.append("_id=");
         sb.append(id);
         if (selection != null) {
             sb.append(" AND (");
             sb.append(selection);
             sb.append(')');
         }
         return sb.toString();
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection,
             String[] selectionArgs, String sortOrder) {
         Cursor c = null;
         int match = sURIMatcher.match(uri);
         Context context = getContext();
         SQLiteDatabase db = getDatabase(context);
         int table = match >> BASE_SHIFT;
         String id;
         SQLiteQueryBuilder qBuilder;
 
         Log.v(TAG, "RecipeProvider.query: uri=" + uri + ", match is " + match);
 
         switch (match) {
         case PATIENT:
         case CASE_HISTORY:
         case RECIPE:
         case MEDICINE:
         case MEDICINE_NAME:
         case NATION:
             c = db.query(TABLE_NAMES[table], projection, selection,
                     selectionArgs, null, null, sortOrder);
             break;
         case RECIPE_MEDICINE:
             qBuilder = new SQLiteQueryBuilder();
             qBuilder.setTables(TABLE_RECIPE_JOINED_MEDICINE_QUERY);
             qBuilder.setProjectionMap(sRecipeMedicineJoinMedicineNameMap);
             c = qBuilder.query(db, projection, selection, selectionArgs, null,
                     null, sortOrder);
             break;
         case MEDICINE_NAME_MEDICINE:
             qBuilder = new SQLiteQueryBuilder();
             qBuilder.setTables(TABLE_MEDICINE_JOINED_ALIAS_QUERY);
             qBuilder.setProjectionMap(sMedicineJoinAliasProjectionMap);
             c = qBuilder.query(db, projection, selection, selectionArgs, null,
                     null, sortOrder);
             break;
         case CASE_HISTORY_ID:
         case PATIENT_ID:
         case RECIPE_ID:
         case MEDICINE_ID:
         case MEDICINE_NAME_ID:
             id = uri.getLastPathSegment();
             c = db.query(TABLE_NAMES[table], projection,
                     whereWithId(id, selection), selectionArgs, null, null,
                     sortOrder);
             break;
         case RECIPE_MEDICINE_ID:
             id = uri.getLastPathSegment();
             qBuilder = new SQLiteQueryBuilder();
             qBuilder.setTables(TABLE_RECIPE_JOINED_MEDICINE_QUERY);
             qBuilder.setProjectionMap(sRecipeMedicineJoinMedicineNameMap);
             c = qBuilder.query(db, projection, RecipeMedicineColumn.TABLE_NAME
                     + "." + whereWithId(id, selection), selectionArgs, null,
                     null, sortOrder);
             break;
         case MEDICINE_NAME_MEDICINE_ID:
             id = uri.getLastPathSegment();
             qBuilder = new SQLiteQueryBuilder();
             qBuilder.setTables(TABLE_MEDICINE_JOINED_ALIAS_QUERY);
             qBuilder.setProjectionMap(sMedicineJoinAliasProjectionMap);
             c = qBuilder.query(db, projection, MedicineNameColumn.TABLE_NAME
                     + "." + whereWithId(id, selection), selectionArgs, null,
                     null, sortOrder);
             break;
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
         c.setNotificationUri(getContext().getContentResolver(), uri);
         return c;
     }
 
     @Override
     public String getType(Uri uri) {
         return null;
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         int match = sURIMatcher.match(uri);
         Context context = getContext();
         // See the comment at delete(), above
         SQLiteDatabase db = getDatabase(context);
         int table = match >> BASE_SHIFT;
         long id;
 
         Log.v(TAG, "RecipeProvider.insert: uri=" + uri + ", match is " + match);
 
         Uri resultUri = null;
 
         switch (match) {
         case PATIENT:
             values.put(PatientColumns.FIRST_TIME, System.currentTimeMillis());
             values.put(PatientColumns.TIMESTAMP, System.currentTimeMillis());
             id = db.insert(TABLE_NAMES[table], "foo", values);
             resultUri = ContentUris.withAppendedId(uri, id);
             break;
         case CASE_HISTORY:
             values.put(CaseHistoryColumn.FIRST_TIME, System.currentTimeMillis());
             values.put(CaseHistoryColumn.TIMESTAMP, System.currentTimeMillis());
             id = db.insert(TABLE_NAMES[table], "foo", values);
             resultUri = ContentUris.withAppendedId(uri, id);
             break;
         case RECIPE:
             values.put(RecipeColumn.TIMESTAMP, System.currentTimeMillis());
             id = db.insert(TABLE_NAMES[table], "foo", values);
             resultUri = ContentUris.withAppendedId(uri, id);
             break;
         case MEDICINE_NAME:
         case MEDICINE:
         case RECIPE_MEDICINE:
             id = db.insert(TABLE_NAMES[table], "foo", values);
             resultUri = ContentUris.withAppendedId(uri, id);
             break;
         default:
             throw new IllegalArgumentException("Unknown URL " + uri);
         }
 
         // Notify with the base uri, not the new uri (nobody is watching a new
         // record)
         getContext().getContentResolver().notifyChange(uri, null);
         return resultUri;
     }
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         final int match = sURIMatcher.match(uri);
         Context context = getContext();
         SQLiteDatabase db = getDatabase(context);
         int table = match >> BASE_SHIFT;
         String id = "0";
         int result = -1;
 
         switch (match) {
         case PATIENT:
         case MEDICINE:
         case MEDICINE_NAME:
         case RECIPE:
         case RECIPE_MEDICINE:
         case CASE_HISTORY:
             result = db.delete(TABLE_NAMES[table], selection, selectionArgs);
             break;
         case RECIPE_MEDICINE_ID:
         case MEDICINE_ID:
         case MEDICINE_NAME_ID:
             id = uri.getPathSegments().get(1);
             result = db.delete(TABLE_NAMES[table], whereWithId(id, selection),
                     selectionArgs);
             break;
         // all case_historys and recipes relate to this patient need to be
         // deleted.
         case PATIENT_ID:
             // delete patient from patient table.
             id = uri.getPathSegments().get(1);
             result = db.delete(TABLE_NAMES[table], whereWithId(id, selection),
                     selectionArgs);
             break;
         // the same strategy as Patient_id.
         case CASE_HISTORY_ID:
             id = uri.getPathSegments().get(1);
             result = db.delete(TABLE_NAMES[table], whereWithId(id, selection),
                     selectionArgs);
             break;
         // the same strategy as Patient_id.
         case RECIPE_ID:
             id = uri.getPathSegments().get(1);
             result = db.delete(TABLE_NAMES[table], whereWithId(id, selection),
                     selectionArgs);
             break;
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
         return result;
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String selection,
             String[] selectionArgs) {
         int match = sURIMatcher.match(uri);
         Context context = getContext();
         // See the comment at delete(), above
         SQLiteDatabase db = getDatabase(context);
         int table = match >> BASE_SHIFT;
         int result = -1;
 
         Log.v(TAG, "RecipeProvider.update: uri=" + uri + ", match is " + match);
         String id;
         switch (match) {
         case PATIENT_ID:
             values.put(PatientColumns.TIMESTAMP, System.currentTimeMillis());
             id = uri.getPathSegments().get(1);
             result = db.update(TABLE_NAMES[table], values,
                     whereWithId(id, selection), selectionArgs);
             break;
         case CASE_HISTORY_ID:
             values.put(CaseHistoryColumn.TIMESTAMP, System.currentTimeMillis());
             id = uri.getPathSegments().get(1);
             result = db.update(TABLE_NAMES[table], values,
                     whereWithId(id, selection), selectionArgs);
             break;
         case MEDICINE_NAME_ID:
             id = uri.getPathSegments().get(1);
             result = db.update(TABLE_NAMES[table], values,
                     whereWithId(id, selection), selectionArgs);
             break;
         case RECIPE_ID:
             values.put(RecipeColumn.TIMESTAMP, System.currentTimeMillis());
             id = uri.getPathSegments().get(1);
             result = db.update(TABLE_NAMES[table], values,
                     whereWithId(id, selection), selectionArgs);
             break;
         case RECIPE_MEDICINE_ID:
         case MEDICINE_ID:
             id = uri.getPathSegments().get(1);
             result = db.update(TABLE_NAMES[table], values,
                     whereWithId(id, selection), selectionArgs);
             break;
         case PATIENT:
         case MEDICINE:
         case MEDICINE_NAME:
         case RECIPE:
         case RECIPE_MEDICINE:
         case CASE_HISTORY:
             result = db.update(TABLE_NAMES[table], values, selection,
                     selectionArgs);
             break;
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         getContext().getContentResolver().notifyChange(uri, null);
         return result;
     }
 
     static {
         sMedicineJoinAliasProjectionMap = new HashMap<String, String>();
         sMedicineJoinAliasProjectionMap.put(MedicineNameColumn._ID,
                 MedicineNameColumn.TABLE_NAME + "." + MedicineNameColumn._ID);
         sMedicineJoinAliasProjectionMap.put(MedicineColumn.TABLE_NAME + "."
                 + MedicineColumn._ID, MedicineColumn.TABLE_NAME + "."
                 + MedicineColumn._ID);
         sMedicineJoinAliasProjectionMap.put(MedicineColumn.AMOUNT,
                 MedicineColumn.AMOUNT);
         sMedicineJoinAliasProjectionMap.put(MedicineNameColumn.MEDICINE_NAME,
                 MedicineNameColumn.MEDICINE_NAME);
         sMedicineJoinAliasProjectionMap.put(MedicineNameColumn.MEDICINE_KEY,
                 MedicineNameColumn.MEDICINE_KEY);
         sMedicineJoinAliasProjectionMap.put(MedicineNameColumn.MEDICINE_NAME_ABBR,
                 MedicineNameColumn.MEDICINE_NAME_ABBR);
 
         sRecipeMedicineJoinMedicineNameMap = new HashMap<String, String>();
         sRecipeMedicineJoinMedicineNameMap.put(MedicineNameColumn._ID,
                 MedicineNameColumn.TABLE_NAME + "." + MedicineNameColumn._ID);
         sRecipeMedicineJoinMedicineNameMap.put(MedicineNameColumn.MEDICINE_KEY,
                 MedicineNameColumn.MEDICINE_KEY);
         sRecipeMedicineJoinMedicineNameMap.put(MedicineNameColumn.MEDICINE_NAME,
                 MedicineNameColumn.MEDICINE_NAME);
         sRecipeMedicineJoinMedicineNameMap.put(RecipeMedicineColumn.TABLE_NAME + "." + RecipeMedicineColumn._ID,
                 RecipeMedicineColumn.TABLE_NAME + "." + RecipeMedicineColumn._ID);
         sRecipeMedicineJoinMedicineNameMap.put(RecipeMedicineColumn.MEDICINE_KEY,
                 RecipeMedicineColumn.MEDICINE_KEY);
         sRecipeMedicineJoinMedicineNameMap.put(RecipeMedicineColumn.RECIPE_KEY,
                 RecipeMedicineColumn.RECIPE_KEY);
         sRecipeMedicineJoinMedicineNameMap.put(RecipeMedicineColumn.WEIGHT,
                 RecipeMedicineColumn.WEIGHT);
         
     }
 }
