 package com.bigtheta.ragedice;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.achartengine.chart.BarChart;
 import org.apache.commons.math3.distribution.NormalDistribution;
 import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
 
 import android.annotation.SuppressLint;
 import android.content.ContentValues;
 import android.database.Cursor;
 
 @SuppressLint("UseSparseArrays")
 public class DieDescription {
     private static String[] tableDieDescriptionColumns = {
         MySQLiteHelper.COLUMN_GAME_ID,
         MySQLiteHelper.COLUMN_NUM_LOW_FACE,
         MySQLiteHelper.COLUMN_NUM_HIGH_FACE,
         MySQLiteHelper.COLUMN_BASE_IDENTIFIER_NAME,
         MySQLiteHelper.COLUMN_BACKGROUND_COLOR,
         MySQLiteHelper.COLUMN_IMAGE_VIEW_RESOURCE,
         MySQLiteHelper.COLUMN_DISPLAY_TYPE
     };
 
     public static final String NUMERIC = "NUMERIC";
     public static final String SHIP = "SHIP";
 
     long m_id;
     long m_gameId;
     int m_numLowFace;
     int m_numHighFace;
     String m_baseIdentifierName;
     int m_backgroundColorResource;
     int m_imageViewResource;
     String m_displayType;
 
     // This opens an oportunity to break... if the database is cleared, this won't be.
     private static HashMap<Long, ArrayList<DieDescription> > cacheRetrieveAll = null;
     private static HashMap<Long, HashMap<Integer, Double> > cacheGetPMF = null;
 
     public DieDescription(Game game, int numLowFace, int numHighFace,
                           String baseIdentifierName,
                           int backgroundColorResource, int imageViewResource,
                           String displayType) {
         BarChart bc;
         m_numLowFace = numLowFace;
         m_numHighFace = numHighFace;
         m_baseIdentifierName = baseIdentifierName;
         m_backgroundColorResource = backgroundColorResource;
         m_imageViewResource = imageViewResource;
         m_displayType = displayType;
 
         ContentValues values = new ContentValues();
         values.put(MySQLiteHelper.COLUMN_GAME_ID, game.getId());
         values.put(MySQLiteHelper.COLUMN_NUM_LOW_FACE, m_numLowFace);
         values.put(MySQLiteHelper.COLUMN_NUM_HIGH_FACE, m_numHighFace);
         values.put(MySQLiteHelper.COLUMN_BASE_IDENTIFIER_NAME,
                    m_baseIdentifierName);
         values.put(MySQLiteHelper.COLUMN_BACKGROUND_COLOR, m_backgroundColorResource);
         values.put(MySQLiteHelper.COLUMN_IMAGE_VIEW_RESOURCE,
                    m_imageViewResource);
         values.put(MySQLiteHelper.COLUMN_DISPLAY_TYPE, m_displayType);
         m_id = MainActivity.getDatabase().insert(MySQLiteHelper.TABLE_DIE_DESCRIPTION,
                                                  null, values);
     }
 
     private DieDescription(long id) {
         m_id = id;
         Cursor cursor = MainActivity.getDatabase().query(
                 MySQLiteHelper.TABLE_DIE_DESCRIPTION,
                 tableDieDescriptionColumns,
                 MySQLiteHelper.COLUMN_ID + " = " + id,
                 null, null, null, null);
         cursor.moveToFirst();
         m_gameId = cursor.getLong(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_GAME_ID));
         m_numLowFace = cursor.getInt(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_NUM_LOW_FACE));
         m_numHighFace = cursor.getInt(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_NUM_HIGH_FACE));
         m_baseIdentifierName = cursor.getString(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_BASE_IDENTIFIER_NAME));
         m_backgroundColorResource = cursor.getInt(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_BACKGROUND_COLOR));
         m_imageViewResource = cursor.getInt(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_IMAGE_VIEW_RESOURCE));
         m_displayType = cursor.getString(
                 cursor.getColumnIndexOrThrow(MySQLiteHelper.COLUMN_DISPLAY_TYPE));
 
         cursor.close();
     }
 
     public static DieDescription retrieve(long id) {
         return new DieDescription(id);
     }
 
     public static ArrayList<DieDescription> retrieveAll(long gameId) {
         if (cacheRetrieveAll == null || !cacheRetrieveAll.containsKey(gameId)) {
             ArrayList<DieDescription> ret = new ArrayList<DieDescription>();
             Cursor cursor = MainActivity.getDatabase().query(
                     MySQLiteHelper.TABLE_DIE_DESCRIPTION,
                     new String[] {MySQLiteHelper.COLUMN_ID},
                     MySQLiteHelper.COLUMN_GAME_ID + " = " + gameId,
                     null, null, null, null);
             cursor.moveToFirst();
             while (!cursor.isAfterLast()) {
                 ret.add(DieDescription.retrieve(cursor.getLong(0)));
                 cursor.moveToNext();
             }
             cursor.close();
             if (cacheRetrieveAll == null) {
                 cacheRetrieveAll = new HashMap<Long, ArrayList<DieDescription> >();
             }
             cacheRetrieveAll.put(gameId, ret);
             return ret;
         } else {
             return cacheRetrieveAll.get(gameId);
         }
     }
 
     public long getId() {
         return m_id;
     }
 
     public long getGameId() {
         return m_gameId;
     }
 
     public int getNumLowFace() {
         return m_numLowFace;
     }
 
     public int getNumHighFace() {
         return m_numHighFace;
     }
 
     public String getBaseIdentifierName() {
         return m_baseIdentifierName;
     }
 
     public int getBackgroundColorResource() {
         return m_backgroundColorResource;
     }
 
    /*
     * TODO
     * Change to getImageViewTag()
     */
     public int getImageViewResource() {
         return m_imageViewResource;
     }
 
     public String getDisplayType() {
         return m_displayType;
     }
 
     public static String getKSDescription(long gameId) {
         String update = new String();
         update += "This test uses the Kolmogorov-Smirnov test to determine "
                 + "how likely it is that this collection of dice rolls came "
                 + "from a fair distribution. The KS statistic is the maximum "
                 + "difference between the observed and the expected cumulative "
                 + "fraction function (cff).";
         update += "After " + Integer.toString(DiceRoll.getNumDiceRolls())
                 + " dice rolls, this value is ";
         update += Double.toString(DiceRoll.calculateKSTestStatistic(gameId));
         update += ". As the maximum difference between the two cffs becomes small, "
                 + "the likelihood that the observed dice rolls were produced by 'fair' "
                 + "dice becomes large... unless we used a biased random number generator.";
         return update;
     }
 
     public static String getCLTDescription(long gameId) {
         String update = new String();
         SummaryStatistics observedSummaryStatistics = DiceRoll.getObservedSummaryStatistics(gameId);
         if (observedSummaryStatistics.getN() < 2) {
             return update;
         }
         update += "This test adds up all dice rolls in this game and detemines "
                 + "how likely it is that the sum is as extreme, or more extreme, "
                 + "as it is. This test works because the Central Limit Theorem "
                 + "states that the sum of all dice rolls is a normal random "
                 + "variable.";
 
         update += "\n\nAfter " + Long.toString(observedSummaryStatistics.getN()) + " rolls, the current sum is ";
         update += new DecimalFormat("#.##").format(observedSummaryStatistics.getSum());
         update += " and the observed average is ";
         update += new DecimalFormat("#.##").format(observedSummaryStatistics.getMean());
         update += ".";
 
         SummaryStatistics expectedSummaryStatistics = DiceRoll.getExpectedSummaryStatistics(gameId);
 
         update += "\n\nAssuming that the dice are fair, the expected sum would be ";
         update += new DecimalFormat("#.##").format(expectedSummaryStatistics.getMean() * observedSummaryStatistics.getN());
         update += " and the expected average would be ";
         update += new DecimalFormat("#.##").format(expectedSummaryStatistics.getMean());
         update += ".";
 
         Long sizeN = observedSummaryStatistics.getN();
         NormalDistribution normalDistribution = new NormalDistribution(sizeN * expectedSummaryStatistics.getMean(),
                                                                        Math.sqrt(sizeN) * expectedSummaryStatistics.getStandardDeviation());
         update += "\n\nThe 95% confidence interval for the sum of all dice rolls is (";
         update += new DecimalFormat("#.##").format(normalDistribution.inverseCumulativeProbability(0.025));
         update += ", ";
         update += new DecimalFormat("#.##").format(normalDistribution.inverseCumulativeProbability(1.0 - 0.025));
         update += ").";
 
         return update;
     }
 
     /*
      * Calculates the expected distribution for each dice result.
      */
     public static HashMap<Integer, Double> getPMF(long gameId) {
         if (cacheGetPMF == null || !cacheGetPMF.containsKey(gameId)) {
             ArrayList<DieDescription> descriptions = retrieveAll(gameId);
             HashMap<Integer, Double> ret = new HashMap<Integer, Double>();
 
             HashMap<Integer, Integer> nonNormed = getNonNormedPMF(descriptions);
             double totalPossibilities = (double)getNumPossibilities(descriptions);
             for (Integer observation : nonNormed.keySet()) {
                 ret.put(observation, nonNormed.get(observation) / totalPossibilities);
             }
             if (cacheGetPMF == null) {
                 cacheGetPMF = new HashMap<Long, HashMap<Integer, Double> >();
             }
             cacheGetPMF.put(gameId, ret);
             return ret;
         } else {
             return cacheGetPMF.get(gameId);
         }
     }
 
     private static int getNumPossibilities(ArrayList<DieDescription> descriptions) {
         int count = 1;
         for (DieDescription description : descriptions) {
             if (description.getDisplayType().equals(DieDescription.NUMERIC)) {
                 count *= description.getNumHighFace() - description.getNumLowFace() + 1;
             }
         }
         return count;
     }
 
     public static HashMap<Integer, Integer> getNonNormedPMF(ArrayList<DieDescription> descriptions) {
         final HashMap<Integer, Integer> ret = new HashMap<Integer, Integer>();
         // Need a recursion because we don't know how many dice there are, or what their number
         // of faces are. Thus, this recursion is a sort of arbitrarily nested loop with
         // backtracking.
         class Recursor {
             public void recurse(ArrayList<DieDescription> remainingDescriptions,
                                 int currentObservation) {
                 if (remainingDescriptions.isEmpty()) {
                     if (ret.containsKey(currentObservation)) {
                         ret.put(currentObservation, ret.get(currentObservation) + 1);
                     } else {
                         ret.put(currentObservation, 1);
                     }
                 } else {
                     DieDescription currentDescription = remainingDescriptions.get(
                             remainingDescriptions.size() - 1);
                     ArrayList<DieDescription> copy = new ArrayList<DieDescription>(
                             remainingDescriptions);
                     copy.remove(copy.size() - 1);
 
                     if (currentDescription.getDisplayType().equals(DieDescription.NUMERIC)) {
                         for (int faceVal = currentDescription.getNumLowFace();
                              faceVal <= currentDescription.getNumHighFace(); faceVal++) {
                             recurse(copy, currentObservation + faceVal);
                         }
                     } else {
                         recurse(copy, currentObservation);
                     }
                 }
             }
         }
         Recursor recursor = new Recursor();
         recursor.recurse(descriptions, 0);
         return ret;
     }
 }
 
