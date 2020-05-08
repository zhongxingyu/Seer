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
 import static entities.Agent.direction.*;
 import static world.World.timeOfDay.*;
 
 public class World {
 	Terrain[][][] terrainGrid;
 	Thing[][][] thingGrid;
 	Agent[][][] agentGrid;
 	float[][][] lightModGrid;
 	ArrayList<Agent> agents;
 	
 	//TODO: refactor this stuff to a new Game State class
 	Hero player;
 	public enum timeOfDay
 	{
 		sunrise, morning, midday, afternoon, sunset, night;
 	}
 	private timeOfDay tod;
 	
 	private final int PIXEL_SIZE = 2;
 	private final int TEXTURE_SIZE = 16;
 	private final int H_TEXTURE_SHEET_SIZE = 256;
 	private final int V_TEXTURE_SHEET_SIZE = 256;
 	
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
 		lightModGrid = new float[xSize][ySize][zSize];
 		
 		agents = new ArrayList<Agent>();
 		
 		displayCenter[0] = (float)xSize/2;
 		displayCenter[1] = (float)ySize/2;
 		displayCenter[2] = (float)zSize/2;
 		
 		setTod(midday);
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
 		lightModGrid = new float[xSize][ySize][zSize];
 		
 		agents = new ArrayList<Agent>();
 		
 		displayCenter[0] = center[0];
 		displayCenter[1] = center[1];
 		displayCenter[2] = center[2];
 		
 		setTod(midday);
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
 	
 	/**
 	 * Update the maximum world height to be rendered depending on whether the Hero Agent has a roof overhead
 	 */
 	private int updateHeightMax()
 	{
 		if (player == null)
 		{
 			return terrainGrid[0][0].length;
 		}
 		
 		int x = player.getPos()[0];
 		int y = player.getPos()[1];
 		int z = player.getPos()[2];
 		
 		//roof check
 		for (int k = z; k < terrainGrid[0][0].length; k ++)
 		{
 			if (terrainGrid[x][y][k].getTerrainType() != air)
 			{
 				return z + 1;
 			}
 		}
 		
 		//occluding wall check
 		for (int j = y - 1; j >= 0; j --)
 		{
 			int k = z + (y - j);
 			if (k >= terrainGrid[0][0].length)
 				break;
 			for (int i = 0; i < 10; i ++)
 			{
 				if (k + i >= terrainGrid[0][0].length)
 					break;
 				if (terrainGrid[x][j][k + i].getTerrainType() != air)
 					return z + 1;
 			}
 		}
 		
 		return terrainGrid[0][0].length;
 	}
 	
 	public boolean isShadowed(int x, int y, int z)
 	{
 		int shadowLength = 1;
 		int shadowDirection = 1;
 		boolean longShadows = false;
 		switch (tod)
 		{
 		case sunrise:
 			longShadows = true;
 			break;
 		case morning:
 			shadowLength = 3;
 			break;
 		case midday: 
 			shadowDirection = 0;
 			break;
 		case afternoon: 
 			shadowLength = 3;
 			shadowDirection = -1;
 			break;
 		case sunset: 
 			shadowDirection = -1;
 			longShadows = true;
 			break;
 		case night: 
 			//no shadows
 			return false;
 		default: 
 			shadowDirection = 0;
 			break;
 		}
 		
 		for (int k = 0; k + z < terrainGrid[0][0].length; k ++)
 		{
 			if (k % shadowLength == 0)
 				x += shadowDirection;
 
 			if (x < 0 || x >= terrainGrid[0].length || y < 0 || y >= terrainGrid[0].length)
 				break;
 			
 			if (!terrainGrid[x][y][k + z].isTransparent())
 				return true;
 			
 			if (longShadows && y + 1 < terrainGrid[0].length && !terrainGrid[x][y + 1][k + z].isTransparent())
 				return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Update specified portion of the grid for light modifications; grid parameters are assumed to be
 	 * in bounds.
 	 * 
 	 * @param xMin minimum x coordinate
 	 * @param xMax maximum x coordinate
 	 * @param yMin minimum y coordinate
 	 * @param yMax maximum y coordinate
 	 * @param zMin minimum z coordinate
 	 * @param zMax maximum z coordinate
 	 */
 	public void updateLightModGrid(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax)
 	{
 		float[][][] clearFloatGrid = new float[terrainGrid.length][terrainGrid[0].length][terrainGrid[0][0].length];
 		lightModGrid = clearFloatGrid;
 		
 		for (int i = xMin; i <= xMax; i ++)
 		{
 			for (int j = yMin; j <= yMax; j ++)
 			{
 				for (int k = zMin; k <= zMax; k ++)
 				{
 					if (producesLight(i, j, k))
 					{
 						//TODO: Update this to distinguish between point and directed lights
 						//point light update
 						int lightDst = 6;
 						int iMin = Math.max(i - lightDst, 0);
 						int jMin = Math.max(j - lightDst, 0);
 						int kMin = Math.max(k - lightDst, 0);
 						int iMax = Math.min(i + lightDst, terrainGrid.length - 1);
 						int jMax = Math.min(j + lightDst, terrainGrid[0].length - 1);
 						int kMax = Math.min(k + lightDst, terrainGrid[0][0].length - 1);
 						for (int i2 = iMin; i2 <= iMax; i2 ++)
 						{
 							for (int j2 = jMin; j2 <= jMax; j2 ++)
 							{
 								for (int k2 = kMin; k2 <= kMax; k2 ++)
 								{
 									if (!checkLightBlockingLineOfSight(i, j, k, i2, j2, k2))
 									{
 										//increase light modification based on distance to light source
 										float lightPower = .7f;
 										float dst = (float)Math.sqrt(Math.pow(i - i2, 2) + Math.pow(j - j2, 2) + Math.pow(k - k2, 2));
 										float updateVal = Math.max(lightPower - dst/10.0f, 0);
 										if (updateVal > lightModGrid[i2][j2][k2])
 											lightModGrid[i2][j2][k2] = updateVal;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 		
 	/**
 	 * Render terrain, things, and agents by layers
 	 */
 	public void renderWorld()
 	{
 		int iMin, iMax, jMin, jMax, kMin, kMax;
 		kMin = 0;
 		kMax = Math.min(terrainGrid[0][0].length - 1, updateHeightMax());
 		iMin = Math.max(0, (int)(displayCenter[0] - 13));
 		iMax = Math.min(terrainGrid.length - 1, (int)(displayCenter[0] + 15));
 		jMin = Math.max(0, (int)(displayCenter[1] - 10 - kMax));
 		jMax = Math.min(terrainGrid[0].length - 1, (int)(displayCenter[1] + 11 + kMax));
 		
		updateLightModGrid(iMin, iMax, jMin, jMax, kMin, kMax);
 		
 		for (int k = kMin; k <= kMax; k ++)
 		{
 			//***************************************************************************************************************
 			//********* TERRAIN AND THING AND AGENT RENDERING ***************************************************************
 			//***************************************************************************************************************
 			for (int j = jMax; j >= jMin; j --)
 			{
 				for (int i = iMin; i <= iMax; i ++)
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
 					    	
 							if (k != kMax)
 							{
 						    	//Determine which part of the texture to use based on how many neighbors are air
 						    	int texX = t.getTexCol();
 						    	int texY = t.getTexRow();
 						    	
 						    	boolean topEmpty = k + 1 >= terrainGrid[0][0].length || terrainGrid[i][j][k+1].getTerrainType() == air,
 				    			rightEmpty = i + 1 >= terrainGrid.length || terrainGrid[i+1][j][k].getTerrainType() == air,
 				    			leftEmpty = i - 1 < 0 || terrainGrid[i-1][j][k].getTerrainType() == air;
 
 				    			boolean bottomOnGround = false;
 				    			if (k - 1 < 0 || j - 1 < 0)
 				    				bottomOnGround = false;
 				    			else if (terrainGrid[i][j-1][k].getTerrainType() == air && terrainGrid[i][j-1][k-1].getTerrainType() != air)
 				    				bottomOnGround = true;
 				    			
 					    		if (topEmpty && bottomOnGround)
 					    		{
 					    			texY += 3;
 					    		}
 					    		else if (topEmpty)
 					    		{
 					    			//texY is unchanged, this case is required for the else case and organizational purposes
 					    		}
 					    		else if (bottomOnGround)
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
 						    	
 					    		float tConv = ((float)TEXTURE_SIZE)/((float)V_TEXTURE_SHEET_SIZE);
 						    	
 					    		
 						    	GL11.glBegin(GL11.GL_QUADS);
 						    		setLighting(false, lightModGrid[i][j][k]);
 									GL11.glTexCoord2f(texX * tConv, texY*tConv + tConv);
 									GL11.glVertex2f(0, 0);
 									GL11.glTexCoord2f(texX*tConv + tConv, texY*tConv + tConv);
 									GL11.glVertex2f(PIXEL_SIZE*TEXTURE_SIZE, 0);
 									GL11.glTexCoord2f(texX*tConv + tConv, texY * tConv);
 									GL11.glVertex2f(PIXEL_SIZE*TEXTURE_SIZE, PIXEL_SIZE*TEXTURE_SIZE);
 									GL11.glTexCoord2f(texX*tConv, texY * tConv);
 									GL11.glVertex2f(0, PIXEL_SIZE*TEXTURE_SIZE);
 								GL11.glEnd();
 							}
 							else
 							{
 								GL11.glColor3f(0, 0, 0);
 								GL11.glBegin(GL11.GL_QUADS);
 									GL11.glVertex2f(0, 0);
 									GL11.glVertex2f(PIXEL_SIZE*TEXTURE_SIZE, 0);
 									GL11.glVertex2f(PIXEL_SIZE*TEXTURE_SIZE, PIXEL_SIZE*TEXTURE_SIZE);
 									GL11.glVertex2f(0, PIXEL_SIZE*TEXTURE_SIZE);
 								GL11.glEnd();
 								GL11.glColor3f(1, 1, 1);
 							}
 							
 						GL11.glPopMatrix();
 						//Edge overhang textures
 						if ((j == 0 || (j - 1 >= 0 && terrainGrid[i][j-1][k].getTerrainType() == air)) 
 								&& k != kMax && terrainGrid[i][j][k+1].getTerrainType() == air)
 						{
 							t = terrainGrid[i][j][k];
 							//Determine position on screen
 							x = PIXEL_SIZE*(TEXTURE_SIZE*i - (int)(displayCenter[0]*TEXTURE_SIZE)) + 400 - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 							y = (PIXEL_SIZE*(TEXTURE_SIZE*j - (int)(displayCenter[1]*TEXTURE_SIZE)) + 300) - PIXEL_SIZE*((int)(displayCenter[2]*TEXTURE_SIZE) - TEXTURE_SIZE*k) - (PIXEL_SIZE*TEXTURE_SIZE)/2;
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
 						
 							boolean rightEmpty = i + 1 >= terrainGrid.length || terrainGrid[i+1][j][k].getTerrainType() == air,
 			    			leftEmpty = i - 1 < 0 || terrainGrid[i-1][j][k].getTerrainType() == air;
 			    		
 				    		texY += 4;
 				    		
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
 				    			setLighting(isShadowed(i, j, k+1), lightModGrid[i][j][k+1]);
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
 					//Display horizontal textures
 					else if (terrainGrid[i][j][k-1].getTerrainType() != air)
 					{
 						if (k - 1 >= 0)
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
 					    			setLighting(isShadowed(i, j, k), lightModGrid[i][j][k]);
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
 					//Display hanging bottom vertical textures
 					else if (k + 1 <= kMax && terrainGrid[i][j][k+1].getTerrainType() != air)
 					{
 						t = terrainGrid[i][j][k+1];
 						//Determine position on screen
 						int x = PIXEL_SIZE*(TEXTURE_SIZE*i - (int)(displayCenter[0]*TEXTURE_SIZE)) + 400 - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						int y = (PIXEL_SIZE*(TEXTURE_SIZE*j - (int)(displayCenter[1]*TEXTURE_SIZE)) + 300) - PIXEL_SIZE*((int)(displayCenter[2]*TEXTURE_SIZE) - TEXTURE_SIZE*k) - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						
 						GL11.glPushMatrix();
 							GL11.glColor3f(1.0f, 1.0f, 1.0f);
 							GL11.glEnable(GL11.GL_TEXTURE_2D);
 							GL11.glTranslatef(x, y, 0);
 							vTerrainTexture.bind();
 							GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
 							
 							int texX = t.getTexCol();
 					    	int texY = t.getTexRow();
 					    	
 					    	boolean rightEmpty = i + 1 >= terrainGrid.length || terrainGrid[i+1][j][k+1].getTerrainType() == air,
 			    			leftEmpty = i - 1 < 0 || terrainGrid[i-1][j][k+1].getTerrainType() == air;
 			    			
 				    		texY += 4;
 				    		
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
 					    	
 				    		float tConv = ((float)TEXTURE_SIZE)/((float)V_TEXTURE_SHEET_SIZE);
 					    	
 				    		
 					    	GL11.glBegin(GL11.GL_QUADS);
 					    		setLighting(false, lightModGrid[i][j][k+1]);
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
 				
 				// Render Things
 				for (int i = iMin; i <= iMax; i ++)
 				{					
 					if (this.hasThing(i, j, k))
 					{
 						int x = PIXEL_SIZE*(TEXTURE_SIZE*i - (int)(displayCenter[0]*TEXTURE_SIZE)) + 400 - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						int y = (PIXEL_SIZE*(TEXTURE_SIZE*j - (int)(displayCenter[1]*TEXTURE_SIZE)) + 300) - PIXEL_SIZE*((int)(displayCenter[2]*TEXTURE_SIZE) - TEXTURE_SIZE*k) - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						
 						GL11.glPushMatrix();
 							setLighting(isShadowed(i, j, k), lightModGrid[i][j][k]);
 							GL11.glTranslatef(x, y, 0);
 							thingGrid[i][j][k].renderThing(PIXEL_SIZE, TEXTURE_SIZE);
 						GL11.glPopMatrix();
 					}
 				}
 				
 				//Render Agents
 				for (int i = iMin; i <= iMax; i ++)
 				{
 					Agent agent = agentGrid[i][j][k];
 					if (agent != null)
 					{
 						int x = PIXEL_SIZE*(TEXTURE_SIZE*i - (int)(displayCenter[0]*TEXTURE_SIZE)) + 400 - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						int y = (PIXEL_SIZE*(TEXTURE_SIZE*j - (int)(displayCenter[1]*TEXTURE_SIZE)) + 300) - PIXEL_SIZE*((int)(displayCenter[2]*TEXTURE_SIZE) - TEXTURE_SIZE*k) - (PIXEL_SIZE*TEXTURE_SIZE)/2;
 						
 						GL11.glPushMatrix();
 							setLighting(isShadowed(i, j, k), lightModGrid[i][j][k]);
 							GL11.glTranslatef(x, y, 0);
 							agent.renderAgent(PIXEL_SIZE, TEXTURE_SIZE);
 						GL11.glPopMatrix();
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Determine the lighting for rendering something depending on shadows,
 	 * light sources, and time of day
 	 */
 	private void setLighting(boolean shadowed, float lightMod)
 	{
 		float r, g, b;
 		switch (tod)
 		{
 		case sunrise:
 			if (shadowed)
 			{
 				r = .5f;
 				g = .5f;
 				b = .5f;
 			}
 			else
 			{
 				r = .9f;
 				g = .8f;
 				b = .8f;
 			}
 		break;
 		case morning:
 			if (shadowed)
 			{
 				r = .7f;
 				g = .7f;
 				b = .7f;
 			}
 			else
 			{
 				r = 1f;
 				g = 1f;
 				b = 1f;
 			}
 		break;
 		case midday: 
 			if (shadowed)
 			{
 				r = .8f;
 				g = .8f;
 				b = .8f;
 			}
 			else
 			{
 				r = 1f;
 				g = 1f;
 				b = 1f;
 			}
 		break;
 		case afternoon: 
 			if (shadowed)
 			{
 				r = .7f;
 				g = .7f;
 				b = .7f;
 			}
 			else
 			{
 				r = 1f;
 				g = 1f;
 				b = 1f;
 			}
 		break;
 		case sunset: 
 			if (shadowed)
 			{
 				r = .5f;
 				g = .5f;
 				b = .5f;
 			}
 			else
 			{
 				r = .9f;
 				g = .75f;
 				b = .85f;
 			}
 		break;
 		case night: 
 			if (shadowed)
 			{
 				r = .3f;
 				g = .3f;
 				b = .5f;
 			}
 			else
 			{
 				r = .4f;
 				g = .4f;
 				b = .6f;
 			}
 		break;
 		default: 
 			if (shadowed)
 			{
 				r = .8f;
 				g = .8f;
 				b = .8f;
 			}
 			else
 			{
 				r = 1f;
 				g = 1f;
 				b = 1f;
 			}
 		}
 		
 		if (lightMod > 0)
 		{
 			r = Math.max(0, r + 1.2f * lightMod);
 			g = Math.max(0, g + lightMod);
 			b = Math.max(0, b + lightMod);
 		}
 		else if (lightMod < 0)
 		{
 			r = Math.min(1, r + .9f * lightMod);
 			g = Math.min(1, g + lightMod);
 			b = Math.min(1, b + .8f * lightMod);
 		}
 		
 		GL11.glColor3f(r, g, b);
 	}
 	
 	/**
 	 * Add an agent to the agent list and the agent grid
 	 * @param newAgent the agent to be added to the world
 	 */
 	public void addAgent(Agent newAgent)
 	{
 		agents.add(newAgent);
 		
 		int[] pos = newAgent.getPos();
 		agentGrid[pos[0]][pos[1]][pos[2]] = newAgent;
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
 	
 	public Agent getAgentAt(int x, int y, int z)
 	{
 		return agentGrid[x][y][z];
 	}
 	
 	public void removeAgentAt(int x, int y, int z)
 	{
 		agents.remove(agentGrid[x][y][z]);
 		agentGrid[x][y][z] = null;
 	}
 	
 	public void addThing(Thing t, int x, int y, int z)
 	{
 		thingGrid[x][y][z] = t;
 		int[] pos = {x, y, z};
 		t.setPos(pos);
 	}
 	
 	public Thing getThingAt(int x, int y, int z)
 	{
 		return thingGrid[x][y][z];
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
 	public void incrementDisplayX(int x)
 	{
 		displayCenter[0] += (float)x/TEXTURE_SIZE;
 	}
 	
 	/**
 	 * Incrementer for the display y
 	 * 
 	 * @param y the amount to increment the display y center, in pixels
 	 */
 	public void incrementDisplayY(int y)
 	{
 		displayCenter[1] += (float)y/TEXTURE_SIZE;
 	}
 	
 	/**
 	 * Incrementer for the display z
 	 * 
 	 * @param z the amount to increment the display z center, in pixels
 	 */
 	public void incrementDisplayZ(int z)
 	{
 		displayCenter[2] += (float)z/TEXTURE_SIZE;
 	}
 	
 	public void cycleTimeOfDay()
 	{
 		switch (tod)
 		{
 		case sunrise: tod = morning; break;
 		case morning: tod = midday; break;
 		case midday: tod = afternoon; break;
 		case afternoon: tod = sunset; break;
 		case sunset: tod = night; break;
 		case night: tod = sunrise; break;
 		}
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
 		if (this.isInBounds(x, y, z))
 			return agentGrid[x][y][z] != null;
 		else
 			return false;
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
 		if (this.isInBounds(x, y, z))
 			return thingGrid[x][y][z] != null;
 		else
 			return false;
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
 	
 	public boolean isLightBlocking(int x, int y, int z)
 	{
 		if (isOccupied(x, y, z) && !agentGrid[x][y][z].isTransparent())
 			return true;
 		else if (hasThing(x, y, z) && thingGrid[x][y][z].isBlocking() && thingGrid[x][y][z].isBlocking())
 			return true;
 		else
 			return !terrainGrid[x][y][z].isTransparent();
 	}
 	
 	public boolean producesLight(int x, int y, int z)
 	{
 		if (hasThing(x, y, z) && thingGrid[x][y][z].isLightSource())
 			return true;
 		else
 			return false;
 	}
 	
 	/**
 	 * Determine whether a light source is blocked before reaching a test cell
 	 * 
 	 * @param x1 light source x
 	 * @param y1 light source y
 	 * @param z1 light source z
 	 * @param x2 test cell x
 	 * @param y2 test cell y
 	 * @param z2 test cell z
 	 * @return true if the cell is blocked, false otherwise
 	 */
 	public boolean checkLightBlockingLineOfSight(int x1, int y1, int z1, int x2, int y2, int z2)
 	{
 		int dx = x2 - x1;
 		int dy = y2 - y1;
 		int dz = z2 - z1;
 		
 		//handle special cases of light-blocking test cells
 		if (isLightBlocking(x2, y2, z2))
 		{
 			if (dy < 0)
 				return true;
 			else if (dy == 0 && dz > 0)
 				return true;
 		}
 		
 		//calculate steps for iterating through line of sight points in between the two points
 		float step = Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));		
 		float xStep = (float)dx / step;
 		float yStep = (float)dy / step;
 		float zStep = (float)dz / step;
 		
 		//check spaces in between light source and test cell
 		//System.out.println();
 		//System.out.println("Light source: " + x1 + ", " + y1 + ", " + z1);
 		for (int n = 1; n < step; n ++)
 		{
 			int i = (int)(x1 + n * xStep + .5);
 			int j = (int)(y1 + n * yStep + .5);
 			int k = (int)(z1 + n * zStep + .5);
 			//System.out.println(i + ", " + j + ", " + k);
 			
 			if (isLightBlocking(i, j, k))
 				return true;
 		}
 		//System.out.println("Target cell: " + x2 + ", " + y2 + ", " + z2);
 		
 		return false;
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
 	
 	public boolean isLandable(int x, int y, int z)
 	{
 		//Add things that can't be landed on here
 		if (this.hasThing(x, y, z) && (this.thingGrid[x][y][z].isRamp() && this.thingGrid[x][y][z].getDir() != down))
 			return false;
 		else
 			return isCrossable(x, y, z);
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
 					if (i < terrainGrid.length && j < terrainGrid[0].length && k < terrainGrid[0][0].length)
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
 
 	public void setTod(timeOfDay tod) {
 		this.tod = tod;
 	}
 
 	public timeOfDay getTod() {
 		return tod;
 	}
 }
