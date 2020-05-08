 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.utils;
 
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.getResourceAsString;
 import static org.oobium.utils.StringUtils.lowerKeys;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.utils.json.JsonUtils.toMap;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.logging.Logger;
 import org.oobium.utils.coercion.TypeCoercer;
 
 public class Config {
 
 	public enum Mode {
 		DEV, TEST, PROD;
 		public static final String SYSTEM_PROPERTY = "org.oobium.mode";
 		public static Mode getSystemMode() {
 			return parse(System.getProperty(SYSTEM_PROPERTY));
 		}
 		public static Mode parse(String str) {
 			if(str != null) {
 				try {
 					return Mode.valueOf(str.toUpperCase());
 				} catch(Exception e) {
 					// discard
 				}
 			}
 			return DEV;
 		}
 	}
 
 	public enum OsgiRuntime { 
 		Equinox, Felix, Knopplerfish;
 		public static OsgiRuntime parse(String str) {
 			if(str != null) {
 				try {
 					return OsgiRuntime.valueOf(str);
 				} catch(Exception e) {
 					// discard
 				}
 			}
 			return Felix;
 		}
 	}
 
 	public static final String HOST = "host";
 	public static final String PORT = "port";
 	
 	public static final String CACHE = "cache";
 	public static final String MODULES = "modules";
 	public static final String PERSIST = "persist";
 	public static final String RUNTIME = "runtime";
 	public static final String SESSION = "session";
 	public static final String SERVER = "server";
 	public static final String MAIL_SEND = "mail.send";
 	public static final String MAIL_RETRIEVE = "mail.retrieve";
 	
 	public static final String MIGRATION_SERVICE = "migration.service";
 	
 	
 	/**
 	 * Loads the configuration of the project containing the given class in its root package.
 	 * @param refClass any class in the application's (or its migration's) root package
 	 * @return an instance of AppConfig, loaded if possible. never null.
 	 */
 	public static Config loadConfiguration(Class<?> refClass) {
 		try {
 			Map<String, Object> map = toMap(getResourceAsString(refClass, "configuration.js"));
 			return new Config(map);
 		} catch(Exception e) {
 			Logger.getLogger(refClass).error("There was an error loading the configuration.", e);
 		}
 		return new Config(new HashMap<String, Object>(0));
 	}
 	
 	public static Config loadConfiguration(File file) {
 		if(file != null && file.exists()) {
 			String fileName = file.getName();
 			try {
 				if(fileName.endsWith(".jar")) {
 					Map<String, Object> map = toMap(FileUtils.readJarEntry(file, "configuration.js"));
 					return new Config(map);
 				} else {
 					Map<String, Object> map = toMap(FileUtils.readFile(file).toString());
 					return new Config(map);
 				}
 			} catch(Exception e) {
 				Logger.getLogger().error("There was an error loading the configuration.", e);
 			}
 		}
 		return new Config(new HashMap<String, Object>(0));
 	}
 	
 	public static Config loadConfiguration(String configuration) {
 		return new Config(toMap(configuration));
 	}
 	
 	protected final Mode mode;
 	protected final Map<String, Object> properties;
 
 	public Config(Map<String, ? extends Object> properties) {
 		this.mode = Mode.getSystemMode();
 		this.properties = lowerKeys(properties);
 	}
 	
 	
 	public Object get(String name) {
 		return get(name, mode);
 	}
 	
 	public <T> T get(String name, Class<T> type) {
 		return TypeCoercer.coerce(get(name, mode), type);
 	}
 	
 	public Object get(String name, Mode mode) {
 		if(!blank(name)) {
 			Object obj = properties.get(name.toLowerCase());
 			Object o = properties.get(mode.name().toLowerCase());
 			if(o instanceof Map<?,?>) {
 				o = ((Map<?,?>) o).get(name.toLowerCase());
 			}
 			if(obj == null && o == null) {
 				return null;
 			}
 			if(obj != null && o == null) {
 				return obj;
 			}
 			if(obj == null && o != null) {
 				return o;
 			}
 			if(obj instanceof List<?>) {
 				List<Object> l = new ArrayList<Object>();
 				l.addAll((List<?>) obj);
 				if(o instanceof List<?>) {
 					l.addAll((List<?>) o);
 					return l;
 				} // else map or object
 				l.add(o);
 				return l;
 			}
 			// obj is a map or object
 			List<Object> l = new ArrayList<Object>();
 			l.add(obj);
 			if(o instanceof List<?>) {
 				l.addAll((List<?>) o);
				return l;
 			}
 			// neither is a list
 			l.add(o);
 			return l;
 		}
 		return null;
 	}
 	
 	public <T> T get(String name, Mode mode, Class<T> type) {
 		return TypeCoercer.coerce(get(name, mode), type);
 	}
 	
 	public <T> T get(String name, Mode mode, T defaultValue) {
 		return TypeCoercer.coerce(get(name, mode), defaultValue);
 	}
 	
 	public <T> T get(String name, T defaultValue) {
 		return TypeCoercer.coerce(get(name, mode), defaultValue);
 	}
 	
 	public String getPathToApp(String base) {
 		String path = getString("app");
 		if(!blank(path)) {
 			if(path.charAt(0) == '/') {
 				return path;
 			} else {
 				return base + "/" + path;
 			}
 		} else {
 			return base;
 		}
 	}
 	
 	public String getPathToCaches(String base) {
 		return getPath("caches", getPathToControllers(base));
 	}
 	
 	public String getPathToControllers(String base) {
 		return getPath("controllers", getPathToApp(base));
 	}
 	
 	public String[] getHosts() {
 		return getHosts(mode);
 	}
 	
 	public String[] getHosts(Mode mode) {
 		String[] hosts;
 		Object o = get(HOST, mode);
 		if(o == null) {
 			hosts = new String[0];
 		} else if(o instanceof List<?>) {
 			List<?> l = (List<?>) o;
 			hosts = new String[l.size()];
 			for(int i = 0; i < hosts.length; i++) {
 				hosts[i] = String.valueOf(l.get(i));
 			}
 		} else {
 			hosts = new String[1];
 			hosts[0] = String.valueOf(o);
 		}
 		return hosts;
 	}
 
 	public String getPathToMailers(String base) {
 		return getPath("mailers", getPathToApp(base));
 	}
 	
 	public Mode getMode() {
 		return mode;
 	}
 	
 	public String getPathToModels(String base) {
 		return getPath("models", getPathToApp(base));
 	}
 	
 	public Config getModuleConfig(Class<?> refClass) {
 		Config moduleConfig = loadConfiguration(refClass);
 		moduleConfig.properties.putAll(properties);
 		return moduleConfig;
 	}
 	
 	public String getPathToObservers(String base) {
 		return getPath("observers", getPathToModels(base));
 	}
 	
 	private String getPath(String name, String base) {
 		String path = getString(name);
 		if(!blank(path)) {
 			if(path.charAt(0) == '/') {
 				return path;
 			} else {
 				return base + "/" + path;
 			}
 		} else {
 			return base + "/" + name;
 		}
 	}
 	
 	public int getPort() {
 		return getPort(mode);
 	}
 	
 	public int getPort(Mode mode) {
 		return coerce(get(PORT, mode), int.class);
 	}
 	
 	/**
 	 * Only <a href='http://felix.apache.org'>Apache Felix</a> is supported at this time.<br>
 	 * Always returns {@link OsgiRuntime#Felix}
 	 * @return {@link OsgiRuntime#Felix}
 	 */
 	public OsgiRuntime getRuntime() {
 		return getRuntime(mode);
 	}
 	
 	/**
 	 * Only <a href='http://felix.apache.org'>Apache Felix</a> is supported at this time.<br>
 	 * Always returns {@link OsgiRuntime#Felix}
 	 * @return {@link OsgiRuntime#Felix}
 	 */
 	public OsgiRuntime getRuntime(Mode mode) {
 //		String runtime = getString("runtime", mode);
 //		if(runtime == null) {
 //			runtime = getString("runtime");
 //		}
 //		return OsgiRuntime.parse(runtime);
 		return OsgiRuntime.Felix;
 	}
 	
 	public String getString(String name) {
 		return getString(name, mode);
 	}
 	
 	public String getString(String name, Mode mode) {
 		Object o = get(name, mode);
 		return (o != null) ? o.toString() : null;
 	}
 
 	public String getString(String name, Mode mode, String defaultValue) {
 		return get(name, mode, defaultValue);
 	}
 
 	public String getString(String name, String defaultValue) {
 		return get(name, mode, defaultValue);
 	}
 	
 	public String getPathToViews(String base) {
 		return getPath("views", getPathToApp(base));
 	}
 	
 	@Override
 	public String toString() {
 		return mode + ": " + String.valueOf(properties);
 	}
 	
 }
