 package zed;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.*;
 
 public class Menu extends BasicGameState {
 	Image button;
 	Image menu_background;
 	
 	public Menu(int state){}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		button = new Image("images/pressedbutton.png");
 		menu_background = new Image("images/conceptfinallow.png");
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		menu_background.draw(0,0, 640, 480);	
 	
 		button.draw(220, 260);
 		button.draw(360, 260);
 		button.draw(289, 260);
 		g.drawString("load", 224, 267);
 		g.drawString("exit", 364, 267);
 		g.drawString("new", 297, 267);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int g)
 			throws SlickException {
 		//XXX put int he logic for when a button is pressed
 		// I have the code written down on paper, just need to
 		// figure out coords and type it in
 		
 		int posX = gc.getInput().getMouseX(); //detects user loading a saved game
 		int posY = gc.getInput().getMouseY();
 		if (((posX > 220) && (posX < 220 + 47))
 				&& ((posY > 260) && (posY < 260 + 39))){
 			if (gc.getInput().isMousePressed(0)){
 				sbg.enterState(1);
 			}
 		}
 		
 		if (((posX > 289) && (posX < 289 + 47)) //detects user starting a new game
 				&& ((posY > 260) && (posY < 260 + 39))){
 			if (gc.getInput().isMousePressed(0)){
 				sbg.enterState(1);
 			}
 		}
 		
 		if (((posX > 360) && (posX < 360 + 47)) && ((posY > 260) && (posY < 260 + 39))){ //detects user exiting the game
 			if (Mouse.isButtonDown(0)){
 				System.exit(0);
 			}
 		}
 		
 		
 		
 	}
 
 	@Override
 	public int getID() {
 		return 0;
 	}
 	
 
 	
 }
