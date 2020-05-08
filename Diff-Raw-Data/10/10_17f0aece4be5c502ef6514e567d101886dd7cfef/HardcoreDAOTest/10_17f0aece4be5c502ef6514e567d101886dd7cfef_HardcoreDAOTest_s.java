 package com.psychobit.hardcore;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.internal.runners.JUnit4ClassRunner;
 
 import com.googlecode.flyway.core.Flyway;
 import com.psychobit.hardcore.HardcoreDAO;
 
 @RunWith(JUnit4ClassRunner.class)
 public class HardcoreDAOTest
 {
     private HardcoreDAO dao;
     private Connection dbConnection;
 
     @Before
     public void Setup() throws SQLException, FileNotFoundException, IOException
     {
         Properties properties = new Properties();
         properties.load(new FileInputStream("test.properties"));
         
         String hostname = properties.getProperty("hostname");
         String database = properties.getProperty("database");
         String username = properties.getProperty("username");
         String password = properties.getProperty("password");
         String dataSource = "jdbc:mysql://" + hostname + "/" + database;
 
         Flyway flyway = new Flyway();
         flyway.setDataSource(dataSource, username, password);
         flyway.clean();
         flyway.migrate();
         
         dbConnection = DriverManager.getConnection(dataSource, username, password );
         
         dao = new HardcoreDAO();
         dao.connect(hostname, database, username, password, null);
     }
     
     @After 
     public void Teardown() throws SQLException
     {
         dbConnection.close();
         
         dao.disconnect(null);
     }
     
     @Test
     public void TestSurvivalTimeForNonExistantPlayer()
     {
         int survivalTime = dao.survivalTime("DoesNotExist");
        Assert.assertEquals("should return 0 survival time for non existant player", 0, survivalTime);
     }
     
     @Test
     public void TestSurvivalTimeForOnlinePlayer() throws SQLException, InterruptedException
     {
         PreparedStatement s = dbConnection.prepareStatement(
                 "INSERT INTO `survival` ( `Username`, `Joined`, `LastOnline`, `SurvivalTime` ) VALUES( 'OnlinePlayer', NOW(), NOW(), 1)" 
         );
         s.executeUpdate();
         
         Thread.sleep(1000);
         
         int survivalTime = dao.survivalTime("OnlinePlayer");
         Assert.assertTrue("should return 2 to 3 seconds for online player survial time", 2 <= survivalTime && 3 >= survivalTime);
     }
     
     @Test
     public void TestSurvivalTimeForOfflinePlayer() throws SQLException, InterruptedException
     {
         PreparedStatement s = dbConnection.prepareStatement(
                 "INSERT INTO `survival` ( `Username`, `Joined`, `LastOnline`, `SurvivalTime` ) VALUES( 'OfflinePlayer', NOW(), NOW(), 1)" 
         );
         s.executeUpdate();
         
         Thread.sleep(1000);
         
         int survivalTime = dao.survivalTime("OfflinePlayer");
         Assert.assertEquals("should return 1 second for offline player survial time", 1, survivalTime);
         
         Thread.sleep(1000);
         
         survivalTime = dao.survivalTime("OfflinePlayer");
         Assert.assertEquals("should not increase survival time while player is offline", 1, survivalTime);        
     }
     
     protected int SelectScalarInteger(String sql) throws SQLException
     {
         PreparedStatement s = dbConnection.prepareStatement(sql);
         
         ResultSet rs = s.executeQuery();
         
         return rs.getInt(1);
     }
 }
