 package se.chalmers.TowerDefence;
 
 import java.util.ArrayList;
 
 public class Road {
 	private enum Direction {NORTH, SOUTH, EAST, WEST};
 	private Direction direction;
 	
 	private ArrayList<RoadSquare> roadIterator;
 	
 	public Road(GameBoard gameBoard){
 		this.roadIterator = new ArrayList<RoadSquare>();
 		findRoad(gameBoard);
 //		findRoadFixed();
 	}
 	
 	private void findRoadFixed(){
 		roadIterator.add(new RoadSquare(0, 0));
 		roadIterator.add(new RoadSquare(0, 4));
 		roadIterator.add(new RoadSquare(4, 4));
 		roadIterator.add(new RoadSquare(4, 0));
 		roadIterator.add(new RoadSquare(8, 0));
 		roadIterator.add(new RoadSquare(8, 4));
 		roadIterator.add(new RoadSquare(8, 8));
 		roadIterator.add(new RoadSquare(4, 8));
 		roadIterator.add(new RoadSquare(4, 12));
 		roadIterator.add(new RoadSquare(8, 12));
 		roadIterator.add(new RoadSquare(12, 12));
 		roadIterator.add(new RoadSquare(12, 8));
 		roadIterator.add(new RoadSquare(16, 8));
 		roadIterator.add(new RoadSquare(16, 12));
 		roadIterator.add(new RoadSquare(16, 16));
 		roadIterator.add(new RoadSquare(20, 16));
 		roadIterator.add(new RoadSquare(24, 16));
 		roadIterator.add(new RoadSquare(28, 16));
 		roadIterator.add(new RoadSquare(28, 12));
 		roadIterator.add(new RoadSquare(24, 12));
 		roadIterator.add(new RoadSquare(24, 8));
 		roadIterator.add(new RoadSquare(38, 8));
 	}
 	private void findRoad(GameBoard gameBoard){
 		int[][] gb = gameBoard.getGameBoard();
 //		//TODO better algoritm
 //		int x = 0;
 //    	int y = 1;
 //    	boolean temp = true;
 //    	roadIterator.add(new RoadSquare(x,y));
 //    	while(temp){
 //    		//*
 //    		int lastX = roadIterator.get(roadIterator.size()-1).getX();
 //    		int lastY = roadIterator.get(roadIterator.size()-1).getY();
 //    		int length = roadIterator.get(roadIterator.size()-1).getLength();
 //    		if(x+1 < gb.length && gb[x+1][y] == 1 && !(x==lastX/length || y==lastY/length)){
 //    			roadIterator.add(new RoadSquare(x+1, y));
 //    			x++;
 //    		}else if(x - 1 >= 0 && gb[x-1][y] == 1 && !(x==lastX/length || y==lastY/length) ){
 //    			roadIterator.add(new RoadSquare(x-1, y));
 //    			x--;
 //    		}else if(y+1 < gb[x].length && gb[x][y+1] == 1 && !(x==lastX/length || y==lastY/length)){
 //    			roadIterator.add(new RoadSquare(x, y+1));
 //    			y++;
 //    		}else if(y - 1 >= 0 && gb[x][y-1] == 1 && !(x==lastX/length || y==lastY/length)){
 //    			roadIterator.add(new RoadSquare(x, y-1));
 //    			y--;
 //    		}else{
 //    			temp = false;
 //    		}
 //    		System.out.println("Road:adding Roadpiece "+x+" "+y);
 //    		//*/	
 //    		/*
 //    		if(gb[x+1][y] == 1){
 //	    		roadIterator.add(new RoadSquare(x+1,y));
 //	    		x++;
 //	    	}else if(gb[x][y+1]==1){
 //	    		roadIterator.add(new RoadSquare(x,y+1));
 //	    		y++;
 //	    	}else{
 //	    		temp=false;
 //	    	}
 //    	}
 //	    	//*/
 //    	}
 //    	System.out.println(roadIterator);
 		
 		
 		//Finding turns instead of next square
 		int x = 0;
		int y = 0;
 		roadIterator.add(new RoadSquare(x,y));
 		setDirection(Direction.EAST);
 		boolean temp = true;
 		while (temp) {
 			System.out.println("Road: " + x + " " + y);
 			if(direction == Direction.EAST) {
 				while(x < gb.length && gb[x+1][y] == 1) {
 					x++;
 					roadIterator.add(new RoadSquare(x,y));
 				}
 				if(y < gb[x].length && gb[x][y+1] == 1) {
 					setDirection(Direction.SOUTH);
 				}else if(y > 0 && gb[x][y-1] == 1){
 					setDirection(Direction.NORTH);
 				}else{
 					temp = false;
 				}
 			}else if(direction == Direction.WEST) {
 				while(x > 0 && gb[x-1][y] == 1) {
 					x--;
 					roadIterator.add(new RoadSquare(x,y));
 				}
 				if(y < gb[x].length && gb[x][y+1] == 1) {
 					setDirection(Direction.SOUTH);
 				}else if(y > 0 && gb[x][y-1] == 1){
 					setDirection(Direction.NORTH);
 				}else{
 					temp = false;
 				}
 			}else if(direction == Direction.NORTH) {
 				while(y-1 >= 0 && gb[x][y-1] == 1) {
 					y--;
 					roadIterator.add(new RoadSquare(x,y));
 				}
 				if(x < gb.length && gb[x+1][y] == 1) {
 					setDirection(Direction.EAST);
 				}else if(x > 0 && gb[x+1][y] == 1) {
 					setDirection(Direction.WEST);
 				}else{
 					temp = false;
 				}
 			}else if(direction == Direction.SOUTH){
 				while(y < gb[x].length && gb[x][y+1] == 1) {
 					y++;
 					roadIterator.add(new RoadSquare(x,y));
 				}
 				if(x < gb.length && gb[x+1][y] == 1) {
 					setDirection(Direction.EAST);
 				}else if(x > 0 && gb[x-1][y] == 1) {
 					setDirection(Direction.WEST);
 				}else{
 					temp = false;
 				}
 			}else{
 				temp = false;
 			}
 			
 		}
 		
 		
 	}
 		
 	
 	public RoadSquare getFirst(){
 		return roadIterator.get(0); 
 	}
 	
 	public RoadSquare getNext(RoadSquare tempSquare){
 		for(int i = 0; i < roadIterator.size(); i++){
 //			System.out.println("Road: " + roadIterator.get(i).getX() + "  " + roadIterator.get(i+1).getX());
 			
 			if(roadIterator.get(i).equals(tempSquare) && !(getLast().equals(tempSquare))){
 				return roadIterator.get(i+1);
 			}
 		}
 		return getLast();
 	}
 	
 	public RoadSquare getLast(){
 		return roadIterator.get(roadIterator.size()-1);
 	}
 	
 	public void typeToString(){
 		for(RoadSquare RS : roadIterator){
 			System.out.println("Road: " + RS.getX() + "  " + RS.getY());
 		}
 	}
 	
 	public void setDirection(Direction direction){
 		this.direction = direction;
 		
 	}
 	
 }
