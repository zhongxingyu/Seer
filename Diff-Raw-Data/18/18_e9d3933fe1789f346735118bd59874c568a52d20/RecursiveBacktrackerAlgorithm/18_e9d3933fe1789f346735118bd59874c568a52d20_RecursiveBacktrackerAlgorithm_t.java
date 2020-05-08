 package com.me.mygdxgame;
 
 import java.util.Stack;
 
 public class RecursiveBacktrackerAlgorithm extends Algorithm {
 
 	// The fields that contain the starting position of the maze generation
 	// private int startX;
 	// private int startY;
 	// 2d array that represents the maze to be generated
 	// private int[][] maze;
 	// 2d array that represents which cells have been visited in the maze
 	private boolean[][] visited;
 	// dimensions of the matrix
 	// private int height;
 	// private int width;
 	// the stack keeps track of how deep we're going
 	private Stack<Cell> stack = new Stack<Cell>();
 
 	// the current position of where we are in the maze
 	// private Cell currentCell;
 
 	// private Random rand = new Random();
 
 	// constructor with random start position
 	public RecursiveBacktrackerAlgorithm(MazeMap map) {
 		super(map);
 		visited = new boolean[width][height];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				visited[i][j] = false;
 			}
 		}
 		visited[0][0] = true;
 		Cell startPosition = new Cell(startX, startY);
 		currentCell = startPosition;
 		stack.push(startPosition);
 	}
 
 	// constructor with specific start position
 	public RecursiveBacktrackerAlgorithm(MazeMap map, int x, int y) {
 		super(map);
 		visited = new boolean[width][height];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				visited[i][j] = false;
 			}
 		}
 		Cell startPosition = new Cell(startX, startY);
		currentCell = new Cell(startX, startY);
 		stack.push(startPosition);
 	}
 
 	// continually call this method in the actual game to do a step by step of
 	// the algorithm
 	@Override
 	public boolean update() {
 		// TODO Auto-generated method stub
 
 		// keeps track of which neighbors are available from the current
 		// position
 		if (!stack.empty()) {
 			int[] neighbors = new int[4];
 			int freeNeighborCount = 0;
 			// check whether there is a free neighbor
 			for (int i = 0; i < 4; i++) {
 				switch (i) {
 				case UP:
 					if (currentCell.y > 0
 							&& !visited[currentCell.x][currentCell.y - 1]) {
 						neighbors[freeNeighborCount++] = i;
 					}
 					break;
 				case RIGHT:
 					if (currentCell.x < width - 1
 							&& !visited[currentCell.x + 1][currentCell.y]) {
 						neighbors[freeNeighborCount++] = i;
 					}
 					break;
 				case DOWN:
 					if (currentCell.y < height - 1
 							&& !visited[currentCell.x][currentCell.y + 1]) {
 						neighbors[freeNeighborCount++] = i;
 					}
 					break;
 				case LEFT:
 					if (currentCell.x > 0
 							&& !visited[currentCell.x - 1][currentCell.y]) {
 						neighbors[freeNeighborCount++] = i;
 					}
 					break;
 				}
 			}
 			// randomly select a free neighbor to move towards
 			if (freeNeighborCount > 0) {
 				switch (neighbors[rand.nextInt(freeNeighborCount)]) {
 				case UP:
 					map.set(currentCell.x, currentCell.y, MazeMap.UP);
 					//*
 					map.set(currentCell.x, currentCell.y, MazeMap.CURSOR);
 					currentCell.y--;
					Cell nextCellUp = new Cell(currentCell.x, currentCell.y);
					stack.push(nextCellUp);
 					visited[currentCell.x][currentCell.y] = true;
 					break;
 				case RIGHT:
 					// add draw right part here
 					map.set(currentCell.x, currentCell.y, MazeMap.RIGHT);
 					//*
 					map.set(currentCell.x, currentCell.y, MazeMap.CURSOR);
 					currentCell.x++;
					Cell nextCellRight = new Cell(currentCell.x, currentCell.y);
					stack.push(nextCellRight);
 					visited[currentCell.x][currentCell.y] = true;
 					break;
 				case DOWN:
 					// add draw down part here
 					map.set(currentCell.x, currentCell.y, MazeMap.DOWN);
 					//*
 					map.set(currentCell.x, currentCell.y, MazeMap.CURSOR);
 					currentCell.y++;
					Cell nextCellDown = new Cell(currentCell.x, currentCell.y);
					stack.push(nextCellDown);
 					visited[currentCell.x][currentCell.y] = true;
 					break;
 				case LEFT:
 					// add draw left part here
 					map.set(currentCell.x, currentCell.y, MazeMap.LEFT);
 					//*
 					map.set(currentCell.x, currentCell.y, MazeMap.CURSOR);
 					currentCell.x--;// CAN FALL OFF OF MAP. Needs fix.
					Cell nextCellLeft = new Cell(currentCell.x, currentCell.y);
					stack.push(nextCellLeft);
 					visited[currentCell.x][currentCell.y] = true;
 					break;
 				}
 			} else {
 				currentCell = stack.pop();
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
