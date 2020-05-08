 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Unit;
 
 import Unit.Player;
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
  */
 public class PlayerTest {
 
     Player p;
     private static final int HEALTH = 100, SPEED = 100, POINTVALUE = 100, X = 200, Y = 200;
     private static Color COLOR = Color.white;
     private static Weapon WEAPON = new BasicWeapon();
 
     /**
      * EnemyTest constructor creates new instance of the player class to be tested.
      */
     public PlayerTest() {
         p = new Player(HEALTH, SPEED, WEAPON, POINTVALUE, X, Y, COLOR);
        p.check();
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
         p = null;
     }
 
     /**
      * testSetX method tests the setX method of the player by comparing the value
      * to the getX method.
      */
     @Test
     public void testSetX() {
         System.out.println("Test set X");
         int setX = 200;
         p.setX(setX);
         assertEquals(setX, p.getX());
     }
 
     /**
      * testSetY method tests the setY method of the player by comparing the value
      * to the getY method.
      */
     @Test
     public void testSetY() {
         System.out.println("Test set Y");
         int setY = 150;
         p.setY(setY);
         assertEquals(setY, p.getY());
     }
 
     /**
      * testGetX method tests the getX method of the player by comparing the
      * result to the original X coordinate when the object was instantiated.
      */
     @Test
     public void testGetX() {
         System.out.println("Test get X");
         int expResult = X;
         assertEquals(expResult, p.getX());
     }
 
     /**
      * testGetY method tests the getY method of the player by comparing the
      * result to the original Y coordinate when the object was instantiated.
      */
     @Test
     public void testGetY() {
         System.out.println("Test get Y");
         int expResult = Y;
         assertEquals(expResult, p.getY());
     }
 
     /**
      * testSetColor method tests the setColor method of the player by comparing
      * the result to the result from the getColor method.
      */
     @Test
     public void testSetColor() {
         System.out.println("Test set Color");
         Color color = Color.ORANGE;
         p.setColor(color);
         assertEquals(color, p.getColor());
     }
 
     /**
      * testGetColor method tests the getColor method of the player by comparing
      * the result to the original color when the object was instantiated.
      */
     @Test
     public void testGetColor() {
         System.out.println("Test get Color");
         Color expResult = COLOR;
         assertEquals(COLOR, p.getColor());
     }
 
     /**
      * testSetLocation method tests the setLocation method of the player by
      * comparing the result to the result from the getLocation method.
      */
     @Test
     public void testSetLocation() {
         System.out.println("Test set location");
         Point newLoc = new Point(100, 100);
         p.setLocation(newLoc);
         assertEquals(newLoc, p.getLocation());
     }
 
     /**
      * testGetLocation method tests the getLocation method of the player by
      * comparing the result to the original color when the object was instantiated.
      */
     @Test
     public void testGetLocation() {
         System.out.println("Test set location");
         Point expResult = new Point(X, Y);
         assertEquals(expResult, p.getLocation());
     }
 
     /*
      * MORE ADDITIONAL TESTS TO COME ONCE MORE FUNCTIONALITY ADDED TO THE GAME
      */
     
 }
