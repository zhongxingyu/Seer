 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package practiceUno;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import java.util.ArrayList; 
 import static org.junit.Assert.*;
 
 /**
  *
  * @author mike
  */
 public class SpecialCardTest {
     
     ArrayList<Player> players = new ArrayList<Player>(); 
     
     public SpecialCardTest() {
     }
     
     @BeforeClass
     public static void setUpClass()
     {
        
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void  setUp() 
     {
         
     }
     
     @After
     public void tearDown() 
     {
        
     }
 
     /**
      * Test of Print method, of class SpecialCard.
      */
     @Test
     public void testPrint() {
         System.out.println("Print");
         for(SpecialCard.cardValues v : SpecialCard.cardValues.values())
         {
             for(Card.cardColor c : Card.cardColor.values())
             {
                 SpecialCard instance = new SpecialCard(v, c);
                 instance.Print();
                 System.out.println("");
             }
         }
         
     }
 
     /**
      * Test of GetColor method, of class SpecialCard.
      */
     @Test
     public void testGetColor() {
         System.out.println("GetColor");
         for(SpecialCard.cardColor c : SpecialCard.cardColor.values())
         {
             for(SpecialCard.cardValues v: SpecialCard.cardValues.values())
             {
                 SpecialCard instance = new SpecialCard(v, c);
                 Card.cardColor expResult = c;
                 Card.cardColor result = instance.GetColor();
                 assertEquals(expResult, result);
             }
         }
         
     }
 
     /**
      * Test of GetSpecial method, of class SpecialCard.
      */
     @Test
     public void testGetSpecial() {
         System.out.println("GetSpecial");
         for(SpecialCard.cardValues v : SpecialCard.cardValues.values())
         {
             for(SpecialCard.cardColor c : SpecialCard.cardColor.values())
             {
                 SpecialCard instance = new SpecialCard(v, c);
                 SpecialCard.cardValues expResult = v;
                 SpecialCard.cardValues result = instance.GetSpecial();
                 assertEquals(expResult, result);
             }
         }
     }
 
     /**
      * Test of Skip method, of class SpecialCard.
      */
     @Test
     public void testSkip() 
     {
         System.out.println("Skip");
         int currentPlayerIndex = players.size()-1;
         SpecialCard instance = new SpecialCard(SpecialCard.cardValues.SKIP, Card.cardColor.RED);
         int expResult = 1;
         int result = instance.Skip(currentPlayerIndex, players);
         assertEquals(expResult, result);
         
     }
 
     /**
      * Test of Reverse method, of class SpecialCard.
      */
     @Test
     public void testReverse() 
     {
         System.out.println("Reverse");
           
         int currentPlayerIndex = 0;
         SpecialCard instance = new SpecialCard(SpecialCard.cardValues.REVERSE, Card.cardColor.YELLOW);
         
         int expResult = players.size() -1;
         int result = instance.Reverse(currentPlayerIndex, players);
         tearDown(); 
         assertEquals(expResult, result);
        
         
     }
 
     /**
      * Test of DrawTwo method, of class SpecialCard.
      */
     @Test
     public void testDrawTwo() 
     {
 
         System.out.println("DrawTwo");
          
         Deck theDeck = new Deck();
        Player affectedPlayer = new Player(); 
         
         for(SpecialCard.cardColor c : SpecialCard.cardColor.values())
         {
             SpecialCard instance = new SpecialCard(SpecialCard.cardValues.DRTWO, c);
            instance.DrawTwo(affectedPlayer, theDeck);
         }
         
        
         
     }
 }
