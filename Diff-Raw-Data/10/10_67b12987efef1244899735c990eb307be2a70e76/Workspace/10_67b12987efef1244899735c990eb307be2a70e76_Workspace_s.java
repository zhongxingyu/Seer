 package model;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import model.scope.MethodScope;
 import model.scope.Scope;
 import util.DataSource;
 import util.Location;
 import util.Paintable;
 
 
 public class Workspace implements DataSource, ITurtle {
 
     private Scope myScope;
     private MethodScope myMethods;
     
     Map<Integer, Turtle> myTurtles = new HashMap<Integer, Turtle>();
     List<Turtle> myActiveTurtles = new ArrayList<Turtle>();
     Dimension myCanvasBounds;
 
     public Workspace (Dimension canvasBounds) {
         myScope = new Scope();
         myMethods = new MethodScope();
         
         Turtle firstTurtle = new Turtle(canvasBounds);
         myTurtles.put(0, firstTurtle);
         myActiveTurtles.add(firstTurtle);
         myCanvasBounds = canvasBounds;
     }
 
     public Scope getScope () {
         return myScope;
     }
 
     public MethodScope getMethodScope () {
         return myMethods;
     }
     
     public void setReturnValue (int returnValue) {
        // TODO
     }
 
     @Override
    public int getReturnValue () {
        // TODO Auto-generated method stub
        return 0;
     }
 
     @Override
     public int getTurtleHeading () {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public String showMessage () {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Image getBackgroundImage () {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public Iterator<Paintable> getPaintableIterator () {
         Collection<Paintable> collection = new ArrayList<Paintable>();
         for(Turtle turtle : myTurtles.values()){
             collection.addAll(turtle.getPaintables());
         }
         return collection.iterator();
     }
 
     @Override
     public Location getTurtlePosition () {
         return myActiveTurtles.get(myActiveTurtles.size() - 1).getTurtlePosition();
     }
 
     private List<Turtle> getActiveTurtles () {
         return myActiveTurtles;
     }
 
     @Override
     public int move (int pixels) {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.move(pixels);
         }
         return i;
     }
 
     @Override
     public int turn (int degrees) {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = (int) turtle.turn(degrees);
         }
         return i;
     }
 
     @Override
     public int setHeading (int heading) {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = (int) turtle.setHeading(heading);
         }
         return i;
     }
 
     @Override
     public int towards (Location location) {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = (int) turtle.towards(location);
         }
         return i;
     }
 
     @Override
     public int setLocation (Location location) {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.setLocation(location);
         }
         return i;
     }
 
     @Override
     public int showTurtle () {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.showTurtle();
         }
         return i;
     }
 
     @Override
     public int hideTurtle () {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.hideTurtle();
         }
         return i;
     }
 
     @Override
     public int showPen () {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.showPen();
         }
         return i;
     }
 
     @Override
     public int hidePen () {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.hidePen();
         }
         return i;
     }
 
     @Override
     public int home () {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.home();
         }
         return i;
     }
 
     @Override
     public int clearScreen () {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.clearScreen();
         }
         return i;
     }
 
     @Override
     public int isTurtleShowing () {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.isTurtleShowing();
         }
         return i;
     }
 
     @Override
     public int isPenDown () {
         int i = 0;
         for (Turtle turtle : getActiveTurtles()) {
             i = turtle.isPenDown();
         }
         return i;
     }
 
     @Override
     public int getHeading () {
         return (int) myActiveTurtles.get(myActiveTurtles.size() - 1).getHeading();
     }
     
     @Override
     public int getX () {
         return (int) myActiveTurtles.get(myActiveTurtles.size() - 1).getX();
     }
     
     @Override
     public int getY () {
         return (int) myActiveTurtles.get(myActiveTurtles.size() - 1).getY();
     }
 
     @Override
     public int setBackgroundColor (int colorIndex) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int setBackgroundImage (int imageIndex) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int setPenColor (int colorIndex) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int setPenSize (int pixels) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int setShape (int shapeIndex) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int setPalette (int colorIndex, int red, int green, int blue) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int getPenColor () {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int stamp () {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int clearStamps () {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int getShapeIndex () {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int getTurtleID () {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int setActiveTurtles (int[] turtleIds) {
         myActiveTurtles = new ArrayList<Turtle>();
         for(int id : turtleIds){
             if(myTurtles.containsKey(id)){
                 myActiveTurtles.add(myTurtles.get(id));
             } else {
                 Turtle newTurtle = new Turtle(myCanvasBounds);
                 myTurtles.put(id, newTurtle);
                 myActiveTurtles.add(newTurtle);
             }
         }
         return turtleIds[turtleIds.length - 1];
     }
 
     @Override
     public int setAllTurtlesActive () {
         myActiveTurtles = new ArrayList<Turtle>(myTurtles.values());
         return myActiveTurtles.size();
     }
 
     @Override
     public int makeEvenTurtlesActive () {
         return evenOddHelper(0);
     }
     
     private int evenOddHelper (int oddOrEven) {
         myActiveTurtles = new ArrayList<Turtle>();
         int lastId = 0;
         for(int id : myTurtles.keySet()){
             if(id % 2 == oddOrEven){
                 myActiveTurtles.add(myTurtles.get(id));
                 lastId = id;
             }
         }
         if (myActiveTurtles.isEmpty()) {
             myActiveTurtles = new ArrayList<Turtle>(myTurtles.values());
         }
         return lastId;
     }
 
     @Override
     public int makeOddTurtlesActive () {
         return evenOddHelper(1);
     }
 
 }
