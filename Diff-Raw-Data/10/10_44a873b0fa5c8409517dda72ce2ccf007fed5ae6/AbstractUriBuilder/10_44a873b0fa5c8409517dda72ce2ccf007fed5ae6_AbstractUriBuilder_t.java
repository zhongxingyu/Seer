 package org.webreformatter.commons.uri;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.webreformatter.commons.uri.AbstractPathBuilder.PathParseListener;
 import org.webreformatter.commons.uri.Path.Builder;
 import org.webreformatter.commons.uri.UriParser.CompositeUriListener;
 import org.webreformatter.commons.uri.UriParser.IUriListener;
 
 /**
  * @author kotelnikov
  * @param <T>
  */
 public abstract class AbstractUriBuilder<T extends AbstractUriBuilder<T>>
     extends
     AbstractUri {
 
     public static class UriParseListener extends UriParser.UriListener {
         private AbstractUriBuilder<?> fBuilder;
 
         public UriParseListener(AbstractUriBuilder<?> builder) {
             fBuilder = builder;
         }
 
         @Override
         public void onFragment(String fragment) {
             fBuilder.setFragment(fragment);
         }
 
         @Override
         public void onHost(String host) {
             fBuilder.setHost(host);
         }
 
         @Override
         public void onPort(int port) {
             fBuilder.fPort = port;
         }
 
         @Override
         public void onQuery(String query) {
             fBuilder.setInternalQueryString(query);
         }
 
         @Override
         public void onScheme(List<String> segments) {
             fBuilder.fSchemeSegments = segments;
         }
 
         @Override
         public void onUserInfo(String userInfo) {
             fBuilder.setUserInfo(userInfo);
         }
 
     }
 
     private static <T extends AbstractUriBuilder<?>> void parseAuthority(
         final T uri,
         String authority) {
         UriParser.parseAuthority(authority, new UriParser.UriListener() {
             @Override
             public void onHost(String host) {
                 uri.setHost(host);
             }
 
             @Override
             public void onPort(int port) {
                 uri.fPort = port;
             }
 
             @Override
             public void onUserInfo(String userInfo) {
                 uri.setUserInfo(userInfo);
             }
         }, false);
     }
 
     private static <T extends AbstractUriBuilder<?>> void parseScheme(
         final T uri,
         String scheme) {
         uri.fSchemeSegments.clear();
         if (scheme != null && !"".equals(scheme)) {
             UriParser.parseScheme(scheme, new UriParser.UriListener() {
                 @Override
                 public void onScheme(List<String> segments) {
                     uri.fSchemeSegments = segments;
                 }
             }, true);
         }
     }
 
     private static <T extends AbstractUriBuilder<?>> void parseSchemeAndAuthority(
         final T uri,
         String hostPart) {
         UriParser.parseSchemeAndAuthority(
             hostPart,
             new UriParser.UriListener() {
                 @Override
                 public void onHost(String host) {
                     uri.setHost(host);
                 }
 
                 @Override
                 public void onPort(int port) {
                     uri.fPort = port;
                 }
 
                 @Override
                 public void onScheme(List<String> segments) {
                     uri.fSchemeSegments = segments;
                 }
 
                 @Override
                 public void onUserInfo(String userInfo) {
                     uri.setUserInfo(userInfo);
                 }
             });
     }
 
     private static List<QueryItem> toQueryItems(Map<String, String> params) {
         List<QueryItem> list = new ArrayList<AbstractUri.QueryItem>();
         for (Map.Entry<String, String> entry : params.entrySet()) {
             String name = entry.getKey();
             String value = entry.getValue();
             QueryItem item = new QueryItem(name, value);
             list.add(item);
         }
         return list;
     }
 
     private String fFragment;
 
     private String fHost;
 
     private Path.Builder fPath = new Path.Builder("");
 
     protected int fPort;
 
     private List<QueryItem> fQueryItems = new ArrayList<AbstractUri.QueryItem>();
 
     protected List<String> fSchemeSegments = new ArrayList<String>();
 
     private String fUserInfo;
 
     public AbstractUriBuilder() {
     }
 
     public AbstractUriBuilder(AbstractUri uri) {
         copyFrom(uri);
     }
 
     /**
      * 
      */
     public AbstractUriBuilder(String uri) {
         setUri(uri);
     }
 
     public T addParam(String key, String value) {
         return addParams(true, key, value);
     }
 
     public T addParam(String key, String value, boolean decode) {
         return addParams(decode, key, value);
     }
 
     public T addParams(boolean decode, String... keyValuePairs) {
         if (keyValuePairs == null || keyValuePairs.length == 0) {
             return cast();
         }
         if (fQueryItems == null) {
             fQueryItems = new ArrayList<QueryItem>();
         }
         int i = 0;
         int len = (keyValuePairs.length / 2) * 2;
         while (i < len) {
             String key = keyValuePairs[i++];
             String value = keyValuePairs[i++];
             QueryItem item = new QueryItem(key, value, decode);
             fQueryItems.add(item);
         }
         if (i != keyValuePairs.length) {
             String key = keyValuePairs[i];
             QueryItem item = new QueryItem(key, "");
             fQueryItems.add(item);
         }
         return cast();
     }
 
     public T addParams(Map<String, String> params) {
         if (params == null || params.size() == 0) {
             return cast();
         }
         List<QueryItem> list = toQueryItems(params);
         setQuery(list, false);
         return cast();
     }
 
     public T addParams(String... keyValuePairs) {
         return addParams(true, keyValuePairs);
     }
 
     /**
      * Returns a copy of this path with appended full path (local path + queries
      * + fragment).
      * 
      * @param path the full path to append
      * @param decode if this flag is <code>true</code> then individual segments
      *        of the appended path will be decoded
      * @return a copy of this path with appended path segments.
      */
     public T appendFullPath(AbstractPath path) {
         if (path == null) {
             return cast();
         }
         fPath.appendPath(path);
         checkPath();
         return cast();
     }
 
     public T appendFullPath(AbstractUri uri) {
         if (uri == null) {
             return cast();
         }
         AbstractPath path = uri.getAbstractPath();
         setQuery(uri.getQueryItems(), true);
         setFragment(uri.getFragment(), false);
         return appendFullPath(path);
     }
 
     /**
      * Returns a copy of this path with appended full path (local path + queries
      * + fragment).
      * 
      * @param path the full path to append
      * @return a copy of this path with appended path segments.
      */
     public T appendFullPath(String path) {
        fPath.appendPath(path, false, AbstractPath.DECODE_PATH);
         checkPath();
         return cast();
     }
 
     /**
      * Returns a copy of this path with appended full path (local path + queries
      * + fragment).
      * 
      * @param path the full path to append
      * @param decode if this flag is <code>true</code> then individual segments
      *        of the appended path will be decoded
      * @return a copy of this path with appended path segments.
      */
     public T appendFullPath(String path, boolean decode) {
         if (path == null) {
             return cast();
         }
         AbstractPathBuilder.parsePath(path, fPath, decode, false);
         setQuery(null, true);
         setFragment(null, false);
         checkPath();
         return cast();
     }
 
     /**
      * Returns a copy of this uri with added scheme segments. If the given flag
      * <code>begin</code> is <code>true</code> then the given segments will be
      * added at the begining of the current segment list. Otherwise they will be
      * appended to the end.
      * 
      * @param segments the scheme segments to append to the scheme part of this
      *        uri
      * @param begin if this flag is <code>true</code> then the given segments
      *        will be added at the begining of the URI's segment list; otherwise
      *        the segments will be appended to the end
      * @return a copy of this uri with appended scheme segments.
      * @see #setScheme(String)
      */
     public T appendSchemeSegments(String segments, boolean begin) {
         List<String> oldSegments = fSchemeSegments;
         fSchemeSegments = new ArrayList<String>();
         parseScheme(this, segments);
         if (begin) {
             fSchemeSegments.addAll(oldSegments);
         } else {
             fSchemeSegments.addAll(0, oldSegments);
         }
         return cast();
     }
 
     /**
      * Returns a copy of this uri with added scheme segments. If the given flag
      * <code>begin</code> is <code>true</code> then the given segments will be
      * added at the begining of the current segment. Otherwise they will be
      * appended to the end.
      * 
      * @param segments the scheme segments to append to the scheme part of this
      *        uri
      * @param begin if this flag is <code>true</code> then the given segments
      *        will be added at the begining of the URI's segment; otherwise the
      *        segments will be appended to the end
      * @return a copy of this uri with appended scheme segments.
      * @see #setScheme(String)
      */
     public T appendSchemeSegments(String[] segments, boolean begin) {
         int pos = begin ? 0 : fSchemeSegments.size();
         for (String segment : segments) {
             fSchemeSegments.add(pos, segment);
             pos++;
         }
         return cast();
     }
 
     @SuppressWarnings("unchecked")
     protected T cast() {
         checkPath();
         return (T) this;
     }
 
     @SuppressWarnings("unchecked")
     protected T cast(AbstractUri uri) {
         if (getClass().isInstance(uri)) {
             return (T) uri;
         }
         return newCopy(uri);
     }
 
     protected String check(String str) {
         if (str == null) {
             return str;
         }
         str = str.trim();
         if ("".equals(str)) {
             return null;
         }
         return str;
     }
 
     private void checkPath() {
         if (hasAuthority()) {
             if (!fPath.isEmpty()) {
                 fPath.makeAbsolutePath();
             }
         }
     }
 
     public void clear() {
         fSchemeSegments.clear();
         fUserInfo = null;
         fHost = null;
         fPort = 0;
         fPath.clear();
         fQueryItems.clear();
         fFragment = null;
     }
 
     private void copyFrom(AbstractUri uri) {
         if (uri == this) {
             return;
         }
         fSchemeSegments.clear();
         fSchemeSegments.addAll(uri.getSchemeSegments());
         setQuery(uri.getQueryItems(), true);
         setFragment(uri.getFragment(), false);
         setUserInfo(uri.getUserInfo());
         fPort = uri.getPort();
         setHost(uri.getHost());
         Builder builder = uri.getPathBuilder();
         fPath = builder.getCopy();
         checkPath();
     }
 
     @Override
     protected AbstractPath getAbstractPath() {
         return fPath;
     }
 
     @Override
     public String getFragment() {
         return fFragment;
     }
 
     /**
      * Returns a full path containing query string and fragment
      * 
      * @return a full path containing query string and fragment
      */
     public T getFullPathAsUri() {
         fSchemeSegments.clear();
         setUserInfo(null);
         setHost(null);
         fPort = 0;
         return cast();
     }
 
     @Override
     public String getHost() {
         return fHost;
     }
 
     @Override
     public Path getPath() {
         checkPath();
         return fPath.build();
     }
 
     public T getPathAsUri() {
         T uri = getFullPathAsUri();
         uri.setQuery(null, true);
         uri.setFragment(null, false);
         return uri;
     }
 
     @Override
     public Builder getPathBuilder() {
         return fPath;
     }
 
     @Override
     public int getPort() {
         return fPort;
     }
 
     @Override
     public List<QueryItem> getQueryItems() {
         return fQueryItems;
     }
 
     public T getRelative(AbstractPath path) {
         fPath.makeRelative(path);
         return cast();
     }
 
     public T getRelative(AbstractUri uri) {
         if (!(fSchemeSegments.equals(uri.getSchemeSegments())
             && AbstractPath.equals(getUserInfo(), uri.getUserInfo())
             && AbstractPath.equals(getHost(), uri.getHost()) && fPort == uri
                 .getPort())) {
             return cast(uri);
         }
         fSchemeSegments.clear();
         setUserInfo(null);
         setHost(null);
         fPort = 0;
         AbstractPath path = uri.getAbstractPath();
         getRelative(path);
         setQuery(uri.getQueryItems(), true);
         setFragment(uri.getFragment(), false);
         checkPath();
         return cast();
     }
 
     public T getResolved(AbstractPath path) {
         fPath.getResolved(path);
         checkPath();
         return cast();
     }
 
     public T getResolved(AbstractUri uri) {
         if (uri == null) {
             return cast();
         }
         if (uri.isAbsoluteUri()) {
             copyFrom(uri);
             return cast();
         }
         List<String> segments = uri.getSchemeSegments();
         if (!segments.isEmpty()) {
             copyFrom(uri);
             return cast();
         }
         setFragment(uri.getFragment(), false);
         setQuery(uri.getQueryItems(), true);
         Path path = uri.getPath();
         return getResolved(path);
     }
 
     @Override
     public List<String> getSchemeSegments() {
         return fSchemeSegments;
     }
 
     @Override
     public String getUri(boolean escape, boolean encode) {
         checkPath();
         return super.getUri(escape, encode);
     }
 
     @Override
     public String getUserInfo() {
         return fUserInfo;
     }
 
     /**
      * Create (if it is possible) a new uri with relative path. Note that if
      * this uri has authority information (user info, host, port...) then the
      * path of the uri can not be changed to a relative one. In this case this
      * uri will be returned.
      * 
      * @return a new uri with the relative path
      */
     public T makeRelativePath() {
         if (hasAuthority()) {
             return cast();
         }
         fPath.makeRelativePath();
         fSchemeSegments.clear();
         setUserInfo(null);
         setHost(null);
         fPort = 0;
         checkPath();
         return cast();
     }
 
     public T newCopy() {
         return newCopy(this);
     }
 
     protected abstract T newCopy(AbstractUri uri);
 
     public UriParser.IUriListener newParseListener() {
         CompositeUriListener listener = new CompositeUriListener();
         listener.addListener(newPathListener());
         listener.addListener(newUriListener());
         return listener;
     }
 
     public PathParseListener newPathListener() {
         return new PathParseListener(fPath);
     }
 
     public AbstractUriBuilder.UriParseListener newUriListener() {
         return new UriParseListener(this);
     }
 
     /**
      * Returns a copy of this uri with removed first scheme segments
      * 
      * @param count the number of segments to remove
      * @return an uri with removed first scheme segments
      */
     public T removeFirstSchemeSegments(int count) {
         return removeSchemeSegments(count, true);
     }
 
     /**
      * Returns a copy of this uri with removed last scheme segments
      * 
      * @param count the number of segments to remove
      * @return an uri with removed last scheme segments
      */
     public T removeLastSchemeSegments(int count) {
         return removeSchemeSegments(count, false);
     }
 
     public T removeParam(String key) {
         if (fQueryItems == null || fQueryItems.size() == 0 || key == null) {
             return cast();
         }
         for (int i = fQueryItems.size() - 1; i >= 0; i--) {
             QueryItem item = fQueryItems.get(i);
             if (key.equals(item.name)) {
                 fQueryItems.remove(i);
             }
         }
         if (fQueryItems.isEmpty()) {
             fQueryItems = null;
         }
         return cast();
     }
 
     /**
      * Returns a copy of this uri with removed scheme segments. If the given
      * <code>begin</code> flag is <code>true</code> then this method removes
      * segments at the begining of the segment list; otherwise the specified
      * number of segments will be removed from the end of the segment list.
      * 
      * @param count the number of scheme segments to remove
      * @param begin if this flag is <code>true</code> then this method removes
      *        segments at the begining of the segment list; otherwise the
      *        specified number of segments will be removed from the end of the
      *        segment list.
      * @return an uri with removed scheme segments
      */
     public T removeSchemeSegments(int count, boolean begin) {
         int len = fSchemeSegments.size();
         count = Math.max(0, Math.min(count, len));
         int pos = begin ? 0 : len - count;
         for (int i = 0; i < count; i++) {
             fSchemeSegments.remove(pos);
         }
         return cast();
     }
 
     /**
      * @param params a map of parameters to serialize a query string
      * @return a string with serialized query parameters from the given map
      */
     protected String serializeQueryParams(Map<String, String> params) {
         StringBuffer buf = new StringBuffer();
         for (Map.Entry<String, String> entry : params.entrySet()) {
             String key = entry.getKey();
             if (key == null) {
                 continue;
             }
             if (buf.length() > 0) {
                 buf.append("&");
             }
             String str = AbstractPath.encode(key, true, true);
             buf.append(str);
             buf.append("=");
             String value = entry.getValue();
             if (value != null) {
                 str = AbstractPath.encode(value, true, true);
                 buf.append(str);
             }
         }
         return buf.toString();
     }
 
     /**
      * @param authority
      * @return a new uri with the specified authority part (user info, host and
      *         port)
      */
     public T setAuthority(String authority) {
         setUserInfo(null);
         setHost(null);
         fPort = 0;
         if (authority != null) {
             parseAuthority(this, authority);
         }
         return cast();
     }
 
     /**
      * Returns a new uri with the specified host and port. This method returns
      * an uri without user info.
      * 
      * @param host the host to set for the new uri
      * @param port the port to set for the new uri
      * @return a new uri with a new authority part
      */
     public T setAuthority(String host, int port) {
         return setAuthority(null, host, port);
     }
 
     /**
      * Returns a new uri with the specified host, port and user info.
      * 
      * @param userInfo the user info to set
      * @param host the host to set for the new uri
      * @param port the port to set for the new uri
      * @return a new uri with a new authority part
      */
     public T setAuthority(String userInfo, String host, int port) {
         setUserInfo(userInfo);
         setHost(host);
         fPort = port;
         return cast();
     }
 
     /**
      * Returns a new uri with the specified fragment part
      * 
      * @param fragment a fragment to set the new uri
      * @param decode if this flag is <code>true</code> then this method will
      *        decode the fragment
      * @return a new uri with the specified fragment part
      */
     public T setFragment(String fragment) {
         return setFragment(fragment, true);
     }
 
     /**
      * Returns a new uri with the specified fragment part
      * 
      * @param fragment a fragment to set the new uri
      * @param decode if this flag is <code>true</code> then this method will
      *        decode the fragment
      * @return a new uri with the specified fragment part
      */
     public T setFragment(String fragment, boolean decode) {
         fragment = check(fragment);
         if (isEmpty(fragment)) {
             fFragment = null;
         } else {
             fFragment = decode ? AbstractPath.decode(fragment) : fragment;
         }
         return cast();
     }
 
     public T setFullPath(AbstractPath path) {
         fPath.setPath(path);
         checkPath();
         return cast();
     }
 
     public T setFullPath(AbstractUri uri) {
         if (uri == null) {
             fPath.setPath("");
             fQueryItems.clear();
             setFragment("", false);
         } else {
             fPath.setPath(uri.getPath());
             fQueryItems.clear();
             fQueryItems.addAll(uri.getQueryItems());
             setFragment(uri.getFragment(), false);
         }
         checkPath();
         return cast();
     }
 
     /**
      * @param host
      * @return a new uri with the specified host
      */
     public T setHost(String host) {
         fHost = check(host);
         checkPath();
         return cast();
     }
 
     /**
      * The internal method used to initialize the {@link #fQueryItems} field and
      * reset the {@link #fQueryCache} field to <code>null</code>.
      * 
      * @param query the query to set
      */
     private void setInternalQueryString(String query) {
         if (query == null) {
             return;
         }
         fQueryItems.clear();
         String[] array = query.split("[&]");
         for (String str : array) {
             String[] pair = str.split("=");
             String name = pair[0].trim();
             if ("".equals(name)) {
                 continue;
             }
             String value = pair.length > 1 ? pair[1] : "";
             QueryItem item = new QueryItem(name, value);
             fQueryItems.add(item);
         }
     }
 
     /**
      * Returns a new uri with the specified port. All other parts will be copied
      * from this uri.
      * 
      * @param port the port to set
      * @return a new uri with the specified port
      */
     public T setPort(int port) {
         fPort = port;
         checkPath();
         return cast();
     }
 
     /**
      * The internal method used to initialize the {@link #fQueryItems} field and
      * reset the {@link #fQueryCache} field to <code>null</code>.
      * 
      * @param query the query to set
      */
     public T setQuery(List<QueryItem> query, boolean clear) {
         if (clear) {
             fQueryItems.clear();
         }
         if (query != null) {
             fQueryItems.addAll(query);
         }
         return cast();
     }
 
     /**
      * Returns a new uri with the specified query part. All other parts will be
      * copied from this uri.
      * 
      * @param params query parameters to set
      * @return a new copy of this with the given query
      */
     public T setQuery(Map<String, String> params) {
         List<QueryItem> list = toQueryItems(params);
         setQuery(list, true);
         return cast();
     }
 
     /**
      * Returns a new uri with the specified query part. All other parts will be
      * copied from this uri.
      * 
      * @param query a query to set
      * @return a new copy of this with the given query
      */
     public T setQuery(String query) {
         setInternalQueryString(query);
         return cast();
     }
 
     /**
      * Returns a new uri with the specified scheme. All other parts will be
      * copied from this uri.
      * 
      * @param scheme the scheme to set
      * @return a new uri with the specified scheme
      */
     public T setScheme(String scheme) {
         parseScheme(this, scheme);
         return cast();
     }
 
     /**
      * Returns a new uri with the given scheme and authority parts (scheme, user
      * info, host, port). All other parts (path, query, fragment) will be copied
      * from this uri.
      * 
      * @param hostPart the host part of the uri to get
      * @return a new uri with the given scheme and authority parts (scheme, user
      *         info, host, port)
      */
     public T setSchemeAndAuthority(String hostPart) {
         fSchemeSegments.clear();
         setUserInfo(null);
         setHost(null);
         fPort = 0;
         if (hostPart != null) {
             parseSchemeAndAuthority(this, hostPart);
         }
         return cast();
     }
 
     public void setUri(String uri) {
         clear();
         IUriListener listener = newParseListener();
         uri = uri.trim();
         UriParser.parse(uri, listener);
     }
 
     /**
      * Returns a new uri with the specified user information. All other parts
      * will be copied from this uri.
      * 
      * @param userInfo the user information to set
      * @return a new uri with the specified user information
      */
     public T setUserInfo(String userInfo) {
         fUserInfo = check(userInfo);
         checkPath();
         return cast();
     }
 
 }
