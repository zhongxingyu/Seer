 package edu.ucsd.cogs160;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.nio.channels.FileChannel;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.os.Environment;
 import android.util.Log;
 
 public class GestureMathDataOpenHelper extends SQLiteOpenHelper {
 
     private static final int DATABASE_VERSION = 2;
     private static final String DATABASE_NAME = "gesture_math";
     
     private static final String SOLVED_PROBLEMS_TABLE_NAME = "solved_problems";
     private static final String KEY_STUDENT_ID = "student_id";
     private static final String KEY_GESTURE_CONDITION = "gesture_condition";
     private static final String KEY_PROBLEM_ID = "problem_id";
     private static final String KEY_CORRECT = "correct";
     
     private static final String OPTIONS_TABLE_NAME = "options";
     private static final String KEY_GESTURE_CONDITION_OPTION = "gesture_condition_option";
     
     private static final String SOLVED_PROBLEMS_TABLE_CREATE =
             "CREATE TABLE " + SOLVED_PROBLEMS_TABLE_NAME + " (" +
                     KEY_STUDENT_ID + " INT, " +
                     KEY_GESTURE_CONDITION + " INT, " +
                     KEY_PROBLEM_ID + " INT, " + 
                     KEY_CORRECT + " INT);";
     
     private static final String OPTIONS_TABLE_CREATE =
             "CREATE TABLE " + OPTIONS_TABLE_NAME + " (" +
                     KEY_GESTURE_CONDITION_OPTION + " INT);";
     
     private static final String INSERT_SOLVED_PROBLEM =
             "INSERT INTO " + SOLVED_PROBLEMS_TABLE_NAME + 
                     " (" + KEY_STUDENT_ID + ", " + KEY_GESTURE_CONDITION + ", " + 
                            KEY_PROBLEM_ID + ", " + KEY_CORRECT + ") " + 
                     " VALUES (?, ?, ?, ?);";
 
     private static final String INSERT_OPTIONS =
             "INSERT INTO " + OPTIONS_TABLE_NAME + " (" + KEY_GESTURE_CONDITION_OPTION + ") " + 
                     " VALUES (?);";
 
     private static final String UPDATE_OPTIONS =
             "UPDATE " + OPTIONS_TABLE_NAME + " SET " + KEY_GESTURE_CONDITION_OPTION + " = ?;";
 
     GestureMathDataOpenHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL(SOLVED_PROBLEMS_TABLE_CREATE);
         db.execSQL(OPTIONS_TABLE_CREATE);
         insertOptions(db, 1);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // TODO Auto-generated method stub
 
     }
     
     public void addSolvedProblem(SQLiteDatabase db, int student_id, boolean isGestureCondition, int problem_id, int correct) {
         int gesture_condition = 0;
         if (isGestureCondition) gesture_condition = 1;
         SQLiteStatement stmt = db.compileStatement(INSERT_SOLVED_PROBLEM);
         stmt.bindLong(1, student_id);
         stmt.bindLong(2, gesture_condition);
         stmt.bindLong(3, problem_id);
         stmt.bindLong(4, correct);
         if (stmt.executeInsert() == -1) {
             //problem!
             Log.e("db insert error", "error inserting solved problem row");
         }
     }
     
     public String getSolvedProblems(SQLiteDatabase db) {
         String output = "student_id\tgesture_condition\tproblem_id\tcorrect\n";
         Cursor c = db.query(SOLVED_PROBLEMS_TABLE_NAME, null, null, null, null, null, null);
         while (c.moveToNext()) {
             output += String.valueOf(c.getInt(0)) + "\t" + 
                       String.valueOf(c.getInt(1)) + "\t\t\t\t\t\t\t\t" + 
                       String.valueOf(c.getInt(2)) + "\t\t\t\t" + 
                       String.valueOf(c.getInt(3)) + "\n";
         }
         return output;
     }
 
     public void insertOptions(SQLiteDatabase db, int gesture_condition) {
         SQLiteStatement stmt = db.compileStatement(INSERT_OPTIONS);
         stmt.bindLong(1, gesture_condition);
         if (stmt.executeInsert() == -1) {
             //problem!
             Log.e("db insert error", "error inserting options");
         }
     }
     
     public void updateOptions(SQLiteDatabase db, int gesture_condition) {
         SQLiteStatement stmt = db.compileStatement(UPDATE_OPTIONS);
         stmt.bindLong(1, gesture_condition);
         if (stmt.executeUpdateDelete() == -1) {
             //problem!
             Log.e("db insert error", "error updating options");
         }
     }
     
     //for now we only have one option so return a single int
     public int getOptions(SQLiteDatabase db) {
         Cursor c = db.query(OPTIONS_TABLE_NAME, null, null, null, null, null, null);
         c.moveToNext();
         return c.getInt(0);
     }
     
     public void deleteAllSolvedProblems(SQLiteDatabase db) {
         db.delete(SOLVED_PROBLEMS_TABLE_NAME, null, null);
     }
     
     public static void backupDb() {
         Log.i("db backup", "start");
         try {
            //TODO: this directory is a hack for the ASUS Transformer TF-101.
            // Environment.getExternalStorageDirectory() reports the SD card path as "/mnt/sdcard", but the correct directory is "/Removable/MicroSD"
            // using the File Manager on the TF-101 shows the correct microSD path.  hopefully this is also the case with other devices
            //File sd = Environment.getExternalStorageDirectory();
            File sd = new File("/Removable/MicroSD");
             File data = Environment.getDataDirectory();
 
             if (sd.canWrite()) {
                 String currentDBPath = "//data//" + "edu.ucsd.cogs160" + "//databases//" + DATABASE_NAME;
                String backupDBPath = DATABASE_NAME + ".sqlite";
                 File currentDB = new File(data, currentDBPath);
                 File backupDB = new File(sd, backupDBPath);
 
                 if (currentDB.exists()) {
                     FileChannel src = new FileInputStream(currentDB).getChannel();
                     FileChannel dst = new FileOutputStream(backupDB).getChannel();
 
                    long bytes = dst.transferFrom(src, 0, src.size());
 
                     src.close();
                     dst.close();
                                        
                    Log.i("db backup", "number of bytes " + String.valueOf(bytes));
                     Log.i("db backup", "backup saved at " + backupDB.toString());
                     
                 } else {
                    Log.e("db backup", "db file does not exist " + currentDB.toString());
                }              
            } else {
                Log.e("db backup", "cannot write to location " + sd.toString());                
             }
         } catch (Exception e) {
             // exception
             Log.e("db backup", "database backup failed " + e.getMessage());
         }
     }
 
 }
