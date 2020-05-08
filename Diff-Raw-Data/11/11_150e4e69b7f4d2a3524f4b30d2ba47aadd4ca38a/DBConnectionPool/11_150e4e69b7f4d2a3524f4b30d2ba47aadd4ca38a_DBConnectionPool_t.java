 /**
  * 
  */
 package prosubmit.db;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import org.apache.tomcat.jdbc.pool.DataSource;
 
 /**
  * @author ramone
  *
  */
 @SuppressWarnings("all")
 public class DBConnectionPool {
 
     private DataSource pool = null; 
     
     public DBConnectionPool() {
    	pool = new DataSource();
	pool.setUrl("jdbc:mysql://zenit.senecac.on.ca:9120/pro_submit");
	pool.setUsername("ramone");
	pool.setPassword("ramone");
     }
     
     public Connection getConnection() {
         Connection connection = null;
         try {
             connection = pool.getConnection();
         } catch (SQLException e) {
         	System.out.println(e.getMessage());
         }
         return connection;
     }
 }
