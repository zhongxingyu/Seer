 package com.github.joakimpersson.tda367.controller;
 
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 import org.newdawn.slick.state.transition.Transition;
 
 import com.github.joakimpersson.tda367.audio.AudioEventListener;
 import com.github.joakimpersson.tda367.controller.input.InputData;
 import com.github.joakimpersson.tda367.controller.input.InputManager;
 import com.github.joakimpersson.tda367.gui.GameplayView;
 import com.github.joakimpersson.tda367.model.BombermanModel;
 import com.github.joakimpersson.tda367.model.IBombermanModel;
 import com.github.joakimpersson.tda367.model.constants.EventType;
 import com.github.joakimpersson.tda367.model.constants.ResetType;
 
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
 		GAME_RUNNING, ROUND_OVER, MATCH_OVER, GAME_OVER, NOT_STARTED, GAME_WAITING;
 	}
 
 	private int stateID = -1;
 	private IBombermanModel model = null;
 	private GameplayView view = null;
 	private STATE currentState = null;
 	private InputManager inputManager = null;
 	private PropertyChangeSupport pcs;
 	private List<EventType> playlist;
 	private boolean playlistIsPlaying = false;
	
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
 		model = BombermanModel.getInstance();
		
 		this.playlist = new ArrayList<EventType>();
 		playlist.add(EventType.ROUND_ENDED);
 		playlist.add(EventType.UPGRADE_SCREEN);
 
 		// TODO maybe models listener should be added in the class where get
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
 		clearInputQueue(container.getInput());
 		pcs.firePropertyChange("play", null, EventType.BATTLE_SCREEN);
 		currentState = STATE.GAME_WAITING;
 		view.enter();
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		if (!currentState.equals(STATE.NOT_STARTED)) {
 			view.render(container, g);
 
 			if (currentState.equals(STATE.ROUND_OVER)) {
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
			
 			// simple check to see whether the turn is over or not
 			if (model.isRoundOver()) {
 				currentState = STATE.ROUND_OVER;
			} else if (model.isMatchOver()) {
				currentState = STATE.MATCH_OVER;
 			} else {
 				gameRunning(container.getInput());
 			}
 
 			break;
 		case MATCH_OVER:
 
 			if (model.isGameOver()) {
 				currentState = STATE.GAME_OVER;
 			} else {
 				Transition fadeIn = new FadeInTransition(Color.cyan, 500);
 				Transition fadeOut = new FadeOutTransition(Color.cyan, 500);
 				game.enterState(BombermanGame.UPGRADE_PLAYER_STATE, fadeOut,
 						fadeIn);
 			}
 			resetState();
 			break;
 		case ROUND_OVER:
 			roundOver(container.getInput());
 
 			break;
 		default:
 			break;
 		}
 
 		// TODO perhaps map to some input object
 		if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 			game.enterState(BombermanGame.MAIN_MENU_STATE);
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
 
 		boolean pressedProceed = inputManager.pressedProceed(input);
 
 		if (pressedProceed) {
 			currentState = STATE.GAME_RUNNING;
 		}
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
 	private void roundOver(Input input) {
		if(!playlistIsPlaying) {
 			pcs.firePropertyChange("playList", null, playlist);
 			playlistIsPlaying = true;
 		}
		
 		boolean pressedProceed = inputManager.pressedProceed(input);
		
 		if (pressedProceed) {
 			if (model.isMatchOver()) {
 				currentState = STATE.MATCH_OVER;
 			} else {
 				resetState();
 				currentState = STATE.GAME_WAITING;
 			}
 		}
 
 	}
 
 	/**
 	 * Reset the model based on the games current state
 	 */
 	private void resetState() {
 
 		switch (currentState) {
 		case ROUND_OVER:
 			model.reset(ResetType.Round);
 			break;
 		case MATCH_OVER:
 			model.reset(ResetType.Match);
 			break;
 		default:
 			break;
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
 	private void gameRunning(Input input) {
		
		if(playlistIsPlaying) {
 			pcs.firePropertyChange("play", null, EventType.BATTLE_SCREEN);
 			playlistIsPlaying = false;
 		}
 
 		List<InputData> data = inputManager.getData(input);
 
 		for (InputData d : data) {
 			model.updateGame(d.getPlayer(), d.getAction());
 		}
 	}
 
 	/**
 	 * Clear everything in the input queue from previous states
 	 * 
 	 * @param input
 	 *            The input method used by the slick framework that contains the
 	 *            latest action
 	 */
 	private void clearInputQueue(Input input) {
 		input.clearControlPressedRecord();
 		input.clearKeyPressedRecord();
 		input.clearMousePressedRecord();
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
