 /*
  * PhysicsTracker.java
  *
  * Created on October 23, 2005, 9:02 PM
  *
  * To change this template, choose Tools | Options and locate the template under
  * the Source Creation and Management node. Right-click the template and choose
  * Open. You can then make changes to the template in the Source Editor.
  */
 
 package CmbMultiPhysics;
 
 import java.util.Vector;
 import java.util.Enumeration;
 import java.awt.Shape;
 import java.lang.Class;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 /**
  *
  * @author Chris Baron
  */
 public class PhysicsTracker {
     
     static private PhysicsTracker instance = null;
     private Hashtable perTickItems;
     private Vector trackableItems;
     
     /** Creates a new instance of PhysicsTracker */
     public PhysicsTracker() {
         trackableItems = new Vector();
     }
     
     static public PhysicsTracker getInstance() {
         if (instance == null) {
             instance = new PhysicsTracker();
         } 
         return(instance);
     }
     
     public void tickForward(float deltaT) {
 
         perTickItems = new Hashtable();
         
         Iterator<Trackable> i = trackableItems.iterator();
     
         while (i.hasNext()) {
             try {
            i.next().tickForward();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
     
     public synchronized void registerItem(Trackable t) {
         trackableItems.add(t);
     }
     
     
     public synchronized void registerTick(Trackable t) {
         Shape incomingShape = t.getShape();
         
         Enumeration<Shape> enumeration = perTickItems.keys();
     
         
         while (enumeration.hasMoreElements()) {
             final Shape nextShape = enumeration.nextElement();
             if (nextShape.contains(incomingShape.getBounds2D())) {
                 // we collided with something, so lets pull out the trackable
                 Trackable collidingTrackable = (Trackable) perTickItems.get(nextShape);
                 
                 // check to see if we've got TWO collision items
                 if ((t.getClass().isAssignableFrom(CollisionItem.class)) && (collidingTrackable.getClass().isAssignableFrom(CollisionItem.class))) {
                     ((CollisionItem) t).doCollision((CollisionItem)collidingTrackable);
                 }
             }
         }
         
     }
     
 }
