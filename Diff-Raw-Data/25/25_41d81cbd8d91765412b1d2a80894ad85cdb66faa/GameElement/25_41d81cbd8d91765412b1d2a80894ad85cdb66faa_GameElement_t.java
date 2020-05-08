 /**
  * @author Kuang Han
  */
 
 package charactersprites;
 
 import collision.GameElementCollision;
 
 import com.golden.gamedev.Game;
 
 import physicsengine.NewtonianSprite;
 
 @SuppressWarnings("serial")
 public abstract class GameElement extends NewtonianSprite{
     protected Game myGame;
     
     public GameElement() {
         super();
         myGame = null;
     }
     
     public GameElement(Game game) {
         super();
         myGame = game;
     }
     
     public void setGame(Game game) {
         myGame = game;
     }
 
     public Game getGame() {
         return myGame;
     }
     
     public void beforeCollidedWith(GameElement e, int collisionSide) {
         switch (collisionSide) {
             case GameElementCollision.RIGHT_LEFT_COLLISION:{
                 beforeHitFromRightBy(e);
                break;
             }
             case GameElementCollision.LEFT_RIGHT_COLLISION:{
                 beforeHitFromLeftBy(e);
                break;
             }
             case GameElementCollision.BOTTOM_TOP_COLLISION:{
                beforeHitFromBottomBy(e);                
                break;
             }
             case GameElementCollision.TOP_BOTTOM_COLLISION:{
                 beforeHitFromTopBy(e);
                break;
             }
         }
     }
     
     public void afterCollidedWith(GameElement e, int collisionSide) {
         switch (collisionSide) {
             case GameElementCollision.RIGHT_LEFT_COLLISION:{
                 afterHitFromRightBy(e);
                break;
             }
             case GameElementCollision.LEFT_RIGHT_COLLISION:{
                 afterHitFromLeftBy(e);
                break;
             }
             case GameElementCollision.BOTTOM_TOP_COLLISION:{
                 afterHitFromBottomBy(e);
                break;
             }
             case GameElementCollision.TOP_BOTTOM_COLLISION:{
                 afterHitFromTopBy(e);
                break;
             }
         }
     }
     
     public void beforeHitFromLeftBy(GameElement e) {}
     
     public void beforeHitFromRightBy(GameElement e) {}
     
     public void beforeHitFromTopBy(GameElement e) {}
     
     public void beforeHitFromBottomBy(GameElement e) {}
     
     public void afterHitFromLeftBy(GameElement e) {}
     
     public void afterHitFromRightBy(GameElement e) {}
     
     public void afterHitFromTopBy(GameElement e) {}
     
     public void afterHitFromBottomBy(GameElement e) {}
 
 }
