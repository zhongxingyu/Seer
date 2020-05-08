 package com.bukkit.tcial.stats;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 public class StatsBlockListener extends BlockListener
 {
   // <Username, <Blocktype, count> >
   private static Map<String, Map<Long, Long>> playerBlocksPlaced = new HashMap<String, Map<Long, Long>>();
   private static Map<String, Map<Long, Long>> playerBlocksBroken = new HashMap<String, Map<Long, Long>>();
 
   private static enum EBlockAction
   {
     ePlaced, eBroken
   };
 
   private StatsPlugin plugin;
 
   public StatsBlockListener(StatsPlugin plugin)
   {
     this.plugin = plugin;
   }
 
   private void blockAdd(EBlockAction blockAction, String userName, long blockType)
       throws StatsException
   {
     Map<String, Map<Long, Long>> playerBlockMap;
     if (blockAction == EBlockAction.ePlaced)
     {
       playerBlockMap = playerBlocksPlaced;
     }
     else if (blockAction == EBlockAction.eBroken)
     {
       playerBlockMap = playerBlocksBroken;
     }
     else
     {
       throw new StatsException("unknown blockaction");
     }
 
     // get player
     Map<Long, Long> blockMap = playerBlockMap.get(userName);
 
     if (blockMap == null)
     {
       // player does not exist - create an entry
       blockMap = new HashMap<Long, Long>();
       playerBlockMap.put(userName, blockMap);
     }
 
     Long mapBlockCount = blockMap.get(blockType);
     if (mapBlockCount == null)
     {
       // blocktype does not exist - create an entry
       mapBlockCount = 1L;
     }
     else
     {
       // ok there is a blocktype - add this event
       mapBlockCount += 1;
     }
     blockMap.put(blockType, mapBlockCount);
 
     // check if we should submit our data to the server
     if (mapBlockCount >= Long.parseLong(StatsProperties.prop
         .getProperty(StatsProperties.C_EventSubmitTrigger)))
     {
       // try to send data to server
       boolean result = false;
       if (blockAction == EBlockAction.ePlaced)
       {
         result = StatsSender.addPlaced(userName, blockType, mapBlockCount);
       }
       else if (blockAction == EBlockAction.eBroken)
       {
         result = StatsSender.addBroken(userName, blockType, mapBlockCount);
       }
       if (result)
       { // on success reset the blocktype for this player
         blockMap.put(blockType, 0L);
       }
     }
   }
 
   @Override
   public void onBlockPlace(BlockPlaceEvent event)
   {
     String userName = event.getPlayer().getName();
     long blockType = event.getBlockPlaced().getType().getId();
     this.blockAdd(EBlockAction.ePlaced, userName, blockType);
   }
 
   @Override
   public void onBlockBreak(BlockBreakEvent event)
   {
     String userName = event.getPlayer().getName();
     long blockType = event.getBlock().getType().getId();
    this.blockAdd(EBlockAction.ePlaced, userName, blockType);
   }
 
   @Override
   public void onBlockDamage(BlockDamageEvent event)
   {
     // event.getPlayer().sendMessage(
     // "onBlockDamage: getDamageLevel():" + event.getDamageLevel().name());
   }
 }
