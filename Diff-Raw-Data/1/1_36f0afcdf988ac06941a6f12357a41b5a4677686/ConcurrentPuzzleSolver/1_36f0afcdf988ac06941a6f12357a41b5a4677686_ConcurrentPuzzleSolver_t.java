 package framework;
 
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 /*
  
  * Generic sequential puzzle solver. 
  * Field puzzle holds the concrete puzzle.
  * Field seen contains the positions seen so far.
 
 */
 
 /*
  * 
  * Essentially, the solver performs a depth-first search over the search space.
  * Method solve() starts the search by initializing the list of nodes with a 
  * node that contains the initial position, and a null previous node.
  * Method search(Node) examines the current node. If seen contains the 
  * position of the node, search returns a null list (i.e., the search 
  * stops at this branch). Otherwise, the search examines if the puzzle 
  * has reached the goal position, in which case it returns the solution in 
  * the form of a list of positions. If the puzzle has not reached the goal 
  * position yet, the search continues trying out each legal position that 
  * is a descendant of the current one. 
  * 
 */
 
 public class ConcurrentPuzzleSolver {
 	
 	class MyThread implements Runnable{
 		Node n;
 		LinkedList l;
 		public MyThread(Node node, LinkedList L){
 			n = node;
 		 	l = L;
 		}
 		public void run() {	
 			
 			l = search(n);
 			if (l != null) {
 			}
 			
 			
 		}
 		
 
 		
 		
 	}
 	Executor e = Executors.newFixedThreadPool(3);
 	//put a lock on it
 	
 	private final Puzzle puzzle;
 	private final ConcurrentHashMap<Puzzle, Node> seen = new ConcurrentHashMap<Puzzle, Node>();
 	
 	
 	public ConcurrentPuzzleSolver(Puzzle puzzle) {
 		this.puzzle = puzzle;
 	}
 	
 	public LinkedList solve() {
 		puzzle.initialPosition();
 		return search(new Node(puzzle, null));
 	}
 	
 	
 	public LinkedList search(Node node) {
 		LinkedList l = new LinkedList();
 		if (seen.putIfAbsent(node.pos, node) == null) {
 			if (node.pos.isGoal()) { 
 				return node.asPositionList();
 			}
 			
 			for (Object o : node.pos.legalMoves(node)) { 
 				LinkedList i = new LinkedList();
 				l.addLast(i);
 				int index = l.size()-1;
 				
 				Puzzle puzzle = (Puzzle) o;
 				Node child = new Node(puzzle, node);
 				
 				MyThread task = new MyThread(child, (LinkedList) l.get(index));
 				e.execute(task);
 				if (!l.isEmpty()) {
 					System.out.println("found solution....suposedly.");
 					return l;
 				}
 				
 				
 					
 			}
 		}
 		return null;
 	}
 	
 }
