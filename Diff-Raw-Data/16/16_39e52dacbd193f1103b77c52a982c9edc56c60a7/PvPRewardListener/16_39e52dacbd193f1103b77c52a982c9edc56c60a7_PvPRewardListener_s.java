 package com.codisimus.plugins.pvpreward;
 
 import java.util.LinkedList;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 /**
  * Listens for PvP Events
  *
  * @author Codisimus
  */
 public class PvPRewardListener implements Listener {
     public static int telePenalty;
     public static boolean denyTele;
     public static boolean penalizeLoggers;
     public static boolean disableTollForPvP;
     public static boolean digGraves;
     public static LinkedList<String> rewardDisabledIn;
 
     /**
      * Flags Records as inCombat when the Player is attacked by another Player
      *
      * @param event The EntityDamageEvent that occurred
      */
     @EventHandler (priority = EventPriority.MONITOR)
     public void onEntityDamage(EntityDamageByEntityEvent event) {
         if (event.isCancelled())
             return;
         
         //Return if the Enitity damaged is not a Player
         Entity wounded = event.getEntity();
         if (!(wounded instanceof Player))
             return;
 
        //Return if the event was not PvP
         Entity attacker = event.getDamager();
         if (!(attacker instanceof Player))
             return;
 
         //Return if the Player is suicidal
         if (attacker.equals(wounded))
             return;
 
         Record record = PvPReward.getRecord(((Player)wounded).getName());
         record.startCombat(((Player)attacker).getName());
     }
 
     /**
      * Flags Records as inCombat when the Player is attacked by another Player
      *
      * @param event The EntityDamageEvent that occurred
      */
     @EventHandler (priority = EventPriority.MONITOR)
     public void onEntityDeath(EntityDeathEvent event) {
         //Return if the Enitity killed is not a Player
         Entity entitityKilled = event.getEntity();
         if (!(entitityKilled instanceof Player))
             return;
 
         Player deaded = (Player)entitityKilled;
         Record record = PvPReward.getRecord(deaded.getName());
 
         //Dig a grave for the killed Player if the option is enabled
         if (digGraves)
             record.digGrave(event.getDrops(), deaded.getLocation().getBlock());
 
         //Charge the death toll and return if the Player is not in combat
         if (!record.inCombat) {
             Rewarder.dropMoney(deaded);
             return;
         }
 
         //Charge the death toll if it is not disabled for PvP
         if (!disableTollForPvP)
             Rewarder.dropMoney(deaded);
 
         //Reward the PvP if it is not disabled in this world
         if (!rewardDisabledIn.contains(deaded.getWorld().getName()))
             Rewarder.rewardPvP(deaded, record);
     }
     
     /**
      * Adds the Outlaw tag to Players who log in with Karma above the Outlaw level
      *
      * @param event The PlayerJoinEvent that occurred
      */
     @EventHandler (priority = EventPriority.MONITOR)
     public void onPlayerJoin (PlayerJoinEvent event) {
         //Return if the reward type is not Karma
         if (!Rewarder.rewardType.equals(Rewarder.RewardType.KARMA))
             return;
 
         Player player = event.getPlayer();
         Record record = PvPReward.getRecord(player.getName());
 
         //Changes the Player's display name if they are an Outlaw
         if (record.isOutlaw() && !Record.outlawTag.isEmpty())
             player.setDisplayName(Record.outlawTag+record.name);
     }
 
     /**
      * Denys teleporting if the Player is in combat
      *
      * @param event The PlayerTeleportEvent that occurred
      */
     @EventHandler
     public void onPlayerTeleport (PlayerTeleportEvent event) {
         if (event.isCancelled())
             return;
         
         //Return if teleporting while in combat is allowed
         if (!denyTele)
             return;
 
         Player player = event.getPlayer();
         Record record = PvPReward.getRecord(player.getName());
 
         //Return if the Player is not in combat
         if (!record.inCombat)
             return;
 
         //Cancel the event and inflict damage on the Player
         player.sendMessage(PvPRewardMessages.getDenyTeleMsg());
         event.setCancelled(true);
         player.damage(telePenalty);
     }
 
     /**
      * Penalizes Players that quit while in combat
      *
      * @param event The PlayerQuitEvent that occurred
      */
     @EventHandler (priority = EventPriority.MONITOR)
     public void onPlayerQuit (PlayerQuitEvent event) {
         //Return if quiting while in combat is allowed
         if (!penalizeLoggers)
             return;
 
         Player player = event.getPlayer();
         Record record = PvPReward.getRecord(player.getName());
 
         //Check if the Player is in combat
         if (record.inCombat)
             //Reward the attacker as if the quiting Player has died
             Rewarder.rewardPvP(player, record);
     }
     
     /**
      * Listens for Player's clicking on tombstone signs
      * The Player robs the grave if it is their own
      *
      * @param event The PlayerQuitEvent that occurred
      */
     @EventHandler (priority = EventPriority.MONITOR)
     public void onPlayerInteract (PlayerInteractEvent event) {
         if (event.isCancelled())
             return;
         
         //Return if the Action was not clicking a Block
         if (!(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
             return;
 
         //Return if the clicked Block is not a Sign
         Block block = event.getClickedBlock();
         if (!block.getType().equals(Material.SIGN_POST))
             return;
 
         Player player = event.getPlayer();
         Sign sign = (Sign)block.getState();
         String name = sign.getLine(2);
 
         //Return if line 2 of the Sign is not the Player's name
         if (!player.getName().equals(name))
             return;
 
         Record record = PvPReward.getRecord(name);
 
         //Return if the Player does not have a recent grave
         if (record == null || record.signLocation == null)
             return;
 
         //Allow the Player to rob the grave if the the Sign Locations match
         if (block.getLocation().equals(record.signLocation))
             record.robGrave(player);
     }
     
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled())
             return;
         
         Block block = event.getBlock();
         
         //Return if the Block is not a Sign
         if (block.getTypeId() != 323)
             return;
 
         Sign sign = (Sign)block;
 
         for (Record record: PvPReward.getRecords())
             if (record.tombstone.equals(sign)) {
                 event.setCancelled(true);
                 break;
             }
     }
 }
