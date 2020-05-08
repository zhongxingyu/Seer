 package saneGUI;
 
 import java.awt.FontFormatException;
 import java.io.IOException;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.tiled.Base64.InputStream;
 import org.newdawn.slick.util.ResourceLoader;
 
 import saneMain.Cleansanity;
 import saneMap.MapArea;
 
 /**
  * Handles the game window's actions.
  * Resolution, window state, render states, all handled by this.
  * Some features of this class may be offloaded to other classes to simplify the code.
  */
 
 
 public class GameMainWindow {
 
 	//Self explanatory
 	public static int framesPerSecond = 60;
 	
 	//Default resolution
 	public static int xResolutionDefault = 600;
 	public static int yResolutionDefault = 300;
 
 	//Whether something wants the game window to close.
 	public static boolean closeRequested;
 
 	//Whether certain things are currently being drawn.
 	public static boolean drawingGame;
 	public static boolean drawingOverlay;
 	public static boolean drawingMenu;
 
 	//Whether the window has been created
 	private static boolean windowCreated;
 
 	//Different fonts for different things.
 	private static java.awt.Font interfaceFontAWT;
 	private static TrueTypeFont interfaceFontSLICK;
 
 	private static java.awt.Font interfaceFontAWT2;
 	private static TrueTypeFont interfaceFontSLICK2;
 
 	public static void createWindow(){
 
 
 
 		try {
 
 			Display.setDisplayMode(new DisplayMode(xResolutionDefault , yResolutionDefault));
 			Display.create();
 
 			windowCreated = true;
 
 		} catch (LWJGLException e){
 			e.printStackTrace();
 		}
 
 		java.io.InputStream fontIn = ResourceLoader.getResourceAsStream("module/cleansanity/fonts/Ubuntu-M.ttf");
 		try {
 			interfaceFontAWT = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontIn);
 			interfaceFontSLICK = new TrueTypeFont(interfaceFontAWT.deriveFont(24f) , false);
 		} catch (FontFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 
 		fontIn = ResourceLoader.getResourceAsStream("module/cleansanity/fonts/telegrama_render.otf");
 		try {
 			interfaceFontAWT2 = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontIn);
 			interfaceFontSLICK2 = new TrueTypeFont(interfaceFontAWT2.deriveFont(24f) , false);
 		} catch (FontFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		GL11.glShadeModel(GL11.GL_SMOOTH);       
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glDisable(GL11.GL_LIGHTING);                   
 
 		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               
 		GL11.glClearDepth(1);                                      
 
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
 		GL11.glViewport(0,0,xResolutionDefault,yResolutionDefault);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glOrtho(0, xResolutionDefault, yResolutionDefault, 0, 100, -100);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		
 		Display.setResizable(true);
 		
 	}
 
 	public static void setTitle(String title){
 		Display.setTitle(title);
 	}
 
 
 	public static void updateGraphics(){
 		
		// DEBUG statement
 		drawingGame = true;
 		
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); 
 		interfaceFontSLICK.drawString(100, 100, "What what what what what what");
 		interfaceFontSLICK2.drawString(100, 150, "? ! ? ! ? ! ? ! ? ! ? ! ? ! ?");
 
 		Cleansanity theSanity = Cleansanity.getSanity();
 		MapArea theMap = theSanity.getCurrentMap();
 		
 		if (drawingGame){
 
 			//DRAW GAME THINGS
 			
 			//Draw the map
 			for (int x = 0; x < theMap.getXSize(); x ++){
 				for (int y = 0; y < theMap.getYSize(); y ++){
 					setGLColour(theMap.getTile(x, y).getFloorColour());
 					int x1 = x * 10;
 					int x2 = x1 + 10;
 					int y1 = y * 10;
 					int y2 = y1 + 10;
 					GL11.glBegin(GL11.GL_QUADS);
 					GL11.glVertex3f(x1, y1, 0);
 					GL11.glVertex3f(x1, y2, 0);
 					GL11.glVertex3f(x2, y2, 0);
 					GL11.glVertex3f(x2, y1, 0);
 					GL11.glEnd();
 				}
 			}
 			
 			
 			
 
 		}
 
 		if (drawingOverlay){
 
 			//DRAW OVERLAY THINGS
 
 		}
 
 		if (drawingMenu){
 
 			//DRAW MENU THINGS
 
 		}
 
 
 
 
 		Display.update();
 		Display.sync(framesPerSecond);
 		closeRequested = Display.isCloseRequested();
 	}
 
 	public static int getXRes(){
 		return Display.getWidth();
 	}
 	public static int getYRes(){
 		return Display.getHeight();
 	}
 
 	public static void setGLColour(Colour theColour){
 		GL11.glColor4f(theColour.r(), theColour.g(), theColour.b(), theColour.a());
 	}
 
 }
