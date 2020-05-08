 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rackspace.racetrack;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author user
  */
 public class TrackElement {
     protected Double timeFactor;
     protected Double originalPosition;
     protected Double position;
     protected Double lastPosition;
     protected ArrayList<TrackElement> dependencies;
     protected Track parent;
     
     public TrackElement(Double position) {
         this.timeFactor = 1.0;
         this.position = position;
         this.originalPosition = position;
         this.lastPosition = position;
         this.parent = null;
         
         dependencies = new ArrayList<TrackElement>();
     }
     
     public TrackElement(Double position, Track parent) {
         this.timeFactor = 1.0;
         this.position = position;
         this.originalPosition = position;
         this.lastPosition = position;
         this.parent = parent;
         
         dependencies = new ArrayList<TrackElement>();
     }
     
     public TrackElement(Double position, Double timeFactor, Track parent) {
         this.timeFactor = timeFactor;
         this.position = position;
         this.originalPosition = position;
         this.lastPosition = position;
         this.parent = parent;
         
         dependencies = new ArrayList<TrackElement>();
     }
     
     public static void link(TrackElement element1, TrackElement element2) {
         element1.dependencies.add(element2);
         element2.dependencies.add(element1);
     }
     
     public void link(TrackElement element) {
         if (dependencies != null) {
             dependencies.add(element);
         }
     }
     
      public Boolean canMove(Double deltaPosition) {
          ArrayList<TrackElement> traversed = new ArrayList<TrackElement>();
          
         for (TrackElement dependency: dependencies) {
             if (traversed.contains(dependency))
                 continue;
             
             traversed.add(this);
             
             if (!dependency.canMove(traversed, deltaPosition)) {
                 return false;
             }
         }
         
         return (position + deltaPosition <= 1 && position + deltaPosition >= 0);
         //return (originalPosition + deltaPosition <= 1 && originalPosition + deltaPosition >= 0);
     }
     
     public Boolean canMove(ArrayList<TrackElement> traversed, Double deltaPosition) {
         for (TrackElement dependency: dependencies) {
             if (traversed.contains(dependency))
                 continue;
             
             traversed.add(this);
             
             if (!dependency.canMove(traversed, deltaPosition)) {
                 return false;
             }
         }
         
         return (position + deltaPosition <= 1 && position + deltaPosition >= 0);
         //return (originalPosition + deltaPosition <= 1 && originalPosition + deltaPosition >= 0);
     }
     
     public Boolean move(Double deltaPosition) {
         ArrayList<TrackElement> traversed = new ArrayList<TrackElement>();
         
         if (this.canMove(deltaPosition)) {
             for (TrackElement dependency: dependencies) {
                 if (!traversed.contains(dependency)) {
                     traversed.add(this);
                     dependency.move(traversed, deltaPosition);
                 }
             }
             
             position += deltaPosition;
             //position = originalPosition + deltaPosition;
             return true;
         }
         else {
             return false;
         }
     }
     
     public Boolean move(ArrayList<TrackElement> traversed, Double deltaPosition) {
         if (this.canMove(deltaPosition)) {
             for (TrackElement dependency: dependencies) {
                 if (!traversed.contains(dependency)) {
                     traversed.add(this);
                     dependency.move(traversed, deltaPosition);
                 }
             }
             
             position += deltaPosition;
             //position = originalPosition + deltaPosition;
             return true;
         }
         else {
             return false;
         }
     }
     
     public Boolean canMoveRelative(Double deltaPosition) {
          ArrayList<TrackElement> traversed = new ArrayList<TrackElement>();
          
         for (TrackElement dependency: dependencies) {
             if (traversed.contains(dependency))
                 continue;
             
             traversed.add(this);
             
             if (!dependency.canMoveRelative(traversed, deltaPosition)) {
                 return false;
             }
         }
         
         //return (position + deltaPosition <= 1 && position + deltaPosition >= 0);
         return (lastPosition + deltaPosition <= 1 && lastPosition + deltaPosition >= 0);
     }
     
     public Boolean canMoveRelative(ArrayList<TrackElement> traversed, Double deltaPosition) {
         for (TrackElement dependency: dependencies) {
             if (traversed.contains(dependency))
                 continue;
             
             traversed.add(this);
             
             if (!dependency.canMoveRelative(traversed, deltaPosition)) {
                 return false;
             }
         }
         
         //return (position + deltaPosition <= 1 && position + deltaPosition >= 0);
         return (lastPosition + deltaPosition <= 1 && lastPosition + deltaPosition >= 0);
     }
     
     public Boolean moveRelative(Double deltaPosition) {
         ArrayList<TrackElement> traversed = new ArrayList<TrackElement>();
         
         if (this.canMoveRelative(deltaPosition)) {
             for (TrackElement dependency: dependencies) {
                 if (!traversed.contains(dependency)) {
                     traversed.add(this);
                     dependency.moveRelative(traversed, deltaPosition);
                 }
             }
             
             //position += deltaPosition;
             position = lastPosition + deltaPosition;
             return true;
         }
         else {
             return false;
         }
     }
     
     public Boolean moveRelative(ArrayList<TrackElement> traversed, Double deltaPosition) {
         if (this.canMoveRelative(deltaPosition)) {
             for (TrackElement dependency: dependencies) {
                 if (!traversed.contains(dependency)) {
                     traversed.add(this);
                     dependency.moveRelative(traversed, deltaPosition);
                 }
             }
             
             //position += deltaPosition;
             position = lastPosition + deltaPosition;
             return true;
         }
         else {
             return false;
         }
     }
     
     public void savePosition() {
        ArrayList<TrackElement> traversed;
        traversed = new ArrayList<TrackElement>();
        
        for (TrackElement dependency: dependencies) {
            if (!traversed.contains(dependency)) {
                traversed.add(this);
                dependency.savePosition(traversed);
            }
        }
        lastPosition = position;
    }
    
    public void savePosition(ArrayList<TrackElement> traversed) {
        for (TrackElement dependency: dependencies) {
            if (!traversed.contains(dependency)) {
                traversed.add(this);
                dependency.savePosition(traversed);
            }
        }
         lastPosition = position;
     }
     
     public void reset() {
         position = lastPosition = originalPosition;
     }
     
     public Double getTimeFactor() {
         return timeFactor;
     }
     
     public Double getPosition() {
         return position;
     }
     
     public Double getOriginalPosition() {
         return originalPosition;
     }
     
     public Double getDeltaPosition() {
         return position - originalPosition;
     }
     
     public Track getParent() {
         return parent;
     }
     
     public void setParent(Track parent) {
         this.parent = parent;
     }
 }
