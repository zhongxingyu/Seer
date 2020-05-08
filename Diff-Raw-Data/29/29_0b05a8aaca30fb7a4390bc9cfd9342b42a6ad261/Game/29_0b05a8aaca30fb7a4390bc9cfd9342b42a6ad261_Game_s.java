 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class Game extends BasicGame {
 
 	static final int SIZE_X = 800;
 	static final int SIZE_Y = 600;
 	Player player1 = null;
 	Player player2 = null;
 	private static final int NORTH = 0;
 	private static final int WEST= 1;
 	private static final int SOUTH = 2;
 	private static final int EAST = 3;
 
 	public Game() {
 		super("Super awesome game");
 	}
 
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app = null;
 		app = new AppGameContainer(new Game());
 
 		app.setDisplayMode(SIZE_X, SIZE_Y, false);
 		app.start();
 
 	}
 
 	@Override
 	public void render(GameContainer arg0, Graphics arg1) throws SlickException {
 		player1.getImage().draw(player1.getPosX(), player1.getPosY(), player1.getScale());
 		player2.getImage().draw(player2.getPosX(), player2.getPosY(), player2.getScale());
 
 	}
 
 	@Override
 	public void init(GameContainer arg0) throws SlickException {
 
		player1 = new Player(new Image("src/resource/sprite.png"), 0, 0);
		player2 = new Player(new Image("src/resource/sprite.png"), 400, 400);
 
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {
 		Input input = gc.getInput();
 
 		if (input.isKeyDown(Input.KEY_A)) {
 			player1.moveWest();
 		} else {
 			player1.setKeyPressed(WEST, false);
 		}
 
 		if (player1.isWalking(WEST)) {
 			player1.walkAnimate(WEST);
 		}
 		
 
 		if (input.isKeyDown(Input.KEY_D)) {
 			player1.moveEast();
 		} else {
 			player1.setKeyPressed(EAST, false);
 		}
 		
 		if (player1.isWalking(EAST)) {
 			player1.walkAnimate(EAST);
 		}
 
 		if (input.isKeyDown(Input.KEY_W)) {
 			player1.moveNorth();
 		} else {
 			player1.setKeyPressed(NORTH, false);
 		}
 		
 		if (player1.isWalking(NORTH)) {
 			player1.walkAnimate(NORTH);
 		}
 
 		if (input.isKeyDown(Input.KEY_S)) {
 			player1.moveSouth();
 		} else {
 			player1.setKeyPressed(SOUTH, false);
 		}
 		
 		if (player1.isWalking(SOUTH)) {
 			player1.walkAnimate(SOUTH);
 		}
 
 		if (input.isKeyDown(Input.KEY_LEFT)) {
 			player2.moveWest();
 		} else {
 			player2.setKeyPressed(WEST, false);
 		}
 		
 		if (player2.isWalking(WEST)) {
 			player2.walkAnimate(WEST);
 		}
 		
 		if (input.isKeyDown(Input.KEY_RIGHT)) {
 			player2.moveEast();
 		} else {
 			player2.setKeyPressed(EAST, false);
 		}
 		
 		if (player2.isWalking(EAST)) {
 			player2.walkAnimate(EAST);
 		}
 
 		if (input.isKeyDown(Input.KEY_UP)) {
 			player2.moveNorth();
 		} else {
 			player2.setKeyPressed(NORTH, false);
 		}
 		
 		if (player2.isWalking(NORTH)) {
 			player2.walkAnimate(NORTH);
 		}
 
 		if (input.isKeyDown(Input.KEY_DOWN)) {
 			player2.moveSouth();
 		} else {
 			player2.setKeyPressed(SOUTH, false);
 		}
 		
 		if (player2.isWalking(SOUTH)) {
 			player2.walkAnimate(SOUTH);
 		}
 	}
 }
