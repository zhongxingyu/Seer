 package view;
 
 import controller.*;
 
 import org.lwjgl.Sys;
 import org.newdawn.slick.GameContainer;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.gui.AbstractComponent;
 import org.newdawn.slick.gui.ComponentListener;
 import org.newdawn.slick.gui.MouseOverArea;
 
 public class MainMenuState extends BasicGameState {
 
     private int stateID = -1;
     private MainController main;
 
     Image background = null;
     Image startGame = null;
     Image dragonSilver = null;
     Image dragonGold = null;
     Image pegasusSilver = null;
     Image pegasusGold = null;
     int mouseX = 0;
     int mouseY = 0;
     boolean isPegasus = true;
 
     public MainMenuState(int stateID) {
 	this.stateID = stateID;
 	main = MainController.getInstance();
     }
 
     @Override
     public int getID() {
 	return stateID;
     }
 
     float startGameScale = 1;
     float exitScale = 1;
 
     @Override
     public void init(GameContainer gc, StateBasedGame sbg)
 	    throws SlickException {
 	background = new Image("res/menu/drasus.png");
 	pegasusSilver = new Image("res/menu/pegasusSilver.png");
 	pegasusGold = new Image("res/menu/pegasusGold.png");
 	dragonGold = new Image("res/menu/dragonGold.png");
 	dragonSilver = new Image("res/menu/dragonSilver.png");
 	startGame = new Image("res/menu/jouer.png");
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame sbg, int delta)
 	    throws SlickException {
 	getPosClicked(gc);
 
 	boolean insideStartGame = false;
 
 	if ((mouseX >= 270 && mouseX <= pegasusGold.getWidth() + 270)
 		&& (mouseY >= 200 && mouseY <= pegasusGold.getHeight() + 200)) {
 	    isPegasus = true;
 	}
 
 	if ((mouseX >= 865 && mouseX <= pegasusGold.getWidth() + 865)
 		&& (mouseY >= 200 && mouseY <= pegasusGold.getHeight() + 200)) {
 	    isPegasus = false;
 	}
 	
 	if ((mouseX > 530 && mouseX < startGame.getWidth() + 530)
 		&& (mouseY >= 500 && mouseY <= startGame.getHeight() + 500)) {
 	    insideStartGame = true;
 	    if(isPegasus){
 	    main.setPlayerA("Pegasus");
 	    }
 	    else{
 		    main.setPlayerA("Dragon");
 	    }
 	}
 	
 	if (insideStartGame)
 	    sbg.enterState(ViewController.GAMEPLAYSTATE);
 
     }
 
     @Override
     public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
 	    throws SlickException {
 	background.draw(0, 0);
 	startGame.draw(530, 500);
 	if (isPegasus) {
 	    pegasusGold.draw(270, 200);
 	    dragonSilver.draw(865, 200);
 	} else {
 	    pegasusSilver.draw(270, 200);
 	    dragonGold.draw(865, 200);
 	}
     }
 
    private void getPosClicked(GameContainer gc) {
 	Input input = gc.getInput();
 	if (input.isMousePressed(0)) {
 
 	    mouseX = input.getMouseX();
 	    mouseY = input.getMouseY();
 
 	}
     }
 }
