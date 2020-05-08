 package com.github.joakimpersson.tda367.controller;
 
 import java.beans.PropertyChangeSupport;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.github.joakimpersson.tda367.audio.AudioEventListener;
 import com.github.joakimpersson.tda367.gui.HighscoreView;
 import com.github.joakimpersson.tda367.gui.IView;
 import com.github.joakimpersson.tda367.model.constants.EventType;
 
 public class HighscoreState extends BasicGameState {
 
 	private int stateID = -1;
 	private IView view = null;
 	private PropertyChangeSupport pcs;
 
 	public HighscoreState(int stateID) {
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
 		this.view = new HighscoreView();
 
 		this.pcs = new PropertyChangeSupport(this);
 		this.pcs.addPropertyChangeListener(AudioEventListener.getInstance());
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
 		if (input.isKeyDown(Input.KEY_ESCAPE)) {
 			container.exit();
 		}
		pcs.firePropertyChange("play", null, EventType.MENU_CLICKED);
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
