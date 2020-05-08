 package com.titankingdoms.dev.TitanIRC;
 
 import org.bukkit.Bukkit;
 
 import java.util.List;
 import java.util.Map;
 
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
     /**
      * Converts the colours from config to use the Minecraft colour codes.
      * @param text A whole string to convert to use correct colour codes
      * @return The coded text
      */
     public static String colour(String text)
     {
         char[] chararray = text.toCharArray();
         for(int i = 0; i < text.length(); i++)
         {
             if(text.toCharArray()[i] == '&')
             {
                 if(text.toCharArray()[i + 1] == '&')
                 {
                     i++;
                 }
                 else if("1234567890abcdefABCDEF".contains(Character.toString(text.toCharArray()[i + 1])))
                 {
                     chararray[i] = '\u00a7';
                 }
             }
         }
         return String.valueOf(chararray);
     }
 
     /**
      * Convert Minecraft colour codes to IRC colour codes
      * @param text Text to convert
      * @return Converted text
      */
     public static String convertToIRC(String text)
     {
         List<Map<?, ?>> colourmap = Bukkit.getServer().getPluginManager().getPlugin("TitanIRC").getConfig().getMapList("config.colour-map");
         String out = text;
         for(Map<?, ?> map : colourmap)
         {
             out = out.replaceAll("\u00a7" + map.get("game"), "\003" + map.get("irc"));
         }
         return out;
     }
 
     /**
      * Convert IRC colour codes to Minecraft colour codes
      * @param text Text to convert
      * @return Converted text
      */
     public static String convertToGame(String text)
     {
         List<Map<?, ?>> colourmap = Bukkit.getServer().getPluginManager().getPlugin("TitanIRC").getConfig().getMapList("config.colour-map");
         String out = text;
         for(Map<?, ?> map : colourmap)
         {
             out = out.replaceAll("\003" + map.get("irc"), "\u00a7" + map.get("game"));
             if(((String)map.get("irc")).contains("0"))
             {
                 out = out.replaceAll("\003" + ((String) map.get("irc")).substring(1),"\u00a7" + map.get("game"));
             }
         }
         return out;
     }
 
     public static TitanIRC instance;
 
     /**
      * Contains all of the formatting for use with events from game sent to irc
      */
     public static class GameToIRC
     {
         /**
          * Sets the %name% variable
          * @param player The *real name* of the player
          * @return The display name with prefixes or suffixes added
          */
         public static String NameFormat(String player)
         {
             String in = colour(instance.getConfig().getString("config.format.game-to-irc.name-format"));
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", instance.getServer().getPlayer(player).getDisplayName());
             return in;
         }
 
         /**
          * Formats the message used when a player enters the game
          * @param player The *real name* of the player
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Join(String player)
         {
             String in = colour(instance.getConfig().getString("config.format.game-to-irc.join"));
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             return in;
         }
 
         /**
          * Formats the message used when a player is killed
          * @param player The *real name* of the player
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Death(String player)
         {
             String in = colour(instance.getConfig().getString("config.format.game-to-irc.death"));
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             return in;
         }
 
         /**
          * Formats the message used when a player leaves the game
          * @param player The *real name* of the player
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Quit(String player)
         {
             String in = colour(instance.getConfig().getString("config.format.game-to-irc.quit"));
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             return in;
         }
         /**
          * Formats the message used when a player is kicked from the server
          * @param player The *real name* of the player
          * @param kickReason TODO
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Kick(String player, String kickReason)
         {
             String in = colour(instance.getConfig().getString("config.format.game-to-irc.kick"));
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             return in;
         }
 
         /**
          * Formats the message when a player chats
          * @param player The *real name* of the player
          * @param message The message sent
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Message(String player, String message)
         {
             String in = colour(instance.getConfig().getString("config.format.game-to-irc.message"));
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             in = in.replaceAll("%message%", message);
             return in;
         }
         /**
          * Formats the message used when a player uses /me
          * @param player The *real name* of the player
          * @param action The message in the action
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Action(String player, String action)
         {
             String in = colour(instance.getConfig().getString("config.format.game-to-irc.action"));
             in = in.replaceAll("%prefix%", instance.bridge.getPrefix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%suffix%", instance.bridge.getSuffix(instance.getServer().getPlayer(player)));
             in = in.replaceAll("%nick%", player);
             in = in.replaceAll("%name%", NameFormat(player));
             in = in.replaceAll("%message%", action);
             return in;
         }
     }
     /**
      * Contains all of the formatting for use with events from IRC sent to IRC
      */
     public static class IRCToIRC
     {
         /**
          * Formats the message used when a user joins an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel joined
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Join(String nick, String channel)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-irc.join"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         /**
          * Formats the message used when a user leaves an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel left
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Part(String nick, String channel)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-irc.part"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         /**
          * Formats the message used when a user leaves an IRC server
          * @param nick The nick of the user
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Quit(String nick)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-irc.quit"));
             in = in.replaceAll("%nick%", nick);
             return in;
         }
 
         /**
          * Formats the message used when a user is kicked from an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel left
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Kick(String nick, String channel)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-irc.kick"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         /**
          * Formats the message used when a user messages an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel left
          * @param message The message sent to the channel
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Message(String nick, String channel, String message)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-irc.message"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             in = in.replaceAll("%message%", message);
             return in;
         }
         /**
          * Formats the message used when a user uses /me an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel left
          * @param action The action
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Action(String nick, String channel, String action)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-irc.action"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             in = in.replaceAll("%message%", action);
             return in;
         }
     }
 
     /**
      * Contains all of the formatting for use with events from IRC sent to game
      */
     public static class IRCToGame
     {
         /**
          * Formats the message used when a user enters an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel entered
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Join(String nick, String channel)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-game.join"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
 
         /**
          * Formats the message used when a user leaves an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel left
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Part(String nick, String channel)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-game.part"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
 
         /**
          * Formats the message used when a user quits an IRC server
          * @param nick The nick of the user
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Quit(String nick)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-game.quit"));
             in = in.replaceAll("%nick%", nick);
             return in;
         }
 
         /**
          * Formats the message used when a user is kicked from an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel left
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Kick(String nick, String channel)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-game.kick"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             return in;
         }
         /**
          * Formats the message used when a user messages an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel
          * @param message The message sent
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Message(String nick, String channel, String message)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-game.message"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             in = in.replaceAll("%message%", message);
             return in;
         }
 
         /**
          * Formats the message used when a user uses /me in an IRC channel
          * @param nick The nick of the user
          * @param channel The name of the channel entered
          * @param action The action performed
          * @return A fully formatted string based on the config with variables replaced
          */
         public static String Action(String nick, String channel, String action)
         {
             String in = colour(instance.getConfig().getString("config.format.irc-to-game.action"));
             in = in.replaceAll("%nick%", nick);
             in = in.replaceAll("%chan%", channel);
             in = in.replaceAll("%message%", action);
             return in;
         }
     }
 
 }
