 package tsa2035.game.engine.core;
 
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.openal.AL;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
import tsa2035.game.content.core.Game;
 import tsa2035.game.engine.scene.Scene;
 
 public class Renderer {
 	private static int screenX, screenY;
 	private static Scene currentScene = null;
 	private static Scene nextScene = null;
 	private static float currentAlpha = 1;
 	private static int switchState = 2;
 	public static void init(int screenX, int screenY) throws LWJGLException
 	{
 		Renderer.screenX = screenX;
 		Renderer.screenY = screenY;
 		Display.setDisplayMode(new DisplayMode(screenX, screenY));
 		Display.create();
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glOrtho(0, screenX, screenY, 0, 0, 0);
 	}
 	
 	private static void renderSceneFade(float alpha)
 	{
 		glColor4f(0,0,0,alpha);
 		glBegin(GL_QUADS);
 		
 		glVertex2f(1, 1);
 		glVertex2f(1, -1);
 		glVertex2f(-1, -1);
 		glVertex2f(-1, 1);
 
 		glEnd();
 	}
 	
 	public static void renderLoop(Scene firstScene) throws LWJGLException
 	{
 		setScene(firstScene);
 		while ( !Display.isCloseRequested() )
 		{
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
 	        GL11.glLoadIdentity();
 	        
 	        if ( currentScene != null )
 	        {
 	        	
 	        	switch ( switchState )
 	        	{
 	        		case 0:
 	        			currentAlpha += 0.01;
 	        			if ( currentAlpha >= 1 )
 	        				switchState++;
 	        			break;
 	        		case 1:
 	        			if ( nextScene == null )
 	        				switchState = -1;
 	        			else
 	        			{
 		        			setScene(nextScene);
 		        			switchState++;
 	        			}
 	        			break;
 	        		case 2:
 	        			currentAlpha -= 0.01;
 	        			if ( currentAlpha <= 0 )
 	        				switchState = -1;
 	        			break;
 	        	}
 	        	
 	        	if ( !Keyboard.isCreated() )
 	        		Keyboard.create();
 	        	Keyboard.poll();
 	        	GL11.glMatrixMode(GL11.GL_MODELVIEW);
 	        	GL11.glLoadIdentity();
 	        	GL11.glEnable(GL11.GL_TEXTURE_2D);
 	        	currentScene.render();
 	        	GL11.glDisable(GL11.GL_TEXTURE_2D);
 	        	renderSceneFade(currentAlpha);
 
 	        	GL11.glFlush();
 	        	Display.sync(60);
 	        	Display.update();
 	        }
 		}
 		
 		Display.destroy();
 		AL.destroy();
 		System.exit(0);
 	}
 
 	public static int getScreenX()
 	{
 		return screenX;
 	}
 	
 	public static int getScreenY()
 	{
 		return screenY;
 	}
 	
 	public static void setScene(Scene s)
 	{
 		currentScene = s;
 	}
 	
 	public static Scene getScene()
 	{
 		return currentScene;
 	}
 	
 	public static void animatedSceneSwitch(Scene newScene)
 	{
 		nextScene = newScene;
 		switchState = 0;
 		currentAlpha = 0;
 	}
 	
 	public static void jumpSceneSwitch(Scene newScene)
 	{
 		setScene(newScene);
 		switchState = -1;
 		currentAlpha = 0;
 	}
 }
