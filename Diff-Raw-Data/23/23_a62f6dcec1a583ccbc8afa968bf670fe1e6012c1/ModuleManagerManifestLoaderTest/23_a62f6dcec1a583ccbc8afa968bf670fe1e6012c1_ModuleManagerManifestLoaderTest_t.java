 package edu.mines.acmX.exhibit.module_manager;
 
 import static org.junit.Assert.*;
 import org.junit.*;
 import org.apache.logging.log4j.Logger;
 
 /**
  * Unit test for ModuleManagerManifestLoader.
  *
  */
 public class ModuleManagerManifestLoaderTest {
 
     @Test
     public void testLoadCorrectModuleManagerManifest() throws ManifestLoadException {
         ModuleManagerMetaData data = new ModuleManagerMetaData("com.example.test", "/home/andrew/");
 		assertEquals(data, ModuleManagerManifestLoader.load("src/test/resources/module_manager/testModuleManagerManifest.xml"));
     }
 
     /**
      * Should fail when the manifest file cannot be located
      * @throws ManifestLoadException 
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadBadModuleManagerManifest() throws ManifestLoadException {
 		String path = "module_manager/IDoNotExist.xml";
         ModuleManagerManifestLoader.load( path );
     }
 
     /**
      * Should fail when the xml cannot be parsed
      * @throws ManifestLoadException 
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadMaformedXMLManifest() throws ManifestLoadException {
        String path = "src/test/resources/module_manager/testBadXMLModuleManagerManifest.xml";
         ModuleManagerManifestLoader.load( path );
     }
 
     /**
      * Should fail when the data is incorrect
      * @throws ManifestLoadException 
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadManifestIllegalStructure() throws ManifestLoadException {
        String path = "src/test/resources/module_manager/testBadDataModuleManagerManifest.xml";
         ModuleManagerManifestLoader.load( path );
     }
 }
