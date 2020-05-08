 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package utils;
 
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
 public class RDFDatabaseTest {
     
     public RDFDatabaseTest() {
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
      * Test of addItem method, of class RDFDatabase.
      */
     @Test
     public void testAddItem() {
         System.out.println("addItem");
        String uri="http://testURI";
         Item item = new Item(uri);
        item.addProp(new Prop("title","le titre",true));
         
         
         RDFDatabase.addItem(item);
         RDFDatabase.print();
         assertEquals(true,RDFDatabase.ItemURIExist(uri));
         RDFDatabase.deleteItem(uri);
         
         RDFDatabase.print();
         assertEquals(false,RDFDatabase.ItemURIExist(uri));
         RDFDatabase.addItem(item);
         assertEquals(true,RDFDatabase.ItemURIExist(uri));
         RDFDatabase.deleteItem(uri);
         assertEquals(false,RDFDatabase.ItemURIExist(uri));
         //fail("The test case is a prototype.");
     }
 
 }
