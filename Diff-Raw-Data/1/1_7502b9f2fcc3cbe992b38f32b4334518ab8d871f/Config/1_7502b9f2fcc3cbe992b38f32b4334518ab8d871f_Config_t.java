 package org.publicmain.common;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileLock;
 import java.util.Properties;
 
 import org.publicmain.sql.DatabaseEngine;
 
 /**
  * Diese Klasse enth�lt wichtige Konfigurationsdaten zur Anwendung.
  * 
  * @author ATRM
  * 
  */
 
 public class Config {
 	public static Color BLUE = new Color(25,169,241);
 	public static Color BLUE2 = new Color(9,112,164);
 	public static Color BLUE3 = new Color(5,64,94);
 	public static Color ORANGE = new Color(255,133,18);
 	public static Color YELLOW = new Color(229,195,0);
 	public static Color GREEN= new Color(51,144,124);
 	public static Color GREEN2= new Color(123,195,165);
 	
 
 	private static final String APPNAME 			=   (System.getProperty("appname")==null)?"publicMAIN":System.getProperty("appname");
 	private static final int CURRENTVERSION			=	5;
 	private static final int MINVERSION				=	5;
 	private static final String APPDATA=System.getenv("APPDATA")+File.separator+"publicMAIN"+File.separator;
 	private static final String JARLOCATION=Config.class.getProtectionDomain().getCodeSource().getLocation().getFile();
 	private static final String	lock_file_name		= 	APPNAME+".loc";
 	private static final String	system_conf_name	= 	APPNAME+"_sys.cfg";
 	private static final String	user_conf_name		= 	APPNAME+".cfg"; 
 	private static File loc_file					=	new File(APPDATA,lock_file_name);
 	private static File system_conf					=	new File(new File(JARLOCATION).getParentFile(),system_conf_name);
 	private static File user_conf					=	new File(APPDATA,user_conf_name);
 	private static Config me;
 	private static DatabaseEngine de;
 	private ConfigData settings;
 
 	/**
 	 * Getter welcher die aktuelle Konfiguration zur�ckliefert.
 	 * 
 	 * @return
 	 */
 	public static synchronized ConfigData getConfig() {
 		if (me == null) {
 			me = new Config();
 		}
 		return me.settings;
 	}
 	public static synchronized ConfigData setConfig(ConfigData newConfig) {
 		if (me == null) {
 			me = new Config();
 		}
 		return me.settings=newConfig;
 	}
 
 	/**
 	 * Schreibt die Konfiguration auf die Festplatte.
 	 */
 	@SuppressWarnings("static-access")
 	public static synchronized void write() {
 		if (me == null) {
 			me = new Config();
 		}
 		me.getConfig().setCurrentVersion(CURRENTVERSION);
 		if (de != null) {
 			de.writeConfig();
 		}
 		me.savetoDisk();
 	}
 
 	/**
 	 * System-Konfiguration schreiben.
 	 * 
 	 * @return
 	 */
 	public static synchronized boolean writeSystemConfiguration() {
 		try (FileOutputStream fos = new FileOutputStream(system_conf)) {
 			getSourceSettings().store(fos, "publicMAIN - SYSTEM - SETTINGS");
 			LogEngine.log(Config.class,
 					"System configurations file written to " + system_conf,
 					LogEngine.INFO);
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			LogEngine.log(Config.class, "Could not write system settings: "
 					+ system_conf + " reason : " + e.getMessage(),
 					LogEngine.WARNING);
 			return false;
 		}
 	}
 
 	/**
 	 * Melde die Datenbank f�r Schreibvorg�nge an der Konfiguration an.
 	 * 
 	 * @param databaseengine
 	 */
 	public static void registerDatabaseEngine(DatabaseEngine databaseengine) {
 		de = databaseengine;
 	}
 
 	/**
 	 * Method tries to Lock a file <code>pm.loc</code> in Users
 	 * <code>APPDATA\publicMAIN</code> folder. And returns result as boolen. It
 	 * also adds a shutdown hook to the VM to remove Lock from File if Program
 	 * exits.
 	 * 
 	 * @return <code>true</code> if File could be locked <code>false</code> if
 	 *         File has already been locked
 	 */
 	public static boolean getLock() {
 		try {
 			if (!loc_file.getParentFile().exists()) {
 				loc_file.getParentFile().mkdirs();
 			}
 			final RandomAccessFile randomAccessFile = new RandomAccessFile(
 					loc_file, "rw");
 			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
 			if (fileLock != null) {
 				Runtime.getRuntime().addShutdownHook(new Thread() {
 					public void run() {
 						try {
 							fileLock.release();
 							randomAccessFile.close();
 							loc_file.delete();
 						} catch (Exception e) {
 							LogEngine.log("ShutdownHook",
 									"Unable to remove lock file: " + loc_file,
 									LogEngine.ERROR);
 						}
 					}
 				});
 				return true;
 			}
 		} catch (Exception e) {
 			LogEngine.log("Config", "Unable to create and/or lock file: "
 					+ loc_file, LogEngine.ERROR);
 		}
 		return false;
 	}
 
 	/**
 	 * Standart-Konstruktor f�r die Config-Klasse.
 	 */
 	private Config() {
 		me = this;
 		settings = new ConfigData(getSourceSettings());
 		LogEngine.log(
 				this,
 				"default settings loaded from source ["
 						+ ((system_conf.exists()) ? "S" : "0") + "|"
 						+ ((user_conf.exists()) ? "U" : "0") + "]",
 						LogEngine.INFO);
 		// Versuche die System-Einstellungen vom JAR zu �berladen
 		if (system_conf.canRead()) {
 			try (FileInputStream in = new FileInputStream(system_conf)) {
 				ConfigData system = new ConfigData(settings);
 				system.load(in);
 				LogEngine.log(this, "system settings loaded from "
 						+ system_conf, LogEngine.INFO);
 				this.settings = system;
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				LogEngine.log(this, "error while loading system settings from "
 						+ system_conf, LogEngine.ERROR);
 			}
 		}
 		ConfigData user = new ConfigData(settings);
 		// Versuche die Benutzer-Einstellungen aus AppData zu �berladen
 		if (user_conf.canRead()) {
 			try (FileInputStream in = new FileInputStream(user_conf)) {
 				user.load(in);
 				if (user.getCurrentVersion() < MINVERSION) {
 					settings.setUserID(user.getUserID());
 					settings.setAlias(user.getAlias());
 					LogEngine.log(this,
 							"user settings outdated only userid and alias will be used from "
 									+ user_conf, LogEngine.INFO);
 				} else {
 					settings = user;
 				}
 				LogEngine.log(this, "user settings loaded from " + user_conf,
 						LogEngine.INFO);
 			} catch (IOException e) {
 				LogEngine.log(this, "default config could not be read. reason:"
 						+ e.getMessage(), LogEngine.WARNING);
 			}
 		}
		LogEngine.setVerbosity(settings.getLogVerbosity());
 	}
 
 	/**
 	 * Die Methode liefert die Standart-Einstellungen der Anwendung
 	 * 
 	 * @return
 	 */
 	private static ConfigData getSourceSettings() {
 		ConfigData tmp = new ConfigData();
 		tmp.setCurrentVersion(CURRENTVERSION);
 		// Netzwerk-Parameter
 		tmp.setMCGroup("230.223.223.223");
 		tmp.setMCPort(6789);
 		tmp.setMCTTL(10);
 		tmp.setDiscoverTimeout(200);
 		tmp.setRootClaimTimeout(200);
 		tmp.setMaxConnections(3);
 		tmp.setTreeBuildTime(1000);
 		tmp.setPingInterval(30000);
 		tmp.setPingEnabled(false);
 		// Dateitransfer-Einstellungen
 		tmp.setMaxFileSize(5000000);
 		tmp.setFileTransferTimeout(120000);
 		tmp.setFileTransferInfoInterval(30000);
 		tmp.setDisableFileTransfer(false);
 		// Standart-Einstellungen der Anwendung
 		tmp.setLogVerbosity(4);
 		tmp.setMaxAliasLength(19);
 		tmp.setMaxGroupLength(19);
 		tmp.setMaxEingabefeldLength(200);
 		tmp.setFontFamily("Arial");
 		tmp.setFontSize(3);
 		tmp.setNamePattern("((([-_]?)([a-zA-Z0-9öäüÖÄÜßéá♥])+))+([-_])?");
 		tmp.setNotifyGroup(false);
 		tmp.setNotifyPrivate(false);
 		// Lokale mySQL Datenbank-Einstellungen
 		tmp.setLocalDBVersion(0);
 		tmp.setLocalDBDatabasename("db_publicMain");
 		tmp.setLocalDBPort("3306");
 		tmp.setLocalDBUser("publicMain");
 		tmp.setLocalDBPw("publicMain");
 		// daten fuer externen DB-Backup-Server
 		tmp.setBackupDBDatabasename("db_publicMain_backup");
 		tmp.setBackupDBPort("3306");
 		tmp.setBackupDBUser("backupPublicMain");
 		tmp.setBackupDBPw("backupPublicMain");
 		return tmp;
 	}
 
 	/**
 	 * Einstellungen in einem Thread als Datei auf die Festplatte speichern.
 	 */
 	private void savetoDisk() {
 		Runnable target = new Runnable() {
 			public void run() {
 				try (final FileOutputStream fos = new FileOutputStream(
 						user_conf)) {
 					settings.store(fos, "publicMAIN - USER - SETTINGS");
 					LogEngine.log(Config.this, "User settings written to "
 							+ user_conf, LogEngine.WARNING);
 				} catch (IOException e1) {
 					LogEngine.log(Config.this,
 							"Could not write user settings: " + user_conf
 									+ " reason : " + e1.getMessage(),
 							LogEngine.WARNING);
 				}
 			}
 		};
 		new Thread(target).start();
 	}
 	
 	/**
 	 * TODO: Kommentar!
 	 * 
 	 * @param tmp
 	 */
 	public static void importConfig(Properties tmp) {
 		ConfigData imported = new ConfigData(getConfig());
 		for (Object key : tmp.keySet()) {
 			if (tmp.get(key) != null) {
 				imported.put(key, tmp.get(key));
 			}
 		}
 		setConfig(imported);
 	}
 
 	/**
 	 * TODO: Kommentar!
 	 * 
 	 * @return
 	 */
 	public static Properties getNonDefault() {
 		Properties rueck = new Properties();
 		ConfigData defaults = getSourceSettings();
 		ConfigData current = getConfig();
 		for (Object key : current.keySet()) {
 			if (!current.get(key).equals(defaults.get(key)))
 				rueck.put(key, current.get(key));
 		}
 		return rueck;
 	}
 }
