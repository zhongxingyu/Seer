 package to.joe.j2mc.tournament;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.tournament.command.JoinCommand;
 import to.joe.j2mc.tournament.command.LeaveCommand;
 import to.joe.j2mc.tournament.command.admin.DuelCommand;
 
 public class J2MC_Tournament extends JavaPlugin implements Listener {
 
     public enum GameStatus {
         Fighting, Idle,
     }
 
     private Location startPositionA;
     private Location startPositionB;
     private Location respawnPosition;
     public ArrayList<String> participants = new ArrayList<String>();
     public ArrayList<String> roundList = new ArrayList<String>();
     public boolean registrationOpen = false;
     public List<Integer> itemList;
     public GameStatus status = GameStatus.Idle;
 
     private boolean isPowerOfTwo(int number) {
         return (number != 0) && ((number & (number - 1)) == 0);
     }
 
     public void setupRound() { //Fills roundlist with pairs to fight each other
         if (roundList.isEmpty()) { //If this isn't empty, do nothing because there are fights to be had
             if (participants.size() == 1) { //Only one player left, winner
                 J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + participants.get(0) + ChatColor.AQUA + " is the last player standing and wins this tournament!");
             } else if (isPowerOfTwo(participants.size())) { //Power of two, everybody can fight
                 roundList.addAll(participants);
             } else { //Not a power of two, this creates a bye with random pairs
                 int numberToEliminate = 1;
                 while (!isPowerOfTwo(participants.size() - numberToEliminate))
                     numberToEliminate++;
                 for (int x = 0; x < numberToEliminate * 2; x++) {
                     //select random person
                     Random playerPicker = new Random();
                     while (true) {
                         int playerNumber = playerPicker.nextInt(participants.size());
                         if (!roundList.contains(participants.get(playerNumber))) {
                             roundList.add(participants.get(playerNumber));
                             break;
                         }
                     }
                 }
             }
         }
     }
 
     public void load() {
         //Setup start positions
         String world = this.getConfig().getString("startLocation.world");
         startPositionA = new Location(this.getServer().getWorld(world), this.getConfig().getInt("startLocation.a.x"), this.getConfig().getInt("startLocation.a.y"), this.getConfig().getInt("startLocation.a.z"));
         startPositionB = new Location(this.getServer().getWorld(world), this.getConfig().getInt("startLocation.b.x"), this.getConfig().getInt("startLocation.b.y"), this.getConfig().getInt("startLocation.b.z"));
         respawnPosition = new Location(this.getServer().getWorld(world), this.getConfig().getInt("spawnLocation.x"), this.getConfig().getInt("spawnLocation.y"), this.getConfig().getInt("spawnLocation.z"));
         startPositionA.setYaw(this.getConfig().getInt("startLocation.a.yaw"));
         startPositionB.setYaw(this.getConfig().getInt("startLocation.b.yaw"));
         respawnPosition.setYaw(this.getConfig().getInt("spawnLocation.yaw"));
 
         //Setup inventory
         itemList = this.getConfig().getIntegerList("inventory");
     }
 
     @Override
     public void onEnable() {
         //Read configuration
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
 
         load();
 
         this.getServer().getPluginManager().registerEvents(this, this);
 
         this.getCommand("join").setExecutor(new JoinCommand(this));
         this.getCommand("leave").setExecutor(new LeaveCommand(this));
         this.getCommand("duel").setExecutor(new DuelCommand(this));
     }
 
     public void processKill(String victor, String loser, boolean killed) {
         Player v = J2MC_Manager.getCore().getServer().getPlayer(victor);
         Player l = J2MC_Manager.getCore().getServer().getPlayer(loser);
         Logger logger = J2MC_Manager.getCore().getLogger();
         if (killed) {
             J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + loser + ChatColor.AQUA + " has been slain.");
             logger.log(Level.INFO, loser + " has been slain, " + victor + " wins");
         } else {
             J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + loser + ChatColor.AQUA + " has abandoned the fight.");
             logger.log(Level.INFO, loser + " has disconnected, " + victor + " wins");
             l.teleport(respawnPosition);
         }
         v.teleport(respawnPosition);
         status = GameStatus.Idle;
         participants.remove(l);
        roundList.remove(l);
        roundList.remove(v);
         J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + victor + ChatColor.AQUA + " wins this duel!");
     }
 
     @EventHandler
     public void onDisconnect(PlayerQuitEvent event) {
         if (status == GameStatus.Fighting) { //If a fight isn't in progress, we don't care if anyone quits
             if (event.getPlayer().getName().equals(roundList.get(0))) {
                 processKill(roundList.get(1), roundList.get(0), false);
             } else if (event.getPlayer().getName().equals(roundList.get(1))) {
                 processKill(roundList.get(0), roundList.get(1), false);
             }
         }
     }
 
     @EventHandler
     public void onDeath(PlayerDeathEvent event) {
         if (status == GameStatus.Fighting) { //If a fight isn't in progress, we don't care if anyone dies
             if (event.getEntity().getName().equals(roundList.get(0))) {
                 processKill(roundList.get(1), roundList.get(0), true);
                 event.getDrops().clear();
             } else if (event.getEntity().getName().equals(roundList.get(1))) {
                 processKill(roundList.get(0), roundList.get(1), true);
                 event.getDrops().clear();
             }
         }
     }
 
     private void giveInventory(Player player) {
         PlayerInventory pInventory = player.getInventory();
         pInventory.clear();
         for (Integer i : itemList) {
             if (i.equals(262) || i.equals(341) || i.equals(332))
                 pInventory.addItem(new ItemStack(i, 16));
             else if (i.equals(298) || i.equals(302) || i.equals(306) || i.equals(310) || i.equals(314) || i.equals(86))
                 pInventory.setHelmet(new ItemStack(i));
             else if (i.equals(299) || i.equals(303) || i.equals(307) || i.equals(311) || i.equals(315))
                 pInventory.setChestplate(new ItemStack(i));
             else if (i.equals(300) || i.equals(304) || i.equals(308) || i.equals(312) || i.equals(316))
                 pInventory.setLeggings(new ItemStack(i));
             else if (i.equals(301) || i.equals(305) || i.equals(309) || i.equals(313) || i.equals(317))
                 pInventory.setBoots(new ItemStack(i));
             else
                 pInventory.addItem(new ItemStack(i));
         }
         player.setHealth(player.getMaxHealth());
         player.setFoodLevel(17); //max 20, 6 or below = no running, 18 or above = regenerate
     }
 
     public void fight() {
         if (roundList.size() >= 2) {
             Logger l = J2MC_Manager.getCore().getLogger();
             Player p1 = J2MC_Manager.getCore().getServer().getPlayerExact(roundList.get(0));
             Player p2 = J2MC_Manager.getCore().getServer().getPlayerExact(roundList.get(1));
             String p1name = roundList.get(0);
             String p2name = roundList.get(1);
 
             //Check for absent players here
             if (p1 == null || p2 == null) {
                 roundList.remove(p1name);
                 roundList.remove(p2name);
             }
             if (p1 == null && p2 == null) {
                 J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.AQUA + "Both " + ChatColor.RED + p1name + ChatColor.AQUA + " and " + ChatColor.RED + p2name + ChatColor.AQUA + " are offline.");
                 J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.AQUA + "Both players are removed from the tournament!");
                 l.log(Level.INFO, "both " + p1name + " and " + p2name + " were offline, both removed");
                 participants.remove(p1name);
                 participants.remove(p2name);
                 return;
             }
             if (p1 == null) {
                 J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + p1name + ChatColor.AQUA + " is offline.");
                 J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + p1name + ChatColor.AQUA + " forfeits and " + ChatColor.RED + p2name + ChatColor.AQUA + " wins by default!");
                 l.log(Level.INFO, p1name + " is offline, " + p2name + " wins");
                 participants.remove(p1name);
                 return;
             }
             if (p2 == null) {
                 J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + p2name + ChatColor.AQUA + " is offline.");
                 J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.RED + p2name + ChatColor.AQUA + " forfeits and " + ChatColor.RED + p1name + ChatColor.AQUA + " wins by default!");
                 l.log(Level.INFO, p2name + " is offline, " + p1name + " wins");
                 participants.remove(p2name);
                 return;
             }
             J2MC_Manager.getCore().getServer().broadcastMessage(ChatColor.AQUA + "Now fighting: " + ChatColor.RED + p1name + ChatColor.AQUA + " and " + ChatColor.RED + p2name);
             status = GameStatus.Fighting;
             giveInventory(p1);
             giveInventory(p2);
             p1.teleport(startPositionA);
             p2.teleport(startPositionB);
         }
     }
 }
