 package gov.nih.nci.evs.browser.properties;
 
 import java.util.*;
 
 import org.apache.log4j.*;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction 
  * with the National Cancer Institute, and so to the extent government 
  * employees are co-authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  *   1. Redistributions of source code must retain the above copyright 
  *      notice, this list of conditions and the disclaimer of Article 3, 
  *      below. Redistributions in binary form must reproduce the above 
  *      copyright notice, this list of conditions and the following 
  *      disclaimer in the documentation and/or other materials provided 
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution, 
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National 
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must 
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not 
  *      authorize the recipient to use any trademarks owned by either NCI 
  *      or NGIT 
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED 
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE 
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team
  * @version 1.0
  * 
  *          Modification history Initial implementation kim.ong@ngc.com
  * 
  */
 
 public class NCItBrowserProperties {
     private static Logger _logger =
         Logger.getLogger(NCItBrowserProperties.class);
     private static List _displayItemList;
     private static List _metadataElementList;
     private static List _defSourceMappingList;
     private static HashMap _defSourceMappingHashMap;
     private static List _securityTokenList;
     private static HashMap _securityTokenHashMap;
     private static HashMap _configurableItemMap;
 
     // KLO
     public static final String DEBUG_ON = "DEBUG_ON";
     public static final String EVS_SERVICE_URL = "EVS_SERVICE_URL";
     public static final String LG_CONFIG_FILE = "LG_CONFIG_FILE";
     public static final String PING_LEXEVS_ENABLED = "PING_LEXEVS_ENABLED";
     public static final String PING_LEXEVS_INTERVAL = "PING_LEXEVS_INTERVAL";
     public static final String MAXIMUM_RETURN = "MAXIMUM_RETURN";
     public static final String EHCACHE_XML_PATHNAME = "EHCACHE_XML_PATHNAME";
     public static final String SORT_BY_SCORE = "SORT_BY_SCORE";
     public static final String MAIL_SMTP_SERVER = "MAIL_SMTP_SERVER";
     public static final String NCICB_CONTACT_URL = "NCICB_CONTACT_URL";
     public static final String MAXIMUM_TREE_LEVEL = "MAXIMUM_TREE_LEVEL";
     public static final String TERMINOLOGY_SUBSET_DOWNLOAD_URL =
         "TERMINOLOGY_SUBSET_DOWNLOAD_URL";
     public static final String NCIT_BUILD_INFO = "NCIT_BUILD_INFO";
     public static final String NCIT_APP_VERSION = "APPLICATION_VERSION";
     public static final String NCIT_APP_VERSION_DISPLAY = "APPLICATION_VERSION_DISPLAY";
     public static final String ANTHILL_BUILD_TAG_BUILT =
         "ANTHILL_BUILD_TAG_BUILT";
     public static final String NCIM_URL = "NCIM_URL";
     public static final String NCIT_URL = "NCIT_URL";
     public static final String TERM_SUGGESTION_APPLICATION_URL =
         "TERM_SUGGESTION_APPLICATION_URL";
     public static final String LICENSE_PAGE_OPTION = "LICENSE_PAGE_OPTION";
 
     public static final String PAGINATION_TIME_OUT = "PAGINATION_TIME_OUT";
     public static final String MINIMUM_SEARCH_STRING_LENGTH =
         "MINIMUM_SEARCH_STRING_LENGTH";
     public static final String SLIDING_WINDOW_HALF_WIDTH =
         "SLIDING_WINDOW_HALF_WIDTH";
     public static final String STANDARD_FTP_REPORT_URL = 
         "STANDARD_FTP_REPORT_URL";
     public static final String STANDARD_FTP_REPORT_INFO = 
         "STANDARD_FTP_REPORT_INFO";
     public static int STANDARD_FTP_REPORT_INFO_MAX = 20;
 
     private static NCItBrowserProperties _browserProperties = null;
     private static Properties _properties = new Properties();
 
     public static boolean _debugOn = false;
     private static int _maxToReturn = 1000;
     private static int _maxTreeLevel = 1000;
     private static String _service_url = null;
     private static String _lg_config_file = null;
 
     private static String _sort_by_score = null;
     private static String _mail_smtp_server = null;
     private static String _ncicb_contact_url = null;
     private static String _terminology_subset_download_url = null;
     private static String _term_suggestion_application_url = null;
 
     private static String _license_page_option = null;
     private static String _ncim_url = null;
     private static String _ncit_url = null;
     private static int _pagination_time_out = 4;
     private static int _minimum_search_string_length = 1;
 
     private static int _sliding_window_half_width = 5;
     private static String _standard_ftp_report_url = "";
     private static Vector<StandardFtpReportInfo> _standard_ftp_report_info_list = 
         new Vector<StandardFtpReportInfo>();
 
     /**
      * Private constructor for singleton pattern.
      */
     private NCItBrowserProperties() {
     }
 
     /**
      * Gets the single instance of NCItBrowserProperties.
      * 
      * @return single instance of NCItBrowserProperties
      * 
      * @throws Exception the exception
      */
     public static NCItBrowserProperties getInstance() throws Exception {
         if (_browserProperties == null) {
             synchronized (NCItBrowserProperties.class) {
 
                 if (_browserProperties == null) {
                     _browserProperties = new NCItBrowserProperties();
                     loadProperties();
 
                     _debugOn = Boolean.parseBoolean(getProperty(DEBUG_ON));
 
                     String max_str =
                         _browserProperties
                             .getProperty(_browserProperties.MAXIMUM_RETURN);
                     _maxToReturn = Integer.parseInt(max_str);
 
                     String max_tree_level_str =
                         _browserProperties
                             .getProperty(_browserProperties.MAXIMUM_TREE_LEVEL);
                     _maxTreeLevel = Integer.parseInt(max_tree_level_str);
 
                     _service_url =
                         _browserProperties
                             .getProperty(_browserProperties.EVS_SERVICE_URL);
                     // _logger.info("EVS_SERVICE_URL: " + service_url);
 
                     _lg_config_file =
                         _browserProperties
                             .getProperty(_browserProperties.LG_CONFIG_FILE);
                     // _logger.info("LG_CONFIG_FILE: " + lg_config_file);
 
                     _sort_by_score =
                         _browserProperties
                             .getProperty(_browserProperties.SORT_BY_SCORE);
                     _ncicb_contact_url =
                         _browserProperties
                             .getProperty(_browserProperties.NCICB_CONTACT_URL);
                     _mail_smtp_server =
                         _browserProperties
                             .getProperty(_browserProperties.MAIL_SMTP_SERVER);
                     _terminology_subset_download_url =
                         _browserProperties
                             .getProperty(_browserProperties.TERMINOLOGY_SUBSET_DOWNLOAD_URL);
                     _term_suggestion_application_url =
                         _browserProperties
                             .getProperty(_browserProperties.TERM_SUGGESTION_APPLICATION_URL);
                     _license_page_option =
                         _browserProperties
                             .getProperty(_browserProperties.LICENSE_PAGE_OPTION);
                     _ncim_url =
                         _browserProperties
                             .getProperty(_browserProperties.NCIM_URL);
                     _ncit_url =
                         _browserProperties
                             .getProperty(_browserProperties.NCIT_URL);
 
                     String pagination_time_out_str =
                         _browserProperties
                             .getProperty(_browserProperties.PAGINATION_TIME_OUT);
                     if (pagination_time_out_str != null) {
                         _pagination_time_out =
                             Integer.parseInt(pagination_time_out_str);
                     }
 
                     String minimum_search_string_length_str =
                         _browserProperties
                             .getProperty(_browserProperties.MINIMUM_SEARCH_STRING_LENGTH);
                     if (minimum_search_string_length_str != null) {
                         int min_search_string_length =
                             Integer.parseInt(minimum_search_string_length_str);
                         if (min_search_string_length > 1) {
                             _minimum_search_string_length =
                                 min_search_string_length;
                         }
                     }
                     String sliding_window_half_width_str =
                         _browserProperties
                             .getProperty(_browserProperties.SLIDING_WINDOW_HALF_WIDTH);
                     if (sliding_window_half_width_str != null) {
                         int sliding_window_halfwidth =
                             Integer.parseInt(sliding_window_half_width_str);
                         if (sliding_window_halfwidth > 1) {
                             _sliding_window_half_width =
                                 sliding_window_halfwidth;
                         }
                     }
                     _standard_ftp_report_url = getProperty(STANDARD_FTP_REPORT_URL);
                     _standard_ftp_report_info_list = StandardFtpReportInfo.parse(
                         STANDARD_FTP_REPORT_INFO, STANDARD_FTP_REPORT_INFO_MAX);
                 }
             }
         }
 
         return _browserProperties;
     }
 
     // public String getProperty(String key) throws Exception{
     public static String getProperty(String key) throws Exception {
         // return properties.getProperty(key);
         String ret_str = (String) _configurableItemMap.get(key);
         if (ret_str == null)
             return null;
         if (ret_str.compareToIgnoreCase("null") == 0)
             return null;
         return ret_str;
     }
 
    public static String getStringProperty(String key, String defaultValue) {
        try {
            getInstance();  // Initializes this singleton class
            String value = getProperty(key);
            if (value == null || value.trim().length() <= 0)
                throw new Exception("Property " + key + " is not set.");
            return value;
        } catch (Exception e) {
            _logger.warn("Defaulting " + key + " property to " + defaultValue + ".");
            e.printStackTrace();
            return defaultValue;
        }
    }

     public static int getIntProperty(String key, int defaultValue) {
         try {
             getInstance();  // Initializes this singleton class
             String value = getProperty(key);
             if (value == null || value.trim().length() <= 0)
                 throw new Exception("Property " + key + " is not set.");
             return Integer.parseInt(value);
         } catch (Exception e) {
             _logger.warn("Defaulting " + key + " property to " + defaultValue + ".");
             e.printStackTrace();
             return defaultValue;
         }
     }
     
     public static boolean getBooleanProperty(String key, boolean defaultValue) {
         try {
             getInstance();  // Initializes this singleton class
             String value = getProperty(key);
             if (value == null || value.trim().length() <= 0)
                 throw new Exception("Property " + key + " is not set.");
             return Boolean.parseBoolean(value);
         } catch (Exception e) {
             _logger.warn("Defaulting " + key + " property to " + defaultValue + ".");
             e.printStackTrace();
             return defaultValue;
         }
     }
 
     public static List getDisplayItemList() {
         return _displayItemList;
     }
 
     public static List getMetadataElementList() {
         return _metadataElementList;
     }
 
     public static List getDefSourceMappingList() {
         return _defSourceMappingList;
     }
 
     public static HashMap getDefSourceMappingHashMap() {
         return _defSourceMappingHashMap;
     }
 
     public static List getSecurityTokenList() {
         return _securityTokenList;
     }
 
     public static HashMap getSecurityTokenHashMap() {
         return _securityTokenHashMap;
     }
 
     private static void loadProperties() throws Exception {
         String propertyFile =
             System.getProperty("gov.nih.nci.evs.browser.NCItBrowserProperties");
         _logger.info("NCItBrowserProperties File Location= " + propertyFile);
         PropertyFileParser parser = new PropertyFileParser(propertyFile);
         parser.run();
 
         _displayItemList = parser.getDisplayItemList();
         _metadataElementList = parser.getMetadataElementList();
         _defSourceMappingList = parser.getDefSourceMappingList();
         _defSourceMappingHashMap = parser.getDefSourceMappingHashMap();
         _securityTokenList = parser.getSecurityTokenList();
         _securityTokenHashMap = parser.getSecurityTokenHashMap();
 
         _configurableItemMap = parser.getConfigurableItemMap();
 
     }
 
     public static String getLicensePageOption() {
         return _license_page_option;
     }
 
     public static String getNCIM_URL() {
         return _ncim_url;
     }
 
     public static String getNCIT_URL() {
         return _ncit_url;
     }
 
     public static int getPaginationTimeOut() {
         return _pagination_time_out;
     }
 
     public static int getMinimumSearchStringLength() {
         return _minimum_search_string_length;
     }
 
     public static int getSlidingWindowHalfWidth() {
         return _sliding_window_half_width;
     }
 
     public static String getStandardFtpReportUrl() {
         return _standard_ftp_report_url;
     }
 
     public static Vector<StandardFtpReportInfo> getStandardFtpReportInfoList() {
         return _standard_ftp_report_info_list;
     }
 }
