 /*
 * Copyright (C) 2011 <galaran.h@gmail.com>
 *
 * This file is part of the Bukkit plugin McTranslit.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307, USA.
 */
 
 package me.galaran.mctranslit;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 
 class McTranslitPlayerListener extends PlayerListener {
 
     private final McTranslit plugin;
 
     public McTranslitPlayerListener(McTranslit plugin) {
         this.plugin = plugin;
     }
     
     @Override
     public void onPlayerChat(PlayerChatEvent event) {
         if (event.getMessage().startsWith("/")) // ignore commands
             return;
         
         String transliteratedMessage = Translitter.transliterate(event.getMessage(), plugin.getTranslitMap());
         if (!event.getMessage().equals(transliteratedMessage)) {
             event.setCancelled(true);
             // print translitted message to console
            System.out.println("[translit]<" + event.getPlayer().getName() + "> " + transliteratedMessage);
             for (Player curPlayer : plugin.getServer().getOnlinePlayers()) {
                 if (plugin.getTranslitPlayers().contains(curPlayer.getName())) {
                     curPlayer.sendMessage('<' + event.getPlayer().getDisplayName() + "> " + transliteratedMessage);
                 } else {
                     curPlayer.sendMessage('<' + event.getPlayer().getDisplayName() + "> " + event.getMessage());
                 }
             } // for
         } // if
     }
     
 }
