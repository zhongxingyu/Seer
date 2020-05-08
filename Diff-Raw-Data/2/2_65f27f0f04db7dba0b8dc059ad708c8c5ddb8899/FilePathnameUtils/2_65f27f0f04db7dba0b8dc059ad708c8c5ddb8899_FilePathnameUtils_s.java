 package org.geworkbench.util;
 
 import java.io.File;
 
 /**
  * Part of the API for accessing file system by geworkbench components.
  *
  * Functions below will convert relative path to absolute path that will start
  * with user home directory as a root.
  *
  * All file system related properties that will be handled here. User can change
  * properties but all properties processing will be done here. If user didn't
  * provide file related properties defaults will be used instead.
  *
  * System related file separator is created as a const so user don't have to use
  * system property, should be rarely used, if at all - as path creating activity
  * should be refactored into this API
  *
  * Currently this API is about String manipulations and creating absolute path.
  * When creating java.io.File user should use only File(String pathname)
  * constructor and use this API to create pathname string.
  * Note: relative path shouldn't be used as a pathname(can we enforce it?)
  *
  * It is also recommended not to hardcode file names but use constants or
  * properties instead.
  *
  * @author Oleg Stheynbuk
  * @version $Id: $
  */
 
 public class FilePathnameUtils {
 
 	// Defaults if file related properties are not set
 	private static final String DEFAULT_USER_SETTING_DIR = ".geworkbench";
 	private static final String DEFAULT_TEMP_FILE_DIR = "temp" + File.separator
 			+ "GEAW";
 
	// file relateted properties
 	private static final String USER_SETTING_DIR = System
 					.getProperty("user.setting.directory");
 	private static final String TEMP_FILE_DIR = System.getProperties().getProperty(
 					"temporary.files.directory");
 	private static final String USER_HOME_DIR = System.getProperty("user.home");
 
 	// System dependent file separator
 	public static final String FILE_SEPARATOR = System
 			.getProperty("file.separator");
 
 	// absolute path
 	private static String userSettingDirectoryPath = null;
 	private static String temporaryFilesDirectoryPath = null;
 
 	/**
 	 * will create absolute path starting with home directory as a root for
 	 * temporary files directory if "temporary.files.directory" property is not
 	 * set will use DEFAULT_TEMP_FILE_DIR
 	 *
 	 * @return user settings directory as an absolute path
 	 *
 	 */
 	public static String getTemporaryFilesDirectoryPath() {
 		if (temporaryFilesDirectoryPath == null){
 			String tempFolder = TEMP_FILE_DIR;
 			if (tempFolder == null) {
 				tempFolder = DEFAULT_TEMP_FILE_DIR;
 			}
 
 			// keep temporary files directory under user setting directory
 			temporaryFilesDirectoryPath = getUserSettingDirectoryPath()+ tempFolder  + File.separator;
 		}
 
 		return temporaryFilesDirectoryPath;
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
 		if (userSettingDirectoryPath == null){
 			String userSettingDirectory = USER_SETTING_DIR;
 			if (userSettingDirectory == null) {
 				userSettingDirectory = DEFAULT_USER_SETTING_DIR;
 			}
 
 			// keep user setting directory under user home directory
 			userSettingDirectoryPath = prependHomeDirName(userSettingDirectory);
 		}
 
 		return userSettingDirectoryPath;
 	}
 
 	/**
 	 * will create absolute path starting with home directory as a root from
 	 * relative path doesn't change parameter, use return value.
 	 *
 	 * @param relative  path - file name or directory tree that will go
 	 * 		under user home directory
 	 *
 	 * @return absolute path starting with user home directory as a root
 	 */
 	private static String prependHomeDirName(String name) {
 		String prependName = USER_HOME_DIR + FILE_SEPARATOR + name + FILE_SEPARATOR;
 
 		return prependName;
 	}
 }
