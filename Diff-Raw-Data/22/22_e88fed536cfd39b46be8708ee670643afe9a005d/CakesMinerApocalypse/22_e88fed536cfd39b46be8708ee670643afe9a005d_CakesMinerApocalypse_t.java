 package me.cakenggt.CakesMinerApocalypse;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CakesMinerApocalypse extends JavaPlugin {
 	private Listener playerListener;
 	private Listener moveListener;
 	private Listener blockListener;
 	private CakesMinerApocalypseVaultCreator chunkListener;
 	private Listener nukeListener;
 	private CakesMinerApocalypseBroadcast broadcast;
 	private int size = 10000;
 	private Map<World, Boolean> worldsTable = new HashMap<World, Boolean>();
 	private List<Location> craters;
 	private List<Location> GECKs;
 	private List<Long> craterTimes;
 	private List<Location> radios;
 	private boolean apocalypseDamage = true;
 	private boolean randomSpawn = true;
 	private double shelterChance = 0.001;
 	private double craterChance = 0.001;
 	private int pipboyID = 345;
 	private int chatDistance = 50;
 	private final String configname = "config.yml";
     
     public void onDisable() {
         // TODO: Place any custom disable code here.
         System.out.println(this + " is now disabled!");
     }
     
     public void onEnable() {
     	if(!loadConfig()) {
     		System.out.println(this + " has encountered an error while reading the configuration file," 
     				+ " continuing with defaults");
     	}
 
     	playerListener = new CakesMinerApocalypsePlayerLogin(this);
     	moveListener = new CakesMinerApocalypsePlayerMovement(this);
     	blockListener = new CakesMinerApocalypseBlockListener(this);
     	chunkListener = new CakesMinerApocalypseVaultCreator(this);
     	nukeListener = new CakesMinerApocalypseNuke(this);
     	try {
 			loadCraters();
 			loadGECKs();
 			loadCraterTimes();
 			loadRadios();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	getServer().getPluginManager().registerEvents(playerListener, this);
     	getServer().getPluginManager().registerEvents(moveListener, this);
     	getServer().getPluginManager().registerEvents(blockListener, this);
     	getServer().getPluginManager().registerEvents(chunkListener, this);
     	getServer().getPluginManager().registerEvents(nukeListener, this);
     	this.broadcast = new CakesMinerApocalypseBroadcast(this);
     	try {
 			checkGECKs();
 			checkRadios();
 		} catch (IOException e) {
 			//e.printStackTrace();
 		}
     	final ShapedRecipe gRecipe = new ShapedRecipe(new ItemStack(Material.SPONGE, 1));
         gRecipe.shape("CBC", "BAB", "CBC");
         gRecipe.setIngredient('A', Material.DIAMOND_BLOCK);
         gRecipe.setIngredient('B', Material.IRON_BLOCK);
         gRecipe.setIngredient('C', Material.REDSTONE);
         final FurnaceRecipe fRecipe = new FurnaceRecipe(new ItemStack(Material.GRAVEL, 1), Material.SNOW_BLOCK);
         final FurnaceRecipe aRecipe = new FurnaceRecipe(new ItemStack(Material.CHAINMAIL_HELMET, 1), Material.GOLD_HELMET);
         final FurnaceRecipe bRecipe = new FurnaceRecipe(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), Material.GOLD_CHESTPLATE);
         final FurnaceRecipe cRecipe = new FurnaceRecipe(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1), Material.GOLD_LEGGINGS);
         final FurnaceRecipe dRecipe = new FurnaceRecipe(new ItemStack(Material.CHAINMAIL_BOOTS, 1), Material.GOLD_BOOTS);
         getServer().addRecipe(gRecipe);
         getServer().addRecipe(fRecipe);
 		if (getConfig().getBoolean("alwaysSneak", true)) {
 			setupRefresh();
 		}
         getServer().addRecipe(aRecipe);
         getServer().addRecipe(bRecipe);
         getServer().addRecipe(cRecipe);
         getServer().addRecipe(dRecipe);
         Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this.broadcast, 20L, 100L);
 
         if (getConfig().getBoolean("alternateWorlds", true)) {
             alternateWorlds();
         }
 
 
         System.out.println(this + " is now enabled!");
     }
 
     private void setupRefresh() {
     	Timer refreshTimer = new Timer();
 	     refreshTimer.scheduleAtFixedRate(new TimerTask() {
 	       public void run() {
 	    	   Player[] players = getServer().getOnlinePlayers();
 	       		for (Player p : players) {
 	       			p.setSneaking(false);
 	       			p.setSneaking(true);
 	       		}
 	       	
 	       }
 	     }
 	     , 500L, 5000L);
 	   }
     
 	private boolean loadConfig() {
 	    YamlConfiguration config = new YamlConfiguration();
 	    getDataFolder().mkdirs();
 	    File configfile = new File(getDataFolder(),configname);
 	    try {
 	      config.load(configfile);
 	    } catch (FileNotFoundException e) {
 	      config.set("size", size);
          config.set("unloadChunksBeyond", true);
 	      for (World world : getServer().getWorlds()){
 	    	  config.set("worlds." + world.getName(), true);
 	      }
 		config.set("randomSpawn", true);
 		config.set("randomRespawnAlways", false);
 		config.set("apocalypseDamage", apocalypseDamage);
 		config.set("apocalypseDamageWater", true);
 		config.set("apocalypseDamageAcidRain", true);
 		config.set("shelterChance", shelterChance);
 		config.set("craterChance", craterChance);
 		config.set("pipboyID", pipboyID);
 		config.set("chatDistance", chatDistance);
 		config.set("alwaysSneak", true);    
 		config.set("alternateWorlds", true);    
         config.set("alternateWorldsFixedTime", true);
         config.set("alternateWorldsTemporalMesh", true);
         config.set("alternateWorldsPotionEffects", true);
         config.set("alternateWorldsPortalDevice", true);
 		config.set("shelter.lightBlock", "glowstone");
 		//config.set("shelter.lightBlock", "128;1");     // PlasticCraft GlowingPlexiglass
         //config.set("shelter.lightBlock", "129");      // Trees++ Dark Crystal Leaves
         //config.set("shelter.lightBlock", "129;1");      // Trees++ Crystal Leaves
 		config.set("shelter.doorBlockUpper", 71);        // iron door (also try IC2 reinforced door)
 		config.set("shelter.doorBlockUpperData", 0);
 		config.set("shelter.doorBlockLower", 71);
 		config.set("shelter.doorBlockLowerData", 8);
 		config.set("shelter.leverBlock", 69);
 		config.set("shelter.buildingBlock", Material.BEDROCK.getId());
 		//config.set("shelter.buildingBlock", 231);     // IC2 reinforced stone (harder than obsidian)
 		config.set("shelter.enabledLootGroups", new String[] { "regular" });
 
 		// Default loot
 		config.set("shelter.loot.regular", new String[] { "pumpkin_seeds", 
 			"bread", "cake", "melon", "mushroom_soup", "cooked_chicken", 
             "cooked_beef", "grilled_pork", "cooked_fish", 
 			"redstone", "356" /* diode item, not block */, "redstone_torch_on", 
             "torch", "iron_fence", "compass", "iron_boots", "iron_chestplate", 
 			"iron_leggings", "melon_seeds", "seeds" });
 
 		// Some examples for reference / inspiration / ease of enabling if mods/plugins installed
 		// These aren't enabled by default (not in enabledLootGroups)
 
         // IndustrialCraft^2
         config.set("shelter.loot.ic2", new String[] {
             "3xiron_block",
             "64xiron_ingot",
             "64xiron_ingot",
             "32xiron_ingot",
             "64x30249",     // refined iron
             "16x30244",     // refined uranium
             "1x30209",      // electric jetpack
             "1x30211",      // rubber boots
             "48x30216",     // rubber
             "16x30239",     // RE Battery
             "16x30239",     // RE Battery
             "16x30239",     // RE Battery
             "16x30239",     // RE Battery
             "16x30238",     // Single-Use Battery
             "16x30238",     // Single-Use Battery
             "1x30222",      // Tin Can 
             "1x30222",      // Tin Can 
             "1x30222",      // Tin Can 
             "1x139;1",      // Rubber Tree Sapling
             "64x30203",     // Depleted Isotope Cell
             });
     
         config.set("shelter.loot.ic2more", new String[] {
             "32x5261",      // bronze ingot
             "1x30194;100",  // bronze chestplate
             "1x30195",      // bronze helmet
             "1x30192;20",   // bronze boots
             "1x30193;50",   // bronze leggings
             "1x30183;10",   // wrench
             "16x30188",     // matter
             "5x30190",      // advanced circuit
             "1x30208;27",   // mining laser
             "64x30184;3",   // insulated gold cable
             "64x30184;1",   // uninsulated copper cable
             "1x23347",      // electric fishing pole  
             "1x31256",      // thermometer
             "16x238",       // solar panel
             "1x30116",      // solar helmet
             "10x183;1",     // medium voltage solar array
         });
 
         // Miscellaneous mods
         config.set("shelter.loot.mixedmods", new String[] {
             "5x181;5",      // Iron Chests - Crystal Chest
             "1x126",        // Wireless Redstone - transmitter
             "1x126",        // Wireless Redstone - transmitter
             "1x127",        // Wireless Redstone - receiver
             "14x214",       // Dynamic Elevators - elevator
             "1x215",        // Dynamic Elevator - elevator buttons
             "64x4308",      // Buildcraft - stone transport pipe
             "64x4310",      // Buildcraft - golden transport pipe
             "64x4312",      // Buildcraft - obsidian transport pipe
             "32x4303",      // Buildcraft - Additional Pipes - item teleport pipe
             "16x4060",      // Buildcraft - diamond gear
             "32x4058",      // Buildcraft - iron gear
             "1x3256",       // Hot Air Balloon
             "1x3256",       // Hot Air Balloon
             "1x3256",       // Hot Air Balloon
             "1x3257",       // Hot Air Balloon - powered
             "1x3257",       // Hot Air Balloon - powered
             "1x567",        // Animal Bikes - cow bike
             "1x567;1",      // Animal Bikes - spider bike
             "1x567;5",      // Animal Bikes - wolf bike
             "16x569",       // Animal Bikes - rawhide
         });
 
         config.set("shelter.loot.forestry", new String[] {
             "1x13304",      // backpacks
             "1x13305",     
             "1x13306",     
             "1x13307",     
             "1x13308",     
             "1x13344",
             "1x13345",
             "1x13346",
             "1x13347",
             "16x13282",     // honeycomb
             "48x13291",     // honeyed slice
             "16x13319",     // honey can
             "16x13322",     // honey capsule
             "8x13787",      // crated honey
             "32x13285",     // beeswax
             "64x5274",      // ash
             "64x13278",     // mulch
             "64x197",       // humus
             "10x13788",     // royal jelly, crated
             "32x13286",     // pollen
             "16x5267",      // empty vial
             "8x5276",       // water can
             "8x5282",       // iodine capsule
             "64x13292",     // short mead
             "64x13292",     // short mead
             "1x13293",      // ambrosia
             "1x13262",      // broken pickaxe
             "1x13262",      // broken pickaxe
             "1x13265",      // broken shovel
             "1x13265",      // broken shovel
         });
 
         config.set("shelter.loot.trees++", new String[] {
             "16x20000",     // Banana
             "16x20000",     // Banana
             "16x20000;1",   // Fig
             "16x20000;2",
             "16x20000;3",
             "16x20000;4",
             "16x20000;5",
             "16x20000;6",
             "16x20000;7",
             "16x20000;7",
             "16x20000;8",
             "16x20000;9",
             "16x20001;0",
             "16x20001;1",
             "16x20001;2",
             "16x20001;3",
             "8x20002",     // Blackberry Bread
             "8x20002;1",     
             "8x20002;2",     
             "8x20002;3",     
             "8x20002;3",     
             "8x20002;4",     
             "8x20002;5",     
             "8x20002;6",     
             "8x20002;7",     
             "8x20002;8",     
             "8x20002;9",     
             "1x176",        // desert ironwood sapling
             "1x176;1", 
             "1x176;2", 
             "1x176;3", 
             "1x176;4", 
             "1x176;5", 
             "1x176;6", 
             "1x176;7", 
             "1x178;2", 
         });
 
     	// http://dev.bukkit.org/server-mods/enchantmore/
 		config.set("shelter.loot.enchantmore", new String[] {
            "1xgold_axe+6@1",                  // Axe + Aqua Affinity
 		   "1xiron_axe+48@1",                 // Axe + Power
 		   "1xiron_axe+48@1",                 // Axe + Power
 		   "1xleather_boots;10+48@1+49@1",    // Boots + Power + Punch
 		   "1xdiamond_boots+2@1",             // Boots + Feather Falling X
 		   "10xbow+2@2",                      // Bow + Feather Falling II
 		   "1xbow+33@1",                      // Bow + Silk Touch
 		   "1xleather_chestplate+33@1",       // Chestplate + Silk Touch
 		   "1xleather_chestplate+49@1",       // Chestplate + Punch
 		   "3xfishing_rod+21@2",              // Fishing Rod + Looting
 		   "4xflint_and_steel;25+5@1",        // Flint & Steel + Respiration
 		   "1xflint_and_steel;5+6@1",         // Flint & Steel + Aqua Affinity
 		   "10xflint_and_steel;60+3@1",       // Flint & Steel + Blast Protection
 		   "1xflint_and_steel;15+17@1",       // Flint & Steel + Smite
 		   "1xflint_and_steel+48@1",          // Flint & Steel + Power
 		   "1xgold_sword;10+48@1",            // Sword + Power
 		   "1xdiamond_sword+49@1",            // Sword + Punch
 		   "1xiron_sword;20+35@1",            // Sword + Fortune
 		   "10xwood_sword+33@1",              // Sword + Silk Touch
 		   "1xiron_sword;20+51@1",            // Sword + Infinity
 		   "5xshears+21@1+17@1",              // Shears + Looting + Smite
 		   "1xshears+18@1",                   // Shears + Bane of Arthropods
 		   "1xshears+35@1",                   // Shears + Fortune
 		   "2xdiamond_spade;1500+33@2",       // Shovel + Silk Touch II
 		   "20xiron_helmet+20@1",             // Helmet + Fire Aspect
 		   "1xdiamond_hoe+6@1",               // Hoe + Aqua Affinity
 		   "1xgold_hoe;30+48@1",              // Hoe + Power
 		   "1xiron_hoe+33@1",                 // Hoe + Silk Touch
 		   "1xdiamond_pickaxe+50@1",          // Pickaxe + Flame
 		   "1xiron_pickaxe+16@1",             // Pickaxe + Power
 		   "1xdiamond_pickaxe;1000+33@2",     // Pickaxe + Silk Touch II
 		   "1xdiamond_pickaxe;100+16@2",      // Pickaxe + Sharpness
 		   "1xdiamond_leggings+2@1",          // Leggings + Feather Falling
 		   "64xarrow" });
 
         // http://dev.bukkit.org/server-mods/potionsplus/
         config.set("shelter.loot.potionsplus", new String[] {
             "1xpotion;6",  // clear
             "1xpotion;11",
             "1xpotion;13",
             "1xpotion;14",
             "1xpotion;22",
             "1xpotion;27",
             "1xpotion;29",
             "1xpotion;30",
             "1xpotion;38",
             "1xpotion;43",
             "1xpotion;45",
             "1xpotion;46",
             "1xpotion;48",
             "1xpotion;54",
             "1xpotion;59",
             "1xpotion;61",
             "1xpotion;62", // stinky
             "1xpotion;16390",  // clear - splash
             "1xpotion;16395",
             "1xpotion;16397",
             "1xpotion;16398",
             "1xpotion;16406",
             "1xpotion;16411",
             "1xpotion;16413",
             "1xpotion;16414",
             "1xpotion;16422",
             "1xpotion;16427",
             "1xpotion;16429",
             "1xpotion;16430",
             "1xpotion;16432",
             "1xpotion;16438",
             "1xpotion;16443",
             "1xpotion;16445",
             "1xpotion;16446", // stinky - splash
         });
 
     	// http://www.minecraftforum.net/topic/182918-11smp-flans-mods-planes-ww2-guns-vehicles-playerapi-moods-mputils-teams/
 		config.set("shelter.loot.plane", new String[] {
 		   "2x23289",  //  metal wing
 		   "2x23267",  //  machine gun
 		   "2x23262",  //  bomb bay
 		   "1x23261",  //  metal tail
 		   "1x23281",  //  metal propeller
 		   "1x23265",  //  metal cockpit
 		   "1x23274",  //  V4 engine
 		   "1x23275",  //  V6 engine
 		   "1x23277",  //  V8 engine
 		   "16x23270", //  bullet
 		   "16x23270", //  bullet
 		   "2x23271",  //  small bomb
 		   "64xcoal",
 		   "2x255" }); //  plane crafting table
 
 		//  http://www.minecraftforum.net/topic/211517-11-balkons-weaponmod-v84-multiplayer/
     	config.set("shelter.loot.balkan", new String[] {
 		   "1x5000",   //  wooden spear
 		   "1x5006",   //  stone halberd
 		   "1x5011",   //  stone battleaxe
 		   "1x5019",   //  gold warhammer
 		   "1x5023",   //  diamond knive
 		   "1x5025",   //  wooden flail
 		   "1x5030",   //  javalin
 		   "1x5033",   //  musket barrel
 		   "5x5037",   //  poisonous dart
 		   "32x5039",  //  fire rod
 		   "1x5040",   //  cannon
 		   "32x5041",  //  cannonball
 		   "32x5043",  //  blunderbuss shot
 		   "1x5046",   //  stock
 		   "1x5045" });//  training dummy
 
 		//  http://www.minecraftforum.net/topic/119361-110-tehkrushs-mods-all-mods-updated-and-plasticcraft-smp/
     	config.set("shelter.loot.plastic", new String[] {
 		   "1x1027",   //  health needle
 		   "1x1027",   //  health needle
 		   "1x1027",   //  health needle
 		   "1x1009",   //  bowl of gelatin
 		   "1x1024",   //  water bottle
 		   "1x1024",   //  water bottle
 		   "1x1025",   //  milk bottle
 		   "1x1028",   //  jello
 		   "16x1013",  //  duct tape
 		   "16x1015",  //  battery
 		   "8x1041",   //  kevlar vest
 		   "8x1042",   //  kevlar pants
 		   "1x1050",   //  plastic shovel
 		   "32x127",   //  C4 plastic explosive
 		   "64x128",   //  plexiglass
 		   "1x1014",   //  plexiglass door
 		   "1x130",    //  microwave oven
 		   "1x1040" });//  night-vision goggles
 
           config.set("shelter.potionLoot", new String[] { "glass_bottle", 
 			"nether_stalk", "glowstone_dust", "redstone", "fermented_spider_eye", "magma_cream",
 			"sugar", "speckled_melon", "spider_eye", "ghast_tear", "blaze_powder", "sulphur" });
 
 	      try {
 	        config.save(configfile);
 	      } catch (IOException e1) {
 	        System.out.println(this + " was unable to create the default config file!");
 	      }
 	    } catch (IOException e) {
 	      e.printStackTrace();
 	      return false;
 	    } catch (InvalidConfigurationException e) {
 	      e.printStackTrace();
 	      return false;
 	    }
 	    this.setSize(config.getInt("size", this.getSize()));
 	    for (World world : getServer().getWorlds()){
 	    	this.setOn(world, config.getBoolean("worlds." + world.getName(), false));
 	    }
 	    this.setRandomSpawn(config.getBoolean("randomSpawn", this.getRandomSpawn()));
 	    this.setApocalypseDamage(config.getBoolean("apocalypseDamage", this.getApocalypseDamage()));
 	    this.setShelterChance(config.getDouble("shelterChance", this.getShelterChance()));
 	    this.setCraterChance(config.getDouble("craterChance", this.getCraterChance()));
 	    this.setPipboyID(config.getInt("pipboyID", this.getPipboyID()));
 	    this.setChatDistance(config.getInt("chatDistance", this.getChatDistance()));
 	    return true;
 	  }
 
 	public void checkGECKs() throws IOException {
 		if (new File("plugins/CakesMinerApocalypse/").mkdirs())
 			System.out.println("GECK file created");
 		File myFile = new File("plugins/CakesMinerApocalypse/GECKs.txt");
 		if (!myFile.exists()){
 			PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/GECKs.txt");
 			System.out.println("GECK file created");
 			outputFile.close();
 		}
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> GECKs = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			GECKs.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		PrintWriter outputFile = new PrintWriter(myFile);
 		if (GECKs == null){
 			outputFile.close();
 			return;
 		}
 		for (Location GECK : GECKs){
 			Block block = GECK.getBlock();
 			if (block.getRelative(BlockFace.NORTH).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.SOUTH).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.EAST).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.WEST).getType() == Material.PISTON_BASE){
 				outputFile.println(block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
 			}
 		}
 		outputFile.close();
 	}
 	
 	public void checkRadios() throws IOException {
 		if (new File("plugins/CakesMinerApocalypse/").mkdirs())
 			System.out.println("radio file created");
 		File myFile = new File("plugins/CakesMinerApocalypse/radios.txt");
 		if (!myFile.exists()){
 			PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/radios.txt");
 			System.out.println("radio file created");
 			outputFile.close();
 		}
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> radios = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			radios.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		PrintWriter outputFile = new PrintWriter(myFile);
 		if (radios == null){
 			outputFile.close();
 			return;
 		}
 		for (Location radio : radios){
 			Block block = radio.getBlock();
 			if (block.isBlockIndirectlyPowered()){
 				outputFile.println(block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
 			}
 		}
 		outputFile.close();
 	}
 	
 	public void setSize(int size) {
 		this.size = size;
 	}
 	
 	public int getSize() {
 		return size;
 	}
 
 	public void setApocalypseDamage(boolean apocalypseDamage) {
 		this.apocalypseDamage = apocalypseDamage;
 	}
 	
 	public boolean getApocalypseDamage() {
 		return apocalypseDamage;
 	}
 
 	public void setOn(World world, boolean isOn){
 		this.worldsTable.put(world, isOn);
 	}
 
 	public Map<World, Boolean> getOn(){
 		return worldsTable;
 	}
 
 	public void setRandomSpawn(boolean randomSpawn){
 		this.randomSpawn = randomSpawn;
 	}
 	
 	public boolean getRandomSpawn() {
 		return randomSpawn;
 	}
 	
 	public void setShelterChance(double shelterChance) {
 		this.shelterChance = shelterChance;
 	}
 	
 	public double getShelterChance() {
 		return shelterChance;
 	}
 	
 	public void setCraterChance(double craterChance) {
 		this.craterChance = craterChance;
 	}
 	
 	public double getCraterChance() {
 		return craterChance;
 	}
 	
 	public void setPipboyID(int id) {
 		this.pipboyID = id;
 	}
 	
 	public int getPipboyID() {
 		return pipboyID;
 	}
 	
 	public void setChatDistance(int distance) {
 		this.chatDistance = distance;
 	}
 	
 	public int getChatDistance() {
 		return chatDistance;
 	}
 	
 	public void loadCraters() throws IOException {
 		File myFile = new File("plugins/CakesMinerApocalypse/craters.txt");
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> craters = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			craters.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		this.craters = craters;
 	}
 	public List<Location> getCraters() {
 		return craters;
 	}
 	public void loadGECKs() throws IOException{
 		File myFile = new File("plugins/CakesMinerApocalypse/GECKs.txt");
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> GECKs = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			GECKs.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		this.GECKs = GECKs;
 	}
 	public List<Location> getGECKs() {
 		return GECKs;
 	}
 	public void loadCraterTimes() throws IOException {
 		File myFile = new File("plugins/CakesMinerApocalypse/craterTimes.txt");
 		Scanner inputFile = new Scanner(myFile);
 		List<Long> craterTimes = new ArrayList<Long>();
 		while (inputFile.hasNextLine()){
 			Long a = Long.valueOf(inputFile.next());
 			craterTimes.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		this.craterTimes = craterTimes;
 	}
 	public List<Long> getCraterTimes() {
 		return craterTimes;
 	}
 	public void loadRadios() throws IOException {
 		File myFile = new File("plugins/CakesMinerApocalypse/radios.txt");
 		Scanner inputFile = new Scanner(myFile);
 		List<Location> radios = new ArrayList<Location>();
 		while (inputFile.hasNextLine()){
 			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
 			radios.add(a);
 			inputFile.nextLine();
 		}
 		inputFile.close();
 		this.radios = radios;
 	}
 	public List<Location> getRadios() {
 		return radios;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 	   	if(cmd.getName().equalsIgnoreCase("radio")){ // If the player typed /radio then do the following...
 	   		Player player = null;
     		if (sender instanceof Player) {
     			player = (Player) sender;
 	    	}
 	   		if (player == null) {
 	   			sender.sendMessage("this command can only be run by a player");
 	   			return false;
 	   		}
 	   		if (player.getItemInHand().getTypeId() != getPipboyID()){
 	   			player.sendMessage("You must be holding a compass to work the radio");
 	   			return true;
 	   		}
     		if (args.length != 1){
 	    		sender.sendMessage(cmd.getUsage());
 	   			return true;
 	   		}
     		if (args[0].equalsIgnoreCase("scan")){
     			try {
 					setFrequency(sender, args[0].toString());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return true;
     		}
     		if (args[0].equalsIgnoreCase("version")){
     			String name = this.toString();
     			player.sendMessage(name);
     			return true;
     		}
     		try {
 				Double.parseDouble(args[0].toString());
 		    }
 			catch (NumberFormatException e) {
 				sender.sendMessage(cmd.getUsage());
 				return true;
 		    }
 	   		String inputString = args[0].toString();
 	   		try {
 	   			setFrequency(sender, inputString);
     		} catch (IOException e) {
 	    		e.printStackTrace();
     		}
     		return true;
 	    }
 	   	if(cmd.getName().equalsIgnoreCase("support")){
 	   		String message = "";
 	   		for (String part : args){
 	   			message = message + " " + part;
 	   		}
 	   		System.out.println("[Support Chat] " + sender.getName() + ":" + message);
 	   		Player[] recipientsArray = sender.getServer().getOnlinePlayers();
 			for (int i = 0; i < recipientsArray.length; i ++){
 				if (recipientsArray[i].isOp()){
 					recipientsArray[i].sendMessage(ChatColor.BLUE + "[Support Chat] " + sender.getName() + ":" + message);
 				}
 			}
 	   		return true;
 	   	}
 
 		if (cmd.getName().equalsIgnoreCase("placeshelter")) {
 			World world = null;
 			if (sender instanceof Player) {
 				if (!((Player)sender).hasPermission("cakesminerapocalypse.placeshelter")) {
 					sender.sendMessage("You do not have permission to place fallout shelters");
 					return true;
 				}
 				world = ((Player)sender).getWorld();
 			}
 
 			// Get location to generate shelter
 			int x = 0, y = 0, z = 0;
 			if (args.length < 3) {
 				if (args.length == 1 && args[0].equalsIgnoreCase("here") && sender instanceof Player) {
 					x = ((Player)sender).getLocation().getBlockX();
 					y = ((Player)sender).getLocation().getBlockY();
 					z = ((Player)sender).getLocation().getBlockZ();
 				} else {
 					sender.sendMessage("Missing required arguments");
 					return false;
 				}
 			} else {
 				try {
 					x = Integer.parseInt(args[0]);
 					y = Integer.parseInt(args[1]);
 					z = Integer.parseInt(args[2]);
 				} catch (NumberFormatException e) {
 					sender.sendMessage("Invalid coordinates");
 					return false;
 				}
 			}
 
 			if (args.length >= 4) {
 				world = Bukkit.getWorld(args[3]);
 			} else if (world == null) {
 				world = Bukkit.getWorlds().get(0);
 			}
 
 			chunkListener.placeShelter(new Location(world, x, y, z));
 			return true;
 		}
 	    	return false;
 	}
 	    
 	   public void setFrequency (CommandSender sender, String inputString) throws IOException{
 		   if (new File("plugins/CakesMinerApocalypse/").mkdirs())
 				System.out.println("Frequencies file created");
 		   File myFile = new File("plugins/CakesMinerApocalypse/frequencies.txt");
 		   if (myFile.exists()){
 			   Scanner inputFileCheck = new Scanner(myFile);
 	           int j = 0;
 	           while (inputFileCheck.hasNext()) {
             	inputFileCheck.nextLine();
             	j++;
 	           }
 	           int size = (j + 1) / 2;
 	           String[] nameArray = new String[size];
 	           String[] circleArray = new String[size];
 	           inputFileCheck.close();
 	           Scanner inputFile = new Scanner(myFile);
 	           for (int i = 0; i < size; i++) {
 	               nameArray[i] = inputFile.nextLine();
 	               circleArray[i] = inputFile.nextLine();
 	           }
 	           boolean isInFile = false;
 	           for (int i = 0; i < size; i++) {
 	               if (nameArray[i].equalsIgnoreCase(sender.getName())){
 	               		circleArray[i] = inputString;
 	               		sender.sendMessage("Frequency " + inputString + " set successfully!");
 	               		isInFile = true;
 	               }
 	           }
 	           inputFile.close();
 	           PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/frequencies.txt");
 	           for (int i = 0; i < size; i++) {
 	               outputFile.println(nameArray[i]);
 	               outputFile.println(circleArray[i]);
 	           }
 	           if (!isInFile){
 	        	   outputFile.println(sender.getName());
 	               outputFile.println(inputString);
 	           }
 	           outputFile.close();
 		}
 		else{
 			PrintWriter outputFile = new PrintWriter("plugins/CakesMinerApocalypse/frequencies.txt");
 	        outputFile.println(sender.getName());
 	        outputFile.println(inputString);
 	        sender.sendMessage("frequency " + inputString + " set successfully!");
 	        outputFile.close();
 		}
     }
 	   
 	   public void alternateWorlds (){
 		   List<World> worlds = getServer().getWorlds();
 		   for (World world : worlds){
 			   if (this.worldsTable.get(world)){
 				   if (getServer().getWorld(world.getName() + "Alternate") == null){
 					   WorldCreator creator = new WorldCreator(world.getName() + "Alternate");
 					   creator.copy(world);
 					   creator.createWorld();
 					   this.setOn(getServer().getWorld(world.getName() + "Alternate"), true);
 				   }
 				   else{
 					   this.setOn(getServer().getWorld(world.getName() + "Alternate"), true);
 				   }
 			   }
 		   }
 	   }
 }
