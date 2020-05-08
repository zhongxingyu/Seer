 package simulation;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.util.List;
 import java.util.ArrayList;
 
 import util.Vector;
 import view.Canvas;
 
 
 /**
  * XXX.
  * 
  * @author Robert C. Duvall
  */
 public class Model {
     // bounds and input for game
     private Canvas myView;
     // simulation state
     private List<Mass> myMasses;
     private List<Spring> mySprings;
     private Physics myPhysics;
     private Environment myEnvironment;
 
     /**
      * Create a game of the given size with the given display for its shapes.
      */
     public Model (Canvas canvas) {
         myView = canvas;
         myMasses = new ArrayList<Mass>();
         mySprings = new ArrayList<Spring>();
         myPhysics = new Physics();
         myEnvironment = new Environment();
     }
 
     /**
      * Draw all elements of the simulation.
      */
     public void paint (Graphics2D pen) {
         for (Spring s : mySprings) {
             s.paint(pen);
         }
         for (Mass m : myMasses) {
             m.paint(pen);
         }
         
         myPhysics.getCenterOfMass().draw(pen);
     }
 
     /**
      * Update simulation for this moment, given the time since the last moment.
      */
     public void update (double elapsedTime) {
         Dimension bounds = myView.getSize();
         myEnvironment.setEnvironment(myPhysics, myMasses);
         
         for (Spring s : mySprings) {
             s.update(elapsedTime, bounds);
         }
         
         for (Mass m : myMasses) {
             m.update(elapsedTime, bounds);
         }
        
     }
 
     /**
      * Add given mass to this simulation.
      */
     public void add(Mass mass) {
         myMasses.add(mass);
     }
     public void setPhysics(Physics physics)
     {
     	myPhysics = physics;
     	myPhysics.getCenterOfMass().setCanvas(myView);
     }
     public Physics getPhysics()
     {
     	return myPhysics;
     }
     /**
      * Add given spring to this simulation.
      */
     public void add (Spring spring) {
         mySprings.add(spring);
     }
 }
