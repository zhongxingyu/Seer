 package epsilon.map.entity;
 
 import epsilon.game.Collision;
 import epsilon.game.Sprite;
 import java.awt.Graphics;
 
 /**
  *
  * @author vz
  */
 public class Floor_1 extends World {
 
     public Floor_1(int posX, int posY) {
         super(posX, posY);
         currentSprite = new Sprite(new String[]{"/pics/crate.png"});
     }
 
     /**
      * Very basic collision detection test
      *
      * @param toCheckAgainst
      * @return
      */
     @Override
     public Collision collision(Entity toCheckAgainst) {
         Collision c = new Collision();
 
         double x = 0;
         double y = 0;
 
         if (toCheckAgainst instanceof MoveableEntity) {
             x = ((MoveableEntity)toCheckAgainst).getNewXPosition();
             y = ((MoveableEntity)toCheckAgainst).getNewYPosition();
         } else {
             x = toCheckAgainst.getXPosition();
             y = toCheckAgainst.getYPosition();
         }
 
         double left1, left2;
         double right1, right2;
         double top1, top2;
         double bottom1, bottom2;
 
         // variables ending with 1 is for this object,
         // and variables ending with 2 is for the object toCheckAgainst
         // all offsets should come from the incomming object, and not
         // some magical numbers I made up.
 
         left1 = this.posX;
         left2 = x;
         right1 = this.posX + this.getWidth();
         right2 = x + toCheckAgainst.getWidth();
         top1 = this.posY;
         top2 = y;
         bottom1 = this.posY + this.getHeight();
         bottom2 = y + toCheckAgainst.getHeight();
 
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
 
         Collision temp;
 
         c.collidedWith = this;
         c.collidingEntity = toCheckAgainst;
 
         for(HitBox h:this.getHitbox()) {
             for(HitBox k:toCheckAgainst.getHitbox()) {
                 temp = h.collidesWith(k, posX, posY, x, y);
                 if (temp.collided) {
                     c.collided = true;
                    
                     c.crossedBottom = (c.crossedBottom || temp.crossedBottom);
                     c.crossedTop = (c.crossedTop || temp.crossedTop);
                     c.crossedLeft = (c.crossedLeft || temp.crossedLeft);
                     c.crossedRight = (c.crossedRight || temp.crossedRight);
 
                     c.deltaBottom = Math.max(temp.deltaBottom, c.deltaBottom);
                     c.deltaTop = Math.max(temp.deltaTop, c.deltaTop);
                     c.deltaLeft = Math.max(temp.deltaLeft, c.deltaLeft);
                     c.deltaRight = Math.max(temp.deltaRight, c.deltaRight);
                 }
                 
             }
         }
 
         return c;
     }
 
     @Override
     public void renderHitBox(Graphics g, double x, double y) {
         double posX = this.posX - x;
         double posY = this.posY - y;
 
         g.drawRect((int)posX, (int)posY, this.getWidth(), this.getHeight());
 
         HitBox[] hitbox = currentSprite.getHitBox();
 
         for (int i=0;i<hitbox.length;i++) {
             hitbox[i].draw(g, posX, posY);
         }
     }
 }
