 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.zoneproject.extractor.twitterreader;
 
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
 public class TwitterApiTest {
     
     public TwitterApiTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
 
     /**
      * Test of getSources method, of class TwitterApi.
      */
     @Test
     public void testGetHashTags() {
         System.out.println("getHashTags");
        String[] expResult = {"#descl","#you","#nice"};
         String[] result = TwitterApi.getHashTags("hello #descl how are #you in #nice?");
         for(String r: result) {
             System.out.println(r);
         }
         assertArrayEquals(expResult, result);
     }
 }
