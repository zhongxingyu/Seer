 package VC;
 import java.io.IOException;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class GameplayState extends BasicGameState{
     public static int VIEWPORT_WIDTH = 600;
     public static int VIEWPORT_HEIGHT = 600;
     public static int VIEWPORT_X, VIEWPORT_Y;
     public static int VIEWPORT_RATIO_X, VIEWPORT_RATIO_Y;
 	int stateID = -1;
 	Player player;
 	long lastClickTime = 0;
 	Map map;
 	int mouseX = 0; int mouseY = 0;
 	public GameplayState(int ID){
 		this.stateID = ID;
 	}
 
 	public int getID() {
 		return stateID;
 	}
 
 	/**
 	 * This method initiates the GameState, creating the map and initializing all the objects
 	 *
 	 */
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		//initialize the viewport & associated ratios
 		VIEWPORT_RATIO_X = PlatformerGame.WIDTH/VIEWPORT_WIDTH;
 		VIEWPORT_RATIO_Y = PlatformerGame.HEIGHT/VIEWPORT_HEIGHT;
 		//Initialize the player, the x/y coordinates will be set via map.loadMap but defaults to the given 100, 100
 		player = new Player(100, 100, VIEWPORT_RATIO_X, VIEWPORT_RATIO_Y);
 		//Initialize the map, to be loaded in the next line
 		map = new Map();
 
 		//grab the map
 		try {
 			//System.out.println(getClass().getClassLoader().getResource("./res/level_1_1.map").getPath());
 			map.loadMap(getClass().getClassLoader().getResource("./res/level_1_1.map").getPath());
 			player.x = map.playerX;
 			player.y = map.playerY;
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		//Initialize the viewport shifts by the player starting coordinates
 		VIEWPORT_X = (int) (player.x - PlatformerGame.WIDTH/2);
 		VIEWPORT_Y = (int) (player.y - PlatformerGame.HEIGHT/2);
 
 	}
 
 	/**
 	 * This method draws the game state.
 	 *
 	 */
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 
 		//render the map and all associated objects
 		map.draw(g, VIEWPORT_X, VIEWPORT_Y);
 		//render the player
 		player.render(g, VIEWPORT_X, VIEWPORT_Y);
 
 	}
 
 	/**
 	 * This method contains all the logic for the game state.
 	 *
 	 */
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 
 		//adjust the viewport to center around the player. everything will obey this shift.
 		VIEWPORT_X = (int) (player.x - PlatformerGame.WIDTH/2);
 		VIEWPORT_Y = (int) (player.y - PlatformerGame.HEIGHT/2);
 //		if(VIEWPORT_X < 0)
 //			VIEWPORT_X = 0;
 //		if(VIEWPORT_Y < 0)
 //			VIEWPORT_Y = 0;
 
 		//translate click events with the viewport coordinates
 		int x = gc.getInput().getMouseX() + VIEWPORT_X;
 		int y = gc.getInput().getMouseY() + VIEWPORT_Y;
 //		int tx = gc.getInput().getMouseX() - VIEWPORT_X;
 //		int ty = gc.getInput().getMouseY() - VIEWPORT_Y;
 
 		//check for input
 		//up
 		if(gc.getInput().isKeyDown(Input.KEY_W)){
 			player.jump();
 		}else{
 
 		//down
 		}if(gc.getInput().isKeyDown(Input.KEY_S)){
 
 		}else{
 
 		//left
 		}if(gc.getInput().isKeyDown(Input.KEY_A)){
 			player.moveLeft();
 
 		}else {
 			player.movingLeft = false;
 		}
 		//right
 		if(gc.getInput().isKeyDown(Input.KEY_D)){
 			player.moveRight();
 		}else{
 			player.movingRight = false;
 		}
 
 		if(System.currentTimeMillis()/100 - lastClickTime/100 > 4){
 			if(gc.getInput().isKeyDown(Input.KEY_1)){
 				map.placeCollectableBlock(player, Color.blue);
 				lastClickTime = System.currentTimeMillis();
 			}else if(gc.getInput().isKeyDown(Input.KEY_2)){
 				map.placeCollectableBlock(player, Color.red);
 				lastClickTime = System.currentTimeMillis();
 			}else if(gc.getInput().isKeyDown(Input.KEY_3)){
 				map.placeCollectableBlock(player , Color.yellow);
 				lastClickTime = System.currentTimeMillis();
 			}else if(gc.getInput().isKeyDown(Input.KEY_4)){
 				map.placeCollectableBlock(player, Color.pink);
 				lastClickTime = System.currentTimeMillis();
 			}else if(gc.getInput().isKeyDown(Input.KEY_5)){
 				map.placeCollectableBlock(player, Color.green);
 				lastClickTime = System.currentTimeMillis();
 			}
 		}
 
 
 		if(gc.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON)){
 			//shoot a bullet, doo
 			player.shoot(x, y);
 			mouseX = x;
 			mouseY = y;
 
 			//doesn't inclide the viewport shift
 			Rectangle mouseclick = new Rectangle(gc.getInput().getMouseX(), gc.getInput().getMouseY(), 1, 1);
 			if(mouseclick.intersects(player.redR)){
 				map.placeCollectableBlock(player, Color.red);
 			}else if(mouseclick.intersects(player.blueR)){
 				map.placeCollectableBlock(player, Color.blue);
 			}else if(mouseclick.intersects(player.yellowR)){
 				map.placeCollectableBlock(player , Color.yellow);
 			}else if(mouseclick.intersects(player.pinkR)){
 				map.placeCollectableBlock(player, Color.pink);
 			}else if(mouseclick.intersects(player.greenR)){
 				map.placeCollectableBlock(player, Color.green);
 			}
 
 			//includes the viewport shift
 			mouseclick = new Rectangle(gc.getInput().getMouseX() + VIEWPORT_X, gc.getInput().getMouseY()+ VIEWPORT_Y, 1, 1);
 			for(int i = 0; i < map.collectableBlocks.size(); i++){
 				CollectableBlock b = map.collectableBlocks.get(i);
 				Rectangle r = new Rectangle(b.getX() - 2, b.getY() - 2, b.width + 12, b.height + 12);
 				if(r.intersects(mouseclick)){
 					player.addBlock(b.getColor());
 					map.removeBlock(b);
 
 					System.out.println("got it");
 					map.collectableBlocks.remove(i);
 				}
 
 
 			}
 
 		}
 
 		//update game elements
 		player.update(gc, delta, map);
 
 		map.update(player);
 
 	}
 }
