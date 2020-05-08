 package cs2114.tiletraveler;
 
 import student.TestCase;
 
 /**
  * Test class for the Player object.
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards (MrZchuck)
  * @author Jacob Stenzel (sjacob95)
  * @version 2013.12.08
  */
 public class PlayerTest
     extends TestCase
 {
 
     private Player   player;
     //private Stage    stage;
     //private Location startLocation;
 
 
     /**
      * Sets up the test area using Stage 1 to test.
      */
     public void setUp()
     {
         Stage stage = new Stage1(10f);
         Location startLocation = new Location(5, 0);
         player = new Player(5f, 6f, 10f, stage);
         player = new Player(startLocation, 10f, stage);
     }
 
 
     /**
      * Tests the act() method in the Player class to ensure that it functions as
      * expected.
      */
     public void testAct()
     {
         player.act(Direction.WEST);
         assertEquals(true, player.isMoving());
         assertEquals(Direction.NORTH, player.getDirection());
         player.movingStopped();
         assertEquals(false, player.isMoving());
         player.act(Direction.EAST);
         assertEquals(Direction.NORTH, player.getDirection());
     }
 
 
     /**
      * Tests the move() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testMove()
     {
         assertEquals(player.getMoveTime(), 400, 0.01);
         player.move(null);
         player.movingStopped();
         player.move(Direction.SOUTH);
         player.move(Direction.NORTH);
         player.move(Direction.NORTH);
         player.move(Direction.WEST);
         player.move(Direction.WEST);
         player.move(Direction.SOUTH);
         player.move(Direction.WEST);
         player.move(Direction.EAST);
        assertEquals(new Location(5, 0), player.getLocation());
     }
 
     /**
      * Test the player winning
      */
     public void testWon()
     {
         for (int x = 0; x < 10; x++)
         {
             player.move(Direction.NORTH);
         }
        assertEquals(true, player.checkCurrentStatus());
        assertEquals(new Location(5, 1), player.getLocation());
     }
 
     /**
      * Test if the player is dead or not
      */
     public void testDie()
     {
         player.move(Direction.NORTH);
         player.checkEnemyCollision();
        assertEquals(true, player.checkCurrentStatus());
         player.move(Direction.WEST);
        assertEquals(true, player.checkCurrentStatus());
     }
 
     /**
      * Tests the nudge() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testNudge()
     {
         player.nudge(Direction.SOUTH);
         assertEquals(player.getLocation(), new Location(5, 0));
 
     }
 
 
     /**
      * Tests the jump() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testJump()
     {
         player.jump(Direction.SOUTH);
        assertEquals(new Location(5, -1), player.getLocation());
     }
 
 
     /**
      * Tests the walk() method in the Player class to ensure that it functions
      * as expected.
      */
     public void testWalk()
     {
         player.walk(Direction.SOUTH);
        assertEquals(new Location(5, -1), player.getLocation());
     }
 
 }
