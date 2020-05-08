 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Anton Dmitrijev
  */
 package eionet.webq.web.interceptor;
 
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.HttpStatusCodeException;
 import org.springframework.web.client.RestOperations;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 /**
  * Intercepts calls to pages for CDR integration.
  */
 @Component
 public class CdrAuthorizationInterceptor extends HandlerInterceptorAdapter {
     /**
      * Logger for this class.
      */
     private static final Logger LOGGER = Logger.getLogger(CdrAuthorizationInterceptor.class);
     /**
      * Flag signals that request handling could proceed.
      */
     private static final boolean PROCEED = true;
     /**
      * Flag signals that response already written and no more handling required.
      */
     private static final boolean STOP_REQUEST_PROPAGATION = false;
     /**
      * Session attribute name for counting unsuccessful authorization counts.
      */
     static final String AUTHORIZATION_TRY_COUNT = "authorization-try-count";
     /**
      * Session attribute name for counting unsuccessful authorization counts.
      */
     static final Integer ALLOWED_AUTHORIZATION_FAILURES_COUNT = 3;
     /**
      * Authorization header name.
      */
     private static final String AUTHORIZATION_HEADER = "Authorization";
     /**
      * Authorization failed attribute.
      */
     public static final String AUTHORIZATION_FAILED_ATTRIBUTE = CdrAuthorizationInterceptor.class.getName();
     /**
      * Rest operations.
      */
     @Autowired
     private RestOperations restOperations;
     /**
      *  Save xml files to cdr method name.
      */
     @Value("#{ws['cdr.login']}")
     private String cdrLoginMethod;
     /**
      * Current session.
      */
     @Autowired
     private HttpSession session;
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         String authorization = request.getHeader(AUTHORIZATION_HEADER);
         if (StringUtils.isNotEmpty(authorization)) {
             HttpHeaders headers = new HttpHeaders();
             headers.add(AUTHORIZATION_HEADER, authorization);
             try {
                 ResponseEntity<String> loginResponse
                         = restOperations.postForEntity(extractCdrUrl(request) + "/" + cdrLoginMethod,
                         new HttpEntity<Object>(headers), String.class);
                 LOGGER.info("Response code received from CDR authorization " + loginResponse.getStatusCode());
                 return PROCEED;
             } catch (HttpStatusCodeException e) {
                 if (e.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                     LOGGER.warn("Authorization against CDR failed with unexpected HTTP status code", e);
                 }
             }
         }
 
         if (isFailureCountsEqualsToAllowedFailuresCount()) {
             request.setAttribute(AUTHORIZATION_FAILED_ATTRIBUTE, AUTHORIZATION_FAILED_ATTRIBUTE);
             session.removeAttribute(AUTHORIZATION_TRY_COUNT);
             return PROCEED;
         }
 
         increaseFailedAuthorizationsCount();
         response.addHeader("WWW-Authenticate", "Basic realm=\"Please login to use webforms.\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
         return STOP_REQUEST_PROPAGATION;
     }
 
     public void setCdrLoginMethod(String cdrLoginMethod) {
         this.cdrLoginMethod = cdrLoginMethod;
     }
 
     private boolean isFailureCountsEqualsToAllowedFailuresCount() {
         return ALLOWED_AUTHORIZATION_FAILURES_COUNT.equals(session.getAttribute(AUTHORIZATION_TRY_COUNT));
     }
 
     /**
      * Extracts CDR host URL from request.
      *
      * @param request http request
      * @return host URL
      */
     private String extractCdrUrl(HttpServletRequest request) {
         return defaultString(getUrlHost(request.getParameter("base_uri")),
                 defaultString(getUrlHost(request.getParameter("envelope")),
                         getUrlHost(request.getParameter("instance"))));
     }
 
     /**
      * Gets host from url string.
      * Result will be in format protocol://host:port.
      *
      * @param url url to be checked
      * @return host or null if provided string is malformed URL
      */
     private String getUrlHost(String url) {
         try {
             URL parsedUrl = new URL(url);
             return parsedUrl.getProtocol() + "://" + parsedUrl.getAuthority();
         } catch (MalformedURLException e) {
             return null;
         }
     }
 
     /**
      * Increase failed authorizations count.
      */
     private void increaseFailedAuthorizationsCount() {
         Integer failedAttempts = (Integer) session.getAttribute(AUTHORIZATION_TRY_COUNT);
         session.setAttribute(AUTHORIZATION_TRY_COUNT, failedAttempts != null ? failedAttempts + 1 : 1);
     }
 }
