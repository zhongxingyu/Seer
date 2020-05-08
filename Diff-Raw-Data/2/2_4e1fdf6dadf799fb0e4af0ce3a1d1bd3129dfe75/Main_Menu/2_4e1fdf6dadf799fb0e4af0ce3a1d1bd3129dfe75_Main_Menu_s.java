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
 	
 	private Input input;
 	
 	public Main_Menu(int id){
 		this.id = id;
 	}
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		background = new Image("res/bg.jpg");
 		playButton= new Button("res/play_button.png",300,200);
 		quitButton= new Button("res/exit_button.png",300,400);
 		input = container.getInput();
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
 		background.draw();
 		playButton.drawButton(g);
 		quitButton.drawButton(g);
 		g.drawString("This is Tiny Tanks!",100,50);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
 		
 		
 		if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 			posX=input.getMouseX();
 			posY=input.getMouseY();
			if(input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
 				game.enterState((STATES.SELECT_WEAPONS_MENU).getId());
 			}
 			if(quitButton.isMouseOverButton(posX, posY)) {
 				System.exit(0);
 			}
 		}
 	}
 
 
 	
 	@Override
 	public void leave(GameContainer container, StateBasedGame game){
 		TinyTank.setPreviousState(id);
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return id;
 	}
 }
