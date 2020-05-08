 package com.github.joakimpersson.tda367.controller;
 
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
 
 import com.github.joakimpersson.tda367.controller.input.InputData;
 import com.github.joakimpersson.tda367.controller.input.InputManager;
 import com.github.joakimpersson.tda367.controller.input.KeyBoardInputHandler;
 import com.github.joakimpersson.tda367.gui.GameplayView;
 import com.github.joakimpersson.tda367.model.BombermanModel;
 import com.github.joakimpersson.tda367.model.IBombermanModel;
 import com.github.joakimpersson.tda367.model.constants.ResetType;
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class GameplayState extends BasicGameState {
 
 	private int stateID = -1;
 	private IBombermanModel model = null;
 	private GameplayView view = null;
 	private STATE currentState = null;
 	private InputManager inputManager = null;
 
 	private List<Player> players = null;
 
 	private enum STATE {
 		GAME_RUNNING, ROUND_OVER, MATCH_OVER, GAME_OVER, NOT_STARTED;
 	}
 
 	public GameplayState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
 
 		currentState = STATE.GAME_RUNNING;
 
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		model = BombermanModel.getInstance();
 		view = new GameplayView();
 		players = model.getPlayers();
 		inputManager = InputManager.getInstance();
 		// TODO should be init in setupgamestate
 		int id = 0;
 		for (Player p : players) {
 			KeyBoardInputHandler inputHandler = new KeyBoardInputHandler(p, id);
 			inputManager.addInputObject(inputHandler);
 			id++;
 		}
 
 		currentState = STATE.NOT_STARTED;
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		if (!currentState.equals(STATE.NOT_STARTED)) {
 			view.render(container, g);
 
 			if (currentState.equals(STATE.ROUND_OVER)) {
 				view.showRoundStats(container, g);
 			}
 
 		}
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		// TODO only during development
 		if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 			container.exit();
 		}
 
 		if (container.getInput().isKeyPressed(Input.KEY_Z)) {
 			currentState = STATE.ROUND_OVER;
 		}
 
 		switch (currentState) {
 		case GAME_RUNNING:
 
 			// simple check to see whether the turn is over or not
 			if (model.isRoundOver()) {
 				currentState = STATE.ROUND_OVER;
 			} else {
 				gameRunning(container);
 			}
 
 			break;
 		case MATCH_OVER:
 			resetState();
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
 
 			roundOver(container);
 
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	private void roundOver(GameContainer container) {
 		Input input = container.getInput();
 
		if (inputManager.pressedProceed(input)) {
 			if (model.isMatchOver()) {
 				currentState = STATE.MATCH_OVER;
 			} else {
 				resetState();
 				// TODO jocke add an wainting state and restart
 			}
 		}
 
 	}
 
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
 
 	private void gameRunning(GameContainer gc) {
 		Input input = gc.getInput();
 
 		// TODO jocke only used during development
 		if (input.isKeyDown(Input.KEY_ESCAPE)) {
 			gc.exit();
 		}
 
 		List<InputData> data = inputManager.getData(input);
 
 		for (InputData d : data) {
 			model.updateGame(d.getPlayer(), d.getAction());
 		}
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
