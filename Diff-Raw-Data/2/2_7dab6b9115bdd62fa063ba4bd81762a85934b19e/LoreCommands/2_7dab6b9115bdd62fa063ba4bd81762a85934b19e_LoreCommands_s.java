 /**
  * This file is part of Lore.
  *
  * Copyright (c) 2013 AlmuraDev <http://almuradev.com/>
  * Lore is licensed under the GNU General Public License Version 3.
  *
  * Lore is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Lore is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuradev.lore;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.file.FileAlreadyExistsException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 
 public class LoreCommands implements CommandExecutor {
     private final LorePlugin plugin;
     private final Path BOOKS_PATH;
     private final String CREATE_PERMISSION_KEY = "lore.command.create";
     private final String GIVE_PERMISSION_KEY = "lore.command.give";
     private final String JOIN_PERMISSION_KEY = "lore.command.join";
     private final String LIST_PERMISSION_KEY = "lore.command.list";
     private final String PERMISSION_MESSAGE_KEY;
     private final String REMOVE_PERMISSION_KEY = "lore.command.remove";
     private final String RESPAWN_PERMISSION_KEY = "lore.command.respawn";
     private final String STICKY_PERMISSION_KEY = "lore.command.sticky";
 
     public LoreCommands(LorePlugin plugin) {
         this.plugin = plugin;
         BOOKS_PATH = Paths.get(plugin.getDataFolder() + File.separator + "books");
         PERMISSION_MESSAGE_KEY = plugin.getConfig().getString("messages.permission");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         final Player player = sender instanceof Player ? (Player) sender : null;
 
         if (args.length == 0) {
             return false;
         }
         switch (args[0].toUpperCase()) {
             case "CREATE":
                 if (player == null) {
                     sender.sendMessage("Only players can perform this command.");
                     return true;
                 }
                 if (args.length < 2) {
                     return false;
                 }
                 if (!player.hasPermission(CREATE_PERMISSION_KEY)) {
                     if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                         sender.sendMessage(PERMISSION_MESSAGE_KEY);
                     }
                     return true;
                 }
                 if (!(player.getItemInHand().getItemMeta() instanceof BookMeta)) {
                     sender.sendMessage("You must hold a signed book with this command.");
                 } else {
                     final BookMeta meta = (BookMeta) player.getItemInHand().getItemMeta();
                     try {
                         plugin.getConfiguration().create(args[1], meta);
                         sender.sendMessage(args[1] + " was added to the library.");
                     } catch (FileAlreadyExistsException e) {
                         sender.sendMessage(args[1] + " already exists in the library.");
                     } catch (IOException e) {
                         sender.sendMessage("There was an issue in adding this book to the library, please inform your server administrator.");
                         plugin.getLogger().log(Level.SEVERE, "Unable to create " + args[1] + ".yml", e);
                     }
                 }
                 return true;
 
             case "GIVE":
                 if (args.length < 3) {
                     return false;
                 }
                 if (player != null && !player.hasPermission(GIVE_PERMISSION_KEY)) {
                     if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                         sender.sendMessage(PERMISSION_MESSAGE_KEY);
                     }
                     return true;
                 }
                 Player target = Bukkit.getPlayerExact(args[1]);
                 if (target == null) {
                     sender.sendMessage(args[1] + " does not appear to be online.");
                     return true;
                 }
                 try {
                     final ItemStack item = plugin.getConfiguration().getMap().get(args[2]);
                     if (item == null) {
                         sender.sendMessage(args[2] + " does not exist in the library.");
                         return true;
                     }
                     if (target.getInventory().contains(item)) {
                         sender.sendMessage(target.getName() + " already has a copy of " + args[2] + ".");
                         return true;
                     }
                     target.getInventory().addItem(item);
                 } catch (NullPointerException e) {
                     plugin.getLogger().log(Level.WARNING, "Unable to obtain ItemStack for " + args[2] + ".", e);
                 }
                 return true;
 
             case "JOIN":
                 if (args.length < 3) {
                     return false;
                 }
                 if (player != null && !player.hasPermission(JOIN_PERMISSION_KEY)) {
                     if (PERMISSION_MESSAGE_KEY != null && PERMISSION_MESSAGE_KEY.isEmpty()) {
                         sender.sendMessage(PERMISSION_MESSAGE_KEY);
                     }
                     return true;
                 }
                 try {
                     final YamlConfiguration config = plugin.getConfiguration().getConfig(args[1]);
                     config.set("join", Boolean.parseBoolean(args[2]));
                     config.save(Paths.get(BOOKS_PATH + File.separator + args[1] + ".yml").toFile());
                     sender.sendMessage(args[1] + " has had the join flag set to " + Boolean.parseBoolean(args[2]) + ".");
                 } catch (FileNotFoundException e) {
                     sender.sendMessage(args[1] + " does not exist in the library.");
                 } catch (IOException e) {
                     plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to save " + args[1] + ".yml", e);
                     sender.sendMessage("An error occurred when attempting to save " + args[1] + ".yml, please inform your server administrator.");
                 }
                 return true;
 
             case "LIST":
                if (player != null && !player.hasPermission(JOIN_PERMISSION_KEY)) {
                     if (PERMISSION_MESSAGE_KEY != null && PERMISSION_MESSAGE_KEY.isEmpty()) {
                         sender.sendMessage(PERMISSION_MESSAGE_KEY);
                     }
                     return true;
                 }
                 sender.sendMessage("Registered books of lore: " + plugin.getConfiguration().getMap().keySet().toString().replace("[", "").replace("]", "").trim());
                 return true;
 
             case "REMOVE":
                 if (args.length < 2) {
                     return false;
                 }
                 if (player != null && !player.hasPermission(REMOVE_PERMISSION_KEY)) {
                     if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                         sender.sendMessage(PERMISSION_MESSAGE_KEY);
                     }
                     return true;
                 }
                 try {
                     plugin.getConfiguration().delete(args[1]);
                 } catch (FileNotFoundException e) {
                     sender.sendMessage(args[1] + " does not exist in the library.");
                 } catch (IOException e) {
                     plugin.getLogger().log(Level.SEVERE, "Unable to delete " + args[1] + ".", e);
                 }
                 return true;
 
             case "RESPAWN":
                 if (args.length < 3) {
                     return false;
                 }
                 if (player != null && !player.hasPermission(RESPAWN_PERMISSION_KEY)) {
                     if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                         sender.sendMessage(PERMISSION_MESSAGE_KEY);
                     }
                     return true;
                 }
                 try {
                     final YamlConfiguration config = plugin.getConfiguration().getConfig(args[1]);
                     config.set("respawn", Boolean.parseBoolean(args[2]));
                     config.save(Paths.get(BOOKS_PATH + File.separator + args[1] + ".yml").toFile());
                     sender.sendMessage(args[1] + " has had the respawn flag set to " + Boolean.parseBoolean(args[2]) + ".");
                 } catch (FileNotFoundException e) {
                     sender.sendMessage(args[1] + " does not exist in the library.");
                 } catch (IOException e) {
                     plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to save " + args[1] + ".yml", e);
                     sender.sendMessage("An error occurred when attempting to save " + args[1] + ".yml, please inform your server administrator.");
                 }
                 return true;
 
             case "STICKY":
                 if (args.length < 3) {
                     return false;
                 }
                 if (player != null && !player.hasPermission(STICKY_PERMISSION_KEY)) {
                     if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                         sender.sendMessage(PERMISSION_MESSAGE_KEY);
                     }
                     return true;
                 }
                 try {
                     final YamlConfiguration config = plugin.getConfiguration().getConfig(args[1]);
                     config.set("sticky", Boolean.parseBoolean(args[2]));
                     config.save(Paths.get(BOOKS_PATH + File.separator + args[1] + ".yml").toFile());
                     sender.sendMessage(args[1] + " has had the sticky flag set to " + Boolean.parseBoolean(args[2]) + ".");
                 } catch (FileNotFoundException e) {
                     sender.sendMessage(args[1] + " does not exist in the library.");
                 } catch (IOException e) {
                     plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to save " + args[1] + ".yml", e);
                     sender.sendMessage("An error occurred when attempting to save " + args[1] + ".yml, please inform your server administrator.");
                 }
                 return true;
         }
         return false;
     }
 }
