 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 
 public class AStarTile extends AStar<Tile> {
 	private Tile goal;
 
 	public AStarTile(Tile goal) {
 		super();
 		this.goal = goal;
 	}
 
 	@Override
 	protected boolean isGoal(Tile node) {
 		return Util.samePosition(goal, node);
 	}
 
 	@Override
 	protected Double g(Tile from, Tile to) {
		if(MyBot.ants.getMyHills().contains(to)) {
			return Double.MAX_VALUE;
		}
 		return (double)MyBot.ants.getDistance(from, to);
 	}
 
 	@Override
 	protected Double h(Tile from, Tile to) {
 		return (double)MyBot.ants.getDistance(to, goal);
 	}
 
 	@Override
 	protected List<Tile> generateSuccessors(Tile node) {
 		List<Aim> allDirections = Arrays.asList(Aim.values());
 		List<Tile> canMove = new ArrayList<Tile>();
  		for(Aim direction : allDirections){
 			if(Util.canMoveDirection(node, direction)){
 				canMove.add(MyBot.ants.getTile(node, direction));
 			}
  		}
 		return canMove;
 	}
 
 }
