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
     public void testLoadCorrectModuleWithoutExtraStuff() throws ManifestLoadException {
        String pathToJar = "/home/andrew/Documents.local/interface_sdk/src/test/resources/modules/HorseSimpleGood.jar";
         Map<InputType, DependencyType> desiredInputs =  new HashMap<InputType, DependencyType>();
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
         
         ModuleMetaData expected = ModuleManifestLoader.load( pathToJar );
         
         System.out.println("Expected:" + shouldEqual.toString());
         System.out.println();
         System.out.println("Actual:" + expected.toString());
 
         assertTrue( shouldEqual.equals(expected));
     }
 
     @Test
     public void testLoadCorrectModuleWithOptionalModulesAndInputs() throws ManifestLoadException {
        String pathToJar = "/home/andrew/Documents.local/interface_sdk/src/test/resources/modules/PiggyGoodWithLotsOfDepend.jar";
         Map<InputType, DependencyType> desiredInputs =  new HashMap<InputType, DependencyType>();
         Map<String, DependencyType> desiredModules =  new HashMap<String, DependencyType>();
 
         desiredInputs.put(InputType.ACCELERATION, DependencyType.OPTIONAL);
         desiredInputs.put(InputType.IMAGE2D, DependencyType.REQUIRED);
 
         desiredModules.put("edu.mines.acmX.some_other_game", DependencyType.REQUIRED);
         desiredModules.put("edu.mines.acmX.another_game", DependencyType.OPTIONAL);
 
         ModuleMetaData shouldEqual = new ModuleMetaData(
                 "com.andrew.lotsofdepends",
                 "Piggy",
                 "0.0.0",
                 "0.0.0",
                 "",
                 "i_love_piggys",
                 "andrew demaria",
                 "0.1",
                 desiredInputs,
                 desiredModules);
 
         assertEquals( shouldEqual, ModuleManifestLoader.load( pathToJar ));
     }
 
     /**
      * Should fail when the manifest file cannot be located
      * @throws ManifestLoadException 
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadBadModuleManifest() throws ManifestLoadException {
        String jarPath = "modules/BadModuleManifest.jar";
         ModuleManifestLoader.load( jarPath );
     }
 
     /**
      * Should fail when the xml cannot be parsed
      * @throws ManifestLoadException 
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadMaformedXMLManifest() throws ManifestLoadException {
        String jarPath = "modules/MaformedXMLManifest.jar";
         ModuleManifestLoader.load( jarPath );
     }
 
     /**
      * Should fail when the data is incorrect
      * @throws ManifestLoadException 
      */
     @Test(expected=ManifestLoadException.class)
     public void testLoadManifestIllegalStructure() throws ManifestLoadException {
        String jarPath = "modules/ManifestIllegalStructure.jar";
         ModuleManifestLoader.load( jarPath );
     }
 }
