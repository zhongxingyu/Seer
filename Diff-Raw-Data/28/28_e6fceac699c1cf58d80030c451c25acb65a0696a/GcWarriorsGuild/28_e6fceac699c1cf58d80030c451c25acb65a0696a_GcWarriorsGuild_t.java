 package gcscripts.gcwarriorsguild;
 
 import gcapi.constants.Areas;
 import gcapi.constants.SceneObjects;
 import gcapi.constants.interfaces.Windows;
 import gcapi.gui.Gui;
 import gcapi.methods.CalculationMethods;
 import gcapi.methods.GenericMethods;
 import gcapi.methods.LocationMethods;
 import gcapi.utils.Antiban;
 import gcapi.utils.Logger;
 import gcscripts.gcwarriorsguild.branches.DefenderFarmer;
 import gcscripts.gcwarriorsguild.branches.TokenFarmer;
 import gcscripts.gcwarriorsguild.nodes.Banker;
 import gcscripts.gcwarriorsguild.nodes.CheckForFood;
 import gcscripts.gcwarriorsguild.nodes.DefenderCollector;
 import gcscripts.gcwarriorsguild.nodes.FightCyclopes;
 import gcscripts.gcwarriorsguild.nodes.ThrowShotput;
 import gcscripts.gcwarriorsguild.nodes.Walker;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import org.powerbot.core.bot.Bot;
 import org.powerbot.core.event.events.MessageEvent;
 import org.powerbot.core.event.listeners.MessageListener;
 import org.powerbot.core.event.listeners.PaintListener;
 import org.powerbot.core.script.ActiveScript;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.core.script.job.state.Tree;
 import org.powerbot.game.api.Manifest;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Equipment;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.wrappers.Area;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.bot.Context;
 
 @Manifest(authors = { "Fuz" }, name = "GC Warriors' Guild", description = "Gathers tokens with shotput/Gathers defenders", version = 1.0)
 public class GcWarriorsGuild extends ActiveScript implements MessageListener, PaintListener {
 
 	public static Logger logger = new Logger(GcWarriorsGuild.class);
 
 	public static boolean problemFound = false;
 
 	private final List<Node> jobsCollection = Collections.synchronizedList(new ArrayList<Node>());
 	private Tree jobContainer = null;
 
 	public static int defendersCollected = 0;
 
 	public static boolean init = false;
 
 	private static SelectorGui selectorGui;
 	private static Gui gui;
 
 	public static boolean collectingTokens = true;
 	public static int defenderId;
 	public static String defenderType;
 	public static boolean guiClosed = false;
 
 	public static int initialTokens;
 	public static int tokensGained;
 
 	public static boolean isBanking = false;
 
 	public static boolean hasDefender = false;
 
 	public static enum Floor {
 		GROUND, MIDDLE, TOP;
 	}
 
 	@Override
 	public void onStart() {// Initialises the logger
		logger = new Logger(this);
 		logger.log("Started script.");
 		initialTokens = getTokens();
 		if (Players.getLocal() == null) {
 			problemFound = true;
 		} else {
 			if (Game.isLoggedIn()) {
 				switch (getFloor()) {
 					case GROUND:
 					case MIDDLE:
 					case TOP:
 						final ReentrantLock lock = new ReentrantLock();
 						final Condition condition = lock.newCondition();
						final SelectorGui frame = new SelectorGui(this.logger);
 						Thread t = new Thread() {
 							public void run() {
 								synchronized (lock) {
 									while (frame.isVisible())
 										try {
 											lock.wait();
 										} catch (InterruptedException e) {
 											e.printStackTrace();
 										}
 								}
 							}
 						};
 						t.start();
 
 						frame.addWindowListener(new WindowAdapter() {
 
 							@Override
 							public void windowClosing(WindowEvent arg0) {
 								synchronized (lock) {
 									if (frame.getOptionsBox().getSelectedIndex() == 1) {
 										collectingTokens = false;
 										defenderId = gcapi.constants.Equipment.DEFENDER_IDS[((JComboBox) frame.getDefendersBox()).getSelectedIndex()];
 										GcWarriorsGuild.defenderType = ((SelectorGui) frame).getDefenders()[((JComboBox) ((SelectorGui) frame).getDefendersBox()).getSelectedIndex()];
 									} else {
 										collectingTokens = true;
 									}
 									frame.setVisible(false);
 									lock.notify();
 								}
 							}
 
 						});
 
 						try {
 							t.join();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						gui = new Gui("GC Warriors' Guild", logger, getData(), this); // Initialises
 						// GUI
 						if (collectingTokens) {
 							gcapi.constants.Equipment equipment = new gcapi.constants.Equipment();
 							int[] EQUIPMENT_HAND_SLOT_IDS = {
 									equipment.WEAPON_SLOT,
 									equipment.GLOVE_SLOT, equipment.SHIELD_SLOT };
 							for (int i : EQUIPMENT_HAND_SLOT_IDS) {
 								if (Equipment.getItem(i) != null) {
 									if (!Inventory.isFull()) {
 										Equipment.unequip(Equipment.getItems()[i].getId());
 									} else {
 										logger.log("Player does not have room to unequip hand items, stopping.");
 										problemFound = true;
 									}
 								}
 							}
 							hasDefender = true;
 							provide(new TokenFarmer(new Node[] {
 									new ThrowShotput(), new CheckForFood(),
 									new Walker(), new Banker() }));
 						} else {
 							logger.log("Collecting " + defenderType + " defenders.");
 							if (defenderType.contains("ragon")) {
 								defenderId++;
 							}
 							if (!Inventory.containsOneOf(defenderId - 1) && !Equipment.containsOneOf(defenderId - 1)) {
 								logger.log("Player does not have any defenders, banking.");
 								isBanking = true;
 								hasDefender = false;
 							} else {
 								logger.log("Player does have a defender, continuing.");
 							}
 							provide(new DefenderFarmer(new Node[] {
 									new FightCyclopes(),
 									new DefenderCollector(),
 									new CheckForFood(), new Walker(),
 									new Banker(), new Antiban() }));
 						}
 						init = true;
 						break;
 
 					default:
 						logger.log("Player is not in the Warriors' Guild, stopping.");
 						problemFound = true;
 						break;
 				}
 
 			} else {
 				problemFound = true;
 			}
 		}
 	}
 
 	public static int getTokens() {
 		String tokens = Widgets.get(1057, 16).getText();
 		if (tokens != null && tokens.replaceAll(" ", "") != "") {
 			return Integer.parseInt(Widgets.get(Windows.WARRIORS_GUILD_TOKENS_PARENT, Windows.WARRIORS_GUILD_TOKENS_STRENGTH).getText()) - initialTokens;
 		}
 		return 0;
 	}
 
 	private boolean getGuiClosed() {
 		return guiClosed;
 	}
 
 	public synchronized final void provide(final Node... jobs) {
 		for (final Node job : jobs) {
 			if (!jobsCollection.contains(job)) {
 				jobsCollection.add(job);
 				logger.log("Provided branch/node: " + job.getClass().getSimpleName());
 			}
 		}
 		jobContainer = new Tree(jobsCollection.toArray(new Node[jobsCollection.size()])); // Reconstructs the updated tree
 	}
 
 	@Override
 	public int loop() {
 		if (problemFound) {
 			this.stop();
 			Game.logout(true);
 		}
 		if (Game.isLoggedIn()) {
 			if ((Areas.WARRIORS_GUILD_GROUND_FLOOR.contains(Players.getLocal()) || Areas.WARRIORS_GUILD_MIDDLE_FLOOR.contains(Players.getLocal()) || Areas.WARRIORS_GUILD_TOP_FLOOR.contains(Players.getLocal()))) {
 				if (init) {
 					if (gui != null && !gui.isVisible()) gui.setVisible(true);
 
 					if (jobContainer != null) {
 						final Node job = jobContainer.state();
 						if (job != null) { // Checks that node is not null
 							jobContainer.set(job); // Sets node to state
 							getContainer().submit(job); // Activates node
 							job.join();
 						}
 					}
 					if (gui != null) gui.updateRows(getData());
 				}
 			} else {
 				logger.log("Player is not in the Warriors' Guild, stopping.");
 				problemFound = true;
 			}
 		}
 
 		return Random.nextInt(100, 500);
 	}
 
 	private Object[][] getData() {
 		if (collectingTokens) { // Checks if collecting tokens
 			if (gui != null) { // Checks if GUI has been initialised
 				return new Object[][] {
 						{ "Tokens collected:",
 								String.format("%,d", getTokens()) },
 						{
 								"Tokens per hour:",
 								CalculationMethods.perHour(getTokens(), gui.runTime) } };
 			}
 			return new Object[][] { { "Tokens collected:", 0 },
 					{ "Tokens per hour:", 0 } };
 		} else { // Otherwise it's collecting defenders
 			if (gui != null) { // Checks if GUI has been initialised
 				return new Object[][] {
 						{ "Defender type:", defenderType },
 						{ "Defenders collected:", defendersCollected },
 						{
 								"Defenders per hour:",
 								CalculationMethods.perHour((int) defendersCollected, gui.runTime) } };
 			}
 			return new Object[][] { { "Defender type:", 0 },
 					{ "Defenders collected:", 0 }, { "Defenders per hour:", 0 } };
 		}
 	}
 
 	@Override
 	public void messageReceived(MessageEvent msg) {
 		if (msg.getId() == 0) {
 			String message = msg.getMessage();
 			if (message.contains("thud.") || message.contains("thump.") || message.contains("floor.") || message.contains("thrown.")) {
 				tokensGained = getTokens();
 				gui.updateRows(getData());
 			}
 		}
 	};
 
 	@Override
 	public void onRepaint(Graphics g) {
 		if (Game.isLoggedIn() && Players.getLocal() != null) {
 			if (Players.getLocal() != null) {
 				switch (Players.getLocal().getPlane()) {
 					case 0:
 						g.setColor(Color.WHITE);
 						for (Tile t : Areas.WARRIORS_GUILD_GROUND_FLOOR.getTileArray()) {
 							t.draw(g);
 						}
 						g.setColor(Color.GREEN);
 						for (Tile t : Areas.WARRIORS_GUILD_BANK_AREA.getTileArray()) {
 							t.draw(g);
 						}
 						break;
 
 					case 1:
 						g.setColor(Color.WHITE);
 						for (Tile t : Areas.WARRIORS_GUILD_MIDDLE_FLOOR.getTileArray()) {
 							t.draw(g);
 						}
 						g.setColor(Color.BLUE);
 						for (Tile t : Areas.WARRIORS_GUILD_SHOTPUT_ROOM.getTileArray()) {
 							t.draw(g);
 						}
 						g.setColor(Color.YELLOW);
 						Area shotput = SceneEntities.getNearest(SceneObjects.SHOTPUT_PILE_ID).getArea();
 						if (shotput != null) {
 							for (Tile t : shotput.getTileArray()) {
 								t.draw(g);
 							}
 							break;
 						}
 
 					case 2:
 						g.setColor(Color.WHITE);
 						for (Tile t : Areas.WARRIORS_GUILD_TOP_FLOOR.getTileArray()) {
 							t.draw(g);
 						}
 						g.setColor(Color.RED);
 						for (Tile t : Areas.WARRIORS_GUILD_CYCLOPS_AREA.getTileArray()) {
 							t.draw(g);
 						}
 						break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onStop() {
 		logger.log("Script stopped.");
 		logger.close();
 		if (selectorGui != null) selectorGui.dispose();
 		if (gui != null) gui.dispose();
 		guiClosed = true;
 	}
 
 	public static Floor getFloor() {
 		if (LocationMethods.isInArea(Areas.WARRIORS_GUILD_GROUND_FLOOR)) {
 			return Floor.GROUND;
 		} else if (LocationMethods.isInArea(Areas.WARRIORS_GUILD_MIDDLE_FLOOR)) {
 			return Floor.MIDDLE;
 		} else if (LocationMethods.isInArea(Areas.WARRIORS_GUILD_TOP_FLOOR)) {
 			return Floor.TOP;
 		} else {
 			return null;
 		}
 	}
 }
