 package de.cismet.cids.jpa.backend.service.impl;
 
 
 
 import de.cismet.cids.jpa.backend.service.Backend;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrExempt;
 import de.cismet.cids.jpa.entity.permission.AttributePermission;
 import de.cismet.cids.jpa.entity.permission.ClassPermission;
 import de.cismet.cids.jpa.entity.permission.NodePermission;
 import de.cismet.cids.jpa.entity.user.User;
 import de.cismet.cids.jpa.entity.user.UserGroup;
 import de.cismet.diff.db.DatabaseConnection;
 import de.cismet.tools.ScriptRunner;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Properties;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.BeforeClass;
 
 import static org.junit.Assert.*;
 
 /**
  *
  * @author martin.scholl@cismet.de
  */
 public class UserBackendTest
 {
 
     private static Properties runtimeProperties;
     private static Backend backend;
 
 //    @BeforeClass
     public static void setUpClass() throws Exception
     {
         final Properties p = new Properties();
         p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
         p.put("log4j.appender.Remote.remoteHost", "localhost");
         p.put("log4j.appender.Remote.port", "4445");
         p.put("log4j.appender.Remote.locationInfo", "true");
         p.put("log4j.rootLogger", "ALL,Remote");
         p.put("log4j.logger.org.hibernate", "WARN,Remote");
         p.put("log4j.logger.com.mchange.v2", "WARN,Remote");
         p.put("log4j.logger.net.sf.ehcache", "WARN,Remote");
         org.apache.log4j.PropertyConfigurator.configure(p);
 
         runtimeProperties = new Properties();
         runtimeProperties.load(UserBackendTest.class.getResourceAsStream("runtime.properties")); // NOI18N
         backend = BackendFactory.getInstance().getBackend(runtimeProperties, false);
     }
 
 //    @AfterClass
     public static void tearDownClass() throws Exception
     {
     }
 
 //    @Before
     public void setUp()throws Exception {
         
         final ScriptRunner runner = new ScriptRunner(DatabaseConnection.getConnection(runtimeProperties), false, true);
         final InputStream scriptStream = ConfigAttrBackendTest.class.getResourceAsStream("userBackendTestData.sql"); // NOI18N
         final BufferedReader scriptReader = new BufferedReader(new InputStreamReader(scriptStream));
         try {
             runner.runScript(scriptReader);
         } finally {
             scriptReader.close();
         }
     }
 
 //    @After
     public void tearDown()
     {
     }
 
 
     private String getCurrentMethodName()
     {
         return new Throwable().getStackTrace()[1].getMethodName();
     }
 
 //    @Test
     public void testGetUser()
     {
     }
 
 //    @Test
     public void testGetClassPermissions()
     {
     }
 
 //    @Test
     public void testGetLowestUGPrio()
     {
     }
 
 //    @Test
     public void testDeleteUG()
     {
         System.out.println("TEST " + getCurrentMethodName());
         
         final UserGroup u = backend.getEntity(UserGroup.class, 1);
         backend.delete(u);
         List l = backend.getAllEntities(UserGroup.class);
         assertEquals("ug still present", 0, l.size());
         
         l = backend.getAllEntities(ClassPermission.class);
         assertEquals("cperm still present", 0, l.size());
         
         l = backend.getAllEntities(NodePermission.class);
         assertEquals("nperm still present", 0, l.size());
         
         l = backend.getAllEntities(AttributePermission.class);
         assertEquals("aperm still present", 0, l.size());
         
         l = backend.getAllEntities(ConfigAttrEntry.class);
         assertEquals("cfg attrs still present", 1, l.size());
        
        l = backend.getAllEntities(ConfigAttrExempt.class);
        assertEquals("cfg attr exempt still present", 0, l.size());
     }
     
 //        @Test
     public void testDeleteUser()
     {
         System.out.println("TEST " + getCurrentMethodName());
         
         final User u = backend.getEntity(User.class, 1);
         backend.delete(u);
         List l = backend.getAllEntities(User.class);
         assertEquals("user still present", 0, l.size());
         
         l = backend.getAllEntities(ConfigAttrEntry.class);
         assertEquals("cfg attrs still present", 6, l.size());
         
         l = backend.getAllEntities(ConfigAttrExempt.class);
         assertEquals("cfg attr exempt still present", 0, l.size());
     }
 }
