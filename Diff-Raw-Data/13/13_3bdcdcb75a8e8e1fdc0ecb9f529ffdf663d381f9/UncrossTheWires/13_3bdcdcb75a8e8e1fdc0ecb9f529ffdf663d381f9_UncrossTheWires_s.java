 package com.github.rocketsurgery;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class UncrossTheWires extends StateBasedGame {
 
 	public static final int MAIN_MENU = 0;
 	public static final int MOST_SOLVED = 1;
 	public static final int LOWEST_SCORE = 2;
 	public static final int SCORE_MENU = 3;
 	
 	public static final String[] gameModes = { "Most Solved", "Lowest Score" };
 	
 	public static final int screenX = 800;
 	public static final int screenY = 600;
 	
 	public UncrossTheWires() {
 		super("Uncross the Wires");
 	}
 
 	@Override
 	public void initStatesList(GameContainer arg0) throws SlickException {
 		this.addState(new MainMenu());
 		this.addState(new MostSolved());
 		this.addState(new LowestScore());
 		this.addState(new ScoreMenu());
 	}
 
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app = new AppGameContainer(new UncrossTheWires());
 
 		app.setDisplayMode(screenX, screenY, false);
 		app.start();
 	}
 
 }
