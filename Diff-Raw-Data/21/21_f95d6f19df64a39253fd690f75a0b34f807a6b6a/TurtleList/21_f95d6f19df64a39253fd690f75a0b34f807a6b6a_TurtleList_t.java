 package backEnd;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 
 public class TurtleList extends Observable implements Observer {
    
     private List<Turtle> myTurtles;
     private Set<Integer> myActiveIDs;
     private Observer myObserver;
 
     public TurtleList() {
         myTurtles = new ArrayList<Turtle>();
         myActiveIDs = new HashSet<Integer>();
     }
 
     public TurtleList(Observer observer) {
         this();
         myObserver = observer;
         addObserver(myObserver);
     }
 
     public TurtleList(TurtleList other) {
         myTurtles = cloneTurtles(other.myTurtles);
         myActiveIDs = new HashSet<Integer>(other.myActiveIDs);
     }
 
     @Override
     public void update(Observable arg0, Object arg1) {
         setChanged();
     }
 
     public void add(Turtle turtle) {
         myTurtles.add(turtle);
         myActiveIDs.add(turtle.getID());
         setChanged();
     }
 
     public void addNewTurtle(int x, int y) {
         Turtle turtle = new Turtle(this, myTurtles.size());
         turtle.setLocation(x, y);
         add(turtle);
 
     }
 
    public Turtle get (int id) {

         for(Turtle t : myTurtles){
             if (t.getID() == id){
                 return t; 
             }
         }
         return null;
     }
 
     public Turtle getLastActive () {
         return myTurtles.get((Integer)myActiveIDs.toArray()[0]);
     }
 
     public Set<Integer> getActiveIDs () {
         return myActiveIDs;
     }
 
     public void activate(int ID){
         myActiveIDs.add(ID);
     }
     public void disactivate(int ID){
         myActiveIDs.remove(ID);
     }
 
     public void disactivateAll(){
     	myActiveIDs.clear();
     }


     public int size() {
         return myTurtles.size();
     }
 

     public int activateEven(){
     	disactivateAll();
     	int ret = 0;
     	for(Turtle t: myTurtles){
     		if(t.getID()%2 == 0){
     			ret = t.getID();
     			activate(ret);
     		}
     	}
     	return ret;
     }
     
     public int activateOdd(){
     	disactivateAll();
     	int ret = 0;
     	for(Turtle t: myTurtles){
     		if(t.getID()%2 != 0){
     			ret = t.getID();
     			activate(ret);
     		}
     	}
     	return ret;
     }
     

     @Override
     protected void setChanged() {
         super.setChanged();
         notifyObservers();
     }
 
     private List<Turtle> cloneTurtles(List<Turtle> cloneFrom) {
         List<Turtle> cloneTo = new ArrayList<Turtle>();
         for (Turtle t : cloneFrom) {
             cloneTo.add(new Turtle(t));
         }
         return cloneTo;
     }
     
     public void setActive(Set<Integer> ids) {
         myActiveIDs = ids;
     }

 }
