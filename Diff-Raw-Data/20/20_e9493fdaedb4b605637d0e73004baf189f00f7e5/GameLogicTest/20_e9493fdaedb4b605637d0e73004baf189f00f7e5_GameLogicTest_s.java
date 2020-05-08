 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
import static org.junit.Assert.*;
 
 /**
  *
  * @author Miroslav Zoricak
  */
 public class GameLogicTest {
     
     public GameLogicTest() {
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
      * Test of initializeGame method, of class GameLogic.
      */
     @Test
     public void testInitializeGame() {
         System.out.println("initializeGame");
         
         GameLogic instance = new GameLogic();
         
         
     }
 
     /**
      * Test of gameFinished method, of class GameLogic.
      */
     @Test
     public void testGameFinished() {
         
     }
 
     /**
      * Test of decideNextMove method, of class GameLogic.
      */
     @Test
     public void testDecideNextMove() {
         System.out.println("decideNextMove");
         GameLogic instance = new GameLogic();
         
     }
 }
