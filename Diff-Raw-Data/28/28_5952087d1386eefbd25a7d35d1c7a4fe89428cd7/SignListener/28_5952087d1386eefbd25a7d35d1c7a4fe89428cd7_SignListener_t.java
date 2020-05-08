 package com.github.lankylord.SimpleSigns;
 
 import java.util.logging.Logger;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
public class SignListener implements Listener {

     SimpleSigns plugin;
     static final Logger logger = Logger.getLogger("Minecraft");

     public SignListener(SimpleSigns instance) {
         this.plugin = instance;
     }

     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         Player player = event.getPlayer();
        if (!player.hasPermission("scs.colour")) {
             return;
         }
         for (int i = 0; i <= 3; i++) {
             String line = event.getLine(i);
             line = line.replaceAll("&(?<!&&)(?=[0-9a-fA-F])", "\u00A7")
                     .replace("&&", "&");
             event.setLine(i, line);
         }
     }
 }
