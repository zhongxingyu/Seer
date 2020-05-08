 package vooga.scroller.marioGame.spritesDefinitions.collisions;
 
 
 import vooga.scroller.sprites.interfaces.ICollectible;
 import vooga.scroller.sprites.interfaces.IDoor;
 import vooga.scroller.sprites.interfaces.IEnemy;
 import vooga.scroller.sprites.interfaces.IPlatform;
 import vooga.scroller.sprites.interfaces.IPlayer;
 
 /**
  * This is where you want to place your visit methods. To keep this class as 
  * clean as possible, I don't actually handle the collision logic here. I handle 
  * that logic in a Game specific Collisions class. You can, of course, handle all 
  * collisions in these visit() methods if you prefer. 
  * 
  * @author Jay Wang
  *
  */
 public class VisitMethods {
 
        
     private MarioCollisions collisions = new MarioCollisions();
 
     public void visit (IPlayer player, IPlatform platform) {
         collisions.marioAndPlatformCollision(player, platform);
     }
     
     public void visit (IPlayer player, ICollectible collectible) {
         collisions.marioAndCollectibleCollision(player, collectible);
     }
         
     public void visit (IPlayer player, IEnemy enemy) {
         collisions.marioAndEnemyCollision(player, enemy);
     }
     
     public void visit (IPlayer player, IDoor levelPortal) {
         collisions.marioAndLevelPortalCollision(player, levelPortal);
     }
 }
