 package net.teamwraith.testgame;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.*;
 
 public class Game extends StateBasedGame{
 
	public static final String GAMENAME = "TESTGAME";
	public static final int MENU = 0;
	public static final int PLAY = 1;
 
	public Game(String GAMENAME){
		super(GAMENAME);
		this.addState(new Menu(MENU));
 	}
 	
 	public void initStatesList(GameContainer gc) throws SlickException {
		this.getState(MENU).init(gc, this);
		this.enterState(MENU);
 	}
 	
 	public static void main(String[] args) {
 		AppGameContainer appgc;
 		try{
			appgc = new AppGameContainer(new Game(GAMENAME));
 			appgc.setDisplayMode(1280, 720, false);
 			appgc.start();
 		}catch(SlickException e){
 			e.printStackTrace();
 		}
 	}
 
 
 }
