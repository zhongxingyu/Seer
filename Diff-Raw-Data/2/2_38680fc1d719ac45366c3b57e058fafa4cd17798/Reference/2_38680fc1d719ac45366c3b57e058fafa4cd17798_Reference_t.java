 package vivadaylight3.myrmecology.common;
 
 import net.minecraft.util.EnumChatFormatting;
 
 /**
  * This class holds the names of blocks, items and mod details
  * 
  * @author samueltebbs
  * 
  */
 public class Reference {
 
     public static final String VERSION_MAJOR = "@VERSION_MAJOR@";
     public static final String VERSION_MINOR = "@VERSION_MINOR@";
     public static final String VERSION_BUILD = "@VERSION_BUILD@";
    public static final String VERSION_MC = "@VERSION_MC@";
     public static final String VERSION_FORGE = "@VERSION_FORGE@";
     public static final String VERSION_CHECK_URL = "@VERSION_URL@";
 
     public static final String MOD_ID = "@MOD_ID@";
     public static final String MOD_CHANNEL = MOD_ID;
     public static final String MOD_URL = "@MOD_URL@";
     public static final String MOD_DESC = "@MOD_DESC@";
     
     public static final String MOD_CHANNEL_INCUBATOR = MOD_CHANNEL + "2";
     public static final String MOD_VERSION = VERSION_MAJOR + "."
 	    + VERSION_MINOR + "." + VERSION_BUILD;
     public static final String MOD_DEPENDENCIES = "";
     
     public static final String DEATH_MESSAGE = "%1$s just angered some ";
     
     public static final String CHAT_PREFIX = "["+EnumChatFormatting.BLUE+MOD_ID+EnumChatFormatting.RESET+"]";
     
     // Ant types
     public static final String[] standardTypeNames = new String[] { "Queen",
 	    "Drone", "Worker", "Larva" };
 
     // Blocks
     public static final String BLOCK_ANTFARM_NAME = "antFarm";
     public static final String BLOCK_ANTHILL_NAME = "antHill";
     public static final String BLOCK_INCUBATOR_NAME = "incubator";
     public static final String BLOCK_FUNGI_NAME = "fungi";
 
     // Items
     public static final String ITEM_EXTRACTOR_NAME = "extractor";
     public static final String ITEM_MYRMOPAEDIA_NAME = "myrmopaedia";
     public static final String ITEM_FUNGI_NAME = "Agaricus Fungi";
 
     // Ant hills
     public static final String HILL_FOREST_NAME = "antHillForest";
     public static final String HILL_DESERT_NAME = "antHillDesert";
     public static final String HILL_JUNGLE_NAME = "antHillJungle";
     public static final String HILL_PLAINS_NAME = "antHillPlains";
     public static final String HILL_SNOW_NAME = "antHillSnow";
     public static final String HILL_STONE_NAME = "antHillStone";
     public static final String HILL_SWAMP_NAME = "antHillSwamp";
     public static final String HILL_WATER_NAME = "antHillWater";
 
     // Ants
     public static final String ANT_BARBARIC_NAME = "antBarbaric";
     public static final String ANT_CARPENTER_NAME = "antCarpenter";
     public static final String ANT_COMMON_NAME = "antCommon";
     public static final String ANT_CULTIVATOR_NAME = "antCultivator";
     public static final String ANT_DESERT_NAME = "antDesert";
     public static final String ANT_DREDGER_NAME = "antDredger";
     public static final String ANT_FOREST_NAME = "antForest";
     public static final String ANT_FUNGAL_NAME = "antFungal";
     public static final String ANT_HARVESTER_NAME = "antHarvester";
     public static final String ANT_HOSTILE_NAME = "antHostile";
     public static final String ANT_JUNGLE_NAME = "antJungle";
     public static final String ANT_MOUND_NAME = "antMound";
     public static final String ANT_ODOUROUS_NAME = "antOdourous";
     public static final String ANT_PLAINS_NAME = "antPlains";
     public static final String ANT_PLENTIFUL_NAME = "antPlentiful";
     public static final String ANT_SCAVENGER_NAME = "antScavenger";
     public static final String ANT_SNOW_NAME = "antSnow";
     public static final String ANT_SPROUTER_NAME = "antSprouter";
     public static final String ANT_STONE_NAME = "antStone";
     public static final String ANT_SWAMP_NAME = "antSwamp";
     public static final String ANT_WATER_NAME = "antWater";
 
 }
