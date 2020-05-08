 package com.bigtheta.ragedice;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import org.apache.commons.math3.distribution.KolmogorovSmirnovDistribution;
 import org.apache.commons.math3.distribution.NormalDistribution;
 import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 
 public class DiceRoll {
     private static String[] tableDiceRollColumns = {
         MySQLiteHelper.COLUMN_ID,
         MySQLiteHelper.COLUMN_PLAYER_ID,
         MySQLiteHelper.COLUMN_TIME_CREATED
     };
 
     long m_id;
     long m_playerId;
     Timestamp m_timeCreated;
 
     private static HashMap<Integer, Integer> cacheGetObservedRolls = null;
     private static HashMap<Long, Long> cacheGetTotalTimes = null;
     private static HashMap<Long, Long> cacheGetRollsPerPlayer = null;
 
     public DiceRoll(Player player) {
         m_playerId = player.getId();
         Calendar calendar = Calendar.getInstance();
 
         m_timeCreated = new Timestamp(calendar.getTime().getTime());
         ContentValues values = new ContentValues();
         values.put(MySQLiteHelper.COLUMN_PLAYER_ID, m_playerId);
         values.put(MySQLiteHelper.COLUMN_TIME_CREATED, m_timeCreated.toString());
         DiceRoll lastRoll = getLastDiceRoll(player.getGameId());
         m_id = MainActivity.getDatabase().insert(MySQLiteHelper.TABLE_DICE_ROLL,
                                                  null, values);
 
         if (lastRoll != null &&
             cacheGetTotalTimes != null &&
             cacheGetTotalTimes.containsKey(lastRoll.getPlayerId()) &&
             cacheGetRollsPerPlayer != null &&
             cacheGetRollsPerPlayer.containsKey(lastRoll.getPlayerId())) {
 
             cacheGetTotalTimes.put(lastRoll.getPlayerId(),
                                    cacheGetTotalTimes.get(lastRoll.getPlayerId()) +
                                    m_timeCreated.getTime() -
                                    lastRoll.getTimeCreated().getTime());
             cacheGetRollsPerPlayer.put(lastRoll.getPlayerId(),
                                        cacheGetRollsPerPlayer.get(lastRoll.getPlayerId()) + 1);
         } else {
             initializeCachesForAverageTimes(player.getGameId());
         }
 
         for (DieDescription dd : DieDescription.retrieveAll(
                                                 Player.retrieve(m_playerId).getGameId())) {
             new DieResult(this, dd);
         }
 
         if (cacheGetObservedRolls != null) {
             int result = getTotalResult();
             if (!cacheGetObservedRolls.containsKey(result)) {
                 cacheGetObservedRolls.put(result, 1);
             } else {
                 cacheGetObservedRolls.put(result, cacheGetObservedRolls.get(result) + 1);
             }
         }
     }
 
     private DiceRoll(long id) {
         m_id = id;
         Cursor cursor = MainActivity.getDatabase().query(
                 MySQLiteHelper.TABLE_DICE_ROLL,
                 tableDiceRollColumns, MySQLiteHelper.COLUMN_ID + " = " + id,
                 null, null, null, null);
         cursor.moveToFirst();
         m_playerId = cursor.getLong(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_PLAYER_ID));
         m_timeCreated = Timestamp.valueOf(cursor.getString(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_TIME_CREATED)));
         cursor.close();
     }
 
     public static DiceRoll retrieve(long id) {
         return new DiceRoll(id);
     }
 
     public int getTotalResult() {
         Integer result = 0;
         for (DieResult dr : DieResult.getDieResults(this)) {
             DieDescription dd = DieDescription.retrieve(dr.getDieDescriptionId());
             if (dd.getDisplayType().equals(DieDescription.NUMERIC)) {
                 result += dr.getDieResult();
             }
         }
         return result;
     }
 
     public static void resetCaches() {
         cacheGetObservedRolls = null;
         cacheGetTotalTimes = null;
         cacheGetRollsPerPlayer = null;
     }
 
     public void delete() {
         resetCaches();
         MainActivity.getDatabase().delete(
                 MySQLiteHelper.TABLE_DICE_ROLL,
                 MySQLiteHelper.COLUMN_ID + " = " + m_id, null);
     }
 
     public static void clear(long gameId) {
         resetCaches();
         MainActivity.getDatabase().delete(MySQLiteHelper.TABLE_DICE_ROLL, null, null);
     }
 
     public static DiceRoll getLastDiceRoll(long gameId) {
         DiceRoll ret;
         if (!isEmpty()) {
             String query_str = "SELECT MAX(" + MySQLiteHelper.COLUMN_ID + ") "
                              + "AS _id FROM " + MySQLiteHelper.TABLE_DICE_ROLL;
             try {
                 Cursor cursor = MainActivity.getDatabase().rawQuery(query_str, null);
                 cursor.moveToFirst();
                 ret = DiceRoll.retrieve(cursor.getLong(0));
                 cursor.close();
             } catch (SQLiteException err) {
                 return null;
             }
         } else {
             ret = null;
         }
         return ret;
     }
 
     public long getId() {
         return m_id;
     }
 
     public long getPlayerId() {
         return m_playerId;
     }
 
     public Timestamp getTimeCreated() {
         return m_timeCreated;
     }
 
     public static HashMap<Long, Long> getAverageTimes(long gameId) {
         HashMap<Long, Long> times = getTotalTimes(gameId);
         HashMap<Long, Long> moves = getRollsPerPlayer(gameId);
         HashMap<Long, Long> ret = new HashMap<Long, Long>();
         for (Long playerId : times.keySet()) {
             try {
                 ret.put(playerId, times.get(playerId) / moves.get(playerId));
             } catch (Exception err) {
                 ret.put(playerId, 0L);
             }
         }
         return ret;
     }
 
     private static void initializeCachesForAverageTimes(long gameId) {
         if (cacheGetTotalTimes == null) {
             cacheGetTotalTimes = new HashMap<Long, Long>();
             cacheGetRollsPerPlayer = new HashMap<Long, Long>();
         }
 
         DiceRoll current = getFirstDiceRoll(gameId);
         DiceRoll next = getNextDiceRoll(current);
 
         for (Player player : Player.getPlayers(gameId)) {
             cacheGetTotalTimes.put(player.getId(), 0L);
             cacheGetRollsPerPlayer.put(player.getId(), 0L);
         }
 
         long delta = 0;
         while (next != null) {
             long key = current.getPlayerId();
             delta = next.getTimeCreated().getTime() - current.getTimeCreated().getTime();
             cacheGetTotalTimes.put(key, cacheGetTotalTimes.get(key) + delta);
             cacheGetRollsPerPlayer.put(key, cacheGetRollsPerPlayer.get(key) + 1);
             current = next;
             next = getNextDiceRoll(current);
         }
 
         if (cacheGetTotalTimes == null) {
             cacheGetTotalTimes = new HashMap<Long, Long>();
         }
 
         if (cacheGetRollsPerPlayer == null) {
             cacheGetRollsPerPlayer = new HashMap<Long, Long>();
         }
     }
 
     private static HashMap<Long, Long> getTotalTimes(long gameId) {
         if (cacheGetTotalTimes == null) {
             initializeCachesForAverageTimes(gameId);
         }
         return cacheGetTotalTimes;
     }
 
     private static HashMap<Long, Long> getRollsPerPlayer(long gameId) {
         if (cacheGetTotalTimes == null) {
             initializeCachesForAverageTimes(gameId);
         }
         return cacheGetRollsPerPlayer;
     }
 
     public static DiceRoll getFirstDiceRoll(long gameId) {
         DiceRoll ret;
         if (!isEmpty()) {
             String query_str = "SELECT MIN(" + MySQLiteHelper.COLUMN_ID + ") "
                              + "AS _id FROM " + MySQLiteHelper.TABLE_DICE_ROLL;
             try {
                 Cursor cursor = MainActivity.getDatabase().rawQuery(query_str, null);
                 cursor.moveToFirst();
                 ret = DiceRoll.retrieve(cursor.getLong(0));
                 cursor.close();
             } catch (SQLiteException err) {
                 return null;
             }
         } else {
             ret = null;
         }
         return ret;
     }
 
     public static DiceRoll getNextDiceRoll(DiceRoll dr) {
         DiceRoll ret;
         if (!isEmpty()) {
             Cursor cursor = MainActivity.getDatabase().query(
                     MySQLiteHelper.TABLE_DICE_ROLL,
                     tableDiceRollColumns, MySQLiteHelper.COLUMN_ID + " > " +
                             Long.toString(dr.getId()),
                     null, null, null, null);
             cursor.moveToFirst();
             if (cursor.getCount() > 0) {
                 ret = DiceRoll.retrieve(cursor.getLong(0));
             } else {
                 ret = null;
             }
             cursor.close();
         } else {
             ret = null;
         }
         return ret;
     }
 
     public static ArrayList<DiceRoll> getDiceRolls(long gameId) {
         Cursor cursor = MainActivity.getDatabase().query(
                 MySQLiteHelper.TABLE_DICE_ROLL,
                 new String[] {MySQLiteHelper.COLUMN_ID},
                 null, null, null, null, null);
         cursor.moveToFirst();
         ArrayList<DiceRoll> rolls = new ArrayList<DiceRoll>();
         while (!cursor.isAfterLast()) {
             rolls.add(new DiceRoll(cursor.getLong(0)));
             cursor.moveToNext();
         }
         cursor.close();
         return rolls;
     }
 
     public static int getNumDiceRolls() {
         Cursor cursor = MainActivity.getDatabase().query(MySQLiteHelper.TABLE_DICE_ROLL,
                                                          null, null, null, null, null, null);
         int retval = cursor.getCount();
         cursor.close();
         return retval;
     }
 
     public static HashMap<Integer, Integer> getObservedRolls(long gameId) {
         if (cacheGetObservedRolls != null) {
             // Consistency check. This catches the issue of the database being deleted.
             int numRollsCache = 0;
             int numRollsDB = getNumDiceRolls();
 
             for (Integer result : cacheGetObservedRolls.keySet()) {
                 numRollsCache += cacheGetObservedRolls.get(result);
             }
 
             if (numRollsCache != numRollsDB) {
                 cacheGetObservedRolls = null;
             }
         }
 
         if (cacheGetObservedRolls == null) {
             cacheGetObservedRolls = new HashMap<Integer, Integer>();
             Cursor cursor = MainActivity.getDatabase().query(
                     MySQLiteHelper.TABLE_DICE_ROLL,
                     null, null, null, null, null, null);
             cursor.moveToFirst();
             while (!cursor.isAfterLast()) {
                 DiceRoll roll = new DiceRoll(
                             cursor.getLong(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
                 int result = roll.getTotalResult();
 
                 if (cacheGetObservedRolls.containsKey(result)) {
                     cacheGetObservedRolls.put(result, cacheGetObservedRolls.get(result) + 1);
                 } else {
                     cacheGetObservedRolls.put(result, 1);
                 }
                 cursor.moveToNext();
             }
             cursor.close();
         }
         return cacheGetObservedRolls;
     }
 
     /*
      * Determines the Kolmogorov-Smirnov test statistics.
      *
      * References:
      *     http://www.physics.csbsju.edu/stats/KS-test.html
      *    http://en.wikipedia.org/wiki/Kolmogorov-Smirnov_test
      */
     public static double calculateKSTestStatistic(long gameId) {
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
         double delta = 1.0 / numRolls;
         double exp_cff = 0.0;
         double obs_cff = 0.0;
 
         HashMap<Integer, Double> pmf = DieDescription.getPMF(gameId);
         HashMap<Integer, Integer> observed = getObservedRolls(gameId);
 
         for (Integer result : pmf.keySet()) {
             exp_cff = exp_cff + delta * (pmf.get(result) * numRolls);
             if (observed.containsKey(result)) {
                 obs_cff = obs_cff + delta * (observed.get(result));
             }
             d = Math.max(d, Math.abs(exp_cff - obs_cff));
         }
         return d;
     }
 
     public static double calculateKSPValue(long gameId) {
         double testStatistic = calculateKSTestStatistic(gameId);
         int numRolls = getNumDiceRolls();
         if (numRolls > 0) {
             KolmogorovSmirnovDistribution dist = new KolmogorovSmirnovDistribution(numRolls);
             return 1.0 - dist.cdf(testStatistic);
         } else {
             return 1.0;
         }
     }
 
     public static SummaryStatistics getObservedSummaryStatistics(long gameId) {
         SummaryStatistics retval = new SummaryStatistics();
         HashMap<Integer, Integer> observedRolls = DiceRoll.getObservedRolls(gameId);
         for (Integer key : observedRolls.keySet()) {
             for (int i = 0; i < observedRolls.get(key); i++) {
                 retval.addValue(key);
             }
         }
         return retval;
     }
 
     public static SummaryStatistics getExpectedSummaryStatistics(long gameId) {
         SummaryStatistics retval = new SummaryStatistics();
         ArrayList<DieDescription> descriptions = DieDescription.retrieveAll(gameId);
         HashMap<Integer, Integer> nonNormedPMF = DieDescription.getNonNormedPMF(descriptions);
         for (Integer key : nonNormedPMF.keySet()) {
             for (int i = 0; i < nonNormedPMF.get(key); i++) {
                 retval.addValue(key);
             }
         }
         return retval;
     }
 
     /*
      * See http://en.wikipedia.org/wiki/Central_limit_theorem
      * (X_bar - mu) / (sigma/sqrt(n)) ~~ Norm(0, 1)
      */
     public static double calculateCentralLimitProbabilityPValue(long gameId) {
         SummaryStatistics observedSummaryStatistics = getObservedSummaryStatistics(gameId);
         if (getNumDiceRolls() < 4) {
             return 1.0;
         }
         SummaryStatistics expectedSummaryStatistics = getExpectedSummaryStatistics(gameId);
 
         double X_bar = observedSummaryStatistics.getMean();
         double mu = expectedSummaryStatistics.getMean();
         double sigma = expectedSummaryStatistics.getStandardDeviation();
         double statistic = Math.abs((X_bar - mu) / (sigma / Math.sqrt(getNumDiceRolls())));
         NormalDistribution standardNormal = new NormalDistribution();
         double pValue = 1.0 - standardNormal.cumulativeProbability(-statistic, statistic);
         return pValue;
     }
 
     private static boolean isEmpty() {
         boolean retval;
         Cursor cursor = MainActivity.getDatabase().query(
                 MySQLiteHelper.TABLE_DICE_ROLL,
                 tableDiceRollColumns,
                 null, null, null, null, null);
         if (cursor.getCount() > 0) {
             retval = false;
         } else {
             retval = true;
         }
         cursor.close();
         return retval;
     }
 }
 
