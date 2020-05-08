 import java.awt.Point;
 import java.util.ArrayList;
 
 public class Grid implements GridInterface {
 
 	protected Node[][] grid;
 	protected int xLength;
 	protected int yLength;
 	
 	private final int cardinal = 10;
 	private final int diagonal = 14;
 	
 	public Grid(int x, int y){
 		xLength = x;
 		yLength = y;
 		grid = new Node[xLength][yLength];
 	}
 	
 	public int getX(){
 		return xLength;
 	}
 	
 	public int getY(){
 		return yLength;
 	}
 	
 	public Node getNode(Point c) {
 		int tempX = (int) c.getX();
 		int tempY = (int) c.getY();
 		if(tempX >= 0 && tempX < xLength && tempY >= 0 && tempY < yLength){
 			return grid[tempX][tempY];
 		}
 		return null;
 		/*for(Node n: grid){
 			if(n.getPosition().equals(c))
 				return n;
 		}
 		return null; */
 	}
 
 	public Node getNode(int x, int y){
 		if(x >= 0 && x < xLength && y >= 0 && y < yLength)
 			return grid[x][y];
 		return null;
 	}
 	
 	public boolean hasNode(Point c) {
 		return (getNode(c) != null);
 	}
 
 	public Node[] getNeighbors(Node n) {
 		return n.getConnections();
 	}
 
 	// Returns length of the edge connecting 2 nodes
 	// Returns 0 if there is no edge connecting them
 	public int getEdgeLength(Node a, Node b) {
 		if(a == null || b == null)
 			return 0;
 		for(Edge e: a.getEdges()){
 			if(e == null)
 				continue;
 			if(e.getEnd(a).equals(b))
 				return e.getLength();
 		}
 		return 0;
 	}
 	
 	public boolean linkNodes(Node a, Node b, int length){
 		if(a.connectionExists(b) || b.connectionExists(a) || a == null || b == null)
 			return false;
 		else{
 			a.addConnection(b, length);
 			b.addConnection(a, length);
 			return true;
 		}
 	}
 	
 	public Node[] getVision(Node n, int sight){
 		int x = (int) n.getPosition().getX();
 		int y = (int) n.getPosition().getY();
 		ArrayList<Node> visible = new ArrayList<Node>();
 		for(int addX = -1 * sight; addX <= sight; addX++){
 			for(int addY = -1 * sight; addY <= sight; addY++){
 				Node temp = getNode(x + addX, y + addY);
 				if(temp != null && (temp.getDistance(n) <= sight))
 					visible.add(temp);
 			}
 		}
 		Node[] update = visible.toArray(new Node[visible.size()]);
 		for(Node i: update){
 			i.setVisibility(true);
 		}
 		return update;
 	}
 	
 	// Returns the adjacent nodes, regardless of whether a connection actually exists
	private Node[] getAdjacent(Node n){
 		int x = (int) n.getPosition().getX();
 		int y = (int) n.getPosition().getY();
 		int size;
 		if((x==0 || x==xLength-1) && (y==0 || y==yLength-1))
 			size = 3;
 		else if ((x==0 || x==xLength-1) || (y==0 || y==yLength-1))
 			size = 5;
 		else
 			size = 8;
 		Node[] neighbors = new Node[size];
 		int place = 0;
 		for(int addX = -1; addX <= 1; addX++){
 			for(int addY = -1; addY <= 1; addY++){
 				Node temp = getNode(x + addX, y + addY);
 				if(temp != null && (addX != 0 || addY != 0))
 					neighbors[place++] = temp;
 			}
 		}
 		return neighbors;
 	}
 
 	// Standard grid with all possible links present, and all nodes passable
 	public void createStandard() {
 		for(int x = 0; x < xLength; x++){
 			for(int y = 0; y < yLength; y++){
 				grid[x][y] = new SquareNode(x, y, true);
 			}
 		}
 		for(int x = 0; x < xLength; x++){
 			for(int y = 0; y < yLength; y++){
 				Node current = getNode(x, y);
 				Node[] neighbors = getAdjacent(current);
 				for(Node n: neighbors){
 					if(n.getPosition().getX() == x || n.getPosition().getY() == y)
 						linkNodes(current, n, cardinal);
 					else
 						linkNodes(current, n, diagonal);
 				}
 			}
 		}
 	}
 	
 	public void turnOnFog(Node current, int sight){
 		Node[] visibles = getVision(current, sight);
 		for(int x = 0; x < xLength; x++){
 			for(int y = 0; y < yLength; y++){
 				boolean canSee = false;
 				for(Node n: visibles){
 					if(n.equals(grid[x][y]))
 						canSee = true;
 				}
 				if(!canSee)
 					grid[x][y].setVisibility(false);
 			}
 		}
 	}
 	
 	public void turnOffFog(){
 		for(int x = 0; x < xLength; x++){
 			for(int y = 0; y < yLength; y++){
 				grid[x][y].setVisibility(true);
 			}
 		}
 	}
 	
 	public void createRandom(Point start, Point end) {
 		double linkProb = 0.3;
 		for(int x = 0; x < xLength; x++){
 			for(int y = 0; y < yLength; y++){
 				grid[x][y] = new SquareNode(x, y, true);
 			}
 		}
 
 		for(int x = 0; x < xLength; x++){
 			for(int y = 0; y < yLength; y++){
 				Node current = grid[x][y];
 				Node[] neighbors = getAdjacent(current);
 				for(Node n: neighbors){
 					if(Math.random() < linkProb){
 						if(n.getPosition().getX() == current.getPosition().getX()
 								|| n.getPosition().getY() == current.getPosition().getY())
 							linkNodes(current, n, cardinal);
 						else
 							linkNodes(current, n, diagonal);
 					}
 				}
 			}
 		}
 	}
 
 }
