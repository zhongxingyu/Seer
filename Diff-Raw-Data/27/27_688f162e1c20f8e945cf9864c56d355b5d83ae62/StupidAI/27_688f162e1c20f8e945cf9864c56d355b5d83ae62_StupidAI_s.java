 package se.danielduner.minesweeper.client;
 
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Random;
 
 import se.danielduner.minesweeper.client.MineSweeperAI.GameStatus;
 import se.danielduner.minesweeper.client.PlayingField.ClickType;
 
 public class StupidAI {
 	private MineField field;
 	private Random random = new Random();
 	private int nextX=0, nextY=0;
 	private ClickType clickType = null;
 	
 	public StupidAI(MineField minefield) {
 		field = minefield;
 	}
 	
 	public void updateSuggestion() {
 		Queue<Coordinate> coordinateQueue = new LinkedList<Coordinate>();
 		int width = field.getWidth();
 		int height = field.getHeight();
 		boolean[][] explored = new boolean[width][height];
 		coordinateQueue.add(new Coordinate(nextX, nextY));
 		explored[nextX][nextY] = true;
 		while (!coordinateQueue.isEmpty()) {
 			Coordinate coordinate = coordinateQueue.poll();
 			int x = coordinate.x;
 			int y = coordinate.y;
 			int hiddenSum = field.getHiddenNeighbours(x, y);
 			int flaggedSum = field.getFlaggedNeighbours(x, y);
 			if (flaggedSum!=field.getValue(x, y) && hiddenSum==field.getValue(x, y)) {
 				for(int yd=y-1; yd<=y+1; yd++) {
 					for(int xd=x-1; xd<=x+1; xd++) {
 						if (!(yd==x && xd==y) && yd>=0 && yd<height && xd>=0 && xd<width
 								&& field.getValue(xd, yd)==MineField.HIDDEN) {
 							nextX = xd;
 							nextY = yd;
 							clickType = ClickType.RIGHTCLICK;
 							return;
 						}
 					}
 				}
 			}
 			if (hiddenSum>0 && flaggedSum==field.getValue(x, y)) {
 				for(int yd=y-1; yd<=y+1; yd++) {
 					for(int xd=x-1; xd<=x+1; xd++) {
 						if (!(yd==x && xd==y) && yd>=0 && yd<height && xd>=0
 								&& xd<width && field.getValue(xd, yd)==MineField.HIDDEN) {
 							nextX = xd;
 							nextY = yd;
 							clickType = ClickType.LEFTCLICK;
 							return;
 						}
 					}
 				}
 			} 
 			
			if(x-1>=0 && y-1>=0 && !explored[x-1][y-1]){
				coordinateQueue.add(new Coordinate(x-1, y-1));
				explored[x-1][y-1] = true;
 			}
			if(x+1<field.getWidth() && y-1>=0 && !explored[x+1][y-1]) {
				coordinateQueue.add(new Coordinate(x+1, y-1));
				explored[x+1][y-1] = true;
 			}
			if(x-1>=0 && y+1<field.getHeight() && !explored[x-1][y+1]) {
				coordinateQueue.add(new Coordinate(x-1, y+1));
				explored[x-1][y+1] = true;
 			}
			if(x+1<field.getWidth() && y+1<field.getHeight() && !explored[x+1][y+1]) {
				coordinateQueue.add(new Coordinate(x+1, y+1));
				explored[x+1][y+1] = true;
 			}
 		}
 		
 		System.out.println("Pick at random");
 		while (field.getGameStatus()==GameStatus.PLAYING) {
 			int x = random.nextInt(field.getWidth());
 			int y = random.nextInt(field.getHeight());
 			if (field.getValue(x, y)==MineField.HIDDEN) {
 				nextX = x;
 				nextY = y;
 				clickType = ClickType.LEFTCLICK;
 				return;
 			}
 		}
 	}
 	
 	/*
 	public void updateSuggestion() {
 		if (field.getGameStatus()!=GameStatus.PLAYING) {
 			return;
 		}
 		int width = field.getWidth();
 		int height = field.getHeight();
 		for (int y=0; y<field.getHeight(); y++) {
 			for (int x=0; x<field.getWidth(); x++) {
 				int hiddenSum = field.getHiddenNeighbours(x, y);
 				int flaggedSum = field.getFlaggedNeighbours(x, y);
 				if (flaggedSum!=field.getValue(x, y) && hiddenSum==field.getValue(x, y)) {
 					for(int yd=y-1; yd<=y+1; yd++) {
 						for(int xd=x-1; xd<=x+1; xd++) {
 							if (!(yd==x && xd==y) && yd>=0 && yd<height && xd>=0 && xd<width
 									&& field.getValue(xd, yd)==MineField.HIDDEN) {
 								nextX = xd;
 								nextY = yd;
 								clickType = ClickType.RIGHTCLICK;
 								return;
 							}
 						}
 					}
 				} else if (hiddenSum>0 && flaggedSum==field.getValue(x, y)) {
 					for(int yd=y-1; yd<=y+1; yd++) {
 						for(int xd=x-1; xd<=x+1; xd++) {
 							if (!(yd==x && xd==y) && yd>=0 && yd<height && xd>=0
 									&& xd<width && field.getValue(xd, yd)==MineField.HIDDEN) {
 								nextX = xd;
 								nextY = yd;
 								clickType = ClickType.LEFTCLICK;
 								return;
 							}
 						}
 					}
 				} 
 			}
 		}
 		
 		while (field.getGameStatus()==GameStatus.PLAYING) {
 			int x = random.nextInt(field.getWidth());
 			int y = random.nextInt(field.getHeight());
 			if (field.getValue(x, y)==MineField.HIDDEN) {
 				nextX = x;
 				nextY = y;
 				clickType = ClickType.LEFTCLICK;
 				return;
 			}
 		}
 	}*/
 	
 	public int getX() {
 		return nextX;
 	}
 	
 	public int getY() {
 		return nextY;
 	}
 	
 	public ClickType getClickType() {
 		return clickType;
 	}
 	
 	private class Coordinate {
 		int x, y;
 		public Coordinate(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 	}
 }
