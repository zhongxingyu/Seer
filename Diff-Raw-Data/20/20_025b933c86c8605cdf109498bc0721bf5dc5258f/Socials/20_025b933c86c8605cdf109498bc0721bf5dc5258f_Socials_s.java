 /**
  * @file Socials.java
  * 
  * Copyright (C) 2011 MUDCraft.org
  * All Rights Reserved.
  *
  * @author Geoffrey Davis
  *
  * $Id$
  */
 package org.mudcraft.bukkit.socials;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mudcraft.bukkit.socials.commands.ProfileCommand;
 import org.mudcraft.bukkit.socials.commands.SocialCommand;
 import org.mudcraft.bukkit.socials.user.Gender;
 import org.mudcraft.bukkit.socials.user.SocialUser;
 
 /**
  * The MUDCraft.org Socials plug-in.
  * @author Geoffrey Davis
  */
 public class Socials extends JavaPlugin {
     /**
      * Gets a suitable logger for this class.
      * @return a <code>Logger</code> object
      */
     public static Logger logger() {
         return Logger.getLogger("Minecraft");
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Class<?>> getDatabaseClasses() {
         return Arrays.asList(new Class<?>[] {
             Social.class,
             SocialUser.class
             });
     }
 
     /**
      * Searches for a {@link SocialUser} by player.
      * If the specified {@link Player} does not have a corresponding
      * {@link SocialUser}, one is created. 
      * @param player the {@link Player} for which the {@link SocialUser}
      *      is to be gotten
      * @return the desired {@link SocialUser}
      * @see #loadOrCreateSocialUser(String)
      */
     public SocialUser loadOrCreateSocialUser(Player player) {
         return player != null ? loadOrCreateSocialUser(player.getName()) : null;
     }
     
     /**
      * Searches for a {@link SocialUser} by name.
      * If the {@link Player} indicated by the specified name does not
      * have a corresponding {@link SocialUser}, one is created. 
      * @param playerName the name of the {@link Player} for which the
      *      {@link SocialUser} to be gotten
      * @return the desired {@link SocialUser} or <code>null</code>
     * @see #loadorCreateSocialUser(Player)
      */
     public SocialUser loadOrCreateSocialUser(String playerName) {
         synchronized (lock) {
             // Attempt to load the social user first.
             SocialUser user = loadSocialUser(playerName);
             
             if (user == null && playerName != null &&
                                 playerName.trim().length() != 0) {
                 // Create a new social user.
                 user = new SocialUser();
                 user.setGender(Gender.NONE);
                 user.setPlayerName(playerName);
                 user.setRealGender(Gender.NONE);
                 user.setShowAge(false);
                 user.setShowBirthday(false);
                 user.setShowBirthYear(false);
                 user.setShowEmailAddress(false);
                 user.setShowRealName(false);
                     
                 // Save the social user.
                 saveSocialUser(user);
             }
             return user;
         }
     }
     
     /**
      * Searches for a {@link Social} by name.
      * @param socialName the name of the {@link Social} to be gotten
      * @return the desired {@link Social} or <code>null</code>
      * @see #loadSocials()
      */
     public Social loadSocial(String socialName) {
         synchronized (lock) {
             if (socialName != null && socialName.trim().length() != 0) {
                 // Make certain the name is trimmed and lowercase.
                 socialName = socialName.trim().toLowerCase();
                 
                 // Search for a corresponding social.
                 return getDatabase().find(Social.class).
                     where().
                     ieq("name", socialName).
                     findUnique();
             }
             return null;
         }
     }
     
     /**
      * Gets the defined {@link Social}s.
      * @return an array containing the defined {@link Social}s
      * @see #loadSocial(String)
      */
     public Social[] loadSocials() {
         synchronized (lock) {
             // Searches for any existing socials.
             return getDatabase().find(Social.class).
                 orderBy("name").
                 findList().
                 toArray(new Social[0]);
         }
     }
     
     /**
      * Searches for a {@link SocialUser} by player.
      * @param player the {@link Player} for which the {@link SocialUser}
      *      is to be gotten
      * @return the desired {@link SocialUser} or <code>null</code>
      * @see #loadSocialUser(String)
      * @see #loadSocialUsers()
      */
     public SocialUser loadSocialUser(Player player) {
         return player != null ? loadSocialUser(player.getName()) : null;
     }
     
     /**
      * Searches for a {@link SocialUser} by name.
      * @param playerName the name of the {@link Player} for which the
      *      {@link SocialUser} to be gotten
      * @return the desired {@link SocialUser} or <code>null</code>
      * @see #loadSocialUser(Player)
      * @see #loadSocialUsers()
      */
     public SocialUser loadSocialUser(String playerName) {
         synchronized (lock) {
             if (playerName != null && playerName.trim().length() != 0) {
                 // Make certain the name is trimmed and lowercase.
                 playerName = playerName.trim().toLowerCase();
                 
                 // Search for a corresponding social user.
                 return getDatabase().find(SocialUser.class).
                     where().
                     ieq("playerName", playerName).
                     findUnique();
             }
             return null;
         }
     }
     
     /**
      * Gets the defined {@link SocialUser}s.
      * @return an array containing the defined {@link SocialUser}s
      * @see #loadSocialUser(Player)
      * @see #loadSocialUser(String)
      */
     public SocialUser[] loadSocialUsers() {
         synchronized (lock) {
             // Searches for any existing socials.
             return getDatabase().find(SocialUser.class).
                 orderBy("playerName").
                 findList().
                 toArray(new SocialUser[0]);
         }
     }
     
     /**
      * Creates a new {@link Message}.
      * @return a {@link Message} object
      * @see #newMessage(String)
      */
     public Message newMessage() {
         return newMessage("This is a new message!");
     }
 
     /**
      * Creates a new {@link Message}.
      * @param formatSpecifier the new message's format specifier
      * @return a {@link Message} object
      * @see #newMessage()
      */
     public Message newMessage(String formatSpecifier) {
         return new Message(this).setFormatSpecifier(formatSpecifier);
     }
     
     /**
      * {@inheritDoc}
      */
     public void onDisable() {
         // Obtain a plug-in descriptor for this plug-in.
         final PluginDescriptionFile pdf = getDescription();
         
         if (logger().isLoggable(Level.INFO)) {
             // Write an informational message to the logger.
             logger().info("Disabled " + pdf.getName() + "!");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void onEnable() {
         // Obtain a PluginManager instance to register hooks.
         final PluginManager pm = getServer().getPluginManager();
         
         // Register events we care about with the plug-in manager.
         pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Monitor, this);
         
         // Register commands.
         getCommand("profile").setExecutor(new ProfileCommand(this));
         getCommand("social").setExecutor(new SocialCommand(this));
         
         // Configure database persistence.
         setupDatabase();
         
         // Obtain a plug-in descriptor for this plug-in.
         final PluginDescriptionFile pdf = getDescription();
         
         if (logger().isLoggable(Level.INFO)) {
             // Write an informational message to the logger.
             logger().info("Enabled " + pdf.getName() + "!");
             logger().info(" - Author(s): " + pdf.getAuthors());
             logger().info(" - Version: " + pdf.getVersion());
         }
     }
     
     /**
      * Saves a {@link Social}.
      * @param social the {@link Social} to be saved
      * @see #loadSocial(String)
      * @see #loadSocials()
      */
     public void saveSocial(Social social) {
         synchronized (lock) {
             if (social != null) {
                 // Save the social.
                 getDatabase().save(social);
             }
         }
     }
     
     /**
      * Saves a {@link SocialUser}.
      * @param user the {@link SocialUser} to be saved
      * @see #loadSocialUser(Player)
      * @see #loadSocialUser(String)
      * @see #loadSocialUsers()
      */
     public void saveSocialUser(SocialUser user) {
         synchronized (lock) {
             if (user != null) {
                 // Save the social user.
                 getDatabase().save(user);
             }
         }
     }
     
     /**
      * Configures persistence.
      */
     private void setupDatabase() {
         synchronized (lock) {
             try {
                 // Try to get the number of social rows.
                 getDatabase().find(Social.class).findRowCount();
                 // Try to get the number of social user rows.
                 getDatabase().find(SocialUser.class).findRowCount();
             } catch (PersistenceException ex) {
                 if (logger().isLoggable(Level.INFO)) {
                     // Write an informational message to the logger.
                     logger().info("Installing database for " + getDescription().getFullName() + "!");
                 }
                 installDDL();
             }
         }
     }
     
     /**
      * A lock for synchronizing database access.
      */
     private final Object lock = new Object();
     
     /**
      * Handles player events for the Socials plug-in.
      */
     private final PlayerListener playerListener = new PlayerListener() {
         
         /**
          * {@inheritDoc}
          */
         @Override
         public void onPlayerCommandPreprocess(
                 PlayerCommandPreprocessEvent event) {
             // We have nothing to do if the event was canceled.
             if (event.isCancelled())
                 return;
             
             // We're going to need the message below.
             String message = event.getMessage().trim();
             
             // Determine whether the message begins with a forward slash.
             if (message.startsWith("/")) {
                 // Trim off the forward slash but keep everything else.
                 message = message.substring(1);
             }
             // Split the message into words.
             String[] split = message.split(" ");
             
             // Make certain there is no command by that name.
             if (split.length != 0 &&
                 getServer().getPluginCommand(split[0]) == null) {
                 // Determine the name of the social to be performed.
                 final String socialName = split[0].toLowerCase();
                 
                 // Search for a corresponding social.
                 final Social social = loadSocial(socialName);
                 
                 if (social != null) {
                     if (split.length == 1) {
                         // Just use a zero-length array of strings.
                         split = new String[0];
                     } else {
                         // Create a list containing the split elements.
                         final List<String> list = new ArrayList<String>(
                                 Arrays.asList(split)
                                 );
                         
                         // Remove the first element from the list.
                         list.remove(0);
                         
                         // Convert the list back into an array of strings.
                         split = list.toArray(new String[0]);
                     }
                     // Cause the player to perform the social.
                     social.perform(Socials.this, event.getPlayer(), split);
                     
                     // Set the event as canceled.
                     event.setCancelled(true);
                 }
             }
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void onPlayerLogin(PlayerLoginEvent event) {
             // Create a social user if one doesn't exist.
             loadOrCreateSocialUser(event.getPlayer());
         }
     };
 }
