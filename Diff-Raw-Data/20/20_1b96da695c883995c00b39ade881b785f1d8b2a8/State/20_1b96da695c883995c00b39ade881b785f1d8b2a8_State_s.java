 package Source;
 
 import java.util.*;
 
 public class State {
 	int stateNum;
 	boolean isFinal;
 	
	private ArrayList<Integer> oChar = new ArrayList<Integer>(255);
 	private ArrayList<State> oEmpty = new ArrayList<State>();
 	
 	public void addepsilonEdge(State next) {
 		
 	}
 	
 	public void addCharEdge(char c, State next) {
 		
 	}
 	
 	public State() {
 		
 	}
 	
 	/*public boolean match(String s) {
 		
 		return matches(s, new ArrayList());
 	}
 	
 	private boolean matches(String s, ArrayList visited) {
 		
 		return false;
 	}
 	*/
 	
 	
 	
 	
 }
