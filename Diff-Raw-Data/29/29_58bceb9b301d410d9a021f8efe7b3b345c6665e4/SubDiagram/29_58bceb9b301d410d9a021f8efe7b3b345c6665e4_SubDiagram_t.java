 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.exercise;
 
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.state.DiagramState;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.bodies.Background;
 import edu.gatech.statics.util.DiagramListener;
import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 abstract public class SubDiagram<StateType extends DiagramState> extends Diagram<StateType> {
 
     private BodySubset bodies;
    //private Diagram<?> currentDiagram = StaticsApplication.getApp().getCurrentDiagram();
    private List<Body> currentDiagram = Exercise.getExercise().getSchematic().allBodies();
     private int totalBodies;
 
     public BodySubset getBodySubset() {
         return bodies;
     }
 
     @Override
     public DiagramKey getKey() {
         return bodies;
     }
 
     public SubDiagram(BodySubset bodies) {
         this.bodies = bodies;
         totalBodies = 0;
 
         assert bodies != null : "Bodies cannot be null in constructing FBD!";
         assert !bodies.getBodies().isEmpty() : "Bodies cannot be empty in constructing FBD!";
 
         //determine if all bodies have been selected
        for (Body body : currentDiagram) {
             if (body instanceof Background) {
                 continue;
             }
             totalBodies++;
         }
 
         if (bodies.getBodies().size() == totalBodies) {
             bodies.setSpecialName("Whole Frame");
         }
 
         for (DiagramListener listener : StaticsApplication.getApp().getDiagramListeners()) {
             listener.onDiagramCreated(this);
         }
     }
 }
