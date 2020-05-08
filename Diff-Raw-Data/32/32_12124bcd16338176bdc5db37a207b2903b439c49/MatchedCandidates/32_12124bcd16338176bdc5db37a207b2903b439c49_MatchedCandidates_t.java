 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Actually this class is used to describe the structure of a temporary result during subgraph querying
  * @author Administrator
  *
  */
 public class MatchedCandidates {
 	
 	HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();
 	HashMap<String, List<String>> paths = new HashMap<String, List<String>>();
 	/**
 	 * Check whether the a component can join with the current candidates 
 	 * @param list
 	 * @param ids
 	 * @return
 	 */
 	public boolean CanJoin(ArrayList<Integer> list, Integer[] ids) {
 		for (int i = 0; i < ids.length; i++) {
 			if (mapping.containsKey(ids[i])) {
 				if (!list.get(i).equals(mapping.get(ids[i])))
 					return false;
 			}
			else {
				if (mapping.containsValue(list.get(i)))
					return false;
			}
		}				
 		return true;
 	}
 	
 	/**
 	 * Join a component with the graph piece
 	 * @param list
 	 * @param ids
 	 */
 	public void Join(ArrayList<Integer> list, Integer[] ids) {
 		for (int i = 0; i < ids.length; i++) {
 			if (!mapping.containsKey(ids[i])) {
 				mapping.put(ids[i], list.get(i));
 			}
 		}
 	}
 	
 	/**
 	 * The constructor
 	 * @param list
 	 * @param ids
 	 */
 	public MatchedCandidates(ArrayList<Integer> list, Integer[] ids) {
 		for (int i = 0; i < ids.length; i++) {
 			mapping.put(ids[i], list.get(i));
 		}
 	}
 
 	/**
 	 * Copy constructor
 	 * @param another
 	 */
 	public MatchedCandidates(MatchedCandidates another) {
 		for (Integer i : another.mapping.keySet()) {
 			mapping.put(i, another.mapping.get(i));
 		}
 	}
 	
 	public void Print() {
 		for (Integer i: mapping.keySet()) {
 			System.out.println(i + " " + mapping.get(i));
 		}
 		
 		if (paths.keySet().size() > 0) {
 			System.out.println("Paths between ");
 			for (String k : paths.keySet()) {
 				System.out.println(k);
 				for (String l : paths.get(k))
 					System.out.print(l + " ");
 				System.out.println();
 			}			
 		}			
 		
 		System.out.println("<----------------->");
 	}
 	
 	/**
 	 * Combine the two matching candidates
 	 * @param another
 	 */
 	public void Combine(MatchedCandidates another) {
 		for (Integer k : another.mapping.keySet()) {
 			this.mapping.put(k, another.mapping.get(k));
 		}
 		
 		for (String k : another.paths.keySet()) {
 			this.paths.put(k, another.paths.get(k));
 		}
 	}
 }
