 package main;
 
 import java.util.List;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import util.InvalidEscapeSequenceException;
 import util.gl.Color;
 import util.input.Input;
 import util.input.MouseEvent;
 import util.ui.GLButton;
 import util.ui.GLUIException;
 import util.ui.GLUITheme;
 
 
 public abstract class Interface {
 
 	protected GLUITheme theme;
 	private GLButton exitButton;
 	
 	private boolean loop;
 	
 	private String title;
 	
 	private Color foreground, background;
 	
 	private boolean drawBoxes;
 	
 	int textX, numTilesX;
 	
 	protected final double ystart = 75, xstart = Math.round(0.1*Main.SCREEN_WIDTH);
 	protected final double yend = Main.SCREEN_HEIGHT-ystart, xend = Main.SCREEN_WIDTH-xstart;
 	
 	public Interface(String title, Color foreground, Color background) {
 		
 		this.title = title;
 		
 		this.foreground = foreground;
 		this.background = background;
 		
 		try {
 			theme = new GLUITheme();
 			theme.setBackgroundColor(new Color("FFFFFF"));
 			theme.setTextColor(new Color("000000"));
 			
 		} catch (GLUIException e) { }
 		
 		exitButton = new GLButton("X", theme, 10, 10, 16, 16){
 			@Override
 			public void onClick() {
 				loop = false;
 			}
 		};
 		
 		textX = (Main.SCREEN_WIDTH - theme.getFont().getStringWidth(title))/2;
 		
 		numTilesX = 16;
 	}
 	
 	public abstract void drawContents();
 	
 	public void showInterface() {
 		
 		loop = true;
 		
 		while(loop) {
 			
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 			
 			Input.poll();
			
			List<MouseEvent> mouseEvents = Input.getMouseEvents();
 			
 			drawBackground();
 			
 			drawContents();
 			
 			if (drawBoxes) drawBoxes();
 			
 			try {
 				theme.getFont().glDrawText(title, textX, 25);
 			} catch (InvalidEscapeSequenceException e) { }
 			
 			exitButton.processMouseEvents(mouseEvents);
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
 		
 		GL11.glBegin(GL11.GL_QUADS);
 		{
 			GL11.glColor3d(background.r, background.g, background.b);
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(Main.SCREEN_WIDTH, 0);
 			GL11.glVertex2d(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
 			GL11.glVertex2d(0, Main.SCREEN_HEIGHT);
 			
 			GL11.glColor3d(foreground.r, foreground.g, foreground.b);
 			GL11.glVertex2d(0, 75);
 			GL11.glVertex2d(Main.SCREEN_WIDTH, 75);
 			GL11.glVertex2d(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT-75);
 			GL11.glVertex2d(0, Main.SCREEN_HEIGHT-75);
 		}
 		GL11.glEnd();
 	}
 	
 	private void drawBoxes() {
 
 		// We want a grid of about 8 across going down infinitely
 		// TODO: For going down, we will need to implement a scroller
 		double width = (xend-xstart)/(float)numTilesX;
 		
 		GL11.glColor4f(0f, 0f, 0f, 0.1f);
 
 		GL11.glBegin(GL11.GL_LINES);
 		// Draw vertical lines
 		for (int i = 0; i <= (xend-xstart)/width; i++) {
 			
 			GL11.glVertex2d(xstart + i*width, ystart);
 			GL11.glVertex2d(xstart + i*width, yend);
 			
 		}
 		// Draw horizontal lines
 		for (double i = 1; i <= (yend-ystart)/width; i++) {
 
 			GL11.glVertex2d(xstart, ystart + i*width);
 			GL11.glVertex2d(xend, ystart + i*width);
 		}
 		GL11.glEnd();
 
 	}
 	
 	protected void setDrawBoxes(boolean drawBoxes) {
 		this.drawBoxes = drawBoxes;
 	}
 	
 }
