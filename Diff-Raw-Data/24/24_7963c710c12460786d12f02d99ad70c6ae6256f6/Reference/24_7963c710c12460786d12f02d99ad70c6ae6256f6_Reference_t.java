 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 package arcanefantasy.common.lib;
 
 import java.util.Locale;
 
 
 /**
  * Handy reference sheet.
  *
  * @author  HMPerson1
  */
 public class Reference {
 
     //<editor-fold defaultstate="collapsed" desc="General Mod related constants">
     /* General Mod related constants */
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
     public static final String LOGGER_NAME = "[" + MOD_ID.toUpperCase(Locale.US) + "]";
 
     /**
      * Current version string.
      */
     public static final String VERSION = "0.0.1";
 
     /**
      * The name of this mod's channel.
      */
     public static final String CHANNEL_NAME = "chan_arcaneFanta";
     /* ^^ Should be:
      * public static final String CHANNEL_NAME = ("chan_" + MOD_ID).substring(0,
      * 15); but annotation params must be compile-time constants. DAMN IT!
      */
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Texture related constants">
     /* Texture related constants */
     /**
      * Directory that the sprite sheets are in.
      */
    public static final String SPRITE_SHEET_LOCATION = "/arcanefantasy/art/sprites/";
 
     /**
      * File name of the icon sprite sheet.
      */
     public static final String ITEM_SPRITE_SHEET = "icons.png";
 
     /**
      * File name of the block sprite sheet.
      */
     public static final String BLOCK_SPRITE_SHEET = "blocks.png";
 
     /**
      * Directory that the textures are in.
      */
    public static final String TEXTURE_LOCATION = "/arcanefantasy/art/mobs/";
 
     /**
      * File name of the Rapace texture.
      */
     public static final String RAPACE_TEXTURE = "ModelRapace.png";
 
     /**
      * File name of the Cheetah texture.
      */
     public static final String CHEETAH_TEXTURE = "ModelCheetah.png";
 
     /**
      * File name of the Eko texture.
      */
     public static final String EKO_TEXTURE = "ModelEko.png";
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Configuration related constants">
     /* Configuration related constants */
     // Placeholder :D
     //</editor-fold>
 }
