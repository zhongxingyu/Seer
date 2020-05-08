 /*
 Copyright (C) 2011 Serge Humphrey
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
 
 package com.btbb.figadmin;
 
 import java.util.Date;
 
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.entity.Player;
 
 import com.btbb.figadmin.FigAdmin;
 
 
 public class FigAdminPlayerListener extends PlayerListener {
     FigAdmin plugin;
 
     public FigAdminPlayerListener(FigAdmin instance) {
         this.plugin = instance;
     }
 
     public void onPlayerLogin(PlayerLoginEvent event) {
         Player player = event.getPlayer();
         for (int i = 0; i < plugin.bannedPlayers.size(); i++) {
             EditBan e = plugin.bannedPlayers.get(i);
             if (e.name.equals(player.getName().toLowerCase())) {
                 long tempTime = e.endTime;
                 boolean tempban = false;
                 if (tempTime > 0) {
                     // Player is banned. Check to see if they are still banned
                     // if it's a tempban
                     long now = System.currentTimeMillis()/1000;
                     long diff = tempTime - now;
                     if (diff <= 0) {
                         plugin.bannedPlayers.remove(i);
                         return;
                     }
                     tempban = true;
                 }
                 Date date = new Date();
                 date.setTime(tempTime * 1000);
                 String kickerMsg = null;
                 if (tempban) {
                     kickerMsg= plugin.formatMessage(plugin.getConfig().getString("messages.LoginTempban"));
                     kickerMsg = kickerMsg.replaceAll("%time%", date.toString());
                     kickerMsg = kickerMsg.replaceAll("%reason%", e.reason);
                 } else if (e.type == EditBan.BAN) { // make sure it isn't an ipban
                     kickerMsg  = plugin.formatMessage(plugin.getConfig().getString("messages.LoginBan"));
                     kickerMsg = kickerMsg.replaceAll("%time%", date.toString());
                     kickerMsg = kickerMsg.replaceAll("%reason%", e.reason);
                 }
                 if (kickerMsg != null) {
                     event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickerMsg);
                     return;
                 }
             }
             
         }
     }
 
     @Override
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         String ip = player.getAddress().getAddress().getHostAddress();
        System.out.println("connect from ip " + ip);
         for (int i = 0; i < plugin.bannedPlayers.size(); i++) {
             EditBan e = plugin.bannedPlayers.get(i);
             if (e.IP != null && e.IP.equals(ip)) {
                 // Player is banned.
                System.out.println("ip is banned");
                 event.setJoinMessage("You are ban");
                 String kickerMsg = plugin.formatMessage(plugin.getConfig().getString(
                         "messages.LoginIPBan"));
                 player.kickPlayer(kickerMsg);
 
                 if (!e.name.equals(player.getName().toLowerCase())) {
                    System.out.println("Updating name");
                     plugin.db.updateAddress(player.getName(), ip);
                 }
                 return;
             }
         }
     }
 }
