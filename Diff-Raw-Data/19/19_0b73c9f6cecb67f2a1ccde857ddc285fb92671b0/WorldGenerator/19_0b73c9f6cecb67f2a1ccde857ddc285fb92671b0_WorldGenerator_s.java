 package generator.world;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * World Generator. Prints a 2D array of characters that represents an ant world.
  *
  * World conforms to following constraints:
  * - Must be 150 * 150
  * - 2 anthills, 14 rocks, and 11 blobs of food.
  * - Anthills are hexagons with sides of length 7.
  * - Food blob is a 5-by-5 rectangle with each cell containing 5 food particles.
  * - The positions and orientations of the elements are chosen randomly.
  * - There must be at least one empty cell between non-food elements.
  * - No elements can overlap.
  *
  * @author Jamie Garner
  * @version 01/04/2013
  */
 public class WorldGenerator
 {
 	private char[][] world;
 	private int numOfRocks;
 	private int numOfFoodBlobs;
 	private boolean[][] tilesOccupied;
 	private File worldFile;
 
 	public WorldGenerator(int rocks, int foodBlobs)
 	{
 		numOfRocks = rocks;
 		numOfFoodBlobs = foodBlobs;
 		generateWorld();
 		try {
 			worldFile = createWorldFile(getWorldData());
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		WorldGenerator wg = new WorldGenerator(14, 11);
 	}
 
 	public char[][] generateWorld()
 	{
 		world = new char[150][150];
 		tilesOccupied = new boolean[150][150];
 
 		//loop through world, set border tiles to '#' and every other tile to '.'
 		for(int i=0; i<150; i++){
 			for(int j=0; j<150; j++){
 
 				//border tile - rock
 				if(i==0 || i==149 || j==0 || j==149){
 					world[i][j] = '#';
 					tilesOccupied[i][j] = true;
 				}
 				else{
 					world[i][j] = '.';
 				}
 			}
 		}
 
 		generateRocks();
 
 		generateFoodBlobs();
 
 		generateAnthill('-'); //Generate Black Anthill
 		generateAnthill('+'); //Generate Red Anthill
 
 		return world;
 	}
 
 	public void generateRocks()
 	{
 		//Create 14 random rock co-ords and add them to world
 		//Positions for rocks will always be valid as they are the first element to be drawn
 		for(int i=0; i<numOfRocks; i++){
 			int x = (int)(148 * Math.random()) + 1;
 			int y = (int)(148 * Math.random()) + 1;
			world[x][y] = '#';
			setTileOccupied(x, y);
 		}
 	}
 
 	public void generateFoodBlobs()
 	{
 		//Create 11 random foodBlob starting co-ords and put a 5x5 grid into world
 		for(int i=0; i<numOfFoodBlobs; i++){
 			int x = (int)(144 * Math.random()) + 1;
 			int y = (int)(144 * Math.random()) + 1;
 
 			if(checkIfFoodBlobIsValid(x, y)){
 				drawFoodBlob(x, y);
 			}
 			//If food blob position is not valid, go round the for loop again
 			else{
 				i--;
 			}
 		}
 	}
 
 	private void drawFoodBlob(int x, int y)
 	{
 		for(int tempX = x; tempX<(x+5); tempX++){
 			for(int tempY = y; tempY<(y+5); tempY++){
 				world[tempX][tempY] = '5';
 				setTileOccupied(tempX, tempY);
 			}
 		}
 	}
 
 	private boolean checkIfFoodBlobIsValid(int x, int y)
 	{
 		for(int tempX = x; tempX<(x+5); tempX++){
 			for(int tempY = y; tempY<(y+5); tempY++){
 				if(tilesOccupied[tempX][tempY]){
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	public void generateAnthill(char c)
 	{
 		int x = (int)(136 * Math.random()) + 1;
 		int y = (int)(136 * Math.random()) + 4;
 
 		if(checkIfAnthillIsValid(x, y)){
 			drawAnthill(c, x, y);
 		}
 		//If food blob position is not valid, go round the for loop again
 		else{
 			generateAnthill(c);
 		}
 
 
 	}
 
 	private boolean checkIfAnthillIsValid(int x, int y)
 	{
 		int numberOfHillTokens = 7;
 		boolean overHalfWay = false;
 		boolean toggle = true;
 
 		while(numberOfHillTokens >= 7){
 			for(int i=0; i<numberOfHillTokens; i++){
 				if(tilesOccupied[x][y+i]){
 					return false;
 				}
 			}
 			if(numberOfHillTokens >= 13){
 				overHalfWay = true; //Over half the hexagon for the anthill has been drawn
 			}
 			if(overHalfWay){
 				numberOfHillTokens--;
 				if(toggle){
 					toggle=false;
 					y++;
 				}
 				else{
 					toggle=true;
 				}
 			}
 			else{
 				numberOfHillTokens++;
 				if(toggle){
 					toggle=false;
 					y--;
 				}
 				else{
 					toggle=true;
 				}
 			}
 			x++; //move x to next line
 		}
 
 		return true;
 	}
 
 	private void drawAnthill(char c, int x, int y)
 	{
 		int numberOfHillTokens = 7;
 		boolean overHalfWay = false;
 		boolean toggle = true;
 		boolean indentFirstHalf;
 
 		//Whether the row is odd or even changes how the hexagon should be drawn.
 		if(x%2 == 0){
 			indentFirstHalf = false;
 		}
 		else{
 			indentFirstHalf = true;
 		}
 
 		while(numberOfHillTokens >= 7){
 			for(int i=0; i<numberOfHillTokens; i++){
 				world[x][y+i] = c;
 				setTileOccupied(x, y+i);
 			}
 
 			if(numberOfHillTokens >= 13){
 				overHalfWay = true; //Over half the hexagon for the anthill has been drawn
 			}
 
 			//Draw 2nd half
 			if(overHalfWay){
 				numberOfHillTokens--;
 				if(toggle){
 					toggle=false;
 					y++;
 				}
 				else{
 					toggle=true;
 				}
 
 				if(!indentFirstHalf){
 					if(toggle){
 						y++;
 					}
 					else{
 						y--;
 					}
 				}
 			}
 
 			//Draw 1st half
 			else{
 				numberOfHillTokens++;
 				if(toggle){
 					toggle=false;
 					y--;
 				}
 				else{
 					toggle=true;
 				}
 
 				if(indentFirstHalf){
 					if(!toggle){
 						y++;
 					}
 					else{
 						y--;
 					}
 				}
 			}
 
 			x++; //move x to next line
 		}
 	}
 
 	//Sets all tiles around a given tile (including given tile) to occupied (So elements wont overlap)
 	public void setTileOccupied(int x, int y)
 	{
 		tilesOccupied[x-1][y-1] = true;
 		tilesOccupied[x][y-1] = true;
 		tilesOccupied[x+1][y-1] = true;
 
 		tilesOccupied[x-1][y] = true;
 		tilesOccupied[x][y] = true;
 		tilesOccupied[x+1][y] = true;
 
 		tilesOccupied[x-1][y+1] = true;
 		tilesOccupied[x][y+1] = true;
 		tilesOccupied[x+1][y+1] = true;
 	}
 
 	public String getWorldData()
 	{
 		String worldData = "";
 
 		//Set world dimensions
 		//System.out.println("150");
 		//System.out.println("150");
 		worldData += "150\n";
 		worldData += "150\n";
 
 		for(int i=0; i<150; i++){
 			for(int j=0; j<150; j++){
 				//System.out.print(world[i][j] + " ");
 				worldData += world[i][j] + " ";
 			}
 			//System.out.println();
 			worldData += "\n";
 			if(i % 2 == 0){
 				//System.out.print(" ");
 				worldData += " ";
 			}
 		}
 
 		return worldData;
 	}
 
 	public File createWorldFile(String worldData) throws IOException
 	{
 		File f;
 		f = new File("AntWorld.world");
 		if(!f.exists()){
 			f.createNewFile();
 		}
 
 		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
 		writer.write(worldData);
 		writer.close();
 		return f;
 	}
 
 	public File getWorld()
 	{
 		return worldFile;
 	}
 }
