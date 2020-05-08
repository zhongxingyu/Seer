 package edu.berkeley.cs.cs162;
 
 import com.mchange.v2.c3p0.*;
 import java.beans.PropertyVetoException;
 import java.sql.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.TreeSet;
 
 public class DatabaseManager {
 
     public static final String databaseHost = "ec2-50-17-180-71.compute-1.amazonaws.com";
     public static final String databaseUser = "group21";
     public static final String databasePassword = "zjKkzjSs";
     public static final String database = "group21";
 
     private ComboPooledDataSource dataSource;
 
     public DatabaseManager() {
         super();
         this.dataSource = new ComboPooledDataSource();
         try {
             this.dataSource.setDriverClass("com.mysql.jdbc.Driver");
             this.dataSource.setJdbcUrl("jdbc:mysql://" + databaseHost + ":3306/" + database);
             this.dataSource.setUser(databaseUser);
             this.dataSource.setPassword(databasePassword);
         } catch (PropertyVetoException e) {
         }
     }
 
     public void shutdown() {
         dataSource.close();
     }
 
     public void emptyDatabase() throws SQLException {
         String query1 = "DELETE FROM `users`;";
         String query2 = "DELETE FROM `groups`;";
         String query3 = "DELETE FROM `group_users`;";
         String query4 = "DELETE FROM `offline_messages`;";
         String query5 = "DELETE FROM `server_info`;";
         Connection connection = null;
         Statement statement1 = null;
         Statement statement2 = null;
         Statement statement3 = null;
         Statement statement4 = null;
         Statement statement5 = null;
         try {
             connection = dataSource.getConnection();
             connection.setAutoCommit(false);
             statement1 = connection.createStatement();
             statement2 = connection.createStatement();
             statement3 = connection.createStatement();
             statement4 = connection.createStatement();
             statement5 = connection.createStatement();
             statement1.executeUpdate(query1);
             statement2.executeUpdate(query2);
             statement3.executeUpdate(query3);
             statement4.executeUpdate(query4);
             statement5.executeUpdate(query5);
             connection.commit();
         } finally {
             if (statement1 != null) {statement1.close();}
             if (statement2 != null) {statement2.close();}
             if (statement3 != null) {statement3.close();}
             if (statement4 != null) {statement4.close();}
             if (statement5 != null) {statement5.close();}
             if (connection != null) {
                 connection.setAutoCommit(true);
                 connection.close();
             }
         }
     }
 
     public HashMap<String, Object> getServer(String serverName) throws SQLException {
         String query = "SELECT `name`,`host`,`c_port`,`s_port` FROM `server_info` WHERE `name`='" + serverName +"';";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             HashMap<String, Object> serverProperties = null;
             if (results.next()) {
                 serverProperties = new HashMap<String, Object>();
                 serverProperties.put("name", results.getString(1));
                 serverProperties.put("host", results.getString(2));
                 serverProperties.put("port", results.getInt(3));
                 serverProperties.put("sport", results.getInt(4));
             }
             return serverProperties;
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public LinkedList<HashMap<String, Object>> getServerList() throws SQLException {
         String query = "SELECT `name`,`host`,`c_port`,`s_port` FROM `server_info`";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             LinkedList<HashMap<String, Object>> servers = new LinkedList<HashMap<String, Object>>();
             HashMap<String, Object> serverProperties;
             while (results.next()) {
                 serverProperties = new HashMap<String, Object>();
                 serverProperties.put("name", results.getString(1));
                 serverProperties.put("host", results.getString(2));
                 serverProperties.put("port", results.getInt(3));
                 serverProperties.put("sport", results.getInt(4));
                 servers.add(serverProperties);
             }
             return servers;
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void addServer(String name, String host, int port, int sport) throws SQLException {
         String query = "INSERT INTO `server_info` (`name`, `host`, `c_port`,`s_port`) VALUES ('" + name + "','" + host + "','" + Integer.toString(port) + "','" + Integer.toString(sport) +"');";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public HashMap<String, Object> getUser(String userName) throws SQLException {
         String query = "SELECT `name`,`password`,`logged_in` FROM `users` WHERE `name`='" + userName +"';";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             HashMap<String, Object> userProperties = null;
             if (results.next()) {
                 userProperties = new HashMap<String, Object>();
                 userProperties.put("name", results.getString(1));
                 userProperties.put("password", results.getString(2));
                 userProperties.put("logged_in", results.getBoolean(3));
             }
             return userProperties;
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public int getUserCount() throws SQLException {
         String query = "SELECT COUNT(*) FROM `users`;";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             results.next();
             return results.getInt(1);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public TreeSet<String> getUserList() throws SQLException {
         String query = "SELECT `name` FROM `users`;";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             TreeSet<String> userList = new TreeSet<String>();
             while (results.next()) {
                 userList.add(results.getString(1));
             }
             return userList;
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public HashMap<String, Object> getGroup(String groupName) throws SQLException {
         String query = "SELECT `name` FROM `groups` WHERE `name`='" + groupName +"';";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             HashMap<String, Object> groupProperties = null;
             if (results.next()) {
                 groupProperties = new HashMap<String, Object>();
                 groupProperties.put("name", results.getString(1));
             }
             return groupProperties;
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public int getGroupCount() throws SQLException {
         String query = "SELECT COUNT(*) FROM `groups`;";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             results.next();
             return results.getInt(1);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public TreeSet<String> getGroupList() throws SQLException {
         String query = "SELECT `name` FROM `groups`;";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             TreeSet<String> groupList = new TreeSet<String>();
             while (results.next()) {
                 groupList.add(results.getString(1));
             }
             return groupList;
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public int getGroupUserCount(String groupName) throws SQLException {
         String query = "SELECT COUNT(*) FROM `group_users` WHERE `group_id`=(SELECT `id` FROM `groups` WHERE `name`='" + groupName + "');";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             results.next();
             return results.getInt(1);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public TreeSet<String> getGroupUserList(String groupName) throws SQLException {
         String query = "SELECT t2.`name` FROM `group_users` AS t1 INNER JOIN `users` AS t2 ON t1.`user_id`=t2.`id` WHERE `group_id`=(SELECT `id` FROM `groups` WHERE `name`='" + groupName + "');";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             ResultSet results = statement.executeQuery(query);
             TreeSet<String> groupUserList = new TreeSet<String>();
             while (results.next()) {
                 groupUserList.add(results.getString(1));
             }
             return groupUserList;
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void addUser(String userName, String password) throws SQLException {
         String query = "INSERT INTO `users` (`name`,`password`,`logged_in`) VALUES ('" + userName + "','" + password + "','0');";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void loginUser(String userName) throws SQLException {
         String query = "UPDATE `users` SET `logged_in`='1' WHERE `name`='" + userName + "';";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void logoutUser(String userName) throws SQLException {
         String query = "UPDATE `users` SET `logged_in`='0' WHERE `name`='" + userName + "';";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void removeUser(String userName) throws SQLException {
         String query = "DELETE FROM `users` WHERE `name`='" + userName + "';";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void addUserToGroup(String userName, String groupName) throws SQLException {
         String query = "INSERT INTO `group_users` (`group_id`,`user_id`) VALUES ((SELECT `id` FROM `groups` WHERE `name`='" + groupName + "'), (SELECT `id` FROM `users` WHERE `name`='" + userName + "'));";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void removeUserFromGroup(String userName, String groupName) throws SQLException {
        String query = "DELETE FROM `group_users` WHERE `group_id`=(SELECT `id` FROM `groups` WHERE `name`='" + groupName + "') AND `user_id`=(SELECT `id` FROM `users` WHERE `name`='" + userName + "')";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void addGroup(String groupName) throws SQLException {
         String query = "INSERT INTO `groups` (`name`) VALUES ('" + groupName + "')";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void removeGroup(String groupName) throws SQLException {
         String query = "DELETE FROM `groups` WHERE `NAME`='" + groupName + "';";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public void logMessage(String userName, Message message) throws SQLException {
         String query = "INSERT INTO `offline_messages` (`user_id`,`timestamp`,`sqn`,`sender`,`receiver`,`text`) VALUES ((SELECT `id` FROM `users` WHERE `name`='" + userName + "'),'" + message.date.getTime() + "','" + Integer.toString(message.sqn) + "','" + message.sender.getUserName() + "','" + message.receiver + "','" + message.text + "');";
         Connection connection = null;
         Statement statement = null;
         try {
             connection = dataSource.getConnection();
             statement = connection.createStatement();
             statement.executeUpdate(query);
         } finally {
             if (statement != null) {statement.close();}
             if (connection != null) {connection.close();}
         }
     }
 
     public LinkedList<HashMap<String, Object>> getOfflineMessages(String userName) throws SQLException {
         String query1 = "SELECT `timestamp`,`sqn`,`sender`,`receiver`,`text` FROM `offline_messages` WHERE `user_id`=(SELECT `id` FROM `users` WHERE `name`='" + userName + "') ORDER BY `timestamp` ASC;";
         String query2 = "DELETE FROM `offline_messages` WHERE `user_id`=(SELECT `id` FROM `users` WHERE `name`='" + userName + "');";
         Connection connection = null;
         Statement statement1 = null;
         Statement statement2 = null;
         try {
             connection = dataSource.getConnection();
             connection.setAutoCommit(false);
             statement1 = connection.createStatement();
             statement2 = connection.createStatement();
             ResultSet results = statement1.executeQuery(query1);
             statement2.executeUpdate(query2);
             connection.commit();
             LinkedList<HashMap<String, Object>> messages = new LinkedList<HashMap<String, Object>>();
             HashMap<String, Object> messageProperties;
             while (results.next()) {
                 messageProperties = new HashMap<String, Object>();
                 messageProperties.put("timestamp", new Date(results.getLong(1)));
                 messageProperties.put("sqn", results.getInt(2));
                 messageProperties.put("sender", results.getString(3));
                 messageProperties.put("receiver", results.getString(4));
                 messageProperties.put("text", results.getString(5));
                 messages.add(messageProperties);
             }
             return messages;
         } finally {
             if (statement1 != null) {statement1.close();}
             if (statement2 != null) {statement2.close();}
             if (connection != null) {
                 connection.setAutoCommit(true);
                 connection.close();
             }
         }
     }
 }
