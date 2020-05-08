 /*
  * DISASTEROIDS
  * Settings.java
  */
 package disasteroids;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Writes and retrieves the user's settings from a file.
  * @author Phillip Cohen
  */
 public class Settings
 {
     /**
      * First time startup?
      */
     private static boolean inSetup = false;
 
     /**
      * The path to the settings file.
      */
     public static final String SETTINGS_FILE_PATH = "DisasteroidsSettings.props";
 
     /**
      * The default settings.
      */
     private static Properties defaultSettings;
 
     /**
      * The user's settings.
      */
     private static Properties userSettings;
     
     private static String oldHighScorer;
     
     private static double oldHighScore;
 
     /**
      * Loads settings from <code>SETTINGS_FILE_PATH</code>, if it exists.
      * 
      * @return  whether settings were loaded
      */
     public static boolean loadFromStorage()
     {
         try
         {
             // Create the default settings.
            //if ( defaultSettings == null )
             {
                 defaultSettings = new Properties();
                 defaultSettings.put( "musicOn", String.valueOf( true ) );
                 defaultSettings.put( "soundOn", String.valueOf( true ) );
                 defaultSettings.put( "fullscreenMode", String.valueOf( true ) );
                 defaultSettings.put( "qualityRendering", String.valueOf( true ) );
                 defaultSettings.put( "lastConnectionIP", "localhost" );
                 defaultSettings.put( "lastGameMode", "wave" );
                 defaultSettings.put( "highScore", String.valueOf( 2000 ) );
                 defaultSettings.put( "highScorer", "Phillip & Andy" );
 //                defaultSettings.put( "lowScore", String.valueOf( -2000 ) );
 //                defaultSettings.put( "lowScorer", "Matt" );
                 defaultSettings.put( "playerName", "Player" );
                 defaultSettings.put( "playerColor", String.valueOf( Color.red.getRGB() ) );
             }
 
             // Create the user's settings with the defaults as a base.
             userSettings = new Properties( defaultSettings );
 
             // Load the settings file.
             File settingsFile = new File( SETTINGS_FILE_PATH );
 
            boolean bSuccess = true;
             if ( settingsFile.exists() )
                 userSettings.load( new FileInputStream( settingsFile ) );
             else
             {
                 setInSetup( true );
                bSuccess = false;
             }
            
            oldHighScore = getHighScore();
            oldHighScorer = getHighScoreName();
         }
         catch ( IOException ex )
         {
             setInSetup( true );
             return false;
         }
 
         // Success!
         return true;
     }
 
     /**
      * Writes settings to <code>SETTINGS_FILE_PATH</code>.
      * 
      * @return  whether settings were saved
      */
     public static boolean saveToStorage()
     {
         try
         {
             // Update the high score.
             if ( Game.getInstance() != null )
             {
                 Ship highestScorer = Game.getInstance().getObjectManager().getPlayers().peek();
                 for ( Ship s : Game.getInstance().getObjectManager().getPlayers() )
                 {
                     if ( s.getScore() > Settings.getHighScore() )
                     {
                         Settings.setHighScoreName( highestScorer.getName() );
                         Settings.setHighScore( highestScorer.getScore() );
                     }
                 }
             }
 
             // Write the settings file.            
             userSettings.store( new FileOutputStream( Settings.SETTINGS_FILE_PATH ), "You're reading the Disasteroids settings file! (v3)." );
         }
         catch ( IOException e )
         {
             Running.warning( "Failed to save settings.", e );
             return false;
         }
         catch ( RuntimeException t )
         {
             Running.warning( "Failed to save settings.", t );
             throw t;
         }
 
         // Success!
         return true;
     }
 
     /**
      * Returns whether the user wants music to play.
      * @since November 15, 2007
      */
     public static boolean isMusicOn()
     {
         return Boolean.parseBoolean( userSettings.getProperty( "musicOn" ) );
     }
 
     /**
      * Sets whether the user wants music to play.
      * @since November 15, 2007
      */
     public static void setMusicOn( boolean aMusicOn )
     {
         userSettings.put( "musicOn", String.valueOf( aMusicOn ) );
     }
 
     /**
      * Returns whether the user wants sound to play.
      * @since November 15, 2007
      */
     public static boolean isSoundOn()
     {
         return Boolean.parseBoolean( userSettings.getProperty( "soundOn" ) );
     }
 
     /**
      * Sets whether the user wants sound to play.
      * @since November 15, 2007
      */
     public static void setSoundOn( boolean aSoundOn )
     {
         userSettings.put( "soundOn", String.valueOf( aSoundOn ) );
     }
 
     /**
      * Returns whether the user wants a fullscreen game (true) or a windowed one (false).
      * @since December 7, 2007
      */
     public static boolean isUseFullscreen()
     {
         return Boolean.parseBoolean( userSettings.getProperty( "fullscreenMode" ) );
     }
 
     /**
      * Saves whether the user wants a fullscreen game (true) or a windowed one (false).
      * @since December 7, 2007
      */
     public static void setUseFullscreen( boolean aUseFullscreen )
     {
         userSettings.put( "fullscreenMode", String.valueOf( aUseFullscreen ) );
     }
 
     /**
      * Returns the last address the user entered in the "Select Server" dialog box.
      * @since December 9, 2007
      */
     public static String getLastConnectionIP()
     {
         return userSettings.getProperty( "lastConnectionIP" );
     }
 
     /**
      * Saves the last address the user entered in the "Select Server" dialog box.
      * @since December 9, 2007
      */
     public static void setLastConnectionIP( String aLastConnectionIP )
     {
         userSettings.put( "lastConnectionIP", aLastConnectionIP );
     }
 
     /**
      * Saves the last used game mode.
      * @since April 17, 2008
      */
     public static void setLastGameMode( Class aMode )
     {
         if ( aMode == WaveGameplay.class )
             userSettings.put( "lastGameMode", "wave" );
         else
             userSettings.put( "lastGameMode", "linear" );
     }
 
     /**
      * Gets the last used game mode.
      * @since April 17, 2008
      */
     public static Class getLastGameMode()
     {
         if ( userSettings.getProperty( "lastGameMode" ).equalsIgnoreCase( "wave" ) )
             return WaveGameplay.class;
         else
             return LinearGameplay.class;
     }
 
     /**
      * Returns whether the user wants quality rendering. This makes graphics look nicer, but at the expense of speed.
      * @since December 15, 2007
      */
     public static boolean isQualityRendering()
     {
         return Boolean.parseBoolean( userSettings.getProperty( "qualityRendering" ) );
     }
 
     /**
      * Sets whether the user wants quality rendering. This makes graphics look nicer, but at the expense of speed.
      * @since December 15, 2007
      */
     public static void setQualityRendering( boolean aQualityRendering )
     {
         userSettings.put( "qualityRendering", String.valueOf( aQualityRendering ) );
     }
 
     /**
      * Returns the highest score we've seen on this computer.
      * @since December 18, 2007
      */
     public static double getHighScore()
     {
         return Double.parseDouble( userSettings.getProperty( "highScore" ) );
     }
 
     /**
      * Updates the highest score we've seen on this computer.
      * @since December 18, 2007
      */
     public static void setHighScore( double aHighScore )
     {
         userSettings.put( "highScore", String.valueOf( aHighScore ) );
     }
 //
 //    /**
 //     * Returns the lowest score we've seen on this computer.
 //     */
 //    public static double getLowScore()
 //    {
 //        return Double.parseDouble( userSettings.getProperty( "lowScore" ) );
 //    }
 //
 //    /**
 //     * Updates the lowest score we've seen on this computer.
 //     */
 //    public static void setLowScore( double aLowScore )
 //    {
 //        userSettings.put( "lowScore", String.valueOf( aLowScore ) );
 //    }
 
     /**
      * Returns the name of the high scorer.
      * @since December 18, 2007
      */
     public static String getHighScoreName()
     {
         return userSettings.getProperty( "highScorer" );
     }
 
     /**
      * Sets the name of the high scorer.
      * @since December 18, 2007
      */
     public static void setHighScoreName( String aHighScoreName )
     {
         userSettings.put( "highScorer", aHighScoreName );
     }
 
     /**
      * Returns the user's in-game name, or "Player" if that's empty.
      * @since December 20, 2007
      */
     public static String getPlayerName()
     {
         if ( userSettings.getProperty( "playerName" ).length() == 0 )
             return "Player";
         else
             return userSettings.getProperty( "playerName" );
     }
 
     /**
      * Sets the user's in-game name.
      * @since December 20, 2007
      */
     public static void setPlayerName( String aPlayerName )
     {
         userSettings.put( "playerName", aPlayerName );
     }
 
     /**
      * Returns the user's in-game ship color.
      * @since April 9, 2008
      */
     public static Color getPlayerColor()
     {
         return new Color( Integer.parseInt( userSettings.getProperty( "playerColor" ) ) );
     }
 
     /**
      * Sets the user's in-game ship color.
      * @since April 9, 2008
      */
     public static void setPlayerColor( Color aPlayerColor )
     {
         if ( aPlayerColor != null )
             userSettings.put( "playerColor", String.valueOf( aPlayerColor.getRGB() ) );
     }
 
     /**
      * Returns if this is a first-time run, where the user has no personal settings.
      * @since April 9, 2008
      */
     public static boolean isInSetup()
     {
         return inSetup;
     }
 
     /**
      * Sets whether this is a first-time, where the user has no personal settings.
      * @since April 9, 2008
      */
     public static void setInSetup( boolean aInSetup )
     {
         inSetup = aInSetup;
     }
 
     public static double getOldHighScore()
     {
         return oldHighScore;
     }
 
     public static String getOldHighScorer()
     {
         return oldHighScorer;
     }
     
     
 }
