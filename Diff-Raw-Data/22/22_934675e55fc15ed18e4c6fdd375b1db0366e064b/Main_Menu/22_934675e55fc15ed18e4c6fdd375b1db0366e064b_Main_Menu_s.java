 package com.tiny.tank;
 
 //import org.lwjgl.input.Mouse;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 
 public class Main_Menu extends BasicGameState {
 	private Button playButton;
 	private Button quitButton;
 	private Image background;
 	private int id;
 	private int posX;
 	private int posY;
 	
 	public Main_Menu(int id){
 		this.id = id;
 	}
 	/** creates new background image as well as new button instances*/
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		background = new Image("res/bg.jpg");
 		playButton= new Button("res/play_button.png",300,200);
 		quitButton= new Button("res/exit_button.png",300,400);
 	}
 	/** prints background and buttons on screen */
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics g) throws SlickException {
 		background.draw();
 		playButton.drawButton(g);
 		quitButton.drawButton(g);
 //		g.drawString("This is Tiny Tanks!",100,50);
 	}
 	/** When a button is clicked, it goes to that state*/
 	@Override
 	public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
 		Input input = arg0.getInput();
 		posX=input.getMouseX();
 		posY=input.getMouseY();
 		
 		
 		if( playButton.isMouseOverButton(posX, posY)) {
			if(input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
 				arg1.enterState((STATES.SELECT_WEAPONS_MENU).getId());
 			}
 		}
 		if(quitButton.isMouseOverButton(posX, posY)) {
			if(input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
 				System.exit(0);
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return id;
 	}
 	
 	@Override
 	public void leave(GameContainer container, StateBasedGame game){
 		TinyTank.setPreviousState(id);
 	}
 
 }
