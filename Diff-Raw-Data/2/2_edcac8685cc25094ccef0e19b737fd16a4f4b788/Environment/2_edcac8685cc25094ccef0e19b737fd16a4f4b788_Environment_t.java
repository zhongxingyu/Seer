 package org.vpac.grisu.settings;
 
 import java.io.File;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * This class manages the location/values of some required files/environment
  * variables.
  * 
  * @author Markus Binsteiner
  * 
  */
 public final class Environment {
 
 	private static final String GRISU_DEFAULT_DIRECTORY = System
 			.getProperty("user.home") + File.separator + ".grisu.beta";
 	private static final String GRISU_SYSTEM_WIDE_CONFIG_DIR = "/etc/grisu";
 	private static final String GRISU_SYSTEM_WIDE_VAR_DIR = "/var/lib/grisu/";
 	private static final String GRISU_CLIENT_DIR = System
			.getProperty("user.home") + File.separator + ".grisu.beta";
 
 	private static String USER_SET_GRISU_DIRECTORY = null;
 
 	private static boolean grisuDirectoryAccessed = false;
 
 	private static String GLOBUS_HOME;
 
 	private static File GRISU_DIRECTORY;
 
 	public static String getAvailableTemplatesDirectory() {
 		return getGrisuDirectory() + File.separator + "templates_available";
 	}
 
 	public static String getAxisClientConfig() {
 		return getGlobusHome() + File.separator + "client-config.wsdd";
 	}
 
 	public static String getCacheDirName() {
 		return "cache";
 	}
 
 	public static String getGlobusHome() {
 
 		if (StringUtils.isBlank(GLOBUS_HOME)) {
 
 			GLOBUS_HOME = getVarGrisuDirectory() + File.separator + "globus";
 
 		}
 		return GLOBUS_HOME;
 	}
 
 	public static File getGrisuClientDirectory() {
 
 		if (getGrisuDirectory().equals(new File(GRISU_SYSTEM_WIDE_CONFIG_DIR))) {
 			final File clientDir = new File(GRISU_CLIENT_DIR);
 			if (!clientDir.exists()) {
 				if (!clientDir.mkdirs()) {
 					throw new RuntimeException(
 							"Could not create grisu client settings directory "
 									+ clientDir.toString()
 									+ ". Please adjust permissions.");
 				}
 
 			}
 			if (!clientDir.canWrite()) {
 				throw new RuntimeException("Can't write to directory "
 						+ clientDir.toString() + ". Please adjust permissions.");
 			}
 			return clientDir;
 		} else {
 			return getGrisuDirectory();
 		}
 
 	}
 
 	/**
 	 * This one returns the location where grisu specific config/cache files are
 	 * stored. If it does not exist it gets created.
 	 * 
 	 * @return the location of grisu specific config/cache files
 	 */
 	public static File getGrisuDirectory() {
 
 		grisuDirectoryAccessed = true;
 
 		if (GRISU_DIRECTORY == null) {
 
 			File grisuDir = null;
 			if (StringUtils.isNotBlank(System.getProperty("grisu.home"))) {
 				grisuDir = new File(System.getProperty("grisu.home"));
 				GRISU_DIRECTORY = grisuDir;
 			} else if (StringUtils.isNotBlank(USER_SET_GRISU_DIRECTORY)) {
 				// first, check whether user specified his own directory
 				grisuDir = new File(USER_SET_GRISU_DIRECTORY);
 				GRISU_DIRECTORY = grisuDir;
 			} else {
 				grisuDir = new File(GRISU_SYSTEM_WIDE_CONFIG_DIR);
 				// now try whether a .grisu directory exists in the users home
 				// dir
 				// if not, check "/etc/grisu"
 				if (grisuDir.exists()) {
 					GRISU_DIRECTORY = grisuDir;
 				} else {
 					grisuDir = new File(GRISU_DEFAULT_DIRECTORY);
 
 					if (grisuDir.exists()) {
 						GRISU_DIRECTORY = grisuDir;
 					} else {
 						// create the default .grisu dir in users home
 						grisuDir.mkdirs();
 						GRISU_DIRECTORY = grisuDir;
 					}
 				}
 			}
 		}
 		return GRISU_DIRECTORY;
 	}
 
 	/**
 	 * The location where the remote filesystems are cached locally.
 	 * 
 	 * @return the root of the local cache
 	 */
 	public static File getGrisuLocalCacheRoot() {
 		final File root = new File(getGrisuClientDirectory(), getCacheDirName());
 		if (!root.exists()) {
 			if (!root.mkdirs()) {
 				if (!root.exists()) {
 					throw new RuntimeException(
 							"Could not create local cache root directory: "
 									+ root.getAbsolutePath()
 									+ ". Please check the permissions.");
 				}
 			}
 		}
 		return root;
 	}
 
 	public static File getGrisuPluginDirectory() {
 		final File dir = new File(getGrisuClientDirectory(), "plugins");
 
 		if (!dir.exists()) {
 			dir.mkdirs();
 		}
 		return dir;
 	}
 
 	/**
 	 * For some jobs/applications it is useful to cache output files locally so
 	 * they don't have to be transferred over and over again.
 	 * 
 	 * @return the location of the local directory where all job output files
 	 *         are chached (in subdirectories named after the jobname)
 	 */
 	public static File getLocalJobCacheDirectory() {
 
 		final File dir = new File(getGrisuClientDirectory(), "jobs");
 		dir.mkdirs();
 		return dir;
 	}
 
 	public static String getTemplateDirectory() {
 		return getGrisuClientDirectory() + File.separator + "templates";
 	}
 
 	public static File getVarGrisuDirectory() {
 
 		if (getGrisuDirectory().equals(new File(GRISU_SYSTEM_WIDE_CONFIG_DIR))) {
 			final File varDir = new File(GRISU_SYSTEM_WIDE_VAR_DIR);
 			if (!varDir.canWrite()) {
 				throw new RuntimeException("Can't write to directory "
 						+ varDir.toString() + ". Please adjust permissions.");
 			}
 			return varDir;
 		} else {
 			return getGrisuDirectory();
 		}
 
 	}
 
 	public static void setGrisuDirectory(String path) {
 
 		if (grisuDirectoryAccessed) {
 			throw new RuntimeException(
 					"Can't set grisu directory because it was accessed once already. You need to set it before you do anything else.");
 		}
 
 		if (GRISU_DIRECTORY != null) {
 			throw new RuntimeException(
 					"Can't set grisu directory because it was already accessed once after the start of this application...");
 		}
 
 		USER_SET_GRISU_DIRECTORY = path;
 	}
 
 	private Environment() {
 	}
 
 }
