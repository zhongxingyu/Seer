 package jworldgen.test;
 
 import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
 import static org.lwjgl.opengl.GL11.glEnable;
 
 import java.util.Hashtable;
 
 import jworldgen.exceptionHandler.CriticalFailure;
 import jworldgen.generator.Generator;
 import jworldgen.generator.World;
 import jworldgen.util.Util;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.GLU;
 
 class Color{
 	public float r;
 	public float g;
 	public float b;
 	public Color(float r, float g, float b)
 	{
 		this.r = r;
 		this.g = g;
 		this.b = b;
 	}
 }
 public class TestStartup {
 	
 	
 	private static final float ROT_SPEED = 3;
 	private static int cuttingDepth = 0;
 	private static boolean pressed = false;
 	private static boolean drawWater = true;
 	private static void setColor(int hue, float sat, float val, int max)
 	{
 		int h = 6*hue/max;
 		float f = 6*(float) hue/max - h;
 		float p = val*(1-sat);
 		float q = val*(1-sat*f);
 		float t = val*(1-sat*(1-f));
 		if (h == 0 || h == 6)
 			GL11.glColor4f(val,t,p,1);
 		if (h == 1)
 			GL11.glColor4f(q,val,p,1);
 		if (h == 2)
 			GL11.glColor4f(p,val,t,1);
 		if (h == 3)
 			GL11.glColor4f(p,q,val,1);
 		if (h == 4)
 			GL11.glColor4f(t,p,val,1);
 		if (h == 5)
 			GL11.glColor4f(val,p,q,1);
 	}
 	
 	private static boolean hasInput(int direction) 
 	{
 	    switch(direction) 
 	    {
 	    	case Keyboard.KEY_LEFT:
 	        return
 	          Keyboard.isKeyDown(Keyboard.KEY_LEFT);
 
 	      case Keyboard.KEY_RIGHT:
 	        return
 	          Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
 
 	      case Keyboard.KEY_SPACE:
 	        return
 	          Keyboard.isKeyDown(Keyboard.KEY_SPACE);
 	    }
 		return false;
 	}
 	static World world;
 	static byte[][][] drawThis;
 	static Hashtable<Integer,String> blockMap;
 	static Hashtable<String,Color> colorMap;
 	
 	private static void setColorMap()
 	{
 		colorMap = new Hashtable<String,Color>();
 		colorMap.put("Water", new Color(0,0.2f,1));
 		colorMap.put("Sand", new Color(0.9f,0.9f,0.5f));
 		colorMap.put("Gold", new Color(0.8f,0.8f,0));
 		colorMap.put("Stone", new Color(0.5f,0.5f,0.5f));
 		colorMap.put("Coal", new Color(0.2f,0.2f,0.2f));
 		colorMap.put("Earth", new Color(0.4f,0.2f,0));
 		colorMap.put("Iron", new Color(0.5f,0.3f,0.4f));
 	}
 	private static void setDrawingStates()
 	{
 		for (int i = cuttingDepth; i < world.getWidth() - cuttingDepth; i++) 
 		{
 			for (int j = cuttingDepth; j < world.getHeight() - cuttingDepth; j++) 
 			{
 				for (int k = cuttingDepth; k < world.getDepth() - cuttingDepth; k++) 
 				{
 					drawThis[i][j][k] = (byte) (world.getValue(i, j, k) == 0 ? 0 : 1);
 					if (!drawWater && blockMap.get(world.getValue(i, j, k)).equals("Water"))
 						drawThis[i][j][k] = 0;
 				}
 			}
 		}
 		for (int i = cuttingDepth + 1; i < world.getWidth()-1 - cuttingDepth; i++) 
 		{
 			for (int j = cuttingDepth + 1; j < world.getHeight()-1 - cuttingDepth; j++) 
 			{
 				for (int k = cuttingDepth + 1; k < world.getDepth()-1 - cuttingDepth; k++) 
 				{
 					if (drawThis[i][j][k] == 1)
 					{
 						drawThis[i][j][k] = 2;
 						if (drawThis[i+1][j][k] == 0)
 							drawThis[i][j][k] = 1;
 						else if (drawThis[i-1][j][k] == 0)
 							drawThis[i][j][k] = 1;
 						else if (drawThis[i][j+1][k] == 0)
 							drawThis[i][j][k] = 1;
 						else if (drawThis[i][j-1][k] == 0)
 							drawThis[i][j][k] = 1;
 						else if (drawThis[i][j][k+1] == 0)
 							drawThis[i][j][k] = 1;
 						else if (drawThis[i][j][k-1] == 0)
 							drawThis[i][j][k] = 1;
 					}
 				}
 			}
 		}
 	}
 	
 	private static float heightFactor;
 	private static float widthFactor;
 	private static void load()
 	{
 		try {
 			long seed = (long) Math.floor((Math.random()*Long.MAX_VALUE));
 			Display.setTitle("Seed: "+Util.seedToSeedString(seed));
			Generator gen = Generator.getGeneratorFromFile("data/TestRules.txt", seed);
			world = gen.generateRandomly(200,200,1);
 			drawThis = new byte[world.getWidth()][world.getHeight()][world.getDepth()];
 			blockMap = gen.getBlockMap();
 			setDrawingStates();
 		} catch (CriticalFailure e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private static void setup3D()
 	{
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GLU.gluPerspective(45.0f, 1.0f, 0.5f, 400.0f);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
 		GL11.glCullFace(GL11.GL_FRONT); // Doesn't draw back faces
 		GL11.glTranslatef(0, -10, -1.5f*(Math.max(world.getWidth(),world.getHeight())+world.getDepth()));
 		GL11.glRotatef(20, 1, 0, 0);
 		GL11.glPushMatrix();
 	}
 	
 	private static void setup2D()
 	{
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glOrtho(0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(), 0, 1, -1);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		widthFactor = Display.getDisplayMode().getWidth()/world.getWidth();
 		heightFactor = Display.getDisplayMode().getHeight()/world.getHeight();
 	}
 	
 	private static void determineColor(int color)
 	{
 		String name = blockMap.get(color);
 		if (colorMap.containsKey(name))
 		{
 			Color col = colorMap.get(name);
 			GL11.glColor4f(col.r, col.g, col.b, 1f);
 		}
 		else
 			setColor(color, 0.5f, color / 10.0f, 10);
 	}
 	private static void render3D()
 	{
 		// Clear the screen and depth buffer
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		GL11.glPushMatrix();
 		GL11.glTranslatef(-0.5f*world.getWidth(), 0.5f*world.getHeight(), 0.5f*world.getDepth());
 
 		for (int i = cuttingDepth; i < world.getWidth() - cuttingDepth; i++) 
 		{
 			GL11.glTranslatef(i, 0.0f, 0.0f);
 			for (int j = cuttingDepth; j < world.getHeight() - cuttingDepth; j++) 
 			{
 				GL11.glTranslatef(0.0f, -j, 0.0f);
 				for (int k = cuttingDepth; k < world.getDepth() - cuttingDepth; k++) 
 				{
 					GL11.glTranslatef(0.0f, 0.0f, -k);
 					if (drawThis[i][j][k] == 1) 
 					{
 						determineColor(world.getValue(i, j, k));
 						GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,
 								GL11.GL_FILL);
 						// draw quad
 						glEnable(GL11.GL_CULL_FACE);
 						glEnable(GL11.GL_POLYGON_OFFSET_FILL);
 						GL11.glPolygonOffset(1, 1);
 						GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
 							GL11.glVertex3f(1, 1, 0);
 							GL11.glVertex3f(0, 1, 0);
 							GL11.glVertex3f(1, 0, 0);
 							GL11.glVertex3f(0, 0, 0);
 							GL11.glVertex3f(0, 0, 1);
 							GL11.glVertex3f(0, 1, 0);
 							GL11.glVertex3f(0, 1, 1);
 							GL11.glVertex3f(1, 1, 0);
 							GL11.glVertex3f(1, 1, 1);
 							GL11.glVertex3f(1, 0, 0);
 							GL11.glVertex3f(1, 0, 1);
 							GL11.glVertex3f(0, 0, 1);
 							GL11.glVertex3f(1, 1, 1);
 							GL11.glVertex3f(0, 1, 1);
 						GL11.glEnd();
 						GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
 						GL11.glColor3f(0.05f, 0.05f, 0.05f);
 						GL11.glDisable(GL11.GL_CULL_FACE);
 						GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
 						// draw quad
 						GL11.glBegin(GL11.GL_LINE_STRIP);
 							GL11.glVertex3f(0, 0, 0);
 							GL11.glVertex3f(0, 1, 0);
 							GL11.glVertex3f(1, 1, 0);
 							GL11.glVertex3f(1, 0, 0);
 							GL11.glVertex3f(0, 0, 0);
 							GL11.glVertex3f(0, 0, 1);
 							GL11.glVertex3f(0, 1, 1);
 							GL11.glVertex3f(1, 1, 1);
 							GL11.glVertex3f(1, 0, 1);
 							GL11.glVertex3f(0, 0, 1);
 						GL11.glEnd();
 						GL11.glBegin(GL11.GL_LINES);
 							GL11.glVertex3f(1, 0, 0);
 							GL11.glVertex3f(1, 0, 1);
 							GL11.glVertex3f(0, 1, 0);
 							GL11.glVertex3f(0, 1, 1);
 							GL11.glVertex3f(1, 1, 0);
 							GL11.glVertex3f(1, 1, 1);
 						GL11.glEnd();
 					}
 					GL11.glTranslatef(0.0f, 0.0f, k);			
 				}
 				GL11.glTranslatef(0.0f, j, 0.0f);
 			}
 			GL11.glTranslatef(-i, 0.0f, 0.0f);
 		}
 		Display.update();
 		GL11.glPopMatrix();
 		boolean leftPressed   = hasInput(Keyboard.KEY_LEFT);
 		boolean rightPressed   = hasInput(Keyboard.KEY_RIGHT);
 		if (Keyboard.isKeyDown(Keyboard.KEY_A))
 			GL11.glRotatef(-ROT_SPEED, 0, 1, 0);
 		if (Keyboard.isKeyDown(Keyboard.KEY_D))
 			GL11.glRotatef(ROT_SPEED, 0, 1, 0);
 		if (Keyboard.isKeyDown(Keyboard.KEY_W))
 			GL11.glRotatef(-ROT_SPEED, 1, 0, 0);
 		if (Keyboard.isKeyDown(Keyboard.KEY_S))
 			GL11.glRotatef(ROT_SPEED, 1, 0, 0);
 		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
 			GL11.glRotatef(-ROT_SPEED, 0, 0, 1);
 		if (Keyboard.isKeyDown(Keyboard.KEY_E))
 			GL11.glRotatef(ROT_SPEED, 0, 0, 1);
 		if (Keyboard.isKeyDown(Keyboard.KEY_ADD))
 			GL11.glTranslatef(0, 0, 10);
 		if (Keyboard.isKeyDown(Keyboard.KEY_SUBTRACT))
 			GL11.glTranslatef(0, 0, -10);
 		if (Keyboard.isKeyDown(Keyboard.KEY_R))
 		{
 			GL11.glPopMatrix();
 			GL11.glPushMatrix();
 			load();
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_T))
 		{
 			drawWater = !drawWater;
 			setDrawingStates();
 		}
 		if (leftPressed && !pressed) {
 			cuttingDepth++;
 			setDrawingStates();
 			pressed = true;
 		} else if (rightPressed && !pressed) {
 			cuttingDepth--;
 			if (cuttingDepth < 0)
 				cuttingDepth = 0;
 			else
 				setDrawingStates();
 			pressed = true;
 		}
 
 		if (!leftPressed && !rightPressed)
 		{
 			pressed = false;
 		}
 	}
 	
 	private static void render2D()
 	{
 		// Clear the screen and depth buffer
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);	
 		for (int k = 0; k < world.getDepth(); k++)
 		{
 			for (int i =0; i < world.getWidth(); i++)
 			{
 				for (int j = 0; j < world.getHeight(); j++)
 				{
 					determineColor(world.getValue(i, j, k));
 					// draw quad
 					GL11.glBegin(GL11.GL_QUADS);
 					    GL11.glVertex2f(widthFactor*i,heightFactor*j);
 					    GL11.glVertex2f(widthFactor*(i+1),heightFactor*j);
 					    GL11.glVertex2f(widthFactor*(i+1),heightFactor*(j+1));
 					    GL11.glVertex2f(widthFactor*i,heightFactor*(j+1));
 					GL11.glEnd();
 				}
 			}
 			Display.update();
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_R))
 		{
 			load();
 		}
 	}
 	
	static boolean use3D = false;
 	public static void main(String[] args)
 	{
 		try {
 			Display.setDisplayMode(new DisplayMode(1000,1000));
 			Display.create();
 			load();
 			setColorMap();
 			if (use3D)
 				setup3D();
 			else
 				setup2D();
 			while(!Display.isCloseRequested())
 			{
 				if (use3D)
 					render3D();
 				else
 					render2D();
 			}
 			Display.destroy();
 		} catch (LWJGLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
