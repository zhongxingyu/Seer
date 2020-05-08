 package org.zone.commandit.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.commons.io.FileUtils;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.scheduler.BukkitTask;
 import org.zone.commandit.CommandIt;
 
 public class Updater {
     
     private static final String url = "http://dev.thechalkpot.com:8080/job/CommandIt/Release/artifact/bin/version.txt";
     
     public void init() {
         if (task == null && active) {
             task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new UpdateTask(), 0, 20 * 60 * 15);
         }
     }
     
     public void stop() {
         if (task != null) {
             task.cancel();
             task = null;
         }
     }
     
     private File pluginFile;
     private CommandIt plugin;
     boolean active = false;
     boolean auto = false;
     String toFetch;
     private BukkitTask task;
     private Version currentVersion;
     private Version availableVersion;
     
     public Updater(CommandIt plugin, File pluginFile) {
         this.plugin = plugin;
         this.pluginFile = pluginFile;
         currentVersion = Version.parse(plugin.getDescription().getVersion());
         availableVersion = currentVersion;
         if (currentVersion.build == -1) {
             plugin.getLogger().warning("Running an in-house dev build! Auto-update disabled!");
         }
         active = plugin.getPluginConfig().getBoolean("updater.auto-check");
         auto = plugin.getPluginConfig().getBoolean("updater.auto-install");
     }
     
     public void installUpdate(CommandSender cs, Version newVersion, String fetch) {
         URL dl = null;
         File fl = null;
         long t = System.nanoTime() / 1000000;
         cs.sendMessage("Updating to version " + newVersion + "...");
         try {
             fl = new File(plugin.getServer().getUpdateFolderFile(), pluginFile.getName());
             dl = new URL(fetch);
             FileUtils.copyURLToFile(dl, fl);
             cs.sendMessage(String.format("Update successfully installed! (%ds)", System.nanoTime() / 1000000 - t));
         } catch (Exception e) {
             cs.sendMessage("Failed to install update:" + e.getLocalizedMessage());
         }
     }
     
     private class UpdateTask implements Runnable {
         
         @Override
         public void run() {
             URL source;
             try {
                 source = new URL(url);
             } catch (MalformedURLException e) {
                 return;
             }
             try {
                 Configuration c = YamlConfiguration.loadConfiguration(source.openStream());
                 Version available = Version.parse(c.getString("version"));
                 if (available.getBuild() > currentVersion.getBuild()) {
                     if (auto) {
                         installUpdate(plugin.getServer().getConsoleSender(), available, "http://dev.bukkit.org/media/files/" + c.getString("download"));
                     } else {
                         availableVersion = available;
                         toFetch = "http://dev.bukkit.org/media/files/" + c.getString("download");
                     }
                 }
             } catch (IOException e) {
                 
             }
         }
         
     }
     
     public static class Version {
         
         int major, minor, revision, build;
         
         private Version(int major, int minor, int revision, int build) {
             this.major = major;
             this.minor = minor;
             this.revision = revision;
             this.build = build;
         }
         
         public String toString() {
             return String.format("%d.%d.%d-%d", major, minor, revision, build);
         }
         
         public static Version parse(String s) {
             String[] parts = s.split("-");
             String build = parts[1];
             parts = parts[0].split("\\.");
             if (build.equalsIgnoreCase("DEV"))
                 return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), -1);
             else
                 return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(build));
         }
         
         public int getMajor() {
             return major;
         }
         
         public int getMinor() {
             return minor;
         }
         
         public int getRevision() {
             return revision;
         }
         
         public int getBuild() {
             return build;
         }
         
     }
     
     public Version getCurrentVersion() {
         return currentVersion;
     }
     
     public Version getAvailableVersion() {
         return availableVersion;
     }
     
 }
