 import java.io.File;
 import java.util.logging.Level;
 
 import org.lwjgl.input.Keyboard;
 
 public class InvTweaksConst {
 
     // Version-specific mod constants
    public static final String MOD_VERSION = "1.34 (1.0.0)";
     public static final String TREE_VERSION = "1.0.0";
 
     // Mod timing constants
     public static final int RULESET_SWAP_DELAY = 1000;
     public static final int AUTO_REFILL_DELAY = 200;
     public static final int POLLING_DELAY = 3;
     public static final int POLLING_TIMEOUT = 1500;
     public static final int SORTING_TIMEOUT = 2999; // > POLLING_TIMEOUT
     public static final int CHEST_ALGORITHM_SWAP_MAX_INTERVAL = 2000;
     public static final int TOOLTIP_DELAY = 1000;
     public static final long CRAFTING_DELAY = 100;
 
     // File constants
     public static final String MINECRAFT_DIR = InvTweaksObfuscation.getMinecraftDir();
     public static final String MINECRAFT_CONFIG_DIR = MINECRAFT_DIR + "config" + File.separatorChar;
     public static final String CONFIG_PROPS_FILE = MINECRAFT_CONFIG_DIR + "InvTweaks.cfg";
     public static final String CONFIG_RULES_FILE = MINECRAFT_CONFIG_DIR + "InvTweaksRules.txt";
     public static final String CONFIG_TREE_FILE = MINECRAFT_CONFIG_DIR + "InvTweaksTree.txt";
     public static final String OLD_CONFIG_TREE_FILE = MINECRAFT_CONFIG_DIR + "InvTweaksTree.xml";
     public static final String OLDER_CONFIG_RULES_FILE = MINECRAFT_DIR + "InvTweaksRules.txt";
     public static final String OLDER_CONFIG_TREE_FILE = MINECRAFT_DIR + "InvTweaksTree.txt";
     public static final String DEFAULT_CONFIG_FILE = "DefaultConfig.dat";
     public static final String DEFAULT_CONFIG_TREE_FILE = "DefaultTree.dat";
     public static final String HELP_URL = "http://wan.ka.free.fr/?invtweaks";
     
     // Global mod constants
     public static final String INGAME_LOG_PREFIX = "InvTweaks: ";
     public static final Level DEFAULT_LOG_LEVEL = Level.WARNING;
     public static final Level DEBUG = Level.INFO;
     public static final int JIMEOWAN_ID = 54696386; // Used in GUIs
     
     // Minecraft constants
     public static final int INVENTORY_SIZE = 36;
     public static final int INVENTORY_ROW_SIZE = 9;
     public static final int CHEST_ROW_SIZE = INVENTORY_ROW_SIZE;
     public static final int DISPENSER_ROW_SIZE = 3;
     public static final int INVENTORY_HOTBAR_SIZE = INVENTORY_ROW_SIZE;
     public static final int PLAYER_INVENTORY_WINDOW_ID = 0;
     
     /**
      * Key binding to trigger sorting. 
      * Maintained by Minecraft so that its keycode is actually
      * what has been configured by the player (not always the R key).
      */
     public static final aby SORT_KEY_BINDING = 
         new aby("Sort inventory", Keyboard.KEY_R); /* KeyBinding */
     
 }
