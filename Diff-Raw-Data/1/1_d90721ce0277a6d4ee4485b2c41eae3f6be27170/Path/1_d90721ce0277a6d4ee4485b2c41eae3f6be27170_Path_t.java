 package stateMachineAgent;
 
 import java.util.ArrayList;
 
 /**
  * Used by the state machine agent to store a sequence of steps in the state
  * machine environment.  This is primarily used to store the best known path
  * from the init state to the goal state.
  */
 
 public class Path {
     //list of steps taken along the path
 	private ArrayList<Character> path;
 
 	//Debugging Variable
 	private boolean debug = true;
 
 	/**
 	 * initializes a path with an array of character 
      *
 	 * @param generated
 	 */
 	public Path (ArrayList<Character> generated) {
 		path = new ArrayList<Character>();
 		for (int i = 0; i < generated.size(); i++) {
 			path.add(generated.get(i));
 		}
 	}
 
     /**
      * creates a copy of this object
      */
 	public Path copy() {
 		ArrayList<Character> createdCopy = new ArrayList<Character>();
 		for (int i = 0; i < path.size(); i++) {
 			createdCopy.add(path.get(i));
 		}
 		return new Path(createdCopy);
 	}
 
 	public int size() {
 		return path.size();
 	}
 
 	public char get(int index) {
 		return path.get(index);
 	}
 
 	public String toString() {
 		String result = "";
 		for (int i = 0; i < path.size(); i++) {
 			result += path.get(i);
 		}
 		return result;
 	}
 
 	public void printpath() {
 		if (debug) {
 			System.out.println("path: " + toString());
 		}
 	}
 
 	public void remove(int index) {
 		path.remove(index);
 	}
 
 	public void add(int index, char toAdd) {
 		path.add(index, toAdd);
 	}
 }
