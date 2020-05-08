 package com.github.joakimpersson.tda367.gui;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class GameplayView implements IView {
 
 	private GameFieldView gameFieldView = null;
 	private PlayerInfoContainerView infoContainer = null;
 	private RoundInfoView roundInfoView = null;
 	private RoundWaitingView roundWaitingView = null;

 	public GameplayView() {
 		init();
 	}
 
 	private void init() {
 		infoContainer = new PlayerInfoContainerView(0, 0);
 		gameFieldView = new GameFieldView(205, 0);
 		roundInfoView = new RoundInfoView();
 		roundWaitingView = new RoundWaitingView();
 	}
 
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
 
 	public void showRoundStats(GameContainer container, Graphics g)
 			throws SlickException {
 		roundInfoView.render(container, g);
 	}
 
 	public void showWaitingBox(GameContainer container, Graphics g)
 			throws SlickException {
		roundWaitingView.render(container, g);
 	}
 
 }
