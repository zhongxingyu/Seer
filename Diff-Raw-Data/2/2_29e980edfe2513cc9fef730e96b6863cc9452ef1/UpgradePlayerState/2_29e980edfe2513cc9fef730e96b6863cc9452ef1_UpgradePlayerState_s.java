 package com.github.joakimpersson.tda367.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.github.joakimpersson.tda367.controller.input.InputData;
 import com.github.joakimpersson.tda367.controller.input.InputManager;
 import com.github.joakimpersson.tda367.gui.IUpgradePlayerView;
 import com.github.joakimpersson.tda367.gui.UpgradePlayerView;
 import com.github.joakimpersson.tda367.model.BombermanModel;
 import com.github.joakimpersson.tda367.model.IBombermanModel;
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class UpgradePlayerState extends BasicGameState {
 
 	private int stateID = -1;
 	private IUpgradePlayerView view = null;
 	private IBombermanModel model = null;
 	private Map<Player, Integer> playersIndex = null;
 	private List<Attribute> attributes = null;
 	private InputManager inputManager = null;
 
 	public UpgradePlayerState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		view = new UpgradePlayerView();
 		model = BombermanModel.getInstance();
 		inputManager = InputManager.getInstance();
 
 		attributes = model.getPlayers().get(0).getPermanentAttributes();
 		playersIndex = new HashMap<Player, Integer>();
 		for (Player p : model.getPlayers()) {
 			playersIndex.put(p, 0);
 		}
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		view.render(container, g, playersIndex);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		Input input = container.getInput();
 
 		// TODO jocke only used during development
 		if (input.isKeyDown(Input.KEY_ESCAPE)) {
 			container.exit();
 		}
 
		if (inputManager.pressedProcced(input)) {
 			game.enterState(BombermanGame.GAMEPLAY_STATE);
 		}
 
 		// TODO change location
 		List<PlayerAction> actions = new ArrayList<PlayerAction>();
 		actions.add(PlayerAction.MoveUp);
 		actions.add(PlayerAction.MoveDown);
 		// should be rename to action perhaps
 		actions.add(PlayerAction.Action);
 		List<InputData> data = inputManager.getData(input, actions);
 
 		for (InputData d : data) {
 			PlayerAction action = d.getAction();
 			Player p = d.getPlayer();
 			switch (action) {
 			case MoveUp:
 				moveIndex(p, -1);
 				break;
 			case MoveDown:
 				moveIndex(p, 1);
 				break;
 			case Action:
 				model.upgradePlayer(p, attributes.get(playersIndex.get(p)));
 				break;
 			default:
 				break;
 			}
 		}
 
 		// TODO really bad solution
 		try {
 			Thread.sleep(80);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void moveIndex(Player p, int delta) {
 		int currentIndex = playersIndex.get(p);
 		int n = attributes.size();
 		int newIndex = (currentIndex + delta);
 
 		int r = newIndex % n;
 		if (r < 0) {
 			r += n;
 
 		}
 		playersIndex.put(p, r);
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
