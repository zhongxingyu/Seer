 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.utils;
 
 
 import ru.histone.evaluator.functions.global.URI;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class PathUtils {
 
     public static final String URL_DIRNAME_REGEXP = "^(.*)\\/";
     public static final String URL_PARSER_REGEXP = "^(?:([^:\\/?\\#]+):)?(?:\\/\\/([^\\/?\\#]*))?([^?\\#]*)(?:\\?([^\\#]*))?(?:\\#(.*))?";
     private static final String SLASH = "/";
     private static final String QUESTION = "?";
     private static final String DOUBLE_SLASH = "//";
     private static final String COLON = ":";
     private static final String HASH = "#";
     private static final String DOT = ".";
     private static final String DOUBLE_DOT = "..";
     private static final char CHAR_SLASH = '/';
     private static final String EMPTY = "";
     
    
     public static String resolveUrl(String relativeUrl, String baseUrl)  {
 
         StringBuilder result = new StringBuilder();
 
         URI relUri = parseURI(relativeUrl);
         URI baseUri = parseURI(baseUrl);
 
         if (StringUtils.isNotEmpty(relUri.getScheme())) {
             result.append((relUri.getScheme() + COLON));
             if (StringUtils.isNotEmpty(relUri.getAuthority())) {
                 result.append((DOUBLE_SLASH + relUri.getAuthority()));
             }
             if (StringUtils.isNotEmpty(removeDotSegments(relUri.getPath()))) {
                 result.append(removeDotSegments(relUri.getPath()));
             }
             if (StringUtils.isNotEmpty(relUri.getQuery())) {
                 result.append((QUESTION + relUri.getQuery()));
             }
         } else {
             if (StringUtils.isNotEmpty(baseUri.getScheme())) {
                 result.append((baseUri.getScheme() + COLON));
             }
             if (StringUtils.isNotEmpty(relUri.getAuthority())) {
                 result.append((DOUBLE_SLASH + relUri.getAuthority()));
                 if (StringUtils.isNotEmpty(removeDotSegments(relUri.getPath()))) {
                     result.append(removeDotSegments(relUri.getPath()));
                 }
                 if (StringUtils.isNotEmpty(relUri.getQuery())) {
                     result.append((QUESTION + relUri.getQuery()));
                 }
             } else {
                 if (StringUtils.isNotEmpty(baseUri.getAuthority())) {
                     result.append((DOUBLE_SLASH + baseUri.getAuthority()));
                 }
                 if (StringUtils.isNotEmpty(relUri.getPath())) {
                     StringBuilder pathStringBuilder = new StringBuilder();
                     if (relUri.getPath().charAt(0) == CHAR_SLASH) {
                         pathStringBuilder.append(relUri.getPath());
                     } else {
                         if (StringUtils.isNotEmpty(baseUri.getAuthority()) && StringUtils.isEmpty(baseUri.getPath())) {
                             pathStringBuilder.append(SLASH);
                         } else {
                             pathStringBuilder.append(resolvePath(baseUri.getPath()));
                         }
                         pathStringBuilder.append(relUri.getPath());
                     }
                     String resolvedPath = removeDotSegments(pathStringBuilder.toString());
                     if (StringUtils.isNotEmpty(resolvedPath)) {
                         result.append(resolvedPath);
                     }
                     if (StringUtils.isNotEmpty(relUri.getQuery())) {
                         result.append((QUESTION + relUri.getQuery()));
                     }
                 } else {
                     if (StringUtils.isNotEmpty(baseUri.getPath())) {
                         result.append(baseUri.getPath());
                     }
                     if (StringUtils.isNotEmpty(relUri.getQuery())) {
                         result.append((QUESTION + relUri.getQuery()));
                     } else if (StringUtils.isNotEmpty(baseUri.getQuery())) {
                         result.append((QUESTION + baseUri.getQuery()));
                     }
                 }
             }
         }
         if (StringUtils.isNotEmpty(relUri.getFragment())) {
             result.append((HASH + relUri.getFragment()));
         }
         return result.toString();
     }
 
     private static String removeDotSegments(String path) {
         String[] splitedPath = path.split(SLASH);
         if (splitedPath.length < 1) {
             return path;
         }
         boolean isAbsolute = StringUtils.isEmpty(splitedPath[0]);
         List<String> result = new ArrayList<String>();
         String fragment = EMPTY;
         int startIndex = isAbsolute ? 1 : 0;
         for (int i = startIndex; i < splitedPath.length; i++) {
             fragment = splitedPath[i];
             if (fragment.equals(DOUBLE_DOT)) {
                 if (result.size() >= 1) {
                     result.remove(result.size() - 1);
                 }
             } else if (!fragment.equals(DOT)) {
                 result.add(fragment);
             }
         }
         if (fragment.equals(DOT) || fragment.equals(DOUBLE_DOT)) {
             result.add(EMPTY);
         }
         StringBuilder builder = new StringBuilder();
         for (String element : result) {
             builder.append(SLASH).append(element);
         }
         if (!isAbsolute && builder.length() > 0) {
             builder.deleteCharAt(0);
         }
         if (path.endsWith(SLASH) && builder.charAt(builder.length() - 1) != CHAR_SLASH) {
             builder.append(SLASH);
         }
         return builder.toString();
     }
 
     public static String resolvePath(String path) {
         Pattern parser = Pattern.compile(URL_DIRNAME_REGEXP);
         Matcher matcher = parser.matcher(path);
         if (matcher.find()) {
             return matcher.group(0);
         }
         return EMPTY;
     }
 
     public static URI parseURI(String uriString) {
         Pattern parser = Pattern.compile(URL_PARSER_REGEXP);
         Matcher matcher = parser.matcher(uriString);
         URI uri = new URI();
 
         if (matcher.find()) {
             uri.setScheme(matcher.group(1));
             uri.setAuthority(matcher.group(2));
             uri.setPath(matcher.group(3));
             uri.setQuery(matcher.group(4));
             uri.setFragment(matcher.group(5));
         }
         return uri;
     }
     
     public static String prepareWinPathForUri(String path){
         String result = path.replace("\\", "/");
         if (!result.startsWith(".") && !result.startsWith("/") && !result.startsWith("file:/")) {
             result = "file:/" + result;
         }
         return result;
     }
 
 }
