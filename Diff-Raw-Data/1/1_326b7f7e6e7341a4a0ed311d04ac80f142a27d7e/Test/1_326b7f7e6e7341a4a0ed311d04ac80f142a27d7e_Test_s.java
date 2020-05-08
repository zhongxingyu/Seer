 package demo;
 
import javax.vecmath.Vector3d;
 import com.github.hiuprocon.pve.core.*;
 import com.github.hiuprocon.pve.obj.*;
 
 public class Test implements CollisionListener {
     static PVEObject ground;
 
     public static void main(String args[]) {
         PVEWorld w = new PVEWorld(PVEWorld.A3CANVAS);
         TestGUI gui = new TestGUI(w.getMainCanvas());
         w.addCollisionListener(new Test());
         w.resume();
 
         ground = new Ground(
                 "x-rzip:x-res:///res/stk_racetrack.zip!/racetrack.wrl");
         w.add(ground);
 
         Chain chain = new Chain();
         chain.setLocRev(0, 2, 5, 0, 0, 0);
         w.add(chain);
 
         // KeyboardForkLift c = new
         // KeyboardForkLift(w,"x-res:///res/stk_tux.a3");
         // KeyboardHeavyCar c = new KeyboardHeavyCar();
         KeyboardHovercraft c = new KeyboardHovercraft();
         c.setLocRev(0, 1, 0, 0, 0, 0);
         w.add(c);
         gui.setCar(c);
     }
 
     @Override
     public void collided(PVEPart a, PVEPart b) {
         PVEObject aa = a.getObject();
         PVEObject bb = b.getObject();
         if (aa == ground || bb == ground)
             return;
         System.out.println("collided " + aa.getClass().getName() + ":"
                 + bb.getClass().getName());
     }
 }
