 package edu.mines.csci598.recycler.frontend.items;
 
 import edu.mines.csci598.recycler.frontend.GameConstants;
 import edu.mines.csci598.recycler.frontend.Hand;
 import edu.mines.csci598.recycler.frontend.RecycleBin;
 import edu.mines.csci598.recycler.frontend.RecycleBins;
 import edu.mines.csci598.recycler.frontend.graphics.*;
 import edu.mines.csci598.recycler.frontend.motion.Movable;
 import org.apache.log4j.Logger;
 
 import java.util.Random;
 
 /**
  * Recyclables are things like bottles, plastic etc. that you would be swiping at.
  * They need to keep track of what kind of recyclable they are, and what state they are in.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:37 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Recyclable implements Displayable, Movable {
 
     private static final Logger logger = Logger.getLogger(Recyclable.class);
 
     private Sprite sprite;
     private RecyclableType type;
     private MotionState currentMotion;
     private Path path;
     private boolean removable;
    private static RecycleBins recycleBins;
 
     public Recyclable(RecyclableType type, Path path, String imagePath) {
         this.type = type;
         this.path = path;
         currentMotion = MotionState.CHUTE;
         removable = true;
         sprite = new Sprite(imagePath, (int) path.getInitialPosition().getX(), (int) path.getInitialPosition().getY());
     }
 
     @Override
     public void reactToCollision(Hand hand, double currentTimeSec) {
         if (!(this instanceof Recyclable)) {
             throw new IllegalStateException("Trying to react to Recyclable collision with a non-Recyclable!");
         }

         //Play sound when gets hit
         type.getHitSound().playSound();
 
         Coordinate position = this.getPosition();
         Line collideLine = null;
         //Make item fall based on how fast we swiped it
         double travelTime = Math.abs(GameConstants.ITEM_PATH_END / hand.getVelocityX());
         travelTime = Math.max(travelTime, GameConstants.MIN_ITEM_TRAVEL_TIME);  //Don't let it go too fast
         travelTime = Math.min(travelTime, GameConstants.MAX_ITEM_TRAVEL_TIME);  //or too slow
         int randomRotation = new Random().nextInt(4)+1;
 
         if (hand.getVelocityX() >= GameConstants.MIN_HAND_VELOCITY) { //Pushed right
             this.setMotionState(MotionState.FALL_RIGHT);
             RecycleBin destBin = recycleBins.findBinForFallingRecyclable(this);
             collideLine = new Line( position.getX(), position.getY(),
                     position.getX() + GameConstants.ITEM_PATH_END,  destBin.getMidPoint(),
                     travelTime, Math.PI * randomRotation);
 
         } else if (hand.getVelocityX() <= -1 * GameConstants.MIN_HAND_VELOCITY) { //Pushed left
             this.setMotionState(MotionState.FALL_LEFT);
             RecycleBin destBin = recycleBins.findBinForFallingRecyclable(this);
             collideLine = new Line( position.getX(), position.getY(),
                     position.getX() - GameConstants.ITEM_PATH_END, destBin.getMidPoint(),
                     travelTime, Math.PI * randomRotation);
         } else {
             throw new IllegalStateException("It really shouldn't be possible to get here!  The hand wasn't moving fast enough to make the conveyor release control!");
         }
 
         Path path = new Path(currentTimeSec);
         path.addLine(collideLine);
         this.setPath(path);
     }
 
     /**
      * checks for a collision with the given point.
      * Does *not* check if the item is touchable.
      *
      * @param point
      * @return - true if there is a collision, false otherwise
      */
     public boolean collidesWithPoint(Coordinate point) {
         return sprite.isPointInside((int) point.getX(), (int) point.getY());
     }
 
     /**
      * Determines if the recyclable should be touched by a hand
      *
      * @return true if recyclable can be touched, false otherwise
      */
     public boolean isTouchable() {
         return currentMotion.isTouchable();
     }
 
     /**
      * Determines if Recyclable can be removed from screen when its path is done
      *
      * @return true if it's okay to remove, false otherwise
      */
     @Override
     public boolean isRemovable() {
         return removable;
     }
 
     @Override
     public void setRemovable(boolean state) {
         removable = state;
     }
 
     @Override
     public Sprite getSprite() {
         return sprite;
     }
 
     public RecyclableType getType() {
         return type;
     }
 
     @Override
     public MotionState getMotionState() {
         return currentMotion;
     }
 
     @Override
     public void setMotionState(MotionState motion) {
         this.currentMotion = motion;
     }
 
     @Override
     public Path getPath() {
     	return path;
     }
     
     public void setPath(Path path) {
     	this.path = path;
     }
 
     public Coordinate getPosition() {
         return sprite.getPosition();
     }
 
     public void setPosition(Coordinate location) {
         sprite.setPosition(location);
     }
 
     @Override
     public String toString() {
         return type + ", moving along path " + path + ", and in current motion state " + currentMotion;
     }
 
     public void tellRecyclablesAboutBins(RecycleBins bins) {
         recycleBins = bins;
     }
 }
