 package zombiefu.util;
 
 import jade.core.Actor;
 import jade.util.Guard;
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Coordinate;
 import java.util.Properties;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import zombiefu.items.Weapon;
 import zombiefu.player.Attribute;
 import zombiefu.util.Action;
 
 public class ZombieSettings {
 
     private Properties props;
     public final String playerName;
     public final HashMap<Attribute, Integer> playerAttributes;
     public final String playerInventar;
     public final ColoredChar playerChar;
     public final String playerStartMap;
     public final Coordinate playerStartCoord;
     public final String globalMap;
     public final boolean debug;
     public final HashMap<String, Action> keybindings;
     public final HashMap<String, File> paths;
 
     public ZombieSettings(String[] args, String res) {
         props = new Properties(defaults(res));
 
         List<String> configFiles = new ArrayList<String>();
         configFiles.add(System.getProperty("user.home") + "/.zombiefurc");
         configFiles.add(res + "/config.cfg");
 
         for (String fileName : configFiles) {
             try {
                 props.load(new FileInputStream(fileName));
                 System.out.println("ZombieSettings: Konfigurationsdatei " + fileName + " geladen.");
             } catch (IOException ex) {
                 System.out.println("ZombieSettings: Konfigurationsdatei " + fileName + " nicht vorhanden.");
             }
         }
 
         // Spielerinfo
         playerName = props.getProperty("player.name");
         playerChar = ColoredChar.create(ZombieTools.getCharFromString(props.getProperty("player.tile.char")), ZombieTools.getColorFromString(props.getProperty("player.tile.color")));
         playerInventar = props.getProperty("player.startItems");
         playerAttributes = new HashMap<Attribute, Integer>();
         playerAttributes.put(Attribute.MAXHP, props.getProperty("player.attr.hp") == null ? null : Integer.decode(props.getProperty("player.attr.hp")));
         playerAttributes.put(Attribute.ATTACK, props.getProperty("player.attr.att") == null ? null : Integer.decode(props.getProperty("player.attr.att")));
         playerAttributes.put(Attribute.DEFENSE, props.getProperty("player.attr.def") == null ? null : Integer.decode(props.getProperty("player.attr.def")));
         playerAttributes.put(Attribute.DEXTERITY, props.getProperty("player.attr.dex") == null ? null : Integer.decode(props.getProperty("player.attr.dex")));
         playerStartMap = props.getProperty("player.start.map");
         playerStartCoord = new Coordinate(Integer.decode(props.getProperty("player.start.x")), Integer.decode(props.getProperty("player.start.y")));
 
         // Weltkarte
         globalMap = props.getProperty("globalmap");
 
         // Debug-Modus
         if (props.getProperty("debug").equalsIgnoreCase("true")) {
             debug = true;
         } else {
             debug = false;
         }
 
         // Die Keybindings einlesen. TODO: Einstampfen in Schleife.
         keybindings = new HashMap<String, Action>();
         keybindings.put(props.getProperty("controls.up"), Action.UP);
         keybindings.put(props.getProperty("controls.down"), Action.DOWN);
         keybindings.put(props.getProperty("controls.left"), Action.LEFT);
         keybindings.put(props.getProperty("controls.right"), Action.RIGHT);
         keybindings.put(props.getProperty("controls.attack"), Action.ATTACK);
         keybindings.put(props.getProperty("controls.nextweapon"), Action.NEXT_WEAPON);
         keybindings.put(props.getProperty("controls.prevweapon"), Action.PREV_WEAPON);
         keybindings.put(props.getProperty("controls.inventory"), Action.INVENTORY);
         keybindings.put(props.getProperty("controls.noop"), Action.NOOP);
         keybindings.put(props.getProperty("controls.help"), Action.HELP);
 
         // Die Pfadangaben einlesen.
         paths = new HashMap<String, File>();
         paths.put("base", new File(props.getProperty("dir.base")));
         paths.put("maps", new File(props.getProperty("dir.maps")));
         paths.put("screens", new File(props.getProperty("dir.screens")));
         paths.put("shops", new File(props.getProperty("dir.shops")));
         paths.put("monsters", new File(props.getProperty("dir.monsters")));
         paths.put("humans", new File(props.getProperty("dir.humans")));
         paths.put("weapons", new File(props.getProperty("dir.weapons")));
         paths.put("food", new File(props.getProperty("dir.food")));
        System.out.println(paths);
         // Überprüfen, ob Pfade lesbar sind.
         Iterator itr = paths.values().iterator();
         while (itr.hasNext()) {
             File f = (File) itr.next();
             Guard.verifyState(f.canRead());
         }
     }
 
     private Properties defaults(String res) {
         Properties def = new Properties();
 
         // Default Verzeichnis-Layout
         def.setProperty("dir.base", res);
         def.setProperty("dir.maps", res + "/maps");
         def.setProperty("dir.screens", res + "/screens");
         def.setProperty("dir.shops", res + "/shops");
         def.setProperty("dir.monsters", res + "/monsters");
         def.setProperty("dir.humans", res + "/humans");
         def.setProperty("dir.weapons", res + "/weapons");
         def.setProperty("dir.food", res + "/food");
 
         // Default Debug Einstellung (aus)
         def.setProperty("debug", "false");
 
         // Default Playername
         def.setProperty("player.name", System.getProperty("user.name"));
         def.setProperty("player.tile.char", "263B");
         def.setProperty("player.tile.color", "7D26CD");
         def.setProperty("player.start.map", "Weltkarte");
         def.setProperty("player.start.x", "15");
         def.setProperty("player.start.y", "53");
 
         def.setProperty("globalmap", "Weltkarte");
 
         // Default Keybindings
         def.setProperty("controls.up", "LATIN SMALL LETTER W");
         def.setProperty("controls.down", "LATIN SMALL LETTER S");
         def.setProperty("controls.left", "LATIN SMALL LETTER A");
         def.setProperty("controls.right", "LATIN SMALL LETTER D");
         def.setProperty("controls.attack", "LINE FEED (LF)");
         def.setProperty("controls.nextweapon", "LATIN SMALL LETTER E");
         def.setProperty("controls.prevweapon", "LATIN SMALL LETTER Q");
         def.setProperty("controls.inventory", "LATIN SMALL LETTER I");
         def.setProperty("controls.noop", "FULL STOP");
         def.setProperty("controls.help", "QUESTION MARK");
 
         return def;
     }
 }
