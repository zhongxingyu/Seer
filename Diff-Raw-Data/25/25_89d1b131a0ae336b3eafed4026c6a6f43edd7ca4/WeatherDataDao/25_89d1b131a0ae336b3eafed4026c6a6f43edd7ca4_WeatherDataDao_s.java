 package no.nith.pg3200.a02.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import com.google.android.gms.maps.model.LatLng;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import no.nith.pg3200.a02.domain.Forecast;
 import no.nith.pg3200.a02.domain.WeatherData;
 import org.intellij.lang.annotations.Language;
 
 /**
  * @author Simen Bekkhus
  */
 public class WeatherDataDao {
     private final DatabaseHelper dbHelper;
     private SQLiteDatabase database;
 
     public WeatherDataDao(final Context context) {
         this.dbHelper = new DatabaseHelper(context);
     }
 
    public void open() throws SQLException {
         if (database == null || !database.isOpen())
             database = dbHelper.getWritableDatabase();
     }
 
    public void close() {
         if (database != null && database.isOpen())
             dbHelper.close();
     }
 
     public void insertWeatherData(final WeatherData weatherData) {
         if (existsInDatabase(weatherData.hashCode())) return;
         final int hash = weatherData.hashCode();
 
         this.open();
 
         final ContentValues values = new ContentValues();
 
         values.put(DatabaseHelper.DATE, weatherData.getCreated().toString());
         values.put(DatabaseHelper.LATITUDE, weatherData.getPosition().latitude);
         values.put(DatabaseHelper.LONGITUDE, weatherData.getPosition().longitude);
         values.put(DatabaseHelper.FORECASTS, hash);
 
         insertForecastData(weatherData.getForecasts(), hash);
 
         this.database.insert(DatabaseHelper.TABLE_WEATHER_DATA, null, values);
 
         this.close();
     }
 
     private void insertForecastData(final List<Forecast> forecasts, final int hash) {
         for (final Forecast forecast : forecasts) {
             final ContentValues values = new ContentValues();
 
             values.put(DatabaseHelper.WEATHER_DATA_REF, hash);
             values.put(DatabaseHelper.SYMBOL, forecast.getSymbol());
             values.put(DatabaseHelper.TIME_OF_DAY, forecast.getTime().toString());
             values.put(DatabaseHelper.TEMPERATURE, forecast.getTemperature());
 
             this.database.insert(DatabaseHelper.TABLE_FORECAST, null, values);
         }
     }
 
     private boolean existsInDatabase(final int hash) {
         @Language("SQLite")
         final String sqlFormat = "SELECT * FROM %s WHERE %s=%d";
 
         final String sql = String.format(sqlFormat,
                 DatabaseHelper.TABLE_WEATHER_DATA,
                 DatabaseHelper.FORECASTS,
                 hash
         );
 
         this.open();
 
         final Cursor cursor = this.database.rawQuery(sql, null);
 
         final boolean exists = cursor.moveToFirst();
 
         this.close();
 
         return exists;
     }
 
     public ArrayList<WeatherData> getAllWeatherData() {
         final ArrayList<WeatherData> data = new ArrayList<WeatherData>();
         @Language("SQLite")
         final String sqlFormat = "SELECT * FROM %s INNER JOIN %s ON %s.%s=%s.%s";
 
         final String sql = String.format(sqlFormat,
                 DatabaseHelper.TABLE_WEATHER_DATA,
                 DatabaseHelper.TABLE_FORECAST,
                 DatabaseHelper.TABLE_WEATHER_DATA,
                 DatabaseHelper.FORECASTS,
                 DatabaseHelper.TABLE_FORECAST,
                 DatabaseHelper.WEATHER_DATA_REF
         );
 
         this.open();
 
         final Cursor cursor = this.database.rawQuery(sql, null);
 
         if (cursor.moveToFirst()) {
             do {
                 final String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATE));
                 final double lon = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LONGITUDE)),
                         lat = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LATITUDE));
 
                 final ArrayList<Forecast> forecasts = new ArrayList<Forecast>();
 
                 for (int i = 0; i < 24; i++) {
                     final int symbol = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SYMBOL));
                     final String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TIME_OF_DAY));
                     final double temp = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TEMPERATURE));
 
                     forecasts.add(new Forecast(time, symbol, temp));
 
                     if (!cursor.moveToNext()) break;
                 }
 
                 data.add(new WeatherData(date, new LatLng(lat, lon), forecasts));
             } while (cursor.moveToNext());
         }
 
         this.close();
 
         Collections.sort(data);
 
         return data;
     }
 
     public void deleteAllData() {
         @Language("SQLite")
         final String sql = "DELETE FROM ";
 
         this.open();
 
         this.database.execSQL(sql + DatabaseHelper.TABLE_WEATHER_DATA);
         this.database.execSQL(sql + DatabaseHelper.TABLE_FORECAST);
 
         this.close();
     }
 }
