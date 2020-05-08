 package net.battlenexus.paintball.listeners;
 
 import net.battlenexus.paintball.Paintball;
 import net.battlenexus.paintball.entities.PBPlayer;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.HashMap;
 
 public class PlayerListener implements Listener {
 
     protected HashMap<String, String> deathMessages = new HashMap<String, String>();
 
     public PlayerListener(JavaPlugin plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         event.getPlayer().teleport(Paintball.INSTANCE.paintball_world.getSpawnLocation());
         event.getPlayer().setFoodLevel(20);
         //PBPlayer.newPlayer(event.getPlayer()); This really isnt need here..
         //event.setJoinMessage("The faggot " + event.getPlayer().getDisplayName() + " has joined the game");
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         Player p = event.getPlayer();
         PBPlayer who;
         if ((who = PBPlayer.getPlayer(p)) == null) {
             return;
         }
         //Removes from queue if in queue when leaving
         Paintball.INSTANCE.getGameService().leaveQueue(who);
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player p = event.getPlayer();
         PBPlayer who;
         if ((who = PBPlayer.getPlayer(p)) == null) {
             return;
         }
         if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
             //GUN
             if (who.getCurrentWeapon() != null && p.getInventory().getItemInHand().getType().equals(who.getCurrentWeapon().getMaterial())) {
                 who.getCurrentWeapon().shoot();
             }
             //RELOAD
             if (who.getCurrentWeapon() != null && p.getInventory().getItemInHand().getType().equals(who.getCurrentWeapon().getReloadItem()) && who.getCurrentWeapon().clipeSize() > who.getCurrentWeapon().currentClipSize()) {
                 who.getCurrentWeapon().reload();
             }
         }
     }
 
     @EventHandler
     public void onPlayerHit(EntityDamageByEntityEvent event) {
         if(!(event.getDamager() instanceof Snowball) || !(event.getEntity() instanceof Player) || !(((Projectile) event.getDamager()).getShooter() instanceof Player)) {
             event.setCancelled(true);
             return;
         }
         Player victim = (Player) event.getEntity();
         PBPlayer pbvictim;
         if ((pbvictim = PBPlayer.getPlayer(victim)) == null) {
             return;
         }
         PBPlayer shooter = PBPlayer.toPBPlayer((Player) ((Snowball)event.getDamager()).getShooter());
         if (shooter.getCurrentTeam() != null) {
             if (shooter.getCurrentTeam().contains(pbvictim)) {
                 shooter.sendMessage("Watch out! " + pbvictim.getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " is on your team!");
             } else {
                 pbvictim.kill(shooter);
             }
         }
     }
 
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event) {
         String playerName = event.getEntity().getName();
         if (deathMessages.containsKey(playerName)) {
             event.setDeathMessage(deathMessages.get(playerName));
             deathMessages.remove(playerName);
         }
     }
 
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
         PBPlayer player;
         if ((player = PBPlayer.getPlayer(event.getPlayer())) != null) {
             if (player.isFrozen())
                 player.handleFrozen();
         }
     }
 
     @EventHandler
     public void onBlockDamage(BlockDamageEvent event) {
         //Part of custom "adventure mode" to stop breaking of blocks
         Player p = event.getPlayer();
         PBPlayer who;
         if ((who = PBPlayer.getPlayer(p)) == null) {
             return;
         }
         //Stop them if they are ingame
         if(who.isInGame()) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         //Part of custom "adventure mode" to stop placing of blocks
         Player p = event.getPlayer();
         PBPlayer who;
         if ((who = PBPlayer.getPlayer(p)) == null) {
             return;
         }
         //Stop them if they are ingame
         if(who.isInGame()) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player p = event.getPlayer();
        PBPlayer who;
        if ((who = PBPlayer.getPlayer(p)) == null) {
            return;
        }
        //Stops them from dropping items if they are inGame
        if(who.isInGame()) {
            return;
        }
    }

    @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event) {
         Player p = event.getPlayer();
         PBPlayer who;
         if ((who = PBPlayer.getPlayer(p)) == null) {
             return;
         }
         if(who.isInGame()) {
             String team = "";
             if(who.getCurrentTeam().equals(Paintball.INSTANCE.getGameService().blueTeam())) {
                 team = "(" + ChatColor.BLUE + "Blue" + ChatColor.RESET + ") ";
             } else { //They are on red team
                 team = "(" + ChatColor.RED + "Red" + ChatColor.RESET + ") ";
             }
             event.setFormat(team + event.getFormat());
         }
     }
 }
