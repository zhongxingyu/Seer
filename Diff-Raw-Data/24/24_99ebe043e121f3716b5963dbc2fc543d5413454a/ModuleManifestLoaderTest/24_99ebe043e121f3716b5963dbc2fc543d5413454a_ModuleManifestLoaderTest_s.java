 package edu.mines.acmX.exhibit.module_manager;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 /**
  * Unit test for ModuleManifestLoader.
  *
  */
 public class ModuleManifestLoaderTest {
 
     @Test
     public void testLoadCorrectModule() throws ManifestLoadException {
         String pathToJar = "modules/HorseSimpleGood.jar";
         Map<String, DependencyType> desiredInputs =  new HashMap<String, DependencyType>();
         Map<String, DependencyType> desiredModules =  new HashMap<String, DependencyType>();
         ModuleMetaData shouldEqual = new ModuleMetaData(
                 "com.andrew.random",
                 "Horses",
                 "0.0.0",
                 "0.0.0",
                 "horse.jpg",
                 "i_love_horseys",
                 "andrew demaria",
                 "0.1",
                 desiredInputs,
                 desiredModules);
 
         assertEquals( shouldEqual, ModuleManifestLoader.load( pathToJar ));
     }
 
     /**
      * Should fail when the manifest file cannot be located
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadBadModuleManifest() {
 
         fail( "Tests not complete" );
     }
 
     /**
      * Should fail when the xml cannot be parsed
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadBadJar() {
         // TODO 
         fail( "Tests not complete" );
     }
 
     /**
     *
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadManifestIllegalStructure() {
         // TODO
         fail( "Tests not complete" );
     }
 }
