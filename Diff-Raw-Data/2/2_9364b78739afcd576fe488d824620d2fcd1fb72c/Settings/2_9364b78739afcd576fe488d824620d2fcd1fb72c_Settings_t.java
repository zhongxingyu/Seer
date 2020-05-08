 /*
  *  Wezzle
  *  Copyright (c) 2007-2009 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.manager;
 
 import java.awt.Color;
 import java.util.EnumSet;
 import java.util.List;
 
 /**
  * Contains all the settings keys and values.
  * 
  * @author cdmckay
  */
 public class Settings 
 {
 
     /** All the keys that may be used. */
     public enum Key
     {                        
         // User values.
                         
         USER_MUSIC(Boolean.class),
         USER_MUSIC_VOLUME(Integer.class),
         USER_SOUND(Boolean.class),
         USER_SOUND_VOLUME(Integer.class),
         USER_HIGHSCORE(Object.class),
         USER_ACHIEVEMENT(List.class),
         USER_ACHIEVEMENT_COMPLETED(List.class),
         USER_DIFFICULTY_DEFAULT(String.class),
         USER_LEVEL_DEFAULT(Integer.class),
         USER_TUTORIAL_DEFAULT(Boolean.class),
         USER_MUSIC_DEFAULT(String.class),
 
         USER_PIECE_PREVIEW_TRADITIONAL(Boolean.class),
         USER_PIECE_PREVIEW_OVERLAY(Boolean.class),
         USER_PIECE_PREVIEW_SHADOW(Boolean.class),
 
         USER_GRAPHICS_FULLSCREEN(Boolean.class),
 
         /** The number of FSAA samples. */
         USER_GRAPHICS_ANTIALIASING_SAMPLES(Integer.class),
 
         /** Whether or not auto-pause is enabled. */
         USER_AUTO_PAUSE(Boolean.class),
         
         // Tutorials.
         
         USER_TUTORIAL_BASIC_RAN(Boolean.class),
         USER_TUTORIAL_ROTATE_RAN(Boolean.class),
         USER_TUTORIAL_ROCKET_RAN(Boolean.class),
         USER_TUTORIAL_GRAVITY_RAN(Boolean.class),
         USER_TUTORIAL_BOMB_RAN(Boolean.class),
         USER_TUTORIAL_STAR_RAN(Boolean.class),
 
         // License values.
 
         USER_SERIAL_NUMBER(String.class),
         USER_LICENSE_KEY(String.class),
 
         // Graphics values.              
         
         /** The number of ticks (frames) per second. */        
         GAME_TICKS_PER_SECOND(Integer.class),
         
         GAME_COLOR_PRIMARY(Color.class),
         GAME_COLOR_SECONDARY(Color.class),
         GAME_COLOR_DISABLED(Color.class),
 
         /** The amount of score to carryover between levels, the top part. */
         GAME_SCORE_CARRYOVER_NUMERATOR(Integer.class),
 
         /** The amount of score to carryover between levels, the bottom part. */
         GAME_SCORE_CARRYOVER_DENOMINATOR(Integer.class),
 
         // Debug values.
 
         /** Whether or not pushing certain keys will add items under the cursor. */
         DEBUG_ENABLE_ITEM_KEYS(Boolean.class),
 
         /** Whether or not to show the clip rectangle.  Only works in JAVA2D mode. */
         DEBUG_SHOW_CLIP_RECT(Boolean.class),
         
         // Refactor values.
         
         REFACTOR_SLOWER_SPEED_X(Integer.class),
         REFACTOR_SLOWER_SPEED_Y(Integer.class),
         REFACTOR_SLOWER_GRAVITY(Integer.class),
 
         REFACTOR_SLOW_SPEED_X(Integer.class),
         REFACTOR_SLOW_SPEED_Y(Integer.class),
         REFACTOR_SLOW_GRAVITY(Integer.class),
 
         REFACTOR_NORMAL_SPEED_X(Integer.class),
         REFACTOR_NORMAL_SPEED_Y(Integer.class),
         REFACTOR_NORMAL_GRAVITY(Integer.class),
 
         REFACTOR_FAST_SPEED_X(Integer.class),
         REFACTOR_FAST_SPEED_Y(Integer.class),
         REFACTOR_FAST_GRAVITY(Integer.class),
 
         REFACTOR_SHIFT_SPEED_X(Integer.class),
         REFACTOR_SHIFT_SPEED_Y(Integer.class),
         REFACTOR_SHIFT_GRAVITY(Integer.class),
         
         // Animation values.
             
         ANIMATION_DROP_ZOOM_OUT_SPEED(Integer.class),
                 
         ANIMATION_JUMP_MOVE_SPEED(Integer.class),
         ANIMATION_JUMP_MOVE_DURATION(Integer.class),
         ANIMATION_JUMP_MOVE_GRAVITY(Integer.class),
         ANIMATION_JUMP_FADE_WAIT(Integer.class),
         ANIMATION_JUMP_FADE_DURATION(Integer.class),
         
         ANIMATION_LEVEL_MOVE_SPEED(Integer.class),
         ANIMATION_LEVEL_MOVE_DURATION(Integer.class),
         ANIMATION_LEVEL_MOVE_GRAVITY(Integer.class),
         ANIMATION_LEVEL_FADE_DURATION(Integer.class),
 
         ANIMATION_LINE_REMOVE_PULSE_MIN_OPACITY(Integer.class),
         ANIMATION_LINE_REMOVE_PULSE_CYCLE_DURATION(Integer.class),
         ANIMATION_LINE_REMOVE_PULSE_CYCLE_COUNT(Integer.class),
         ANIMATION_LINE_REMOVE_ZOOM_SPEED(Integer.class),
         ANIMATION_LINE_REMOVE_FADE_WAIT(Integer.class),
         ANIMATION_LINE_REMOVE_FADE_DURATION(Integer.class),
         
         ANIMATION_ITEM_ACTIVATE_ZOOM_SPEED(Integer.class),
         ANIMATION_ITEM_ACTIVATE_ZOOM_DURATION(Integer.class),
         ANIMATION_ITEM_ACTIVATE_FADE_WAIT(Integer.class),
         ANIMATION_ITEM_ACTIVATE_FADE_DURATION(Integer.class),
         
         ANIMATION_ROCKET_MOVE_DURATION(Integer.class),
         ANIMATION_ROCKET_MOVE_SPEED(Integer.class),
         ANIMATION_ROCKET_MOVE_GRAVITY(Integer.class),
         ANIMATION_ROCKET_FADE_WAIT(Integer.class),
         ANIMATION_ROCKET_FADE_DURATION(Integer.class),
         
         ANIMATION_BOMB_TILE_FADE_WAIT(Integer.class),
         ANIMATION_BOMB_TILE_FADE_DURATION(Integer.class),
         
         ANIMATION_BOMB_SHRAPNEL_FADE_WAIT(Integer.class),
         ANIMATION_BOMB_SHRAPNEL_FADE_DURATION(Integer.class),
         ANIMATION_BOMB_SHRAPNEL_MOVE_WAIT(Integer.class),
         ANIMATION_BOMB_SHRAPNEL_MOVE_DURATION(Integer.class),
         ANIMATION_BOMB_SHRAPNEL_MOVE_SPEED(Integer.class),
         ANIMATION_BOMB_SHRAPNEL_MOVE_GRAVITY(Integer.class),
         ANIMATION_BOMB_SHRAPNEL_MOVE_OMEGA(Double.class),
                
         ANIMATION_BOMB_EXPLODE_ZOOM_SPEED(Integer.class),
         ANIMATION_BOMB_EXPLODE_ZOOM_DURATION(Integer.class),
         ANIMATION_BOMB_EXPLODE_FADE_WAIT(Integer.class),
         ANIMATION_BOMB_EXPLODE_FADE_DURATION(Integer.class),
         
         ANIMATION_EXPLOSION_SPEED(Integer.class),
         ANIMATION_EXPLOSION_OPACITY(Integer.class),
         ANIMATION_EXPLOSION_INITIAL_WIDTH(Integer.class),
         ANIMATION_EXPLOSION_INITIAL_HEIGHT(Integer.class),
         
         ANIMATION_ROWFADE_WAIT(Integer.class),
         ANIMATION_ROWFADE_DURATION(Integer.class),
         
         ANIMATION_SLIDEFADE_FADE_WAIT(Integer.class),
         ANIMATION_SLIDEFADE_FADE_DURATION(Integer.class),
         ANIMATION_SLIDEFADE_MOVE_WAIT(Integer.class),
         ANIMATION_SLIDEFADE_MOVE_DURATION(Integer.class),
         ANIMATION_SLIDEFADE_MOVE_SPEED(Integer.class),
         
         ANIMATION_PIECE_PULSE_SPEED_SLOW(Integer.class),
         ANIMATION_PIECE_PULSE_SPEED_FAST(Integer.class),
                 
         // Achievement values.
         
         ACHIEVEMENT_COLOR_BRONZE(Color.class),
         ACHIEVEMENT_COLOR_SILVER(Color.class),
         ACHIEVEMENT_COLOR_GOLD(Color.class),
         ACHIEVEMENT_COLOR_PLATINUM(Color.class),
         
         // SCT values.
 
         SCT_COLOR_PIECE(Color.class),
         SCT_COLOR_LINE(Color.class),
         SCT_COLOR_ITEM(Color.class),
         SCT_SCORE_MOVE_DURATION(Integer.class),
         SCT_SCORE_MOVE_SPEED(Integer.class),
         SCT_SCORE_MOVE_THETA(Integer.class),
         SCT_SCORE_FADE_WAIT(Integer.class),
         SCT_SCORE_FADE_DURATION(Integer.class),
         SCT_SCORE_FADE_MIN_OPACITY(Integer.class),
         SCT_SCORE_FADE_MAX_OPACITY(Integer.class),
         SCT_LEVELUP_TEXT(String.class),
         SCT_LEVELUP_TEXT_SIZE(Integer.class),
         SCT_LEVELUP_MOVE_DURATION(Integer.class),
         SCT_LEVELUP_MOVE_SPEED(Integer.class),
         SCT_LEVELUP_MOVE_THETA(Integer.class),
                 
         // Menu values.
         
         LOADER_BAR_FADE_WAIT(Integer.class),
         LOADER_BAR_FADE_DURATION(Integer.class),
         MAIN_MENU_STARBURST_OMEGA(Double.class),
         MAIN_MENU_WINDOW_OPACITY(Integer.class),
         MAIN_MENU_WINDOW_SPEED(Integer.class),
         MAIN_MENU_SLIDE_SPEED(Integer.class),
         MAIN_MENU_SLIDE_MIN_X(Integer.class),
         MAIN_MENU_SLIDE_WAIT(Integer.class),
         MAIN_MENU_LOGO_FADE_IN_WAIT(Integer.class),
         MAIN_MENU_LOGO_FADE_IN_DURATION(Integer.class),
         MAIN_MENU_LOGO_FADE_OUT_WAIT(Integer.class),
         MAIN_MENU_LOGO_FADE_OUT_DURATION(Integer.class),
         
         // Item values.
         ITEM_COOLDOWN_STAR(Integer.class);
 
         private Class<?> intendedType;
 
         Key(Class<?> intendedType)
         { this.intendedType = intendedType; }      
 
         public Class<?> getIntendedType()
         { return this.intendedType; }
     }
     
     // Static settings.
     
     /** The upgrade URL. */
     final private static String upgradeUrl = "http://couchware.ca";
     
     /** The cross platform line separator. */
     final private static String lineSeparator = System.getProperty("line.separator");
     
     /** The path to the resources. */
     final private static String resourcesPath = "resources";
 
     /** The path to the fonts. */
     final private static String fontResourcesPath
             = resourcesPath + "/fonts";
     
     /** The path to the sprites. */
     final private static String spriteResourcesPath
             = resourcesPath + "/sprites";
     
     /** The path to the sounds. */
     final private static String soundResourcesPath
             = resourcesPath + "/sounds";
     
     /** The path to the music. */
     final private static String musicResourcesPath
             = resourcesPath + "/music";
     
      /** The path to the XML data. */
     final private static String configResourcesPath
             = resourcesPath + "/config";
         
     /** The name of the settings file. */
     final private static String gameSettingsFileName = "game-settings.xml";     
     
     /** The name of the user settings file. */
     final private static String userSettingsFileName = "user-settings.xml";
     
     /** The name of the achievements file. */
     final private static String achievementsFileName = "achievements.xml";
 
     /** The name of the license file. */
     final private static String licenseFileName = "license.xml";
     
     /** The file path of default game settings file. */
     final private static String defaultGameSettingsFilePath = configResourcesPath
             + "/" + gameSettingsFileName;
     
     /** The file path of default user settings file. */    
     final private static String defaultUserSettingsFilePath = configResourcesPath
             + "/" + userSettingsFileName;
     
     /** The file path of default achievements file. */    
     final private static String defaultAchievementsFilePath = configResourcesPath
             + "/" + achievementsFileName;
     
     /** The path to the user settings file. */
     final private static String externalSettingsPath = 
            System.getProperty("user.home") + "/.couchware/Wezzle";
                     
     /** The file path of external game settings file. */
     final private static String gameSettingsFilePath = externalSettingsPath 
             + "/" + gameSettingsFileName;
     
     /** The file path of external user settings file. */
     final private static String userSettingsFilePath = externalSettingsPath 
             + "/" + userSettingsFileName;
     
     /** The file path of external user settings file. */
     final private static String achievementsFilePath = externalSettingsPath 
             + "/" + achievementsFileName;
 
      /** The file path of external license settings file. */
     final private static String licenseFilePath = externalSettingsPath
             + "/" + licenseFileName;
 
     /** The file path to the disk cache. */
     final private static String cachePath = externalSettingsPath + "/Cache";
     
     /** The path to the log file. */
     final private static String logPath = externalSettingsPath;
     
     /** A set of all the user keys. */
     final private static EnumSet<Key> userKeys = calculateUserKeys();
     
     /** A set of all the user keys. */
     final private static EnumSet<Key> achievementKeys = calculateAchievementKeys();
     
     /**
      * Determines all the user settings keys and returns them.
      * @return
      */
     final private static EnumSet<Key> calculateUserKeys()
     {
         EnumSet<Key> set = EnumSet.noneOf(Key.class);
         
         for (Key key : Key.values())
             if (key.toString().startsWith("USER_") && !key.toString().contains("ACHIEVEMENT"))
                 set.add(key);       
         
         return set;
     }    
             
     /**
      * Determines all the achievement settings keys and returns them.
      * @return
      */
     final private static EnumSet<Key> calculateAchievementKeys()
     {
         EnumSet<Key> set = EnumSet.noneOf(Key.class);                       
         
         for (Key key : Key.values())
             if (key.toString().contains("ACHIEVEMENT"))
                 set.add(key); 
         
         return set;
     }   
 
     public static EnumSet<Key> getUserKeys()
     {
         return userKeys;
     }    
     
     public static EnumSet<Key> getAchievementKeys()
     {
         return achievementKeys;
     } 
     
     public static String getUpgradeUrl()
     {
         return upgradeUrl;
     }   
     
     public static String getDefaultGameSettingsFilePath()
     {
         return defaultGameSettingsFilePath;
     }
     
     public static String getDefaultUserSettingsFilePath()
     {
         return defaultUserSettingsFilePath;
     }
 
     public static String getDefaultAchievementsFilePath()
     {
         return defaultAchievementsFilePath;
     }    
     
     public static String getFontResourcesPath()
     {
         return fontResourcesPath;
     }
 
     public static String getLineSeparator()
     {
         return lineSeparator;
     }
 
     public static String getCachePath()
     {
         return cachePath;
     }
 
     public static String getLogPath()
     {
         return logPath;
     }
 
     public static String getMusicResourcesPath()
     {
         return musicResourcesPath;
     }
 
     public static String getResourcesPath()
     {
         return resourcesPath;
     }
 
     public static String getGameSettingsFileName()
     {
         return gameSettingsFileName;
     }
     
     public static String getUserSettingsFileName()
     {
         return userSettingsFileName;
     }
 
     public static String getAchievementsFileName()
     {
         return achievementsFileName;
     }
 
     public static String getLicenseFileName()
     {
         return licenseFileName;
     }
 
     public static String getTextResourcesPath()
     {
         return configResourcesPath;
     }   
 
     public static String getSoundResourcesPath()
     {
         return soundResourcesPath;
     }
 
     public static String getSpriteResourcesPath()
     {
         return spriteResourcesPath;
     }   
     
     public static String getGameSettingsFilePath()
     {
         return gameSettingsFilePath;
     }
     
     public static String getUserSettingsFilePath()
     {
         return userSettingsFilePath;
     }
 
     public static String getAchievementsFilePath()
     {
         return achievementsFilePath;
     }
 
     public static String getLicenseFilePath()
     {
         return licenseFilePath;
     }
 
     public static String getExternalSettingsPath()
     {
         return externalSettingsPath;
     }        
     
     // Dynamic non-text file settings.
     
     /** Cached milliseconds per tick. */
     private static boolean millisecondsPerTickInitialized = false;
     private static int millisecondsPerTick;        
     
     /**
      * Calculates all the special settings.
      * @param settingsMan
      */
     public static void calculate()
     {
         calculateMillisecondsPerTick();
     }       
     
     /** Recalculates milliseconds per tick. */
     private static void calculateMillisecondsPerTick()
     {         
         millisecondsPerTick = 1000 / SettingsManager.get().getInt(Key.GAME_TICKS_PER_SECOND);                 
     }
     
     /** Returns the number of milliseconds per tick. */
     public static int getMillisecondsPerTick()
     {
         if (millisecondsPerTickInitialized == false)
         {
             millisecondsPerTickInitialized = true;
             calculateMillisecondsPerTick();
         }
         
         return millisecondsPerTick;
     }
 }
