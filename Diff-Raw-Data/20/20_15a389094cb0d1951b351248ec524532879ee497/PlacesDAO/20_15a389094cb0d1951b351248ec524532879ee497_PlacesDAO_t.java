 package com.quizz.places.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.util.Log;
 
import com.google.ads.c;
 import com.quizz.core.db.DbHelper;
 import com.quizz.core.db.QuizzDAO;
 import com.quizz.core.models.Level;
 import com.quizz.core.models.Section;
 import com.quizz.core.models.Stat;
 import com.quizz.places.R;
 
 public class PlacesDAO {
 
 	private Context mContext;
 
 	public PlacesDAO(Context context) {
 		this.mContext = context;
 	}
 
 	public List<Stat> getStats() {
 
 		String sqlQuery = "SELECT" + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_SECTIONS + " WHERE "
 				+ DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_UNLOCKED
 				+ " = " + Section.SECTION_UNLOCKED + ") AS sections_unlocked,"
 				+ " (SELECT" + " COUNT(" + DbHelper.TABLE_SECTIONS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_SECTIONS + ") AS sections_total,"
 				+ " (SELECT" + " COUNT(" + DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")" 
 				+ " FROM " + DbHelper.TABLE_LEVELS + " " + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_STATUS + " = "
 				+ Level.STATUS_LEVEL_CLEAR + ") AS levels_clear," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID
 				+ ")" + " FROM " + DbHelper.TABLE_LEVELS + ") AS levels_total," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_STATUS + " = "
 				+ Level.STATUS_LEVEL_CLEAR + " AND " + DbHelper.TABLE_LEVELS
 				+ "." + DbHelper.COLUMN_DIFFICULTY + " = \"" + Level.LEVEL_EASY
 				+ "\"" + ") AS levels_easy_clear," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_DIFFICULTY
 				+ " = \"" + Level.LEVEL_EASY + "\"" + ") AS levels_easy_total,"
 				+ " (SELECT" + " COUNT(" + DbHelper.TABLE_LEVELS + "."
 				+ DbHelper.COLUMN_ID + ")" + " FROM " + DbHelper.TABLE_LEVELS
 				+ " WHERE " + DbHelper.TABLE_LEVELS + "."
 				+ DbHelper.COLUMN_STATUS + " = " + Level.STATUS_LEVEL_CLEAR
 				+ " AND " + DbHelper.TABLE_LEVELS + "."
 				+ DbHelper.COLUMN_DIFFICULTY + " = \"" + Level.LEVEL_MEDIUM
 				+ "\"" + ") AS levels_medium_clear," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_DIFFICULTY
 				+ " = \"" + Level.LEVEL_MEDIUM + "\""
 				+ ") AS levels_medium_total," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_STATUS + " = "
 				+ Level.STATUS_LEVEL_CLEAR + " AND " + DbHelper.TABLE_LEVELS
 				+ "." + DbHelper.COLUMN_DIFFICULTY + " = \"" + Level.LEVEL_HARD
 				+ "\"" + ") AS levels_hard_clear," + " (SELECT" + " COUNT("
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_ID + ")"
 				+ " FROM " + DbHelper.TABLE_LEVELS + " WHERE "
 				+ DbHelper.TABLE_LEVELS + "." + DbHelper.COLUMN_DIFFICULTY
 				+ " = \"" + Level.LEVEL_HARD + "\"" + ") AS levels_hard_total"
 				+ " FROM " + DbHelper.TABLE_SECTIONS + " LEFT JOIN "
 				+ DbHelper.TABLE_LEVELS;
 
 		Cursor cursor = QuizzDAO.INSTANCE.getDbHelper().getReadableDatabase()
 				.rawQuery(sqlQuery, null);
 
 		return this.cursorToStat(cursor);
 	}
 
 	public void resetDB() {
 		ContentValues cv = new ContentValues();
		
 		cv.put(DbHelper.COLUMN_STATUS, Level.STATUS_LEVEL_UNCLEAR);
 		QuizzDAO.INSTANCE.getDbHelper().getWritableUserdataDatabase()
 				.update(DbHelper.TABLE_USERDATA, cv, 
 						DbHelper.COLUMN_REF_FROM_TABLE + " = \"" + DbHelper.TABLE_LEVELS + "\"",
 						null);
		
 		cv.clear();
		
 		cv.put(DbHelper.COLUMN_STATUS, Section.SECTION_LOCKED);
 		QuizzDAO.INSTANCE.getDbHelper().getWritableUserdataDatabase()
 			.update(DbHelper.TABLE_USERDATA, cv, 
					DbHelper.COLUMN_REF_FROM_TABLE + " = \"" + DbHelper.TABLE_SECTIONS + "\""
 					+ " AND " + DbHelper.COLUMN_REF + " != \"section_1\"", null);
 	}
 	
 	public List<Stat> cursorToStat(Cursor cursor) {
 
 		ArrayList<Stat> stats = new ArrayList<Stat>();
 
 		cursor.moveToFirst();
 		stats.add(new Stat(R.drawable.sections, mContext.getString(R.string.unlocked_lvl), cursor
 				.getInt(cursor.getColumnIndex("sections_unlocked")), cursor
 				.getInt(cursor.getColumnIndex("sections_total")), true));
 		stats.add(new Stat(R.drawable.levels, mContext.getString(R.string.places_found), cursor
 				.getInt(cursor.getColumnIndex("levels_clear")), cursor
 				.getInt(cursor.getColumnIndex("levels_total")), true));
 		stats.add(new Stat(R.drawable.easy, mContext.getString(R.string.easy_found), cursor
 				.getInt(cursor.getColumnIndex("levels_easy_clear")), cursor
 				.getInt(cursor.getColumnIndex("levels_easy_total")), true));
 		stats.add(new Stat(R.drawable.medium, mContext.getString(R.string.medium_found), cursor
 				.getInt(cursor.getColumnIndex("levels_medium_clear")), cursor
 				.getInt(cursor.getColumnIndex("levels_medium_total")), true));
 		stats.add(new Stat(R.drawable.hard, mContext.getString(R.string.hard_found),
 				cursor.getInt(cursor.getColumnIndex("levels_hard_clear")),
 				cursor.getInt(cursor.getColumnIndex("levels_hard_total")), true));
 		stats.add(new Stat(R.drawable.hint, mContext.getString(R.string.used_hints), cursor
 				.getInt(cursor.getColumnIndex("revealed_hints")), cursor
 				.getInt(cursor.getColumnIndex("total_hints")), false));
 		cursor.close();
 
 		return stats;
 	}
 }
