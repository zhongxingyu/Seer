 /* Copyright 2013 Lyor Goldstein
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.community.chest.gitcloud.facade.frontend.git;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.zip.GZIPInputStream;
 
 import javax.inject.Inject;
 import javax.management.JMException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.net.ssl.HttpsURLConnection;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.collections15.ExtendedCollectionUtils;
 import org.apache.commons.io.ExtendedIOUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.output.AsciiLineOutputStream;
 import org.apache.commons.io.output.TeeOutputStream;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.ExtendedCharSequenceUtils;
 import org.apache.commons.lang3.ExtendedValidate;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.Validate;
 import org.apache.commons.logging.ExtendedLogUtils;
 import org.apache.commons.net.ssl.SSLUtils;
 import org.eclipse.jgit.http.server.GitSmartHttpTools;
 import org.eclipse.jgit.lib.Constants;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.RefreshedContextAttacher;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.SystemPropertyUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * @author Lyor Goldstein
  * @since Sep 12, 2013 1:17:34 PM
  */
 @Controller // TODO make it a @ManagedObject and expose internal configuration values for JMX management (Read/Write)
 public class GitController extends RefreshedContextAttacher {
     public static final Set<String> ALLOWED_SERVICES=
             Collections.unmodifiableSet(
                     new TreeSet<String>(
                             Arrays.asList(GitSmartHttpTools.UPLOAD_PACK, GitSmartHttpTools.RECEIVE_PACK)));
     public static final String  LOOP_DETECT_TIMEOUT="gitcloud.frontend.git.controller.loop.detect.timeout";
         public static final long    DEFAULT_LOOP_DETECT_TIMEOUT=0L; // disabled
         private static final String LOOP_DETECT_TIMEOUT_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                             + LOOP_DETECT_TIMEOUT
                                             + SystemPropertyUtils.VALUE_SEPARATOR
                                             + DEFAULT_LOOP_DETECT_TIMEOUT
                                             + SystemPropertyUtils.PLACEHOLDER_SUFFIX;
     public static final String  URL_REDIRECT_CONNECT_TIMEOUT="gitcloud.frontend.git.controller.url.redirect.conn.timeout";
         public static final int DEFAULT_URL_REDIRECT_CONNECT_TIMEOUT=5 * 1000;  // msec.
         private static final String URL_REDIRECT_CONNECT_TIMEOUT_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                             + URL_REDIRECT_CONNECT_TIMEOUT
                                             + SystemPropertyUtils.VALUE_SEPARATOR
                                             + DEFAULT_URL_REDIRECT_CONNECT_TIMEOUT
                                             + SystemPropertyUtils.PLACEHOLDER_SUFFIX;
     public static final String  URL_REDIRECT_READ_TIMEOUT="gitcloud.frontend.git.controller.url.redirect.read.timeout";
         public static final int DEFAULT_URL_REDIRECT_READ_TIMEOUT=30 * 1000;  // msec.
         private static final String URL_REDIRECT_READ_TIMEOUT_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                             + URL_REDIRECT_READ_TIMEOUT
                                             + SystemPropertyUtils.VALUE_SEPARATOR
                                             + DEFAULT_URL_REDIRECT_READ_TIMEOUT
                                             + SystemPropertyUtils.PLACEHOLDER_SUFFIX;
 
     private final MBeanServer   mbeanServer;
     private final long  loopRetryTimeout;
     private final int   urlRedirectConnectTimeout, urlRedirectReadTimeout;
     private volatile long    initTimestamp=System.currentTimeMillis();
     private volatile boolean    loopDetected;
 
     @Inject
     public GitController(MBeanServer localMbeanServer,
             @Value(LOOP_DETECT_TIMEOUT_VALUE) long loopDetectTimeout,
             @Value(URL_REDIRECT_CONNECT_TIMEOUT_VALUE) int redirectConnectTimeout,
             @Value(URL_REDIRECT_READ_TIMEOUT_VALUE) int redirectReadTimeout) {
         mbeanServer = Validate.notNull(localMbeanServer, "No MBean server", ArrayUtils.EMPTY_OBJECT_ARRAY);
         loopRetryTimeout = loopDetectTimeout;
         
         Validate.isTrue(redirectConnectTimeout > 0, "Invalid URL redirect connect timeout: %d", redirectConnectTimeout);
         urlRedirectConnectTimeout = redirectConnectTimeout;
 
         Validate.isTrue(redirectReadTimeout > 0, "Invalid URL redirect read timeout: %d", redirectReadTimeout);
         urlRedirectReadTimeout = redirectReadTimeout;
     }
 
     @Override
     protected void onContextInitialized(ApplicationContext context) {
         super.onContextInitialized(context);
         initTimestamp = System.currentTimeMillis();
 
         logger.info("MBeanServer default domain: " + mbeanServer.getDefaultDomain());
         
         String[]    domains=mbeanServer.getDomains();
         if (!ArrayUtils.isEmpty(domains)) {
             for (String d : domains) {
                 logger.info("MBeanServer extra domain: " + d);
             }
         }
     }
 
     @RequestMapping(method=RequestMethod.GET)
     public void serveGetRequests(HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
         serveRequest(RequestMethod.GET, req, rsp);
     }
     
     @RequestMapping(method=RequestMethod.POST)
     public void servePostRequests(HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
         serveRequest(RequestMethod.POST, req, rsp);
     }
 
     private void serveRequest(RequestMethod method, HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
         if (logger.isDebugEnabled()) {
             logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]");
         }
 
         if ((loopRetryTimeout > 0L) && (!loopDetected)) {
             long    now=System.currentTimeMillis(), diff=now - initTimestamp;
             if ((diff > 0L) && (diff < loopRetryTimeout)) {
                 try {
                     MBeanInfo   mbeanInfo=
                             mbeanServer.getMBeanInfo(new ObjectName("net.community.chest.gitcloud.facade.backend.git:name=BackendRepositoryResolver"));
                     if (mbeanInfo != null) {
                         logger.info("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                   + " detected loop: " + mbeanInfo.getClassName() + "[" + mbeanInfo.getDescription() + "]");
                         loopDetected = true;
                     }
                 } catch(JMException e) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                 + " failed " + e.getClass().getSimpleName()
                                 + " to detect loop: " + e.getMessage());
                     }
                 }
             }
         }
 
         /*
          * NOTE: this feature requires enabling cross-context forwarding.
          * In Tomcat, the 'crossContext' attribute in 'Context' element of
          * 'TOMCAT_HOME\conf\context.xml' must be set to true, to enable cross-context 
          */
         URL url=resolveTargetRepository(method, req);
         if (loopDetected) {
             // TODO see if can find a more efficient way than splitting and re-constructing
             ServletContext  curContext=req.getServletContext();
             String          urlPath=url.getPath(), urlQuery=url.getQuery();
             String[]        comps=StringUtils.split(urlPath, '/');
             String          appName=comps[0];
             ServletContext  loopContext=Validate.notNull(curContext.getContext("/" + appName), "No cross-context for %s", appName);
             // build the relative path in the re-directed context
             StringBuilder   sb=new StringBuilder(urlPath.length() + 1 + (StringUtils.isEmpty(urlQuery) ? 0 : urlQuery.length()));
             for (int index=1; index < comps.length; index++) {
                 sb.append('/').append(comps[index]);
             }
             if (!StringUtils.isEmpty(urlQuery)) {
                 sb.append('?').append(urlQuery);
             }
             
             String              redirectPath=sb.toString();
             RequestDispatcher   dispatcher=Validate.notNull(loopContext.getRequestDispatcher(redirectPath), "No dispatcher for %s", redirectPath);
             dispatcher.forward(req, rsp);
             if (logger.isDebugEnabled()) {
                 logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                            + " forwarded to " + loopContext.getContextPath() + "/" + redirectPath);
             }
         } else {
             executeRemoteRequest(method, url, req, rsp);
         }
     }
     
     private void executeRemoteRequest(final RequestMethod method, URL url, final HttpServletRequest req, HttpServletResponse rsp) throws IOException {
         if (logger.isDebugEnabled()) {
             logger.debug("executeRemoteRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                        + " redirected to " + url.toExternalForm());
         }
 
         HttpURLConnection   conn=openTargetConnection(method, url);
         try {
             Map<String,String>  reqHeaders=copyRequestHeadersValues(req, conn);
             if (RequestMethod.POST.equals(method)) {
                 transferPostedData(method, req, conn, reqHeaders);
             }
             
             int statusCode=conn.getResponseCode();
             if ((statusCode < HttpServletResponse.SC_OK) || (statusCode >= HttpServletResponse.SC_MULTIPLE_CHOICES)) {
                 String    rspMsg=conn.getResponseMessage();
                 logger.warn("executeRemoteRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                           + " bad status code (" + statusCode + ")"
                           + " on redirection to " + url.toExternalForm() + ": " + rspMsg);
                 rsp.sendError(statusCode, rspMsg);
             } else {
                rsp.setStatus(statusCode);
 
                 Map<String,String>  rspHeaders=copyResponseHeadersValues(conn, rsp);
                 if (RequestMethod.GET.equals(method)) {
                     transferBackendResponse(method, req, rsp, conn, rspHeaders);
                 }
             }
         } finally {
             conn.disconnect();
         }
     }
 
     private void transferPostedData(
                 final RequestMethod method, final HttpServletRequest req, HttpURLConnection conn, Map<String,String> reqHeaders)
             throws IOException {
         InputStream postData=req.getInputStream();
         try {
             ByteArrayOutputStream   bytesStream=null;
             OutputStream            postTarget=conn.getOutputStream(), logStream=null;
             String                  encoding=reqHeaders.get("Content-Encoding");
             if (logger.isTraceEnabled()) {
                 logStream = new AsciiLineOutputStream() {
                         @Override
                         @SuppressWarnings("synthetic-access")
                         public void writeLineData(CharSequence lineData) throws IOException {
                             logger.trace("transferPostedData(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                        + " C: " + lineData);
                         }
     
                         @Override
                         public boolean isWriteEnabled() {
                             return true;
                         }
                     };
                     
                 if ("gzip".equalsIgnoreCase(encoding)) {
                     bytesStream = new ByteArrayOutputStream();
                 }
 
                 postTarget = new TeeOutputStream(postTarget, (bytesStream == null) ? logStream : bytesStream);
             }
 
             try {
                 long    cpyLen=IOUtils.copyLarge(postData, postTarget);
                 if (logger.isTraceEnabled()) {
                     final URL   url=conn.getURL();
                     logger.trace("transferPostedData(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                + " copied " + cpyLen + " bytes to " + url.toExternalForm());
 
                     if (bytesStream != null) {
                         InputStream gzStream=new GZIPInputStream(new ByteArrayInputStream(bytesStream.toByteArray()));
                         try {
                             IOUtils.copyLarge(gzStream, logStream);
                         } finally {
                             gzStream.close();
                         }
                     }
                 }
             } finally {
                 ExtendedIOUtils.closeAll(postTarget, logStream, bytesStream);
             }
         } finally {
             postData.close();
         }
     }
 
     private void transferBackendResponse(
             final RequestMethod method, final HttpServletRequest req, HttpServletResponse rsp, HttpURLConnection conn, Map<String,String>  rspHeaders)
                     throws IOException {
         InputStream rspData=conn.getInputStream();
         try {
             ByteArrayOutputStream   bytesStream=null;
             OutputStream            rspTarget=rsp.getOutputStream(), logStream=null;
             String                  encoding=rspHeaders.get("Content-Encoding");
             if (logger.isTraceEnabled()) {
                 logStream = new AsciiLineOutputStream() {
                         @Override
                         @SuppressWarnings("synthetic-access")
                         public void writeLineData(CharSequence lineData) throws IOException {
                             logger.trace("transferBackendResponse(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                        + " S: " + lineData);
                         }
 
                         @Override
                         public boolean isWriteEnabled() {
                             return true;
                         }
                     };
                 if ("gzip".equalsIgnoreCase(encoding)) {
                     bytesStream = new ByteArrayOutputStream();
                 }
                 rspTarget = new TeeOutputStream(rspTarget, (bytesStream == null) ? logStream : bytesStream);
             }
 
             try {
                 long    cpyLen=IOUtils.copyLarge(rspData, rspTarget);
                 if (logger.isTraceEnabled()) {
                     URL url=conn.getURL();
                     logger.trace("transferBackendResponse(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                + " copied " + cpyLen + " bytes from " + url.toExternalForm());
                     
                     if (bytesStream != null) {
                         InputStream gzStream=new GZIPInputStream(new ByteArrayInputStream(bytesStream.toByteArray()));
                         try {
                             IOUtils.copyLarge(gzStream, logStream);
                         } finally {
                             gzStream.close();
                         }
                     }
                 }
             } finally {
                 ExtendedIOUtils.closeAll(rspTarget, logStream, bytesStream);
             }
         } finally {
             rspData.close();
         }
     }
 
     private URL resolveTargetRepository(RequestMethod method, HttpServletRequest req) throws IOException {
         String  op=null, uriPath=req.getPathInfo();
         if (RequestMethod.GET.equals(method)) {
             op = req.getParameter("service");
         } else {
             int pos=uriPath.lastIndexOf('/');
             if ((pos > 0) && (pos < (uriPath.length() - 1))) {
                 op = uriPath.substring(pos + 1);
             }
         }
 
         if (!StringUtils.isEmpty(op)) {
             ExtendedValidate.isTrue(ALLOWED_SERVICES.contains(op), "Unsupported service: %s", op);
         }
         
         String repoName=extractRepositoryName(uriPath);
         if (StringUtils.isEmpty(repoName)) {
             throw ExtendedLogUtils.thrownLogging(logger, Level.WARNING,
                          "resolveTargetRepository(" + uriPath + ")",
                          new IllegalArgumentException("Failed to extract repo name from " + uriPath));
         }
 
         // TODO access an injected resolver that returns the back-end location URL
         String  query=req.getQueryString();
         if (StringUtils.isEmpty(query)) {
             return new URL("http://localhost:8080/git-backend/git" + uriPath);
         } else {
             return new URL("http://localhost:8080/git-backend/git" + uriPath + "?" + query);
         }
     }
     
     // TODO move this to some generic util location
     private HttpURLConnection openTargetConnection(RequestMethod method, URL url) throws IOException {
         HttpURLConnection   conn=(HttpURLConnection) url.openConnection();
         if (conn instanceof HttpsURLConnection) {
             HttpsURLConnection    https=(HttpsURLConnection) conn;
             https.setHostnameVerifier(SSLUtils.ACCEPT_ALL_HOSTNAME_VERIFIER);
             https.setSSLSocketFactory(SSLUtils.ACCEPT_ALL_FACTORY.create());
         }
 
         conn.setConnectTimeout(urlRedirectConnectTimeout);
         conn.setReadTimeout(urlRedirectReadTimeout);
         conn.setRequestMethod(method.name());
         
         if (RequestMethod.POST.equals(method)) {
             conn.setDoOutput(true);
         }
 
         return conn;
     }
 
     // TODO move this to some generic util location
     private Map<String,String> copyRequestHeadersValues(HttpServletRequest req, HttpURLConnection conn) {
         Map<String,String>  hdrsValues=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
         for (Enumeration<String> hdrs=req.getHeaderNames(); (hdrs != null) && hdrs.hasMoreElements(); ) {
             String  hdrName=capitalizeHttpHeaderName(hdrs.nextElement()), hdrValue=req.getHeader(hdrName);
             if (StringUtils.isEmpty(hdrValue)) {
                 continue;
             }
 
             if (logger.isTraceEnabled()) {
                 logger.trace("copyRequestHeadersValues(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                            + " " + hdrName + ": " + hdrValue);
             }
 
             conn.setRequestProperty(hdrName, hdrValue);
             hdrsValues.put(hdrName, hdrValue);
         }
 
         return hdrsValues;
     }
 
     // TODO move this to some generic util location
     private Map<String,String> copyResponseHeadersValues(HttpURLConnection conn, HttpServletResponse rsp) {
         Map<String,String>          hdrsValues=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
         Map<String,List<String>>    headerFields=conn.getHeaderFields();
         for (Map.Entry<String,List<String>> hdr : headerFields.entrySet()) {
             String  hdrName=capitalizeHttpHeaderName(hdr.getKey());
             if (StringUtils.isEmpty(hdrName)) {
                 continue;   // The response code + message is encoded using an empty header name
             }
 
             List<String>    values=hdr.getValue();
             if (ExtendedCollectionUtils.isEmpty(values)) {
                 continue;
             }
 
             if (values.size() == 1) {
                 String  hdrValue=values.get(0);
                 if (StringUtils.isEmpty(hdrValue)) {
                     continue;
                 }
 
                 if (logger.isTraceEnabled()) {
                     URL url=conn.getURL();
                     logger.trace("copyResponseHeadersValues(" + url.toExternalForm() + "] " + hdrName + ": " + hdrValue);
                 }
 
                 rsp.setHeader(hdrName, hdrValue);
                 hdrsValues.put(hdrName, hdrValue);
             } else {
                 for (int index=0; index < values.size(); index++) {
                     String  hdrValue=values.get(index);
                     if (StringUtils.isEmpty(hdrValue)) {
                         continue;   // unexpected, but ignored
                     }
 
                     rsp.addHeader(hdrName, hdrValue);
                     if (logger.isTraceEnabled()) {
                         URL url=conn.getURL();
                         logger.trace("copyResponseHeadersValues(" + url.toExternalForm() + "] " + hdrName + "[" + index + "]: " + hdrValue);
                     }
                 }
 
                 hdrsValues.put(hdrName, StringUtils.join(values, ','));
             }
         }
         
         return hdrsValues;
     }
 
     // TODO move this to some generic util location
     public static final String capitalizeHttpHeaderName(String hdrName) {
         if (StringUtils.isEmpty(hdrName)) {
             return hdrName;
         }
 
         int curPos=hdrName.indexOf('-');
         if (curPos < 0) {
             return ExtendedCharSequenceUtils.capitalize(hdrName);
         }
 
         StringBuilder   sb=null;
         for (int  lastPos=0; ; ) {
             char    ch=hdrName.charAt(lastPos), tch=Character.toTitleCase(ch);
             if (ch != tch) {
                 if (sb == null) {
                     sb = new StringBuilder(hdrName.length());
                     // append the data that was OK
                     if (lastPos > 0) {
                         sb.append(hdrName.substring(0, lastPos));
                     }
                 }
                 
                 sb.append(tch);
                 
                 if (curPos > lastPos) {
                     sb.append(hdrName.substring(lastPos + 1 /* excluding the capital letter */, curPos + 1 /* including the '-' */));
                 } else {    // last component in string
                     sb.append(hdrName.substring(lastPos + 1 /* excluding the capital letter */));
                 }
             }
 
             if (curPos < lastPos) {
                 break;
             }
 
             if ((lastPos=curPos + 1) >= hdrName.length()) {
                 break;
             }
             
             curPos = hdrName.indexOf('-', lastPos);
         }
 
         if (sb == null) {   // There was no need to modify anything
             return hdrName;
         } else {
             return sb.toString();
         }
     }
 
     // TODO move this to some generic util location
     public static final String extractRepositoryName(String uriPath) {
         if (StringUtils.isEmpty(uriPath)) {
             return null;
         }
         
         int gitPos=uriPath.indexOf(Constants.DOT_GIT_EXT);
         if (gitPos <= 0) {
             return null;
         }
         
         int startPos=gitPos;
         for ( ; startPos >= 0; startPos--) {
             if (uriPath.charAt(startPos) == '/') {
                 startPos++;
                 break;
             }
         }
         
         if (startPos < 0) {
             startPos = 0;   // in case did not start with '/'
         }
 
         int endPos=gitPos;
         for ( ; endPos < uriPath.length(); endPos++) {
             if (uriPath.charAt(endPos) == '/') {
                 break;
             }
         }
         
         String  pureName=uriPath.substring(startPos, endPos);
         if (Constants.DOT_GIT_EXT.equals(pureName)) {
             return null;
         } else {
             return pureName;
         }
     }
 }
