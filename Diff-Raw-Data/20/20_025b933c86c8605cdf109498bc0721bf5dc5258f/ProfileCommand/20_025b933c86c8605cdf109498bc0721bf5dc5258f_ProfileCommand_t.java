 /**
  * @file ProfileCommand.java
  * 
  * Copyright (C) 2011 MUDCraft.org
  * All Rights Reserved.
  *
  * @author Geoffrey Davis
  *
  * $Id$
  */
 package org.mudcraft.bukkit.socials.commands;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.mudcraft.bukkit.socials.Socials;
 import org.mudcraft.bukkit.socials.user.SocialUser;
 
 /**
  * The /profile command.
  * @author Geoffrey Davis
  */
 public class ProfileCommand implements CommandExecutor {
    /**
      * Constructs a new {@link ProfileCommand} instance.
      * @param plugin the {@link Socials} object
      */
     public ProfileCommand(Socials plugin) {
         if (plugin == null) {
             // Throw an exception to indicate an error.
             throw new IllegalArgumentException("plugin is null");
         }
         this.plugin = plugin;
     }
     
     /**
      * Displays a player's profile.
      * @param sender the {@link CommandSender} object
     * @param target the {@link Player} whose profile is to be displayed
      * @return <code>true</code> if the operation was successful
      */
     protected boolean displayProfile(CommandSender sender, Player target) {
         // Get the social user for the specified player.
         final SocialUser user = getPlugin().loadOrCreateSocialUser(target);
         
         sender.sendMessage(ChatColor.DARK_GREEN + "Name: " + ChatColor.GREEN + target.getName());
         sender.sendMessage(ChatColor.DARK_GREEN + "Display Name: " + ChatColor.GREEN + target.getDisplayName());
         if (sender.equals(target) || user.getShowRealName()) {
             sender.sendMessage(ChatColor.DARK_GREEN + "Real Name: " + ChatColor.GREEN + user.getRealName());
         }
         if (sender.equals(target) || user.getShowEmailAddress()) {
             sender.sendMessage(ChatColor.DARK_GREEN + "Email Address: " + ChatColor.GREEN + user.getEmailAddress());
         }
         sender.sendMessage(ChatColor.DARK_GREEN + "Gender: " + ChatColor.GREEN + user.getGender());
         sender.sendMessage(ChatColor.DARK_GREEN + "Real Gender: " + ChatColor.GREEN + user.getRealGender());
         if (user.getBirthDate() == null || !user.getShowBirthday()) {
             sender.sendMessage(ChatColor.DARK_GREEN + "Birthday: " + ChatColor.GREEN + "Not Disclosed");
         } else {
            // Display the player's birthday.
         }
         return true;
     }
     
     /**
      * Gets the {@link Socials} object.
      * @return the {@link Socials} object
      */
     public Socials getPlugin() {
         return plugin;
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label,
             String[] split) {
         // Indicates whether the command was processed.
         boolean retcode = false;
         
         // The player whose profile to display.
         Player target = null;
         
         if (split.length == 0) {
             if (sender instanceof Player) {
                 // Use the command sender instead.
                 target = (Player) sender;
             } else {
                sender.sendMessage(ChatColor.RED + "You can't use this command anonymously!");
                 retcode = true;
             }
         } else if (split.length == 1) {
             // Attempt to match the player by name.
             List<Player> matches = sender.getServer().matchPlayer(split[0]);
             
             if (matches.size() != 0) {
                 // Use the first player we matched.
                 target = matches.get(0);
             }
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Can't find a player named " + split[0] + "!");
                retcode = true;
            }
         }
         if (target != null) {
             // Display the target's profile to the command sender.
             retcode = displayProfile(sender, target);
         }
         return retcode;
     }
     
     /**
      * The {@link Socials} object.
      * @see {@literal #getPlugin()}
      */
     private final Socials plugin;
 }
