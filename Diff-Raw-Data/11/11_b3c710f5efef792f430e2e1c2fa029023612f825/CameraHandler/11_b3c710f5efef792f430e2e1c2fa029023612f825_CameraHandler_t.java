 package memeograph.renderer.processing;
 
 import java.awt.event.MouseWheelEvent;
 import processing.core.PApplet;
 import static processing.core.PApplet.*;
 import processing.core.PVector;
 
 
 
 public class CameraHandler {
     private static final PVector DO_NOTHING = new PVector(0, 0, 0);
     private static final PVector camNorth = new PVector(0, 1, 0);
     private static final int MOVE_TICK = 150; //How much move to move in TIME_TICK millis
     private static final int TIME_TICK = 350; //How time per move
 
     public enum DIRECTION {POSITIVE, NEGATIVE};
 
     private PApplet processing;
     private PVector pos;
     private PVector dir;
 
     private Mover xmove = null;
     private Mover ymove = null;
 
     public CameraHandler(PApplet papplet){
         processing = papplet;
     }
 
     public void setup(){
         pos = new PVector(processing.width/2.0f, processing.height/2.0f, (processing.height/2.0f) / tan(PI*60.0f / 360.0f));
         dir = new PVector(processing.width/2.0f, processing.height/2.0f, 0);
     }
 
     public void draw(){
         if (ymove != null) {
             PVector d = ymove.delta();
             pos.add(d);
             dir.add(d);
         }
         if (xmove != null) {
             PVector d = xmove.delta();
             pos.add(d);
             dir.add(d);
         }
         processing.camera(pos.x, pos.y, pos.z,
                           dir.x, dir.y, dir.z,
                           camNorth.x, camNorth.y, camNorth.z);
     }
 
     private float dtheta = .03f;
     public void mouseDragged() {
         float dy = processing.pmouseY - processing.mouseY;
         if (dy != 0) {
             float y = (pos.y-dir.y);
             float z = (pos.z-dir.z);
             float r = PApplet.sqrt(y*y + z*z);
             float theta = PApplet.atan2(y, z);
 
             float theta_new = theta + ((dy > 0) ? dtheta : (-1*dtheta));
             y = sin(theta_new) * r;
             z = cos(theta_new) * r;
             pos.y = dir.y + y;
             pos.z = dir.z + z;
         }
 
         float dx = processing.pmouseX - processing.mouseX;
         if (dx != 0) {
             float x = (pos.x-dir.x);
             float z = (pos.z-dir.z);
             float r = sqrt(x*x + z*z);
             float theta = atan2(z, x);
 
             float theta_new = theta + ((dx < 0) ? dtheta : (-1*dtheta));
             x = cos(theta_new) * r;
             z = sin(theta_new) * r;
             pos.x = dir.x + x;
             pos.z = dir.z + z;
         }
     }
 
      void keyPressed() {
         char k = (char)processing.key;
         switch(k){
             case 'w':
            case 'W': ymove = new YMover(DIRECTION.NEGATIVE); break;
             case 's':
            case 'S': ymove = new YMover(DIRECTION.POSITIVE); break;
             case 'a':
            case 'A': xmove = new XMover(DIRECTION.NEGATIVE); break;
             case 'd':
            case 'D': xmove = new XMover(DIRECTION.POSITIVE); break;
             default: break;
         }
     }
 
 
   void mouseWheelMoved(MouseWheelEvent e) {
       int notches = -1*e.getWheelRotation(); //notches goes negative if the
                                           //wheel is scrolled up.
 
       PVector camera = PVector.sub(dir,pos);
       camera.normalize();
 
       pos.add(PVector.mult(camera, (float)notches * 100f));
       dir.add(PVector.mult(camera, (float)notches * 100f));
   }
 
   private abstract class Mover{
       private final int start = processing.millis();
       private int lastcall = start;
       private boolean isDisabled = false;
       public final DIRECTION direction;
 
       public Mover(DIRECTION direction){
           this.direction = direction;
       }
 
       public void enable(){
           isDisabled = false;
       }
 
       public void disable(){
           isDisabled = true;
       }
 
       public PVector delta(){
           if (isDisabled) {
             return DO_NOTHING;
           }
           int now = processing.millis();
           int totalMoveTime = now-start;
           if (totalMoveTime >= TIME_TICK) {
              disable();
              return DO_NOTHING;
           }
 
           int timePassed = now - lastcall;
           lastcall = now;
           PVector d = getDir();
           d.mult(timePassed/(float)TIME_TICK);
           return d;
       }
 
       public abstract PVector getDir();
   }
 
     private class XMover extends Mover{
         public XMover(DIRECTION direction){super(direction);}
         public PVector getDir(){
           PVector camera = PVector.sub(dir,pos);
           PVector cross = camera.cross(camNorth);
           cross.normalize();
           cross.mult(direction == DIRECTION.POSITIVE ? MOVE_TICK : -MOVE_TICK);
           return cross;
         }
     }
 
     private class YMover extends Mover{
         public YMover(DIRECTION direction){super(direction);}
         public PVector getDir(){
             PVector camera = PVector.sub(dir,pos);
             PVector cross = camera.cross(camNorth);
             PVector up = cross.cross(camera);
             up.normalize();
             up.mult(direction == DIRECTION.POSITIVE ? MOVE_TICK : -MOVE_TICK);
             return up;
         }
     }
 
 }
