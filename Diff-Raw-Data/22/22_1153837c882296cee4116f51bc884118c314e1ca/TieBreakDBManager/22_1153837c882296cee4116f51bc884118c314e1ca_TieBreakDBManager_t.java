 package DAL;
 
 import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
 import java.io.FileReader;
 import java.util.Properties;
 
 public class TieBreakDBManager
 {
 

     /**
     * Establishes the connection to the database for all DAL classes
     * SQLServerDataSource is from the sqljdbc4 library
      */
     protected SQLServerDataSource ds;
 
   
     /**
     * Constructor of the tie break db manager
     * This makes the connection between the DAL and the database
     * It is used in every DAL class
      * @throws Exception
      */
     public TieBreakDBManager() throws Exception
     {
         Properties props = new Properties();
         props.load(new FileReader("TieBreak.cfg"));
 
         ds = new SQLServerDataSource();
 
         ds.setServerName(props.getProperty("SERVER"));
         ds.setPortNumber(Integer.parseInt(props.getProperty("PORT")));
         ds.setDatabaseName(props.getProperty("DATABASE"));
         ds.setUser(props.getProperty("USER"));
         ds.setPassword(props.getProperty("PASSWORD"));
     }
 }
