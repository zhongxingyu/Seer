 package com.codisimus.plugins.pvpreward.listeners;
 
 import com.codisimus.plugins.pvpreward.PvPReward;
 import com.codisimus.plugins.pvpreward.Record;
 import com.codisimus.plugins.pvpreward.listeners.EntityEventListener.RewardType;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 /**
  * Listens for Player's logging, teleporting, and clicking tombstone signs
  *
  * @author Codisimus
  */
 public class PlayerEventListener extends PlayerListener {
     public static String denyTeleMsg;
     public static int telePenalty;
     public static boolean denyTele;
     public static boolean penalizeLoggers;
 
     /**
      * Adds the Outlaw tag to Players who log in with Karma above the Outlaw level
      *
      * @param event The PlayerJoinEvent that occurred
      */
     @Override
     public void onPlayerJoin (PlayerJoinEvent event) {
         //Return if the reward type is not Karma
         if (!EntityEventListener.rewardType.equals(RewardType.KARMA))
             return;
 
         Player player = event.getPlayer();
         Record record = PvPReward.getRecord(player.getName());
 
         //Changes the Player's display name if they are an Outlaw
        if (record.karma > EntityEventListener.amount && !EntityEventListener.outlawTag.isEmpty())
            player.setDisplayName(EntityEventListener.outlawTag+record.name);
     }
 
     /**
      * Denys teleporting if the Player is in combat
      *
      * @param event The PlayerTeleportEvent that occurred
      */
     @Override
     public void onPlayerTeleport (PlayerTeleportEvent event) {
         //Return if teleporting while in combat is allowed
         if (!denyTele)
             return;
 
         Player player = event.getPlayer();
         Record record = PvPReward.getRecord(player.getName());
 
         //Return if the Player is not in combat
         if (!record.inCombat)
             return;
 
         //Cancel the event and inflict damage on the Player
         player.sendMessage(denyTeleMsg);
         event.setCancelled(true);
         player.damage(telePenalty);
         return;
     }
 
     /**
      * Penalizes Players that quit while in combat
      *
      * @param event The PlayerQuitEvent that occurred
      */
     @Override
     public void onPlayerQuit (PlayerQuitEvent event) {
         //Return if quiting while in combat is allowed
         if (!penalizeLoggers)
             return;
 
         Player player = event.getPlayer();
         Record record = PvPReward.getRecord(player.getName());
 
         //Check if the Player is in combat
         if (record.inCombat)
             //Reward the attacker as if the quiting Player has died
             EntityEventListener.rewardPvP(player, record);
     }
     
     /**
      * Listens for Player's clicking on tombstone signs
      * The Player robs the grave if it is their own
      *
      * @param event The PlayerQuitEvent that occurred
      */
     @Override
     public void onPlayerInteract (PlayerInteractEvent event) {
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
 }
