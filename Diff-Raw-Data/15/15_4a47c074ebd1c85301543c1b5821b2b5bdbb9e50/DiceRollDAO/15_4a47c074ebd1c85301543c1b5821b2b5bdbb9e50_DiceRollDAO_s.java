 package com.bigtheta.ragedice;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import org.apache.commons.math3.distribution.KolmogorovSmirnovDistribution;
 
 
 public class DiceRollDAO {
 
 	  // Database fields
 	  private SQLiteDatabase database;
 	  private MySQLiteHelper dbHelper;
 	  private String[] allColumns = {
 			  MySQLiteHelper.COLUMN_ID,
 			  MySQLiteHelper.COLUMN_ROLL_RESULT
 	      };
 	  
 	  public DiceRollDAO(Context context) {
 	    dbHelper = new MySQLiteHelper(context);
 	  }
 
 	  public void open() throws SQLException {
 	    database = dbHelper.getWritableDatabase();
 	  }
 
 	  public void close() {
 	    dbHelper.close();
 	  }
 
 	  public DiceRoll createDiceRoll(int rollResult) {
 	    ContentValues values = new ContentValues();
 	    values.put(MySQLiteHelper.COLUMN_ROLL_RESULT, rollResult);
 	    long insertId = database.insert(MySQLiteHelper.TABLE_DICE_ROLLS, null,
 	        values);
 	    Cursor cursor = database.query(MySQLiteHelper.TABLE_DICE_ROLLS,
 	        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
 	        null, null, null);
 	    cursor.moveToFirst();
 	    DiceRoll newDiceRoll = cursorToDiceRoll(cursor);
 	    cursor.close();
 	    return newDiceRoll;
 	  }
 
 	  public void deleteDiceRoll(DiceRoll roll) {
 	    long id = roll.getId();
 	    System.out.println("DiceRoll deleted with id: " + id);
 	    database.delete(MySQLiteHelper.TABLE_DICE_ROLLS, MySQLiteHelper.COLUMN_ID
 	        + " = " + id, null);
 	  }
 	  
 	  public void deleteAllDiceRolls() {
 		  database.delete(MySQLiteHelper.TABLE_DICE_ROLLS, null, null);
 	  }
 
 	  public int getCountForRoll(int roll) {
 		  /*
 		  String query = 
 			  "SELECT count(" + dbHelper.COLUMN_ROLL_RESULT + " ) " +
 			  "AS my_count " +
 			  "FROM " + dbHelper.TABLE_DICE_ROLLS + " " +
 			  "WHERE " + dbHelper.COLUMN_ROLL_RESULT + "=" + Integer.toString(roll);
 		  */
 		  return (int)DatabaseUtils.queryNumEntries(database, dbHelper.TABLE_DICE_ROLLS,
 				  									dbHelper.COLUMN_ROLL_RESULT + "=" + Integer.toString(roll));
 	  }
 	  
 	  public int getNumDiceRolls() {
 		  Cursor cursor = database.query(MySQLiteHelper.TABLE_DICE_ROLLS,
 					 					 allColumns, null, null, null, null, null);
 		  return cursor.getCount();
 	  }
 	  
 	  public DiceRoll getLastDiceRoll() {
 		  Cursor cursor = database.query(MySQLiteHelper.TABLE_DICE_ROLLS,
 				  						 allColumns, null, null, null, null, null);
 		  cursor.moveToPosition(cursor.getCount() - 1);
 		  return cursorToDiceRoll(cursor);
 	  }
 	  
 	  /* 
 	   * Determines how likely it is to see a result this extreme. This can be used
 	   * as a P value.
 	   * 
 	   * References:
 	   * 	http://www.physics.csbsju.edu/stats/KS-test.html
 	   *	http://www.physics.csbsju.edu/stats/KS-test.html
 	   */
 	  public double calculateKSProbability() {
 		  int numRolls = getNumDiceRolls();
 		  
 		  // Find the d statistic using the cff (cumulative fraction function).
 		  // The d statistic is the greatest deviation between the expected cff and
 		  // the observed cff.
 		  // Note: we could possibly make this statistic more extreme if we sorted the
 		  // observed data so that we evaluate all rolls that are less than the expected.
 		  double d = 0.0;
 		  double obs_cff = 0.0;
 		  double exp_cff = 0.0;
 		  for (int i = 2; i <=12; i++) {
 			  obs_cff += getCountForRoll(i) / (double)numRolls;
 			  exp_cff += getExpectedCount(i) / (double)numRolls;
 			  if (Math.abs(exp_cff - obs_cff) > d) {
 				  d = Math.abs(exp_cff - obs_cff);
 			  }
 		  }
 		  
		  KolmogorovSmirnovDistribution dist = new KolmogorovSmirnovDistribution(numRolls);
 		  
 		  return dist.cdf(d);
 	  }
 	  
 	  private double getExpectedCount(int diceResult) {
 		  int expected;
 		  switch (diceResult) {
 		  case 2:
 		  case 12:
 			  expected = 1;
 		  	  break;
 		  case 3:
 		  case 11:
 			  expected = 2;
 		  	  break;
 		  case 4:
 		  case 10:
 			  expected = 3;
 			  break;
 		  case 5:
 		  case 9:
 			  expected = 4;
 			  break;
 		  case 6:
 		  case 8:
 			  expected = 5;
 		  default:
 			  expected = 6;
 		  }
 		  
 		  return (expected / 36.0) * (double)getNumDiceRolls();
 	  }
 	  
 	  public List<DiceRoll> getAllDiceRolls() {
 	    List<DiceRoll> diceRolls = new ArrayList<DiceRoll>();
 
 	    Cursor cursor = database.query(MySQLiteHelper.TABLE_DICE_ROLLS,
 	    							   allColumns, null, null, null, null, null);
 
 	    cursor.moveToFirst();
 	    while (!cursor.isAfterLast()) {
 	      DiceRoll roll = cursorToDiceRoll(cursor);
 	      diceRolls.add(roll);
 	      cursor.moveToNext();
 	    }
 	    // Make sure to close the cursor
 	    cursor.close();
 	    return diceRolls;
 	  }
 
 	  private DiceRoll cursorToDiceRoll(Cursor cursor) {
 	    DiceRoll roll = new DiceRoll();
 	    roll.setId(cursor.getLong(0));
 	    roll.setRollResult(cursor.getInt(1));
 	    return roll;
 	  }
 }
