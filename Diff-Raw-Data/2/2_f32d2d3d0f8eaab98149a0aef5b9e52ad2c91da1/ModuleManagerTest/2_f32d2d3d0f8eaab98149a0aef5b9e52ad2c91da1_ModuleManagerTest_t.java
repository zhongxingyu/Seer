 package edu.mines.acmX.exhibit.module_manager;
 
 import static org.junit.Assert.*;
 import org.junit.*;
 import org.apache.logging.log4j.Logger;
 
 /**
 * Unit test for ModuleManager
  */
 public class ModuleManagerTest 
 {
 
 	@Test
     public void testGetInstance() {
         ModuleManager m = ModuleManager.getInstance();
         assertTrue( m instanceof ModuleManager);
         ModuleManager other = ModuleManager.getInstance();
         assertTrue( m == other );
     }
 
     // The module manager should have an instance of ModuleManagerMetaData that
     // has been correctly instantiated with the given xml file.
 	@Test
     public void testLoadModuleManagerConfig() {
         String xmlPath = "module_manager/testModuleManagerManifest.xml";
         ModuleManager m = ModuleManager.getInstance();
         m.loadModuleManagerConfig(xmlPath);
         ModuleManagerMetaData shouldEqual = new ModuleManagerMetaData("com.example.test");
         assertTrue( m.getMetaData().equals( shouldEqual ));
     }
 
     // expect a throw when the xml is baddly formed
     @Test( expected=Exception.class )
     public void testBadXMLModuleManagerConfig() {
         String xmlPath = "module_manager/testBadXMLModuleManagerManifest.xml";
         ModuleManager m = ModuleManager.getInstance();
         m.loadModuleManagerConfig(xmlPath);
     }
 
     // expect a throw when an xml attribute is missing.
     @Test( expected=Exception.class )
     public void testBadDataModuleManagerConfig() {
         String xmlPath = "module_manager/testBadDataModuleManagerManifest.xml";
         ModuleManager m = ModuleManager.getInstance();
         m.loadModuleManagerConfig(xmlPath);
     }
 
     // This should go through the test/resources/modules directory and get the
     // appropriate ModuleMetaData structures from jar files.
     @Test
     public void testLoadAllModuleConfigs() {
         ModuleManager m = ModuleManager.getInstance();
         m.loadAllModuleConfigs( "modules" );
         // TODO check that modules are loaded correctly
         assertTrue( false );
     }
 
     @Test
     public void testLoadModuleFromMetaData() {
         // TODO
         assertTrue( false );
     }
 
     @Test
     public void testCheckModuleDependencies() {
         // TODO
         assertTrue( false );
     }
 
     @Test
     public void testModuleCanRun() {
         // TODO
         // may need to check order
         assertTrue( false );
     }
 
     @Test
     public void testRun() {
         // TODO
         assertTrue( false );
     }
 
     @Test
     public void testSetDefaultModule() {
         // TODO
         assertTrue( false );
     }
 
     @Test
     public void testSetNextModule() {
         // TODO
         assertTrue( false );
     }
 
 }
