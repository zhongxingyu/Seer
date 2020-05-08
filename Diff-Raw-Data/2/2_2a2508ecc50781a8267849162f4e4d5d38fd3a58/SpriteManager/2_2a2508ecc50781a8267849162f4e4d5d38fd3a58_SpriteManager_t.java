 package vooga.scroller.level_management;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 import vooga.scroller.collision_manager.CollisionManager;
 import vooga.scroller.level_editor.Level;
 import vooga.scroller.sprites.superclasses.NonStaticEntity;
 import vooga.scroller.sprites.superclasses.Player;
 import vooga.scroller.util.Direction;
 import vooga.scroller.util.Sprite;
 import vooga.scroller.view.GameView;
 
 public class SpriteManager {
 
     private List<Sprite> mySprites;
     private List<Sprite> myFrameOfActionSprites;
     private List<Sprite> myFrameOfReferenceSprites;
     private Player myPlayer;
     private Dimension frameOfReferenceSize;
     //private Dimension frameOfActionSize;
     private Level myLevel;
 
 
     public SpriteManager(Level level){
         myLevel = level;
         //frameOfActionSize = calcActionFrameSize(view.getSize());
         mySprites = new ArrayList<Sprite>();
         initFrames();
     }
 
     private void initFrames () {
         myFrameOfActionSprites = new ArrayList<Sprite>();
         myFrameOfReferenceSprites = new ArrayList<Sprite>();
     }
 
     public void removeSprite (Sprite s) {
         mySprites.remove(s);
     }
 
     public void addSprite (Sprite s) {
         mySprites.add(s);
     }
 
     public void addPlayer(Player p){
         myPlayer = p;
        myPlayer.setCenter(myLevel.getStartPoint().x, myLevel.getStartPoint().y);
         for (Sprite sprite : mySprites) {
             if (sprite instanceof NonStaticEntity) {
                 addPlayerToSprite((NonStaticEntity) sprite);
             }
         }
     }
 
     public void addPlayerToSprite (NonStaticEntity sprite) {
         sprite.addPlayer(myPlayer);
     }
 
     public Player getPlayer(){
         return myPlayer;
     }
 
     public void updateSprites(double elapsedTime, Dimension bounds, GameView gameView) {
 
         if (myPlayer != null) {
             updateFrames(gameView);
             myPlayer.update(elapsedTime, bounds);
             checkPlayerOutOfBounds();
             if (myPlayer.getHealth() <= 0) {
                 myPlayer.handleDeath();
             }
             for (Sprite s : myFrameOfActionSprites) {
                 s.update(elapsedTime, bounds);
                 if (s.getHealth() <= 0) {
                     this.removeSprite(s);
                 }
             }
             if (myPlayer.getHealth() <= 0) {
                 myPlayer.handleDeath();
             }
             intersectingSprites();
         }
     }
 
     private void checkPlayerOutOfBounds () {
         double xCoord = myPlayer.getX();
         double yCoord = myPlayer.getY();
         double rightLevelBounds = myLevel.getLevelBounds().getWidth();
         double leftLevelBounds = 0;
         double upperLevelBounds = 0;
         double lowerLevelBounds = myLevel.getLevelBounds().getHeight();
         rightLevelBounds = myLevel.getScrollManager().getHardBoundary(Direction.RIGHT, rightLevelBounds);
         lowerLevelBounds = myLevel.getScrollManager().getHardBoundary(Direction.BOTTOM, lowerLevelBounds);
         leftLevelBounds = myLevel.getScrollManager().getHardBoundary(Direction.LEFT, leftLevelBounds);
         upperLevelBounds = myLevel.getScrollManager().getHardBoundary(Direction.TOP, upperLevelBounds);
 
         if (xCoord >= rightLevelBounds) {
             xCoord = rightLevelBounds - (myPlayer.getSize().getWidth() / 2);
             myPlayer.setCenter(xCoord, yCoord);
         }
         if (xCoord <= leftLevelBounds) {
             xCoord = leftLevelBounds + (myPlayer.getSize().getWidth() / 2);
             myPlayer.setCenter(xCoord, yCoord);
         }
         if (yCoord <= upperLevelBounds) {
             yCoord = upperLevelBounds + (myPlayer.getSize().getHeight() / 2);
             myPlayer.setCenter(xCoord, yCoord);
         }
         if (yCoord >= lowerLevelBounds) {
             yCoord = upperLevelBounds - (myPlayer.getSize().getHeight() / 2);
             myPlayer.setCenter(xCoord, yCoord);
         }
     }
 
     private void updateFrames (GameView gameView) {
         myFrameOfActionSprites.clear();
         myFrameOfReferenceSprites.clear();
         frameOfReferenceSize = gameView.getSize();
         //frameOfActionSize = calcActionFrameSize(view.getSize());
         if (mySprites.size() > 0) {
             for (Sprite s : mySprites) {
                 updateFrameOfActionSprites(s,frameOfReferenceSize);
             }
         }
     }
 
 
 //    private Dimension calcActionFrameSize (Dimension size) {
 //        Dimension temp = new Dimension((int) size.getWidth() + 200, (int) size.getHeight() + 200);
 //        return temp;
 //    }
 
     private void intersectingSprites () {
         Sprite obj1;
         Sprite obj2;
         CollisionManager cm = new CollisionManager(myLevel);
 
         mySprites.add(myPlayer);
         for (int i = 0; i < mySprites.size(); i++) {
             for (int j = 0; j < mySprites.size(); j++) {
                 obj1 = mySprites.get(i);
                 obj2 = mySprites.get(j);
                 if (obj1.intersects(obj2)) {
                     cm.handleCollision(obj1, obj2);
                 }
             }
         }
         mySprites.remove(mySprites.size() - 1);
     }
 
     private void updateFrameOfActionSprites (Sprite sprite, Dimension frame) {            
         boolean condition = (myPlayer != null &&
                 myLevel.getLeftBoundary(frame) <= sprite.getX()
                 && myLevel.getRightBoundary(frame) >= sprite.getX()
                 && myLevel.getLowerBoundary(frame) >= sprite.getY()
                 && myLevel.getUpperBoundary(frame) <+ sprite.getY());
         if(!myFrameOfActionSprites.contains(sprite) && condition) {
             myFrameOfActionSprites.add(sprite);
         }       
     }
 
     public void paint (Graphics2D pen) {
         if (myPlayer != null) {
             for (Sprite s : this.mySprites) {
                 s.paint(pen, myPlayer.getCenter(), myPlayer.getPaintLocation());
             }
             myPlayer.paint(pen);
         }
     }
 }
