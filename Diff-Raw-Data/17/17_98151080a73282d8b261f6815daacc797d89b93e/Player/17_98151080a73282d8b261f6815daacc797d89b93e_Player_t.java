 package main;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 
 /**
  * Subclass of Entity. 
  * 
  */
 public class Player extends Entity {
 
 	/** The player's experience */
 	private Experience exp;
 
 	/** The player's inventory */
 	private Inventory inventory;
 
 	/** The player's quest log */
 	private QuestLog questLog;
 
 	/**
 	 * Basic constructor to be called when a new character is
 	 * to be created.
 	 * 
 	 * @param x x coordinate for the entity
 	 * @param y y coordinate for the entity
 	 * @param w width of the entity
 	 * @param h height of the entity
 	 */
 	public Player(String name) {
 
 		super();
 
 		exp = new Experience();
 
 		inventory = new Inventory();
 
 		questLog = new QuestLog();
 
 	}
 
 	/**
 	 * 
 	 * Constructor for our finished product.<p>
 	 * 
 	 * This will create our player with all of his attributes
 	 * and sets him up for beginning the game.
 	 * 
 	 * @param name The player's name
 	 * @param HP The player's current hit points
 	 * @param maxHP The player's maximum hit points
 	 * @param PP The player's current power points
 	 * @param maxPP The player's maximum power points
 	 * @param experience The player's current experience
 	 * @param sceneX The player's current sceneX coordinate
 	 * @param sceneY The player's current sceneY coordinate
 	 * @param x The player's current x coordinate
 	 * @param y The player's current y coordinate
 	 */
 	public Player(String name, int HP, int maxHP, int PP, int maxPP, int experience, int sceneX, int sceneY, int x, int y) {
 
 		super(name, HP, maxHP, PP, maxPP, sceneX, sceneY, x, y);
 
 		exp = new Experience();
 		exp.increaseExperience(experience);
 		
 		inventory = new Inventory();
 
 		questLog = new QuestLog();
 		
 	}
 
 	/**
 	 * Method to move entity from user interaction.
 	 * 
 	 */
 	public void input() {
 
 		boolean leftPressed = Keyboard.isKeyDown(Main.KEY_LEFT);
 		boolean rightPressed = Keyboard.isKeyDown(Main.KEY_RIGHT);
 		boolean upPressed = Keyboard.isKeyDown(Main.KEY_UP);
 		boolean downPressed = Keyboard.isKeyDown(Main.KEY_DOWN);
 
 		if ((leftPressed) && (!rightPressed)) {
 			tryToMove(Main.KEY_LEFT);
 		} else if ((rightPressed) && (!leftPressed)) {
 			tryToMove(Main.KEY_RIGHT);
 		} else if ((upPressed) && (!downPressed)) {
 			tryToMove(Main.KEY_UP);
 		} else if ((downPressed) && (!upPressed)) {
 			tryToMove(Main.KEY_DOWN);
 		}
 	}
 
 	/**
 	 * Here we test if we are within the bounds of the screen. If yes,
 	 * we call the super method to move. If not, we return control.<p>
 	 * 
 	 */
 	@Override
 	public void move(int keycode) {
 
 		if (keycode == Main.KEY_RIGHT) {
 			setRight(true);
 		} else if (keycode == Main.KEY_LEFT) {
 			setRight(false);
 		}
 
 		checkBorders(keycode);
 
 		if (checkCollisions(keycode))
 			super.move(keycode);
 
 	}
 
 	/**
 	 * Check to see if we are moving past one of the borders.<br>
 	 * If we are at one of the borders and we try and move, change the scene.
 	 * 
 	 * @param keycode Integer corresponding to the Keyboard class key event
 	 */
 	private void checkBorders(int keycode) {
 
 		if (getX() < 1 && keycode == Main.KEY_LEFT) {
 
 			setScene(getSceneX()-1, getSceneY());
 			World.setCurrentMap(getSceneX(), getSceneY());
 			setCoordinates(Main.SCREEN_PLAYABLE_WIDTH-Main.gridSize, getY());
 
 		} else if (getX() >= Main.SCREEN_PLAYABLE_WIDTH-getW()-5 && keycode == Main.KEY_RIGHT) {
 
 			setScene(getSceneX()+1, getSceneY());
 			World.setCurrentMap(getSceneX(), getSceneY());
 			setCoordinates(0, getY());
 
 		} else if (getY() <= 1 && keycode == Main.KEY_UP) {
 
 			setScene(getSceneX(), getSceneY()+1);
 			World.setCurrentMap(getSceneX(), getSceneY());
 			setCoordinates(getX(), Main.SCREEN_PLAYABLE_HEIGHT);
 
 		} else if (getY() >= Main.SCREEN_PLAYABLE_HEIGHT-getH()-2 && keycode == Main.KEY_DOWN) {
 
 			setScene(getSceneX(), getSceneY()-1);
 			World.setCurrentMap(getSceneX(), getSceneY());
 			setCoordinates(getX(), -getH());
 		}
 	}
 
 	/**
 	 * Check to see if there is a solid object in our current direction of movement
 	 * 
 	 * @param keycode
 	 * @return 	<code>true</code> if there is no collision, i.e. we can move<br>
 	 * 		<code>false</code> if there is a collision
 	 */
 	private boolean checkCollisions(int keycode) {
 
 		if (World.currentMap().solidAtLeftPoint(getX(),getY()) && keycode == Main.KEY_LEFT) {
 			return false;
 		} else if (World.currentMap().solidAtRightPoint(getX(), getY()) && keycode == Main.KEY_RIGHT) {
 			return false;
 		} else if (World.currentMap().solidAtTopPoint(getX(), getY()) && keycode == Main.KEY_UP) {
 			return false;
 		} else if (World.currentMap().solidAtBottomPoint(getX(), getY()) && keycode == Main.KEY_DOWN) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method to execute if this object collided with another entity.
 	 * 
 	 */
 	@Override
 	public void collided(Entity x) {
 
 		x.destroy();
 
 	}
 
 	/** 
 	 * Draw our player to the screen. For the moment this is done as a square but
 	 * soon it will be drawn as a graphic.
 	 * 
 	 */
 	@Override
 	public void draw() {
 
 		GL11.glPushMatrix();
 
 		GL11.glTranslatef(super.getX(), super.getY(), 0);
 		GL11.glColor3f(1.0f, 1.0f, 1.0f);
 		Main.BLANK_TEXTURE.bind();
 		GL11.glBegin(GL11.GL_QUADS);
 		{
 
 			GL11.glColor3f(1.0f, 0.0f, 0.0f);
 			GL11.glVertex2f(0, 0); // top left
 			GL11.glVertex2f(0, Main.gridSize); // bottom left
 			GL11.glVertex2f(Main.gridSize, Main.gridSize); // bottom right
 			GL11.glVertex2f(Main.gridSize, 0); // top right
 
 
 			if (isRight()) {
 				GL11.glColor3f(1.0f, 1.0f, 1.0f);
 				GL11.glVertex2f(12, 0);
 				GL11.glVertex2f(12, 12);
 				GL11.glVertex2f(Main.gridSize, 12);
 				GL11.glVertex2f(Main.gridSize, 0);
 
 				GL11.glColor3f(0.0f, 0.0f, 0.0f);
 				GL11.glVertex2f(Main.gridSize-6, 3);
 				GL11.glVertex2f(Main.gridSize-6, 9);
 				GL11.glVertex2f(Main.gridSize, 9);
 				GL11.glVertex2f(Main.gridSize, 3);
 
 			} else {
 				GL11.glColor3f(1.0f, 1.0f, 1.0f);
 				GL11.glVertex2f(0, 0);
 				GL11.glVertex2f(0, 12);
 				GL11.glVertex2f(12, 12);
 				GL11.glVertex2f(12, 0);
 
 				GL11.glColor3f(0.0f, 0.0f, 0.0f);
 				GL11.glVertex2f(0, 3);
 				GL11.glVertex2f(0, 9);
 				GL11.glVertex2f(4, 9);
 				GL11.glVertex2f(4, 3);
 			}
 
 		}
 		GL11.glEnd();
 
 		GL11.glPopMatrix();
 
 	}
 
 	/**
 	 * Write the player's data to a file to be loaded later.
 	 * 
 	 * @throws IOException
 	 */
 	public void savePlayer() throws IOException {
 
		File f = new File("Player.sav");
		
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
 
 		String newline = System.getProperty("line.separator");
 		
 		w.write(toString() + newline);
 
 		for (Quest entry : QuestLog.questButtonMap.keySet()) {
 			
 			w.write("q, " + entry.getID() + ", " + entry.isComplete() + newline);
 		}
 		
 		for (Entry<Item, Integer> entry : inventory.items.entrySet()) {
 			
 			w.write("i, " + entry.getKey() + ", " + entry.getValue() + newline);
 		}
 		
 		w.close();
 	}
	
 	/**
 	 * Loads the player's data
 	 * @throws IOException
 	 */
 	public static Player loadPlayer(String playerName) throws IOException {
		
		File f = new File("Player.sav");
		
		Scanner s = new Scanner(f);
 		
 		String[] strs = s.nextLine().split(", ");
 		
 		Player player = new Player(strs[0],
 				Integer.parseInt(strs[1]), 
 				Integer.parseInt(strs[2]), 
 				Integer.parseInt(strs[3]), 
 				Integer.parseInt(strs[4]), 
 				Integer.parseInt(strs[5]), 
 				Integer.parseInt(strs[6]), 
 				Integer.parseInt(strs[7]), 
 				Integer.parseInt(strs[8]), 
 				Integer.parseInt(strs[9]));
 		
 		while (s.hasNext()) {
 			
 			strs = s.nextLine().split(", ");
 			
 			if (strs[0].equals("q")) {
 				player.getQuestLog().addQuest(strs[1], Boolean.parseBoolean(strs[2]));
 			} else if (strs[0].equals("i")) {
 				if (Integer.parseInt(strs[2]) == 0) {
 					player.getInventory().addItem(strs[1]);
				} else {
					player.getInventory().addMultipleItems(strs[1], Integer.parseInt(strs[2]));
 				}
 			}
 		}
 		
 		s.close();
 		
 		return player;
 	}
 
 	/** Return the experience object for the player */
 	public Experience getExp() {
 		return exp;
 	}
 
 	/** Return the player's inventory */
 	public Inventory getInventory() {
 		return inventory;
 	}
 
 	/** Return the player's quest log */
 	public QuestLog getQuestLog() {
 		return questLog;
 	}
 
 	/** What happens when we die */
 	@Override
 	public void notifyDeath() {
 		Game.GAME_STATE = Game.GAME_STATE_DEAD;
 
 	}
 
 	/**
 	 * Creates a String that shows all of the player's data
 	 * 
 	 */
 	@Override
 	public String toString() {
 
 		String separator = ", ";
 
 		String str = getName() + separator +
 				getHp() + separator + 
 				getMaxHP() + separator +
 				getPp() + separator + 
 				getMaxPP() + separator +
 				getExp().getCurrentExperience() + separator +
 				getSceneX() + separator + 
 				getSceneY() + separator +
 				getX() + separator + 
 				getY();
 		
 		return str;
 	}
 }
