 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package extractor;
 
 import java.io.File;
 import java.util.ArrayList;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class WikiMetaRequestTest {
     
     public WikiMetaRequestTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of getProperties method, of class WikiMetaRequest.
      */
     @Test
     public void testGetProperties_String() {
         System.out.println("getProperties");
         String texte = "Bienvenue à Antibes";
         ArrayList result = WikiMetaRequest.getProperties(texte);
         System.out.println(result);
         assertEquals(1,result.size());
     }
 
     /**
      * Test of getProperties method, of class WikiMetaRequest.
      */
     @Test
     public void testGetProperties_File() {
         System.out.println("getProperties");
         File file = new File("resources/examples/WikiMetaOutput_pip.json");
         ArrayList result = WikiMetaRequest.getProperties(file);
         assertEquals(result.size(), 0);
     }
     
     /**
      * Test of getProperties method, of class WikiMetaRequest.
      */
     @Test
     public void testGetProperties_File_2() {
         System.out.println("getProperties");
         File file = new File("resources/examples/WikiMetaOutput_mars.json");
         ArrayList result = WikiMetaRequest.getProperties(file);
         System.out.println(result);
        assertEquals(2,result.size());
     }
     
     @Test
     public void testGetProperties_String_cleaningResult() {
         System.out.println("getProperties");
         String texte = "Arnaud Montebourg est Arnaud Montebourg";
         ArrayList result = WikiMetaRequest.getProperties(texte);
         System.out.println(result);
         assertEquals(1,result.size());
     }
 }
