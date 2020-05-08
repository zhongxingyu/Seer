 package com.github.joakimpersson.tda367.gui;
 
 import java.util.List;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.gui.guiutils.GUIUtils;
 import com.github.joakimpersson.tda367.gui.guiutils.ImageLoader;
 import com.github.joakimpersson.tda367.model.IPyromaniacModel;
 import com.github.joakimpersson.tda367.model.PyromaniacModel;
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * A view showing about every ended round
  * 
  * @author joakimpersson
  * 
  */
 public class RoundInfoView implements IView {
 
 	private int startX;
 	private int startY;
 	private IPyromaniacModel model = null;
 	private List<Player> players = null;
 	private Font smlFont = null;
 	private Font bigFont = null;
 	private ImageLoader imageLoader = null;
 	private Player roundWinner = null;
 	private Animation textAnimation = null;
 	private Animation winningPlayerAnimation = null;
 	private List<Player> playerSnapshot;
 
 	/**
 	 * Creates a new view containing info about the players stats from the
 	 * PlayerPoints object
 	 */
 	public RoundInfoView() {
 		init();
 	}
 
 	/**
 	 * Responsible for fetching instances ,info from the model and init fonts
 	 * etc
 	 */
 	private void init() {
 		model = PyromaniacModel.getInstance();
 		try {
 			smlFont = GUIUtils.getSmlFont();
 			bigFont = GUIUtils.getBigFont();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		imageLoader = ImageLoader.getInstance();
 	}
 
 	@Override
 	public void enter() {
 		startX = 205 + 65;
 		startY = 65;
 		players = model.getPlayers();
 
 		Image[] textAnimationImgs = {
 				imageLoader.getImage("info/winnerEffects1"),
 				imageLoader.getImage("info/winnerEffects2") };
 		textAnimation = new Animation(textAnimationImgs, 300);
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		String imageName = "round-info/bg";
 		GUIUtils.drawImage(startX, startY, imageName, g);
 		int x = startX + 20;
 		int y = startY + 20;
 		drawPlayers(x, y, g);
 	}
 
 	/**
 	 * Draw info about all the players in the game onto the screen
 	 * 
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 * @param g
 	 *            The games graphics object
 	 */
 	private void drawPlayers(int x, int y, Graphics g) {
 		int deltaX = 285 + 10;
 		int deltaY = 225 + 10;
 		int index = 0;
 		for (Player player : players) {
 			if (players.size() < 3) {
 				drawPlayer(player, x, y + 92, g);
 				x += deltaX;
 			} else {
 				if (index < 2) {
 					drawPlayer(player, x + (deltaX * index), y, g);
 				} else {
 					drawPlayer(player, x + (deltaX * (index - 2)), y + deltaY,
 							g);
 				}
 				index++;
 			}
 		}
 
 	}
 
 	/**
 	 * Create an simple player winning animation based on two images
 	 */
 	private void createPlayerAnimation() {
 		Image imgOne = imageLoader.getImage(
 				"player/" + roundWinner.getIndex() + "/win1").getScaledCopy(2);
 		Image imgTwo = imageLoader.getImage(
 				"player/" + roundWinner.getIndex() + "/win2").getScaledCopy(2);
 		Image[] winningPlayerAnimationImgs = { imgOne, imgTwo };
 		winningPlayerAnimation = new Animation(winningPlayerAnimationImgs, 400);
 	}
 
 	/**
 	 * 
 	 * Draw a player onto the screen including his name, image and if he won the
 	 * last round or not. It also displays how many rounds and matches he won.
 	 * 
 	 * @param player
 	 *            The player to be drawn onto the screen
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 * @param g
 	 *            The games graphics object
 	 */
 	private void drawPlayer(Player player, int x, int y, Graphics g) {
 		int xD = 30;
 		int yD = 55;
 
 		boolean isWinner = false;
 		if (model.getLastRoundWinner() != null) {
 			isWinner = (model.getLastRoundWinner().getIndex() == player
 					.getIndex());
 		}
 
 		String imgageName = "round-info/overlay";
 		GUIUtils.drawImage(x, y, imgageName, g);
 
 		x += xD;
 		y += yD;
 
 		drawPlayerInfo(player, isWinner, x, y, g);
 
 		// draw number of round wins
 		drawNumberOfRoundsWon(player, x, y, g);
 
 		// draw number of match wins
 		drawNumbeMatchesWon(player, x, y, g);
 
 		// draw winner string
 		drawRoundOverStatus(isWinner, x, y, g);
 	}
 
 	/**
 	 * Draw general info about the player onto the screen
 	 * 
 	 * @param player
 	 *            The player who's info we are drawing
 	 * @param isWinner
 	 *            If the player won the round or not
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 * @param g
 	 *            The games graphics object
 	 */
 	private void drawPlayerInfo(Player player, boolean isWinner, int x, int y,
 			Graphics g) {
 
 		// draws scaled player image
 		if (isWinner) {
 			winningPlayerAnimation.draw(x, y + 7);
 		} else {
 			String imageName = player.getImage();
 			GUIUtils.drawImage(x, y + 7, imageName, 2, g);
 		}
 
 		// draw name
 		g.setFont(bigFont);
 		int textD = 95 + GUIUtils.getStringCenterX(player.getName(), 122, g);
 		g.setColor(Color.white);
 		g.drawString(player.getName(), x + textD, y + 10);
 
 	}
 
 	/**
 	 * Draw how many rounds a player has won
 	 * 
 	 * @param player
 	 *            The player who's info we are drawing
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 * @param g
 	 *            The games graphics object
 	 */
 	private void drawNumberOfRoundsWon(Player player, int x, int y, Graphics g) {
 		g.setFont(bigFont);
 		int textD = 95 + GUIUtils.getStringCenterX(player.getName(), 122, g);
 
 		int xD = textD + g.getFont().getWidth(player.getName()) + 5;
 		int yD = 10;
 
 		int roundWins = Math.max(player.getRoundsWon(),
 				playerSnapshot.get(player.getIndex() - 1).getRoundsWon());
		for (int i = 0; i < roundWins; i++) {
 			GUIUtils.drawImage(x + xD, y + yD, "info/chevron", g);
 			yD += 4;
 		}
 
 	}
 
 	/**
 	 * Draw how many matches a player has won
 	 * 
 	 * @param player
 	 *            The player who's info we are drawing
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 * @param g
 	 *            The games graphics object
 	 */
 	private void drawNumbeMatchesWon(Player player, int x, int y, Graphics g) {
 		int xD = 110;
 		int yD = 90;
 		int matchWins = player.getMatchesWon();
 		String imageName = "info/star";
 		for (int i = 0; i < matchWins; i++) {
 			GUIUtils.drawImage(x + xD, y + yD, imageName, g);
 			xD += 15;
 		}
 	}
 
 	/**
 	 * Draw onto the screen if a player has won the last round or not
 	 * 
 	 * @param isWinner
 	 *            Is he the winner or not
 	 * @param x
 	 *            The starting coordinate in the x-axis
 	 * @param y
 	 *            The starting coordinate in the y-axis
 	 * @param g
 	 *            The games graphics object
 	 */
 	private void drawRoundOverStatus(boolean isWinner, int x, int y, Graphics g) {
 		int xD = 117;
 		int yD = 54;
 
 		g.setFont(smlFont);
 		g.setColor(Color.darkGray);
 		String gameStatusString = "LOSER";
 
 		if (isWinner) {
 			g.setFont(bigFont);
 			g.setColor(Color.yellow);
 			gameStatusString = "WINNER";
 			xD -= 10;
 			yD -= 6;
 			textAnimation.draw(x + 96, y + 36);
 		}
 		g.drawString(gameStatusString, x + xD, y + yD);
 	}
 
 	/**
 	 * Set the list of players
 	 * 
 	 * @param playerList
 	 *            A list of players
 	 */
 	public void setPlayerList(List<Player> playerList) {
 		this.playerSnapshot = playerList;
 	}
 
 	/**
 	 * Notify the view about who is the winning Player of the last played round
 	 * 
 	 * @param winningPlayer
 	 *            The player who won the last round
 	 */
 	public void setWinningPlayer(Player winningPlayer) {
 		if (winningPlayer != null
 				&& (this.roundWinner == null || !(this.roundWinner
 						.equals(winningPlayer)))) {
 			this.roundWinner = winningPlayer;
 			createPlayerAnimation();
 		}
 	}
 }
