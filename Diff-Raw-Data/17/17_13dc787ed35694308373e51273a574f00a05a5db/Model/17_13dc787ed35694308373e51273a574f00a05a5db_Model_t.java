 package model;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 import util.DataSource;
 import util.Location;
 import util.Paintable;
 import commands.ICommand;
 import exceptions.FormattingException;
 import exceptions.VariableNotFoundException;
 import factory.Parser;
 
 
 /**
  * The main Model class.
  * 
  * @author Zhen Gou
  * @author David Winegar
  * @author Will Nance
  * @author Jay Wang
  * 
  */
 public class Model extends Observable implements IModel, DataSource {
 
     /**
      * return value in the event of an error.
      */
     public static final int ERROR_RETURN_VALUE = -1;
 
     private Parser myParser;
     private Turtle myTurtle;
     private Scope myScope;
     private int myReturnValue;
     private String myReturnMessage;
 
     /**
      * Instantiates parser, scope, and turtle and passes the canvasBounds.
      * 
      * @param canvasBounds to pass to turtle
      */
     public Model (Dimension canvasBounds) {
         myTurtle = new Turtle(canvasBounds);
         myScope = new Scope();
         myParser = new Parser(this);
     }
 
     /**
      * returns the current turtle
      * 
      * @return turtle to return
      */
     public Turtle getTurtle () {
         return myTurtle;
 
     }
 
     /**
      * returns the current scope
      * 
      * @return myScope
      */
     public Scope getScope () {
         return myScope;
     }
 
     @Override
     public void executeCommand (String command) {
 
         ICommand executable;
        int returnValue = ERROR_RETURN_VALUE;
         try {
             executable = myParser.parse(command);
            returnValue = executable.execute();
            myReturnMessage = "";
         }
         catch (FormattingException e) {
            myReturnMessage = "Formatting error";
            
         }
         catch (VariableNotFoundException e) {
            myReturnMessage = "Variable not found";
         }
         finally {
            myReturnValue = returnValue;
             notifyView();
         }
 
     }
 
     /**
      * sets observable as changed and notifies observers.
      */
     private void notifyView () {
         setChanged();
         notifyObservers();
     }
 
     @Override
     public void saveFunctionsAndVariables (File fileToSave) {
 
     }
 
     @Override
     public void loadFunctionsAndVariables (File fileToLoad) {
 
     }
 
     @Override
     public DataSource getDataSource () {
         return this;
     }
 
     @Override
     public void initializeObserver (Observer observer) {
         addObserver(observer);
     }
 
     @Override
     public Iterator<Paintable> getPaintableIterator () {
         return myTurtle.getPaintableIterator();
     }
 
     @Override
     public int getReturnValue () {
         return myReturnValue;
     }
 
     @Override
     public Location getTurtlePosition () {
         return myTurtle.getTurtlePosition();
     }
 
     @Override
     public int getTurtleHeading () {
         return (int) myTurtle.getHeading();
     }
 
     @Override
     public String showMessage () {
         return myReturnMessage;
     }
 
     /**
      * Sets current return value
      * 
      * @param value to set
      */
     public void setReturnValue (int value) {
         myReturnValue = value;
     }
 
     /**
      * Sets the return message
      * 
      * @param message to set
      */
     public void setReturnMessage (String message) {
         myReturnMessage = message;
     }
 }
