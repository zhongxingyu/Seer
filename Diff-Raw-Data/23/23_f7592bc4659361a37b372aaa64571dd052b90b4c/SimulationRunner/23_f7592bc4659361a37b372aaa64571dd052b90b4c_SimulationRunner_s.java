 package student;
 
 import student.gui.MouseInteractionHandler;
 import student.gui.WorldFrame;
 import student.world.World;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  *
  * @author Panda
  */
 public class SimulationRunner {
 
     public static void main(String[] args) {
         World model = new World(6,6);
         WorldFrame view = new WorldFrame(model);
         MouseInteractionHandler controller = new MouseInteractionHandler(model, view);
         view.setVisible(true);
     }
 }
