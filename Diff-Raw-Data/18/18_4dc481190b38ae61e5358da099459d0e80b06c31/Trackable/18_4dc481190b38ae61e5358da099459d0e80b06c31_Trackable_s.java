 /*
  * Trackable.java
  *
  * Created on October 23, 2005, 9:05 PM
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
 public interface Trackable {
     public Shape getShape();
     public FloatVector getPosition();
    public void tickForward();
 }
