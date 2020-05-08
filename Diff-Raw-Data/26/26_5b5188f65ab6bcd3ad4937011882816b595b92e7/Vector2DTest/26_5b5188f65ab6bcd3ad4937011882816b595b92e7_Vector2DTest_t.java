 package fr.aumgn.bukkitutils.geom;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import static fr.aumgn.bukkitutils.geom.VectorUtils.*;
 
 public class Vector2DTest {
 
     @Test
     public void testBlockComponents() {
         Vector2D vector = new Vector2D(1.2, 400.6);
 
         assertEquals(1, vector.getBlockX());
         assertEquals(401, vector.getBlockZ());
     }
 
     @Test
     public void testEqualsBlock() {
         Vector2D vector1 = new Vector2D(1.2, 400.6);
         Vector2D vector2 = new Vector2D(1.4, 400.8);
         Vector2D vector3 = new Vector2D(1.1, 400.49);
 
         assertFalse(vector1.equals(vector2));
         assertTrue(vector1.equalsBlock(vector2));
         assertFalse(vector1.equalsBlock(vector3));
     }
 
     @Test
     public void testGetMiddle() {
         Vector2D vector1 = new Vector2D(4, 400);
         Vector2D vector2 = new Vector2D(7, 500);
 
         Vector2D middle = vector1.getMiddle(vector2);
 
         assertEquals(new Vector2D(5.5, 450), middle);
     }
 
     @Test
     public void testInside() {
         Vector2D min = new Vector2D(4, 400);
         Vector2D max = new Vector2D(7, 500);
 
         Vector2D vector1 = new Vector2D(5, 500);
         Vector2D vector2 = new Vector2D(5, 501);
 
         assertTrue(vector1.isInside(min, max));
         assertFalse(vector2.isInside(min, max));
     }
 
     @Test
     public void testPositive() {
         Vector2D vector = new Vector2D(-5, 500);
 
         assertEquals(new Vector2D(5, 500), vector.positive());
     }
 
     @Test
     public void testDistanceSq() {
         Vector2D vector1 = new Vector2D(4, 400);
         Vector2D vector2 = new Vector2D(7, 500);
 
         assertEquals(
                 vector1.distanceSq(vector2),
                 vector1.subtract(vector2).lengthSq(),
                 0);
     }
 
     @Test
     public void testNormalize() {
         Vector2D vec1 = new Vector2D(1, 1).normalize();
 
        assertVector2DEquals(new Vector2D(0.707, 0.707), vec1, 0.001);
     }
 }
