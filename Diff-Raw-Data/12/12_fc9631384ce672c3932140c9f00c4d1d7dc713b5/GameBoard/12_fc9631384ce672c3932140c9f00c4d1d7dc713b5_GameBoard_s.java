 package data;
 import java.util.*;
 
 public class GameBoard {
 	public StaticBoard staticBoard;
 	public ArrayList<Point> boxes;
 	public Point player;
 
 	Random random;
 
 	private static int[][] boxHashTable;
 	private static int[][] playerHashTable;
 
 	
 
 	public void makeMove(Point movedBox,int pos, char dir){
 
 		// hitta o erstt boxen som ska bytas ut
 		for(Point box: boxes){
 			if(box.equals(movedBox)){
 				movedBox=movedBox.makeMove(dir);
 //				boxes.get(pos)=movesBox;
 				boxes.set(pos, movedBox);
 			}
 		}
 		player=player.makeMove(dir);
 		
 		
 			
 	}
 	
 	public GameBoard getEndBoard(){
 		ArrayList<Point> goalBoxPositions= new ArrayList<Point>();
 		for(int i=0;i<staticBoard.ySize;i++){
 			for(int k=0; k<staticBoard.xSize;k++){
 				if(staticBoard.isGoal(k,i)==true){
 					goalBoxPositions.add(new Point(k,i));
 				}
 				
 			}
 		}
 		
 		GameBoard newBoard = new GameBoard(this,goalBoxPositions);
 		newBoard.staticBoard = staticBoard;
 		
 		return newBoard;
 	}
 	
 	public static ArrayList<Point> cloneList(ArrayList<Point> list) {
 		ArrayList<Point> clone = new ArrayList<Point>(list.size());
 		for(Point item: list) {
 			clone.add((Point) item.clone());
 
 		}
 		return clone;
 	}
 
 	public GameBoard(GameBoard old,ArrayList<Point> goalBoxPositions){
 		this.staticBoard=old.staticBoard;
 		this.boxes=cloneList(goalBoxPositions);
 		this.player=old.player.clone();
		this.boxHashTable=old.boxHashTable;
		this.playerHashTable=old.playerHashTable;	
		
 	}
 	public GameBoard(GameBoard old){
 		this.staticBoard=old.staticBoard;
 		this.boxes=cloneList(old.boxes);
 		this.player=old.player.clone();
		this.boxHashTable=old.boxHashTable;
		this.playerHashTable=old.playerHashTable;	
		
 	}
 	public GameBoard(String[] data) {
 		random = new Random();
 
 		// find out dimensions
         int width = 0;
 		int height = data.length;
 		
 		for(int i = 0; i < height; i++) {
 			width = width < data[i].length() ? data[i].length() : width;
 		}
 		
 		staticBoard = new StaticBoard(width, height);
 		
 		// parse string and create board
 		// copy boxes and player into startBoard, mutate stuff and make endBoard
 		// let startBoard and endBoard point to the same StaticBoard as this instance.
 
 		int xSize = width;
 		int ySize = height;
 		playerHashTable =new int[xSize][ySize];
 		boxHashTable =new int[xSize][ySize];
 
 		for(int y = 0; y < ySize; y++)
 			for(int x = 0; x < xSize; x++) {
 				boxHashTable[x][y] = random.nextInt();
 				playerHashTable[x][y] = random.nextInt();
 			}
 		boxes=new ArrayList<Point>();
 		for(int y = 0; y < data.length; y++) {
 			char[] lineData = data[y].toCharArray();
 			for(int x = 0; x < lineData.length; x++) {
 				switch(lineData[x]) {
 				case 0x2b:
 					staticBoard.goal[x][y] = true;
 				case 0x40:
 					player = new Point(x, y);
 					staticBoard.start = new Point(x, y);
 				case 0x20:
 					staticBoard.floor[x][y] = true;
 					break;
 				case 0x2a:
 					boxes.add(new Point(x, y));
 				case 0x2e:
 					staticBoard.goal[x][y] = true;
 					staticBoard.floor[x][y] = true;
 					break;
 				case 0x24:
 					boxes.add(new Point(x, y));
 					staticBoard.floor[x][y] = true;
 					break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		/*
 		position of all the boxes
 		position of the player
 			position is not actual position, but rather
 			the position it would have if the player moved
 			to the topmost row reachable from its current
 			position, then the leftmost column from there.
 		 */
 
 		// get the position the player would have if it moved top, then left
 		Point anchor = getAnchorPoint(player.getX(), player.getY());
 
 		int hashValue = playerHashTable[anchor.getX()][anchor.getY()];
 
 		for(int b = 0; b < boxes.size(); b++)
 			hashValue ^= boxHashTable[boxes.get(b).getX()][boxes.get(b).getY()];
 
 		return hashValue;
 	}
 
 	private Point getAnchorPoint(int fromX, int fromY) {
 		boolean visited[][] = new boolean[staticBoard.xSize][staticBoard.ySize];
 
 		Queue<Point> queue = new LinkedList<Point>();
 
 		queue.add(new Point(fromX, fromY));
 
 		int minX = fromX;
 		int minY = fromY;
 
 		Point currentPoint;
 		int x;
 		int y;
 		// find minY
 		while(!queue.isEmpty()) {
 			currentPoint = queue.poll();
 
 			x = currentPoint.getX();
 			y = currentPoint.getY();
 
 			visited[x][y] = true;
 
 			int dx[] = new int[] {0,1,0,-1};
 			int dy[] = new int[] {-1,0,1,0};
 			
 			for(int d = 0; d < dx.length; d++) {
 				int nx = x+dx[d];
 				int ny = y+dy[d];
 				if(nx>=0 && nx<staticBoard.xSize) {
 					if(ny>=0 && ny<staticBoard.ySize)
 						if(!visited[nx][ny])
 							if(isWalkable(nx, ny)) {
 								queue.add(new Point(x+dx[d],y+dy[d]));
 								if(y < minY) {
 									minY = y;
 									minX = x;
 								}
 							}
 				}
 			}
 		}
 
 		// find min x
 		x = minX;
 		y = minY;
 		while(staticBoard.isFloor(x - 1, y)) {
 			minX = x = x - 1;
 		}
 
 		return new Point(x, y);
 	}
 
 	private GameBoard update(Move m){
 		//use the makeMove-function in Move to change a move's direction
 		return null;
 	}
 	//returns true if we can go from start point to goal point
 	public boolean goToPoint(int fromX, int fromY, int toX, int toY){
 		int start = fromY*staticBoard.xSize + fromX;
 		int goal = toY*staticBoard.xSize+toX;
 		ArrayList<ArrayList<Integer>> neighbour = new ArrayList<ArrayList<Integer>>();
 		char[][] map=new char[staticBoard.xSize][staticBoard.ySize];
 		for (int i = 0; i < staticBoard.xSize*staticBoard.ySize; i++) {
 			neighbour.add(new ArrayList<Integer>());
 		}
 		for (int i = 0; i < staticBoard.xSize - 1; i++) {
 			for (int j = 0; j < staticBoard.ySize - 1; j++) {
 				char current = map[i][j];
 				//check if neighbours to the right
 				if(current == ' ' || current == '.' || current == '+'|| current == '@') {
 					char next = map[i][j + 1];
 					if (next == ' ' || next == '.' || next == '+'|| next == '@') {
 						neighbour.get(i * staticBoard.xSize + j).add(i * staticBoard.xSize + j + 1);
 						//add next as neighbour to current
 						neighbour.get(i * staticBoard.xSize + j + 1).add(i * staticBoard.xSize + j);
 						//add current as neighbour to next
 					}
 				}
 				//check if neighbours to under
 				if(current == ' ' || current == '.' || current == '+'|| current == '@') {
 					char down = map[i+1][j];
 					if (down == ' ' || down == '.' || down == '+'|| down == '@') {
 						neighbour.get(i * staticBoard.xSize + j).add((i+1)*staticBoard.xSize + j );
 						//add next as neighbour to current
 						neighbour.get((i+1) * staticBoard.xSize + j).add(i * staticBoard.xSize + j );
 						//add next as neighbour to current
 					}
 				}
 
 			}
 		}
 		//BFS
 		if(start==goal){
 			return true;
 		}
 		Queue<Integer> queue = new LinkedList<Integer>();
 		queue.add(start);
 		System.out.println(start);
		boolean[] visited = new boolean[staticBoard.getSize().getX()*staticBoard.getSize().getY()];
 		visited[start]=true;
 		ArrayList<Integer> adjacent;
 		int current;
 		while(!queue.isEmpty()){
 			current = queue.poll();
 			adjacent = neighbour.get(current);
 			for(int i:adjacent){
 				if(!visited[i]){
 					queue.add(i);                                       
 					visited[i]=true;
 					if(i == goal){
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean canPull(Point p, char direction) {
 		int x=p.getX();
 		int y=p.getY();
 		switch (direction) {
 		case 'U':
 			if (y < staticBoard.ySize && y > 1) {
 				if (isWalkable(x,y-1)&&(isWalkable(x,y-2)))
 					return true;
 			}
 			break;
 		case 'R':
 			if (x < (staticBoard.xSize - 2) && x >= 0) {
 				if (isWalkable(x+1,y)&&(isWalkable(x+2,y)))
 					return true;
 			}
 			break;
 		case 'D':
 			if (y < (staticBoard.ySize - 2) && y >= 0) {
 				if (isWalkable(x,y+1)&&(isWalkable(x,y+2)))
 					return true;
 			}
 			break;
 		case 'L':
 			if (x < staticBoard.xSize && x > 1) {
 				if (isWalkable(x-1,y)&&(isWalkable(x-2,y)))
 					return true;
 			}
 			break;
 		}
 
 		return false;
 	}
 
 	public boolean isWalkable(int x, int y) {
 		return staticBoard.isFloor(x, y) && !hasBox(x, y);
 	}
 
 	public boolean hasBox(int x, int y) {
 		for(int b = 0; b < boxes.size(); b++) {
 			if(boxes.get(b).getX() == x && boxes.get(b).getY() == y)
 				return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public String toString() {
 		return Renderer.draw(this);
 	}
 }
