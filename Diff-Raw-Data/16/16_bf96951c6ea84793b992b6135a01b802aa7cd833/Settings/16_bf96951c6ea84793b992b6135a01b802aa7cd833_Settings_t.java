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
 
 import fr.free.movierenamer.mediainfo.MediaInfoLibrary;
 import fr.free.movierenamer.scrapper.MovieScrapper;
 import fr.free.movierenamer.scrapper.SubtitleScrapper;
 import fr.free.movierenamer.scrapper.TvShowScrapper;
 import fr.free.movierenamer.scrapper.impl.IMDbScrapper;
 import fr.free.movierenamer.scrapper.impl.SubsceneSubtitleScrapper;
 import fr.free.movierenamer.scrapper.impl.TheTVDBScrapper;
 import fr.free.movierenamer.utils.FileUtils;
 import fr.free.movierenamer.utils.StringUtils;
 import fr.free.movierenamer.utils.URIRequest;
 import fr.free.movierenamer.utils.XPathUtils;
 
 /**
  * Class Settings , Movie Renamer settings <br>
  * Only public and non static attributes are written in conf file !
 *
  * @author Nicolas Magré
  * @author Simon QUÉMÉNEUR
  */
 public final class Settings {
 
   static {
     APPNAME = getApplicationProperty("application.name");
     APPMODULE = getApplicationProperty("application.module.name");
     VERSION = getApplicationProperty("application.module.version");
     appName_nospace = getApplicationProperty("application.name").replace(' ', '_');
     appModule_nospace = getApplicationProperty("application.module.name").replace(' ', '_');
     appFolder = getApplicationFolder();
   }
 
   public static final String APPNAME;
   public static final String APPMODULE;
   public static final String VERSION;
   public static final File appFolder;
   private static final String appName_nospace;
   private static final String appModule_nospace;
 
   // files
   private static final String configFileName = appModule_nospace + ".conf";
   private static final String logFileName = appModule_nospace + ".log";
 
   // Logger
   public static final Logger LOGGER = Logger.getLogger(appModule_nospace + " Logger");
 
   // Settings instance
   private static Settings instance;
 
   // Settings xml conf instance
   private final Document settingsDocument;
   private final Node settingsNode;
 
   private boolean autosave = true;
 
   private static final String appSettingsNodeName = appName_nospace + "_" + appModule_nospace;
   private static final String settingNodeName = "settings";
 
   public static enum SettingsProperty {
     // app lang
     appLanguage, // (Locale.ENGLISH.toString()),
     // movie filename
     movieFilenameFormat, // ("<t> (<y>)"),
     movieFilenameSeparator, // (", "),
     movieFilenameLimit, // (Integer.decode("3").toString()),
     movieFilenameCase, // (StringUtils.CaseConversionType.FIRSTLA.name()),
     movieFilenameTrim, // (Boolean.TRUE.toString()),
     movieFilenameRmDupSpace, // (Boolean.TRUE.toString()),
     movieFilenameCreateDirectory, // (Boolean.FALSE.toString()),
     // movie folder
     movieFolderFormat, // ("<t> (<y>)"),
     movieFolderSeparator, // (", "),
     movieFolderLimit, // (Integer.decode("3").toString()),
     movieFolderCase, // (""),
     movieFolderTrim, // (Boolean.TRUE.toString()),
     movieFolderRmDupSpace, // (Boolean.TRUE.toString()),
     // tvShow
     tvShowFilenameFormat, // ("<st> S<s>E<e> <et>"),
     tvShowFilenameSeparator, // (", "),
     tvShowFilenameLimit, // (Integer.decode("3").toString()),
     tvShowFilenameCase, // (""),
     tvShowFilenameTrim, // (Boolean.TRUE.toString()),
     tvShowFilenameRmDupSpace, // (Boolean.TRUE.toString()),
     // Cache
     cacheClear, // (Boolean.FALSE.toString()),
     // Search
     searchMovieScrapper, // (IMDbScrapper.class.toString()),
     searchTvshowScrapper, // (TheTVDBScrapper.class.toString()),
     searchSubtitleScrapper, // (IMDbScrapper.class.toString()),
     searchScrapperLang, // (Locale.ENGLISH.toString()),
     searchSortBySimiYear, // (Boolean.TRUE.toString()),
     searchNbResult, // (Integer.decode("2").toString()),
     searchDisplayApproximateResult, // (Boolean.FALSE.toString()),
     // Proxy
     proxyIsOn, // (Boolean.FALSE.toString()),
     proxyUrl, // (""), // "10.2.1.10"
     proxyPort, // (Integer.decode("0").toString()), // 3128
     // http param
     httpRequestTimeOut, // (Integer.decode("30").toString()),
     httpCustomUserAgent; // Mozilla/5.0 (Windows NT 5.1; rv:10.0.2) Gecko/20100101 Firefox/10.0.2
   }
 
   /**
    * Private build for singleton fix
   *
    * @return
    */
   private static synchronized Settings newInstance() {
     if (instance == null) {
       instance = new Settings();
     }
     return instance;
   }
 
   /**
    * Access to the Settings instance
   *
    * @return The only instance of MR Settings
    */
   public static synchronized Settings getInstance() {
     if (instance == null) {
       instance = newInstance();
     }
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
     saveSetting();
   }
 
   private String get(SettingsProperty key) {
     String value;
     if (key != null) {
       Node found = XPathUtils.selectNode(key.name(), this.settingsNode);
       if (found != null) {
         value = XPathUtils.getTextContent(found);
       } else {
         value = null;
       }
     } else {
       value = null;
     }
     if (value == null) {
       throw new NullPointerException("Setting property is null");
     }
     return value;
   }
 
   public void set(SettingsProperty key, Object value) {
     if (value != null && key != null) {
       Node found = XPathUtils.selectNode(key.name(), this.settingsNode);
       if (found == null) {
         found = settingsDocument.createElement(key.name());
         // param.appendChild(settingsDocument.createTextNode(value.toString()));
         this.settingsNode.appendChild(found);
       }
       found.setTextContent(value.toString());
       saveSetting();
     }
   }
 
   public void clear() {
     Logger.getLogger(Settings.class.getName()).log(Level.INFO, String.format("Clear Settings"));
     NodeList list = this.settingsNode.getChildNodes();
     for (int i = 0; i < list.getLength(); i++) {
       this.settingsNode.removeChild(list.item(i));
     }
     saveSetting();
   }
 
   public Locale getAppLanguage() {
     try {
       return new Locale(get(SettingsProperty.appLanguage));
     } catch (Exception e) {
       return Locale.ENGLISH;
     }
   }
 
   public String getMovieFilenameFormat() {
     try {
       return get(SettingsProperty.movieFilenameFormat);
     } catch (Exception ex) {
       return "<t> (<y>)";
     }
   }
 
   public String getMovieFilenameSeparator() {
     try {
       return get(SettingsProperty.movieFilenameSeparator);
     } catch (Exception ex) {
       return ", ";
     }
   }
 
   public int getMovieFilenameLimit() {
     try {
       return Integer.parseInt(get(SettingsProperty.movieFilenameLimit));
     } catch (Exception e) {
       return 3;
     }
   }
 
   public StringUtils.CaseConversionType getMovieFilenameCase() {
     try {
       return StringUtils.CaseConversionType.valueOf(get(SettingsProperty.movieFilenameCase));
     } catch (Exception e) {
       return StringUtils.CaseConversionType.FIRSTLA;
     }
   }
 
   public boolean isMovieFilenameTrim() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.movieFilenameTrim));
     } catch (Exception ex) {
       return Boolean.TRUE;
     }
   }
 
   public boolean isMovieFilenameRmDupSpace() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.movieFilenameRmDupSpace));
     } catch (Exception ex) {
       return Boolean.TRUE;
     }
   }
 
   public boolean isMovieFilenameCreateDirectory() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.movieFilenameCreateDirectory));
     } catch (Exception ex) {
       return Boolean.FALSE;
     }
   }
 
   public String getMovieFolderFormat() {
     try {
       return get(SettingsProperty.movieFolderFormat);
     } catch (Exception ex) {
       return "<t> (<y>)";
     }
   }
 
   public String getMovieFolderSeparator() {
     try {
       return get(SettingsProperty.movieFolderSeparator);
     } catch (Exception ex) {
       return ", ";
     }
   }
 
   public int getMovieFolderLimit() {
     try {
       return Integer.parseInt(get(SettingsProperty.movieFolderLimit));
     } catch (Exception ex) {
       return 3;
     }
   }
 
   public String getMovieFolderCase() {
     try {
       return get(SettingsProperty.movieFolderCase);
     } catch (Exception ex) {
       return "";
     }
   }
 
   public boolean isMovieFolderTrim() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.movieFolderTrim));
     } catch (Exception ex) {
       return Boolean.TRUE;
     }
   }
 
   public boolean isMovieFolderRmDupSpace() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.movieFolderRmDupSpace));
     } catch (Exception ex) {
       return Boolean.TRUE;
     }
   }
 
   public String getTvShowFilenameFormat() {
     try {
       return get(SettingsProperty.tvShowFilenameFormat);
     } catch (Exception ex) {
       return "<st> S<s>E<e> <et>";
     }
   }
 
   public String getTvShowFilenameSeparator() {
     try {
       return get(SettingsProperty.tvShowFilenameSeparator);
     } catch (Exception ex) {
       return ", ";
     }
   }
 
   public int getTvShowFilenameLimit() {
     try {
       return Integer.parseInt(get(SettingsProperty.tvShowFilenameLimit));
     } catch (Exception ex) {
       return 3;
     }
   }
 
   public String getTvShowFilenameCase() {
     try {
       return get(SettingsProperty.tvShowFilenameCase);
     } catch (Exception ex) {
       return "";
     }
   }
 
   public boolean isTvShowFilenameTrim() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.tvShowFilenameTrim));
     } catch (Exception ex) {
       return Boolean.TRUE;
     }
   }
 
   public boolean isTvShowFilenameRmDupSpace() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.tvShowFilenameRmDupSpace));
     } catch (Exception ex) {
       return Boolean.TRUE;
     }
   }
 
   public boolean isCacheClear() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.cacheClear));
     } catch (Exception ex) {
       return Boolean.FALSE;
     }
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
     try {
       return new Locale(get(SettingsProperty.searchScrapperLang));
     } catch (Exception ex) {
       return Locale.ENGLISH;
     }
   }
 
   public boolean isSearchSortBySimiYear() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.searchSortBySimiYear));
     } catch (Exception ex) {
       return Boolean.TRUE;
     }
   }
 
   public int getSearchNbResult() {
     try {
       return Integer.parseInt(get(SettingsProperty.searchNbResult));
     } catch (Exception ex) {
       return 2;
     }
   }
 
   public boolean isSearchDisplayApproximateResult() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.searchDisplayApproximateResult));
     } catch (Exception ex) {
       return Boolean.FALSE;
     }
   }
 
   public boolean isProxyIsOn() {
     try {
       return Boolean.parseBoolean(get(SettingsProperty.proxyIsOn));
     } catch (Exception ex) {
       return Boolean.FALSE;
     }
   }
 
   public String getProxyUrl() {
     try {
       return get(SettingsProperty.proxyUrl);
     } catch (Exception ex) {
       return ""; // ex. "10.2.1.10"
     }
   }
 
   public int getProxyPort() {
     try {
       return Integer.parseInt(get(SettingsProperty.proxyPort));
     } catch (Exception ex) {
       return 0; // ex. 3128
     }
   }
 
   public int getHttpRequestTimeOut() {
     try {
       return Integer.parseInt(get(SettingsProperty.httpRequestTimeOut));
     } catch (Exception ex) {
       return 30;
     }
   }
 
   public String getHttpCustomUserAgent() {
     try {
       return get(SettingsProperty.httpCustomUserAgent);
     } catch (Exception ex) {
       return "";// ex. // Mozilla/5.0 (Windows NT 5.1; rv:10.0.2) Gecko/20100101 Firefox/10.0.2
     }
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
   private boolean saveSetting() {
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
 
   private static File getApplicationFolder() {
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
    if(mediainfo == null) mediainfo = Boolean.FALSE;
     return mediainfo.equals(Boolean.TRUE);
   }
 
 }
