 package zombiefu.util;
 
 import jade.core.Actor;
 import jade.core.World;
 import jade.util.Guard;
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Coordinate;
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import zombiefu.actor.Door;
 import zombiefu.human.Shop;
 import zombiefu.itembuilder.HealingItemBuilder;
 import zombiefu.itembuilder.ItemBuilder;
 import zombiefu.itembuilder.WaffenBuilder;
 import zombiefu.items.Item;
 import zombiefu.items.KeyCard;
 import zombiefu.actor.Teleporter;
 import zombiefu.items.Waffe;
 import zombiefu.items.Waffentyp;
 import zombiefu.level.Level;
 import zombiefu.mapgen.RoomBuilder;
 
 public class ConfigHelper {
 
     private static HashMap<String, ItemBuilder> items;
     private static HashMap<String, Door> doors;
     private static HashMap<String, Shop> shops;
     private static HashMap<String, Level> levels;
     private static HashMap<Character, Color> charSet;
     private static HashMap<Character, Boolean> passSet;
     private static HashMap<Character, Boolean> visibleSet;
     private static Character defaultChar;
 
     private static void createBidirectionalTeleporter(World world1,
             Coordinate from1, Coordinate to2, World world2, Coordinate from2,
             Coordinate to1) {
         /*
          * fromi: Wo befindet sich der Teleporter in Welt i? toi: Wo soll der
          * Player in Welt i hinteleportiert werden?
          */
         Teleporter tel1 = new Teleporter(world2, to2);
         Teleporter tel2 = new Teleporter(world1, to1);
         world1.addActor(tel1, from1);
         world2.addActor(tel2, from2);
     }
 
     private static void initItems() {
         ZombieTools.log("initItems(): Initialisiere Items");
         items = new HashMap<String, ItemBuilder>();
 
         // Lade Waffen
         ZombieTools.log("initItems(): Lade Waffen");
         String[] waffen = getStrings(new File(ZombieGame.getItemDirectory(), "Waffen.txt"));
         for (String s : waffen) {
             String[] st = s.split(" ");
             ColoredChar chr = ColoredChar.create(st[1].charAt(0),
                     Color.decode("0x" + st[2]));
             WaffenBuilder waffenbuilder;
             int munition;
             if (st[4].equals("unbegrenzt")) {
                 munition = -1;
             } else {
                 munition = Integer.decode(st[4]);
             }
             if (st[3].equals("Nahkampf")) {
                 waffenbuilder = new WaffenBuilder(chr, st[0],
                         Integer.decode(st[5]), Waffentyp.NAHKAMPF, munition);
             } else if (st[3].equals("Fernkampf")) {
                 waffenbuilder = new WaffenBuilder(chr, st[0],
                         Integer.decode(st[5]), Waffentyp.FERNKAMPF, munition,
                         Integer.decode(st[6]));
             } else if (st[3].equals("Umkreis")) {
                 waffenbuilder = new WaffenBuilder(chr, st[0],
                         Integer.decode(st[5]), Waffentyp.UMKREIS, munition,
                         Double.parseDouble(st[6]));
             } else {
                 waffenbuilder = new WaffenBuilder(chr, st[0],
                         Integer.decode(st[5]), Waffentyp.GRANATE, munition,
                         Double.parseDouble(st[6]), Integer.decode(st[7]));
             }
             items.put(st[0], waffenbuilder);
         }
 
         // Lade HealingItems
         ZombieTools.log("initItems(): Lade HealingItems");
         String[] healingItems = getStrings(new File(ZombieGame.getItemDirectory(), "HealingItems.txt"));
         for (String s : healingItems) {
             String[] st = s.split(" ");
             items.put(
                     st[0],
                     new HealingItemBuilder(ColoredChar.create(st[2].charAt(0),
                     Color.decode("0x" + st[3])), st[0], Integer
                     .decode(st[1])));
         }
     }
 
     private static void initLevels() {
         ZombieTools.log("initLevels(): Initialisiere Levels");
 
         levels = new HashMap<String, Level>();
 
         // Lade Levelliste
         ZombieTools.log("initLevels(): Lade Levelliste");
         String[] levelList = getStrings(new File(ZombieGame.getSourceDirectory(), "levels.txt"));
 
         // Lade alle Level
         ZombieTools.log("initLevels(): Lade alle Level");
         for (String s : levelList) {
             Level level = createLevelFromFile(s);
             levels.put(s, level);
         }
 
         // Lade Teleporter
         ZombieTools.log("initLevels(): Lade alle Teleporter");
         String[] teles = getStrings(new File(ZombieGame.getSourceDirectory(), "teleporters.txt"));
         for (String s : teles) {
             String[] d = s.split(" ");
             Level world1 = levels.get(d[0]);
             Coordinate from1 = new Coordinate(Integer.decode(d[1]),
                     Integer.decode(d[2]));
             Coordinate to2 = new Coordinate(Integer.decode(d[3]),
                     Integer.decode(d[4]));
             Level world2 = levels.get(d[5]);
             Coordinate from2 = new Coordinate(Integer.decode(d[6]),
                     Integer.decode(d[7]));
             Coordinate to1 = new Coordinate(Integer.decode(d[8]),
                     Integer.decode(d[9]));
             createBidirectionalTeleporter(world1, from1, to2, world2, from2,
                     to1);
         }
     }
 
     private static void initCharSet() {
         charSet = new HashMap<Character, Color>();
         passSet = new HashMap<Character, Boolean>();
         visibleSet = new HashMap<Character, Boolean>();
         String[] settings = getStrings(new File(ZombieGame.getSourceDirectory(), "CharSet.txt"));
         for (int i = 0; i < settings.length; i++) {
             String[] setting = settings[i].split(" ");
             charSet.put(setting[0].charAt(0), Color.decode("0x" + setting[3]));
             passSet.put(setting[0].charAt(0), setting[1].equals("passable"));
             visibleSet.put(setting[0].charAt(0), setting[2].equals("visible"));
         }
     }
 
     public static ItemBuilder getItemBuilderByName(String s) {
         if (items == null) {
             initItems();
         }
         ItemBuilder i = items.get(s);
         Guard.argumentIsNotNull(i);
         return i;
     }
 
     public static Item newItemByName(String s) {
         return getItemBuilderByName(s).buildItem();
     }
 
     public static Waffe newWaffeByName(String s) {
         Item w = newItemByName(s);
         Guard.argumentIsNotNull(w);
         Guard.verifyState(w instanceof Waffe);
         return (Waffe) w;
     }
 
     private static Level getLevelByName(String s) {
         if (levels == null) {
             initLevels();
         }
         return levels.get(s);
     }
 
     private static Door getDoorByName(String s) {
         if (doors == null) {
             doors = new HashMap<String, Door>();
         }
         if (!doors.containsKey(s)) {
             doors.put(s, new Door(s));
         }
         return doors.get(s);
     }
 
     private static Shop getShopByName(String s) {
         if (shops == null) {
             shops = new HashMap<String, Shop>();
         }
         if (!shops.containsKey(s)) {
             String[] shop = getStrings(new File(ZombieGame.getShopDirectory(), s + ".shop"));
             String[] charInfo = shop[0].split(" ");
             HashMap<ItemBuilder, Integer> items = new HashMap<ItemBuilder, Integer>();
             for (int i = 1; i < shop.length; i++) {
                 String[] it = shop[i].split(" ");
                 items.put(getItemBuilderByName(it[0]), Integer.valueOf(it[1]));
             }
             shops.put(s, new Shop(ColoredChar.create(charInfo[0].charAt(0), Color.decode("0x" + charInfo[1])), s, items));
 
         }
         return shops.get(s);
     }
 
     private static KeyCard getKeyCardByName(String s) {
         return new KeyCard(getDoorByName(s));
     }
 
     public static HashMap<Character, Color> getCharSet() {
         if (charSet == null) {
             initCharSet();
         }
         return charSet;
     }
 
     public static HashMap<Character, Boolean> getPassSet() {
         if (passSet == null) {
             initCharSet();
         }
         return passSet;
     }
 
     public static HashMap<Character, Boolean> getVisibleSet() {
         if (visibleSet == null) {
             initCharSet();
         }
         return visibleSet;
     }
 
     public static Level getFirstLevel() {
         return getLevelByName(getFirstWordOfFile(new File(ZombieGame.getSourceDirectory(), "levels.txt")));
     }
 
     public static boolean isValidChar(char c) {
         return getCharSet().containsKey(c);
     }
 
     public static char getDefaultChar() {
         if (defaultChar == null) {
             defaultChar = getFirstWordOfFile(new File(ZombieGame.getSourceDirectory(), "CharSet.txt")).charAt(0);
         }
         return defaultChar;
     }
 
     public static Level createLevelFromFile(String mapName) {
 
         ZombieTools.log("createLevelFromFile(" + mapName + ")");

         // Lese ItemMap ein
         ZombieTools.log("createLevelFromFile(" + mapName + "): Lese Itemmap ein");
         HashMap<Character, String> itemMap = new HashMap<Character, String>();
         String[] items = getStrings(new File(ZombieGame.getMapDirectory(), mapName + ".itm"));
         for (String st : items) {
             String[] it = st.split(" ");
             itemMap.put(it[0].charAt(0), it[1]);
         }
         // Lese Map ein
         ZombieTools.log("createLevelFromFile(" + mapName + "): Lese Maps aus Mapfile");
         String[] level = getStrings(new File(ZombieGame.getMapDirectory(), mapName + ".map"));
         ColoredChar[][] chars = new ColoredChar[level.length][level[0].length()];
         for (int i = 0; i < level.length; i++) {
             for (int j = 0; j < level[i].length(); j++) {
                 if (isValidChar(level[i].charAt(j))) {
                     chars[i][j] = ColoredChar.create(level[i].charAt(j));
                 } else if (itemMap.containsKey(level[i].charAt(j))){
                     chars[i][j] = ColoredChar.create(getDefaultChar());
                 } else {
                     chars[i][j] = ColoredChar.create(level[i].charAt(j),Color.WHITE);
                 }
             }
         }
 
         // Baue Level
         ZombieTools.log("createLevelFromFile(" + mapName + "): Erzeuge Level");
         RoomBuilder builder = new RoomBuilder(chars);
         Level lev = new Level(builder.width(), builder.height(), builder, mapName);
 
         // Lade statische Items auf Map
         ZombieTools.log("createLevelFromFile(" + mapName + "): Lade statische Items");
         for (int x = 0; x < lev.width(); x++) {
             for (int y = 0; y < lev.height(); y++) {
                 char c;
                 try {c = level[y].charAt(x);}
                 catch (StringIndexOutOfBoundsException exc) {c = ' ';}
                 if (itemMap.containsKey(c)) {
                     String itemName = itemMap.get(c);
                     Actor actor = null;
                     Matcher m = Pattern.compile("^(\\w+)\\((.+)\\)$").matcher(itemName);
                     if (m.matches()) {
                         if (m.group(1).equals("door")) {
                             actor = getDoorByName(m.group(2));
                         } else if (m.group(1).equals("key")) {
                             actor = getKeyCardByName(m.group(2));
                         } else if (m.group(1).equals("shop")) {
                             actor = getShopByName(m.group(2));
                         } else {
                             Guard.validateArgument(false);
                         }
                     } else {
                         actor = newItemByName(itemName);
                     }
                     lev.addActor(actor, x, y);
                 }
             }
         }
 
         return lev;
     }
 
     public static String[] getStrings(File input) {
         LinkedList<String> lines = new LinkedList<String>();
         try {
             InputStreamReader reader = new InputStreamReader(
                     new FileInputStream(input), "UTF-8");
             BufferedReader text = new BufferedReader(reader);
             String temp;
             while ((temp = text.readLine()) != null) {
                 if (!temp.startsWith("//")) {
                     // End of Line comments done right?
                     lines.add(temp.replaceFirst("\\s*\\/\\/.*$", ""));
                 }
             }
             text.close();
             reader.close();
         } catch (IOException e) {
         }
         String[] erg = new String[lines.size()];
         for (int i = 0; i < lines.size(); i++) {
             erg[i] = lines.get(i);
         }
         return erg;
     }
 
     // Liest eine Datei im UTF-16 Format ein und gibt das 2-dim Feld in
     // ColoredChars zurück
     private static ColoredChar[][] readFile(File input) throws IOException {
         String[] level = getStrings(input);
         ColoredChar[][] chars = new ColoredChar[level.length][level[0].length()];
         for (int i = 0; i < level.length; i++) {
             for (int j = 0; j < level[i].length(); j++) {
                 chars[i][j] = ColoredChar.create(level[i].charAt(j),
                         Color.white);
             }
         }
         return chars;
     }
 
     private static String getFirstWordOfFile(File input) {
         String firstLine = getStrings(input)[0];
         return firstLine.split(" ")[0];
     }
 
     public static ColoredChar[][] getImage(String imageName) throws IOException {
         return readFile(new File(ZombieGame.getScreenDirectory(), imageName + ".scr"));
     }
 }
