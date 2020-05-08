/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package DAL;
 
 import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
 import java.io.FileReader;
 import java.util.Properties;
 
/**
 *
 * @author Chris
 */
 public class TieBreakDBManager
 {
 
     /**
     *
      */
     protected SQLServerDataSource ds;
 
     /**
     *
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
