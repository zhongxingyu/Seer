 package com.github.joakimpersson.tda367.gui;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
 * A container for the main gameplay view
  * 
  * @author joakimpersson
  * @modified adderollen
  * 
  */
 public class GameplayView implements IView {
 
 	private GameFieldView gameFieldView = null;
 	private PlayerInfoContainerView infoContainer = null;
 	private RoundInfoView roundInfoView = null;
 	private RoundWaitingView roundWaitingView = null;
 
 	/**
 	 * Creates a new view holding the game view and sub panels
 	 */
 	public GameplayView() {
 		init();
 	}
 
 	/**
 	 * Responsible for fetching instances, info from the model and init fonts
 	 * etc
 	 * 
 	 */
 	private void init() {
 		infoContainer = new PlayerInfoContainerView(0, 0);
 		gameFieldView = new GameFieldView(205, 0);
 		roundInfoView = new RoundInfoView();
 		roundWaitingView = new RoundWaitingView();
 	}
 
 	@Override
 	public void enter() {
 		infoContainer.enter();
 		gameFieldView.enter();
 		roundInfoView.enter();
 		roundWaitingView.enter();
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		gameFieldView.render(container, g);
 		infoContainer.render(container, g);
 
 	}
 
 	/**
 	 * 
 	 * @param container
 	 *            The container holding the game
 	 * @param g
 	 *            The graphics context to render to
 	 * @throws SlickException
 	 *             Indicates a failure to render an gui object
 	 */
 	public void showRoundStats(GameContainer container, Graphics g)
 			throws SlickException {
 		roundInfoView.render(container, g);
 	}
 
 	/**
 	 * Notify the view about who is the winning Player of the last played round
 	 * 
 	 * @param winningPlayer
 	 *            The player who won the last round
 	 */
 	public void setRoundWinner(Player winningPlayer) {
 		roundInfoView.setWinningPlayer(winningPlayer);
 
 	}
 
 	/**
 	 * Set the player List with a given player list
 	 * 
 	 * @param playerList
 	 *            The List with Players
 	 */
 	public void setPlayerList(ArrayList<Player> playerList) {
 		roundInfoView.setPlayerList(playerList);
 	}
 
 	/**
 	 * 
 	 * @param container
 	 *            The container holding the game
 	 * @param g
 	 *            The graphics context to render to
 	 * @throws SlickException
 	 *             Indicates a failure to render an gui object
 	 */
 	public void showWaitingBox(GameContainer container, Graphics g)
 			throws SlickException {
 		roundWaitingView.render(container, g);
 	}
 
 	/**
 	 * Sets the number that will be displayed for count-down.
 	 * 
 	 * @param countDown
 	 *            The number that will be displayed for count-down.
 	 */
 	public void setCountDown(int countDown) {
 		roundWaitingView.setCountDown(countDown);
 	}
 
 }
