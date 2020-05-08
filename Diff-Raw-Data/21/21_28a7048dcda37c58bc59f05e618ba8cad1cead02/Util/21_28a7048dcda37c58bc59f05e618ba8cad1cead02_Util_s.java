 /* Copyright (c) 2013, HotDocs Limited
    Use, modification and redistribution of this source is subject
    to the New BSD License as set out in LICENSE.TXT. */
 
 package com.hotdocs.cloud;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.Scanner;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 /**
  * Static utility methods.
  */
 public class Util {
 
     /**
      * Reads a whole stream into a string.
      * 
      * @param stream
      * @return
      */
     public static String readString(InputStream stream) {
         try (Scanner scanner = new Scanner(stream)) {
             scanner.useDelimiter("\\A");
             return scanner.hasNext() ? scanner.next() : "";
         }
     }
 
     /**
      * Copies one stream to another.
      * 
      * @param from
      * @param to
      * @throws IOException
      */
     public static void copyStream(InputStream from, OutputStream to)
             throws IOException {
         byte[] buffer = new byte[1024];
         int len;
         while ((len = from.read(buffer)) != -1) {
             to.write(buffer, 0, len);
         }
         from.close();
         to.close();
     }
 
     /**
      * Determines whether an HTTP request was successful.
      * 
      * @param status
      * @return
      * @throws IOException
      */
     public static boolean httpOk(HttpURLConnection conn) throws IOException {
         // Is the response code >= 200 and < 300?
         return (conn.getResponseCode() / 100 == 2);
     }
 
     /**
      * Returns the body of the response.
      * 
      * @param conn
      * @return
      * @throws IOException
      */
     public static InputStream getResponseStream(HttpURLConnection conn)
             throws IOException {
         return httpOk(conn) ? conn.getInputStream() : conn.getErrorStream();
     }
 
     /**
      * Typical isNullOrEmpty call.
      * 
      * @param str
      * @return
      */
     public static boolean isNullOrEmpty(String str) {
         return (str == null || str.length() == 0);
     }
 
     /**
      * Typical join call
      */
     public static String join(String[] strs, String delim) {
         StringBuilder buffer = new StringBuilder();
         for (int i = 0; i < strs.length; i++) {
             buffer.append(strs[i]);
             if (i < strs.length - 1) {
                 buffer.append(delim);
             }
         }
         return buffer.toString();
     }
 
     /**
      * Get a value from a string of name-value pairs formatted like:
      *   name1=value1;name2=value2;name3=value3
      * The delimiter needn't be a semicolon -- it can be any string.
      * @param str
      * @param delim
      * @param name
      * @return
      */
     public static String getNamedValue(String str, String delim, String name) {
         String[] pairs = str.split(delim);
         for (String s : pairs) {
             if (s.startsWith(name + "=")) {
                 return s.substring(name.length() + 1);
             }
         }
         return null;
     }
 
     /**
      * Configures the default SSLContext to ignore certificate errors.
      * For testing only! Do not call this in a production deployment!
      */
     public static void setSSLContextToIgnoreCertErrors() {
         try {
             SSLContext ctx = SSLContext.getInstance("TLS");
             ctx.init(new KeyManager[0],
                     new TrustManager[] { new IgnoreCertErrorsTrustManager() },
                     new SecureRandom());
             SSLContext.setDefault(ctx);
         } catch (NoSuchAlgorithmException | KeyManagementException ex) {
             // Since this method is for testing only, ignore all exceptions.
         }
     }
 
     /**
      * Configures an HttpsURLConnection to ignore host name errors.
      * For testing only! Do not call this in a production deployment!
      * 
      * @param conn
      */
     public static void setConnToIgnoreHostNameErrors(HttpsURLConnection conn) {
         conn.setHostnameVerifier(new HostnameVerifier() {
             @Override
             public boolean verify(String hostname, SSLSession session) {
                 return true;
             }
         });
     }
 
     /**
      * Custom trust manager that ignores cert errors.
      * For testing only! Do not use this in a production deployment!
      */
     private static class IgnoreCertErrorsTrustManager
             implements X509TrustManager {
         @Override
         public void checkClientTrusted(X509Certificate[] cert, String authType)
                 throws CertificateException {
         }
 
         @Override
         public void checkServerTrusted(X509Certificate[] cert, String authType)
                 throws CertificateException {
         }
 
         @Override
         public X509Certificate[] getAcceptedIssuers() {
             return null;
         }
     }
 }
