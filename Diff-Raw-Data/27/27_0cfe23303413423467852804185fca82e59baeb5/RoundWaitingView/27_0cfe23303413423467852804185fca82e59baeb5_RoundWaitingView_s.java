 package com.github.joakimpersson.tda367.gui;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 /**
  * 
  * @author joakimpersson
 *
  */
 public class RoundWaitingView implements IView {
 	private static final int WIDTH = 600;
 	private static final int HEIGHT = 100;
 	private Font bigFont = null;
 	private int startX;
 	private int startY;
 
 	public RoundWaitingView() {
 		init();
 	}
 
 	private void init() {
 		try {
 			bigFont = GUIParameters.INSTANCE.getBigFont();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void enter() {
 		startX = GUIParameters.INSTANCE.getGameWidth() / 2 - WIDTH / 2;
 		startY = GUIParameters.INSTANCE.getGameHeight() / 2 - HEIGHT / 2;
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		g.setColor(Color.white);
 		g.setFont(bigFont);
 		drawBackgroundContainer(g);
 		drawText(g);
 	}
 
 	private void drawText(Graphics g) {
 		String str = "Press Proceed to start the game";
 		int x = getStrinCenterX(str, WIDTH, g) + startX;
 		int y = getStrinCenterY(str, HEIGHT, g) + startY;
 		g.drawString(str, x, y);
 	}
 
 	private void drawBackgroundContainer(Graphics g) {
 		g.setColor(Color.black);
 		g.fillRoundRect(startX, startY, WIDTH, HEIGHT, 25);
 		g.setColor(Color.white);
 		g.drawRoundRect(startX, startY, WIDTH, HEIGHT, 25);
 	}
 
 	private int getStrinCenterX(String str, int width, Graphics g) {
 		int strWidth = g.getFont().getWidth(str);
 		return width / 2 - strWidth / 2;
 	}
 
 	private int getStrinCenterY(String str, int height, Graphics g) {
 		int strHeight = g.getFont().getHeight(str);
 		return height / 2 - strHeight / 2;
 	}
 }
