 package com.network.monitor.service;
 
 import com.network.monitor.controller.ContactController;
 import com.network.monitor.domain.Contact;
 import com.network.monitor.domain.Setting;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.CookiePolicy;
 import java.net.CookieStore;
 import java.net.HttpCookie;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.List;
 import java.util.logging.Level;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author
  */
 public class SMSNotificationService {
 
     private static final Logger LOGGER = Logger.getLogger(SMSNotificationService.class);
 
     public void sendSMSToContacts(Setting setting, List<Contact> contacts, String message) {
 
         for (Contact contact : contacts) {
             try {
                 sendSMS(setting.getSmsUrlWebStar(), contact.getSmsNumber(), message, "JavaApp");
             } catch (Exception ex) {
                 try {
                     sendSingTelSMS(setting.getSmsUrlSingTel(), contact.getSmsNumber(), message);
                 } catch (Exception ex1) {
                     LOGGER.error(ex1.getMessage(), ex1);
                 }
             }
         }
     }
 
     public void sendStarhubTestSMS(Setting setting, String phoneNumber, String senderName, String testMessage) throws Exception {
         if (senderName == null || senderName.equals("")) {
             senderName = "JavaApp";
         }
         if (testMessage == null || testMessage.equals("")) {
             testMessage = "Default Test Message";
         }
         sendSMS(setting.getSmsUrlWebStar(), phoneNumber, testMessage, senderName);
     }
 
     public void sendSingtelTestSMS(Setting setting, String phoneNumber, String senderName, String testMessage) throws Exception {
         if (senderName == null || senderName.equals("")) {
             senderName = "JavaApp";
         }
         if (testMessage == null || testMessage.equals("")) {
             testMessage = "Default Test Message";
         }
         sendSingTelSMS(setting.getSmsUrlSingTel(), phoneNumber, testMessage);
     }
 
     public void sendSMS(String smsUrl, String smsNumber, String smsMessage, String senderName) throws MalformedURLException, IOException, Exception {
         try {
 
             CookieManager manager = new CookieManager();
             manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
             CookieHandler.setDefault(manager);
             HttpCookie httpCookie = null;
 
             String content = "?method=createSession"
                     + "&isUTF="
                     + "&message=" + URLEncoder.encode(smsMessage)
                     + "&recipients=" + smsNumber
                     + "&senderName=" + URLEncoder.encode(senderName);
 
             String fullUrl = smsUrl + content;
 
             URL url = new URL(fullUrl);
             System.out.println(url);
 
             HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.setDoOutput(true);
             httpURLConnection.setRequestMethod("POST");
             httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
             httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
 
             httpURLConnection.connect();
             httpURLConnection.getContent();
             // get cookies from underlying
             // CookieStore
             CookieStore cookieJar = manager.getCookieStore();
             List<HttpCookie> cookies =
                     cookieJar.getCookies();
             for (HttpCookie cookie : cookies) {
                 System.out.println("cookie: " + cookie.getName());
                 if (cookie.getName().contains("JSESSIONID")) {
                     httpCookie = cookie;
                 }
             }
             System.out.println("Cookies: " + cookies.toString());
             System.out.println("Init session: " + httpURLConnection.getResponseCode());
 
             String sessionId = getChatSession(httpCookie);
             logoutFromChat(httpCookie, sessionId);
 
         } catch (MalformedURLException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (Exception ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         }
     }
 
     public void sendSingTelSMS(String smsUrl, String smsNumber, String smsMessage) throws Exception {
         try {
 
             HttpCookie httpCookie = login();
             CookieManager manager = new CookieManager();
             CookieHandler.setDefault(manager);
             CookieStore cookieJar = manager.getCookieStore();
 
 
             String content = "?mobile=" + smsNumber;
 
             String fullUrl = smsUrl + content;
 
             URL url = new URL(fullUrl);
             cookieJar.add(url.toURI(), httpCookie);
             System.out.println(url);
 
             HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.setDoOutput(true);
             httpURLConnection.setRequestMethod("POST");
             httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
             httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 
             httpURLConnection.connect();
             System.out.println(httpURLConnection.getResponseCode());
 
             if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 System.out.println("SMS chat started successful");
                 InputStreamReader isr = new InputStreamReader(httpURLConnection.getInputStream());
                 BufferedReader reader = new BufferedReader(isr);
                 String str;
                 StringBuffer buffer = new StringBuffer();
                 while ((str = reader.readLine()) != null) {
                     buffer.append(str);
                 }
                 reader.close();
 
                 System.out.println("Response on start chat: " + buffer.toString());
                 String senderId = getSenderId(buffer.toString());
 
                 System.out.println("Sender: " + senderId);
 
 
                 sendMessage(httpCookie, smsNumber, senderId, smsMessage);
 
                 logout(httpCookie);
             }
 
         } catch (MalformedURLException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (URISyntaxException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (Exception ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         }
     }
 
     private HttpCookie login() throws Exception {
         HttpCookie httpCookie = null;
 
         try {
             String content = "?email=mklchan%40singnet.com.sg&password=Melvin123456&submit.x=27&submit.y=8";
             String fullUrl = "https://sms.singtel.com/internetsms/loginAction.do" + content;
 
             URL url = new URL(fullUrl);
             System.out.println(url);
 
 
             CookieManager manager = new CookieManager();
             manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
             CookieHandler.setDefault(manager);
 
             HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.setDoOutput(true);
             httpURLConnection.setRequestMethod("POST");
             httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
             httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
 
             httpURLConnection.connect();
             httpURLConnection.getContent();
             // get cookies from underlying
             // CookieStore
             CookieStore cookieJar = manager.getCookieStore();
             List<HttpCookie> cookies =
                     cookieJar.getCookies();
             for (HttpCookie cookie : cookies) {
                 System.out.println("cookie: " + cookie.getName());
                 if (cookie.getName().contains("JSESSIONID")) {
                     httpCookie = cookie;
                 }
             }
             System.out.println("Cookies: " + cookies.toString());
 
 
             if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 System.out.println("Login successful");
             }
 
         } catch (MalformedURLException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (Exception ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         }
         return httpCookie;
 
     }
 
     private void logout(HttpCookie httpCookie) throws Exception {
         try {
 
             String fullUrl = "https://sms.singtel.com/internetsms/closeSendWindowAction.do";
 
             CookieManager manager = new CookieManager();
             CookieHandler.setDefault(manager);
             CookieStore cookieJar = manager.getCookieStore();
 
             URL url = new URL(fullUrl);
             cookieJar.add(url.toURI(), httpCookie);
 
             HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.setDoOutput(true);
             httpURLConnection.setRequestMethod("POST");
             httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
             httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
 
             httpURLConnection.connect();
             System.out.println(httpURLConnection.getResponseCode());
 
             if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 System.out.println("Logout successful");
             }
 
         } catch (MalformedURLException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (URISyntaxException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (Exception ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         }
     }
 
     private String getSenderId(String xmlResponse) {
         String start = "<sender>";
         String end = "</sender>";
         int iStartIndex = xmlResponse.indexOf(start) + start.length();
         int iEndIndex = xmlResponse.indexOf(end);
         return xmlResponse.substring(iStartIndex, iEndIndex);
     }
 
     private void sendMessage(HttpCookie httpCookie, String smsNumber, String senderId, String smsMessage) throws Exception {
 
         try {
 
             String message = "";
 
             for (int i = 0; i < smsMessage.length(); i++) {
                 char c = smsMessage.charAt(i);
                 message = message.concat((int) c + "");
                 if (i < smsMessage.length() - 1) {
                     message = message.concat(",");
                 }
             }
 
             String fullUrl = "https://sms.singtel.com/internetsms/sendMessageAction.do"
                     + "?recipient=" + smsNumber
                     + "&sender=" + senderId
                     + "&message=" + URLEncoder.encode(message);
 
             CookieManager manager = new CookieManager();
             CookieHandler.setDefault(manager);
             CookieStore cookieJar = manager.getCookieStore();
 
             URL url = new URL(fullUrl);
             cookieJar.add(url.toURI(), httpCookie);
 
             HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.setDoOutput(true);
             httpURLConnection.setRequestMethod("POST");
             httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
             httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
 
             httpURLConnection.connect();
             System.out.println(httpURLConnection.getResponseCode());
 
             if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 System.out.println("Sent successful");
             } else {
                 InputStream is = httpURLConnection.getErrorStream();
 
                 //InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr);
                 String str;
                 StringBuffer buffer = new StringBuffer();
                 while ((str = reader.readLine()) != null) {
                     buffer.append(str);
                 }
                 reader.close();
                 System.out.println("Request: " + buffer.toString());
             }
 
 
         } catch (MalformedURLException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (URISyntaxException ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         } catch (Exception ex) {
             LOGGER.error(ex.getMessage(), ex);
             throw ex;
         }
 
     }
 
    private String getChatSession(HttpCookie httpCookie) throws MalformedURLException, URISyntaxException, IOException {
         String sessionId = "";
         try {
 
             String fullUrl = "http://websms.starhub.com/websmsn/usr/chat.do";
 
             CookieManager manager = new CookieManager();
             CookieHandler.setDefault(manager);
             CookieStore cookieJar = manager.getCookieStore();
 
             URL url = new URL(fullUrl);
             cookieJar.add(url.toURI(), httpCookie);
 
             HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.setRequestMethod("GET");
             httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
             httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
 
             httpURLConnection.connect();
             LOGGER.info("Response code for chatDo: " + httpURLConnection.getResponseCode());
 
             if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 InputStream is = httpURLConnection.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr);
                 String str;
                 StringBuffer buffer = new StringBuffer();
                 
                 PrintWriter printWriter = new PrintWriter("D:/crazy.html");
                 
                 
                 while ((str = reader.readLine()) != null) {
                     printWriter.println(str);
                     if (str.startsWith("var activeMsgSessionId")) {
                         String parts[] = str.split("=");
                         sessionId = parts[1].trim();
                         sessionId = sessionId.substring(sessionId.indexOf("\"") + 1, sessionId.lastIndexOf("\""));
                     }
                 }
                 reader.close();
                 printWriter.close();
                 System.out.println("Session: id: " + sessionId);
 
 
             } else {
                 InputStream is = httpURLConnection.getErrorStream();
 
                 //InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr);
                 String str;
                 StringBuffer buffer = new StringBuffer();
                 while ((str = reader.readLine()) != null) {
                     buffer.append(str);
                 }
                 reader.close();
                 System.out.println("Request: " + buffer.toString());
             }
 
 
         } catch (MalformedURLException ex) {
             LOGGER.error(ex.getMessage(), ex);
            throw ex;
         } catch (URISyntaxException ex) {
             LOGGER.error(ex.getMessage(), ex);
            throw ex;
         } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
            throw ex;
         }
         return sessionId;
 
 
     }
 
     private void logoutFromChat(HttpCookie httpCookie, String sessionId) {
         try {
 
             String fullUrl = "http://websms.starhub.com/websmsn/usr/deleteMsgSessionSubmit.do"
                     + "?method=deleteSession&messageSessionId=" + sessionId.trim();
 
             CookieManager manager = new CookieManager();
             CookieHandler.setDefault(manager);
             CookieStore cookieJar = manager.getCookieStore();
 
             URL url = new URL(fullUrl);
             cookieJar.add(url.toURI(), httpCookie);
 
             HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.setRequestMethod("GET");
             httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
             httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
 
             httpURLConnection.connect();
             LOGGER.info("Response code for delete session: " + httpURLConnection.getResponseCode());
 
             if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 LOGGER.info("Session " + sessionId + " has been successfully closed");
             } else {
                 InputStream is = httpURLConnection.getErrorStream();
 
                 //InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr);
                 String str;
                 StringBuffer buffer = new StringBuffer();
                 while ((str = reader.readLine()) != null) {
                     buffer.append(str);
                 }
                 reader.close();
                 System.out.println("Request: " + buffer.toString());
             }
 
 
         } catch (MalformedURLException ex) {
             LOGGER.error(ex.getMessage(), ex);
         } catch (URISyntaxException ex) {
             LOGGER.error(ex.getMessage(), ex);
         } catch (IOException ex) {
             LOGGER.error(ex.getMessage(), ex);
         }
     }
 }
