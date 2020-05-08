 package main;
 
 import java.util.ArrayList;
 import java.util.Properties;
 
 import main.components.FighterComponent;
 import main.helpers.Message;
 import main.objects.Entity;
 import main.objects.Tile;
 import net.slashie.libjcsi.CSIColor;
 import net.slashie.libjcsi.CharKey;
 import net.slashie.libjcsi.ConsoleSystemInterface;
 import net.slashie.libjcsi.wswing.WSwingConsoleInterface;
 
 /**
  * Starting Class, contains game properties, handling keys and game loop
  * 
  * @author prokk
  * 
  */
 public class MainGame
 {
 	// private static Logger log = Logger.getLogger(MainGame.class.getName());
 	private static MainGame					instance;
 	private static ConsoleSystemInterface	csi;
 	private static Tile						map[][]		= new Tile[MainMap.MAP_WIDTH][MainMap.MAP_HEIGHT];
 	private ArrayList<Message>				game_msgs	= new ArrayList<Message>();
 	private boolean							stop;
 	private boolean							game_started;
 	private String							gameState;
 	private String							playerAction;
 	private Entity							player;
 
 	/**
 	 * 
 	 * @return instance of the class
 	 */
 	public static MainGame getInstance()
 	{
 		return instance;
 	}
 
 	/**
 	 * 
 	 * @return the array of the map
 	 */
 	public Tile[][] getMap()
 	{
 		return map;
 	}
 
 	/**
 	 * 
 	 * @return the player entity
 	 */
 	public Entity getPlayer()
 	{
 		return player;
 	}
 
 	/**
 	 * 
 	 * @return interface for drawing in console
 	 */
 	public static ConsoleSystemInterface getCSI()
 	{
 		return csi;
 	}
 
 	/**
 	 * Sets the game state
 	 * 
 	 * @param state
 	 */
 	public void setGameState(String state)
 	{
 		gameState = state;
 	}
 
 	private ArrayList<Entity>	inventory	= new ArrayList<Entity>();
 
 	/**
 	 * 
 	 * @return array of inventory
 	 */
 	public ArrayList<Entity> getInventory()
 	{
 		return inventory;
 	}
 
 	/**
 	 * VOID MAIN
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		Properties text = new Properties();
 		text.setProperty("fontSize", "15");
 		text.setProperty("font", "roguelike.ttf");
 		csi = new WSwingConsoleInterface("RogueLike", text);
 		instance = new MainGame();
 		instance.run();
 	}
 
 	/**
 	 * Runs the game
 	 */
 	public void run()
 	{
 		// create new player entity
 		// and set a fighter component for it
 		player = new Entity(MainMap.MAP_WIDTH / 2, MainMap.MAP_HEIGHT / 2, '@', "player", CSIColor.RED, true);
 		FighterComponent fighter_component = new FighterComponent(player, 30, 0, 2, 5);
 		player.setFighterComponent(fighter_component);
 
 		// initial map generation
 		// and map drawing
 		MainMap.getInstance().generateMap();
 		MainMap.getInstance().drawMap();
 
 		// initial game state and player action
 		// set to default
 		gameState = "playing";
 		playerAction = "";
 
 		// boolean stop for exiting the game
 		stop = false;
 
 		// game started boolean for start window
 		game_started = false;
 		// main game loop
 		while (!stop)
 		{
 
 			// clearing the game screen
 			if (!inventoryShown)
 				if (!characterShown)
 					if (!levelUp)
 						if (!dropShown)
 							csi.cls();
 
 			// / SHOW START HELP WINDOW //
 			if (!game_started)
 			{
 				showStartWindow();
 				handleMenu();
 				playerAction = "didnt-take-turn";
 			}
 
 			MainMap.getInstance().moveCamera(player.getX(), player.getY());
 			// printing GUI
 			printGUI();
 			// drawing the map
 			if (!inventoryShown)
 				if (!characterShown)
 					if (!levelUp)
 						if (!dropShown)
 							MainMap.getInstance().drawMap();
 			// drawing the player
 			player.draw();
 			// refreshing the console output
 			csi.refresh();
 
 			checkLevelUp();
 
 			// handling the player keys
 			if (game_started)
 				playerAction = handleKeys();
 			if (playerAction.equals("exit"))
 				System.exit(0);
 
 			// let monsters take their turn
 			if (gameState.equals("playing") && (!playerAction.equals("didnt-take-turn")))
 				for (Entity object : MainMap.getInstance().getObjects())
 					if (object != player)
 						if (object.getAIComponent() != null)
 							object.getAIComponent().takeTurn();
 		}
 		System.exit(0);
 	}
 
 	/**
 	 * Handle keys while start screen is active
 	 */
 	public void handleMenu()
 	{
 		CharKey dir = csi.inkey();
 
 		// Q is the key for exit
 		if (dir.code == CharKey.Q || dir.code == CharKey.q)
 		{
 			stop = true;
 		}
 		if (dir.code == CharKey.ENTER)
 		{
 			game_started = true;
 			csi.cls();
 		}
 	}
 
 	/**
 	 * Handle keys while the game is active
 	 * 
 	 * @return the player action
 	 */
 	public String handleKeys()
 	{
 		// getch()
 		CharKey dir = csi.inkey();
 		// Q is the key for exit
 		if (dir.code == CharKey.Q || dir.code == CharKey.q)
 		{
 			stop = true;
 			return "exit";
 		}
 
 		// C is the key to clear the combat log
 		if (gameState.equals("playing"))
 			if (dir.code == CharKey.l || dir.code == CharKey.L)
 			{
 				game_msgs.clear();
 				return "didnt-take-turn";
 			}
 		if (game_started)
 			if (characterShown)
 			{
 				if (dir.code == CharKey.C || dir.code == CharKey.c)
 					showCharacterWindow();
 				return "didnt-take-turn";
 			}
 		if (game_started)
 			if (characterShown)
 			{
 				if (dir.code == CharKey.d || dir.code == CharKey.D)
 					showDrop();
 				return "didnt-take-turn";
 			}
 		if (game_started)
 			if (levelUp)
 			{
 				switch (dir.code)
 				{
 				case CharKey.N1:
				case CharKey.T1:
 					player.getFighterComponent().setMaxHp(player.getFighterComponent().getMaxHP() + 10);
 					player.getFighterComponent().incConst(1);
 					player.getFighterComponent().setHp(player.getFighterComponent().getMaxHP());
 					player.getFighterComponent().setMana(player.getFighterComponent().getMaxMana());
 					showLevelupWindow();
 					break;
 				case CharKey.N2:
				case CharKey.T2:
 					player.getFighterComponent().setMaxMana(player.getFighterComponent().getMaxMana() + 10);
 					player.getFighterComponent().incIntellect(1);
 					player.getFighterComponent().setHp(player.getFighterComponent().getMaxHP());
 					player.getFighterComponent().setMana(player.getFighterComponent().getMaxMana());
 					showLevelupWindow();
 					break;
 				case CharKey.N3:
				case CharKey.T3:
 					player.getFighterComponent().incPower(1);
 					showLevelupWindow();
 					break;
 				case CharKey.N4:
				case CharKey.T4:
 					player.getFighterComponent().incDefence(1);
 					showLevelupWindow();
 					break;
 				}
 				return "didnt-take-turn";
 			}
 		if (game_started)
 			if (inventoryShown)
 			{
 				if (dir.code == CharKey.I || dir.code == CharKey.i)
 					showInventory();
 				if (dir.code >= 118 && dir.code <= 126)
 				{
 					if (getInventory().size() > (dir.code - 118))
 						getInventory().get(dir.code - 118).getItemComponent().useItem();
 					showInventory();
 				}
				
				if (dir.code >= 131 && dir.code <= 139)
				{
					if (getInventory().size() > (dir.code - 131))
						getInventory().get(dir.code - 131).getItemComponent().useItem();
					showInventory();
				}
 
 				return "didnt-take-turn";
 			}
 
 		if (game_started)
 			if (dropShown)
 			{
 				if (dir.code == CharKey.d || dir.code == CharKey.D)
 					showDrop();
 				if (dir.code >= 118 && dir.code <= 126)
 				{
 					if (getInventory().size() > (dir.code - 118))
 						getInventory().get(dir.code - 118).getItemComponent().dropItem();
 					showDrop();
 				}
 				return "didnt-take-turn";
 			}
 
 		// moving the player with ARROWS
 		if (gameState.equals("playing"))
 		{
 			if (dir.isUpArrow() && (player.getY() - 1 >= 0))
 			{
 				MainMap.getInstance().playerMoveOrAttack(0, -1);
 			} else
 				if (dir.isDownArrow() && (player.getY() + 1 < MainMap.MAP_HEIGHT))
 				{
 					MainMap.getInstance().playerMoveOrAttack(0, 1);
 				} else
 					if (dir.isLeftArrow() && (player.getX() - 1 >= 0))
 					{
 						MainMap.getInstance().playerMoveOrAttack(-1, 0);
 					} else
 						if (dir.isRightArrow() && (player.getX() + 1 < MainMap.MAP_WIDTH))
 						{
 							MainMap.getInstance().playerMoveOrAttack(1, 0);
 						} else
 							if (dir.code == CharKey.Z || dir.code == CharKey.z)
 							{
 								MainMap.getInstance().nextLevel();
 							} else
 								if (dir.code == CharKey.I || dir.code == CharKey.i)
 								{
 									showInventory();
 									return "didnt-take-turn";
 								} else
 									if (dir.code == CharKey.c || dir.code == CharKey.C)
 									{
 										showCharacterWindow();
 										return "didnt-take-turn";
 									} else
 										if (dir.code == CharKey.d || dir.code == CharKey.D)
 										{
 											showDrop();
 											return "didnt-take-turn";
 										} else
 											if (dir.code == CharKey.G || dir.code == CharKey.g)
 											{
 												MainMap.getInstance().grabItem();
 											} else
 												return "didnt-take-turn";
 		}
 		return "";
 	}
 
 	/**
 	 * print the GUI on the screen
 	 */
 	private void printGUI()
 	{
 		// printing player hp
 		csi.print(0, MainMap.CAMERA_HEIGHT, "HP " + player.getFighterComponent().getHp() + "/" + player.getFighterComponent().getMaxHP());
 		csi.print(0, MainMap.CAMERA_HEIGHT + 1, "MP " + player.getFighterComponent().getMana() + "/" + player.getFighterComponent().getMaxMana());
 		csi.print(0, MainMap.CAMERA_HEIGHT + 3, "LEVEL " + player.getLevel());
 		csi.print(0, MainMap.CAMERA_HEIGHT + 4, "XP " + player.getFighterComponent().getXP() + "/" + xpForLevelUp());
 		// printing the combat log
 		int y = 0;
 		for (Message message : game_msgs)
 		{
 			csi.print(20, y + MainMap.CAMERA_HEIGHT, message.getMessage(), message.getColor());
 			y++;
 		}
 		// ////////////////////////
 	}
 
 	/**
 	 * Adds new message to combat log
 	 * 
 	 * @param message
 	 * @param color
 	 *            of message
 	 */
 	public void newMessage(String message, CSIColor color)
 	{
 		Message mes = new Message(message, color);
 		// removing the messages from log if too much
 		if (game_msgs.size() > 4)
 			game_msgs.remove(0);
 		game_msgs.add(mes);
 	}
 
 	/**
 	 * Show start screen with help
 	 */
 	public void showStartWindow()
 	{
 		// clear all window
 		csi.cls();
 		csi.print(30, 15, "INFINITE CAVY STORY", CSIColor.AMETHYST);
 		csi.print(27, 16, "Old-school rogue-like game");
 
 		csi.print(20, 19, "Use arrows to move");
 		csi.print(20, 20, "Use arrows to the targets direction to attack");
 		csi.print(20, 21, "Press L to clear combat log");
 		csi.print(20, 21, "Press Z to go to kext level while on stairs");
 		csi.print(20, 22, "Press G to grab item");
 		csi.print(20, 22, "Press I to open inventory");
 		csi.print(20, 23, "Press D to open drop window");
 		csi.print(20, 24, "Press C to open your character window");
 		csi.print(20, 27, "Press Q to quit");
 
 		csi.print(28, 28, "PRESS ENTER TO CONTINUE", CSIColor.GREEN_YELLOW);
 
 		csi.print(64, 44, "Created by Prokk");
 	}
 
 	boolean	inventoryShown	= false;
 
 	/**
 	 * Show inventory on screen
 	 */
 	public void showInventory()
 	{
 		if (!inventoryShown)
 		{
 			csi.print(30, 15, "====== INVENTORY ======");
 			int i = 0;
 			if (getInventory().isEmpty())
 			{
 				csi.print(30, 16, "Empty");
 				i++;
 			} else
 			{
 				for (int count = 0; count < getInventory().size(); count++)
 				{
 					csi.print(30, 16 + i, (i + 1) + ": " + getInventory().get(i).getName());
 					i++;
 				}
 			}
 			csi.print(30, 16 + i, "=======================");
 			inventoryShown = true;
 		} else
 		{
 			MainMap.getInstance().drawMap();
 			inventoryShown = false;
 		}
 	}
 
 	boolean	dropShown	= false;
 
 	/**
 	 * Show drop window on screen
 	 */
 	public void showDrop()
 	{
 		if (!dropShown)
 		{
 			csi.print(30, 15, "====== DROP ITEMS ======");
 			int i = 0;
 			if (getInventory().isEmpty())
 			{
 				csi.print(30, 16, "Empty");
 				i++;
 			} else
 			{
 				for (int count = 0; count < getInventory().size(); count++)
 				{
 					csi.print(30, 16 + i, (i + 1) + ": " + getInventory().get(i).getName());
 					i++;
 				}
 			}
 			csi.print(30, 16 + i, "=======================");
 			dropShown = true;
 		} else
 		{
 			MainMap.getInstance().drawMap();
 			dropShown = false;
 		}
 	}
 
 	boolean	characterShown	= false;
 
 	/**
 	 * show character stats window on screen
 	 */
 	public void showCharacterWindow()
 	{
 		if (!characterShown)
 		{
 			csi.print(30, 15, "====== CHARACTER ======");
 			csi.print(30, 16, "LEVEL " + player.getLevel());
 			csi.print(42, 16, "XP " + player.getFighterComponent().getXP());
 
 			csi.print(30, 17, "HP " + player.getFighterComponent().getHp() + "/" + player.getFighterComponent().getMaxHP());
 			csi.print(42, 17, "MP " + player.getFighterComponent().getMana() + "/" + player.getFighterComponent().getMaxMana());
 
 			csi.print(30, 18, "CONST " + player.getFighterComponent().getConstitution());
 			csi.print(42, 18, "INT " + player.getFighterComponent().getIntellect());
 			csi.print(30, 19, "ATTACK " + player.getFighterComponent().getPower());
 			csi.print(42, 19, "DEF " + player.getFighterComponent().getDefence());
 			csi.print(30, 20, "=======================");
 			characterShown = true;
 		} else
 		{
 			MainMap.getInstance().drawMap();
 			characterShown = false;
 		}
 	}
 
 	private boolean	levelUp	= false;
 
 	/**
 	 * show levelup window on screen
 	 */
 	public void showLevelupWindow()
 	{
 		printGUI();
 		if (!levelUp)
 		{
 			csi.print(30, 15, "====== LEVEL UP ======");
 			csi.print(30, 16, "1: INCREASE CONSTITUTION TO " + (player.getFighterComponent().getConstitution() + 1));
 			csi.print(30, 17, "2: INCREASE INTELLECT TO " + (player.getFighterComponent().getIntellect() + 1));
 			csi.print(30, 18, "3: INCREASE ATTACK TO " + (player.getFighterComponent().getPower() + 1));
 			csi.print(30, 19, "4: INCREASE DEFENCE TO " + (player.getFighterComponent().getDefence() + 1));
 			levelUp = true;
 		} else
 		{
 
 			MainMap.getInstance().drawMap();
 			levelUp = false;
 		}
 
 	}
 
 	private final static int	LEVEL_UP_BASE	= 200;
 	private final static int	LEVEL_UP_FACTOR	= 150;
 
 	/**
 	 * Count xp needed for next level up
 	 * 
 	 * @return
 	 */
 	public int xpForLevelUp()
 	{
 		return LEVEL_UP_BASE + player.getLevel() * LEVEL_UP_FACTOR;
 	}
 
 	/**
 	 * Check if there is a levelup
 	 */
 	public void checkLevelUp()
 	{
 		int levelUpXP = LEVEL_UP_BASE + player.getLevel() * LEVEL_UP_FACTOR;
 		if (player.getFighterComponent().getXP() >= levelUpXP)
 		{
 			player.incLevel();
 			player.getFighterComponent().setXP(player.getFighterComponent().getXP() - levelUpXP);
 			MainGame.getInstance().newMessage("Your battle skills grow stronger! You reached level " + player.getLevel(), CSIColor.GOLDEN);
 			MainGame.getInstance().showLevelupWindow();
 			player.getFighterComponent().setHp(player.getFighterComponent().getMaxHP());
 			player.getFighterComponent().setMana(player.getFighterComponent().getMaxMana());
 		}
 	}
 }
