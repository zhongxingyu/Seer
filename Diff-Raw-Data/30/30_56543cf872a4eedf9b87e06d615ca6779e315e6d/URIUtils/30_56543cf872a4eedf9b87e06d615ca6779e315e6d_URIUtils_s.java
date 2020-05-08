 package com.tjh.swivel.utils;
 
 import vanderbilt.util.PopulatingMap;
 
 import java.lang.reflect.Constructor;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 public class URIUtils {
 
     public static Map<String, List<String>> createMapFromQueryString(String queryString) {
         if (queryString == null) return Collections.emptyMap();
         try {
             Constructor constructor = ArrayList.class.getConstructor();
             Map<String, List<String>> result = new PopulatingMap<String, List<String>>(constructor);
             for (String entry : queryString.split("\\&")) {
                 String[] keyVal = entry.split("=");
                 result.get(keyVal[0]).add(keyVal[1]);
             }
             return result;
         } catch (NoSuchMethodException e) {
             throw new RuntimeException("Programmer error", e);
         }
     }
 
     public static URI resolveUri(URI baseUri, URI requestedUri, URI matchedUri) {
         String relativePath = requestedUri
                 .getPath()
                 .substring(matchedUri.getPath().length());
        return baseUri.resolve(URI.create(relativePath));
     }
 }
