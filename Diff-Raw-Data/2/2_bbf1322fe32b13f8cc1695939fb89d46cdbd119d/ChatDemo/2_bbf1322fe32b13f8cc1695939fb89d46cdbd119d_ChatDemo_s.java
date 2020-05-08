 package org.nagazumi.demo.chat;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class ChatDemo
 {
     // JDBC Driver Datasource
     private DataSource dataSource = null;
     private final String CONTEXT_LOOKUP = "java:comp/env/jdbc/my-db";
 
     // Initial DataSorece
     public void initChatDemo() throws Exception {
       try{
         Context context = new InitialContext();
         dataSource = (DataSource)context.lookup(CONTEXT_LOOKUP);
       } catch (NamingException e) {
         throw new Exception(e);
       }
     }
     
     // Insert Chat Data
     public void insertChatData(ChatData data) throws Exception {
 
       String sql = "insert into DEMO_CHAT (ChatName, ChatMesg, ChatDate) values (?, ?, now())";
 
       try {
         Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, data.getName());
         stmt.setString(2, data.getMesg());
         stmt.executeUpdate();
         stmt.close();
         conn.close();
       } catch (SQLException e) {
         throw new Exception(e);
       }
     }
 
     // Select ALL Chat Data
     public ArrayList<ChatData> selectAllChatData() throws Exception {
       ArrayList<ChatData> array = new ArrayList<ChatData>();
 
       String sql = "select ChatName, ChatMesg, DATE_FORMAT(ChatDate, \'%Y/%m/%d %k:%i:%s\') "
          + " from DEMO_CHAT order by ChatDate";
 
       try {
         Connection conn = dataSource.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rst = stmt.executeQuery(sql);
         while (rst.next()) {
           ChatData data = new ChatData();
           data.setName(rst.getString(1));
           data.setMesg(rst.getString(2));
           data.setDate(rst.getString(3));
           array.add(data);
         }
         rst.close();
         stmt.close();
         conn.close();        
       } catch  (SQLException e) {
         throw new Exception(e);
       }
       return array;
     }
 
     // Delete ALL Chat Data
     public void deleteAllChatData() throws Exception {
 
       String sql = "delete from DEMO_CHAT";
 
       try {
         Connection conn = dataSource.getConnection();
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(sql);
         stmt.close();
         conn.close();
       } catch (SQLException e) {
         throw new Exception(e);
       }
     }
 }
