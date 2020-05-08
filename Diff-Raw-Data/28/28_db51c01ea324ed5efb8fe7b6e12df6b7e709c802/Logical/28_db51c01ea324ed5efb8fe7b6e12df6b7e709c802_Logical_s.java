 package quoridor;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 public class Logical implements State {
 	protected Board setting;
 	protected Player currentTurn;
 	protected GenericMove nextMove;
 	public int score;
 	
 	public Logical(Board setting, Player currentTurn) {
 		this.setting = setting;
 		this.currentTurn = currentTurn;
 		nextMove = null;
 	}
 	
 	@Override
 	public Iterator<StateImpl> iterator() {
 		return new MoveGenerator(currentTurn, setting);
 	}
 
 	@Override
 	public GenericMove nextBestMove() {
 		return nextMove;
 	}	
 	
 	private class Node implements Comparable<Node>, Iterable<Node> {
 		protected Square square;
 		protected int cost;
 		Node parent;
 		
 		public Node(Square s, int n, Node parent) {
 			square = s;
 			cost = n;
 			this.parent = parent;
 		}
 		
 		public Square getSquare() {
 			return square;
 		}
 		
 		public int getCost() {
 			return cost;
 		}
 
 		public Node getParent() {
 			return this.parent;
 		}
 		
 		@Override
 		public int compareTo(Node o) {
 			return o.cost - cost;
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			return ((Node)o).square.equals(square);
 		}
 		
 		@Override
 		public int hashCode() {
 			return square.hashCode();
 		}
 
 		@Override
 		public Iterator<Node> iterator() {
 			return new NodeIterator(this, cost);
 		}
 		
 		private class NodeIterator implements Iterator<Node> {
 			List <Node> nextList = new LinkedList <Node>();
 			Iterator <Node> backer;
 			public NodeIterator(Node start, int n) {
 				for (int i = -2; i <= 2 ; i ++) {
 					for (int j = -2;j<=2;j++) {
 						if (i != 0 || j != 0) {
 							Board settingClone = new BoardImpl((BoardImpl)setting);
 							Player moverClone = new PlayerImpl(currentTurn);
 							settingClone.getPawn(moverClone, settingClone).setSquare(square);
 							MovePawn tentative = new MovePawnImpl(square.getCol() + j, square.getRow() + i, moverClone, settingClone );
 							if (tentative.isValid()) {
 								Node next = new Node(new SquareImpl(square.getCol()+j, square.getRow() + i), n + 1, start);
 								nextList.add(next);
 							}
 						}
 					}
 				}				
 				backer = nextList.iterator();		
 			}
 			
 			@Override
 			public boolean hasNext() {
 				return backer.hasNext();
 			}
 
 			@Override
 			public Node next() {
 				return backer.next();
 			}
 
 			@Override
 			public void remove() {
 			}
 		}	
 	}
 	
 	public Node pathLength(Player player) {	
 		Queue <Node> toVisit = new LinkedList <Node> ();
 		Node current = new Node(setting.getPawn(player, setting).getSquare(), 0, null);
 		toVisit.add(current);
 		HashSet <Node> seen = new HashSet<Node>();
 		
 		while (current.getSquare().getRow() != destRow(player.goalEnd()) ) {
 			current = toVisit.remove();
 			seen.add(current);
 			for (Node n : current) {
 				if (!seen.contains(n)) {
 					toVisit.add(n);
 				}
 			}
 		}
 		
 		return current;
 	}
 	
 	public void logicalPlay() {
 		Node pathP1 = pathLength(currentTurn);
 		int costP1 = pathP1.getCost();
 		Node pathP2 = pathLength(currentTurn.getOpponent());	
 		int costP2 = pathP2.getCost();
 		if (costP1 < costP2) {
 			nextMove = new MovePawnImpl(pathP1.getSquare().getCol(), pathP1.getSquare().getRow(), 
 											currentTurn, setting);
 		} else {
 			if (setting.getPawn(currentTurn, setting).getOwner().wallCount() <= 0) {
 				nextMove = new MovePawnImpl(pathP1.getSquare().getCol(), pathP1.getSquare().getRow(), 
 						currentTurn, setting);
 			} else {
 				if (wallMove() == false) {
 					nextMove = new MovePawnImpl(pathP1.getSquare().getCol(), pathP1.getSquare().getRow(), 
 													currentTurn, setting);
 				}
 			}
 		}
 		findPathTaken(pathP1);
 	}
 	
 	private boolean wallMove() {
 		Player p2 = currentTurn.getOpponent();
 		Square tmp = setting.getPawn(p2, setting).getSquare();
 		Square rightS = new SquareImpl(tmp.getCol()-1, tmp.getRow());
 		PlaceWallImpl next = null;
 		boolean exit = false;
 		boolean retVal = true;
 		
		if ((next = new PlaceWallImpl(tmp.getCol(), tmp.getRow(), Wall.HORIZONTAL, currentTurn, setting)).isValid()) {
 			nextMove = next;
 		} else if ((next = new PlaceWallImpl(tmp.getCol(), tmp.getRow(), Wall.VERTICAL, currentTurn, setting)).isValid()) {
 			nextMove = next;
		} else if ((next = new PlaceWallImpl(rightS.getCol(), rightS.getRow(), Wall.HORIZONTAL, currentTurn, setting)).isValid()) {
 			nextMove = next;
 		} else {
 			for (int i = tmp.getCol()-3; i < tmp.getCol() +3 && exit == false; i++) {
 				for (int j = tmp.getRow()-3; j < tmp.getRow() +3 && exit == false; j++) {
 					tmp = new SquareImpl(i, j);
 					if ((next = new PlaceWallImpl(tmp.getCol(), tmp.getRow(), Wall.HORIZONTAL, currentTurn, setting)).isValid()) {
 						nextMove = next;
 						exit = true;
 					} else if ((next = new PlaceWallImpl(tmp.getCol(), tmp.getRow(), Wall.VERTICAL, currentTurn, setting)).isValid()) {
 						nextMove = next;
 						exit = true;
 					}
 				}
 			}
 		}
 		
 		if (nextMove == null) {
 			retVal = false;
 		} 
 		
 		return retVal;
 	}
 	
 	private Square findPathTaken(Node path) {
 		Square start = setting.getPawn(currentTurn, setting).getSquare();
 		Square current = path.getSquare();
 		Node curNode = path;
 		current = curNode.getParent().getSquare();
 		while (current.getRow() != start.getRow() || current.getCol() != start.getCol()) {
 			current = curNode.getParent().getSquare();
 			curNode = curNode.getParent();
 		}
 
 		return current;
 	}
 	
 	private int destRow(boolean dest) {
 		if (dest == Player.TOP){
 			return 0;
 		} else {
 			return 8;
 		}
 	}
 	
 	private class MoveGenerator implements Iterator <StateImpl> {
 		List <StateImpl> backerList;
 		Iterator <StateImpl> backer;
 		Player mover;
 		Board setting;
 		
 		public MoveGenerator(Player mover, Board setting) {
 			backerList = new LinkedList <StateImpl>();
 			this.mover = mover;
 			this.setting = setting;
 			populate();
 			backer = backerList.iterator();
 		}
 		
 		private void populate() {
 			//populate with MovePawns
 			for (int i = -2; i <= 2 ; i ++) {
 				for (int j = -2;j<=2;j++) {
 					Square tmp = setting.getPawn(mover, setting).getSquare();
 					Board settingClone = new BoardImpl((BoardImpl)setting);
 					Player moverClone = new PlayerImpl(mover);
 					MovePawn tentative = new MovePawnImpl(tmp.getCol() + j, tmp.getRow() + i, moverClone, settingClone );
 					if (tentative.isValid()) {
 						backerList.add(new StateImpl(settingClone, moverClone, tentative));
 					}
 				}
 			}
 			
 		}
 		
 		@Override
 		public boolean hasNext() {
 			return backer.hasNext();
 		}
 
 		@Override
 		public StateImpl next() {
 			return backer.next();
 		}
 
 		@Override
 		public void remove() {
 			// TODO Auto-generated method stub
 		}
 	}
 
 }
