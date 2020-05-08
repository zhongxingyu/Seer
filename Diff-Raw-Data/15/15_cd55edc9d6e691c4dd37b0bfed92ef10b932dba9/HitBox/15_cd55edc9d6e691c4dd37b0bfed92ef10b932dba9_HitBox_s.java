 package epsilon.map.entity;
 
 import epsilon.game.Collision;
 import java.awt.Graphics;
 
 /**
  *
  * @author Marius
  */
 public class HitBox {
 
     private int offsetX;
     private int offsetY;
 
     private int width;
     private int height;
 
     public HitBox(int offX, int offY, int width, int height) {
         offsetX = offX;
         offsetY = offY;
 
         this.width = width;
         this.height = height;
     }
 
     public Collision collidesWith(HitBox box, double ownPosX, double ownPosY, double otherPosX, double otherPosY) {
 
         Collision c = new Collision();
 
         double left1, left2;
         double right1, right2;
         double top1, top2;
         double bottom1, bottom2;
 
         // variables ending with 1 is for this object,
         // and variables ending with 2 is for the object toCheckAgainst
         // all offsets should come from the incomming object, and not
         // some magical numbers I made up.
 
         left1 = ownPosX+offsetX;
         left2 = otherPosX + box.getOffsetX();
         right1 = left1 + width;
         right2 = left2 + box.getWidth();
         top1 = ownPosY + offsetY;
         top2 = otherPosY + box.getOffsetY();
         bottom1 = top1 + height;
         bottom2 = top2 + box.getHeight();
 
         if (bottom1 < top2) {
             return c;
         }
         if (top1 > bottom2) {
             return c;
         }
 
         if (right1 < left2) {
             return c;
         }
         if (left1 > right2) {
             return c;
         }
 
         c.collided = true;
 
         // check if the other entity crosses the left side of this hitbox
         if (right2 > left1 && right2 < right1) {
             c.crossedLeft = true;
         }
 
         // check if the other entity crosses the right side of this hitbox
         if (left2 < right1 && left2 > left1) {
             c.crossedRight = true;
         }
 
         // check if the other entity crosses the top of this hitbox
         if (bottom2 > top1 && bottom2 < bottom1) {
             c.crossedTop = true;
         }
 
         // check if the other entity crosses the bottom of this hitbox
         if (top2 < bottom1 && top2 > top1) {
             c.crossedBottom = true;
         }
 
         c.deltaRight = right1 - left2;
         c.deltaBottom = bottom1 - top2;
         c.deltaTop = bottom2 - top1;
         c.deltaLeft = right2 - left1;
 
        return c; //not yet implemented
     }
 
     public void draw (Graphics g, double posX, double posY) {
         g.drawRect((int)posX + offsetX, (int) posY + offsetY, width, height);
     }
 
     public int getOffsetX() {
         return offsetX;
     }
 
     public int getOffsetY() {
         return offsetY;
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getHeight() {
         return height;
     }
 
 }
