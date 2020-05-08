 package com.github.joakimpersson.tda367.controller;
 
 import java.beans.PropertyChangeSupport;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.github.joakimpersson.tda367.audio.AudioEventListener;
 import com.github.joakimpersson.tda367.gui.IView;
 import com.github.joakimpersson.tda367.gui.MainMenuView;
 import com.github.joakimpersson.tda367.model.constants.EventType;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class MainMenuState extends BasicGameState {
 
 	private IView view = null;
 	private int stateID = -1;
 	private PropertyChangeSupport pcs;
 
 	public MainMenuState(int stateID) {
 		this.stateID = stateID;
 	}
	
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
		
 		pcs.firePropertyChange("play", null, EventType.TITLE_SCREEN);
		
 	}
	
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		this.pcs = new PropertyChangeSupport(this);
 		this.pcs.addPropertyChangeListener(AudioEventListener.getInstance());
 		view = new MainMenuView();
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		view.render(container, g);
 
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		Input input = container.getInput();
 
 		// TODO jocke only used during development
 		if (input.isKeyDown(Input.KEY_ESCAPE)
 				|| input.isKeyPressed(Input.KEY_Q)) {
 			container.exit();
 		}
 
 		if (input.isKeyDown(Input.KEY_G)) {
 			pcs.firePropertyChange("play", null, EventType.MENU_CLICKED);
 			game.enterState(BombermanGame.SETUP_GAME_STATE);
			// game.enterState(BombermanGame.GAMEPLAY_STATE);
 		}
 
 		if (input.isKeyDown(Input.KEY_H)) {
 			pcs.firePropertyChange("play", null, EventType.MENU_CLICKED);
 			game.enterState(BombermanGame.HIGHSCORE_STATE);
 		}
 		if (input.isKeyDown(Input.KEY_D)) {
 			pcs.firePropertyChange("play", null, EventType.MENU_CLICKED);
 			game.enterState(BombermanGame.GAMEPLAY_STATE);
 		}
 
 		if (input.isKeyDown(Input.KEY_U)) {
 			pcs.firePropertyChange("play", null, EventType.MENU_CLICKED);
 			game.enterState(BombermanGame.UPGRADE_PLAYER_STATE);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
