 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package zombiefu.util;
 
 import jade.core.Actor;
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Direction;
 import jade.util.Guard;
 import java.awt.Color;
 import java.io.IOException;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import zombiefu.exception.NoDirectionGivenException;
 import zombiefu.player.Player;
 import zombiefu.builder.ItemBuilder;
 import zombiefu.items.ConsumableItem;
 import zombiefu.items.Item;
 import zombiefu.level.Level;
 import zombiefu.player.Attribute;
 import zombiefu.player.Discipline;
 import zombiefu.ui.ZombieFrame;
 
 /**
  *
  * @author tomas
  */
 public class ZombieGame {
 
     private static ZombieSettings settings;
     private static ZombieFrame frame;
     private static Player player;
     private static Level globalmap;
 
     public static void createGame(String[] args, String name) {
         settings = new ZombieSettings(args, "res");
         frame = new ZombieFrame(name);
     }
 
     public static ZombieSettings getSettings() {
         return settings;
     }
 
     public static char showStaticImage(String file) {
         try { //bevor fortgefahren wird
             ColoredChar[][] start = ConfigHelper.getImage(file);
             frame.mainTerm().clearBuffer();
             for (int x = 0; x < frame.mainTerm().DEFAULT_COLS; x++) {
                 for (int y = 0; y < frame.mainTerm().DEFAULT_ROWS; y++) {
                     if (y >= start.length || x >= start[0].length) {
                         frame.mainTerm().bufferChar(x, y,
                                 ColoredChar.create(' '));
                     } else {
                         frame.mainTerm().bufferChar(x, y, start[y][x]);
                     }
                 }
             }
             frame.mainTerm().refreshScreen();
         } catch (IOException ex) {
         }
 
         return askPlayerForKey();
     }
 
     public static void initialize() {
         Discipline discipline = askPlayerForDiscipline();
         globalmap = ConfigHelper.getLevelByName(settings.globalMap);
         
         // Attribute laden.
         HashMap<Attribute,Integer> atts = new HashMap<Attribute,Integer>();
         for(Attribute att: Attribute.values()) {
             Integer setting = settings.playerAttributes.get(att); 
             if(setting == null) {
                 atts.put(att, discipline.getBaseAttribute(att));
            } else {
                atts.put(att, setting);
             }
         }
         
         // Spieler erzeugen        
         player = new Player(settings.playerChar, settings.playerName, discipline, atts);
         
         // StartItems erzeugen
         Set<Actor> items = ConfigHelper.decodeITM(settings.playerInventar == null ? discipline.getItems(): settings.playerInventar);
         for (Actor a : items) {
             Guard.verifyState(a instanceof Item);
             player.obtainItem((Item) a);
         }
         
         // Gib Spieler Geld
         player.addMoney(discipline.getMoney());
         
         // Setze Spieler in Welt
         player.changeWorld(ConfigHelper.getLevelByName(settings.playerStartMap));
         if (player.world().insideBounds(settings.playerStartCoord)) {
             player.setPos(settings.playerStartCoord);
         }
 
         frame.mainTerm().registerCamera(player, 40, 17);
     }
 
     public static void setTopFrameContent(String s) {
         frame.topTerm().clearBuffer();
         if (s != null) {
             frame.topTerm().bufferString(0, 0, s);
         }
         frame.topTerm().refreshScreen();
     }
 
     public static char askPlayerForKeyWithMessage(String s) {
         refreshMainFrame();
         setTopFrameContent(s);
         char key = 0;
         try {
             key = frame.mainTerm().getKey();
         } catch (InterruptedException ex) {
             Guard.verifyState(false);
         }
 
         setTopFrameContent(null);
         return key;
     }
 
     public static void newMessage(String s) {
         askPlayerForKeyWithMessage(s);
     }
 
     public static void refreshMainFrame() {
         frame.mainTerm().clearBuffer();
         frame.mainTerm().bufferCameras();
         frame.mainTerm().refreshScreen();
     }
 
     public static void refreshBottomFrame() {
         frame.bottomTerm().clearBuffer();
         frame.bottomTerm().bufferString(
                 0,
                 0,
                 "Waffe: " + player.getActiveWeapon().getName() + " ("
                 + player.getActiveWeapon().getMunitionToString()
                 + " / " + player.getActiveWeapon().getDamage() + ") "
                 + " | HP: " + player.getHealthPoints() + "/"
                 + player.getMaximalHealthPoints() + " | A: "
                 + player.getAttackValue() + " | D: "
                 + player.getDefenseValue() + " | I: "
                 + player.getDexterity());
         frame.bottomTerm().bufferString(
                 0,
                 1,
                 "Ort: " + ((Level) player.world()).getName() + "(" + player.pos().x() + "|" + player.pos().y() + ")"
                 + " | € " + player.getMoney() + " | ECTS "
                 + player.getECTS() + " | Sem " + player.getSemester() + " | GodMode: "
                 + (player.isGod() ? "an" : "aus"));
         frame.bottomTerm().bufferCameras();
         frame.bottomTerm().refreshScreen();
     }
 
     public static void startGame() {
         while (!player.expired()) {
             refreshMainFrame();
             refreshBottomFrame();
             player.world().tick();
         }
         System.exit(0);
     }
 
     public static void showHelp() {
         showStaticImage("help");
         refreshMainFrame();
     }
 
     public static Player getPlayer() {
         return player;
     }
 
     public static char askPlayerForKey() {
         try {
             return frame.mainTerm().getKey();
         } catch (InterruptedException ex) {
             Guard.verifyState(false);
             return 0;
         }
     }
 
     public static Direction askPlayerForDirection()
             throws NoDirectionGivenException {
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
 
     public static String askPlayerForItemInInventar() {
         String output = null;
         HashMap<String, ArrayList<ConsumableItem>> inventar = getPlayer().getInventar();
         ArrayList<String[]> inventarList = new ArrayList<String[]>();
         for (String s : inventar.keySet()) {
             int i = inventar.get(s).size();
             if (i == 1) {
                 inventarList.add(new String[]{s, inventar.get(s).get(0).face() + " " + s});
             } else if (i > 1) {
                 inventarList.add(new String[]{s, inventar.get(s).get(0).face() + " " + s + " (" + i + "x)"});
             }
         }
         if (inventarList.isEmpty()) {
             ZombieGame.newMessage("Inventar ist leer.");
             return null;
         }
         frame.mainTerm().clearBuffer();
         frame.mainTerm().bufferString(0, 0, "Inventarliste:");
         for (int i = 0; i < inventarList.size(); i++) {
             String[] s = inventarList.get(i);
             frame.mainTerm().bufferString(
                     0,
                     2 + i,
                     "[" + ((char) (97 + i)) + "] " + s[1]);
         }
         frame.mainTerm().refreshScreen();
         int key = ((int) ZombieGame.askPlayerForKey()) - 97;
         if (key >= 0 && key <= 25 && key < inventar.size()) {
             output = inventarList.get(key)[0];
         }
         refreshMainFrame();
         return output;
     }
 
     public static ItemBuilder askPlayerForItemToBuy(HashMap<ItemBuilder, Integer> itemMap) {
 
         if (itemMap.isEmpty()) {
             ZombieGame.newMessage("Dieser Shop hat keine Artikel.");
             return null;
         }
 
         ItemBuilder output = null;
 
         ArrayList<ItemBuilder> itemSet = new ArrayList<ItemBuilder>();
         for (ItemBuilder it : itemMap.keySet()) {
             itemSet.add(it);
         }
 
         frame.mainTerm().clearBuffer();
         frame.mainTerm().bufferString(0, 0, "Artikel:");
         for (int i = 0; i < itemSet.size(); i++) {
             frame.mainTerm().bufferString(
                     0,
                     2 + i,
                     "[" + ((char) (97 + i)) + "] " + itemSet.get(i).face() + " - "
                     + itemSet.get(i).getName() + " (Preis: " + itemMap.get(itemSet.get(i)) + ")");
         }
         frame.mainTerm().refreshScreen();
         int key = ((int) ZombieGame.askPlayerForKey()) - 97;
         if (key >= 0 && key <= 25 && key < itemMap.size()) {
             output = itemSet.get(key);
         }
         refreshMainFrame();
         return output;
     }
 
     public static Discipline askPlayerForDiscipline() {
         char alpha = showStaticImage("discipline");
         Discipline output;
 
         switch (alpha) {
             case 'a':
                 output = Discipline.POLITICAL_SCIENCE;
                 break;
             case 'b':
                 output = Discipline.COMPUTER_SCIENCE;
                 break;
             case 'c':
                 output = Discipline.MEDICINE;
                 break;
             case 'd':
                 output = Discipline.PHILOSOPHY;
                 break;
             case 'e':
                 output = Discipline.PHYSICS;
                 break;
             case 'f':
                 output = Discipline.BUSINESS;
                 break;
             case 'g':
                 output = Discipline.CHEMISTRY;
                 break;
             case 'h':
                 output = Discipline.SPORTS;
                 break;
             case 'i':
                 output = Discipline.MATHEMATICS;
                 break;
             default:
                 output = askPlayerForDiscipline();
         }
         // Quick fix. TODO: sebastian denkt sich was aus.
         Guard.argumentIsNotNull(output);
         return output;
     }
 
     public static Attribute askPlayerForAttrbuteToRaise() {
         char alpha = showStaticImage("askForAttribute");
         Attribute output;
 
         switch (alpha) {
             case 'a':
                 output = Attribute.MAXHP;
                 break;
             case 'b':
                 output = Attribute.ATTACK;
                 break;
             case 'c':
                 output = Attribute.DEFENSE;
                 break;
             case 'd':
                 output = Attribute.DEXTERITY;
                 break;
             default:
                 output = askPlayerForAttrbuteToRaise();
         }
         // Quick fix. TODO: sebastian denkt sich was aus.
         Guard.argumentIsNotNull(output);
         System.out.println(output);
         return output;
     }
 
     public static File getResourceDirectory(String identifier) {
         return settings.paths.get(identifier);
     }
 
     public static Level getGlobalMap() {
         return globalmap;
     }
 
     public static void endGame() {
         // TODO: Im Endscreen dynamisch Informationen anzeigen.
         showStaticImage("endscreen");
         System.exit(0);
     }
 }
