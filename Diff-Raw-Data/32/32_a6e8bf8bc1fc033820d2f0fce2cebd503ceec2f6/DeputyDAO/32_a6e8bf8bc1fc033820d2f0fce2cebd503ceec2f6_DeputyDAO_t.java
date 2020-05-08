 package com.hackaton.duma.dao;
 
 import com.hackaton.duma.hotornot.HotOrNotException;
 import com.hackaton.duma.index.IndexException;
 import com.hackaton.duma.model.Deputy;
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ernest
  * Date: 9/15/13
  * Time: 12:17 AM
  */
 @Component("deputyDAO")
 public class DeputyDAO {
     private static final Log logger = LogFactory.getLog(DeputyDAO.class);
 
     private static final String SELECT_DEPUTY =
             "select d.deputy_id, d.last_name, d.first_name, d.middle_name, " +
                     "d.big_photo_url, site_id " +
                     "from deputy d " +
                     "where d.deputy_id = ?";
 
     private static final String SELECT_DEPUTY_PAIR =
             "select d1.big_photo_url, d1.first_name, d1.last_name, d1.deputy_id, " +
                     "d2.big_photo_url, d2.first_name, d2.last_name, d2.deputy_id " +
                     "from deputy d1, deputy d2 " +
                     "where d1.deputy_id != d2.deputy_id " +
                     "and d1.deputy_id != ? and d1.deputy_id != ? " +
                     "and d2.deputy_id != ? and d2.deputy_id != ? " +
                     "order by random() limit 1";
 
     private static final String SELECT_RATING =
             "select deputy_id, first_name, last_name, small_photo_url, big_photo_url, " +
                     "positive_voices, negative_voices, " +
                     "positive_voices * 2 - negative_voices as rating " +
                     "from deputy order by rating desc offset ? limit ?";
 
     private static final String SELECT_RATING_REVERT =
             "select deputy_id, first_name, last_name, small_photo_url, big_photo_url, " +
             "positive_voices, negative_voices, " +
             "positive_voices * 2 - negative_voices as rating " +
             "from deputy order by rating asc offset ? limit ?";
 
     private static final String SELECT_LAZIEST =
             "select deputy_id, first_name, last_name, small_photo_url, big_photo_url, " +
                     "laziness, law_count " +
                    "from deputy order by law_count asc offset ? limit ?";
 
     private static final String SELECT_USER_TOPICS =
             "select dt.topic_id, cast(dt.law_count as float) / d.law_count * 100  " +
            "from deputy_topic dt join deputy d on dt.deputy_id = d.api_id where d.deputy_id = ? " +
             "order by topic_id";
 
 
     @Resource(name = "connectionFactory")
     private ConnectionFactory connectionFactory;
 
     private interface RowMapper {
         Deputy mapRow(ResultSet rs) throws SQLException;
     }
 
     private class DefaultRowMapper implements RowMapper {
         @Override
         public Deputy mapRow(ResultSet rs) throws SQLException {
             Deputy deputy = new Deputy();
             deputy.setId(rs.getInt(1));
             deputy.setFirstName(rs.getString(2));
             deputy.setLastName(rs.getString(3));
             deputy.setSmallPhotoURL(rs.getString(4));
             deputy.setBigPhotoURL(rs.getString(5));
             deputy.setPositiveVoices(rs.getInt(6));
             deputy.setNegativeVoices(rs.getInt(7));
             deputy.setRating(rs.getInt(8));
             return deputy;
         }
     }
 
     private List<Deputy> getDeputiesList(String sql, int limit, int offset, RowMapper rowMapper) {
         List<Deputy> result = new ArrayList<Deputy>();
         Connection connection = null;
         try {
             connection = connectionFactory.createConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ps.setInt(2, limit);
             ps.setInt(1, offset);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 result.add(rowMapper.mapRow(rs));
             }
         } catch (SQLException e) {
             logger.error(e);
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     logger.error(e);
                 }
             }
         }
         return result;
     }
 
     public List<Deputy> getHotTopDeputies(int limit, int offset) {
         return getDeputiesList(SELECT_RATING, limit, offset, new DefaultRowMapper());
     }
 
     public List<Deputy> getHotWorstDeputies(int limit, int offset) {
         return getDeputiesList(SELECT_RATING_REVERT, limit, offset, new DefaultRowMapper());
     }
 
     public List<Deputy> getLaziest(int limit, int offset) {
         return getDeputiesList(SELECT_LAZIEST, limit, offset, new RowMapper() {
             @Override
             public Deputy mapRow(ResultSet rs) throws SQLException {
                 Deputy deputy = new Deputy();
                 deputy.setId(rs.getInt(1));
                 deputy.setFirstName(rs.getString(2));
                 deputy.setLastName(rs.getString(3));
                 deputy.setSmallPhotoURL(rs.getString(4));
                 deputy.setBigPhotoURL(rs.getString(5));
                 deputy.setLaziness(rs.getFloat(6));
                 deputy.setLawCount(rs.getInt(7));
                 return deputy;
             }
         });
     }
 
     public Deputy getDeputy(int id) throws IndexException {
         Deputy deputy = new Deputy();
 
         Connection connection = null;
         try {
             connection = connectionFactory.createConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_DEPUTY);
             ps.setInt(1, id);
             ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 deputy.setId(rs.getInt(1));
                 deputy.setLastName(rs.getString(2));
                 deputy.setFirstName(rs.getString(3));
                 deputy.setMiddleName(rs.getString(4));
                 deputy.setBigPhotoURL(rs.getString(5));
                 deputy.setSiteId(rs.getInt(6));
                 logger.info("Big photoUrl:" + deputy.getBigPhotoURL());
             } else {
                 logger.error("Something wrong with connection.\n" +
                         "Cannot get next pair of deputies.");
                 throw new IndexException();
             }
         } catch (SQLException e) {
             logger.error(e.getMessage());
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     logger.error(e.getMessage());
                 }
             }
         }
         return deputy;
     }
 
     public Deputy[] getNextDeputies(int previousFirst, int previousSecond)
             throws HotOrNotException {
         Deputy first = new Deputy();
         Deputy second = new Deputy();
 
         Connection connection = null;
         try {
             connection = connectionFactory.createConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_DEPUTY_PAIR);
             ps.setInt(1, previousFirst);
             ps.setInt(2, previousSecond);
             ps.setInt(3, previousFirst);
             ps.setInt(4, previousSecond);
             ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 first.setBigPhotoURL(rs.getString(1));
                 first.setFirstName(rs.getString(2));
                 first.setLastName(rs.getString(3));
                 first.setId(rs.getInt(4));
                 second.setBigPhotoURL(rs.getString(5));
                 second.setFirstName(rs.getString(6));
                 second.setLastName(rs.getString(7));
                 second.setId(rs.getInt(8));
             } else {
                 logger.error("Something wrong with connection.\n" +
                         "Cannot get next pair of deputies.");
                 throw new HotOrNotException();
             }
         } catch (SQLException e) {
             logger.error(e.getMessage());
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     logger.error(e.getMessage());
                 }
             }
         }
 
         return new Deputy[] {first, second};
     }
 
    public Map<String, Float> getTopicRate(int id) throws IndexException {
        Map<String, Float> result = new HashMap<String, Float>();
 
         Connection connection = null;
         try {
             connection = connectionFactory.createConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_USER_TOPICS);
             ps.setInt(1, id);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                result.put(String.valueOf(rs.getInt(1)), rs.getFloat(2));
                 logger.info("Topic: " + rs.getInt(1) + " Rate: " + rs.getInt(2));
 
             }
         } catch (SQLException e) {
             logger.error(e.getMessage());
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     logger.error(e.getMessage());
                 }
             }
         }
         return result;
     }
 
     public void voting(final int first, final int second, final String result) {
         if (!result.equals("left") && !result.equals("right")) {
             return;
         }
         new Thread(new Runnable() {
             @Override
             public void run() {
                 Connection connection = null;
                 try {
                     connection = connectionFactory.createConnection();
                     PreparedStatement psPlus = connection.prepareStatement("update deputy set positive_voices = positive_voices + 1 where deputy_id = ?");
                     PreparedStatement psMinus = connection.prepareStatement("update deputy set negative_voices = negative_voices + 1 where deputy_id = ?");
                     if (result.equals("left")) {
                         psPlus.setInt(1, first);
                         psMinus.setInt(1, second);
                     } else {
                         psPlus.setInt(1, second);
                         psMinus.setInt(1, first);
                     }
                     psPlus.executeUpdate();
                     psMinus.executeUpdate();
                 } catch (SQLException e) {
                     logger.error(e);
                 } finally {
                     if (connection != null) {
                         try {
                             connection.close();
                         } catch (SQLException e) {
                             logger.error(e);
                         }
                     }
                 }
             }
         }).start();
     }
 }
