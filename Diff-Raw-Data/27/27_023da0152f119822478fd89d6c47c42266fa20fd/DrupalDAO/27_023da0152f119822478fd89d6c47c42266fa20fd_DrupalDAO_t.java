 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ywc.dao;
 
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import ywc.core.data;
 import ywc.core.settings;
 import ywc.model.CacheEntry;
 import ywc.notification.Mail;
 
 public class DrupalDAO {
 
     private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DrupalDAO.class);
 
     String contentType, protocol, domain, path, url, drupalUsername, drupalPassword, cookie, token, httpUser, httpPass;
 
     public DrupalDAO() {
         protocol = settings.getProp("drupal.protocol", null);
         domain = settings.getProp("drupal.domain", null);
         path = settings.getProp("drupal.path", null);
         url = protocol + "://" + domain + path;
         
         drupalUsername = settings.getProp("drupal.user", null);
         drupalPassword = settings.getProp("drupal.pass", null);
         httpUser = settings.getProp("drupal.http_user", null);
         httpPass = settings.getProp("drupal.http_pass", null);
     }
 
     public DrupalDAO(String pType) {
         contentType = pType;
         protocol = settings.getProp("drupal.protocol", null);
         domain = settings.getProp("drupal.domain", null);
         path = settings.getProp("drupal.path", null);
         url = protocol + "://" + domain + path;
         
         drupalUsername = settings.getProp("drupal.user", null);
         drupalPassword = settings.getProp("drupal.pass", null);
         httpUser = settings.getProp("drupal.http_user", null);
         httpPass = settings.getProp("drupal.http_pass", null);
     }
 
     public void setEndpoint(String pEndpoint, String pUser, String pPass) {
         url = pEndpoint;
         drupalUsername = pUser;
         drupalPassword = pPass;
     }
 
     public HashMap getDrupalHeaders(String httpMethod) {
         HashMap drupalHeaders = new HashMap();
         drupalHeaders.put("method", httpMethod.toUpperCase());
         drupalHeaders.put("Content-Type", "application/x-www-form-urlencoded");
         if (httpUser != null && httpPass != null) {
             drupalHeaders.put("http_user", httpUser);
             drupalHeaders.put("http_pass", httpPass);
         }
         if (this.cookie != null) {
             drupalHeaders.put("Cookie", getCookie());
         }
         if (this.token != null) {
             drupalHeaders.put("X-CSRF-Token", getToken());
         }
         return drupalHeaders;
     }
 
     public boolean validLogin() {
 
         boolean rtrn = false;
 
         HashMap loginCreds = new HashMap();
         loginCreds.put("username", getUsername());
         loginCreds.put("password", getPassword());
 
         String loginResponse = data.requestHTTP(url + "/user/login.json", getDrupalHeaders("POST"), loginCreds);
 
         if (loginResponse != null) {
             JsonObject loginJsonObj = (JsonObject) new JsonParser().parse(loginResponse);
             if ((loginJsonObj != null) && loginJsonObj.has("sessid") && loginJsonObj.has("session_name")) {
                 setCookie(loginJsonObj.get("session_name").getAsString() + "=" + loginJsonObj.get("sessid").getAsString());
                 String csrfResponse = data.requestHTTP(protocol+"://"+domain+"/services/session/token", getDrupalHeaders("GET"), new HashMap());
                 if (csrfResponse != null) {
                     setToken(csrfResponse);
                     rtrn = true;
                 }
             }
         }
         return rtrn;
     }
 
     public void doDrupalLogOff() {
         String json = data.requestHTTP(url + "/user/logout.json", getDrupalHeaders("POST"), new HashMap());
         if (json != null && json.equals("true")) {
             logger.debug("Logout successful from " + domain);
         }
     }
 
     public String getNode(String nid) {
         String rtrn = null;
         if (validLogin()) {
             String json = data.requestHTTP(url + "/node/" + nid + ".json", getDrupalHeaders("GET"), new HashMap());
             //JsonObject jsobj = (JsonObject) new JsonParser().parse(json);
             if (json != null) {
                 rtrn = json;
             }
             doDrupalLogOff();
         }
         return rtrn;
     }
 
     public String updateNode(String nid, HashMap params) {
         String rtrn = null;
         if (validLogin()) {
             //if nid == null -> create node
             String updateUri = url + "/node.json";
             String updateMethod = "POST";
 
             if (nid != null) {
                 updateUri = url + "/node/" + nid + ".json";
                 updateMethod = "PUT";
             }
            String jsonResponse = data.requestHTTP(updateUri, getDrupalHeaders(updateMethod), params);
 
             if (jsonResponse != null) {
                 JsonObject jsonObj = (JsonObject) new JsonParser().parse(jsonResponse);
                 if (nid == null) {
                     rtrn = jsonObj.get("nid").getAsString();
                 } else {
                     rtrn = nid;
                 }
             }
             doDrupalLogOff();
         }
         return rtrn;
     }
 
     public boolean deleteNode(String nid) {
         boolean rtrn = false;
         if (validLogin()) {
 
             String jsonResponse = data.requestHTTP(url + "/node/" + nid + ".json", getDrupalHeaders("DELETE"), new HashMap());
 
             if ((jsonResponse != null) && jsonResponse.equals("true")) {
                 rtrn = true;
             }
             doDrupalLogOff();
         }
         return rtrn;
     }
 
     public boolean subscribeList(String tid, String mail) {
         boolean rtrn = false;
         if (validLogin()) {
 
             HashMap params = new HashMap();
             params.put("mail", mail);
             params.put("tid", tid);
 
             String jsonResponse = data.requestHTTP(url + "/simplenews/subscribe.json", getDrupalHeaders("POST"), params);
 
             if ((jsonResponse != null) && jsonResponse.equals("true")) {
                 rtrn = true;
             }
             doDrupalLogOff();
         }
         return rtrn;
     }
 
     public boolean unsubscribeList(String tid, String mail) {
         boolean rtrn = false;
         if (validLogin()) {
 
             HashMap params = new HashMap();
             params.put("mail", mail);
             params.put("tid", tid);
 
             String jsonResponse = data.requestHTTP(url + "/simplenews/unsubscribe.json", getDrupalHeaders("POST"), params);
 
             if ((jsonResponse != null) && jsonResponse.equals("true")) {
                 rtrn = true;
             }
             doDrupalLogOff();
         }
         return rtrn;
     }
 
     public void informSubscribers(String nid, String cacheID) {
         HashMap properties = new HashMap();
         if (httpUser != null && httpPass != null) {
             properties.put("http_user", "oist");
             properties.put("http_pass", "oist");
         }
 
         CacheEntry entry = CacheDAO.getCacheEntry(cacheID);
 
         // if mail is disabled, let's only notify the admins
         if (settings.getProp("ywc.mail.enable", null) != null
                 && settings.getProp("ywc.mail.enable", null).equals("false")) {
 
             Mail mail = new Mail();
             // send mails to subscribers
             logger.info("Notify admins only since ywc.mail.enable=false");
             mail.sendMail(settings.get("ywc.mail.admins"),
                     "ADMIN ONLY - " + data.requestHTTP(settings.get("ywc.url.noauth") + "/ywc/intranet/title/" + entry.name + "/" + nid, properties, null),
                     data.requestHTTP(settings.get("ywc.url.noauth") + "/ywc/intranet/email/" + entry.name + "/" + nid, properties, null),
                     "text/html");
             return;
         }
 
         if (entry.properties.get("subscribe_id") != null) {
             Document dom;
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             try {
                 DocumentBuilder db = dbf.newDocumentBuilder();
                 dom = db.parse(settings.getPathYwcCache() + "/xml/cache/aaaaaj.xml");
                 Element docEle = dom.getDocumentElement();
 
                 //get a nodelist of <employee> elements
                 NodeList nl = docEle.getElementsByTagName("simplenews_subscriber");
 
                 if (nl != null && nl.getLength() > 0) {
                     for (int i = 0; i < nl.getLength(); i++) {
                         Element el = (Element) nl.item(i);
                         String listID = getTextValue(el, "list_id");
                         String address = getTextValue(el, "mail");
 
                         if (listID.equals(entry.properties.get("subscribe_id"))) {
                             Mail mail = new Mail();
 
                             // send mails to subscribers
                             logger.info("Notify subscriber of " + listID + " address:" + address);
                             mail.sendMail(address,
                                     data.requestHTTP(settings.get("ywc.url.noauth") + "/ywc/intranet/title/" + entry.name + "/" + nid, properties, null),
                                     data.requestHTTP(settings.get("ywc.url.noauth") + "/ywc/intranet/email/" + entry.name + "/" + nid, properties, null),
                                     "text/html");
                         }
                     }
                 }
             } catch (Exception ex) {
                 logger.warn("Something went wrong..");
             }
 
         } else {
             logger.warn("No subscribe_id found in caching table for " + cacheID);
         }
 
     }
 
     private String getTextValue(Element ele, String tagName) {
         String textVal = null;
         NodeList nl = ele.getElementsByTagName(tagName);
         if (nl != null && nl.getLength() > 0) {
             Element el = (Element) nl.item(0);
             textVal = el.getFirstChild().getNodeValue();
         }
         return textVal;
     }
 
     public String getCookie() {
         return this.cookie;
     }
 
     private void setCookie(String cookie) {
         this.cookie = cookie.replaceAll("\\s", "");
     }
 
     public String getToken() {
         return this.token;
     }
 
     private void setToken(String token) {
         this.token = token.replaceAll("\\s", "");
     }
 
     public String getUsername() {
         return this.drupalUsername;
     }
 
     public String getPassword() {
         return this.drupalPassword;
     }
 
 }
