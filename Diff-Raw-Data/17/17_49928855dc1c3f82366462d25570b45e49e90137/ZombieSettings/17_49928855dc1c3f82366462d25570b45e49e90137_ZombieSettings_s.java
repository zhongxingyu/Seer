 package zombiefu.util;
 
 import jade.core.Actor;
 import jade.util.Guard;
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Coordinate;
 import java.util.Properties;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import zombiefu.items.Weapon;
 import zombiefu.player.Attribute;
 import zombiefu.util.Action;
 import zombiefu.ZombieFU;
 import zombiefu.util.ZombieTools;
 
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
     public final HashMap<String, String> paths;
 
     public ZombieSettings(String[] args) {
         props = new Properties(defaults(""));
 
        String userconfig = System.getProperty("user.home") + "/.zombiefurc";
         try {
            props.load(new FileInputStream(userconfig));
            System.out.println("ZombieSettings: User-Konfigurationsdatei " + userconfig + " geladen.");
        } catch (IOException ex) {
            System.out.println("ZombieSettings: Keine User-Konfigurationsdatei " + userconfig + " vorhanden. Benutze defaults.");
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
         paths = new HashMap<String, String>();
         paths.put("base", props.getProperty("dir.base"));
         paths.put("maps", props.getProperty("dir.maps"));
         paths.put("screens", props.getProperty("dir.screens"));
         paths.put("shops", props.getProperty("dir.shops"));
         paths.put("monsters", props.getProperty("dir.monsters"));
         paths.put("humans", props.getProperty("dir.humans"));
         paths.put("weapons", props.getProperty("dir.weapons"));
         paths.put("food", props.getProperty("dir.food"));
 
         // Überprüfen, ob Pfade lesbar sind.
         Iterator itr = paths.values().iterator();
         while (itr.hasNext()) {
             InputStream in = ZombieFU.class.getResourceAsStream((String) itr.next());
             Guard.argumentIsNotNull(in);
         }
     }
 
     private Properties defaults(String res) {
         Properties def = new Properties();
 
         // Default Verzeichnis-Layout
         def.setProperty("dir.base", "");
         def.setProperty("dir.maps", "maps/");
         def.setProperty("dir.screens", "screens/");
         def.setProperty("dir.shops", "shops/");
         def.setProperty("dir.monsters", "monsters/");
         def.setProperty("dir.humans", "humans/");
         def.setProperty("dir.weapons",  "weapons/");
         def.setProperty("dir.food", "food/");
 
         // Default Debug Einstellung (aus)
         def.setProperty("debug", "true");
 
         // Default Playername
         try {
             def.setProperty("player.name", System.getProperty("user.name"));
         } catch (SecurityException e) {
             def.setProperty("player.name", "Anonymous");
         }
 
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
