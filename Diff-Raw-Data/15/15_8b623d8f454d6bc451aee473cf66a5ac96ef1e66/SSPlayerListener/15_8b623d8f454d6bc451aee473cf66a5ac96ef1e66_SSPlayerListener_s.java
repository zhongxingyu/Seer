 package com.acyrid.SunSteel.listeners;
 
 import com.acyrid.SunSteel.SunSteel;
 import com.acyrid.SunSteel.utils.SSConfig;
 import com.acyrid.SunSteel.utils.SSMechanics;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import java.util.*;
 import static java.lang.Math.abs;
 
 public class SSPlayerListener implements Listener{
     public SSPlayerListener(SunSteel plugin) {
     }
     private Map<Player, Set<Block>> revertSet = new HashMap<Player,Set<Block>>();
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event){
         Player player = event.getPlayer();
         Block waterWalkBlock;
         if(SSMechanics.hasSSHoverWater(player)||SSMechanics.hasSSHoverLava(player)){
             if(!revertSet.containsKey(player)){revertSet.put(player,new HashSet<Block>());}
             Block toBlockLoc = event.getTo().getBlock();
             Block fromBlockLoc = event.getFrom().getBlock();
             if (!fromBlockLoc.equals(toBlockLoc)){
                 final int range = 1;
                 for (int dx = -range; dx <=range; dx++) {
                     for (int dz = -range; dz <=range; dz++) {
                         Block block = toBlockLoc.getRelative(dx, -1, dz);
                         if(((block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER))&&
                                 SSMechanics.hasSSHoverWater(player)){
                             player.sendBlockChange(block.getLocation() ,Material.getMaterial(SSConfig.waterwalkID),(byte)0 );
                             revertSet.get(player).add(block);
                             revertCheck(player, Material.WATER);
                         }else if(((block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA))&&
                                 SSMechanics.hasSSHoverLava(player)){
                             player.sendBlockChange(block.getLocation() ,Material.getMaterial(SSConfig.lavawalkID),(byte)0 );
                             revertSet.get(player).add(block);
                             revertCheck(player, Material.LAVA);
                         }
                     }
                 }
             }
         }
     } 
     
     public void revertCheck(Player player, Material material){
         Block playerBlock = player.getLocation().getBlock();
         Iterator<Block> iterator = revertSet.get(player).iterator();
         while(iterator.hasNext()){
             Block nextBlock = iterator.next();
             int xCheck = abs(nextBlock.getX()) - abs(playerBlock.getX());
             int zCheck = abs(nextBlock.getZ()) - abs(playerBlock.getZ());
             if((abs(xCheck) > 3 || abs(zCheck) >3)){
                 player.sendBlockChange(nextBlock.getLocation(), material,(byte)0 );
                 iterator.remove();
             }
         }
     }
 }
