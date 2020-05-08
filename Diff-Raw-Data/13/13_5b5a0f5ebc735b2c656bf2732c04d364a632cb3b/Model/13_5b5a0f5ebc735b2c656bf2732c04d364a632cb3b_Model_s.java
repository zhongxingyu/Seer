 package slogo;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import object.Turtle;
 import behavior.ICommand;
 import exceptions.NoSuchVariableException;
 
 
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
      * Title of program
      */
     public static final String TITLE = "SLOGO";
 
     private Map<String, ICommand> myUserToCommands = new HashMap<String, ICommand>();
     private Map<Integer, Turtle> myTurtles;
     private List<Integer> myCurrentActivatedTurtles;
     private List<Integer> myFutureActivatedTurtles;
     private int myTurtleNumber;
     private Map<String, String> myVariables;
     private Controller myController;
     private int myModelID;
 
     /**
      * Constructs model that holds objects
      * 
      * @param controller the controller
      * @param id the model id
      */
     public Model (Controller controller, int id) {
 
         myTurtles = new HashMap<Integer, Turtle>();
         myCurrentActivatedTurtles = new ArrayList<Integer>();
         myFutureActivatedTurtles = new ArrayList<Integer>();
         myTurtleNumber = 0;
         myVariables = new HashMap<String, String>();
         myController = controller;
 
     }
 
     /**
      * Get model id
      * 
      * @return
      */
     public int getID () {
         return myModelID;
     }
 
     /**
      * get controller
      * 
      * @return
      */
     public Controller getController () {
         return myController;
     }
 
     /**
      * add user defined commands
      * 
      * @param string String
      * @param command commands
      */
     public void addUserCommands (String string, ICommand command) {
         myUserToCommands.put(string, command);
     }
 
     /**
      * return user command map
      * 
      * @return
      */
     public Map<String, ICommand> getUserCommands () {
         return myUserToCommands;
     }
 
     /**
      * Return specific turtle
      * 
      * @param index index of turtle
      * @return
      */
     public Turtle getMyTurtle (int index) {
         return myTurtles.get(index);
     }
 
     /**
      * adds a new turtle
      */
     public void addNewTurtle () {
         Turtle turtle = new Turtle();
         myTurtles.put(myTurtleNumber, turtle);
         myTurtleNumber++;
     }
     
     public void addNewTurtle(int turtleNumber) {
         Turtle turtle = new Turtle();
         myTurtles.put(turtleNumber, turtle);
         myTurtleNumber ++;
     }
 
     /**
      * return list of turtles
      * 
      * @return
      */
     public Map<Integer, Turtle> getMyTurtles () {
         return myTurtles;
     }
 
     /**
      * Show message
      * 
      * @param message the message
      */
     public void showMessage (String message) {
         myController.showMessage(message);
     }
 
     /**
      * Add a variable
      * 
      * @param name the variable
      * @param value the value
      */
     public void addVariable (String name, String value) {
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
         if (!myVariables.containsKey(name)) { throw new NoSuchVariableException(); }
         return Double.parseDouble(myVariables.get(name));
     }
 
     /**
      * set variable value
      * 
      * @param key string key
      * @param value value
      */
     public void setVariableValue (String key, Double value) {
         myVariables.put(key, value + "");
     }
 
     /**
      * get usre variables
      * 
      * @return
      */
     public Map<String, String> getUserVariables () {
         return myVariables;
     }
 
     /**
      * Clear the values of the all variables
      */
     public void clearVariable () {
         myVariables.clear();
     }
 
     /**
      * clear future activiated turtles
      */
     public void clearFutureActivatedTurtles () {
         myFutureActivatedTurtles.clear();
     }
 
     /**
      * activated turtles
      */
     public void activatedAllTurtles () {
         for (int i = 0; i < myTurtleNumber; i++) {
             myFutureActivatedTurtles.add(i);
         }
     }
 
     /**
      * add future activated turtles
      * 
      * @param turtleIndex turtle index
      */
     public void addFutureActivatedTurtles (int turtleIndex) {
        myFutureActivatedTurtles.add(turtleIndex);
     }
 
     /**
      * get current activated turtles
      * 
      * @return
      */
     public List<Integer> getMyCurrentActivatedTurtles () {
         return myCurrentActivatedTurtles;
     }
 
     /**
      * replace activated turltes
      */
     public void replaceActivatedTurtles () {
         myCurrentActivatedTurtles = new ArrayList<Integer>(myFutureActivatedTurtles);
     }
 
     /**
      * add current activated turtles
      * 
      * @param turtleIndex turlte index
      */
     public void addCurrentActivatedTurtles (int turtleIndex) {
         myCurrentActivatedTurtles.add(turtleIndex);
     }
 
 }
