 package giter.http.utils;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.Proxy;
 import java.net.URLConnection;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Better wrapper for LLHTTPClient
  * 
  * @author giter
  */
 public final class HttpClient {
 
   public static final int DEFAULT_READ_TIMEOUT = 120000;
   public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
 
   private int connectTimeOut = DEFAULT_CONNECT_TIMEOUT;
   private int readTimeOut = DEFAULT_READ_TIMEOUT;
 
   private Proxier proxier = null;
   private Proxy proxy = null;
 
   private LinkedHashMap<String, String> headers = null;
   private LinkedHashMap<String, String> cookies = null;
 
   private boolean persistCookies = true;
   private boolean follow;
 
   public HttpClient() {
   }
 
   /**
    * Set User-Agent
    * 
    * @param agent
    *          User-Agent to set
    * @return this object
    */
   public HttpClient agent(String agent) {
     headers().put("User-Agent", agent);
     return this;
   }
 
   /**
    * Http Basic Realm
    * 
    * @param username
    *          username
    * @param password
    *          password
    * @return this object
    */
   public HttpClient auth(String username, String password) {
    headers().put("Authorization", "Basic " + B64Code.decode(username + ":" + password));
     return this;
   }
 
   /**
    * connect timeout
    * 
    * @param timeout
    *          in ms
    * @return this object
    */
   public HttpClient connect(int timeout) {
     this.connectTimeOut = timeout;
     return this;
   }
 
   /**
    * Set single cookie to request
    * 
    * @param key
    *          cookie key
    * @param value
    *          cookie value
    * @return this object
    */
   public HttpClient cookie(String key, String value) {
     cookies().put(key, value);
     headers().put("Cookie", cookiesString());
     return this;
   }
 
   private LinkedHashMap<String, String> cookies() {
 
     if (cookies == null) {
       synchronized (HttpClient.class) {
         if (cookies == null) {
           cookies = new LinkedHashMap<>();
         }
       }
     }
 
     return cookies;
   }
 
   private String cookiesString() {
 
     LinkedHashMap<String, String> cookies = cookies();
 
     if (cookies.size() > 0) {
 
       StringBuilder sb = new StringBuilder();
       boolean first = true;
 
       for (Map.Entry<String, String> kv : cookies.entrySet()) {
 
         if (first) {
           first = false;
         } else {
           sb.append(";");
         }
 
         sb.append(kv.getKey());
         sb.append("=");
         sb.append(kv.getValue());
       }
 
       return sb.toString();
     }
 
     return null;
 
   }
 
   public String check(URLConnection c) throws IOException {
 
     HttpURLConnection conn = (HttpURLConnection) c;
 
     switch (conn.getResponseCode()) {
     case HttpURLConnection.HTTP_MOVED_PERM:
     case HttpURLConnection.HTTP_MOVED_TEMP:
       return conn.getHeaderField("Location");
     case HttpURLConnection.HTTP_OK:
       return null;
     default:
       throw new IOException();
     }
   }
 
   /**
    * delete method
    * 
    * @param url
    *          url to delete
    * @return this object
    * @throws IOException
    */
   public SimpleEntry<URLConnection, String> DELETE(String url) throws IOException {
 
     SimpleEntry<URLConnection, String> r = cookies(LLHttpClient.DELETE(proxy(), url, connectTimeOut, readTimeOut,
         headers()));
 
     if ((url = check(r.getKey())) != null) { return GET(url); }
 
     return r;
   }
 
   public HttpClient follow(boolean follow) {
     this.follow = follow;
     return this;
   }
 
   /**
    * GET url
    * 
    * @param url
    *          to GET
    * @return this object
    * @throws IOException
    */
   public SimpleEntry<URLConnection, String> GET(String url) throws IOException {
 
     int redirects = 0;
     SimpleEntry<URLConnection, String> r;
 
     do {
       r = cookies(LLHttpClient.GET(proxy(), url, connectTimeOut, readTimeOut, headers()));
       url = check(r.getKey());
       redirects++;
     } while (follow && url != null && redirects < 5);
 
     return r;
   }
 
   private LinkedHashMap<String, String> headers() {
 
     if (headers == null) {
       synchronized (HttpClient.class) {
         if (headers == null) {
           headers = new LinkedHashMap<>();
         }
       }
     }
 
     return headers;
   }
 
   /**
    * directly put header
    * 
    * @param header
    *          header to put
    * @return this object
    */
   final public HttpClient header(Map.Entry<String, String> header) {
     headers().put(header.getKey(), header.getValue());
     return this;
   }
 
   /**
    * whether cookies should be persistence
    * 
    * @param bool
    * @return this object
    */
   public HttpClient persistCookies(boolean bool) {
     this.persistCookies = bool;
     return this;
   }
 
   /**
    * POST url
    * 
    * @param url
    *          url to POST
    * @param params
    *          params to POST
    * @return this object
    * @throws IOException
    */
   public SimpleEntry<URLConnection, String> POST(String url, Map<String, String> params) throws IOException {
 
     SimpleEntry<URLConnection, String> r = cookies(LLHttpClient.POST(proxy(), url, params, connectTimeOut, readTimeOut,
         headers()));
 
     if ((url = check(r.getKey())) != null) { return GET(url); }
 
     return r;
   }
 
   /**
    * Proxier to get proxy for each single request
    * 
    * @param proxier
    * @return this object
    * @see Proxier
    */
   public HttpClient proxier(Proxier proxier) {
     this.proxier = proxier;
     return this;
   }
 
   /**
    * get proxy vars
    * 
    * @return proxier.get() || proxy
    */
   public Proxy proxy() {
     return proxier != null ? proxier.get() : proxy;
   }
 
   /**
    * Fix proxy for each request
    * 
    * @param proxy
    * @return this object
    */
   public HttpClient proxy(Proxy proxy) {
     this.proxy = proxy;
     return this;
   }
 
   /**
    * read timeout
    * 
    * @param timeout
    *          timeout in ms
    * @return this object
    */
   public HttpClient read(int timeout) {
     this.readTimeOut = timeout;
     return this;
   }
 
   /**
    * Set URL referer
    * 
    * @param url
    *          URL Referer to set
    * @return this object
    */
   public HttpClient referer(String url) {
     headers().put("Referer", url);
     return this;
   }
 
   protected SimpleEntry<URLConnection, String> cookies(SimpleEntry<URLConnection, String> r) {
 
     final URLConnection conn = r.getKey();
     final String d = conn.getURL().getHost();
 
     if (!persistCookies) return r;
 
     for (Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
 
       if (header.getKey() == null || !header.getKey().equalsIgnoreCase("Set-Cookie")) {
         continue;
       }
 
       for (String cookie : header.getValue()) {
 
         String[] pieces = cookie.split(";");
 
         if (pieces[0].indexOf('=') <= 0) {
           continue;
         }
 
         String[] kv = pieces[0].split("=", 2);
 
         String key = kv[0];
         String val = kv[1];
 
         String domain = d;
 
         for (int i = 1; i < pieces.length; i++) {
 
           String[] p = pieces[i].split("=");
 
           if (p.length != 2) continue;
 
           switch (p[0].trim().toLowerCase()) {
           case "domain":
             domain = p[1].trim();
             break;
           case "path":
             break;
           case "expires":
             break;
           }
         }
 
         if (d.endsWith(domain)) {
           cookie(key, val);
         }
       }
     }
 
     return r;
   }
 }
