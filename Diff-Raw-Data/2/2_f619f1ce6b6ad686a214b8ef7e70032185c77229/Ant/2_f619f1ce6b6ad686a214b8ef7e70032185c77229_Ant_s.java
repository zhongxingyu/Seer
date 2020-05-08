 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 
 public class Ant {
 	static final Random r = new Random(System.currentTimeMillis());
 	Ants ants;
 	Tile tile;
 	Tile destination;
 	Tile order;
 	Aim preferredDirection;
 	Aim currentDirection;
 	int turnCount = 0;
 	int turnBias = 0;
 	int moveCount = 0;
 	Tile preferredTarget;
 	
 	public Ant(Ants ants, Tile tile) {
 		super();
 		this.ants = ants;
 		this.tile = tile;
 		int randomDirection = r.nextInt(Aim.values().length);
 		preferredDirection = Aim.values()[randomDirection];
 		turnBias = r.nextInt(2);
 	}
 	
 	private void pickPreferredTarget() {
 		this.preferredTarget = new Tile(r.nextInt(ants.getRows()), r.nextInt(ants.getCols()));
 		if (getPathTo(this.preferredTarget) == null || ants.getIlk(this.preferredTarget).equals(Ilk.WATER)) {
 			pickPreferredTarget();
 		}
 	}
 	
 	public Tile closestFood() {
 		Set<Tile> visibleFood = ants.getFoodTiles();
 		Tile closestFood = null;
 		int distance = 0;
 		for (Tile food : visibleFood) {
 			int foodDistance = ants.getDistance(this.tile, food);
 			if (foodDistance >= ants.getViewRadius2()) {
 				continue;
 			}
 			PathNode path = getPathTo(food);
 			if (path != null && closestFood == null || path.totalCost < distance) {
 				closestFood = food;
 				distance = path.totalCost;
 			}
 		}
 		return closestFood;
 	}
 	
 	@Override
 	public String toString() {
 		return this.tile.toString();
 	}
 	
 	
 	public void setDestination(Tile destination) {
 		this.destination = destination;
 	}
 	
 	public static Comparator<Ant> distanceComparator(final Ants ants, final Tile target) {
 		
 		return new Comparator<Ant>() {
 
 			@Override
 			public int compare(Ant o1, Ant o2) {
 				int d1 = ants.getDistance(o1.tile, target);
 				int d2 = ants.getDistance(o2.tile, target);
 				if (d1 > d2) return 1;
 				if (d2 > d1) return -1;
 				return 0;
 			}
 		};
 		
 	}
 
 
 	public static Comparator<Ant> pathComparator(final Ants ants, final Tile target) {
 		
 		return new Comparator<Ant>() {
 
 			@Override
 			public int compare(Ant o1, Ant o2) {
 				PathNode p1 = o1.getPathTo(target);
 				PathNode p2 = o2.getPathTo(target);
 				if (p1 == null && p2 == null) {
 					return 0;
 				}
 				if (p1 != null && p2 == null) {
 					return 1;
 				}
 				if (p1 == null && p2 != null) {
 					return -1;
 				}
 				int d1 = p1.totalCost;
 				int d2 = p2.totalCost;
 				if (d1 > d2) return 1;
 				if (d2 > d1) return -1;
 				return 0;
 			}
 		};
 		
 	}
 
 	public boolean moveToPreferredTile(HashMap<Tile, Tile> orders) {
		if (ants.getIlk(preferredTarget).equals(Ilk.WATER) || ants.getDistance(this.tile, this.preferredTarget) <= ants.getViewRadius2()) {
 			pickPreferredTarget();
 		}
 		this.destination = preferredTarget;
 		return this.move(orders);
 	}
 	
 	public boolean moveInPreferredDirection(HashMap<Tile, Tile> orders) {
 		//System.err.println("Tile " + tile + ", Prefer " + preferredDirection + ", current " + currentDirection + ", turns " + turnCount);
 		this.destination = null;
 		if (currentDirection == null) {
 			currentDirection = preferredDirection;
 		}
 		if (currentDirection.equals(preferredDirection) && turnCount == 0) {
 			if (move(orders, currentDirection)) {
 				return true;
 			} else {
 				for (int i = 0; i < 3; i++) {
 					if (turnBias == 0) {
 						currentDirection = currentDirection.rightTurn();
 					} else {
 						currentDirection = currentDirection.leftTurn();
 					}
 					turnCount++;
 					if (move(orders, currentDirection)) {
 						return true;
 					}
 				}
 			}
 		} else {
 			//try to turn left to get back to our preferred direction
 			if (turnBias == 0) {
 				currentDirection = currentDirection.leftTurn();
 			} else {
 				currentDirection = currentDirection.rightTurn();
 			}
 			turnCount--;
 			if (move(orders, currentDirection)) {
 				return true;
 			} else {
 				for (int i = 0; i < 3; i++) {
 					if (turnBias == 0) {
 						currentDirection = currentDirection.rightTurn();
 					} else {
 						currentDirection = currentDirection.leftTurn();
 					}
 					turnCount++;
 					if (move(orders, currentDirection)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
     public boolean move(HashMap<Tile, Tile> orders, Aim direction) {
         // Track all moves, prevent collisions
     	Tile newLoc = ants.getTile(this.tile, direction);
     	//System.err.println("Attempting to move tile " + this.tile);
     	if (!ants.getIlk(newLoc).isUnoccupied()) {
     		if (ants.getIlk(newLoc).equals(Ilk.MY_ANT)) {
     			direction = direction.rightTurn();
     		}
     	}
         if ((ants.getMyAnts().size() == 1 || !ants.getMyHills().contains(newLoc)) && ants.getIlk(newLoc).isPassable() && !orders.containsValue(newLoc) && !orders.containsKey(this.tile)) {
             ants.issueOrder(this.tile, direction);
             orders.put(this.tile, newLoc);
             this.order = newLoc;
             return true;
         } else {
             return false;
         }
     }
 
     PathNode getPathTo(Tile target) {
     	List<PathNode> open = new ArrayList<PathNode>();
     	HashMap<Tile, PathNode> openMap = new HashMap<Tile, PathNode>();
     	PathNode start = new PathNode(null, tile, 0, ants.getDistance(tile, target));
     	open.add(start);
     	openMap.put(start.tile, start);
     	
     	HashMap<Tile, PathNode> closed = new HashMap<Tile, PathNode>();
     	
     	while(open.size() > 0) {
     		Collections.sort(open);
     		PathNode cheapest = open.get(0);
     		if (cheapest.tile.equals(target)) {
     	    	return cheapest;    			
     		}
     		
     		open.remove(0);
     		openMap.remove(cheapest.tile);
     		closed.put(cheapest.tile, cheapest);
     		List<Tile> neighbors = getNeighbors(cheapest.tile);
     		for (Tile neighbor : neighbors) {
     			PathNode neighborNode = closed.get(neighbor); 
     			boolean inClosed = (neighborNode != null);
 	   			if (neighborNode != null && cheapest.steps < neighborNode.steps - 1) {
     				neighborNode.steps = cheapest.steps + 1;
     				neighborNode.parent = cheapest;
     			} else if (!inClosed) {
     				neighborNode = openMap.get(neighbor);
     	   			if (neighborNode != null && cheapest.steps < neighborNode.steps - 1) {
         				neighborNode.steps = cheapest.steps + 1;
         				neighborNode.parent = cheapest;
         			} else if (neighborNode == null){
         				PathNode next = new PathNode(cheapest, neighbor, cheapest.steps + 1, ants.getDistance(neighbor, target));
         				open.add(next);
         				openMap.put(next.tile, next);
         			}   				
     			}
     		}
     	}
     	return null;
     	
     }
     
     public boolean move(HashMap<Tile, Tile> orders) {
     	//System.err.println("Moving " + tile + " toward " + destination);
     	PathNode path = getPathTo(this.destination);
     	if (path == null) {
     		this.destination = null;
     		return false;
     	}
 
     	List<Aim> directions = ants.getDirections(this.tile, path.getFirstTileInPath());
     	for (Aim dir : directions) {
         	if (move(orders, dir)) {
             	return true;
         	}
     	}
     	this.destination = null;
     	return false;    			
     }
 
     private List<Tile> getNeighbors(Tile tile) {
     	ArrayList<Tile> neighbors = new ArrayList<Tile>();
     	for (Aim aim : Aim.values()) {
     		Tile neighbor = ants.getTile(tile, aim);
     		if (ants.getIlk(neighbor).isPassable() && !ants.getMyHills().contains(neighbor)) {
     			neighbors.add(neighbor);
     		}
     	}
     	return neighbors;
     }
 
 }
