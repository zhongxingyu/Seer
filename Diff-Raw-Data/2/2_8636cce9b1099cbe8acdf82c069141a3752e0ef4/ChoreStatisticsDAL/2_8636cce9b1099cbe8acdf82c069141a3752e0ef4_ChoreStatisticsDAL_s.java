 package il.ac.huji.chores.dal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import il.ac.huji.chores.ApartmentChore;
 import il.ac.huji.chores.ChoreApartmentStatistics;
 import il.ac.huji.chores.ChoreStatistics;
 import il.ac.huji.chores.exceptions.ChoreStatisticsException;
 
 public class ChoreStatisticsDAL {
 
 	public static ChoreStatistics getMostAccomplishedChore()
 			throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("choreStatistics");
 		int max = 0;
 		String choreName = "";
 		List<ParseObject> chores = query.find();
 		for (ParseObject chore : chores) {
 			int missed = chore.getInt("totalMissed");
 			int done = chore.getInt("totalDone");
 			if ((missed + done > 0) && (done / (done + missed)) >= max) {
 				max = done / (done + missed);
 				choreName = chore.getString("chore");
 			}
 		}
 		return getChoreStatistic("choreName");
 
 	}
 
 	public static int getChoreAverageValue(String choreName) throws ParseException {
 		ChoreStatistics stats = getChoreStatistic(choreName);
 		if (stats == null) {
 			return -1;
 		}
 		return stats.getAverageValue();
 	}
 
 	public static ChoreStatistics getMostMissedChore() throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("choreStatistics");
 		int max = 0;
 		String choreName = "";
 		List<ParseObject> chores = query.find();
 		for (ParseObject chore : chores) {
 			int missed = chore.getInt("totalMissed");
 			int done = chore.getInt("totalDone");
 			if ((missed + done > 0) && (missed / (done + missed)) > max) {
 				max = missed / (done + missed);
 				choreName = chore.getString("chore");
 			}
 		}
 		return getChoreStatistic("choreName");
 
 	}
 
 	public static void updateChoreMissedCount(String choreName, int count)
 			throws ParseException {
 		ParseObject choreStatistics = getChoreStatisticsObj(choreName);
 		if (choreStatistics == null) {
 			choreStatistics = createChoreStatistic(choreName);
 		}
 		int currentCount = choreStatistics.getInt("totalMissed");
 		currentCount += count;
 		choreStatistics.put("totalMissed", currentCount);
 		choreStatistics.save();
 
 	}
 
 	public static void updateChoreDoneCount(String choreName, int count)
 			throws ParseException {
 		ParseObject choreStatistics = getChoreStatisticsObj(choreName);
 		if (choreStatistics == null) {
 			choreStatistics = createChoreStatistic(choreName);
 		}
 		int currentCount = choreStatistics.getInt("totalDone");
 		currentCount += count;
 		choreStatistics.put("totalDone", currentCount);
 		choreStatistics.save();
 
 	}
 
 	public static void updateChoreTotalCount(String choreName, int count)
 			throws ParseException {
 		ParseObject choreStatistics = getChoreStatisticsObj(choreName);
 		if (choreStatistics == null) {
 			choreStatistics = createChoreStatistic(choreName);
 		}
 		int currentCount = choreStatistics.getInt("totalCount");
 		currentCount += count;
 		choreStatistics.put("totalCount", currentCount);
 		choreStatistics.save();
 	}
 
 	public static void updateChorePointsTotalCount(String choreName, int count)
 			throws ParseException {
 		ParseObject choreStatistics = getChoreStatisticsObj(choreName);
 		if (choreStatistics == null) {
 			choreStatistics = createChoreStatistic(choreName);
 		}
 		int currentCount = choreStatistics.getInt("totalCoins");
 		currentCount += count;
 		choreStatistics.put("totalCoins", currentCount);
 		choreStatistics.save();
 	}
 
 	public static ParseObject createChoreStatistic(String choreName)
 			throws ParseException {
 		ParseObject choreStatistics = new ParseObject("choreStatistics");
 		choreStatistics.put("chore", choreName);
 		choreStatistics.put("totalCount", 0);
 		choreStatistics.put("totalMissed", 0);
 		choreStatistics.put("totalDone", 0);
 		choreStatistics.put("totalCoins", 0);
 		choreStatistics.save();
 		return choreStatistics;
 	}
 
 	public static boolean choreStatisticsExists(String choreName)
 			throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("choreStatistics");
 		query.whereEqualTo("chore", choreName);
 		List<ParseObject> results = query.find();
 		return results.size() > 0;
 	}
 
 	public static ParseObject getChoreStatisticsObj(String choreName) throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("choreStatistics");
 		query.whereEqualTo("chore", choreName);
 		List<ParseObject> results = query.find();
 		if (results == null || results.size() == 0) {
 			return null;// This needs to be handled above - inside this
 		}
 		return results.get(0);
 	}
 
     public static List<String> getChoreStatisticsNames() throws ParseException {
         ParseQuery query = ParseQuery.getQuery("choreStatistics");
        List<String> results = new ArrayList<>();
         List<ParseObject> queryResults = query.find();
         for (ParseObject res : queryResults) {
             results.add(res.getString("chore"));
         }
         return results;
     }
 
     public static ChoreApartmentStatistics getChoreApartmentStatistic(String choreName, String apartmentId)
             throws ParseException {
         List<ParseObject> results = ParseQuery.getQuery("Chores")
                 .whereEqualTo("apartment", apartmentId).whereEqualTo("name", choreName).find();
         ChoreApartmentStatistics statistic = new ChoreApartmentStatistics(getChoreStatistic(choreName));
         int assigned = 0, missed = 0, done = 0, value = 0;
         for (ParseObject chore : results) {
             if (chore.getString("assignedTo") != null) {
                 assigned++;
             }
             if ("STATUS_MISSED".equals(chore.getString("status"))) {
                 missed++;
             }
             if ("STATUS_DONE".equals(chore.getString("status"))) {
                 done++;
             }
             value = chore.getInt("coins");
         }
         statistic.setApartmentAssigned(assigned);
         statistic.setApartmentMissed(missed);
         statistic.setApartmentDone(done);
         statistic.setApartmentValue(value);
         return statistic;
     }
 
     public static ChoreStatistics getChoreStatistic(String choreName) throws ParseException {
 		ParseObject choreStaticsObj = getChoreStatisticsObj(choreName);
 		if (choreStaticsObj == null)
 			return null;
 		ChoreStatistics choreStatistics = new ChoreStatistics();
 		choreStatistics.setChoreName(choreName);
 		choreStatistics.setTotalCount(choreStaticsObj.getInt("totalCount"));
 		choreStatistics.setTotalDone(choreStaticsObj.getInt("totalDone"));
 		choreStatistics.setTotalMissed(choreStaticsObj.getInt("totalMissed"));
 		choreStatistics.setTotalPoints(choreStaticsObj.getInt("totalCoins"));
         choreStatistics.setTotalPoints(choreStaticsObj.getInt("totalAssigned"));
 		if (choreStatistics.getTotalCount() > 0) {
 			choreStatistics.setAverageValue(choreStatistics.getTotalPoints()
 					/ choreStatistics.getTotalCount());
 		} else {
 			choreStatistics.setAverageValue(0);
 		}
 		return choreStatistics;
 	}
 }
