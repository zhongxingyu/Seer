 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It's a bridge between the filemanager and a storage backend and 
  * works like a transparent VFS or proxy.
  * Copyright (C) Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c.resource;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.thischwa.c5c.requestcycle.UserAction;
 
 /**
 * It provides localized messages for failed user actions of the {@link UserAction} implementation.
  */
 public class UserActionMessageHolder {
 	private static Logger logger = LoggerFactory.getLogger(UserActionMessageHolder.class);
 	
 	private static Map<String, Properties> langData = new HashMap<String, Properties>();
	
	private static String defaultLanguage = PropertiesLoader.getDefaultLocale().getLanguage();
 
 	private static String baseName = "userActionMessages";
 	
 	static {
 		Map<String, InputStream> data = new HashMap<String, InputStream>();
		List<String> langs = Arrays.asList(Locale.getISOLanguages());
 		
 		// load known language files in the library
		data.put(defaultLanguage, UserActionMessageHolder.class.getResourceAsStream(String.format("%s.properties", baseName)));
 		data.put("de", UserActionMessageHolder.class.getResourceAsStream(String.format("%s_de.properties", baseName)));
 		
 		// load user bundles
 		Pattern baseNamePattern = Pattern.compile(String.format(".*%s_?([a-z]{2})?\\.properties$", baseName));
 		File dir = new File(System.getProperty("user.dir"));
 		for(File file : dir.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.startsWith(baseName) && name.endsWith(".properties");
 			}
 		})) {
 			String path = file.getAbsolutePath();
 			Matcher matcher = baseNamePattern.matcher(path);
 			if(!matcher.matches())
 				continue;
 			String lang = matcher.group(1);
 			if(langs.contains(lang)) {
 				try {
 					boolean replaced = data.containsKey(lang);
 					data.put(lang, new BufferedInputStream(new FileInputStream(file)));
 					if(replaced) 
 						logger.debug("user-bundle-search: lang#{} found, lang in library-bundle replaced", lang);
 					else
 						logger.debug("user-bundle-search: lang#{} found", lang);
 				} catch (FileNotFoundException e) {
 				}
 			}
 		}
 
 		// build the bundles and cache it
 		for(String lang : data.keySet()) {
 			try {
 				Properties props = new Properties();
 				props.load(data.get(lang));
 				langData.put(lang, props);
 			} catch (IOException e) {
 				throw new RuntimeException(String.format("A ResourceBundle couldn't be initialized for %s", lang));
 			}
 		}
 		
 		logger.info("*** {} successful initialized.", UserActionMessageHolder.class.getName());
 	}
 
 	public static String get(Locale locale, Key key) {
 		String lang = locale.getLanguage();
 		try {
 			if(!langData.containsKey(lang))
				return langData.get(defaultLanguage).getProperty(key.getPropertyName());
 			return langData.get(lang).getProperty(key.getPropertyName());
 		} catch (MissingResourceException e) {
 			logger.error("Missinig key for locale [{}]: {}", locale.toString(), key);
 			return String.format("MISSING KEY | %s |", key);
 		}
 	} 
 
 	public enum Key {
 		UploadNotAllowed("upload.notallowed"),
 		CreateFolderNotAllowed("createfolder.notallowed");
 		
 		private String propertyName;
 		
 		private Key(String propertyName) {
 			this.propertyName = propertyName;
 		}
 		
 		public String getPropertyName() {
 			return propertyName;
 		}
 	}
 }
 
