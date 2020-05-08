 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server;
 
 import com.janrain.backplane.common.AuthException;
 import com.janrain.backplane.common.BackplaneServerException;
 import com.janrain.backplane.common.DateTimeUtils;
 import com.janrain.backplane.config.BackplaneConfig;
 import com.janrain.backplane.dao.DaoException;
 import com.janrain.backplane.server2.MessageResponse;
 import com.janrain.backplane.server2.dao.BP2DAOs;
 import com.janrain.backplane.server2.model.*;
 import com.janrain.backplane.server2.model.Channel;
 import com.janrain.backplane.server2.oauth2.model.*;
 import com.janrain.backplane.server2.oauth2.model.Token;
 import com.janrain.backplane.server2.model.Backplane2Message;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.oauth2.*;
 import com.janrain.servlet.InvalidRequestException;
 import com.janrain.util.RandomUtils;
 import com.janrain.util.ServletUtil;
 import com.janrain.utils.AnalyticsLogger;
 import com.yammer.metrics.core.MetricName;
 import com.yammer.metrics.core.TimerContext;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.HttpRequestMethodNotSupportedException;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 import scala.Option;
 import scala.Tuple2;
 import scala.collection.JavaConversions;
 
 import javax.inject.Inject;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 import static com.janrain.oauth2.OAuth2.*;
 import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
 
 /**
  * Backplane API implementation.
  *
  * @author Johnny Bufu, Tom Raney
  */
 @Controller
 @RequestMapping(value="/v2/*")
 @SuppressWarnings({"UnusedDeclaration"})
 public class Backplane2Controller {
 
     // - PUBLIC
 
     /** both view name and jsp variable */
     public static final String DIRECT_RESPONSE = "direct_response";
 
     /**
      * Handle dynamic discovery of this server's registration endpoint
      * @return
      */
     @RequestMapping(value = "/.well-known/host-meta", method = { RequestMethod.GET})
     public ModelAndView xrds(HttpServletRequest request, HttpServletResponse response) {
 
         ModelAndView view = new ModelAndView("xrd");
         view.addObject("host", "http://" + request.getServerName());
         view.addObject("secureHost", "https://" + request.getServerName());
         return view;
     }
 
     @RequestMapping(value = "/authorize", method = { RequestMethod.GET, RequestMethod.POST })
     public ModelAndView authorize(
                             HttpServletRequest request,
                             HttpServletResponse response,
                             @CookieValue( value = AUTH_SESSION_COOKIE, required = false) String authSessionCookie,
                             @CookieValue( value = AUTHORIZATION_REQUEST_COOKIE, required = false) String authorizationRequestCookie)
             throws AuthorizationException, DaoException {
 
         AuthorizationRequest authzRequest = null;
         String httpMethod = request.getMethod();
         String authZdecisionKey = request.getParameter(AUTHZ_DECISION_KEY);
         if (authZdecisionKey != null) {
             logger.debug("received valid authZdecisionKey:" + authZdecisionKey);
         }
 
         // not return from /authenticate && not authz decision post
         if ( request.getParameterMap().size() > 0  &&  StringUtils.isEmpty(authZdecisionKey) ) { 
             // incoming authz request
             authzRequest = parseAuthZrequest(request);
         }
 
         String authenticatedBusOwner = getAuthenticatedBusOwner(request, authSessionCookie);
         if (null == authenticatedBusOwner) {
             if (null != authzRequest) {
                 try {
                     logger.info("Persisting authorization request for client: " + authzRequest.get(AuthorizationRequestFields.CLIENT_ID()) +
                                 "[" + authzRequest.get(AuthorizationRequestFields.COOKIE())+"]");
                     com.janrain.backplane.server2.dao.BP2DAOs.authorizationRequestDao().store(authzRequest);
                     response.addCookie(new Cookie(AUTHORIZATION_REQUEST_COOKIE, authzRequest.get(AuthorizationRequestFields.COOKIE()).get()));
                 } catch (Exception e) {
                     throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_SERVER_ERROR, e.getMessage(), request, e);
                 }
             }
             logger.info("Bus owner not authenticated, redirecting to /authenticate");
             return new ModelAndView("redirect:https://" + request.getServerName() + "/v2/authenticate");
         }
 
         if (StringUtils.isEmpty(authZdecisionKey)) {
             // authorization request
             if (null == authzRequest) {
                 // return from /authenticate
                 try {
                     logger.debug("bp2.authorization.request cookie = " + authorizationRequestCookie);
                     authzRequest = com.janrain.backplane.server2.dao.BP2DAOs.authorizationRequestDao().get(authorizationRequestCookie).get();
                     logger.info("Retrieved authorization request for client:" + authzRequest.get(AuthorizationRequestFields.CLIENT_ID()) +
                                 "[" + authzRequest.get(AuthorizationRequestFields.COOKIE())+"]");
                 } catch (Exception e) {
                     throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_SERVER_ERROR, e.getMessage(), request, e);
                 }
             }
             return processAuthZrequest(authzRequest, authSessionCookie, authenticatedBusOwner);
         } else {
             // authZ decision from bus owner, accept only on post
             if (! "POST".equals(httpMethod)) {
                 throw new InvalidRequestException("Invalid HTTP method for authorization decision post: " + httpMethod);
             }
             return processAuthZdecision(authZdecisionKey, authSessionCookie, authenticatedBusOwner, authorizationRequestCookie, request);
         }
     }
 
     /**
      * Authenticates a bus owner and stores the authenticated session (cookie) to simpleDB.
      *
      * GET: displays authentication form
      * POST: processes authentication and returns to /authorize
      */
     @RequestMapping(value = "/authenticate", method = { RequestMethod.GET, RequestMethod.POST })
     public ModelAndView authenticate(
                           HttpServletRequest request,
                           HttpServletResponse response,
                           @RequestParam(required = false) String busOwner,
                           @RequestParam(required = false) String password) throws AuthException, BackplaneServerException, DaoException {
 
         ServletUtil.checkSecure(request);
 
         String httpMethod = request.getMethod();
         if ("GET".equals(httpMethod)) {
             logger.debug("returning view for GET");
             return new ModelAndView(BUS_OWNER_AUTH_FORM_JSP);
         } else if ("POST".equals(httpMethod)) {
             BP2DAOs.busOwnerDao().getAuthenticated(busOwner, password);
             logger.info("Authenticated bus owner: " + busOwner);
             persistAuthenticatedSession(response, busOwner);
             return new ModelAndView("redirect:https://" + request.getServerName() + "/v2/authorize");
         } else {
             throw new InvalidRequestException("Unsupported method for /authenticate: " + httpMethod);
         }
     }
 
     /**
      * The OAuth "Token Endpoint" is used to obtain an access token to be used
      * for retrieving messages from the Get Messages endpoint.
      *
      * @param scope     optional
      * @param callback  required
      * @return
      * @throws AuthException
      * @throws SimpleDBException
      * @throws BackplaneServerException
      */
 
     @RequestMapping(value = "/token", method = { RequestMethod.GET})
     @ResponseBody
     public Map<String,Object> getToken(final HttpServletRequest request, HttpServletResponse response,
                                            @RequestParam(value = OAUTH2_SCOPE_PARAM_NAME, required = false) final String scope,
                                            @RequestParam(required = false) final String bus,
                                            @RequestParam(required = false) final String callback,
                                            @RequestHeader(value = "Referer", required = false) String referer) throws DaoException {
 
         ServletUtil.checkSecure(request);
 
         final TimerContext context = getRegularTokenTimer.time();
 
         try {
             Map<String,Object> result = new AnonymousTokenRequest(callback, bus, scope, request).tokenResponse();
             // Refresh token requests are not logged to analytics.
             if (StringUtils.isNotEmpty(bus)) {
                 aniLogNewChannel(request, referer, bus, (String) result.get(OAUTH2_SCOPE_PARAM_NAME));
             }
 
             return result;
 
         } catch (TokenException e) {
             return handleTokenException(e, response);
         } finally {
             context.stop();
         }
     }
 
     /**
      * The OAuth "Token Endpoint" is used to obtain an access token to be used
      * for retrieving messages from the Get Messages endpoint.
      *
      * @param client_id
      * @param grant_type
      * @param redirect_uri
      * @param code
      * @param client_secret
      * @param scope
      * @return
      * @throws AuthException
      * @throws SimpleDBException
      * @throws BackplaneServerException
      */
 
     @RequestMapping(value = "/token", method = { RequestMethod.POST})
     @ResponseBody
     public Map<String,Object> token(HttpServletRequest request, HttpServletResponse response,
                                         @RequestParam(value = "client_id", required = false) String client_id,
                                         @RequestParam(value = "grant_type", required = false) String grant_type,
                                         @RequestParam(value = "redirect_uri", required = false) String redirect_uri,
                                         @RequestParam(value = "code", required = false) String code,
                                         @RequestParam(value = "client_secret", required = false) String client_secret,
                                         @RequestParam(value = "scope", required = false) String scope,
                                         @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
 
         ServletUtil.checkSecure(request);
 
         TimerContext context = getPrivilegedTokenTimer.time();
 
         try {
             checkClientCredentialsBasicAuthOnly(request.getQueryString(), client_id, client_secret);
             Client authenticatedClient = getAuthenticatedClient(authorizationHeader);
 
             return (new AuthenticatedTokenRequest(
                     grant_type, authenticatedClient, code, redirect_uri, scope, request)).tokenResponse();
         } catch (TokenException e) {
             return handleTokenException(e, response);
         } catch (AuthException e) {
             logger.error(e.getMessage());
             return handleTokenException(new TokenException(OAUTH2_TOKEN_INVALID_CLIENT, "Client authentication failed", SC_UNAUTHORIZED, e), response);
         } catch (Exception e) {
             return handleTokenException(new TokenException(OAUTH2_TOKEN_SERVER_ERROR, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e), response);
         } finally {
             context.stop();
         }
     }
 
     /**
      * Retrieve messages from the server.
      *
      * @param access_token required
      * @param block        optional
      * @param callback     optional
      * @param since        optional
      * @return json object
      * @throws SimpleDBException
      * @throws BackplaneServerException
      */
 
     @RequestMapping(value = "/messages", method = { RequestMethod.GET})
     public @ResponseBody Map<String,Object> messages(final HttpServletRequest request, HttpServletResponse response,
                                                      @RequestParam(value = "block", defaultValue = "0", required = false) String block,
                                                      @RequestParam(required = false) String callback,
                                                      @RequestParam(value = "since", required = false) String since,
                                                      @RequestHeader(value = "Referer", required = false) String referer)
             throws SimpleDBException, BackplaneServerException {
 
         ServletUtil.checkSecure(request);
 
         TimerContext context = null;
         // only time the event if it is not blocking
         if ("0".equals(block)) {
             context = v2GetsTimer.time();
         }
 
         try {
             MessageRequest messageRequest = new MessageRequest(callback, since, block);
 
             Option<Token> token = Token.fromRequest(request);
             if ( ! token.isDefined()) {
               throw new TokenException("invalid token", HttpServletResponse.SC_FORBIDDEN);
             }
             if (token.get().grantType().isRefresh()) {
                 return returnMessage(OAuth2.OAUTH2_TOKEN_INVALID_REQUEST, "Invalid token type: " + token.get().grantType(),
                         HttpServletResponse.SC_FORBIDDEN, response);
             }
 
             // val (framesResult, messages) = ...
             Tuple2<Map<String,Object>,scala.collection.immutable.List<Backplane2Message>> framesResultAndMessages = MessageResponse.scalaObject().apply(
                     request.getServerName(), token.get().grantType().isPrivileged(),
                     token.get().scope(), messageRequest.getSince(), MESSAGES_POLL_SLEEP_MILLIS, messageRequest.getReturnBefore());
             aniLogPollMessages(request, referer, JavaConversions.asJavaList(framesResultAndMessages._2()));
             return framesResultAndMessages._1();
 
         } catch (TokenException te) {
             return handleTokenException(te, response);
         } catch (InvalidRequestException ire) {
             throw ire;
         } catch (Exception e) {
             throw new BackplaneServerException("Error processing messages request: " + e.getMessage(), e);
         } finally {
             if (context != null) {
                 context.stop();
             }
         }
     }
 
 /*
     public Map<String, Object> asResponseFields(String serverName, boolean privileged) throws BackplaneServerException {
         List<Map<String,Object>> frames = new ArrayList<Map<String, Object>>();
         for (Backplane2Message message : messages) {
             frames.add(message.asFrame(serverName, privileged));
         }
 
         Map<String, Object> messagesResponse = new HashMap<String, Object>();
         messagesResponse.put("nextURL", "https://" + serverName + "/v2/messages" +
                 (!StringUtils.isBlank(lastMessageId) ? "?since=" + lastMessageId : ""));
         messagesResponse.put("moreMessages", moreMessages);
         messagesResponse.put("messages", frames);
         return messagesResponse;
     }
 */
 
 
     /**
      * Retrieve a single message from the server.
      *
      * @param request
      * @param response
      * @return
      */
     @RequestMapping(value = "/message/{msg_id:.*}", method = { RequestMethod.GET})
     public @ResponseBody Map<String,Object> message(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable final String msg_id,
                                 @RequestParam(required = false) String callback)
             throws BackplaneServerException, SimpleDBException, DaoException {
 
         ServletUtil.checkSecure(request);
 
         TimerContext context = v2GetSingleMessageTimer.time();
 
         try {
             new MessageRequest(callback, null, "0"); // validate callback only, if present
         } catch (InvalidRequestException e) {
             return handleInvalidRequest(e, response);
         }
 
         try {
             Option<Token> token = Token.fromRequest(request);
             if ( ! token.isDefined()) {
                 throw new TokenException("invalid token", HttpServletResponse.SC_FORBIDDEN);
             }
             if (token.get().grantType().isRefresh()) {
                 throw new TokenException("Invalid token type: " + token.get().grantType(), HttpServletResponse.SC_FORBIDDEN);
             }
 
             Backplane2Message message = BP2DAOs.messageDao().get(msg_id).getOrElse(null);
 
             if (message != null) {
                 if (! token.get().scope().isMessageInScope(message)) {
                     throw new TokenException("Message id '" + msg_id + "' not found", HttpServletResponse.SC_NOT_FOUND);
                 }
                 Map<String,Object> result = JavaConversions.mapAsJavaMap(message.asFrame(request.getServerName(), token.get().grantType().isPrivileged()));
                 aniLogGetMessage(request, message, token.get());
                 return result;
             } else {
                 return returnMessage(OAuth2.OAUTH2_TOKEN_INVALID_REQUEST, "Message id '" + msg_id + "' not found",
                         HttpServletResponse.SC_NOT_FOUND, response);
             }
         } catch (TokenException te) {
             return handleTokenException(te, response);
         } finally {
             context.stop();
         }
     }
 
     /**
      * Publish message to Backplane.
      * @param request
      * @param response
      * @return
      */
     @RequestMapping(value = "/message", method = { RequestMethod.POST})
     public @ResponseBody Map<String,Object>  postMessages(
             HttpServletRequest request, HttpServletResponse response,
             @RequestBody Map<String,Map<String,Object>> messagePostBody)
             throws SimpleDBException, BackplaneServerException {
 
         ServletUtil.checkSecure(request);
 
         final TimerContext context = v2PostTimer.time();
 
         try {
             Option<Token> token = Token.fromRequest(request);
             if ( ! token.isDefined()) {
                 throw new TokenException("invalid token", HttpServletResponse.SC_FORBIDDEN);
             }
             if ( token.get().grantType().isRefresh() || ! token.get().grantType().isPrivileged() ) {
                 throw new TokenException("Invalid token type: " + token.get().grantType(), HttpServletResponse.SC_FORBIDDEN);
             }
 
             Backplane2Message message = parsePostedMessage(messagePostBody, token.get());
             BP2DAOs.messageDao().store(message);
 
             aniLogNewMessage(request, message, token.get());
             response.setStatus(HttpServletResponse.SC_CREATED);
             return null;
 
         } catch (TokenException te) {
             return handleTokenException(te, response);
         } catch (InvalidRequestException ire) {
             throw ire;
         } catch (Exception e) {
             throw new BackplaneServerException("Error processing post request: " + e.getMessage(), e);
         } finally {
             context.stop();
         }
     }
 
     public Map<String, Object> returnMessage(final String errorCode, final String errorMessage, int responseCode, HttpServletResponse response) {
         response.setStatus(responseCode);
         return new HashMap<String,Object>() {{
             put(ERR_MSG_FIELD, errorCode);
             put(ERR_MSG_DESCRIPTION, errorMessage);
         }};
     }
 
     @ExceptionHandler
     public ModelAndView handleOauthAuthzError(final AuthorizationException e) {
         return authzRequestError(e.getOauthErrorCode(), e.getMessage(), e.getRedirectUri(), e.getState());
     }
     
     @ExceptionHandler
     @ResponseBody
     public Map<String,Object> handleTokenException(final TokenException e, HttpServletResponse response) {
         logger.warn("Error processing token request: " + e.getMessage(), BackplaneConfig.getDebugException(e));
         response.setStatus(e.getHttpResponseCode());
         return new HashMap<String,Object>() {{
             put(ERR_MSG_FIELD, e.getOauthErrorCode());
             put(ERR_MSG_DESCRIPTION, e.getMessage());
         }};
     }
 
     /**
      * Handle auth errors as part of normal application flow
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final AuthException e, HttpServletResponse response) {
         logger.warn("Backplane authentication error: " + e.getMessage(), BackplaneConfig.getDebugException(e));
         response.setStatus(SC_UNAUTHORIZED);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, e.getMessage());
         }};
     }
 
     /**
      * Handle invalid requests as a normal part of application flow
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, Object> handleInvalidRequest(final InvalidRequestException e, HttpServletResponse response) {
         logger.error("Error handling backplane request: " + e.getMessage(), BackplaneConfig.getDebugException(e));
         response.setStatus(e.getHttpResponseCode());
         return new HashMap<String,Object>() {{
             put(ERR_MSG_FIELD, e.getMessage());
             String errorDescription = e.getErrorDescription();
             if (StringUtils.isNotEmpty(errorDescription)) {
                 put(ERR_MSG_DESCRIPTION, errorDescription);
             }
         }};
     }
 
     /**
      * Handle invalid HTTP request method exceptions
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, Object> handleInvalidRequest(final HttpRequestMethodNotSupportedException e, HttpServletResponse response) {
         logger.warn("Error handling backplane request: " + e.getMessage(), BackplaneConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return new HashMap<String,Object>() {{
             put(ERR_MSG_FIELD, e.getMessage());
         }};
     }
 
     /**
      * Handle all other errors not normally a part of application flow.
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final Exception e, HttpServletResponse response) {
         logger.error("Error handling backplane request: " + e.getMessage(), BackplaneConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         return new HashMap<String,String>() {{
                 put(ERR_MSG_FIELD, BackplaneConfig.isDebugMode() ? e.getMessage() : "Error processing request.");
         }};
     }
 
 
     /*
     public static String randomString(int length) {
         byte[] randomBytes = new byte[length];
         // the base64 character set per RFC 4648 with last two members '-' and '_' removed due to possible
         // compatibility issues.
         byte[] digits = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T',
                          'U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n',
                          'o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7',
                          '8','9'};
         random.nextBytes(randomBytes);
         for (int i = 0; i < length; i++) {
             byte b = randomBytes[i];
             int c = Math.abs(b % digits.length);
             randomBytes[i] = digits[c];
         }
         try {
             return new String(randomBytes, "US-ASCII");
         }
         catch (UnsupportedEncodingException e) {
             logger.error("US-ASCII character encoding not supported", e); // shouldn't happen
             return null;
         }
     }
     */
     
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(Backplane2Controller.class);
 
     private static final String ERR_MSG_FIELD = "error";
     private static final String ERR_MSG_DESCRIPTION = "error_description";
 
     private static final String BUS_OWNER_AUTH_FORM_JSP = "bus_owner_auth";
     private static final String CLIENT_AUTHORIZATION_FORM_JSP = "client_authorization";
 
     private static final int AUTH_SESSION_COOKIE_LENGTH = 30;
     private static final String AUTH_SESSION_COOKIE = "bp2.bus.owner.auth";
     private static final int AUTHORIZATION_REQUEST_COOKIE_LENGTH = 30;
     private static final String AUTHORIZATION_REQUEST_COOKIE = "bp2.authorization.request";
 
     public static final String AUTHZ_DECISION_KEY = "auth_key";
 
     private static final int MESSAGES_POLL_SLEEP_MILLIS = 3000;
 
     @Inject
     private AnalyticsLogger anilogger;
 
     private void persistAuthenticatedSession(HttpServletResponse response, String busOwner) throws BackplaneServerException, DaoException {
         try {
             String authCookie = RandomUtils.randomString(AUTH_SESSION_COOKIE_LENGTH);
             com.janrain.backplane.server2.dao.BP2DAOs.authSessionDao().store(new AuthSession(busOwner, authCookie));
             response.addCookie(new Cookie(AUTH_SESSION_COOKIE, authCookie));
         } catch (Exception e) {
             throw new BackplaneServerException(e.getMessage());
         }
     }
 
     private String getAuthenticatedBusOwner(HttpServletRequest request, String authSessionCookie) throws DaoException {
         if (authSessionCookie == null) return null;
         try {
             AuthSession authSession = com.janrain.backplane.server2.dao.BP2DAOs.authSessionDao().get(authSessionCookie).getOrElse(null);
             if (authSession == null) {
                 return null;
             } else {
                 String authenticatedOwner = authSession.get(AuthSessionFields.AUTH_USER()).get();
                 logger.info("Session found for previously authenticated bus owner: " + authenticatedOwner);
                 return authenticatedOwner;
             }
         } catch (Exception e) {
             logger.error("Error looking up session for cookie: " + authSessionCookie, e);
             return null;
         }
     }
 
     private void authError(String errMsg) throws AuthException {
         logger.error(errMsg);
         try {
             throw new AuthException("Access denied. " + (BackplaneConfig.isDebugMode() ? errMsg : ""));
         } catch (Exception e) {
             throw new AuthException("Access denied.");
         }
     }
 
     private String paddedResponse(String callback, String s) {
         if (StringUtils.isBlank(callback)) {
             throw new InvalidRequestException("Callback cannot be blank.");
         }
         StringBuilder result = new StringBuilder(callback);
         result.append("(").append(s).append(")");
         return result.toString();
     }
 
     /** Parse, extract & validate an OAuth2 authorization request from the HTTP request */
     private AuthorizationRequest parseAuthZrequest(HttpServletRequest request) throws AuthorizationException {
         try {
             // parse authz request
             AuthorizationRequest authorizationRequest = new AuthorizationRequest(
                     RandomUtils.randomString(AUTHORIZATION_REQUEST_COOKIE_LENGTH),
                     ServletUtil.singleValueRequestParams(request));
             logger.info("Parsed authorization request: " + authorizationRequest);
             return authorizationRequest;
         } catch (Exception e) {
             throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_INVALID_REQUEST, e.getMessage(), request, e);
         }
     }
 
     /** Returns an authenticated Client, never null, or throws AuthException */
     private Client getAuthenticatedClient(String basicAuth) throws AuthException {
         String userPass = null;
         if (basicAuth == null || !basicAuth.startsWith("Basic ") || basicAuth.length() < 7) {
             authError("Invalid client authorization header: " + basicAuth);
         } else {
             try {
                 userPass = new String(Base64.decodeBase64(basicAuth.substring(6).getBytes("utf-8")));
             } catch (UnsupportedEncodingException e) {
                 authError("Cannot check client authentication, unsupported encoding: utf-8"); // shouldn't happen
             }
         }
 
         @SuppressWarnings({"ConstantConditions"})
         int delim = userPass.indexOf(":");
         if (delim == -1) {
             authError("Invalid Basic auth token: " + userPass);
         }
         String client = userPass.substring(0, delim);
         String pass = userPass.substring(delim + 1);
 
 
         return BP2DAOs.clientDao().getAuthenticated(client, pass);
     }
 
     /** Present an authorization form to the bus owner and obtain authorization decision */
     private ModelAndView processAuthZrequest(AuthorizationRequest authzRequest, String authSessionCookie, String authenticatedBusOwner) throws AuthorizationException {
         Map<String,String> model = new HashMap<String, String>();
 
         // generate & persist authZdecisionKey
         logger.debug("generate & persist authZdecisionKey");
         try {
             AuthorizationDecisionKey authorizationDecisionKey = new AuthorizationDecisionKey(authSessionCookie);
             com.janrain.backplane.server2.dao.BP2DAOs.authorizationDecisionKeyDao().store(authorizationDecisionKey);
 
             model.put("auth_key", authorizationDecisionKey.get(AuthorizationDecisionKeyFields.KEY()).get());
            model.put(AuthorizationRequestFields.CLIENT_ID().name().toLowerCase(), (String) authzRequest.get(AuthorizationRequestFields.CLIENT_ID().name()).getOrElse(null));
             model.put(AuthorizationRequestFields.REDIRECT_URI().name().toLowerCase(), authzRequest.getRedirectUri());
 
            Option<String> scopeOption = authzRequest.get(AuthorizationRequestFields.SCOPE().name());
            String scope = scopeOption.isDefined() ? scopeOption.get() : null;
             model.put(AuthorizationRequestFields.SCOPE().name().toLowerCase(), checkScope(scope, authenticatedBusOwner) );
 
             // return authZ form
             logger.info("Requesting bus owner authorization for :" + authzRequest.get(AuthorizationRequestFields.CLIENT_ID()) +
                     "[" + authzRequest.get(AuthorizationRequestFields.COOKIE())+"]");
             return new ModelAndView(CLIENT_AUTHORIZATION_FORM_JSP, model);
 
         } catch (Exception e) {
             throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_SERVER_ERROR, e.getMessage(), authzRequest, e);
         }
     }
 
     private String checkScope(String scope, String authenticatedBusOwner) throws BackplaneServerException {
         StringBuilder result = new StringBuilder();
         List<BusConfig2> ownedBuses = JavaConversions.seqAsJavaList(BP2DAOs.busDao().retrieveByOwner(authenticatedBusOwner));
         if(StringUtils.isEmpty(scope)) {
             // request scope empty, ask/offer permission to all owned buses
             for(BusConfig2 bus : ownedBuses) {
                 result.append("bus:").append(bus.get(BusConfig2Fields.BUS_NAME())).append(" ");
             }
             if(result.length() > 0) {
                 result.deleteCharAt(result.length()-1);
             }
         } else {
             List<String> ownedBusNames = new ArrayList<String>();
             for(BusConfig2 bus : ownedBuses) {
                 ownedBusNames.add(bus.id());
             }
             for(String scopeToken : scope.split(" ")) {
                 if(scopeToken.startsWith("bus:")) {
                     String bus = scopeToken.substring(4);
                     if (! ownedBusNames.contains(bus) ) continue;
                 }
                 result.append(scopeToken).append(" ");
             }
             if(result.length() > 0) {
                 result.deleteCharAt(result.length()-1);
             }
         }
 
         String resultString = result.toString();
         if (! resultString.equals(scope)) {
             logger.info("Authenticated bus owner " + authenticatedBusOwner + " is authoritative for requested scope: " + resultString);
         }
         return resultString;
     }
 
     private ModelAndView processAuthZdecision(String authZdecisionKey, String authSessionCookie,
                                               String authenticatedBusOwner,
                                               String authorizationRequestCookie, HttpServletRequest request) throws AuthorizationException {
         AuthorizationRequest authorizationRequest = null;
 
         logger.debug("processAuthZdecision()");
 
         try {
             // retrieve authorization request
             authorizationRequest = com.janrain.backplane.server2.dao.BP2DAOs.authorizationRequestDao().get(authorizationRequestCookie).get();
 
             // check authZdecisionKey
             AuthorizationDecisionKey authZdecisionKeyEntry = com.janrain.backplane.server2.dao.BP2DAOs.authorizationDecisionKeyDao().get(authZdecisionKey).getOrElse(null);
             if (null == authZdecisionKeyEntry || ! authSessionCookie.equals(authZdecisionKeyEntry.get(AuthorizationDecisionKeyFields.AUTH_COOKIE()).getOrElse(null))) {
                 throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_ACCESS_DENIED, "Presented authorization key was issued to a different authenticated bus owner.", authorizationRequest);
             }
 
             if (! "Authorize".equals(request.getParameter("authorize"))) {
                 throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_ACCESS_DENIED, "Bus owner denied authorization.", authorizationRequest);
             } else {
                 // todo: use (and check) scope posted back by bus owner
                Option<String> scopeOption = authorizationRequest.get(AuthorizationRequestFields.SCOPE());
                String scopeString = checkScope(scopeOption.isDefined() ? scopeOption.get() : null, authenticatedBusOwner);
                 // create grant/code
                 Grant2 grant =  new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE,
                             authenticatedBusOwner,
                             authorizationRequest.get(AuthorizationRequestFields.CLIENT_ID()).get(),
                             scopeString).buildGrant();
                 BP2DAOs.grantDao().store(grant);
 
                 logger.info("Authorized " + authorizationRequest.get(AuthorizationRequestFields.CLIENT_ID())+
                         "[" + authorizationRequest.get(AuthorizationRequestFields.COOKIE())+"]" + "grant ID: " + grant.id());
 
                 // return OAuth2 authz response
                 final String code = grant.id();
                Option<String> stateOption = authorizationRequest.get(AuthorizationRequestFields.STATE());
                final String state = scopeOption.isDefined() ? scopeOption.get() : null;
 
                 try {
                     return new ModelAndView("redirect:" + UrlResponseFormat.QUERY.encode(
                             authorizationRequest.getRedirectUri(),
                             new HashMap<String, String>() {{
                                 put(OAuth2.OAUTH2_AUTHZ_RESPONSE_CODE, code);
                                 if (StringUtils.isNotEmpty(state)) {
                                     put(OAuth2.OAUTH2_AUTHZ_RESPONSE_STATE, state);
                                 }
                             }}));
                 } catch (ValidationException ve) {
                     String errMsg = "Error building (positive) authorization response: " + ve.getMessage();
                     logger.error(errMsg, ve);
                     return authzRequestError(OAuth2.OAUTH2_AUTHZ_DIRECT_ERROR, errMsg,
                             authorizationRequest.getRedirectUri(),
                             (String)authorizationRequest.get(AuthorizationRequestFields.STATE()).getOrElse(null));
                 }
             }
         } catch (Exception e) {
             throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_SERVER_ERROR, e.getMessage(), authorizationRequest, e);
         }
     }
 
     private static ModelAndView authzRequestError( final String oauthErrCode, final String errMsg,
                                                    final String redirectUri, final String state) {
         // direct or in/redirect
         if (OAuth2.OAUTH2_AUTHZ_DIRECT_ERROR.equals(oauthErrCode)) {
             logger.error("Authorization error: " + errMsg);
             return new ModelAndView(DIRECT_RESPONSE, new HashMap<String, Object>() {{
                 put(DIRECT_RESPONSE, errMsg);
             }});
         } else {
             try {
                 return new ModelAndView("redirect:" + UrlResponseFormat.QUERY.encode(
                         redirectUri,
                         new HashMap<String, String>() {{
                             put(OAuth2.OAUTH2_AUTHZ_ERROR_FIELD_NAME, oauthErrCode);
                             put(OAuth2.OAUTH2_AUTHZ_ERROR_DESC_FIELD_NAME, errMsg);
                             if (StringUtils.isNotEmpty(state)) {
                                 put(AuthorizationRequestFields.STATE().name(), state);
                             }
                         }}));
 
             } catch (ValidationException e) {
                 logger.error("Error building redirect_uri: " + e.getMessage());
                 return new ModelAndView(DIRECT_RESPONSE, new HashMap<String, Object>() {{
                     put(DIRECT_RESPONSE, errMsg);
                 }});
             }
         }
     }
 
     /**
      * Throws AuthException if either of the following fail:
      *   Client credentials MUST NOT be included in the request URI (OAuth2 2.3.1)
      *   Client credentials in request body are NOT RECOMMENDED (OAuth2 2.3.1)
      *
      * @param queryString request query string
      * @param client_id from request parameters (may be from POST/request body)
      * @param client_secret from request parameters (may be from POST/request body)
      */
     private void checkClientCredentialsBasicAuthOnly(String queryString, String client_id, String client_secret) throws AuthException {
         if (StringUtils.isNotEmpty(queryString)) {
             Map<String,String> queryParamsMap = new HashMap<String, String>();
             for(String queryParamPair : Arrays.asList(queryString.split("&"))) {
                 String[] nameVal = queryParamPair.split("=", 2);
                 queryParamsMap.put(nameVal[0], nameVal.length >0 ? nameVal[1] : null);
             }
             if(queryParamsMap.containsKey("client_id") || queryParamsMap.containsKey("client_secret")) {
                 authError("Client credentials MUST NOT be included in the request URI (OAuth2 2.3.1)");
             }
         }
         if (StringUtils.isNotEmpty(client_id) || StringUtils.isNotEmpty(client_secret)) {
             authError("Client credentials in request body are NOT RECOMMENDED (OAuth2 2.3.1)");
         }
     }
 
     private Backplane2Message parsePostedMessage(Map<String, Map<String, Object>> messagePostBody, Token token) throws BackplaneServerException, DaoException {
         List<Backplane2Message> result = new ArrayList<Backplane2Message>();
 
         Map<String,Object> msg = messagePostBody.get("message");
         if (msg == null) { // no message body?
             throw new InvalidRequestException("Missing message payload", HttpServletResponse.SC_BAD_REQUEST);
         }
 
         if (messagePostBody.keySet().size() != 1) { // other garbage in the payload
             throw new InvalidRequestException("Invalid data in payload", HttpServletResponse.SC_BAD_REQUEST);
         }
 
         String channelId = msg.get(Backplane2MessageFields.CHANNEL().name()) != null ? msg.get(Backplane2MessageFields.CHANNEL().name()).toString() : null;
         String bus = msg.get(Backplane2MessageFields.BUS().name()) != null ? msg.get(Backplane2MessageFields.BUS().name()).toString() : null;
         Channel channel = getChannel(channelId);
         String boundBus = channel == null ? null : (String) channel.get(ChannelFields.BUS()).getOrElse(null);
         if ( channel == null || ! StringUtils.equals(bus, boundBus)) {
             throw new InvalidRequestException("Invalid bus - channel binding ", HttpServletResponse.SC_FORBIDDEN);
         }
 
         // check to see if channel is already full
         if (BP2DAOs.messageDao().messageCount(channelId) >= BackplaneConfig.getDefaultMaxMessageLimit()) {
             throw new InvalidRequestException("Message limit of " + BackplaneConfig.getDefaultMaxMessageLimit() + " has been reached for channel '" + channel + "'",
                     HttpServletResponse.SC_FORBIDDEN);
         }
 
         try {
             Backplane2Message message = new Backplane2Message(
                     (String)token.get(TokenFields.CLIENT_SOURCE_URL()).getOrElse(null),
                     Integer.parseInt( (String) channel.get(ChannelFields.MESSAGE_EXPIRE_DEFAULT_SECONDS()).getOrElse(null)),
                     Integer.parseInt( (String) channel.get(ChannelFields.MESSAGE_EXPIRE_MAX_SECONDS()).getOrElse(null)),
                     msg);
             if ( ! token.scope().isMessageInScope(message) ) {
                 throw new InvalidRequestException("Invalid bus in message", HttpServletResponse.SC_FORBIDDEN);
             }
             return  message;
         } catch (Exception e) {
             throw new InvalidRequestException("Invalid message data: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
         }
     }
 
     private Channel getChannel(String channelId) throws BackplaneServerException, DaoException {
         Option<Channel> channel = BP2DAOs.channelDao().get(channelId);
         return channel.getOrElse(null);
     }
 
     private void aniLogNewChannel(HttpServletRequest request, String referer, String bus, String scope) {
         if (!anilogger.isEnabled()) {
             return;
         }
 
         int delim = scope.indexOf("channel:");
         String channel = scope.substring(delim + 8);
         String channelId = "https://" + request.getServerName() + "/v2/bus/" + bus + "/channel/" + channel;
         String siteHost = (referer != null) ? ServletUtil.getHostFromUrl(referer) : null;
 
         Map<String,Object> aniEvent = new HashMap<String,Object>();
         aniEvent.put("channel_id", channelId);
         aniEvent.put("bus", bus);
         aniEvent.put("site_host", siteHost);
 
         aniLog("new_channel", aniEvent);
     }
 
     private void aniLogPollMessages(HttpServletRequest request, String referer, List<Backplane2Message> messages) {
         if (!anilogger.isEnabled()) {
             return;
         }
 
         String siteHost = (referer != null) ? ServletUtil.getHostFromUrl(referer) : null;
         String serverName = request.getServerName();
         Map<String,Object> aniEvent = new HashMap<String,Object>();
 
         List<Map<String,String>> messagesMeta = new ArrayList<Map<String,String>>();
         for (Backplane2Message message : messages) {
             String bus = message.bus();
             String channelId = "https://" + serverName + "/v2/bus/" + bus + "/channel/" + message.channel();
             Map<String,String> anotherMeta = new HashMap<String,String>();
             anotherMeta.put("id", message.id());
             anotherMeta.put("bus", bus);
             anotherMeta.put("channel_id", channelId);
             messagesMeta.add(anotherMeta);
         }
         aniEvent.put("messages", messagesMeta);
         aniEvent.put("site_host", siteHost);
 
         aniLog("poll_messages", aniEvent);
     }
 
     private void aniLogGetMessage(HttpServletRequest request, Backplane2Message message, Token token) {
         if (!anilogger.isEnabled()) {
             return;
         }
 
         String bus = message.bus();
         String channelId = "https://" + request.getServerName() + "/v2/bus/" + bus + "/channel/" + message.channel();
         Map<String,Object> aniEvent = new HashMap<String,Object>();
         aniEvent.put("message_id", message.id());
         aniEvent.put("bus", bus);
         aniEvent.put("channel_id", channelId);
         aniEvent.put("client_id", token.get(TokenFields.ISSUED_TO_CLIENT()));
 
         aniLog("get_message", aniEvent);
     }
 
     private void aniLogNewMessage(HttpServletRequest request, Backplane2Message message, Token token) {
         if (!anilogger.isEnabled()) {
             return;
         }
 
         String bus = message.bus();
         String channel = message.channel();
         String channelId = "https://" + request.getServerName() + "/v2/bus/" + bus + "/channel/" + channel;
         String clientId = (String) token.get(TokenFields.ISSUED_TO_CLIENT()).getOrElse(null);
         Map<String,Object> aniEvent = new HashMap<String,Object>();
         aniEvent.put("channel_id", channelId);
         aniEvent.put("bus", bus);
         aniEvent.put("client_id", clientId);
 
         aniLog("new_message", aniEvent);
     }
 
     private void aniLog(String eventName, Map<String,Object> eventData) {
         ObjectMapper mapper = new ObjectMapper();
         String time = DateTimeUtils.ISO8601.get().format(new Date(System.currentTimeMillis()));
         eventData.put("time", time);
         eventData.put("version", "v2");
         try {
             anilogger.log(eventName, mapper.writeValueAsString(eventData));
         } catch (Exception e) {
             String errMsg = "Error sending analytics event: " + e.getMessage();
             logger.error(errMsg, BackplaneConfig.getDebugException(e));
         }
     }
 
     private final com.yammer.metrics.core.Timer v2GetsTimer =
             com.yammer.metrics.Metrics.newTimer(new MetricName("v2", this.getClass().getName().replace(".","_"), "v2_gets_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
     private final com.yammer.metrics.core.Timer v2GetSingleMessageTimer =
             com.yammer.metrics.Metrics.newTimer(new MetricName("v2", this.getClass().getName().replace(".","_"), "v2_get_single_message_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
     private final com.yammer.metrics.core.Timer v2PostTimer =
             com.yammer.metrics.Metrics.newTimer(new MetricName("v2", this.getClass().getName().replace(".","_"), "v2_posts_time"), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
     private final com.yammer.metrics.core.Timer getRegularTokenTimer =
             com.yammer.metrics.Metrics.newTimer(new MetricName("v2", this.getClass().getName().replace(".","_"), "v2_get_reg_tokens_time"), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
     private final com.yammer.metrics.core.Timer getPrivilegedTokenTimer =
             com.yammer.metrics.Metrics.newTimer(new MetricName("v2", this.getClass().getName().replace(".","_"), "v2_get_privileged_tokens_time"), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
 
 }
