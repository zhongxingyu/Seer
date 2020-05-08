 package slogo;
 
 import object.Turtle;
 
 /**
  * 
  * @author Jerry
  *
  */
 public class Factory {
     
     private Controller myController;
     
     /**
      * Constructs factory for controller
      * @param controller        the controller
      */
     public Factory (Controller controller) {
         myController = controller;
     }
     
     /**
      * Creates turtle 
      * @param model     the model id to identify model turtle is created in
      */
     public void createTurtle(int model) {
         Turtle turtle = new Turtle();
         myController.getMyModels().get(model).getMyTurtles().add(turtle);
     }
     
 }
 
