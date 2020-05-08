 package com.titankingdoms.dev.TitanIRC;
 
 /**
  * Copyright (C) 2012 Chris Ward
  * <p/>
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class Format {
     public static TitanIRC instance;
     public static class GameToIRC
     {
         public static String NameFormat(String player)
         {
             String in = instance.getConfig().getString("config.format.game-to-irc.name-format");
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             return in;
         }
         public static String Join(String player)
         {
             String in = instance.getConfig().getString("config.format.game-to-irc.join");
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             return in;
         }
         public static String Death(String player)
         {
             String in = instance.getConfig().getString("config.format.game-to-irc.death");
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             return in;
         }
         public static String Quit(String player)
         {
             String in = instance.getConfig().getString("config.format.game-to-irc.quit");
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             return in;
         }
         public static String Kick(String player)
         {
             String in = instance.getConfig().getString("config.format.game-to-irc.kick");
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             return in;
         }
         public static String Message(String player, String message)
         {
             String in = instance.getConfig().getString("config.format.game-to-irc.message");
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
            in = in.replaceAll("%message%", message);
             return in;
         }
         public static String Action(String player, String action)
         {
             String in = instance.getConfig().getString("config.format.game-to-irc.action");
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             in = in.replaceAll("%message%", action);
             return in;
         }
     }
     public static class IRCToIRC
     {
         public static String Join(String nick, String channel)
         {
             String in = instance.getConfig().getString("config.format.irc-to-irc.join");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         public static String Part(String nick, String channel)
         {
             String in = instance.getConfig().getString("config.format.irc-to-irc.part");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         public static String Quit(String nick)
         {
             String in = instance.getConfig().getString("config.format.irc-to-irc.quit");
             in = in.replaceAll("%nick%", nick);
             return in;
         }
         public static String Kick(String nick, String channel)
         {
             String in = instance.getConfig().getString("config.format.irc-to-irc.kick");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         public static String Message(String nick, String channel, String message)
         {
             String in = instance.getConfig().getString("config.format.irc-to-irc.message");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             in = in.replaceAll("%message%", message);
             return in;
         }
         public static String Action(String nick, String channel, String action)
         {
             String in = instance.getConfig().getString("config.format.irc-to-irc.action");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             in = in.replaceAll("%message%", action);
             return in;
         }
     }
     public static class IRCToGame
     {
         public static String Join(String nick, String channel)
         {
             String in = instance.getConfig().getString("config.format.irc-to-game.join");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         public static String Part(String nick, String channel)
         {
             String in = instance.getConfig().getString("config.format.irc-to-game.part");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         public static String Quit(String nick)
         {
             String in = instance.getConfig().getString("config.format.irc-to-game.quit");
             in = in.replaceAll("%nick%", nick);
             return in;
         }
         public static String Kick(String nick, String channel)
         {
             String in = instance.getConfig().getString("config.format.irc-to-game.kick");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         public static String Message(String nick, String channel, String message)
         {
             String in = instance.getConfig().getString("config.format.irc-to-game.message");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             in = in.replaceAll("%message%", message);
             return in;
         }
         public static String Action(String nick, String channel, String action)
         {
             String in = instance.getConfig().getString("config.format.irc-to-game.action");
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             in = in.replaceAll("%message%", action);
             return in;
         }
     }
 
 }
