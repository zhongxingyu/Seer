 package com.ft.hack.dynamite.db;
 
 import com.datastax.driver.core.ResultSet;
 import com.datastax.driver.core.Row;
 import com.datastax.driver.core.Session;
 import com.ft.hack.dynamite.model.Recommendation;
 
 import java.util.*;
 
 /**
  * User: anuragkapur
  * Date: 20/05/2013 17:52
  */
 
 public class UserActivityDAO {
 
     public static List<Recommendation> getUserActivityByCompany(String companyName, String timeFilter) {
 
         List<Recommendation> byCompany = new ArrayList<Recommendation>();
 
         String query = String.format("SELECT * FROM ftdynamite.companies WHERE company_name='%s'",companyName);
         Session session = SimpleClient.getSession();
         Row row = session.execute(query).one();
         if(row != null) {
             // Info for this company exists
             query = String.format("SELECT * FROM ftdynamite.useractivity_company_%s",companyName);
             System.out.println("Query >> " + query);
             ResultSet results = session.execute(query);
 
             for (Row row1 : results) {
                 Recommendation recommendation = ArticleDAO.getArticle(row1.getString("article_id"));
                 if(recommendation != null) {
                     recommendation.setCount((int)row1.getLong("count"));
                     byCompany.add(recommendation);
                 }
             }
         }else {
             // no info exists
         }
 
        // sort
        Collections.sort(byCompany);
         return byCompany;
     }
 
     public static List<Recommendation> getUserActivityBySector(String sectorName, String timeFilter) {
 
         sectorName = tableNameCleanup(sectorName);
 
         List<Recommendation> bySector = new ArrayList<Recommendation>();
 
         String query = String.format("SELECT * FROM ftdynamite.sectors WHERE sector_name='%s'",sectorName);
         System.out.println("Query >> " + query);
         Session session = SimpleClient.getSession();
         Row row = session.execute(query).one();
         if(row != null) {
             // Info for this sector exists
            query = String.format("SELECT * FROM ftdynamite.useractivity_sector_%s",sectorName);
             ResultSet results = session.execute(query);
             for (Row row1 : results) {
                 Recommendation recommendation = ArticleDAO.getArticle(row1.getString("article_id"));
                 row1.getString("article_id");
 
                 //TODO sort by count and use that to return top 5 or 10
                row1.getLong("count");
                 bySector.add(recommendation);
             }
         }else {
             // no info exists
         }
         return bySector;
     }
 
     public static List<Recommendation> getUserActivityByPosition(String positionName, String timeFilter) {
 
         List<Recommendation> byPosition = new ArrayList<Recommendation>();
 
         String query = String.format("SELECT * FROM ftdynamite.positions WHERE position_name='%s'",positionName);
         Session session = SimpleClient.getSession();
         Row row = session.execute(query).one();
         if(row != null) {
             // Info for this position exists
             query = String.format("SELECT * FROM ftdynamite.useractivity_position_%s",positionName);
             ResultSet results = session.execute(query);
             for (Row row1 : results) {
                 Recommendation recommendation = ArticleDAO.getArticle(row1.getString("article_id"));
                 row1.getString("article_id");
 
                 //TODO sort by count and use that to return top 5 or 10
                 row1.getLong("count");
                 byPosition.add(recommendation);
             }
         }else {
             // no info exists
         }
         return byPosition;
     }
 
     public static void processTimeFilter(String timeFilter) {
 
         Date fromDate = null;
         Date toDate = null;
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         int hour = calendar.get(Calendar.HOUR_OF_DAY);
         int minute = calendar.get(Calendar.MINUTE);
 
         if(timeFilter!= null && timeFilter.equals("last5mins")) {
             int newMinute = minute - minute%5;
 
             //hack in absence of enough data
             calendar.set(Calendar.YEAR, 2013);
             calendar.set(Calendar.DATE, 8);
             calendar.set(Calendar.MONTH, Calendar.MAY);
             calendar.set(Calendar.HOUR_OF_DAY, 12);
 
             calendar.set(Calendar.MINUTE, newMinute);
             calendar.set(Calendar.SECOND, 0);
             toDate = calendar.getTime();
 
             calendar.add(Calendar.MINUTE, -5);
             fromDate = calendar.getTime();
 
             System.out.println(toDate.toString() + " :: " + fromDate.toString() );
 
         } else if(timeFilter!= null && timeFilter.equals("last1hour")) {
             //hack in absence of enough data
             calendar.set(Calendar.YEAR, 2013);
             calendar.set(Calendar.DATE, 8);
             calendar.set(Calendar.MONTH, Calendar.MAY);
             calendar.set(Calendar.HOUR_OF_DAY, 12);
 
             calendar.set(Calendar.MINUTE, 0);
             calendar.set(Calendar.SECOND, 0);
 
             toDate = calendar.getTime();
 
             calendar.add(Calendar.HOUR, -1);
             fromDate = calendar.getTime();
             System.out.println(toDate.toString() + " :: " + fromDate.toString() );
         } else {
             //hack in absence of enough data
             calendar.set(Calendar.YEAR, 2013);
             calendar.set(Calendar.DATE, 8);
             calendar.set(Calendar.MONTH, Calendar.MAY);
             calendar.set(Calendar.HOUR_OF_DAY, 23);
 
             calendar.set(Calendar.MINUTE, 0);
             calendar.set(Calendar.SECOND, 0);
 
             toDate = calendar.getTime();
             calendar.add(Calendar.HOUR, -23);
 
             fromDate=calendar.getTime();
             System.out.println(toDate.toString() + " :: " + fromDate.toString() );
         }
     }
 
     private static String tableNameCleanup(String tableName) {
         return tableName.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
     }
 
     public static void main(String args[]) {
        UserActivityDAO.getUserActivityByCompany("apple", null);
     }
 }
