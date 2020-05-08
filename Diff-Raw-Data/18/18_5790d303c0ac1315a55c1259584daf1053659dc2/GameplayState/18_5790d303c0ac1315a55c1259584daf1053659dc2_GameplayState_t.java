 package com.github.joakimpersson.tda367.controller;
 
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.github.joakimpersson.tda367.audio.AudioEventListener;
 import com.github.joakimpersson.tda367.controller.input.InputData;
 import com.github.joakimpersson.tda367.controller.input.InputManager;
 import com.github.joakimpersson.tda367.controller.utils.ControllerUtils;
import com.github.joakimpersson.tda367.gui.GameplayView;
 import com.github.joakimpersson.tda367.model.PyromaniacModel;
 import com.github.joakimpersson.tda367.model.IPyromaniacModel;
 import com.github.joakimpersson.tda367.model.constants.EventType;
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class GameplayState extends BasicGameState {
 
 	/**
 	 * A simple enum containing the different states in the game while running
 	 * 
 	 */
 	private enum STATE {
 		GAME_RUNNING, ROUND_OVER, MATCH_OVER, GAME_OVER, NOT_STARTED, GAME_WAITING, ROUND_INFO_STATE;
 	}
 
 	private int stateID = -1;
 	private IPyromaniacModel model = null;
	private GameplayView view = null;
 	private STATE currentState = null;
 	private InputManager inputManager = null;
 	private PropertyChangeSupport pcs;
 	private List<EventType> playlist;
 	private boolean playlistIsPlaying = false;
 	private Timer countDownTimer;
 	private boolean readyToStart;
 
 	/**
 	 * Create a new slick BasicGameState controller for the Gameplaystate
 	 * 
 	 * @param stateID
 	 *            The states id number
 	 */
 	public GameplayState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		pcs = new PropertyChangeSupport(this);
 		AudioEventListener audioEL = AudioEventListener.getInstance();
 		pcs.addPropertyChangeListener(audioEL);
 		model = PyromaniacModel.getInstance();
 
 		this.playlist = new ArrayList<EventType>();
 		playlist.add(EventType.ROUND_ENDED);
 		playlist.add(EventType.UPGRADE_SCREEN);
 
 		// TODO natan maybe models listener should be added in the class where get
 		// instance is first called?
 		model.addPropertyChangeListener(audioEL);
		view = new GameplayView();
 		inputManager = InputManager.getInstance();
 
 		currentState = STATE.NOT_STARTED;
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
 		ControllerUtils.clearInputQueue(container.getInput());
 		currentState = STATE.GAME_WAITING;
 		view.enter();
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		if (!currentState.equals(STATE.NOT_STARTED)) {
 			view.render(container, g);
 
 			if (currentState.equals(STATE.ROUND_INFO_STATE)) {
 				view.showRoundStats(container, g);
 			}
 
 			if (currentState.equals(STATE.GAME_WAITING)) {
 				view.showWaitingBox(container, g);
 			}
 
 		}
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 
 		switch (currentState) {
 
 		case GAME_WAITING:
 			gameWaiting(container.getInput());
 			break;
 		case GAME_RUNNING:
 			gameRunning(container.getInput(), game);
 			break;
 		case MATCH_OVER:
 			matchOver(game);
 			break;
 		case ROUND_OVER:
 			roundOver(container.getInput());
 			break;
 		case ROUND_INFO_STATE:
 			roundInfo(container.getInput());
 			break;
 		case GAME_OVER:
 			gameOver(game);
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * 
 	 * Listens to player interaction while the game is waiting to send the
 	 * player into another State
 	 * 
 	 * @param input
 	 *            The input method used by the slick framework that contains the
 	 *            latest action
 	 */
 	private void gameWaiting(Input input) {
 		
 		if(countDownTimer == null) {
 			pcs.firePropertyChange("play", null, EventType.BATTLE_SCREEN);
 			readyToStart = false;
 			countDownTimer = new Timer();
 			view.setCountDown(3);
 			pcs.firePropertyChange("play", null, EventType.COUNT_DOWN);
 			countDownTimer.schedule(new CountDownTask(2), 700);
 		} else if(readyToStart) {
 				currentState = STATE.GAME_RUNNING;
 				readyToStart = false;
 				countDownTimer = null;
 		}
 	}
 
 	private void gameRunning(Input input, StateBasedGame game) {
 		// simple check to see whether the turn is over or not
 		if (model.isRoundOver()) {
 			currentState = STATE.ROUND_OVER;
 		} else {
 			updateGame(input);
 		}
 
 	}
 
 	/**
 	 * 
 	 * Responsible for updating the game when the game is running and is
 	 * responsible for handling the users inputs and notify the model.
 	 * 
 	 * @param input
 	 *            The input method used by the slick framework that contains the
 	 *            latest action
 	 */
 	private void updateGame(Input input) {
 
 		if (playlistIsPlaying) {
 			pcs.firePropertyChange("play", null, EventType.BATTLE_SCREEN);
 			playlistIsPlaying = false;
 		}
 
 		List<InputData> data = inputManager.getData(input);
 
 		for (InputData d : data) {
 			model.updateGame(d.getPlayer(), d.getAction());
 		}
 	}
 
 	private void roundOver(Input input) {
 		List<Player> players = model.getPlayers();
 		ArrayList<Player> playerList = new ArrayList<Player>();
 		for (Player p : players) {
 			playerList.add(new Player(p));
 		}
 		
 		model.roundOver();
 		if (model.isMatchOver()) {
 			model.matchOver();
 		}
 		
 		Player winningPlayer = model.getLastRoundWinner();
 		view.setRoundWinner(winningPlayer);
 		view.setPlayerList(playerList);
 
 		ControllerUtils.clearInputQueue(input);
 		currentState = STATE.ROUND_INFO_STATE;
 	}
 
 	/**
 	 * 
 	 * Responsible for determine what state the game should enter when a round
 	 * is over and listens for the player to press the proceed button to enter
 	 * the next state.
 	 * 
 	 * @param input
 	 *            The input method used by the slick framework that contains the
 	 *            latest action
 	 */
 	private void roundInfo(Input input) {
 		if (!playlistIsPlaying) {
 			pcs.firePropertyChange("playList", null, playlist);
 			playlistIsPlaying = true;
 		}
 
 		boolean pressedProceed = inputManager.pressedProceed(input);
 		boolean matchOver = model.isMatchOver();
 
 		if (pressedProceed) {
 			if (matchOver) {
 				currentState = STATE.MATCH_OVER;
 			} else {
 				currentState = STATE.GAME_WAITING;
 			}
 		}
 
 	}
 
 	private void matchOver(StateBasedGame game) {
 		if (model.isGameOver()) {
 			model.gameOver();
 			currentState = STATE.GAME_OVER;
 		} else {
 			int newState = PyromaniacsGame.UPGRADE_PLAYER_STATE;
 			ControllerUtils.changeState(game, newState);
 		}
 		model.resetRoundStats();
 	}
 
 	private void gameOver(StateBasedGame game) {
 
 		int newState = PyromaniacsGame.GAMEOVER_STATE;
 		ControllerUtils.changeState(game, newState);
 
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 	
 	/**
 	 * Keeps track of the count-down by lowering the index until it is complete.
 	 */
 	private class CountDownTask extends TimerTask {
 		private int countDown;
 		
 		public CountDownTask(int countDown) {
 			this.countDown = countDown;
 		}
 		
 		@Override
 		public void run() {
 			if(countDown == 0) {
 				readyToStart = true;
 			} else {
 				view.setCountDown(countDown);
 				if(countDown == 1) {
 					pcs.firePropertyChange("play", null, EventType.BOMB_EXPLODED);
 				} else {
 					pcs.firePropertyChange("play", null, EventType.COUNT_DOWN);
 				}
 				countDownTimer.schedule(new CountDownTask(countDown - 1), 700);
 			}
 		}
 		
 	}
 	
 }
