 package com.github.joakimpersson.tda367.gui;
 
 import java.util.List;
 
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.gui.guiutils.GUIUtils;
 import com.github.joakimpersson.tda367.model.PyromaniacModel;
 import com.github.joakimpersson.tda367.model.IPyromaniacModel;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.highscore.Score;
 import com.github.joakimpersson.tda367.model.player.PlayerPoints;
 
 /**
  * A class representing the GameOverWinnerView
  * 
  * @author adderollen
  * 
  */
 public class GameOverWinnerView implements IView {
 
 	private final int X;
 	private final int Y;
 
 	private Font bigFont = null;
 
 	private IPyromaniacModel model = null;
 	private List<Score> playerScores = null;
 	private Score winningScore = null;
 
 	/**
 	 * Creating a GameOverWinnerView at a giver x any y position.
 	 * 
 	 * @param x
 	 *            The x-position where the view will be created.
 	 * @param y
 	 *            The y-position where the view will be created.
 	 */
 	public GameOverWinnerView(int x, int y) {
 		this.X = x;
 		this.Y = y;
 		init();
 	}
 
 	/**
 	 * Initiates the view.
 	 */
 	private void init() {
 		model = PyromaniacModel.getInstance();
 		try {
 			bigFont = GUIUtils.getBigFont();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void enter() {
 		playerScores = model.getGameOverSummary();
 		winningScore = getWinningPlayerScore();
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		int x = X;
 		int y = Y;
 		int yDelta = 50;
 
 		drawContainerHeader(x, y, g);
 
 		y += yDelta;
 
 		drawPlayerInfo(x, y, g);
 
 		y += yDelta;
 
 		drawGameSummary(x, y, g);
 	}
 
 	/**
 	 * Draws the "Winner" header at a given x and y position.
 	 * 
 	 * @param x
 	 *            The x-position where the header will be draw.
 	 * @param y
 	 *            The y-position where the header will be draw.
 	 * @param g
 	 *            The graphic context to write with.
 	 */
 	private void drawContainerHeader(int x, int y, Graphics g) {
 		g.setFont(bigFont);
 		String str = "Winner:";
 		g.drawString(str, x, y);
 	}
 
 	/**
 	 * Draws the winning players info at a given x and y position.
 	 * 
 	 * @param x
 	 *            The x-position where the info will be draw.
 	 * @param y
 	 *            The y-position where the info will be draw.
 	 * @param g
 	 *            The graphic context to write with.
 	 */
 	private void drawPlayerInfo(int x, int y, Graphics g) {
 		g.setFont(bigFont);
 		String playerSummaryStr = getPlayerSummaryStr();
 		g.drawString(playerSummaryStr, x, y);
 	}
 
 	/**
 	 * Draws the summary of the winners game.
 	 * 
 	 * @param posX
 	 *            The x-position where the summary will be draw.
 	 * @param posY
 	 *            The y-position where the summary will be draw.
 	 * @param g
 	 *            The graphic context to write with.
 	 */
 	private void drawGameSummary(int posX, int posY, Graphics g) {
 		PlayerPoints playerPoints = winningScore.getPlayerPoints();
 		int x = posX;
 		int y = posY;
		int xDelta = 400;
		int yDelta = 70;
 		String str = "Players Killed: "
 				+ playerPoints.getEarnedPointGiver(PointGiver.KillPlayer);
 		g.drawString(str, x, y);
 		y += yDelta;
 		str = "Destroyed Boxes: "
 				+ playerPoints.getEarnedPointGiver(PointGiver.Box);
 		g.drawString(str, x, y);
 
 		x += xDelta;
 		y = posY;
 
 		str = "Players Hit: "
 				+ playerPoints.getEarnedPointGiver(PointGiver.PlayerHit);
 		g.drawString(str, x, y);
 		y += yDelta;
 		str = "Destroyed Pillars: "
 				+ playerPoints.getEarnedPointGiver(PointGiver.Pillar);
 		g.drawString(str, x, y);
 	}
 
 	/**
 	 * Gets a String containing a summary of the winners stats.
 	 * 
 	 * @return A String containing a summary of the winners stats
 	 */
 	private String getPlayerSummaryStr() {
 		PlayerPoints playerPoints = winningScore.getPlayerPoints();
 		StringBuilder strBuilder = new StringBuilder();
 
 		strBuilder.append("1. ");
 		strBuilder.append(winningScore.getPlayerName());
 		strBuilder.append(" ");
 		strBuilder.append(playerPoints.getScore());
 		strBuilder.append("p");
 
 		return strBuilder.toString();
 	}
 
 	/**
 	 * Gets the winning player score.
 	 * 
 	 * @return The winning players score.
 	 */
 	private Score getWinningPlayerScore() {
 		// the score object with highest score should be the first one
 		return playerScores.get(0);
 	}
 }
