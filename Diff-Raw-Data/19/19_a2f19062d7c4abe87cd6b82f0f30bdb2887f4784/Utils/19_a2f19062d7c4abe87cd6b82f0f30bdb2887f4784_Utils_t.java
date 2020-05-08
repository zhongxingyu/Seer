 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package logic;
 
 import java.sql.*;
 import java.util.*;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJBException;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import models.entitys.AlbumModel;
 import models.entitys.GroupModel;
 
 /**
  *
  * @author Klaritin
  */
 public class Utils {
 
     private static String connectionString = "jdbc/OracleDataSource";
 
     public static List<Entry<String, Double>> searchGroupRating() {
         List<Entry<String, Double>> groups = new ArrayList<Entry<String, Double>>();
         DataSource dataSource;
         Context context;
         Connection connection = null;
         PreparedStatement statement = null;
         String query;
         try {
             context = new InitialContext();
             dataSource = (DataSource) context.lookup(connectionString);
             connection = dataSource.getConnection();
             query = "SELECT \"group\".name,  rated_group.rate "
                     + "FROM (SELECT \"group\".id,  AVG(rated_tracks.avgrate) as rate "
                     + " FROM "
                     + "  (SELECT track.id, AVG(rate.value) as avgrate "
                     + "   FROM groups \"group\", albums album, tracks track, rates rate "
                     + "   WHERE "
                     + "   album.\"group\" = \"group\".id AND "
                     + "   track.album = album.id AND "
                     + "   rate.track = track.id "
                     + "   GROUP BY track.id) rated_tracks, groups \"group\", albums album, tracks track "
                     + " WHERE "
                     + " album.\"group\" = \"group\".id AND "
                     + " track.album = album.id AND "
                     + " rated_tracks.id = track.id "
                     + " GROUP BY \"group\".id) rated_group, groups \"group\" "
                     + "WHERE rated_group.id = \"group\".id  AND rownum < 21"
                     + "ORDER BY rated_group.rate DESC";
             statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery();

             while (resultSet.next()) {
                String name = resultSet.getString(1);
                double rate = resultSet.getDouble(2);
                 groups.add(new SimpleEntry(name, rate));
             }
         } catch (NamingException ex) {
             Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             closeConnection(connection, statement);
         }
         return groups;
     }
 
     public static List<Entry<String, Double>> searchAlbumRating() {
         List<Entry<String, Double>> albums = new ArrayList<Entry<String, Double>>();
         DataSource dataSource;
         Context context;
         Connection connection = null;
         PreparedStatement statement = null;
         String query;
         try {
             context = new InitialContext();
             dataSource = (DataSource) context.lookup(connectionString);
             connection = dataSource.getConnection();
             query = "SELECT album.name, rated_albums.rate as rate "
                     + "FROM "
                     + " (SELECT album.id, SUM(rated_tracks.avgrate) as rate "
                     + "  FROM "
                     + "   (SELECT track.id, AVG(rate.value) as avgrate "
                     + "   FROM groups \"group\", albums album, tracks track, rates rate "
                     + "   WHERE "
                     + "   album.\"group\" = \"group\".id AND "
                     + "   rate.track = track.id "
                     + "   GROUP BY track.id) rated_tracks, groups \"group\", albums album, tracks track "
                     + " WHERE album.\"group\" = \"group\".id AND "
                     + " track.album = album.id AND "
                     + " rated_tracks.id = track.id "
                     + " GROUP BY album.id) rated_albums, albums album "
                     + "WHERE album.id = rated_albums.id AND rownum < 21 "
                     + "ORDER BY rated_albums.rate DESC";
             statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery();

             while (resultSet.next()) {
                String name = resultSet.getString(1);
                double rate = resultSet.getDouble(2);
                 albums.add(new SimpleEntry(name, rate));
             }
         } catch (NamingException ex) {
             Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             closeConnection(connection, statement);
         }
         return albums;
     }
 
     private static void closeConnection(Connection connection, Statement statement) {
         try {
             if (statement != null) {
                 statement.close();
             }
         } catch (Exception e) {
         }
         try {
             if (connection != null) {
                 connection.close();
             }
         } catch (SQLException e) {
         }
     }
 }
