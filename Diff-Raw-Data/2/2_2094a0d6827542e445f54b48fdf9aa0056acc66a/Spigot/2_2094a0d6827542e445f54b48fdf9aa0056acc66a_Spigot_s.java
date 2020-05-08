 package com.md_5.spigot;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.UnknownHostException;
 import java.util.Map;
 import net.minecraft.server.Block;
 import net.minecraft.server.ChunkCoordinates;
 import net.minecraft.server.ConvertProgressUpdater;
 import net.minecraft.server.Convertable;
 import net.minecraft.server.EntityTracker;
 import net.minecraft.server.IProgressUpdate;
 import net.minecraft.server.IWorldAccess;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.NetworkListenThread;
 import net.minecraft.server.ServerNBTManager;
 import net.minecraft.server.WorldLoaderServer;
 import net.minecraft.server.WorldManager;
 import net.minecraft.server.WorldMapCollection;
 import net.minecraft.server.WorldServer;
 import net.minecraft.server.WorldSettings;
 import net.minecraft.server.WorldType;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.world.WorldInitEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldSaveEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Spigot extends JavaPlugin implements Listener {
 
     public static Spigot instance;
     //
     private String restartScriptLocation;
     private int timeoutTime;
     private boolean restartOnCrash;
     private boolean filterUnsafeIps;
     private String whitelistMessage;
     //
     private WatchdogThread watchdog;
     //
     private MinecraftServer console;
     private Map<String, World> worlds;
 
     @Override
     public void onEnable() {
         instance = this;
         //
         FileConfiguration conf = getConfig();
         conf.options().copyDefaults(true);
         saveConfig();
         restartScriptLocation = conf.getString("restart-script-location");
         timeoutTime = conf.getInt("timeout-time");
         restartOnCrash = conf.getBoolean("restart-on-crash");
         filterUnsafeIps = conf.getBoolean("filter-unsafe-ips");
         whitelistMessage = conf.getString("whitelist-message");
         //
         console = ((CraftServer) getServer()).getHandle().server;
         worlds = (Map<String, World>) getPrivate(console.server, "worlds");;
         console.primaryThread.setUncaughtExceptionHandler(new ExceptionHandler());
         //
         hijackWorlds();
         //
         watchdog = new WatchdogThread(timeoutTime * 1000L, restartOnCrash);
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
                 watchdog.tick();
             }
         }, 1, 1);
         //
         register();
         //
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     private void hijackWorlds() {
         for (org.bukkit.World w : getServer().getWorlds()) {
             WorldCreator creator = new WorldCreator(w.getName());
             creator.seed(w.getSeed());
             creator.environment(w.getEnvironment());
             creator.generator(w.getGenerator());
             creator.type(w.getWorldType());
             creator.generateStructures(w.canGenerateStructures());
             //
             int dimension = ((CraftWorld) w).getHandle().dimension;
             int gamemode = getServer().getDefaultGameMode().getValue();
             WorldMapCollection maps = ((CraftWorld) w).getHandle().worldMaps;
             //
             unloadWorld(w, true);
             getLogger().info("Unloaded world " + w.getName());
             //
             createWorld(creator, gamemode, maps, dimension);
         }
     }
 
     private void register() {
         for (int i = 0; i < 255; i++) {
             Block old = Block.byId[i];
             boolean n = Block.n[i];
             int lightBlock = Block.lightBlock[i];
             boolean p = Block.p[i];
             int lightEmission = Block.lightEmission[i];
             boolean r = Block.r[i];
             boolean s = Block.s[i];
             //
             Block.byId[i] = null;
             Block replaced = null;
             switch (i) {
                 case 2:
                     //replaced = new SpecialGrass(i);
                     break;
                 case 6:
                     replaced = new SpecialSapling(i, 15);
                     break;
                 case 59:
                     replaced = new SpecialCrops(i, 88);
                     break;
                 case 75:
                     replaced = new SpecialRedstoneTorch(i, 115, false);
                     break;
                 case 76:
                     replaced = new SpecialRedstoneTorch(i, 99, true);
                     break;
                 case 81:
                     replaced = new SpecialCactus(i, 70);
                     break;
                 case 83:
                     replaced = new SpecialReed(i, 71);
                     break;
                 case 104:
                     replaced = new SpecialStem(i, Block.PUMPKIN);
                     break;
                 case 105:
                     replaced = new SpecialStem(i, Block.MELON);
                     break;
             }
             if (replaced != null) {
                 getLogger().info("Replaced block id: " + replaced.id);
             } else {
                 Block.byId[i] = old;
             }
             //
             Block.n[i] = n;
             Block.lightBlock[i] = lightBlock;
             Block.p[i] = p;
             Block.lightEmission[i] = lightEmission;
             Block.r[i] = r;
             Block.s[i] = s;
         }
     }
 
     @Override
     public void onDisable() {
         watchdog.interrupt();
         try {
             watchdog.join();
         } catch (InterruptedException ex) {
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         restart();
         return true;
     }
 
     @EventHandler
     public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
         if (filterUnsafeIps) {
             try {
                 String ip = event.getAddress().getHostAddress();
                 String[] split = ip.split("\\.");
                 StringBuilder lookup = new StringBuilder();
                 for (int i = split.length - 1; i >= 0; i--) {
                     lookup.append(split[i]);
                     lookup.append(".");
                 }
                 lookup.append("xbl.spamhaus.org.");
                 if (InetAddress.getByName(lookup.toString()) != null) {
                     event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "Your IP address is flagged as unsafe by spamhaus.org/xbl");
                 }
             } catch (UnknownHostException ex) {
                 //
             }
         }
     }
 
     @EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (getServer().hasWhitelist() && !event.getPlayer().isWhitelisted()) {
             event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, whitelistMessage);
         }
     }
 
     public void restart() {
         try {
             File file = new File(restartScriptLocation);
             if (file.exists() && !file.isDirectory()) {
                 System.out.println("Attempting to restart with " + restartScriptLocation);
                 //
                 for (Player p : getServer().getOnlinePlayers()) {
                     p.kickPlayer("Server is restarting");
                 }
                 //
                 NetworkListenThread listenThread = ((CraftServer) getServer()).getHandle().server.networkListenThread;
                 listenThread.b = false;
                 //
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException ex) {
                     //
                 }
                 //
                 Field field = listenThread.getClass().getDeclaredField("d");
                 field.setAccessible(true);
                 ((ServerSocket) field.get(listenThread)).close();
                 //
                 try {
                     ((CraftServer) getServer()).getHandle().server.stop();
                 } catch (Throwable t) {
                     //
                 }
                 //
                 String os = System.getProperty("os.name").toLowerCase();
                 if (os.contains("win")) {
                     Runtime.getRuntime().exec("cmd /c start " + file.getPath());
                 } else {
                     Runtime.getRuntime().exec(file.getPath());
                 }
                 System.exit(0);
             } else {
                 System.out.println("Startup script '" + restartScriptLocation + "' does not exist!");
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     public World createWorld(WorldCreator creator, int gamemode, WorldMapCollection maps,int dimension) {
         CraftServer craft = console.server;
         //
         if (creator == null) {
             throw new IllegalArgumentException("Creator may not be null");
         }
 
         String name = creator.name();
         ChunkGenerator generator = creator.generator();
         File folder = new File(craft.getWorldContainer(), name);
         World world = craft.getWorld(name);
         WorldType type = WorldType.getType(creator.type().getName());
         boolean generateStructures = creator.generateStructures();
 
         if (world != null) {
             return world;
         }
 
         if ((folder.exists()) && (!folder.isDirectory())) {
             throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
         }
 
         if (generator == null) {
             generator = craft.getGenerator(name);
         }
 
         Convertable converter = new WorldLoaderServer(craft.getWorldContainer());
         if (converter.isConvertable(name)) {
             getLogger().info("Converting world '" + name + "'");
             converter.convert(name, new ConvertProgressUpdater(console));
         }
 
         boolean used = false;
         do {
             for (WorldServer server : console.worlds) {
                 used = server.dimension == dimension;
                 if (used) {
                     dimension++;
                     break;
                 }
             }
         } while (used);
         boolean hardcore = false;
 
         WorldServer internal = new SpecialWorld(console, new ServerNBTManager(craft.getWorldContainer(), name, true), name, dimension, new WorldSettings(creator.seed(), gamemode, generateStructures, hardcore, type), creator.environment(), generator);
         if (!(worlds.containsKey(name.toLowerCase()))) {
             return null;
         }
 
         internal.worldMaps = maps;
 
         internal.tracker = new EntityTracker(console, internal); // CraftBukkit
         internal.addIWorldAccess((IWorldAccess) new WorldManager(console, internal));
         internal.difficulty = 1;
         internal.setSpawnFlags(true, true);
         console.worlds.add(internal);
 
         if (generator != null) {
             internal.getWorld().getPopulators().addAll(generator.getDefaultPopulators(internal.getWorld()));
         }
 
         getServer().getPluginManager().callEvent(new WorldInitEvent(internal.getWorld()));
         System.out.print("Preparing start region for level " + (console.worlds.size() - 1) + " (Seed: " + internal.getSeed() + ")");
 
         if (internal.getWorld().getKeepSpawnInMemory()) {
             short short1 = 196;
             long i = System.currentTimeMillis();
             for (int j = -short1; j <= short1; j += 16) {
                 for (int k = -short1; k <= short1; k += 16) {
                     long l = System.currentTimeMillis();
 
                     if (l < i) {
                         i = l;
                     }
 
                     if (l > i + 1000L) {
                         int i1 = (short1 * 2 + 1) * (short1 * 2 + 1);
                         int j1 = (j + short1) * (short1 * 2 + 1) + k + 1;
 
                         System.out.println("Preparing spawn area for " + name + ", " + (j1 * 100 / i1) + "%");
                         i = l;
                     }
 
                     ChunkCoordinates chunkcoordinates = internal.getSpawn();
                     internal.chunkProviderServer.getChunkAt(chunkcoordinates.x + j >> 4, chunkcoordinates.z + k >> 4);
 
                     while (internal.updateLights()) {
                         ;
                     }
                 }
             }
         }
         getServer().getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
         return internal.getWorld();
     }
 
     public boolean unloadWorld(World world, boolean save) {
         if (world == null) {
             return false;
         }
 
         WorldServer handle = ((CraftWorld) world).getHandle();
 
         if (!(console.worlds.contains(handle))) {
             return false;
         }
 
         /*
          if (!(handle.dimension > 1)) {
          return false;
          }
          */
         if (handle.players.size() > 0) {
             return false;
         }
 
         WorldUnloadEvent e = new WorldUnloadEvent(handle.getWorld());
         getServer().getPluginManager().callEvent(e);
 
         if (e.isCancelled()) {
             return false;
         }
 
         if (save) {
             handle.save(true, (IProgressUpdate) null);
             handle.saveLevel();
             WorldSaveEvent event = new WorldSaveEvent(handle.getWorld());
             getServer().getPluginManager().callEvent(event);
         }
 
         worlds.remove(world.getName().toLowerCase());
         console.worlds.remove(console.worlds.indexOf(handle));
 
         return true;
     }
 
     private Object getPrivate(Object clazz, String field) {
         Object result = null;
         try {
             Field f = clazz.getClass().getDeclaredField(field);
             f.setAccessible(true);
             result = f.get(clazz);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return result;
     }
 
     public static void setPrivate(Class clazz, Object obj, String field, Object value) {
         try {
 
             Field f = clazz.getDeclaredField(field);
             f.setAccessible(true);
             Field modifiersField = Field.class.getDeclaredField("modifiers");
             modifiersField.setAccessible(true);
             modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
             f.set(obj, value);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 }
