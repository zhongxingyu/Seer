 package com.bigtheta.ragedice;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.math3.distribution.KolmogorovSmirnovDistribution;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class DiceRollDAO {
 
 	  // Database fields
 	  private SQLiteDatabase database;
 	  private MySQLiteHelper dbHelper;
 	  private String[] allColumns = {
 			  MySQLiteHelper.COLUMN_ID,
 			  MySQLiteHelper.COLUMN_ROLL_RESULT
 	      };
 	  
 	  private int cache_last_updated_on_roll = 0;
 	  private int[] cache_dice_rolls = new int[13];
 	  
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
 	    
 	    cache_dice_rolls[rollResult]++;
 	    cache_last_updated_on_roll++;
 	    
 	    return newDiceRoll;
 	  }
 
 	  public void deleteDiceRoll(DiceRoll roll) {
 		cache_dice_rolls[(int)roll.getRollResult()]--;
 		cache_last_updated_on_roll--;
 	    long id = roll.getId();
 	    System.out.println("DiceRoll deleted with id: " + id);
 	    database.delete(MySQLiteHelper.TABLE_DICE_ROLLS, MySQLiteHelper.COLUMN_ID
 	        + " = " + id, null);
 	  }
 	  
 	  public void deleteAllDiceRolls() {
 		  database.delete(MySQLiteHelper.TABLE_DICE_ROLLS, null, null);
 	  }
 
 	  public int getCountForRoll(int roll) {
 		  if (cache_last_updated_on_roll == getNumDiceRolls()) {
 			  return cache_dice_rolls[roll];
 		  } else {  // This shouldn't happen often, but it could on resets or
 			  		// unrolls.
 			  for (int i = 0; i <= 12; i++) {
 				  cache_dice_rolls[i] = 0;
 			  }
 			  for (DiceRoll dr : getAllDiceRolls()) {
 				  cache_dice_rolls[(int)dr.getRollResult()]++;
 			  }
 			  cache_last_updated_on_roll = getNumDiceRolls();
 			  
 		  }
 		  return cache_dice_rolls[roll];
 		  /*
 		  return (int)DatabaseUtils.queryNumEntries(database, dbHelper.TABLE_DICE_ROLLS,
 				  									dbHelper.COLUMN_ROLL_RESULT + "=" + Integer.toString(roll));
 		  */
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
 	   * Determines how likely it is that the dice are fair.
 	   * 
 	   * References:
 	   * 	http://www.physics.csbsju.edu/stats/KS-test.html
 	   *	http://en.wikipedia.org/wiki/Kolmogorov-Smirnov_test
 	   */
 	  public double calculateKSProbability() {
 
 		  int numRolls = getNumDiceRolls();
 		  if (numRolls == 0) {
 			  return 0.0;
 		  }
 		  
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
 	  
 	  /* 
 	   * Determines how likely it is that the dice are fair. This test abuses the
 	   * data in order to come up with a more extreme statistic.
 	   * 
 	   * References:
 	   * 	http://en.wikipedia.org/wiki/Kolmogorov-Smirnov_test
 	   *	http://www.physics.csbsju.edu/stats/KS-test.html
 	   */
 	  public double calculateKSProbabilityMaximized() {
 		  int numRolls = getNumDiceRolls();
 		  if (numRolls == 0) {
 			  return 0.0;
 		  }
 		  
 		  double obs = 0.0;
 		  double exp = 0.0;
 		  double obs_cff = 0.0;
 		  double exp_cff = 0.0;
 		  
 		  // Create the cff in as an extreme a way as possible. To do this, I
 		  // am first taking out values where the observed count is less than
 		  // the expected count. After this is done, it doesn't matter what
 		  // the rest of the cff is, because the d statistic will be the difference
 		  // between the two cffs.
 		  for (int i = 2; i <= 12; i++) {
 			  exp = getExpectedCount(i);
 			  obs = getCountForRoll(i);
 			  if (obs < exp) {
 				  obs_cff += obs / (double)numRolls;
 				  exp_cff += exp / (double)numRolls;
 			  }
 		  }
 
 		  double d = exp_cff - obs_cff;
 		  KolmogorovSmirnovDistribution dist = new KolmogorovSmirnovDistribution(numRolls);
 		  return dist.cdf(d);
 	  }
 	  
 	  public double getExpectedCount(int diceResult) {
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
 			  break;
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
