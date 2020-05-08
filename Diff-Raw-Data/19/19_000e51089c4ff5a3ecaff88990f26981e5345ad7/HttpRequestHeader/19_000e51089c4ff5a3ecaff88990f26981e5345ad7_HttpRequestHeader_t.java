 package com.akrivos.eos.http.constants;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * An enum with all the request headers.
  * Taken from https://github.com/bigcompany/know-your-http
  */
 public enum HttpRequestHeader {
     Accept("Accept"),
     AcceptCharset("Accept-Charset"),
     AcceptEncoding("Accept-Encoding"),
     AcceptLanguage("Accept-Language"),
     AcceptDatetime("Accept-Datetime"),
     Authorization("Authorization"),
     CacheControl("Cache-Control"),
     Connection("Connection"),
     Cookie("Cookie"),
     ContentLength("Content-Length"),
     ContentMd5("Content-MD5"),
     ContentType("Content-Type"),
     Date("Date"),
     Expect("Expect"),
     From("From"),
     Host("Host"),
     IfMatch("If-Match"),
     IfModifiedSince("If-Modified-Since"),
     IfNoneMatch("If-None-Match"),
     IfRange("If-Range"),
     IfUnmodifiedSince("If-Unmodified-Since"),
     MaxForwards("Max-Forwards"),
     Origin("Origin"),
     Pragma("Pragma"),
    ProxyAuthorization("Proxy-Authorization"),
     Range("Range"),
     Referer("Referer"),
     TE("TE"),
     Upgrade("Upgrade"),
     UserAgent("User-Agent"),
     Via("Via"),
     Warning("Warning");
 
     /**
      * Keep a map containing all the enums with their {@link String} value.
      * http://stackoverflow.com/questions/1167982/check-if-enum-exists-in-java
      */
     private static final Map<String, HttpRequestHeader> nameToValueMap =
             new HashMap<String, HttpRequestHeader>();
     private final String name;
 
     /**
      * Populates the map using EnumSet.allOf, which is much more efficient for
      * enums without a large number of elements.
      */
     static {
         for (HttpRequestHeader value : EnumSet.allOf(HttpRequestHeader.class)) {
            nameToValueMap.put(value.getName(), value);
         }
     }
 
     /**
      * Creates a new {@link HttpRequestHeader} with the specified header name.
      *
      * @param name the {@link String} representation of a {@link HttpRequestHeader}.
      */
     private HttpRequestHeader(String name) {
         this.name = name;
     }
 
     /**
      * Parses a {@link String} and tries to match the {@link HttpRequestHeader}.
      *
      * @param name the {@link String} representation of a {@link HttpRequestHeader}.
      * @return the {@link HttpRequestHeader} if found, null otherwise.
      */
     public static HttpRequestHeader forName(String name) {
         return nameToValueMap.get(name);
     }
 
     /**
      * Returns the name of the header.
      *
      * @return the name of the header.
      */
     public String getName() {
         return name;
     }
 }
