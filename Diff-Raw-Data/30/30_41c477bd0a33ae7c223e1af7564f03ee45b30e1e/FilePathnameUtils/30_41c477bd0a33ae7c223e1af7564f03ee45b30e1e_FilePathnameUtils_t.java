 package org.geworkbench.util;
 
 import java.io.File;
 
 /**
  * Part of the API for accessing file system by geworkbench components.
  *
  * Functions below will convert relative path to absolute path that will start
  * with user home directory as a root.
  *
  * All file system related properties will be handled here. User can change
  * properties but all properties processing will be done here. If user didn't
  * provide file related properties - defaults will be used instead.
  *
  * All file related properties are read when class is loaded and they can't be
  * changed at run time. To specify geworkbench location for read access
  * "components.dir" property should be used.
  * NOTE: consider changing comments in application.properties file.
  *
  * System related file separator is created as a const so user don't have to use
  * system property or File.separator, this const should be rarely used, if at
  * all - as path creating activity should be refactored into this API.
  *
  * Currently this API is about String manipulations and creating absolute path.
  * When creating java.io.File user should use only File(String pathname)
  * constructor and use this API to create pathname string.
  * NOTE: relative path shouldn't be used as a pathname(can we enforce it?)
  *
  * Each time this API is used, before the String returned to the user, it will be checked
  * if directory structure exist and if it doesn't - directory structure will be created
  * with all subdirectories.
  * Methods checkDirExist(dir) and checkParentDirExist are made public as in many places in
  * geworkbench similar code exist.
  * User can call these methods directly if directory structure is created without using this API,
  * using this API will guarantee that directory structure exist.
  * It will allow creating new directory structure by just changing application.properties file,
  * BTW application.properties file might need some cleaning.
  *
  * Strings representing directories will have FILE_SEPARATOR at the end but Strings representing files
  * will not have it.
  *
  * It is also recommended not to hardcode file names but use constants or
  * properties instead.
  *
  * @author Oleg Stheynbuk
  * @version $Id: $
  */
 
 public class FilePathnameUtils {
 	// System dependent file separator
 	public static final String FILE_SEPARATOR = System
 			.getProperty("file.separator");
 
 	// Defaults if file related properties are not set
 		// directories
 	private static final String DEFAULT_USER_SETTING_DIR = ".geworkbench";
 	private static final String DEFAULT_TEMP_FILE_DIR = "temp" + FILE_SEPARATOR
 			+ "GEAW";
	private static final String USER_SETTING_DIR = System
			.getProperty("user.setting.directory");
 	private static final String DEFAULT_DATA_FILES_DIR = ".";
	
 	private static final String DEFAULT_USERSETTINGS_FILE = DEFAULT_TEMP_FILE_DIR
 			+ FILE_SEPARATOR + "userSettings.config";		
 
 	// file related properties
	
 	private static final String TEMP_FILE_DIR = System
 			.getProperty("temporary.files.directory");
 	private static final String HOUSEKEEPINGNORMALIZERSETTINGS_FILE = System
 			.getProperty("housekeepingnormalizerSettings");
 	private static final String IDEASETTINGS_FILE = System
 	.getProperty("ideaSettings");
 	private static final String DATA_FILES_DIR = System
 			.getProperty("data.files.dir");
 	private static final String USERSETTINGS_FILE = System
 			.getProperty("userSettings");
 
 	private static final String USER_HOME_DIR = System.getProperty("user.home");
 
     private static final String CONF_DIRECTORY = "conf";
 
 	// absolute path
 	private static String userSettingDirectoryPath = null;
 	private static String temporaryFilesDirectoryPath = null;
 	private static String housekeepingnormalizersettingsFilePath = null;
 	private static String ideasettingsFilePath = null;
 	private static String dataFilesDirPath = null;
 	private static String userSettingsFilePath = null;
 
 	/**
 	 * will create absolute path starting with home directory as a root for user settings
 	 * file, if "userSettings" property is not set will use DEFAULT_USERSETTINGS_FILE
 	 *
 	 * @return user settings file as an absolute path
 	 *
 	 */
 	public static String getUserSettingsFilePath() {
 		if (userSettingsFilePath == null) {
 			String tempFolder = USERSETTINGS_FILE;
 			if (tempFolder == null) {
 				tempFolder = DEFAULT_USERSETTINGS_FILE;
 			}
 
 			// keep data files directory under user setting directory
 			userSettingsFilePath = getUserSettingDirectoryPath() + tempFolder
 					+ FILE_SEPARATOR;
 		}
 
 		checkParentDirExist(userSettingsFilePath);
 		return userSettingsFilePath;
 	}
 
 	/**
 	 * will create absolute path starting with the user current working directory
 	 * as it is used to read data files that come with geworkbench distribution.
 	 * If "data.files.dir" property is not set
 	 * will use DEFAULT_DATA_FILES_DIR
 	 *
 	 * @return user settings directory as an absolute path
 	 *
 	 */
 	public static String getDataFilesDirPath() {
 		if (dataFilesDirPath == null) {
 			String tempFolder = DATA_FILES_DIR;
 			if (tempFolder == null) {
 				tempFolder = DEFAULT_DATA_FILES_DIR;
 			}
 
 			// keep data files directory under user's current working directory
 			dataFilesDirPath = System.getProperty("user.dir") + FILE_SEPARATOR + tempFolder
 					+ FILE_SEPARATOR;
 		}
 
 		// probably don't need to check
 		checkDirExist(dataFilesDirPath);
 		return dataFilesDirPath;
 	}
 
 	/**
 	 * will create absolute path starting with home directory as a root for
 	 * temporary files directory if "temporary.files.directory" property is not
 	 * set will use DEFAULT_TEMP_FILE_DIR
 	 *
 	 * @return user settings directory as an absolute path
 	 *
 	 */
 	public static String getTemporaryFilesDirectoryPath() {
 		if (temporaryFilesDirectoryPath == null) {
 			String tempFolder = TEMP_FILE_DIR;
 			if (tempFolder == null) {
 				tempFolder = DEFAULT_TEMP_FILE_DIR;
 			}
 
 			// keep temporary files directory under user setting directory
 			temporaryFilesDirectoryPath = getUserSettingDirectoryPath()
 					+ tempFolder + FILE_SEPARATOR;
 		}
 
 		checkDirExist(temporaryFilesDirectoryPath);
 		return temporaryFilesDirectoryPath;
 	}
 
 	/**
 	 * will create absolute path starting with home directory as a root for
 	 * configuration settings directory
 	 *
 	 * @return configuration settings directory as an absolute path
 	 *
 	 */
 	public static String getGlobalConfigurationSettingsDir() {
 		// keep configuration settings directory under user setting directory
 		String confSettingsDirPath = getUserSettingDirectoryPath()
 				+ CONF_DIRECTORY + FILE_SEPARATOR;
 
 		checkDirExist(confSettingsDirPath);
 
 		return confSettingsDirPath;
 	}
 
 	/**
 	 * will create absolute path starting with home directory as a root for
 	 * configuration settings directory
 	 *
 	 * @param name component name
 	 * @return configuration settings directory as an absolute path
 	 *
 	 */
 	public static String getComponentConfigurationSettingsDir(String name) {
 		// keep configuration settings directory under user setting directory
 		String confSettingsDirPath = getUserSettingDirectoryPath() + name
 				+ FILE_SEPARATOR + CONF_DIRECTORY + FILE_SEPARATOR;
 
 		checkDirExist(confSettingsDirPath);
 
 		return confSettingsDirPath;
 	}
 
 	/**
 	 * will create absolute path starting with home directory as a root for user
 	 * setting directory if "user.setting.directory" property is not set will
 	 * use DEFAULT_USER_SETTING_DIR
 	 *
 	 * @return user settings directory as an absolute path
 	 *
 	 */
 	public static String getUserSettingDirectoryPath() {
 		if (userSettingDirectoryPath == null) {
 			String userSettingDirectory = USER_SETTING_DIR;
 			if (userSettingDirectory == null) {
 				userSettingDirectory = DEFAULT_USER_SETTING_DIR;
 			}
 
 			// keep user setting directory under user home directory
 			userSettingDirectoryPath = prependHomeDirName(userSettingDirectory) + FILE_SEPARATOR;
 		}
 
 		checkDirExist(userSettingDirectoryPath);
 		return userSettingDirectoryPath;
 	}
 
 	/**
 	 * @return housekeepingnormalizersettings file as an absolute path
 	 *
 	 */
	
 	public static String getHousekeepingnormalizerSettingsPath() {
 		if (housekeepingnormalizersettingsFilePath == null) {
			String housekeepingnormalizersettingsFile = USER_SETTING_DIR 
					+ FILE_SEPARATOR + HOUSEKEEPINGNORMALIZERSETTINGS_FILE;	
 			// keep housekeepingnormalizersettings file under user home
 			// directory
 			housekeepingnormalizersettingsFilePath = prependHomeDirName(housekeepingnormalizersettingsFile);
 		}
 
 		checkParentDirExist(housekeepingnormalizersettingsFilePath);
 		return housekeepingnormalizersettingsFilePath;
 
 	}
 
 	public static String getIDEASettingsPath() {
 		if (ideasettingsFilePath == null) {
 			String ideasettingsFile = IDEASETTINGS_FILE;			
 			ideasettingsFilePath = prependHomeDirName(ideasettingsFile);
 		}
 		checkParentDirExist(ideasettingsFilePath);
 		return ideasettingsFilePath;
 	}
 
 	
 	
 	/**
 	 * will create absolute path starting with home directory as a root from
 	 * relative path doesn't change parameter, use return value.
 	 *
 	 * @param relative
 	 *            path - file name or directory tree that will go under user
 	 *            home directory
 	 *
 	 * @return absolute path starting with user home directory as a root
 	 */
 	private static String prependHomeDirName(String name) {
 		String prependName = USER_HOME_DIR + FILE_SEPARATOR + name;
 
 		return prependName;
 	}
 
 	/**
 	 * if directory structure doesn't exist - create
 	 *
 	 * @param file that parent directory will be checked
 	 */
 	public static void checkParentDirExist(String file){
 		File nf = new File(file);
 		String parentDir = nf.getParent();
 		if (parentDir != null){
 			checkDirExist(parentDir);
 		}
 	}
 
 	/**
 	 * if directory structure doesn't exist - create
 	 *
 	 * @param dir to check
 	 */
 	public static void checkDirExist(String dir){
 		File nd = new File(dir);
 		if (!nd.exists()) {
 			nd.mkdirs();
 		}
 
 	}
 }
