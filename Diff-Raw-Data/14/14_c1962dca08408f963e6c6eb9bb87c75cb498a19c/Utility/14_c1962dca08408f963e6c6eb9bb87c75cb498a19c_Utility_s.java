 package com.dumbear.dumboj.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Utility {
     public static String encodeHtmlSource(byte[] bytes, String charsetName) throws Exception {
         if (charsetName == null) {
             charsetName = "UTF-8";
         }
         String tmp = new String(bytes, charsetName);
         Matcher matcher = Pattern.compile("charset=([-\\w]+)", Pattern.CASE_INSENSITIVE).matcher(tmp);
         if (matcher.find()) {
             String name = matcher.group(1);
             if (name.toLowerCase().equals(charsetName.toLowerCase())) {
                 return tmp;
             }
             try {
                 Charset.forName(name);
                 charsetName = name;
             } catch (Exception e) {
             }
         }
         return new String(bytes, charsetName);
     }
 
     public static byte[] readToEnd(InputStream is) throws Exception {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         int length = 0;
         byte[] buffer = new byte[1024];
         while ((length = is.read(buffer)) != -1) {
             os.write(buffer, 0, length);
         }
         return os.toByteArray();
     }
 
     public static String getHtmlSourceByGet(String spec, String charsetName, StringBuffer cookie) throws Exception {
         byte[] bytes = null;
         for (int i = 3; i > 0; --i) {
             try {
                 URL url = new URL(spec);
                 HttpURLConnection.setFollowRedirects(false);
                 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                 if (cookie != null) {
                     connection.setRequestProperty("Cookie", cookie.toString());
                 }
                 InputStream is = connection.getInputStream();
                 bytes = readToEnd(is);
                 is.close();
                 if (cookie != null) {
                     List<String> cookieList = connection.getHeaderFields().get("Set-Cookie");
                     if (cookieList != null) {
                         for (String c : cookieList) {
                             cookie.append(c);
                             cookie.append("; ");
                         }
                     }
                 }
                 connection.disconnect();
                 break;
             } catch (Exception e) {
                 if (i == 1) {
                     throw e;
                 }
             }
         }
         return encodeHtmlSource(bytes, charsetName);
     }
 
     public static String getHtmlSourceByPost(String spec, String charsetName, byte[] postData, StringBuffer cookie) throws Exception {
         byte[] bytes = null;
         for (int i = 3; i > 0; --i) {
             try {
                 URL url = new URL(spec);
                 HttpURLConnection.setFollowRedirects(false);
                 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                 connection.setRequestMethod("POST");
                 connection.setDoOutput(true);
                 connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                 connection.setRequestProperty("Connection", "keep-alive");
                 connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
                 if (cookie != null) {
                     connection.setRequestProperty("Cookie", cookie.toString());
                 }
                 OutputStream os = connection.getOutputStream();
                 os.write(postData);
                 os.flush();
                 os.close();
                 InputStream is = connection.getInputStream();
                 bytes = readToEnd(is);
                 is.close();
                 if (cookie != null) {
                     List<String> cookieList = connection.getHeaderFields().get("Set-Cookie");
                     if (cookieList != null) {
                         for (String c : cookieList) {
                             cookie.append(c);
                             cookie.append("; ");
                         }
                     }
                 }
                 connection.disconnect();
                 break;
             } catch (Exception e) {
                 if (i == 1) {
                     throw e;
                 }
             }
         }
         return encodeHtmlSource(bytes, charsetName);
     }
 
     public static String getMatcherString(String input, String regex, int group) {
         Matcher matcher = Pattern.compile(regex).matcher(input);
         return matcher.find() ? matcher.group(group) : "";
     }
 
     public static String getMatcherString(String input, Pattern pattern, int group) {
         Matcher matcher = pattern.matcher(input);
         return matcher.find() ? matcher.group(group) : "";
     }
 
     public static String resolveHtmlLinks(String source, String base) {
         source = source.replaceAll("((?i)src|href)\\s*=\\s*(?!['\"]|[a-zA-Z0-9+.-]*:)(\\./|/)?([^\\s>]+)", "$1=\"" + base + "$3\"");
         source = source.replaceAll("((?i)src|href)\\s*=\\s*'(?![a-zA-Z0-9+.-]*:)(\\./|/)?([^']+)'", "$1=\"" + base + "$3\"");
         source = source.replaceAll("((?i)src|href)\\s*=\\s*\"(?![a-zA-Z0-9+.-]*:)(\\./|/)?([^\"]+)\"", "$1=\"" + base + "$3\"");
         return source;
     }
 
     public static String resolveHtmlLinks(String source, String base, String root) {
         source = source.replaceAll("((?i)src|href)\\s*=\\s*(?!['\"]|[a-zA-Z0-9+.-]*:)/([^\\s>]+)", "$1=\"" + root + "$2\"");
         source = source.replaceAll("((?i)src|href)\\s*=\\s*'(?![a-zA-Z0-9+.-]*:)/([^']+)'", "$1=\"" + root + "$2\"");
         source = source.replaceAll("((?i)src|href)\\s*=\\s*\"(?![a-zA-Z0-9+.-]*:)/([^\"]+)\"", "$1=\"" + root + "$2\"");
         source = source.replaceAll("((?i)src|href)\\s*=\\s*(?!['\"]|[a-zA-Z0-9+.-]*:)(\\./)?([^\\s>]+)", "$1=\"" + base + "$3\"");
         source = source.replaceAll("((?i)src|href)\\s*=\\s*'(?![a-zA-Z0-9+.-]*:)(\\./)?([^']+)'", "$1=\"" + base + "$3\"");
         source = source.replaceAll("((?i)src|href)\\s*=\\s*\"(?![a-zA-Z0-9+.-]*:)(\\./)?([^\"]+)\"", "$1=\"" + base + "$3\"");
         return source;
     }
 
     public static String md5(String data, String charsetName) {
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.update(data.getBytes(charsetName));
             StringBuffer buffer = new StringBuffer();
             for (byte b : md.digest()) {
                 buffer.append(Integer.toHexString((b >>> 4) & 0xF));
                 buffer.append(Integer.toHexString((b >>> 0) & 0xF));
             }
             return buffer.toString();
         } catch (Exception e) {
             return "";
         }
     }
 }
