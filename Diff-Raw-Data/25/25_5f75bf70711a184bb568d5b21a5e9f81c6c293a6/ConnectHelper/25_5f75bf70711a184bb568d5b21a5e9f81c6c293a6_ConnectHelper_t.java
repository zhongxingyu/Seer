 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav "Gussoh" Sohtell
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout.commons;
 
 import java.nio.ByteBuffer;
 
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.net.URL;
 import java.net.URLEncoder;
 
 import java.net.HttpURLConnection;
 import java.io.OutputStreamWriter;
 
 /**
  *
  **
  */
 public class ConnectHelper {
 
     // Content types the tile giver should check
     private static final String[] contentTypes = new String[]{"text/html",
         "application/xhtml+xml", "application/xml", "text/xml"};
     // Charset to fall back to if none was found
     private static final String fallbackCharset = "iso-8859-1";
 
 
     private static String getCharset(String contentType) {
         String[] parameters = contentType.split(";");
         
         for(int i = 1; i < parameters.length; i++) {
             String parameter = parameters[i].trim();
             if(parameter.indexOf('=') != -1) {
                 int    split = parameter.indexOf('=');
                 String key   = parameter.substring(0, split);
                 String value = parameter.substring(split + 1);
                 
                 if(key.equalsIgnoreCase("charset")) {
                     //TODO: fix for quoted strings
                     return value;
                 }
             }
         }
         return null;
     }
 
     private static boolean okContentType(String contentType) {
         for (int i = 0; i < contentTypes.length; i++) {
             if (contentTypes[i].equalsIgnoreCase(contentType)) {
                 return true;
             }
         }
 //        System.out.println("damn content type!");
         return false;
     }
 
 
     /**
      *
      * @param connectionType
      * @param server
      * @param file
      * @param port
      * @param maxContentLength
      * @return
      */
     public static String Connect(String connectionType, String server, String file, int port, int maxContentLength,
             String requestMethod, Map<String, String> postData) {
         try {
             // Set up connection to disallow output and allow input. It should follow
             // redirects but dont use a cache.
            URL url = new URL(connectionType, server, port, file);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
             con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)");
 
             con.setAllowUserInteraction(false);
             con.setDoInput(true);
             con.setDoOutput(false);
             con.setUseCaches(false);
             con.setInstanceFollowRedirects(true);
 
             // Request method
             if(requestMethod != null) {
                 con.setRequestMethod(requestMethod);
             }
 
             // Write postdata:
             if(postData != null && !postData.isEmpty()) {
                 OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                 String postdata = "";
                 for(Map.Entry<String,String> entry: postData.entrySet()) {
                     postdata = "&" + URLEncoder.encode(entry.getKey(), "utf-8")
                             + "=" + URLEncoder.encode(entry.getValue(), "utf-8");
                 }
                 wr.write(postdata.substring(1));
                 wr.flush();
             }
             
             // Find out charset and content type. As default, if we don't find a
             // charset in the HTML header we try to use ISO-8559-1 later.
             String contentType = con.getContentType().split(";")[0];
             String charset     = getCharset(con.getContentType());
             
             // Check for content type. Only accept web pages.
             if (!okContentType(contentType)) {
                 if(contentType.contains(";"))
                     contentType = contentType.substring(0,contentType.indexOf(";"));
                 else return null;
             }
 
             // Byte buffer (from content length):
             int contentLength = con.getContentLength();
             if (contentLength > maxContentLength || contentLength < 100) {
                 contentLength = maxContentLength;
             }
             ByteBuffer bb = ByteBuffer.allocate(contentLength);
 
             try {
                 while (true) {
                     byte[] tmp = new byte[256];
                     int cnt = con.getInputStream().read(tmp);
                     if (cnt == -1) {
                         break;
                     }
                     bb.put(tmp, 0, cnt);
                 }
             } catch (java.io.IOException e) {
                 e.printStackTrace();
                 return null;
             } catch (java.nio.BufferOverflowException e) {
                 //e.printStackTrace();
             // TODO: Work around
             }
 
             if (charset == null) {
                 String patternMeta = "(?i)<meta\\s([^>]*)>";
                 Pattern pm = Pattern.compile(patternMeta);
                 Matcher mm = pm.matcher(new String(bb.array(), fallbackCharset));
 
                 while(mm.find()) {
 
                     String patternAttrib = "(?i)([a-z\\-]+)=(\"|')([^\"|']*)(\"|')";
                     Pattern pa = Pattern.compile(patternAttrib);
                     Matcher ma = pa.matcher(mm.group(1));
                     
                     System.out.println(mm.group(1));
                     
                     String httpEquiv = null, content = null;
                     while(ma.find()) {
                         System.out.println(ma.group(1) + ": " + ma.group(3));
                         if(ma.group(1).equalsIgnoreCase("http-equiv")) {
                             httpEquiv = ma.group(3);
                         } else if(ma.group(1).equalsIgnoreCase("content")) {
                             content  = ma.group(3);
                         }
                     }
                     
                     if(httpEquiv != null && content != null) {
                         if(httpEquiv.equalsIgnoreCase("Content-Type")) {
                           System.out.println("Found charset in meta");
                           System.out.println(httpEquiv + ", " + content);
                           charset = getCharset(content);
                           break;
                         }
                     }
                 }
             }
             
             // If still no charset, fall back to fallbackCharset
             if (charset == null) {
                 charset = fallbackCharset;
                  System.out.println("Using fallback charset: " + charset);
             }
            
             return new String(bb.array(), charset);
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
 }
 
