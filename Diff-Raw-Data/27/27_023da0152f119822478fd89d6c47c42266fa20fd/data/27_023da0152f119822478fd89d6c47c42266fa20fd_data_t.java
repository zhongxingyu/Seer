 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ywc.core;
 import com.jcraft.jsch.*;
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import sun.misc.BASE64Encoder;
 import ywc.model.CacheEntry;
 /**
  *
  * @author jd
  */
 public class data {
     
     public static String retrieve(CacheEntry entry) {
         String data = "";
         if (entry.assetID == null || entry.type == null) return null;
         
         if (entry.type.equals("http")) {
             data = requestHTTP(entry.url, entry.properties, entry.params);
         } else {
             throw new UnsupportedOperationException("type not recognized for cache entry " + entry.assetID);
         }
         return data;
     }
     public static void cache(CacheEntry entry, String data) {
         //FileWriter outFile;
         boolean writeSucc;
         try {
             //outFile = new FileWriter(new File());
             OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(settings.getPathYwcCache() + "/xml/cache/" + entry.assetID + ".xml"),"UTF-8");
             PrintWriter writeXml = new PrintWriter(out);
             writeXml.println(data);
             writeXml.close();
             if (filesystem.fileExists(settings.getPathYwcCache() + "/xml/cache/" + entry.assetID + ".xml")) {
                 writeSucc = true;
                 
                 //make sure tomcat will be able to update the file
                 ProcessBuilder pb = new ProcessBuilder("chmod", "777", settings.getPathYwcCache() + "/xml/cache/" + entry.assetID + ".xml");
                 pb.start();
             }
             
         } catch (Exception ex) {
             //Logger.getLogger(xml.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
     public static boolean isCacheUpToDate(CacheEntry entry, String data) {
         
         if (!filesystem.fileExists(settings.getPathYwcCache() + "/xml/cache/" + entry.assetID + ".xml")) return false;
         
         FileReader fr = null; String cachedData = ""; 
         try {
             fr = new FileReader(settings.getPathYwcCache() + "/xml/cache/" + entry.assetID + ".xml");
             BufferedReader br = new BufferedReader(fr);
             String s;
             while((s = br.readLine()) != null) { 
                 cachedData += s +"\n"; 
             }
             fr.close();
             
         } catch (Exception ex) {
             Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 fr.close();
             } catch (IOException ex) {
                 Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         if (cachedData != null && !cachedData.equals("") && data.equals(cachedData)) {
             return true;
         }
         return false;
     }
     public static void flushCache() {
         //flush mc cache if cache is not up to date
     }
     
     public static String getHttpContent(String url, HashMap params) {
         String data = "";
         try {
             URL oURL = new URL(url);
             HttpURLConnection connection = (HttpURLConnection)oURL.openConnection();
             
             if (params.containsKey("user")) {
                 BASE64Encoder enc = new sun.misc.BASE64Encoder();
                 String userpassword = params.get("user") + ":" + params.get("pass");
                 String encodedAuthorization = enc.encode( userpassword.getBytes() );
                 connection.setRequestProperty("Authorization", "Basic "+
                 encodedAuthorization);
             }
             
             BufferedReader in = new BufferedReader(
                                     new InputStreamReader(
                                     connection.getInputStream()));
             String inputLine;
 
             while ((inputLine = in.readLine()) != null) {
                 data += inputLine + "\n";
             }
             in.close();
         } catch (Exception ex) {
             Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
         }
         return data;
         
     }
     public static String requestHTTP(String url, HashMap properties, HashMap params) {
         String data = null;
         try {
             
             //trust all certs
             TrustManager[] trustAllCerts = new TrustManager[]{
                 new X509TrustManager() {
                 @Override
                     public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                         return null;
                     }
                 @Override
                     public void checkClientTrusted(
                         java.security.cert.X509Certificate[] certs, String authType) {
                     }
                 @Override
                     public void checkServerTrusted(
                         java.security.cert.X509Certificate[] certs, String authType) {
                     }
                 }
             };
 
             // Install the all-trusting trust manager
             try {
                 SSLContext sc = SSLContext.getInstance("SSL");
                 sc.init(null, trustAllCerts, new java.security.SecureRandom());
                 HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
             } catch (Exception e) {
             }
             
             
             URL oURL = new URL(url);
             URLConnection connection = null;
             if (url.startsWith("https://")) {
                 connection = (HttpsURLConnection)oURL.openConnection();
             } else {
                 connection = (HttpURLConnection)oURL.openConnection();
             }
             connection.setDoOutput(true);
             connection.setRequestProperty("Content-Encoding", "UTF-8");
             
             if (properties != null && properties.size() > 0) {
                 if (properties.containsKey("method")) {
                     if (url.startsWith("https://")) {
                         ((HttpsURLConnection) connection).setRequestMethod(properties.get("method").toString());
                     } else {
                         ((HttpURLConnection) connection).setRequestMethod(properties.get("method").toString());
                     }
                     
                 }
                
                 Iterator iteratorProp = properties.keySet().iterator();
                 while (iteratorProp.hasNext()) {
                     String key = (String) iteratorProp.next();
                    
                     //set method
                     if (key != null) {
                         if (!key.equals("method") && !key.equals("http_user") && !key.equals("http_pass")) {
                             connection.setRequestProperty(key, properties.get(key).toString());
                         }
                     }
                 }
                
             
                 if (properties.containsKey("http_user")) {
                     BASE64Encoder enc = new sun.misc.BASE64Encoder();
                     String userpassword = properties.get("http_user") + ":" + properties.get("http_pass");
                     String encodedAuthorization = enc.encode( userpassword.getBytes() );
                     connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);                
                 }   
             }
             
             
             //post params if exists
             if (params!=null && params.size() > 0) {
                 OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                 String parameters = "";
                 Iterator iterator = params.keySet().iterator();
                 while (iterator.hasNext()) {
                   String key = (String) iterator.next();
//                  System.out.println("params: " + key + " means " + params.get(key));
                   if (!parameters.equals("")) {
                       parameters = parameters + "&";
                   } 
                   parameters += key + "=" + URLEncoder.encode(params.get(key).toString(), "UTF-8");
                 }
                 writer.write(parameters);
                 writer.flush();
             }
             
             
            
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
             String inputLine;
 
             while ((inputLine = in.readLine()) != null) {
                 if (data == null) {
                     data = inputLine + "\n";
                 } else {
                     data += inputLine + "\n";
                 }
             }
             in.close();
         } catch (Exception ex) {
             Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
         return data;
         
     }
     
     public static boolean sendFile(String server, String user, String pass, String localDir, String localFile, String remoteDir, String remoteFile) {
         Boolean ret = false;
         try {
             //try {
                 JSch jsch = new JSch();
                 
                 java.util.Properties config = new java.util.Properties(); 
                 config.put("StrictHostKeyChecking", "no");
                 config.put("VerifyHostKeyDNS", "no");
                 
                 //System.out.println("sftp_getsession");
                 Session session = jsch.getSession(user, server);
                 session.setConfig(config);
                 session.setPassword(pass);
                 //System.out.println("sftp_connect");
                 session.connect();
 
                 //System.out.println("sftp_open_channel");
                 Channel channel = session.openChannel("sftp");
                 channel.connect();
 
                 ChannelSftp sftpChannel = (ChannelSftp) channel;
                 System.out.println("sftp_put " + localDir + "/" + localFile + " -> " + remoteDir + "/" + remoteFile);
                 sftpChannel.put(localDir + "/" + localFile, remoteDir + "/" + remoteFile);
 
                 System.out.println("sftp_read");
                 String res = sftpChannel.ls(remoteDir + "/" + remoteFile).toString();
                 if (res.indexOf(remoteFile) != -1) {
                     ret = true;
                 }
 
                 sftpChannel.exit();
                 session.disconnect();
                 
         } catch (SftpException ex) {
             Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JSchException ex) {
             Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
         } 
         return ret;
     }
 }
