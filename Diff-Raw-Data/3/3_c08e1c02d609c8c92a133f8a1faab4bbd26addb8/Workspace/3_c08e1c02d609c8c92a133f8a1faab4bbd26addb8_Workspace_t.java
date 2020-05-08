 package model;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
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
 import util.Pixmap;
 
 
 public class Workspace implements DataSource, ITurtle {
 
     private Scope myScope;
     private MethodScope myMethods;
     
     Map<Integer, Turtle> myTurtles = new HashMap<Integer, Turtle>();
     List<Turtle> myActiveTurtles = new ArrayList<Turtle>();
     Dimension myCanvasBounds;
     int myReturnValue;
     int myBackgroundImageIndex = 0;
     int myTurtleImageIndex = 0;
     int myPenColorIndex = 0;
     int myBackgroundColorIndex = 0;
     WorkspaceContainer myContainer;
 
     public Workspace (Dimension canvasBounds, WorkspaceContainer container) {
         myScope = new Scope();
         myMethods = new MethodScope();
         myContainer = container;
         
         Turtle firstTurtle = new Turtle(canvasBounds, myContainer.getTurtleImage(myTurtleImageIndex));
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
         myReturnValue = returnValue;
     }
 
     @Override
     public int getReturnValue () { 
         return myReturnValue;
     }
 
     @Override
     public int getTurtleHeading () {
         return (int) getLastActiveTurtle().getHeading();
     }
 
     @Override
     public Image getBackgroundImage () {
        //return myContainer.getBackgroundImage(myBackgroundImageIndex);
        return null;
     }
     
     @Override
     public void paint (Graphics2D pen) {
         for(Turtle turtle : myTurtles.values()){
             turtle.paint(pen);
         }
     }
 
     @Override
     public Color getBackgroundColor () {
         return myContainer.getColor(myBackgroundColorIndex);
     }
     
     @Override
     public Map<String, Integer> getUserVariables () {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Map<String, String> getUserFunctions () {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void toggleHighlighter () {
         for (Turtle turtle : getActiveTurtles()) {
             turtle.toggleTurtleHighlighter();
         }
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
         return getLastActiveTurtle().getTurtlePosition();
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
         return (int) getLastActiveTurtle().getHeading();
     }
     
     @Override
     public int getX () {
         return (int) getLastActiveTurtle().getX();
     }
     
     @Override
     public int getY () {
         return (int) getLastActiveTurtle().getY();
     }
 
     @Override
     public int setBackgroundColor (int colorIndex) {
         Color color = myContainer.getColor(colorIndex);
         if(color == null) {
             return 0;
         }
         myBackgroundColorIndex = colorIndex;
         return colorIndex;
     }
 
     @Override
     public int setBackgroundImage (int imageIndex) {
         if(myContainer.getBackgroundImage(imageIndex) == null) {
             return 0;
         }
         myBackgroundImageIndex = imageIndex;
         return myBackgroundImageIndex;
     }
 
     @Override
     public int setPenColor (int colorIndex) {
         Color color = myContainer.getColor(colorIndex);
         if(color == null) {
             return 0;
         }
         for(Turtle turtle : myActiveTurtles){
             turtle.setPenColor(color);
         }
         return colorIndex;
     }
 
     @Override
     public int setPenSize (int pixels) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int setShape (int shapeIndex) {
         Pixmap image = myContainer.getTurtleImage(shapeIndex);
         if(image == null) {
             return 0;
         }
         myTurtleImageIndex = shapeIndex;
         for(Turtle turtle : myActiveTurtles) {
             turtle.setView(image);
         }
         return myTurtleImageIndex;
     }
 
     @Override
     public int setPalette (int colorIndex, int red, int green, int blue) {
         myContainer.addColor(colorIndex, new Color(red, green, blue));
         return colorIndex;
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
                 Turtle newTurtle = new Turtle(myCanvasBounds, myContainer.getTurtleImage(myTurtleImageIndex));
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
 
    private Turtle getLastActiveTurtle () {
        return myActiveTurtles.get(myActiveTurtles.size() - 1);
    }
 }
