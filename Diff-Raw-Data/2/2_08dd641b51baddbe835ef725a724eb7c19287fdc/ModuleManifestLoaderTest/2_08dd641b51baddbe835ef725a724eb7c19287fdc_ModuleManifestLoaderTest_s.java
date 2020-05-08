 package edu.mines.acmX.exhibit.module_manager;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Unit test for Module.
  *
  * TODO cleanup
  * Module should have methods that can be overridden by child classes.
  */
 public class ModuleManifestLoaderTest
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public ModuleManifestLoaderTest( String testName )
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
        return new TestSuite( ModuleManifestLoader.class );
     }
 
 }
