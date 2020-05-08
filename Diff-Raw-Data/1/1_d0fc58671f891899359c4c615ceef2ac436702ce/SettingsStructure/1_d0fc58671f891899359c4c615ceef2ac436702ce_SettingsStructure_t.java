 package br.org.indt.ndg.mobile.settings;
 
 import br.org.indt.ndg.lwuit.extended.DateField;
 import br.org.indt.ndg.mobile.AppMIDlet;
 import java.io.PrintStream;
 
 import br.org.indt.ndg.mobile.Utils;
 import br.org.indt.ndg.mobile.settings.PhotoSettings.PhotoResolution;
 import br.org.indt.ndg.mobile.structures.Language;
 import java.util.Vector;
 
 /**
  * READ FIRST!
  * To add a new setting You need to perform 5 steps:
  * 1) Implement Setter and Getter for new setting
  * 2) Add default setting value constant
  * 3) Apply the constant to initial value of new setting
  * 4) Add setting default value to createDefaultSettings method
  * 5) Update SettingsHandler
  * @author tomasz.baniak
  */
 
 public class SettingsStructure {
 
     public static final int NOT_REGISTERED = 0;
     public static final int REGISTERED = 1;
 
     /* Default values */
     private static final boolean DEFAULT_USE_COMPRESSION = true;
     private static final int DEFAULT_SPLASH_TIME = 8000;
     private static final int DEFAULT_IS_REGISTERED = NOT_REGISTERED;
     private static final boolean DEFAULT_GPS = true;
     private static final boolean DEFAULT_GEO_TAGGING = true;
     private static final int DEFAULT_PHOTO_RESULUTION_ID = 0;
     private static final int DEFAULT_STYLE_ID = 0;
     private static final boolean DEFAULT_LOG_SUPPORT = false;
     private static final int DEFAULT_DATE_FORMAT_ID = DateField.DDMMYYYY;
     private static final boolean DEFAULT_ENCRYPTION = false;
     private static final int DEFAULT_ENCRIPTION_CONFIGURED = 0;
     private static final String DEFAULT_LANGUAGE_NAME = "Default (English)";
     private static final String DEFAULT_LANGUAGE_LOCALE = "en-GB";
 
     private String server_normal_url;
     private String server_compress_url;
     private String localization_serving_url;
     private String server_results_openrosa_url;
     private String receive_survey_url;
     private String update_check_url;
     private String register_imei_url;
     private String language_list_url;
     private boolean compress_state = DEFAULT_USE_COMPRESSION;
 
     private int splash_time = DEFAULT_SPLASH_TIME;
     private int isRegistered_flag = DEFAULT_IS_REGISTERED;
 
     private boolean gps_configured = DEFAULT_GPS;
     private boolean geoTagging_configured = DEFAULT_GEO_TAGGING;
 
     private int selectedResolution = DEFAULT_PHOTO_RESULUTION_ID;
     private int selectedStyle = DEFAULT_STYLE_ID;
 
     private boolean logSupport = DEFAULT_LOG_SUPPORT;
     private int dateFormatId = DEFAULT_DATE_FORMAT_ID;
 
     private int encryptionConfigured = DEFAULT_ENCRIPTION_CONFIGURED;
     private boolean encryption = DEFAULT_ENCRYPTION;
 
     private String language = DEFAULT_LANGUAGE_NAME;
     private String appVersion;
     private Language defaultLanguage = new Language(DEFAULT_LANGUAGE_NAME, DEFAULT_LANGUAGE_LOCALE);
 
     private Vector languages = new Vector();
 
 
 
     public SettingsStructure() {
         initializeDefaultRuntimeSettings();
     }
 
     private void initializeDefaultRuntimeSettings() {
         String defaultServerUrl = AppMIDlet.getInstance().getDefaultServerUrl();
         String[] defaultServlets = AppMIDlet.getInstance().getDefaultServlets();
 
         server_normal_url = defaultServerUrl + defaultServlets[0] + defaultServlets[1];
         server_compress_url = defaultServerUrl + defaultServlets[0] + defaultServlets[1];
         localization_serving_url = defaultServerUrl + defaultServlets[0] + defaultServlets[6];
         language_list_url = defaultServerUrl + defaultServlets[0] + defaultServlets[7];
         server_results_openrosa_url = defaultServerUrl + defaultServlets[0] + defaultServlets[5];
         receive_survey_url = defaultServerUrl + defaultServlets[0] + defaultServlets[2];
         update_check_url = defaultServerUrl + defaultServlets[0] + defaultServlets[3];
         register_imei_url = defaultServerUrl + defaultServlets[0] + defaultServlets[4];
         appVersion = AppMIDlet.getInstance().getAppVersion();
         languages.addElement(defaultLanguage);
     }
 
     public void createDefaultSettings(PrintStream _out) {
         String defaultServerUrl = AppMIDlet.getInstance().getDefaultServerUrl();
         String[] defaultServlets = AppMIDlet.getInstance().getDefaultServlets();
 
         // Reset to default values
         setLanguage(defaultLanguage.getLocale());
         setRegisteredFlag(DEFAULT_IS_REGISTERED);
         setSplashTime(DEFAULT_SPLASH_TIME);
         setGpsConfigured(DEFAULT_GPS);
         setGeoTaggingConfigured(DEFAULT_GEO_TAGGING);
         setPhotoResolutionId(DEFAULT_PHOTO_RESULUTION_ID);
         setStyleId(DEFAULT_STYLE_ID);
         setLogSupport(DEFAULT_LOG_SUPPORT);
         setServerCompression(DEFAULT_USE_COMPRESSION);
         setDateFormatId(DEFAULT_DATE_FORMAT_ID);
         setEncryptionConfigured(DEFAULT_ENCRIPTION_CONFIGURED);
         setEncryption(DEFAULT_ENCRYPTION);
         setServerUrl_Compress(defaultServerUrl + defaultServlets[0] + defaultServlets[1]);
         setServerUrl_Normal(defaultServerUrl + defaultServlets[0] + defaultServlets[1]);
         setServerUrl_ResultsOpenRosa(defaultServerUrl + defaultServlets[0] + defaultServlets[5]);
         setReceiveSurveyURL(defaultServerUrl + defaultServlets[0] + defaultServlets[2]);
         setUpdateCheckURL(defaultServerUrl + defaultServlets[0] + defaultServlets[3]);
         setRegisterIMEIUrl(defaultServerUrl + defaultServlets[0] + defaultServlets[4]);
         setLocalizationServingURL(defaultServerUrl + defaultServlets[0] + defaultServlets[6]);
         setLanguageListURL(defaultServerUrl + defaultServlets[0] + defaultServlets[7]);
         setAppVersion(AppMIDlet.getInstance().getAppVersion());
 
         saveSettings(_out);
     }
 
     public void saveSettings(PrintStream _out) {
         _out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 
         _out.print("<settings");
         _out.print(" registered=\"" + getRegisteredFlag() + "\"");
         _out.print(" splash=\"" + getSplashTime() + "\"");
         _out.print(" language=\"" + getLanguage() + "\"");
         _out.println(" showEncryptionScreen=\""+ (isEncryptionConfigured() ? "1" : "0") + "\">");
 
         writeGpsSettings(_out);
         writeGeoTaggingSettings(_out);
         writePhotoResolutionSettings(_out);
         writeStyleSettings(_out);
         writeLogSettings(_out);
         writeServerSettings(_out);
         writeLanguageSettings(_out);
         writeVersionSettings(_out);
         writeDateFormatSettings(_out);
         writeEncryption(_out);
 
         _out.println("</settings>");
     }
 
     public void writeServerSettings(PrintStream _out) {
         _out.print("<server compression=\"");
         if (compress_state) _out.println("on\">");
         else _out.println("off\">");
 
         _out.println("<url_compress>" + server_compress_url + "</url_compress>");
         _out.println("<url_normal>" + server_normal_url + "</url_normal>");
         _out.println("<url_results_openrosa>" + server_results_openrosa_url + "</url_results_openrosa>");
         _out.println("<url_receive_survey>" + receive_survey_url + "</url_receive_survey>");
         _out.println("<url_update_check>" + update_check_url + "</url_update_check>");
         _out.println("<url_register_imei>" + register_imei_url + "</url_register_imei>");
         _out.println("<url_localization_serving>" + localization_serving_url + "</url_localization_serving>");
         _out.println("<url_language_list>" + language_list_url + "</url_language_list>");
 
         _out.println("</server>");
     }
 
     private void writeLanguageSettings(PrintStream _out) {
         
         if(languages != null)
         {
             _out.println("<languages>");
             StringBuffer languageString = null;
             for(int i = 0 ; i < languages.size(); i++)
             {
                 languageString = new StringBuffer();
                 languageString.append("<language name=\"").append( ((Language)languages.elementAt(i)).getLangName());
                 languageString.append("\" locale= \"").append(((Language)languages.elementAt(i)).getLocale()).append("\"/>");
                 _out.println(languageString);
             }
             _out.println("</languages>");
         }
         
     }
 
     public void writeGpsSettings(PrintStream _out) {
         _out.print("<gps configured=\"");
         if (gps_configured) _out.println("yes\"/>");
         else _out.println("no\"/>");
     }
 
     public void writeGeoTaggingSettings(PrintStream _out) {
         _out.print("<geotagging configured=\"");
         if (geoTagging_configured) _out.println("yes\"/>");
         else _out.println("no\"/>");
     }
 
     void writeLogSettings(PrintStream _out) {
         String strLogSupport = logSupport ? "yes" : "no";
         _out.println("<log active=\"" + strLogSupport + "\"" + "/>");
     }
 
     void writeVersionSettings(PrintStream _out) {
         _out.println("<version application=\"" + appVersion + "\"/>");
     }
 
     void writePhotoResolutionSettings(PrintStream output) {
         output.print("<photoResolution configId=\"");
         output.print( String.valueOf(selectedResolution) );
         output.println( "\"/>" );
     }
 
     void writeStyleSettings(PrintStream output) {
         output.print("<style id=\"");
         output.print( String.valueOf(selectedStyle) );
         output.println( "\"/>" );
     }
 
     void writeDateFormatSettings(PrintStream output){
         output.print("<dateFormat id=\"");
         output.print( String.valueOf(dateFormatId) );
         output.println( "\"/>" );
     }
 
     public void writeEncryption(PrintStream _out) {
         _out.print("<encryption enabled=\"");
         if (encryption) _out.println("yes\"/>");
         else _out.println("no\"/>");
     }
 
     void setLogSupport(boolean _logSupport) {
         logSupport = _logSupport;
     }
     public boolean getLogSupport(){
         return logSupport;
     }
 
     public void setLanguage(String _lang) {
         language = _lang;
     }
     public String getLanguage() {
         if(language == null || language.equals("")){
             language = defaultLanguage.getLocale();
         }
         return language;
     }
 
     void setAppVersion(String _ver) {
         appVersion = _ver;
     }
     public String getAppVersion() {
         return appVersion;
     }
 
     void setUpdateCheckURL(String _url) {
         update_check_url = _url;
     }
     public String getUpdateCheckURL() {
         return update_check_url;
     }
 
     public void setServerCompression(boolean _state) {
         compress_state = _state;
     }
     public boolean getServerCompression() {
         return compress_state;
     }
 
     public void setServerUrl_Compress(String _url) {
         server_compress_url = _url;
     }
     public void setServerUrl_Normal(String _url) {
         server_normal_url = _url;
     }
 
     public void setServerUrl_ResultsOpenRosa(String _url) {
         server_results_openrosa_url = _url;
     }
 
     public String getDateFormatString(){
         //TODO format to string;
        return "0";
     }
 
     public int getDateFormatId(){
         return dateFormatId;
     }
 
     public void setDateFormatId(int _id){
         dateFormatId = _id;
     }
 
     public String getServerUrl( int surveyFormat ) {
         String result = null;
         switch (surveyFormat) {
             case Utils.NDG_FORMAT:
                 if ( compress_state )
                     result = server_compress_url;
                 else
                     result = server_normal_url;
                 break;
             case Utils.OPEN_ROSA_FORMAT:
                 result = server_results_openrosa_url;
                 break;
             default:
                 throw new RuntimeException("Unsupported Survey Format");
         }
         return result;
     }
 
     public void setReceiveSurveyURL(String url){
         receive_survey_url = url;
     }
 
     public String getReceiveSurveyURL(){
         return receive_survey_url;
     }
 
     public String getLocalizationServingURL()
     {
         return localization_serving_url;
     }
 
     public void setLocalizationServingURL(String url){
         localization_serving_url = url;
     }
 
     public String getLanguageListURL()
     {
         return language_list_url;
     }
 
     public void setLanguageListURL(String url)
     {
         language_list_url = url;
     }
 
     public String getRegisterIMEIUrl() {
         return register_imei_url;
     }
 
     public void setRegisterIMEIUrl(String url) {
         this.register_imei_url = url;
     }
 
     public void setRegisteredFlag(int _flag) {
         isRegistered_flag = _flag;
     }
 
     public int getRegisteredFlag() {
         return isRegistered_flag;
     }
 
     public void setSplashTime(int _time) {
         splash_time = _time;
     }
 
     public int getSplashTime() {
         return splash_time;
     }
 
     public void setGpsConfigured(boolean _state) {
         gps_configured = _state;
     }
     public boolean getGpsConfigured() {
         return gps_configured;
     }
 
     public void setGeoTaggingConfigured(boolean _state) {
         geoTagging_configured = _state;
     }
     public boolean getGeoTaggingConfigured() {
         return geoTagging_configured;
     }
 
     public void setPhotoResolutionId(int _resConf ) {
         selectedResolution = _resConf;
     }
     public int getPhotoResolutionId() {
         return selectedResolution;
     }
 
     public void setStyleId(int styleId ) {
         selectedStyle = styleId;
     }
     public int getStyleId() {
         return selectedStyle;
     }
 
     public PhotoResolution getPhotoResolution() {
         return PhotoSettings.getInstance().getPhotoResolution( selectedResolution );
     }
 
     public String[] getResolutionList() {
         return PhotoSettings.getInstance().getResolutionList();
     }
 
     public boolean isEncryptionConfigured() {
         return encryptionConfigured== 1 ? true : false;
     }
 
     public void setEncryptionConfigured(int encryption) {
         encryptionConfigured = encryption;
     }
 
     public boolean getEncryption() {
         return encryption;
     }
 
     public void setEncryption(boolean encrypt) {
         encryption = encrypt;
     }
 
 
     public Vector getLanguages() {
         return languages;
     }
 
     public void setLanguages(Vector languages) {
         this.languages = languages;
     }
 
     public Language getDefaultLanguage(){
         return defaultLanguage;
     }
 }
