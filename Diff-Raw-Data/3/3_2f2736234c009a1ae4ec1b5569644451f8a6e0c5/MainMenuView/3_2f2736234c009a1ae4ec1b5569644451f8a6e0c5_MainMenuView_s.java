 package com.github.joakimpersson.tda367.gui;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.gui.guiutils.GUIUtils;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class MainMenuView {
 
 	private Font smlFont = null;
 	private Font bigFont = null;
 	private final int WIDTH;
 	private ImageLoader imageLoader = null;
 
 	/**
 	 * Creates a new view representing the main menu in the game
 	 */
 	public MainMenuView() {
 		init();
 		WIDTH = GUIUtils.getGameWidth();
 	}
 
 	/**
 	 * Responsible for fetching instances, info from the model and init fonts
 	 * etc
 	 */
 	private void init() {
 		try {
 			smlFont = GUIUtils.getSmlFont();
 			bigFont = GUIUtils.getBigFont();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		imageLoader = ImageLoader.getInstance();
 	}
 
 	/**
 	 * Render this view to the game's graphics context
 	 * 
 	 * @param container
 	 *            The container holding the game
 	 * @param g
 	 *            The graphics context to render to
 	 * @param selection
 	 *            The index that is selected
 	 * @throws SlickException
 	 *             Indicates a failure to render an gui object
 	 */
 	public void render(GameContainer container, Graphics g, int selection)
 			throws SlickException {
 		g.drawImage(imageLoader.getImage("bg"), 0, 0);
 		int posY = 140;
 		drawTitle(posY, g);
 		posY += 80;
 		drawMenu(posY, selection, g);
 	}
 
 	/**
 	 * Draw the title of the game at a given y-postion.
 	 * 
 	 * @param y
 	 *            The y-position where the title will be drawn
 	 * @param g
 	 *            The graphics context to draw with
 	 */
 	private void drawTitle(int y, Graphics g) {
 		String title = "Pyromaniacs";
 		g.setFont(bigFont);
 		g.setColor(Color.cyan);
 		g.drawString(title, GUIUtils.getStringCenterX(title, WIDTH, g), y);
 	}
 
 	/**
 	 * Draw the menu at a given y-position
 	 * 
 	 * @param y
 	 *            The y-position where the menu will be drawn
 	 * @param selection
 	 *            The index that is selected in the menu
 	 * @param g
 	 *            The graphics context to draw with
 	 */
 	private void drawMenu(int y, int selection, Graphics g) {
 		g.setColor(Color.white);
 		g.setFont(smlFont);
 
 		for (int i = 0; i < 4; i++) {
 			if (selection == i) {
 				g.setColor(Color.cyan);
 			} else {
 				g.setColor(Color.gray);
 			}
 
 			if (i == 1) {
 				drawMenuItem("Start Game", y, g);
 				y += 40;
 			} else if (i == 2) {
 				drawMenuItem("Highscore View", y, g);
 				y += 40;
 			} else if (i == 3) {
 				drawMenuItem("Exit Game", y, g);
 				y += 40;
 			}
 		}
 	}
 
 	/**
 	 * Draw a singel item in the menu
 	 * 
 	 * @param str
 	 *            The String that will be drawn
 	 * @param y
 	 *            The y-position the item will be drawn at
 	 * @param g
 	 *            The graphicals context to draw with
 	 */
 	private void drawMenuItem(String str, int y, Graphics g) {
 		g.drawString(str, GUIUtils.getStringCenterX(str, WIDTH, g), y);
 
 	}
 }
