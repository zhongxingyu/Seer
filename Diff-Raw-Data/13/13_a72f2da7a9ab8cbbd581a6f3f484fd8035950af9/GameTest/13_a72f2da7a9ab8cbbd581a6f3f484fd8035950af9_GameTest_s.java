 package edu.ucsb.cs56.S13.lab04.richard_nguyen;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 /** test class for Game
 
 @author Richard Nguyen
 @version CS56, S13, lab04
 @see Game
 
 */
 
 public class GameTest{
     /**test no arg constructor from Game
        @see Game#Game()
     */
     @Test
     public void test_DefaultConstructor_and_getters(){
 	Game game = new Game();
 	assertEquals("noname",game.getName());
 	assertEquals(0,game.getYear());
     }
     /**
        test 2 args constructor from GameTest
        @see Game#Game()
     */
     @Test
     public void test_TwoArgConstrutor_and_getters(){
 	Game game = new Game("Battlefield", 1974);
 	assertEquals("Battlefield", game.getName());
 	assertEquals(1974, game.getYear());
     }
     /**
        test getName from Game
        @see Game#getName()
     */
     @Test 
     public void testGetName(){
 	Game game = new Game("Battlefield", 2012);
         assertEquals("Battlefield", game.getName());
     }
     /**
        test getYear from Game
        @see Game#getYear()
     */
     @Test
     public void testGetYear(){
         Game game = new Game("COD MW 24", 4113);
         assertEquals(4113, game.getYear());
     }
     /**
        test setName from Game
       @see Game#setName()
     */
 
     @Test
     public void testSetName(){
 	Game game = new Game();
 	game.setName("COD");
 	assertEquals("COD",game.getName());
     }
     /**
        test setYear from Game
       @see Game#setYear()
     */
 
     @Test
     public void testYear(){
         Game game = new Game();
         game.setYear(452);
         assertEquals(452,game.getYear());
     }
     /**
        test equal from Game
       @see Game#equal()
     */
 
     @Test
     public void testEquals(){
 	Game game = new Game("CS56",5656);
 	Game game1= new Game("CS56",5656);
 	assertEquals(true, game.equals(game1));
     }
     @Test
     public void test_toString(){
 	Game game = new Game("Skyfall" ,2013);
 	assertEquals("[Skyfall: 2013]",game.toString());
 	    
     }
 }
