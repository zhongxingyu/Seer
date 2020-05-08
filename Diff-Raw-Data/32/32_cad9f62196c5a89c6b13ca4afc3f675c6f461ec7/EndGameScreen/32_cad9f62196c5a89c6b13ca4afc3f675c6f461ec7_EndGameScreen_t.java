 package states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import entities.Camera;
 import game.GunsAndHats;
 import game.ResourceManager;
 
 public class EndGameScreen extends BasicGameState {
 
 	private int id;
 	Camera camera;
 	int winner; //0-3 
 	Image background0;
 	Image background1;
 	Image background2;
 	Image background3;
 	Image b_start;
 	Image b_start_hover;
 	Image b_history;
 	Image b_history_hover;
 	Image b_exit;
 	Image b_exit_hover;
 	
 	long time;
 	
 	public EndGameScreen(int id, Camera camera) {
 		this.id = id;
 		this.camera = camera;
 		winner = 0;
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		background0 = new Image ("data/GameOverRed.jpg");
 		background1 = new Image ("data/GameOverGreen.jpg");
 		background2 = new Image ("data/GameOverPurple.jpg");
 		background3 = new Image ("data/GameOverCian.jpg");
 		b_start         = ResourceManager.getInstance().getImage("SS_BUTTON_NEW_GAME");
 		b_start_hover   = ResourceManager.getInstance().getImage("SS_BUTTON_NEW_GAME_HOVER");
 		b_history       = ResourceManager.getInstance().getImage("SS_BUTTON_HISTORY");
 		b_history_hover = ResourceManager.getInstance().getImage("SS_BUTTON_HISTORY_HOVER");
 		b_exit          = ResourceManager.getInstance().getImage("SS_BUTTON_EXIT");
 		b_exit_hover    = ResourceManager.getInstance().getImage("SS_BUTTON_EXIT_HOVER");
 		time = gc.getTime();
 		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics gr)
 			throws SlickException {
 		if(winner==1) background0.draw(camera.getOffset().getX(), camera.getOffset().getY(), camera.getScale());
 		if(winner==2) background1.draw(camera.getOffset().getX(), camera.getOffset().getY(), camera.getScale());
 		if(winner==3) background2.draw(camera.getOffset().getX(), camera.getOffset().getY(), camera.getScale());
 		if(winner==4) background3.draw(camera.getOffset().getX(), camera.getOffset().getY(), camera.getScale());
 		if(gc.getTime() - time >= 5000) {
 			b_start.draw(camera.getOffset().getX()+50*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), camera.getScale());
 			b_history.draw(camera.getOffset().getX()+310*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), camera.getScale());
 			b_exit.draw(camera.getOffset().getX()+550*camera.getScale(), camera.getOffset().getY()+450*camera.getScale(), camera.getScale());
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		if(gc.getTime() - time >= 5000){
 			Input input = gc.getInput();
 			int mouseX = input.getMouseX();
 			int mouseY = input.getMouseY();
 			
 			if(input.isKeyPressed(Input.KEY_K)) ResourceManager.getInstance().getMusic("MUSIC").pause();
 			if(input.isKeyPressed(Input.KEY_L)) ResourceManager.getInstance().getMusic("MUSIC").resume();
 			
 			if(mouseY >= camera.getOffset().getY() + 450*camera.getScale() && mouseY <= camera.getOffset().getY() + 450*camera.getScale() + b_start.getHeight()*camera.getScale()) {
 				if(mouseX >= camera.getOffset().getX() + 50*camera.getScale() && mouseX <= camera.getOffset().getX() + 50*camera.getScale() + b_start.getWidth()*camera.getScale()) {
 					b_start=b_start_hover.copy();
 					if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
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
 					if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
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
 					if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) gc.exit();
 				} else {
 					b_exit = ResourceManager.getInstance().getImage("SS_BUTTON_EXIT"); 
 				}
 			} else {
 				b_exit = ResourceManager.getInstance().getImage("SS_BUTTON_EXIT");
 			}
 		}
 		
 	}
 
 	@Override
 	public int getID() {
 		return id;
 	}
 	public void setWinner(int winner){
 		ResourceManager.getInstance().getMusic("MUSIC").setVolume(1);
 		this.winner=winner;
 	}
 	}
