 /*
  * The MIT License
  *
  * Copyright (c) 2011 Andrew Williams (Nik_Doof/Matalok)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.pleaseignore.BukkitXMPP;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 /**
  * Handle events for all Player related events
  * @author Matalok
  */
 public class BukkitXMPPPlayerListener extends PlayerListener {
     private final BukkitXMPP plugin;
 
     public BukkitXMPPPlayerListener(BukkitXMPP instance) {
         plugin = instance;
     }
 
     @Override
     public void onPlayerChat(PlayerChatEvent event) {
         Player p = event.getPlayer();
 	String msg = event.getMessage();
         try {
             plugin.sendMUCMessage("[" + p.getDisplayName() + "] " + msg);
         } catch (Exception e) {
             // TODO: Error handling
         }
 
     }
 
     @Override
     public void onPlayerQuit (PlayerQuitEvent event) {
     	Player p = event.getPlayer();
         try {
             plugin.sendMUCMessage(p.getDisplayName() + " has left the server");
         } catch (Exception e) {
             // TODO: Error handling
         }
     }
 
     @Override
     public void onPlayerJoin (PlayerJoinEvent event) {
     	Player p = event.getPlayer();
         try {
             plugin.sendMUCMessage(p.getDisplayName() + " has joined the server");
         } catch (Exception e) {
             // TODO: Error handling
         }
     }
 }
 
