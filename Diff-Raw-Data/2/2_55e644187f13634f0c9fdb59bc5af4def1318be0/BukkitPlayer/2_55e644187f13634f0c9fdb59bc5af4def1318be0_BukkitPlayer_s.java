 package it.mcblock.mcblockit.bukkit;
 
 import it.mcblock.mcblockit.api.MCBIPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  * A Bukkit implementation of MCBlockIt API
  * 
  * @author Matt Baxter
  * 
  *         Copyright 2012 Matt Baxter
  * 
  *         Licensed under the Apache License, Version 2.0 (the "License");
  *         you may not use this file except in compliance with the License.
  *         You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  *         Unless required by applicable law or agreed to in writing, software
  *         distributed under the License is distributed on an "AS IS" BASIS,
  *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *         See the License for the specific language governing permissions and
  *         limitations under the License.
  * 
  */
 public class BukkitPlayer extends MCBIPlayer {
     private final Player player;
 
     public BukkitPlayer(Player player) {
         this.player = player;
     }
 
     @Override
     public String getIP() {
        return this.player.getAddress().getAddress().getHostAddress();
     }
 
     @Override
     public String getName() {
         return this.player.getName();
     }
 
     @Override
     public void kick(final String reason) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(MCBlockItPlugin.instance, new Runnable() {
 
             @Override
             public void run() {
                 BukkitPlayer.this.player.kickPlayer(reason);
             }
 
         });
     }
 
     @Override
     public void messageIfAdmin(final String message) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(MCBlockItPlugin.instance, new Runnable() {
 
             @Override
             public void run() {
                 if (BukkitPlayer.this.player.hasPermission("mcblockit.notifications")) {
                     BukkitPlayer.this.player.sendMessage(message);
                 }
             }
 
         });
     }
 
     @Override
     public void sendMessage(final String message) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(MCBlockItPlugin.instance, new Runnable() {
 
             @Override
             public void run() {
                 BukkitPlayer.this.player.sendMessage(message);
             }
 
         });
     }
 
 }
