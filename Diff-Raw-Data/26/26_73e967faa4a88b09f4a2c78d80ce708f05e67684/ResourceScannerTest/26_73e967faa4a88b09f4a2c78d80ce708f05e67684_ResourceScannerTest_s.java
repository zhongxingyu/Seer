 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.peacocks;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
import java.util.Collections;
 import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
 import static org.junit.Assert.*;
import org.junit.BeforeClass;
 
 /**
  *
  * @author edvicif
  */
 public class ResourceScannerTest {
     
 
     @Test
     public void testResourceAreFind() throws IOException {
         Path path = Paths.get("./src/test/resources");
         List<ResourceSubject> results = ResourceScanner.INSTANCE.findResources(path);
         assertEquals(7, results.size());
         
         ResourceSubject expected1 = ResourceSubject.Builder.instance().packageName("").resourceName("TextResource").resourceType("txt").build();
         ResourceSubject expected2 = ResourceSubject.Builder.instance().packageName("sub.dep").resourceName("DepXMLDocument").resourceType("xml").build();
         ResourceSubject expected3 = ResourceSubject.Builder.instance().packageName("sub.dep").resourceName("noextension").resourceType("").build();
         
         assertTrue(results.contains(expected1));
         assertTrue(results.contains(expected2));
         assertTrue(results.contains(expected3));
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testWhenFilePassedInstedOfDirectory() throws IOException{
         Path path = Paths.get("./src/test/resources/TestResource.txt");
         ResourceScanner.INSTANCE.findResources(path);
     }
     
     @Test
     public void testThatSeparatorsTurnedToDots(){
         String test1 = "test/something";
         String test2 = "test\\something";
         
         String expected = "test.something";
         assertEquals(expected, ResourceScanner.turnSeparatorsToDots(test1));
         assertEquals(expected,ResourceScanner.turnSeparatorsToDots(test2));
     }
 }
