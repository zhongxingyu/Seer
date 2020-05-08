 package com.quiptiq.incraftible.message;
 
 /**
  * Dumping ground for message constants.
  */
 public final class FixedMessage {
     /**
      * Private constructor.
      */
     private FixedMessage() {
     }
 
     /**
      * Version of Incraftible: {@value}
      */
     public static final String VERSION = "0.10";
 
     public static final String PLUGIN_NAME = "Incraftible";
 
     /**
      * {@value}.
      */
     public static final String LOG_PREFIX = "[" + PLUGIN_NAME + "] ";
 
     /**
      * {@value}.
      */
     public static final String LOG_WARN_NO_SPOUT = LOG_PREFIX
             + "Spout not found. Can't intercept crafting events.";
 
     /**
      * {@value}.
      */
     public static final String LOG_WARN_NO_CONFIG = LOG_PREFIX
             + "Config has not been loaded - plugin did not load correctly.";
 
     /**
      * {@value}.
      */
     public static final String LOG_WARN_NO_BUKKIT_CONFIG = LOG_PREFIX
             + "Error loading file using Bukkit config.";
 
     /**
      * {@value}.
      */
     public static final String LOG_WARN_INVALID_KEY = LOG_PREFIX + "Did not understand config key \"%s\"";
 
     /**
      * {@value}.
      */
     public static final String LOG_WARN_INVALID_DISALLOWED_ITEM_ID = LOG_PREFIX + "Invalid disallowed item id %s";
 
     /**
      * {@value}.
      */
     public static final String USAGE = LOG_PREFIX + "Plugin should be placed in the plugins directory of the Minecraft"
             + " server.";
 
     /**
      * {@value}.
      */
     public static final String ENTER_TO_CONTINUE = LOG_PREFIX + "Please press Enter to continue.";
 
     /**
      * @{value}.
      */
     public static final String LOG_ITEM_DISALLOW_LIST = LOG_PREFIX + "Config file loaded, disallowed items are: %s";
 
     /**
      * @{value}.
      */
     public static final String LOG_ITEM_DISALLOW_NONE = LOG_PREFIX + "Config file loaded, no items are disallowed.";
 
     /**
      * @{value}.
      */
     public static final String LOG_ITEM_CRAFT_ATTEMPT = "Prevented player %s from crafting %s";
 
     /**
      * @{value}.
      */
     public static final String LOG_ENABLED = LOG_PREFIX + PLUGIN_NAME + " " + VERSION + " enabled.";
 
     /**
      * @{value}.
      */
     public static final String LOG_DISABLED = LOG_PREFIX + "Plugin disabled";
 
     /**
      * Top-level command used for incraftible subcommands.
      */
     public static final String COMMAND_PREFIX = "ic";
 
     /**
      * @{value}.
      */
     public static final String[] COMMAND_USAGE = {
             PLUGIN_NAME + " " + VERSION + " by Taufiq Hoven",
             "/" + COMMAND_PREFIX + " logperms <loggedInPlayerName>"
     };
 
     /**
      * @{value}.
      */
     public static final String PLAYER_MESSAGE_LOGPERMS_USAGE = "Usage: /" + COMMAND_PREFIX + " logperms <loggedInPlayerName>";
 
     /**
      * @{value}.
      */
     public static final String PLAYER_MESSAGE_COMMAND_FAIL = "Command failed! Possibly an error saving config.";
 
     /**
      * @{value}.
      */
     public static final String PLAYER_MESSAGE_LIST_NONE = "No items are currently disallowed from crafting.";
 }
