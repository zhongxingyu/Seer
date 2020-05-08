 package main;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 import java.util.Scanner;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import util.input.Input;
 import util.input.KeyEvent;
 import util.input.MouseEvent;
 
 /**
  * Game class holds the running of our game.
  * 
  */
 public class Game {
 	
 	/** Instance of Player that the user will control */
 	public static Player player;
 	
 	/** Instance of Interface that displays player data */
 	private PlayerInterface gameInterface;
 	
 	/** Instance of Map to hold the current map */
 	public static Map map;
 
 	/** ArrayList to hold Interactables in the game */
 	private static ArrayList<Interactable> interactables;
 	
 	/** The current state the game is in */
 	public static int GAME_STATE;
 	
 	/** The initial state when the game is run */
 	public static final int GAME_STATE_START = 0;
 	
 	/** The state whilst the game is being played */
 	public static final int GAME_STATE_PLAYING = 1;
 	
 	/** The state when the player has died */
 	public static final int GAME_STATE_DEAD = 2;
 	
 	/** The state when quit has been requested */
 	public static final int GAME_STATE_QUIT = 3;
 	
 	/** The interface for the in game menu */
 	private GameMenuInterface gameMenu;
 	
 	public static ArrayList<Entity> entities;
 	
 	private FriendlyNPC testEntity;
 	
 	private RightClickMenu rightClickMenu;
 	private boolean rightMenu = false;
 	
 	/** Constructor for the game */
 	public Game() {
 
 		GAME_STATE = GAME_STATE_START;
 		
 		try {
 			player = Player.loadPlayer("New Player.sav");
 		} catch (FileNotFoundException e) {
 			player = new Player("New Player");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * This method when called runs the game and houses the 
 	 * main loop that the game runs out of. Responsible for 
 	 * dealing with game state changes.
 	 * 
 	 */
 	public void begin() {
 		
 		Keyboard.enableRepeatEvents(true);
 		
 		new World();
 		
 		World.setCurrentMap(player.getSceneX(), player.getSceneY());
 		
 		gameInterface = new PlayerInterface();
 		
 		gameMenu = new GameMenuInterface();
 		
 		setupInteractables();
 		
 		entities = new ArrayList<Entity>();
 		
 		testEntity = new FriendlyNPC("Dave", 12*Main.gridSize, 12*Main.gridSize, 0, 0);
 		
 		entities.add(player);
 		entities.add(testEntity);
 		
 		rightClickMenu = new RightClickMenu();
 		
 		while (GAME_STATE != GAME_STATE_QUIT) {
 
 			switch (GAME_STATE) {
 				case GAME_STATE_START:
 					gameStateStart();
 					break;
 				case GAME_STATE_PLAYING:
 					
 					try { gameStatePlaying(); } 
 					catch (ConcurrentModificationException e) { }
 					
 					break;
 				case GAME_STATE_DEAD:
 					gameStateDead();
 					break;
 				default:
 					GAME_STATE = GAME_STATE_QUIT;
 					break;
 			}
 		}
 		
 	}
 
 	/**
 	 * TODO: Opening story goes here, tutorial section etc.
 	 * 
 	 * Beginning section goes here, everything that isn't required
 	 * later on.
 	 * 
 	 */
 	private void gameStateStart() {
 		
 		// Code here
 		
 		GAME_STATE = GAME_STATE_PLAYING;
 		
 	}
 	
 	/**
 	 * The playing state of the game corresponding to GAME_STATE_PLAYING.
 	 * 
 	 */
 	private void gameStatePlaying() throws ConcurrentModificationException {
 		
 		while (GAME_STATE == GAME_STATE_PLAYING && !Display.isCloseRequested()) {
 			
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 			
 			World.draw();
 			
 			gameInterface.updateInterface();
 			
 			drawInteractables();
 			
 			drawEntities();
 			
 			drawGrid();
 			
			if (rightMenu) rightClickMenu.drawMenu();
			
 			Input.poll();
 			
			for (KeyEvent e : Input.getKeyEvents()) {
 			
 				if (e.getEvent() == KeyEvent.KEY_PRESSED) {
 					
 					player.input(e);
 					
 				}
 				else if (e.getEvent() == KeyEvent.KEY_RELEASED && !e.isRepeatEvent()) {
 					
 					if (e.getKeyCode() == Main.KeyBindings.KEY_INTERACT.getUserKey()) {
 						checkInteractables();
 					}
 					else if (e.getKeyCode() == Main.KeyBindings.KEY_INVENTORY.getUserKey()) {
 						player.getInventory().showInventory();
 					}
 					else if (e.getKeyCode() == Main.KeyBindings.KEY_MENU.getUserKey()) {
 						gameMenu.showInterface();
 					}
 					else if (e.getKeyCode() == Main.KeyBindings.KEY_QUESTLOG.getUserKey()) {
 						player.getQuestLog().showInterface();
 					}
 					else if (e.getKeyCode() == Main.KeyBindings.KEY_TALK.getUserKey()) {
 						interactEntities();
 					}
 				}
 				
 			}
 			
			for (MouseEvent e : Input.getMouseEvents()) {
 				
 				if (e.getEvent() == MouseEvent.BUTTON_PRESSED) {
 					if (Input.isMouseButtonDown(Input.RIGHT_MOUSE)) {
 						
 						rightMenu = true;
 						rightClickMenu.setCoordinates(e);
 						
 					} else if (Input.isMouseButtonDown(Input.LEFT_MOUSE)
 							&& !e.getEventPoint().inside(rightClickMenu.getBounds())) {
 						
 						rightMenu = false;
 					}
 				}
 			}
 			
 			Display.update();
 			
 			if (Display.isCloseRequested()) {
 				
 				try {
 					player.savePlayer();
 				} catch (IOException e) {
 					System.err.println("Could not save player data");
 					e.printStackTrace();
 				}
 				
 				GAME_STATE = GAME_STATE_QUIT;
 			}
 		}
 	}
 
 	/**
 	 * TODO: Implement death screen / game over<p>
 	 * 
 	 * Death screen state of the game, once the player's health <= 0.
 	 * Corresponds to GAME_STATE_DEAD.
 	 * 
 	 */
 	private void gameStateDead() {
 		
 		/*
 		 * Game Over
 		 * Write player stats to the screen
 		 * Offer to play again or maybe just take straight to the menu
 		 * 
 		 */
 		
 		GAME_STATE = GAME_STATE_QUIT;
 	}
 	
 	/**
 	 * Draws a grid to the screen for development purposes and so
 	 * should not be included in final versions.<p>
 	 * 
 	 * The grid size is specified by the global variable gridSize.
 	 * Entity movement is based on this grid size.
 	 * 
 	 */
 	private void drawGrid() {
 
 		GL11.glColor4f(0f, 0f, 0f, 0.1f);
 
 		GL11.glBegin(GL11.GL_LINES);
 		// Draw vertical lines
 		for (int i = 0; i <= Main.SCREEN_PLAYABLE_WIDTH/Main.gridSize; i++) {
 
 			GL11.glVertex2f(i*Main.gridSize, 0);
 			GL11.glVertex2f(i*Main.gridSize, Main.SCREEN_PLAYABLE_HEIGHT);
 		}
 		// Draw horizontal lines
 		for (int i = 0; i <= Main.SCREEN_PLAYABLE_HEIGHT/Main.gridSize; i++) {
 
 			GL11.glVertex2f(0, i*Main.gridSize);
 			GL11.glVertex2f(Main.SCREEN_PLAYABLE_WIDTH, i*Main.gridSize);
 		}
 		GL11.glEnd();
 
 	}
 	
 	private void setupInteractables() {
 		
 		interactables = new ArrayList<Interactable>();
 		
 		Scanner s = new Scanner(ResourceManager.getResourceAsStream("../res/map/interactables"));
 		
 		while (s.hasNext()) {
 			
 			String[] strs = s.nextLine().split(", ");
 			if (strs[0].equals("portal")) {
 				Interactable i = new Portal(Integer.parseInt(strs[1]), 
 						Integer.parseInt(strs[2]), 
 						Integer.parseInt(strs[3]), 
 						Integer.parseInt(strs[4]), 
 						Integer.parseInt(strs[5]), 
 						Integer.parseInt(strs[6]), 
 						Integer.parseInt(strs[7]), 
 						Integer.parseInt(strs[8]));
 				
 				interactables.add(i);
 			} else if (strs[0].equals("signpost")) {
 				Interactable i = new SignPost(strs[1], 
 						Integer.parseInt(strs[2]), 
 						Integer.parseInt(strs[3]), 
 						Integer.parseInt(strs[4]), 
 						Integer.parseInt(strs[5]));
 				
 				interactables.add(i);
 			} else if (strs[0].equals("chest")) {
 				
 				LinkedList<Item> items = new LinkedList<Item>();
 				
 				if (strs.length > 5) {
 					for (int i = 5; i < strs.length; i++) {
 						items.add(Item.getItem(strs[i]));
 					}
 				}
 				
 				Interactable i = new Chest(Integer.parseInt(strs[1]), 
 						Integer.parseInt(strs[2]), 
 						Integer.parseInt(strs[3]), 
 						Integer.parseInt(strs[4]), 
 						items);
 				
 				interactables.add(i);
 			}
 			
 		}
 		
 		s.close();
 		
 	}
 	
 	private void drawInteractables() {
 		
 		for (Interactable i : interactables) {
 			
 			i.draw();
 		}
 	}
 	
 	private void checkInteractables() {
 		
 		for (Interactable i : interactables) {
 			
 			if (i.checkPlayer()) {
 
 				i.activate();
 			}
 		}
 	}
 	
 	private void drawEntities() {
 		
 		for (Entity e : entities) {
 			if (World.currentMap().getSceneX() == e.getSceneX() && 
 					World.currentMap().getSceneY() == e.getSceneY())
 				e.draw();
 		}
 	}
 	
 	private void interactEntities() {
 		
 		// TODO: Implement how we talk to entities
 		
 		testEntity.interact();
 	}
 	
 }
