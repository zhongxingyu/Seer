 package vahdin.data;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileUtils;
 import org.h2.api.Trigger;
 
 import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
 import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
 
 public class DB {
 
    private static final String H2_DB_NAME = "vahdin";
    private static final String H2_DB_PATH = System.getProperty("user.home") + System.getProperty("file.separator");
     private static final String DRIVER = "org.h2.Driver";
    private static final String CONNECTION_URI = "jdbc:h2:" + H2_DB_PATH + H2_DB_NAME
             + ";FILE_LOCK=NO";
     private static final String USER = "vahdin";
     private static final String PASSWORD = "";
     private static final Logger logger = Logger.getGlobal();
 
     public static final JDBCConnectionPool pool;
 
     static {
         try {
             logger.info("Initializing connection to " + CONNECTION_URI);
            File h2File = new File(H2_DB_PATH + H2_DB_NAME + ".h2.db");
             if (!h2File.exists()) {
                 logger.info("Copying database to working directory from classpath.");
                 InputStream in = DB.class.getResourceAsStream(H2_DB_NAME
                         + ".h2.db");
                 FileUtils.copyInputStreamToFile(in, h2File);
             }
             pool = new SimpleJDBCConnectionPool(DRIVER, CONNECTION_URI, USER,
                     PASSWORD);
         } catch (SQLException | IOException e) {
             throw new Error(e);
         }
     }
 
     public static class VersionIncrementer implements Trigger {
 
         private String tableName;
 
         @Override
         public void close() throws SQLException {
         }
 
         @Override
         public void fire(Connection connection, Object[] oldRow, Object[] newRow)
                 throws SQLException {
             Integer oldVersion = (Integer) oldRow[0];
             Integer newVersion = (Integer) newRow[0];
             if (oldVersion.equals(newVersion)) {
                 PreparedStatement prep = connection.prepareStatement("UPDATE "
                         + tableName + " SET version=version+1");
                 prep.execute();
             }
         }
 
         @Override
         public void init(Connection connection, String schemaName,
                 String triggerName, String tableName, boolean before, int type)
                 throws SQLException {
             this.tableName = tableName;
         }
 
         @Override
         public void remove() throws SQLException {
         }
     }
 }
