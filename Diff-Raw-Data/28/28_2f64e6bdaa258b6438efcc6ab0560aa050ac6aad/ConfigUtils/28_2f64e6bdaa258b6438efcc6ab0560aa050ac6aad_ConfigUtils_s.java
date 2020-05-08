 package org.tsaikd.java.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ConfigUtils {
 
 	static Log log = LogFactory.getLog(ConfigUtils.class);
 
 	static final String defaultResPath = "/config.properties";
 	static ConfigUtils instance = new ConfigUtils();
 
 	public class PropInfo {
 		Properties prop = null;
 		String path = null;
 		long modTime = 0;
 		boolean autoReaload = false;
 	}
 
 	List<PropInfo> propList = new LinkedList<PropInfo>();
 	Collection<String> pathList = new HashSet<String>();
 
 	protected ConfigUtils() {
		try {
			addClassResource(defaultResPath);
		} catch (Exception e) {
		}
 	}
 
 	public static ConfigUtils addProperties(PropInfo info) {
 		if (info == null) {
 			return instance;
 		}
 		instance.propList.add(0, info);
 		instance.pathList.add(info.path);
 		return instance;
 	}
 
 	public static ConfigUtils addClassResource(String path, boolean autoReload) {
 		if (instance.pathList.contains(path)) {
 			return instance;
 		}
 
 		PropInfo info = getPropInfoByPath(path);
 		info.autoReaload = autoReload;
 		return addProperties(info);
 	}
 
 	public static ConfigUtils addClassResource(String path) {
 		return addClassResource(path, false);
 	}
 
 	public static void set(final String key, final String value) {
 		System.setProperty(key, value);
 	}
 
 	public static String get(final String key, final String defaultValue) {
 		if (key == null) {
 			return null;
 		}
 		String value = System.getProperty(key);
 		if (value != null) {
 			return value;
 		}
 
 		try {
 			for (PropInfo info : instance.propList) {
 				if (info.autoReaload && (info.path != null)) {
 					File file = new File(info.path);
 					if (file.exists() && file.canRead()) {
 						if ((file.lastModified() != info.modTime) || (info.prop == null)) {
 							if (info.prop == null) {
 								info.prop = new Properties();
 							} else {
 								info.prop.clear();
 							}
 							info.prop.load(new FileInputStream(file));
 							info.modTime = file.lastModified();
 							log.debug("Reload file: " + info.path);
 						}
 					}
 				}
 	
 				if (info.prop == null) {
 					continue;
 				}
 	
 				value = info.prop.getProperty(key);
 				if (value != null) {
 					return value;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return defaultValue;
 	}
 
 	public static String get(final String key) {
 		return get(key, null);
 	}
 
 	public static void set(final String key, final int value) {
 		System.setProperty(key, String.valueOf(value));
 	}
 
 	public static int getInt(final String key, final int defaultValue) {
 		String value = get(key, null);
 		if (value == null) {
 			return defaultValue;
 		} else {
 			return Integer.parseInt(value);
 		}
 	}
 
 	public static int getInt(final String key) {
 		return getInt(key, 0);
 	}
 
 	public static void set(final String key, final long value) {
 		System.setProperty(key, String.valueOf(value));
 	}
 
 	public static long getLong(final String key, final long defaultValue) {
 		String value = get(key, null);
 		if (value == null) {
 			return defaultValue;
 		} else {
 			return Integer.parseInt(value);
 		}
 	}
 
 	public static long getLong(final String key) {
 		return getLong(key, 0);
 	}
 
 	public static void set(final String key, final boolean value) {
 		System.setProperty(key, String.valueOf(value));
 	}
 
 	public static boolean getBool(final String key, final boolean defaultValue) {
 		String value = get(key, null);
 		if (value == null) {
 			return defaultValue;
 		} else {
 			value = value.toLowerCase().trim();
 			if (value.equals("true"))
 				return true;
 			if (value.equals("yes"))
 				return true;
 			return false;
 		}
 	}
 
 	public static boolean getBool(final String key) {
 		return getBool(key, false);
 	}
 
 	public static PropInfo getPropInfoByPath(String path) {
 		PropInfo info = instance.new PropInfo();
 		InputStream is;
 
 		info.path = path;
 
 		is = Thread.currentThread().getClass().getResourceAsStream(path);
 
		if (is == null) {
 			File file = new File(path);
 			if (file.exists() && file.canRead()) {
 				try {
 					is = new FileInputStream(file);
 					info.modTime = file.lastModified();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if (is != null) {
 			info.prop = new Properties();
 			try {
 				info.prop.load(is);
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			log.debug("Can not load file: " + path);
 		}
 
 		return info;
 	}
 
 }
