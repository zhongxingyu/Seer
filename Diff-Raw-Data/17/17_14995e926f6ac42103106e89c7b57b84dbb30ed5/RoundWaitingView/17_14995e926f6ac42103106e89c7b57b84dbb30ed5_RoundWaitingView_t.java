 package com.github.joakimpersson.tda367.gui;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.gui.guiutils.GUIUtils;
 
 /**
 * A view displaying a count down before a turn starts.
  * 
  * @author joakimpersson
 * @modified adderollen
  * 
  */
 public class RoundWaitingView implements IView {
 	private static final int WIDTH = 140;
 	private static final int HEIGHT = 70;
 	private Font bigFont = null;
 	private int startX;
 	private int startY;
 	private int countDown;
 
 	/**
 	 * Creates a new view that shows info when the game is waiting for input
 	 */
 	public RoundWaitingView() {
 		init();
 	}
 
 	/**
 	 * Responsible for fetching instances ,info from the model and init fonts a
 	 */
 	private void init() {
 		try {
 			bigFont = GUIUtils.getBigFont();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void enter() {
 		startX = GUIUtils.getGameWidth() / 2 - WIDTH / 2;
 		startY = GUIUtils.getGameHeight() / 2 - HEIGHT / 2;
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		g.setColor(Color.white);
 		g.setFont(bigFont);
 		drawBackgroundContainer(g);
 		drawText(g);
 	}
 
 	/**
 	 * Draws the info text to the screen
 	 * 
 	 * @param g
 	 *            The graphics context to render to
 	 */
 	private void drawText(Graphics g) {
 		String str;
 		Color color = Color.white;
		switch (countDown) {
 		case 3:
 			str = "READY";
 			break;
 		case 2:
 			str = "SET";
 			break;
 		case 1:
 			str = "FIRE!";
 			color = Color.red;
 			break;
 		default:
 			str = "...";
 			break;
 		}
 		int x = GUIUtils.getStringCenterX(str, WIDTH, g) + startX;
 		int y = GUIUtils.getStringCenterY(str, HEIGHT, g) + startY;
 		g.setColor(color);
 		g.drawString(str, x, y);
 	}
 
 	/**
 	 * Draws the background container to the game
 	 * 
 	 * @param g
 	 *            The graphics context to render to
 	 */
 	private void drawBackgroundContainer(Graphics g) {
 		int border = 6;
 		g.setColor(Color.red);
		g.fillRoundRect(startX - border, startY - border, WIDTH + 2 * border,
				HEIGHT + 2 * border, 12);
 		g.setColor(Color.black);
 		g.fillRoundRect(startX, startY, WIDTH, HEIGHT, 12);
 	}

 	/**
 	 * Sets the number that will be displayed for count-down.
 	 * 
	 * @param countDown
	 *            The number that will be displayed for count-down.
 	 */
 	public void setCountDown(int countDown) {
 		this.countDown = countDown;
 	}
 
 }
