 package ch.hsr.wa.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class ScoreDBHandler {
 	private ScoreDB mScoreDb;
 
 	public ScoreDBHandler(Context context) {
 		mScoreDb = new ScoreDB(context);
 	}
 
 	private Cursor getCursorById(String pID) {
 		SQLiteDatabase dbConn = mScoreDb.getReadableDatabase();
 		Cursor c = dbConn.query(ScoreTable.TABLE_NAME, null, ScoreTable.ID + " = ?", new String[] { pID }, null, null, null);
 		c.moveToFirst();
 		dbConn.close();
 		return c;
 	}
 
 	private void update(String pID, ContentValues pContentValues) {
 		SQLiteDatabase dbConn = mScoreDb.getWritableDatabase();
 		dbConn.update(ScoreTable.TABLE_NAME, pContentValues, ScoreTableColumns.ID + " = ?", new String[] { pID });
 		dbConn.close();
 	}
 
 	public void insert(String pID, boolean pUnlocked, boolean pPassed, int pScore) {
 		SQLiteDatabase dbConn = mScoreDb.getWritableDatabase();

		ContentValues contentValue = new ContentValues();
		contentValue.put(ScoreTableColumns.ID, pID);
		contentValue.put(ScoreTableColumns.UNLOCKED, pUnlocked);
		contentValue.put(ScoreTableColumns.PASSED, pPassed);
		contentValue.put(ScoreTableColumns.SCORE, pScore);

		dbConn.insert(ScoreTable.TABLE_NAME, null, contentValue);
 
 		dbConn.close();
 	}
 
 	public boolean isUnlocked(String pID) {
 		Cursor c = getCursorById(pID);
 		int columnIndex = c.getColumnIndex(ScoreTableColumns.UNLOCKED);
 		return c.getInt(columnIndex) == 1;
 	}
 
 	public boolean isPassed(String pID) {
 		Cursor c = getCursorById(pID);
 		int columnIndex = c.getColumnIndex(ScoreTableColumns.PASSED);
 		return c.getInt(columnIndex) == 1;
 	}
 
 	public int getScore(String pID) {
 		Cursor c = getCursorById(pID);
 		int columnIndex = c.getColumnIndex(ScoreTableColumns.SCORE);
 		return c.getInt(columnIndex);
 	}
 
 	public void setUnlocked(String pID) {
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(ScoreTableColumns.UNLOCKED, true);
 		update(pID, contentValues);
 	}
 
 	public void setPassed(String pID) {
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(ScoreTableColumns.PASSED, true);
 		update(pID, contentValues);
 	}
 
 	public void setScore(String pID, int pScore) {
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(ScoreTableColumns.SCORE, pScore);
 		update(pID, contentValues);
 	}
 }
