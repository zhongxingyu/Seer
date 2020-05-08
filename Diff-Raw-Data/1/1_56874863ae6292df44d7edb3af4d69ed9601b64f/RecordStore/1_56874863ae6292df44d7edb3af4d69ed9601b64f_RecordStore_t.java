 package org.paulg.ispend.model;
 
 import java.util.*;
 import java.util.function.Predicate;
 import java.util.stream.Collector;
 
 import org.paulg.ispend.utils.StringUtils;
 
 public class RecordStore {
 
 	private final Map<String, Account> accounts = new LinkedHashMap<>();
 
 	public void addRecord(final Record r) {
 		Account acc = accounts.get(r.getAccountNumber());
 		if (acc == null) {
 			acc = new Account(r.getAccountNumber(), r.getAccountName());
 			accounts.put(r.getAccountNumber(), acc);
 		}
 		acc.addRecord(r);
 	}
 
 	public List<Record> getRecordsByAccountNumber(final String number) {
 		if (accounts.get(number) != null) {
 			return new ArrayList<>(accounts.get(number).getRecords());
 		}
 		return new ArrayList<>();
 	}
 
 	public void printSummary() {
 		for (final Account a : accounts.values()) {
 			a.printSummary();
 		}
 	}
 
 	public List<Record> getAllRecords() {
 		final List<Record> allRecords = new ArrayList<>();
 		for (final Account a : accounts.values()) {
 			allRecords.addAll(a.getRecords());
 		}
 		return allRecords;
 	}
 
 	public List<Record> filter(final String text) {
 		final List<Record> unfiltered = getAllRecords();
 		final List<Record> filtered = new ArrayList<>();
 		for (final Record r : unfiltered) {
 			if (StringUtils.containsIgnoreCase(r.getDescription(), text)) {
 				filtered.add(r);
 			}
 		}
 		return filtered;
 	}
 
 	private String[] parseArguments(final String text) {
 		return text.split(",");
 	}
 
 	public List<AggregatedRecord> groupByDescription(final String query) {
 		String[] tags = parseArguments(query);
 		final List<AggregatedRecord> tagRecords = new ArrayList<>();
         if (query.isEmpty())
             return tagRecords;
 
 		if ((tags != null) && (tags.length > 0)) {
 
 			for (Account a : accounts.values()) {
 				a.setCovered(0);
 			}
 
 			for (String tag : tags) {
 				tag = tag.trim();
 				final AggregatedRecord tagRecord = new AggregatedRecord(tag, 0);
 				for (Account a : accounts.values()) {
 					for (final Record r : a.getRecords()) {
 						if (StringUtils.containsIgnoreCase(r.getDescription(), tag)) {
 							tagRecord.addRecord(r);
 							r.setCovered(true);
 							a.setCovered(a.getCovered() + 1);
 						} else {
 							r.setCovered(false);
 						}
 					}
 				}
 				tagRecords.add(tagRecord);
 			}
 		}
 		return tagRecords;
 	}
 
 	public double getTotalIncome() {
 		double income = 0;
 		for (Account a : accounts.values())
             income += a.getRecords().stream().
                     mapToDouble(Record::getValue).
                     filter(x -> x > 0).
                     sum();
 		return income;
 	}
 
 	public double getTotalSpent() {
 		Double spent = 0.0;
 		for (Account a : accounts.values()) {
             spent += a.getRecords().stream().
                     mapToDouble(Record::getValue).
                     filter(x -> x < 0).
                    map(x -> Math.abs(x)).
                     sum();
 		}
 		return spent;
 	}
 
 	public Collection<Account> getAccounts() {
 		return accounts.values();
 	}
 
     public Map<Date, Double> getWeeklyBalance() {
         return getBalance(Calendar.WEEK_OF_YEAR);
     }
 
     public Map<Date, Double> getMonthlyBalance() {
         return getBalance(Calendar.MONTH);
     }
 
     private Map<Date, Double> getBalance(int period) {
         Map<Integer, Map<Integer, Double>> weeklyBalance = new HashMap<>();
 
         Date firstDate = null, lastDate = null;
 
         for (Account a : accounts.values()) {
             List<Record> rs = a.getRecords();
             Collections.sort(rs);
 
             Date rDate = rs.get(0).getDate();
             if (firstDate == null || rDate.compareTo(firstDate) <= 0)
                 firstDate = rDate;
             if (lastDate == null || rDate.compareTo(lastDate) >= 0)
                 lastDate = rDate;
 
             // year -> period -> last_balance
             Map<Integer, Map<Integer, Double>> weekly = new HashMap<>();
 
             for (Record r : rs) {
                 Calendar c = Calendar.getInstance();
                 c.setTime(r.getDate());
                 int monthOfYear = c.get(period);
                 int year = c.get(Calendar.YEAR);
 
                 Map<Integer, Double> y = weekly.get(year);
                 if (y == null) {
                     y = new HashMap<>();
                     weekly.put(year, y);
                 }
                 y.put(monthOfYear, r.getBalance());
             }
 
             // aggregate the total across all accounts
             for (Map.Entry<Integer, Map<Integer, Double>> me : weekly.entrySet()) {
                 Map<Integer, Double> wbForYear = weeklyBalance.get(me.getKey());
                 if (wbForYear == null) {
                     wbForYear = new HashMap<>();
                     weeklyBalance.put(me.getKey(), wbForYear);
                 }
 
                 for (Map.Entry<Integer, Double> me2 : me.getValue().entrySet()) {
                     Double aggregatedBalanceForWeek = wbForYear.get(me2.getKey());
                     if (aggregatedBalanceForWeek == null)
                         wbForYear.put(me2.getKey(), me2.getValue());
                     else
                         wbForYear.put(me2.getKey(), aggregatedBalanceForWeek + me2.getValue());
                 }
             }
 
         }
 
         Map<Date, Double> simpleWeeklyTotal = new HashMap<>();
         for (Map.Entry<Integer, Map<Integer, Double>> me : weeklyBalance.entrySet()) {
             int y = me.getKey();
             for (Map.Entry<Integer, Double> me2 : me.getValue().entrySet()) {
                 int week = me2.getKey();
                 Calendar c = Calendar.getInstance();
                 c.setTimeInMillis(0);
                 c.set(Calendar.YEAR, y);
                 c.set(period, week);
                 Date d = c.getTime();
                 simpleWeeklyTotal.put(d, me2.getValue());
             }
         }
 
         return simpleWeeklyTotal;
     }
 
 }
