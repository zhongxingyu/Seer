 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class WorldToolsListener extends PluginListener {
 	
 	private static Logger log = Logger.getLogger("Minecraft");
 	private static Object logger;
 	
 	ArrayList<String> god = new ArrayList<String>();
 	ArrayList<String> frozen = new ArrayList<String>();
 
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
 		   private static boolean DisableThunderWeather;
 		   private static boolean DisableNightTime;
		   private static boolean DisableEndermanBlockPickup;
 		   private static boolean AlwaysRaining;
 		   private static boolean ClassicWater;
 		   public static int rad;
 		   public static boolean rlsponge;
 		   private static String leavetypes;
 		   private Set<Integer> DisallowFireSpreadBlocks;
 		   private Set<Integer> DisallowLavaSpreadBlocks;
 		 //  private Set<Integer> DisallowWaterSpreadBlocks;
 		   
 	    
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
 				log.info("[WorldTools] could not create PropertiesFile!");
 			}
 	    	makeSettingsFile("plugins/config/WorldTools/WorldTools.properties");
 	    	loadprops("plugins/config/WorldTools/WorldTools.properties");
 	    	}
 	    	loadprops("plugins/config/WorldTools/WorldTools.properties");
 	    }
     
     /**
      * Write the properties file and all its Settings
      * @author glacksy
      */
 
    private static void makeSettingsFile(String file) {
 	try {
 		BufferedWriter out = new BufferedWriter(new FileWriter(file));
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
 		out.write("#Block creepers from causing world and player damage"); out.newLine();
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
 		out.write("#Disable Creeper damage"); out.newLine();
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
 		out.write("#Leave DamageValues");out.newLine();
 		out.write("#these are leaves wich arnt allowed to decay");
 		out.write("Leave-DamageValues=0,1,2,3");
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
 		out.write("#block players from destrying farmland by walking and jumping on it"); out.newLine();
 		out.write("block-farmland-physics=false"); out.newLine();
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
 		out.write("#Sponge radius");out.newLine();
 		out.write("sponge-radius=2");out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Realistic Sponge");out.newLine();
 		out.write("use-sponge=false");out.newLine();
 		out.write(" "); out.newLine();
 	//	out.write("#Disallow water spread on the set blocks.");out.newLine();
 	//	out.write("#Note: This Does Not Support Damagevalues Yet.");out.newLine();
 	//	out.write("disallowed-water-spread-blocks=50,76");out.newLine();
 	//	out.write(" "); out.newLine();
 		out.write("#Disallow lava spread on the set blocks.");out.newLine();
 		out.write("#Note: This Does Not Support Damagevalues Yet.");out.newLine();
 		out.write("disallowed-lava-spread-blocks=2,3");out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#Disallow fire spread on the set blocks.");out.newLine();
 		out.write("#Note: This Does Not Support Damagevalues Yet.");out.newLine();
 		out.write("disallowed-lava-spread-blocks=5,17,18,30,35");out.newLine();
 		out.write(" "); out.newLine();
 		out.write("#ExactSpawn"); out.newLine();
 		out.write("enable-exact-spawn=true");
 		out.write(" "); out.newLine();
 		out.write("#Use classic water"); out.newLine();
 		out.write("classic-water=false");
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
 		   DisallowFireSpreadBlocks = toBlockIDSet(properties.getProperty("disallowed-fire-spread-blocks"));
 	       DisallowLavaSpreadBlocks = toBlockIDSet(properties.getProperty("disallowed-lava-spread-blocks"));
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
            
         // ExactSpawn = Boolean.parseBoolean(properties.getProperty("disable-exact-spawn"));
            
            rlsponge = Boolean.parseBoolean(properties.getProperty("use-sponge"));
     	   try{ rad = Integer.parseInt(properties.getProperty("sponge-radius"));}catch(NumberFormatException nfe){log.info("[WorldTools] - The sponge radius must be an number!");}
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
     /* if ((BlockCreeperExplosion) && (block.getStatus() == 2)) {
        return true;
      }*/
 	 if ((BlockCreeperExplosion) && (block.getStatus() == 2)) {
 			for (Player player : etc.getServer().getPlayerList()) {
 			    if (WorldToolsVoids.isInExplosionRadius(player, block)) {
 			     player.setHealth(player.getHealth() - WorldToolsVoids.calculateDamage(player, block));
 			    if (player.getHealth() < 1) {
 			        player.dropInventory();}}}  //TODO: force the drop to keep enchantments
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
      /**
       * Borrowed some of the disallowfirespreadblocks code from WorldGuard
       * @Author sk89q
       */
      if ((DisallowFireSpreadBlocks != null && block.getStatus() == 3)) 
      {
          int x = block.getX();
          int y = block.getY();
          int z = block.getZ();
          if (DisallowFireSpreadBlocks.contains(etc.getServer().getWorld(0).getBlockIdAt(x, y - 1, z))
                  || DisallowFireSpreadBlocks.contains(block.getWorld().getBlockIdAt(x + 1, y, z))
                  || DisallowFireSpreadBlocks.contains(block.getWorld().getBlockIdAt(x - 1, y, z))
                  || DisallowFireSpreadBlocks.contains(block.getWorld().getBlockIdAt(x, y, z - 1))
                  || DisallowFireSpreadBlocks.contains(block.getWorld().getBlockIdAt(x, y, z + 1))) 
      {
              return true;
          }
      }
      
      return false;
    }
    
    /**
     * Disable water and lava flow with config
     * @author Glacksy
     */
    public boolean onFlow(Block blockFrom, Block blockTo)
    {
 	World world = blockTo.getWorld();
 	
 	if ((DisableWaterFlow) && (blockFrom.getType() == 8 || blockFrom.getType() == 9))
    {
 		return true;
    }
 	if ((DisableLavaFlow) && (blockFrom.getType() == 10 || blockFrom.getType() == 11))
    {
 		return true;
    }
 	if ((ClassicWater) && (blockFrom.getType() == 8) || (blockFrom.getType() == 9)) {
 	      int bB = world.getBlockIdAt(blockFrom.getX(), blockFrom.getY() - 1, blockFrom.getZ()); 
 	      if ((bB != 0) && (bB != 8) && (bB != 9)) { 
 	      world.setBlockAt(9, blockFrom.getX(), blockFrom.getY(), blockFrom.getZ());
 	        return false;
 	      }
 	    }
 	/**
 	 * 
 	 * TODO: damage values support
 	 */
 	if (DisallowLavaSpreadBlocks != null && blockFrom.getType() == 10 || blockFrom.getType() == 11) {
 		int a1337 = blockFrom.getWorld().getBlockIdAt(blockTo.getX(), blockTo.getY() - 1, blockTo.getZ());
         if (!DisallowLavaSpreadBlocks.contains(a1337)) {
             return true;  //TODO: Test this code
         }
     }
 	return false;
    }
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
    //   public boolean onCommand(Player Player, int fromid, int toid, int radius, String[]cmd)
    /**
     * Drain and command part
     * @author Glacksy
     */
    public boolean onCommand(Player Player, String[] cmd) {
 	   if ((cmd[0].equalsIgnoreCase("/drain")) && (Player.canUseCommand("/worldtools"))) {
 	       int dist = 0;
 	       if (cmd.length == 2) try { dist = Integer.parseInt(cmd[1]); } catch (Throwable localThrowable) {
 	         } if (dist == 0) {
 	         Player.sendMessage("6#[-----Drain Help-----]#");
 	         Player.sendMessage("a/drain <radius> - Drain water/lava");
 	         Player.sendMessage("a/drainwater <radius> - Drain water");
 	         Player.sendMessage("a/drainlava <radius>  - Drain lava");
 	         Player.sendMessage("a/ext <radius> - Remove fire"); return true;
 	       }
 	         int radius = 0;
 	       try {radius = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /drain <radius>");return true;}
 	       int xmin = (int)Player.getX()-radius;
 	         int xmax = (int)Player.getX()+radius;
 	         int ymin = (int)Player.getY()-radius;
 	         int ymax = (int)Player.getY()+radius;
 	         int zmin = (int)Player.getZ()-radius;
 	         int zmax = (int)Player.getZ()+radius;
 	        
 	         for (int x = xmin; x <= xmax; x++) {
 	                 for (int y = ymin; y <= ymax; y++) {
 	                         for (int z = zmin; z <= zmax; z++) {    
 	                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 8){Player.getWorld().setBlockAt(95, x, y, z);}
 	                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(9, x, y, z);}
 	                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 10){Player.getWorld().setBlockAt(95, x, y, z);}
 	                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(11, x, y, z);}
 	                         }
 	                       }
 	                     }
 	         Player.sendMessage("aLava & Water Successfully Drained!");
 	         return true;
 	                   }
 
 
      if ((cmd[0].equalsIgnoreCase("/drainlava")) && (Player.canUseCommand("/worldtools"))) {
        int dist = 0;
        if (cmd.length == 2) try { dist = Integer.parseInt(cmd[1]); } catch (Throwable localThrowable1) {
          } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /drainlava <radius>"); return true; }
          int radius = 0;
          try {radius = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /drainlava <radius>");return true;}
          int xmin = (int)Player.getX()-radius;
          int xmax = (int)Player.getX()+radius;
          int ymin = (int)Player.getY()-radius;
          int ymax = (int)Player.getY()+radius;
          int zmin = (int)Player.getZ()-radius;
          int zmax = (int)Player.getZ()+radius;
         
          for (int x = xmin; x <= xmax; x++) {
                  for (int y = ymin; y <= ymax; y++) {
                          for (int z = zmin; z <= zmax; z++) {    
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 11){Player.getWorld().setBlockAt(95, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(10, x, y, z);}
                          }
                        }
                      }
        Player.sendMessage("aLava Successfully Removed!");
        return true;
      }
 
      if ((cmd[0].equalsIgnoreCase("/drainwater")) && (Player.canUseCommand("/worldtools"))) {
        int dist = 0;
        if (cmd.length == 2) try { dist = Integer.parseInt(cmd[1]); } catch (Throwable localThrowable2) {
          } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /drainwater <radius>"); return true; }
          int radius = 0;
          try {radius = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /drainwater <radius>");return true;}
          int xmin = (int)Player.getX()-radius;
          int xmax = (int)Player.getX()+radius;
          int ymin = (int)Player.getY()-radius;
          int ymax = (int)Player.getY()+radius;
          int zmin = (int)Player.getZ()-radius;
          int zmax = (int)Player.getZ()+radius;
         
          for (int x = xmin; x <= xmax; x++) {
                  for (int y = ymin; y <= ymax; y++) {
                          for (int z = zmin; z <= zmax; z++) {    
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 9){Player.getWorld().setBlockAt(95, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(8, x, y, z);}
                          }
                        }
                      }
        Player.sendMessage("aWater Successfully Removed!");
        return true;
      }
 
      if ((cmd[0].equalsIgnoreCase("/ext")) && (Player.canUseCommand("/worldtools"))) {
        int dist = 0;
        if (cmd.length == 2) try { dist = Integer.parseInt(cmd[1]); } catch (Throwable localThrowable3) {
          } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /ext <radius>"); return true; }
          int radius = 0;
          try {radius = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /ext <radius>");return true;}
          int xmin = (int)Player.getX()-radius;
          int xmax = (int)Player.getX()+radius;
          int ymin = (int)Player.getY()-radius;
          int ymax = (int)Player.getY()+radius;
          int zmin = (int)Player.getZ()-radius;
          int zmax = (int)Player.getZ()+radius;
         
          for (int x = xmin; x <= xmax; x++) {
                  for (int y = ymin; y <= ymax; y++) {
                          for (int z = zmin; z <= zmax; z++) {    
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 51){Player.getWorld().setBlockAt(0, x, y, z);}
                          }
                        }
                      }
        Player.sendMessage("aFire Successfully Extinguished");
        return true;
      }
 
      if ((cmd[0].equalsIgnoreCase("/melt")) && (Player.canUseCommand("/worldtools"))) {
        int dist = 0;
        if (cmd.length == 2) try { dist = Integer.parseInt(cmd[1]); } catch (Throwable localThrowable4) {
          } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /melt <radius>"); return true; }
          int radius = 0;
          try {radius = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /melt <radius>");return true;}
          int xmin = (int)Player.getX()-radius;
          int xmax = (int)Player.getX()+radius;
          int ymin = (int)Player.getY()-radius;
          int ymax = (int)Player.getY()+radius;
          int zmin = (int)Player.getZ()-radius;
          int zmax = (int)Player.getZ()+radius;
         
          for (int x = xmin; x <= xmax; x++) {
                  for (int y = ymin; y <= ymax; y++) {
                          for (int z = zmin; z <= zmax; z++) {    
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 78){Player.getWorld().setBlockAt(0, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 79){Player.getWorld().setBlockAt(8, x, y, z);}
                          }
                        }
                      }
        Player.sendMessage("aThe Snow & Ice has been successfully melted!");
        return true;
      }
 
      if ((cmd[0].equalsIgnoreCase("/snow")) && (Player.canUseCommand("/worldtools"))) {
        int dist = 0;
        if (cmd.length == 2) try { dist = Integer.parseInt(cmd[1]); } catch (Throwable localThrowable5) {
          } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /snow <radius>"); return true; }
          int radius = 0;
          try {radius = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /snow <radius>");return true;}
          int xmin = (int)Player.getX()-radius;
          int xmax = (int)Player.getX()+radius;
          int ymin = (int)Player.getY()-radius;
          int ymax = (int)Player.getY()+radius;
          int zmin = (int)Player.getZ()-radius;
          int zmax = (int)Player.getZ()+radius;
         
          for (int x = xmin; x <= xmax; x++) {
                  for (int y = ymin; y <= ymax; y++) {
                          for (int z = zmin; z <= zmax; z++) {    
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 0){Player.getWorld().setBlockAt(78, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 8){Player.getWorld().setBlockAt(79, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 9){Player.getWorld().setBlockAt(79, x, y, z);}
                          }
                        }
                      }
        Player.sendMessage("a Snow placed & water frozen!");
        return true;
      }
 
      if ((cmd[0].equalsIgnoreCase("/waterfix")) && (Player.canUseCommand("/worldtools"))) {
        int dist = 0;
        if (cmd.length == 2) try { dist = Integer.parseInt(cmd[1]); } catch (Throwable localThrowable6) {
          } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /waterfix <radius>"); return true; }
          int radius = 0;
          try {radius = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /water fix <radius>");return true;}
          int xmin = (int)Player.getX()-radius;
          int xmax = (int)Player.getX()+radius;
          int ymin = (int)Player.getY()-radius;
          int ymax = (int)Player.getY()+radius;
          int zmin = (int)Player.getZ()-radius;
          int zmax = (int)Player.getZ()+radius;
         
          for (int x = xmin; x <= xmax; x++) {
                  for (int y = ymin; y <= ymax; y++) {
                          for (int z = zmin; z <= zmax; z++) {    
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 8){Player.getWorld().setBlockAt(95, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 9){Player.getWorld().setBlockAt(95, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(9, x, y, z);}
                          }
                        }
                      }
        Player.sendMessage("a Water Successfully Fixed!");
        return true;
      }
 
      if ((cmd[0].equalsIgnoreCase("/lavafix")) && (Player.canUseCommand("/worldtool"))) {
        int dist = 0;
        if (cmd.length == 2) try { dist = Integer.parseInt(cmd[1]); } catch (Throwable localThrowable7) {
          } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /lavafix <radius>"); return true; }
          int radius = 0;
          try {radius = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /lavafix <radius>");return true;}
          int xmin = (int)Player.getX()-radius;
          int xmax = (int)Player.getX()+radius;
          int ymin = (int)Player.getY()-radius;
          int ymax = (int)Player.getY()+radius;
          int zmin = (int)Player.getZ()-radius;
          int zmax = (int)Player.getZ()+radius;
         
          for (int x = xmin; x <= xmax; x++) {
                  for (int y = ymin; y <= ymax; y++) {
                          for (int z = zmin; z <= zmax; z++) {    
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 10){Player.getWorld().setBlockAt(95, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 11){Player.getWorld().setBlockAt(95, x, y, z);}
                     if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(9, x, y, z);}
                          }
                        }
                      }
        Player.sendMessage("a Lava Successfully Fixed!");
        return true;
      }
      
      if ((cmd[0].equalsIgnoreCase("/lighter")) && (Player.canUseCommand("/lighter") || (Player.canUseCommand("/worldtools")))) {
        int dist = 0;
        if (cmd.length == 0) try { dist = Integer.parseInt(cmd[0]); } catch (Throwable localThrowable8) {
          } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /lighter"); return true;
        }
        Player.giveItem(260, 1);
        Player.sendMessage("a No smoke without 6fire!"); 
        return true;
      }
      
      if (cmd[0].equalsIgnoreCase("/cmob") && (Player.canUseCommand("/cmob") || (Player.canUseCommand("/worldtools")))) {
          try {
            int r = Integer.valueOf(cmd[1]).intValue();
            WorldToolsVoids.cMob(r);
            Player.sendMessage("aCleared mobs");
            return true;
          } catch (Exception e) {
            Player.sendMessage("cWrong syntax! Usage: /cmob <radius>");
            return true;
          }
        }
      
      /**
       * killmobs
       */
      if (cmd[0].equalsIgnoreCase("/killmobs") && (Player.canUseCommand("/killmobs") || (Player.canUseCommand("/worldtools")))) {
          int mobcount = Player.getWorld().getMobList().size();
          for (int i = 0; i < mobcount; i++) {
            ((Mob)Player.getWorld().getMobList().get(i)).setHealth(0);
          }
          Player.sendMessage("aYou Killed " + mobcount + " Mobs.");
          return true;
        }
      
      /**
       * replace feature
       * 
       */ 
      if (cmd[0].equalsIgnoreCase("/wreplace") && (Player.canUseCommand("/wreplace") || (Player.canUseCommand("/worldtools")))) {
          if (cmd.length <4 || cmd.length >4){
                  Player.notify("cThe correct usage is '/wreplace fromid toid radius'");
                  return true;
          } 
          Integer.parseInt(cmd[1]); Integer.parseInt(cmd[2]);Integer.parseInt(cmd[3]);
      int fromid = 0;
      try{fromid = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is '/wreplace fromid toid radius");return true;}
      int toid = 0;
      try{fromid = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is '/wreplace fromid toid radius");return true;}
      int radius = 0;
      try{fromid = Integer.parseInt(cmd[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is '/wreplace fromid toid radius");return true;}
          WorldToolsVoids.replace(Player,fromid,toid,radius);
          
          Player.sendMessage("aBlocks Replaced.");
          return true;
  }
      
      if ((cmd[0].equalsIgnoreCase("/worldtools")) && (Player.canUseCommand("/worldtools"))) {
            Player.sendMessage("6 WorldTools 2.0 by Glacksy");
       
            return true;
          }
      
      if ((cmd[0].equalsIgnoreCase("/suicide")) && (Player.canUseCommand("/suicide") && (Player.canUseCommand("/worldtool")))) 
      {
            Player.setHealth(0);
            Player.sendMessage("cYou committed suicide");
            return true;
      }
      
      /**
       * kill code
       * @author spenk
       */
            if ((cmd[0].equalsIgnoreCase("/kill")) && (Player.canUseCommand("/worldtools")))           
            {    
         	   if (Player.canUseCommand("/kill")){
         		   if (cmd.length <2 || cmd.length >2){
         		   Player.notify("The correct usage is /kill <player>");
         		   return true;
         	   }else{
         		   Player player = etc.getServer().matchPlayer(cmd[1]);
         		   if (player == null){
         			   Player.notify("cThis Player Doesnt Exist or is currently not online!");
         			   return true;
         		   }
         		   player.sendMessage("4You got killed by 2"+Player.getName());
         		   player.dropInventory(player.getLocation());
         		   player.setHealth(0);
         		   Player.sendMessage("2Player sucsessfully killed!");
         		   return true;
         	   }
         	   }else{
         		   Player.sendMessage("cYou cant use this command");
         		   return true;
         	   }
            }
            
            if ((cmd[0].equalsIgnoreCase("/heal")) && (Player.canUseCommand("/worldtools")))           
            {            
     		   if (Player.canUseCommand("/heal")){
     			   if (cmd.length <2 || cmd.length >2){
             		   Player.sendMessage("cThe correct usage is /heal <player>");
             		   return true; 
     			   }else{
         		   Player player = etc.getServer().matchPlayer(cmd[1]);
         		   if (player == null){
         			   Player.sendMessage("cThis Player Doesnt Exist or is currently not online!");
         			   return true;
         		   }
         		   player.sendMessage("4You were healed by 2"+Player.getName());
         		   player.setHealth(20);
    				   player.setFoodLevel(20);
         		   Player.sendMessage("2Player sucsessfully healed!");
         		   return true;
     			   }
         	   }else{
         		   Player.sendMessage("cYou cant use this command");
         		   return true;
            }
            }
            
            if (cmd[0].equalsIgnoreCase("/save-inv") && Player.canUseCommand("/worldtools")){
         	   etc.getServer().saveInventories();
         	   Player.sendMessage("aInventories saved");
                return true;
            }
            
            if ((cmd[0].equalsIgnoreCase("/godmode")) && (Player.canUseCommand("/godmode"))) {
         	      if (!god.contains(Player.getName())) {
         	        god.add(Player.getName());
        	        Player.sendMessage("eGodmode have been disabled");
         	        return true;
         	      }else{
         	      god.remove(Player.getName());
        	      Player.sendMessage("eGodmode have been enabled");
         	      return true;
         	      }
         	    }
            try { if ((cmd[0].equalsIgnoreCase("/freeze")) && (Player.canUseCommand("/freeze")) && (Player.canUseCommand("/worldtools"))) {
                Player offender = etc.getServer().matchPlayer(cmd[1]);
                if ((!frozen.contains(offender.getName())) && (cmd[1] != null)) {
                  offender.sendMessage("2You have been frozen by 3" + Player.getName() + "e!");
                  Player.sendMessage("2You froze 3" + offender.getName() + "2!");
                  frozen.add(offender.getName());
                  return true;
                }
                offender.sendMessage("2You have been thawed!");
                Player.sendMessage("2You unfroze 3" + Player.getName() + "2!");
                frozen.remove(offender.getName());
                return true;
              }
            } catch (ArrayIndexOutOfBoundsException e) {
              Player.sendMessage("cWrong syntax! Usage: /freeze <Player>");
              return true;
            } catch (NullPointerException e) {
              Player.sendMessage("cPlayer not found!");
              return true;
            }
            
      return false;
    }
    
    /**
     * For the Freeze player part
     */
    public void onPlayerMove(Player player, Location from, Location to) {
        if (frozen.contains(player.getName()))
          player.teleportTo(from);
      }
    
    /**
     * Godmode code
     * @author Glacksy
     */
    public void onLogin(Player player) {
 	    if (god.contains(player.getName()))
	        god.remove(player.getName());
 	  }
    
    /**
     * List of damage types which can be disabled
     */
    public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount)
    {
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
      
      if ((defender.isPlayer()) && 
     	      (!god.contains(defender.getPlayer().getName()))) {
     	      Player localplayer = defender.getPlayer();
 
     	      if ((localplayer.canUseCommand("/godmode")) && 
     	        (type.equals(PluginLoader.DamageType.ENTITY)) && 
     	        (type.equals(PluginLoader.DamageType.CACTUS)) && 
     	        (type.equals(PluginLoader.DamageType.FALL)) && 
     	        (type.equals(PluginLoader.DamageType.FIRE)) && 
     	        (type.equals(PluginLoader.DamageType.LAVA)) && 
     	        (type.equals(PluginLoader.DamageType.SUFFOCATION)) && 
     	        (type.equals(PluginLoader.DamageType.FIRE_TICK)) &&
     	        (type.equals(PluginLoader.DamageType.CREEPER_EXPLOSION)) &&
     	        (type.equals(PluginLoader.DamageType.EXPLOSION)) &&
     	        (type.equals(PluginLoader.DamageType.POTION)) &&
     	        (type.equals(PluginLoader.DamageType.STARVATION)) &&
     	        (type.equals(PluginLoader.DamageType.WATER)) &&
     	        (type.equals(PluginLoader.DamageType.LIGHTNING)))
     	      {
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
 		   if (damagess.contains(block.getData())){//TODO: Test this
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
 	   if (BlockEating) {
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
    * @Deprecated. Use #onEntityRightClick(Player, Entity, Item)  instead.
     */
    public boolean onCowMilk(Player player, Mob cow)
    {
 	   if (BlockCowMilking) {
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
 	  return false; 
    }
    
    /**
     * Disable Inventories
     * TODO: add a inventory list you want to disable (you do it lol)
     */
    public boolean onOpenInventory(Player player,Inventory inventory){
 	   if (DisableInventories) { 
 		   if ((!player.canUseCommand("/ignoreinv") && player.canUseCommand("/worldtools"))){
 			   if (inventory.getContentsSize() == 27){
 				   player.notify("You cant open this inventory!");
 				   return true;
 			   }
 			   if (inventory.getContentsSize() == 1){
 				   player.notify("You cant open this inventory!");
 				   return true;
 			   }
 			   if (inventory.getContentsSize() == 3){
 				   player.notify("You cant open this inventory!");
 				   return true;
 			   }
 			   if (inventory.getContentsSize() == 54){
 				   player.notify("You cant open this inventory!");
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
 	   if (DisableItemPickup) {
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
 	   if (DisableItemDropping) {
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
 		   if (etc.getServer().getDefaultWorld().isRaining()){   //have to change this later, as it will only rain forever after the first natural rain
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
     * Prevent Changing of time, not yet done.
     */
    public boolean onTimeChange(World world, long newValue)
    {
 	   if (DisableNightTime){
 		   etc.getServer().getDefaultWorld().setTime(1);
 		   return true;
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
 }
 
 //TODO: ignore command for every feature, should also contain "/worldtools" as a second ignore command
 //TODO: add multiworld support for all features which needs it <--- cant be done yet with the current methods =/
    
