 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.service;
 
 import com.google.common.annotations.VisibleForTesting;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jtalks.poulpe.model.dao.UserDao;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.service.exceptions.JcommuneRespondedWithErrorException;
 import org.jtalks.poulpe.service.exceptions.JcommuneUrlNotConfiguredException;
 import org.jtalks.poulpe.service.exceptions.NoConnectionToJcommuneException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 /**
  * Notifier to notify JCommune component about elements' deleting. It is useful to help forum keep such information, as
  * user's messages count, up to date.
  *
  * @author Nickolay Polyarniy
  * @author Mikhail Zaitsev
  */
 public class JcommuneHttpNotifier {
     /** Minimum value of a successful status, less than that is an error HTTP response. */
     private static final int MIN_HTTP_STATUS = 200;
     /** Maximum value of a successful status, starting from 300 means an error response. */
     private static final int MAX_HTTP_STATUS = 299;
     /**
      * A link which means 'delete the whole component' which will cause all the topics from all the branches to be
      * removed by JCommune.
      */
     private static final String WHOLEFORUM_URL_PART = "/component";
     /** A URL to trigger re-indexing of forum search engine. */
     private static final String REINDEX_URL_PART = "/search/index/rebuild";
     /** URL to ask JCommune to remove content of the specific section. */
     private static final String SECTIONS_URL_PART = "/sections/";
     /** URL to ask JCommune to remove content of the specific branch. */
     private static final String BRANCH_URL_PART = "/branches/";
     /**
      * If either connection can't be established (e.g. firewall drops it) or connection was established, but response is
      * not coming back during this timeout, then the connection will be dropped. Note, that these are milliseconds.
      */
     private static final int HTTP_CONNECTION_TIMEOUT = 5000;
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private final UserDao userDao;
 
     public JcommuneHttpNotifier(UserDao userDao) {
         this.userDao = userDao;
     }
 
     /**
      * Notifies delete the section
      *
      * @param jCommuneUrl JCommune Url
      * @param section     which will be deleted
      * @throws NoConnectionToJcommuneException
      *          some connection problems happened, while trying to notify Jcommune
      * @throws JcommuneRespondedWithErrorException
      *          occurs when the response status is not in the interval {@link #MIN_HTTP_STATUS} and {@link
      *          #MAX_HTTP_STATUS}
      * @throws JcommuneUrlNotConfiguredException
      *          occurs when the {@code jCommuneUrl} is incorrect
      */
     public void notifyAboutSectionDelete(String jCommuneUrl, PoulpeSection section)
            throws NoConnectionToJcommuneException, JcommuneRespondedWithErrorException, JcommuneUrlNotConfiguredException {
         long id = section.getId();
         notifyAboutDeleteElement(jCommuneUrl + SECTIONS_URL_PART + id);
     }
 
     /**
      * Notifies delete the branch
      *
      * @param jCommuneUrl JCommune Url
      * @param branch      which will be deleted
      * @throws NoConnectionToJcommuneException
      *          some connection problems happened, while trying to notify Jcommune
      * @throws JcommuneRespondedWithErrorException
      *          occurs when the response status is not in the interval {@link #MIN_HTTP_STATUS} and {@link
      *          #MAX_HTTP_STATUS}
      * @throws JcommuneUrlNotConfiguredException
      *          occurs when the {@code jCommuneUrl} is incorrect
      */
    public void notifyAboutBranchDelete(String jCommuneUrl, PoulpeBranch branch)
            throws NoConnectionToJcommuneException, JcommuneRespondedWithErrorException, JcommuneUrlNotConfiguredException {
         long id = branch.getId();
         notifyAboutDeleteElement(jCommuneUrl + BRANCH_URL_PART + id);
     }
 
     /**
      * Notifies delete the component
      *
      * @param jCommuneUrl JCommune Url
      * @throws NoConnectionToJcommuneException
      *          some connection problems happend, while trying to notify Jcommune
      * @throws JcommuneRespondedWithErrorException
      *          occurs when the response status is not in the interval {@code MIN_HTTP_STATUS} and {@code
      *          MAX_HTTP_STATUS}
      * @throws JcommuneUrlNotConfiguredException
      *          occurs when the {@code jCommuneUrl} is incorrect
      */
    public void notifyAboutComponentDelete(String jCommuneUrl)
            throws NoConnectionToJcommuneException, JcommuneRespondedWithErrorException, JcommuneUrlNotConfiguredException {
         notifyAboutDeleteElement(jCommuneUrl + WHOLEFORUM_URL_PART);
     }
 
     /**
      * Notifies reindex сomponent
      *
      * @param jCommuneUrl JCommune Url
      * @throws NoConnectionToJcommuneException
      *          some connection problems happened, while trying to notify Jcommune
      * @throws JcommuneRespondedWithErrorException
      *          occurs when the response status is not in the interval {@code MIN_HTTP_STATUS} and {@code
      *          MAX_HTTP_STATUS}
      * @throws org.jtalks.poulpe.service.exceptions.JcommuneUrlNotConfiguredException
      *          occurs when the {@code jCommuneUrl} is incorrect
      */
     public void notifyAboutReindexComponent(String jCommuneUrl) throws NoConnectionToJcommuneException,
             JcommuneRespondedWithErrorException, JcommuneUrlNotConfiguredException {
         checkUrlIsConfigured(jCommuneUrl);
         createAndSendRequest(jCommuneUrl + REINDEX_URL_PART, HttpPost.METHOD_NAME);
     }
 
     /**
      * Notifies JCommune that an element is about to be deleted (for instance Component, Branch, Section).
      *
      * @param url JCommune Url
      * @throws NoConnectionToJcommuneException
      *          some connection problems happened, while trying to notify Jcommune
      * @throws JcommuneRespondedWithErrorException
      *          occurs when the response status is not in the interval {@code MIN_HTTP_STATUS} and {@code
      *          MAX_HTTP_STATUS}
      * @throws JcommuneUrlNotConfiguredException
      *          occurs when the {@code url} is incorrect
      */
     protected void notifyAboutDeleteElement(String url) throws NoConnectionToJcommuneException,
             JcommuneRespondedWithErrorException, JcommuneUrlNotConfiguredException {
         checkUrlIsConfigured(url);
         createAndSendRequest(url, HttpDelete.METHOD_NAME);
     }
 
     /**
      * Checks the url
      *
      * @param jCommuneUrl JCommune Url
      * @throws JcommuneUrlNotConfiguredException
      *          occurs when the {@code jCommuneUrl is incorrect
      */
     protected void checkUrlIsConfigured(String jCommuneUrl) throws JcommuneUrlNotConfiguredException {
         if (StringUtils.isBlank(jCommuneUrl)) {
             throw new JcommuneUrlNotConfiguredException();
         }
     }
 
     /**
      * Creates the HTTP request from specified data, adds admin password to the URL and sends to JCommune. The password
      * is required to be set in order to secure this invocation, otherwise anyway would be able to send such request to
      * JCommune.
      *
      * @param url        an address to send the request to
      * @param httpMethod delete or post request, see {@link HttpDelete}, {@link HttpPost}
      * @throws JcommuneRespondedWithErrorException
      *          if HTTP request reached JCommune, but JCommune responded with error code, such situation may happen for
      *          instance when we're deleting some branch, but it was already deleted, or JCommune has troubles removing
      *          that branch (database connection lost). Note that if we reach some other site and it responds with 404
      *          for example, this will be still this error.
      * @throws NoConnectionToJcommuneException
      *          if nothing was found at the specified URL, note that if URL was set incorrectly to point to another
      *          site, this can't be figured out by us, we just operate with HTTP codes, which means that either the
      *          request will be fine or {@link JcommuneRespondedWithErrorException} might be thrown in case if some
      *          other site was specified and it returned 404
      */
     private void createAndSendRequest(String url, String httpMethod) throws JcommuneRespondedWithErrorException,
             NoConnectionToJcommuneException {
         logger.info("Sending [{}] request to JCommune: [{}]", httpMethod, url);
         String adminPassword = userDao.getByUsername("admin").getPassword();
         HttpRequestBase request = createWithHttpMethod(httpMethod, url + "?password=" + adminPassword);
         try {
             HttpResponse response = doSendRequest(request);
             assertStatusSuccessful(response);
         } catch (IOException e) {
             logger.info("Was not possible to send request since [{}] does not respond", url);
             throw new NoConnectionToJcommuneException(e);
         } finally {
             request.releaseConnection();
         }
     }
 
     /**
      * Figures out whether the HTTP response was successful or not, if not than exception is thrown.
      *
      * @param response JCommune's HTTP response
      * @throws JcommuneRespondedWithErrorException
      *          if the HTTP code in the response appeared to be error one
      */
     private void assertStatusSuccessful(HttpResponse response) throws JcommuneRespondedWithErrorException {
         int statusCode = response.getStatusLine().getStatusCode();
         if (statusCode < MIN_HTTP_STATUS || statusCode > MAX_HTTP_STATUS) {
             logErrorResponse(response);
             throw new JcommuneRespondedWithErrorException(String.valueOf(statusCode));
         }
     }
 
     /**
      * Logs an error that was received from JCommune in the HTTP response body. Note that this doesn't check whether the
      * response was actually error one or not, so caller methods should ensure this.
      *
      * @param response HTTP response from JCommune to log its body
      */
     private void logErrorResponse(HttpResponse response) {
         try {
             logger.info("Error HTTP code was returned by JCommune: [{}]. Response body: [{}]",
                     response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent()));
         } catch (IOException e) {
             logger.warn("Was not possible to read JCommune error response due to exception.", e);
         }
     }
 
     /**
      * Creates HTTP request based on the specified method.
      *
      * @param httpMethod either {@link HttpDelete#getMethod()} or {@link HttpPost#getMethod()}
      * @param url        address of JCommune
      * @return constructed HTTP request based on specified method
      * @throws IllegalArgumentException if the method was neither DELETE nor POST
      */
     private HttpRequestBase createWithHttpMethod(String httpMethod, String url) {
         if (HttpDelete.METHOD_NAME.equals(httpMethod)) {
             return new HttpDelete(url);
         } else if (HttpPost.METHOD_NAME.equals(httpMethod)) {
             return new HttpPost(url);
         } else {
             throw new IllegalArgumentException("Wrong HTTP method name was specified: [" + httpMethod + "]");
         }
     }
 
     /**
      * Sets http-specific parameters and actually sends the request, this was factored out in a separate method in order
      * to be mocked out since it's impossible to mock the HTTP call itself.
      *
      * @param request prepared http request that has to be only sent, no URL or other configuration should be required
      * @return the HTTP response that actually came from the host
      * @throws IOException if problems (e.g. no connection) were found while sending request
      */
     @VisibleForTesting
     HttpResponse doSendRequest(HttpUriRequest request) throws IOException {
         HttpClient httpClient = new DefaultHttpClient();
         httpClient.getParams().setParameter("http.socket.timeout", HTTP_CONNECTION_TIMEOUT);
         httpClient.getParams().setParameter("http.connection.timeout", HTTP_CONNECTION_TIMEOUT);
         return httpClient.execute(request);
     }
 
 }
