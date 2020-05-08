 package main;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import main.interfaces.QuestLog;
 
 import org.newdawn.slick.opengl.TextureLoader;
 
 import util.input.KeyEvent;
 import util.ui.GLButton;
 
 /**
  * Subclass of Entity. Represents the player.
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
 
 		super(name);
 
 		exp = new Experience();
 
 		inventory = new Inventory();
 
 		questLog = new QuestLog();
 		
 		try {
 			texture = TextureLoader.getTexture("PNG", ResourceManager.getResourceAsStream("../res/img/player.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		facing = TextureID.FRONT;
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
 	public Player(String name, int HP, int maxHP, int PP, int maxPP, int experience, int level, int sceneX, int sceneY, int x, int y) {
 
 		super(name, HP, maxHP, PP, maxPP, sceneX, sceneY, x, y);
 
 		exp = new Experience(experience, level);
 
 		setLevel(level);
 
 		inventory = new Inventory();
 
 		questLog = new QuestLog();
		
		try {
			texture = TextureLoader.getTexture("PNG", ResourceManager.getResourceAsStream("../res/img/player.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		facing = TextureID.FRONT;
 	}
 
 	/**
 	 * Method to move entity from user interaction.
 	 * 
 	 */
 	public void input(KeyEvent event) {
 
 		if (event.getKeyCode() == Main.KeyBindings.KEY_UP.getUserKey()) {
 			move(Main.KeyBindings.KEY_UP.getUserKey());
 		}
 		if (event.getKeyCode() == Main.KeyBindings.KEY_DOWN.getUserKey()) {
 			move(Main.KeyBindings.KEY_DOWN.getUserKey());
 		}
 		if (event.getKeyCode() == Main.KeyBindings.KEY_LEFT.getUserKey()) {
 			move(Main.KeyBindings.KEY_LEFT.getUserKey());
 		}
 		if (event.getKeyCode() == Main.KeyBindings.KEY_RIGHT.getUserKey()) {
 			move(Main.KeyBindings.KEY_RIGHT.getUserKey());
 		}
 	}
 
 	/**
 	 * Here we test if we are within the bounds of the screen. If yes,
 	 * we call the super method to move. If not, we return control.<p>
 	 * 
 	 */
 	@Override
 	public void move(int keycode) {
 
 		if (keycode == Main.KeyBindings.KEY_RIGHT.getUserKey()) {
 			facing = TextureID.RIGHT;
 		} else if (keycode == Main.KeyBindings.KEY_LEFT.getUserKey()) {
 			facing = TextureID.LEFT;
 		} else if (keycode == Main.KeyBindings.KEY_UP.getUserKey()) {
 			facing = TextureID.BACK;
 		} else {
 			facing = TextureID.FRONT;
 		}
 
 		checkBorders(keycode);
 
 		if (checkMapCollisions(keycode) && checkEntityCollisions(keycode))
 			super.move(keycode);
 
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
 
		facing.drawTexture(getX()-4, getY()-10);
 	}
 
 	/**
 	 * Write the player's data to a file to be loaded later.
 	 * 
 	 * @throws IOException
 	 */
 	public void savePlayer() throws IOException {
 
 		String filename = getName() + ".sav";
 
 		File f = new File(filename);
 
 		BufferedWriter w = new BufferedWriter(new FileWriter(f));
 
 		String newline = System.getProperty("line.separator");
 
 		w.write(toString() + newline);
 		
 		for (GLButton b : QuestLog.questButtons) {
 			
 			Quest q = (Quest)b.getContext();
 			w.write("q, " + q.getID() + ", " + q.isComplete() + newline);
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
 	public static Player loadPlayer(String path) throws IOException, FileNotFoundException {
 
 		File f = new File(path);
 
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
 				Integer.parseInt(strs[9]),
 				Integer.parseInt(strs[10]));
 
 		while (s.hasNext()) {
 
 			strs = s.nextLine().split(", ");
 
 			if (strs[0].equals("q")) {
 				player.getQuestLog().addQuest(strs[1], Boolean.parseBoolean(strs[2]));
 			} else if (strs[0].equals("i")) {
 				if (Integer.parseInt(strs[2]) == 0) {
 					player.getInventory().addItem(strs[1], 0);
 				} else {
 					player.getInventory().addItem(strs[1], Integer.parseInt(strs[2]));
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
 
 	public void interact() {
 		// Don't interact with ourself!
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
 				getExp().getTotalExperience() + separator +
 				getExp().getCurrentLevel() + separator +
 				getSceneX() + separator + 
 				getSceneY() + separator +
 				getX() + separator + 
 				getY();
 
 		return str;
 	}
 }
