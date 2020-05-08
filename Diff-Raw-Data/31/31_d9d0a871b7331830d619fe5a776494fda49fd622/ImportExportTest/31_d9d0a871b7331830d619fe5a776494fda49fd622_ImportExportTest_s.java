 /*
  * Copyright (C) 2011 cismet GmbH
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.cids.custom.sudplan.wupp.geocpm.ie;
 
 import de.cismet.remotetesthelper.ws.rest.RemoteTestHelperClient;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import de.cismet.tools.ScriptRunner;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.Properties;
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author bfriedrich
  */
 public class ImportExportTest 
 {    
     private GeoCPMImport importer;
     private GeoCPMExport exporter;
     private File         testOutFile;
     
     private static Connection CON;
     private static Statement  STMT;
 
     private static final String TEST_INPUT_FILE  = "GeoCPM_test.ein";
     private static final String TEST_OUTPUT_FILE = "GeoCPM_test_out.ein";
     
     private static final String DB_USER   = "postgres";
     private static final String DB_PWD    = "cismetz12";
     
     
     private static final String TEST_DB_NAME = "simple_geocpm_test_db";
     private static final RemoteTestHelperClient SERVICE = new RemoteTestHelperClient();
     
     
     public ImportExportTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception 
     {
         final Properties p = new Properties();
         p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
         p.put("log4j.appender.Remote.remoteHost", "localhost");
         p.put("log4j.appender.Remote.port", "4445");
         p.put("log4j.appender.Remote.locationInfo", "true");
         p.put("log4j.rootLogger", "ALL,Remote");
         org.apache.log4j.PropertyConfigurator.configure(p);
 
         if (! Boolean.valueOf(SERVICE.initCidsSystem(TEST_DB_NAME))) 
         {
             throw new IllegalStateException("cannot initilise test db");
         }
         
         CON  = SERVICE.getConnection(TEST_DB_NAME);
         STMT = CON.createStatement();    
 
         try
         {
             STMT.executeUpdate("drop view geosuche;"); // removed as geometry column modification wouldn't be possible otherwise
             STMT.execute("SELECT DropGeometryColumn('public','geom','geo_field');");
             STMT.execute("SELECT AddGeometryColumn( 'public','geom','geo_field', 31466, 'GEOMETRY', 3 );");
             
         }
         catch(final SQLException e)
         {
             e.printStackTrace();
             e.getNextException().printStackTrace();
             throw e;
         }
         
         final ScriptRunner runner = new ScriptRunner(CON, true, true);
         runner.runScript(new BufferedReader(
                                  new InputStreamReader(                       
                                      ImportExportTest.class.getResourceAsStream("../geocpm_db_v2.sql"))));
         
         System.out.println("");
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception 
     {
         STMT.close();
         CON.close();
         
         if (! Boolean.valueOf(SERVICE.dropDatabase(TEST_DB_NAME))) 
         {
             throw new IllegalStateException("could not drop test db");
         }
     }
     
     @Before
     public void setUp() throws Exception
     {
         this.testOutFile = new File(TEST_OUTPUT_FILE);
     }
     
     @After
     public void tearDown() throws Exception
     {
         this.testOutFile.delete();
     }
     
     private int getNewestConfigId() throws Exception
     {
         ResultSet result = null;
         try
         {
             result = STMT.executeQuery("select max(id) from geocpm_configuration");
             assertTrue(result.next());
             final int newConfigId = result.getInt(1);
             result.close();
             return newConfigId;  
         }
         finally
         {
            result.close();
         }
 
     }
     
     
     @Test
     public void testImportExport() throws Exception
     {
         final String dbURL =  CON.getMetaData().getURL();
        
         this.importer = new GeoCPMImport(ImportExportTest.class.getResourceAsStream(TEST_INPUT_FILE), DB_USER, DB_PWD, dbURL);
        this.importer.beginImport();
         
         this.exporter = new GeoCPMExport(this.getNewestConfigId(), this.testOutFile, DB_USER, DB_PWD, dbURL);
        this.exporter.beginExport();
     
       
         final List<String> inData  = IOUtils.readLines(ImportExportTest.class.getResourceAsStream(TEST_INPUT_FILE));
         final List<String> outData = IOUtils.readLines(new FileInputStream(this.testOutFile));
     
         assertEquals(inData.size(), outData.size());
         
         final int size = inData.size();
         for(int i = 0; i < size; i++)
         {
             assertEquals(inData.get(i).trim(), outData.get(i).trim());
         }
     }
 }
