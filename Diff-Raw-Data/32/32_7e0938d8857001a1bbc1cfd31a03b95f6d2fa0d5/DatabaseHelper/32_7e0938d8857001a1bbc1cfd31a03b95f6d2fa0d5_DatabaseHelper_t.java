 package be.noselus.db;
 
import be.noselus.util.Service;
 import com.jolbox.bonecp.BoneCP;
 import com.jolbox.bonecp.BoneCPConfig;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 @Singleton
 public class DatabaseHelper implements Service {
 
     private final DbConfig dbConfig;
     private BoneCP connectionPool;
 
     @Inject
     public DatabaseHelper(final DbConfig dbConfig) {
         this.dbConfig = dbConfig;
     }
 
     public void start() {
         dbConfig.invoke();
         try {
             // load the database driver (make sure this is in your classpath!)
             Class.forName(dbConfig.getDriver());
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         try {
             // setup the connection pool
             BoneCPConfig config = new BoneCPConfig();
             config.setJdbcUrl(dbConfig.getUrl()); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
             config.setUsername(dbConfig.getUsername());
             config.setPassword(dbConfig.getPassword());
             config.setMinConnectionsPerPartition(5);
             config.setMaxConnectionsPerPartition(18);
             config.setPartitionCount(1);
             connectionPool = new BoneCP(config); // setup the connection pool
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
 
 
     }
 
     public Connection getConnection(boolean autoCommit, boolean readOnly) throws SQLException {
         final Connection connection = connectionPool.getConnection();
         connection.setAutoCommit(autoCommit);
 //        connection.setReadOnly(readOnly);
         return connection; // fetch a connection
     }
 
     public void stop() {
         connectionPool.shutdown(); // shutdown connection pool.
     }
 }
