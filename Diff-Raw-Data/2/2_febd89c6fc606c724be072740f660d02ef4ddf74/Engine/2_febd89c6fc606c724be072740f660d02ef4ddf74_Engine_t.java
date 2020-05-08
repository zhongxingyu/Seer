 package engine;
 
 import java.nio.ByteBuffer;
 
 import opengl.BufferLoader;
 import opengl.FXOpenGL;
 import opengl.FXTexture;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.newdawn.slick.opengl.Texture;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class Engine
 {
 	// Static Attributes
 	private static FXOpenGL openGL;
 
 	// Static Getter Methods
 	public static FXOpenGL getOpenGL()
 	{
 		return Engine.openGL;
 	}
 	public static int getWidth()
 	{
 		return Display.getWidth();
 	}
 	public static int getHeight()
 	{
 		return Display.getHeight();
 	}
 	public static int getX()
 	{
 		return Display.getX();
 	}
 	public static int getY()
 	{
 		return Display.getY();
 	}
 
 	// Static Setter methods
 	public static void setTitle(String title)
 	{
 		Display.setTitle(title);
 	}
 	public static void setBrightness(int gamma,int brightness,int contrast)
 	{
 		try
 		{
 			Display.setDisplayConfiguration(gamma,brightness,contrast);
 		}
 		catch(LWJGLException e)
 		{
 			System.out.println("Could not configure Display");
 			e.printStackTrace();
 		}
 	}
 	public static void setResolution(int width,int height)
 	{
 		try
 		{
 			Display.setDisplayMode(new DisplayMode(width,height));
 		}
 		catch(LWJGLException e)
 		{
 			System.out.println("Could not change Resolution");
 			e.printStackTrace();
 		}
 	}
 	public static void fullscreen()
 	{
 		try
 		{
 			Display.setFullscreen(true);
 		}
 		catch(LWJGLException e)
 		{
 			System.out.println("Could not change to Fullscreen");
 			e.printStackTrace();
 		}
 	}
 	public static void windowed()
 	{
 		try
 		{
 			Display.setFullscreen(false);
 		}
 		catch(LWJGLException e)
 		{
 			System.out.println("Could not change to Windowed");
 			e.printStackTrace();
 		}
 	}
 
 	public void setIcon(String filename)
 	{
 		Display.setIcon(BufferLoader.loadIcon(filename));
 	}
 
 	// Constructor
 	public Engine(int width,int height)
 	{
 		// Create the Display
 		try
 		{
 			Display.setDisplayMode(new DisplayMode(width,height));
 			Display.create();
 		}
 		catch(LWJGLException e)
 		{
 			System.out.println("Display could not be created!");
 			e.printStackTrace();
 		}
 
 		// Run OpenGL
 		Engine.openGL=new FXOpenGL();
 	}
 
 	// FIXME: This is only Temporary / Replace with SceneManager
 	public void testLoop()
 	{
 		FXTexture texture=Engine.getOpenGL().getTextureManager()
 				.addTexture("default","imgs/test.png");
		Engine.getOpenGL().getTextureManager().bindTexture("default");
 
 		while(!Display.isCloseRequested())
 		{
 			glBegin(GL_QUADS);
 			glTexCoord2f(0,0);
 			glVertex2f(100,100);
 			glTexCoord2f(1,0);
 			glVertex2f(100+texture.getTextureWidth(),100);
 			glTexCoord2f(1,1);
 			glVertex2f(100+texture.getTextureWidth(),
 					100+texture.getTextureHeight());
 			glTexCoord2f(0,1);
 			glVertex2f(100,100+texture.getTextureHeight());
 			glEnd();
 
 			Display.update();
 		}
 		Display.destroy();
 	}
 }
