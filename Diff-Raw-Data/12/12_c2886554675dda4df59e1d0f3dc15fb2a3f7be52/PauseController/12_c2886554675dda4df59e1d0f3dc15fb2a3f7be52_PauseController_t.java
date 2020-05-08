 package controller;
 
 import java.util.ArrayList;
 import model.Game;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import view.PauseView;
 
 public class PauseController extends BasicGameState{
 	private GameContainer gc;
 	private StateBasedGame sbg;
 	private GameController gameController;
 	private PauseView pauseView;
 	ArrayList<Rectangle> buttonList;
 	
 	public PauseController(GameController gameController){
 		this.gameController = gameController;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		this.gc = gc;
 		this.sbg = sbg;
 		this.pauseView = new PauseView(gameController.getInGameController().getWorldController().getWorld().getWorldWidthPx(), 
									gameController.getInGameController().getWorldController().getWorld().getWorldHeightPx());
 		
 	}
 	/*Render in view*/
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		this.pauseView.render(gc, sbg, g);
 		
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void keyPressed (int key, char c) {
 		if(key == Input.KEY_ESCAPE){
 			sbg.enterState(Game.IN_GAME);
 		}
 		//Check "buttons aka Rects if pressed TODO
 	}
 
 	@Override
 	public int getID() {
 		return Game.PAUSE_MENU;
 	}
 	
 	
 
 }
