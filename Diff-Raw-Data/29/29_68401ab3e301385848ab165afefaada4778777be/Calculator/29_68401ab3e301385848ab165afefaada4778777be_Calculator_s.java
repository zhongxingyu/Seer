 package roborally.utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import roborally.basics.Energy;
 import roborally.basics.Orientation;
 import roborally.basics.Position;
 import roborally.model.Board;
 import roborally.model.Node;
 import roborally.model.Robot;
 
 public class Calculator {
 	
 	//TODO: documentatie en annotations
 
 	/**
 	 * Deze Methode gaat de manhattandistance van punt A met pos pos1 tot punt B met pos pos2 berekenen.
 	 * 
 	 * @param 	pos1
 	 * 
 	 * @param 	pos2
 	 * 
 	 * @return	
 	 * 
 	 */
 	public static long calculateManhattan(Position pos1, Position pos2){
 		return (Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()));
 	} 
 	
 	
 	public static HashMap<Position,Node> aStarNextTo(Robot a, Position pos){
 		//hier gaat de robot uiteindelijk naast de positie moeten uitkomen
 	
 		
 		HashMap<Position,Node> openSet = new HashMap<Position,Node>(); 
 		// de experimentele posities die nog gevalueerd moeten/kunnen worden
 		Node startNode = new Node(a.getPosition(), a.getBoard(), new Energy(0) , 
 				getHCost(a.getPosition(), a.getOrientation(),pos, a),a.getOrientation(), null);
 		
 		openSet.put(a.getPosition(), startNode);
 		// de startPositie aan de open list toevoegen
 		HashMap<Position,Node> closedSet = new HashMap<Position, Node>(); 
 		// de lijst met al gevalueerde posities
 		
 		/*Position goal = getBestPosToMoveTo(a, pos);*/
 		
 		while (!openSet.isEmpty()){
 			Node currentNode = getMinimalFNode(openSet);
 			if (pos.getNeighbours(a.getBoard()).contains(currentNode.getPosition())){
 				openSet.remove(currentNode.getPosition());
 		        closedSet.put(currentNode.getPosition(), currentNode);
 				return closedSet;
 			}
 			openSet.remove(currentNode.getPosition());
 	        closedSet.put(currentNode.getPosition(), currentNode);
 	        
 	        ArrayList<Position> neighbours = currentNode.getPosition().getNeighbours(a.getBoard());
 	        for (Position neighbour : neighbours){
 	        	if (closedSet.containsKey(neighbour))
 	        		continue;
 	        	
 	        	Energy tentativeGScore = getGCost(currentNode,neighbour, a);
 	        	boolean tentativeIsBetter = false;
 	        	
 	        	if (!openSet.containsKey(neighbour)){
 	        		openSet.put(neighbour,new Node(neighbour,currentNode.getBoard(),getGCost(currentNode, neighbour, a),getHCost(neighbour, getNodeOrientation(currentNode, neighbour),pos, a),
 	        				getNodeOrientation(currentNode, neighbour),currentNode));
 	        		tentativeIsBetter = true;
 	        	}
 	        	else if (tentativeGScore.getEnergy() < openSet.get(neighbour).getGCost().getEnergy())
 	        		tentativeIsBetter = true;
 	        	else
 	        		tentativeIsBetter = false;	        		
 	        	
 	        	if (tentativeIsBetter == true){
 	        		openSet.get(neighbour).setParent(currentNode);
 	        		openSet.get(neighbour).setGCost(tentativeGScore);
 	        	}
 	        }
 			
 		}
 		return closedSet;
 
 	}
 	
 	/*private static Position getBestPosToMoveTo(Robot a, Position pos) {
 		// deze methode moet het optimale doel teruggeven juist naast de pos
 		ArrayList<Position> neighbours = a.getPosition().getNeighbours(a.getBoard());
 		;
 	}*/
 
 
 	public static HashMap<Position,Node> aStarOnTo(Robot a, Position pos){
 		//deze gaat direct naar de positie die opgegeven wordt
 		
 		HashMap<Position,Node> open = new HashMap<Position,Node>(); 
 		// de experimentele posities die nog gevalueerd moeten/kunnen worden
 		Node startNode = new Node(a.getPosition(), a.getBoard(), new Energy(0) , 
 				getHCost(a.getPosition(), a.getOrientation(),pos, a),a.getOrientation(), null);
 		
 		open.put(a.getPosition(), startNode);
 		// de startPositie aan de open list toevoegen
 		HashMap<Position,Node> closed = new HashMap<Position, Node>(); 
 		// de lijst met al gevalueerde posities
 		Board board = a.getBoard();
 		
 		while ( !open.isEmpty()){
 			Node currentNode = getMinimalFNode(open);
 			if ((pos.getX() == currentNode.getPosition().getX()) && (pos.getY() == currentNode.getPosition().getY())){
 				open.remove(currentNode.getPosition());
 		        closed.put(currentNode.getPosition(), currentNode);
 				return closed;
 			}
 			
 			open.remove(currentNode.getPosition());
 	        closed.put(currentNode.getPosition(), currentNode);
 	        
 	        ArrayList<Position> neighbours = currentNode.getPosition().getNeighbours(a.getBoard());
 	        for (Position neighbour : neighbours){
 	        	int gCostNeighbour = getGCost(currentNode, neighbour, a ).getEnergy();
 	        	if (closed.containsKey(neighbour)){
 	        		if(closed.get(neighbour).getGCost().getEnergy() > gCostNeighbour){
 	        			closed.get(neighbour).setGCost(new Energy(gCostNeighbour));
 	        			closed.get(neighbour).setParent(currentNode);
 	        		}
 	         		continue;
 	        	}
 	        	else if ((open.containsKey(neighbour)) && (open.get(neighbour).getGCost().getEnergy() > gCostNeighbour)){
 	        		open.get(neighbour).setGCost(new Energy(gCostNeighbour));
         			open.get(neighbour).setParent(currentNode);
 	        	}
 	        	else{
 	        		if (board.isPlacableOnPosition(neighbour)){
 	        			open.put(neighbour, new Node(neighbour,board, new Energy(gCostNeighbour),
 	        					getHCost(neighbour,getNodeOrientation(currentNode,neighbour) , pos, a),
 	        					getNodeOrientation(currentNode,neighbour),currentNode));
 	        		}
 	        	}
 	        }
 			
 		}
 		return closed;
 
 	}
 	
 	private static Orientation getNodeOrientation(Node currentNode, Position pos) {
 		Position previousPosition = currentNode.getPosition();
 		if (previousPosition.getX() == pos.getX()){
 			if (previousPosition.getY() > pos.getY())
 				return Orientation.DOWN;
 			return Orientation.UP;
 		}
 		if (previousPosition.getX() > pos.getX())
 			return Orientation.LEFT;
 		return Orientation.RIGHT;
 	}
 
 	/**
 	 * Deze methode gaat de heuristiek kost naar een gegeven positie vanuit de meegegeven node berekenen.
 	 * 
 	 * @param 	position
 	 * 			
 	 * @param 	orientation
 	 * 			
 	 * @param 	pos
 	 * 			
 	 * @param 	robot
 	 * 			
 	 * @return	Energy
 	 * 		
 	 * 			
 	 */
 	private static Energy getHCost(Position position, Orientation orientation, Position pos, Robot robot) {
 		//de manhattan kost om met de robot van de beginpositie tot de eindpos te geraken.
 		Energy manHattanCost = new Energy(Robot.moveCost(robot).getEnergy() * (int) calculateManhattan(position, pos));
 		Energy turnCost = new Energy(Robot.TURN_COST.getEnergy()*getTurns(new Node(position,orientation,robot.getBoard()),pos));
 		return Energy.energySum(manHattanCost, turnCost);
 	}
 
 	/**
 	 * 
 	 * @param currentNode
 	 * @param pos
 	 * @param robot
 	 * @return
 	 */
 	private static Energy getGCost(Node currentNode, Position pos, Robot robot) {
 		return Energy.energySum(Energy.energySum(currentNode.getGCost(), Robot.moveCost(robot)),
 				new Energy(Robot.TURN_COST.getEnergy()*getTurns(currentNode, pos)));
 	}
 	/**
 	 * methode voor het aantal turns terug te geven om van een node met orientatie m naar een nabijgelegen node te 
 	 * bewegen (vlak naast)
 	 *  
 	 * @param node
 	 * @param pos 
 	 * @return
 	 */
 	private static int getTurns(Node node, Position pos){
 		int result = 0;
 		if(node.getPosition().getX() == pos.getX() && node.getPosition().getY() == pos.getY())
 			return result;
 		/*
 		 * In dit gedeelte kijken we de verhouding van de huidige robot met zijn bestemming na. Hier worden alle gevallen overlopen.
 		 * Om dit visueel voor te stellen staan er letters die de posities voorstellen. De hoekpunten zijn in wijzerszin A, B, C en D.
 		 * Vervolgens worden de middens van elke rand voorgesteld met E, F, G en H.
 		 */
 		if(pos.getX() == node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// E
 			switch(node.getOrientation()){
 			case RIGHT:
 			case LEFT:
 				result = 1;
 				break;
 			case DOWN:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() == node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// G
 			switch(node.getOrientation()){
 			case LEFT:
 			case RIGHT:
 				result = 1;
 				break;
 			case UP:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() == node.getPosition().getY()){
 			// F
 			switch(node.getOrientation()){
 			case DOWN:
 			case UP:
 				result = 1;
 				break;
 			case LEFT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// B
 			switch(node.getOrientation()){
 			case RIGHT:
 			case UP:
 				result = 1;
 				break;
 			case DOWN:
 			case LEFT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// C
 			switch(node.getOrientation()){
 			case DOWN:
 			case RIGHT:
 				result = 1;
 				break;
 			case LEFT:
 			case UP:
 				result = 2;	
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() == node.getPosition().getY()){
 			// H
 			switch(node.getOrientation()){
 			case UP:
 			case DOWN:
 				result = 1;
 				break;
 			case RIGHT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// A
 			switch(node.getOrientation()){
 			case UP:
 			case LEFT:
 				result = 1;
 				break;
 			case RIGHT:
 			case DOWN:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// D
 			switch(node.getOrientation()){
 			case LEFT:
 			case DOWN:
 				result = 1;
 				break;
 			case UP:
 			case RIGHT:
 				result = 2;
 				break;
 			}
 		}
 		return result;
 	}
 	
 	public static Node getMinimalFNode(HashMap<Position, Node> map){
 		Collection<Node> c = map.values();
 		Iterator<Node> itr = c.iterator();
 		Node minimalNode = itr.next();
 		for (Node node : c){
 			if (node.getFCost().getEnergy() < minimalNode.getFCost().getEnergy())
 				minimalNode = node;
 		}
 		return minimalNode;
 	}
 	
 	/**
 	 * Deze methode geeft de volgende positie weer met de huidige orintatie.
 	 * 
 	 * @param	pos
 	 * 			Positie van waaruit vertrokken moet worden.
 	 * 
 	 * @param	or
 	 * 			De huidige orintatie.
 	 * 
 	 * @return	De volgende positie met de huidige orintatie
 	 * 			|if(or.equals(Orientation.UP))
 	 *			|	new Position(pos.getX(), pos.getY() - 1)
 	 *			|if(or.equals(Orientation.RIGHT))
 	 *			|	new Position(pos.getX() + 1, pos.getY())
 	 *			|if(or.equals(Orientation.DOWN))
 	 *			|	new Position(pos.getX(), pos.getY() + 1);
 	 *			|if(or.equals(Orientation.LEFT))
 	 *			|	new Position(pos.getX() - 1, pos.getY())
 	 *			|null
 	 *
 	 * @throws	IllegalStateException
 	 * 			Er bestaat geen verdere positie meer met deze orintatie.
 	 */
 	public static Position getNextPosition(Position pos, Orientation or) throws IllegalStateException{
 		Position result = null;
 		if(or.equals(Orientation.UP)){
 			try {
 				result = new Position(pos.getX(), pos.getY() - 1);
 			} catch (IllegalArgumentException e) {
 				throw new IllegalStateException("Er bestaat geen verdere positie meer met deze orintatie.");
 			}
 		}else if(or.equals(Orientation.RIGHT)){
 			try {
 				result = new Position(pos.getX() + 1, pos.getY());
 			} catch (IllegalArgumentException e) {
 				throw new IllegalStateException("Er bestaat geen verdere positie meer met deze orintatie.");
 			}
 		}else if(or.equals(Orientation.DOWN)){
 			try {
 				result = new Position(pos.getX(), pos.getY() + 1);
 			} catch (IllegalArgumentException e) {
 				throw new IllegalStateException("Er bestaat geen verdere positie meer met deze orintatie.");
 			}
 		}else if(or.equals(Orientation.LEFT)){
 			try {
 				result = new Position(pos.getX() - 1, pos.getY());
 			} catch (IllegalArgumentException e) {
 				throw new IllegalStateException("Er bestaat geen verdere positie meer met deze orintatie.");
 			}
 		}
 		return result;
 	}
 	
	public static List<Object> getKeysFromValue(Map<?, ?> hashmap, Object value){
	    List <Object>list = new ArrayList<Object>();
	    for(Object o:hashmap.keySet()){
	        if(hashmap.get(o).equals(value)) {
	            list.add(o);
 	        }
 	    }
 	    return list;
 	  }
 	
 }
