 package Unit;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import Weapon.*;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Jere
  * @modified David
  */
 public class UnitTest {
 
     Unit instance;
 
     public UnitTest() {
         instance = new Player(100, 100, new BasicWeapon(), 100, 100, 100, Color.BLUE);
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
      * Test of setX method, of class Unit.
      */
     @Test
     public void testSetX() {
         System.out.println("setX");
         int newX = 50;
         instance.setX(newX);
         assertEquals(newX, instance.getX());
 
     }
 
     /**
      * Test of getX method, of class Unit.
      */
     @Test
     public void testGetX() {
         System.out.println("getX");
         int expResult = 100;
         int result = instance.getX();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setY method, of class Unit.
      */
     @Test
     public void testSetY() {
         System.out.println("setY");
         int newY = 50;
 
         instance.setY(newY);
         assertEquals(newY, instance.getY());
     }
 
     /**
      * Test of getY method, of class Unit.
      */
     @Test
     public void testGetY() {
         System.out.println("getY");
 
         int expResult = 100;
         int result = instance.getY();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setColor method, of class Unit.
      */
     @Test
     public void testSetColor() {
         System.out.println("setColor");
         Color newColor = Color.RED;
 
         instance.setColor(newColor);
         assertEquals(newColor, instance.getColor());
     }
 
     /**
      * Test of getColor method, of class Unit.
      */
     @Test
     public void testGetColor() {
         System.out.println("getColor");
 
         Color expResult = Color.BLUE;
         Color result = instance.getColor();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setLocation method, of class Unit.
      */
     @Test
     public void testSetLocation() {
         System.out.println("setLocation");
         Point newLoc = new Point(50, 50);
         instance.setLocation(newLoc);
         assertEquals(newLoc, instance.getLocation());
     }
 
     /**
      * Test of getLocation method, of class Unit.
      */
     @Test
     public void testGetLocation() {
         System.out.println("getLocation");
 
         Point expResult = new Point(100, 100);
         Point result = instance.getLocation();
         assertEquals(expResult, result);
     }
 
     /**
      * checks if the class can be extended
      */
     public class UnitImpl extends Unit {
 
         public UnitImpl() {
            super(0, 0, null, 0, 0, 0, null, null);
         }
 
         public void draw(Graphics g) {
         }
     }
 }
