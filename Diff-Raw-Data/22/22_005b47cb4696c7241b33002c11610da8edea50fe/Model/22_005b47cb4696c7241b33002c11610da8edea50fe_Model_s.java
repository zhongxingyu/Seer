 package slogo;
 
 
 import exceptions.NoSuchVariableException;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import object.Turtle;
 import view.DisplayArea;
 
 
 /**
  * 
  * @author Richard Yang
  * 
  */
 public class Model {
 
     /**
      * Default turtle area size
      */
     public static final Dimension TURTLE_AREA_SIZE = new Dimension(800, 500);
     /**
      * Default display area size
      */
     public static final Dimension DISPLAY_AREA_SIZE = new Dimension(200, 500);
     /**
      * Default Frame size
      */
     public static final Dimension SIZE = new Dimension(1100, 700);
     /**
      * String title
      */
     
     
     public static final String TITLE = "SLOGO";
     
     private int myID;
     private Controller myController;
     private List<Turtle> myTurtles;
     private DisplayArea myDisplayArea;
     private Map<String, Double> myVariables;
    private Turtle myInitialTurtle;
 
     /**
      * Constructs model that holds objects
      * 
      * @param controller the controller
      * @param id         the model id
      */
     public Model (Controller controller, int id) {
         myController = controller;
         myID = id;
         myTurtles = new ArrayList<Turtle>();
        myInitialTurtle = new Turtle();
        myTurtles.add(myInitialTurtle);
         myVariables = new HashMap<String, Double>();
 
     }
 
     /**
      * Return specific turtle
      * @param index     index of turtle
      * @return
      */
     public Turtle getMyTurtle (int index) {
         return myTurtles.get(index);
     }
     
     /**
      * return list of turtles
      * @return
      */
     public List<Turtle> getMyTurtles () {
         return myTurtles;
     }
 
     /**
      * Show message
      * 
      * @param message the message
      */
     public void showMessage (String message) {
         myDisplayArea.showMessage(message);
     }
 
     /**
      * Add a variable
      * 
      * @param name the variable
      * @param value the value
      */
     public void addVariable (String name, Double value) {
         myVariables.put(name, value);
     }
 
     /**
      * Remove a variable
      * 
      * @param name the string
      */
     public void removeVariable (String name) {
         myVariables.remove(name);
     }
 
     /**
      * get the variable name
      * 
      * @param name the variable name
      * @return
      * @throws NoSuchVariableException throws if there is no variable
      */
     public double getVariableValue (String name) throws NoSuchVariableException {
         if (!myVariables.containsKey(name)) {
             throw new NoSuchVariableException();
         }
         return myVariables.get(name);
     }
 
     /**
      * Clear the values of the all variables
      */
     public void clearVariable () {
         myVariables.clear();
     }
 
 }
