 package org.sammelbox.controller.filesystem;
 
 import java.io.File;
 
 public class FileSystemLocations {
 	public static final String DATABASE_NAME					= "sammelbox.db";
 	public static final String DATABASE_TO_RESTORE_NAME			= "sammelbox.restore.db";
 
 	public static final String TEMP_DIR 						= System.getProperty("java.io.tmpdir");
 	public static final String USER_HOME 						= System.getProperty("user.home");
 	public static final String DEFAULT_SAMMELBOX_HOME			= System.getProperty("user.home") + File.separatorChar + ".sammelbox";
 	public static final String DEFAULT_SAMMELBOX_TEST_HOME		= System.getProperty("user.home") + File.separatorChar + ".sammelbox.tests";
 	
 	/** The path to the currently active home directory */
 	private static String activeHomeDir = null;
 	
 	/** Sets the path to the currently active home directory 
 	 * This method must be called during initialization of the program. */
 	public static void setActiveHomeDir(String activeHomeDir) {
 		FileSystemLocations.activeHomeDir = activeHomeDir;
 	}
 	
 	public static String getActiveHomeDir() 		{ return activeHomeDir; }
 	public static String getAppDataDir() 			{ return activeHomeDir + File.separatorChar + "app-data"; }
 	public static String getThumbnailsDir() 		{ return activeHomeDir + File.separatorChar + "thumbnails"; }
 	public static String getBackupDir() 			{ return activeHomeDir + File.separatorChar + "backups"; }
 	public static String getAlbumPicturesDir()		{ return activeHomeDir + File.separatorChar + "album-pictures"; }
 	public static String getDatabaseFile() 			{ return activeHomeDir + File.separatorChar + DATABASE_NAME; }
 	public static String getDatabaseRestoreFile() 	{ return activeHomeDir + File.separatorChar + DATABASE_TO_RESTORE_NAME; }
 	
 	public static String getPlaceholderPNG() 		{ return getAppDataDir() + File.separatorChar + "placeholder.png"; }
 	public static String getPlaceholder2PNG() 		{ return getAppDataDir() + File.separatorChar + "placeholder2.png"; }
 	public static String getPlaceholder3PNG() 		{ return getAppDataDir() + File.separatorChar + "placeholder3.png"; }
 	public static String getZeroStarsPNG() 			{ return getAppDataDir() + File.separatorChar + "zerostars.png"; }
 	public static String getOneStarPNG() 			{ return getAppDataDir() + File.separatorChar + "onestar.png"; }
 	public static String getTwoStarsPNG() 			{ return getAppDataDir() + File.separatorChar + "twostars.png"; }
 	public static String getThreeStarsPNG() 		{ return getAppDataDir() + File.separatorChar + "threestars.png"; }
 	public static String getFourStarsPNG() 			{ return getAppDataDir() + File.separatorChar + "fourstars.png"; }
 	public static String getFiveStarsPNG() 			{ return getAppDataDir() + File.separatorChar + "fivestars.png"; }
 	public static String getLogoPNG() 				{ return getAppDataDir() + File.separatorChar + "logo.png"; }
 	public static String getLogoSmallPNG() 			{ return getAppDataDir() + File.separatorChar + "logo-small.png"; }
 	public static String getWelcomeXML() 			{ return getAppDataDir() + File.separatorChar + "welcome.xml"; }
 	public static String getViewsXML() 				{ return getAppDataDir() + File.separatorChar + "views.xml"; }
 	public static String getAlbumsXML() 			{ return getAppDataDir() + File.separatorChar + "albums.xml"; }
 	public static String getSettingsXML()			{ return getAppDataDir() + File.separatorChar + "settings.xml"; }
 	public static String getLockFile() 				{ return getAppDataDir() + File.separatorChar + ".lock"; }
 	public static String getEffectsJS() 			{ return getAppDataDir() + File.separatorChar + "effects.js"; }
	public static String getStyleCSS() 				{ return getAppDataDir() + File.separatorChar + "style.css"; }
 }
