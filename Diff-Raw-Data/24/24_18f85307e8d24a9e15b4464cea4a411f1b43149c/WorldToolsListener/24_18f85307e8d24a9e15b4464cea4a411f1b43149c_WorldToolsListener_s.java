 /**
  * @Author Glacksy & Spenk
  * @category World
  * @Version 2.0
  * 
  * @Description
  * tools for World
  */
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 public class WorldToolsListener extends PluginListener {
 	
 	private static Logger log = Logger.getLogger("Minecraft");
 
 	   /**
 		 * Disable and Enable different features in properties file
 		 * This part is used to "wire" prop file and features
 		 */
 		   private static PropertiesFile properties;
 		   private static boolean BlockFireSpread;
 		   private static boolean BlockLavaFire;
 		   private static boolean BlockLighter;
 		   private static boolean BlockFireBlockDestory;
 		   private static boolean BlockLightningFire;
 		   private static boolean BlockCreeperExplosion;
 		   private static boolean BlockGhastExplosion;
 		   private static boolean BlockPortalCreating;
 		   private static boolean BlockPortalDestroying;
 		   private static boolean BlockTntExplosion;
 		   private static boolean DisableFallDamage;
 		   private static boolean DisableLavaDamage;
 		   private static boolean DisableFireDamage;
 		   private static boolean DisableFireTickDamage;
 		   private static boolean DisableCactusDamage;
 		   private static boolean DisableEntityDamage;
 		   private static boolean DisableCreeperDamage;
 		   private static boolean DisableLightningDamage;
 		   private static boolean DisableStarvationDamage;
 		   private static boolean DisableSuffocationDamage;
 		   private static boolean DisableWaterDamage;
 		   private static boolean DisablePotionDamage;
 		   private static boolean DisableWaterFlow;
 		   private static boolean DisableLavaFlow;
 		   private static boolean DisablePhysicsGravel;
 		   private static boolean DisablePhysicsSand;
 		   private static boolean BlockLeafDecay;
 		   private static boolean BlockCowMilking;
 		   private static boolean BlockEating;
 		   private static boolean BlockDispenser;
 		   private static boolean FarmlandDestroy;
 		   private static boolean DisableEntityDespawning;
 		   private static boolean DisableLightningStrike;
 		   private static boolean DisableInventories;
 		   private static boolean DisableItemPickup;
 		   private static boolean DisableItemDropping;
 		   private static boolean DisableWeather;
 		   private static boolean DisableDayTime;
 		   private static boolean DisableThunderWeather;
 		   private static boolean DisableNightTime;
 		   private static boolean DisableWolfTame;
            private static boolean DisableEndermanBlockPickup;
 		   private static boolean AlwaysRaining;
 		   private static boolean Instandtame;
 		   private static boolean ClassicWater;
 		   private static boolean TpHomeOnDeath;
 		   private static boolean DisablePigZombificatio;
 		   
 		   public static int rad;
 		   public static boolean rlsponge;
 		   private static String leavetypes;
 		   
 		   private static boolean teleporttootherworld;
 		   private static String world;
 		   private static int level;
 		   private static boolean kickondeath;
 		   private static String reason;
 		   private static boolean BlockIceMelting;
 		   private static boolean BlockWaterFreezing;
 		   private static boolean BlockLavaObsidian;
 		 //  private Set<Integer> DisallowFireSpreadBlocks;     //Throwing errors, have to recode it
 		 //  private Set<Integer> DisallowLavaSpreadBlocks;     //Throwing errors, have to recode it
 		 //  private Set<Integer> DisallowWaterSpreadBlocks;    //Throwing errors, this disable water flow somehow
 		   
 	    
 	    /** checking if the file exists
 	     * and creating the file if it doesnt
 	     * 
 	     * also the new direction of the properties file
 	     * plugins/config/WorldTools
 	     * @author spenk
 	     */
 	    public void createfile(){
 	    	File f1 = new File("plugins/config");
 	    	f1.mkdir();
 	    	File f2 = new File("Plugins/config/WorldTools");
 	    	f2.mkdir();
 	    	if (!new File("plugins/config/WorldTools/WorldTools.properties").exists()){
 	    	PropertiesFile f = new PropertiesFile("plugins/config/WorldTools/WorldTools.properties");
 	    	try {
 				f.load();
 			} catch (IOException e) {
 				log.info("[WorldTools] - could not create PropertiesFile!");
 			}
 	    	makeSettingsFile("plugins/config/WorldTools/WorldTools.properties");
 	    	loadprops("plugins/config/WorldTools/WorldTools.properties");
 	    	}
 	    	loadprops("plugins/config/WorldTools/WorldTools.properties");
 	    }
     
     /**
      * Write the properties file and all its Settings
      * @author Glacksy
      */
 
    private static void makeSettingsFile(String file) {
 	try {
 		File f = new File(file);
 		BufferedWriter out = new BufferedWriter(new FileWriter(f));
 		out.write("#==========================#"); out.newLine();
 		out.write("#WorldTools Settings File#"); out.newLine();
 		out.write("#Set it True to enable and false to disable"); out.newLine();
 		out.write("#==========================#"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block fire from spreading"); out.newLine();
 		out.write("block-fire-spread=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block lava from causing fire"); out.newLine();
 		out.write("block-lava-fire=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block lighter from being used"); out.newLine();
 		out.write("#To ignore this, give the player/group this permission: /lighter-usage "); out.newLine();
 		out.write("block-lighter=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block Fire from destrying blocks"); out.newLine();
 		out.write("block-fire-block-destroy=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block lightning from creating fire"); out.newLine();
 		out.write("block-lightning-fire=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block creepers from causing world damage"); out.newLine();
 		out.write("block-creeper-explosion=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block Ghasts from causing world and player damage"); out.newLine();
 		out.write("block-ghast-explosion=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block nether portals from being activated"); out.newLine();
 		out.write("block-portal-creating=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block nether portals from being destroyed"); out.newLine();
 		out.write("block-portal-destroying=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block TnT from dealing damage to player and world"); out.newLine();
 		out.write("block-tnt-explosion=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable fall damage"); out.newLine();
 		out.write("disable-fall-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable lava damage"); out.newLine();
 		out.write("disable-lava-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Fire Damage"); out.newLine();
 		out.write("disable-fire-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Firetick Damage"); out.newLine();
 		out.write("disable-firetick-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable cactus damage"); out.newLine();
 		out.write("disable-cactus-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Mob/Entity Damage"); out.newLine();
 		out.write("disable-entity-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Creeper - player damage(not world damage)"); out.newLine();
 		out.write("disable-creeper-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable lightning damage"); out.newLine();
 		out.write("disable-lightning-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Hunger/Starvation damage"); out.newLine();
 		out.write("disable-starvation-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable suffocation damage"); out.newLine();
 		out.write("disable-suffocation-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Drowning/water Damage"); out.newLine();
 		out.write("disable-drowning-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Potion Damage"); out.newLine();
 		out.write("disable-potion-damage=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Water Flow"); out.newLine();
 		out.write("disable-water-flow=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Lava Flow"); out.newLine();
 		out.write("disable-lava-flow=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable sand physics"); out.newLine();
 		out.write("disable-gravel-physics=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable sand physics"); out.newLine();
 		out.write("disable-sand-physics=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable leaf decay"); out.newLine();
 		out.write("disable-leaf-decay=false"); out.newLine();
 		out.write(" ");out.newLine();
 		out.write("#Leave DamageValues");out.newLine();
 		out.write("#these are leaves wich arnt allowed to decay");out.newLine();
 		out.write("Leave-DamageValues=0,1,2,3");out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block cow milking"); out.newLine();
 		out.write("#Its pointless but might be useful"); out.newLine();
 		out.write("block-cow-milking=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block Players From Eating"); out.newLine();
 		out.write("block-eating=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Block Dispensers from shooting"); out.newLine();
 		out.write("block-dispensers=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#block players / mobs from destrying farmland by walking and jumping on it"); out.newLine();
 		out.write("block-farmland-physics=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#block Ice from melting"); out.newLine();
 		out.write("block-Ice-physics=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#block Water from Freezing"); out.newLine();
 		out.write("block-Water-physics=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#block Lava from turning into obsidian"); out.newLine();
 		out.write("block-Lava-physics=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Entity Despawning"); out.newLine();
 		out.write("disable-entity-despawning=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Lightning Strikes"); out.newLine();
 		out.write("disable-lightning-strike=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Inventories, this does not work for player inventory only furnace,dispenser,chest..etc.."); out.newLine();
 		out.write("disable-inventories=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Item Pickup"); out.newLine();
 		out.write("disable-item-pickup=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Item Dropping"); out.newLine();
 		out.write("disable-item-dropping=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable weather"); out.newLine();
 		out.write("disable-weather=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Thunder Weather"); out.newLine();
 		out.write("disable-thunder-weather=false"); out.newLine();
 		out.write(" "); out.newLine();
         out.write("#Force rain and snow on all the time."); out.newLine();
 		out.write("always-raining=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Night Time/Skip Night Time"); out.newLine();
 		out.write("disable-night-time=false"); out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disable Day Time/Skip Day Time");out.newLine();
 		out.write("disable-day-time=false");out.newLine();
 		out.write(" "); out.newLine();
		out.write("#Sponge radius");out.newLine();
		out.write("sponge-radius=2");out.newLine();
		out.write(" "); out.newLine();
		out.write("#Realistic Sponge");out.newLine();
 		out.write("use-sponge=false");out.newLine();
         out.write(" "); out.newLine();
 		out.write("#ExactSpawn"); out.newLine();
 		out.write("enable-exact-spawn=true");
         out.write(" "); out.newLine();
		out.write("#Use classic water"); out.newLine();
 		out.write("classic-water=false");
         out.write(" "); out.newLine();
 		out.write("#Prevent enderman from picking up blocks"); out.newLine();
 		out.write("disable-enderman-pickup=false");out.newLine();
 		out.newLine();
 		out.write("#Prevent Wolfs from being tamed"); out.newLine();
 		out.write("prevent-wolf-tame=false");out.newLine();
 		out.newLine();
 		out.write("#Instand Tame wolfs"); out.newLine();
 		out.write("instand-tame=false");out.newLine();
 		out.newLine();
 		out.write("#Prevent lightning from turning a pig in to a pigzombie!"); out.newLine();
 		out.write("disable-pig-zombification=false");out.newLine();
 		out.newLine();
 		out.write("#Teleport an player to the nether/end when he reaches a certain level"); out.newLine();
 		out.write("Teleport-Player-OnReachLayer=false");out.newLine();
 		out.write("#World to teleport player when he reaches an certain level (choose Nether or End)"); out.newLine();
 		out.write("World-To-Teleport-Player-To=Nether");out.newLine();
 		out.write("#Y level wich a player needs to reach to get teleported (- values are accepted)"); out.newLine();
 		out.write("Y-Level=-1");out.newLine();
 		out.newLine();
 		out.write("#Kick a player on death"); out.newLine();
 		out.write("Kick-on-Death=false");out.newLine();
 		out.write("#Kick Message - Reason (use &colorcode for colors)"); out.newLine();
 		out.write("Reason=&cPlease Rejoin");out.newLine();
 		out.newLine();
 		out.close();
 		
 		
 		
 	}catch (IOException e){
 		log.severe("[WorldTools] - Couldn't Create Settings File!");
 	}
    }
    
    /**
     * Convert a comma-delimited list to a set of integers.
     * @author sk89q
     * @param str
     * @return
     * This is only used as a part of the borrowed code for lava and fire block stuff
     */
 /*   DISABLED BECAUSE ITS DERPING AND PREVENT THE PLUGIN FROM LOADING
    private static Set<Integer> toBlockIDSet(String str) {
        if (str.trim().length() == 0) {
            return null;
        }
        
        String[] items = str.split(",");
        Set<Integer> result = new HashSet<Integer>();
 
        for (String item : items) {
            try {
                result.add(Integer.parseInt(item.trim()));
            } catch (NumberFormatException e) {
                int id = etc.getDataSource().getItem(item.trim());
                if (id != 0) {
                    result.add(id);
                } else {
                    ((Logger) logger).log(Level.WARNING, "WorldTools: Unknown block name: "
                            + item);
                }
            }
        }
 
        return result;
    }
 	*/
 	/**
 	    * props loading stuff
 	    * parses boolean from string
 	    * @author spenk
 	    */
 	
 	public void loadprops(String file){
 		   properties = new PropertiesFile(file);
 		   
 		   // Anti Grief protections
 		   BlockFireSpread =Boolean.parseBoolean(properties.getProperty("block-fire-spread"));
 		   BlockLavaFire = Boolean.parseBoolean(properties.getProperty("block-lava-fire"));
 		   BlockLighter = Boolean.parseBoolean(properties.getProperty("block-lighter"));
 		   BlockFireBlockDestory = Boolean.parseBoolean(properties.getProperty("block-fire-block-destroy"));
 		   BlockLightningFire = Boolean.parseBoolean(properties.getProperty("block-lightning-fire"));
 		   BlockCreeperExplosion = Boolean.parseBoolean(properties.getProperty("block-creeper-explosion"));
 		   BlockGhastExplosion = Boolean.parseBoolean(properties.getProperty("block-ghast-explosion"));
 		   BlockPortalCreating = Boolean.parseBoolean(properties.getProperty("block-portal-creating"));
 		   BlockPortalDestroying = Boolean.parseBoolean(properties.getProperty("block-portal-destroying"));
 		   BlockTntExplosion = Boolean.parseBoolean(properties.getProperty("block-tnt-explosion"));
 		   
 		   // Disable different damage types
 		   DisableFallDamage = Boolean.parseBoolean(properties.getProperty("disable-fall-damage"));
 		   DisableLavaDamage = Boolean.parseBoolean(properties.getProperty("disable-lava-damage"));
 		   DisableFireDamage = Boolean.parseBoolean(properties.getProperty("disable-fire-damage"));
 		   DisableFireTickDamage = Boolean.parseBoolean(properties.getProperty("disable-firetick-damage"));
 		   DisableCactusDamage = Boolean.parseBoolean(properties.getProperty("disable-cactus-damage"));
 		   DisableEntityDamage = Boolean.parseBoolean(properties.getProperty("disable-entity-damage"));
 		   DisableCreeperDamage = Boolean.parseBoolean(properties.getProperty("disable-creeper-damage"));
 		   DisableLightningDamage = Boolean.parseBoolean(properties.getProperty("disable-lightning-damage"));
 		   DisableStarvationDamage = Boolean.parseBoolean(properties.getProperty("disable-starvation-damage"));
 		   DisableSuffocationDamage = Boolean.parseBoolean(properties.getProperty("disable-suffocation-damage"));
 		   DisableWaterDamage = Boolean.parseBoolean(properties.getProperty("disable-drowning-damage"));
 		   DisablePotionDamage = Boolean.parseBoolean(properties.getProperty("disable-potion-damage"));
 		   DisableWaterFlow = Boolean.parseBoolean(properties.getProperty("disable-water-flow"));
 		   DisableLavaFlow = Boolean.parseBoolean(properties.getProperty("disable-lava-flow"));
 		   
 		  //Disable lava, fire and water spread on custom blocks
 	    //   DisallowFireSpreadBlocks = toBlockIDSet(properties.getProperty("disallowed-fire-spread-blocks"));
 	    //   DisallowLavaSpreadBlocks = toBlockIDSet(properties.getProperty("disallowed-lava-spread-blocks"));
 	    //   DisallowWaterSpreadBlocks = toBlockIDSet(properties.getProperty("disallowed-water-spread-blocks"));
            
 	       // Other Random Stuff
            DisablePhysicsGravel = Boolean.parseBoolean(properties.getProperty("disable-gravel-physics"));
            DisablePhysicsSand = Boolean.parseBoolean(properties.getProperty("disable-sand-physics"));
            BlockLeafDecay = Boolean.parseBoolean(properties.getProperty("disable-leaf-decay"));
            leavetypes = properties.getProperty("Leave-DamageValues");
            BlockCowMilking = Boolean.parseBoolean(properties.getProperty("block-cow-milking"));
            BlockEating = Boolean.parseBoolean(properties.getProperty("block-eating"));
            BlockDispenser = Boolean.parseBoolean(properties.getProperty("block-dispensers"));
            FarmlandDestroy = Boolean.parseBoolean(properties.getProperty("block-farmland-physics"));
            DisableEntityDespawning = Boolean.parseBoolean(properties.getProperty("disable-entity-despawning"));
            DisableLightningStrike = Boolean.parseBoolean(properties.getProperty("disable-lightning-strike"));
            DisableInventories = Boolean.parseBoolean(properties.getProperty("disable-inventories"));
            DisableItemPickup = Boolean.parseBoolean(properties.getProperty("disable-item-pickup"));
            DisableItemDropping = Boolean.parseBoolean(properties.getProperty("disable-item-dropping"));
            DisableWeather = Boolean.parseBoolean(properties.getProperty("disable-weather"));
            DisableThunderWeather = Boolean.parseBoolean(properties.getProperty("disable-thunder-weather"));
            DisableNightTime = Boolean.parseBoolean(properties.getProperty("disable-night-time"));
            AlwaysRaining = Boolean.parseBoolean(properties.getProperty("always-raining"));
            ClassicWater = Boolean.parseBoolean(properties.getProperty("classic-water"));
            DisableEndermanBlockPickup = Boolean.parseBoolean(properties.getProperty("disable-enderman-pickup"));     
            DisableDayTime = Boolean.parseBoolean(properties.getProperty("disable-day-time"));
            DisableWolfTame = Boolean.parseBoolean(properties.getProperty("prevent-wolf-tame"));
            Instandtame = Boolean.parseBoolean(properties.getProperty("instant-tame"));
            DisablePigZombificatio = Boolean.parseBoolean(properties.getProperty("disable-pig-zombification"));
            teleporttootherworld = Boolean.parseBoolean(properties.getProperty("Teleport-Player-OnReachLayer"));
            world = properties.getProperty("World-To-Teleport-Player-To");
            try{level = Integer.parseInt(properties.getProperty("Y-Level"));}catch(NumberFormatException nfe){log.info("[WorldTools] - The Y-Level must be an number!"); level = -1;}
            BlockIceMelting = Boolean.parseBoolean(properties.getProperty("block-Ice-physics"));
            BlockWaterFreezing = Boolean.parseBoolean(properties.getProperty("block-Water-physics"));
            BlockLavaObsidian = Boolean.parseBoolean(properties.getProperty("block-Lava-physics"));
            
 
         // ExactSpawn = Boolean.parseBoolean(properties.getProperty("disable-exact-spawn"));
            
            rlsponge = Boolean.parseBoolean(properties.getProperty("use-sponge"));
     	   try{ rad = Integer.parseInt(properties.getProperty("sponge-radius"));}catch(NumberFormatException nfe){log.info("[WorldTools] - The sponge radius must be an number!"); rad = 2;}
        }
    
    /**
     * Block onExplosion related stuff
     * @author Glacksy
     */
 
    public boolean onExplode(Block block)
    {
 	 if ((BlockTntExplosion) && (block.getStatus() == 1)) {
 	   return true;
 	 }
 	 if ((BlockCreeperExplosion) && (block.getStatus() == 2)) {
 		 if (!DisableCreeperDamage){
 			for (Player player : etc.getServer().getPlayerList()) {
 			    if (WorldToolsVoids.isInExplosionRadius(player, block)) {
 			     player.setHealth(player.getHealth() - WorldToolsVoids.calculateDamage(player, block));
 			    if (player.getHealth() < 1) {
 			        player.dropInventory();}}}}  //TODO: force the drop to keep enchantments
 	   return true;
 	 }
      if ((BlockGhastExplosion) && (block.getStatus() == 3)) {
        return true;
      }
      return false;
    }
    
    /**
     * Block Portal Creating
     * @author Glacksy
     */
    public boolean onPortalCreate(Block[][] blocks)
    {
 	if (BlockPortalCreating)
 	{
 	  return true;
     }
 	return false;
    }
    
    /**
     * Block Portal Destroying
     */
    public boolean onPortalDestroy(Block[][] blocks)
    {
 	if (BlockPortalDestroying)
 	{
 		return true;
     }
 	return false;
    }
 
    /**
     * Prevent enderman from picking up blocks
     * TODO: make a list of blocks the enderman can pickup
     * glack enderman can not pickup all blocks!
     */
    public boolean onEndermanPickup(Enderman entity, Block block)
    {
     if (DisableEndermanBlockPickup)
     {
 	   return true;
     }
    return false;
    }
 
 
 
    /**
     * Block different types of fire stuff
     * @author Glacksy
     */
    public boolean onIgnite(Block block, Player player)
    {
      if ((BlockLavaFire) && (block.getStatus() == 1)) {
        return true;
      }
      if ((BlockLighter) && (block.getStatus() == 2))
      {
        return true;
      }
      if ((BlockFireSpread) && (block.getStatus() == 3)) {
        return true;
      }
      if ((BlockFireBlockDestory) && (block.getStatus() == 4)) {
          return true;
      }
      if ((BlockLightningFire) && (block.getStatus() == 5)) {
          return true;
      }
      return false;
    }
    
    /**
     * Disable water and lava flow with config
     * @author Glacksy
     */
    public boolean onFlow(Block blockFrom, Block blockTo)
    {
 	if ((DisableWaterFlow) && (blockFrom.getType() == 8 || blockFrom.getType() == 9))
    {
 		return true;
    }
 	if ((DisableLavaFlow) && (blockFrom.getType() == 10 || blockFrom.getType() == 11))
    {
 		return true;
    }
        if ((ClassicWater) && (blockFrom.getType() == 8) || (blockFrom.getType() == 9)) {
 	      int bB = blockFrom.getWorld().getBlockIdAt(blockFrom.getX(), blockFrom.getY() - 1, blockFrom.getZ()); 
 	      if ((bB != 0) && (bB != 8) && (bB != 9)) { 
 	    	  blockFrom.getWorld().setBlockAt(9, blockFrom.getX(), blockFrom.getY(), blockFrom.getZ());
 	        return false;
 	    }
        }
 	return false;
    }
 	/**
 	 * 
 	 * TODO: damage values support
 	 */
 /*	if (DisallowLavaSpreadBlocks != null && blockFrom.getType() == 10 || blockFrom.getType() == 11) {
 		int a1337 = blockFrom.getWorld().getBlockIdAt(blockTo.getX(), blockTo.getY() - 1, blockTo.getZ());
         if (!DisallowLavaSpreadBlocks.contains(a1337)) {
             return true;  //TODO: Test this code, added multiworld used to be etc.getServer
      //   }
     } */
 //	return false;
   // }
 	/**
 	 * TODO: Multiworld support and damage values support (damage values impossibru :O)
 	 * Does ONLY support default world at the moment
 	 */
 	/*if (DisallowWaterSpreadBlocks != null && blockFrom.getType() == 8 || blockFrom.getType() == 9) {
 		int a1337 = etc.getServer().getWorld(0).getBlockIdAt(blockTo.getX(), blockTo.getY() - 1, blockTo.getZ());
         if (!DisallowLavaSpreadBlocks.contains(a1337)) {
             return true;
         }
     }
 	return false;
    }*/
    
    /**
     * List of damage types which can be disabled
     */
    public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount)
    {//hehehe
      if (defender.isPlayer()) {
        defender.getPlayer();
 
        if ((DisableFallDamage) && (type == PluginLoader.DamageType.FALL)) {
          return true;
        }
        if ((DisableLavaDamage) && (type == PluginLoader.DamageType.LAVA)) {
          return true;
        }
        if ((DisableFireDamage) && ((type == PluginLoader.DamageType.FIRE))) { 
          return true;
        }
        if ((DisableFireTickDamage) && (type == PluginLoader.DamageType.FIRE_TICK)) {
            return true;
         }
        if ((DisableCactusDamage) && (type == PluginLoader.DamageType.CACTUS)) {
            return true;
        }
        if ((DisableEntityDamage) && (type == PluginLoader.DamageType.ENTITY)) {
            return true;
        }
        if ((DisableCreeperDamage) && (type == PluginLoader.DamageType.CREEPER_EXPLOSION)) {
            return true;
        }
        if ((DisableLightningDamage) && (type == PluginLoader.DamageType.LIGHTNING)) {
            return true;
        }
        if ((DisableStarvationDamage) && (type == PluginLoader.DamageType.STARVATION)) {
            return true;
          }
        if ((DisableSuffocationDamage) && (type == PluginLoader.DamageType.SUFFOCATION)) {
           return true;
          }
        if ((DisableWaterDamage) && (type == PluginLoader.DamageType.WATER)) {
            return true;
         }
        if ((DisablePotionDamage) && (type == PluginLoader.DamageType.POTION)) {
            return true;
         }
    }	      
      return false;
    }
    
    /**
     * Disable block physics
     */
    public boolean onBlockPhysics(Block block, boolean placed)
    {
      if ((DisablePhysicsGravel) && (block.getType() == 13)) {
        return true;
      }
 
      if ((DisablePhysicsSand) && (block.getType() == 12)) {
        return true;
      }
   return false;
    }
    
    /**
     * Disable LeafDecay
     */
    public boolean onLeafDecay(Block block)
    {
 	   if (BlockLeafDecay) { 
 		   String[] damages = leavetypes.split(",");
 		   List<String> damagess = Arrays.asList(damages);
 		   if (damagess.contains(block.getData())){
 			   return true;
 		   }
 	    }
 	   return false;   
    }
    
    /**
     * Block players from eating
     * This is also pointless but might be useful.
     */
    public boolean onEat(Player player,Item item)
    {
 	   if (BlockEating && !player.canUseCommand("/worldtools") && !player.canUseCommand("/canEat")) {
 		   return true;
 	   }
 	  return false; 
    }
    
    /**
     * Block Dispenser
     * This is also pointless
     */
    public boolean onDispense(Dispenser dispenser, BaseEntity tobedispensed)
    {
 	   if (BlockDispenser) {
 		   return true; //block it
 	   }
 	  return false;  // enable it
    }
    
    /**
     * Block any player from destrying farmland 
     * by jumping or walking on it.
     */
    public boolean onBlockUpdate(Block block)
    {
 	   if ((FarmlandDestroy) && (block.getType() == 60)) {
 		   return true;
 	   }
 	    if (BlockIceMelting && block.getType() == 79){
 	    	return true;
 	    }
 	    if (BlockWaterFreezing && block.getType() == 9){
 	    	return true;
 	    }
 	    if (BlockLavaObsidian && block.getType() == 11){
 	    	return true;
 	    }
 	  return false; 
    }
    
    /**
     * Disable mob despawning
     * This can cause serverside issues and lag
     * But the possibility is still here.. 
     */
    public boolean onEntityDespawn(BaseEntity entity)
    {
 	   if (DisableEntityDespawning){  //TODO: add a list of mobs which shouldnt despawn
 		   return true;
 	   }
 	  return false; 
    }
    
    /**
     * This is pointless but i prefer having the possibility.
     * Disable Cow Milking
     */
    public boolean onCowMilk(Player player, Mob cow)
    {
 	   if (BlockCowMilking && !player.canUseCommand("/worldtools") && !player.canUseCommand("/canMilk")) {
 		   return true;
 	   }
 	  return false; 
    }
    
    /**
     * Disable lightning strikes
     */
    public boolean onLightningStrike(BaseEntity entity)
    {
 	   if (DisableLightningStrike) {
 		   return true;
 	   }
 	   
 	   if ((DisablePigZombificatio) && (entity.isAnimal())) {
 		   entity.destroy(); //destory the entity, should only destory pig but couldnt figure out how :o
 		   return true;
 	   }
 	   
 	  return false; 
    }
    
    /**
     * Disable Inventories
     * NOTE: THIS DOES NOT WORK FOR PLAYER INVENTORY ONLY DISPENSER,FURNACE AND SUCH
     * TODO: add a inventory list you want to disable (you do it lol)
     */
    public boolean onOpenInventory(Player player,Inventory inventory){
 	   if (DisableInventories) { 
 		   if (!player.canUseCommand("/ignoreinv") && !player.canUseCommand("/worldtools")){
 			   if (inventory.getContentsSize() == 27){
 				   player.sendMessage("cYou cant open this inventory!");
 				   return true;
 			   }
 			   if (inventory.getContentsSize() == 1){
 				   player.sendMessage("cYou can't open this inventory!");
 				   return true;
 			   }
 			   if (inventory.getContentsSize() == 3){
 				   player.sendMessage("cYou can't open this inventory!");
 				   return true;
 			   }
 			   if (inventory.getContentsSize() == 54){
 				   player.sendMessage("cYou can't open this inventory!");
 				   return true;
 			   }
 		   }
 		   return true;
 	   }
 	  return false; 
    }
    
    
    /**
     * Disable item pickup
     * TODO: blacklist of items which shouldnt be picked up with ignore command(cant work with damagevalues :))
     * pickup blacklist should be in props, should work with damage values
     */
    public boolean onItemPickUp(Player player, ItemEntity item)
    {
 	   if (DisableItemPickup && !player.canUseCommand("/worldtools") && !player.canUseCommand("/canPickup")) {
 		   return true;
 	   }
 	  return false; 
    }
    
    /**
     * Disable item dropping
     * TODO: blacklist of items which shouldnt be dropped with ignore command(cant work with damagevalues :))
     * Same as above
     */
    public boolean onItemDrop(Player player, ItemEntity item)
    {
 	   if (DisableItemDropping && !player.canUseCommand("/worldtools") && !player.canUseCommand("/canDrop")) {
 		   return true;
 	   }
 	  return false; 
    }
    
    /**
     * Disable weather from changing, this can cause problems if you enable it when its raining
     * cause then it will rain forever.
     */
    public boolean onWeatherChange(World world, boolean newValue)
    {
 	   if (DisableWeather) {
 		   if (etc.getServer().getDefaultWorld().isRaining()){
 		   etc.getServer().getDefaultWorld().setRaining(false);}
 		   return true;
 	   }
 	   if (AlwaysRaining) { 
 		   if (etc.getServer().getDefaultWorld().isRaining()){   //have to clean and fix this afterwards
 			   etc.getServer().getDefaultWorld().setRaining(true);} //TODO:  test this
 		   return true;
 	   }
 	  return false; 
    }
    
    /**
     * Disable thunder weather from happening
     * TODO: Set weather thunder to normal weather only if the weather is thunder.
     */
    public boolean onThunderChange(World world, boolean newValue)
    {
 	   if (DisableThunderWeather)  {
 	       if (etc.getServer().getDefaultWorld().isThundering()){
 	       etc.getServer().getDefaultWorld().setThundering(false);}
 		   return true;
 	   }
 	  return false; 
    }
    
    /**
     * @author spenk
     * Prevent Changing of time.
     */
    public boolean onTimeChange(World world, long newValue)
    {
 	   if (DisableNightTime){
 		   if (etc.getServer().getDefaultWorld().getTime() < 18000){
 		   etc.getServer().getDefaultWorld().setTime(0);
 		   return false;
 		   }
 	   }
 	   if (DisableDayTime){
 		   if (etc.getServer().getDefaultWorld().getTime() > 18000){
 		   etc.getServer().getDefaultWorld().setTime(18000);
 		   return false;
 		   } 
 	   }
 	  return false;
    }
   
      /**
       * sponge feature code
       * @author spenk
       */
 public boolean onBlockCreate(Player player,Block block,Block blockClicked,int itemInHand){
	if (player.canUseCommand("/worldtools")){
 		if (block != null){
 		int type = block.getType();
 	if (type == 19){
 		if (rlsponge == false){
 			WorldToolsVoids.airout(block,rad);
 			return false;
 		}
 	}
 	}
 	return false;
 	}
 	return false;
 }
 
 public boolean onBlockDestroy(Player player,Block block){
	if (block.getType() == 19 && player.canUseCommand("/worldtools")){
 		if (rlsponge == false){
 			WorldToolsVoids.replacewater(block,rad);
 		return false;
 		}
 	}
 	return false;
 }
 
 public boolean onBlockRightClick(Player player,Block block,Item itemInHand){
	if (block.getType() == 19 && player.canUseCommand("/worldtools")){
 		if (rlsponge){
 			if(WorldToolsVoids.iswater(block, rad)){
 				WorldToolsVoids.airout(block,rad);
 				return false;
 			}else{
 				WorldToolsVoids.fillarea(block,rad);
 				return false;
 			}
 		}
 		return false;
 	}
 	return false;
 }
 public PluginLoader.HookResult onTame(Player player,Mob wolf,boolean shouldSucceed){
 	if (DisableWolfTame){
 		if (player.canUseCommand("/worldtools")){
 		return PluginLoader.HookResult.PREVENT_ACTION;
 	}else{
 		return PluginLoader.HookResult.DEFAULT_ACTION;
 	}
 }
 	if (Instandtame){
 		return PluginLoader.HookResult.ALLOW_ACTION;
 	}
 	return PluginLoader.HookResult.DEFAULT_ACTION;
 }
 public void onPlayerMove(Player player,Location from,Location to){
 	if (teleporttootherworld){
 		if (to.y > level){
 		if (world.equalsIgnoreCase("Nether")){
 			player.switchWorlds(-1);
 			player.setY(player.getY()+5);
 		}
 		if (world.equalsIgnoreCase("End")){
 			player.switchWorlds(1);
 			player.setY(player.getY()+5);
 		}
 		}
 	}
 }
 public boolean onHealthChange(Player player,int oldValue,int newValue){
 	
 	if ((TpHomeOnDeath) || (oldValue <= 0) || (newValue == 20)) {
         Warp loc = etc.getDataSource().getHome(player.getName());
         if (loc != null) {
           player.teleportTo(loc.Location);
         }
         return true;
    }
 	
 	if (newValue < 1){
 		if (kickondeath){
 			if (!player.canUseCommand("/worldtools")){
 			reason = reason.replace("&", "");
 		player.kick(reason);	
 		}	
 	  }
 	}
 	return false;
 }
 /**
  * @function keep chunks loaded
  * @author Spenk
  */
 public boolean onSignChange(Player player,Sign sign){
 	PropertiesFile f = new PropertiesFile("plugins/config/WorldTools/WorldToolsChunks.properties");
 	if (sign.getText(1).equalsIgnoreCase("[LoadChunk]")){
 		if (player.canUseCommand("/worldTools")){
 			sign.setText(1, "[LoadChunk]");
 			sign.update();
 			Chunk c = sign.getWorld().getChunk(sign.getBlock());
 			if (c == null){
 				log.info("Chunk not found!");
 				player.notify("Chunk not found!");
 				return false;
 			}
 			if (f.containsKey(c.getX()+","+c.getZ())){
 				player.notify("There is already an loadsign on this chunk!");
 				return false;
 			}
 			WorldToolsVoids.savechunk(c.getX()+","+c.getZ(),sign.getX()+","+sign.getY()+","+sign.getZ());
 			player.sendMessage("2This chunk will be forever loaded from now!");
 			return false;
 		}
 		player.notify("You are not allowed to place these signs!");
 		return true;
 	}
 	return false;
 }
 
 public void onChunkUnload(Chunk chunk){
 	PropertiesFile f = new PropertiesFile("plugins/config/WorldTools/WorldToolsChunks.properties");
 	if (f.containsKey(chunk.getX()+","+chunk.getZ())){
 		String[] ia = f.getProperty(chunk.getX()+","+chunk.getZ()).split(",");
 		int x =Integer.parseInt(ia[0]);
 		int y =Integer.parseInt(ia[1]);
 		int z =Integer.parseInt(ia[2]);
 		if (chunk.getBlockIdAt(x, y, z) == 63 || chunk.getBlockIdAt(x, y, z) == 68){
 			Sign s = (Sign)chunk.getWorld().getComplexBlock(x, y, z);
 			if (s.getText(1).equalsIgnoreCase("[LoadChunk]")){
 			chunk.getWorld().loadChunk(chunk.getX(),chunk.getZ());
 			}
 		}
 	}
 }
 public boolean onBlockBreak(Player player,Block block){
 	PropertiesFile f = new PropertiesFile("plugins/config/WorldTools/WorldToolsChunks.properties");
 	if (block.getType() == 63 || block.getType() == 68){
 		Sign sign = (Sign)block.getWorld().getComplexBlock(block.getX(), block.getY(), block.getZ());
 		if (sign.getText(1).equalsIgnoreCase("[LoadChunk]")){
 			if (player.canUseCommand("/worldtools")){
 			Chunk chunk = block.getWorld().getChunk(block);
 			f.removeKey(chunk.getX()+","+chunk.getZ());
 			player.sendMessage("4Sign sucsessfully destroyed!");
 			return false;
 		}
			player.notify("You cant break this sign block!");
 			return true;
 		}
 	}
 	return false;
 }
 
 }
 //TODO: add multiworld support for all features which needs it <--- cant be done yet with the current methods =/
    
