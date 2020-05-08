 package ca.dreamteam.newrecipebook.Models;
 
 
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
public class IngredientSqlTable extends SQLiteOpenHelper
 {
     public static final String TABLE_INGREDIENTS = "ingredients";
     public static final String COLUMN_ID = "_id";
     public static final String COLUMN_INGREDIENT = "ingredient";
     
     private static final String DATABASE_NAME = "ingredients.db";
     private static final int DATABASE_VERSION = 1;
     
     private static final String DATABASE_CREATE = "create table "
             + TABLE_INGREDIENTS + "(" + COLUMN_ID
             + " integer primary key autoincrement, " + COLUMN_INGREDIENT
             + " text not null);";

    public IngredientSqlTable(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }
 
     
     @Override
     public void onCreate(SQLiteDatabase database) {
       database.execSQL(DATABASE_CREATE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(IngredientSqlTable.class.getName(),
           "Upgrading database from version " + oldVersion + " to "
               + newVersion + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
       onCreate(db);
     }
   
 }
