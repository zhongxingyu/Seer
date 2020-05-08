 package vooga.scroller.collision_manager;
 
 import vooga.scroller.level_editor.Level;
 import vooga.scroller.level_management.LevelPortal;
 import vooga.scroller.sprites.test_sprites.MarioLib;
 import vooga.scroller.sprites.test_sprites.MarioLib.Coin;
 import vooga.scroller.sprites.test_sprites.MarioLib.Plant;
 import vooga.scroller.sprites.test_sprites.mario.Mario;
 import vooga.scroller.util.Direction;
 
 public class VisitMethods {
 
     /**
      * Begin the long slew of visit() methods... 
      * Thinking about ways to refactor/repackage this.
      * 
      */
     
     
     
     private CollisionDirection direction = new CollisionDirection();
     private MarioCollisions collisions = new MarioCollisions();
     private Level myLevel;
     
     public VisitMethods (Level level) {
         myLevel = level;
     }
     
     public void visit (Mario mario, Mario mario2) {
         //System.out.println("Mario has just collided with Mario!");
         
     }
     
     public void visit (Coin coin, LevelPortal levelPortal) {
         System.out.println("Collision working");
     }
      
     public void visit (Mario mario, Plant plant) {
         //System.out.println("Mario hits plant");
         mario.takeHit(mario.getHealth());  //kill Mario
         //System.out.println(mario.getHealth());
     }
     
     public void visit (Mario mario, MarioLib.Coin coin) {
         mario.scorePoints(coin.getValue());
         //System.out.println("Mario just collected a coin");
         
     }
 
     public void visit (Mario mario, MarioLib.Koopa koopa) {
         if (direction.collisionDirection(mario, koopa).equals(Direction.TOP)) {
             koopa.takeHit();
         }
         else {
             mario.takeHit(koopa.getHit());
         }        
     }
 
 
 
     public void visit (Mario mario, MarioLib.Platform platform) {
         collisions.marioAndNonStaticEntityCollision(mario, platform);
        System.out.println("********Mario has just collided with Platform!");
         
     }
 
     public void visit (Mario mario, MarioLib.LevelTwoBlockOne platform) {
         collisions.marioAndNonStaticEntityCollision(mario, platform);
         System.out.println("Mario has just collided with Platform!");
         
     }
     
     public void visit (Mario mario, MarioLib.LevelTwoBlockTwo platform) {
         collisions.marioAndNonStaticEntityCollision(mario, platform);
         System.out.println("Mario has just collided with Platform!");
         
     }
     
     public void visit (Mario mario, MarioLib.LevelTwoBlockThree platform) {
         collisions.marioAndNonStaticEntityCollision(mario, platform);
         System.out.println("Mario has just collided with Platform!");
         
     }
     
     public void visit (Mario mario, MarioLib.MovingPlatformOne movingPlatform) {
         collisions.marioAndNonStaticEntityCollision(mario, movingPlatform);
         //System.out.println("Mario has just collided with Platform!");
         
     }
     
     public void visit (Mario mario, MarioLib.Turtle turtle) {
         //endGame();
 
         if (direction.collisionDirection(mario, turtle).equals(Direction.TOP)) {
             turtle.takeHit();
         }
         else {
             mario.takeHit(turtle.getHit());
         }        
     }
     
     public void visit (Mario mario, LevelPortal sp) {
         System.out.println("Hit portal");
         sp.goToNextStartPoint(mario);
     }
     
     public void visit (MarioLib.Coin coin, Mario mario) {
         myLevel.removeSprite(coin);
     }
 
     public void visit (MarioLib.Coin coin, MarioLib.Coin coin2) {
         //System.out.println("Coin has just collided with Coin!");
         
     }
 
     public void visit (MarioLib.Coin coin, MarioLib.Koopa koopa) {
         //System.out.println("Coin has just collided with Koopa!");
         
     }
 
     public void visit (MarioLib.Coin coin, MarioLib.Platform platform) {
         //System.out.println("Coin has just collided with Platform!");
         
     }
 
     public void visit (MarioLib.Coin coin, MarioLib.Turtle turtle) {
         //System.out.println("Coin has just collided with Turtle!");
     }
 
     public void visit (MarioLib.Koopa koopa, Mario mario) {
         //System.out.println("Koopa has just collided with Mario!");
     }
 
     public void visit (MarioLib.Koopa koopa, MarioLib.Coin coin) {
         //System.out.println("Koopa has just collided with Coin!");
         
     }
 
     public void visit (MarioLib.Koopa koopa, MarioLib.Koopa koopa2) {
         //System.out.println("Koopa has just collided with Koopa!");
         
     }
 
     public void visit (MarioLib.Koopa koopa, MarioLib.Platform platform) {
         //System.out.println("Koopa has just collided with Platform!");
         
     }
 
     public void visit (MarioLib.Koopa koopa, MarioLib.Turtle turtle) {
         //System.out.println("Koopa has just collided with Turtle!");
         
     }
     
     public void visit (MarioLib.Platform platform, Mario mario) {
         //System.out.println("Platform has just collided with Mario!");
         
     }
 
     public void visit (MarioLib.Platform platform, MarioLib.Coin coin) {
         //System.out.println("Platform has just collided with Coin!");
         
     }
 
     public void visit (MarioLib.Platform platform, MarioLib.Koopa koopa) {
         //System.out.println("Platform has just collided with Koopa!");
         
     }
 
     public void visit (MarioLib.Platform platform, MarioLib.Platform platform2) {
         //System.out.println("Platform has just collided with Platform!");
         
     }
 
     public void visit (MarioLib.Platform platform, MarioLib.Turtle turtle) {
         //System.out.println("Platform has just collided with Turtle!");
         
     }
     
     public void visit (MarioLib.Turtle turtle, Mario mario) {
         //System.out.println("Turtle has just collided with Mario!");
         
     }
 
     public void visit (MarioLib.Turtle turtle, MarioLib.Coin coin) {
         //System.out.println("Turtle has just collided with Coin!");
         
     }
 
     public void visit (MarioLib.Turtle turtle, MarioLib.Koopa koopa) {
         //System.out.println("Turtle has just collided with Koopa!");
         
     }
 
     public void visit (MarioLib.Turtle turtle, MarioLib.Platform platform) {
         //System.out.println("Turtle has just collided with Platform!");
         
     }
 
     public void visit (MarioLib.Turtle turtle, MarioLib.Turtle turtle2) {
         //System.out.println("Turtle has just collided with Turtle!");
         
     }
      
 }
