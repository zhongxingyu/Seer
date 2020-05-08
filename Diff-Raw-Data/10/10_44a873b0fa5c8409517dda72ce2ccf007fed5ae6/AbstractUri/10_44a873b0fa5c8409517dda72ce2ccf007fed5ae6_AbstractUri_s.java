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
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
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
  *    URI         = scheme &quot;:&quot; hier-part [ &quot;?&quot; query ] [ &quot;#&quot; fragment ]
  *    hier-part   = &quot;//&quot; authority path-abempty
  *                / path-absolute
  *                / path-rootless
  *                / path-empty
  * 
  * 4.2 Relative Reference:
  *      relative-ref  = relative-part [ &quot;?&quot; query ] [ &quot;#&quot; fragment ]
  *      relative-part = &quot;//&quot; authority path-abempty
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
  *         userinfo    host     port
  *           __|__   ____|____   _|
  *          /     \ /         \ /  \
  *    foo://usr:pwd@example.com:8042/over/there?name=ferret#nose
  *    \_/   \______________________/\_________/ \_________/ \__/
  *     |               |               |            |       |
  *  scheme         authority         path        query   fragment
  *     |_________________________  ____|___   _____|___   _|
  *    /                          \/        \ /         \ /  \
  *    urn:example:a:b:c:d:e:f:g:h/over/there?name=ferret#nose
  * 
  *  Note:
  *    Such an uri should not be parsed - it is considered as an &quot;opaque&quot; uri.
  *    But this library split it to different parts to simplify operations with
  *    such uris. The first part is considered as an array of schemas
  *    (urn:example:a:b:c:d:e)
  * </pre>
  * 
  * @author kotelnikov
  */
 public abstract class AbstractUri {
 
     public static class QueryItem {
 
         final String name;
 
         private final String value;
 
         public QueryItem(String name, String value) {
             this(name, value, true);
         }
 
         public QueryItem(String name, String value, boolean decode) {
             this.name = decode ? AbstractPath
                 .decode(!isEmpty(name) ? name : "") : name;
             this.value = decode ? AbstractPath.decode(!isEmpty(value)
                 ? value
                 : "") : value;
         }
 
         public void appendPair(
             StringBuilder builder,
             boolean escape,
             boolean encode) {
             String key = getName(escape, encode);
             String value = getValue(escape, encode);
             builder.append(key);
             builder.append("=");
             builder.append(value);
         }
 
         /**
          * @see java.lang.Object#equals(java.lang.Object)
          */
         @Override
         public boolean equals(Object obj) {
             if (obj == this) {
                 return true;
             }
             if (!(obj instanceof AbstractUri.QueryItem)) {
                 return false;
             }
             AbstractUri.QueryItem o = (AbstractUri.QueryItem) obj;
             return name.equals(o.name) && value.equals(o.value);
         }
 
         public String getName(boolean escape, boolean encode) {
             if (!escape && !encode) {
                 return name;
             }
             return AbstractPath.encode(name, escape, encode);
         }
 
         public String getValue(boolean escape, boolean encode) {
             if (!escape && !encode) {
                 return value;
             }
             return AbstractPath.encode(value, escape, encode);
         }
 
         /**
          * @see java.lang.Object#hashCode()
          */
         @Override
         public int hashCode() {
             int a = name.hashCode();
             int b = name.hashCode();
             return a ^ b;
         }
 
         /**
          * @see java.lang.Object#toString()
          */
         @Override
         public String toString() {
             StringBuilder builder = new StringBuilder();
             appendPair(builder, false, false);
             return builder.toString();
         }
     }
 
     /**
      * Appends authority information to the given buffer string
      * 
      * @param buf here the authority information will be added
      */
     private static <T extends AbstractUri> void appendAuthority(
         T uri,
         StringBuffer buf) {
         String userInfo = uri.getUserInfo();
         String host = uri.getHost();
         int port = uri.getPort();
         if (!isEmpty(userInfo)) {
             buf.append(userInfo);
             buf.append("@");
         }
         if (!isEmpty(host)) {
             buf.append(host);
         }
         if (port > 0) {
             buf.append(":");
             buf.append(port);
         }
     }
 
     protected static <T extends AbstractUri> void appendFullPath(
         T uri,
         StringBuffer buf,
         boolean escape,
         boolean encode) {
         Path fPath = uri.getPath();
         if (fPath.isAbsolutePath() || !fPath.getPathSegments().isEmpty()) {
             AbstractPath.appendPath(fPath, buf, escape, encode);
         }
         String query = uri.getQuery(escape, encode);
         if (!isEmpty(query)) {
             buf.append("?");
             buf.append(query);
         }
         String fragment = uri.getFragment(escape, encode);
         if (!isEmpty(fragment)) {
             buf.append("#");
             buf.append(fragment);
         }
     }
 
     public static boolean isEmpty(String fragment) {
         return fragment == null || "".equals(fragment);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         if (!(obj instanceof AbstractUri)) {
             return false;
         }
         AbstractUri o = (AbstractUri) obj;
         return equals(getSchemeSegments(), o.getSchemeSegments())
             && equals(getUserInfo(), o.getUserInfo())
             && equals(getHost(), o.getHost())
             && (getPort() == o.getPort())
             && equals(getAbstractPath(), o.getAbstractPath())
             && equals(getQueryItems(), o.getQueryItems())
             && equals(getFragment(), o.getFragment());
     }
 
     private boolean equals(Object first, Object second) {
         return first != null && second != null
             ? first.equals(second)
             : first == second;
     }
 
     protected abstract AbstractPath getAbstractPath();
 
     /**
      * @return the authority part of the URI
      */
     public String getAuthority() {
         String userInfo = getUserInfo();
         String host = getHost();
         int port = getPort();
         if (!isEmpty(userInfo) || !isEmpty(host) || port != 0) {
             StringBuffer buf = new StringBuffer();
             appendAuthority(this, buf);
             return buf.toString();
         } else {
             return null;
         }
     }
 
     /**
      * @return return the non-encoded fragment
      */
     public abstract String getFragment();
 
     /**
      * @param encode if this flag is <code>true</code> then the returned
      *        fragment is hex-encoded (like %0D...)
      * @return return the escaped fragment
      */
     public String getFragment(boolean encode) {
         return getFragment(true, encode);
     }
 
     /**
      * @param escape if this parameter is <code>true</code> then all space
      *        symbols in the fragment will be "escaped" (replaced by the "+"
      *        symbol)
      * @param encode if this parameter is <code>true</code> then all specifal
      *        (non-URI) symbols will be encoded in the form "%xx".
      * @return the fragment part of the uri
      */
     public String getFragment(boolean escape, boolean encode) {
         String result = getFragment();
         if (escape || encode) {
             result = AbstractPath.encode(result, escape, encode);
         }
         return result;
     }
 
     /**
      * Returns a full path containing query string and fragment
      * 
      * @return a full path containing query string and fragment
      */
     public String getFullPath() {
         return getFullPath(AbstractPath.ENCODE_PATH);
     }
 
     /**
      * Returns a full path containing query string and fragment
      * 
      * @param encode if this parameter is <code>true</code> then the returned
      *        path is UTF-8 URL encoded
      * @return a full path containing query string and fragment
      */
     public String getFullPath(boolean encode) {
         return getFullPath(true, encode);
     }
 
     /**
      * Returns a full path containing query string and fragment
      * 
      * @param escape if this flag is <code>true</code> then spaces in the
      *        returned path are replaced by '+' symbols
      * @param encode if this parameter is <code>true</code> then the returned
      *        path is UTF-8 URL encoded
      * @return a full path containing query string and fragment
      */
     public String getFullPath(boolean escape, boolean encode) {
         AbstractPath path = getAbstractPath();
         return path.getPath(escape, encode);
     }
 
     /**
      * @return the host of this uri
      */
     public abstract String getHost();
 
     public abstract Path getPath();
 
     public abstract Path.Builder getPathBuilder();
 
     /**
      * @return the port number of this uri
      */
     public abstract int getPort();
 
     /**
      * @return the query part of the uri
      */
     public String getQuery() {
         return getQuery(false, false);
     }
 
     /**
      * @param escape if this parameter is <code>true</code> then all space
      *        symbols in the query will be "escaped" (replaced by the "+"
      *        symbol)
      * @param encode if this parameter is <code>true</code> then all special
      *        (non-URI) symbols will be encoded in the form "%xx".
      * @return the fragment part of the uri
      */
     public String getQuery(boolean escape, boolean encode) {
         List<QueryItem> query = getQueryItems();
         if (query == null || query.isEmpty()) {
             return null;
         }
         StringBuilder builder = new StringBuilder();
         for (QueryItem item : query) {
             if (builder.length() > 0) {
                 builder.append("&");
             }
             item.appendPair(builder, escape, encode);
         }
         String result = builder.toString();
         return result;
     }
 
     /**
      * Returns a list of query items corresponding to this URI.
      * 
      * @return a list of query items
      */
     public abstract List<QueryItem> getQueryItems();
 
     public Map<String, List<String>> getQueryMap() {
         return getQueryMap(true, false);
     }
 
     /**
      * Parses the internal query string and returns a map of corresponding
      * key/value pairs defined by the query.
      * 
      * @param queryString the string to parse
      * @return a map containing key/value pairs defined in the query of this URI
      */
     public Map<String, List<String>> getQueryMap(boolean escape, boolean encode) {
         Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
         List<QueryItem> query = getQueryItems();
         if (query != null) {
             for (QueryItem item : query) {
                 String name = item.getName(escape, encode);
                 List<String> list = result.get(name);
                 if (list == null) {
                     list = new ArrayList<String>();
                     result.put(name, list);
                 }
                 String value = item.getValue(escape, encode);
                 list.add(value);
             }
         }
         return result;
     }
 
     /**
      * @return the scheme part of this uri
      */
     public String getScheme() {
         return getScheme(-1);
     }
 
     /**
      * Returns the specified number of scheme segments serialized as a string.
      * 
      * @param count the number of first segments to show
      * @return the scheme part of this uri
      */
     public String getScheme(int count) {
         List<String> schemeSegments = getSchemeSegments();
         if (schemeSegments.isEmpty()) {
             return null;
         }
         int size = schemeSegments.size();
         count = Math.min(count, size);
         if (count < 0) {
             count = size;
         }
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < count; i++) {
             String segment = schemeSegments.get(i);
             if (i > 0) {
                 buf.append(":");
             }
             if (!isEmpty(segment)) {
                 buf.append(segment);
             }
         }
         return buf.length() > 0 ? buf.toString() : null;
     }
 
     /**
      * Returns a scheme segment from the given position
      * 
      * @param pos from this position a scheme segment will be returned
      * @return a scheme segment from the given position
      */
     public String getSchemeSegment(int pos) {
         List<String> schemeSegments = getSchemeSegments();
         if (pos < 0 || pos >= schemeSegments.size()) {
             return null;
         }
         return schemeSegments.get(pos);
     }
 
     /**
      * @return the number of the scheme segments
      */
     public int getSchemeSegmentCount() {
         List<String> schemeSegments = getSchemeSegments();
         return schemeSegments.size();
     }
 
     /**
      * @return an array of all scheme segments in this uri
      */
     public abstract List<String> getSchemeSegments();
 
     /**
      * @return the string representation of the uri
      */
     public String getUri() {
         return getUri(AbstractPath.ENCODE_PATH);
     }
 
     /**
      * Returns the string representation of this URI <strong>without</strong>
      * UTF-8 encoding of the returned URI.
      * 
      * @param encode if this flag is <code>true</code> then this method encodes
      *        all path segments (if it is required) in the resulting string.
      * @return the string representation of the URI
      */
     public String getUri(boolean encode) {
         return getUri(true, encode);
     }
 
     /**
      * Returns the string representation of the full URI.
      * 
      * @param escape if this flag is <code>true</code> then spaces in the URI
      *        path are replaced by '+' symbols
      * @param encode if this parameter is <code>true</code> then the returned
      *        path is UTF-8 encoded
      * @return the string representation of the URI
      */
     public String getUri(boolean escape, boolean encode) {
         StringBuffer result = new StringBuffer();
         getUri(result, escape, encode);
         return result.toString();
     }
 
     /**
      * Appends the full string representation of the full URI to the given
      * string buffer.
      * 
      * @param result the string buffer where the full URI should be appended
      * @param escape if this flag is <code>true</code> then spaces in the URI
      *        path are replaced by '+' symbols
      * @param encode if this parameter is <code>true</code> then the returned
      *        path is UTF-8 encoded
      */
     public void getUri(StringBuffer result, boolean escape, boolean encode) {
         List<String> schemeSegments = getSchemeSegments();
         if (schemeSegments != null && !schemeSegments.isEmpty()) {
             for (String segment : schemeSegments) {
                 if (!isEmpty(segment)) {
                     segment = AbstractPath.encode(segment, escape, encode);
                     result.append(segment);
                 }
                 result.append(":");
             }
         }
 
         if (hasAuthority()) {
            result.append("//");
             appendAuthority(this, result);
         }
         appendFullPath(this, result, escape, encode);
     }
 
     /**
      * @return the user info of this uri
      */
     public abstract String getUserInfo();
 
     /**
      * @return <code>true</code> if this uri has an authority part (user info,
      *         host or port
      */
     public boolean hasAuthority() {
         return !isEmpty(getUserInfo()) || !isEmpty(getHost()) || getPort() != 0;
     }
 
     @Override
     public int hashCode() {
         int a = hashCode(getSchemeSegments());
         int b = hashCode(getUserInfo());
         int c = hashCode(getHost());
         int d = getPort();
         int e = hashCode(getAbstractPath());
         int f = hashCode(getQueryItems());
         int g = hashCode(getFragment());
         int hashCode = 1;
         hashCode = 31 * hashCode + a;
         hashCode = 31 * hashCode + b;
         hashCode = 31 * hashCode + c;
         hashCode = 31 * hashCode + d;
         hashCode = 31 * hashCode + e;
         hashCode = 31 * hashCode + f;
         hashCode = 31 * hashCode + g;
         return hashCode;
     }
 
     private int hashCode(Object o) {
         return o != null ? o.hashCode() : 0;
     }
 
     public boolean hasSchema() {
         List<String> schemeSegments = getSchemeSegments();
         return schemeSegments != null && !schemeSegments.isEmpty();
     }
 
     /**
      * @return <code>true</code> if this uri is an absolute uri: it contains
      *         host and (maybe) user info.
      */
     public boolean isAbsoluteUri() {
         return !isEmpty(getUserInfo()) || !isEmpty(getHost());
     }
 
     @Override
     public String toString() {
         return getUri();
     }
 
 }
