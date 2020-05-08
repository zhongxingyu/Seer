 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gravety;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author Dan
  */
 public class QuadTest {
   private Quad instance;
   
   public QuadTest() {
   }
   
   @Before
   public void initialize() {
     instance = new Quad(1, 1, 10);
   }
   
   /**
    * Test of contains method, of class Quad.
    */
   @Test
   public void testContains() {
     assertTrue(instance.contains(5, 5));
   }
 
   /**
    * Test of NW method, of class Quad.
    */
   @Test
   public void testNW() {
     Quad NW = instance.NW();
    Quad expected = new Quad(1, 1, 5);
     assertTrue(expected.getLength() == NW.getLength());
     assertTrue(expected.getX() == NW.getX());
     assertTrue(expected.getY() == NW.getY());
 }
 
   /**
    * Test of NE method, of class Quad.
    */
   @Test
   public void testNE() {
     Quad NE = instance.NE();
    Quad expected = new Quad(6, 1, 5);
     assertTrue(expected.getLength() == NE.getLength());
     assertTrue(expected.getX() == NE.getX());
     assertTrue(expected.getY() == NE.getY());
   }
 
   /**
    * Test of SW method, of class Quad.
    */
   @Test
   public void testSW() {
     Quad SW = instance.SW();
    Quad expected = new Quad(1, 6, 5);
     assertTrue(expected.getLength() == SW.getLength());
     assertTrue(expected.getX() == SW.getX());
     assertTrue(expected.getY() == SW.getY());
   }
 
   /**
    * Test of SE method, of class Quad.
    */
   @Test
   public void testSE() {
     Quad SE = instance.SE();
    Quad expected = new Quad(6, 6, 5);
     assertTrue(expected.getLength() == SE.getLength());
     assertTrue(expected.getX() == SE.getX());
     assertTrue(expected.getY() == SE.getY());
   }
 }
