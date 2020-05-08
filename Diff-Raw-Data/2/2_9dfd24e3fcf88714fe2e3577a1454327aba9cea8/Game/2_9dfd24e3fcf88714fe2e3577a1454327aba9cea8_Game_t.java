 package main;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 import main.interactables.Chest;
 import main.interactables.Door;
 import main.interactables.Interactable;
 import main.interactables.Portal;
 import main.interactables.SignPost;
 import main.interfaces.GameMenuInterface;
 import main.interfaces.PlayerInterface;
 import main.world.Map;
 import main.world.Square;
 import main.world.World;
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import util.InvalidEscapeSequenceException;
 import util.geom.Point;
 import util.gl.Font;
 import util.input.Input;
 import util.input.KeyEvent;
 import util.input.MouseEvent;
 import util.ui.GLButton;
 import util.ui.GLUIException;
 import util.ui.GLUITheme;
 
 /**
  * Game class holds the running of our game.
  * 
  */
 public class Game {
 
 	/** Instance of Player that the user will control */
 	public static Player player;
 
 	/** Instance of Interface that displays player data */
 	private static PlayerInterface gameInterface;
 
 	/** Instance of Map to hold the current map */
 	public static Map map;
 
 	/** ArrayList to hold Interactables in the game */
 	public static ArrayList<Interactable> interactables;
 
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
 
 	private Entity testEntity, questGiver;
 
 	private RightClickMenu rightClickMenu, entityMenu, interactableMenu, walkHereMenu, closeMenu;
 	private boolean rightMenu = false;
 
 	private Object focus;
 
 	private Font font;
 	
 	int fps;
 	long lastFPS, lastFrame;
 	
 	/** Constructor for the game */
 	public Game() {
 
 		//GAME_STATE = GAME_STATE_START;
 		GAME_STATE = GAME_STATE_PLAYING;
 		
 		try {
 			player = Player.loadPlayer("New Player.sav");
 			// Uncomment the following line after testing.
 			//GAME_STATE = GAME_STATE_PLAYING;
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
 
 		testEntity = new FriendlyNPC("Dave", 7, 12*Main.gridSize, 12*Main.gridSize, 0, 0);
 		questGiver = new QuestGiver("SHAMALA", 9, 15*Main.gridSize, 17*Main.gridSize, 0, 0, "001");
 
 		entities.add(player);
 		entities.add(testEntity);
 		entities.add(questGiver);
 		
 		try {
 			font = new GLUITheme().getFont();
 			createMenus();
 		} catch (GLUIException e) { e.printStackTrace(); }
 
 		while (GAME_STATE != GAME_STATE_QUIT) {
 
 			switch (GAME_STATE) {
 				case GAME_STATE_START:
 					
 					try { gameStateStart(); } 
 					catch (InvalidEscapeSequenceException e1) { }
 					
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
 	 * @throws InvalidEscapeSequenceException 
 	 * 
 	 */
 	private void gameStateStart() throws InvalidEscapeSequenceException {
 
 		boolean done = false;
 		
 		Display.sync(60);
 		
 		while (!done) {
 
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
 			
 			GL11.glClearColor(0, 0, 0, 1);
 			
 			drawBackground();
 			
 			font.glDrawText("\\c#FFFFFFPress ESCAPE to skip...", Main.SCREEN_WIDTH-200, Main.SCREEN_HEIGHT-50);
 			
 			Display.update();
 
 			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 				done = true;
 			} else if (Display.isCloseRequested()) {
 				done = true;
 				Game.GAME_STATE = Game.GAME_STATE_QUIT;
 			}
 		}
 		
 		fade();
 		
 		GAME_STATE = GAME_STATE_PLAYING;
 
 	}
 	
 	private void drawBackground() {
 		
 		int border = 80;
 		
 		GL11.glBegin(GL11.GL_QUADS);
 		
 		{
 			GL11.glColor3d(255, 255, 255);
 			GL11.glVertex2d(0, border);
 			GL11.glVertex2d(Main.SCREEN_WIDTH, border);
 			GL11.glVertex2d(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT-border);
 			GL11.glVertex2d(0, Main.SCREEN_HEIGHT-border);
 			
 		}
 		
 		GL11.glEnd();
 	}
 
 	private void fade() {
 		
 		float alpha = 1;
 		
 		while (alpha > 0) {
 			
 			render();
 			
 			GL11.glBegin(GL11.GL_QUADS);
 			{
 				GL11.glColor4d(0, 0, 0, alpha);
 				GL11.glVertex2d(0, 0);
 				GL11.glVertex2d(Main.SCREEN_WIDTH, 0);
 				GL11.glVertex2d(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
 				GL11.glVertex2d(0, Main.SCREEN_HEIGHT);
 			}
 			GL11.glEnd();
 			
 			alpha-=0.0005;
 			
 			Display.update();
 			
 			if (Display.isCloseRequested()) {
 				alpha = 0;
 				GAME_STATE = GAME_STATE_QUIT;
 			}
 		}
 	}
 	
 	/**
 	 * The playing state of the game corresponding to GAME_STATE_PLAYING.
 	 * 
 	 */
 	private void gameStatePlaying() throws ConcurrentModificationException {
 		
 		while (GAME_STATE == GAME_STATE_PLAYING && !Display.isCloseRequested()) {
 
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 
 			render();
 
 			Input.poll();
 
 			List<KeyEvent> keyEvents = Input.getKeyEvents();
 			List<MouseEvent> mouseEvents = Input.getMouseEvents();
 
 			if (rightMenu) {
 				rightClickMenu.renderGL();
 				rightClickMenu.update(0);
 				rightClickMenu.processMouseEvents(mouseEvents);
 			}
 
 			for (KeyEvent e : keyEvents) {
 
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
 
 			for (MouseEvent e : mouseEvents) {
 
 				if (e.getEvent() == MouseEvent.BUTTON_PRESSED) {
 					if (Input.isMouseButtonDown(Input.RIGHT_MOUSE) 
 							&& e.getEventX() < Main.SCREEN_PLAYABLE_WIDTH 
 							&& e.getEventY() < Main.SCREEN_PLAYABLE_HEIGHT) {
 
 						rightMenu = true;
 						focus = getFocusObject(e);
 
 						if (focus instanceof Entity) {
 							rightClickMenu = entityMenu;
 						} else if (focus instanceof Interactable){
 							rightClickMenu = interactableMenu;
 						} else if (focus instanceof Square) {
 							rightClickMenu = walkHereMenu;
 						} else {
 							rightClickMenu = closeMenu;
 						}
 
 						rightClickMenu.setContext(focus);
 						rightClickMenu.setCoordinates(e);
 
					} else {
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
 
 	public static void render() {
 		
 		World.draw();
 
 		gameInterface.updateInterface();
 
 		drawInteractables();
 
 		drawEntities();
 
 		//drawGrid();
 		
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
 	@SuppressWarnings("unused")
 	private static void drawGrid() {
 
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
 
 		Scanner s = new Scanner(ResourceManager.getResourceAsStream("../main/interactables/interactables"));
 
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
 			} else if (strs[0].equals("door")) {
 				
 				Interactable i = new Door(Integer.parseInt(strs[1]), 
 						Integer.parseInt(strs[2]), 
 						Integer.parseInt(strs[3]), 
 						Integer.parseInt(strs[4]),
 						Boolean.parseBoolean(strs[5]));
 				
 				interactables.add(i);
 			}
 
 		}
 
 		s.close();
 
 	}
 
 	private static void drawInteractables() {
 
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
 
 	private static void drawEntities() {
 
 		for (Entity e : entities) {
 			if (World.currentMap().getSceneX() == e.getSceneX() && 
 					World.currentMap().getSceneY() == e.getSceneY())
 				e.draw();
 		}
 	}
 
 	private void interactEntities() {
 
 		for (Entity e : entities) {
 			
 			if (!(e instanceof Player)) {
 				
 				if (player.nextToEntity(e)) {
 					e.interact();
 				}
 			}
 		}
 	}
 
 	private void createMenus() throws GLUIException {
 
 		entityMenu = new RightClickMenu();
 		interactableMenu = new RightClickMenu();
 		walkHereMenu = new RightClickMenu();
 		closeMenu = new RightClickMenu();
 
 		GLButton talkButton = new GLButton("Talk", new GLUITheme()) {
 
 			@Override
 			protected void onClick() {
 
 				((FriendlyNPC)RightClickMenu.getContext()).interact();
 			}
 		};
 		talkButton.setLabelAlignment(GLButton.ALIGN_LEFT);
 
 		GLButton closeButton = new GLButton("Close", new GLUITheme()) {
 
 			@Override
 			protected void onClick() {
 
 				rightMenu = false;
 			}
 		};
 		closeButton.setLabelAlignment(GLButton.ALIGN_LEFT);
 
 		entityMenu.addButton(talkButton);
 		entityMenu.addButton(closeButton);
 		
 		GLButton interactButton = new GLButton("Interact", new GLUITheme()) {
 
 			@Override
 			protected void onClick() {
 
 				((Interactable)RightClickMenu.getContext()).activate();
 			}
 		};
 		interactButton.setLabelAlignment(GLButton.ALIGN_LEFT);
 
 		interactableMenu.addButton(interactButton);
 		interactableMenu.addButton(closeButton);
 
 		GLButton walkHere = new GLButton("Walk here", new GLUITheme()) {
 			
 			@Override
 			protected void onClick() {
 			
 				// TODO: Add walk here functionality.
 				
 			}
 		};
 		walkHere.setLabelAlignment(GLButton.ALIGN_LEFT);
 		
 		walkHereMenu.addButton(walkHere);
 		walkHereMenu.addButton(closeButton);
 		
 		closeMenu.addButton(closeButton);
 
 	}
 
 	private Object getFocusObject(MouseEvent event) {
 
 		/*
 		 * We need to search through entities, interactables,
 		 * items and finally the world to see what exists
 		 * at the event point.
 		 */
 
 		Point p = event.getEventPoint();
 
 		for (Entity e : entities) {
 			
 			if (p.getX()-e.getX() <= e.getW() && p.getX()-e.getX() > 0 
 					&& p.getY()-e.getY() <= e.getH() && p.getY()-e.getY() > 0
 					&& e.getSceneX() == World.currentMap().getSceneX() && e.getSceneY() == World.currentMap().getSceneY()
 					&& !(e instanceof Player)) {
 				
 				return e;
 			}
 		}
 
 		for (Interactable e : interactables) {
 			// We need to deal with special case of a portal
 			if (e instanceof Portal) {
 				
 				Portal t = (Portal)e;
 				if (p.getX()-t.getX0() <= Main.gridSize && p.getX()-t.getX0() > 0
 						&& p.getY()-t.getY0() <= Main.gridSize && p.getY()-t.getY0()>0) {
 					if (t.getSceneX0() == World.currentMap().getSceneX() && t.getSceneY0() == World.currentMap().getSceneY()) {
 						return e;
 					}
 				} else if (p.getX()-t.getX1() <= Main.gridSize && p.getX()-t.getX1() > 0
 						&& p.getY()-t.getY1() <= Main.gridSize && p.getY()-t.getY1()>0) {
 					if (t.getSceneX1() == World.currentMap().getSceneX() && t.getSceneY1() == World.currentMap().getSceneY()) {
 						return e;
 					}
 				}
 			}
 			else if (p.getX()-e.getX() <= Main.gridSize && p.getX()-e.getX() > 0 
 					&& p.getY()-e.getY() <= Main.gridSize && p.getY()-e.getY() > 0) {
 				
 				if (e.getSceneX() == World.currentMap().getSceneX() && e.getSceneY() == World.currentMap().getSceneY()) {
 					return e;
 				}
 			}
 		}
 
 		Square s = World.currentMap().getSquare(p);
 		if (!s.isSolid()) {
 			return s;
 		}
 
 		return null;
 
 	}
 	
 	public long getTime() {
 		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
 	}
 	
 	public int getDelta() {
 		long time = getTime();
 		int delta = (int)(time-lastFrame);
 		lastFrame = time;
 		return delta;
 	}
 
 }
