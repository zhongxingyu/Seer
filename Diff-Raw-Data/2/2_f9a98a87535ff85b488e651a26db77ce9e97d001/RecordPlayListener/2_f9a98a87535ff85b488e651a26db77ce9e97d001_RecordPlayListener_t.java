 package com.extrahardmode.extrastats;
 
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.Material;
 import org.bukkit.block.Jukebox;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 /**
  * @author Diemex
  */
 public class RecordPlayListener implements Listener
 {
     private final ExtraStatsPlugin plugin;
 
 
     public RecordPlayListener(ExtraStatsPlugin plugin)
     {
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
 
     /**
      * When a player right clicks a jukebox with a record in his hand
      */
     @EventHandler
     private void onRecordPlay(PlayerInteractEvent event)
     {
         //If the Item in hand will be inserted into a jukebox
        if (event.hasBlock() && event.getClickedBlock().getType() == Material.JUKEBOX && event.getMaterial().isRecord())
         {
             Jukebox beatBox = (Jukebox) event.getClickedBlock().getState();
             //record will only play if there is no record playing atm, then the record will be ejected first
             if (!beatBox.isPlaying())
             {
                 plugin.getServer().broadcastMessage("yay");
                 plugin.getStat(event.getPlayer().getName(), event.getPlayer().getWorld().getName(), "recordplays", readableRecord(event.getItem().getType())).incrementStat(1);
             }
         }
     }
 
 
     /**
      * Convert a Record into human readable form
      *
      * @param record record to convert
      *
      * @return recordname
      *
      * @throws java.lang.IllegalArgumentException
      *          if not a record
      */
     public static String readableRecord(Material record)
     {
         Validate.isTrue(record.isRecord(), record.name() + " is not a record");
         switch (record)
         {
             case GOLD_RECORD:
                 return "13";
             case GREEN_RECORD:
                 return "cat";
             case RECORD_3:
                 return "blocks";
             case RECORD_4:
                 return "chirp";
             case RECORD_5:
                 return "far";
             case RECORD_6:
                 return "mall";
             case RECORD_7:
                 return "mellohi";
             case RECORD_8:
                 return "stal";
             case RECORD_9:
                 return "strad";
             case RECORD_10:
                 return "ward";
             case RECORD_11:
                 return "11";
             case RECORD_12:
                 return "wait";
         }
         return "";
     }
 }
