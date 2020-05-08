 /*
  * TrackedMotionItem.java
  *
  * Created on October 23, 2005, 9:58 PM
  *
  * To change this template, choose Tools | Options and locate the template under
  * the Source Creation and Management node. Right-click the template and choose
  * Open. You can then make changes to the template in the Source Editor.
  */
 
 package CmbMultiPhysics;
 
 import java.awt.Shape;
 
 /**
  *
  * @author Administrator
  */
 public class TrackedMotionItem extends MotionItem implements Trackable {
     
     PhysicsTracker pt;
     
     /** Creates a new instance of TrackedMotionItem */
     public TrackedMotionItem() {
         super();
         pt = pt.getInstance();
         pt.registerItem(this);
     }
     
     public TrackedMotionItem(FloatVector position, float mass) {
         super(position, mass);
         pt = pt.getInstance();
         pt.registerItem(this);
     }
     
     public Shape getShape() {
        
     }
     
 }
