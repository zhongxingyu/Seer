 /*
  Modreq Minecraft/Bukkit server ticket system
  Copyright (C) 2013 Sven Wiltink
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package modreq.commands;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import modreq.ModReq;
 import modreq.korik.SubCommandExecutor;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class UpdatemodreqCommand extends SubCommandExecutor {
 
     private ModReq plugin;
 
     public UpdatemodreqCommand(ModReq instance) {
         plugin = instance;
     }
 
     @command
     public void Null(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             if (sender.hasPermission("modreq.update")) {
                 if (plugin.getConfig().getBoolean("check-updates", true)) {
                     File Jar = new File(plugin.getDataFolder()
                             .getAbsolutePath() + "/" + plugin.latestVersion,
                             "modreq.jar");
                     File ChangeLog = new File(plugin.getDataFolder()
                             .getAbsolutePath() + "/" + plugin.latestVersion,
                             "Changelog.txt");
                     if (!ChangeLog.exists()) {
                         try {
                             saveUrl(ChangeLog.getAbsolutePath(),
                                     "http://website.shadowblox.com/plugins/modreqchangelog.txt");
                         } catch (Exception e) {
                         }
                     }
                     if (!Jar.exists()) {
 
                         Jar.getParentFile().mkdir();
                         try {
                             String link = "http://dev.bukkit.org/media/files/"
                                     + plugin.DownloadLink.split("/files/")[1];
                             if (fileIsApproved()) {
                                 saveUrl(Jar.getAbsolutePath(), link);
                                 sender.sendMessage(ChatColor.GOLD
                                         + "[ModReq]"
                                         + ChatColor.GREEN
                                         + "version "
                                         + plugin.latestVersion
                                         + " has been download to the plugin folder");
                                 return;
                             } else {
                                 Bukkit.broadcastMessage(ChatColor.RED + "The version of ModReq you are trying to download has not yet been approved, please be patient");
                             }
                         } catch (Exception e) {
                             sender.sendMessage(ChatColor.RED
                                     + "Could not download the latest version of ModReq");
                             return;
                         }
                     } else {
                         sender.sendMessage(ChatColor.RED
                                 + "You already have the lastest version downloaded");
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED
                             + "This feature is not enabled in the config");
                     return;
                 }
             }
         }
     }
 
     private boolean fileIsApproved() {
         try {
             URL file = new URL("http://dev.bukkit.org/server-mods/modreq/files");
            BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
 
             String inputLine;
             while ((inputLine = in.readLine()) != null) {
                if (inputLine != null) {
                    if (inputLine.contains("version " + ModReq.getInstance().latestVersion)) {
                         return true;
                     }
                 }
             }
             in.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return false;
     }
 
     public void saveUrl(final String filename, final String urlString)
             throws MalformedURLException, IOException {
 
         Bukkit.getScheduler().runTaskAsynchronously(ModReq.getInstance(),
                 new Runnable() {
             @Override
             public void run() {
 
                 BufferedInputStream in = null;
                 FileOutputStream fout = null;
                 try {
                     in = new BufferedInputStream(new URL(urlString)
                             .openStream());
                     fout = new FileOutputStream(filename);
 
                     byte data[] = new byte[1024];
                     int count;
                     while ((count = in.read(data, 0, 1024)) != -1) {
                         fout.write(data, 0, count);
                     }
                     if (in != null) {
                         in.close();
                     }
                     if (fout != null) {
                         fout.close();
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
             }
         });
     }
 }
