 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.reporting.dbwriter;
 import com.sun.grid.reporting.dbwriter.db.Database;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.logging.Level;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class TestParsing extends AbstractDBWriterTestCase {
    
    String debugLevel;
    
    /** Creates a new instance of TestParsing */
    public TestParsing(String name) {
       super(name);
    }
    
    public void setUp() throws Exception {
       
       super.setUp();
       
       debugLevel = DBWriterTestConfig.getTestDebugLevel();
       if( debugLevel == null ) {
          debugLevel = Level.INFO.toString();
       }
       
    }
    
    public static Test suite() {
       TestSuite suite = new TestSuite(TestParsing.class);
       return suite;
    }
    
    
    /**
     * This test case test wether the dbwriter
     * can handle optional fields in the reporting file
     * without exiting
     * @throws java.lang.Exception
     */
    public void testOptionalValues() throws Exception {
       Iterator iter = getDBList().iterator();
       
       while(iter.hasNext()) {
          TestDB db = (TestDB)iter.next();
          String orgDebugLevel = db.getDebugLevel();
          try {
             db.setDebugLevel(debugLevel);
             doTestOptionalValues(db);
          } finally {
             db.setDebugLevel(orgDebugLevel);
          }
       }
    }
    
    
    /**
     * This test case test wether the dbwriter
     * can handle optional fields in the reporting file
     * without exiting
     * @throws java.lang.Exception
     */
    private void doTestOptionalValues(TestDB db) throws Exception {
       
       db.cleanDB();
       
       ReportingDBWriter dbw = createDBWriter(debugLevel, db);
       TestFileWriter writer = new TestFileWriter();
       SQLHistory sqlHistory = new SQLHistory();
       
       dbw.setReportingFile(writer.getReportingFile().getAbsolutePath());
       dbw.getDatabase().addDatabaseListener(sqlHistory);
       dbw.initialize();
       dbw.start();
       assertEquals( "Error on dbwriter startup, dbwriter thread is not alive", true, dbw.isAlive() );
       
       try {
          long timestamp = System.currentTimeMillis() / 1000;
          String hostname = "wowamd.sfbay.sun.com";
          
          // Write a hostline with a delimiter in the load value names
          writer.writeHostLine(timestamp, hostname, new String[] {
             "cpu","np_load_avg","mem_free","virtual_free","arch"}, new String[] {
             "0.120304","0.159159","1413.792969M","9876.123456M","lx24-amd64"} );
          
          assertTrue("rename of reporting file failed", writer.rename());
          writer.waitUntilFileIsDeleted();
          
          int hostValues = queryHostValues(dbw.getDatabase());
          assertEquals( "Not correct number of entries in the sge_host_values", 5, hostValues);
          
       } finally {
          shutdownDBWriter(dbw);
       }
    }
    
    /**
     * query the host values entries from the database.
     * @param db          database of the dbwriter
     * @throws Exception  can throw any exception
     * @return number of job log entries
     */
    private int queryHostValues(Database db) throws Exception {
       Connection conn = db.getConnection();
       try {
          String sql = DBWriterTestConfig.getTestHostValuesSQL();
          Statement stmt = db.executeQuery( sql, conn );
          try {
             ResultSet rs = stmt.getResultSet();
             try {
                if( rs.next() ) {
                   return rs.getInt(1);
                } else {
                   return 0;
                }
             } finally {
                rs.close();
             }
          } finally {
             stmt.close();
          }
       } finally {
          db.release(conn);
       }
    }
    
    /**
     * This test case test wether the dbwriter
     * can handle invalid lines in the reporting file
     * without exiting
     * @throws java.lang.Exception
     */
    public void testInvalidLine() throws Exception {
       Iterator iter = getDBList().iterator();
       
       while(iter.hasNext()) {
          TestDB db = (TestDB)iter.next();
          String orgDebugLevel = db.getDebugLevel();
          try {
             db.setDebugLevel(debugLevel);
             doTestInvalidLine(db);
          } finally {
             db.setDebugLevel(orgDebugLevel);
          }
       }
    }
    
    
    /**
     * This test case test wether the dbwriter
     * can handle invalid lines in the reporting file
     * without exiting
     * @throws java.lang.Exception
     */
    private void doTestInvalidLine(TestDB db) throws Exception {
       
       db.cleanDB();
       ReportingDBWriter dbw = createDBWriter(debugLevel, db);
       TestFileWriter writer = new TestFileWriter();
       dbw.setReportingFile(writer.getReportingFile().getAbsolutePath());
       dbw.initialize();
       dbw.start();
       
       try {
          long timestamp = System.currentTimeMillis() / 1000;
          long oldtms=timestamp;
          String hostname = "schrotty";
          
          // write a invalid and a valid line into the reporting file
          PrintWriter pw = writer.getPrintWriter();
          pw.println("HumpfelBumpf");
          
          // Write a hostline with an invalid timestamp at the beginning;
          pw.println("Ja irgendwann:host:schrotty:cpu=1");
          pw.flush();
          
          // Write a hostline with a delimiter in the load value names
          // This line must be skipped
          writer.writeHostLine(timestamp, hostname, new String[] { "cpu:" }, new String[] { "1.3" } );
          
          // Write a hostline with a value with an unknown suffix is considered as text attribute
          // This test line must be skipped
          // writer.writeHostLine(timestamp, hostname, new String[] { "cpu" }, new String[] { "1.3q" } );
          
          // write a valid line into the reporting file
          
          String [] value_names   = new String  [] { "cpu", "mem_free"     , "null_value", "np_load_avg" };
          String [] string_values = new String  [] { "1.3", null           , ""          , "3.0"       };
          double [] double_values = new double  [] { 1.3  , Double.NaN     , 0.0         , 3.0        };
          
          Object [][] memValues = {
             { "4.12G", new Double(4.12*1024.0*1024.0*1024.0)  },
             { "20.2g", new Double(20.2*1000.0*1000.0*1000.0)  },
             { "1023M", new Double(1023.0*1024.0*1024.0)       },
             { "1023m", new Double(1023.0*1000.0*1000.0)       },
             { "1023K", new Double(1023.0*1024.0)            },
             { "1023k", new Double(1023.0*1000.0)            },
          };
          
          for(int i = 0; i < memValues.length; i++ ) {
             
             string_values[1] = (String)memValues[i][0];
             double_values[1] = ((Double)memValues[i][1]).doubleValue();
             while(oldtms==timestamp){
                Thread.sleep(250);
                timestamp = System.currentTimeMillis() / 1000;
             }
             oldtms=timestamp;
             writer.writeHostLine(timestamp, hostname, value_names, string_values);
             
             assertEquals( "Renaming failed", writer.rename(), true );
             
             writer.waitUntilFileIsDeleted();
             
             assertEquals( "Error on dbwriter startup, dbwriter thread is not alive", true, dbw.isAlive() );
 
             queryHostValues(dbw.getDatabase(), hostname, timestamp, value_names, string_values, double_values );
          }
          
       } finally {
          shutdownDBWriter(dbw);
       }
    }
    
    
    
    public void testParsing() throws Exception {
       Iterator iter = getDBList().iterator();
       
       
       while(iter.hasNext()) {
          TestDB db = (TestDB)iter.next();
          String orgDebugLevel = db.getDebugLevel();
          try {
             db.setDebugLevel(debugLevel);
             doTestParsing(db);
          } finally {
             db.setDebugLevel(orgDebugLevel);
          }
       }
    }
    
    /**
     * This method tests the parsing of the double values
     * "inf", "NaN" and the parsing of the suffixes 'm', 'M',
     * 'k' and 'K'.
     *
     * @throws java.lang.Exception
     */
    private void doTestParsing(TestDB db) throws Exception {
       
       db.cleanDB();
       
       ReportingDBWriter dbw = createDBWriter(debugLevel, db);
       
       TestFileWriter writer = new TestFileWriter();
       
       
       dbw.setReportingFile(writer.getReportingFile().getAbsolutePath());
       
       // It is import that the normalize the timestamp to seconds, because
       // the timestamp in the reporting file is also in seconds and the milliseconds
       // are truncated.
       
       long timestamp = System.currentTimeMillis() / 1000;
       String hostname = "schrotty";
       
       String [] value_names   = new String  [] { "inf", "nan", "bin_mega"   , "dec_mega" , "bin_kilo", "dec_kilo", "plain" };
       String [] string_values = new String  [] { "inf", "NaN"  , "1.0M"     , "1.0m"     , "1.0K"    , "1.0k"    , "1.0"};
       double [] double_values = new double  [] { 0.0  , 0.0    , 1024*1024  , 1000*1000  , 1024      , 1000      , 1.0 };
       
       writer.writeHostLine(timestamp, hostname, value_names, string_values);
       
       assertEquals( "Renaming failed", writer.rename(), true );
       
       // start the dbwriter, it will parse the three lines and write int into
       // the database
       
       dbw.initialize();
       
       try {
          dbw.start();
          
          writer.waitUntilFileIsDeleted();
          
          assertEquals( "Error on dbwriter startup, dbwriter thread is not alive", true, dbw.isAlive() );
 
          queryHostValues(dbw.getDatabase(), hostname, timestamp, value_names, string_values, double_values );
       } finally {
          shutdownDBWriter(dbw);
       }
    }
    
    public void testAccountingLine() throws Exception {
       Iterator iter = getDBList().iterator();
       
       
       while(iter.hasNext()) {
          TestDB db = (TestDB)iter.next();
          String orgDebugLevel = db.getDebugLevel();
          try {
             db.setDebugLevel(debugLevel);
             doTestAccountingLine(db);
          } finally {
             db.setDebugLevel(orgDebugLevel);
          }
       }
    }
    
    /**
     * This methods test the parsing of the 'acct' line that does not
     * contain the ar_number. This could come from file prior to 6.2.
     */
    private void doTestAccountingLine(TestDB db) throws Exception {
       
       db.cleanDB();
       
       ReportingDBWriter dbw = createDBWriter(debugLevel, db);
       
       TestFileWriter writer = new TestFileWriter();
       
       
       dbw.setReportingFile(writer.getReportingFile().getAbsolutePath());
       
       // It is import that the normalize the timestamp to seconds, because
       // the timestamp in the reporting file is also in seconds and the milliseconds
       // are truncated.
       
       long current = System.currentTimeMillis() / 1000;
       long start = current - 20;
       long submission = start - 20;
      
       writer.writeAccountingLineWithoutAR(current, start, submission);
       
       assertEquals( "Renaming failed", writer.rename(), true );
       
       // start the dbwriter, it will parse the three lines and write int into
       // the database
       
       dbw.initialize();
       
       try {
          dbw.start();
          
          writer.waitUntilFileIsDeleted();
          
          assertEquals( "Error on dbwriter startup, dbwriter thread is not alive", true, dbw.isAlive() );
         
          String sql = "SELECT COUNT(*) FROM sge_job";         
          int job = queryDB(dbw.getDatabase(), sql);
          assertEquals( "Not correct number of entries in the sge_job", 1, job);
          
          sql = "SELECT COUNT(*) FROM sge_job_usage";
          int jobUsage = queryDB(dbw.getDatabase(), sql);
          assertEquals( "Not correct number of entries in the sge_job", 1, jobUsage);
       } finally {
          shutdownDBWriter(dbw);
       }
    }
    
    /**
     * query the database with a count query
     * @param db          database of the dbwriter
     * @param sql         the sql String (generally count query)
     * @throws Exception  can throw any exception
     * @return number of job log entries
     */
    private int queryDB(Database db, String sql) throws Exception {
       Connection conn = db.getConnection();
       try {
          Statement stmt = db.executeQuery( sql, conn );
          try {
             ResultSet rs = stmt.getResultSet();
             try {
                if( rs.next() ) {
                   return rs.getInt(1);
                } else {
                   return 0;
                }
             } finally {
                rs.close();
             }
          } finally {
             stmt.close();
          }
       } finally {
          db.release(conn);
       }
    }
    
    /**
     * query host values from the database.
     * @param db            database of the dbwriter
     * @param hostname      name of the host
     * @param value_names   name of the values
     * @param string_values the expected string values
     * @param double_values the expected double values
     * @throws Exception  can throw any exception
     */
    private void queryHostValues(Database db, String hostname, long timestamp,
          String [] value_names, String [] string_values, double[] double_values ) throws Exception {
       Connection conn = db.getConnection();
       try {
          Timestamp time = new Timestamp(timestamp*1000);
          
          String sql = "select hv_variable, hv_svalue, hv_dvalue from sge_host_values, sge_host "
                + "where sge_host.h_id = sge_host_values.hv_parent"
                + "  and sge_host.h_hostname = '" + hostname + "'"
                + "  and sge_host_values.hv_time_start >= {ts '" + time.toString() + "'}"
                + "  and sge_host_values.hv_time_end   <= {ts '" + time.toString() + "'}";
          
          boolean [] tested = new boolean[value_names.length];
          
          Arrays.fill(tested,false);
          
          Statement stmt = db.executeQuery( sql, conn );
          try {
             ResultSet rs = stmt.getResultSet();
             
             try {
                while( rs.next() ) {
                   
                   String name = rs.getString("hv_variable");
                   String svalue = rs.getString("hv_svalue");
                   double dvalue = rs.getDouble("hv_dvalue");
                   
                   boolean notFound = true;
                   for(int i = 0; i < value_names.length; i++ ) {
                      if( value_names[i].equals(name) ) {
                         notFound = false;
                         
                         tested[i] = true;
                         // test the string value
                         if( svalue == null ) {
                            svalue = "";
                         }
                         assertEquals("invalid string value for " + name, string_values[i], svalue);
                         
                         // test the double value
                         assertEquals("invalid double value " + name + "(" + string_values[i] + ")",
                               double_values[i], dvalue, 0.000001 );
                      }
                   }
                   assertEquals("found unexpected host_value " + name, false, notFound );
                }
                
                // Check that all values have been found in the database
                for(int i = 0; i < value_names.length; i++ ) {
                   assertEquals("host value " + value_names[i] + "(" + string_values[i] + ") has not been tested", tested[i], true );
                }
                
             } finally {
                rs.close();
             }
             
             
          } finally {
             stmt.close();
          }
       } finally {
          db.release(conn);
       }
    }
    
 }
