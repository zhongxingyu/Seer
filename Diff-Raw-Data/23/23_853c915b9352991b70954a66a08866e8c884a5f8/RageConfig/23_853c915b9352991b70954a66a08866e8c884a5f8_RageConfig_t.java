 package net.rageland.ragemod;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.util.config.Configuration;
 
 import net.rageland.ragemod.data.TownLevel;
 
 // Stores and loads configuration values
 public class RageConfig {
 	
 	/* TODO: 
 	 *  - Faction info?
 	 * 
 	 */
 	
 	private Configuration pluginConfig;
 	private RageMod plugin;
 	
 	// General settings
 	public String SERVER_NAME = "Rageland";
 	public boolean PRE_RELEASE_MODE;
 	public String OWNER_NAME = "TheIcarusKid";		// For sending certain debug notifications
 	
 	// Database settings
 	public String DB_URL;
     public String DB_NAME;
     public String DB_DRIVER;
     public String DB_USER;
     public String DB_PASSWORD;
     public long DB_TIMEOUT;			// The timeout (in ms) for individual connections, regardless of status.  Should be less than WAIT_TIMEOUT on MySQL
     
     // Town settings
     public int Town_MIN_DISTANCE_BETWEEN = 400;
     public int Town_MIN_DISTANCE_ENEMY_CAPITOL = 2000;
     public int Town_MIN_DISTANCE_SPAWN = 1000;  // This leaves the issue of towns hanging into the neutral zone
     public int Town_MAX_LEVEL_NEUTRAL = 4;
     public int Town_MAX_LEVEL_FACTION = 5;
     public int Town_DISTANCE_BETWEEN_BEDS = 6;
     public double Town_UPKEEP_PER_PLAYER = 1.00;
     public int Town_MAX_BANKRUPT_DAYS = 7;
     public int Town_COLORED_SHEEP_CHANCE = 20;
     
     // Money settings
     public String CURRENCY_NAME = "Silver";			// iConomy's currency name
     public String CURRENCY_MINOR = "Copper";		// Minor currency
 	public int PRICE_GOLD = 2;						// The amount of money a gold ingot is worth
     public int INCOME_PER_BLOCK = 1;				// The amount of money per day for each treasury block
     public int DEFAULT_PORTAL_COST = 1;				// The amount of money to enter Travel Zone
     
     public HashMap<Integer, TownLevel> townLevels;
     
     // Zone settings
     public String Zone_NAME_A = "the Neutral Zone";
     public int Zone_BORDER_A = 1000;  // Distance from spawn
     public String Zone_NAME_B = "the War Zone";
     public int Zone_BORDER_B = 2000;  // Distance from spawn
     public String Zone_NAME_C = "The Wilds";
     public int Zone_BORDER_C = 2500;  // Distance from spawn
     public String Zone_TZ_CENTER = "-489,10,702";
     public String Zone_TZ_REGION = "-229,127,1042,-749,1,522";
 
     // Lot settings
     public int Lot_X_OFFSET;			// How to convert the web X coordinates to in-game coords
     public int Lot_Z_OFFSET;
     public int Lot_MULTIPLIER = 16;			// The lot grid is based on 16x16 chunks
     public int Lot_PRICE_COAL = 10;			// Price for Coal-level member lot in USD
     public int Lot_PRICE_IRON = 20;			// Price for Iron-level member lot in USD
     public int Lot_PRICE_GOLD = 30;			// Price for Gold-level member lot in USD
     public int Lot_PRICE_DIAMOND = 40;		// Price for Diamond-level member lot in USD
     
     // Capitol settings
     public String Capitol_Name;
     public String Capitol_CodedName = "<tc>Rage City</tc>";
     public int Capitol_X1a;			// The NW corner of region A for capitol
     public int Capitol_Z1a;
     public int Capitol_X2a;			// The SE corner of region A for capitol
     public int Capitol_Z2a;
     public int Capitol_X1b;			// The NW corner of region B for capitol
     public int Capitol_Z1b;
     public int Capitol_X2b;			// The SE corner of region B for capitol
     public int Capitol_Z2b;
     public String Capitol_SANDLOT;		// Auto-regen sand mine
     public int Capitol_SANDLOT_GOLD_ODDS = 50000;				// Chance (1 / x) of gold spawning in sand mine
     public int Capitol_SANDLOT_DIAMOND_ODDS = 500000;
     public String Capitol_PORTAL_LOCATION = "-73,68,-56";
     
     // Cooldowns (in seconds)
     public int Cooldown_Spawn = 30;
     public int Cooldown_Home = 30;
     
     // Faction settings
     public int Faction_BaseJoinCost = 100;		// Initial cost in coins to join a faction
     public int Faction_JoinCostIncrease = 10; 	// Amount the join cost will go up due to population imbalance
     
     // Task frequencies (in seconds)
     // 1 hour: 3600
     // 1 day:  86400
     // 1 week: 604800
     public int Task_TOWN_UPKEEP = 86400;			// Charge taxes for player towns
     public int Task_FILL_SANDLOT = 86400;			// Replenish sand in public sand mine
     
     // NPC settings
    public int NPC_TOTAL_FLOATING;				// Number of NPCs to spawn outside of NPCTowns
     public int NPC_TTL_MIN = 360;				// Minimum length of minutes to keep NPC instances
     public int NPC_TTL_MAX = 4320;				// Maximum length of minutes to keep NPC instances
     public int NPC_PHRASES = 2;					// Number of phrases each NPC will say at a time
     public int NPC_PHRASE_POOL = 8;				// Number of phrases pulled from DB to be selected from randomly
     public int NPCTOWN_GUEST_CHANCE = 33;		// Percent chance that NPC spawned in NPCTown will be floating guest
     public String CUSTOM_SKIN_PATH = "http://www.rageland.net/skins/";
     public int NPC_HUMAN_ID = 5;				// The Human race has special functionality
     public float NPC_AFFINITY_MAX = 10;			// Maximum affinity for each NPC
     public float NPC_MIN_AFFINITY = -10;			// Minimum affinity for each NPC
     public float NPC_AFFINITY_GAIN_TALK = 1;		// Amount of affinity gained for talking to an NPC
     
     // Lists
     public HashMap<Integer, String> NPC_LANGUAGE_NAMES;
     public HashMap<Integer, String> NPC_RACE_NAMES;
     public HashMap<Integer, String> NPC_AFFINITY_NAMES;
     public HashMap<Integer, String> NPC_AFFINITY_CODED_NAMES;
     
     
     public RageConfig (RageMod plugin)
     {
     	this.plugin = plugin;
     	
     	loadConfigValues();	
     	loadDefaultTownLevels();
     	
     	NPC_LANGUAGE_NAMES = new HashMap<Integer, String>();
     	NPC_LANGUAGE_NAMES.put(1, "Creeptongue");
     	NPC_LANGUAGE_NAMES.put(2, "Gronuk");
     	NPC_LANGUAGE_NAMES.put(3, "Benali");
     	NPC_LANGUAGE_NAMES.put(4, "Avialese");
     	
     	NPC_RACE_NAMES = new HashMap<Integer, String>();
     	NPC_RACE_NAMES.put(0, "None");
     	NPC_RACE_NAMES.put(1, "Creep");
     	NPC_RACE_NAMES.put(2, "Pigman");
     	NPC_RACE_NAMES.put(3, "Benali");
     	NPC_RACE_NAMES.put(4, "Avian");
     	NPC_RACE_NAMES.put(5, "Human");
     	
     	NPC_AFFINITY_NAMES = new HashMap<Integer, String>();
     	NPC_AFFINITY_NAMES.put(-2, "Dislike");
     	NPC_AFFINITY_NAMES.put(-1, "Aversion");
     	NPC_AFFINITY_NAMES.put(0, "Neutral");
     	NPC_AFFINITY_NAMES.put(1, "Friendship");
     	NPC_AFFINITY_NAMES.put(2, "Trust");
     	
     	NPC_AFFINITY_CODED_NAMES = new HashMap<Integer, String>();
     	NPC_AFFINITY_CODED_NAMES.put(-2, ChatColor.DARK_RED + "Dislike");
     	NPC_AFFINITY_CODED_NAMES.put(-1, ChatColor.RED + "Aversion");
     	NPC_AFFINITY_CODED_NAMES.put(0, ChatColor.GRAY + "Neutral");
     	NPC_AFFINITY_CODED_NAMES.put(1, ChatColor.GREEN + "Friendship");
     	NPC_AFFINITY_CODED_NAMES.put(2, ChatColor.DARK_GREEN + "Trust");
     }
     
     private void loadConfigValues() 
     {
     	pluginConfig = plugin.getConfiguration();
     	
     	PRE_RELEASE_MODE = pluginConfig.getBoolean("general.pre_release_mode", true);
 		if( PRE_RELEASE_MODE )
 			System.out.println("Activating Pre-release mode.");
     	
     	DB_URL = pluginConfig.getString("database.url");
 		DB_NAME = pluginConfig.getString("database.name");
 		DB_DRIVER = pluginConfig.getString("database.driver");
 		DB_USER = pluginConfig.getString("database.user");
 		DB_PASSWORD = pluginConfig.getString("database.password"); 
 		DB_TIMEOUT = pluginConfig.getInt("database.timeout", 20000000);
 		
 		this.Lot_X_OFFSET = pluginConfig.getInt("lots.x_offset", -384); 
 		this.Lot_Z_OFFSET = pluginConfig.getInt("lots.z_offset", 416); 
 		
 		Capitol_Name = pluginConfig.getString("capitol.name", "Rage City");
 	    Capitol_X1a = pluginConfig.getInt("capitol.x1a", -386);			// The NW corner of region A for capitol
 	    Capitol_Z1a = pluginConfig.getInt("capitol.z1a", 146);
 	    Capitol_X2a = pluginConfig.getInt("capitol.x2a", -82);			// The SE corner of region A for capitol
 	    Capitol_Z2a = pluginConfig.getInt("capitol.z2a", -261);
 	    Capitol_X1b = pluginConfig.getInt("capitol.x1b", -83);			// The NW corner of region B for capitol
 	    Capitol_Z1b = pluginConfig.getInt("capitol.z1b", 418);
 	    Capitol_X2b = pluginConfig.getInt("capitol.x2b", 513);			// The SE corner of region B for capitol
 	    Capitol_Z2b = pluginConfig.getInt("capitol.z2b", -261);
 	    Capitol_SANDLOT = pluginConfig.getString("capitol.sandlot", "114,60,-19,141,68,-46");
	    
	    NPC_TOTAL_FLOATING = pluginConfig.getInt("npc.total_floating", 5);
 		
 		System.out.println("Connecting to " + DB_URL + "...");		// Debug
 		
 		
 		
     }    
     
     private void loadDefaultTownLevels()
     {
     	townLevels = new HashMap<Integer, TownLevel>();
     	
     	TownLevel townLevel = new TownLevel();
     	townLevel.level = 1;
     	townLevel.name = "Settlement";
     	townLevel.size = 80;
     	townLevel.initialCost = 1000;
     	townLevel.upkeepCost = 5;
     	townLevel.minimumBalance = 100;
     	townLevel.maxResidents = 5;
     	townLevel.maxNPCs = 1;
     	townLevel.treasuryLevel = 0;
     	townLevel.sanctumFloor = buildSanctumFloor(townLevel.level);
     	townLevels.put(1, townLevel);
     	
     	townLevel = new TownLevel();
     	townLevel.level = 2;
     	townLevel.name = "Village";
     	townLevel.size = 120;
     	townLevel.initialCost = 2000;
     	townLevel.upkeepCost = 10;
     	townLevel.minimumBalance = 200;
     	townLevel.maxResidents = 10;
     	townLevel.maxNPCs = 2;
     	townLevel.treasuryLevel = 1;
     	townLevels.put(2, townLevel);
     	
     	townLevel = new TownLevel();
     	townLevel.level = 3;
     	townLevel.name = "Town";
     	townLevel.size = 180;
     	townLevel.initialCost = 3000;
     	townLevel.upkeepCost = 15;
     	townLevel.minimumBalance = 300;
     	townLevel.maxResidents = 15;
     	townLevel.maxNPCs = 3;
     	townLevel.treasuryLevel = 2;
     	townLevels.put(3, townLevel);
     	
     	townLevel = new TownLevel();
     	townLevel.level = 4;
     	townLevel.name = "City";
     	townLevel.size = 270;
     	townLevel.initialCost = 5000;
     	townLevel.upkeepCost = 25;
     	townLevel.minimumBalance = 500;
     	townLevel.maxResidents = 25;
     	townLevel.maxNPCs = 4;
     	townLevel.treasuryLevel = 2;
     	townLevels.put(4, townLevel);
     	
     	townLevel = new TownLevel();
     	townLevel.level = 5;
     	townLevel.name = "Capitol";
     	townLevel.size = 400;
     	townLevel.initialCost = 10000;
     	townLevel.upkeepCost = 50;
     	townLevel.minimumBalance = 1000;
     	townLevel.maxResidents = 50;
     	townLevel.maxNPCs = 6;
     	townLevel.treasuryLevel = 3;
     	townLevel.isCapitol = true;
     	townLevels.put(5, townLevel);
     }
     
     //		c: Cobblestone
     //		d: Dirt
     //		p: Wood Planks
     //		s: Stone
     //		t: Tile (slab)
     //		o: Obsidian
 	//		O: Obsidian stack 2-high w/ glowstone on top
     //		g: Glowstone
 	//		G: Glowstone stack 2-high
     //		w: Wool (of appropriate color)
     //		-: Portal (inside)
     //		|: Portal (outside)
     //		b: Bedrock
     //		L: Liquid (water/lava)
     //		n: Snow
     //		i: Iron Block
     //		S: Special faction block (lapis/netherrack)
     public static ArrayList<String> buildSanctumFloor(int level)
     {
     	ArrayList<String> floor = new ArrayList<String>();
     	
     	if( level == 1 )
     	{
     		floor.add("cccccccccccccccccccc"); 
     		floor.add("cddddddddccddddddddc");
     		floor.add("cddddddddccddddddddc");
     		floor.add("cddddddddccddddddddc");
     		floor.add("cddddddddccddddddddc"); // 5
     		floor.add("cddddddddccddddddddc"); 
     		floor.add("cddddddddccddddddddc");
     		floor.add("cdddddccccccccdddddc");
     		floor.add("cdddddcwwwwwwcdddddc");
     		floor.add("cccccccwoggowccccccc"); // 10
     		floor.add("cccccccwoggowccccccc"); 
     		floor.add("cdddddcwwwwwwcdddddc");
     		floor.add("cdddddccccccccdddddc");
     		floor.add("cddddddddccddddddddc");
     		floor.add("cddddddddccddddddddc"); // 15
     		floor.add("cddddddddccddddddddc"); 
     		floor.add("cddddddddccddddddddc");
     		floor.add("cddddddddccddddddddc");
     		floor.add("cddddddddccddddddddc");
     		floor.add("cccccccccccccccccccc"); // 20
     	}
     	else if( level == 2 )
     	{
     		floor.add("ssssssssssssssssssss");
     		floor.add("sppppppppsspppppppps");
     		floor.add("sppppppppsspppppppps");
     		floor.add("sppppppppsspppppppps");
     		floor.add("sppppppppsspppppppps"); // 5
     		floor.add("sppppppppsspppppppps");
     		floor.add("spppppssssssssppppps");
     		floor.add("sppppsswwwwwwsspppps");
     		floor.add("sppppswwwwwwwwspppps");
     		floor.add("sssssswgOggOgwssssss"); // 10
     		floor.add("sssssswgOggOgwssssss");
     		floor.add("sppppswwwwwwwwspppps");
     		floor.add("sppppsswwwwwwsspppps");
     		floor.add("spppppssssssssppppps");
     		floor.add("sppppppppsspppppppps"); // 15
     		floor.add("sppppppppsspppppppps");
     		floor.add("sppppppppsspppppppps");
     		floor.add("sppppppppsspppppppps");
     		floor.add("sppppppppsspppppppps");
     		floor.add("ssssssssssssssssssss"); // 20
     	}
     	else if( level == 3 )
     	{
     		floor.add("bbbbbbbbbbbbbbbbbbbb");
     		floor.add("bssssssssbbssssssssb");
     		floor.add("bssssssssbbssssssssb");
     		floor.add("bssssssssbbssssssssb");
     		floor.add("bssssssssbbssssssssb"); // 5
     		floor.add("bsssssbbbbbbbbsssssb");
     		floor.add("bssssbbwwwwwwbbssssb");
     		floor.add("bsssbbwwwwwwwwbbsssb");
     		floor.add("bsssbwwwggggwwwbsssb");
     		floor.add("bbbbbwwg|--|gwwbbbbb"); // 10
     		floor.add("bbbbbwwg|--|gwwbbbbb");
     		floor.add("bsssbwwwggggwwwbsssb");
     		floor.add("bsssbbwwwwwwwwbbsssb");
     		floor.add("bssssbbwwwwwwbbssssb");
     		floor.add("bsssssbbbbbbbbsssssb"); // 15
     		floor.add("bssssssssbbssssssssb");
     		floor.add("bssssssssbbssssssssb");
     		floor.add("bssssssssbbssssssssb");
     		floor.add("bssssssssbbssssssssb");
     		floor.add("bbbbbbbbbbbbbbbbbbbb"); // 20
     	}
     	else if( level == 4 )
     	{
     		floor.add("tttttttttttttttttttt");
     		floor.add("tnnnnnnntwwtnnnnnnnt");
     		floor.add("tnnnnnnntwwtnnnnnnnt");
     		floor.add("tnnnnnnntwwtnnnnnnnt");
     		floor.add("tnnnnntttwwtttnnnnnt"); // 5
     		floor.add("tnnnnttwwwwwwttnnnnt");
     		floor.add("tnnnttwwwwwwwwttnnnt");
     		floor.add("tnnttwwwwwwwwwtttnnt");
     		floor.add("ttttwwwggggggwwwtttt");
     		floor.add("twwwwwwg|--|gwwwwwwt"); // 10
     		floor.add("twwwwwwg|--|gwwwwwwt");
     		floor.add("ttttwwwggggggwwwtttt");
     		floor.add("tnnttwwwwwwwwwwttnnt");
     		floor.add("tnnnttwwwwwwwwttnnnt");
     		floor.add("tnnnnttwwwwwwttnnnnt"); // 15
     		floor.add("tnnnnntttwwtttnnnnnt");
     		floor.add("tnnnnnnntwwtnnnnnnnt");
     		floor.add("tnnnnnnntwwtnnnnnnnt");
     		floor.add("tnnnnnnntwwtnnnnnnnt");
     		floor.add("tttttttttttttttttttt"); // 20
     	}
     	else if( level == 5 )
     	{
     		floor.add("iiiiiiiiiiiiiiiiiiii");
     		floor.add("iLLLLLLLiwwiLLLLLLLi");
     		floor.add("iLLLLLLLiwwiLLLLLLLi");
     		floor.add("iLLLLLiiiwwiiiLLLLLi");
     		floor.add("iLLLLiiwwwwwwiiLLLLi"); // 5
     		floor.add("iLLLiiwwwwwwwwiiLLLi");
     		floor.add("iLLiiwwwwwwwwwwiiLLi");
     		floor.add("iLiiwwggggggggwwiiLi");
     		floor.add("iiiwwwgSSSSSSgwwwiii");
     		floor.add("iwwwwwgS|--|Sgwwwwwi"); // 10
     		floor.add("iwwwwwgS|--|Sgwwwwwi");
     		floor.add("iiiwwwgSSSSSSgwwwiii");
     		floor.add("iLiiwwggggggggwwiiLi");
     		floor.add("iLLiiwwwwwwwwwwiiLLi");
     		floor.add("iLLLiiwwwwwwwwiiLLLi");
     		floor.add("iLLLLiiwwwwwwiiLLLLi");
     		floor.add("iLLLLLiiiwwiiiLLLLLi");
     		floor.add("iLLLLLLLiwwiLLLLLLLi");
     		floor.add("iLLLLLLLiwwiLLLLLLLi");
     		floor.add("iiiiiiiiiiiiiiiiiiii");
     		
     	}
     	
     	return floor;
     }
 
     
     
     
     
     
     
 }
