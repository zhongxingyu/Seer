 package org.soupware.pewpew;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class MenuState extends BasicGameState {
 	
 	Image background = null;
 	Image menuOptions = null;
     Image hostGameOption = null;
     Image joinGameOption = null;
     Image highscoreOption = null;
     Image exitOption = null;
     Image hostGameHeading = null;
     Image joinGameHeading = null;
     Image highscoreHeading = null;
     Image bottomOptions = null;
     Image backOption = null;
     Image rightOption = null;
 
     int selected[] = {0, 0, 0, 0};
     int bottomSelected[] = {0, 0, 0};
     int selectedOption = 0;
     int stateID = 0;
     int level = 0;
     int bgX = 0;
     int bgY = 0;
     int headingX = 10;
     int headingY = 490;
     int bottomLeftX = 10;
     int bottomRightX = 390;
     int bottomY = 880;
     int menuX = 360;
     int menuY = 200;
     int bgMoveDir = 0;
 
     public MenuState(int stateID) {
         this.stateID = stateID;
     }
     
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		background = new Image("data/img/splash.png");
 		menuOptions = new Image("data/img/menuops.png");
 		bottomOptions = new Image("data/img/bottomops.png");
 	}
 
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		background.draw(bgX, bgY);
 		
 		hostGameOption = menuOptions.getSubImage(selected[0] * 320, 0, 320, 60);
 		joinGameOption = menuOptions.getSubImage(selected[1] * 320, 60, 320, 60);
 		highscoreOption = menuOptions.getSubImage(selected[2] * 320, 120, 320, 60);
 		exitOption = menuOptions.getSubImage(selected[3] * 320, 180, 320, 60);
 		
 		hostGameHeading = menuOptions.getSubImage(320, 0, 320, 60);
 		joinGameHeading = menuOptions.getSubImage(320, 60, 320, 60);
 		highscoreHeading = menuOptions.getSubImage(320, 120, 320, 60);
 		
 		backOption = bottomOptions.getSubImage(bottomSelected[0] * 320, 0, 320, 60);
 		rightOption = bottomOptions.getSubImage(bottomSelected[1] * 320, bottomSelected[2] * 60, 320, 60);
 		
 		int offMenuY = menuY + bgY;
 		int offHeadingY = headingY + bgY;
 		int offBottomY = bottomY + bgY;
 		
 		hostGameOption.draw(menuX, offMenuY);
 		joinGameOption.draw(menuX, offMenuY + 60);
 		highscoreOption.draw(menuX, offMenuY + 120);
 		exitOption.draw(menuX, offMenuY + 180);
 		
 		backOption.draw(bottomLeftX, offBottomY);
 		if (bottomSelected[2] != 0) {
 			rightOption.draw(bottomRightX, offBottomY);
 		}
 		
 		if (selected[0] == 1) {
 			hostGameHeading.draw(headingX, offHeadingY);
 		} else if (selected[1] == 1) {
 			joinGameHeading.draw(headingX, offHeadingY);
 		} else if (selected[2] == 1) {
 			highscoreHeading.draw(headingX, offHeadingY);
 		}
 	}
 
 	private boolean between(int x, int a, int b) {
 		return (x > a && x < b);
 	}
 	
 	@Override
 	public void update(GameContainer arg0, StateBasedGame arg1, int delta)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		Input input = arg0.getInput();
 		 
 		int mouseX = input.getMouseX();
 		int mouseY = input.getMouseY();
 		int mouseOn = -1;
 		
 		if (level == 0) {
 			selected[0] = 0;
 			selected[1] = 0;
 			selected[2] = 0;
 			selected[3] = 0;
 						
 			if (between(mouseX, menuX, menuX + 320)) {
 				if (between(mouseY, menuY, menuY + 60)) {
 					selectedOption = 0;
 					mouseOn = 0;
 				} else if (between(mouseY, menuY + 60, menuY + 120)) {
 					selectedOption = 1;
 					mouseOn = 1;
 				} else if (between(mouseY, menuY + 120, menuY + 180)) {
 					selectedOption = 2;
					mouseOn = 1;
 				} else if (between(mouseY, menuY + 180, menuY + 240)) {
 					selectedOption = 3;
					mouseOn = 1;
 				} else {
 					mouseOn = -1;
 				}
 			} else {
 				mouseOn = -1;
 			}
 			
 			if (input.isKeyPressed(Input.KEY_S) || input.isKeyPressed(Input.KEY_DOWN)) {
 				selectedOption = (selectedOption + 1) % 4;
 			}
 			
 			if (input.isKeyPressed(Input.KEY_W) || input.isKeyPressed(Input.KEY_UP)) {
 				selectedOption = (selectedOption + 7) % 4;
 			}
 			
 			selected[selectedOption] = 1;
 			
 			if (selected[0] == 1) {
 				if ((mouseOn == 0 && input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) || input.isKeyPressed(Input.KEY_ENTER)) {
 					 bgMoveDir = 1;
 					 level = 1;
 					 bottomSelected[2] = 1;
				 }
 			}
 			
 			if (selected[1] == 1) {
 				if ((mouseOn == 1 && input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) || input.isKeyPressed(Input.KEY_ENTER)) {
 					 bgMoveDir = 1;
 					 level = 1;
 					 bottomSelected[2] = 2;
				 }
 			}
 			
 			if (selected[2] == 1) {
 				if ((mouseOn == 2 && input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) || input.isKeyPressed(Input.KEY_ENTER)) {
 					 bgMoveDir = 1;
 					 level = 1;
 					 bottomSelected[2] = 0;
				 }
 			}
 			
 			if (selected[3] == 1) {
 				 if ((mouseOn == 3 && input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) || input.isKeyPressed(Input.KEY_ENTER)) {
 					 arg0.exit();
 				 }
 			}
 		}
 		
 		if (level == 1) {
 			bottomSelected[0] = 0;
 			bottomSelected[1] = 0;
 
 			if (between(mouseY, bottomY - 480, bottomY + 420)) {
 				if (between(mouseX, bottomLeftX, bottomLeftX + 320)) {
 					bottomSelected[0] = 1;
 				} else if (between(mouseX, bottomRightX, bottomRightX + 320)) {
 					bottomSelected[1] = 1;
 				}
 			}
 			
 			if (input.isKeyPressed(Input.KEY_ESCAPE)) {
 				level = 0;
 				bgMoveDir = -1;
 			}
 			
 			if (bottomSelected[0] == 1) {
 				if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
 					level = 0;
 					bgMoveDir = -1;
 				}
 			}
 		}
 		
 		if (bgMoveDir == 1) {
 			if (bgY > -480) {
 				bgY -= 1.0f * delta;
 			} else {
 				bgY = -480;
 				bgMoveDir = 0;
 			}
 		} else if (bgMoveDir == -1) {
 			if (bgY < 0) {
 				bgY += 1.0f * delta;
 			} else {
 				bgY = 0;
 				bgMoveDir = 0;
 			}
 		}
 	}
 	
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return stateID;
 	}
 
 }
