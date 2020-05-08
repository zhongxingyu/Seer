 package states;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import entities.Camera;
 import entities.Hat;
 import entities.Player;
 import entities.Tank;
 import entities.World;
 import game.GunsAndHats;
 import game.ResourceManager;
 
 public class PreGameMenuNew extends BasicGameState{
 	
 	private int id;
 	private Camera camera;
 	
 	private static int tankTypes = 3; //1-3
 	private static int worldTypes = 4; //0-3
 	
 	private boolean tankError;
 	private Vector2f tankErrorPos = new Vector2f(500, 400);
 	private Vector2f tankErrorPos_Rel;
 	private static String tankError1 = "Some Players have";
 	private static String tankError2 = "no tanks selected";
 	
 	//Image variables
 	Image background;
 	Image worldThumb;
 	Image border_tanks;
 	Image border_world;
 	Image startGame;
 	Image player1_background;
 	Image player2_background;
 	Image player3_background;
 	Image player4_background;
 	Image player3_plus;
 	Image player4_plus;
 	
 	
 	//static variable for images
 	private static Vector2f pB_pos = new Vector2f(20, 20);
 	private static int pB_delta_x = 180;
 	private static Vector2f world_pos = new Vector2f(36, 300);
 	private static Vector2f startGame_pos = new Vector2f(500,450);
 	
 	private Vector2f pB_pos_Rel;
 	private float pB_delta_x_Rel;
 	private float tankOffsetX;
 	private float tankDeltaY;
 	private Vector2f world_pos_Rel;
 	private Vector2f startGame_pos_Rel;
 
 	
 	//World creation variable
 	private int worldID;
 	private Player[] players;
 	private Tank[] player1_tanks;
 	private Tank[] player2_tanks;
 	private Tank[] player3_tanks;
 	private Tank[] player4_tanks;
 	
 	public PreGameMenuNew (int id, Camera cam) {
 		this.id = id;
 		this.camera = cam;
 		worldID = 0;
 		players = new Player[4];
 		player1_tanks = new Tank[4];
 		player2_tanks = new Tank[4];
 		player3_tanks = new Tank[4];
 		player4_tanks = new Tank[4];
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		Image sprites = ResourceManager.getInstance().getImage("SPRITES_GUI");
 		
 		//background;
 		worldThumb = ResourceManager.getInstance().getImage("WORLD_"+worldID+"_THUMB");
 		border_tanks = ResourceManager.getInstance().getImage("LEFT_SMALL");
 		border_world = ResourceManager.getInstance().getImage("WORLD_0_THUMB");
 		startGame = ResourceManager.getInstance().getImage("STARTGAMEBUTTON");
 		player1_background = sprites.getSubImage(252, 0, 126, 40);
 		player2_background = sprites.getSubImage(379, 0, 126, 40);
 		player3_background = sprites.getSubImage(506, 0, 126, 40);
 		player4_background = sprites.getSubImage(633, 0, 126, 40);
 		player3_plus = sprites.getSubImage(506, 0, 126, 40);
 		player4_plus = sprites.getSubImage(633, 0, 126, 40);
 		
 		pB_pos_Rel = camera.getRelFocusPos(pB_pos).add(camera.getOffset());
 		pB_delta_x_Rel = pB_delta_x * camera.getScale();
 		tankOffsetX = player1_background.getWidth()*camera.getScale()/2 - 16*camera.getScale(); //16 is the tankWidth/2
 		tankDeltaY = 48 * camera.getScale(); //tankHeight * 1.5 = 48;
 		
 		world_pos_Rel = camera.getRelPos(world_pos).add(camera.getOffset());
 		startGame_pos_Rel = camera.getRelPos(startGame_pos).add(camera.getOffset());
 		tankErrorPos_Rel = camera.getRelPos(tankErrorPos).add(camera.getOffset());
 		
 		players[0] = new Player("Player 1", player1_tanks);
 		players[1] = new Player("Player 2", player2_tanks);
 		
 		tankError = false;
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics gr)
 			throws SlickException {
 		
 		//Player 1
 		player1_background.draw(pB_pos_Rel.x, pB_pos_Rel.y, camera.getScale());
 		for(int i = 0; i < player1_tanks.length; i++) {
 			if(player1_tanks[i] != null) {
 				player1_tanks[i].getImage().draw(pB_pos_Rel.x + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 				border_tanks.draw(pB_pos_Rel.x + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 			} else {
 				border_tanks.draw(pB_pos_Rel.x + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 			}
 		}
 		
 		//Player 2
 		player2_background.draw(pB_pos_Rel.x + pB_delta_x_Rel, pB_pos_Rel.y, camera.getScale());
 		for(int i = 0; i < player2_tanks.length; i++) {
 			if(player2_tanks[i] != null) {
 				player2_tanks[i].getImage().draw(pB_pos_Rel.x + pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 				border_tanks.draw(pB_pos_Rel.x + pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 			} else {
 				border_tanks.draw(pB_pos_Rel.x + pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 			}
 		}
 		
 		//Player 3
 		if(players[2] != null) {
 			player3_background.draw(pB_pos_Rel.x + 2*pB_delta_x_Rel, pB_pos_Rel.y, camera.getScale());
 			for(int i = 0; i < player3_tanks.length; i++) {
 				if(player3_tanks[i] != null) {
 					player3_tanks[i].getImage().draw(pB_pos_Rel.x + 2*pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 					border_tanks.draw(pB_pos_Rel.x + 2*pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 				} else {
 					border_tanks.draw(pB_pos_Rel.x + 2*pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 				}
 			}
 		} else {
 			player3_plus.draw(pB_pos_Rel.x + 2*pB_delta_x_Rel, pB_pos_Rel.y, camera.getScale());
 		}
 		
 		//Player 3
 		if(players[3] != null) {
 			player4_background.draw(pB_pos_Rel.x + 3*pB_delta_x_Rel, pB_pos_Rel.y, camera.getScale());
 			for(int i = 0; i < player4_tanks.length; i++) {
 				if(player4_tanks[i] != null) {
 					player4_tanks[i].getImage().draw(pB_pos_Rel.x + 3*pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 					border_tanks.draw(pB_pos_Rel.x + 3*pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 				} else {
 					border_tanks.draw(pB_pos_Rel.x + 3*pB_delta_x_Rel + tankOffsetX, pB_pos_Rel.y + (i+1)*tankDeltaY, camera.getScale());
 				}
 			}
 		}else {
 			player4_plus.draw(pB_pos_Rel.x + 3*pB_delta_x_Rel, pB_pos_Rel.y, camera.getScale());
 		}
 		
 		//World
 		worldThumb.draw(world_pos_Rel.x, world_pos_Rel.y, camera.getScale());
 		//border_world.draw(world_pos_Rel.x, world_pos_Rel.y, camera.getScale());
 		
 		//StartGameButton
 		startGame.draw(startGame_pos_Rel.x, startGame_pos_Rel.y, camera.getScale());
 		
 		//Draw Error
 		if(tankError) {
 			gr.drawString(tankError1, tankErrorPos_Rel.x, tankErrorPos_Rel.y);
 			gr.drawString(tankError2, tankErrorPos_Rel.x, tankErrorPos_Rel.y + 20*camera.getScale());
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 		Input in = gc.getInput();
 		int mx = in.getMouseX();
 		int my = in.getMouseY();
 		
 		//Check for Position and Click
 		//Player 1
 		for(int i = 0; i < player1_tanks.length; i++) {
 			if(pB_pos_Rel.x + tankOffsetX < mx && mx < pB_pos_Rel.x + tankOffsetX + 32*camera.getScale()
 					&& pB_pos_Rel.y + (i+1)*tankDeltaY < my && my < pB_pos_Rel.y + (i+1)*tankDeltaY + 32*camera.getScale()) {
 				if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 					int playerId = 1;
 					if(player1_tanks[i] == null) {
 						player1_tanks[i] = new Tank(1,0,0,playerId);
 					} else {
 						Tank t = player1_tanks[i];
 						if(t.type == tankTypes) {
 							player1_tanks[i] = null;
 						} else {
 							player1_tanks[i] = new Tank(t.type+1,0,0,playerId);
 						}
 					}
 				}
 			}
 		}
 		
 		//Player 2
 		for(int i = 0; i < player1_tanks.length; i++) {
 			if(pB_pos_Rel.x + pB_delta_x_Rel + tankOffsetX < mx && mx < pB_pos_Rel.x + pB_delta_x_Rel + tankOffsetX + 32*camera.getScale()
 					&& pB_pos_Rel.y + (i+1)*tankDeltaY < my && my < pB_pos_Rel.y + (i+1)*tankDeltaY + 32*camera.getScale()) {
 				if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 					int playerId = 2;
 					if(player2_tanks[i] == null) {
 						player2_tanks[i] = new Tank(1,0,0,playerId);
 					} else {
 						Tank t = player2_tanks[i];
 						if(t.type == tankTypes) {
 							player2_tanks[i] = null;
 						} else {
 							player2_tanks[i] = new Tank(t.type+1,0,0,playerId);
 						}
 					}
 				}
 			}
 		}
 
 		
 		//Player 3
 		if(players[2] != null) {
 			//Removing Player
 			if( pB_pos_Rel.x + 2*pB_delta_x_Rel < mx && mx < pB_pos_Rel.x + 2*pB_delta_x_Rel + player3_background.getWidth()*camera.getScale()
 					&& pB_pos_Rel.y < my && my < pB_pos_Rel.y + player3_background.getHeight()*camera.getScale()) {
 				if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 					players[2] = null;
 				}
 			}
 			
 			for(int i = 0; i < player1_tanks.length; i++) {
 				if(pB_pos_Rel.x + 2*pB_delta_x_Rel + tankOffsetX < mx && mx < pB_pos_Rel.x + 2*pB_delta_x_Rel + tankOffsetX + 32*camera.getScale()
 						&& pB_pos_Rel.y + (i+1)*tankDeltaY < my && my < pB_pos_Rel.y + (i+1)*tankDeltaY + 32*camera.getScale()) {
 					if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 						int playerId = 3;
 						if(player3_tanks[i] == null) {
 							player3_tanks[i] = new Tank(1,0,0,playerId);
 						} else {
 							Tank t = player3_tanks[i];
 							if(t.type == tankTypes) {
 								player3_tanks[i] = null;
 							} else {
 								player3_tanks[i] = new Tank(t.type+1,0,0,playerId);
 							}
 						}
 					}
 				}
 			}
 		} else {
 			if( pB_pos_Rel.x + 2*pB_delta_x_Rel < mx && mx < pB_pos_Rel.x + 2*pB_delta_x_Rel + player3_background.getWidth()*camera.getScale()
 					&& pB_pos_Rel.y < my && my < pB_pos_Rel.y + player3_background.getHeight()*camera.getScale()) {
 				if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 					players[2] = new Player("Player 3", player3_tanks);
 				}
 			}
 		}
 		
 		//Player 4
 		if(players[3] != null) {
 			//Removing Player
 			if( pB_pos_Rel.x + 3*pB_delta_x_Rel < mx && mx < pB_pos_Rel.x + 3*pB_delta_x_Rel + player3_background.getWidth()*camera.getScale()
 					&& pB_pos_Rel.y < my && my < pB_pos_Rel.y + player3_background.getHeight()*camera.getScale()) {
 				if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 					players[3] = null;
 				}
 			}
 			
 			for(int i = 0; i < player1_tanks.length; i++) {
 				if(pB_pos_Rel.x + 3*pB_delta_x_Rel + tankOffsetX < mx && mx < pB_pos_Rel.x + 3*pB_delta_x_Rel + tankOffsetX + 32*camera.getScale()
 						&& pB_pos_Rel.y + (i+1)*tankDeltaY < my && my < pB_pos_Rel.y + (i+1)*tankDeltaY + 32*camera.getScale()) {
 					if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 						int playerId = 4;
 						if(player4_tanks[i] == null) {
 							player4_tanks[i] = new Tank(1,0,0,playerId);
 						} else {
 							Tank t = player4_tanks[i];
 							if(t.type == tankTypes) {
 								player4_tanks[i] = null;
 							} else {
 								player4_tanks[i] = new Tank(t.type+1,0,0,playerId);
 							}
 						}
 					}
 				}
 			}
 		} else {
 			//Adding Player
 			if( pB_pos_Rel.x + 3*pB_delta_x_Rel < mx && mx < pB_pos_Rel.x + 3*pB_delta_x_Rel + player3_background.getWidth()*camera.getScale()
 					&& pB_pos_Rel.y < my && my < pB_pos_Rel.y + player3_background.getHeight()*camera.getScale()) {
 				if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 					players[3] = new Player("Player 4", player4_tanks);
 				}
 			}
 		}
 	
 		//World
 		if( world_pos_Rel.x < mx && mx < world_pos_Rel.x + border_world.getWidth()*camera.getScale()
 				&& world_pos_Rel.y < my && my < world_pos_Rel.y + border_world.getHeight()*camera.getScale()) {
 			if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 				if(worldID == worldTypes-1) {
 					worldID = 0;
 				} else {
 					worldID++;
 				}
 				worldThumb = ResourceManager.getInstance().getImage("WORLD_"+worldID+"_THUMB");
 			}
 		}
 		
 		//Start Game
 		if( startGame_pos_Rel.x < mx && mx < startGame_pos_Rel.x + startGame.getWidth()*camera.getScale()
 				&& startGame_pos_Rel.y < my && my < startGame_pos_Rel.y + startGame.getHeight()*camera.getScale()) {
 			if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 				startGame(gc, sb);
 			}
 		}
 	}
 	
 	public void startGame(GameContainer gc, StateBasedGame sb) {
 		tankError = false;
 		int playerCount = 2;
 		//Check Player 1
 		players[0] = makePlayer(players[0], player1_tanks);
 		//Check Player 2
 		players[1] = makePlayer(players[1], player2_tanks);
 		//Check Player 3
 		if(players[2] != null) {
 			players[2] = makePlayer(players[2], player3_tanks);
 			playerCount++;
 		}
 		//Check Player 4
 		if(players[3] != null) {
 			players[3] = makePlayer(players[3], player4_tanks);
 			playerCount++;
 		}
 		
 		Player[] newPlayers = new Player[playerCount];
 		newPlayers[0] = players[0];
 		newPlayers[1] = players[1];
 		if(playerCount == 3) {
 			if(players[2] != null) newPlayers[2] = players[2];
 			else newPlayers[2] = players[3];
 		}
 		if(playerCount == 4) {
 			newPlayers[2] = players[2];
 			newPlayers[3] = players[3];
 		}
 		
 		if(!tankError) {
 			World world = new World(worldID);
 			
 			ArrayList<Hat> hats = new ArrayList<Hat>();
 			hats = dispenseHats(playerCount);
 			
 			GameState gs = (GameState) sb.getState(GunsAndHats.GAMESTATE);
 			gs.startGame(world, newPlayers, hats, gc);
 			sb.enterState(GunsAndHats.GAMESTATE);
 		}
 	}
 	
 	private ArrayList<Hat> dispenseHats(int amount) {
 		ArrayList<Hat> hats = new ArrayList<Hat>();
 		Random rand = new Random();
 		int worldWidth = ResourceManager.getInstance().getImage("WORLD_" + worldID + "_LEVEL").getWidth();
 		
 		int numberOfHats = amount; 
 		
 		for (int i = 0; i < numberOfHats; i++){
 			int hatX = rand.nextInt(worldWidth - 40) + 20;
 			hats.add(new Hat(rand.nextInt(4)+1, new Vector2f(hatX,-900), 1)); // Has to be large & negative Y value.. don't ask!
 		}
 		
 		return hats;
 	}
 	
 	public Player makePlayer(Player p, Tank[] ts) {
 		int tanks = 0;
 		for(int i = 0; i<ts.length; i++) {
 			if(ts[i] != null) tanks++;
 		}
 		if(tanks > 0) {
 			Tank[] newTs = new Tank[tanks];
 			int k = 0;
 			for(int i = 0; i < ts.length; i++) {
 				if(ts[i] != null) {
 					newTs[k] = ts[i];
 					//SET RANDOM POSITIONS HERE
 					Random rand = new Random();	
 					int worldWidth = ResourceManager.getInstance().getImage("WORLD_" + worldID + "_LEVEL").getWidth();
 					newTs[k].setPosition(new Vector2f(rand.nextInt(worldWidth - 40) + 20, 200));
 					
 					k++;
 				}
 			}
 			return new Player(p.getPlayerName(), newTs);
 		} else {
 			tankError = true;
			return p;
 		}
 	}
 
 	@Override
 	public int getID() {
 		return id;
 	}
 
 }
