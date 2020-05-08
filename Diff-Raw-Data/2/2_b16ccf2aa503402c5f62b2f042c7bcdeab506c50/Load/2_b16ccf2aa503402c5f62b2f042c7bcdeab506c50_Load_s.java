 package com.rmatcher.core.json;
 
 import com.google.common.base.Joiner;
 
 import java.sql.*;
 import java.util.Iterator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ameen
  * Date: 4/19/13
  * Time: 8:35 PM
  */
 
 public class Load {
 
     public static void main(String [] args) throws Exception{
 
         String pathToFolder = "C:\\Users\\Ameen\\Desktop\\Yelp\\";
         //String pathToFolder = "/Users/santoki/yelp/yelp_phoenix_academic_dataset/";
 
         String businessFilePath = pathToFolder + "yelp_academic_dataset_business.json";
         String checkinFilePath = pathToFolder + "yelp_academic_dataset_checkin.json";
         String reviewFilePath = pathToFolder + "yelp_academic_dataset_review.json";
         String userFilePath = pathToFolder + "yelp_academic_dataset_user.json";
 
         Connection connect = null;
         ResultSet resultSet = null;
 
         JsonParser<Yelp_Business> businessJsonParser = new JsonParser(new Yelp_Business());
         businessJsonParser.createBuffer(businessFilePath);
         JsonParser<Yelp_Checkin> checkinJsonParser = new JsonParser(new Yelp_Checkin());
         checkinJsonParser.createBuffer(checkinFilePath);
         JsonParser<Yelp_Review> reviewJsonParser = new JsonParser(new Yelp_Review());
         reviewJsonParser.createBuffer(reviewFilePath);
         JsonParser<Yelp_User> userJsonParser = new JsonParser(new Yelp_User());
         userJsonParser.createBuffer(userFilePath);
 
 
         Iterator<Yelp_Business> businessIterator = businessJsonParser.iterator();
         Iterator<Yelp_Checkin> checkinIterator = checkinJsonParser.iterator();
         Iterator<Yelp_Review> reviewIterator = reviewJsonParser.iterator();
         Iterator<Yelp_User> userIterator = userJsonParser.iterator();
 
 
         try {
             Class.forName("com.mysql.jdbc.Driver");
             connect = DriverManager
                     .getConnection("jdbc:mysql://localhost/rmatcher?"
                             + "user=root&password=123456");
             connect.setAutoCommit(false);
 
             loadBusiness(connect, businessIterator);
             loadUser(connect, userIterator);
             loadCheckin(connect, checkinIterator);
             loadReview(connect, reviewIterator);
 
         } catch (Exception e) {
             throw e;
         } finally {
             if (resultSet != null) {
                 resultSet.close();
             }
 
             if (connect != null) {
                 connect.close();
             }
         }
     }
 
     private static void loadBusiness(Connection connect, Iterator<Yelp_Business> businessIterator) throws SQLException {
 
         PreparedStatement statement = connect
                 .prepareStatement("INSERT INTO rmatcher.business values (?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ? , ?)");
 
         int count = 0;
         while(businessIterator.hasNext()){
             Yelp_Business business = businessIterator.next();
 
             statement.setString(1, business.get_business_id());
             statement.setString(2, business.get_name());
             statement.setString(3, business.get_full_address());
             statement.setString(4, business.get_city());
             statement.setString(5, business.get_state());
             statement.setDouble(6, business.get_latitude());
             statement.setDouble(7, business.get_longitude());
             statement.setDouble(8, business.get_stars());
             statement.setInt(9, business.get_review_count());
             statement.setInt(10, business.is_open() ? 1 : 0);
             statement.setString(11, Joiner.on(",").join(business.getNeighborhoods()));
             statement.setString(12, Joiner.on(",").join(business.get_categories()));
             statement.addBatch();
             ++count;
             if (count % 1000 == 0) {
                 statement.executeBatch();
                 count = 0;
             }
         }
         statement.executeBatch();
         connect.commit();
         statement.close();
     }
 
     // load Checkin
     private static void loadCheckin(Connection connect, Iterator<Yelp_Checkin> checkinIterator) throws SQLException {
         PreparedStatement statement = connect
                 .prepareStatement("INSERT INTO rmatcher.checkin values (?, ?)");
 
         int count = 0;
         while(checkinIterator.hasNext()){
             Yelp_Checkin checkin = checkinIterator.next();
 
             statement.setString(1, checkin.get_business_id());
            statement.setString(2, Joiner.on(",").join(checkin.get_checkin_info()));
             statement.addBatch();
             ++count;
             if (count % 1000 == 0) {
                 statement.executeBatch();
                 count = 0;
             }
         }
         statement.executeBatch();
         connect.commit();
         statement.close();
     }
 
     private static void loadReview(Connection connect, Iterator<Yelp_Review> reviewIterator) throws SQLException {
         PreparedStatement statement = connect
                 .prepareStatement("INSERT INTO rmatcher.review values (?, ?, ?, ?, ?, ?)");
 
         int count = 0;
         while(reviewIterator.hasNext()){
             Yelp_Review review = reviewIterator.next();
 
             statement.setString(1, review.get_business_id());
             statement.setString(2, review.get_review_id());
             statement.setDouble(3, review.get_stars());
             statement.setString(4, review.get_text());
             statement.setString(5, review.get_date());
             statement.setString(6, review.get_votes().toString());
             statement.addBatch();
             ++count;
             if (count % 1000 == 0) {
                 statement.executeBatch();
                 count = 0;
             }
         }
         statement.executeBatch();
         connect.commit();
         statement.close();
     }
 
     private static void loadUser(Connection connect, Iterator<Yelp_User> userIterator) throws SQLException {
         PreparedStatement statement = connect
                 .prepareStatement("INSERT INTO rmatcher.user values (?, ?, ?, ?, ?)");
 
         int count = 0;
         while(userIterator.hasNext()){
             Yelp_User user = userIterator.next();
 
             statement.setString(1, user.get_user_id());
             statement.setString(2, user.get_name());
             statement.setInt(3, user.get_review_count());
             statement.setDouble(4, user.get_average_stars());
             statement.setString(5, user.get_votes().toString());
             statement.addBatch();
             ++count;
             if (count % 1000 == 0) {
                 statement.executeBatch();
                 count = 0;
             }
         }
         statement.executeBatch();
         connect.commit();
         statement.close();
     }
 }
