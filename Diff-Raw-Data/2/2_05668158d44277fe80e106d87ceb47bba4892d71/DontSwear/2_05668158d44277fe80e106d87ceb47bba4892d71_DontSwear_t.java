 /*
  * Copyright (c) 2013 LankyLord.
  * All rights reserved.
  * 
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * The name of the author may not be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.github.lankylord.dontswear;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author LankyLord
  */
 public class DontSwear extends JavaPlugin implements Listener {
 
   static final Logger logger = Logger.getLogger("Minecraft");
 
   @EventHandler
   public void onPlayerChat(AsyncPlayerChatEvent ce) {
     Player player = ce.getPlayer();
     for (String word : ce.getMessage().split(" ")) {
       word = word.toLowerCase();
       if (getConfig().getStringList("BadWords").contains(word))
         if (!player.hasPermission("DontSwear.bypass")) {
           ce.setCancelled(true);
           player.sendMessage(ChatColor.RED + "You're not allowed to say that.");
         }
     }
   }
 
   @Override
   public void onEnable() {
     logger.info("[DontSwear] DontSwear Enabled.");
     getConfig().options().copyDefaults(true);
     saveDefaultConfig();
     saveConfig();
     Bukkit.getServer().getPluginManager().registerEvents(this, this);
     if (getConfig().getBoolean("AutoUpdater.Enabled", true)) {
      Updater updater = new Updater(this, "dontswear", this.getFile(), Updater.UpdateType.DEFAULT, true);
       logger.info("[DontSwear] AutoUpdater Enabled.");
     }
     try {
       MetricsLite metrics = new MetricsLite(this);
       metrics.start();
     } catch (IOException e) {
       logger.info("[DontSwear] Error while submitting stats.");
     }
   }
 }
