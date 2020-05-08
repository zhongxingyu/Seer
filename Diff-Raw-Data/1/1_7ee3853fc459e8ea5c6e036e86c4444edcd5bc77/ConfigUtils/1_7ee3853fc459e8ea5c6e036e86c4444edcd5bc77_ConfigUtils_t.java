 package org.tsaikd.java.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ConfigUtils {
 
 	static Log log = LogFactory.getLog(ConfigUtils.class);
 
 	static final String defaultResPath = "config.properties";
 	static ConfigUtils instance = new ConfigUtils();
 	static Class<?> searchBase = ConfigUtils.class;
 
 	@SuppressWarnings("serial")
 	static LinkedList<String> searchPath = new LinkedList<String>() {{
 		add("");
 		add("/");
 		add("/../");
 		add("/../../");
 	}};
 
 	public class PropInfo {
 		Properties prop = null;
 		String path = null;
 		long modTime = 0;
 		boolean autoReaload = false;
 	}
 
 	LinkedList<PropInfo> propList = new LinkedList<PropInfo>();
 	HashMap<String, PropInfo> propPathMap = new HashMap<String, PropInfo>();
 
 	protected ConfigUtils() {
 	}
 
 	public static ConfigUtils addProperties(PropInfo info) {
 		if (info == null) {
 			return instance;
 		}
 		instance.propList.add(0, info);
 		instance.propPathMap.put(info.path, info);
 		return instance;
 	}
 
 	public static ConfigUtils addClassResource(String path, boolean autoReload) {
 		if (instance.propPathMap.containsKey(path)) {
 			PropInfo info = instance.propPathMap.get(path);
 			if (info.autoReaload != autoReload) {
 				info.autoReaload = autoReload;
 				instance.propPathMap.put(path, info);
 				log.debug("Modify properties resource: " + path);
 			}
 			return instance;
 		}
 
 		PropInfo info = getPropInfoByPath(path);
 		info.autoReaload = autoReload;
 		return addProperties(info);
 	}
 
 	public static ConfigUtils addClassResource(String path) {
 		return addClassResource(path, false);
 	}
 
 	public static void setSearchBase(Class<?> base) {
 		searchBase = base;
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
 
 		value = System.getenv(key);
 		if (value != null) {
 			return value;
 		}
 
 		if (instance.propList.isEmpty()) {
 			addClassResource(defaultResPath);
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
 
 	private static File searchPropFromFile(String path) {
 		String base = searchBase.getProtectionDomain().getCodeSource().getLocation().getPath();
 		File file = new File(path);
 
 		if (file.exists() && file.canRead()) {
 			return file;
 		}
 
 		for (String inc : searchPath) {
 			file = new File(base + inc + path);
 			if (file.exists() && file.canRead()) {
 				return file;
 			}
 		}
 
 		return null;
 	}
 
 	private static InputStream searchPropFromResource(String path) {
 		InputStream is = null;
 
 		for (String inc : searchPath) {
 			is = searchBase.getResourceAsStream(inc + path);
 			if (is != null) {
 				log.debug("Load properties from search path resource: " + inc + path);
 				return is;
 			}
 		}
 
 		is = Thread.currentThread().getClass().getResourceAsStream(path);
 		if (is != null) {
 			log.debug("Load properties from current thread resource: " + path);
 			return is;
 		}
 
 		return is;
 	}
 
 	public static PropInfo getPropInfoByPath(String path) {
 		PropInfo info = instance.new PropInfo();
 		InputStream is = null;
 
 		info.path = path;
 
 		File file = searchPropFromFile(path);
 		if (file != null && file.exists() && file.canRead()) {
 			log.debug("Load properties file: " + file.getAbsolutePath());
 			try {
 				is = new FileInputStream(file);
				info.path = file.getAbsolutePath();
 				info.modTime = file.lastModified();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 
 		if (is == null) {
 			is = searchPropFromResource(path);
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
 			log.error("Can not load properties file: " + path);
 			if (log.isDebugEnabled()) {
 				String base = searchBase.getProtectionDomain().getCodeSource().getLocation().getPath();
 				log.debug("Loader base: " + base);
 			}
 		}
 
 		return info;
 	}
 
 }
