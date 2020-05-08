 package com.bigtheta.ragedice;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class MySQLiteHelper extends SQLiteOpenHelper {
     private static final String DATABASE_NAME = "rage_dice.db";
     private static final int DATABASE_VERSION = 1;
 
     public static final String COLUMN_ID = "_id";
 
     // TABLE_GAME
     public static final String TABLE_GAME = "game";
     public static final String COLUMN_RANDOM_NUMBER = "random_number";
     private static final String CREATE_TABLE_GAME = "CREATE TABLE "
             + TABLE_GAME + "("
             + COLUMN_ID + " integer primary key autoincrement, "
             + COLUMN_RANDOM_NUMBER + " integer "
             + ");";
 
     // TABLE_PLAYER
     public static final String TABLE_PLAYER = "player";
     public static final String COLUMN_GAME_ID = "game_id";
     public static final String COLUMN_PLAYER_NUMBER = "number";
     public static final String COLUMN_PLAYER_NAME = "name";
     private static final String CREATE_TABLE_PLAYER = "CREATE TABLE "
             + TABLE_PLAYER + "("
             + COLUMN_ID + " integer primary key autoincrement, "
             + COLUMN_GAME_ID + " integer references " + TABLE_GAME + " on delete cascade, "
             + COLUMN_PLAYER_NUMBER + " integer not NULL, "
             + COLUMN_PLAYER_NAME + " text "
             + ");";
 
     // TABLE_DICE_ROLL
     public static final String TABLE_DICE_ROLL = "dice_roll";
     public static final String COLUMN_PLAYER_ID = "player_id";
     public static final String COLUMN_TIME_CREATED = "time_created";
     private static final String CREATE_TABLE_DICE_ROLL = "CREATE TABLE "
             + TABLE_DICE_ROLL + "("
             + COLUMN_ID + " integer primary key autoincrement, "
             + COLUMN_TIME_CREATED + " timestamp, "
             + COLUMN_PLAYER_ID + " integer references " + TABLE_PLAYER + " on delete cascade "
             + ");";
 
     // TABLE_DIE_DESCRIPTION
     public static final String TABLE_DIE_DESCRIPTION = "die_description";
     // COLUMN_GAME_ID is reused
     public static final String COLUMN_NUM_LOW_FACE = "num_low_face";
     public static final String COLUMN_NUM_HIGH_FACE = "num_high_face";
     public static final String COLUMN_BASE_IDENTIFIER_NAME = "base_identifier_name";
     public static final String COLUMN_BACKGROUND_COLOR = "background_color";
     public static final String COLUMN_IMAGE_VIEW_RESOURCE = "image_view_resource";
     public static final String COLUMN_DISPLAY_TYPE = "display_type";
     private static final String CREATE_TABLE_DIE_DESCRIPTION = "CREATE TABLE "
             + TABLE_DIE_DESCRIPTION + "("
             + COLUMN_ID + " integer primary key autoincrement, "
             + COLUMN_GAME_ID + " integer references " + TABLE_GAME + " on delete cascade, "
             + COLUMN_NUM_LOW_FACE + " integer not NULL, "
             + COLUMN_NUM_HIGH_FACE + " integer not NULL, "
             + COLUMN_BASE_IDENTIFIER_NAME + " text, "
             + COLUMN_BACKGROUND_COLOR + " integer not NULL, "
             + COLUMN_IMAGE_VIEW_RESOURCE + " integer, "
             + COLUMN_DISPLAY_TYPE + " text "
             + ");";
 
     // TABLE_DIE_RESULT
     public static final String TABLE_DIE_RESULT = "die_result";
     public static final String COLUMN_DICE_ROLL_ID = "dice_roll_id";
     public static final String COLUMN_DIE_DESCRIPTION_ID = "description";
     public static final String COLUMN_DIE_RESULT = "result";
     private static final String CREATE_TABLE_DIE_RESULT = "CREATE TABLE "
             + TABLE_DIE_RESULT + "("
             + COLUMN_ID + " integer primary key autoincrement, "
             + COLUMN_DICE_ROLL_ID + " integer references "
                                   + TABLE_DICE_ROLL + " on delete cascade, "
             + COLUMN_DIE_DESCRIPTION_ID + " integer references "
                                         + TABLE_DIE_DESCRIPTION + " on delete cascade, "
             + COLUMN_DIE_RESULT + " integer not NULL "
             + ");";
 
     public MySQLiteHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase database) {
         database.execSQL(CREATE_TABLE_GAME);
         database.execSQL(CREATE_TABLE_PLAYER);
         database.execSQL(CREATE_TABLE_DICE_ROLL);
         database.execSQL(CREATE_TABLE_DIE_DESCRIPTION);
         database.execSQL(CREATE_TABLE_DIE_RESULT);
         database.execSQL("PRAGMA foreign_keys = ON;");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w(MySQLiteHelper.class.getName(),
                 "Upgrading database from version " + oldVersion + " to "
                 + newVersion + ", which will destroy all old data");
         resetDatabase(db);
         /*
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYER);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_DICE_ROLL);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIE_DESCRIPTION);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIE_RESULT);
         onCreate(db);
         */
     }
 
     public void resetDatabase(SQLiteDatabase database) {
         database.execSQL("DROP TABLE IF EXISTS " + TABLE_DIE_RESULT);
         database.execSQL("DROP TABLE IF EXISTS " + TABLE_DIE_DESCRIPTION);
         database.execSQL("DROP TABLE IF EXISTS " + TABLE_DICE_ROLL);
         database.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYER);
         database.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME);
         onCreate(database);
     }
 }
 
