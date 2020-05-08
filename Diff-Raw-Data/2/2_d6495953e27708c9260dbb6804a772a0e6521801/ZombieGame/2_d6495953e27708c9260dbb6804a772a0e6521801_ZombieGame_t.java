 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package zombiefu.util;
 
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Direction;
 import java.awt.Color;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import zombiefu.creature.Door;
 import zombiefu.creature.Player;
 import zombiefu.items.ConsumableItem;
 import zombiefu.items.Item;
 import zombiefu.items.KeyCard;
 import zombiefu.items.Waffe;
 import zombiefu.level.Level;
 import zombiefu.ui.ZombieFrame;
 import zombiefu.util.ConfigHelper;
 
 /**
  *
  * @author tomas
  */
 public class ZombieGame {
 
     private static final String sourceDir = "src/sources/";
     private static final String screenDir = "src/sources/screens/";
     private static final String itemDir = "src/sources/items/";
     private static final String mapDir = "src/sources/maps/";
     private static KeyEdit settings;
     private static ZombieFrame frame;
     private static Player player;
 
     public static void createGame(String[] args, String name) {
        settings = new KeyEdit(args, "src/sources");
         frame = new ZombieFrame(name);
     }
 
     public static KeyEdit getSettings() {
         return settings;
     }
 
     public static void createPlayer() {
         ArrayList<Waffe> waffen = new ArrayList<Waffe>();
         waffen.add(ConfigHelper.newWaffeByName("SuperFist"));
         player = new Player(ColoredChar.create('\u263B',
                 Color.decode("0x7D26CD")), settings.name, 100, 10, 10, 10,
                 waffen);
     }
 
     public static void showStaticImage(String file) {
         try {
             ColoredChar[][] start = ConfigHelper.getImage(file);
             frame.mainTerm().clearBuffer();
             for (int x = 0; x < frame.mainTerm().DEFAULT_COLS; x++) {
                 for (int y = 0; y < frame.mainTerm().DEFAULT_ROWS; y++) {
                     if (y >= start.length || x >= start[0].length) {
                         frame.mainTerm().bufferChar(x, y, ColoredChar.create(' '));
                     } else {
                         frame.mainTerm().bufferChar(x, y, start[y][x]);
                     }
                 }
             }
             frame.mainTerm().refreshScreen();
             frame.mainTerm().getKey();
         } catch (IOException ex) {
         } catch (InterruptedException ex) {
         }
     }
 
     public static void initialize() {
         Level firstLevel = ConfigHelper.getFirstLevel();
         firstLevel.addActor(player);
         firstLevel.fillWithEnemies();
         frame.mainTerm().registerCamera(player, 40, 17);
     }
 
     public static void setTopFrameContent(String s) {
         frame.topTerm().clearBuffer();
         if (s != null) {
             frame.topTerm().bufferString(0, 0, s);
         }
         frame.topTerm().refreshScreen();
     }
 
     public static void newMessage(String s) {
         refreshMainFrame();
         setTopFrameContent(s);
         char key = 0;
         try {
             key = frame.mainTerm().getKey();
         } catch (InterruptedException ex) {
         }
         setTopFrameContent(null);
     }
 
     public static void refreshMainFrame() {
         frame.mainTerm().clearBuffer();
         frame.mainTerm().bufferCameras();
         frame.mainTerm().refreshScreen();
     }
 
     public static void refreshBottomFrame() {
         frame.bottomTerm().clearBuffer();
         frame.bottomTerm().bufferString(0,
                 0,
                 "Waffe: " + player.getActiveWeapon().getName() + " ("
                 + player.getActiveWeapon().getDamage() + ") " + " | HP: "
                 + player.getHealthPoints() + "/" + player.getMaximalHealthPoints() + " | A: "
                 + player.getAttackValue() + " | D: " + player.getDefenseValue() + " | I: "
                 + player.getIntelligenceValue());
         frame.bottomTerm().bufferString(0,
                 1,
                 "Coord: (" + player.pos().x() + "|" + player.pos().y() + ")"
                 + " | € " + player.getMoney() + " | ECTS " + player.getECTS() + " | Sem "
                 + player.getSemester());
         frame.bottomTerm().bufferCameras();
         frame.bottomTerm().refreshScreen();
     }
 
     public static void startGame() {
         while (!player.expired()) {
             refreshMainFrame();
             refreshBottomFrame();
             player.world().tick();
         }
     }
 
     public static Player getPlayer() {
         return player;
     }
 
     public static char askPlayerForKey() throws InterruptedException {
         return frame.mainTerm().getKey();
     }
 
     public static Direction askPlayerForDirection() throws NoDirectionGivenException {
         setTopFrameContent("Bitte gib die Richtung an.");
         Direction d = null;
         try {
             d = Direction.keyToDir(frame.mainTerm().getKey());
         } catch (InterruptedException ex) {
         }
         setTopFrameContent(null);
         if (d == null || d == Direction.ORIGIN) {
             throw new NoDirectionGivenException();
         }
         return d;
     }
 
     public static ConsumableItem askPlayerForItem() {
         ConsumableItem output = null;
         if (player.getInventar().isEmpty()) {
             ZombieGame.newMessage("Inventar ist leer.");
             return null;
         }
         frame.mainTerm().clearBuffer();
         frame.mainTerm().bufferString(0, 0, "Inventarliste:");
         for (int i = 0; i < player.getInventar().size(); i++) {
             Item it = player.getInventar().get(i);
             frame.mainTerm().bufferString(
                     0,
                     2 + i,
                     "[" + ((char) (97 + i)) + "] " + it.face() + " - "
                     + it.getName());
         }
         frame.mainTerm().refreshScreen();
         try {
             int key = ((int) ZombieGame.askPlayerForKey()) - 97;
             if (key >= 0 && key <= 25 && key < player.getInventar().size()) {
                 output = player.getInventar().get(key);
             }
         } catch (InterruptedException ex) {
         }
         refreshMainFrame();
         return output;
     }
 
     public static String getSourceDirectory() {
         return sourceDir;
     }
 
     public static String getItemDirectory() {
         return itemDir;
     }
 
     public static String getMapDirectory() {
         return mapDir;
     }
 
     public static String getScreenDirectory() {
         return screenDir;
     }
 
     public static void endGame() {
         // TODO: Im Endscreen dynamisch Informationen anzeigen.
         showStaticImage("endscreen");
         System.exit(0);
     }
 }
