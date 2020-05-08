 package com.tiny.tank;
 
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Pause_Menu extends BasicGameState {
 	
 	private Button playButton;
 	private Button menuButton;
 	private Image pause_menu_background;
 	private int id; 
 	private int posX;
 	private int posY;
 	
 	public Pause_Menu(int id) {
 		this.id=id;
 	}
 	
 	
 	public void enter(GameContainer container,StateBasedGame game) {
 		/** Make an image out of what was displayed during gameplay*/
 		try {
 			pause_menu_background=new Image(container.getWidth(),container.getHeight());
 			
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		Graphics g=container.getGraphics();
 		/** Don't continue prints the FPS counter*/
 		container.setShowFPS(false);
 		g.copyArea(pause_menu_background, 0, 0);
 		
 		
 		
 	}
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
 		playButton=new Button("res/play_button.png",300,100);
 		menuButton=new Button("res/back_button.png",300,300);
 	}
 
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics g)
 			throws SlickException {
 		pause_menu_background.draw();/** Draw the image of the previous state*/
 		/** draw the buttons on the pause menu*/
 		playButton.drawButton(g);
 		menuButton.drawButton(g);
 	}
 
 	@Override
 	public void update(GameContainer arg0, StateBasedGame state, int arg2)
 			throws SlickException {
 		/** create new input object for getting mouse coordinates*/
 		Input input=arg0.getInput();
 		posX=input.getMouseX();
 		posY=input.getMouseY();
 		
 		/** if the mouse is clicked over a button, go to the state specified under the control statement*/
 		if(playButton.isMouseOverButton(posX, posY)) {
			if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 				state.enterState(STATES.MAIN_GAMEPLAY.getId());
 			}
 		}
 		if(menuButton.isMouseOverButton(posX, posY)) {
			if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 				state.enterState(STATES.MAIN_MENU.getId());
 			}
 		}
 	}
	@Override
	public void leave(GameContainer container,StateBasedGame game) {
		container.getInput().clearMousePressedRecord();
	}
 	/** this is the state ID that specified in the STATES class*/
 	@Override
 	public int getID() {
 		return id;
 	}
	
 }
