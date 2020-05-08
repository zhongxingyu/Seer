 package com.github.deltawhy.abbarules;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 import org.bukkit.scheduler.BukkitTask;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 
 public class AbbaRules extends JavaPlugin implements Listener {
     Scoreboard sb;
     Objective obj;
     List<String> playerList;
     Map<Material, Integer> blockValues;
     boolean ready = false;
     boolean started = false;
 
     BukkitTask countdownTask;
     BukkitTask timerTask;
 
     public void onDisable() {
         if (obj != null) {
             obj.unregister();
             obj = null;
         }
         if (countdownTask != null) countdownTask.cancel();
         if (timerTask != null) timerTask.cancel();
     }
 
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
         playerList = new ArrayList<String>();
         blockValues = new HashMap<Material, Integer>();
 
         //static for now
         blockValues.put(Material.REDSTONE_ORE, 1);
         blockValues.put(Material.GLOWING_REDSTONE_ORE, 1);
         blockValues.put(Material.LAPIS_ORE, 1);
         blockValues.put(Material.GOLD_ORE, 3);
         blockValues.put(Material.DIAMOND_ORE, 5);
         blockValues.put(Material.EMERALD_ORE, 7);
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         String usage = "/abba create: Start a game\n" +
                 "/abba join: Join a game\n" +
                 "/abba start: Start the timer\n" +
                 "/abba stop: Stop the timer\n" +
                 "/abba reset: End a game";
         if (!(sender instanceof Player)) {
             sender.sendMessage("This command can't be run from the console.");
             return false;
         }
         if (label.equals("abba")) {
             if (args.length == 0) {
                 sender.sendMessage(usage);
             } else if (args[0].equalsIgnoreCase("create")) {
                 createGame((Player)sender);
             } else if (args[0].equalsIgnoreCase("join")) {
                 addPlayer((Player)sender);
             } else if (args[0].equalsIgnoreCase("start")
                     && sender.getName().equalsIgnoreCase(playerList.get(0))) {
                 startGame((Player) sender);
             } else if (args[0].equalsIgnoreCase("stop")
                     && sender.getName().equalsIgnoreCase(playerList.get(0))) {
                 stopGame();
             } else if (args[0].equalsIgnoreCase("reset")
                     && sender.getName().equalsIgnoreCase(playerList.get(0))) {
                 resetGame((Player)sender);
             } else {
                 sender.sendMessage(usage);
             }
             return true;
         } else {
             return false;
         }
     }
 
     public void createGame(Player player) {
         if (sb == null) sb = Bukkit.getScoreboardManager().getMainScoreboard();
         if (obj != null) {
             obj.unregister();
             obj = null;
         }
         obj = sb.registerNewObjective("abba_score", "dummy");
         obj.setDisplayName("Score");
         obj.setDisplaySlot(DisplaySlot.SIDEBAR);
 
         playerList.clear();
         ready = true;
         started = false;
 
         Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " has started a game of ABBA rules caving!" +
                 " Join the game with /abba join.");
         player.sendMessage(ChatColor.AQUA + "Once everyone has joined, start the game with /abba start");
         addPlayer(player);
     }
 
     public void resetGame(Player player) {
         if (!ready) {
             player.sendMessage(ChatColor.RED + "There is no game to end.");
             return;
         }
         if (started) {
             stopGame();
         }
         if (obj != null) {
             obj.unregister();
             obj = null;
         }
         ready = false;
         started = false;
     }
 
     public void addPlayer(Player player) {
         if (!ready) {
             player.sendMessage(ChatColor.RED + "There is no game to join.");
             return;
         }
         OfflinePlayer op = Bukkit.getOfflinePlayer(player.getName());
         obj.getScore(op).setScore(1);
         obj.getScore(op).setScore(0);
         playerList.add(player.getName());
     }
 
     public void startGame(Player player) {
         if (!ready) {
             player.sendMessage(ChatColor.RED + "There is no game to start.");
             return;
         }
         if (countdownTask != null) countdownTask.cancel();
         countdownTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
             int time = 3;
             public void run() {
                 if (time > 0) {
                     Bukkit.broadcastMessage(ChatColor.GOLD + Integer.toString(time));
                     time--;
                 } else {
                     started = true;
                     Bukkit.broadcastMessage(ChatColor.GOLD + "Let the games begin!");
                     countdownTask.cancel();
                     countdownTask = null;
                     startTimer();
                 }
             }
         }, 0, 20);
     }
 
     public void startTimer() {
         timerTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            int time = 1200;
             public void run() {
                 if (time > 0) {
                     obj.getScore(Bukkit.getOfflinePlayer("Time")).setScore(time);
                     time--;
                 } else {
                     stopGame();
                 }
             }
         }, 0, 20);
     }
 
     public void stopGame() {
         if (!started) {
             return;
         }
         started = false;
         if (countdownTask != null) {
             countdownTask.cancel();
             countdownTask = null;
         }
         if (timerTask != null) {
             timerTask.cancel();
             timerTask = null;
         }
         Bukkit.broadcastMessage(ChatColor.GOLD + "Time's up!");
         sb.resetScores(Bukkit.getOfflinePlayer("Time"));
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         if (!started) return;
         Player player = event.getPlayer();
         if (player == null || !playerList.contains(player.getName())) {
             return;
         }
         Integer value = blockValues.get(event.getBlock().getType());
         if (value != null) {
             Score score = obj.getScore(Bukkit.getOfflinePlayer(player.getName()));
             score.setScore(score.getScore()+value);
         }
     }
 
 }
 
