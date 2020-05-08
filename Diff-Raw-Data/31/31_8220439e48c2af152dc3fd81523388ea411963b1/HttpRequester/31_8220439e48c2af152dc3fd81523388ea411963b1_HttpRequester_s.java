 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.util.service;
 
 import de.escidoc.core.common.servlet.EscidocServlet;
 import de.escidoc.core.common.servlet.UserHandleCookieUtil;
 import de.escidoc.core.common.util.IOUtils;
 import org.slf4j.Logger; import org.slf4j.LoggerFactory;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.security.SecureRandom;
 import java.security.cert.X509Certificate;
 
 /*
  * Created on 05.10.2006
  * 
  */
 
 /**
  * @author Michael Hoppe
  * 
  *         Class for requesting http-requests.
  * 
  * 
  */
 public class HttpRequester {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequester.class);
 
     private int timeout = 180000;
 
     private static final boolean SSL = false;
 
     private final String domain;
 
     private String securityHandle;
 
     private String cookie;
 
     /**
      * Default-Constructor.
      * 
      * @param domain
      *            The domain to send requests to.
      * @binding
      */
     public HttpRequester(final String domain) {
         this.domain = domain;
     }
 
     /**
      * Constructor with security-Handle for HTTP-Basic-Authentication.
      * 
      * @param domain
      *            The domain to send requests to.
      * @param securityHandle
      *            The escidoc security handle.
      * 
      * @binding
      */
     public HttpRequester(final String domain, final String securityHandle) {
         this.securityHandle = securityHandle;
         this.domain = domain;
     }
 
     /**
      * Sets the followRedirects-Flag of the URLConnection.
      * 
      * @param flag
      *            boolean flag
      * 
      */
     public void setFollowRedirects(final boolean flag) {
         HttpURLConnection.setFollowRedirects(flag);
         HttpURLConnection.setFollowRedirects(flag);
     }
 
     /**
      * Sends a GET-request to given URI and returns result as String.
      * 
      * @param resource
      *            String resource
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     public String doGet(final String resource) throws Exception {
         return request(resource, "GET", null);
     }
 
     /**
      * Sends a PUT-request with the given body to given URI and returns result
      * as String.
      * 
      * @param resource
      *            String resource
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     public String doPut(final String resource, final String body)
         throws Exception {
         if (body == null || body.length() == 0) {
             throw new Exception("body may not be null");
         }
         return request(resource, "PUT", body);
     }
 
     /**
      * Sends a POST-request with the given body to given URI and returns result
      * as String.
      * 
      * @param resource
      *            String resource
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     public String doPost(final String resource, final String body)
         throws Exception {
         if (body == null || body.length() == 0) {
             throw new Exception("body may not be null");
         }
         return request(resource, "POST", body);
     }
 
     /**
      * Sends a DELETE-request to given URI and returns result as String.
      * 
      * @param resource
      *            String resource
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     public String doDelete(final String resource) throws Exception {
         return request(resource, "DELETE", null);
     }
 
     /**
      * Sends request with given method and given body to given URI and returns
      * result as String.
      * 
      * @param resource
      *            String resource
      * @param method
      *            String method
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     private String request(
         final String resource, final String method, final String body)
         throws Exception {
         return SSL ? requestSsl(resource, method, body) : requestNoSsl(resource, method, body);
     }
 
     /**
      * Sends request with given method and given body to given URI and returns
      * result as String.
      * 
      * @param resource
      *            String resource
      * @param method
      *            String method
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     private String requestSsl( // Ignore FindBugs
         final String resource, final String method, final String body)
         throws Exception {
         final String response;
 
         // Open Connection to given resource
         final URL url = new URL(this.domain + resource);
         final TrustManager[] tm = { new RelaxedX509TrustManager() };
         final SSLContext sslContext = SSLContext.getInstance("SSL");
         sslContext.init(null, tm, new SecureRandom());
         final SSLSocketFactory sslSF = sslContext.getSocketFactory();
         final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
         con.setSSLSocketFactory(sslSF);
 
         // Set Basic-Authentication Header
         if (this.securityHandle != null && securityHandle.length() != 0) {
             final String encoding =
                 UserHandleCookieUtil.createEncodedUserHandle(this.securityHandle);
             con.setRequestProperty("Authorization", "Basic " + encoding);
             // Set Cookie
             con.setRequestProperty("Cookie", EscidocServlet.COOKIE_LOGIN + '='
                 + this.securityHandle);
         }
         else if (getCookie() != null) {
             con.setRequestProperty("Cookie", getCookie());
         }
 
         // Set request-method and timeout
         con.setRequestMethod(method.toUpperCase());
         con.setReadTimeout(this.timeout);
 
         // If PUT or POST, write given body in Output-Stream
         if (("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))
             && body != null) {
             con.setDoOutput(true);
             final OutputStream out = con.getOutputStream();
             try {
                 out.write(body.getBytes(XmlUtility.CHARACTER_ENCODING));
                 out.flush();
             } finally {
                 IOUtils.closeStream(out);
             }
         }
 
         // Request
         final InputStream is = con.getInputStream();
         setCookie(con.getHeaderField("Set-cookie"));
 
         // Read response
         try {
             response = IOUtils.readStringFromStream(is);
         } finally {
             IOUtils.closeStream(is);
         }
         return response;
     }
 
     /**
      * Sends request with given method and given body to given URI and returns
      * result as String.
      * 
      * @param resource
      *            String resource
      * @param method
      *            String method
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     private String requestNoSsl(
         final String resource, final String method, final String body)
         throws Exception {
         HttpURLConnection connection = null;
         InputStream is = null;
         OutputStream out = null;
         final String response;
 
         try {
             // Open Connection to given resource
             final URL url = new URL(this.domain + resource);
             connection = (HttpURLConnection) url.openConnection();
 
             // Set Basic-Authentication Header
             if (this.securityHandle != null && securityHandle.length() != 0) {
                 final String encoding =
                     UserHandleCookieUtil
                         .createEncodedUserHandle(this.securityHandle);
                 connection.setRequestProperty("Authorization", "Basic " + encoding);
                 // Set Cookie
                 connection.setRequestProperty("Cookie", EscidocServlet.COOKIE_LOGIN
                     + '=' + this.securityHandle);
             }
             else if (getCookie() != null) {
                 connection.setRequestProperty("Cookie", getCookie());
             }
 
             // Set request-method and timeout
             connection.setRequestMethod(method.toUpperCase());
             connection.setReadTimeout(this.timeout);
 
             // If PUT or POST, write given body in Output-Stream
             if (("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) && body != null) {
                 connection.setDoOutput(true);
                 out = connection.getOutputStream();
                 try {
                     out.write(body.getBytes(XmlUtility.CHARACTER_ENCODING));
                     out.flush();
                 } finally {
                     IOUtils.closeStream(out);
                 }
             }
 
             // Request
             is = connection.getInputStream();
             setCookie(connection.getHeaderField("Set-cookie"));
 
             // Read response
             final BufferedReader br = null;
             try {
                 response = IOUtils.readStringFromStream(is);
             } finally {
                 IOUtils.closeStream(br);
             }
         } finally {
             IOUtils.closeStream(out);
             IOUtils.closeStream(is);
             try {
                 connection.disconnect();
             } catch (final Exception e) {
                 if(LOGGER.isWarnEnabled()) {
                     LOGGER.warn("Error on disconnecting connection.");
                 }
                 if(LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Error on disconnecting connection.", e);
                 }
             }
         }
         return response;
     }
 
     /**
      * @author Michael Hoppe
      * 
      *         Overwrite X509TrustManager.
      * 
      * 
      */
     static class RelaxedX509TrustManager implements X509TrustManager {
         /**
          * Gets accepted Issuers.
          * 
          * @return X509Certificate[] response
          * 
          */
         @Override
         public X509Certificate[] getAcceptedIssuers() {
             return null;
         }
 
         /**
          * Checks Client trusted.
          * 
          * @param chain
          *            X509Certificate[]
          * @param authType
          *            String
          * 
          */
         @Override
         public void checkClientTrusted(
             final X509Certificate[] chain,
             final String authType) {
         }
 
         /**
          * Checks Server trusted.
          * 
          * @param chain
          *            X509Certificate[]
          * @param authType
          *            String
          * 
          */
         @Override
         public void checkServerTrusted(
             final X509Certificate[] chain,
             final String authType) {
         }
     }
 
     /**
      * @return the cookie
      */
     public String getCookie() {
         return this.cookie;
     }
 
     /**
      * @param cookie
      *            the cookie to set
      */
     public void setCookie(final String cookie) {
         this.cookie = cookie;
     }
 
     /**
      * @param timeout
      *            the timeout to set
      */
     public void setTimeout(final int timeout) {
         this.timeout = timeout;
     }
 
 }
