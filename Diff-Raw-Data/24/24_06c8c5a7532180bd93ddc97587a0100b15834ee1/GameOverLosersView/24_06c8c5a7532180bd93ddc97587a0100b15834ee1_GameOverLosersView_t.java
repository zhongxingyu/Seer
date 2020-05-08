 package com.github.joakimpersson.tda367.gui;
 
 import java.util.List;
 
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.gui.guiutils.GUIUtils;
 import com.github.joakimpersson.tda367.model.PyromaniacModel;
 import com.github.joakimpersson.tda367.model.IPyromaniacModel;
 import com.github.joakimpersson.tda367.model.highscore.Score;
 import com.github.joakimpersson.tda367.model.player.PlayerPoints;
 
 /**
  * A view demonstrating the players who lost the game. It displays the players
  * who lost by their name and final score
  * 
  * @author joakimpersson
  * 
  */
 public class GameOverLosersView implements IView {
 
 	private final int X;
 	private final int Y;
 
	private Font smlFont = null;
 	private Font bigFont = null;
 
 	private IPyromaniacModel model = null;
 	private List<Score> playerScores = null;
 
 	/**
 	 * Create a new instance of the view
 	 * 
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 */
 	public GameOverLosersView(int x, int y) {
 		this.X = x;
 		this.Y = y;
 		init();
 	}
 
 	/**
 	 * Initiate the view
 	 */
 	private void init() {
 		model = PyromaniacModel.getInstance();
 		try {
			smlFont = GUIUtils.getSmlFont();
 			bigFont = GUIUtils.getBigFont();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void enter() {
 		playerScores = model.getGameOverSummary();
 		removeWinnerFromScoreList();
 	}
 
 	/**
 	 * Remove the winner from this list.
 	 */
 	private void removeWinnerFromScoreList() {
 		// the winner should be first in the list
 		playerScores.remove(0);
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		int x = X;
 		int y = Y;
 		int yDelta = 50;
 
 		drawContainerHeader(x, y, g);
 
 		y += yDelta;
 
 		drawScoreInfo(x, y, g);
 	}
 
 	/**
 	 * Draw the containers header
 	 * 
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 * 
 	 * @param g
 	 *            The games graphics object
 	 */
 	private void drawContainerHeader(int x, int y, Graphics g) {
 		g.setFont(bigFont);
 		String str = "Losers:";
 		g.drawString(str, x, y);
 	}
 
 	/**
 	 * 
 	 * Draw all the score objects onto the screen
 	 * 
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 * 
 	 * @param g
 	 *            The games graphics object
 	 */
 	private void drawScoreInfo(int x, int y, Graphics g) {
		g.setFont(smlFont);
 		int i = 2;
 		for (Score score : playerScores) {
 			String playerSummaryStr = getScoreSummaryStr(score, i);
 			g.drawString(playerSummaryStr, x, y);
			i++;
			y += 25;
 		}
 	}
 
 	/**
 	 * Get a summary from a score object, including its player name and total
 	 * score
 	 * 
 	 * @param score
 	 *            The score object
 	 * @param index
 	 *            Its final position in the game
 	 * @return A summarized string with the info
 	 */
 	private String getScoreSummaryStr(Score score, int index) {
 		StringBuilder strBuilder = new StringBuilder();
 		String playerName = score.getPlayerName();
 		PlayerPoints playerPoints = score.getPlayerPoints();
 
 		strBuilder.append(index);
 		strBuilder.append(". ");
 		strBuilder.append(playerName);
 		strBuilder.append(" ");
 		strBuilder.append(playerPoints.getScore());
 		strBuilder.append("p");
 
 		return strBuilder.toString();
 	}
 
 }
