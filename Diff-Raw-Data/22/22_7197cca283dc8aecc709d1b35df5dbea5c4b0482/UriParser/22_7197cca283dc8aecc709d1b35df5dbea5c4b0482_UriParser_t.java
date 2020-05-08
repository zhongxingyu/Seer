 /* ************************************************************************** *
  * See the NOTICE file distributed with this work for additional information
  * regarding copyright ownership.
  * 
  * This file is licensed to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  * ************************************************************************** */
 package org.webreformatter.commons.uri;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class is an URI parser splitting the given string to different URI
  * parts. For more information see http://www.ietf.org/rfc/rfc3986.txt &
  * http://www.ietf.org/rfc/rfc2396.txt.
  * <p>
  * From the specification (http://www.ietf.org/rfc/rfc3986.txt):
  * </p>
  * "The generic URI syntax consists of a hierarchical sequence of components
  * referred to as the scheme, authority, path, query, and fragment.
  * 
  * <pre>
  * 3. Syntax Components:
  *    URI         = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
  *    hier-part   = "//" authority path-abempty
  *                / path-absolute
  *                / path-rootless
  *                / path-empty
  *                
  * 4.2 Relative Reference:
  *      relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
  *      relative-part = "//" authority path-abempty
  *                       / path-absolute
  *                       / path-noscheme
  *                       / path-empty
  * </pre>
  * 
  * "The scheme and path components are required, though the path may be empty
  * (no characters). When authority is present, the path must either be empty or
  * begin with a slash ("/") character. When authority is not present, the path
  * cannot begin with two slash characters ("//")."
  * 
  * <pre>
  * Example1:
  * 
  *   scheme  userinfo   domain    port     path       query    fragment
  *    _|_   ____|____ ____|______ _|__ _____|_____ _____|_____ _|__
  *    foo://login:pwd@example.com:8042/over/there/?name=ferret#nose
  *
  *
  * Example2 - an opaque (non interpreted) URI:
  * 
  *   opaque scheme  userinfo   domain    port     path       query  fragment
  *    _____|_____  ____|____ ____|______ _|__ _____|_____ _____|_____ _|__
  *    image:http://login:pwd@example.com:8042/over/there/?name=ferret#nose
  *
  *           opaque scheme      path            query             fragment
  *    ___________|_________ _____|_____ __________|_________________ _|__
  *    urn:example:a:b:c:d:e/over/there?firstName=John&lastName=Smith#nose
  * 
  *  Note for Example2:
  *    Such URIs should not be parsed - they are considered as "opaque" (non 
  *    interpreted) URIs.  But this library splits them to different parts by 
  *    interpreting the first part as "scheme segments" and these segments using 
  *    a list (like "image:http" and "urn:example:a:b:c:d:e"). 
  *    See {@link IUriListener#onScheme(List)}
  * </pre>
  * 
  * @author kotelnikov
  */
 public class UriParser {
 
     public static class CompositeUriListener implements IUriListener {
 
         private List<IUriListener> fList = new ArrayList<IUriListener>();
 
         public void addListener(IUriListener listener) {
             fList.add(listener);
         }
 
         public void onFragment(String fragment) {
             for (IUriListener listener : fList) {
                 listener.onFragment(fragment);
             }
         }
 
         public void onHost(String host) {
             for (IUriListener listener : fList) {
                 listener.onHost(host);
             }
         }
 
         public void onPath(
             boolean absolute,
             List<String> segments,
             boolean hasTrailingSeparator) {
             for (IUriListener listener : fList) {
                 listener.onPath(absolute, segments, hasTrailingSeparator);
             }
         }
 
         public void onPort(int port) {
             for (IUriListener listener : fList) {
                 listener.onPort(port);
             }
         }
 
         public void onQuery(String query) {
             for (IUriListener listener : fList) {
                 listener.onQuery(query);
             }
         }
 
         public void onScheme(List<String> segments) {
             for (IUriListener listener : fList) {
                 listener.onScheme(segments);
             }
         }
 
         public void onUserInfo(String userInfo) {
             for (IUriListener listener : fList) {
                 listener.onUserInfo(userInfo);
             }
         }
 
         public void removeListener(IUriListener listener) {
             fList.remove(listener);
         }
 
     }
 
     public interface IUriListener {
 
         void onFragment(String fragment);
 
         void onHost(String host);
 
         void onPath(
             boolean absolute,
             List<String> segments,
             boolean hasTrailingSeparator);
 
         void onPort(int port);
 
         void onQuery(String query);
 
         void onScheme(List<String> segments);
 
         void onUserInfo(String userInfo);
 
     }
 
     public static class UriListener implements IUriListener {
 
         public void onFragment(String fragment) {
             //
         }
 
         public void onHost(String host) {
             //
         }
 
         public void onPath(
             boolean absolute,
             List<String> segments,
             boolean hasTrailingSeparator) {
             //
         }
 
         public void onPort(int port) {
             //
         }
 
         public void onQuery(String query) {
             //
         }
 
         public void onScheme(List<String> segments) {
             //
         }
 
         public void onUserInfo(String userInfo) {
             //
         }
 
     }
 
     /**
      * @param uri
      * @param listener
      * @see IUriListener#onScheme(List)
      * @see IUriListener#onUserInfo(String)
      * @see IUriListener#onHost(String)
      * @see IUriListener#onPort(int)
      * @see IUriListener#onPath(boolean, List, boolean)
      * @see IUriListener#onQuery(String)
      * @see IUriListener#onFragment(String)
      */
     public static void parse(char[] uri, IUriListener listener) {
         UriParser parser = new UriParser(listener);
         int pos = 0;
         pos = parser.parseSchemeAndAuthority(uri, pos);
         parser.parseFullPath(uri, pos);
     }
 
     /**
      * @param uri
      * @param listener
      * @see IUriListener#onScheme(List)
      * @see IUriListener#onUserInfo(String)
      * @see IUriListener#onHost(String)
      * @see IUriListener#onPort(int)
      * @see IUriListener#onPath(boolean, List, boolean)
      * @see IUriListener#onQuery(String)
      * @see IUriListener#onFragment(String)
      */
     public static void parse(String uri, IUriListener listener) {
         if (uri == null) {
             uri = "";
         }
         char[] array = uri.toCharArray();
         parse(array, listener);
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                _____________
      * </pre>
      * 
      * @param authority
      * @param listener
      * @param check
      * @see IUriListener#onUserInfo(String)
      * @see IUriListener#onHost(String)
      * @see IUriListener#onPort(int)
      */
     public static void parseAuthority(
         char[] authority,
         IUriListener listener,
         boolean check) {
         UriParser parser = new UriParser(listener);
         parser.parseAuthority(authority, 0, check);
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                _____________
      * </pre>
      * 
      * @param authority
      * @param listener
      * @param check
      * @see IUriListener#onUserInfo(String)
      * @see IUriListener#onHost(String)
      * @see IUriListener#onPort(int)
      */
     public static void parseAuthority(
         String authority,
         IUriListener listener,
         boolean check) {
         if (authority == null) {
             authority = "";
         }
         char[] array = authority.toCharArray();
         parseAuthority(array, listener, check);
     }
 
     /**
      * <pre>
      * Path:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                              ________
      *
      * Query:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                                          _______
      *
      * Fragment:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                                                   ______
      * </pre>
      * 
      * @param path
      * @param listener
      * @see IUriListener#onPath(boolean, List, boolean)
      * @see IUriListener#onQuery(String)
      * @see IUriListener#onFragment(String)
      */
     public static void parseFullPath(char[] path, IUriListener listener) {
         UriParser parser = new UriParser(listener);
         parser.parseFullPath(path, 0);
     }
 
     /**
      * <pre>
      * Path:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                              ________
      *
      * Query:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                                          _______
      *
      * Fragment:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                                                   ______
      * </pre>
      * 
      * @param path
      * @param listener
      * @see IUriListener#onPath(boolean, List, boolean)
      * @see IUriListener#onQuery(String)
      * @see IUriListener#onFragment(String)
      */
     public static void parseFullPath(String path, IUriListener listener) {
         if (path == null) {
             path = "";
         }
         char[] array = path.toCharArray();
         parseFullPath(array, listener);
     }
 
     /**
      * <pre>
      * Path:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                              ________
      * </pre>
      * 
      * @param path
      * @param listener
      * @see IUriListener#onPath(boolean, List, boolean)
      */
     public static void parsePath(char[] path, IUriListener listener) {
         UriParser parser = new UriParser(listener);
         parser.parsePath(path, 0);
     }
 
     /**
      * <pre>
      * Path:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                              ________
      * </pre>
      * 
      * @param path
      * @param listener
      * @see IUriListener#onPath(boolean, List, boolean)
      */
     public static void parsePath(String path, IUriListener listener) {
         if (path == null) {
             path = "";
         }
         char[] array = path.toCharArray();
         parsePath(array, listener);
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *    __________
      * </pre>
      * 
      * @param scheme
      * @param listener
      * @param includeTail
      * @see IUriListener#onScheme(List)
      */
     public static void parseScheme(
         char[] scheme,
         UriListener listener,
         boolean includeTail) {
         UriParser parser = new UriParser(listener);
         parser.parseScheme(scheme, 0, includeTail);
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *    __________
      * </pre>
      * 
      * @param scheme
      * @param listener
      * @param includeTail
      * @see IUriListener#onScheme(List)
      */
     public static void parseScheme(
         String scheme,
         UriListener listener,
         boolean includeTail) {
         if (scheme == null) {
             scheme = "";
         }
         char[] array = scheme.toCharArray();
         parseScheme(array, listener, includeTail);
     }
 
     /**
      * <pre>
      * Scheme:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *    __________
      *
      * Authority:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                _____________
      * </pre>
      * 
      * @param schemeAndAuthority
      * @param listener
      * @see IUriListener#onScheme(List)
      * @see IUriListener#onUserInfo(String)
      * @see IUriListener#onHost(String)
      * @see IUriListener#onPort(int)
      */
     public static void parseSchemeAndAuthority(
         char[] schemeAndAuthority,
         IUriListener listener) {
         UriParser parser = new UriParser(listener);
         parser.parseSchemeAndAuthority(schemeAndAuthority, 0);
     }
 
     /**
      * <pre>
      * Scheme:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *    __________
      *
      * Authority:
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                _____________
      * </pre>
      * 
      * @param schemeAndAuthority
      * @param listener
      * @see IUriListener#onScheme(List)
      * @see IUriListener#onUserInfo(String)
      * @see IUriListener#onHost(String)
      * @see IUriListener#onPort(int)
      */
     public static void parseSchemeAndAuthority(
         String schemeAndAuthority,
         IUriListener listener) {
         if (schemeAndAuthority == null) {
             schemeAndAuthority = "";
         }
         char[] array = schemeAndAuthority.toCharArray();
         parseSchemeAndAuthority(array, listener);
     }
 
     private IUriListener fListener;
 
     /**
      * 
      */
     private UriParser(IUriListener listener) {
         super();
         fListener = listener;
     }
 
     private String getStr(char[] array, int begin, int end) {
         return begin >= 0 && end > begin
             ? new String(array, begin, end - begin)
             : null;
 
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                _____________
      * </pre>
      * 
      * @param array
      */
     private int parseAuthority(char[] array, int pos, boolean check) {
         if (pos >= array.length) {
             return -1;
         }
         /**
          * <pre>
          * // ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          * //                 __
          * </pre>
          */
         if (check) {
             boolean validAuthority = (pos < array.length - 2
                 && array[pos] == '/' && array[pos + 1] == '/');
             if (!validAuthority) {
                 return -1;
             }
             pos += 2;
         }
         int start = pos;
         /**
          * <pre>
          * // ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          * //                    _______
          * </pre>
          */
         int portPos = -1;
         int hostStart = start;
         boolean validPort = false;
         loop: while (pos < array.length) {
             char ch = array[pos];
            if (pos == hostStart && ch == '.') {
                break;
            }
             switch (ch) {
                 case '/':
                 case '?':
                 case '#':
                     break loop;
                 case '@':
                     String userInfo = getStr(array, start, pos);
                     fListener.onUserInfo(userInfo);
                     hostStart = pos + 1;
                     break;
                 case ':':
                     portPos = pos + 1;
                     validPort = true;
                     break;
                 default:
                     if (portPos > 0) {
                         validPort &= Character.isDigit(ch);
                     }
                     break;
             }
             pos++;
         }
 
         if (portPos >= 0 && validPort && portPos < array.length) {
             String str = getStr(array, portPos, pos);
             try {
                 int port = Integer.parseInt(str);
                 fListener.onPort(port);
                 portPos--;
             } catch (NumberFormatException e) {
                 portPos = pos;
             }
         } else {
             portPos = pos;
         }
         String host = getStr(array, hostStart, portPos);
         fListener.onHost(host);
         return pos;
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                                                   ______
      * </pre>
      * 
      * @param array
      */
     private int parseFragment(char[] array, int pos) {
         if (pos >= array.length) {
             return -1;
         }
         /**
          * <pre>
          * // ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          * //
          * </pre>
          */
         char ch = array[pos++];
         if (ch != '#') {
             return -1;
         }
         /**
          * <pre>
          * // ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          * //                                                    ____
          * </pre>
          */
         int start = pos;
         pos = array.length;
         String fragment = getStr(array, start, pos);
         fListener.onFragment(fragment);
         return pos;
     }
 
     private void parseFullPath(char[] array, int pos) {
         int fix = pos;
         /**
          * <pre>
          * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          *                              ________
          * </pre>
          */
         pos = parsePath(array, pos);
         if (pos < 0) {
             pos = fix;
         } else {
             fix = pos;
         }
 
         /**
          * <pre>
          * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          *                                          _______
          * </pre>
          */
         pos = parseQuery(array, pos);
         if (pos < 0) {
             pos = fix;
         } else {
             fix = pos;
         }
 
         /**
          * <pre>
          * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          *                                                   ______
          * </pre>
          */
         pos = parseFragment(array, pos);
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                              ________
      * </pre>
      * 
      * @param array
      */
     private int parsePath(char[] array, int pos) {
         List<String> segments = new ArrayList<String>();
         int firstPos = pos;
         int start = pos;
         boolean absolute = false;
         boolean separator = false;
         while (pos < array.length) {
             if (array[pos] == '?' || array[pos] == '#') {
                 break;
             }
             separator = (array[pos] == '\\' || array[pos] == '/');
             if (separator) {
                 if (pos == firstPos) {
                     absolute = true;
                     separator = false;
                 }
                 if (pos > start) {
                     segments.add(getStr(array, start, pos));
                 }
                 start = pos + 1;
             }
             pos++;
         }
         if (pos > start) {
             segments.add(getStr(array, start, pos));
         }
         boolean hasTrailingSeparator = separator;
         fListener.onPath(absolute, segments, hasTrailingSeparator);
         return pos;
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *                                          _______
      * </pre>
      * 
      * @param array
      */
     private int parseQuery(char[] array, int pos) {
         if (pos >= array.length) {
             return -1;
         }
         // ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         // _
         if (array[pos++] != '?') {
             return -1;
         }
 
         int start = pos;
         // ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         // _______
         for (; pos < array.length; pos++) {
             char ch = array[pos];
             if (ch == '#') {
                 break;
             }
         }
         String query = getStr(array, start, pos);
         fListener.onQuery(query);
         return pos;
     }
 
     /**
      * <pre>
      * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
      *    __________
      * </pre>
      * 
      * @param array
      */
     private int parseScheme(char[] array, int pos, boolean includeTail) {
         int firstPos = pos;
         List<String> segments = new ArrayList<String>();
         loop: while (pos < array.length) {
             char ch = array[pos];
             switch (ch) {
                 case ':':
                     segments.add(getStr(array, firstPos, pos));
                     firstPos = pos + 1;
                     break;
                 case '/':
                 case '?':
                 case '#':
                     break loop;
                 default:
                     break;
             }
             pos++;
         }
         if (includeTail) {
             String segment = getStr(array, firstPos, pos);
             if (segment != null) {
                 segments.add(segment);
             }
         } else {
             pos = firstPos;
         }
         fListener.onScheme(segments);
         return pos;
     }
 
     private int parseSchemeAndAuthority(char[] array, int pos) {
         int fix = 0;
         /**
          * <pre>
          * http://foo.bar.com/my/local/path?parameter=value#Anchor
          * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          *    __________      _______    ______    _______    ____
          *    1               2          3         4          5
          * Where:
          *   1 - scheme     (http)
          *   2 - authority  (foo.bar.com)
          *   3 - path       (/my/local/path)
          *   4 - query      (parameter=value)
          *   5 - fragment   (Anchor)
          * </pre>
          */
 
         /**
          * <pre>
          * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          *    __________
          * </pre>
          */
         pos = parseScheme(array, pos, false);
         if (pos < 0) {
             pos = fix;
         } else {
             fix = pos;
         }
         /**
          * <pre>
          * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
          *                _____________
          * </pre>
          */
         pos = parseAuthority(array, pos, true);
         if (pos < 0) {
             pos = fix;
         } else {
             fix = pos;
         }
         return pos;
     }
 
 }
