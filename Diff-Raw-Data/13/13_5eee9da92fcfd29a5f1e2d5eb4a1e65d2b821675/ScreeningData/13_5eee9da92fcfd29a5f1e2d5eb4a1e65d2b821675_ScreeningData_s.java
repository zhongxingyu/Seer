 package com.sportsfire.screening;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.sportsfire.Player;
 import com.sportsfire.db.DBHelper;
 import com.sportsfire.db.ScreeningAverageValuesTable;
 import com.sportsfire.db.ScreeningValuesTable;
 import com.sportsfire.sync.Provider;
 
 public class ScreeningData {
 
 	private String seasonID;
 	private String week;
 	private ContentResolver content;
 	public ScreeningData(Context context,String seasonID,String week){
 		content = context.getContentResolver();
 		this.seasonID = seasonID;
 		this.week = week;
 	}
 	
 	private String getWhere(String playerID,String measurementType,String week){
 		final String where = ScreeningValuesTable.KEY_PLAYER_ID + " = '" + playerID + "' and " +
 				ScreeningValuesTable.KEY_SEASON_ID + " = '" + seasonID + "' and " + 
 				ScreeningValuesTable.KEY_WEEK + " = '" + week + "' and " +
 				ScreeningValuesTable.KEY_MEASUREMENT_TYPE + " = '" + measurementType + "'";
 		
 		return where;
 	}
 	
 	private String getAvgWhere(String playerID,String measurementType){
 		final String where = ScreeningAverageValuesTable.KEY_PLAYER_ID + " = '" + playerID + "' and " +
 				ScreeningAverageValuesTable.KEY_SEASON_ID + " = '" + seasonID + "' and " + 
 				ScreeningValuesTable.KEY_MEASUREMENT_TYPE + " = '" + measurementType + "'";
 		
 		return where;
 	}
 	
 	
 	public boolean setValue(String playerID,String measurementType,String value){
 		if(value == "") 
 			return false;
 		try{
 			Double.parseDouble(value);
 		}
 		catch(NumberFormatException e){
 			return false;
 		}
 		
 		String where = getWhere(playerID,measurementType, week);
 		
 		String[] projection = { ScreeningValuesTable.KEY_VALUE };
 		Cursor cursor = content.query(Provider.CONTENT_URI_SCREENING_VALUES, projection, where, null, null);
 		
 		ContentValues values = new ContentValues();
 		
 		if(cursor.getCount() == 0){
 			values.put(ScreeningValuesTable.KEY_MEASUREMENT_TYPE, measurementType);
 			values.put(ScreeningValuesTable.KEY_PLAYER_ID, playerID);
 			values.put(ScreeningValuesTable.KEY_SEASON_ID, seasonID);
 			values.put(ScreeningValuesTable.KEY_VALUE, value);
 			values.put(ScreeningValuesTable.KEY_WEEK, week);
 			content.insert(Provider.CONTENT_URI_SCREENING_VALUES, values);
 			
 			addToAverage(playerID,measurementType,value);
 		} else {
 			values.put(ScreeningValuesTable.KEY_VALUE, value);
 			
 			cursor.moveToFirst();
 			changeAverage(playerID,measurementType,cursor.getString(0),value);
 			
 			content.update(Provider.CONTENT_URI_SCREENING_VALUES, values, where, null);
 			
 			
 		}
 		
 		return true;
 	}
 	
 	
 	
 
 	public String getValue(String playerID,String measurementType){
 		String where = getWhere(playerID,measurementType, week);
 		
 		String[] projection = { ScreeningValuesTable.KEY_VALUE };
 		Cursor cursor = content.query(Provider.CONTENT_URI_SCREENING_VALUES, projection, where, null, null);
 		
 		if(cursor.getCount() == 0){
 			return "";
 		}
 		else{
 			cursor.moveToFirst();
 			String result = cursor.getString(0);
 			double number = Double.parseDouble(result);
 			
 			cursor.close();
			return String.format("%1$..2f", number);		}
 	}
 	
 
 	private void changeAverage(String playerID, String measurementType, String oldValue, String newValue) {
 String where = getAvgWhere(playerID,measurementType);
 		
 		String[] projection = { ScreeningAverageValuesTable.KEY_AVERAGE, ScreeningAverageValuesTable.KEY_NUM_MEASUREMENTS};
		Cursor cursor = content.query(Provider.CONTENT_URI_SCREENING_AVERAGES, null, where, null, null);
 		ContentValues values = new ContentValues();
 		if(cursor.moveToFirst()){
 			String currentAverage = cursor.getString(0);
 			int numberOfMeasurements = cursor.getInt(1);
 			double oldAverage = Double.parseDouble(currentAverage);
 
 			double newAverage = ((oldAverage * numberOfMeasurements) - Double.parseDouble(oldValue) + Double.parseDouble(newValue)) / numberOfMeasurements;
 
 			values.put(ScreeningAverageValuesTable.KEY_AVERAGE,Double.toString(newAverage));
 			
 			content.update(Provider.CONTENT_URI_SCREENING_AVERAGES, values, where, null);
 		}		
 	}
 	
 	private void addToAverage(String playerID,String measurementType,String value){
 		String where = getAvgWhere(playerID,measurementType);
 		
 		String[] projection = { ScreeningAverageValuesTable.KEY_AVERAGE, ScreeningAverageValuesTable.KEY_NUM_MEASUREMENTS};
 		Cursor cursor = content.query(Provider.CONTENT_URI_SCREENING_AVERAGES, projection, where, null, null);
 		ContentValues values = new ContentValues();
 		if(cursor.getCount() == 0){
 			values.put(ScreeningAverageValuesTable.KEY_AVERAGE, value);
 			values.put(ScreeningAverageValuesTable.KEY_MEASUREMENT_TYPE, measurementType);
 			values.put(ScreeningAverageValuesTable.KEY_NUM_MEASUREMENTS, 1);
 			values.put(ScreeningAverageValuesTable.KEY_PLAYER_ID, playerID);
 			values.put(ScreeningAverageValuesTable.KEY_SEASON_ID,seasonID);
 			
 			content.insert(Provider.CONTENT_URI_SCREENING_AVERAGES, values);
 		}
 		else{
 			cursor.moveToFirst();
 			String currentAverage = cursor.getString(0);
 			int oldNumberOfMeasurements = cursor.getInt(1);
 			int newNumberOfMeasurements = oldNumberOfMeasurements + 1;
 			double oldAverage = Double.parseDouble(currentAverage);
 			double newAverage = ((oldAverage * oldNumberOfMeasurements) + Double.parseDouble(value)) / newNumberOfMeasurements;
 			
 			values.put(ScreeningAverageValuesTable.KEY_AVERAGE,Double.toString(newAverage));
 			values.put(ScreeningAverageValuesTable.KEY_NUM_MEASUREMENTS,Integer.toString(newNumberOfMeasurements));
 			
 			content.update(Provider.CONTENT_URI_SCREENING_AVERAGES, values, where, null);
 		}
 	}
 	
 	public String getAverageValue(String playerID,String measurementType){
 		String where = getAvgWhere(playerID,measurementType);
 		
 		String[] projection = { ScreeningAverageValuesTable.KEY_AVERAGE };
 		Cursor cursor = content.query(Provider.CONTENT_URI_SCREENING_AVERAGES, projection, where, null, null);
 		
 		if(cursor.getCount() == 0){
 			return "";
 		}
 		else{
 			cursor.moveToFirst();
			String result = cursor.getString(0);
 			cursor.close();
			return result;
 		}
 	}
 	
 	
 
 	public String getPreviousValue(String playerID,String measurementType){
 		int lastweek = Integer.parseInt(week);
 		if(lastweek > 0){
 			lastweek--; // this needs to be changed for monthly
 			
 			String where = getWhere(playerID,measurementType,Integer.toString(lastweek));
 			
 			
 			String[] projection = { ScreeningValuesTable.KEY_VALUE };
 			Cursor cursor = content.query(Provider.CONTENT_URI_SCREENING_VALUES, projection, where, null, null);
 			
 			if(cursor.getCount() == 0){
 				return "";
 			}
 			else{
 				cursor.moveToFirst();
 				String result = cursor.getString(0);
 				cursor.close();
 				return result;
 			}
 		}
 		
 		return "";		
 	}
 }
