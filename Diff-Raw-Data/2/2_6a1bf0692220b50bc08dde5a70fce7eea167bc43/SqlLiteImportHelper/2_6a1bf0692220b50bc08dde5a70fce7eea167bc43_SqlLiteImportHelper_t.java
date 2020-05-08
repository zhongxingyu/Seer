 package com.xmedic.troll.service.db;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.DatabaseUtils;
 import android.database.DatabaseUtils.InsertHelper;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class SqlLiteImportHelper {
 
 	private static final String LOG_TAG = "troll-db-import";
 	
 	public static void importInitialData(
 			SQLiteDatabase db, 
 			Context context) {
 
 		insertCities(db, getReader(context, "city.txt"));
 		insertRoads(db, getReader(context, "road.txt"));
 		insertLevels(db, getReader(context, "level.txt"));
 	}
 
 
 	private static void insertCities(
 			SQLiteDatabase db,
 			BufferedReader reader) {
 
 		DatabaseUtils.InsertHelper cities = new InsertHelper(db, SqlLiteOpenHelper.CITY);
 
 		try {
             String line = null;
 
             ContentValues values = new ContentValues();
             while ((line = reader.readLine()) != null) {
                 String[] columns = line.split(";");
                 values.clear();
                 values.put("country", "LT");
                 values.put("id", columns[0].trim());
                 values.put("name", columns[1].trim());
                 values.put("latitude", columns[2].trim());
                 values.put("longitude", columns[3].trim());
                 values.put("population", columns[4].trim().replace(",", ""));
                 if (columns.length > 5) {
                 	values.put("x", columns[5].trim());
                 	values.put("y", columns[6].trim());
                 }
                 cities.insert(values);
             }
 
         } catch (Exception e) {
             Log.e(LOG_TAG, e.getMessage(), e);
 
         } finally {
             cities.close();
             closeQuietly(reader);
         }       
 		
 	}
 
 
 	private static void insertRoads(SQLiteDatabase db, BufferedReader reader) {
 		DatabaseUtils.InsertHelper roads = new InsertHelper(db, SqlLiteOpenHelper.ROAD);
 
 		try {
             String line = null;
 
             
             while ((line = reader.readLine()) != null) {
                 String[] columns = line.split(";");
                 
                 for (int i = 1; i < columns.length; i++) {
                 	ContentValues values = new ContentValues();
                 	values.clear();
                 	values.put("fromCityId", columns[0]);
                	values.put("toCityId", columns[i]);
                 	roads.insert(values);
                 }
 
             }
 
         } catch (Exception e) {
             Log.e(LOG_TAG, e.getMessage(), e);
 
         } finally {
             roads.close();
             closeQuietly(reader);
         }       
 		
 	}
 	
 	private static void insertLevels(SQLiteDatabase db, BufferedReader reader) {
 		DatabaseUtils.InsertHelper levels = new InsertHelper(db, SqlLiteOpenHelper.LEVEL);
 
 		try {
              String line = null;
 
 			 ContentValues values = new ContentValues();
 			 while ((line = reader.readLine()) != null) {
 			     String[] columns = line.split(";");
 			     values.clear();
 			     values.put("id", columns[0].trim());
 			     values.put("name", columns[1].trim());
 			     values.put("description", columns[2].trim());
 			     values.put("startCityId", columns[3].trim());
 			     values.put("goalCityId", columns[4].trim());
 			     levels.insert(values);
 			 }
 
         } catch (Exception e) {
             Log.e(LOG_TAG, e.getMessage(), e);
 
         } finally {
         	levels.close();
             closeQuietly(reader);
         }       
 		
 		
 	}
 
 	private static BufferedReader getReader(Context context, String asset) {
 		try {
 			return new BufferedReader(new InputStreamReader(context.getAssets().open(asset)));
 		} catch (IOException ioe) {
 			Log.e(LOG_TAG, ioe.getMessage(), ioe);
 			return null;
 		}
 	}
 	
 	private static void closeQuietly(BufferedReader reader) {
 		try {
 			reader.close();
 		} catch (IOException ioe) {
 			Log.e(LOG_TAG, ioe.getMessage(), ioe);
 		}
 	}
 }
