 /*
  * Adventron - Stan Schwertly
  * 
  * Map.java
  * Reads in map data and parses it. 
  */
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 
 public class Map
 {
 	public static final int WIDTH = 800;
     public static final int HEIGHT = 400;
     public static final int HALF_WIDTH = WIDTH/2;
     public static final int HALF_HEIGHT = HEIGHT/2;
     
     // Quadrants of the map. Used to minimize collision-detection times
     public static final int TOP_LEFT = 0;
     public static final int TOP_RIGHT =1;
     public static final int BOTTOM_LEFT = 2;
     public static final int BOTTOM_RIGHT = 3;
     public static final int OUT_OF_BOUNDS = -1;
     
     public static final Color WALL_COLOR = Color.white;
     
 	public String[] rows;
 	public BufferedImage image;
 	private ArrayList<Rectangle> walls[];
 
 	public Map()
 	{
 		rows = new String[25];
 		image = new BufferedImage(WIDTH,HEIGHT, BufferedImage.TYPE_INT_RGB);
 		walls = new ArrayList[4];
 		
 		for (int i=0; i<4; i++)
 		{
 			walls[i] = new ArrayList<Rectangle>();
 		}
 	}
 	
 	public ArrayList<Rectangle> getWalls(int whichWall)
 	{
 		return walls[whichWall];
 	}
 	
 	public void readLevel(String filename, Graphics g)
 	{
 		// Read in the level
 		try
 		{
			URL level = new URL("http://mad.eofw.in/levels/" + filename);
 			BufferedReader br = new BufferedReader(new InputStreamReader(level.openStream()));
 			String strLine = br.readLine();
 			int index = 0;
 			
 			while ((strLine = br.readLine()) != null)   
 			{
 				rows[index] = strLine;
 				index++;
 			}
 			
 			br.close();
 		}
 		
 		catch (Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			System.exit(0);
 		}
 		// Level is read. process it.
 		
 		// java reminder for me. This creates the background image
 		g = image.createGraphics();
 		g.setColor(WALL_COLOR);  
 		
 		// Spacer for walls. Increases as lines of wall continue
 		int spaceBelowScreen = 20;
 		int lastWall[] = {0,0,0,0};
 		int tempQuad = 0; // Where will the wall be placed?
 		
 		/* loop through and look for walls. if a wall is found,
 		 * figure out which quadrant it's in and put it there
 		*/
 		for (int i=0; i <rows.length; i++)
 		{
 			for (int j=0; j<rows[i].length(); j++)
 			{
 				if (rows[i].charAt(j) == '#')
 				{
 					tempQuad = determineQuadrant((j*10), spaceBelowScreen);
 					walls[tempQuad].add(new Rectangle((j*10), spaceBelowScreen, 10, 10));
 					
 					g.drawRect(
 							walls[tempQuad].get(lastWall[tempQuad]).x, 
 							walls[tempQuad].get(lastWall[tempQuad]).y, 
 							walls[tempQuad].get(lastWall[tempQuad]).width, 
 							walls[tempQuad].get(lastWall[tempQuad]).height);
 					lastWall[tempQuad]++;
 				}
 			}
 			spaceBelowScreen+=10;
 		}
 	}
 	
 	private int determineQuadrant(int x, int y)
 	{
 		if (x < WIDTH/2)
 		{
 			if (y < HEIGHT/2)
 				return TOP_LEFT;
 			else
 				return BOTTOM_LEFT;
 		}
 		else if (y < HEIGHT/2)
 			return TOP_RIGHT;
 		else return BOTTOM_RIGHT;
 	}
 }
