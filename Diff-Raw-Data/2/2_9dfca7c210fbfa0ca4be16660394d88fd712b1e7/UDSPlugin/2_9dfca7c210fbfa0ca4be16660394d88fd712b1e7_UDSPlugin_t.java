 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.commands.*;
 import com.undeadscythes.udsplugin.eventhandlers.*;
 import java.io.*;
 import java.util.*;
 import java.util.logging.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.material.*;
 import org.bukkit.plugin.java.*;
 import org.bukkit.util.Vector;
 
 /**
  * The main plugin. The heart of UDSPlugin.
  * @author UndeadScythe
  */
 public class UDSPlugin extends JavaPlugin {
     public static final int BUILD_LIMIT = 255;
     public static final String INT_REGEX = "[0-9][0-9]*";
     private static final Vector HALF_BLOCK = new Vector(.5, .5, .5);
     private static final File BLOCKS_PATH = new File("plugins/UDSPlugin/blocks");
     private static final File DATA_PATH = new File("plugins/UDSPlugin/data");
     private static final HashSet<Byte> TRANSPARENT_BLOCKS = new HashSet<Byte>();
     private static final List<Material> WATER = new ArrayList<Material>(Arrays.asList(Material.WATER, Material.STATIONARY_WATER));
     private static final List<Material> RAILS = new ArrayList<Material>(Arrays.asList(Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL));
     private static final List<Material> VIP_WHITELIST = new ArrayList<Material>();
     private static final List<EntityType> HOSTILE_MOBS = new ArrayList<EntityType>(Arrays.asList(EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.ENDER_DRAGON, EntityType.GHAST, EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.WITCH, EntityType.WITHER, EntityType.ZOMBIE));
     private static final List<EntityType> PASSIVE_MOBS = new ArrayList<EntityType>(Arrays.asList(EntityType.BAT, EntityType.CHICKEN, EntityType.COW, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PIG, EntityType.SHEEP, EntityType.SQUID, EntityType.VILLAGER));
     private static final List<EntityType> NEUTRAL_MOBS = new ArrayList<EntityType>(Arrays.asList(EntityType.IRON_GOLEM, EntityType.PIG_ZOMBIE, EntityType.SNOWMAN, EntityType.WOLF));
     private static final List<Kit> KITS = new ArrayList<Kit>();
     private static final Map<EntityType, Integer> MOB_REWARDS = new HashMap<EntityType, Integer>();
     private static final Map<RegionFlag, Boolean> GLOBAL_FLAGS = new HashMap<RegionFlag, Boolean>();
     private static final SaveableHashMap CLANS = new SaveableHashMap();
     private static final SaveableHashMap PLAYERS = new SaveableHashMap();
     private static final SaveableHashMap REGIONS = new SaveableHashMap();
     private static final SaveableHashMap WARPS = new SaveableHashMap();
     private static final MatchableHashMap<ChatRoom> CHAT_ROOMS = new MatchableHashMap<ChatRoom>();
     private static final MatchableHashMap<Request> REQUESTS = new MatchableHashMap<Request>();
     private static final MatchableHashMap<Session> SESSIONS = new MatchableHashMap<Session>();
     private static final MatchableHashMap<Region> ARENAS = new MatchableHashMap<Region>();
     private static final MatchableHashMap<Region> BASES = new MatchableHashMap<Region>();
     private static final MatchableHashMap<Region> CITIES = new MatchableHashMap<Region>();
     private static final MatchableHashMap<Region> HOMES = new MatchableHashMap<Region>();
     private static final MatchableHashMap<Region> QUARRIES = new MatchableHashMap<Region>();
     private static final MatchableHashMap<Region> SHOPS = new MatchableHashMap<Region>();
     private static final MatchableHashMap<SaveablePlayer> ONLINE_PLAYERS = new MatchableHashMap<SaveablePlayer>();
     private static final MatchableHashMap<SaveablePlayer> VIPS = new MatchableHashMap<SaveablePlayer>();
 
     private static UDSPlugin plugin;
     private static Timer timer;
     private static Data data;
     private static boolean serverLockedDown = false;
 
     /**
      * Used for testing in NetBeans. Woo! NetBeans!
      * @param args Blah.
      */
     public static void main(final String[] args) {}
 
     @Override
     public final void onEnable() {
         UDSPlugin.plugin = this;
         if(DATA_PATH.mkdirs()) {
             getLogger().info("Created data directory tree.");
         }
         if(BLOCKS_PATH.mkdirs()) {
             getLogger().info("Created blocks directory tree.");
         }
         saveDefaultConfig();
         loadConfig();
         UDSPlugin.TRANSPARENT_BLOCKS.clear();
         for(Material material : Material.values()) {
             if(material.isBlock() && !material.isSolid()) {
                 TRANSPARENT_BLOCKS.add((byte)material.getId());
             }
         }
         getLogger().info("Config loaded.");
         data = new Data(this);
         data.reloadData();
         data.saveDefaultData();
         data.saveData();
         getLogger().info("Data loaded.");
         try {
             loadFiles();
         } catch (IOException ex) {
             Logger.getLogger(UDSPlugin.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             UDSPlugin.timer = new Timer();
         } catch (IOException ex) {
             Logger.getLogger(UDSPlugin.class.getName()).log(Level.SEVERE, null, ex);
         }
         Bukkit.getScheduler().scheduleSyncRepeatingTask(this, UDSPlugin.timer, 100, 100);
         getLogger().info("Timer started.");
         setCommandExecutors();
         getLogger().info("Commands registered.");
         registerEvents();
         getLogger().info("Events registered.");
         addRecipes();
         getLogger().info("Recipes added.");
         Censor.initCensor();
         getLogger().info("Censor online.");
         EntityTracker.findMinecarts();
         getLogger().info("Tracking minecarts.");
         final String message = getName() + " version " + this.getDescription().getVersion() + " enabled.";
         getLogger().info(message); // Looks a bit like the Sims loading screen right?
     }
 
     @Override
     public final void onDisable() {
         try {
             saveFiles();
             final String message = (CLANS.size() + REGIONS.size() + WARPS.size() + PLAYERS.size()) + " clans, regions, warps and players saved.";
             getLogger().info(message);
         } catch (IOException ex) {
             Logger.getLogger(UDSPlugin.class.getName()).log(Level.SEVERE, null, ex);
         }
         final String message = getName() + " disabled.";
         getLogger().info(message);
     }
 
     public static void reloadConf() {
         plugin.reloadConfig();
         plugin.loadConfig();
     }
 
     public final void loadConfig() {
         final FileConfiguration config = getConfig();
         UDSPlugin.VIP_WHITELIST.clear();
         for(int typeId : config.getIntegerList(ConfigRef.VIP_WHITELIST.getReference())) {
             UDSPlugin.VIP_WHITELIST.add(Material.getMaterial(typeId));
         }
         UDSPlugin.KITS.clear();
         for(String kit : config.getStringList(ConfigRef.KITS.getReference())) {
             final String[] kitSplit = kit.split(",");
             final List<ItemStack> items = new ArrayList<ItemStack>();
             for(Object item : ArrayUtils.subarray(kitSplit, 3, kitSplit.length -1)) {
                 items.add(new ItemStack(Material.getMaterial(Integer.parseInt((String)item))));
             }
             KITS.add(new Kit(kitSplit[0], Integer.parseInt(kitSplit[1]), items, PlayerRank.getByName(kitSplit[2])));
         }
         UDSPlugin.MOB_REWARDS.clear();
         for(EntityType entityType : EntityType.values()) {
             String entityName = ConfigRef.MOB_REWARDS.getReference() + "." + entityType.getName();
             if(entityName != null) {
                 entityName = entityName.toLowerCase();
                 MOB_REWARDS.put(entityType, config.getInt(entityName));
             }
         }
         UDSPlugin.GLOBAL_FLAGS.clear();
         for(RegionFlag flag : RegionFlag.values()) {
             final String flagname = ConfigRef.GLOBAL_FLAGS.getReference() + "." + flag.toString().toLowerCase();
             GLOBAL_FLAGS.put(flag, config.getBoolean(flagname));
         }
     }
 
     /**
      * Saves all the listed objects to file.
      * @throws IOException When a file can't be opened.
      */
     public static void saveFiles() throws IOException {
         data.saveData();
         CLANS.save(DATA_PATH + File.separator + Clan.PATH);
         REGIONS.save(DATA_PATH + File.separator + Region.PATH);
         WARPS.save(DATA_PATH + File.separator + Warp.PATH);
         PLAYERS.save(DATA_PATH + File.separator + SaveablePlayer.PATH);
     }
 
     /**
      * Loads the listed files from file.
      * @throws FileNotFoundException When a file can't be accessed.
      * @throws IOException When a file can't be read from.
      */
     private void loadFiles() throws IOException {
         BufferedReader file;
         String nextLine;
         String message;
         try {
             file = new BufferedReader(new FileReader(DATA_PATH + File.separator + SaveablePlayer.PATH));
             while((nextLine = file.readLine()) != null) {
                 final SaveablePlayer player = new SaveablePlayer(nextLine);
                 PLAYERS.put(nextLine.split("\t")[0], player);
                 if(player.getVIPTime() > 0) {
                     VIPS.put(player.getName(), player);
                 }
             }
             file.close();
             message = PLAYERS.size() + " players loaded.";
             getLogger().info(message);
         } catch (FileNotFoundException ex) {
             getLogger().info("No player file exists yet.");
         }
         try {
             file = new BufferedReader(new FileReader(DATA_PATH + File.separator + Region.PATH));
             while((nextLine = file.readLine()) != null) {
                 final Region region = new Region(nextLine);
                 REGIONS.put(region.getName(), region);
                 if(region.getType().equals(RegionType.BASE)) {
                     BASES.put(region.getName(), region);
                 } else if(region.getType().equals(RegionType.HOME)) {
                     HOMES.put(region.getName(), region);
                 } else if(region.getType().equals(RegionType.QUARRY)) {
                     QUARRIES.put(region.getName(), region);
                 } else if(region.getType().equals(RegionType.SHOP)) {
                     SHOPS.put(region.getName(), region);
                 } else if(region.getType().equals(RegionType.CITY)) {
                     CITIES.put(region.getName(), region);
                 } else if(region.getType().equals(RegionType.ARENA)) {
                     ARENAS.put(region.getName(), region);
                 }
             }
             file.close();
             message = REGIONS.size() + " regions loaded.";
             getLogger().info(message);
         } catch (FileNotFoundException ex) {
             getLogger().info("No region file exists yet.");
         }
         try {
             file = new BufferedReader(new FileReader(DATA_PATH + File.separator + Warp.PATH));
             while((nextLine = file.readLine()) != null) {
                 WARPS.put(nextLine.split("\t", 1)[0], new Warp(nextLine));
             }
             file.close();
             message = WARPS.size() + " warps loaded.";
             getLogger().info(message);
         } catch (FileNotFoundException ex) {
             getLogger().info("No warp file exists yet.");
         }
         try {
             file = new BufferedReader(new FileReader(DATA_PATH + File.separator + Clan.PATH));
             while((nextLine = file.readLine()) != null) {
                 final Clan clan = new Clan(nextLine);
                 CLANS.put(nextLine.split("\t")[0], clan);
                 clan.linkMembers();
             }
             file.close();
             message = CLANS.size() + " clans loaded.";
             getLogger().info(message);
         } catch (FileNotFoundException ex) {
             getLogger().info("No clan file exists yet.");
         }
     }
 
     /**
      * Connects the commands with their executors.
      */
     private void setCommandExecutors() {
         getCommand("a").setExecutor(new ACmd());
         getCommand("acceptrules").setExecutor(new AcceptRulesCmd());
         getCommand("admin").setExecutor(new AdminCmd());
         getCommand("afk").setExecutor(new AfkCmd());
         getCommand("back").setExecutor(new BackCmd());
         getCommand("ban").setExecutor(new BanCmd());
         getCommand("bounty").setExecutor(new BountyCmd());
         getCommand("broadcast").setExecutor(new BroadcastCmd());
         getCommand("butcher").setExecutor(new ButcherCmd());
         getCommand("c").setExecutor(new CCmd());
         getCommand("call").setExecutor(new CallCmd());
         getCommand("challenge").setExecutor(new ChallengeCmd());
         getCommand("check").setExecutor(new CheckCmd());
         getCommand("ci").setExecutor(new CiCmd());
         getCommand("city").setExecutor(new CityCmd());
         getCommand("clan").setExecutor(new ClanCmd());
         getCommand("day").setExecutor(new DayCmd());
         getCommand("debug").setExecutor(new DebugCmd());
         getCommand("delwarp").setExecutor(new DelWarpCmd());
         getCommand("demote").setExecutor(new DemoteCmd());
         getCommand("enchant").setExecutor(new EnchantCmd());
         getCommand("face").setExecutor(new FaceCmd());
         getCommand("gift").setExecutor(new GiftCmd());
         getCommand("god").setExecutor(new GodCmd());
         getCommand("heal").setExecutor(new HealCmd());
         getCommand("help").setExecutor(new HelpCmd());
         getCommand("home").setExecutor(new HomeCmd());
         getCommand("i").setExecutor(new ICmd());
         getCommand("ignore").setExecutor(new IgnoreCmd());
         getCommand("invsee").setExecutor(new InvSeeCmd());
         getCommand("jail").setExecutor(new JailCmd());
         getCommand("kick").setExecutor(new KickCmd());
         getCommand("kit").setExecutor(new KitCmd());
         getCommand("lockdown").setExecutor(new LockdownCmd());
         getCommand("map").setExecutor(new MapCmd());
         getCommand("me").setExecutor(new MeCmd());
         getCommand("mod").setExecutor(new ModCmd());
         getCommand("money").setExecutor(new MoneyCmd());
         getCommand("n").setExecutor(new NCmd());
         getCommand("nick").setExecutor(new NickCmd());
         getCommand("night").setExecutor(new NightCmd());
         getCommand("p").setExecutor(new PCmd());
         getCommand("paybail").setExecutor(new PayBailCmd());
         getCommand("pet").setExecutor(new PetCmd());
         getCommand("powertool").setExecutor(new PowertoolCmd());
         getCommand("private").setExecutor(new PrivateCmd());
         getCommand("promote").setExecutor(new PromoteCmd());
         getCommand("r").setExecutor(new RCmd());
         getCommand("rain").setExecutor(new RainCmd());
         getCommand("region").setExecutor(new RegionCmd());
         getCommand("rules").setExecutor(new RulesCmd());
         getCommand("scuba").setExecutor(new ScubaCmd());
         getCommand("server").setExecutor(new ServerCmd());
         getCommand("setspawn").setExecutor(new SetSpawnCmd());
         getCommand("setwarp").setExecutor(new SetWarpCmd());
         getCommand("shop").setExecutor(new ShopCmd());
         getCommand("signs").setExecutor(new SignsCmd());
         getCommand("sit").setExecutor(new SitCmd());
         getCommand("spawn").setExecutor(new SpawnCmd());
         getCommand("spawner").setExecutor(new SpawnerCmd());
         getCommand("stack").setExecutor(new StackCmd());
         getCommand("stats").setExecutor(new StatsCmd());
         getCommand("storm").setExecutor(new StormCmd());
         getCommand("sun").setExecutor(new SunCmd());
         getCommand("tp").setExecutor(new TPCmd());
         getCommand("tell").setExecutor(new TellCmd());
         getCommand("ticket").setExecutor(new TicketCmd());
         getCommand("tgm").setExecutor(new TGMCmd());
         getCommand("unban").setExecutor(new UnBanCmd());
         getCommand("unjail").setExecutor(new UnJailCmd());
         getCommand("vip").setExecutor(new VIPCmd());
         getCommand("we").setExecutor(new WECmd());
         getCommand("warden").setExecutor(new WardenCmd());
         getCommand("warp").setExecutor(new WarpCmd());
         getCommand("where").setExecutor(new WhereCmd());
         getCommand("who").setExecutor(new WhoCmd());
         getCommand("whois").setExecutor(new WhoIsCmd());
         getCommand("xp").setExecutor(new XPCmd());
         getCommand("y").setExecutor(new YCmd());
     }
 
     private void registerEvents() {
         getServer().getPluginManager().registerEvents(new AsyncPlayerChat(), this);
         getServer().getPluginManager().registerEvents(new BlockBreak(), this);
         getServer().getPluginManager().registerEvents(new BlockBurn(), this);
         getServer().getPluginManager().registerEvents(new BlockDispense(), this);
         getServer().getPluginManager().registerEvents(new BlockFade(), this);
         getServer().getPluginManager().registerEvents(new BlockForm(), this);
         getServer().getPluginManager().registerEvents(new BlockFromTo(), this);
         getServer().getPluginManager().registerEvents(new BlockGrow(), this);
         getServer().getPluginManager().registerEvents(new BlockIgnite(), this);
         getServer().getPluginManager().registerEvents(new BlockPhysics(), this);
         getServer().getPluginManager().registerEvents(new BlockPistonExtend(), this);
         getServer().getPluginManager().registerEvents(new BlockPistonRetract(), this);
         getServer().getPluginManager().registerEvents(new BlockPlace(), this);
         getServer().getPluginManager().registerEvents(new BlockRedstone(), this);
         getServer().getPluginManager().registerEvents(new BlockSpread(), this);
         getServer().getPluginManager().registerEvents(new CreatureSpawn(), this);
         getServer().getPluginManager().registerEvents(new EntityBlockForm(), this);
         getServer().getPluginManager().registerEvents(new EntityChangeBlock(), this);
         getServer().getPluginManager().registerEvents(new EntityCreatePortal(), this);
         getServer().getPluginManager().registerEvents(new EntityDamage(), this);
         getServer().getPluginManager().registerEvents(new EntityDamageByEntity(), this);
         getServer().getPluginManager().registerEvents(new EntityDeath(), this);
         getServer().getPluginManager().registerEvents(new EntityExplode(), this);
         getServer().getPluginManager().registerEvents(new EntityInteract(), this);
         getServer().getPluginManager().registerEvents(new EntityPortalEnter(), this);
         getServer().getPluginManager().registerEvents(new InventoryClick(), this);
         getServer().getPluginManager().registerEvents(new InventoryOpen(), this);
         getServer().getPluginManager().registerEvents(new ItemDespawn(), this);
         getServer().getPluginManager().registerEvents(new HangingBreak(), this);
         getServer().getPluginManager().registerEvents(new HangingPlace(), this);
         getServer().getPluginManager().registerEvents(new PlayerBucketEmpty(), this);
         getServer().getPluginManager().registerEvents(new PlayerBucketFill(), this);
         getServer().getPluginManager().registerEvents(new PlayerDeath(), this);
         getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
         getServer().getPluginManager().registerEvents(new PlayerInteractEntity(), this);
         getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
         getServer().getPluginManager().registerEvents(new PlayerMove(), this);
         getServer().getPluginManager().registerEvents(new PlayerPortal(), this);
         getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
         getServer().getPluginManager().registerEvents(new PlayerRespawn(), this);
         getServer().getPluginManager().registerEvents(new PlayerShearEntity(), this);
         getServer().getPluginManager().registerEvents(new SignChange(), this);
         getServer().getPluginManager().registerEvents(new VehicleEntityCollision(), this);
         getServer().getPluginManager().registerEvents(new VehicleCreate(), this);
         getServer().getPluginManager().registerEvents(new VehicleDestroy(), this);
         getServer().getPluginManager().registerEvents(new VehicleExit(), this);
     }
 
     private final static String ROW = "AAA";
     private final static String SWJ = "ABA";
     private final static String GAP = "A A";
     private final static String DOT = " A ";
 
     @SuppressWarnings("deprecation")
     private void addRecipes() {
         final ShapedRecipe mossyStoneBrick = new ShapedRecipe(new ItemStack(98, 1, (short) 0, (byte) 1)).shape(ROW, SWJ, ROW).setIngredient('A', Material.VINE).setIngredient('B', new MaterialData(98, (byte) 0));
         this.getServer().addRecipe(mossyStoneBrick);
         final ShapelessRecipe crackedStoneBrick = new ShapelessRecipe(new ItemStack(98, 1, (short) 0, (byte) 2)).addIngredient(Material.WOOD_PICKAXE).addIngredient(new MaterialData(98, (byte) 0));
         this.getServer().addRecipe(crackedStoneBrick);
         final ShapedRecipe circleStoneBrick = new ShapedRecipe(new ItemStack(98, 8, (short) 0, (byte) 3)).shape(ROW, GAP, ROW).setIngredient('A', new MaterialData(98, (byte) 0));
         this.getServer().addRecipe(circleStoneBrick);
         final ShapedRecipe mossyCobbleStone = new ShapedRecipe(new ItemStack(48, 1)).shape(ROW, SWJ, ROW).setIngredient('A', Material.VINE).setIngredient('B', Material.COBBLESTONE);
         this.getServer().addRecipe(mossyCobbleStone);
         final ShapedRecipe iceBlock = new ShapedRecipe(new ItemStack(79, 1)).shape(DOT, SWJ, DOT).setIngredient('A', Material.SNOW_BALL).setIngredient('B', Material.WATER_BUCKET);
         this.getServer().addRecipe(iceBlock);
         final ShapedRecipe chainHelmet = new ShapedRecipe(new ItemStack(302, 1)).shape(ROW, GAP).setIngredient('A', Material.FLINT);
         this.getServer().addRecipe(chainHelmet);
         final ShapedRecipe chainChest = new ShapedRecipe(new ItemStack(303, 1)).shape(GAP, ROW ,ROW).setIngredient('A', Material.FLINT);
         this.getServer().addRecipe(chainChest);
         final ShapedRecipe chainLegs = new ShapedRecipe(new ItemStack(304, 1)).shape(ROW, GAP, GAP).setIngredient('A', Material.FLINT);
         this.getServer().addRecipe(chainLegs);
         final ShapedRecipe chainBoots = new ShapedRecipe(new ItemStack(305, 1)).shape(GAP, GAP).setIngredient('A', Material.FLINT);
         this.getServer().addRecipe(chainBoots);
         final ShapedRecipe grassBlock = new ShapedRecipe(new ItemStack(Material.GRASS, 1)).shape("A", "B").setIngredient('A', new MaterialData(31, (byte) 1)).setIngredient('B', Material.DIRT);
         this.getServer().addRecipe(grassBlock);
         final ShapedRecipe snowLayer = new ShapedRecipe(new ItemStack(Material.SNOW, 1)).shape(ROW).setIngredient('A', Material.SNOW_BALL);
         this.getServer().addRecipe(snowLayer);
         final ShapedRecipe creeperEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 50)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.SULPHUR);
         this.getServer().addRecipe(creeperEgg);
         final ShapedRecipe skeletonEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 51)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.BONE);
         this.getServer().addRecipe(skeletonEgg);
         final ShapedRecipe spiderEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 52)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.SPIDER_EYE);
         this.getServer().addRecipe(spiderEgg);
         final ShapedRecipe zombieEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 54)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.ROTTEN_FLESH);
         this.getServer().addRecipe(zombieEgg);
         final ShapedRecipe slimeEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 55)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.SLIME_BALL);
         this.getServer().addRecipe(slimeEgg);
         final ShapedRecipe ghastEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 56)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.GHAST_TEAR);
         this.getServer().addRecipe(ghastEgg);
         final ShapedRecipe pigZombieEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 57)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.GOLD_NUGGET);
         this.getServer().addRecipe(pigZombieEgg);
         final ShapedRecipe endermanEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 58)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.ENDER_PEARL);
         this.getServer().addRecipe(endermanEgg);
         final ShapedRecipe caveSpiderEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 59)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.STRING);
         this.getServer().addRecipe(caveSpiderEgg);
         final ShapedRecipe silverFishEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 60)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.STONE);
         this.getServer().addRecipe(silverFishEgg);
         final ShapedRecipe blazeEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 61)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.BLAZE_ROD);
         this.getServer().addRecipe(blazeEgg);
         final ShapedRecipe magmaCubeEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 62)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.MAGMA_CREAM);
         this.getServer().addRecipe(magmaCubeEgg);
         final ShapedRecipe pigEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 90)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.PORK);
         this.getServer().addRecipe(pigEgg);
         final ShapedRecipe sheepEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 91)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.WOOL);
         this.getServer().addRecipe(sheepEgg);
         final ShapedRecipe cowEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 92)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.LEATHER);
         this.getServer().addRecipe(cowEgg);
         final ShapedRecipe chickenEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 93)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.FEATHER);
         this.getServer().addRecipe(chickenEgg);
         final ShapedRecipe squidEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 94)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.INK_SACK);
         this.getServer().addRecipe(squidEgg);
         final ShapedRecipe wolfEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 95)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.WHEAT);
         this.getServer().addRecipe(wolfEgg);
         final ShapedRecipe mooshroomEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 96)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.RED_MUSHROOM);
         this.getServer().addRecipe(mooshroomEgg);
         final ShapedRecipe villagerEgg = new ShapedRecipe(new ItemStack(383, 1, (short) 0, (byte) 120)).shape(ROW, SWJ, ROW).setIngredient('A', Material.GOLD_BLOCK).setIngredient('B', Material.RED_ROSE);
         this.getServer().addRecipe(villagerEgg);
         final ShapedRecipe webBlock = new ShapedRecipe(new ItemStack(Material.WEB)).shape(ROW, GAP, ROW).setIngredient('A', Material.STRING);
         this.getServer().addRecipe(webBlock);
     }
 
     /**
      * Grab the chat rooms map.
      * @return Chat rooms map.
      */
     public static MatchableHashMap<ChatRoom> getChatRooms() {
         return CHAT_ROOMS;
     }
 
     /**
      * Grab and cast the clans map.
      * @return Clans map.
      */
     public static MatchableHashMap<Clan> getClans() {
         return CLANS.toMatchableHashMap(Clan.class);
     }
 
     /**
      * Grab and cast the players map.
      * @return Players map.
      */
     public static MatchableHashMap<SaveablePlayer> getPlayers() {
         return PLAYERS.toMatchableHashMap(SaveablePlayer.class);
     }
 
     /**
      * Grab and cast the regions map.
      * @return Regions map.
      */
     public static MatchableHashMap<Region> getRegions() {
         return REGIONS.toMatchableHashMap(Region.class);
     }
 
     /**
      * Grab the requests map.
      * @return Requests map.
      */
     public static MatchableHashMap<Request> getRequests() {
         return REQUESTS;
     }
 
     /**
      * Grab and cast the sessions map.
      * @return Sessions map.
      */
     public static MatchableHashMap<Session> getSessions() {
         return SESSIONS;
     }
 
     /**
      * Grab and cast the warps map.
      * @return Warps map.
      */
     public static MatchableHashMap<Warp> getWarps() {
         return WARPS.toMatchableHashMap(Warp.class);
     }
 
     /**
      * Grab the quarries map.
      * @return Quarries map.
      */
     public static MatchableHashMap<Region> getQuarries() {
         return QUARRIES;
     }
 
     /**
      * Grab the homes map.
      * @return Homes map.
      */
     public static MatchableHashMap<Region> getHomes() {
         return HOMES;
     }
 
     /**
      * Grab the shops map.
      * @return Shops map.
      */
     public static MatchableHashMap<Region> getShops() {
         return SHOPS;
     }
 
     /**
      * Grab the bases map.
      * @return Bases map.
      */
     public static MatchableHashMap<Region> getBases() {
         return BASES;
     }
 
     /**
      * Grab the cities map.
      * @return Cities map.
      */
     public static MatchableHashMap<Region> getCities() {
         return CITIES;
     }
 
     /**
      * Grab the cities map.
      * @return Cities map.
      */
     public static MatchableHashMap<Region> getArenas() {
         return ARENAS;
     }
 
     /**
      * Grab the VIPs map.
      * @return VIPs map.
      */
     public static MatchableHashMap<SaveablePlayer> getVIPS() {
         return VIPS;
     }
 
     /**
      * Grab the online players map.
      * @return Online players map.
      */
     public static MatchableHashMap<SaveablePlayer> getOnlinePlayers() {
         return ONLINE_PLAYERS;
     }
 
     /**
      *
      * @return
      */
     public static Data getData() {
         return data;
     }
 
     public static void toggleLockdown() {
         serverLockedDown ^= true;
     }
 
     public static boolean isLockedDown() {
         return serverLockedDown;
     }
 
     public static boolean getConfigBool(final ConfigRef ref) {
         return plugin.getConfig().getBoolean(ref.getReference());
     }
 
     public static byte getConfigByte(final ConfigRef ref) {
         return (byte)plugin.getConfig().getInt(ref.getReference());
     }
 
     public static int getConfigInt(final ConfigRef ref) {
         return plugin.getConfig().getInt(ref.getReference()) * (int)ref.getMultiplier();
     }
 
     public static int getConfigIntSq(final ConfigRef ref) {
         return (int)Math.pow(plugin.getConfig().getInt(ref.getReference()) * (int)ref.getMultiplier(), 2);
     }
 
     public static long getConfigLong(final ConfigRef ref) {
         return plugin.getConfig().getLong(ref.getReference()) * ref.getMultiplier();
     }
 
     public static String getConfigString(final ConfigRef ref) {
         return plugin.getConfig().getString(ref.getReference());
     }
 
     public static List<String> getConfigStringList(final ConfigRef ref) {
         return plugin.getConfig().getStringList(ref.getReference());
     }
 
     public static Material getConfigMaterial(final ConfigRef ref) {
        return Material.getMaterial(plugin.getConfig().getString(ref.getReference()).toUpperCase());
     }
 
     public static File getBlocksPath() {
         return BLOCKS_PATH;
     }
 
     public static Vector getHalfBlock() {
         return HALF_BLOCK;
     }
 
     public static Map<RegionFlag, Boolean> getGlobalFlags() {
         return GLOBAL_FLAGS;
     }
 
     public static List<Material> getVipWhitelist() {
         return VIP_WHITELIST;
     }
 
     public static Map<EntityType, Integer> getMobRewards() {
         return MOB_REWARDS;
     }
 
     public static List<Kit> getKits() {
         return KITS;
     }
 
     public static List<Material> getRails() {
         return RAILS;
     }
 
     public static List<Material> getWater() {
         return WATER;
     }
 
     public static List<EntityType> getHostileMobs() {
         return HOSTILE_MOBS;
     }
 
     public static List<EntityType> getPassiveMobs() {
         return PASSIVE_MOBS;
     }
 
     public static HashSet<Byte> getTransparentBlocks() {
         return TRANSPARENT_BLOCKS;
     }
 }
