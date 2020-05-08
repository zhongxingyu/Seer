 package arena.newliberty.com;
 
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set; 
 
 public class Arena extends JavaPlugin implements Listener {
 
     //TODO put these in the config
     public static final String ENTERING_OLYMPIAD = ChatColor.RED + "Entering Olympiad in 3 seconds...";
     public static final String FIGHT_STARTING_IN = ChatColor.RED + "Fight starting in %d seconds...";
     public static final String FIGHT_STARTED = ChatColor.BLUE + "Fight!";
     LinkedList<Player> queue = new LinkedList<Player>();
     public boolean taken = false; //boolean to see if the arena is taken
     private Player player1;
     private Player player2;
     public static Economy econ = null;
 
     public void onEnable() {
         // Create all the default configs under a tree
         final FileConfiguration config = getConfig();
         config.addDefault("Command.join.price", 100);
         config.addDefault("Command.join.message.success", "You were successfully added to the queue.");
         config.addDefault("Command.join.message.error.queue", "You are already in the queue!");
         config.addDefault("Command.join.message.error.money", "You do not have enough money!");
         config.addDefault("Command.leave.message.success", "You were successfully removed from the queue.");
         config.addDefault("Command.leave.message.error.queue", "You are not in the queue!");
         config.addDefault("Command.check.message.success", "Your position in the queue is: %position%");
         config.addDefault("Command.check.message.error.queue", "You are not in the queue!");
         config.addDefault("Arena.open", "The Olympiad arena is now open!");
         config.addDefault("Winner.message", "WINNER!\nPlease collect your items...");
         config.addDefault("Wait.time", 10);
         config.addDefault("Cleanup.time", 30);
         config.addDefault("Command.help.message", "/olympiad j to join the queue.\n/olympiad l to leave the queue.\n/olympiad c to check your position in the queue.");
         config.addDefault("Timers.check.queue", 100L);
         config.addDefault("Command.join.price", 100);
         config.addDefault("Winner.prize", 150);
 
         config.addDefault("Arena.1.1.x", 0);
         config.addDefault("Arena.1.1.y", 0);
         config.addDefault("Arena.1.1.z", 0);
         config.addDefault("Arena.1.2.x", 0);
         config.addDefault("Arena.1.2.y", 0);
         config.addDefault("Arena.1.2.z", 0);
 
         config.addDefault("Hold.1.1.x", 0);
         config.addDefault("Hold.1.1.y", 0);
         config.addDefault("Hold.1.1.z", 0);
 
         config.options().copyDefaults(true);
 
         saveConfig(); //save the file
         
         setupEconomy(); //set up vault
 
         
         final Server server = getServer();
         server.getPluginManager().registerEvents(this, this);
 
 
         /*
         * When the queue size greater than 1 start the arena and remove those 2 players from the queue
         */
         server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
                 if (queue.size() >= 2 && taken == false) { //if the queue is ready and the arena isn't taken
                     Player first = queue.remove(0);
                     Player second = queue.remove(0);
                     start(first, second);
                 }
             }
 
 
             /**
              * Teleport the players to the arena
              */
             private void start(final Player player1, final Player player2) {
                 player1.sendMessage(ENTERING_OLYMPIAD); //starting ..
                 player2.sendMessage(ENTERING_OLYMPIAD);
 
                 server.getScheduler().scheduleSyncDelayedTask(Arena.this, new ArenaFightStart(player1, player2, config, server), 60L); // 3 second wait = 60 tick
             }
 
 
         }, 0L, config.getLong("Timers.check.queue"));
     }
 
     /*
       * onCommand method gets the command the user did
       * if the user did join or j then it checks to see if inventory is empty and adds them to the queue
       * if they did leave it checks to see if they are in the queue then removes them
       * if the user this check it gets their spot in the queue
       * if the user did ? it displays a message
       */
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         Player player = (Player) sender;
         if (cmd.getName().equalsIgnoreCase("olympiad") && args.length == 1) {
             if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")) {
                     if (!queue.contains(player) && econ.has(player.getPlayerListName(), getConfig().getDouble("Command.join.price"))) {
                         queue.addLast(player);
                         econ.withdrawPlayer(player.getPlayerListName(), getConfig().getDouble("Command.join.price"));
                         player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Command.join.message.success")));
                         player.teleport(player.getWorld().getBlockAt(getConfig().getInt("Hold.1.1.x"), getConfig().getInt("Hold.1.1.y"), getConfig().getInt("H.1.1.z")).getLocation());
                     } else 
                     	if (queue.contains(player))  { // At this point the player will always be in the queue
                         player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Command.join.message.error.queue")));
                    
                     	}
                     else {
                         player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Command.join.message.error.money")));
                     }
                     
                 
             } else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("l")) {
                 if (queue.remove(player)) {
                 	econ.depositPlayer(player.getPlayerListName(), getConfig().getDouble("Command.join.price"));
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Command.leave.message.success")));
                 } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Command.leave.message.error.queue")));
                 }
             } else if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("c")) {
                 int queuePos = queue.indexOf(player);
                 if (queuePos != -1) {
                 	econ.withdrawPlayer(player.getPlayerListName(), getConfig().getDouble("Command.join.price"));
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Command.check.message.success").replaceAll("%position%", Integer.toString(queuePos))));
                 } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Command.check.message.error.queue")));
                 }
             } else if (args[0].equalsIgnoreCase("help") || args[0].equals("?")) {
                 player.sendMessage(ChatColor.BLUE + (getConfig().getString("Command.help.message")));
             }
             return true;
         }
         else if (cmd.getName().equalsIgnoreCase("olympiad") && args.length == 0){
         	player.sendMessage(ChatColor.BLUE + (getConfig().getString("Command.help.message")));
         }
         return false;
     }
 
     final public HashSet<String> playing = new HashSet<String>();
     
     @EventHandler
     public void onDeath(PlayerDeathEvent event){
     	
     	 if(playing.contains(event.getEntity().getName())){ //error here?
     		 
     		if(event.getEntity().getName().equals(player1.getName())){ //
     			playing.remove(player1.getName());
     			playing.remove(player2.getName());
     			player2.sendMessage(getConfig().getString("Winner.message")); 
     			econ.depositPlayer(player2.getName(), getConfig().getDouble("Winner.prize"));
     			class Countdown implements Runnable {
                     private int count;
                     private int tid = -1;
 
                     Countdown(int count) {
                         this.count = count;
                     }
 
                     @Override
                     public void run() {
 
                         final String fightMsg = (count + " seconds left in cleanup...");
                         if(count == 30 || count == 15 || count == 10 || count <= 5){
                         player2.sendMessage(fightMsg);
                         }
                         if (count == 0) {
                             getServer().getScheduler().cancelTask(tid);
                             player2.sendMessage(ChatColor.BLUE + "Teleporting to spawn...");
                             player2.teleport(player2.getWorld().getSpawnLocation());
                             
                             
                         } else count--;
 
                     }
 
                     void start() {
                         tid = getServer().getScheduler().scheduleSyncRepeatingTask(Arena.this, this, 0, 20);
                     }
                 }
 
                 
                 new Countdown(getConfig().getInt("Cleanup.time")).start();
 
                 long x = getConfig().getInt("Cleanup.time") * 20;
         		getServer().getScheduler().scheduleSyncDelayedTask(Arena.this, new Runnable() {
 
         			   public void run() {
         			       Bukkit.broadcastMessage(ChatColor.GREEN + "The Olympiad arena is now open!");
         			       taken = false;
         			   }
         			}, x);
         		
     		 }
     		else if(event.getEntity().getName().equals(player2.getName())){ 
     			playing.remove(player1.getName());
     			playing.remove(player2.getName());
     			player1.sendMessage(getConfig().getString("Winner.message")); 
     			econ.depositPlayer(player1.getName(), getConfig().getDouble("Winner.prize"));
     			
     			class Countdown implements Runnable {
                     private int count;
                     private int tid = -1;
 
                     Countdown(int count) {
                         this.count = count;
                     }
 
                     @Override
                     public void run() {
 
                         final String fightMsg = (count + " seconds left in cleanup..");
                         if(count == 30 || count == 15 || count == 10 || count <= 5){
                         player1.sendMessage(fightMsg);
                         }
 
                         if (count == 0) {
                             getServer().getScheduler().cancelTask(tid);
                             player1.sendMessage(ChatColor.BLUE + "Teleporting to spawn...");
                             player1.teleport(player2.getWorld().getSpawnLocation());
                             
                         } else count--;
 
                     }
 
                     void start() {
                         tid = getServer().getScheduler().scheduleSyncRepeatingTask(Arena.this, this, 0, 20);
                     }
                 }
 
                 
                 new Countdown(getConfig().getInt("Cleanup.time")).start();
                 
                 long x = getConfig().getInt("Cleanup.time") * 20;
         		getServer().getScheduler().scheduleSyncDelayedTask(Arena.this, new Runnable() {
 
         			   public void run() {
         			       Bukkit.broadcastMessage(ChatColor.GREEN + "The Olympiad arena is now open!");
         			       taken = false;
         			   }
         			}, x);
     		 }
     		
         }
         
     }
 
 	final public Set<String> frozen = new HashSet<String>();
 
     public void freeze(final Player p) { //add the player to frozen
         frozen.add(p.getName());
     }
 
     public boolean isFrozen(final Player p) { //method to check if a user is frozen
         return frozen.contains(p.getName());
     }
 
     public void unfreeze(final Player p) { //remove the player from the HashSet frozen
         frozen.remove(p.getName());
     }
 
 
     @EventHandler(ignoreCancelled = true)
     public void onMove(PlayerMoveEvent e) { //check to see if the player is frozen then freeze them
         Player p = e.getPlayer();
         if (isFrozen(p)) {
             // Get old and new position
             Location location = e.getFrom().clone();
             Location to = e.getTo();
 
             // Allow head rotations
             location.setPitch(to.getPitch());
             location.setYaw(to.getYaw());
 
             // Reset position
             e.setTo(location);
         }
     }
 
     private class ArenaFightStart implements Runnable {
         private final FileConfiguration config;
         private final Server server;
 
         public ArenaFightStart(Player player1, Player player2, FileConfiguration config, Server server) {
             Arena.this.player1 = player1;
             Arena.this.player2 = player2;
             this.config = config;
             this.server = server;
         }
 
         @Override
         public void run() {
         	taken = true;
             player1.teleport(player1.getWorld().getBlockAt(config.getInt("Arena.1.1.x"), config.getInt("Arena.1.1.y"), config.getInt("Arena.1.1.z")).getLocation()); //teleport the players to the designated spot
             player2.teleport(player1.getWorld().getBlockAt(config.getInt("Arena.1.2.x"), config.getInt("Arena.1.2.y"), config.getInt("Arena.1.2.z")).getLocation());
 
             // countdown to fight! :D
 
             class Countdown implements Runnable {
                 private int count;
                 private int tid = -1;
 
                 Countdown(int count) {
                     this.count = count;
                 }
 
                 @Override
                 public void run() {
                     freeze(player1);
                     freeze(player2);
 
                     final String fightMsg = String.format(FIGHT_STARTING_IN, count);
                     player1.sendMessage(fightMsg);
                     player2.sendMessage(fightMsg);
 
                     if (count == 0) {
                         server.getScheduler().cancelTask(tid);
                         unfreeze(player1);
                         unfreeze(player2);
                         player1.sendMessage(FIGHT_STARTED);
                         player2.sendMessage(FIGHT_STARTED);
                         
                         
                     } else count--;
                     
                     playing.add(player1.getName()); 
                     playing.add(player2.getName());
                 }
 
                 void start() {
                     tid = server.getScheduler().scheduleSyncRepeatingTask(Arena.this, this, 0, 20);
                 }
             }
 
             
             new Countdown(config.getInt("Wait.time")).start();
         }
     }
     private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
     
     
     
 }
