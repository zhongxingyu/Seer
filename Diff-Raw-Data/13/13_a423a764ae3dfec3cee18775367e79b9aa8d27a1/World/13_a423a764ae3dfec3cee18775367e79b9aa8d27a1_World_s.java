 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Vector2f;
 
 
 public class World {
 
 	Random rand = new Random();
 	Image floorTexture;
 	
 	float terrainCellWidth;
 	int terrainGridWidth;
 	int terrainGridHeight;
 	
 	int[][] terrainGrid;
 	
 	
 	public World(int width, int height, float cellWidth) {
 		terrainGridWidth = width;
 		terrainGridHeight = height;
 		terrainGrid = new int[width][height];
 		this.terrainCellWidth = cellWidth;
 		
 		floorTexture = TextureManager.getTexture("textures/floor.png");
 	}
 	
 	
 	public void placeEntity(float x, float y, int id) {
 		if (x < 0 || x > terrainCellWidth*terrainGridWidth || y < 0 || y > terrainCellWidth*terrainGridHeight) {
 			System.err.println("Failed to place entity ("+id+") at ("+x+", "+y+")");
 			return;
 		}
 		terrainGrid[(int)(x/terrainCellWidth)][(int)(y/terrainCellWidth)] = id;
 	}
 	
 	/*
 	 * Returns the id of the GridSpace entity in the grid location specified by x, y
 	 */
 	public int getCellContents(float x, float y) {
 		if (x < 0 || x > terrainCellWidth*terrainGridWidth || y < 0 || y > terrainCellWidth*terrainGridHeight) {
			System.err.println("Tried to get cell contents at invalid coords: ("+x+", "+y+")");
 			return -1;
 		}
 		return terrainGrid[(int)(x/terrainCellWidth)][(int)(y/terrainCellWidth)];
 	}
 	
 	/*
 	 * Returns the id of the GridSpace entity in the grid location specified by i, j
 	 */
 	public int getCellContents(int i, int j) {
 		if (i < 0 || i > terrainGridWidth || j < 0 || j > terrainGridHeight) {
			System.err.println("Tried to get cell contents at invalid cell coords: ("+i+", "+j+")");
 			return -1;
 		} else
 			return terrainGrid[i][j];
 	}
 	
 	/*
 	 * Returns the type of the GridSpace entity in the grid location specified by x, y
 	 */
 	public int getCellGridSpaceType(float x, float y) {
 		if (x < 0 || x > terrainCellWidth*terrainGridWidth || y < 0 || y > terrainCellWidth*terrainGridHeight) {
			System.err.println("Tried to get cell contents at invalid coords: ("+x+", "+y+")");
 			return -1;
 		}
 		return ((GridSpace)EntityManager.getEntity(terrainGrid[(int)(x/terrainCellWidth)][(int)(y/terrainCellWidth)])).type;
 	}
 	
 	/*
 	 * Returns the type of the GridSpace entity in the grid location specified by i, j
 	 */
 	public int getCellGridSpaceType(int i, int j) {
 		if (i < 0 || i > terrainGridWidth || j < 0 || j > terrainGridHeight) {
			System.err.println("Tried to get cell contents at invalid cell coords: ("+i+", "+j+")");
 			return -1;
 		} else {
 			return ((GridSpace)EntityManager.getEntity(terrainGrid[i][j])).type;
 		}
 	}
 	
 	/*
 	 * Returns the list of GridSpace walls that are present in the 8 surrounding cells of x, y (FLOATS in world! -- NOT i, j coords)
 	 */
 	public ArrayList<GridSpace> getSurroundingCellWalls(float x, float y) {
 		int gridX = (int)(x/terrainCellWidth);
 		int gridY = (int)(y/terrainCellWidth);
 		
 		return getSurroundingCellWalls(gridX, gridY);
 	}	
 	
 	/*
 	 * Returns the list of GridSpace walls that are present in the 8 surrounding cells of (i, j)
 	 */
 	public ArrayList<GridSpace> getSurroundingCellWalls(int i, int j) {
 		ArrayList<GridSpace> surroundingCells = new ArrayList<GridSpace>();
 		// Iterate through the 8 surrounding cells--don't include the middle cell
 		for (int a = i-1; a <= i+1; a++) {
 			for (int b = j-1; b <= j+1; b++) {
 				// check the bounds of the grid
 				if ( !(a == i && b == j) && a >= 0 && a < terrainGridWidth && b >= 0 && b < terrainGridHeight) {
 					// We only care about the walls
 					GridSpace cell = ((GridSpace)EntityManager.getEntity(getCellContents(a, b)));
 					if (cell.type == 1) {
 						surroundingCells.add(cell);
 					}
 				}
 			}
 		}
 		return surroundingCells;
 	}
 	
 	private void placeOuterWalls() {	
 		for (int i = 0; i < terrainGridWidth; i++) {
 			terrainGrid[i][0] = new GridSpace(null, 1, i*terrainCellWidth, 0).id();
 			terrainGrid[i][terrainGridHeight-1] = new GridSpace(null, 1, i*terrainCellWidth, (terrainGridHeight-1)*terrainCellWidth).id();
 		}
 		
 		for (int i = 0; i < terrainGridHeight; i++) {
 			terrainGrid[0][i] = new GridSpace(null, 1, 0, i*terrainCellWidth).id();
 			terrainGrid[terrainGridWidth-1][i] = new GridSpace(null, 1, (terrainGridWidth-1)*terrainCellWidth, i*terrainCellWidth).id();
 		}
 		
 	}
 	
 	/*
 	 * Helper for room generation.  Chooses the orientation a wall should be constructed.
 	 * Returns a 1 for horizontal or 0 for vertical.
 	 */
 	private int getOrientation(int width, int height) {
 		if (width > height) 
 			return 1; // Horizontal
 		else if (height > width)
 			return 0; // Vertical
 		else
 			return rand.nextInt(2);
 	}
 	
 	/*
 	 * Recursively
 	 */
 	private void placeWall(int i, int j, int width, int height, int minRoomWidth, int doorwayWidth) {
 		
 		// If the resolution of our room is too small, we'll stop subdividing
 		if (width < minRoomWidth*2+1 || height < minRoomWidth*2+1) { // Base case for recursive subdividing
 			return;
 		} else {
 			// First determine the orientation we'll be dividing from
 			boolean horizontal = getOrientation(width, height) == 1;
 			
 			// Calculate the i, j coords for the start of the wall
 			int wallI = (i+minRoomWidth) + (horizontal ? rand.nextInt(width-2*minRoomWidth) : -minRoomWidth);
 			int wallJ = (j+minRoomWidth) + (horizontal ? -minRoomWidth : rand.nextInt(height-2*minRoomWidth));
 			
 			// Calculate the i, j coords for the opening in the wall
 			int openIndex = 1 + (horizontal ? rand.nextInt(height-doorwayWidth) : rand.nextInt(width-doorwayWidth));
 			
 			// Calculate the amount to move the wall when drawing it in
 			int dx = horizontal ? 0 : 1;
 			int dy = horizontal ? 1 : 0;
 
 			// Draw in the walls to the grid
 			for (int count = 0; count <= (horizontal ? height : width); count++) {
 				// Calculate which grid coords we're placing a wall into
 				int gridI = wallI+dx*count;
 				int gridJ = wallJ+dy*count;
 				
 				// Do some basic index bounds checking
 				if (gridI < 0 || gridI >= terrainGridWidth)
 					continue;
 				if (gridJ < 0 || gridJ >= terrainGridHeight)
 					continue;
 				
 				// Continue the loop if this is an opening in the wall
 				int openDiff = openIndex-count;
 				if (openDiff >= 0 & openDiff < doorwayWidth)
 					continue;
 				
 				// Don't place a new wall if one already exists here
 				if (terrainGrid[gridI][gridJ] == 0)
 					terrainGrid[gridI][gridJ] = new GridSpace(null, 1, gridI*terrainCellWidth, gridJ*terrainCellWidth).id();
 			}
 			
 			if (horizontal) {
 				// Divide the left and right rooms
 				placeWall(i, j, wallI-i, height, minRoomWidth, doorwayWidth);
 				placeWall(wallI, j, i+width-wallI, height, minRoomWidth, doorwayWidth);
 			} else {
 				// Divide the top and bottom rooms
 				placeWall(i, j, width, wallJ-j, minRoomWidth, doorwayWidth);
 				placeWall(i, wallJ, width, j+height-wallJ, minRoomWidth, doorwayWidth);
 			}
 			
 			// And that's how we do recursion!  Fun stuff, really.
 		}	
 	}
 	
 	private void placeFloorSpaces() {	
 		for (int i = 0; i < terrainGridWidth; i++) {
 			for (int j = 0; j < terrainGridHeight; j++) {
 				if (terrainGrid[i][j] == 0) { 
 					terrainGrid[i][j] = new GridSpace(null, 0, i*terrainCellWidth, j*terrainCellWidth).id();
 				}
 			}
 		}
 		
 	}
 	
 	public void generateRoom() {
 		placeWall(0, 0, terrainGridWidth, terrainGridHeight, 4, 3);
 		placeOuterWalls();
 		placeFloorSpaces();
 		EntityManager.syncEntities();
 	}	
 	
	
 }
