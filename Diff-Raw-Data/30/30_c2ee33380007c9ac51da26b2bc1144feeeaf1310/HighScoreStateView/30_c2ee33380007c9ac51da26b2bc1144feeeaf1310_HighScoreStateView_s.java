 package view;
 
 import model.Game;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import utils.SaveUtils;
 
public class HighScoreStateView {	
	public HighScoreStateView () {}
 
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
		Image background = new Image("pics/highscore_background.png");
 		background.draw();
 		g.setColor(Color.green);
 		int y = 200;
 		String space = " ";
 		for (int i = 0; i < 10; i++) {
 			if (i == 9) {
 				space = "";
 			}
			g.drawString(Integer.toString(i+1) + ". " + space + SaveUtils.getNameList()[i] +
					" .................................... " + Integer.toString(SaveUtils.getScoreList()[i]), (Game.WINDOW_WIDTH/2-200), y);
 			y = y + 40;
 		}
 	}
 }
