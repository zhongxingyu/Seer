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
  *  - GNU Lesser General Public License Version 3.0 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl-3.0.html
  * 
  *  - Mozilla Public License Version 2.0 or later (the "MPL")
  *    http://www.mozilla.org/MPL/2.0/
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import de.thischwa.c5c.exception.FilemanagerException;
 import de.thischwa.c5c.resource.PropertiesLoader;
 import de.thischwa.c5c.util.Path;
 
 /**
  * The default implementation of the {@link MessageResolver} interface. It holds the messages 
  * provided by several javascript files located in the path 'scripts/languages' inside
  * the folder of filemanager.
  */
 public class FilemanagerMessageResolver implements MessageResolver {
 	private static Logger logger = LoggerFactory.getLogger(FilemanagerMessageResolver.class);
 
 	protected static String langPath = "scripts/languages";
 	
 	protected FilenameFilter jsFilter = new FilenameFilter() {
 		@Override
 		public boolean accept(File dir, String name) {
 			return name.endsWith(".js");
 		}
 	};
 
 	private Map<String, Map<String, String>> messageStore = new HashMap<>();
 
 	@Override
 	public void setServletContext(ServletContext servletContext) throws RuntimeException {
 		Path path = new Path(PropertiesLoader.getFilemanagerPath()).addFolder(langPath);
 		File msgFolder = new File(servletContext.getRealPath(path.toString()));
 		if(!msgFolder.exists())
 			throw new RuntimeException("C5 scripts folder couldn't be found!");
 
 		ObjectMapper mapper = new ObjectMapper();
 		try {
 			for(File file: msgFolder.listFiles(jsFilter)) {
 				String lang = FilenameUtils.getBaseName(file.getName());
 		        @SuppressWarnings("unchecked")
 				Map<String, String> langData = mapper.readValue(file, Map.class);
 				collectLangData(lang, langData);
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	protected void collectLangData(final String lang, final Map<String, String> data) {
 		messageStore.put(lang, data);
 	}
 	
 	@Override
 	public String getMessage(Locale locale, FilemanagerException.Key key) throws IllegalArgumentException {
 		String lang = locale.getLanguage().toLowerCase();
 		if(!messageStore.containsKey(lang)) {
 			logger.warn("Language [{}] not supported, take the default.", lang);
 			lang = PropertiesLoader.getDefaultLocale().getLanguage().toLowerCase();
 		}
 		Map<String, String> messages = messageStore.get(lang);
 		if(!messages.containsKey(key.getPropertyName()))
 			throw new IllegalArgumentException("Message key not found: " + key.getPropertyName());
		return messages.get(key);
 	}
 }
