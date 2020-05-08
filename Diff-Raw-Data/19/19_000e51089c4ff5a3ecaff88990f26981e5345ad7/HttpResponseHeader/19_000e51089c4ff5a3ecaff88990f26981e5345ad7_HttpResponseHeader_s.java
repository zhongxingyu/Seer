 package com.akrivos.eos.http.constants;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * An enum with all the response headers.
  * Taken from https://github.com/bigcompany/know-your-http
  */
 public enum HttpResponseHeader {
     AccessControlAllowOrigin("Access-Control-Allow-Origin"),
     AcceptRanges("Accept-Ranges"),
     Age("Age"),
     Allow("Allow"),
     CacheControl("Cache-Control"),
     Connection("Connection"),
     ContentEncoding("Content-Encoding"),
     ContentLanguage("Content-Language"),
     ContentLength("Content-Length"),
     ContentLocation("Content-Location"),
     ContentMd5("Content-MD5"),
     ContentDisposition("Content-Disposition"),
     ContentType("Content-Type"),
     Etag("Etag"),
     Expires("Expires"),
     LastModified("Last-Modified"),
     Link("Link"),
     Location("Location"),
     P3P("P3P"),
     Pragma("Pragma"),
     ProxyAuthenticate("Proxy-Authenticate"),
     RetryAfter("Retry-After"),
     Server("Server"),
     SetCookie("Set-Cookie"),
     Status("Status"),
     StrictTransportSecurity("Strict-Transport-Security"),
     Trailer("Trailer"),
     TransferEncoding("Transfer-Encoding"),
     Vary("Vary"),
     Via("Via"),
     Warning("Warning"),
    WWWAuthenticate("WWW-Authenticate"),
    Refresh("Refresh");
 
     /**
      * Keep a map containing all the enums with their {@link String} value.
      * http://stackoverflow.com/questions/1167982/check-if-enum-exists-in-java
      */
     private static final Map<String, HttpResponseHeader> nameToValueMap =
             new HashMap<String, HttpResponseHeader>();
     private final String name;
 
     /**
      * Populates the map using EnumSet.allOf, which is much more efficient for
      * enums without a large number of elements.
      */
     static {
         for (HttpResponseHeader value : EnumSet.allOf(HttpResponseHeader.class)) {
            nameToValueMap.put(value.name(), value);
         }
     }
 
     /**
      * Creates a new {@link HttpResponseHeader} with the specified header name.
      *
      * @param name the {@link String} representation of a {@link HttpResponseHeader}.
      */
     private HttpResponseHeader(String name) {
         this.name = name;
     }
 
     /**
      * Parses a {@link String} and tries to match the {@link HttpResponseHeader}.
      *
      * @param name the {@link String} representation of a {@link HttpResponseHeader}.
      * @return the {@link HttpResponseHeader} if found, null otherwise.
      */
     public static HttpResponseHeader forName(String name) {
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
