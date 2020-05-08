 package play.modules.messagefiles;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 import play.Logger;
 import play.Play;
 import play.PlayPlugin;
 import play.i18n.Messages;
 import play.libs.IO;
 import play.vfs.VirtualFile;
 
 public class MessageFilesPlugin extends PlayPlugin {
 	private static final String PLUGIN_NAME = "MessageFiles Plugin";
 	private static final String CONF_LANGS_PATH_KEY = "messagefiles.path";
 	private static final String CONF_DEFAULT_LOCALE_KEY = "messagefiles.defaultLocale";
 	private static final String CONF_ENABLE_DIAGNOSTICS = "messagefiles.enableDiagnostics";
 	private static final String PROPERTIES_EXTENSION = ".properties";
 	private static long lastLoading = 0L;
 	private FilenameFilter propertiesFileFilter;
 	private boolean diagEnabled = false;
 	
 	public MessageFilesPlugin() {
 		propertiesFileFilter = new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.contains(PROPERTIES_EXTENSION);
 			}
 		};
 		
 		diagMessage("has been instantiated");
 	}
 	
 	@Override
 	public void onApplicationStart() {
 		diagEnabled = Boolean.parseBoolean(Play.configuration.getProperty(CONF_ENABLE_DIAGNOSTICS, "false"));
 		loadMessages();
 	}
 	
 	@Override
 	public void detectChange() {
 		VirtualFile langsRoot = getLangsRoot();
 		
 		if (langsRoot != null) {
 			for (String locale : Play.langs) {
 				VirtualFile localeRoot = langsRoot.child(locale);
 				if (localeRoot != null && localeRoot.exists()) {
 					for (File file : localeRoot.getRealFile().listFiles(propertiesFileFilter)) {
 						if (file.lastModified() > lastLoading) {
 							loadMessages();
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void loadMessages() {
 		diagMessage("going to load messages");
 		
 		String langsPath = getLangsPath();
 		VirtualFile langsRoot = getLangsRoot();
 		String defaultLocale = Play.configuration.getProperty(CONF_DEFAULT_LOCALE_KEY);
 		int totalMessagesLoaded = 0;
 		
 		if (defaultLocale == null) {
 			Logger.warn("%s is not defined in application.conf", CONF_DEFAULT_LOCALE_KEY);
 		}
 		
 		if (langsPath == null) {
 			Logger.warn("%s is not defined in application.conf", CONF_LANGS_PATH_KEY);
 		} else if (langsRoot == null || !langsRoot.exists()) {
 			Logger.error("%s is defined in application.conf but respective folder could not be found", CONF_LANGS_PATH_KEY);
 		} else {
 			for (String locale : Play.langs) {
 				diagMessage("found %s locale in Play locales", locale);
 				
 				VirtualFile localeRoot = langsRoot.child(locale);
 				if (localeRoot == null || !localeRoot.exists()) {
 					Logger.warn("Could not find %s locale in %s", locale, langsPath);
 				} else {
 					diagMessage("going to load %s messages from %s", locale, localeRoot.getRealFile().getAbsolutePath());
 					
 					for (File file : localeRoot.getRealFile().listFiles(propertiesFileFilter)) {
 						try {
 							diagMessage("going to load messages from %s", file.getAbsolutePath());
 							
 							String prefix = file.getName().replace(PROPERTIES_EXTENSION, "");
 							Properties props = readLocalizationFile(file);
 							Properties propsWithPrefix = new Properties();
 							
 							// Prepend prefix.
 							for (Entry<Object, Object> prop : props.entrySet()) {
								propsWithPrefix.put(prefix + "." + ((String)prop.getKey()).trim(), prop.getValue());
 							}
 							
							Properties alreadyLoaded = Messages.locales.get(locale);
							
							if (alreadyLoaded == null) {
								alreadyLoaded = new Properties();
							}
							
							// Append messages to already existing.
							alreadyLoaded.putAll(propsWithPrefix);
							Messages.locales.put(locale, alreadyLoaded);
 							
 							diagMessage("loaded %d messages", propsWithPrefix.size());
 							totalMessagesLoaded += propsWithPrefix.size();
 							
 							// Put messages from this locale in Play default messages 
 							// if this locale is defined as default in configuration.
 							if (defaultLocale != null && defaultLocale.equals(locale)) {
 								if (Messages.defaults == null) {
 									Messages.defaults = new Properties();
 								}
 								Messages.defaults.putAll(propsWithPrefix);
 								
 								diagMessage("locale %s is default", locale);
 							}
 						} catch (FileNotFoundException e) {
 							Logger.warn(e, "Could not read %s", file.getAbsolutePath());
 						}
 					}
 					
 					diagMessage("done loading %s messages", locale);
 				}
 			}
 		}
 		
 		diagMessage("loaded %d messages total", totalMessagesLoaded);
 		diagMessage("done loading messages");
 	}
 	
 	private String getLangsPath() {
 		return Play.configuration.getProperty(CONF_LANGS_PATH_KEY);
 	}
 	
 	private VirtualFile getLangsRoot() {
 		return getLangsPath() != null ? Play.getVirtualFile(getLangsPath()) : null;
 	}
 	
 	private Properties readLocalizationFile(File f) throws FileNotFoundException {
 		if (f != null) {
 			return IO.readUtf8Properties(new FileInputStream(f));
 		}
 		
 		return null;
 	}
 	
 	private void diagMessage(String message, Object... args) {
 		if (diagEnabled) {
 			Logger.info(PLUGIN_NAME + ": " + message, args);
 		}
 	}
 }
