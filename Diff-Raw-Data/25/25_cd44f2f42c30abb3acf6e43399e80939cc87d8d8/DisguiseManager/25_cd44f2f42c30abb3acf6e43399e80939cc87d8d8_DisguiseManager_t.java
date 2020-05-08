 package com.ne0nx3r0.blockdisguise.disguise;
 
 import com.ne0nx3r0.blockdisguise.BlockDisguise;
 import java.util.HashMap;
 import java.util.Map;
import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 public class DisguiseManager
 {
     final BlockDisguise plugin;
     final Map<String,DisguisedPlayer> players;
     
     public DisguiseManager(BlockDisguise plugin)
     {
         this.plugin = plugin;
         
         this.players = new HashMap<String,DisguisedPlayer>();
         
 // Setup scheduled task
         long lUpdateInverval = plugin.getConfig().getLong("update-interval");
         
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                 plugin, 
                 new hidePlayers(this),
                 lUpdateInverval,
                 lUpdateInverval
         );
     }
 
     public boolean isDisguised(Player player)
     {
         return this.players.containsKey(player.getName());
     }
 
 
     public void disguise(Player player, Material material, byte blockData)
     {
         this.players.put(player.getName(), new DisguisedPlayer(player.getName(),material,blockData));
         
         if(plugin.MAKE_PLAYERS_INVISIBLE)
         {
             for(Player pHideFrom: plugin.getServer().getOnlinePlayers())
             {
                 if(!pHideFrom.hasPermission("BlockDisguise.vision"))
                 {
                     pHideFrom.hidePlayer(player);
                 }
             }
         }
     }
 
     public void undisguise(Player player)
     {
         if(this.players.containsKey(player.getName()))
         {
             DisguisedPlayer dp = this.players.remove(player.getName());
 
             for(Player p : player.getWorld().getPlayers())
             {
                 p.sendBlockChange(dp.lastBlock.getLocation(), dp.lastBlock.getType(), dp.lastBlock.getData());
             }
             
             if(plugin.MAKE_PLAYERS_INVISIBLE)
             {
                 for(Player pShowTo: plugin.getServer().getOnlinePlayers())
                 {
                     pShowTo.showPlayer(player);
                 }
             }
         }
     }
 
     public void hideDisguisedPlayersFrom(Player player)
     {            
         if(plugin.MAKE_PLAYERS_INVISIBLE && !player.hasPermission("BlockDisguise.vision"))
         {
             for(DisguisedPlayer dp : this.players.values())
             {
                 player.hidePlayer(plugin.getServer().getPlayer(dp.playerName));
             }
         }
     }
 
    public boolean isDisguisedBlock(Block block)
     {
         for(DisguisedPlayer dp : this.players.values())
         {
             if(dp.lastBlock.equals(block))
             {
                 return true;
             }
         }
         return false;
     }
 }
