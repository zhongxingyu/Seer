 package com.tiny.tank;
 
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 
 
 public class Select_Weapons_Menu extends BasicGameState {
 	private int id;
 
 	private Image title = null;
 	private Image p1_title=null;
 	private Image p2_title=null;
 	private Image button_play= null;
 	private Image button_menu= null;
 	private Image ammo_title= null;
	
	
	 Image background = null;
 
 	
 		public Select_Weapons_Menu(int id){
 			this.id = id;
 		}
 	
 		public void init(GameContainer container, StateBasedGame game)
 		throws SlickException {
 			
 
 			background = new Image("res/bg.jpg");
 			title = new Image("res/Weapon_Select_ title.png");
 			button_play= new Image("res/play_button.png");
 			button_menu= new Image("res/back_button.png");
 			p1_title= new Image("res/P1.png");
 			p2_title= new Image("res/P2.png");
 			ammo_title= new Image("res/ammo.png");
 
 
 		}
 
 
 		public void render(GameContainer container, StateBasedGame game, Graphics g)
 		throws SlickException {
 			
 			background.draw(0,0);
 			title.draw(0,0);
 			button_play.draw(500,520);
 			button_menu.draw(80,520);
 			p1_title.draw(30, 150);
 			p2_title.draw(540,150);
 			ammo_title.draw(235,80);
 		
 		}
 
 	
 
 		public void update(GameContainer container, StateBasedGame game, int delta)
 		throws SlickException {
 			
 			int posX = Mouse.getX();
 			int posY = Mouse.getY();
 			
 			// If play game button is clicked on play game, enter the play game state
 			if((posX>500 && posX<720) && (posY>20 && posY<80)){
 				if(Mouse.isButtonDown(0)){
 					game.enterState(STATES.MAIN_GAMEPLAY.getId());
 				}
 				
 			}
 				// If back button is clicked, change state to main menu state		
 			if((posX>80 && posX<300) && (posY>20 && posY<80)){
 				if(Mouse.isButtonDown(0)){
 					game.enterState(0);		
 					//**********game.enterState(STATES.MAIN_GAMEPLAY.getId());
 				}
 			}
 			
 			
 		
 		}
 
 		public int getID() {
 		return id;
 		}
 
 	}
