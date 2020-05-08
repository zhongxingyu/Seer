 package bowgame;
 
 import javax.microedition.lcdui.Image;
 import javax.microedition.lcdui.game.Sprite;
 import util.Vector2d;
 
 /**
  *
  * @author lucacp
  */
 public class Arrow extends Sprite {
     //private static final int ARROW_INITIAL_POS_X = BowCanvas.AIM_X;
     //private static final int ARROW_INITIAL_POS_Y = BowCanvas.AIM_Y;
 
     private Vector2d pos;
     private Vector2d prev_pos;
     private Vector2d vel;
 
     private Vector2d accel;
     
     public Arrow(Image img, int posX, int posY) {
         super(img, 128, 128);
         defineReferencePixel(63, 63);
         pos = new Vector2d(posX, posY);
         setRefPixelPosition((int)pos.getX(),(int)pos.getY());
         prev_pos = new Vector2d(pos);
         vel = new Vector2d(Vector2d.Zero);
         accel = new Vector2d(Vector2d.Zero);
     }
 
     public Vector2d getAccel() {
         return accel;
     }
 
     public void setAccel(Vector2d accel) {
         this.accel = accel;
     }
 
     public Vector2d getVel() {
         return vel;
     }
 
     public void setVel(Vector2d vel) {
         this.vel = vel;
     }
     
     public void update() {
         vel = vel.add(accel);
         prev_pos = pos;
         pos = pos.add(vel);
        setFrame(findCorrectFrame(new Vector2d(vel)));
         setRefPixelPosition((int)pos.getX(), (int)pos.getY());
     }
 
     public void setPos(int x, int y) {
         pos.setX(x);
         pos.setY(y);
         prev_pos.setX(x);
         prev_pos.setY(y);
         setRefPixelPosition((int)pos.getX(), (int)pos.getY());
     }
 
     public Vector2d getPos() {
         return pos;
     }
     
     public void setPrev_pos(Vector2d prev_pos) {
         this.prev_pos = prev_pos;
     }
 
     private int findCorrectFrame(Vector2d vel) {
         vel.normalize();
         if(-vel.getY() > 0) {
             if(vel.getX() > Math.cos(Math.toRadians(22.5)))
                 return 0;
             else if(vel.getX() > Math.cos(Math.toRadians(67.5)))
                 return 7;
             else if(vel.getX() > Math.cos(Math.toRadians(112.5)))
                 return 6;
             else if(vel.getX() > Math.cos(Math.toRadians(157.5)))
                 return 5;
             else
                 return 4;
         } else {
             if(vel.getX() > Math.cos(Math.toRadians(22.5)))
                 return 0;
             else if(vel.getX() > Math.cos(Math.toRadians(67.5)))
                 return 1;
             else if(vel.getX() > Math.cos(Math.toRadians(112.5)))
                 return 2;
             else if(vel.getX() > Math.cos(Math.toRadians(157.5)))
                 return 3;
             else
                 return 4;
         }
     }
 }
