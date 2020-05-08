 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.helsinki.cs.okkopa;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author phemmila
  */
 public class SettingsTest {
     
     private Settings testSettings;
     
     public SettingsTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() throws FileNotFoundException, IOException {     
         //before each test
         testSettings = new Settings("test.xml");
     }
     
     @After
     public void tearDown() {
         //after all the tests
     }
 
     @Test
     public void testSettingNotNull() {
         assertNotNull("Loading \"test.xml\" caused an exception. Check for correct path or whether the file exists.",testSettings.getSettings());
     }
   
     
     @Test 
     public void testSettingContainsValues() {
       assertTrue("test.xml has no content, should contain 11 key value pairs.",testSettings.getSettings().size() == 12);
     }
 
 
 }
