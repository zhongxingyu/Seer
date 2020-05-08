 /*
  * movie-renamer-core
  * Copyright (C) 2012 Nicolas Magré
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.free.movierenamer.settings;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.bind.DatatypeConverter;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.sun.jna.NativeLibrary;
 import com.sun.jna.Platform;
 import fr.free.movierenamer.info.NfoInfo;
 
 import fr.free.movierenamer.mediainfo.MediaInfoLibrary;
 import fr.free.movierenamer.scrapper.MovieScrapper;
 import fr.free.movierenamer.scrapper.SubtitleScrapper;
 import fr.free.movierenamer.scrapper.TvShowScrapper;
 import fr.free.movierenamer.scrapper.impl.IMDbScrapper;
 import fr.free.movierenamer.scrapper.impl.SubsceneSubtitleScrapper;
 import fr.free.movierenamer.scrapper.impl.TheTVDBScrapper;
 import fr.free.movierenamer.utils.FileUtils;
 import fr.free.movierenamer.utils.Sorter;
 import fr.free.movierenamer.utils.StringUtils;
 import fr.free.movierenamer.utils.URIRequest;
 import fr.free.movierenamer.utils.XPathUtils;
 
 /**
  * Class Settings , Movie Renamer settings
  *
  * @author Nicolas Magré
  * @author Simon QUÉMÉNEUR
  */
 public final class Settings {
 
   static {
     String appName = getApplicationProperty("application.name");
     String appNameNospace = appName.replace(' ', '_');
     String appModule = getApplicationProperty("application.module.name");
     String appModuleNospace = appModule.replace(' ', '_');
     APPNAME = appName;
     APPMODULE = appModule;
     VERSION = getApplicationProperty("application.module.version");
     appName_nospace = appNameNospace;
     appFolder = getApplicationFolder();
     configFileName = appNameNospace + "_" + appModuleNospace + ".conf";
     logFileName = appNameNospace + "_" + appModuleNospace + ".log";
     LOGGER = Logger.getLogger(appName + " Logger");
     appSettingsNodeName = appNameNospace + "_" + appModuleNospace;
     settingNodeName = "settings";
   }
   public static final String APPNAME;
   public static final String APPMODULE;
   public static final String VERSION;
   public static final File appFolder;
   private static final String appName_nospace;
   // files
   private static final String configFileName;
   private static final String logFileName;
   // Logger
   public static final Logger LOGGER;
   private static final String appSettingsNodeName;
   private static final String settingNodeName;
   // Settings instance
   private static final Settings instance = new Settings();
   // Settings xml conf instance
   private final Document settingsDocument;
   private final Node settingsNode;
   private boolean autosave = true;
 
   public interface IProperty {
 
     public Class<?> getVclass();
     public Object getDefaultValue();
     public String getValue();
     public void setValue(Object value) throws IOException;
   }
 
   public enum SettingsProperty implements IProperty {
     // app lang
 
     appLanguage(Locale.ENGLISH), // (Locale.ENGLISH.toString()),
     // movie filename
     movieFilenameFormat("<t> (<y>)"), // ("<t> (<y>)"),
     movieFilenameSeparator(", "), // (", "),
     movieFilenameLimit(3), // (Integer.decode("3").toString()),
     movieFilenameCase(StringUtils.CaseConversionType.FIRSTLA.name()), // (StringUtils.CaseConversionType.FIRSTLA.name()),
     movieFilenameTrim(Boolean.TRUE), // (Boolean.TRUE.toString()),
     movieFilenameRmDupSpace(Boolean.TRUE), // (Boolean.TRUE.toString()),
     movieFilenameCreateDirectory(Boolean.FALSE), // (Boolean.FALSE.toString()),
     // movie folder
     movieFolderFormat("<t> (<y>)"), // ("<t> (<y>)"),
     movieFolderSeparator(", "), // (", "),
     movieFolderLimit(3), // (Integer.decode("3").toString()),
     movieFolderCase(""), // (""),
     movieFolderTrim(Boolean.TRUE), // (Boolean.TRUE.toString()),
     movieFolderRmDupSpace(Boolean.TRUE), // (Boolean.TRUE.toString()),
     // movie NFO
     movieNfoType(NfoInfo.NFOtype.XBMC.name()), // (NfoInfo.NFOtype.XBMC)
     // tvShow
     tvShowFilenameFormat("<st> S<s>E<e> <et>"), // ("<st> S<s>E<e> <et>"),
     tvShowFilenameSeparator(", "), // (", "),
     tvShowFilenameLimit(3), // (Integer.decode("3").toString()),
     tvShowFilenameCase(""), // (""),
     tvShowFilenameTrim(Boolean.TRUE), // (Boolean.TRUE.toString()),
     tvShowFilenameRmDupSpace(Boolean.TRUE), // (Boolean.TRUE.toString()),
     // Cache
     cacheClear(Boolean.FALSE), // (Boolean.FALSE.toString()),
     // Search
     searchMovieScrapper(IMDbScrapper.class), // (IMDbScrapper.class.toString()),
     searchTvshowScrapper(TheTVDBScrapper.class), // (TheTVDBScrapper.class.toString()),
     searchSubtitleScrapper(IMDbScrapper.class), // (IMDbScrapper.class.toString()),// FIXME
     searchScrapperLang(Locale.ENGLISH), // (Locale.ENGLISH.toString()),
     searchSort(Sorter.SorterType.LEVEN_YEAR), // (Boolean.TRUE.toString()),
     searchNbResult(2), // (Integer.decode("2").toString()),
     searchDisplayApproximateResult(Boolean.FALSE), // (Boolean.FALSE.toString()),
     // Proxy
     proxyIsOn(Boolean.FALSE), // (Boolean.FALSE.toString()),
     proxyUrl(""), // (""), // "10.2.1.10"
     proxyPort(0), // (Integer.decode("0").toString()), // 3128
     // http param
     httpRequestTimeOut(30), // (Integer.decode("30").toString()),
     httpCustomUserAgent(""); // Mozilla/5.0 (Windows NT 5.1; rv:10.0.2) Gecko/20100101 Firefox/10.0.2
     private Class<?> vclass;
     private Object defaultValue;
 
     private SettingsProperty(Object defaultValue) {
       this.vclass = defaultValue.getClass();
       this.defaultValue = defaultValue;
     }
 
     @Override
     public Class<?> getVclass() {
       return vclass;
     }
 
     @Override
     public Object getDefaultValue() {
       return defaultValue;
     }
 
     @Override
     public String getValue() {
       return instance.get(this);
     }
 
     @Override
     public void setValue(Object value) {
       instance.set(this, value);
     }
   }
 
   /**
    * Access to the Settings instance
    *
    * @return The only instance of MR Settings
    */
   public static Settings getInstance() {
     return instance;
   }
 
   /**
    * Constructor
    */
   private Settings() {
     // Log init
     try {
       File logsRoot = new File(Settings.appFolder, "logs");
       if (!logsRoot.isDirectory() && !logsRoot.mkdirs()) {
         throw new IOException("Failed to create logs dir: " + logsRoot);
       }
       FileHandler fh = new FileHandler(logsRoot.getAbsolutePath() + File.separator + logFileName);
       LOGGER.addHandler(fh);
     } catch (SecurityException e) {
       LOGGER.log(Level.SEVERE, e.getMessage());
     } catch (IOException e) {
       LOGGER.log(Level.SEVERE, e.getMessage());
     }
 
     // settingsDocument init
     Document settingsDocument;
     Node settingsNode;
     try {
       File confRoot = new File(Settings.appFolder, "conf");
       File file = new File(confRoot, configFileName);
       settingsDocument = URIRequest.getXmlDocument(file.toURI());
       Node appSettingsNode = XPathUtils.selectNode(appSettingsNodeName, settingsDocument);
       if (!VERSION.equals(XPathUtils.getAttribute("Version", appSettingsNode))) {
         throw new NullPointerException("App version is different");
       }
       settingsNode = XPathUtils.selectNode(settingNodeName, appSettingsNode);
       // TODO convert if version are diff !
     } catch (Exception ex) {
       try {
         DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder;
         docBuilder = docFactory.newDocumentBuilder();
 
         // root elements
         settingsDocument = docBuilder.newDocument();
         Element rootElement = settingsDocument.createElement(appSettingsNodeName);
         settingsDocument.appendChild(rootElement);
 
         Attr version = settingsDocument.createAttribute("Version");
         version.setValue(VERSION);
         rootElement.setAttributeNode(version);
 
         // setting elements
         settingsNode = settingsDocument.createElement(settingNodeName);
         rootElement.appendChild(settingsNode);
 
       } catch (ParserConfigurationException ex1) {
         settingsDocument = null;
         settingsNode = null;
       }
     }
     this.settingsDocument = settingsDocument;
     this.settingsNode = settingsNode;
     if(autosave){
       saveSetting();
     }
   }
 
   private synchronized String get(SettingsProperty key) {
     String value;
     if (key != null) {
       Node found = XPathUtils.selectNode(key.name(), settingsNode);
       if (found != null) {
         value = XPathUtils.getTextContent(found);
       } else {
         value = null;
       }
     } else {
       value = null;
     }
     if (value == null) {
       value = key.getDefaultValue().toString();
     }
     return value;
   }
 
   public synchronized void set(SettingsProperty key, Object value) {
     if (value != null && key != null) {
       Object savedValue = key.getValue();
 
       if (savedValue.toString().equals(value.toString())) {
         return;
       }
 
       Node found = XPathUtils.selectNode(key.name(), settingsNode);
       if (found == null) {
         found = settingsDocument.createElement(key.name());
         // param.appendChild(settingsDocument.createTextNode(value.toString()));
         settingsNode.appendChild(found);
       }
       found.setTextContent(value.toString());
       if(autosave) {
         saveSetting();
       }
     }
   }
 
   public synchronized void clear() {
     Logger.getLogger(Settings.class.getName()).log(Level.INFO, String.format("Clear Settings"));
     NodeList list = settingsNode.getChildNodes();
     for (int i = 0; i < list.getLength(); i++) {
       settingsNode.removeChild(list.item(i));
     }
     if(autosave) {
       saveSetting();
     }
   }
 
   public Locale getAppLanguage() {
     return new Locale(get(SettingsProperty.appLanguage));
   }
 
   public String getMovieFilenameFormat() {
     return get(SettingsProperty.movieFilenameFormat);
   }
 
   public String getMovieFilenameSeparator() {
     return get(SettingsProperty.movieFilenameSeparator);
   }
 
   public int getMovieFilenameLimit() {
     return Integer.parseInt(get(SettingsProperty.movieFilenameLimit));
   }
 
   public StringUtils.CaseConversionType getMovieFilenameCase() {
     return StringUtils.CaseConversionType.valueOf(get(SettingsProperty.movieFilenameCase));
   }
 
   public boolean isMovieFilenameTrim() {
     return Boolean.parseBoolean(get(SettingsProperty.movieFilenameTrim));
   }
 
   public boolean isMovieFilenameRmDupSpace() {
     return Boolean.parseBoolean(get(SettingsProperty.movieFilenameRmDupSpace));
   }
 
   public boolean isMovieFilenameCreateDirectory() {
     return Boolean.parseBoolean(get(SettingsProperty.movieFilenameCreateDirectory));
   }
 
   public String getMovieFolderFormat() {
     return get(SettingsProperty.movieFolderFormat);
   }
 
   public String getMovieFolderSeparator() {
     return get(SettingsProperty.movieFolderSeparator);
   }
 
   public int getMovieFolderLimit() {
     return Integer.parseInt(get(SettingsProperty.movieFolderLimit));
   }
 
   public String getMovieFolderCase() {
     return get(SettingsProperty.movieFolderCase);
   }
 
   public boolean isMovieFolderTrim() {
     return Boolean.parseBoolean(get(SettingsProperty.movieFolderTrim));
   }
 
   public boolean isMovieFolderRmDupSpace() {
     return Boolean.parseBoolean(get(SettingsProperty.movieFolderRmDupSpace));
   }
 
   public NfoInfo.NFOtype getMovieNfoType() {
     return NfoInfo.NFOtype.valueOf(get(SettingsProperty.movieNfoType));
   }
 
   public String getTvShowFilenameFormat() {
     return get(SettingsProperty.tvShowFilenameFormat);
   }
 
   public String getTvShowFilenameSeparator() {
     return get(SettingsProperty.tvShowFilenameSeparator);
   }
 
   public int getTvShowFilenameLimit() {
     return Integer.parseInt(get(SettingsProperty.tvShowFilenameLimit));
   }
 
   public String getTvShowFilenameCase() {
     return get(SettingsProperty.tvShowFilenameCase);
   }
 
   public boolean isTvShowFilenameTrim() {
     return Boolean.parseBoolean(get(SettingsProperty.tvShowFilenameTrim));
   }
 
   public boolean isTvShowFilenameRmDupSpace() {
     return Boolean.parseBoolean(get(SettingsProperty.tvShowFilenameRmDupSpace));
   }
 
   public boolean isCacheClear() {
     return Boolean.parseBoolean(get(SettingsProperty.cacheClear));
   }
 
   @SuppressWarnings("unchecked")
   public Class<? extends MovieScrapper> getSearchMovieScrapper() {
     try {
       return (Class<MovieScrapper>) Class.forName(get(SettingsProperty.searchMovieScrapper));
     } catch (Exception ex) {
       return IMDbScrapper.class;
     }
   }
 
   @SuppressWarnings("unchecked")
   public Class<? extends TvShowScrapper> getSearchTvshowScrapper() {
     try {
       return (Class<TvShowScrapper>) Class.forName(get(SettingsProperty.searchTvshowScrapper));
     } catch (Exception ex) {
       return TheTVDBScrapper.class;
     }
   }
 
   @SuppressWarnings("unchecked")
   public Class<? extends SubtitleScrapper> getSearchSubtitleScrapper() {
     try {
       return (Class<SubtitleScrapper>) Class.forName(get(SettingsProperty.searchSubtitleScrapper));
     } catch (Exception ex) {
       return SubsceneSubtitleScrapper.class;
     }
   }
 
   public Locale getSearchScrapperLang() {
     return new Locale(get(SettingsProperty.searchScrapperLang));
   }
 
  public Sorter.SorterType getSearchSorter() {
     return Sorter.SorterType.valueOf(get(SettingsProperty.searchSort));
   }
 
   public int getSearchNbResult() {
     return Integer.parseInt(get(SettingsProperty.searchNbResult));
   }
 
   public boolean isSearchDisplayApproximateResult() {
     return Boolean.parseBoolean(get(SettingsProperty.searchDisplayApproximateResult));
   }
 
   public boolean isProxyIsOn() {
     return Boolean.parseBoolean(get(SettingsProperty.proxyIsOn));
   }
 
   public String getProxyUrl() {
     return get(SettingsProperty.proxyUrl);
   }
 
   public int getProxyPort() {
     return Integer.parseInt(get(SettingsProperty.proxyPort));
   }
 
   public int getHttpRequestTimeOut() {
     return Integer.parseInt(get(SettingsProperty.httpRequestTimeOut));
   }
 
   public String getHttpCustomUserAgent() {
     return get(SettingsProperty.httpCustomUserAgent);
   }
 
   public boolean isAutosave() {
     return autosave;
   }
 
   public void setAutosave(boolean autosave) {
     this.autosave = autosave;
   }
 
   /**
    * Save setting
    *
    * @return True if setting was saved, False otherwise
    */
   private synchronized boolean saveSetting() {
     boolean saveSuccess;
     try {
       LOGGER.log(Level.INFO, "Save configuration to {0}", configFileName);
       File confRoot = new File(Settings.appFolder, "conf");
       if (!confRoot.isDirectory() && !confRoot.mkdirs()) {
         throw new IOException("Failed to create conf dir: " + confRoot);
       }
       try {
         // write it to file
         File confFile = new File(confRoot, configFileName);
         FileUtils.writeXmlFile(settingsDocument, confFile);
         saveSuccess = true;
       } catch (Exception e) {
         LOGGER.log(Level.SEVERE, e.getMessage());
         saveSuccess = false;
       }
     } catch (IOException ex) {
       LOGGER.log(Level.SEVERE, ex.getMessage());
       saveSuccess = false;
     }
     return saveSuccess;
   }
 
   public String getVersion() {
     return VERSION;
   }
 
   public static String decodeApkKey(String apkkey) {
     return new String(DatatypeConverter.parseBase64Binary(StringUtils.rot13(apkkey)));
   }
 
   public static String getApplicationProperty(String key) {
     return ResourceBundle.getBundle(Settings.class.getName(), Locale.ROOT).getString(key);
   }
 
   public static File getApplicationFolder() {
     String applicationDirPath = System.getProperty("application.dir");
     String userHome = System.getProperty("user.home");
     String userDir = System.getProperty("user.dir");
     File applicationFolder = null;
 
     if (applicationDirPath != null && applicationDirPath.length() > 0) {
       // use given path
       applicationFolder = new File(applicationDirPath);
     } else if (userHome != null) {
       // create folder in user home
       applicationFolder = new File(userHome, Platform.isWindows() ? appName_nospace : "." + appName_nospace);
     } else {
       // use working directory
       applicationFolder = new File(userDir);
     }
 
     // create folder if necessary
     if (!applicationFolder.exists()) {
       applicationFolder.mkdirs();
     }
 
     return applicationFolder;
   }
   private static boolean libzen = false;
   private static Boolean mediainfo = null;
 
   /**
    * Check if lib media info is installed
    *
    * @return True if lib media info is installed, otherwhise false
    */
   public static boolean libMediaInfo() {
     if (mediainfo != null) {
       return mediainfo;
     }
 
     boolean linux = Platform.isLinux();
     if (linux) {
       try {
         NativeLibrary.getInstance("zen");
         libzen = true;
       } catch (LinkageError e) {
         Settings.LOGGER.log(Level.WARNING, "Failed to preload libzen");
       }
     }
     if ((linux && libzen) || !linux) {
       try {
         MediaInfoLibrary.INSTANCE.New();
         mediainfo = Boolean.TRUE;
       } catch (LinkageError e) {
         mediainfo = Boolean.FALSE;
       }
     }
     if (mediainfo == null) {
       mediainfo = Boolean.FALSE;
     }
     return mediainfo.equals(Boolean.TRUE);
   }
 }
