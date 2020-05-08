 package org.listerkeler.scripts.ChlenixChopper;
 
 import org.listerkeler.api.randoms.MysteryBox;
 import org.listerkeler.api.util.Condition;
 import org.listerkeler.api.util.Methods;
 import org.listerkeler.api.util.Timer;
 import org.listerkeler.scripts.ChlenixChopper.ChlenixChopper.ScriptState;
 import org.vinsert.bot.script.ScriptManifest;
 import org.vinsert.bot.script.StatefulScript;
 import org.vinsert.bot.script.api.*;
 import org.vinsert.bot.script.api.generic.Filters;
 import org.vinsert.bot.script.api.generic.Hullable;
 import org.vinsert.bot.script.api.generic.Interactable;
 import org.vinsert.bot.script.api.tools.Game;
 import org.vinsert.bot.script.api.tools.Game.Tabs;
 import org.vinsert.bot.script.api.tools.Navigation.NavigationPolicy;
 import org.vinsert.bot.script.api.tools.Skills;
 import org.vinsert.bot.util.Filter;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.text.DecimalFormat;
 
 @ScriptManifest(name = "ChlenixChopper", authors = {"SpK", "Chlenix"}, description = "Chops various trees", version = 1.5D)
 public class ChlenixChopper extends StatefulScript<ScriptState> {
 
     // Debugging stuff
     private final boolean debugMode = true;
     public boolean waitForGUI = true;
     private static boolean handlingRandom = false;
 
     private long lastMove = System.currentTimeMillis();
     private long startTime = System.currentTimeMillis();
     private long runningTime = 0L;
     private long antiBanCountdown = 0L;
     private long antiBanInterval = 0L;
     private long lastAntiBan = 0L;
 
     private double experiencePerHour = 0D;
     private double timeTillLevel = 0D;
 
     private int startExperience = 0;
     private int timesBanked = 0;
     private int inventoryLogs = 0;
     private int logsCut = 0;
     private int experienceGained = 0;
     private int logsTillNextLevel = 0;
 
     private Npc randomEventNPC = null;
     private Item randomEventItem = null;
 
     private String formattedTime = "00:00:00";
     private String antiBanStatus = "";
     private String formattedTimeTillLevel = "00:00:00";
 
     // GUI
     private final ChlenixChopperGUI GUI = new ChlenixChopperGUI(this);
     private final JFrame frame = new JFrame("ChlenixChopper");
 
     // Settings
     private static ChlenixChopperSettings scriptSettings;
 
     private static Area bankArea;
     private static Area treeArea;
 
     private static int[] TREE_OBJECT;
     private static int TREE_LOG;
 
     private static Path[] treePath;
     private static Path[] bankPath;
 
     // Formats
     private final DecimalFormat numberFormat = new DecimalFormat("#,###");
 
     // Areas
     private final static Area seersBankArea = new Area(new Tile(2721, 3490), new Tile(2730, 3493));
 
     // Tiles
     private static Tile bankPosition;
     private static Tile treePosition;
 
     // IDs
     public static final int[] WILLOW_TREE = { 1308, 5551, 5552, 5553 };
     public static final int WILLOW_LOG = 1520;
 
     public static final int[] MAPLE_TREE = { 1307 };
     public static final int MAPLE_LOG = 1520;
 
     public static final int[] YEW_TREE = { 1309 };
     public static final int YEW_LOG = 1520;
 
     public static final int[] MAGIC_TREE = { 1306 };
     public static final int MAGIC_LOG = 1514;
 
 
     private static final int[] BANK_BOOTHS = { 25808 };
 
     // Axes
     private final int[] axeIds = new int[] { 1360, 1351, 1352, 1349, 1350, 1353, 1354, 1355, 1356, 1357, 1358, 1359, 3063, 6740 };
 
     // NPCs
     private final int[] randomEventNPCs = new int[] { 2476, 3117, 2539, 409, 410, 2540, 411, 2470, 4375, 956, 407, 4416 };
     private final int[] randomEventItems = new int[] { 3063, 9004 };
 
     // Tree Randoms
     private final int[] treeEnts = new int[] { 1740, 1731, 1735, 1736, 1739, 1737, 1734, 777 };
     private final int[] birdNests = new int[] { 5071, 5072, 5073, 5074, 5075, 5070, 7413, 5076 };
 
     // Anti-ban strings
     private final String[] randomChat = new String[]{"I'm almost level {wcLevel}", "Brb", "Back", "My dog is so annoying omg",
             "About to hit {wcLevel}", "This is so boring", "How much are yews now?",
             "Wc levels?", "What level are you guys?", "What to chop for best exp?",
             "Swag", "Yolo"};
 
     @Override
     public ScriptState determine() {
         Player me = localPlayer;
         if (me.isMoving() || localPlayer.getAnimation() == 867) {
             lastMove = System.currentTimeMillis();
         }
 
         // Check if we're in combat and run away
         if (me.isInCombat()) {
             return ScriptState.COMBAT;
         }
 
         // Check if any random events  have appeared
         if (getRandomNPCHere() || getRandomItemHere()) {
             return ScriptState.SOLVING_RANDOM;
         }
 
         // Check if the script is paused
         if (isPaused()) {
             return ScriptState.PAUSED;
         }
 
         // Check if we need to bank, and bank is open
         if (((bankArea.contains(me)) && (inventory.isFull())) || bank.isOpen()) {
             return ScriptState.BANK;
         }
 
         // Check if we need to walk to the bank
         if ((inventory.isFull()) && (!bankArea.contains(me))) {
             return ScriptState.WALK_TO_BANK;
         }
 
         // Check if we need to walk to the trees
         if (!treeArea.contains(me)) {
             return ScriptState.WALK_TO_TREES;
         }
 
         // Check if we need to start chopping
         if (!inventory.isFull()) {
             if (localPlayer.getAnimation() == -1) {
                 return ScriptState.CHOP;
             } else if (localPlayer.getAnimation() == 867) {
                 return ScriptState.CHOPPING;
             }
         }
 
         return ScriptState.RECOVERY;
     }
 
     public void setSettings(ChlenixChopperSettings newSettings) {
         scriptSettings = newSettings;
         bankArea = scriptSettings.getBankArea();
         treeArea = scriptSettings.getTreeArea();
 
         TREE_LOG = scriptSettings.getLogId();
         TREE_OBJECT = scriptSettings.getTreeIds();
 
         treePath = scriptSettings.getTreePath();
         bankPath = new Path[treePath.length];
         for (int p = 0; p < treePath.length; p++) {
             bankPath[p] = treePath[p].reverse();
         }
 
         bankPosition = newSettings.getBankPosition();
         treePosition = newSettings.getTreePosition();
     }
 
     @Override
     public int handle(ScriptState state) {
         switch (state) {
             case BANK:
                 if (bank.isOpen()) {
                     if (inventory.isFull()) {
                         bank.depositAllExcept(Filters.itemId(axeIds));
                         sleep(250, 350);
                         bank.close();
                     }
 
                     timesBanked++;
                     inventoryLogs = 0;
                 } else {
                     GameObject bankBooth = objects.getNearest(Filters.objectId(BANK_BOOTHS));
                     if (bankBooth != null) {
                         if (!camera.isVisible(bankBooth)) {
                             camera.rotateToObject(bankBooth);
                             sleep(800, 1200);
                         }
                         interact(bankBooth, "Bank");
                         sleep(800, 1200);
                     }
                 }
                 break;
 
             case WALK_TO_BANK:
                 if (((System.currentTimeMillis() - lastMove) > 20000L)) {
                     log("Haven't moved for 20 seconds... trying some shit");
                     requestExit();
                 }
                 navigation.navigate(bankPath[random(0, bankPath.length - 1)], 2, NavigationPolicy.MINIMAP);
                 break;
 
             case WALK_TO_TREES:
                 if (((System.currentTimeMillis() - lastMove) > 20000L)) {
                     log("Haven't moved for 20 seconds... trying some shit");
                     requestExit();
                 }
                 navigation.navigate(treePath[random(0, treePath.length - 1)], 2, NavigationPolicy.MINIMAP);
                 break;
 
             case CHOPPING:
                 antiBan(false);
                 detectEnts();
                 checkForBirdsNest();
                 break;
 
             case CHOP:
                 checkForBirdsNest();
 
                 GameObject nearestTree = objects.getNearest(Filters.objectId(TREE_OBJECT));
 
                 if (debugMode) {
                     System.out.println("FOUND TREE! X=" + nearestTree.getLocation().getX() + ", Y=" + nearestTree.getLocation().getY());
                 }
 
                 if (nearestTree != null) {
                     if (!camera.isVisible(nearestTree)) {
                         camera.rotateToObject(nearestTree);
                         sleep(800, 1200);
                     }
                     interact(nearestTree, "Chop");
                     sleep(800, 1200);
                 } else {
                     if (debugMode) {
                         System.out.println("Waiting for trees...");
                     }
                     antiBan(true);
                 }
 
                 break;
 
             case RECOVERY:
                 log("Something went terribly wrong, in RECOVERY state.");
                 break;
 
             case SOLVING_RANDOM:
                 if(!handlingRandom) {
                     handlingRandom = true;
                     if (randomEventNPC != null) {
                         log("The random with ID " + randomEventNPC.getId() + " (" + randomEventNPC.getName() + ")" + " appeared!");
                         if (randomEventNPC.getSpeech() != null) {
                             log("He is saying: \"" + randomEventNPC.getSpeech() + "\"");
                         }
 
                         // Let's handle the NPC!
                         solveRandomNPC(randomEventNPC.getId());
                     }
                     if (randomEventItem != null) {
                         log("The random item with ID " + randomEventItem.getId() + " (" + randomEventItem.toString() + ")" + " is in your inventory!");
 
                         // Let's handle the item!
                         solveRandomItem(randomEventItem.getId());
                     }
                 }
                 break;
 
             case COMBAT:
                 log("We're being attacked! RUN CHLENIX!");
                 while (localPlayer.isInCombat()) {
                     navigation.navigate(bankPath[random(0, treePath.length - 1)], 2, NavigationPolicy.MINIMAP);
                 }
 
             case PAUSED:
                 return random(1000, 2000);
         }
         return random(50, 150);
     }
 
     public boolean waitFor(final Condition condition, final long timeOut) {
         Timer timer = new Timer(timeOut);
         while (timer.isRunning()) {
             if (condition.validate()) {
                 return true;
             }
         }
         return false;
     }
 
     private <T extends Interactable & Hullable> void interact(T obj, String action) {
         Point objPoint;
         try {
             objPoint = obj.hullPoint(obj.hull());
         } catch (Exception e) {
             return;
         }
         mouse.move(objPoint.x, objPoint.y);
 
         Polygon p = obj.hull();
         if (p != null && !p.contains(getContext().getBot().getInputHandler().getPosition())) {
             return;
         }
         sleep(100, 150);
 
         int index = menu.getIndex(action);
         if (index != -1) {
             if (index == 0 && random(100) < 50) {
                 mouse.click();
             } else {
                 mouse.click(true);
                 index = menu.getIndex(action);
                 Point menuPoint = menu.getClickPoint(index);
                 sleep(100, 150);
                 mouse.move(menuPoint.x, menuPoint.y);
                 mouse.click();
             }
         }
     }
 
     @Override
     public boolean init() {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 frame.setContentPane(GUI.getContentPane());
                 frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                 frame.addWindowListener(new WindowAdapter() {
                     public void windowClosing(WindowEvent e) {
                         waitForGUI = false;
                     }
                 });
                 frame.setSize(268, 100);
                 frame.setLocationRelativeTo(null);
                 frame.setResizable(false);
                 frame.setVisible(true);
             }
         });
 
         while (waitForGUI) {
             sleep(500, 1000);
         }
 
         if (game.getGameState() == Game.GameState.LOGIN) {
             waitFor(new Condition() {
                 @Override
                 public boolean validate() {
                     return game.getGameState() == Game.GameState.INGAME;
                 }
             }, random(6000, 7000));
         }
 
         startExperience = skills.getExperience(Skills.WOODCUTTING);
 
         antiBanInterval = random(10, 50) * 1000L;
         antiBanCountdown = antiBanInterval / 1000L;
 
         inventoryLogs = inventory.getCount(false, TREE_LOG);
 
         for (int i = 0; i < 25; i++) {
             camera.adjustPitch(1);
         }
 
         return true;
     }
 
     @Override
     public void close() {
         log("Chlenix stopped chopping! Going back to pants.");
     }
 
     private void antiBan(boolean waitingForTrees) {
         if ((System.currentTimeMillis() > lastAntiBan + antiBanInterval) && (!localPlayer.isMoving())) {
             int r = random(1, 20);
 
             if (waitingForTrees)
                 antiBanStatus = "Antiban - Prance around";
             else {
                 antiBanStatus = "Antiban - ";
             }
 
             this.antiBanInterval = (random(10, 50) * 1000L);
             lastAntiBan = System.currentTimeMillis();
             antiBanCountdown = (antiBanInterval / 1000L);
             if (!waitingForTrees) {
                 switch (r) {
                     case 1:
                         antiBanStatus += "Check Exp";
                         game.openTab(Tabs.STATS);
                         sleep(800, 1000);
                         mouse.move(679 + random(15, 45), 390 - random(8, 19));
                         sleep(3500, 5500);
                         game.openTab(Tabs.INVENTORY);
                         sleep(800, 1000);
                         break;
 
                     case 2:
                         antiBanStatus += "Camera Pitch";
                         camera.adjustPitch(random(75, 100));
                         sleep(300, 500);
                         break;
 
                     case 3:
                         antiBanStatus += "Mouse";
                         mouse.move(random(7, 750), random(7, 400));
                         sleep(300, 500);
                         break;
 
                     case 4:
                         Player randomPlayer = getNearestPlayer();
 
                         if (randomPlayer != null) {
                             if ((camera.isVisible(randomPlayer)) && (!randomPlayer.getName().equalsIgnoreCase(localPlayer.getName()))) {
                                 antiBanStatus += "Inspect Player";
                                 mouse.hover(randomPlayer);
                                 mouse.click(true);
                                 sleep(800, 1200);
                                 mouse.move(random(0, mouse.getPosition().x + random(10, 100)), random(0, mouse.getPosition().y));
                             }
                         }
                         break;
 
                     case 5:
                         antiBanStatus += "Camera Angle";
                         camera.rotateAngleTo(-random(10, 175));
                         sleep(300, 500);
                         break;
 
                     case 6:
                         antiBanStatus += "Camera Angle";
                         camera.rotateAngleTo(random(10, 175));
                         sleep(300, 500);
                         break;
 
                     case 7:
                         antiBanStatus += "Camera Pitch";
                         sleep(300, 500);
                         break;
 
                     case 8:
                         int shouldTalk = random(1, 2);
                         if (shouldTalk == 1) {
                             antiBanStatus += "Talking with people";
                             String randomString = randomChat[random(0, randomChat.length)];
                             if (randomString.contains("{wcLevel}")) {
                                 randomString = replace(randomString, new String[]{"{wcLevel}"}, new String[]{Integer.toString(skills.getLevel(Skills.WOODCUTTING) + 1)});
                             }
                             keyboard.type(randomString, true);
                         } else {
                             antiBanStatus += "Not talking";
                         }
                         break;
 
                     case 9:
 
                     case 10:
                         antiBanStatus += "Face North";
                         camera.rotateAngleTo(random(345, 359));
                         camera.adjustPitch(random(90, 100));
                         sleep(300, 500);
                         break;
 
                     case 11:
                         antiBanStatus = "";
                         moveMouseOffScreen(true);
                         sleep(500, 800);
                         break;
 
                     default:
                         antiBanStatus += "Skip";
                         sleep(500, 700);
                 }
             } else {
                 navigation.navigate(navigation.deviate(treePosition, 2, 2), NavigationPolicy.MIXED);
             }
             wiggleMouse();
         }
     }
 
     private String replace(String string, String[] toFind, String[] toReplace) {
         if (toFind.length != toReplace.length) {
             throw new IllegalArgumentException("Arrays must be of the same length.");
         }
         for (int i = 0; i < toFind.length; i++) {
             string = string.replace(toFind[i], toReplace[i]);
         }
         return string;
     }
 
     private void moveMouseOffScreen(boolean report) {
         int r = random(1, 100);
 
         if (report) {
             antiBanStatus = "Antiban - Move Off Screen";
             r = 60;
         }
 
         if ((r > 50) && (r < 80)) {
             switch (random(1, 4)) {
                 case 1:
                     mouse.move(-10, random(1, 510));
                     break;
                 case 2:
                     mouse.move(770, random(1, 510));
                     break;
                 case 3:
                     mouse.move(random(1, 760), 510);
                     break;
             }
         }
     }
 
     private void wiggleMouse() {
         int r = random(1, 100);
         if ((r > 35) && (r < 50)) {
             for (int i = 0; i < random(4, 7); i++)
                 mouse.move(random(10, 490), random(10, 250));
         }
     }
 
     private void updateTimeData() {
         if (System.currentTimeMillis() >= this.startTime + 1000L) {
             this.runningTime += 1L;
             if (this.antiBanCountdown > 0L) {
                 this.antiBanCountdown -= 1L;
             }
             this.startTime = System.currentTimeMillis();
         }
 
         int mins;
         int hours;
         int seconds;
 
         seconds = (int) runningTime;
         mins = seconds / 60;
         hours = mins / 60;
 
         seconds -= mins * 60;
         mins -= hours * 60;
 
         DecimalFormat format = new DecimalFormat("00");
 
         formattedTime = (format.format(hours) + ":" + format.format(mins) + ":" + format.format(seconds));
     }
 
     private void updateRenderData() {
         // Chopped down logs
         if (inventory.getCount(false, TREE_LOG) > inventoryLogs) {
             logsCut++;
             inventoryLogs = inventory.getCount(false, TREE_LOG);
         }
 
         // Experience based variables
         experienceGained = skills.getExperience(Skills.WOODCUTTING) - startExperience;
         experiencePerHour = (experienceGained / (runningTime / 3600.0D));
         timeTillLevel = skills.getExperienceToNextLevel(Skills.WOODCUTTING) / experiencePerHour * 3600.0D;
         formattedTimeTillLevel = formatClock((int) timeTillLevel);
         if (logsCut != 0) {
             logsTillNextLevel = (skills.getExperienceToNextLevel(Skills.WOODCUTTING) / (experienceGained / logsCut)) + 1;
         }
     }
 
     @Override
     public void render(Graphics2D g) {
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
         // Let's update the variables before showing them
         updateTimeData();
         updateRenderData();
 
         drawProgressBar(g, 4, 323, 512, 15, Color.WHITE, Color.GREEN, 200, skills.getPercentageToNextLevel(Skills.WOODCUTTING));
         g.setFont(new Font("Segoe UI", 0, 13));
         g.setColor(Color.BLACK);
         g.drawString(skills.getPercentageToNextLevel(Skills.WOODCUTTING) + "% to " + (skills.getLevel(Skills.WOODCUTTING) + 1), 240, 335);
 
         g.setFont(new Font("Calibri", 0, 12));
         g.setColor(new Color(204, 204, 204));
         g.drawString("Time: ", 8, 256);
         g.drawString("Status: ", 8, 270);
         g.drawString("Logs Cut: ", 8, 284);
         g.drawString("Experience: ", 8, 298);
         g.drawString("Exp/Hour: ", 8, 312);
         g.setColor(Color.WHITE);
         g.drawString(formattedTime, 43, 256);
         g.drawString(antiBanStatus + " | " + determine(), 49, 270);
         g.drawString(numberFormat.format(logsCut) + " | " + logsTillNextLevel + " | " + timesBanked, 65, 284);
         g.drawString(numberFormat.format(experienceGained), 80, 298);
         g.drawString(numberFormat.format(experiencePerHour) + " | " + formattedTimeTillLevel, 70, 312);
 
         // Draw cursor
         Point mousePoint = mouse.getPosition();
         g.setColor(Color.RED);
         g.drawLine(mousePoint.x - 20, mousePoint.y, mousePoint.x + 20, mousePoint.y);
         g.drawLine(mousePoint.x, mousePoint.y - 20, mousePoint.x, mousePoint.y + 20);
     }
 
     private static void drawProgressBar(Graphics2D g, final int x, final int y, final int width, final int height, final Color main, final Color progress,
                                        final int alpha, final int percentage) {
 
         g.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
         final GradientPaint base = new GradientPaint(x, y, new Color(200, 200, 200, alpha), x, y + height, main);
         final GradientPaint overlay = new GradientPaint(x, y, new Color(200, 200, 200, alpha), x, y + height, progress);
         if (height > width) {
             g.setPaint(base);
             g.fillRect(x, y, width, height);
             g.setPaint(overlay);
             g.fillRect(x,
                     y + (height - (int) (height * (percentage / 100.0D))),
                     width, (int) (height * (percentage / 100.0D)));
         } else {
             g.setPaint(base);
             g.fillRect(x, y, width, height);
             g.setPaint(overlay);
             g.fillRect(x, y, (int) (width * (percentage / 100.0D)), height);
         }
         g.setColor(Color.BLACK);
         g.drawRect(x, y, width, height);
     }
 
     private String formatClock(int s) {
         int mins;
         int hours;
         int seconds;
 
         seconds = s;
         mins = seconds / 60;
         hours = mins / 60;
 
         seconds -= mins * 60;
         mins -= hours * 60;
 
         DecimalFormat format = new DecimalFormat("00");
 
         return format.format(hours) + ":" + format.format(mins) + ":" + format.format(seconds);
     }
 
     private void checkForBirdsNest() {
         for (GroundItem nest : groundItems.getAll(Filters.groundItemId(birdNests), treeArea)) {
             if (nest != null) {
                 if (debugMode) {
                     System.out.println("Found birds nest! Picking up...");
                 }
                 interact(nest, "Take");
                 sleep(500, 1100);
             }
         }
     }
 
     private void detectEnts() {
         if (isTreeEnt()) {
             if (debugMode) {
                 System.out.println("Ent detected!");
             }
             navigation.navigate(treeArea.getCenter(), NavigationPolicy.MIXED);
             sleep(500, 1100);
         }
     }
 
     private boolean isTreeEnt() {
         Actor a = localPlayer.getInteracting();
         return a!= null && a instanceof Npc;
     }
 
     private <T> boolean contains(T[] arr, T obj) {
         for (T t : arr) {
             if (t == null) continue;
             if (t.equals(obj))
                 return true;
         }
         return false;
     }
 
     private Player getNearestPlayer() {
         Player nearestPlayer = null;
         for (Player p : players.getPlayers(new Filter<Player>() {
             public boolean accept(Player player) {
                 if ((player.getLocation().distanceTo(localPlayer.getLocation()) < 7) && !(player.getName().equalsIgnoreCase(localPlayer.getName().toLowerCase()))) {
                     return true;
                 }
                 return false;
             }
         })) {
             if (nearestPlayer != null) {
                 if ((localPlayer.getLocation().distanceTo(p.getLocation())) < (localPlayer.getLocation().distanceTo(nearestPlayer.getLocation()))) {
                     nearestPlayer = p;
                 }
             } else {
                 nearestPlayer = p;
             }
         }
         return nearestPlayer;
     }
 
     private Tile getCenterOfArea(Area a) {
         return new Tile((a.getBottomLeft().getX() + a.getTopRight().getX()) / 2, (a.getBottomLeft().getY() + a.getTopRight().getY()) / 2);
     }
 
     private Tile getBestWalkableTile() {
         Tile bestTile = null;
         for (int i = 0; i < treePath.length - 1; i++) {
             for (Tile t : treePath[i].getTiles()) {
                 if (bestTile != null) {
                     if ((localPlayer.getLocation().distanceTo(t) < localPlayer.getLocation().distanceTo(bestTile))) {
                         bestTile = t;
                     }
                 } else {
                     bestTile = t;
                 }
             }
         }
         return bestTile;
     }
 
     private boolean getRandomNPCHere() {
         Npc randomNPC = npcs.getNearest(randomEventNPCs);
         if (randomNPC != null && (localPlayer.getLocation().distanceTo(randomNPC.getLocation()) <= 2)) {
             randomEventNPC = randomNPC;
             return true;
         }
         return false;
     }
 
     private boolean getRandomItemHere() {
         if (inventory.contains(Filters.itemId(randomEventItems))) {
             randomEventItem = inventory.getItem(randomEventItems);
             return true;
         }
         return false;
     }
 
     public static void finishedRandom() {
         handlingRandom = false;
     }
 
     // TODO: Remove when added into API
     private void clickToContinue() {
         Point randPoint = Methods.getRandomPointNear(new Point(305, 449), 7);
         mouse.move(randPoint.x, randPoint.y);
         sleep(200, 400);
         mouse.click(true);
     }
 
     // TODO: Merge into one main solveRandom() method after debugging -- use real RandomEvent
     private void solveRandomNPC(int npcId) {
         switch (npcId) {
             case 2476:
                 // Rick Turpentine
                 break;
 
             case 3117:
                 // Sandwich lady
                 break;
 
             case 2539:
                 // Cap'n Hand
                 break;
 
             case 409:
                 // Genie
                 break;
 
             case 410:
                 // Old Man
                 break;
 
             case 2540:
                 // Dr Jekyll
                 break;
 
             case 411:
                 // Swarm
                 break;
 
             case 2470:
                 // Frog
                 break;
 
             case 4375:
                 // Guard
                 break;
 
             case 956:
                 // Dwarf
                 break;
 
             case 407:
                 // Strange Plant
                 break;
 
             case 4416:
                 // Bee keeper
                 break;
         }
     }
 
     // TODO: Merge into one main solveRandom() method after debugging -- use real RandomEvent
     private void solveRandomItem(int itemId) {
         switch (itemId) {
             case 9004:
                 // Security Handbook TODO: Change to int when .getItem is fixed
                 inventory.interact(inventory.indexOf(inventory.getItem(new int[]{ itemId })), "Drop");
                 sleep(250, 500);
                 finishedRandom();
                 break;
 
             case 3063:
                 // Mystery Box
                 MysteryBox mb = new MysteryBox(getContext());
                 mb.solve();
                 break;
         }
     }
 
     public static enum ScriptState {
         BANK,
         CHOP,
         WALK_TO_BANK,
         RECOVERY,
         WALK_TO_TREES,
         CHOPPING,
         PAUSED,
         SOLVING_RANDOM,
         COMBAT
     }
 
     public static enum Tree {
         WILLOW,
         MAPLE,
         YEW,
         MAGIC
     }
 
     public class ChlenixChopperGUI extends JFrame implements ActionListener {
 
         ChlenixChopper context;
         private static final long serialVersionUID = 1L;
 
         JButton btnStart;
         JComboBox comboTrees;
         JLabel lblTreesToChop;
 
         public ChlenixChopperGUI(ChlenixChopper script) {
             context = script;
 
             setFont(new Font("Segoe UI", Font.PLAIN, 12));
             setTitle("ChlenixChopper Settings");
             setDefaultCloseOperation(EXIT_ON_CLOSE);
             setLayout(null);
 
             lblTreesToChop = new JLabel("Trees to chop:");
             lblTreesToChop.setFont(new Font("Segoe UI", Font.BOLD, 11));
             lblTreesToChop.setBounds(10, 13, 75, 14);
             add(lblTreesToChop);
 
             comboTrees = new JComboBox();
             comboTrees.setFont(new Font("Segoe UI", Font.PLAIN, 11));
             comboTrees.setModel(new DefaultComboBoxModel(new String[]{"Willows", "Maples", "Yews", "Magics"}));
             comboTrees.setBounds(95, 11, 159, 20);
             add(comboTrees);
 
             btnStart = new JButton("Start");
             btnStart.addActionListener(this);
             btnStart.setFont(new Font("Segoe UI", Font.PLAIN, 11));
             btnStart.setBounds(10, 38, 244, 23);
             add(btnStart);
         }
 
         @Override
         public void actionPerformed(java.awt.event.ActionEvent e) {
             if (e.getSource().equals(btnStart)) {
                 switch (comboTrees.getSelectedItem().toString()) {
                     case "Willows":
                         context.setSettings(new ChlenixChopperSettings(WILLOW_TREE, seersBankArea, WILLOW_LOG, Tree.WILLOW));
                         break;
                     case "Maples":
                         context.setSettings(new ChlenixChopperSettings(MAPLE_TREE, seersBankArea, MAPLE_LOG, Tree.MAPLE));
                         break;
                     case "Yews":
                         context.setSettings(new ChlenixChopperSettings(YEW_TREE, seersBankArea, YEW_LOG, Tree.YEW));
                         break;
                     case "Magics":
                         context.setSettings(new ChlenixChopperSettings(MAGIC_TREE, seersBankArea, MAGIC_LOG, Tree.MAGIC));
                         break;
                 }
                context.waitForGUI = false;
             }
         }
     }
 
     public class ChlenixChopperSettings {
         private int[] treeIds;
         private Area bankArea;
         private int logId;
         private Tree treeType;
         private Tile bankPosition;
         private Tile treePosition;
 
         public int[] getTreeIds() {
             return this.treeIds;
         }
 
         public Area getTreeArea() {
             switch (getTreeType()) {
                 case WILLOW:
                     return new Area(new Tile(2704, 3504), new Tile(2720, 3514));
 
                 case MAPLE:
                     // TODO: Replace with real MAPLE area
                     return new Area(new Tile(2704, 3504), new Tile(2720, 3514));
 
                 case YEW:
                     // TODO: Replace with real YEW area
                     return new Area(new Tile(2704, 3504), new Tile(2720, 3514));
 
                 case MAGIC:
                     // TODO: Replace with real MAGIC area
                     return new Area(new Tile(2704, 3504), new Tile(2720, 3514));
             }
             return null;
         }
 
         public Area getBankArea() {
             return this.bankArea;
         }
 
         public int getLogId() {
             return this.logId;
         }
 
         public Path[] getTreePath() {
             switch (getTreeType()) {
                 case WILLOW:
                     return  new Path[] {new Path(bankPosition, new Tile(2717, 3499), new Tile(2709, 3507), new Tile(2710, 3505), treePosition),
                             new Path(bankPosition, new Tile(2718, 3498), new Tile(2708, 3506), new Tile(2709, 3505), treePosition),
                             new Path(bankPosition, new Tile(2716, 3497), new Tile(2707, 3505), new Tile(2711, 3508), treePosition)
                     };
 
                 case MAPLE:
                     // TODO: Replace with real MAPLE path
                     return  new Path[] {new Path(bankPosition, new Tile(2717, 3499), new Tile(2709, 3507), new Tile(2710, 3505), treePosition),
                             new Path(bankPosition, new Tile(2718, 3498), new Tile(2708, 3506), new Tile(2709, 3505), treePosition),
                             new Path(bankPosition, new Tile(2716, 3497), new Tile(2707, 3505), new Tile(2711, 3508), treePosition)
                     };
 
                 case YEW:
                     // TODO: Replace with real YEW path
                     return  new Path[] {new Path(bankPosition, new Tile(2717, 3499), new Tile(2709, 3507), new Tile(2710, 3505), treePosition),
                             new Path(bankPosition, new Tile(2718, 3498), new Tile(2708, 3506), new Tile(2709, 3505), treePosition),
                             new Path(bankPosition, new Tile(2716, 3497), new Tile(2707, 3505), new Tile(2711, 3508), treePosition)
                     };
 
                 case MAGIC:
                     // TODO: Replace with real MAGIC path
                     return  new Path[] {new Path(bankPosition, new Tile(2717, 3499), new Tile(2709, 3507), new Tile(2710, 3505), treePosition),
                             new Path(bankPosition, new Tile(2718, 3498), new Tile(2708, 3506), new Tile(2709, 3505), treePosition),
                             new Path(bankPosition, new Tile(2716, 3497), new Tile(2707, 3505), new Tile(2711, 3508), treePosition)
                     };
             }
             return null;
         }
 
         public Tree getTreeType() {
             return this.treeType;
         }
         public Tile getBankPosition() {
             return this.bankPosition;
         }
         public Tile getTreePosition() {
             return this.treePosition;
         }
 
         public ChlenixChopperSettings(int[] treeIds, Area bankArea, int logId, Tree treeType) {
             this.logId = logId;
             this.treeIds = treeIds;
             this.bankArea = bankArea;
             this.treeType = treeType;
             this.bankPosition = getCenterOfArea(bankArea);
             this.treePosition = getCenterOfArea(treeArea);
         }
     }
 }
