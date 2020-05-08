 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import org.powerbot.concurrent.strategy.Strategy;
 import org.powerbot.game.api.ActiveScript;
 import org.powerbot.game.api.Manifest;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Settings;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Attack;
 import org.powerbot.game.api.methods.tab.Equipment;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.tab.Prayer;
 import org.powerbot.game.api.methods.tab.Skills;
 import org.powerbot.game.api.methods.widget.Bank;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.methods.widget.Lobby;
 import org.powerbot.game.api.util.Filter;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Time;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.api.wrappers.Area;
 import org.powerbot.game.api.wrappers.Locatable;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 import org.powerbot.game.api.wrappers.interactive.Player;
 import org.powerbot.game.api.wrappers.node.Item;
 import org.powerbot.game.api.wrappers.node.SceneObject;
 import org.powerbot.game.api.wrappers.widget.WidgetChild;
 import org.powerbot.game.bot.Context;
 import org.powerbot.game.bot.event.listener.PaintListener;
 
 @Manifest(authors = { "Epics" }, name = "Epics Chinner", description = "Kills chins and banks when necessary.", version = 1.0)
 public class EpicsChins extends ActiveScript implements PaintListener,
 		MouseListener {
 	// GUI
 	private GUI gui;
 	// GUI variables
 	private int Food = 0; // user selected food
 	private int[] Antipoison = { 0 }; // user selected Antipoison
 	private boolean usingGreegree, startscript = true;
 	// Paint variables
 	private long startTime;
 	private int zombieKillCount;
 	private final Timer runtime = new Timer(0);
 	private int RANGEstartExp;
 	private int HPstartExp;
 	private int rangegainedExp;
 	private int hpgainedExp;
 	private int expHour;
 	private int chinsThrown;
 	private final int chinThrowID = 2779;
 	private int mouseX = 0;
 	private int mouseY = 0;
 	private boolean showpaint = true;
 	// Antiban variables
 	private int RANDOM_PITCH;
 	private int RANDOM_ANGLE;
 	private int state;
 	// Members Worlds array
 	private final int[] WORLDS_MEMBER = { 5, 6, 9, 12, 15, 18, 21, 22, 23, 24,
 			25, 26, 27, 28, 31, 32, 36, 39, 40, 42, 44, 45, 46, 48, 49, 51, 52,
 			53, 54, 56, 58, 59, 60, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72,
 			73, 74, 76, 77, 78, 79, 82, 83, 84, 85, 86, 87, 88, 89, 91, 92, 96,
 			97, 99, 100, 103, 104, 105, 114, 115, 116, 117, 119, 123, 124, 137,
 			138, 139 };
 	// Path details
 	public final Area AREA_GE = new Area(new Tile(3135, 3464, 0), new Tile(
 			3203, 3516, 0));
 	private final Area AREA_WAYDAR = new Area(new Tile(2642, 4525, 0),
 			new Tile(2652, 4515, 0));
 	private final Area AREA_LUMDO = new Area(new Tile(2896, 2730, 0), new Tile(
 			2887, 2717, 0));
 	private final Area AREA_INSIDE_TREE_DOOR = new Area(
 			new Tile(2896, 2730, 0), new Tile(2887, 2717, 0));
 	private final Tile TILE_GRAND_TREE = new Tile(3185, 3508, 0);
 	private final Tile TILE_SPIRIT_MID = new Tile(2542, 3169, 0);
 	private final Tile TILE_SPIRIT_END = new Tile(2462, 3444, 0);
 	private final Tile TILE_APE_START = new Tile(2802, 2707, 0);
 	private final Tile TILE_GNOME_LADDER_MID = new Tile(2466, 3994, 1);
 	private final Tile TILE_APE_LADDER_TOP = new Tile(2764, 2703, 0);
 	private final Tile TILE_APE_LADDER_BOTTOM = new Tile(2764, 9103, 0);
 	private final Tile TILE_CHIN_1 = new Tile(2715, 9127, 0);
 	private final Tile TILE_CHIN_2 = new Tile(2746, 9122, 0);
 	private final Tile TILE_CHIN_3 = new Tile(2709, 9116, 0);
 	private final Tile TILE_CHIN_4 = new Tile(2701, 9111, 0);
 	private final Tile TILE_TREE_DOOR = new Tile(2466, 3491, 0);
 	private final Tile TILE_TREE_DAERO = new Tile(2480, 3488, 1);
 	private final static Tile TILE_PRAYER = new Tile(3254, 3485, 0);
 	private final Area AREA_CHIN_3_4 = new Area(new Tile(2709, 9116, 0),
 			new Tile(2701, 9111, 0));
 	private final Tile[] CHIN_ARRAY = { TILE_CHIN_1, TILE_CHIN_2, TILE_CHIN_3,
 			TILE_CHIN_4 };
 	// GREEGREE_IDS IDs
 	private final static int[] GREEGREE_IDS = { 4031, 4024, 4025, 40256, 4027,
 			4028, 4029, 4030 };
 	// Potion IDs
 	private final static int[] FLASK_RANGING = { 23303, 23305, 23307, 23309,
 			23311, 23313 };
 	private final static int[] POT_PRAYER = { 2434, 139, 141, 143 };
 	private final static int POT_PRAYER_DOSE_4 = 2434;
 	private final static int[] FLASK_PRAYER_RENEWAL = { 23609, 23611, 23613,
 			23615, 23617, 23619 };
 	private int[] FLASK_ANTIPOISON_SUPER = { 23327, 23329, 23331, 23333, 23335,
 			23337 };
 	private int[] FLASK_ANTIPOISON_PLUSPLUS = { 23591, 23593, 23595, 23597,
 			23599, 23601 };
 	private int[] FLASK_ANTIPOISON_PLUS = { 23579, 23581, 23583, 23585, 23587,
 			23589 };
 	private int[] FLASK_ANTIPOISON = { 23315, 23317, 23319, 23321, 23323, 23325 };
 	private int[] MIX_ANTIPOISON = { 11433, 11435 };
 	private int[] POT_ANTIPOISON_SUPER = { 2448, 181, 183, 185 };
 	private int[] POT_ANTIPOISON_PLUSPLUS = { 5952, 5954, 5956, 5958 };
 	private int[] POT_ANTIPOISON_PLUS = { 5943, 5945, 5947, 5949 };
 	private int[] POT_ANTIPOISON = { 2446, 175, 177, 179 };
 	private int[] ELIXIR_ANTIPOISON = { 20879 };
 	// Tab IDs
 	private final static int TAB_VARROCK = 8007;
 	private final static int TAB_LUMBRIDGE = 8008;
 	private final static int TAB_FALADOR = 8009;
 	private final static int TAB_CAMELOT = 8010;
 	private final static int TAB_ARDOUGNE = 8011;
 	private final static int TAB_WATCHTOWER = 8012;
 	private final static int TAB_HOUSE = 8013;
 	private final static int[] tab = { TAB_VARROCK, TAB_FALADOR, TAB_LUMBRIDGE,
 			TAB_CAMELOT, TAB_ARDOUGNE, TAB_WATCHTOWER, TAB_HOUSE };
 	// General IDs
 	Timer t = null;
 	private int chinnum;
 	private boolean runcheck = true;
 	// Interaction IDs
 	private final static int ID_ANIMATION_TREE = 7082; // Tree animation when
 	// being
 	// teleported
 	private final static int ID_ANIMATION_PRAY = 645;
 	private final static int[] ID_TREEDOOR = { 69197, 69198 }; // Open Tree
 	// Door
 	private final static int ID_SPIRITTREE_GE = 1317; // Teleport Spirit tree
 	private final static int ID_SPIRITTREE_MAIN = 68974;
 	private final static int ID_LADDER_GNOME = 69499; // Climb-up Ladder
 	private final static int ID_LADDER_APE = 4780; // Climb-down Ladder
 	private final static int ID_ALTAR_VARROCK = 24343;
 	private final static int ID_ANIMATION_DEATH_ZOMBIE = 1384;
 	// NPC IDs
 	private final static int ID_NPC_DAERO = 824; // first NPC "Travel" and reply
 	// Yes
 	private final static int ID_NPC_WAYDAR = 1407; // second NPC "Travel" and
 	// reply
 	// Yes
 	private final static int ID_NPC_LUMBO = 1408; // third NPC "Travel" and
 	// reply
 	// Yes
 	private final static int ID_NPC_MONKEY_ZOMBIE = 1465;
 
 	@Override
 	protected void setup() {
 
 		try {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					gui = new GUI();
 					gui.setVisible(true);
 				}
 			});
 		} catch (Exception e) {
 		}
 
 		RANGEstartExp = Skills.getExperience(Skills.RANGE);
 		HPstartExp = Skills.getExperience(Skills.CONSTITUTION);
 		startTime = System.currentTimeMillis();
 
 		provide(new checks());
 		provide(new runToChins());
 		provide(new throwChins());
 		provide(new Banking());
 	}
 
 	private class checks extends Strategy implements Runnable {
 
 		@Override
 		public void run() {
 			chinnum = Equipment.getItem(10034).getStackSize();
 
 			if (!Attack.isAutoRetaliateEnabled()) {
 				Widgets.get(884).getChild(11).click(true);
 			}
 			runcheck = false;
 		}
 
 		@Override
 		public boolean validate() {
 			return runcheck;
 		}
 	}
 
 	private class runToChins extends Strategy implements Runnable {
 		@Override
 		public void run() {
 			log.info("Running walk there code");
 			SceneObject spiritTreeGe = SceneEntities
 					.getNearest(ID_SPIRITTREE_GE);
 			if (AREA_GE.contains(Players.getLocal().getLocation())) {
 				Item food = Inventory.getItem(Food);
 				Item prayerPot = Inventory.getItem(POT_PRAYER_DOSE_4);
 				checkRun();
 				doPreEat(food, prayerPot);
 				doChargePrayer();
 				Walking.findPath(TILE_GRAND_TREE).traverse();
 				if (spiritTreeGe != null && spiritTreeGe.isOnScreen()) {
 					if (spiritTreeGe.interact("Teleport")) {
 						Time.sleep(50);
 						if (Players.getLocal().getAnimation() == ID_ANIMATION_TREE) {
 							Time.sleep(50, 400);
 						}
 					}
 				}
 			} else if (!AREA_GE.contains(Players.getLocal().getLocation())) {
 				log.info("You aren't in the Grand Exchange! Shutting down...");
 				stop();
 			}
 			SceneObject spiritTreeMain = SceneEntities
 					.getNearest(ID_SPIRITTREE_MAIN);
 			if (spiritTreeMain != null
 					&& TILE_SPIRIT_MID.equals(Players.getLocal().getLocation())
 					&& spiritTreeMain.isOnScreen()) {
 				if (spiritTreeMain.interact("Teleport")) {
 					Time.sleep(50);
 					if (Players.getLocal().getAnimation() == ID_ANIMATION_TREE) {
 					} else {
 						log.info("Tree animation is not present. Something has gone turribly wrong!");
 					}
 				}
 				Time.sleep(50, 400);
 				WidgetChild spiritTreeInterface = Widgets.get(6, 0);
 				if (spiritTreeInterface.validate()) {
 					if (spiritTreeInterface.click(true)) {
 						Time.sleep(50);
 					}
 				}
 			} else if (!spiritTreeMain.isOnScreen()) {
 				Camera.turnTo(spiritTreeMain);
 			}
 			if (TILE_SPIRIT_END.equals(Players.getLocal().getLocation())) {
 				SceneObject treeDoor = SceneEntities.getNearest(ID_TREEDOOR);
 				Walking.findPath(TILE_TREE_DOOR).traverse();
 				if (treeDoor != null && treeDoor.isOnScreen()) {
 					if (treeDoor.interact("Open")) {
 						Time.sleep(50);
 					}
 				}
 			}
 			if (AREA_INSIDE_TREE_DOOR
 					.contains(Players.getLocal().getLocation())) {
 				SceneObject gnomeLadder = SceneEntities
 						.getNearest(ID_LADDER_GNOME);
 				if (gnomeLadder != null
 						&& Players.getLocal().getAnimation() == -1) {
 					Camera.turnTo(gnomeLadder);
 					if (gnomeLadder.interact("Climb-up")) {
 						Time.sleep(50);
 					}
 				}
 			}
 			if (TILE_GNOME_LADDER_MID.equals(Players.getLocal().getLocation())) {
 				Walking.findPath(TILE_TREE_DAERO).traverse();
 				NPC daero = NPCs.getNearest(ID_NPC_DAERO);
 				if (daero != null && daero.isOnScreen()
 						&& daero.getAnimation() == -1) {
 					Camera.turnTo(daero);
 					if (daero.interact("Travel")) {
 						Time.sleep(50);
 						WidgetChild yesInterface = Widgets.get(1188, 3);
 						if (yesInterface.validate()) {
 							if (yesInterface.click(true)) {
 								Time.sleep(Random.nextInt(100, 125));
 							}
 						}
 					}
 				}
 			}
 			if (AREA_WAYDAR.contains(Players.getLocal().getLocation())) {
 				NPC waydar = NPCs.getNearest(ID_NPC_WAYDAR);
 				if (waydar != null && waydar.isOnScreen()
 						&& waydar.getAnimation() == -1) {
 					Camera.turnTo(waydar);
 					if (waydar.interact("Travel")) {
 						Time.sleep(50);
 						WidgetChild yesInterface = Widgets.get(1188, 3);
 						if (yesInterface.validate()) {
 							yesInterface.click(true);
 						}
 					}
 				}
 			}
 			if (AREA_LUMDO.contains(Players.getLocal().getLocation())) {
 				NPC lumdo = NPCs.getNearest(ID_NPC_LUMBO);
 				if (lumdo != null && lumdo.isOnScreen()
 						&& lumdo.getAnimation() == -1) {
 					if (lumdo.interact("Travel")) {
 						Time.sleep(50);
 						WidgetChild yesInterface = Widgets.get(1188, 3);
 						if (yesInterface.validate()) {
 							if (yesInterface.click(true)) {
 								Time.sleep(50, 70);
 							}
 						}
 					}
 				}
 			}
 			if (TILE_APE_START.equals(Players.getLocal().getLocation())
 					&& usingGreegree) {
 				equipGreegree();
 				Walking.findPath(TILE_APE_LADDER_TOP).traverse();
 				Time.sleep(Random.nextInt(50, 125));
 				SceneObject apeLadder = SceneEntities.getNearest(ID_LADDER_APE);
 				if (apeLadder != null
 						&& apeLadder.isOnScreen()
 						&& TILE_APE_LADDER_TOP.equals(Players.getLocal()
 								.getLocation())) {
 					if (apeLadder.interact("Climb-down"))
 						Time.sleep(300, 425);
 				}
 			} else if (TILE_APE_START.equals(Players.getLocal().getLocation())
 					&& !usingGreegree) {
 				checkRun();
 			}
 			if (TILE_APE_LADDER_BOTTOM.equals(Players.getLocal().getLocation())) {
 				checkRenewal();
 				Prayer.setQuick();
 				Walking.walk(TILE_CHIN_1);
 				if (tileContainsTwoOrMore(TILE_CHIN_1)) {
 					Walking.walk(TILE_CHIN_2);
 					if (tileContainsTwoOrMore(TILE_CHIN_2)) {
 						Walking.walk(TILE_CHIN_3);
 						if (areaContainsTwoOrMore(AREA_CHIN_3_4)) {
 							changeWorlds();
 						}
 					}
 				}
 			}
 		}
 
 		@Override
 		public boolean validate() {
 			int praypotcountdata = 0;
 			for (Item y : Inventory.getItems()) {
 				for (int x : POT_PRAYER) {
 					if (y.getId() == x) {
 						praypotcountdata++;
 					}
 				}
 			}
 			int flaskrenewalcountdata = 0;
 			for (Item y : Inventory.getItems()) {
 				for (int x : FLASK_PRAYER_RENEWAL) {
 					if (y.getId() == x) {
 						flaskrenewalcountdata++;
 					}
 				}
 			}
 			int rangingflaskdata = 0;
 			for (Item y : Inventory.getItems()) {
 				for (int x : FLASK_RANGING) {
 					if (y.getId() == x) {
 						rangingflaskdata++;
 					}
 				}
 			}
 			int antipoisondata = 0;
 			for (Item y : Inventory.getItems()) {
 				for (int x : Antipoison) {
 					if (y.getId() == x) {
 						antipoisondata++;
 					}
 				}
 			}
 			return AREA_GE.contains(Players.getLocal().getLocation())
 					&& !isPoisoned() && Inventory.getCount(Food) >= 1
 					&& flaskrenewalcountdata == 3 && praypotcountdata == 18
 					&& rangingflaskdata == 3 && antipoisondata == 1
 					&& TAB_VARROCK > 0 && chinnum >= 500
 					&& !CHIN_ARRAY.equals(Players.getLocal().getLocation())
 					&& startscript && Game.isLoggedIn();
 		}
 	}
 
 	private class throwChins extends Strategy implements Runnable {
 		private NPC monkey_zombie;
 
 		@Override
 		public void run() {
 			log.info("Running attack code");
 			final Item rangePotItem = Inventory.getItem(FLASK_RANGING);
 			int realRange = Skills.getRealLevel(Skills.RANGE);
 			int potRange = Skills.getLevel(Skills.RANGE);
 			int rangeDifference = potRange - realRange;
 
 			if (Players.getLocal().isInCombat()) {
 				Timer throwtimer = new Timer(5000);
 				while (Players.getLocal().getAnimation() == chinThrowID
 						&& throwtimer.isRunning() && isRunning()) {
 					Time.sleep(Random.nextInt(20, 50));
 					if (Players.getLocal().getAnimation() == chinThrowID) {
 						chinnum--;
 					}
 				}
 			}
 			doAttackMonkey(monkey_zombie);
 			if (rangePotItem != null && Players.getLocal().isInCombat()
 					&& Prayer.getPoints() >= 42 && !isPoisoned()
 					&& Players.getLocal().getHpPercent() >= 90
 					&& rangeDifference >= 3) {
 				log.info("Killing monkeys and nothing is needed. Using antiban...");
 				antiban();
 			}
 			Time.sleep(Random.nextInt(50, 75));
 			if (!Prayer.isQuickOn()) {
 				Prayer.setQuick();
 				if (Players.getLocal().getPrayerIcon() == Prayer.PRAYER_BOOK_NORMAL) {
 					if (Players.getLocal().getPrayerIcon() == 19) {
 					} else {
 						Logger.getLogger("EpicsChins")
 								.info("You didn't set up your quick prayer correctly. Shutting down...");
 					}
 				} else if (Players.getLocal().getPrayerIcon() == 9) {
 				} else {
 					Logger.getLogger("EpicsChins")
 							.info("You didn't set up your quick prayer correctly. Shutting down...");
 
 				}
 			}
 			if (isPoisoned()) {
 				doDrinkAntipoison();
 			} else {
 				log.info("We're out of antipoison & we're poisoned! Teleporting to safety to bank...");
 				doBreakTab();
 			}
 			if (Players.getLocal().getAnimation() == chinThrowID) {
 				chinsThrown++;
 				Time.sleep(Random.nextInt(20, 50));
 			}
 			if (monkey_zombie != null
 					&& monkey_zombie.getAnimation() == ID_ANIMATION_DEATH_ZOMBIE) {
 				zombieKillCount++;
 			}
 			final int vialid = 229;
 			Item vial = Inventory.getItem(vialid);
 			if (Inventory.getItem() == vial) {
 				vial.getWidgetChild().interact("Drop");
 			}
 		}
 
 		@Override
 		public boolean validate() {
 			int praypotcountdata = 0;
 			for (Item y : Inventory.getItems()) {
 				for (int x : POT_PRAYER) {
 					if (y.getId() == x) {
 						praypotcountdata++;
 					}
 				}
 			}
 			return (monkey_zombie = NPCs.getNearest(ID_NPC_MONKEY_ZOMBIE)) != null
 					&& CHIN_ARRAY.equals(Players.getLocal().getLocation())
 					&& chinnum >= 200
 					&& praypotcountdata >= 1
 					&& startscript
 					&& Game.isLoggedIn();
 		}
 	}
 
 	private class Banking extends Strategy implements Runnable {
 		Item antipoisonItem = Bank.getItem(new Filter<Item>() {
 			@Override
 			public boolean accept(final Item l) {
 				for (int id : Antipoison) {
 					if (l.getId() == id)
 						return true;
 				}
 				return false;
 			}
 		});
 		Item greegreeItem = Bank.getItem(new Filter<Item>() {
 			@Override
 			public boolean accept(final Item m) {
 				for (int id : GREEGREE_IDS) {
 					if (m.getId() == id)
 						return true;
 				}
 				return false;
 			}
 		});
 		Item rangeFlaskItem = Bank.getItem(new Filter<Item>() {
 			@Override
 			public boolean accept(final Item n) {
 				for (int id : FLASK_RANGING) {
 					if (n.getId() == id)
 						return true;
 				}
 				return false;
 			}
 		});
 		Item prayerRenewalFlaskItem = Bank.getItem(new Filter<Item>() {
 			@Override
 			public boolean accept(final Item p) {
 				for (int id : FLASK_PRAYER_RENEWAL) {
 					if (p.getId() == id)
 						return true;
 				}
 				return false;
 			}
 		});
 
 		@Override
 		public void run() {
 			if (!AREA_GE.contains(Players.getLocal().getLocation())) {
 				log.info("You aren't in the Grand Exchange! Shutting down...");
 				stop();
 			}
 			log.info("Running banking code");
 			if (!AREA_GE.contains(Players.getLocal().getLocation())
 					&& Inventory.getCount(tab) >= 1) {
 				doBreakTab();
 			}
 			checkRun();
 			// TODO walking path to banks of Varrock
 			Bank.open();
 			Time.sleep(Random.nextInt(500, 700));
 			if (Bank.isOpen()) {
 				if (Bank.withdraw(10034, 2000)) {
 					return;
 				} else if (Bank.getItemCount(10034) <= 1500) {
 					log.info("Not enough chins to continue! Shutting down...");
 					Game.logout(true);
 					stop();
 				}
 				if (Bank.close()) {
 					if (Inventory.getCount(10034) >= 0) {
 						Item chinItem = Inventory.getItem(10034);
 						chinItem.getWidgetChild().click(true);
 						if (Inventory.getCount(10034) < 1) {
 							chinnum = Equipment.getItem(10034).getStackSize();
 						} else {
 							return;
 						}
 					}
 				}
 				if (usingGreegree) {
 					log.info("Selected use a greegree, banking accordingly");
 					Bank.depositInventory();
 					if (Inventory.getCount(Food) == 0) {
 						if (Bank.withdraw(Food, 1)) {
 							return;
 						}
 					} else if (Inventory.getCount(greegreeItem.getId()) == 0
 							&& usingGreegree) {
 						if (Bank.withdraw(greegreeItem.getId(), 1)) {
 							return;
 						} else if (Bank.getItemCount(greegreeItem.getId()) == 0
 								&& usingGreegree) {
 							log.info("No greegree is present. Shutting down...");
 							Game.logout(true);
 							stop();
 						}
 					} else if (!usingGreegree) {
 						log.info("Selected not to use a greegree, banking accordingly");
 						return;
 					} else if (Inventory.getCount(POT_PRAYER_DOSE_4) == 0) {
 						if (Bank.withdraw(POT_PRAYER_DOSE_4, 18)) {
 							return;
 						} else if (Bank.getItemCount(POT_PRAYER_DOSE_4) < 18) {
 							log.info("Not enough prayer pots. Shutting down...");
 							Game.logout(true);
 							stop();
 						}
 					} else if (Inventory.getCount(Antipoison) == 0) {
 						if (Bank.withdraw(antipoisonItem.getId(), 1)) {
 							return;
 						} else if (Bank.getItemCount(Antipoison) < 1) {
 							log.info("Not enough antipoison. Shutting down...");
 							Game.logout(true);
 							stop();
 						}
 					} else if (Inventory.getCount(TAB_VARROCK) == 0) {
 						if (Bank.withdraw(TAB_VARROCK, 1)) {
 							return;
 						} else if (Bank.getItemCount(TAB_VARROCK) == 0) {
 							log.info("Not enough tabs. Shutting down...");
 							Game.logout(true);
 							stop();
 						}
 					} else if (Inventory.getCount(prayerRenewalFlaskItem
 							.getId()) == 0) {
 						if (Bank.withdraw(prayerRenewalFlaskItem.getId(), 3)) {
 							return;
 						} else if (Bank.getItemCount(prayerRenewalFlaskItem
 								.getId()) < 3) {
 							log.info("Not enough prayer renewal flasks. Shutting down...");
 							Game.logout(true);
 							stop();
 						}
 					} else if (Inventory.getCount(rangeFlaskItem.getId()) == 0) {
 						if (Bank.withdraw(rangeFlaskItem.getId(), 3)) {
 							return;
 						} else if (Bank.getItemCount(rangeFlaskItem.getId()) < 3) {
 							log.info("Not enough ranged flasks. Shutting down...");
 							Game.logout(true);
 							stop();
 						}
 						Bank.close();
 					}
 				}
 			}
 			if (Players.getLocal().getHpPercent() <= 70) {
 				log.info("HP is low when banking, eating");
 				Bank.open();
 				Time.sleep(Random.nextInt(500, 700));
 				if (Bank.isOpen()) {
 					Bank.depositInventory();
 					Bank.withdraw(Food, 2);
 					Bank.close();
 				}
 				Item food = Inventory.getItem(Food);
 				if (food.getWidgetChild().interact("Eat")) {
 					Time.sleep(Random.nextInt(900, 1200));
 				}
 			}
 		}
 
 		@Override
 		public boolean validate() {
 			int antipoisondata = 0;
 			for (Item y : Inventory.getItems()) {
 				for (int x : Antipoison) {
 					if (y.getId() == x) {
 						antipoisondata++;
 					}
 				}
 			}
 			return (Inventory.getCount(POT_PRAYER) <= 1 || chinnum <= 100
 					|| isPoisoned() && antipoisondata == 0 || Players
 					.getLocal().getHpPercent() <= 25)
 					&& startscript
 					&& Game.isLoggedIn();
 		}
 	}
 
 	private boolean isPoisoned() {
 		return Settings.get(102) != 0;
 	}
 
 	private void changeWorlds() {
 		log.info("Running changeWorlds code");
 		Game.logout(true);
 		Time.sleep(Random.nextInt(2000, 5000));
 		if (Lobby.isOpen() && Lobby.STATE_LOBBY_IDLE != 0) {
 			int randomWorld = WORLDS_MEMBER[Random.nextInt(0,
 					WORLDS_MEMBER.length) - 1];
 			Context.setLoginWorld(randomWorld);
 			Time.sleep(Random.nextInt(200, 400));
 			if (Game.isLoggedIn()) {
 				Prayer.setQuick();
 			}
 		}
 	}
 
 	private void checkRun() {
 		if (Walking.getEnergy() > 30) {
 			Walking.setRun(true);
 		}
 	}
 
 	private void checkPrayer() {
 		log.info("Running checkPrayer code");
 		if (Prayer.getPoints() <= 250) {
 			final Item prayerPot = Inventory.getItem(POT_PRAYER);
 			if (prayerPot != null
 					&& prayerPot.getWidgetChild().interact("Drink")) {
 				final int id = prayerPot.getId();
 				final int count = Inventory.getCount(id);
 				final Timer t = new Timer(2500);
 				while (t.isRunning() && Inventory.getCount(id) == count) {
 					Time.sleep(50);
 				}
 			} else {
 				log.info("Prayer is above 25%, not using potion!");
 			}
 		}
 	}
 
 	private void checkRenewal() {
 		log.info("Running checkRenewal code");
 		if (t == null || t != null && !t.isRunning()) {
 			doDrinkRenewal();
 			t = new Timer(300000);
 		} else {
 			t.reset();
 		}
 	}
 
 	private void doDrinkRenewal() {
 		log.info("Running doDrinkRenewal code");
 		final Item prayerRenewal = Inventory.getItem(FLASK_PRAYER_RENEWAL);
 		if (FLASK_PRAYER_RENEWAL != null
 				&& prayerRenewal.getWidgetChild().interact("Drink")) {
 			final int id = prayerRenewal.getId();
 			final int count = Inventory.getCount(id);
 			final Timer t = new Timer(2500);
 			while (t.isRunning() && Inventory.getCount(id) == count) {
 				Time.sleep(50);
 			}
 		}
 	}
 
 	private void doDrinkAntipoison() {
 		log.info("Running doDrinkAntipoison code");
 		final Item ANTIPOISON_ALL = Inventory.getItem(Antipoison);
 		if (ANTIPOISON_ALL != null
 				&& ANTIPOISON_ALL.getWidgetChild().interact("Drink")) {
 			final int id = ANTIPOISON_ALL.getId();
 			final int count = Inventory.getCount(id);
 			final Timer t = new Timer(2500);
 			while (t.isRunning() && Inventory.getCount(id) == count) {
 				Time.sleep(50);
 			}
 		}
 	}
 
 	private void doBreakTab() {
 		log.info("Running doBreakTab code");
 		final Item tabItem = Inventory.getItem(tab);
 		if (tabItem != null && tabItem.getWidgetChild().interact("Break")) {
 			final int id = tabItem.getId();
 			final int count = Inventory.getCount(id);
 			final Timer t = new Timer(2500);
 			while (t.isRunning() && Inventory.getCount(id) == count) {
 				Time.sleep(50);
 			}
 		}
 	}
 
 	private void doDrinkRangePotion() {
 		log.info("Running doDrinkRangePotion code");
 		final Item rangePotItem = Inventory.getItem(FLASK_RANGING);
 		int realRange = Skills.getRealLevel(Skills.RANGE);
 		int potRange = Skills.getLevel(Skills.RANGE);
 		int rangeDifference = potRange - realRange;
 		if (rangePotItem != null && rangeDifference <= 3
 				&& rangePotItem.getWidgetChild().interact("Drink")) {
 			final int id = rangePotItem.getId();
 			final int count = Inventory.getCount(id);
 			final Timer t = new Timer(2500);
 			while (t.isRunning() && Inventory.getCount(id) == count) {
 				Time.sleep(50);
 			}
 		} else {
 			log.info("We're out of ranging pots, resuming until prayer potions are gone!");
 		}
 	}
 
 	private void doAttackMonkey(final NPC npc) {
 		log.info("Running doAttackMonkey code");
 		checkPrayer();
 		checkRenewal();
 		doDrinkRangePotion();
 		if (npc != null && npc.isOnScreen()) {
 			if (npc.interact("Attack")) {
 				Time.sleep(50);
 				if (!Players.getLocal().isInCombat()
 						&& Players.getLocal().getInteracting() == null) {
 					Time.sleep(Random.nextInt(700, 800));
 				}
 				if (!npc.isOnScreen()) {
 					Camera.turnTo(npc);
 					if (npc.interact("Attack")) {
 						Time.sleep(50);
 					}
 				}
 			}
 		}
 	}
 
 	private void equipGreegree() {
 		log.info("Running equipGreegree code");
 		final Item GREEGREE = Inventory.getItem(GREEGREE_IDS);
 		if (GREEGREE != null && GREEGREE.getWidgetChild().interact("Equip")) {
 			final int id = GREEGREE.getId();
 			final int count = Inventory.getCount(id);
 			final Timer t = new Timer(2500);
 			while (t.isRunning() && Inventory.getCount(id) == count) {
 				Time.sleep(50);
 			}
 		}
 	}
 
 	private static void doChargePrayer() {
 		SceneObject varrockAltar = SceneEntities.getNearest(ID_ALTAR_VARROCK);
 		Logger.getLogger("EpicsChins").info("Running doChargePrayer code");
 		if (Prayer.getPoints() < 300) {
 			Logger.getLogger("EpicsChins").info(
 					"Prayer is low, let's go charge up before we head out.");
 			Walking.findPath(TILE_PRAYER).traverse();
 			if (varrockAltar != null && varrockAltar.isOnScreen()) {
 				Camera.turnTo(varrockAltar);
 				Time.sleep(Random.nextInt(20, 50));
 				varrockAltar.click(true);
 				if (Players.getLocal().getAnimation() == ID_ANIMATION_PRAY) {
 					Time.sleep(100, 400);
 				}
 				if (Players.getLocal().getPrayerIcon() == 100) {
 					Logger.getLogger("EpicsChins").info(
 							"All charged up, let's get going.");
 				}
 			} else {
 				Logger.getLogger("EpicsChins")
 						.info("Can't find the altar, we'll proceed without charging up I suppose...");
 			}
 		}
 	}
 
 	private static void doPreEat(final Item item, Item item2) {
 		Logger.getLogger("EpicsChins").info("Running doPreEat code");
 		if (Players.getLocal().getHpPercent() < 30) {
			Walking.findPath((Locatable) Bank.getNearest());
 			Bank.open();
 			if (Bank.isOpen()) {
 				if (Inventory.isFull()) {
 					Bank.deposit(POT_PRAYER_DOSE_4, 3);
 				}
 				if (Bank.withdraw(item.getId(), 3))
 					Time.sleep(200, 400);
 				Bank.close();
 			}
 			if (!Bank.isOpen() && Players.getLocal().getHpPercent() > 75) {
 				item.getWidgetChild().interact("Eat");
 				Time.sleep(Random.nextInt(300, 400));
 			}
 			if (Bank.open())
 				Time.sleep(200, 400);
 			if (Bank.isOpen()) {
 				Bank.deposit(item2.getId(), 5);
 				if (Inventory.getCount(item.getId()) == 0) {
 					Bank.getNearest();
 					Bank.open();
 					if (Bank.isOpen()) {
 						if (Bank.withdraw(item.getId(), 2))
 							Time.sleep(200, 300);
 						Bank.withdraw(item2.getId(), 3);
 					}
 				}
 			}
 		}
 	}
 
 	private boolean tileContainsTwoOrMore(final Tile tile) {
 		Logger.getLogger("EpicsChins").info(
 				"Running tileContainsTwoOrMore code");
 		Player[] playersOnTile = Players.getLoaded(new Filter<Player>() {
 			@Override
 			public boolean accept(Player t) {
 				return t.getLocation().equals(tile);
 			}
 		});
 		if (Game.getClientState() != Game.INDEX_MAP_LOADING
 				&& playersOnTile.length >= 2) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean areaContainsTwoOrMore(final Area area) {
 		Logger.getLogger("EpicsChins").info(
 				"Running areaContainsTwoOrMore code");
 		Player[] playersInArea = Players.getLoaded(new Filter<Player>() {
 			@Override
 			public boolean accept(Player t) {
 				return t.getLocation().equals(area);
 			}
 		});
 		if (Game.getClientState() != Game.INDEX_MAP_LOADING
 				&& playersInArea.length >= 2) {
 			return true;
 		}
 		return false;
 	}
 
 	private void antiban() {
 		Logger.getLogger("EpicsChins").info("Running antiban code");
 		state = Random.nextInt(0, 3);
 		switch (state) {
 		case 1:
 			Logger.getLogger("EpicsChins").info(
 					"Setting random camera angle & pitch");
 			RANDOM_ANGLE = Random.nextInt(350, 10);
 			RANDOM_PITCH = Random.nextInt(89, 50);
 			Camera.setAngle(RANDOM_ANGLE);
 			Camera.setPitch(RANDOM_PITCH);
 			break;
 		case 2:
 			Logger.getLogger("EpicsChins").info(
 					"Checking constitution leveling progress");
 			WidgetChild c = Widgets.get(1213).getChild(12);
 			if (c.validate()) {
 				c.hover();
 				Time.sleep(1000);
 			}
 			break;
 		case 3:
 			Logger.getLogger("EpicsChins").info(
 					"Checking ranging leveling progress");
 			WidgetChild d = Widgets.get(1213).getChild(14);
 			if (d.validate()) {
 				d.hover();
 				Time.sleep(1000);
 				break;
 			}
 		}
 	}
 
 	private class GUI extends JFrame {
 		private static final long serialVersionUID = 3853009753324932631L;
 
 		public GUI() {
 			String version = " v0.1";
 			// Title
 			setTitle("EC" + version);
 			setResizable(false);
 			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			Container contentPane = getContentPane();
 			contentPane.setLayout(null);
 			// ---- foodLabel ----
 			final JLabel foodLabel = new JLabel("What food should we use?");
 			foodLabel.setBackground(new Color(212, 208, 200));
 			foodLabel.setFont(foodLabel.getFont().deriveFont(
 					foodLabel.getFont().getStyle() | Font.BOLD));
 			contentPane.add(foodLabel);
 			foodLabel.setBounds(20, 185, 155,
 					foodLabel.getPreferredSize().height);
 			// ---- antiLabel ----
 			final JTextPane antiLabel = new JTextPane();
 			antiLabel.setBackground(new Color(212, 208, 200));
 			antiLabel.setText("What antipoison should we use?");
 			antiLabel.setFont(antiLabel.getFont().deriveFont(
 					antiLabel.getFont().getStyle() | Font.BOLD));
 			antiLabel.setEditable(false);
 			contentPane.add(antiLabel);
 			antiLabel.setBounds(5, 235, 190, 25);
 			// ---- warningLabel ----
 			final JLabel warningLabel = new JLabel("WARNING");
 			warningLabel.setForeground(Color.red);
 			warningLabel.setFont(warningLabel.getFont().deriveFont(
 					warningLabel.getFont().getStyle() | Font.BOLD));
 			contentPane.add(warningLabel);
 			warningLabel.setBounds(70, 285, 60,
 					warningLabel.getPreferredSize().height);
 			// ---- warningLabelB ----
 			final JLabel warningLabelB = new JLabel(
 					"Start in the Grand Exchange!");
 			contentPane.add(warningLabelB);
 			warningLabelB.setBounds(new Rectangle(new Point(40, 305),
 					warningLabelB.getPreferredSize()));
 			// chinLabelLeft
 			final Image chinPictureLeft = getImage("http://2c1c.net/images/faceRight.png");
 			final JLabel chinLabelLeft = new JLabel(new ImageIcon(
 					chinPictureLeft));
 			contentPane.add(chinLabelLeft);
 			chinLabelLeft.setBounds(10, 10, 24, 24);
 			// chinLabelRight
 			final Image chinPictureRight = getImage("http://2c1c.net/images/faceLeft.png");
 			if (chinPictureRight == null) {
 				Logger.getLogger("EpicsChinsGUI").info("Image failed to load");
 			}
 			final JLabel chinLabelRight = new JLabel(new ImageIcon(
 					chinPictureRight));
 			if (chinPictureLeft == null) {
 				Logger.getLogger("EpicsChinsGUI").info("Image failed to load");
 			}
 			contentPane.add(chinLabelRight);
 			chinLabelRight.setBounds(160, 10, 24, 24);
 			// ---- greeLabel ----
 			final JLabel greeLabel = new JLabel("Are we using a greegree?");
 			greeLabel.setFont(greeLabel.getFont().deriveFont(
 					greeLabel.getFont().getStyle() | Font.BOLD));
 			contentPane.add(greeLabel);
 			greeLabel.setBounds(new Rectangle(new Point(25, 140), greeLabel
 					.getPreferredSize()));
 			// ---- titleLabel ----
 			final JLabel titleLabel = new JLabel("Epics Chinner" + version);
 			titleLabel.setFont(titleLabel.getFont().deriveFont(
 					titleLabel.getFont().getStyle() | Font.BOLD));
 			contentPane.add(titleLabel);
 			titleLabel.setBounds(45, 10, 110, 25);
 			// ---- reqTextPane ----
 			final JTextPane reqTextPane = new JTextPane();
 			reqTextPane.setBackground(new Color(212, 208, 200));
 			reqTextPane.setCursor(Cursor
 					.getPredefinedCursor(Cursor.TEXT_CURSOR));
 			reqTextPane.setDisabledTextColor(new Color(240, 240, 240));
 			reqTextPane.setEditable(false);
 			reqTextPane.setText("Requirements:");
 			reqTextPane.setFont(reqTextPane.getFont().deriveFont(
 					reqTextPane.getFont().getStyle() | Font.BOLD));
 			contentPane.add(reqTextPane);
 			reqTextPane.setBounds(45, 40, 95, 20);
 			// ---- reqTextPaneB ----
 			final JTextPane reqTextPaneB = new JTextPane();
 			reqTextPaneB.setBackground(new Color(212, 208, 200));
 			reqTextPaneB
 					.setText("- Access to Ape Atoll\n- 43 Prayer\n- 55 Ranged\n- 3+ Prayer renewal flasks\n- 3+ Ranged flasks");
 			reqTextPaneB.setEditable(false);
 			contentPane.add(reqTextPaneB);
 			reqTextPaneB.setBounds(25, 55, 135, 75);
 			// ---- greeBoxYes ----
 			final JCheckBox greeBoxYes = new JCheckBox("Yes");
 			greeBoxYes.setSelected(true);
 			if (greeBoxYes.isSelected()) {
 				usingGreegree = true;
 			}
 			contentPane.add(greeBoxYes);
 			greeBoxYes.setBounds(new Rectangle(new Point(45, 160), greeBoxYes
 					.getPreferredSize()));
 			// ---- greeBoxNo ----
 			final JCheckBox greeBoxNo = new JCheckBox("No");
 			greeBoxNo.setSelected(false);
 			if (greeBoxNo.isSelected()) {
 				usingGreegree = false;
 			}
 			contentPane.add(greeBoxNo);
 			greeBoxNo.setBounds(new Rectangle(new Point(100, 160), greeBoxNo
 					.getPreferredSize()));
 			// ---- foodCombo ----
 			final JComboBox<String> foodCombo = new JComboBox<>();
 			foodCombo.setModel(new DefaultComboBoxModel<>(new String[] {
 					"Select your food...", "Shark", "Rocktail", "Monkfish",
 					"Swordfish", "Lobster", "Tuna", "Trout", "Salmon" }));
 			contentPane.add(foodCombo);
 			foodCombo.setBounds(25, 210, 150,
 					foodCombo.getPreferredSize().height);
 			// ---- poisonCombo ----
 			final JComboBox<String> poisonCombo = new JComboBox<String>();
 			poisonCombo.setModel(new DefaultComboBoxModel<>(new String[] {
 					"Select an antipoison...", "Super antipoison flask",
 					"Antipoison++ flask", "Antipoison+ flask",
 					"Antipoison flask", "Super antipoison", "Antipoison++",
 					"Antipoison+", "Antipoison", "Antipoison mix",
 					"Antipoison elixir" }));
 			contentPane.add(poisonCombo);
 			poisonCombo.setBounds(25, 260, 150,
 					poisonCombo.getPreferredSize().height);
 			{
 				Dimension preferredSize = new Dimension();
 				for (int i = 0; i < contentPane.getComponentCount(); i++) {
 					Rectangle bounds = contentPane.getComponent(i).getBounds();
 					preferredSize.width = Math.max(bounds.x + bounds.width,
 							preferredSize.width);
 					preferredSize.height = Math.max(bounds.y + bounds.height,
 							preferredSize.height);
 				}
 				Insets insets = contentPane.getInsets();
 				preferredSize.width += insets.right;
 				preferredSize.height += insets.bottom;
 				contentPane.setMinimumSize(preferredSize);
 				contentPane.setPreferredSize(preferredSize);
 			}
 			setSize(210, 395);
 			setLocationRelativeTo(null);
 			// ---- startButton ----
 			final JButton startButton = new JButton("Start");
 			contentPane.add(startButton);
 			startButton.setBounds(5, 330, 185, 25);
 			startButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					startscript = true;
 					String chosenFood = foodCombo.getSelectedItem().toString();
 					if (chosenFood.equals("Select your food...")) {
 						Logger.getLogger("EpicsChins").info(
 								"No food selected, stopping script");
 						stop();
 					}
 					if (chosenFood.equals("Shark")) {
 						Food = 385;
 					}
 					if (chosenFood.equals("Rocktail")) {
 						Food = 15272;
 					}
 					if (chosenFood.equals("Monkfish")) {
 						Food = 7946;
 					}
 					if (chosenFood.equals("Swordfish")) {
 						Food = 373;
 					}
 					if (chosenFood.equals("Lobster")) {
 						Food = 379;
 					}
 					if (chosenFood.equals("Tuna")) {
 						Food = 361;
 					}
 					if (chosenFood.equals("Trout")) {
 						Food = 333;
 					}
 					if (chosenFood.equals("Salmon")) {
 						Food = 329;
 					}
 					String chosenAntipoison = poisonCombo.getSelectedItem()
 							.toString();
 					if (chosenAntipoison.equals("Select an antipoison...")) {
 						Logger.getLogger("EpicsChins").info(
 								"No antipoison selected, stopping script");
 						Game.logout(false);
 						stop();
 					}
 					if (chosenAntipoison.equals("Super antipoison flask")) {
 						Antipoison = FLASK_ANTIPOISON_SUPER;
 					}
 					if (chosenAntipoison.equals("Antipoison++ flask")) {
 						Antipoison = FLASK_ANTIPOISON_PLUSPLUS;
 					}
 					if (chosenAntipoison.equals("Antipoison+ flask")) {
 						Antipoison = FLASK_ANTIPOISON_PLUS;
 					}
 					if (chosenAntipoison.equals("Antipoison Flask")) {
 						Antipoison = FLASK_ANTIPOISON;
 					}
 					if (chosenAntipoison.equals("Super Antipoison")) {
 						Antipoison = POT_ANTIPOISON_SUPER;
 					}
 					if (chosenAntipoison.equals("Antipoison++")) {
 						Antipoison = POT_ANTIPOISON_PLUSPLUS;
 					}
 					if (chosenAntipoison.equals("Antipoison+")) {
 						Antipoison = POT_ANTIPOISON_PLUS;
 					}
 					if (chosenAntipoison.equals("Antipoison")) {
 						Antipoison = POT_ANTIPOISON;
 					}
 					if (chosenAntipoison.equals("Antipoison mix")) {
 						Antipoison = MIX_ANTIPOISON;
 					}
 					if (chosenAntipoison.equals("Antipoison elixir")) {
 						Antipoison = ELIXIR_ANTIPOISON;
 					}
 					gui.dispose();
 					log.info("GUI disposed, providing methods");
 				}
 			});
 		}
 	}
 
 	private Image getImage(String url) {
 		try {
 			return ImageIO.read(new URL(url));
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	private final Color color1 = new Color(255, 255, 255);
 	private final Font font1 = new Font("Verdana", 0, 10);
 	private final Image img1 = getImage("http://2c1c.net/images/paint.png");
 
 	public void onRepaint(Graphics g1) {
 		mouseX = Mouse.getX();
 		mouseY = Mouse.getY();
 		rangegainedExp = Skills.getExperience(Skills.RANGE) - RANGEstartExp;
 		hpgainedExp = Skills.getExperience(Skills.CONSTITUTION) - HPstartExp;
 		expHour = (int) ((rangegainedExp) * 3600000D / (System
 				.currentTimeMillis() - startTime));
 		if (showpaint) {
 			Graphics2D g = (Graphics2D) g1;
 			g.drawImage(img1, -4, 336, null);
 			g.setFont(font1);
 			g.setColor(color1);
 			g.drawString(runtime.toElapsedString(), 215, 444);
 			g.drawString(String.valueOf(rangegainedExp), 198, 460);
 			g.drawString(String.valueOf(hpgainedExp), 181, 480);
 			g.drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10);
 			g.drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY);
 			g.drawString(String.valueOf(expHour), 183, 497);
 			g.drawString(String.valueOf(chinsThrown), 351, 443);
 			g.drawString(String.valueOf(zombieKillCount), 359, 461);
 		} else {
 			Graphics2D g = (Graphics2D) g1;
 			g.drawRect(502, 389, 15, 15);
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (new Rectangle(502, 389, 14, 15).contains(e.getPoint())) {
 			if (showpaint) {
 				showpaint = false;
 			} else if (img1 == null) {
 				Logger.getLogger("EpicsChinsGUI").info("Image failed to load");
 			}
 			showpaint = true;
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 }
