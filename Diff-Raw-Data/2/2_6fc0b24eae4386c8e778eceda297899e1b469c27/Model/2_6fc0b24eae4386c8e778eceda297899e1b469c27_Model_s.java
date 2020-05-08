 package simulation;
 
 import control.Controller;
 import control.Environment;
 import drawing.Palette;
 import drawing.StampSprite;
 import drawing.lines.Point;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import view.View;
 
 
 /**
  * Represents the simulation of the drawings on the screen. The model holds the drawer (the turtle)
  * and all of the lines that have been drawn.
  * 
  * @author Scott Valentine
  * @author Ryan Fishel
  * @author Ellango Jothimurugesan
  */
 public class Model implements DisplayEditor {
 
     private static final int MAXIMUM_STATES_REMEMBERED = 10;
     /**
      * Represents all of the state of the simulation.  It contains all of the 
      * turtles, the active turtle, the lines and stamps on the screen, and the
      * environment for variables and functions.
      * 
      * It is stored as one class so that it can be reverted with undo/redo operations
      * 
      * Class is private so that only Model has access.
      */
     private class State {
         Collection<Turtle> turtles;
         Turtle activeTurtle;
         Collection<Point> lines;
         Collection<StampSprite> stamps;
         Environment environment;
     }
 
     private State myState;
     private LinkedList<State> myPreviousStates;
     private LinkedList<State> myUndoneStates;
 
     private View myView;
 
     /**
      * Instantiates a model with a turtle and a collection of lines.
      */
     public Model () {
        State myState = new State();
         myPreviousStates = new LinkedList<State>();
         myUndoneStates = new LinkedList<State>();
 
         myState.turtles = new ArrayList<Turtle>();
         myState.lines = new ArrayList<Point>();
         myState.stamps = new ArrayList<StampSprite>();
     }
 
     /**
      * Initializes the model with an environment and at least one active turtle.
      * 
      * @return The environment that is initialized in the model.
      */
     public Environment initialize () {
         myState.environment = new Environment(myView.getResources());
         myState.activeTurtle = new Turtle(this);
         myState.turtles.add(myState.activeTurtle);
         return myState.environment;
     }
 
     /**
      * Sets the view used by the model for painting.
      * 
      * @param view on which the model paints.
      */
     public void setView (View view) {
         myView = view;
     }
 
     /**
      * Updates all of the elements of model.
      * 
      * @param elapsedTime is the time since the last update.
      * @param bounds is the current bounds of the canvas in the view. (This is the area where the
      *        lines and turtle are displayed).
      */
     public void update (double elapsedTime, Dimension bounds) {
 
         for (Turtle turt : myState.turtles) {
             turt.update(elapsedTime, bounds);
         }
     }
 
     /**
      * Paints all current elements (turtles and lines) of the model.
      * 
      * @param pen is the graphic that is used to paint lins and turtles.
      */
     public void paint (Graphics2D pen) {
         for (StampSprite st : myState.stamps) {
             st.paint(pen);
         }
         for (Turtle t : myState.turtles) {
             t.paint(pen);
         }
         for (Point line : myState.lines) {
             line.paint(pen);
         }
         myState.activeTurtle.paintStatus(pen);
     }
 
     /**
      * Gives the current active turtle in the model.
      * 
      * @return The active turtle in the model.
      */
     public Turtle getTurtle () {
         return myState.activeTurtle;
     }
 
     /**
      * Returns the environment containing instructions, variables, Palette.
      * 
      * @return environment
      */
     public Environment getEnvironment () {
         return myState.environment;
     }
 
     @Override
     public Palette getPalette () {
         return myState.environment.getPalette();
     }
 
     @Override
     public void addLine (Point line) {
         myState.lines.add(line);
     }
 
     @Override
     public void addStamp (StampSprite st) {
         myState.stamps.add(st);
     }
 
     /**
      * Clears stamps and lines from model.
      */
     public void clear () {
         clearLines();
         clearStamps();
     }
 
     /**
      * Clears all lines from the model.
      */
     public void clearLines () {
         myState.lines.clear();
     }
 
     /**
      * Clears all stamps in the current workspace.
      */
     public void clearStamps () {
         myState.stamps.clear();
     }
 
     /**
      * Calls the view method to display the result of the command, or an error
      * message back to the user. Appends an indicator string to the beginning
      * to differentiate the result from commands issued by the user.
      * 
      * @param s return message
      */
     public void informView (String s) {
         myView.displayText(Controller.PRINT_INDICATOR + s);
     }
     
  
 //    /**
 //     * After execution of an instruction, the current state is copied and stored
 //     * onto a stack of previous states, so that the state can be restored at a 
 //     * later point with undo.
 //     * 
 //     * It is not implemented right now because every object that the
 //     * state contains needs to be copied, and we did not have time to write
 //     * .copy() methods for each object 
 //     */
 //    public void newState () {
 //        State old = myState.copy();
 //        if (myPreviousStates.size() > MAXIMUM_STATES_REMEMBERED) {
 //            myPreviousStates.removeLast();
 //        }
 //        myPreviousStates.push(old);
 //    }
 //
 //    /**
 //     * Restores the state to the previous most state on the stack
 //     */
 //    public void undo () {
 //        try {
 //            myUndoneStates.push(myState);
 //            if (myPreviousStates.isEmpty()) { throw new UndoException(); }
 //            myState = myPreviousStates.pop();
 //        }
 //        catch (UndoException e) {
 //            informView(e.toString());
 //        }
 //
 //    }
 //
 //    /**
 //     * Restores the state to the last state undone
 //     */
 //    public void redo () {
 //        try {
 //            myPreviousStates.push(myState);
 //            if (myUndoneStates.isEmpty()) { throw new RedoException(); }
 //            myState = myUndoneStates.pop();
 //        }
 //        catch (RedoException e) {
 //            informView(e.toString());
 //        }
 //    }
 
 }
