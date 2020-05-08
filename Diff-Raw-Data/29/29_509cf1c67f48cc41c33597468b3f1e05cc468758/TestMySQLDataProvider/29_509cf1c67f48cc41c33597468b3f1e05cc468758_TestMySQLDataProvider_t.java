 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tehbeard.beardstat.dataproviders;
 
 import com.tehbeard.beardstat.DatabaseConfiguration;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.Properties;
 import org.junit.Assume;
 import org.junit.Before;
 
 /**
  *
  * @author James
  */
 
 public class TestMySQLDataProvider extends IStatDataProviderTest  {
     
     public TestMySQLDataProvider(){
         
 
     }
     
     @Before
     public void setUp() throws IOException, SQLException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("mysql.properties");
         if(is == null){
             System.out.println("WARNING: MYSQL TEST NOT CONFIGURED, TEST SKIPPED.");
         }
         Assume.assumeTrue("MySQL test properties configured.", is != null);
         DatabaseConfiguration config = new DatabaseConfiguration(7);
         Properties properties = new Properties();
         properties.load(is);
         new JavaPropertiesInjector(properties).inject(config);
         config.version = config.latestVersion;
         
         //System.out.println(config);
         instance = new MysqlStatDataProvider(new TestPlatform(), config);
     }
 
     
 }
