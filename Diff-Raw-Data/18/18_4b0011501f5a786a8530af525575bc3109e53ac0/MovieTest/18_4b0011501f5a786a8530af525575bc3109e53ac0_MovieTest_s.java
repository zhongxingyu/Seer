 package edu.ucsb.cs56.S13.lab04.Daized;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 
 /**
  *The test class MovieTest, to test the Movie class.
  * @author Roeland Singer-Heinze
  * @version CS56, 13S, lab04
  * @see Movie
  */
 
 public class MovieTest{
 
 @Test
     public void test_DefaultConstructor_and_getters(){
 
     Movie m = new Movie();
    assertEquals(null, m.getTitle());
     assertEquals( 0 , m.getYearOfRelease());
 }
 
 @Test
     public void test_TwoArgConstructor_and_getters_Inception(){
 
     Movie m = new Movie("Inception", 2010);
     assertEquals("Inception", m.getTitle());
     assertEquals(2010, m.getYearOfRelease());
 }
 
 @Test
     public void test_TwoArgConstructor_and_getters_Vertigo(){
 
     Movie m = new Movie("Vertigo", 1958);
     assertEquals("Vertigo", m.getTitle());
     assertEquals(1958, m.getYearOfRelease());
 
 }
 
 @Test
     public void test_TwoArgConstructor_RagingBull(){
 
     Movie m = new Movie("Raging Bull", 1980);
     assertEquals("Raging Bull", m.getTitle());
     assertEquals(1980, m.getYearOfRelease());
 }
 
 @Test
     public void test_Setters(){
 
     Movie m = new Movie();
     m.setTitle("Inception");
     m.setYearOfRelease(2010);
     assertEquals("Inception", m.getTitle());
     assertEquals(2010, m.getYearOfRelease());
 }
 
 @Test
 public void test_toString_Inception(){
 
     Movie m = new Movie("Inception",2010);
     assertEquals("Inception (2010)", m.toString());
 }
 
 @Test
     public void test_toString_Vertigo(){
 
     Movie m = new Movie("Vertigo",1958);
     assertEquals("Vertigo (1958)", m.toString());
 }
 
 @Test
     public void test_toString_RagingBull(){
 
     Movie m = new Movie("Raging Bull",1980);
    assertEquals("Ragin Bull (1980)", m.toString());
 }
 
 @Test
     public void test_equals_equal1(){
 
     Movie m1 = new Movie("Raging Bull", 1980);
     Movie m2 = new Movie("Raging Bull", 1980);
     assertTrue(m1.equals(m2));
 }
 
 @Test
     public void test_equals_equal2(){
 
     Movie m1 = new Movie();
     Movie m2 = new Movie();
     assertTrue(m1.equals(m2));
 }
 
 @Test
     public void test_equals_notequal(){
 
     Movie m1 = new Movie();
     Movie m2 = new Movie("Blah",2000);
     assertFalse(m1.equals(m2));
 }
 
 }
