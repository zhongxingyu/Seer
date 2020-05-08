 package com.github.joakimpersson.tda367.gui;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.model.BombermanModel;
 import com.github.joakimpersson.tda367.model.IBombermanModel;
 import com.github.joakimpersson.tda367.model.Score;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class HighscoreListView {
 
 	private final int X;
 	private final int Y;
 	private final static int WIDTH = 450;
 	private IBombermanModel model = null;
 	private Score[] highscore = null;
 
 	public HighscoreListView(int x, int y) {
 		this.X = x;
 		this.Y = y;
 		init();
 	}
 
 	private void init() {
 		model = BombermanModel.getInstance();
 	}
 
 	public void enter() {
 		if (model.getHighscoreList().length > 0) {
 			highscore = model.getHighscoreList();
 		}
 	}
 
 	public void render(GameContainer container, Graphics g, int currentIndex)
 			throws SlickException {
 		int midX = 0;
		int x = X;
 		int y = Y;
 		if (highscore != null) {
 			for (Score s : highscore) {
 				System.out.println(s);
 			}
 		} else {
 			String str = "Nothing here";
			midX = getStrinCenterX(str, WIDTH, g);
			g.drawString(str, midX + x, y);
 		}
 	}
 
	private int getStrinCenterX(String str, int width, Graphics g) {
 		int strWidth = g.getFont().getWidth(str);
 		return width / 2 - strWidth / 2;
 	}
 
 }
