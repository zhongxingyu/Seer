 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.database;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import no.hials.muldvarp.v2.database.tables.*;
 
 /**
  * This class is responsible for creating, maintaining and upgrading the SQLITE database. 
  * 
  * @author johan
  */
 public class MuldvarpDBHelper extends SQLiteOpenHelper {
 
     //Singleton-pattern variable
     private static MuldvarpDBHelper mDBHelper = null;
     private final Context dbContext;
 
     //General
     private static final String DATABASE_NAME = "muldvarp.db";
    private static final int DATABASE_VERSION = 2;
 
     public static MuldvarpDBHelper getInstance(Context context){
         if(mDBHelper == null){
             mDBHelper = new MuldvarpDBHelper(context);
         }
 
         return mDBHelper;
     }
 
     public MuldvarpDBHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         this.dbContext = context;
         
         
     }
 
     @Override
     public void onCreate(SQLiteDatabase database) {
         //Create tables
         database.execSQL(getTableCreationString(ProgrammeTable.TABLE_NAME, ProgrammeTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(CourseTable.TABLE_NAME, CourseTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(TopicTable.TABLE_NAME, TopicTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(VideoTable.TABLE_NAME, VideoTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(DocumentTable.TABLE_NAME, DocumentTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(QuizTable.TABLE_NAME, QuizTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(ProgrammeHasCourseTable.TABLE_NAME, ProgrammeHasCourseTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(ProgrammeHasDocumentTable.TABLE_NAME, ProgrammeHasDocumentTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(ProgrammeHasQuizTable.TABLE_NAME, ProgrammeHasQuizTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(CourseHasTopicTable.TABLE_NAME, CourseHasTopicTable.TABLE_COLUMNS));
         database.execSQL(getTableCreationString(CourseHasDocumentTable.TABLE_NAME, CourseHasDocumentTable.TABLE_COLUMNS));
 
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         
         if (oldVersion < newVersion) {  
             Log.w("Database Upgrade", "Database version higher, upgrading");
             dbContext.deleteDatabase(DATABASE_NAME);
             Log.w(MuldvarpDBHelper.class.getName(),
             "Upgrading database from version " + oldVersion + " to "
             + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS " + ProgrammeTable.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + CourseTable.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + TopicTable.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + VideoTable.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + DocumentTable.TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + QuizTable.TABLE_NAME);
             onCreate(db);
         } else {
             Log.w("Database Upgrade", "Database version lower, no upgrade,");
         }
     }
 
 
         /**
      * This method returns the field names of a table.
      *
      * @param tableColumns
      * @return
      */
     public static String[] getColumns(String[][] tableColumns){
 
         String[] retVal = new String[tableColumns.length];
 
         for (int i = 0; i < tableColumns.length; i++) {
             retVal[i] = tableColumns[i][0];
         }
 
         return retVal;
     }
 
     /**
      * This function creates a SQL-creation statement based on a String name for the
      * table, and a multi-dimensional array of String values containing field names
      * and their attributes.
      *
      * @param tableName
      * @param fields
      * @return
      */
     public static String getTableCreationString(String tableName, String[][] fields){        
         String retVal = "CREATE TABLE " + tableName + "(";
         for (int i = 0; i < fields.length; i++) {
             for (int j = 0; j < fields[i].length; j++) {
                 retVal += fields[i][j];
             }
             if(i < (fields.length -1)){
                 retVal +=",";
             }
         }
         retVal += ");";        
         return retVal;
     }
 
 }
