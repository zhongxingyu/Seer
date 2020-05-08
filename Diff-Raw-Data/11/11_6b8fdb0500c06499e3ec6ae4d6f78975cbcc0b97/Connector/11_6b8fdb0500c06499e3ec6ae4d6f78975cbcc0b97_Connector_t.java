 /*
  * Joint.java
  *
  * Created on June 9, 2007, 3:12 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.objects;
 
 import edu.gatech.statics.math.Vector;
 import com.jme.math.Vector3f;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.util.SolveListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 abstract public class Connector extends SimulationObject {
 
     private Point anchor;
     private Body body1,  body2;
     private boolean isSolved = false;
     private List<Vector> solvedReactions = null;
     private List<Vector> solvedReactionsNegated = null;
 
     public void solveReaction(Body solveBody, List<Vector> reactions) {
 
         if (isSolved) {
             return;
         }
 
         //solvedReactions = reactions;
         List<Vector> negatedReactions = new ArrayList<Vector>();
         for (Vector v : reactions) {
             Vector v1 = v.negate();
 
             negatedReactions.add(v1);
         }
         
         if(solveBody == body1) {
             solvedReactions = reactions;
             solvedReactionsNegated = negatedReactions;
         } else if(solveBody == body2) {
             solvedReactions = negatedReactions;
             solvedReactionsNegated = reactions;
         } else {
            throw new UnsupportedOperationException(
                    "Attempting to solve a connector from the wrong body: "+solveBody+
                    " my bodies are "+body1+" and "+body2);
         }
 
         isSolved = true;
 
         for (SolveListener listener : StaticsApplication.getApp().getSolveListeners()) {
             listener.onJointSolved(this);
         }
     }
 
     public boolean isSolved() {
         return isSolved;
     }
 
     @Override
     public Vector3f getTranslation() {
         return anchor.getTranslation();
     }
 
     public void createDefaultSchematicRepresentation() {
     }
 
     public Point getAnchor() {
         return anchor;
     }
 
     public void attachToWorld(Body body) {
         /*this.body1 = body;
         this.body2 = null;
 
         body.addObject(this);*/
         attach(body, null);
     }
 
     public void attach(Body body1, Body body2) {
         this.body1 = body1;
         this.body2 = body2;
 
         if (body1 != null) {
             body1.addObject(this);
         }
         if (body2 != null) {
             body2.addObject(this);
         }
     }
 
     public Body getBody1() {
         return body1;
     }
 
     public Body getBody2() {
         return body2;
     }
 
     /** in some joints, notably pins and fixes, the reaction forces have 
      * horizontal and vertical components, meaning that they can go either direction
      */
     public boolean isForceDirectionNegatable() {
         return false;
     }
 
     /**
      * Also: Concerning finding forces and moments as a result of joints, note
      * that depending on whose perspective we are observing, the force will be 
      * reversed. So, we treat body1 as the first logical receptor to the force,
      * so if we are observing from body2, the force will be reversed.
      */
     abstract public List<Vector> getReactions();
 
     /** Creates a new instance of Joint */
     public Connector(Point point) {
         anchor = point;
     }
 
     public List<Vector> getReactions(Body body) {
         if (body != body1 && body != body2) {
             return Collections.EMPTY_LIST;
         }
 
         if (isSolved) {
             if (body == body1) {
                 return solvedReactions;
             } else {
                 return solvedReactionsNegated;
             }
 
         } else {
             List<Vector> reactions = new ArrayList<Vector>();
             if (body == body1) {
                 reactions.addAll(getReactions());
             } else {
                 for (Vector v : getReactions()) {
                     reactions.add(v.negate());
                 }
             }
             return reactions;
         }
     }
 
     @Override
     public String toString() {
         return getClass().getSimpleName() + " @ " + getAnchor().getName();
     }
 
     abstract public String connectorName();
 }
