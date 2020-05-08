 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gravety;
 
import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author Dan
  */
 public class BodyTest {
   private Body b;
   
   public BodyTest() {
   }
 
   @Before
   public void initialize() {
    b = new Body(10, 10, 0, 0, 100);
   }
 
   /**
    * Test of add method, of class Body.
    */
   @Test
   public void testAdd() {
    Body a = new Body(20, 20, 0, 0, 100);
    Body expected = new Body(15, 15, 0, 0, 200);
     Body result = Body.add(a, b);
     assertEquals(expected, result);
   }
 
   /**
    * Test of in method, of class Body.
    */
   @Test
   public void testIn() {
     Quad q = new Quad(1, 1, 10);
     assertTrue(b.in(q));
   }
 }
