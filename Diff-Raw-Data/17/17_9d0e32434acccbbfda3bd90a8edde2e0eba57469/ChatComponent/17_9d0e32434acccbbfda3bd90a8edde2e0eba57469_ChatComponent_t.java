 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  * Copyright 2012 StarTux
  *
  * This file is part of Winthier.
  *
  * Winthier is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Winthier is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Winthier.  If not, see <http://www.gnu.org/licenses/>.
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 package com.winthier;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.server.ServerCommandEvent;
 
 public class ChatComponent extends AbstractComponent implements Listener {
         private VariableMessage meMessage;
         private VariableMessage senderMessage;
         private VariableMessage recipientMessage;
         private String consoleName;
         private Map<CommandSender, CommandSender> lastPm = new HashMap<CommandSender, CommandSender>();
 
         public ChatComponent(WinthierPlugin plugin) {
                 super(plugin, "chat");
                 getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
         }
 
         @Override
         public void loadConfiguration() {
                 meMessage = new VariableStringFilter().parseMessage(getConfig().getString("me.Format"));
                 senderMessage = new VariableStringFilter().parseMessage(getConfig().getString("msg.SenderFormat"));
                 recipientMessage = new VariableStringFilter().parseMessage(getConfig().getString("msg.RecipientFormat"));
                 consoleName = new ColorStringFilter().replace(getConfig().getString("ConsoleName", "*Console"));
         }
 
         @EventHandler(ignoreCancelled = true)
         public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
                if (onCommand(event.getPlayer(), event.getMessage().substring(1))) {
                        getPlugin().getLogger().info(event.getPlayer().getName() + " issued command: " + event.getMessage().trim());
                         event.setCancelled(true);
                 }
         }
 
         @EventHandler()
         public void onServerCommand(ServerCommandEvent event) {
                 if (onCommand(event.getSender(), event.getCommand())) {
                         event.setCommand("winthier devnull");
                 }
         }
 
         private String getName(CommandSender sender) {
                 if (sender instanceof ConsoleCommandSender) return consoleName;
                 if (sender instanceof Player) return ((Player)sender).getDisplayName();
                 return sender.getName();
         }
 
         private ConfigurableColorStringFilter getColorFilter(CommandSender sender) {
                 ConfigurableColorStringFilter result = new ConfigurableColorStringFilter();
                 if (sender instanceof ConsoleCommandSender) return result;
                 for (ChatColor color : ChatColor.values()) {
                         if (!sender.hasPermission("winthier.chat.color." + color.getChar())) {
                                 result.blacklist(color);
                         }
                 }
                 return result;
         }
 
         protected boolean onCommand(CommandSender sender, String command) {
                 String[] tokens = command.split(" +", 2);
                 if (tokens.length == 0) return false;
                 String cmd = tokens[0];
                 String args = tokens.length == 1 ? "" : tokens[1].trim();
                 if (cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("tell")) {
                         if (!sender.hasPermission("winthier.chat.pm")) {
                                 sender.sendMessage("" + ChatColor.RED + "You don't have permission.");
                                 return true;
                         }
                         tokens = args.split(" +", 2);
                         if (tokens.length != 2) {
                                 sender.sendMessage("" + ChatColor.RED + "Usage: /" + cmd.toLowerCase() + " <player> <message>");
                                 return true;
                         }
                         CommandSender recipient = null;
                         if (tokens[0].equals(consoleName) || tokens[0].equals("!")) {
                                 recipient = getPlugin().getServer().getConsoleSender();
                         } else {
                                 List<Player> recipients = getPlugin().getServer().matchPlayer(tokens[0]);
                                 if (recipients.isEmpty()) {
                                         sender.sendMessage("" + ChatColor.RED + "No players matched query.");
                                         return true;
                                 }
                                 recipient = recipients.get(0);
                         }
                         String message = getColorFilter(sender).replace(tokens[1].trim());
                         senderMessage.setVariable("from", getName(sender));
                         senderMessage.setVariable("to", getName(recipient));
                         senderMessage.setVariable("message", message);
                         senderMessage.sendTo(sender);
                         if (!IgnoreComponent.getInstance().doesIgnore(sender.getName(), recipient.getName())) {
                                 recipientMessage.setVariable("from", getName(sender));
                                 recipientMessage.setVariable("to", getName(recipient));
                                 recipientMessage.setVariable("message", message);
                                 recipientMessage.sendTo(recipient);
                         }
                         lastPm.put(recipient, sender);
                 } else if (cmd.equalsIgnoreCase("reply") || cmd.equalsIgnoreCase("r")) {
                         if (!sender.hasPermission("winthier.cmd.msg")) {
                                 sender.sendMessage("" + ChatColor.RED + "You don't have permission.");
                                 return true;
                         }
                         CommandSender recipient = lastPm.get(sender);
                         if (recipient == null) {
                                 sender.sendMessage("" + ChatColor.RED + "No previous message.");
                                 return true;
                         }
                         if (args.length() == 0) {
                                 sender.sendMessage("" + ChatColor.RED + "Usage: /" + cmd.toLowerCase() + " <message>");
                                 return true;
                         }
                         String message = getColorFilter(sender).replace(args);
                         senderMessage.setVariable("from", getName(sender));
                         senderMessage.setVariable("to", getName(recipient));
                         senderMessage.setVariable("message", message);
                         senderMessage.sendTo(sender);
                         if (!IgnoreComponent.getInstance().doesIgnore(sender.getName(), recipient.getName())) {
                                 recipientMessage.setVariable("from", getName(sender));
                                 recipientMessage.setVariable("to", getName(recipient));
                                 recipientMessage.setVariable("message", message);
                                 recipientMessage.sendTo(recipient);
                         }
                         lastPm.put(recipient, sender);
                 } else if (cmd.equalsIgnoreCase("me")) {
                         if (!sender.hasPermission("winthier.chat.me")) {
                                 sender.sendMessage("" + ChatColor.RED + "You don't have permission.");
                                 return true;
                         }
                         if (args.length() == 0) {
                                 sender.sendMessage("" + ChatColor.RED + "Usage: /" + cmd.toLowerCase() + " <message>");
                                 return true;
                         }
                         meMessage.setVariable("player", getName(sender));
                         meMessage.setVariable("message", getColorFilter(sender).replace(args));
                         IgnoreComponent.getInstance().broadcast(sender, meMessage.toString(), false);
                 } else {
                         return false;
                 }
                 return true;
         }
 
         protected void msgCommand(CommandSender sender, String token, String args) {
                 System.out.println(sender.getName() + " /" + token + " " + args);
         }
 
         protected void meCommand(CommandSender sender, String token, String args) {
                 System.out.println(sender.getName() + " /" + token + " " + args);
         }
 }
