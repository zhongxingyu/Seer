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
 package fr.free.movierenamer.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.Inflater;
 import java.util.zip.InflaterInputStream;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.cyberneko.html.parsers.DOMParser;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import fr.free.movierenamer.settings.Settings;
 
 /**
  * Class URIRequest
  *
  * @author Nicolas Magré
  * @author Simon QUÉMÉNEUR
  */
 public final class URIRequest {
 
   public static final String UTF = "UTF-8";
   public static final String ISO = "ISO-8859-1";
 
   public static String getDocumentContent(URI uri, RequestProperty... properties) throws SAXException, IOException {
     return getDocumentContent(openConnection(uri, properties));
   }
 
   private static String getDocumentContent(URLConnection connection) throws IOException, SAXException {
     return getDocumentContent(getReader(connection));
   }
 
   private static String getDocumentContent(Reader reader) throws SAXException, IOException {
     BufferedReader rd = new BufferedReader(reader);
     StringBuilder sb = new StringBuilder();
     String line;
 
     while ((line = rd.readLine()) != null) {
       line = line.trim();
       if (line.length() > 0) {
         sb.append(line).append(StringUtils.ENDLINE);
       }
     }
     rd.close();
     return sb.toString();
   }
 
   public static Document getHtmlDocument(URI uri, RequestProperty... properties) throws IOException, SAXException {
     return getHtmlDocument(openConnection(uri, properties));
   }
 
   private static Document getHtmlDocument(URLConnection connection) throws IOException, SAXException {
     return getHtmlDocument(getReader(connection));
   }
 
   private static Document getHtmlDocument(Reader reader) throws SAXException, IOException {
     DOMParser parser = new DOMParser();
     parser.setFeature("http://xml.org/sax/features/namespaces", false);
     parser.parse(new InputSource(reader));
 
     return parser.getDocument();
   }
 
   public static Document getXmlDocument(URI uri, RequestProperty... properties) throws IOException, SAXException {
     return getXmlDocument(openConnection(uri, properties));
   }
 
   private static Document getXmlDocument(URLConnection connection) throws IOException, SAXException {
     return getXmlDocument(new InputSource(getReader(connection)));
   }
 
   private static Document getXmlDocument(InputSource source) throws IOException, SAXException {
     try {
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       factory.setValidating(false);
       factory.setFeature("http://xml.org/sax/features/namespaces", false);
       factory.setFeature("http://xml.org/sax/features/validation", false);
       return factory.newDocumentBuilder().parse(source);
     } catch (ParserConfigurationException e) {
       // will never happen
       throw new RuntimeException(e);
     }
   }
 
   public static JSONObject getJsonDocument(URI uri, RequestProperty... properties) throws IOException {
     return getJsonDocument(openConnection(uri, properties));
   }
 
   private static JSONObject getJsonDocument(URLConnection connection) throws IOException {
     return getJsonDocument(getReader(connection));
   }
 
   private static JSONObject getJsonDocument(Reader reader) {
     return (JSONObject) JSONValue.parse(reader);
   }
 
  private synchronized static URLConnection openConnection(URI uri, RequestProperty... properties) throws IOException {
     boolean isHttpRequest = Proxy.Type.HTTP.name().equalsIgnoreCase(uri.getScheme());
     URLConnection connection;
     if (isHttpRequest && Settings.getInstance().isProxyIsOn()) {
       Settings settings = Settings.getInstance();
       Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(settings.getProxyUrl(), settings.getProxyPort()));
       connection = uri.toURL().openConnection(proxy);
 
     } else {
       connection = uri.toURL().openConnection();
     }
 
     if (isHttpRequest) {
       Settings settings = Settings.getInstance();
       connection.setReadTimeout(settings.getHttpRequestTimeOut() * 1000); // in ms
       //fake user agent ;)
       connection.addRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
       connection.addRequestProperty("From", "googlebot(at)googlebot.com");
       connection.addRequestProperty("Accept", "*/*");
       String customUserAgent = settings.getHttpCustomUserAgent();
       if (customUserAgent != null && customUserAgent.length() > 0) {
         connection.addRequestProperty("User-Agent", customUserAgent);
       }
     }
 
     connection.addRequestProperty("Accept-Encoding", "gzip,deflate");
     connection.addRequestProperty("Accept-Charset", UTF + "," + ISO); // important for accents !
 
     if (properties != null) {
       for (RequestProperty property : properties) {
         connection.addRequestProperty(property.getKey(), property.getValue());
       }
     }
 
     return connection;
   }
 
   public static InputStream getInputStream(URI uri, RequestProperty... properties) throws IOException {
     return getInputStream(openConnection(uri, properties));
   }
 
   private static InputStream getInputStream(URLConnection connection) throws IOException {
     String encoding = connection.getContentEncoding();
     InputStream inputStream;
     try {
       inputStream = connection.getInputStream();
     } catch (IOException ioe) {
       throw ioe;
     }
 
     if ("gzip".equalsIgnoreCase(encoding)) {
       inputStream = new GZIPInputStream(inputStream);
     } else if ("deflate".equalsIgnoreCase(encoding)) {
       inputStream = new InflaterInputStream(inputStream, new Inflater(true));
     }
 
     return inputStream;
   }
 
   private static Reader getReader(URLConnection connection) throws IOException {
     Charset charset = getCharset(connection.getContentType());
 
     return new InputStreamReader(getInputStream(connection), charset);
   }
 
   private static Charset getCharset(String contentType) {
     if (contentType != null) {
       // Content-Type: text/html; charset=iso-8859-1
       Matcher matcher = Pattern.compile("charset=(\\p{Graph}+)").matcher(contentType);
 
       if (matcher.find()) {
         try {
           return Charset.forName(matcher.group(1));
         } catch (IllegalArgumentException e) {
           Logger.getLogger(URIRequest.class.getName()).log(Level.WARNING, e.getMessage());
         }
       }
 
       // use http default encoding only for text/html
       if (contentType.equals("text/html")) {
         return Charset.forName(ISO);
       }
     }
 
     // default
     return Charset.forName(UTF);
   }
 
   public static int getResponseCode(URL url, RequestProperty... properties) {
     try {
       HttpURLConnection huc = (HttpURLConnection) openConnection(url.toURI(), properties);
       huc.setRequestMethod("GET");
       huc.connect();
       return huc.getResponseCode();
     } catch (Exception ex) {
       return 0;
     }
   }
 
   public static String encode(String string) {
     try {
       return URLEncoder.encode(string, UTF);
     } catch (UnsupportedEncodingException e) {
       throw new RuntimeException(e);
     }
   }
 
   public static class RequestProperty {
 
     private final String key;
     private final String value;
 
     public RequestProperty(String key, String value) {
       this.key = key;
       this.value = value;
     }
 
     public String getKey() {
       return key;
     }
 
     public String getValue() {
       return value;
     }
   }
 }
