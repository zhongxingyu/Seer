 /*
    Copyright (C) 2005-2012, by the President and Fellows of Harvard College.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
          http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
    Dataverse Network - A web application to share, preserve and analyze research data.
    Developed at the Institute for Quantitative Social Science, Harvard University.
    Version 3.0.
 */
 /*
 
 Copyright (c) 2000-2012 Board of Trustees of Leland Stanford Jr. University,
 all rights reserved.
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
 Except as contained in this notice, the name of Stanford University shall not
 be used in advertising or otherwise to promote the sale, use or other dealings
 in this Software without prior written authorization from Stanford University.
 
 */
 
 //DVN CUSTOM -- BEGIN
 package edu.harvard.iq.dvn.lockss.plugin;
 //DVN CUSTOM -- END
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.text.*;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.lockss.app.*;
 import org.lockss.state.*;
 import org.lockss.config.*;
 import org.lockss.plugin.*;
 import org.lockss.plugin.base.BaseArchivalUnit;
 import org.lockss.repository.*;
 import org.lockss.util.*;
 import org.lockss.util.urlconn.*;
 import org.lockss.daemon.*;
 import org.lockss.crawler.*;
 
 //DVN CUSTOM -- BEGIN
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.httpclient.HttpClient;
 //import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 import edu.harvard.iq.dvn.core.web.dvnremote.DvnTermsOfUseAccess;
 //DVN CUSTOM -- END
 
 /**
  * Basic, fully functional UrlCacher.  Utilizes the LockssRepository for
  * caching, and {@link LockssUrlConnection}s for fetching.  Plugins may
  * extend this to achieve, <i>eg</i>, specialized host connection or
  * authentication.  The redirection semantics offered here must be
  * preserved.
  */
 //DVN CUSTOM -- BEGIN
 public class DVNOAIUrlCacher implements UrlCacher {
     protected static Logger logger = Logger.getLogger("DVNOAIUrlCacher");
   //DVN CUSTOM -- END
 
   /** If true, normalize redirect targets (location header). */
   public static final String PARAM_NORMALIZE_REDIRECT_URL =
     Configuration.PREFIX + "baseuc.normalizeRedirectUrl";
   public static final boolean DEFAULT_NORMALIZE_REDIRECT_URL = true;
 
   /** The algorithm to use for content checksum calculation. An empty value disables checksums */
   public static final String PARAM_CHECKSUM_ALGORITHM =
 		    Configuration.PREFIX + "baseuc.checksumAlgorithm";
   public static final String DEFAULT_CHECKSUM_ALGORITHM = null;
 
   /** Limit on rewinding the network input stream after checking for a
    * login page.  If LoginPageChecker returns false after reading father
    * than this the page will be refetched. */
   public static final String PARAM_LOGIN_CHECKER_MARK_LIMIT =
     Configuration.PREFIX + "baseuc.loginPageCheckerMarkLimit";
   public static final int DEFAULT_LOGIN_CHECKER_MARK_LIMIT = 24 * 1024;
 
   /** Maximum number of redirects that will be followed */
   //DVN CUSTOM -- BEGIN
   static final int MAX_REDIRECTS = 1;
   //DVN CUSTOM -- END
 
   // Preferred date format according to RFC 2068(HTTP1.1),
   // RFC 822 and RFC 1123
   public static final SimpleDateFormat GMT_DATE_FORMAT =
     new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
   static {
     GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
   }
 
   protected final ArchivalUnit au;
   protected final String origUrl;		// URL with which I was created
   protected String fetchUrl;		// possibly affected by redirects
   private List otherNames;
   protected int redirectOptions = REDIRECT_OPTION_FOLLOW_AUTO;
   private LockssUrlConnectionPool connectionPool;
   private LockssUrlConnection conn;
   private final LockssRepository repository;
   private final NodeManager nodeMgr;
   private final CacheResultMap resultMap;
   private CIProperties uncachedProperties;
   private PermissionMapSource permissionMapSource;
   private String proxyHost = null;
   private int proxyPort;
   private IPAddr localAddr = null;
   private Properties reqProps;
   private LockssWatchdog wdog;
   private String previousContentType;
   private CrawlRateLimiter crl;
   private BitSet fetchFlags = new BitSet();
 
   private static final String SHOULD_REFETCH_ON_SET_COOKIE =
     "refetch_on_set_cookie";
   private static final boolean DEFAULT_SHOULD_REFETCH_ON_SET_COOKIE = true;
 
 //DVN CUSTOM -- BEGIN
   private HttpClient client = null;
 
   private HttpClient getClient() {
     //if (client == null) {
     //    client = new HttpClient( new MultiThreadedHttpConnectionManager() );
     //}
     client = new HttpClient();
     return client;
   }
 //DVN CUSTOM -- END
 
   
   //DVN CUSTOM -- BEGIN
   public DVNOAIUrlCacher(ArchivalUnit owner, String url) {
   //DVN CUSTOM -- END
     this.origUrl = url;
     this.fetchUrl = url;
     //au = owner.getArchivalUnit();
     au = owner;
     //DVN CUSTOM -- BEGIN
     DVNOAIPlugin plugin = (DVNOAIPlugin)au.getPlugin();
     //DVN CUSTOM -- END
 
     repository = plugin.getDaemon().getLockssRepository(au);
     nodeMgr = plugin.getDaemon().getNodeManager(au);
     logger.debug3("Node manager "+nodeMgr);
     //DVN CUSTOM -- BEGIN
     resultMap = plugin.getCacheResultMap();
     //DVN CUSTOM -- END
 
   }
 
   /**
    * Returns the original URL (the one the UrlCacher was created with),
    * independent of any redirects followed.
    * @return the url string
    */
   public String getUrl() {
     return origUrl;
   }
 
   /**
    * Overrides normal <code>toString()</code> to return a string like
    * "BUC: <url>"
    * @return the class-url string
    */
   public String toString() {
     return "[BUC: "+origUrl+"]";
   }
 
   /**
    * Return the CachedUrlSet to which this UrlCacher belongs.
    * @return the owner CachedUrlSet
    * @deprecated Not used, kept for plugin binary compatibility
    */
   public CachedUrlSet getCachedUrlSet() {
     throw new UnsupportedOperationException("No longer implemented");
   }
 
   /**
    * Return the ArchivalUnit to which this UrlCacher belongs.
    * @return the owner ArchivalUnit
    */
   public ArchivalUnit getArchivalUnit() {
     return au;
   }
 
   /**
    * Return <code>true</code> if the underlying url is one that the plug-in
    * believes should be preserved.  (<i>Ie</i>, is within the AU's
    * crawlSpec.)
    * @return a boolean indicating if it should be cached
    */
   public boolean shouldBeCached() {
     return au.shouldBeCached(origUrl);
   }
 
   /**
    * Return a CachedUrl for the content stored.  May be
    * called only after the content is completely written.
    * @return CachedUrl for the content stored.
    */
   public CachedUrl getCachedUrl() {
     return au.makeCachedUrl(origUrl);
   }
 
   public void setConnectionPool(LockssUrlConnectionPool connectionPool) {
     this.connectionPool = connectionPool;
   }
 
   public void setProxy(String proxyHost, int proxyPort) {
     this.proxyHost = proxyHost;
     this.proxyPort = proxyPort;
   }
 
   public void setLocalAddress(IPAddr localAddr) {
     this.localAddr = localAddr;
   }
 
   public void setFetchFlags(BitSet fetchFlags) {
     this.fetchFlags = fetchFlags;
   }
 
   public BitSet getFetchFlags() {
     return fetchFlags;
   }
 
   public void setRequestProperty(String key, String value) {
     if (reqProps == null) {
       reqProps = new Properties();
     }
     reqProps.put(key, value);
   }
 
   public void setRedirectScheme(RedirectScheme scheme) {
     if (logger.isDebug3()) logger.debug3("setRedirectScheme: " + scheme);
     this.redirectOptions = scheme.getOptions();
   }
 
   public void setWatchdog(LockssWatchdog wdog) {
     this.wdog = wdog;
   }
 
   public void setPreviousContentType(String previousContentType) {
     this.previousContentType = previousContentType;
   }
 
   public void setCrawlRateLimiter(CrawlRateLimiter crl) {
     this.crl = crl;
   }
 
   private boolean isDamaged() {
     DamagedNodeSet dnSet = nodeMgr.getDamagedNodes();
     if (dnSet == null) {
       return false;
     }
     return dnSet.hasDamage(origUrl);
   }
 
   public int cache() throws IOException {
     String lastModified = null;
     if (!fetchFlags.get(REFETCH_FLAG) &&
 	!(fetchFlags.get(REFETCH_IF_DAMAGE_FLAG) && isDamaged())) {
       CachedUrl cachedVersion = getCachedUrl();
 
       // if it's been cached, get the last modified date and use that
       if ((cachedVersion!=null) && cachedVersion.hasContent()) {
 	CIProperties cachedProps = cachedVersion.getProperties();
 	lastModified =
 	  cachedProps.getProperty(CachedUrl.PROPERTY_LAST_MODIFIED);
 	cachedVersion.release();
       }
     }
     return cache(lastModified);
   }
 
   private int cache(String lastModified) throws IOException {
     InputStream input = getUncachedInputStream(lastModified);
     // null input indicates unmodified content, so skip caching
     if (input == null) {
       return CACHE_RESULT_NOT_MODIFIED;
     }
     try {
       CIProperties headers = getHeaders();
       if (headers.get("Set-Cookie") != null) {
 	if (shouldRefetchOnCookies()) {
 	  logger.debug3("Found set-cookie header, refetching");
 	  IOUtil.safeClose(input);
 	  input = null; // ensure don't reclose in finally if next line throws
 	  releaseConnection();
 	  input = getUncachedInputStream(lastModified);
 	  if (input == null) {
 	    //this is odd if it happens.
 	    logger.warning("Got null input stream on second call to getUncachedInputStream");
 	    return CACHE_RESULT_NOT_MODIFIED;
 	  }
 	  headers = getHeaders();
 	}
       }
       storeContent(input, headers);
       if (fetchFlags.get(CLEAR_DAMAGE_FLAG)) {
 	DamagedNodeSet dnSet = nodeMgr.getDamagedNodes();
 	if (dnSet != null) {
 	  CachedUrl cu = getCachedUrl();
 	  if (cu.isLeaf()) {
 	    logger.debug3("Removing "+fetchUrl+" from damaged set");
 	    dnSet.removeFromDamage(fetchUrl);
 	  } else {
 	    logger.debug3(fetchUrl + " isn't a leaf, so not removing it " +
 			  "from damaged set");
 	  }
 	}
       }
       return CACHE_RESULT_FETCHED;
     } finally {
       IOUtil.safeClose(input);
     }
   }
 
   /**
    * Reset the UrlCacher to its pre-opened state, so that it can be
    * reopened.
    */
   public void reset() {
     releaseConnection();
     fetchUrl = origUrl;
     otherNames = null;
     uncachedProperties = null;
   }
 
   /**
    * Try to reset the provided input stream, if we can't then return
    * new input stream for the given url
    */
   private InputStream resetInputStream(InputStream is, String url,
 				       String lastModified)
       throws IOException {
     try {
       is.reset();
     } catch (IOException e) {
       logger.debug("Couldn't reset input stream, so getting new one", e);
       is.close();
       releaseConnection();
       is = new BufferedInputStream(getUncachedInputStreamOnly(lastModified));
     }
     return is;
   }
 
   private InputStream checkLoginPage(InputStream input, Properties headers,
 				     String lastModified)
       throws IOException {
     LoginPageChecker checker = au.getCrawlSpec().getLoginPageChecker();
     if (checker != null) {
       logger.debug3("Found a login page checker");
       if (!input.markSupported()) {
         input = new BufferedInputStream(input);
       }
       input.mark(CurrentConfig.getIntParam(PARAM_LOGIN_CHECKER_MARK_LIMIT,
 					   DEFAULT_LOGIN_CHECKER_MARK_LIMIT));
       Reader reader = new InputStreamReader(input, Constants.DEFAULT_ENCODING);
       try {
 	if (checker.isLoginPage(headers, reader)) {
 	  throw new CacheException.PermissionException("Found a login page");
 	} else {
 	  input = resetInputStream(input, fetchUrl, lastModified);
 	}
       } catch (PluginException e) {
         throw new RuntimeException(e);
       }	
     } else {
       logger.debug3("Didn't find a login page checker");
     }
     return input;
   }
 
   private boolean shouldRefetchOnCookies() {
     //DVN CUSTOM -- BEGIN
     return false;
     //DVN CUSTOM -- END
   }
 
   private CIProperties getHeaders() throws IOException {
     CIProperties headers = getUncachedProperties();
     if (headers == null) {
       String err = "Received null headers for url '" + origUrl + "'.";
       logger.error(err);
       throw new NullPointerException(err);
     }
     return headers;
   }
 
   /** Store into the repository the content and headers from a successful
    * fetch.  If redirects were followed and
    * REDIRECT_OPTION_STORE_ALL was specified, store the content and
    * headers under each name in the chain of redirections.
    */
   public void storeContent(InputStream input, CIProperties headers)
       throws IOException {
     if (logger.isDebug2()) logger.debug2("Storing url '"+ origUrl +"'");
     storeContentIn(origUrl, input, headers);
     if (logger.isDebug3()) {
       logger.debug3("otherNames: " + otherNames);
       logger.debug3("isStoreAll: " +
 		    isRedirectOption(REDIRECT_OPTION_STORE_ALL));
     }
     if (otherNames != null &&
 	isRedirectOption(REDIRECT_OPTION_STORE_ALL)) {
       CachedUrl cu = getCachedUrl();
       CIProperties headerCopy  = CIProperties.fromProperties(headers);
       int last = otherNames.size() - 1;
       for (int ix = 0; ix <= last; ix++) {
 	String name = (String)otherNames.get(ix);
 	if (logger.isDebug2())
 	  logger.debug2("Storing in redirected-to url: " + name);
 	InputStream is = cu.getUnfilteredInputStream();
 	try {
 	  if (ix < last) {
 	    // this one was redirected, set its redirected-to prop to the
 	    // next in the list.
 	    headerCopy.setProperty(CachedUrl.PROPERTY_REDIRECTED_TO,
 				   (String)otherNames.get(ix + 1));
 	  } else if (!name.equals(fetchUrl)) {
 	    // Last in list.  If not same as fetchUrl, means the final
 	    // redirection was a directory(slash) redirection, which we don't
 	    // store as a different name or put on otherNames.  Indicate the
 	    // redirection to the slashed version.  The proxy must be aware
 	    // of this.  (It can't rely on this property being present,
 	    // becuase foo/ might later be fetched, not due to a redirect
 	    // from foo.)
 	    headerCopy.setProperty(CachedUrl.PROPERTY_REDIRECTED_TO, fetchUrl);
 	  } else {
 	    // This is the name that finally got fetched, don't store
 	    // redirect prop or content-url
 	    headerCopy.remove(CachedUrl.PROPERTY_REDIRECTED_TO);
 	    headerCopy.remove(CachedUrl.PROPERTY_CONTENT_URL);
 	  }
 	  storeContentIn(name, is, headerCopy);
 	} finally {
 	  IOUtil.safeClose(is);
 	}
       }
     }
   }
 
   public void storeContentIn(String url, InputStream input,
 			     CIProperties headers)
       throws IOException {
     RepositoryNode leaf = null;
     OutputStream os = null;
     try {
       try {
 	leaf = repository.createNewNode(url);
 	leaf.makeNewVersion();
 
 	MessageDigest checksumProducer = null;
 	String checksumAlgorithm =
 	  CurrentConfig.getParam(PARAM_CHECKSUM_ALGORITHM,
 				 DEFAULT_CHECKSUM_ALGORITHM);
 	if (!StringUtil.isNullString(checksumAlgorithm)) {
 	  try {
 	    checksumProducer = MessageDigest.getInstance(checksumAlgorithm);
 	  } catch (NoSuchAlgorithmException ex) {
 	    logger.warning(String.format("Checksum algorithm %s not found, checksuming disabled", checksumAlgorithm));
 	  }
 	}
 
 	os = leaf.getNewOutputStream();
 	StreamUtil.copy(input, os, -1, wdog, true, checksumProducer);
 	if (!fetchFlags.get(DONT_CLOSE_INPUT_STREAM_FLAG)) {
 	  try {
 	    input.close();
 	  } catch (IOException ex) {
 	    CacheException closeEx =
 	      resultMap.mapException(au, conn, ex, null);
 	    if (!(closeEx instanceof CacheException.IgnoreCloseException)) {
 	      throw new StreamUtil.InputException(ex);
 	    }
 	  }
 	}
 	os.close();
 	headers.setProperty(CachedUrl.PROPERTY_NODE_URL, url);
 	if (checksumProducer != null) {
 	  byte bdigest[] = checksumProducer.digest();
 	  String sdigest = ByteArray.toHexString(bdigest);
 	  headers.setProperty(CachedUrl.PROPERTY_CHECKSUM,
 			      String.format("%s:%s", checksumAlgorithm, sdigest));
 	}
 	leaf.setNewProperties(headers);
 	leaf.sealNewVersion();
       } catch (StreamUtil.InputException ex) {
 	throw resultMap.mapException(au, conn, ex.getIOCause(), null);
       } catch (StreamUtil.OutputException ex) {
 	throw resultMap.getRepositoryException(ex.getIOCause());
       }
     } catch (IOException ex) {
       logger.debug("storeContentIn1", ex);
       if (leaf != null) {
 	try {
 	  leaf.abandonNewVersion();
 	} catch (Exception e) {
 	  // just being paranoid
 	}
       }
       throw ex;
     } catch (Exception ex) {
       logger.debug("storeContentIn2", ex);
       if (leaf != null) {
 	try {
 	  leaf.abandonNewVersion();
 	} catch (Exception e) {
 	  // just being paranoid
 	}
       }
       throw resultMap.getRepositoryException(ex);
     } finally {
       IOUtil.safeClose(os);
     }
   }
 
   /**
    * Gets an InputStream for this URL, using the last modified time as
    * 'if-modified-since'.  Checks for login pages.  If a 304 is generated
    * (not modified), it returns null.  Subclasses probably want to override
    * {@link #getUncachedInputStreamOnly(String)}
    * @param lastModified the last modified time
    * @return the InputStream, or null
    * @throws CacheException.PermissionException if the
    * @throws IOException
    */
   public final InputStream getUncachedInputStream() throws IOException {
     return getUncachedInputStream(null);
   }
 
   /**
    * Gets an InputStream for this URL, using the last modified time as
    * 'if-modified-since'.  If a 304 is generated (not modified), it returns
    * null.
    * @param lastModified the last modified time
    * @return the InputStream, or null
    * @throws IOException
    */
   protected final InputStream getUncachedInputStream(String lastModified)
       throws IOException {
     InputStream is = getUncachedInputStreamOnly(lastModified);
     if (is != null) {
       // Check for login page iff got new content
       is = checkLoginPage(is, getHeaders(), lastModified);
     }
     return is;
   }
 
   protected InputStream getUncachedInputStreamOnly(String lastModified)
       throws IOException {
     InputStream input = null;
     try {
       openConnection(lastModified);
       if (conn.isHttp()) {
 	// http connection; check response code
 	int code = conn.getResponseCode();
 	if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
 	  logger.debug2("Unmodified content not cached for url '" +
 			origUrl + "'");
 	  return null;
 	}
       }
       input = conn.getResponseInputStream();
       if (input == null) {
 	logger.warning("Got null input stream back from conn.getResponseInputStream");
       }
     } finally {
       if (conn != null && input == null) {
 	logger.debug3("Releasing connection");
 	conn.release();
       }
     }
     return input;
   }
 
   public CIProperties getUncachedProperties()
       throws UnsupportedOperationException {
     if (conn == null) {
       throw new UnsupportedOperationException("Called getUncachedProperties before calling getUncachedInputStream.");
     }
 //     openConnection();
     if (uncachedProperties == null) {
       CIProperties props = new CIProperties();
       // set header properties in which we have interest
 
       String ctype = conn.getResponseContentType();
       if (ctype != null) {
 	props.setProperty(CachedUrl.PROPERTY_CONTENT_TYPE, ctype);
       }
       props.setProperty(CachedUrl.PROPERTY_FETCH_TIME,
 			Long.toString(TimeBase.nowMs()));
       if (origUrl != fetchUrl &&
 	  !UrlUtil.isDirectoryRedirection(origUrl, fetchUrl)) {
 	// XXX this property does not have consistent semantics.  It will be
 	// set to the first url in a chain of redirects that led to content,
 	// which could be different depending on fetch order.
 	props.setProperty(CachedUrl.PROPERTY_ORIG_URL, origUrl);
       }
       conn.storeResponseHeaderInto(props, CachedUrl.HEADER_PREFIX);
       String actualURL = conn.getActualUrl();
       if (!origUrl.equals(actualURL)) {
 	props.setProperty(CachedUrl.PROPERTY_CONTENT_URL, actualURL);
       }
       if (otherNames != null) {
 	props.setProperty(CachedUrl.PROPERTY_REDIRECTED_TO,
 			  (String)otherNames.get(0));
       } else if (!origUrl.equals(actualURL)) {
 	props.setProperty(CachedUrl.PROPERTY_REDIRECTED_TO, actualURL);
       }
       uncachedProperties = props;
     }
     return uncachedProperties;
   }
 
   void checkConnectException(LockssUrlConnection conn) throws IOException {
     if(conn.isHttp()) {
       if (logger.isDebug3()) {
 	logger.debug3("Response: " + conn.getResponseCode() + ": " +
 		      conn.getResponseMessage());
       }
       CacheException c_ex = resultMap.checkResult(au, conn);
       if(c_ex != null) {
 	// The stack below here is misleading.  Makes more sense for it
 	// to reflect the point at which it's thrown
 	c_ex.fillInStackTrace();
 	throw c_ex;
       }
     }
   }
 
   /**
    * If we haven't already connected, creates a connection from url, setting
    * the user-agent and ifmodifiedsince values.  Then actually connects to the
    * site and throws if we get an error code
    */
   private void openConnection(String lastModified) throws IOException {
     if (conn==null) {
       if (isRedirectOption(REDIRECT_OPTION_IF_CRAWL_SPEC +
 			   REDIRECT_OPTION_ON_HOST_ONLY)) {
 	openWithRedirects(lastModified);
       } else {
 	openOneConnection(lastModified);
       }
     }
   }
 
   private void openWithRedirects(String lastModified) throws IOException {
     int retry = 0;
     while (true) {
       try {
 	openOneConnection(lastModified);
 	break;
       } catch (CacheException.NoRetryNewUrlException e) {
         //DVN CUSTOM -- BEGIN
         //get the location header to find out where to redirect to
         String location = conn.getResponseHeaderValue("location");
         if (logger.isDebug3()) {
             logger.debug3("Redirect requested from '" + fetchUrl +
                 "' to '" + location + "'");
         }
         if (au.isLoginPageUrl(location)) {
             logger.debug3("Redirected to TermsOfUse page: " + location);
         } else {
             throw e;
         }
 
         if (!processTermsOfUseResponse()) {
             throw e;
         }
         checkConnectException(conn);
         break;
         //DVN CUSTOM -- END
       }
     }
   }
 
   protected LockssUrlConnection makeConnection(String url,
 					       LockssUrlConnectionPool pool)
       throws IOException {
     LockssUrlConnection res = makeConnection0(url, pool);
     String cookiePolicy = au.getCrawlSpec().getCookiePolicy();
     if (cookiePolicy != null) {
       res.setCookiePolicy(cookiePolicy);
     }
     return res;
   }
 
   /** Overridable so testing code can return a MockLockssUrlConnection */
   protected LockssUrlConnection makeConnection0(String url,
 						LockssUrlConnectionPool pool)
       throws IOException {
     return UrlUtil.openConnection(url, pool);
   }
 
   /**
    * Create a connection object from url, set the user-agent and
    * ifmodifiedsince values.  Then actually connect to the site and throw
    * if we get an error response
    */
   private void openOneConnection(String lastModified) throws IOException {
     if (conn != null) {
       throw
 	new IllegalStateException("Must call reset() before reusing UrlCacher");
     }
     try {
        if (fetchUrl.matches(".*/dvn/api/metadata/.*")) {
            /* 
             *  This is a bit of a hack: 
             *  Metadata API is accessible over HTTPS only;
             *  However, LOCKSS thinks that https://foobar/ is a different
             *  location from http://foobar/. So to make it crawl, we store
             *  these urls with the "http://" prefix, then change it to 
             *  https:// in real time. 
             *  I'm hoping to come up with a better solution eventually.
             *          -- L.A. 
             */
            fetchUrl = fetchUrl.replaceFirst("^http:", "https:");
        }
       conn = makeConnection(fetchUrl, connectionPool);
       if (proxyHost != null) {
 	if (logger.isDebug3()) logger.debug3("Proxying through " + proxyHost
 					     + ":" + proxyPort);
 	conn.setProxy(proxyHost, proxyPort);
       }
       if (localAddr != null) {
 	conn.setLocalAddress(localAddr);
       }
       String userPass = getUserPass();
       if (userPass != null) {
 	List<String> lst = StringUtil.breakAt(userPass, ':');
 	if (lst.size() == 2) {
 	  conn.setCredentials(lst.get(0), lst.get(1));
 	}
       }
       if (reqProps != null) {
 	for (Iterator iter = reqProps.keySet().iterator(); iter.hasNext(); ) {
 	  String key = (String)iter.next();
 	  conn.setRequestProperty(key, reqProps.getProperty(key));
 	}
       }
       conn.setFollowRedirects(isRedirectOption(REDIRECT_OPTION_FOLLOW_AUTO));
       conn.setRequestProperty("user-agent", LockssDaemon.getUserAgent());
 
       if (lastModified != null) {
 	conn.setIfModifiedSince(lastModified);
       }
       pauseBeforeFetch();
       conn.execute();
     } catch (MalformedURLException ex) {
       logger.debug2("openConnection", ex);
       throw resultMap.getMalformedURLException(ex);
     } catch (IOException ex) {
       logger.debug2("openConnection", ex);
       throw resultMap.mapException(au, conn, ex, null);
     } catch (RuntimeException e) {
       logger.warning("openConnection: unexpected exception", e);
       throw e;
     }
     checkConnectException(conn);
   }
 
   private void releaseConnection() {
     if (conn != null) {
       logger.debug3("conn isn't null, releasing");
       conn.release();
       conn = null;
     }
   }
 
   protected void pauseBeforeFetch() {
     if (crl != null) {
       crl.pauseBeforeFetch(fetchUrl, previousContentType);
     }
   }
 
   String getUserPass() {
     Configuration auConfig = au.getConfiguration();
     if (auConfig != null) {		// can be null in unit tests
       return auConfig.get(ConfigParamDescr.USER_CREDENTIALS.getKey());
     }
     return null;
   }
 
   /** Handle a single redirect response: determine whether it should be
    * followed and change the state (fetchUrl) to set up for the next fetch.
    * @return true if another request should be issued, false if not. */
   private boolean processRedirectResponse() throws CacheException {
     //get the location header to find out where to redirect to
     String location = conn.getResponseHeaderValue("location");
     if (location == null) {
       // got a redirect response, but no location header
       logger.siteError("Received redirect response " + conn.getResponseCode()
 		       + " but no location header");
       return false;
     }
     if (logger.isDebug3()) {
       logger.debug3("Redirect requested from '" + fetchUrl +
 		    "' to '" + location + "'");
     }
     // update the current location with the redirect location.
     try {
       String newUrlString = UrlUtil.resolveUri(fetchUrl, location);
       if (CurrentConfig.getBooleanParam(PARAM_NORMALIZE_REDIRECT_URL,
 					DEFAULT_NORMALIZE_REDIRECT_URL)) {
 	try {
 	  newUrlString = UrlUtil.normalizeUrl(newUrlString, au);
 	  logger.debug3("Normalized to '" + newUrlString + "'");
 	} catch (PluginBehaviorException e) {
 	  logger.warning("Couldn't normalize redirect URL: " + newUrlString, e);
 	}
       }
       // Check redirect to login page *before* crawl spec, else plugins
       // would have to include login page URLs in crawl spec
       //DVN CUSTOM -- BEGIN
       logger.debug3("Checking if a login page");
       //DVN CUSTOM -- END
 
       if (au.isLoginPageUrl(newUrlString)) {
        //DVN CUSTOM -- BEGIN
         String msg = "Redirected to login page: " + newUrlString;
         logger.debug3(msg);
         // BaseCrawler throws an exception here;
         // we don't want to throw an exception,
         // we want to go ahead and authenticate.
         //DVN CUSTOM -- END
 
       }
       if (isRedirectOption(REDIRECT_OPTION_IF_CRAWL_SPEC)) {
 	if (!au.shouldBeCached(newUrlString)) {
 	  logger.warning("Redirect not in crawl spec: " + newUrlString +
 			 " from: " + origUrl);
  	  throw
 	    new CacheException.RedirectOutsideCrawlSpecException(newUrlString);
 	  //	  return false;
 	}
       }
       PermissionMap permissionMap = null;
       if (permissionMapSource != null) {
 	logger.debug3("Getting permission map");
 	permissionMap = permissionMapSource.getPermissionMap();
       }
 
       // TODO: swap isSameHost with isRedirectOption and
       // add permission check on same level as redirectOption.
       if(!UrlUtil.isSameHost(fetchUrl, newUrlString)) {
 	if (isRedirectOption(REDIRECT_OPTION_ON_HOST_ONLY)) {
 	  logger.warning("Redirect to different host: " + newUrlString +
 			 " from: " + origUrl);
 	  return false;
 	} else if(permissionMap == null ||
 		  permissionMap.getStatus(newUrlString) != PermissionRecord.PERMISSION_OK) {
           logger.warning("No permission for redirect to different host: "
                          + newUrlString + " from: " + origUrl);
           return false;
         }
       }
       releaseConnection();
 
       // XXX
       // The names .../foo and .../foo/ map to the same repository node, so
       // the case of a slash-appending redirect requires special handling.
       // (Still. sigh.)  The node should be written only once, so don't add
       // another entry for the slash redirection.
 
       if (!UrlUtil.isDirectoryRedirection(fetchUrl, newUrlString)) {
 	if (otherNames == null) {
 	  otherNames = new ArrayList();
 	}
 	otherNames.add(newUrlString);
       }
       fetchUrl = newUrlString;
       logger.debug2("Following redirect to " + newUrlString);
       return true;
     } catch (MalformedURLException e) {
       logger.siteWarning("Redirected location '" + location +
 			 "' is malformed", e);
       return false;
     }
   }
     
     //DVN CUSTOM -- BEGIN
     // Very DVN-specific, custom case -- the content is protected
     // by a Terms-of-Use agreement clickthrough, but otherwise is
     // available to the public.
     private boolean processTermsOfUseResponse() throws IOException {
 
         //get the location header to find out where to redirect:
 
         String TOUurl = conn.getResponseHeaderValue("location");
 
         String extraCookies = conn.getResponseHeaderValue("set-cookie");
 
         releaseConnection();
 
         GetMethod method = null;
         String jsessionid = null;
 
         if (reqProps != null) {
             for (Iterator iter = reqProps.keySet().iterator(); iter.hasNext();) {
                 String key = (String) iter.next();
 
                 logger.debug("TOU key: " + key);
                 logger.debug("TOU property: " + reqProps.getProperty(key));
                 if (key.equals("Cookie") || key.equals("cookie")) {
                     String cookieProperty = reqProps.getProperty(key);
                     if (cookieProperty != null) {
                         String regexpJsession = "JSESSIONID=([^;]*);";
                         Pattern patternJsession = Pattern.compile(regexpJsession);
                         Matcher matcher = patternJsession.matcher(cookieProperty);
                         if (matcher.find()) {
                             jsessionid = matcher.group(1);
                             logger.debug("TOU found jsessionid: " + jsessionid);
                         }
                     }
                 }
             }
         }
 
 
         DvnTermsOfUseAccess dvnTOU = new DvnTermsOfUseAccess();
 
         logger.debug("attempting to process DVN Terms of Use: " + TOUurl);
 
         jsessionid = dvnTOU.dvnAcceptRemoteTOU(TOUurl, jsessionid, fetchUrl, extraCookies);
 
         if (jsessionid != null) {
             logger.debug("Received JSESSIONID: " + jsessionid);
         } else {
             logger.debug("Failed to obtain JSESSIONID");
             return false;
         }
 
         // OK, we can try again now!
 
         logger.debug3("trying to access the file again " + fetchUrl);
 
         try {
             conn = makeConnection(fetchUrl, connectionPool);
             if (localAddr != null) {
                 conn.setLocalAddress(localAddr);
             }
             if (reqProps != null) {
                 for (Iterator iter = reqProps.keySet().iterator(); iter.hasNext();) {
                     String key = (String) iter.next();
                     conn.setRequestProperty(key, reqProps.getProperty(key));
                 }
             }
 
             conn.setRequestProperty("user-agent", LockssDaemon.getUserAgent());
             String myCookie = "JSESSIONID=" + jsessionid;
             conn.addRequestProperty("Cookie", myCookie);
 
             logger.debug("executing request");
             conn.execute();
         } catch (MalformedURLException ex) {
             logger.debug2("openConnection", ex);
             throw resultMap.getMalformedURLException(ex);
         } catch (IOException ex) {
             logger.warning("openConnection: IO Exception", ex);
             throw ex;
         } catch (RuntimeException e) {
             logger.warning("openConnection: unexpected exception", e);
             throw e;
         }
 
         return true;
     }
     //DVN CUSTOM -- END
 
 
   /** Return true iff there are options in common between the argument and
    * redirectOptions */
   private boolean isRedirectOption(int option) {
     return (redirectOptions & option) != 0;
   }
   /**
    * setPermissionMap
    *
    * @param permissionMap PermissionMap
    */
   /*
   public void setPermissionMap(PermissionMap permissionMap) {
     this.permissionMap = permissionMap;
   }
 */
 
   /**
    * Sets the PermissionMapSource object, which is what BaseUrlCacher
    * will use to get the permission map when needed
    *
    */
   public void setPermissionMapSource(PermissionMapSource permissionMapSource) {
     this.permissionMapSource = permissionMapSource;
   }
 
   //DVN CUSTOM -- BEGIN
   protected DVNOAIArchivalUnit.ParamHandlerMap getParamMap() {
     try {
       DVNOAIArchivalUnit bau = (DVNOAIArchivalUnit) au;
       return bau.getParamMap();
     } catch (ClassCastException ex) {
       logger.error("Expected au to be DVNOAIArchivalUnit", ex);
     }
     return null;
   }
   //DVN CUSTOM -- END
 
 
 }
