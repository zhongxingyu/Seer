 package persistence.connectAndSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import persistence.exception.NoConnectionToDatabaseException;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: agnidash
  * Date: 4/17/13
  * Time: 5:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Connector {
 
     @Autowired
    private DriverManagerDataSource source;
 
     /**
      * Instantiate connection
      *
      * @return new connection to h2 database
      * @throws RuntimeException if SQLException occurs
      */
     public Connection newConnection() {
         try {
             return source.getConnection();
         } catch (SQLException e) {
            throw new NoConnectionToDatabaseException("Can't instantiate connection to '" + source.getUrl() + "' database ", e);
         }
     }
 }
