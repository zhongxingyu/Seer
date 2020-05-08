 package objects;
 
 import java.awt.image.BufferedImage;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brad
  * Date: 4/4/13
  * Time: 6:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class Entity extends Thing {
 
     public Entity(BufferedImage img) {
         super(img);
     }
 
     public abstract void move(int direction);
 
     public void kill() {
         destroyed = true;
     }
 }
