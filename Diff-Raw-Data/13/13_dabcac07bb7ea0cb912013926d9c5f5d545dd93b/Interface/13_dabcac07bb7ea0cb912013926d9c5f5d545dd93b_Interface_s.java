 package main.interfaces;
 
 import java.util.List;
 import main.Game;
 import main.Main;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import util.InvalidEscapeSequenceException;
 import util.gl.Color;
 import util.input.Input;
 import util.input.KeyEvent;
 import util.input.MouseEvent;
 import util.ui.GLButton;
 import util.ui.GLUIException;
 import util.ui.GLUITheme;
 
 public abstract class Interface {
 
 	protected GLUITheme theme;
 
 	private GLButton exitButton;
 
 	protected boolean loop;
 
 	private String title;
 
 	private Color background;
 
 	private boolean drawBoxes;
 
 	int textX, numTilesX;
 
 	protected final int ystart = 75, xstart = (int) Math.round(0.05 * Main.SCREEN_WIDTH);
 
 	protected final int yend, xend;
 
 	protected List<MouseEvent> mouseEvents;
 
 	protected List<KeyEvent> keyEvents;
 
 	public static double width;
 	
 	public Interface(String title, Color foreground, Color background) {
 
 		this.title = title;
 
 		this.background = background;
 
 		try {
 			theme = new GLUITheme();
 			
 			theme.setBackgroundColor(Color.TRANSPARENT);
 			theme.setBackgroundHoverColor(Color.WHITE);
 			theme.setBackgroundClickedColor(Color.TRANSPARENT);
 			
 			theme.setTextColor(Color.WHITE);
 			theme.setTextHoverColor(Color.BLACK);
 			theme.setTextClickedColor(Color.WHITE);
 
 		} catch (GLUIException e) {
 		}
 
 		exitButton = new GLButton("X", theme, 10, 10, 16, 16) {
 
 			@Override
 			public void onClick() {
 
 				loop = false;
 			}
 		};
 		xend = Main.SCREEN_WIDTH*5/9;
 		textX = ((xend+xstart) - theme.getFont().getStringWidth(
 				title)) / 2;
 
 		
 		numTilesX = 10;
 		width = ((xend - xstart) / (float) numTilesX);
 		
		yend = (int) ((Main.SCREEN_HEIGHT - ystart)+(Main.SCREEN_HEIGHT - ystart)%width);
 		
 	}
 
 	public abstract void drawContents();
 	
 	public void showInterface() {
 
 		loop = true;
 
 		while (loop) {
 			
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 
 			Input.poll();
 			mouseEvents = Input.getMouseEvents();
 			keyEvents = Input.getKeyEvents();
 			
 			Game.render();
 			
 			drawBackground();
 
 			drawContents();
 
 			if (drawBoxes) drawBoxes();
 
 			try {
 				theme.getFont().glDrawText(title, textX, 25);
 			} catch (InvalidEscapeSequenceException e) {
 			}
 
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
 			GL11.glColor4d(background.r, background.g, background.b, 0.9);
 			GL11.glVertex2d(0, 0);
 			GL11.glVertex2d(xend+xstart, 0);
			GL11.glVertex2d(xend+xstart, Main.SCREEN_HEIGHT);
			GL11.glVertex2d(0, Main.SCREEN_HEIGHT);
 		}
 		GL11.glEnd();
 	}
 
 	private void drawBoxes() {
 
 		// We want a grid going down infinitely
 		// TODO: For going down, we will need to implement a scroller
 
 		GL11.glColor4f(255, 255, 255, 0.5f);
 
 		GL11.glBegin(GL11.GL_LINES);
 		// Draw vertical lines
 		for (int i = 0; i < numTilesX + 1; i++) {
 
 			GL11.glVertex2d(xstart + i * width, ystart);
			GL11.glVertex2d(xstart + i * width, yend-1);
 
 		}
 		// Draw horizontal lines
		for (double i = 0; i <= (yend - ystart) / (float)width; i++) {
 
 			GL11.glVertex2d(xstart, ystart + i * width);
 			GL11.glVertex2d(xend - xend%width -1, ystart + i * width);
 		}
 		GL11.glEnd();
 
 	}
 
 	/**
 	 * Determines whether the boxes should be drawn or not.
 	 * 
 	 * @param drawBoxes
 	 *        <code>true</code> if the boxes should be drawn.<br>
 	 *        <code>false</code> otherwise.
 	 */
 	protected void setDrawBoxes(boolean drawBoxes) {
 
 		this.drawBoxes = drawBoxes;
 	}
 
 }
