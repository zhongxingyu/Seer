 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package arcaneFantasy.common.lib;
 
 import java.lang.reflect.Field;
 import java.util.*;
 import java.util.logging.*;
 
 /**
  * Handy reference sheet.
  *
  * @author HMPerson1
  */
 public class Reference {
 
     /**
      * This mod's unique id.
      */
     public static final String MOD_ID = "arcaneFantasy";
     /**
      * User-friendly name.
      */
     public static final String MOD_NAME = "Arcane and Fantasy Mod";
     /**
      * Name for logging.
      */
     public static final String LOGGER_NAME = MOD_ID.toUpperCase(Locale.US);
     /**
      * Current version string.
      */
     public static final String VERSION = "0.0.1";
     /**
      * The name of this mod's channel.
      */
     public static final String CHANNEL_NAME = "chan_arcaneFanta";
     /* ^^ Should be:
      * public static final String CHANNEL_NAME = ("chan_" + MOD_ID).substring(0, 15);
      * but annotation params must be compile-time constants. DAMN IT! 
      */
     /**
      * Directory that the sprite sheets are in.
      */
    public static final String SPRITE_SHEET_LOCATION = "/arcaneFantasy/client/art/sprites/";
     /**
      * File name of the icon sprite sheet.
      */
     public static final String ITEM_SPRITE_SHEET = "icons.png";
     /**
      * File name of the block sprite sheet.
      */
     public static final String BLOCK_SPRITE_SHEET = "blocks.png";
 }
