 //File: Bot.java
 //Purpose: Game Bot class.
 
 package edu.sru.andgate.bitbot.graphics;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import javax.microedition.khronos.opengles.GL10;
 import edu.sru.andgate.bitbot.R;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 //import android.graphics.drawable.Drawable;
 import android.opengl.GLUtils;
 import android.util.Log;
 
 import java.util.*;
 import java.io.*;
 
 public class TileMap
 {
 	float[] parameters;
 	int ID = 0;
 	int textureCount = 0;
 	int MAX_TEXTURE_ARRAY_SIZE = 500;
 	int SELECTED_TEXTURE = 0;
 	public int mapWidth, mapHeight;
 	public int tileSize;
 	int tileStep;
 	float drawX1, drawX2, drawY1, drawY2;
 	int mapOriginX, mapOriginY;
 	int LDrawPoint, RDrawPoint, TDrawPoint, BDrawPoint;
 	int rightMapEdge, topMapEdge;
 	int numTextures = 0;
 	public int[][][] tileTextures;
 	public float[][][] tileLocations;
 	public int[][][] tileCodes;
 	public int[][][] tileBoundaries;
 	int[] drawBufferCount;
 	ArrayList<Integer> textureHopper;
 	ArrayList<int[][]> drawBuffer;
 	boolean textureLoaded = false;
 	
 	int MAX_TILES_PER_FRAME = 200;	//IMPORTANT WHEN PINCH TO ZOOM COMES INTO PLAY
 	
 	private FloatBuffer vertexBuffer;	// buffer holding the vertices
 	private float vertices[] =
 	{
 			-1.0f, -1.0f,  0.0f,		// V1 - bottom left
 			-1.0f,  1.0f,  0.0f,		// V2 - top left
 			 1.0f, -1.0f,  0.0f,		// V3 - bottom right
 			 1.0f,  1.0f,  0.0f,		// V4 - top right
 	};
 
 	private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
 	private float texture[] =
 	{    		
 			// Mapping coordinates for the vertices
 			0.0f, 1.0f,		// top left		(V2)
 			0.0f, 0.0f,		// bottom left	(V1)
 			1.0f, 1.0f,		// top right	(V4)
 			1.0f, 0.0f,		// bottom right	(V3)
 	};
 	
 	//Texture pointer
 	private int[] textures = new int[MAX_TEXTURE_ARRAY_SIZE];
 
 	public TileMap()
 	{
 		// a float has 4 bytes so we allocate for each coordinate 4 bytes
 		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
 		byteBuffer.order(ByteOrder.nativeOrder());
 		
 		// allocates the memory from the byte buffer
 		vertexBuffer = byteBuffer.asFloatBuffer();
 		
 		// fill the vertexBuffer with the vertices
 		vertexBuffer.put(vertices);
 		
 		// set the cursor position to the beginning of the buffer
 		vertexBuffer.position(0);
 		
 		byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
 		byteBuffer.order(ByteOrder.nativeOrder());
 		textureBuffer = byteBuffer.asFloatBuffer();
 		textureBuffer.put(texture);
 		textureBuffer.position(0);
 		
 		textureHopper = new ArrayList<Integer>(MAX_TEXTURE_ARRAY_SIZE);
 		
 		//***********************TESTING SETUP**************************
 		/*
 		//Textures
 		this.addTexture(R.drawable.tile2);
 		this.addTexture(R.drawable.grass);
 		this.addTexture(R.drawable.stone);
 		this.addTexture(R.drawable.brick);
 		//Map&Tile Size
 		this.setMapSize(100,100);
 		this.setTileSize(1);
 		
 		//Figure some stuff about the map
 		tileStep = (tileSize*2);
 		rightMapEdge = mapWidth-1;
 		topMapEdge = mapHeight-1;
 		
 		tileLocations = new float[100][100][2];
 		tileTextures = new int[100][100][1];
 
 		drawBuffer = new ArrayList<int[][]>(numTextures);
 		drawBufferCount = new int[numTextures];
 		
 		for(int i=0;i<numTextures;i++)
 		{
 			drawBuffer.add(new int[MAX_TILES_PER_FRAME][2]);
 			drawBufferCount[i] = 0;
 		}
 		
 		int xCount = -102;
 		int yCount = 100;
 		
 		//Set Tile Locations/Textures
 		for(int i=0;i<100;i++)
 		{
 			for(int j=0;j < 100;j++)
 			{
 				tileLocations[j][i][0] = xCount+=tileStep;
 				tileLocations[j][i][1] = yCount;
 				//Texture
 				tileTextures[j][i][0] = 0;
 			}
 			yCount-=tileStep;
 			xCount = -102;
 		}
 		
 		//Set random test textures
 		tileTextures[50][50][0] = 2;
 		tileTextures[51][50][0] = 2;
 		tileTextures[52][50][0] = 2;
 		tileTextures[53][50][0] = 2;
 		tileTextures[54][50][0] = 2;
 		
 		tileTextures[54][52][0] = 1;
 		tileTextures[49][53][0] = 1;
 		tileTextures[51][52][0] = 3;
 		tileTextures[54][55][0] = 3;
 		*/
 		
 	}
 	
 	public void loadMapFile(String mapFile, Context context)
 	{
 		Scanner sc;
 		int nTexturesInMap = 0;
 		
 		Log.v("bitbot", "Loading Map...");
 
 		try
 		{			
 		    InputStream mapFileStream = context.getAssets().open(mapFile);
 			sc = new Scanner(mapFileStream);
 
 			//Dump current texture set
 			numTextures = 0;
 			textureHopper.clear();
 		  
 			//Read basic map information
			mapWidth = Integer.parseInt(sc.next());
			mapHeight = Integer.parseInt(sc.next());
 		  
			tileSize = Integer.parseInt(sc.next());
			tileStep = Integer.parseInt(sc.next());
 		  
			nTexturesInMap = Integer.parseInt(sc.next());
 		  
 			//Figure some stuff about the map
 			//tileStep = (tileSize*2);
 			rightMapEdge = mapWidth-1;
 			topMapEdge = mapHeight-1;
 			
 			//Generate draw buffer
 			drawBuffer = new ArrayList<int[][]>(nTexturesInMap);
 			drawBufferCount = new int[nTexturesInMap];
 	          
 	  		//Generate Local Arrays
 	  		tileLocations = new float[mapWidth][mapHeight][2];
 	  		tileTextures = new int[mapWidth][mapHeight][1];
 	  		tileBoundaries = new int[mapWidth][mapHeight][1];
 	  		tileCodes = new int[mapWidth][mapHeight][1];
 	          
 	        //Generate meta tiles
 	  		int xCount = (((mapWidth * tileStep)/2) * -1) - tileStep;
 	  		int storedXCount = xCount;
 	  		int yCount = mapHeight;
 	  		
 	  		//Set Tile Locations/Textures
 	  		for(int i=0;i<mapHeight;i++)
 	  		{
 	  			for(int j=0;j < mapWidth;j++)
 	  			{
 	  				//Set tile meta info
 	  				tileLocations[j][i][0] = xCount+=tileStep;
 	  				tileLocations[j][i][1] = yCount;
 	  				//Set default texture
 	  				tileTextures[j][i][0] = 0;
 	  			}
 	  			//Reset x-position like a typewriter, and return a line. (Ching!)
 	  			yCount-=tileStep;
 	  			xCount = storedXCount;
 	  		}
 	  		//Initialize draw buffer for efficient drawing
 	        for(int i=0;i<nTexturesInMap;i++)
 	        {
 	        	System.out.println("draw buffer setup" + i);
 	        	drawBuffer.add(new int[MAX_TILES_PER_FRAME][2]);
 	        	drawBufferCount[i] = 0;
 	        }
 	        //Read textures
 	        for(int i=0;i<mapHeight;i++)
 	        {
 	        	for(int j=0;j<mapWidth;j++)
 	        	{
	        		tileTextures[j][i][0] = Integer.parseInt(sc.next());
 	        	}
 	        }
 	        //Read boundaries
 	        for(int i=0;i<mapHeight;i++)
 	        {
 	        	for(int j=0;j<mapWidth;j++)
 	        	{
	        		tileBoundaries[j][i][0] = Integer.parseInt(sc.next());
 	        	}
 	        }
 	        //Read Map Codes
 	        for(int i=0;i<mapHeight;i++)
 	        {
 	        	for(int j=0;j<mapWidth;j++)
 	        	{
	        		tileCodes[j][i][0] = Integer.parseInt(sc.next());
 	        	}
 	        }
 	        //Read and load textures ***************************NEEDS FIXED**********************************
 	        //for(int i=0;i<1;i++)
 	        //{
 	        	  this.addTexture(R.drawable.deftile);
 	        	  this.addTexture(R.drawable.seltile);
 	        	  this.addTexture(R.drawable.stone);
 	        	  this.addTexture(R.drawable.brick);
 	        	  this.addTexture(R.drawable.grass);
 	        	  this.addTexture(R.drawable.sandtile);
 	          //}
 	          mapFileStream.close();
 	          Log.v("bitbot", "Map loading complete.");
 	      //}
 		}
 		catch(Exception e)
 		{
 			System.out.println("Error loading map file: " + e.toString());
 		}
 	}
 	
 	public void setSelectedTexture(int selectedTex)
 	{
 		SELECTED_TEXTURE = selectedTex;
 	}
 	
 	public int getSelectedTexture()
 	{
 		return SELECTED_TEXTURE;
 	}
 	
 	public void addTexture(int texturePointer)
 	{
 		textureHopper.add(texturePointer);
 		numTextures++;
 	}
 	
 	public void setMapSize(int width, int height)
 	{
 		mapWidth = width;
 		mapHeight = height;
 	}
 	
 	public void setTileSize(int tSize)
 	{
 		tileSize = tSize;
 	}
 	
 	public void setDrawRegion(float x1, float x2, float y1, float y2)
 	{
 		drawX1 = x1;
 		drawX2 = x2;
 		drawY1 = y1;
 		drawY2 = y2;
 	}
 	
 	public void setTileTextures(int[][][] textures)
 	{
 		tileTextures = textures;
 	}
 	
 	public void setTileCodes(int[][][] codes)
 	{
 		tileCodes = codes;
 	}
 
 	public void loadGLTexture(GL10 gl, Context context, int curTexPointer)
 	{		
 		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),textureHopper.get(curTexPointer));
 
 		// generate one texture pointer
 		gl.glGenTextures(1, textures, curTexPointer);
 		// ...and bind it to our array
 		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[curTexPointer]);
 		
 		// create nearest filtered texture
 		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
 		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
 		
 		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
 		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
 		
 		// Clean up
 		bitmap.recycle();
 		
 		//textureLoaded = true;
 	}
 
 	
 	public void draw(GL10 gl)
 	{
 		//Figure some stuff about the draw region
 		//Left Draw Bound
 		LDrawPoint = (int)Math.floor(drawX1);
 		if((LDrawPoint%tileStep) !=0)
 		{
 			LDrawPoint++;
 		}
 		//Right Draw Bound
 		RDrawPoint = (int)Math.ceil(drawX2);
 		if((RDrawPoint%tileStep) !=0)
 		{
 			RDrawPoint--;
 		}
 		//Top Draw Bound
 		TDrawPoint = (int)Math.ceil(drawY1);
 		if((TDrawPoint%tileStep) != 0)
 		{
 			TDrawPoint--;	//NOTE: ORIGINALL WORKING AS TDrawPoint++ !
 		}
 		//Bottom Draw Bound
 		BDrawPoint = (int)Math.floor(drawY2);
 		if((BDrawPoint%tileStep) !=0)
 		{
 			BDrawPoint--;
 		}
 		
 		// Point to our buffers
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 		
 		// Set the face rotation
 		gl.glFrontFace(GL10.GL_CW);
 		
 		// Point to our vertex buffer
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
 		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
 		
 		int startXIndex = rightMapEdge - ((rightMapEdge-LDrawPoint)/tileStep);
 		int endXIndex = rightMapEdge - ((rightMapEdge-RDrawPoint)/tileStep)+1;
 		int startYIndex = topMapEdge - ((topMapEdge + TDrawPoint)/tileStep);
 		int endYIndex = topMapEdge - ((topMapEdge + BDrawPoint)/tileStep)+1;
 		
 		//Reset Texture Draw Counts
 		for(int i=0;i<numTextures;i++)
 		{
 			//textureDrawCount[i] = 0;
 			drawBufferCount[i] = 0;
 		}
 		
 		//Populate Texture Draw Buffer
 		for(int i=startYIndex;i<endYIndex;i++)
 		{
 			for(int j=startXIndex;j<endXIndex;j++)
 			{
 				drawBuffer.get(tileTextures[j][i][0])[drawBufferCount[tileTextures[j][i][0]]][0] = j; //X
 				drawBuffer.get(tileTextures[j][i][0])[drawBufferCount[tileTextures[j][i][0]]][1] = i; //Y
 				drawBufferCount[tileTextures[j][i][0]]++;
 			}
 		}
 		//System.out.println(drawBufferCount[0]);
 		
 		//Draw Tiles in View with Appropriate Texture
 		for(int i=0;i<numTextures;i++)
 		{
 			//If a tile with the current texture exists in the frame bind and draw
 			if(drawBufferCount[i] > 0)
 			{
 				gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
 				for(int j=0;j<drawBufferCount[i];j++)
 				{
 		        	gl.glLoadIdentity();
 					gl.glTranslatef(tileLocations[drawBuffer.get(i)[j][0]][drawBuffer.get(i)[j][1]][0], tileLocations[drawBuffer.get(i)[j][0]][drawBuffer.get(i)[j][1]][1], -5.0f);
 					gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); //Draw Tile
 				}
 			}
 		}
 		
 		//Disable the client state before leaving
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 }
