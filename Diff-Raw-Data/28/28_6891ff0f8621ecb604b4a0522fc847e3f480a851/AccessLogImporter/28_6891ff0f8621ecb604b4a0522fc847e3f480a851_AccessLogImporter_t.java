 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.util;
 
 import com.mne.advertmanager.model.AccessLog;
 import com.mne.advertmanager.model.AccessSource;
 import com.mne.advertmanager.model.AffProgram;
 import com.mne.advertmanager.service.AccessLogService;
 import com.mne.advertmanager.service.AccessSourceService;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Nina Eidelshtein and Misha Lebedev
  */
 public class AccessLogImporter implements BillingDataImporter {
 
     //crate logger
     private static final Logger logger = LoggerFactory.getLogger(BillingDataImporter.class);
     private AccessLogService accessLogService;
     private AccessSourceService accessSourceService;
     
     private DateFormat df = null;
 
     /**
      * C-tor: set text encoding detector and data format
      */
     public AccessLogImporter() {
         df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
     }
 
     //seter used by Spring context injection
     public void setAccessLogService(AccessLogService accessLogService) {
         this.accessLogService = accessLogService;
     }
     //seter used by Spring context injection
     public void setAccessSourceService(AccessSourceService accessSourceService) {
         this.accessSourceService = accessSourceService;
     }
 
 //======================== importDataItemProperty ==============================
 //this function fill target(AccessLog obj) with given data(itemName:itemValue)
     @Override
     public Object importDataItemProperty(Object target, String itemName, String itemValue) {
 
         AccessLog access = (AccessLog) target;
 
         if (("DateTime").equals(itemName)) {
             access.setAccessTime(processDate(itemValue));
         } else if (("IP").equals(itemName)) {
             processIPAddress(access,itemValue);
         } else if (("Referer").equals(itemName)) {
             processReferer(access, itemValue);
         } else if (("Target").equals(itemName)) {
             access.setTargetURL(itemValue);
         }
 
         return target;
     }
 //================================== processReferer ============================
 
     private String decodeQuery(URL url) {
         //try to convert given access data (link string) to something readable,
         //by guessing proper encoding of link's URL
 
         String charSet = "UTF-8";
         String query = url.getQuery();
         String result = null;
         if (query != null) {
             try {
                 int queryTextFirstIdx = Math.max(0, query.indexOf("=")+1);
                 int queryTextLastIdx = query.indexOf("&");
                 if (queryTextLastIdx < 0)
                     queryTextLastIdx = query.length();
                 
                 String term = query.substring(queryTextFirstIdx, queryTextLastIdx);
                 
                 result = URLDecoder.decode(term, charSet);
                 String encoded = URLEncoder.encode(result, charSet);
                 encoded = encoded.replaceAll("\\+", "%20");
                 term = term.replaceAll("\\+", "%20");
                 
                 if (!term.equals(encoded)) {
                     charSet = "windows-1251";
                     result = URLDecoder.decode(term, charSet);
                     logger.debug("Term={}:::Encoded={}",term,encoded);
                 }
                 logger.debug("detectedCharset:{}, result:{}", charSet, result);
             } catch (UnsupportedEncodingException ex) {
                 logger.error(ex.getMessage());
             }
         }
         return result;
     }
 
     /**
      * This function try to find domain name of given access. if domain name(acessSource) not exist in DB, then create new one and store it in DB. then the function try to guess
      * link(URL) encoding format, and convert it to this format if proper format found.
      */
     private void processReferer(AccessLog access, String itemValue) {
         //guess encoding and normalize location
         URL url = buildURL(itemValue);
 
         if (url != null) {
             String query = decodeQuery(url);
             //find accessSource (domain) of given access in DB.
             AccessSource referer = findAccessSource(url);
             //set update access sourceDomainId property with one of referer.
             access.setSourceDomainId(referer);
             access.setQuery(query);
             access.setRefererURL(url.toString());
         }
     }
 
     private AccessSource findAccessSource(URL url) {
 
         AccessSource result = null;
         String host = url.getHost();
 
         logger.debug("Looking up accessSource by host {}", host);
         result = accessSourceService.findByDomain(url.getHost());
 
         //if this access source dosen't exist in DB, then create one. 
         if (result == null) {
             result = new AccessSource(-1, host, "imported domain");
             accessSourceService.create(result);
         }
         return result;
     }
 
     private URL buildURL(String itemValue) {
         URL result = null;
         try {
             result = new URL(itemValue);
         } catch (Exception e) {
             logger.error(e.toString());
         }
         return result;
     }
 //========================= saveDataItem =======================================
 
     @Override
     public void saveDataItem(AffProgram program, Object dataItem) {
         AccessLog access = (AccessLog) dataItem;
         access.setAffProgram(program);
         accessLogService.createAccessLog(access);
     }
 //============================== processDate ===================================
 
     private Date processDate(String value) {
         Date result = null;
         try {
             result = df.parse(value);
         } catch (Exception ex) {
             result = Calendar.getInstance().getTime();
             logger.error(ex.toString());
         }
         return result;
     }
     
     private void processIPAddress(AccessLog access,String itemValue) {
         access.setIpAddress(itemValue);
         GeoData geoData = accessLogService.findCountryDataByIP(itemValue);
         if (geoData != null) {
            access.setCountryName(geoData.getCountryName());
             access.setCountryCode(geoData.getCountryCode());
         }
     }
     
 }
