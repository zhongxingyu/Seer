 import java.util.HashSet;
 import java.util.Stack;
 import java.util.Vector;
 
 public class Player {
 	private Stack<Board> stack = new Stack<Board>();
 	private HashSet<Integer> visited = new HashSet<Integer>();
 
 	Player () {
 
 	}
 	public String dfs(Board startState) {
 		stack.push(startState);
 
 		while (!stack.isEmpty()) {
 			Board currentState = stack.peek();
 
 			if (currentState.isEOG())
 				return currentState.path.toString();
 
 			Vector<Direction> moves = currentState.findPossibleMoves();
 			System.out.println(moves.size());
 
 			if (moves.size() != 0) {
 				for (Direction d : moves) {
 					Board nextBoard = new Board(currentState, d);
 					if(!visited(nextBoard.cells.hashCode())){
 						nextBoard.addDirectionToPath(d);
 						stack.push(nextBoard);
 						System.out.println(stack.size());
 					}
 				}
 			} else {
 				stack.pop();
 			}
 		}
 
 		return null;
 	}
 
 	
 	/*
 	 * Checks if a state has been visited before.
 	 * @param: hashCode of Board.cells.hashCode()
 	 * returns false iff not visited else true
 	 * 
 	 */
 	private boolean visited(int hashCode){
 		//.add returns true if hashCode was added (meaning hashCode haven't been added before)
 		if(visited.add(hashCode))
 			return false;
 		else
 			return true;
 	}
 	
 	//	Push the root node onto a stack.
 	//	Pop a node from the stack and examine it.
 	//	If the element sought is found in this node, quit the search and return a result.
 	//	Otherwise push all its successors (child nodes) that have not yet been discovered onto the stack.
	//	If the stack is empty, every node in the tree has been examined  quit the search and return "not found".
 	//	If the stack is not empty, repeat from Step 2.
 }
