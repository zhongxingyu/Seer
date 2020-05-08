 // Copyright (c) 2006 ScenPro, Inc.
 
// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/Seed.java,v 1.9 2008-05-28 19:01:20 hebell Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.freestylesearch.util;
 
 import gov.nih.nci.cadsr.freestylesearch.tool.DBAccess;
 import gov.nih.nci.cadsr.freestylesearch.tool.DBAccessIndex;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Timestamp;
import java.util.Enumeration;
 import java.util.InvalidPropertiesFormatException;
 import java.util.Properties;
 import javax.sql.DataSource;
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 /**
  * Seed the freestyle search index tables. The caDSR is scanned for updates using the record date_modified
  * and comparing to the last seed timestamp. Updated records are used to refresh the index tables.
  * 
  * @author lhebel Mar 3, 2006
  */
 public class Seed
 {
     /**
      * Constructor
      *
      */
     public Seed()
     {
     }
     
     /**
      * Main entry when started from a command line.
      * 
      * @param args_ no valid arguments at this time.
      */
     public static void main(String[] args_)
     {
         long millis = System.currentTimeMillis();
 
         DOMConfigurator.configure(args_[0]);
         
         Seed var = new Seed();
 
         Properties prop = new Properties();
         FileInputStream ins = null;
         try
         {
             ins = new FileInputStream(args_[1]);
             prop.loadFromXML(ins);
         }
         catch (FileNotFoundException e)
         {
             e.printStackTrace();
             return;
         }
         catch (InvalidPropertiesFormatException e)
         {
             e.printStackTrace();
             return;
         }
         catch (IOException e)
         {
             e.printStackTrace();
             return;
         }
         finally
         {
             if (ins != null)
             {
                 try
                 {
                     ins.close();
                 }
                 catch (Exception ex)
                 {
                 }
             }
         }
         
         String version = Search.getVersion();
         
         var._dbIndex = prop.getProperty("index.DSurl");
         var._userIndex = prop.getProperty("index.DSusername");
         var._pswdIndex = prop.getProperty("index.DSpassword");
         var._dbData = prop.getProperty("data.DSurl");
         var._userData = prop.getProperty("data.DSusername");
         var._pswdData = prop.getProperty("data.DSpassword");
         
         String schema = prop.getProperty("index.DSschema");
         DBAccessIndex.setSchema(schema);
         
         _logger.info(" ");
         _logger.info("---------------------------------------------------------------------------------");
         _logger.info("Seed " + version + " started for schema... " + schema.toUpperCase());
 
         try
         {
             var.parseDatabase(millis);
         }
         catch (Exception ex)
         {
             _logger.error(ex.toString());
            Enumeration loop = prop.propertyNames();
            while (loop.hasMoreElements())
            {
                String key = (String) loop.nextElement();
                _logger.error("Property " + key + " = " + prop.getProperty(key));
            }
         }
 
         _logger.info("Seed ended ...");
     }
     
     /**
      * Set the DataSource holding the connections.
      * 
      * @param ds_ the DataSource
      */
     public void setDS(DataSource ds_)
     {
         _ds = ds_;
     }
     
     /**
      * Set the database connection credentials
      * 
      * @param user_ the user account
      * @param pswd_ the password
      */
     public void setCredentials(String user_, String pswd_)
     {
         _userData = user_;
         _userIndex = user_;
         _pswdData = pswd_;
         _pswdIndex = pswd_;
     }
     
     /**
      * Parse the database and extract selected tokens.
      * 
      * @param millis_ the marker time for this run in milliseconds 
      * @exception SearchException
      */
     public void parseDatabase(long millis_) throws SearchException
     {
         // Connect to database.
         DBAccess data = new DBAccess();
         DBAccessIndex index = new DBAccessIndex();
 
         try
         {
             if (_ds == null)
             {
                 data.open(_dbData, _userData, _pswdData);
                 index.open(_dbIndex, _userIndex, _pswdIndex);
             }
             else
             {
                 data.open(_ds, _userData, _pswdData);
                 index.open(_ds, _userIndex, _pswdIndex);
             }
 
             getLastSeedTimestamp(data, millis_);
     
             _logger.info("Retrieving changes made since " + _start.toString());
             data.parseDatabase(_start, index);
         }
         catch (SearchException ex)
         {
             throw ex;
         }
         finally
         {
             index.close();
             data.close();
         }
     }
     
     /**
      * Calculate the time at midnight the previous day. This will be >24 and <48
      * hours in the past.
      * 
      * @return the timestamp for midnight yesterday
      */
     private Timestamp getYesterday()
     {
         Timestamp start;
         long ct = System.currentTimeMillis();
         ct -= 24 * 60 *60 * 1000;
         start = new Timestamp(ct);
         String yesterday = start.toString().split(" ")[0];
         start = Timestamp.valueOf(yesterday + " 00:00:00");
 
         start = Timestamp.valueOf("2006-06-15 00:00:00");
         return start;
     }
 
     /**
      * Get the last seed timestamp and set the current timestamp to the value
      * specified.
      * 
      * @param index_ the index table database access object
      * @param millis_ >0 resets the seed timestamp, ==0 does not
      * @exception SearchException
      */
     private void getLastSeedTimestamp(DBAccess index_, long millis_) throws SearchException
     {
         _start = index_.getLastSeedTimestamp();
         if (_start == null)
             _start = getYesterday();
         
         if (millis_ > 0)
             index_.setLastSeedTimestamp(millis_);
     }
 
     private Timestamp _start;
     private String _dbIndex;
     private String _userIndex;
     private String _pswdIndex;
     private String _dbData;
     private String _userData;
     private String _pswdData;
     private DataSource _ds;
     
     private static final Logger _logger = Logger.getLogger(Seed.class.getName());
 }
