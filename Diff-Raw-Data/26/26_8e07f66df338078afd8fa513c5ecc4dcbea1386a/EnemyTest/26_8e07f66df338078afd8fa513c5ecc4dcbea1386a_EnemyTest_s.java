 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Unit;
 
 import java.awt.geom.*;
 import java.awt.Shape;
 import java.awt.Point;
 import Weapon.*;
 import java.awt.Color;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author danielcecil
  * @modified David
  */
 public class EnemyTest {
 
     Enemy e;
     private static final int HEALTH = 100, SPEED = 100, POINTVALUE = 100, X = 200, Y = 200;
     private static Shape SHAPE = new Rectangle2D.Double(10,10,5,5);
     private static Color COLOR = Color.white;
     private static Weapon WEAPON = new BasicWeapon();
 
     /**
      * EnemyTest constructor creates new instance of the enemy class to be tested.
      */
     public EnemyTest() {
        e = new Enemy(HEALTH, SPEED, WEAPON, POINTVALUE, X, Y, COLOR);
        e.check();
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
         e = null;
     }
 
     /**
      * testSetX method tests the setX method of the enemy by comparing the value
      * to the getX method.
      */
     @Test
     public void testSetX() {
         System.out.println("Test set X");
         int setX = 200;
         e.setX(setX);
         assertEquals(setX, e.getX());
     }
 
     /**
      * testSetY method tests the setY method of the enemy by comparing the value
      * to the getY method.
      */
     @Test
     public void testSetY() {
         System.out.println("Test set Y");
         int setY = 150;
         e.setY(setY);
         assertEquals(setY, e.getY());
     }
 
     /**
      * testGetX method tests the getX method of the enemy by comparing the
      * result to the original X coordinate when the object was instantiated.
      */
     @Test
     public void testGetX() {
         System.out.println("Test get X");
         int expResult = X;
         assertEquals(expResult, e.getX());
     }
 
     /**
      * testGetY method tests the getY method of the enemy by comparing the
      * result to the original Y coordinate when the object was instantiated.
      */
     @Test
     public void testGetY() {
         System.out.println("Test get Y");
         int expResult = Y;
         assertEquals(expResult, e.getY());
     }
 
     /**
      * testSetColor method tests the setColor method of the enemy by comparing
      * the result to the result from the getColor method.
      */
     @Test
     public void testSetColor() {
         System.out.println("Test set Color");
         Color color = Color.ORANGE;
         e.setColor(color);
         assertEquals(color, e.getColor());
     }
 
     /**
      * testGetColor method tests the getColor method of the enemy by comparing
      * the result to the original color when the object was instantiated.
      */
     @Test
     public void testGetColor() {
         System.out.println("Test get Color");
         Color expResult = COLOR;
         assertEquals(COLOR, e.getColor());
     }
 
     /**
      * testSetLocation method tests the setLocation method of the enemy by
      * comparing the result to the result from the getLocation method.
      */
     @Test
     public void testSetLocation() {
         System.out.println("Test set location");
         Point newLoc = new Point(100, 100);
         e.setLocation(newLoc);
         assertEquals(newLoc, e.getLocation());
     }
 
     /**
      * testGetLocation method tests the getLocation method of the enemy by
      * comparing the result to the original location when the object was instantiated.
      */
     @Test
     public void testGetLocation() {
         System.out.println("Test set location");
         Point expResult = new Point(X, Y);
         assertEquals(expResult, e.getLocation());
     }
 }
