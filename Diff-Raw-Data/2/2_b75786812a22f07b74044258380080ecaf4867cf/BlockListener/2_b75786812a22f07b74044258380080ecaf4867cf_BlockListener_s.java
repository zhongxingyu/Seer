 package com.minecarts.miraclegrow.listener;
 
 import com.minecarts.miraclegrow.MiracleGrow;
 import com.minecarts.miraclegrow.BlockStateRestore.Cause;
 import org.bukkit.event.block.*;
 
 public class BlockListener extends org.bukkit.event.block.BlockListener {
     
     private MiracleGrow plugin;
     
     public BlockListener(MiracleGrow plugin) {
         this.plugin = plugin;
     }
     
     
     @Override
     public void onBlockPlace(BlockPlaceEvent event) {
         if(event.isCancelled()) return;
         
         plugin.scheduleRestore(event.getBlockReplacedState(), Cause.PLAYER);
     }
     
     @Override
     public void onBlockBreak(BlockBreakEvent event) {
         if(event.isCancelled()) return;
         
         plugin.scheduleRestore(event.getBlock(), Cause.PLAYER);
     }
     
     @Override
     public void onBlockFade(BlockFadeEvent event) {
         if(event.isCancelled()) return;
         
         plugin.scheduleRestore(event.getBlock(), Cause.WORLD);
     }
     
     @Override
     public void onBlockForm(BlockFormEvent event) {
         if(event.isCancelled()) return;
         
         plugin.scheduleRestore(event.getBlock(), Cause.WORLD);
     }
     
     @Override
     public void onBlockSpread(BlockSpreadEvent event) {
         if(event.isCancelled()) return;
         
         plugin.scheduleRestore(event.getBlock(), Cause.WORLD);
     }
     
     @Override
     public void onBlockFromTo(BlockFromToEvent event) {
         if(event.isCancelled()) return;
         
         plugin.scheduleRestore(event.getToBlock(), Cause.WORLD);
     }
     
     @Override
     public void onLeavesDecay(LeavesDecayEvent event) {
         if(event.isCancelled()) return;
         
        plugin.scheduleRestore(event.getBlock(), Cause.WORLD);
     }
     
     @Override
     public void onBlockIgnite(BlockIgniteEvent event) {
         if(event.isCancelled()) return;
         
         plugin.scheduleRestore(event.getBlock(), Cause.WORLD);
         // TODO: igniting portals?
     }
     
     @Override
     public void onBlockBurn(BlockBurnEvent event) {
         if(event.isCancelled()) return;
         
         plugin.scheduleRestore(event.getBlock(), Cause.WORLD);
     }
     
     @Override
     public void onBlockPistonRetract(BlockPistonRetractEvent event) {
         if(event.isCancelled()) return;
         if(!event.isSticky()) return;
         
         // pulled block
         plugin.scheduleRestore(event.getBlock().getRelative(event.getDirection(), 2), Cause.PLAYER);
         // pulled block's destination
         plugin.scheduleRestore(event.getBlock().getRelative(event.getDirection(), 1), Cause.PLAYER);
     }
     
     @Override
     public void onBlockPistonExtend(BlockPistonExtendEvent event) {
         if(event.isCancelled()) return;
         
         for(int i = 1, length = event.getLength(); i <= length; i++) {
             plugin.scheduleRestore(event.getBlock().getRelative(event.getDirection(), i), Cause.PLAYER);
         }
     }
     
 }
