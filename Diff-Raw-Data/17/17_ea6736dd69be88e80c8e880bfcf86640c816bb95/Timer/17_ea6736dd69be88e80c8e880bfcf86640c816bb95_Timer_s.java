 package com.projectbarks.nxannouncer.announcer;
 
 import com.projectbarks.nxannouncer.NXAnnouncer;
 import com.projectbarks.nxannouncer.config.Config;
 import lombok.Getter;
 import lombok.Setter;
 import lombok.extern.java.Log;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.util.logging.Level;
 
 @Log
 public class Timer extends BukkitRunnable implements Runnable {
 
     @Getter
     @Setter
     private int count;
     private NXAnnouncer main;
     private Config config;
 
     public Timer(NXAnnouncer main) {
         this.count = 0;
         this.main = main;
         this.config = main.getConf();
     }
 
     @Override
     public void run() {
         if (config.getAnnouncements().size() > 0) {
             if (main.getServer().getOnlinePlayers().length <= 0 && config.isNoPlayers()) {
                 log.log(Level.INFO, "Not enough players broadcast canceled");
                 return;
             }
             Announcement announcement = config.getAnnouncements().get(getCount());
             this.broadcast(config.getColor() + announcement.getColorizedHeader());
             for (String message : announcement.getTranslatedMessage()) {
                 String colorizedMessage = Announcement.colorize(message);
                String dynamicChar = Announcement.formatChar(colorizedMessage,
                        announcement.getColorizedHeader(),
                        announcement.getColorizedFooter());
                String wrappedColor = ChatColor.getLastColors(colorizedMessage);
                if (wrappedColor.length() <= 0) {
                    wrappedColor = ChatColor.getLastColors(colorizedMessage);
                 }
                this.broadcast(wrappedColor + dynamicChar + colorizedMessage);
             }
             this.broadcast(config.getColor() + announcement.getColorizedFooter());
             setCount(count + 1);
             if (getCount() >= config.getAnnouncements().size()) {
                 setCount(0);
             }
             log.log(Level.INFO, "{0}Message {1} has been sent to {2} players.", new Object[]{config.getPluginName(), getCount(), main.getServer().getOnlinePlayers().length});
         }
     }
 
     private void broadcast(String message) {
         for (Player player : Bukkit.getOnlinePlayers()) {
             player.sendMessage(message);
         }
     }
 }
