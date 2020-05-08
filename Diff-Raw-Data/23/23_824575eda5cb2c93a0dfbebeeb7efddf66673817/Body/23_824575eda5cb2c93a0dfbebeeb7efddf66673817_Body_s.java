 /*
  * SimBody.java
  *
  * Created on June 4, 2007, 2:35 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.objects;
 
 import com.jme.math.Vector3f;
 import edu.gatech.statics.*;
 import edu.gatech.statics.math.Quantity;
 import edu.gatech.statics.math.Unit;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 abstract public class Body extends SimulationObject {
 
     //private float weight = 0;
     private Point centerOfMassPoint;
     //private boolean massless;
     private float width;  // x
     private float height; // y
     private float depth;  // z
 
     public void setDimensions(float width, float height, float depth) {
         this.width = width;
         this.height = height;
         this.depth = depth;
     }
 
     public float getWidth() {
         return width;
     }
 
     public float getHeight() {
         return height;
     }
 
     public float getDepth() {
         return depth;
     }
     //public void setWeight(float weight) {this.weight = weight;}
     //public float getWeight() {return weight;}
     private Quantity weight = new Quantity(Unit.force, BigDecimal.ZERO);
 
     public Quantity getWeight() {
         return weight;
     }
 
     // This method is lifted from Vector
     //private String getMagnitudeString() {
     //    UnitUtils units = StaticsApplication.getApp().getUnits();
     //    return String.format("%."+units.getDecimalPrecisionForces()+"f", getWeight());
     //}
 
     //public String getWeightText() {
     //    return ""+getMagnitudeString()+" "+StaticsApplication.getApp().getUnits().getForce();
     //}
     //public boolean isMassless() {return massless;}
     //public void setMassless(boolean massless) {this.massless = massless;}
     public Vector3f getCenterOfMass() {
         return getTranslation();
     }
 
     public void setCenterOfMassPoint(Point p) {
         this.centerOfMassPoint = p;
         addObject(p);
     }
 
     public Point getCenterOfMassPoint() {
         return centerOfMassPoint;
     }
     /*
     private List<Point> points = new ArrayList();
     private List<Force> attachedForces = new ArrayList();
     private List<Moment> attachedMoments = new ArrayList();
     public void addPoint(Point p) {points.add(p);}
     public void removePoint(Point p) {points.remove(p);}
     public void addForce(Force f) {attachedForces.add(f);}
     public void addMoment(Moment m) {attachedMoments.add(m);}
     public void removeForce(Force f) {attachedForces.remove(f);}
     public void removeMoment(Moment m) {attachedMoments.remove(m);}
     public List<Force> getForces() {return Collections.unmodifiableList(attachedForces);}
     public List<Moment> getMoments() {return Collections.unmodifiableList(attachedMoments);}
     public List<Point> getPoints() {return Collections.unmodifiableList(points);}
      */
     private List<SimulationObject> attachedObjects = new ArrayList();
 
     public void addObject(SimulationObject obj) {

         if (!attachedObjects.contains(obj)) {
             attachedObjects.add(obj);
         }
 
         if (obj instanceof Joint) {
             addObject(((Joint) obj).getPoint());
         }
     }
 
     public void removeObject(SimulationObject obj) {
         attachedObjects.remove(obj);
     }
 
     public List<SimulationObject> getAttachedObjects() {
         return Collections.unmodifiableList(attachedObjects);
     }
 
     @Override
     public String toString() {
         return getName();
     }
 
     /** Creates a new instance of SimBody */
     public Body() {
     }
 
     @Override
     public String getDescription() {
         return "Body: " + getName() + "<br>" +
                 "Weight: " + getWeight() + " kg";
     }
 }
