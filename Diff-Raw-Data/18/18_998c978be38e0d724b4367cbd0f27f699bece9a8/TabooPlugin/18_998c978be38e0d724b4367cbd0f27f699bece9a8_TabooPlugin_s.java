 /*
  * This file is part of Taboo.
  *
  * Copyright (c) 2013 CraftFire <http://www.craftfire.com/>
  * Taboo is licensed under the GNU Lesser General Public License.
  *
  * Taboo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Taboo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.taboo.layer.bukkit;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.craftfire.taboo.TabooException;
 import com.craftfire.taboo.TabooManager;
 import com.craftfire.taboo.TabooPlayer;
 
 import com.craftfire.commons.util.LoggingManager;
 
 public class TabooPlugin extends JavaPlugin implements Listener {
     private LoggingManager logger;
     private TabooManager manager;
 
     @Override
     public void onEnable() {
         this.logger = new LoggingManager(getLogger().getName(), "[Taboo]");
         this.logger.info("Enabling Taboo");
         try {
             this.manager = loadTabooManager();
         } catch (TabooException e) {
             this.logger.stackTrace(e);
             this.logger.severe("Error occurred during initialization of Taboo. Disabling self.");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         getServer().getPluginManager().registerEvents(this, this);
         this.logger.info("Taboo enabled");
     }
 
     @Override
     public void onDisable() {
         this.manager = null;
         this.logger.info("Taboo disabled.");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("taboo") && args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
             sender.sendMessage("Reloading Taboo...");
             this.logger.info("Reloading Taboo (command issued by " + sender.getName() + ")");
             try {
                 this.manager = loadTabooManager();
                 sender.sendMessage("Taboo reloaded");
                 this.logger.info("Taboo reloaded");
             } catch (TabooException e) {
                 sender.sendMessage("Failed to reload Taboo. Check errors on console.");
                 this.logger.stackTrace(e);
             }
             return true;
         }
         return false;
     }
 
     @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(final AsyncPlayerChatEvent event) throws InterruptedException, ExecutionException {
        this.logger.debug("Got an AsyncPlayerChatEvent");
         String message = event.getMessage();
         final TabooPlayer player = new TabooBukkitPlayer(event.getPlayer());
         if (event.isAsynchronous()) {
             Future<String> future = getServer().getScheduler().callSyncMethod(this, new Callable<String>() {
                 @Override
                 public String call() {
                     return TabooPlugin.this.manager.processMessage(event.getMessage(), player, true);
                 }
             });
            message = future.get();
         } else {
             message = this.manager.processMessage(message, player, false);
         }
         if (message.isEmpty()) {
             event.setCancelled(true);
             return;
         }
         event.setMessage(message);
     }
 
     protected TabooManager loadTabooManager() throws TabooException {
         TabooManager manager = new TabooManager(new BukkitLayer(getServer(), this), getDataFolder());
         manager.setLoggingManager(this.logger);
         manager.load();
         return manager;
     }
 }
