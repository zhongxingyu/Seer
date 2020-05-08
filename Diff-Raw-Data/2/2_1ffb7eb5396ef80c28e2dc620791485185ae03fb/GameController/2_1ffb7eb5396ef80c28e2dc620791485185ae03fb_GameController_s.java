 package edu.chl.codenameg.controller;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * This controller starts a StateBasedGame and adds the states that Romijam requires
  * 
  * @author ????
  */
 public class GameController extends StateBasedGame {
 	
 	public GameController() {
 		super("CodenameG");
 	}
 	
 	/**
 	 * Initates the MainMenuState, LevelState, PausedLevelState and SelectLevelMenuState
 	 * for use in the StateBasedGame
 	 */
 	public void initStatesList(GameContainer container) throws SlickException {
 		this.addState(new MainMenuState());
 		LevelState levelState = new LevelState();
 		this.addState(levelState);
 		this.addState(new PausedLevelState(levelState));
 		this.addState(new SelectLevelMenuState());
 	}
 	
 	@Override
 	public void preRenderState(GameContainer gc, Graphics g)
 			throws SlickException {
 		g.translate(20,20);
		g.setClip(20, 20, 500, 500);
 	}
 }
