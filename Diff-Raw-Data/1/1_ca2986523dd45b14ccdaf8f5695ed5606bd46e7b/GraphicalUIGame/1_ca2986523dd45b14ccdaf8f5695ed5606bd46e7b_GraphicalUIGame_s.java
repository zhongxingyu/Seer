 package bombgame.ui.gui;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import bombgame.controller.ai.IManAI;
 import bombgame.controller.gamehandler.IGameHandler;
 import bombgame.entities.IBomb;
 import bombgame.entities.IExplosion;
 import bombgame.entities.IGameObject;
 import bombgame.entities.IMan;
 
 public final class GraphicalUIGame extends BasicGameState {
 	
 	public static final int ID = 2;
 	
 	// Can put this in player
 	public static final char UP = 'w';
 	public static final char LEFT = 'a';
 	public static final char DOWN = 's';
 	public static final char RIGHT = 'd';
 	public static final char BOMB = 'j';
 	
 	//private int tile_size = 30; // 30 * 30 tiles
 	
 	private IGameHandler handler;
 	private IGameObject[][] field;
 	private char user_input = '?';
 	
 	private static final String WALL = "res/wall.png";
 	private static final String PATH = "res/path.png";
 	private static final String MAN = "res/man.png";
 	private static final String BOMBS = "res/bomb.png";
 	private static final String EXPLOSION = "res/explosion.png";
 	private Image wall_img;
 	private Image path_img;
 	private Image man_img;
 	private Image bomb_img;
 	private Image explosion_img;
 	
 	private int width_px;
 	private int height_px;
 	
 	private GameContainer container;
 	private StateBasedGame game;
 	
 	//test
 	Animation anim;
 	
 	public GraphicalUIGame(IGameHandler handler) {
 		this.handler = handler;
 		field = handler.getField().getField();
 	}
 	
 	public void init(GameContainer container, StateBasedGame game)
 	throws SlickException {
 	
 		this.container = container;
 		this.game = game;
 		
 		// Tile size
 		int width = field.length;
 		int height = field[0].length;
 		
 		// Real display size
 		width_px = container.getWidth() / width;
 		height_px = container.getHeight() / height;
 		
 		wall_img = new Image(WALL);
 		path_img = new Image(PATH);
 		man_img = new Image(MAN);
 		bomb_img = new Image(BOMBS);
 		explosion_img = new Image(EXPLOSION);
 		
 		//container.setTargetFrameRate(80);
 		container.setMinimumLogicUpdateInterval(250);
 		
 		// test
 		//anim = new Animation(new Image[]{man_img, man_img, man_img}, 250);
 	}
 	
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 	throws SlickException {
 		
 		// Go through the complete field
 		for (int i = 0; i < field[0].length; i++) {
 			for (int j = 0; j < field.length; j++) {
 					int x = j * width_px;
 						int y = i * height_px;
 		
 				if (field[j][i] == null) {
 					path_img.draw(x, y, width_px, height_px);
 				} else if (field[j][i] instanceof IMan) {
 				
 					/*if (changed) {
 					for (IManAI ai : handler.getAIs()) {
 					smoothMove(ai.getMan().getX(), ai.getMan().getY());
 					}
 					changed = false;
 					}*/
 					
 					man_img.draw(x, y, width_px, height_px);
 				} else if (field[j][i] instanceof IBomb) {
 					bomb_img.draw(x, y, width_px, height_px);
 				} else if (field[j][i] instanceof IExplosion) {
 					explosion_img.draw(x, y, width_px, height_px);
 				} else {
 					wall_img.draw(x, y, width_px, height_px);
 				}
 			}
 		}
 	}
 	
 	public void update(GameContainer container, StateBasedGame game, int delta)
 	throws SlickException {
 	
 		handler.getPlayer().move(user_input);
 		handler.updateAll();
 		field = handler.getField().getField();
 		}
 		
 	public int getID() {
 		return ID;
 	}
 	
 	public void keyPressed(int key, char c) {
 		if (key == Input.KEY_W) {
 			user_input = UP;
 		}
 		
 		if (key == Input.KEY_A) {
 			user_input = LEFT;
 		}
 		
 		if (key == Input.KEY_S) {
 			user_input = DOWN;
 		}
 		
 		if (key == Input.KEY_D) {
 			user_input = RIGHT;
 		}
 		
 		if (key == Input.KEY_SPACE) {
 			user_input = BOMB;
 		}
 		
 		if (key == Input.KEY_ESCAPE) {
 			game.enterState(GraphicalUIMenu.ID, new FadeOutTransition(Color.black), new FadeInTransition(Color.black));
 		}
 	}
 	
 	public void keyReleased(int key, char c) {
 		//user_input = '?';
 	}
 
 }
