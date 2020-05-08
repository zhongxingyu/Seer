 package org.djd.fun.taiga.dao;
 
 import org.djd.fun.taiga.model.SomeData;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: acorn
  * Date: 10/13/12
  * Time: 11:03 AM
  * To change this template use File | Settings | File Templates.
  */
 public class WayOfDao {
 
 
   public void deleteAll() throws DaoException {
     Connection connection = getConnection();
     try {
       Statement stmt = connection.createStatement();
       stmt.executeUpdate("DROP TABLE IF EXISTS logs");
      stmt.executeUpdate("CREATE TABLE logs (log TEXT, id SERIAL)");
     } catch (SQLException e) {
       throw new DaoException(e);
     }
   }
 
   public void add(String logText) throws DaoException {
     Connection connection = getConnection();
     try {
       Statement stmt = connection.createStatement();
       stmt.executeUpdate(String.format("INSERT INTO logs VALUES ('%s')", logText));
     } catch (SQLException e) {
       throw new DaoException(e);
     }
   }
 
   public List<SomeData> getAll() throws DaoException {
     List<SomeData> result = new ArrayList<SomeData>();
     Connection connection = getConnection();
     try {
       Statement stmt = connection.createStatement();
       ResultSet rs = stmt.executeQuery("SELECT id, log FROM logs");
       while (rs.next()) {
         result.add(new SomeData(rs.getInt("id"), rs.getString("log")));
       }
     } catch (SQLException e) {
       throw new DaoException(e);
     }
     return result;
   }
 
 
   private Connection getConnection() throws DaoException {
     try {
       URI dbUri = new URI(System.getenv("HEROKU_POSTGRESQL_ROSE_URL"));
       String username = dbUri.getUserInfo().split(":")[0];
       String password = dbUri.getUserInfo().split(":")[1];
       String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
 
       return DriverManager.getConnection(dbUrl, username, password);
     } catch (SQLException e) {
       throw new DaoException(e);
     } catch (URISyntaxException e) {
       throw new DaoException(e);
     }
   }
 
 }
