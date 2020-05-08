 package no.nith.pg3200.a02.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import org.intellij.lang.annotations.Language;
 
 /**
  * @author Simen Bekkhus
  */
public class DatabaseHelper extends SQLiteOpenHelper {
     public static final String TABLE_WEATHER_DATA = "weather_data";
     public static final String TABLE_FORECAST = "forecast";
     public static final String KEY_COLUMN_ID = "id";
     public static final String DATE = "date_stored";
     public static final String LONGITUDE = "longitude";
     public static final String LATITUDE = "latitude";
     public static final String FORECASTS = "forecasts";
     public static final String WEATHER_DATA_REF = "weather_data_ref";
     public static final String SYMBOL = "symbol";
     public static final String TIME_OF_DAY = "time_of_day";
     public static final String TEMPERATURE = "temperature";
     private static final String DATABASE_NAME = "weather_data.db";
     private static final int DATABASE_VERSION = 1;
 
     public DatabaseHelper(final Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(final SQLiteDatabase db) {
         @Language("SQLite")
         final String weatherDataDb = "CREATE TABLE " + TABLE_WEATHER_DATA
                 + "("
                 + KEY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                 + DATE + " TEXT NOT NULL, "
                 + LONGITUDE + " REAL NOT NULL, "
                 + LATITUDE + " REAL NOT NULL, "
                 + FORECASTS + " INTEGER NOT NULL, "
                 + "FOREIGN KEY(" + FORECASTS + ") REFERENCES " + TABLE_FORECAST + "(" + WEATHER_DATA_REF + ")"
                 + ");";
 
         @Language("SQLite")
         final String forecastDataDb = "CREATE TABLE " + TABLE_FORECAST
                 + "("
                 + KEY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                 + WEATHER_DATA_REF + " INTEGER NOT NULL, "
                 + SYMBOL + " INTEGER NOT NULL, "
                 + TIME_OF_DAY + " TEXT NOT NULL, "
                 + TEMPERATURE + " REAL NOT NULL "
                 + ");";
 
         db.execSQL(weatherDataDb);
         db.execSQL(forecastDataDb);
     }
 
     @Override
     public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
         @Language("SQLite")
         final String sql = "DROP TABLE IF EXISTS ";
 
         db.execSQL(sql + TABLE_WEATHER_DATA);
         db.execSQL(sql + TABLE_FORECAST);
 
         onCreate(db);
     }
 }
