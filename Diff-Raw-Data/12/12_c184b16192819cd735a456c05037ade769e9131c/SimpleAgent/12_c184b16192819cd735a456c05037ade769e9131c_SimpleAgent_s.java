 package TicTacToe;
 
 public class SimpleAgent extends Agent {
 	
 	private int nodesVisited;
 	
 	@Override
 	public Position getNextMove() {
 		nodesVisited = 0;
		for(int i = 0; i < state.getHeight(); i++) {
			for(int j = 0; j < state.getWidth(); j++) {
 				Position pos = new Position(j, i);
 				nodesVisited++;
 				if(state.getTypeAt(pos) == null) {
 					return pos;
 				}
 			}
 		}
 		//should never happen
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return "Simple";
 	}
 	
 	@Override
 	public int getNodesVisited() {
 		return nodesVisited;
 	}
 
 }
