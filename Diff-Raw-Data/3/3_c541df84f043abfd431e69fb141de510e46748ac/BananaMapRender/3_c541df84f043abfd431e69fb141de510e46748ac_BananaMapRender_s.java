 /*
  * BananaMapRender.java
  * 
  * Version 0.1
  * 
  * Last Edited
  * 18/07/2011
  * 
  * written by codename_B
  * forked by K900
  * forked by Nightgunner5
  */
 
 package com.ubempire.render;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.bukkit.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BananaMapRender extends JavaPlugin {
     protected final static Logger logger = Logger.getLogger("Minecraft");
 
     Timer renderStarter;
     PlayerScript markups;
 
     int renderThreads;
     List<GeneratorThread> threadQueue;
 
     Map<Integer, Color> colors;
     Map<Integer, List<Color>> multiColors;
     boolean showDepth, showWaterDepth, showLavaDepth;
 
     @Override
     public void onDisable() {
         final PluginDescriptionFile pdfFile = getDescription();
         System.out.println("[" + (pdfFile.getName()) + "]" + " version " + pdfFile.getVersion() + " is disabled!");
         getServer().getScheduler().cancelTasks(this);
         renderStarter.cancel();
     }
 
     @Override
     public void onEnable() {
         IdToColor.plugin = this;
         colors = new HashMap<Integer, Color>();
         multiColors = new HashMap<Integer, List<Color>>();
         setDefaultColors();
         loadVars();
         displayWorldName();
 
         renderThreads = 0;
         threadQueue = new LinkedList<GeneratorThread>();
 
         markups = new PlayerScript(this);
         renderStarter = new Timer();
         renderStarter.schedule(new RenderStarterTask(this), 1000, 1000);
 
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 for (World world : getServer().getWorlds()) {
                     markups.updateMapMarkers(world);
                 }
             }
         }, 0, varMarkerUpdatesFrequency() * 60 * 20);
 
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 chunkToRender();
             }
         }, 0, varTileCheckerFrequency() * 60 * 20);
 
         final PluginDescriptionFile pdfFile = getDescription();
         System.out.println("[" + (pdfFile.getName()) + "]" + " version " + pdfFile.getVersion() + " is enabled!");
     }
 
     public void displayWorldName() {
         for (World world : getServer().getWorlds()) {
             String directory = getDir(world.getName());
             String wfile = directory + "/world.js";
             try {
                 new File(wfile).createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             PrintWriter out = null;
             try {
                 out = new PrintWriter(new FileWriter(wfile));
                 out.print("document.getElementById('worldname').innerHTML = '" + getAlias(world) + "';");
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 if (out != null) out.close();
             }
         }
     }
 
     private String getAlias(World world) {
         return getConfiguration().getString("alias." + world.getName(), world.getName()); // Another little goodie :P
     }
 
     public void chunkToRender() {
         for (World world : getServer().getWorlds()) {
             markups.updateMapMarkers(world);
             List<Player> players = world.getPlayers();
             for (Player player : players) {
                 double playerX = player.getLocation().getX();
                 double playerZ = player.getLocation().getZ();
                 int chunkx = (int) (Math.round(playerX / 512));
                 int chunkz = (int) (Math.round(playerZ / 512));
                 threadQueue.add(new GeneratorThread(this, chunkx, chunkz, world));
             }
         }
     }
 
     static Set<Chunk> getChunksInUse(World world) {
         HashSet<Chunk> chunks = new HashSet<Chunk>();
 
         for (Player player : world.getPlayers()) {
             Chunk center = player.getLocation().getBlock().getChunk();
             // No way in the Bukkit spec to get the view radius, so let's settle for the maximum.
             for (int x = -15; x <= 15; x++) {
                 for (int z = -15; z <= 15; z++) {
                     chunks.add(world.getChunkAt(center.getX() + x, center.getZ() + z));
                 }
             }
         }
 
         return chunks;
     }
 
     static ChunkSnapshot[][] prepareRegion(World world, int x, int z) {
         Set<Chunk> chunksInUse = getChunksInUse(world);
 
         ChunkSnapshot[][] region = new ChunkSnapshot[32][32];
         for (int i = 0; i < 32; i++) {
             for (int j = 0; j < 32; j++) {
                 int cx = x * 32 + i, cz = z * 32 + j;
                 Chunk chunk = world.getChunkAt(cx, cz);
                 region[i][j] = chunk.getChunkSnapshot();
                 if (!chunksInUse.contains(chunk)) {
                     world.unloadChunkRequest(cx, cz);
                 }
             }
         }
         return region;
     }
 
     @Override
     public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
         if (!sender.hasPermission("bmr.render")) {
             sender.sendMessage("You don't have permission to do it.");
             return true;
         }
         try {
             if (commandLabel.equalsIgnoreCase("bmr")) {
 
                 // Not calling displayWorldName here is a generally bad idea, because sometimes people remove old maps altogether.
                 displayWorldName();
 
                 if (args.length > 0 && args[0].equalsIgnoreCase("length")) {
                    sender.sendMessage("The render queue has " + renderThreads
                            + " regions.");
                     return true;
                 }
 
                 Location loc = null;
                 World world;
                 String worldName;
                 worldName = getServer().getWorlds().get(0).getName();
                 int range = 0;
 
                 if (args.length > 0) range = Integer.parseInt(args[0]);
                 if (args.length > 1) worldName = args[1];
 
                 //Prevent user from shooting himself in the foot
                 if (range > varMaxRendersize()) {
                     sender.sendMessage(ChatColor.RED + "I'm sorry, Dave. I'm afraid I can't do that.");
                     return true;
                 }
 
                 //Location to render
                 if (sender instanceof Player) {
                     final Player player = (Player) sender;
                     worldName = (worldName == null) ? player.getWorld().getName() : worldName;
                     loc = player.getLocation();
                 }
                 world = getServer().getWorld(worldName);
                 if (world != null) {
                     loc = (loc == null) ? world.getSpawnLocation() : loc;
                     //Create directory
                     String worldDir = getDir(world.getName());
                     markups.updateMapMarkers(world);
 
                     sender.sendMessage(ChatColor.GREEN + "Starting map render");
                     System.out.println("Start copying template files...");
                     new CopierThread(new File(getDataFolder(), "template"), new File(worldDir)).start();
 
                     final int playerX = (int) (loc.getX() / 512);
                     final int playerZ = (int) (loc.getZ() / 512);
 
                     for (int i = 0; i <= range; i++) {
                         for (int x = -i + playerX; x <= i + playerX; x++) {
                             for (int z = -i + playerZ; z <= i + playerZ; z++) {
                                 threadQueue.add(new GeneratorThread(this, x, z, world));
                             }
                         }
                     }
                     return true;
                 }
                 sender.sendMessage(ChatColor.RED + "World " + worldName + " doesn't exist.");
             }
         } catch (Exception e) {
             System.out.println("You're doing it wrong");
             e.printStackTrace();
         }
         return false;
     }
 
     // Config variables are fetched here (option names and default values)
 
     public String getDir() {
         String dir = getConfiguration().getString("directory");
         return (dir.charAt(dir.length() - 1) == '/') ? dir : dir + "/";
     }
 
     public String getDir(String worldName) {
         String directory = getDir() + worldName + "/";
         File dir = new File((directory.substring(0, directory.length() - (directory.endsWith("/") ? 1 : 0))));
         if (!dir.exists()) dir.mkdirs(); // Make full path, not just one folder. Because shit generally happens.
         return directory;
     }
 
     protected int varMaxRendersize() {
         return getConfiguration().getInt("max-render", 4);
     }
 
     protected int varMaxThreads() {
         return getConfiguration().getInt("max-threads", 2);
     }
 
     protected int varExpirationHours() {
         return getConfiguration().getInt("expiration-hours", 1);
     }
 
     protected int varTileCheckerFrequency() {
         return getConfiguration().getInt("tile-checker-frequency", 5);
     }
 
     protected boolean showNightTiles() {
         return getConfiguration().getBoolean("night-tiles", true);
     }
 
     protected int varMarkerUpdatesFrequency() {
         return getConfiguration().getInt("marker-updates-frequency", 1);
     }
 
     protected boolean varWorldborderEnable() {
         return getConfiguration().getBoolean("worldborder.enable", false);
     }
 
     protected String varWorldborderColor() {
         return getConfiguration().getString("worldborder.color", "#ff0000");
     }
 
     protected double varWorldborderOpacity() {
         return getConfiguration().getDouble("worldborder.opacity", 0.5);
     }
 
     protected double varWorldborderFillOpacity() {
         return getConfiguration().getDouble("worldborder.fill-opacity", 0.0);
     }
 
     protected boolean varWorldguardRegionsEnable() {
         return getConfiguration().getBoolean("worldguard-regions.enable", true);
     }
 
     protected String varWorldguardRegionsColor() {
         return getConfiguration().getString("worldguard-regions.color", "#0033ff");
     }
 
     protected double varWorldguardRegionsOpacity() {
         return getConfiguration().getDouble("worldguard-regions.opacity", 0.5);
     }
 
     protected double varWorldguardRegionsFillOpacity() {
         return getConfiguration().getDouble("worldguard-regions.fill-opacity", 0.1);
     }
 
     protected boolean varEntitiesEnable() {
         return getConfiguration().getBoolean("entities.enable", true);
     }
 
     protected int varEntitiesMaxPerChunk() {
         return getConfiguration().getInt("entities.max-per-chunk", 3);
     }
 
     protected boolean varEntitiesPlayers() {
         return getConfiguration().getBoolean("entities.players", true);
     }
 
     protected boolean varEntitiesPlayerPopups() {
         return getConfiguration().getBoolean("entities.player-popups", true);
     }
 
     protected boolean varEntitiesMob(String name) {
         return getConfiguration().getBoolean("entities.mobs." + name, true);
     }
 
     protected boolean varEntitiesTamedWolves() {
         return getConfiguration().getBoolean("entities.tamed-wolves", true);
     }
 
     protected boolean varTileEntitiesEnable() {
         return getConfiguration().getBoolean("tile-entities.enable", true);
     }
 
     protected int varTileEntitiesMaxPerChunk() {
         return getConfiguration().getInt("tile-entities.max-per-chunk", 3);
     }
 
     protected boolean varTileEntitiesSpawners() {
         return getConfiguration().getBoolean("tile-entities.spawners", false);
     }
 
     protected double varTileEntitiesSpawnerChance() {
         return getConfiguration().getDouble("tile-entities.spawner-chance", 0.2);
     }
 
     protected boolean varTileEntitiesSigns() {
         return getConfiguration().getBoolean("tile-entities.signs.show", true);
     }
 
     protected boolean varDepthWater() {
         return showWaterDepth;
     }
 
     protected boolean varDepthLava() {
         return showLavaDepth;
     }
 
     protected boolean varDepthGround() {
         return showDepth;
     }
 
     protected Color varColor(int id) {
         if (colors.containsKey(id))
             return colors.get(id);
         return new Color(255, 255, 255); //Default color
     }
 
     protected Color varColor(int id, int damage) {
         if (multiColors.containsKey(id) && damage < multiColors.get(id).size())
             return multiColors.get(id).get(damage);
         return varColor(id);
     }
 
     private void setDefaultColors() {
         Color c;
         colors.put(0, new Color(255, 255, 255));
         colors.put(1, new Color(139, 137, 137));
         c = new Color(15, 255, 0);
         colors.put(2, c);
         colors.put(31, c);
         colors.put(37, c);
         colors.put(38, c);
         colors.put(39, c);
         colors.put(40, c);
         colors.put(59, c);
         c = new Color(139, 69, 19);
         colors.put(3, c);
         colors.put(60, c);
         colors.put(88, c);
         colors.put(4, new Color(205, 197, 191));
         c = new Color(148, 124, 80);
         colors.put(5, c);
         colors.put(32, c);
         colors.put(53, c);
         colors.put(54, c);
         colors.put(58, c);
         colors.put(85, c);
         colors.put(86, c);
         colors.put(90, c);
         colors.put(6, new Color(139, 69, 19));
         colors.put(7, new Color(52, 52, 52));
         c = new Color(20, 20, 200);
         colors.put(8, c);
         colors.put(9, c);
         c = new Color(252, 87, 0);
         colors.put(10, c);
         colors.put(11, c);
         colors.put(51, c);
         colors.put(12, new Color(134, 114, 94));
         c = new Color(144, 144, 144);
         colors.put(13, c);
         colors.put(14, c);
         colors.put(15, c);
         colors.put(16, c);
         colors.put(21, c);
         colors.put(56, c);
         colors.put(61, c);
         colors.put(62, c);
         colors.put(67, c);
         colors.put(73, c);
         colors.put(74, c);
         c = new Color(160, 82, 45);
         colors.put(17, c);
         colors.put(81, c);
         colors.put(83, c);
         colors.put(18, new Color(35, 100, 40));
         c = new Color(255, 255, 255);
         colors.put(19, c);
         colors.put(20, c);
         colors.put(22, new Color(26, 70, 161));
         colors.put(24, new Color(214, 207, 154));
         colors.put(31, new Color(20, 140, 0));
         colors.put(41, new Color(255, 251, 86));
         colors.put(42, new Color(240, 240, 240));
         c = new Color(164, 164, 164);
         colors.put(43, c);
         colors.put(44, c);
         c = new Color(157, 77, 55);
         colors.put(45, c);
         colors.put(46, c);
         colors.put(47, c);
         colors.put(48, new Color(33, 76, 33));
         colors.put(49, new Color(15, 15, 24));
         c = new Color(255, 255, 255);
         colors.put(50, c);
         colors.put(51, c);
         colors.put(52, c);
         colors.put(55, new Color(252, 87, 0));
         colors.put(57, new Color(156, 234, 231));
         colors.put(79, new Color(90, 134, 191));
         colors.put(83, new Color(20, 140, 0));
         colors.put(86, new Color(255, 140, 0));
         colors.put(87, new Color(128, 8, 8));
         colors.put(89, new Color(150, 110, 48));
         colors.put(91, new Color(255, 140, 0));
         List<Color> variants;
         variants = new ArrayList<Color>();
         variants.add(new Color(34, 100, 34));
         variants.add(new Color(40, 72, 0));
         variants.add(new Color(20, 105, 36));
         multiColors.put(18, variants);
         variants = new ArrayList<Color>();
         variants.add(new Color(241, 241, 241));
         variants.add(new Color(235, 129, 56));
         variants.add(new Color(185, 57, 197));
         variants.add(new Color(126, 156, 219));
         variants.add(new Color(212, 187, 32));
         variants.add(new Color(62, 198, 49));
         variants.add(new Color(221, 141, 163));
         variants.add(new Color(63, 63, 63));
         variants.add(new Color(173, 180, 180));
         variants.add(new Color(31, 96, 123));
         variants.add(new Color(135, 56, 205));
         variants.add(new Color(35, 46, 141));
         variants.add(new Color(82, 49, 27));
         variants.add(new Color(54, 74, 24));
         variants.add(new Color(167, 45, 41));
         variants.add(new Color(10, 10, 10));
         multiColors.put(35, variants);
     }
 
     @SuppressWarnings("unchecked")
     private void loadVars() {
         List<Color> variants;
         List<String> mckeys = getConfiguration().getKeys("multi-colors");
         if (mckeys != null) for (String key : mckeys) {
             Integer id = Integer.parseInt(key.substring(1));
             variants = new ArrayList<Color>();
             for (Object sublist : getConfiguration().getList("multi-colors." + key)) {
                 if (!(sublist instanceof List<?>)) continue;
                 List<Object> inner = ((List<Object>) sublist);
                 if (inner.size() < 3) continue;
                 if (inner.get(0) instanceof Integer && inner.get(1) instanceof Integer && inner.get(2) instanceof Integer)
                     variants.add(new Color((Integer) inner.get(0), (Integer) inner.get(1), (Integer) inner.get(2)));
             }
             multiColors.put(id, variants);
         }
         mckeys = getConfiguration().getKeys("colors");
         if (mckeys != null) for (String key : mckeys) {
             Integer id = Integer.parseInt(key.substring(1));
             List<Integer> values = getConfiguration().getIntList("colors." + key, new ArrayList<Integer>());
             if (values.size() < 3) continue;
             colors.put(id, new Color(values.get(0), values.get(1), values.get(2)));
         }
         showDepth = getConfiguration().getBoolean("depth.ground", true);
         showWaterDepth = getConfiguration().getBoolean("depth.water", true);
         showLavaDepth = getConfiguration().getBoolean("depth.lava", true);
     }
 
     public boolean isPlayerHidden(Player player) {
         return getConfiguration().getBoolean("hide." + player.getName(), false);
     }
 
     public String getAttributionString() {
         return getConfiguration().getString("attribution", "Map of the United Banana Empire");
     }
 
     public Pattern getSignRegex() {
         return Pattern.compile(getConfiguration().getString("tile-entities.signs.filter-regex", ".*"));
     }
 }
 
