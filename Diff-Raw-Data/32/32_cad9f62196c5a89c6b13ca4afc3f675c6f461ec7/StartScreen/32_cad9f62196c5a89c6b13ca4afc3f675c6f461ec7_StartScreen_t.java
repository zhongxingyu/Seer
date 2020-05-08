 package states;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import entities.Camera;
 import game.GunsAndHats;
 import game.ResourceManager;
 
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
 
 		background		= ResourceManager.getInstance().getImage("START_SCREEN");
 		b_start         = ResourceManager.getInstance().getImage("SS_BUTTON_NEW_GAME");
 		b_start_hover   = ResourceManager.getInstance().getImage("SS_BUTTON_NEW_GAME_HOVER");
 		b_history       = ResourceManager.getInstance().getImage("SS_BUTTON_HISTORY");
 		b_history_hover = ResourceManager.getInstance().getImage("SS_BUTTON_HISTORY_HOVER");
 		b_exit          = ResourceManager.getInstance().getImage("SS_BUTTON_EXIT");
 		b_exit_hover    = ResourceManager.getInstance().getImage("SS_BUTTON_EXIT_HOVER");
 		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics gr)
 			throws SlickException {
 		
 		gr.setColor(Color.white);
 		
 		float scale = camera.getScale();
 		background.draw(camera.getOffset().getX(), camera.getOffset().getY(), scale);
 		b_start.draw(camera.getOffset().getX()+50*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), scale);
 		b_history.draw(camera.getOffset().getX()+310*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), scale);
 		b_exit.draw(camera.getOffset().getX()+550*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), scale);
 		
 		Vector2f creditPos = camera.getRelPos(new Vector2f(20,550));
 		
 		gr.drawString("A Game by: Victor Dumitrescu, Gordon Edwards,", creditPos.x + camera.getOffset().x, creditPos.y + camera.getOffset().y);
 		gr.drawString("Peter Henderson, Oana Hritcu, Nikolaus Huber", creditPos.x + camera.getOffset().x, creditPos.y + camera.getOffset().y + 20*camera.getScale());
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		Input input = gc.getInput();
 		int mouseX = input.getMouseX();
 		int mouseY = input.getMouseY();
 		
 		if(input.isKeyPressed(Input.KEY_K)) ResourceManager.getInstance().getMusic("MUSIC").pause();
 		if(input.isKeyPressed(Input.KEY_L)) ResourceManager.getInstance().getMusic("MUSIC").resume();
 		
 		//if inside the right Y range for all buttons
 		if(mouseY >= camera.getOffset().getY() + 450*camera.getScale() && mouseY <= camera.getOffset().getY() + 450*camera.getScale() + b_start.getHeight()*camera.getScale()) {
 			//if inside the right X range for the start button
 			if(mouseX >= camera.getOffset().getX() + 50*camera.getScale() && mouseX <= camera.getOffset().getX() + 50*camera.getScale() + b_start.getWidth()*camera.getScale()) {
 				b_start=b_start_hover.copy();//.setRotation(5.0f);
 				if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
					PreGameMenuNew pre = (PreGameMenuNew)game.getState(GunsAndHats.PREGAMESTATE);
					pre.init(gc, game);
 					game.enterState(GunsAndHats.PREGAMESTATE);
 				}
 			} else {
 				b_start = ResourceManager.getInstance().getImage("SS_BUTTON_NEW_GAME");
 			}
 		} else {
 			b_start = ResourceManager.getInstance().getImage("SS_BUTTON_NEW_GAME");
 		}
 		
 		if(mouseY >= camera.getOffset().getY() + 450*camera.getScale() && mouseY <= camera.getOffset().getY() + 450*camera.getScale() + b_start.getHeight()*camera.getScale()) {
 			if(mouseX >= camera.getOffset().getX() + 310*camera.getScale() && mouseX <= camera.getOffset().getX() + 310*camera.getScale() + b_start.getWidth()*camera.getScale()) {
 				b_history=b_history_hover.copy();
 				if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 					game.getState(GunsAndHats.HISTORYSCREEN).init(gc, game);
 					game.enterState(GunsAndHats.HISTORYSCREEN);
 				}
 			} else {
 				b_history = ResourceManager.getInstance().getImage("SS_BUTTON_HISTORY");  
 			}
 		} else {
 			b_history = ResourceManager.getInstance().getImage("SS_BUTTON_HISTORY");
 		}
 		
 		if(mouseY >= camera.getOffset().getY() + 450*camera.getScale() && mouseY <= camera.getOffset().getY() + 450*camera.getScale() + b_start.getHeight()*camera.getScale()) {
 			if(mouseX >= camera.getOffset().getX() + 550*camera.getScale() && mouseX <= camera.getOffset().getX() + 550*camera.getScale() + b_start.getWidth()*camera.getScale()) {
 				b_exit=b_exit_hover.copy();
 				if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) gc.exit();
 			} else {
 				b_exit = ResourceManager.getInstance().getImage("SS_BUTTON_EXIT"); 
 			}
 		} else {
 			b_exit = ResourceManager.getInstance().getImage("SS_BUTTON_EXIT");
 		}
 		
 	}
 
 	@Override
 	public int getID() {
 		return id;
 	}
 
 }
