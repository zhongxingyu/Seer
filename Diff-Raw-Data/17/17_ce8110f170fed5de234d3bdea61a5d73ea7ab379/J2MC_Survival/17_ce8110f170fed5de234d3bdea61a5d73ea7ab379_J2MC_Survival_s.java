 package to.joe.j2mc.survival;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.minecraft.server.Packet20NamedEntitySpawn;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.vanish.VanishPerms;
 import org.kitteh.vanish.staticaccess.VanishNoPacket;
 import org.kitteh.vanish.staticaccess.VanishNotLoadedException;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.survival.command.ArenaCommand;
 import to.joe.j2mc.survival.command.JoinCommand;
 import to.joe.j2mc.survival.command.LeaveCommand;
 import to.joe.j2mc.survival.command.LobbyCommand;
 import to.joe.j2mc.survival.command.ReadyCommand;
 
 public class J2MC_Survival extends JavaPlugin implements Listener {
 
     public void copyFolder(File src, File dest) throws IOException {
         if (src.isDirectory()) {
             if (!dest.exists()) {
                 dest.mkdir();
             }
             String files[] = src.list();
             for (String file : files) {
                 File srcFile = new File(src, file);
                 File destFile = new File(dest, file);
                 copyFolder(srcFile, destFile);
             }
         } else {
             InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest);
             byte[] buffer = new byte[1024];
             int length;
             while ((length = in.read(buffer)) > 0) {
                 out.write(buffer, 0, length);
             }
             in.close();
             out.close();
         }
     }
 
     public boolean deleteFolder(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++) {
                 boolean success = deleteFolder(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
         return dir.delete();
     }
 
     public enum GameStatus {
         PreRound, Countdown, InGame, PostRound,
     }
 
     public enum LossMethod {
         Disconnect, Killed, Left,
     }
 
     public GameStatus status = GameStatus.PreRound;
     public ArrayList<String> participants = new ArrayList<String>();
     public ArrayList<String> deadPlayers = new ArrayList<String>();
     public ArrayList<String> readyPlayers = new ArrayList<String>();
     public ArrayList<String> mapCycle;
     public ArrayList<Integer> breakableBlocks;
     public int minPlayers = 2;
     public int maxPlayers = 2;
     int countdown;
     int maxWait;
     public double minReadyPercent;
     public World lobbyWorld;
     public World gameWorld;
     private FileConfiguration mapConfig;
     private File mapConfigFile;
     public SpawnManager spm;
     boolean tubeMines;
     String mapName;
     String author;
     int autostartTask;
     int compassUpdateTask;
 
     public void reloadCustomConfig() {
         if (mapConfigFile == null) {
             mapConfigFile = new File(getDataFolder(), "customConfig.yml");
         }
         mapConfig = YamlConfiguration.loadConfiguration(mapConfigFile);
         InputStream defConfigStream = this.getResource("customConfig.yml");
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             mapConfig.setDefaults(defConfig);
         }
     }
 
     public FileConfiguration getCustomConfig() {
         if (mapConfig == null) {
             this.reloadCustomConfig();
         }
         return mapConfig;
     }
 
     public void loadMainConfig() {
         countdown = this.getConfig().getInt("countdown");
         maxWait = this.getConfig().getInt("maxWait");
         minReadyPercent = this.getConfig().getDouble("percentReady");
         mapCycle = new ArrayList<String>(this.getConfig().getStringList("mapcycle"));
         tubeMines = this.getConfig().getBoolean("tubeMines");
     }
 
     public void fixTele(final Player player1, final Player player2) {
         if (player1.equals(player2))
             return;
         ((CraftPlayer) player1).getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(((CraftPlayer) player2).getHandle()));
         ((CraftPlayer) player2).getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(((CraftPlayer) player1).getHandle()));
         Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 
             @Override
             public void run() {
                 ((CraftPlayer) player1).getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(((CraftPlayer) player2).getHandle()));
                 ((CraftPlayer) player2).getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(((CraftPlayer) player1).getHandle()));
             }
         }, 20);
     }
 
     public void loadMap(boolean firstLoad) {
         List<Player> toTeleport = Arrays.asList(this.getServer().getOnlinePlayers());
         for (Player p : toTeleport) {
             if (p.getWorld().equals(gameWorld))
                 toLobby(p);
         }
         String newMap = mapCycle.get(0);
         mapCycle.add(mapCycle.remove(0));
         if (!firstLoad) {
             this.getServer().unloadWorld(gameWorld, false);
             deleteFolder(new File(gameWorld.getName()));
         }
         try {
             copyFolder(new File(newMap + "_bak"), new File(newMap));
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         gameWorld = this.getServer().createWorld(new WorldCreator(newMap));
 
         mapConfigFile = new File(getDataFolder(), newMap + ".yml");
         reloadCustomConfig();
         minPlayers = mapConfig.getInt("minPlayers");
         maxPlayers = mapConfig.getInt("maxPlayers");
         mapName = mapConfig.getString("longName");
         author = mapConfig.getString("author");
         spm = new SpawnManager(gameWorld, mapConfig.getStringList("spawns"), this);
         breakableBlocks = new ArrayList<Integer>(mapConfig.getIntegerList("breakableBlocks"));
 
         for (Player p : this.getServer().getOnlinePlayers()) {
             for (Player p2 : this.getServer().getOnlinePlayers()) {
                 fixTele(p, p2);
             }
         }
         status = GameStatus.PreRound;
         participants.clear();
         readyPlayers.clear();
         deadPlayers.clear();
         getServer().broadcastMessage(ChatColor.AQUA + "Now playing on: " + ChatColor.RED + mapName + ChatColor.AQUA + " by " + ChatColor.RED + author);
 
         autostartTask = getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 
             @Override
             public void run() {
                 if (participants.size() >= minPlayers) {
                     startCountdown();
                 } else {
                     loadMap(false);
                 }
             }
         }, maxWait * 20);
     }
 
     public void toLobby(Player p) {
         p.teleport(lobbyWorld.getSpawnLocation());
         p.setAllowFlight(true);
     }
 
     @Override
     public void onEnable() {
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
         loadMainConfig();
 
         this.getServer().getPluginManager().registerEvents(this, this);
         this.getCommand("join").setExecutor(new JoinCommand(this));
         this.getCommand("leave").setExecutor(new LeaveCommand(this));
         this.getCommand("ready").setExecutor(new ReadyCommand(this));
         this.getCommand("lobby").setExecutor(new LobbyCommand(this));
         this.getCommand("arena").setExecutor(new ArenaCommand(this));
 
         J2MC_Manager.getPermissions().addFlagPermissionRelation("j2mc.chat.spectator", 'P', true);
         //J2MC_Manager.getPermissions().addFlagPermissionRelation("vanish.see", 'P', true);
         J2MC_Manager.getPermissions().addFlagPermissionRelation("worldedit.navigation.thru", 'P', true);
         //J2MC_Manager.getPermissions().addFlagPermissionRelation("vanish.nopickup", 'P', true);
         //J2MC_Manager.getPermissions().addFlagPermissionRelation("vanish.nofollow", 'P', true);
         //J2MC_Manager.getPermissions().addFlagPermissionRelation("vanish.notrample", 'P', true);
         //J2MC_Manager.getPermissions().addFlagPermissionRelation("vanish.nointeract", 'P', true);
         //J2MC_Manager.getPermissions().addFlagPermissionRelation("vanish.silentchests", 'P', true);
         //J2MC_Manager.getPermissions().addFlagPermissionRelation("vanish.preventdamage", 'P', true);
         //J2MC_Manager.getPermissions().addFlagPermissionRelation("j2mc.teleport.to", 'P', true);
 
         //Run announceDead() once per day
         J2MC_Manager.getCore().getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 if (gameWorld.getTime() >= 12000 && gameWorld.getTime() < 13000)
                     announceDead();
             }
         }, 1000, 1000);
 
         //We are assuming that the first world loaded (which should be defined in server.properties) will be the lobby.
         //Also good if the plugin breaks.
         lobbyWorld = this.getServer().getWorlds().get(0);
         loadMap(true);
     }
 
     @Override
     public void onDisable() {
         this.getServer().unloadWorld(gameWorld, false);
         deleteFolder(new File(gameWorld.getName()));
     }
 
     public void scheduleCountdownMessage(final int time, final int totalTime) {
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 int number = totalTime - (totalTime - time);
                 if (number == 1)
                     getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + number + " second");
                 else if (number <= 5)
                     getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + number + " seconds");
                 else
                     getServer().broadcastMessage(ChatColor.AQUA + "" + number + " seconds");
             }
         }, (totalTime - time) * 20);
     }
 
     public void startCountdown() {
         getServer().getScheduler().cancelTask(autostartTask);
         status = GameStatus.Countdown;
         spm.spawnPlayers();
         for (Player p : this.getServer().getOnlinePlayers()) {
             for (Player p2 : this.getServer().getOnlinePlayers()) {
                 fixTele(p, p2);
             }
         }
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 startGame();
             }
         }, countdown * 20);
         getServer().broadcastMessage(ChatColor.AQUA + "The survival games begin in " + countdown + " seconds");
         getServer().broadcastMessage(ChatColor.AQUA + "Best of luck");
         for (int x = 1; x < countdown; x++) {
             if (x <= 10 || x % 10 == 0)
                 scheduleCountdownMessage(x, countdown);
         }
     }
 
     public void startGame() {
         getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "BEGIN!");
         status = GameStatus.InGame;
 
         compassUpdateTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
             @Override
             public void run() {
                 for (String s1 : participants) {
                     Player p1 = getServer().getPlayer(s1);
                     ArrayList<String> participantsMinusPlayer = new ArrayList<String>(participants);
                     participantsMinusPlayer.remove(s1);
                     Player closestPlayer = getServer().getPlayer(participantsMinusPlayer.get(0));
                     for (String s2 : participants) {
                         Player p2 = getServer().getPlayer(s2);
                         if (p1.equals(p2))
                             continue;
                         if (p1.getLocation().distanceSquared(p2.getLocation()) < p1.getLocation().distanceSquared(closestPlayer.getLocation())) {
                             closestPlayer = p2;
                         }
                     }
                     p1.setCompassTarget(closestPlayer.getLocation());
                 }
             }
         }, 0, 100);
     }
 
     private void announceDead() {
         if (status != GameStatus.InGame)
             return;
         if (deadPlayers.size() == 0) {
             this.getServer().broadcastMessage(ChatColor.AQUA + "In the previous day no players were eliminated");
             return;
         }
         if (deadPlayers.size() > 1)
             this.getServer().broadcastMessage(ChatColor.AQUA + "In the previous day, the following players were eliminated");
         else
             this.getServer().broadcastMessage(ChatColor.AQUA + "In the previous day, the following player was eliminated");
         int offset = 0;
         for (final String s : deadPlayers) {
             offset += 30;
             J2MC_Manager.getCore().getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                 @Override
                 public void run() {
                     getServer().broadcastMessage(ChatColor.RED + s);
                 }
             }, offset);
         }
         offset += 30;
         J2MC_Manager.getCore().getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 getServer().broadcastMessage(ChatColor.AQUA + "" + participants.size() + " players remain");
             }
         }, offset);
         deadPlayers.clear();
     }
 
     public void handleLoss(Player p, LossMethod m) {
         String n = p.getName();
         switch (m) {
             case Disconnect:
                 getServer().broadcast(ChatColor.RED + n + ChatColor.AQUA + " has disconnected", "j2mc.chat.spectator");
                 break;
             case Killed:
                 getServer().broadcast(ChatColor.RED + n + ChatColor.AQUA + " has been killed", "j2mc.chat.spectator");
                 break;
             case Left:
                 getServer().broadcast(ChatColor.RED + n + ChatColor.AQUA + " has left the game", "j2mc.chat.spectator");
                 break;
         }
         setSpectate(p, true);
         deadPlayers.add(n);
         final boolean weather = p.getWorld().isThundering();
         p.getWorld().strikeLightningEffect(p.getLocation());
         getServer().broadcastMessage(ChatColor.AQUA + "You hear the sound of a cannon in the distance");
         p.sendMessage(ChatColor.RED + "You have been eliminated");
         p.getWorld().setStorm(weather);
         participants.remove(n);
         checkForWinner();
     }
 
     public void checkForWinner() {
         if (participants.size() == 1) {
             String winner = participants.get(0);
             Player p = getServer().getPlayer(winner);
             spm.preparePlayer(p);
             setSpectate(p, true);
             p.teleport(gameWorld.getSpawnLocation());
             getServer().broadcastMessage(ChatColor.RED + winner + ChatColor.AQUA + " has won the survival games!");
             mapConfigFile = new File(getDataFolder(), mapCycle.get(0) + ".yml");
             reloadCustomConfig();
             mapName = mapConfig.getString("longName");
             author = mapConfig.getString("author");
             getServer().broadcastMessage(ChatColor.AQUA + "The next map is " + ChatColor.RED + mapName + ChatColor.AQUA + " by " + ChatColor.RED + author);
             getServer().broadcastMessage(ChatColor.AQUA + "The next map will load in 30 seconds");
             status = GameStatus.PostRound;
             deadPlayers.clear();
             getServer().getScheduler().cancelTask(compassUpdateTask);
             getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                 @Override
                 public void run() {
                     loadMap(false);
                 }
             }, 600);
         }
     }
 
     @EventHandler
     public void onDisconnect(PlayerQuitEvent event) {
         if (participants.contains(event.getPlayer().getName())) {
             switch (status) {
                 case InGame:
                 case Countdown:
                     handleLoss(event.getPlayer(), LossMethod.Disconnect);
                     return;
                 case PreRound:
                     getServer().broadcastMessage(ChatColor.RED + event.getPlayer().getName() + ChatColor.AQUA + " has left the survival games!");
                     spm.removePlayer(event.getPlayer().getName());
                     participants.remove(event.getPlayer().getName());
                     readyPlayers.remove(event.getPlayer().getName());
                     return;
             }
         }
     }
 
     @EventHandler
     public void onDeath(PlayerDeathEvent event) {
         if (status == GameStatus.InGame || status == GameStatus.Countdown)
            event.setDeathMessage("");
         if (participants.contains(event.getEntity().getName())) {
             switch (status) {
                 case InGame:
                 case Countdown:
                     handleLoss(event.getEntity(), LossMethod.Killed);
                     return;
             }
         }
     }
 
     //stolen from https://github.com/tomjw64/HungerBarGames/blob/master/HungerBarGames/src/me/tomjw64/HungerBarGames/Listeners/Countdown/CountdownMotionListener.java
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
         if (status == GameStatus.Countdown && participants.contains(event.getPlayer().getName())) {
             Location from = event.getFrom();
             Location to = event.getTo();
             double x = Math.floor(from.getX());
             double z = Math.floor(from.getZ());
             if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
                 if (tubeMines) {
                     event.getFrom().getWorld().createExplosion(to, 0, false);
                     event.getPlayer().damage(20);
                 } else {
                     x += .5;
                     z += .5;
                     event.getPlayer().teleport(new Location(from.getWorld(), x, from.getY(), z, to.getYaw(), to.getPitch()));
                 }
             }
         }
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         if ((status == GameStatus.InGame || status == GameStatus.Countdown) && participants.contains(event.getPlayer().getName()) && !breakableBlocks.contains(event.getBlock().getTypeId()))
             event.setCancelled(true);
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent event) {
         final Player player = event.getPlayer();
         this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 if (!player.getWorld().equals(lobbyWorld) || !player.getWorld().equals(gameWorld)) {
                     toLobby(player);
                     setSpectate(player, true);
                     switch (status) {
                         case Countdown:
                         case PreRound:
                             player.sendMessage(ChatColor.AQUA + "Now playing on: " + ChatColor.RED + mapName + ChatColor.AQUA + " by " + ChatColor.RED + author);
                             player.sendMessage(ChatColor.AQUA + "The round has not started yet. Type" + ChatColor.RED + " /join" + ChatColor.AQUA + " to join");
                             break;
                         case InGame:
                             player.sendMessage(ChatColor.AQUA + "Now playing on: " + ChatColor.RED + mapName + ChatColor.AQUA + " by " + ChatColor.RED + author);
                             player.sendMessage(ChatColor.AQUA + "A round is currently in progress");
                         case PostRound:
                             player.sendMessage(ChatColor.AQUA + "The next map will be loading shortly.");
                     }
                 }
             }
         }, 1);
     }
 
     @EventHandler
     public void onDrop(PlayerDropItemEvent event) {
         if ((status == GameStatus.Countdown || status == GameStatus.InGame) && participants.contains(event.getPlayer().getName()) && event.getItemDrop().getItemStack().getType().equals(Material.COMPASS))
             event.setCancelled(true);
     }
 
     public void setSpectate(Player player, boolean spec) {
         try {
             if (spec && !VanishNoPacket.isVanished(player.getName())) {
                 player.setGameMode(GameMode.ADVENTURE);
                 J2MC_Manager.getPermissions().addFlag(player, 'P');
                 //XXX
                 VanishPerms.toggleSeeAll(player);
                 VanishPerms.toggleNoPickup(player);
                 VanishPerms.toggleNoFollow(player);
                 //Trample?
                 VanishPerms.toggleNoInteract(player);
                 VanishPerms.toggleSilentChestReads(player);
                 VanishPerms.toggleDamageIn(player);
                 VanishPerms.toggleDamageOut(player);
                 VanishNoPacket.toggleVanishSilent(player);
                 player.setAllowFlight(true);
             } else if (!spec && VanishNoPacket.isVanished(player.getName())) {
                 player.setGameMode(GameMode.SURVIVAL);
                 J2MC_Manager.getPermissions().delFlag(player, 'P');
                 VanishPerms.toggleSeeAll(player);
                 VanishPerms.toggleNoPickup(player);
                 VanishPerms.toggleNoFollow(player);
                 //Trample?
                 VanishPerms.toggleNoInteract(player);
                 VanishPerms.toggleSilentChestReads(player);
                 VanishPerms.toggleDamageIn(player);
                 VanishPerms.toggleDamageOut(player);
                 VanishNoPacket.toggleVanishSilent(player);
                 player.setAllowFlight(false);
             }
         } catch (VanishNotLoadedException e) {
             getServer().getLogger().severe("Vanish no packet missing!");
         }
     }
 
     //This is a cheap trick for getting spawn point coords
     @EventHandler
     public void onRightClick(PlayerInteractEvent event) {
         if (event.hasBlock() && event.getPlayer().isOp()) {
             Location l = event.getClickedBlock().getLocation();
             getServer().getLogger().info("- " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
         }
     }
 
     @EventHandler
     public void onRespawn(PlayerRespawnEvent event) {
         final Player p = event.getPlayer();
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 p.setAllowFlight(true);
             }
         }, 1);
     }
 }
