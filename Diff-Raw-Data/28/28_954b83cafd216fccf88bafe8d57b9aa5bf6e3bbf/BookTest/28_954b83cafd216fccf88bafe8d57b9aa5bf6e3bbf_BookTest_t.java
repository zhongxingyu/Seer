 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ict306lab.MikeHan.majorassignment.ass02;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
 /**
  * Test class for Book.
  * 
  * @author mmhan
  */
 public class BookTest {
     
     public BookTest() {
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
      * Test of getNextId method, of class Book.
      */
     @Test
     public void testGetNextId() {
         System.out.println("getNextId");
         int expResult = Book.counter + 1;
         int result = Book.getNextId();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getId method, of class Book.
      */
     @Test
     public void testGetId() {
         System.out.println("getId");
         Book instance = new Book();
         int expResult = Book.counter;
         int result = instance.getId();
         assertEquals(expResult, result);
        
        Book nextBook = new Book("Bla", "Bla", "Bla");
        expResult++;
        assertEquals(expResult, nextBook.getId());
     }
 
     /**
      * Test of getImageFile method, of class Book.
      */
     @Test
     public void testGetImageFile() {
         System.out.println("getImageFile");
         Book instance = new Book();
         String expResult = "";
         String result = instance.getImageFile();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getNotesFile method, of class Book.
      */
     @Test
     public void testGetNotesFile() {
         System.out.println("getNotesFile");
         Book instance = new Book();
         String expResult = "";
         String result = instance.getNotesFile();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setImageFile method, of class Book.
      */
     @Test
     public void testSetImageFile() {
         System.out.println("setImageFile");
         String imageFile = "x://test/test.jpg";
         Book instance = new Book();
         try{
             instance.setImageFile(imageFile);
         }catch(IOException e){
             assertNull(instance.getImageFile());
         }
         
         try{
             imageFile = "p://test/test.jpg";
             instance.setImageFile(imageFile);
             assertEquals(imageFile, instance.getImageFile());
         }catch(IOException e){
             fail("Shouldn't throw exception");
         }
     }
 
     /**
      * Test of setNotesFile method, of class Book.
      */
     @Test
     public void testSetNotesFile() {
         System.out.println("setNotesFile");
         String notesFile = "x://test/test.jpg";
         Book instance = new Book();
         try{
             instance.setNotesFile(notesFile);
         }catch(IOException e){
             assertNull(instance.getNotesFile());
         }
         
         notesFile = "p://test/test.txt";
         try{
             instance.setNotesFile(notesFile);
             assertEquals(notesFile, instance.getNotesFile());
         }catch(IOException e){
             fail("Shouldn't throw exception");
         }
     }
 
     /**
      * Test of toString method, of class Book.
      */
     @Test
     public void testToString() {
         System.out.println("toString");
         Book instance = new Book();
         String expResult = "";
         String result = instance.toString();
         assertNotNull(result);
     }
 }
