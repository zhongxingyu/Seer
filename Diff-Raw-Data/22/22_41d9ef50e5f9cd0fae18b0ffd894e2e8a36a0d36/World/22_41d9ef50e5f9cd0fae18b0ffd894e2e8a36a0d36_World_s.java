 package world;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import things.Thing;
 import entities.Agent;
 import entities.Hero;
 
 import static world.Terrain.terrainType.*;
 
 public class World {
 	Terrain[][][] terrainGrid;
 	Thing[][][] thingGrid;
 	Agent[][][] agentGrid;
 	ArrayList<Agent> agents;
 	
 	Hero player;
 	
 	private final int PIXEL_SIZE = 2;
 	private final int TEXTURE_SIZE = 16;
 	private final int H_TEXTURE_SHEET_SIZE = 256;
 	private final int V_TEXTURE_SHEET_SIZE = 128;
 	
 	//Textures
 	private Texture hTerrainTexture;
 	private Texture vTerrainTexture;
 	
 	float[] displayCenter = new float[3];
 	
 	/**
 	 * Constructor, initializes display center to the center of the world
 	 * 
 	 * @param xSize world length
 	 * @param ySize world width
 	 * @param zSize world height
 	 */
 	public World(int xSize, int ySize, int zSize)
 	{
 		terrainGrid = new Terrain[xSize][ySize][zSize];
 		thingGrid = new Thing[xSize][ySize][zSize];
 		agentGrid = new Agent[xSize][ySize][zSize];
 		
 		agents = new ArrayList<Agent>();
 		
 		displayCenter[0] = (float)xSize/2;
 		displayCenter[1] = (float)ySize/2;
 		displayCenter[2] = (float)zSize/2;
 	}
 	
 	/**
 	 * Constructor, sets initial display center
 	 * 
 	 * @param xSize world length
 	 * @param ySize world width
 	 * @param zSize world height
 	 * @param center (x,y,z) center of the screen
 	 */
 	public World(int xSize, int ySize, int zSize, int[] center)
 	{
 		terrainGrid = new Terrain[xSize][ySize][zSize];
 		thingGrid = new Thing[xSize][ySize][zSize];
 		agentGrid = new Agent[xSize][ySize][zSize];
 		
 		displayCenter[0] = center[0];
 		displayCenter[1] = center[1];
 		displayCenter[2] = center[2];
 	}
 	
 	public void loadTextures()
 	{
 		try {
 			hTerrainTexture = TextureLoader.getTexture("png", ResourceLoader.getResourceAsStream("graphics/terrain/HTerrain.png"));
 			vTerrainTexture = TextureLoader.getTexture("png", ResourceLoader.getResourceAsStream("graphics/terrain/VTerrain.png"));
 		} catch (IOException e) {e.printStackTrace();}
 	}
 	
 	public void updateAgents()
 	{
 		for (int i = 0; i < agents.size(); i ++)
 		{
 			agents.get(i).executeAction(this);
 			agents.get(i).decideNextAction(this);
 		}
 	}
 	
 	public void renderWorld()
 	{
 		for (int k = 0; k < terrainGrid[0][0].length; k ++)
 		{
 			
 			//***************************************************************************************************************
 			//********* TERRAIN RENDERING ***********************************************************************************
 			//***************************************************************************************************************
 			for (int j = terrainGrid[0].length - 1; j >= 0; j --)
 			{
 				for (int i = 0; i < terrainGrid.length; i ++)
 				{
 					Terrain t = terrainGrid[i][j][k];					
 					
 					//Display vertical textures
 					if (t.getTerrainType() != air)
 					{
 						//Determine position on screen
 						int x = PIXEL_SIZE*(TEXTURE_SIZE*i - (int)(displayCenter[0]*TEXTURE_SIZE)) + 400 - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						int y = (PIXEL_SIZE*(TEXTURE_SIZE*j - (int)(displayCenter[1]*TEXTURE_SIZE)) + 300) - PIXEL_SIZE*((int)(displayCenter[2]*TEXTURE_SIZE) - TEXTURE_SIZE*k) - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						
 						GL11.glPushMatrix();
 						
 							//Translate to screen position and bind appropriate texture
 							GL11.glColor3f(1.0f, 1.0f, 1.0f);
 							GL11.glEnable(GL11.GL_TEXTURE_2D);
 							GL11.glTranslatef(x, y, 0);
 							vTerrainTexture.bind();
 							GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
 					    	
 					    	//Determine which part of the texture to use based on how many neighbors are air
 					    	int texX = t.getTexCol();
 					    	int texY = t.getTexRow();
 					    	float tConv;
 				    		if ((i - 1 < 0 || terrainGrid[i-1][j][k].getTerrainType() == air) && 
 				    				(i + 1 >= terrainGrid.length || terrainGrid[i+1][j][k].getTerrainType() == air))
 				    		{
 				    			texY += 1;
 				    		}
 				    		else if (i - 1 < 0 || terrainGrid[i-1][j][k].getTerrainType() == air)
 				    		{
 				    			texX += 1;
 				    			texY += 1;
 				    		}
 				    		else if (i + 1 >= terrainGrid.length || terrainGrid[i+1][j][k].getTerrainType() == air)
 				    		{
 				    			texX += 1;
 				    		}
 				    		
 				    		tConv = ((float)TEXTURE_SIZE)/((float)V_TEXTURE_SHEET_SIZE);
 					    	
 					    	GL11.glBegin(GL11.GL_QUADS);
 								GL11.glTexCoord2f(texX * tConv, texY*tConv + tConv);
 								GL11.glVertex2f(0, 0);
 								GL11.glTexCoord2f(texX*tConv + tConv, texY*tConv + tConv);
 								GL11.glVertex2f(PIXEL_SIZE*TEXTURE_SIZE, 0);
 								GL11.glTexCoord2f(texX*tConv + tConv, texY * tConv);
 								GL11.glVertex2f(PIXEL_SIZE*TEXTURE_SIZE, PIXEL_SIZE*TEXTURE_SIZE);
 								GL11.glTexCoord2f(texX*tConv, texY * tConv);
 								GL11.glVertex2f(0, PIXEL_SIZE*TEXTURE_SIZE);
 							GL11.glEnd();
 							
 						GL11.glPopMatrix();
 					}
 					//Display horizontal textures
 					else if (k - 1 >= 0)
 					{
 						if (terrainGrid[i][j][k-1].getTerrainType() != air)
 						{
 							t = terrainGrid[i][j][k-1];
 							//Determine position on screen
 							int x = PIXEL_SIZE*(TEXTURE_SIZE*i - (int)(displayCenter[0]*TEXTURE_SIZE)) + 400 - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 							int y = (PIXEL_SIZE*(TEXTURE_SIZE*j - (int)(displayCenter[1]*TEXTURE_SIZE)) + 300) - PIXEL_SIZE*((int)(displayCenter[2]*TEXTURE_SIZE) - TEXTURE_SIZE*k) - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 							
 							GL11.glPushMatrix();
 								
 								//Translate to screen position and bind appropriate texture
 								GL11.glColor3f(1.0f, 1.0f, 1.0f);
 								GL11.glEnable(GL11.GL_TEXTURE_2D);
 								GL11.glTranslatef(x, y, 0);
 								hTerrainTexture.bind();
 								GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
 						    	
 						    	//Determine which part of the texture to use based on how many neighbors are air
 						    	int texX = t.getTexColTop();
 						    	int texY = t.getTexRowTop();
 						    	float tConv;
 							
 								boolean topEmpty = j + 1 >= terrainGrid[0].length || terrainGrid[i][j+1][k-1].getTerrainType() == air,
 				    			bottomEmpty = j - 1 < 0 || terrainGrid[i][j-1][k-1].getTerrainType() == air,
 				    			rightEmpty = i + 1 >= terrainGrid.length || terrainGrid[i+1][j][k-1].getTerrainType() == air,
 				    			leftEmpty = i - 1 < 0 || terrainGrid[i-1][j][k-1].getTerrainType() == air;
 				    		
 					    		if (topEmpty && bottomEmpty)
 					    		{
 					    			texY += 3;
 					    		}
 					    		else if (topEmpty)
 					    		{
 					    			//texY is unchanged, this case is required for the else case and organizational purposes
 					    		}
 					    		else if (bottomEmpty)
 					    		{
 					    			texY += 2;
 					    		}
 					    		else
 					    		{
 					    			texY += 1;
 					    		}
 					    		
 					    		if (leftEmpty && rightEmpty)
 					    		{
 					    			texX += 3;
 					    		}
 					    		else if (leftEmpty)
 					    		{
 					    			//texX is unchanged, this case is required for the else case and organizational purposes
 					    		}
 					    		else if (rightEmpty)
 					    		{
 					    			texX += 2;
 					    		}
 					    		else
 					    		{
 					    			texX += 1;
 					    		}
 					    		
 					    		tConv = ((float)TEXTURE_SIZE)/((float)H_TEXTURE_SHEET_SIZE);	//width and height of texture sheet
 					    		
 					    		GL11.glBegin(GL11.GL_QUADS);
 									GL11.glTexCoord2f(texX * tConv, texY*tConv + tConv);
 									GL11.glVertex2f(0, 0);
 									GL11.glTexCoord2f(texX*tConv + tConv, texY*tConv + tConv);
 									GL11.glVertex2f(PIXEL_SIZE*TEXTURE_SIZE, 0);
 									GL11.glTexCoord2f(texX*tConv + tConv, texY * tConv);
 									GL11.glVertex2f(PIXEL_SIZE*TEXTURE_SIZE, PIXEL_SIZE*TEXTURE_SIZE);
 									GL11.glTexCoord2f(texX*tConv, texY * tConv);
 									GL11.glVertex2f(0, PIXEL_SIZE*TEXTURE_SIZE);
 								GL11.glEnd();
 							
 							GL11.glPopMatrix();
 						}
 					}
 				}
 			}
 			
 			//***************************************************************************************************************
			//********* OBJECT RENDERING ************************************************************************************
 			//***************************************************************************************************************
 			for (int j = terrainGrid[0].length - 1; j >= 0; j --)
 			{
 				for (int i = 0; i < terrainGrid.length; i ++)
 				{					
 					if (this.hasThing(i, j, k))
 					{
 						int x = PIXEL_SIZE*(TEXTURE_SIZE*i - (int)(displayCenter[0]*TEXTURE_SIZE)) + 400 - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						int y = (PIXEL_SIZE*(TEXTURE_SIZE*j - (int)(displayCenter[1]*TEXTURE_SIZE)) + 300) - PIXEL_SIZE*((int)(displayCenter[2]*TEXTURE_SIZE) - TEXTURE_SIZE*k) - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						
 						GL11.glPushMatrix();
 							GL11.glTranslatef(x, y, 0);
 							thingGrid[i][j][k].renderThing(PIXEL_SIZE, TEXTURE_SIZE);
 						GL11.glPopMatrix();
 					}
 				}
			}
			
			//***************************************************************************************************************
			//********* AGENT RENDERING *************************************************************************************
			//***************************************************************************************************************
			for (int j = terrainGrid[0].length - 1; j >= 0; j --)
			{
 				for (int i = 0; i < terrainGrid.length; i ++)
 				{
 					Agent agent = agentGrid[i][j][k];
 					if (agent != null)
 					{
 						int x = PIXEL_SIZE*(TEXTURE_SIZE*i - (int)(displayCenter[0]*TEXTURE_SIZE)) + 400 - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						int y = (PIXEL_SIZE*(TEXTURE_SIZE*j - (int)(displayCenter[1]*TEXTURE_SIZE)) + 300) - PIXEL_SIZE*((int)(displayCenter[2]*TEXTURE_SIZE) - TEXTURE_SIZE*k) - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						
 						GL11.glPushMatrix();
 							GL11.glTranslatef(x, y, 0);
 							agent.renderAgent(PIXEL_SIZE, TEXTURE_SIZE);
 						GL11.glPopMatrix();
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Add agents to the agent list and the agent grid
 	 * @param newAgents agents to be added to the world
 	 */
 	public void addAgents(ArrayList<Agent> newAgents)
 	{
 		agents.addAll(newAgents);
 		for (int i = 0; i < newAgents.size(); i ++)
 		{
 			int[] pos = newAgents.get(i).getPos();
 			agentGrid[pos[0]][pos[1]][pos[2]] = newAgents.get(i);
 		}
 	}
 	
 	public void addThing(Thing t, int x, int y, int z)
 	{
 		thingGrid[x][y][z] = t;
 		int[] pos = {x, y, z};
 		t.setPos(pos);
 	}
 	
 	public void moveAgent(Agent agent, int xChange, int yChange, int zChange)
 	{
 		int[] pos = agent.getPos();
 		int oldX = pos[0];
 		int oldY = pos[1];
 		int oldZ = pos[2];
 		int newX = oldX + xChange;
 		int newY = oldY + yChange;
 		int newZ = oldZ + zChange;
 		if (newX < 0 || newX >= agentGrid.length || newY < 0 || newY >= agentGrid[0].length || newZ < 0 || newZ >= agentGrid[0][0].length)
 		{
 			System.out.println("Could not move agent, position out of bounds");
 		}
 		else
 		{
 			int[] newPos = {newX, newY, newZ};
 			agent.setPos(newPos);
 			agentGrid[oldX][oldY][oldZ] = null;
 			agentGrid[newX][newY][newZ] = agent;
 		}
 	}
 	
 	/**
 	 * Setter for the display center
 	 * 
 	 * @param center the new display center
 	 */
 	public void setDisplayCenter(int[] center)
 	{
 		displayCenter[0] = center[0];
 		displayCenter[1] = center[1];
 		displayCenter[2] = center[2];
 	}
 	
 	/**
 	 * Incrementer for the display x
 	 * 
 	 * @param x the amount to increment the display x center, in pixels
 	 */
 	public void IncrementDisplayX(int x)
 	{
 		displayCenter[0] += (float)x/TEXTURE_SIZE;
 	}
 	
 	/**
 	 * Incrementer for the display y
 	 * 
 	 * @param y the amount to increment the display y center, in pixels
 	 */
 	public void IncrementDisplayY(int y)
 	{
 		displayCenter[1] += (float)y/TEXTURE_SIZE;
 	}
 	
 	
 	/**
 	 * Incrementer for the display z
 	 * 
 	 * @param z the amount to increment the display z center, in pixels
 	 */
 	public void IncrementDisplayZ(int z)
 	{
 		displayCenter[2] += (float)z/TEXTURE_SIZE;
 	}
 	
 	/**
 	 * Getter for the display x
 	 * 
 	 * @return x display pixel coordinate
 	 */
 	public float getDisplayX()
 	{
 		return displayCenter[0];
 	}
 	
 	/**
 	 * Getter for the display y
 	 * 
 	 * @return y display pixel coordinate
 	 */
 	public float getDisplayY()
 	{
 		return displayCenter[1];
 	}
 	
 	/**
 	 * Getter for the display z
 	 * 
 	 * @return z display pixel coordinate
 	 */
 	public float getDisplayZ()
 	{
 		return displayCenter[2];
 	}
 	
 	/**
 	 * Determine if there is an agent at a certain location
 	 * 
 	 * @param x grid location
 	 * @param y grid location
 	 * @param z grid location
 	 * @return true if the space is occupied
 	 */
 	public boolean isOccupied(int x, int y, int z)
 	{
 		return agentGrid[x][y][z] != null;
 	}
 	
 	/**
 	 * Determine if there is a thing at a certain location
 	 * 
 	 * @param x grid location
 	 * @param y grid location
 	 * @param z grid location
 	 * @return true if the space has a thing in it
 	 */
 	public boolean hasThing(int x, int y, int z)
 	{
 		return thingGrid[x][y][z] != null;
 	}
 	
 	/**
 	 * Determine whether an agent can move onto a grid cell based on other agents,
 	 * things and whether they can be crossed, and terrain blocking
 	 * 
 	 * @param x grid location
 	 * @param y grid location
 	 * @param z grid location
 	 * @return true if an agent cannot move to the grid space
 	 */
 	public boolean isBlocked(int x, int y, int z)
 	{
 		if (this.isOccupied(x, y, z))
 		{
 			return true;
 		}
 		else if (this.hasThing(x, y, z))
 		{
 			return this.thingGrid[x][y][z].isBlocking();
 		}
 		else
 		{
 			return terrainGrid[x][y][z].isBlocking();
 		}
 	}
 	
 	public boolean isCrossable(int x, int y, int z)
 	{
 		if (this.hasThing(x, y, z))
 		{
 			return this.thingGrid[x][y][z].isCrossable();
 		}
 		else
 		{
 			if (!this.isInBounds(x, y, z - 1))
 			{
 				return false;
 			}
 			else
 			{
 				return this.terrainGrid[x][y][z-1].isBlocking();
 			}
 		}
 	}
 	
 	/**
 	 * Check whether a specified location is within the bounds of the world grid
 	 * 
 	 * @param x grid location
 	 * @param y grid location
 	 * @param z grid location
 	 * @return true if the specified grid space is within bounds
 	 */
 	public boolean isInBounds(int x, int y, int z)
 	{
 		if (x >= 0 && x < terrainGrid.length && y >= 0 && y < terrainGrid[0].length && z >= 0 && z < terrainGrid[0][0].length)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public void setTerrain(Terrain[][][] t)
 	{
 		for (int i = 0; i < t.length; i ++)
 		{
 			for (int j = 0; j < t[0].length; j ++)
 			{
 				for (int k = 0; k < t[0][0].length; k ++)
 				{
 					//bounds checking
 					if (i < terrainGrid.length && j < terrainGrid[0].length && k < terrainGrid[0].length)
 					{
 						terrainGrid[i][j][k] = t[i][j][k];
 					}
 				}
 			}
 		}
 	}
 	
 	public void rotateC()
 	{
 		//TODO: Rotate display center as well
 		Terrain[][][] tempTerrain = new Terrain[terrainGrid.length][terrainGrid[0].length][terrainGrid[0][0].length];
 		Thing[][][] tempThing = new Thing[thingGrid.length][thingGrid[0].length][thingGrid[0][0].length];
 		Agent[][][] tempAgent = new Agent[agentGrid.length][agentGrid[0].length][agentGrid[0][0].length];
 		for (int i = 0; i < terrainGrid.length; i ++)
 		{
 			for (int j = 0; j < terrainGrid.length; j ++)
 			{
 				for (int k = 0; k < terrainGrid.length; k ++)
 				{
 					tempTerrain[j][terrainGrid.length - 1 - i][k] = terrainGrid[i][j][k];
 					tempThing[j][terrainGrid.length - 1 - i][k] = thingGrid[i][j][k];
 					tempAgent[j][terrainGrid.length - 1 - i][k] = agentGrid[i][j][k];
 				}
 			}
 		}
 		
 		terrainGrid = tempTerrain;
 		thingGrid = tempThing;
 		agentGrid = tempAgent;
 	}
 	
 	public void rotateCC()
 	{
 		//TODO: Rotate display center as well
 		Terrain[][][] tempTerrain = new Terrain[terrainGrid.length][terrainGrid[0].length][terrainGrid[0][0].length];
 		Thing[][][] tempThing = new Thing[thingGrid.length][thingGrid[0].length][thingGrid[0][0].length];
 		Agent[][][] tempAgent = new Agent[agentGrid.length][agentGrid[0].length][agentGrid[0][0].length];
 		for (int i = 0; i < terrainGrid.length; i ++)
 		{
 			for (int j = 0; j < terrainGrid.length; j ++)
 			{
 				for (int k = 0; k < terrainGrid.length; k ++)
 				{
 					tempTerrain[terrainGrid[0].length - 1 - j][i][k] = terrainGrid[i][j][k];
 					tempThing[thingGrid[0].length - 1 - j][i][k] = thingGrid[i][j][k];
 					tempAgent[agentGrid[0].length - 1 - j][i][k] = agentGrid[i][j][k];
 				}
 			}
 		}
 		
 		terrainGrid = tempTerrain;
 		thingGrid = tempThing;
 		agentGrid = tempAgent;
 	}
 	
 	/**
 	 * Getter for the player-controlled agent
 	 * @return the current player-controlled agent, null if there isn't one
 	 */
 	public Hero getPlayer()
 	{
 		return player;
 	}
 	
 	/**
 	 * Setter for the player-controlled agent
 	 * @param agent the new player-controlled agent
 	 */
 	public void setPlayer(Hero hero)
 	{
 		player = hero;
 	}
 }
