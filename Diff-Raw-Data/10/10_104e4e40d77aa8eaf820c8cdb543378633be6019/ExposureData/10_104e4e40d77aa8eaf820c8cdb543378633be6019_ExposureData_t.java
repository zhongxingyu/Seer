 package com.sportsfire.exposure.objects;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 
 import com.sportsfire.exposure.db.PlayerSessionsTable;
 import com.sportsfire.exposure.db.SquadSessionsTable;
 import com.sportsfire.exposure.db.UpdatesTable;
 import com.sportsfire.exposure.sync.Provider;
 
 public class ExposureData {
 
 	private String seasonID;
 	private ContentResolver content;
 	private Cursor cursor;
 	
 	public ExposureData(Context context, String seasonID) {
 		content = context.getContentResolver();
 		this.seasonID = seasonID;
 	}
 	
 	public void closeCursor(){
 		try {
 			cursor.close();
 		} catch (Exception e) {
 		}
 	}
 
 	public boolean setSquadExposure(String squadID, String type, String duration, String date, String value) {
 		if (value == "")
 			return false;
 		try {
 			Double.parseDouble(value);
 		} catch (NumberFormatException e) {
 			return false;
 		}
 
 		final String where = SquadSessionsTable.KEY_SQUAD_ID + " = '" + squadID + "' and "
 				+ SquadSessionsTable.KEY_SEASON_ID + " = '" + seasonID + "' and " + SquadSessionsTable.KEY_DATE
 				+ " = strftime('%w-%W','" + date + "')";
 
 		String[] projection = { SquadSessionsTable.KEY_ID };
 		cursor = content.query(Provider.CONTENT_URI_SQUAD_SESSIONS, projection, where, null, null);
 
 		ContentValues values = new ContentValues();
 
 		if (cursor.getCount() == 0) {
 			values.put(SquadSessionsTable.KEY_TYPE, type);
 			values.put(SquadSessionsTable.KEY_SQUAD_ID, squadID);
 			values.put(SquadSessionsTable.KEY_SEASON_ID, seasonID);
 			values.put(SquadSessionsTable.KEY_DURATION, duration);
 			values.put(SquadSessionsTable.KEY_SESSION, value);
 			values.put(SquadSessionsTable.KEY_DATE, date);
 			Uri uri = content.insert(Provider.CONTENT_URI_SQUAD_SESSIONS, values);
 			String insertID = uri.getLastPathSegment();
 
 			values.clear();
 			values.put(UpdatesTable.KEY_VALUE_ID, Integer.parseInt(insertID));
 
 			content.insert(Provider.CONTENT_URI_SCREENING_UPDATES, values);
 
 		} else {
 			values.put(SquadSessionsTable.KEY_SESSION, value);
 			cursor.moveToFirst();
 			content.update(Provider.CONTENT_URI_SQUAD_SESSIONS, values, where, null);
 			values.clear();
 			values.put(UpdatesTable.KEY_VALUE_ID, cursor.getInt(0));
 			values.put(UpdatesTable.KEY_TABLE_NAME, SquadSessionsTable.TABLE_NAME);
 			content.insert(Provider.CONTENT_URI_SCREENING_UPDATES, values);
 
 		}
 
 		cursor.close();
 
 		return true;
 	}
 	
 	public boolean setPlayerExposure(String playerID, String type, String duration, String date, String value,
 			String preTraining, String postTraining) {
 		if (value == "")
 			return false;
 		try {
 			Double.parseDouble(value);
 		} catch (NumberFormatException e) {
 			return false;
 		}
 
 		final String where = PlayerSessionsTable.KEY_PLAYER_ID + " = '" + playerID + "' and "
 				+ PlayerSessionsTable.KEY_SEASON_ID + " = '" + seasonID + "' and " + PlayerSessionsTable.KEY_DATE
 				+ " = strftime('%w-%W','" + date + "')";
 
 		String[] projection = { PlayerSessionsTable.KEY_ID };
 		cursor = content.query(Provider.CONTENT_URI_PLAYER_SESSIONS, projection, where, null, null);
 
 		ContentValues values = new ContentValues();
 
 		if (cursor.getCount() == 0) {
 			values.put(PlayerSessionsTable.KEY_TYPE, type);
 			values.put(PlayerSessionsTable.KEY_PLAYER_ID, playerID);
 			values.put(PlayerSessionsTable.KEY_SEASON_ID, seasonID);
 			values.put(PlayerSessionsTable.KEY_DURATION, duration);
 			values.put(PlayerSessionsTable.KEY_SESSION, value);
 			values.put(PlayerSessionsTable.KEY_DATE, date);
 			Uri uri = content.insert(Provider.CONTENT_URI_PLAYER_SESSIONS, values);
 			String insertID = uri.getLastPathSegment();
 
 			values.clear();
 			values.put(UpdatesTable.KEY_VALUE_ID, Integer.parseInt(insertID));
 
 			content.insert(Provider.CONTENT_URI_SCREENING_UPDATES, values);
 
 		} else {
 			values.put(PlayerSessionsTable.KEY_SESSION, value);
 			values.put(PlayerSessionsTable.KEY_TYPE, type);
 			values.put(PlayerSessionsTable.KEY_POST_TRAINING, preTraining);
 			values.put(PlayerSessionsTable.KEY_PRE_TRAINING, postTraining);
 			cursor.moveToFirst();
 			content.update(Provider.CONTENT_URI_PLAYER_SESSIONS, values, where, null);
 			values.clear();
 			values.put(UpdatesTable.KEY_VALUE_ID, cursor.getInt(0));
 			values.put(UpdatesTable.KEY_TABLE_NAME, PlayerSessionsTable.TABLE_NAME);
 			content.insert(Provider.CONTENT_URI_SCREENING_UPDATES, values);
 
 		}
 
 		cursor.close();
 
 		return true;
 	}
 	public ArrayList<String> getSquadExposure(String squadID, String date) {
		ArrayList<String> data = new ArrayList<String>();
 		final String where = SquadSessionsTable.KEY_SQUAD_ID + " = '" + squadID + "' and "
 				+ SquadSessionsTable.KEY_SEASON_ID + " = '" + seasonID + "' and " + SquadSessionsTable.KEY_DATE
 				+ " = strftime('%w-%W','" + date + "')";
 
		String[] projection = { SquadSessionsTable.KEY_SESSION, SquadSessionsTable.KEY_DURATION, SquadSessionsTable.KEY_TYPE};
 		cursor = content.query(Provider.CONTENT_URI_SQUAD_SESSIONS, projection, where, null, null);
 
 		ContentValues values = new ContentValues();
 		if (cursor.getCount() == 0) {
 			cursor.close();
 			return null;
 		}else{
 			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
 				try {
					data.add(cursor.getString(0));
 				} catch (Exception e) {
 				}
 
 			}
 			
 		}
		return data;
 	}
 
 }
