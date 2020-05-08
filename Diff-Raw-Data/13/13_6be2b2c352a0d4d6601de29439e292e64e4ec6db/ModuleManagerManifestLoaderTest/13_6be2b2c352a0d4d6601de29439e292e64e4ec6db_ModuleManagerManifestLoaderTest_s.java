 package edu.mines.acmX.exhibit.module_management.loaders;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 import edu.mines.acmX.exhibit.module_management.metas.ModuleManagerMetaData;
 
 /**
  * Unit test for ModuleManagerManifestLoader.
  *
  */
 public class ModuleManagerManifestLoaderTest {
 
     @Test
     public void testLoadCorrectModuleManagerManifest() throws ManifestLoadException {
     	Map<String,String> configs = new HashMap<String,String>();
    	configs.put("kinectopenni", "src/test/resources/openni-config.xml");
     	ModuleManagerMetaData data = new ModuleManagerMetaData(
     			"com.austindiviness.cltest",
     			"src/test/resources/modules",
     			configs);
     	ModuleManagerMetaData actual = ModuleManagerManifestLoader.load("src/test/resources/module_manager/ExampleModuleManagerManifest.xml");
     	System.out.println("Actual: " + actual);
 		assertEquals(data, actual);
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
