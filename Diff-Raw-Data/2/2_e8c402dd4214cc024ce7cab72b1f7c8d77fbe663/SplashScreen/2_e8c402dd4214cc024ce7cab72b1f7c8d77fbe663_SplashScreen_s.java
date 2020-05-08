 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.matej.tanks.gameStates;
 
 import me.matej.tanks.GameState;
 import me.matej.tanks.Main;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 /**
  *
  * @author 12039762
  */
 public class SplashScreen extends GameStateClass {
 	
	public static String splashPath = "me/matej/Tanks/resources/SplashScreen.png";
 	public Texture splash;
 	public long splashTimeLeft;
 	
 	public Main main;
 	
 	public SplashScreen () {}
 	
 	public void init () {
 		try {
 			splash = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(splashPath));
 		} catch (Exception e) {
 			e.printStackTrace(System.err);
 		}
 		
 		splashTimeLeft = 2000;
 	}
 	
 	@Override
 	public void update (int delta) {
 		splashTimeLeft -= delta;
 		if (splashTimeLeft <= 0) {
 			active = false;
 			GameState.MainMenu.getStateInstance().active = true;
 			splashTimeLeft = 0;
 			GL11.glDisable(GL11.GL_TEXTURE_2D);
 			//main.gamePaused = false;
 		}
 	}
 	
 	@Override
 	public void draw () {
 		GL11.glLoadIdentity();
 		
 		Color.white.bind();
 		splash.bind();
 		
 		int texW = splash.getTextureWidth(), texH = splash.getTextureHeight();
 		
 		DisplayMode dp = Main.getInstance().getOpenGL().getDisplayMode();
 		
 		GL11.glTranslatef(dp.getWidth()/2 - 384/2, dp.getHeight()/2 - texH/2, 0f);
 		
 		GL11.glBegin(GL11.GL_QUADS);
 			GL11.glTexCoord2f(0f,0f);
 			GL11.glVertex2f(0f,0f);
 			GL11.glTexCoord2f(1f,0f);
 			GL11.glVertex2f(texW,0f);
 			GL11.glTexCoord2f(1f,1f);
 			GL11.glVertex2f(texW,texH);
 			GL11.glTexCoord2f(0f,1f);
 			GL11.glVertex2f(0f,texH);
 		GL11.glEnd();
 	}
 
 	@Override
 	public void keyPressed(int key) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void mouseButtonPressed(int index) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 	
 }
