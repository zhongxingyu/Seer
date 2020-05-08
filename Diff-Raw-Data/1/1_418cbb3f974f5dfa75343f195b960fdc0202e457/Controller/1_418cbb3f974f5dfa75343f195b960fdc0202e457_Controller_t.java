 package controller;
 
 import backEnd.Instruction;
 import backEnd.Model;
 import backEnd.Turtle;
 import backEnd.Workspace;
 import frontEnd.Canvas;
 import java.util.Observable;
 import java.util.Observer;
 
 
 /**
  * Workspace object serves as the controller between the model and the view.
  * Implements Observer interface. Observes Turtle object. Observes Workspace object.
  * 
  * @author Danny Goodman, Francesco Agosti, Challen Herzberg-Brovold, Eunsu (Joe) Ryu
  * 
  */
 public class Controller implements Observer {
 
     private Canvas myView;
     private Model myModel;
 
     public Controller (Canvas view) {
         myView = view;
         myModel = new Model(this);
     }
 
     /**
      * Called by Observable's notifyObservers method
      */
     @Override
     public void update (Observable arg0, Object arg1) {
         if(arg0.getClass().equals(Turtle.class)){
             myView.updateTurtle((Turtle) arg0);
         }
         if(arg0.getClass().equals(Workspace.class)){
             // TODO
         }
     }
 
     /**
      * Called by Canvas class when "Enter" button is pressed. Backend implements this to pass
      * the un-parsed text from the controller to the model.
      * 
      * @param text
      */
 
     public void sendInput (String text) {
         Instruction commands = myModel.formatString(text);
         myModel.processInstruction(commands);
     }
 
     public int getWorkspaceIndex () {
         return myView.getWorkspaceIndex();
     }
 
     public void addDimension () {
     	myModel.addTurtleList();
     }
 
     public void sendHistory (double value) {
         myView.writeHistory(Double.toString(value));
 
     }
 
     public void showErrorMsg (String text) {
         myView.showErrorMsg(text);
     }
 
 }
