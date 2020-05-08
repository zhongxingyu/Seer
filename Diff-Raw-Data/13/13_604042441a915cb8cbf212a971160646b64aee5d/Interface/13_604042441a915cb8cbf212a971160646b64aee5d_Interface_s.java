 package main;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 
 import glui.Color;
 import glui.GLButton;
 import glui.GLUIException;
 import glui.GLUITheme;
 import glui.InvalidEscapeSequenceException;
 
 public abstract class Interface {
 
 	protected GLUITheme theme;
 	private GLButton exitButton;
 	
 	private boolean loop;
 	
 	private String title;
 	
 	private Color foreground, background;
 	
 	private boolean drawBoxes;
 	
 	public static double margin = 0.1;
 	public static int leftBackMargin = (int) (margin*Main.SCREEN_PLAYABLE_WIDTH);
 	public static int rightBackMargin = (int) ((1-margin)*Main.SCREEN_PLAYABLE_WIDTH);
 	public static int topBackMargin = (int) (margin*Main.SCREEN_PLAYABLE_HEIGHT);
 	public static int bottomBackMargin = (int) ((1-margin)*Main.SCREEN_PLAYABLE_HEIGHT);
 	
 	public static int leftFrontMargin = (int) (3.0*margin*Main.SCREEN_PLAYABLE_WIDTH/2.0);
 	public static int rightFrontMargin = (int) (17.0*(1-margin)/18.0*Main.SCREEN_PLAYABLE_WIDTH);
 	public static int topFrontMargin = (int) (3.0*margin/2.0*Main.SCREEN_PLAYABLE_HEIGHT);
 	public static int bottomFrontMargin = (int) (17.0*(1-margin)/18.0*Main.SCREEN_PLAYABLE_HEIGHT);
 	
 	int textX, numTilesX;
 	
 	public Interface(String title, Color foreground, Color background) {
 		
 		this.title = title;
 		
 		this.foreground = foreground;
 		this.background = background;
 		
 		try {
 			theme = new GLUITheme();
 			theme.setBackgroundColor(new Color("FFFFFF"));
 			theme.setTextColor(new Color("000000"));
 		} catch (GLUIException e) { }
 		
 		exitButton = new GLButton("X", theme, (int)(0.11*Main.SCREEN_PLAYABLE_WIDTH), (int)(0.11*Main.SCREEN_PLAYABLE_HEIGHT), 16, 16){
 			@Override
 			public void onClick() {
 				loop = false;
 			}
 		};
 		
 		textX = (int) ((rightBackMargin+leftBackMargin)/2-theme.getFont().getStringWidth(title));
 		
 		numTilesX = 8;
 	}
 	
 	public abstract void drawContents();
 	
 	public void showInterface() {
 		
 		loop = true;
 		
 		while(loop) {
 			
 			drawBackground();
 			
 			drawContents();
 			
 			if (drawBoxes) drawBoxes();
 			
 			try {
 				theme.getFont().glDrawText(title, textX, (int)topBackMargin+10);
 			} catch (InvalidEscapeSequenceException e) {
 				
 			}
 			
 			exitButton.update(0);
 			exitButton.renderGL();
 			
 			Display.update();
 			
 			if (Display.isCloseRequested()) {
 				Game.GAME_STATE = Game.GAME_STATE_QUIT;
 				loop = false;
 			} else if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 				loop = false;
 			}
 			
 		}
 	}
 	
 	private void drawBackground() {
 		
 		GL11.glPushMatrix();
 		
 		Main.BLANK_TEXTURE.bind();
 		GL11.glBegin(GL11.GL_QUADS);
 		{
 			GL11.glColor3d(background.r, background.g, background.b);
 			GL11.glVertex2d(leftBackMargin, topBackMargin);
 			GL11.glVertex2d(rightBackMargin, topBackMargin);
 			GL11.glVertex2d(rightBackMargin, bottomBackMargin);
 			GL11.glVertex2d(leftBackMargin, bottomBackMargin);
 			
 			GL11.glColor3d(foreground.r, foreground.g, foreground.b);
 			GL11.glVertex2d(leftFrontMargin, topFrontMargin);
 			GL11.glVertex2d(rightFrontMargin, topFrontMargin);
 			GL11.glVertex2d(rightFrontMargin, bottomFrontMargin);
 			GL11.glVertex2d(leftFrontMargin, bottomFrontMargin);
 		}
 		GL11.glEnd();
 
 		GL11.glPopMatrix();
 	}
 	
 	private void drawBoxes() {
 
 		// We want a grid of about 8 across going down infinitely
 		// TODO: For going down, we will need to implement a scroller
		double width = (rightFrontMargin-leftFrontMargin)/numTilesX;
 		
 		GL11.glColor4f(0f, 0f, 0f, 0.4f);
 
 		GL11.glBegin(GL11.GL_LINES);
 		// Draw vertical lines
		for (int i = 1; i < (rightFrontMargin-leftFrontMargin)/width; i++) {
 			
			GL11.glVertex2d(leftFrontMargin + i*width, topFrontMargin+1);
			GL11.glVertex2d(leftFrontMargin + i*width, bottomFrontMargin);
 			
 		}
 		// Draw horizontal lines
 		for (double i = 1; i < (bottomFrontMargin-topFrontMargin)/width; i++) {
 
			GL11.glVertex2d(leftFrontMargin+1, topFrontMargin + i*width);
 			GL11.glVertex2d(rightFrontMargin, topFrontMargin + i*width);
 		}
 		GL11.glEnd();
 
 	}
 	
 	protected void setDrawBoxes(boolean drawBoxes) {
 		this.drawBoxes = drawBoxes;
 	}
 	
 }
