 package edu.mines.csci598.recycler.frontend.graphics;
 
 import edu.mines.csci598.recycler.frontend.GameConstants;
 import org.apache.log4j.Logger;
 
import java.awt.*;

 /**
  * This class keeps track of the sprites location on disk, transforms, position velocity etc.
  * <p/>
  * <p/>
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:37 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Sprite {
     private static final Logger logger = Logger.getLogger(Sprite.class);
 
     Coordinate location;
     private String fileName;
 
     public Sprite(String fileName, int x, int y) {
         this.location = new Coordinate(x,y);
         this.fileName = fileName;
         setImage(fileName);
     }
 
     /*
       * Sets the file path for this sprite's icon image.
       *
       * param {String} fileName
       * 		The name of the file to be loaded.
       */
     public void setImage(String fileName) {
         this.fileName = fileName;
     }
 
 
 
     public void setY(int y) {
         location.setY(y);
     }
 
     public void setX(int x) {
         location.setX(x);
     }
 
     /*
       * Gets the x position.
       * Protected because only graphics should care about where it is scaled
       * return {int}
       */
     protected int getScaledX() {
         return (int) Math.round(location.getX() * GraphicsConstants.SCALE_FACTOR);
     }
 
     public int getX() {
         return (int) Math.round(location.getX());
     }
 
     /*
       * Gets the y position.
       *
       * return {int}
       */
     protected int getScaledY() {
         return (int) Math.round(location.getY() * GraphicsConstants.SCALE_FACTOR);
     }
 
     public int getY() {
         return (int) Math.round(location.getY());
     }
 
     /*
     * Gets the icon image.
     *
     * return {Image}
     */
     public Image getImage() {
         return ResourceManager.getInstance().getImage(fileName);
     }
 
     /**
      * Checks to see if a given point is within the bounds of the sprite
      *
      * @param x
      * @param y
      * @return
      */
 
 
     public boolean isPointInside(int x, int y) {

        if (x >= getX() - (GameConstants.SPRITE_X_OFFSET) &&
                x <= getX() + GameConstants.SPRITE_X_OFFSET) {
            if (y >= getY() - (GameConstants.SPRITE_Y_OFFSET) &&
                    y <= getY() + (GameConstants.SPRITE_Y_OFFSET)) {
                 return true;
             }
         }
         return false;
     }
     
     public Coordinate getPosition(){
     	return location;
     }
 
 	public synchronized void setPosition(Coordinate location) {
 		this.location=location;
 	}
 
 
 
 }
