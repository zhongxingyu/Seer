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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class MailComponent extends AbstractComponent implements CommandExecutor, Listener  {
         private Map<String, LinkedList<Mail>> mails = new HashMap<String, LinkedList<Mail>>();
         private static final String filename = "mails.yml";
 
         public MailComponent(WinthierPlugin plugin) {
                super(plugin, "message");
         }
 
         @Override
         public void enable() {
                 getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
                 getPlugin().getCommand("mail").setExecutor(this);
                 getPlugin().getCommand("mailto").setExecutor(this);
                 load();
         }
 
         @Override
         public void disable() {
                 save();
         }
 
         private static final void sendMessage(CommandSender sender, String message) {
                 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
         }
 
         @Override
         public boolean onCommand(CommandSender sender, Command command, String alias, String args[]) {
                 if (alias.equalsIgnoreCase("mail")) {
                         if (args.length == 0) {
                                 LinkedList<Mail> list = getMailsFor(sender.getName());
                                 if (list == null) {
                                         sendMessage(sender, "&cNo mail for you.");
                                         return true;
                                 }
                                 Mail mail = list.removeFirst();
                                 sender.sendMessage("");
                                 sendMessage(sender, "&7Mail from &b" + mail.sender + "&7:");
                                 sender.sendMessage(mail.message);
                                 if (list.isEmpty()) {
                                         sendMessage(sender, "&7No more mail.");
                                 } else if (list.size() == 1) {
                                         sendMessage(sender, "&71 more mail.");
                                 } else {
                                         sendMessage(sender, "&7" + list.size() + " more mails.");
                                 }
                         } else {
                                 return false;
                         }
                 } else if (alias.equalsIgnoreCase("mailto")) {
                         if (args.length > 1) {
                                 String recipient = args[0];
                                 StringBuilder sb = new StringBuilder(args[1]);
                                 for (int i = 2; i < args.length; ++i) sb.append(" ").append(args[i]);
                                 String message = sb.toString();
                                 addMail(new Mail(sender.getName(), recipient, message));
                                 sendMessage(sender, "&3Mail sent to " + recipient);
                                 Player player = getPlugin().getServer().getPlayer(recipient);
                                 if (player != null) {
                                         sendMessage(player, "&7You have mail. Type &b/mail&7.");
                                 }
                         } else {
                                 return false;
                         }
                 }
                 return true;
         }
 
         @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
         public void onPlayerJoin(PlayerJoinEvent event) {
                 List<Mail> list = getMailsFor(event.getPlayer().getName());
                 if (list != null && !list.isEmpty()) {
                         sendMessage(event.getPlayer(), "&7You have mail. Type &b/mail&7.");
                 }
         }
 
         public LinkedList<Mail> getMailsFor(String playerName) {
                 LinkedList<Mail> result = mails.get(playerName.toLowerCase());
                 if (result == null || result.isEmpty()) return null;
                 return result;
         }
 
         public void addMail(Mail mail) {
                 LinkedList<Mail> list = mails.get(mail.recipient.toLowerCase());
                 if (list == null) {
                         list = new LinkedList<Mail>();
                         mails.put(mail.recipient.toLowerCase(), list);
                 }
                 list.add(mail);
         }
 
         @SuppressWarnings("unchecked")
         public void load() {
                 mails.clear();
                 YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(getPlugin().getDataFolder(), filename));
                 long date = System.currentTimeMillis();
                 for (Object o : config.getList("mails", new ArrayList<Map<String, Object>>())) {
                         if (o instanceof Map) {
                                 Map<String, Object> map = (Map<String, Object>)o;
                                 Mail mail = new Mail((String)map.get("sender"), (String)map.get("recipient"),
                                                      (String)map.get("message"), (Long)map.get("created"));
                                 if (TimeUnit.MILLISECONDS.toDays(date - mail.created) > 30) {
                                         // expire
                                 } else {
                                         addMail(mail);
                                 }
                         }
                 }
         }
 
         public void save() {
                 YamlConfiguration config = new YamlConfiguration();
                 List<Map<String, Object>> mailsSection = new ArrayList<Map<String, Object>>();
                 for (List<Mail> list : mails.values()) {
                         for (Mail mail : list) {
                                 Map<String, Object> map = new LinkedHashMap<String, Object>();
                                 map.put("sender", mail.sender);
                                 map.put("recipient", mail.recipient);
                                 map.put("message", mail.message);
                                 map.put("created", mail.created);
                                 mailsSection.add(map);
                         }
                 }
                 config.set("mails", mailsSection);
                 try {
                         config.save(new File(getPlugin().getDataFolder(), filename));
                 } catch (IOException ioe) {
                         getPlugin().getLogger().warning("[Mail] Save failed");
                 }
         }
 }
 
 class Mail {
         public final String sender;
         public final String recipient;
         public final String message;
         public final long created;
         public Mail(String sender, String recipient, String message, long created) {
                 this.sender = sender;
                 this.recipient = recipient;
                 this.message = message;
                 this.created = created;
         }
 
         public Mail(String sender, String recipient, String message) {
                 this(sender, recipient, message, System.currentTimeMillis());
         }
 }
