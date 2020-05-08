 package model.pathfinding;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import model.geometrical.Position;
 import model.world.Tile;
 import model.world.World;
 
 public class EnemyPathfinder {
 	private List<PathfindingNode> openTileList;
 	private List<PathfindingNode> closedTileList;
 	private PathfindingNode goal;
 	private PathfindingNode currentNode;
 	private PathfindingNode[][] nodes;
 	private World world;
 	private boolean noWayFound = false;
 	
 	public EnemyPathfinder(World world){
 		this.world = world;
 		Tile[][] tempTiles = world.getTiles();
 		nodes = new PathfindingNode[tempTiles.length][tempTiles[0].length];
 		for(int i = 0; i<tempTiles.length; i++){
 			for(int j = 0; j<tempTiles[i].length; j++){
 				this.nodes[i][j] = new PathfindingNode(tempTiles[i][j], 0);//TODO samma som ovan
 			}
 		}
 	}
 	public List<PathfindingNode> findWay(Tile t, Tile goal, Tile[][] tiles){
 		noWayFound = false;
 		closedTileList = new ArrayList<PathfindingNode>();
 		openTileList = new ArrayList<PathfindingNode>();
 //		this.goal = new PathfindingNode(goal, 0);//TODO kolla ver andra "0"
 		this.goal = nodes[(int)goal.getX()][(int)goal.getY()];
 		openTileList.add(nodes[(int)t.getX()][(int)t.getY()]);
 		currentNode = openTileList.get(0);
 		currentNode.setParentNode(null);
 		
 		
 		while(((currentNode.getTile().getX() != this.goal.getTile().getX() ||
				currentNode.getTile().getY() != this.goal.getTile().getY())) && !noWayFound /*&& 
 				!(world.canMove(currentNode.getTile().getPosition(), 
						this.goal.getTile().getPosition()))*/){
 			findWay(t);
 		}
 		List<PathfindingNode> correctList = new ArrayList<PathfindingNode>();
//		correctList.add(currentNode);
//		this.goal.setParentNode(currentNode);
//		currentNode = this.goal;
 		System.out.println(noWayFound);
 		correctList.add(currentNode);
 		while(currentNode.getParentNode() != null){
 			correctList.add(currentNode.getParentNode());
 			currentNode = currentNode.getParentNode();
 		}
 		
 		List<PathfindingNode> correctListInverted = new ArrayList<PathfindingNode>();
 		for(int i = correctList.size()-1; i>-1; i--){
 			correctListInverted.add(correctList.get(i));
 		}
 		return correctListInverted;
 	}
 	private void findWay(Tile t){
 		if(openTileList.size() == 0){
 			noWayFound = true;
 			return;
 		}
 		PathfindingNode lowF = openTileList.get(0);
 		for(PathfindingNode node : openTileList){
 			if(currentNode != null){
 				node.setDistanceToGoal(getDistance(node.getTile(), goal.getTile()));
 			}
 			if(node.getPathfindingDistance() < lowF.getPathfindingDistance() && 
 					node != currentNode){
 				lowF = node;
 			}
 		}
 		
 		currentNode = lowF;
 		closedTileList.add(currentNode);
 		openTileList.remove(lowF);
 		openTileList.addAll(surroundingTiles(currentNode));
 	}
 	private List<PathfindingNode> surroundingTiles(PathfindingNode t){
 		List<PathfindingNode> surroundingTiles = new ArrayList<PathfindingNode>();
 		for(float x = currentNode.getTile().getX() - 1; x < currentNode.getTile().getX() + 2; x++){
 			for(float y = currentNode.getTile().getY() - 1; y < currentNode.getTile().getY() + 2; y++){
 				if(!(closedTileList.contains(nodes[(int)x][(int)y])) && 
 						world.canMove(currentNode.getTile().getPosition(), nodes[(int)x][(int)y].getTile().getPosition())){
 					if((openTileList.contains(nodes[(int)x][(int)y]))){
 						if(world.canMove(currentNode.getTile().getPosition(), nodes[(int)x][(int)y].getTile().getPosition())
 								&& currentNode.getDistanceFromStart()+
 								getDistance(currentNode.getTile(), nodes[(int)x][(int)y].getTile()) < nodes[(int)x][(int)y].getDistanceFromStart()){
 							nodes[(int)x][(int)y].setParentNode(currentNode);
 							nodes[(int)x][(int)y].setDistanceFromParent(getDistance(currentNode.getTile(), nodes[(int)x][(int)y].getTile()));
 						}
 					}else{
 						nodes[(int)x][(int)y].setParentNode(currentNode);
 						openTileList.add(nodes[(int)x][(int)y]);
 					}
 				}
 			}
 		}
 		return surroundingTiles;
 	}
 	
 	
 	private float getDistance(Tile t1, Tile t2){
 		float dx = Math.abs(t1.getX() - t2.getX());
 		float dy = Math.abs(t1.getY() - t2.getY());
 		return (float)Math.sqrt(dx*dx+dy*dy);
 	}
 	
 	
 }
