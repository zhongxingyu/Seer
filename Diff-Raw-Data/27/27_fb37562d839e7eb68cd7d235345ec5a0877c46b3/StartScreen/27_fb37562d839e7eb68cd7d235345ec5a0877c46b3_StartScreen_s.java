 package states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import entities.Camera;
 
 public class StartScreen extends BasicGameState{
 
 	private int id;
 	Camera camera;
 	
 	Image background;
 	Image b_start;
 	Image b_start_hover;
 	Image b_history;
 	Image b_history_hover;
 	Image b_exit;
 	Image b_exit_hover;
 	
 	public StartScreen(int id, Camera camera) {
 		this.id = id;
 		this.camera = camera;
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		background      = new Image("data/startscreen.JPG");
 		b_start         = new Image("data/button_new_game.png");
 		b_start_hover   = new Image("data/button_new_game_hover.png");
 		b_history       = new Image("data/button_history.png");
 		b_history_hover = new Image("data/button_history_hover.png");
 		b_exit          = new Image("data/button_exit.png");
 		b_exit_hover    = new Image("data/button_exit_hover.png");
 		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics gr)
 			throws SlickException {
 		gr.drawString("LOL",0,0);
 		background.draw(camera.getOffset().getX(), camera.getOffset().getY(), camera.getScale());
 		b_start.draw(camera.getOffset().getX()+50*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), camera.getScale());
 		b_history.draw(camera.getOffset().getX()+310*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), camera.getScale());
 		b_exit.draw(camera.getOffset().getX()+550*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), camera.getScale());
 		// +n*camera.getScale()
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 		Input input = gc.getInput();
 		int mouseX = input.getMouseX();
 		int mouseY = input.getMouseY();
 		
 		//if inside the right Y range for all buttons
 		if(mouseY >= camera.getOffset().getY() + 450*camera.getScale() && mouseY <= camera.getOffset().getY() + 450*camera.getScale() + b_start.getHeight()*camera.getScale()) {
 			//if inside the right X range for the start button
 			if(mouseX >= camera.getOffset().getX() + 50*camera.getScale() && mouseX <= camera.getOffset().getX() + 50*camera.getScale() + b_start.getWidth()*camera.getScale()) {
 				b_start=b_start_hover.copy();//.setRotation(5.0f);
				if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) sb.enterState(3); // id 3 => GAMESTATE
 			} else {
 				b_start = new Image("data/button_new_game.png");
 			}
 		} else {
 			b_start = new Image("data/button_new_game.png");
 		}
 		
 		if(mouseY >= camera.getOffset().getY() + 450*camera.getScale() && mouseY <= camera.getOffset().getY() + 450*camera.getScale() + b_start.getHeight()*camera.getScale()) {
 			if(mouseX >= camera.getOffset().getX() + 310*camera.getScale() && mouseX <= camera.getOffset().getX() + 310*camera.getScale() + b_start.getWidth()*camera.getScale()) {
 				b_history=b_history_hover.copy();
				if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) sb.enterState(6); // 6 should be HISTORYSCREEN according to GunsAndHats.java
 			} else {
 				b_history = new Image("data/button_history.png");   
 			}
 		} else {
 			b_history = new Image("data/button_history.png");
 		}
 		
 		if(mouseY >= camera.getOffset().getY() + 450*camera.getScale() && mouseY <= camera.getOffset().getY() + 450*camera.getScale() + b_start.getHeight()*camera.getScale()) {
 			if(mouseX >= camera.getOffset().getX() + 550*camera.getScale() && mouseX <= camera.getOffset().getX() + 550*camera.getScale() + b_start.getWidth()*camera.getScale()) {
 				b_exit=b_exit_hover.copy();
				if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) sb.enterState(0); // !not sure what to do about this one
 			} else {
 				b_exit = new Image("data/button_exit.png"); 
 			}
 		} else {
 			b_exit = new Image("data/button_exit.png");
 		}
 		
 	}
 
 	@Override
 	public int getID() {
 		return id;
 	}
 
 }
