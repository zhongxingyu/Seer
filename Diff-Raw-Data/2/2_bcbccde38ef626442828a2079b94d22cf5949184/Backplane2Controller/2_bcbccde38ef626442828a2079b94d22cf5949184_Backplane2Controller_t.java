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
 
 import com.janrain.backplane2.server.config.*;
 import com.janrain.backplane2.server.dao.DaoFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.crypto.ChannelUtil;
 import com.janrain.crypto.HmacHashUtils;
 import com.janrain.metrics.MetricsAccumulator;
 import com.janrain.oauth2.*;
 import com.janrain.servlet.ServletUtil;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.TimerMetric;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.inject.Inject;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 import java.util.concurrent.Callable;
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
             throws AuthorizationException {
 
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
                     logger.info("Persisting authorization request for client: " + authzRequest.get(AuthorizationRequest.Field.CLIENT_ID) +
                                 "[" + authzRequest.get(AuthorizationRequest.Field.COOKIE)+"]");
                     daoFactory.getAuthorizationRequestDAO().persist(authzRequest);
                     response.addCookie(new Cookie(AUTHORIZATION_REQUEST_COOKIE, authzRequest.get(AuthorizationRequest.Field.COOKIE)));
                 } catch (SimpleDBException e) {
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
                     authzRequest = daoFactory.getAuthorizationRequestDAO().retrieveAuthorizationRequest(authorizationRequestCookie);
                     logger.info("Retrieved authorization request for client:" + authzRequest.get(AuthorizationRequest.Field.CLIENT_ID) +
                                 "[" + authzRequest.get(AuthorizationRequest.Field.COOKIE)+"]");
                 } catch (SimpleDBException e) {
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
                           @RequestParam(required = false) String password) throws AuthException, SimpleDBException {
 
         ServletUtil.checkSecure(request);
 
         String httpMethod = request.getMethod();
         if ("GET".equals(httpMethod)) {
             logger.debug("returning view for GET");
             return new ModelAndView(BUS_OWNER_AUTH_FORM_JSP);
         } else if ("POST".equals(httpMethod)) {
             checkBusOwnerAuth(busOwner, password);
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
                                            @RequestParam(required = false) final String refresh_token,
                                            @RequestHeader(value = "Authorization", required = false) final String authorizationHeader) {
 
         ServletUtil.checkSecure(request);
 
         try {
             return v2GetRegTokens.time(new Callable<Map<String, Object>>() {
                 @Override
                 public Map<String, Object> call() throws Exception {
                     return (new AnonymousTokenRequest(callback, bus, scope, refresh_token, daoFactory, request, authorizationHeader).tokenResponse());
                 }
             });
         } catch (TokenException e) {
             return handleTokenException(e, response);
         } catch (InvalidRequestException ire) {
             throw ire;
         } catch (Exception e) {
             return handleTokenException(new TokenException(OAUTH2_TOKEN_SERVER_ERROR, "Error processing anonymous token request: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e), response);
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
                                         @RequestParam(required = false) String refresh_token,
                                         @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
 
         ServletUtil.checkSecure(request);
 
         Client authenticatedClient;
         try {
             checkClientCredentialsBasicAuthOnly(request.getQueryString(), client_id, client_secret);
             authenticatedClient = getAuthenticatedClient(authorizationHeader);
         } catch (AuthException e) {
             logger.error(e.getMessage());
             return handleTokenException(new TokenException(OAUTH2_TOKEN_INVALID_CLIENT, "Client authentication failed", SC_UNAUTHORIZED, e), response);
         }
 
         try {
             return (new AuthenticatedTokenRequest(
                     grant_type, authenticatedClient, code, redirect_uri, refresh_token, scope,
                     daoFactory, request, authorizationHeader)).tokenResponse();
         } catch (TokenException e) {
             return handleTokenException(e, response);
         } catch (Exception e) {
             return handleTokenException(new TokenException(OAUTH2_TOKEN_SERVER_ERROR, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e), response);
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
                                 @RequestParam(value = OAUTH2_ACCESS_TOKEN_PARAM_NAME, required = false) final String access_token,
                                 @RequestParam(value = "block", defaultValue = "0", required = false) String block,
                                 @RequestParam(required = false) String callback,
                                 @RequestParam(value = "since", required = false) String since,
                                 @RequestHeader(value = "Authorization", required = false) final String authorizationHeader)
             throws SimpleDBException, BackplaneServerException {
 
         ServletUtil.checkSecure(request);
 
         final MessageRequest messageRequest;
         try {
             messageRequest = new MessageRequest(callback, since, block);
         } catch (InvalidRequestException e) {
             return handleInvalidRequest(e, response);
         }
 
         try {
             return v2Gets.time(new Callable<Map<String, Object>>() {
                 @Override
                 public Map<String, Object> call() throws Exception {
                     Token token = Token.fromRequest(daoFactory, request, access_token, authorizationHeader);
                     if (token.getType().isRefresh()) {
                         throw new TokenException("Invalid token type: " + token.getType(), HttpServletResponse.SC_FORBIDDEN);
                     }
 
 
                     MessagesResponse bpResponse = new MessagesResponse(messageRequest.getSince());
                     boolean exit = false;
                     do {
                         daoFactory.getBackplaneMessageDAO().retrieveMesssagesPerScope(bpResponse, token);
                         if (!bpResponse.hasMessages() && new Date().before(messageRequest.getReturnBefore())) {
                             try {
                                 Thread.sleep(3000);
                             } catch (InterruptedException e) {
                                 //ignore
                             }
                         } else {
                             exit = true;
                         }
                     } while (!exit);
 
                     return bpResponse.asResponseFields(request.getServerName(), token.getType().isPrivileged());
                 }
             });
         } catch (TokenException te) {
             return handleTokenException(te, response);
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (BackplaneServerException bse) {
             throw bse;
         } catch (InvalidRequestException ire) {
             throw ire;
         } catch (Exception e) {
             throw new BackplaneServerException("Error processing messages request: " + e.getMessage(), e);
         }
     }
 
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
                                 @RequestParam(value = OAUTH2_ACCESS_TOKEN_PARAM_NAME, required = false) String access_token,
                                 @RequestParam(required = false) String callback,
                                 @RequestHeader(value = "Authorization", required = false) String authorizationHeader)
             throws BackplaneServerException, SimpleDBException {
 
         ServletUtil.checkSecure(request);
 
         try {
             new MessageRequest(callback, null, "0"); // validate callback only, if present
         } catch (InvalidRequestException e) {
             return handleInvalidRequest(e, response);
         }
 
         try {
             Token token = Token.fromRequest(daoFactory, request, access_token, authorizationHeader);
             if (token.getType().isRefresh()) {
                 throw new TokenException("Invalid token type: " + token.getType(), HttpServletResponse.SC_FORBIDDEN);
             }
 
             return daoFactory.getBackplaneMessageDAO().retrieveBackplaneMessage(msg_id, token)
                     .asFrame(request.getServerName(), token.getType().isPrivileged());
 
         } catch (TokenException te) {
             return handleTokenException(te, response);
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
                   final HttpServletRequest request, final HttpServletResponse response,
                   @RequestBody final Map<String,Map<String,Object>> messagePostBody,
                   @RequestParam(value = OAUTH2_ACCESS_TOKEN_PARAM_NAME, required = false) final String access_token,
                   @RequestHeader(value = "Authorization", required = false) final String authorizationHeader)
             throws SimpleDBException, BackplaneServerException {
 
         ServletUtil.checkSecure(request);
 
         try {
             return v2Posts.time(new Callable<Map<String, Object>>() {
                 @Override
                 public Map<String, Object> call() throws Exception {
                     Token token = Token.fromRequest(daoFactory, request, access_token, authorizationHeader);
                     if ( token.getType().isRefresh() || ! token.getType().isPrivileged() ) {
                         throw new TokenException("Invalid token type: " + token.getType(), HttpServletResponse.SC_FORBIDDEN);
                     }
 
                     daoFactory.getBackplaneMessageDAO().persist(parsePostedMessage(messagePostBody, token));
 
                     response.setStatus(HttpServletResponse.SC_CREATED);
                     return null;
                 }
             });
         } catch (TokenException te) {
             return handleTokenException(te, response);
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (InvalidRequestException ire) {
             throw ire;
         } catch (Exception e) {
             throw new BackplaneServerException("Error processing post request: " + e.getMessage(), e);
         }
     }
 
     @ExceptionHandler
     public ModelAndView handleOauthAuthzError(final AuthorizationException e) {
         return authzRequestError(e.getOauthErrorCode(), e.getMessage(), e.getRedirectUri(), e.getState());
     }
     
     @ExceptionHandler
     @ResponseBody
     public Map<String,Object> handleTokenException(final TokenException e, HttpServletResponse response) {
         logger.error("Error processing token request: " + e.getMessage(), bpConfig.getDebugException(e));
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
         logger.error("Backplane authentication error: " + bpConfig.getDebugException(e));
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
         logger.error("Error handling backplane request", bpConfig.getDebugException(e));
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
      * Handle all other errors not normally a part of application flow.
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final Exception e, HttpServletResponse response) {
         logger.error("Error handling backplane request", bpConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         return new HashMap<String,String>() {{
             try {
                 put(ERR_MSG_FIELD, bpConfig.isDebugMode() ? e.getMessage() : "Error processing request.");
             } catch (SimpleDBException e1) {
                 put(ERR_MSG_FIELD, "Error processing request.");
             }
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
 
     @Inject
     private Backplane2Config bpConfig;
 
     @Inject
     private DaoFactory daoFactory;
 
     @Inject
     private MetricsAccumulator metricAccumulator;
 
     //private static final Random random = new SecureRandom();
 
     private void checkBusOwnerAuth(String busOwner, String password) throws AuthException {
         User busOwnerEntry = null;
         try {
             busOwnerEntry = daoFactory.getBusOwnerDAO().retrieveBusOwner(busOwner);
         } catch (SimpleDBException e) {
             logger.error("Error looking up bus owner user: " + busOwner, e);
             authError("Error looking up bus owner user: " + busOwner);
         }
 
         if (busOwnerEntry == null) {
             authError("Bus owner user not found: " + busOwner);
         } else if ( ! HmacHashUtils.checkHmacHash(password, busOwnerEntry.get(User.Field.PWDHASH)) ) {
             authError("Incorrect password for bus owner user " + busOwner);
         }
         logger.info("Authenticated bus owner: " + busOwner);
     }
 
     private void persistAuthenticatedSession(HttpServletResponse response, String busOwner) throws SimpleDBException {
         String authCookie = ChannelUtil.randomString(AUTH_SESSION_COOKIE_LENGTH);
         daoFactory.getAuthSessionDAO().persist(new AuthSession(busOwner, authCookie));
         response.addCookie(new Cookie(AUTH_SESSION_COOKIE, authCookie));
     }
 
     private String getAuthenticatedBusOwner(HttpServletRequest request, String authSessionCookie) {
         if (authSessionCookie == null) return null;
         try {
             AuthSession authSession = daoFactory.getAuthSessionDAO().retrieveAuthSession(authSessionCookie);
             String authenticatedOwner = authSession.get(AuthSession.Field.AUTH_USER);
             logger.info("Session found for previously authenticated bus owner: " + authenticatedOwner);
             return authenticatedOwner;
         } catch (SimpleDBException e) {
             logger.error("Error looking up session for cookie: " + authSessionCookie, e);
             return null;
         }
     }
 
     private void authError(String errMsg) throws AuthException {
         logger.error(errMsg);
         try {
             throw new AuthException("Access denied. " + (bpConfig.isDebugMode() ? errMsg : ""));
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
                     ChannelUtil.randomString(AUTHORIZATION_REQUEST_COOKIE_LENGTH),
                     request.getParameterMap());
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
 
         Client clientEntry = null;
         try {
             clientEntry = daoFactory.getClientDAO().retrieveClient(client);
         } catch (SimpleDBException e) {
             logger.error("Error looking up client: " + client, e);
             authError("Error looking up client: " + client);
         }
 
         if (clientEntry == null) {
             authError("Client not found: " + client);
         } else if (!HmacHashUtils.checkHmacHash(pass, clientEntry.get(Client.Field.PWDHASH))) {
             authError("Incorrect password for client " + client);
         }
 
         logger.info("Authenticated client: " + client);
         return clientEntry;
     }
 
     /** Present an authorization form to the bus owner and obtain authorization decision */
     private ModelAndView processAuthZrequest(AuthorizationRequest authzRequest, String authSessionCookie, String authenticatedBusOwner) throws AuthorizationException {
         Map<String,String> model = new HashMap<String, String>();
 
         // generate & persist authZdecisionKey
         logger.debug("generate & persist authZdecisionKey");
         try {
             AuthorizationDecisionKey authorizationDecisionKey = new AuthorizationDecisionKey(authSessionCookie);
             daoFactory.getAuthorizationDecisionKeyDAO().persist(authorizationDecisionKey);
 
             model.put("auth_key", authorizationDecisionKey.get(AuthorizationDecisionKey.Field.KEY));
             model.put(AuthorizationRequest.Field.CLIENT_ID.getFieldName().toLowerCase(), authzRequest.get(AuthorizationRequest.Field.CLIENT_ID));
             model.put(AuthorizationRequest.Field.REDIRECT_URI.getFieldName().toLowerCase(), authzRequest.getRedirectUri(daoFactory.getClientDAO()));
 
             String scope = authzRequest.get(AuthorizationRequest.Field.SCOPE);
             model.put(AuthorizationRequest.Field.SCOPE.getFieldName().toLowerCase(), checkScope(scope, authenticatedBusOwner) );
 
             // return authZ form
             logger.info("Requesting bus owner authorization for :" + authzRequest.get(AuthorizationRequest.Field.CLIENT_ID) +
                     "[" + authzRequest.get(AuthorizationRequest.Field.COOKIE)+"]");
             return new ModelAndView(CLIENT_AUTHORIZATION_FORM_JSP, model);
 
         } catch (SimpleDBException e) {
             throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_SERVER_ERROR, e.getMessage(), authzRequest, e);
         }
     }
 
     private String checkScope(String scope, String authenticatedBusOwner) throws SimpleDBException {
         StringBuilder result = new StringBuilder();
         List<BusConfig2> ownedBuses = daoFactory.getBusDao().retrieveByOwner(authenticatedBusOwner);
         if(StringUtils.isEmpty(scope)) {
             // request scope empty, ask/offer permission to all owned buses
             for(BusConfig2 bus : ownedBuses) {
                 result.append("bus:").append(bus.get(BusConfig2.Field.BUS_NAME)).append(" ");
             }
             if(result.length() > 0) {
                 result.deleteCharAt(result.length()-1);
             }
         } else {
             List<String> ownedBusNames = new ArrayList<String>();
             for(BusConfig2 bus : ownedBuses) {
                 ownedBusNames.add(bus.get(BusConfig2.Field.BUS_NAME));
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
             authorizationRequest = daoFactory.getAuthorizationRequestDAO().retrieveAuthorizationRequest(authorizationRequestCookie);
 
             // check authZdecisionKey
             AuthorizationDecisionKey authZdecisionKeyEntry = daoFactory.getAuthorizationDecisionKeyDAO().retrieveAuthorizationRequest(authZdecisionKey);
             if (null == authZdecisionKeyEntry || ! authSessionCookie.equals(authZdecisionKeyEntry.get(AuthorizationDecisionKey.Field.AUTH_COOKIE))) {
                 throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_ACCESS_DENIED, "Presented authorization key was issued to a different authenticated bus owner.", authorizationRequest);
             }
 
             if (! "Authorize".equals(request.getParameter("authorize"))) {
                 throw new AuthorizationException(OAuth2.OAUTH2_AUTHZ_ACCESS_DENIED, "Bus owner denied authorization.", authorizationRequest);
             } else {
                 // todo: use (and check) scope posted back by bus owner
                 String scopeString = checkScope(authorizationRequest.get(AuthorizationRequest.Field.SCOPE), authenticatedBusOwner);
                 // create grant/code
                 Grant grant =  new Grant.Builder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE,
                             authenticatedBusOwner,
                             authorizationRequest.get(AuthorizationRequest.Field.CLIENT_ID),
                             scopeString).buildGrant();
                 daoFactory.getGrantDao().persist(grant);
                 
                 logger.info("Authorized " + authorizationRequest.get(AuthorizationRequest.Field.CLIENT_ID)+
                         "[" + authorizationRequest.get(AuthorizationRequest.Field.COOKIE)+"]" + "grant ID: " + grant.getIdValue());
 
                 // return OAuth2 authz response
                 final String code = grant.getIdValue();
                 final String state = authorizationRequest.get(AuthorizationRequest.Field.STATE);
 
                 try {
                     return new ModelAndView("redirect:" + UrlResponseFormat.QUERY.encode(
                             authorizationRequest.getRedirectUri(daoFactory.getClientDAO()),
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
                             authorizationRequest.getRedirectUri(daoFactory.getClientDAO()),
                             authorizationRequest.get(AuthorizationRequest.Field.STATE));
                 }
             }
         } catch (SimpleDBException e) {
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
                                 put(AuthorizationRequest.Field.STATE.getFieldName(), state);
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
                 throw new AuthException("Client credentials MUST NOT be included in the request URI (OAuth2 2.3.1)");
             }
         }
         if (StringUtils.isNotEmpty(client_id) || StringUtils.isNotEmpty(client_secret)) {
             throw new AuthException("Client credentials in request body are NOT RECOMMENDED (OAuth2 2.3.1)");
         }
     }
 
     private BackplaneMessage parsePostedMessage(Map<String, Map<String, Object>> messagePostBody, Token token) throws SimpleDBException {
         List<BackplaneMessage> result = new ArrayList<BackplaneMessage>();
 
         Map<String,Object> msg = messagePostBody.get("message");
         if (msg == null) { // no message body?
             throw new InvalidRequestException("Missing message payload", HttpServletResponse.SC_BAD_REQUEST);
         }
 
         if (messagePostBody.keySet().size() != 1) { // other garbage in the payload
             throw new InvalidRequestException("Invalid data in payload", HttpServletResponse.SC_BAD_REQUEST);
         }
 
         String channel = msg.get(BackplaneMessage.Field.CHANNEL.getFieldName()) != null ? msg.get(BackplaneMessage.Field.CHANNEL.getFieldName()).toString() : null;
         String bus = msg.get(BackplaneMessage.Field.BUS.getFieldName()) != null ? msg.get(BackplaneMessage.Field.BUS.getFieldName()).toString() : null;
         if ( ! daoFactory.getTokenDao().isValidBinding(channel, bus)) {
             throw new InvalidRequestException("Invalid bus - channel binding ", HttpServletResponse.SC_FORBIDDEN);
         }
 
         // check to see if channel is already full
         if (daoFactory.getBackplaneMessageDAO().isChannelFull(channel)) {
             throw new InvalidRequestException("Message limit of " + bpConfig.getDefaultMaxMessageLimit() + " has been reached for channel '" + channel + "'",
                     HttpServletResponse.SC_FORBIDDEN);
         }
 
         BackplaneMessage message;
         try {
             message = new BackplaneMessage(token.get(Token.TokenField.CLIENT_SOURCE_URL), msg);
         } catch (Exception e) {
             throw new InvalidRequestException("Invalid message data: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
         }
         if ( ! token.getScope().isMessageInScope(message) ) {
             throw new InvalidRequestException("Invalid bus in message", HttpServletResponse.SC_FORBIDDEN);
         }
         return  message;
     }
 
     private final TimerMetric v2Gets = Metrics.newTimer(Backplane2Controller.class, "v2_get", "v2_gets", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
     private final TimerMetric v2Posts =  Metrics.newTimer(Backplane2Controller.class, "v2_post", "v2_posts", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
     private final TimerMetric v2GetRegTokens =  Metrics.newTimer(Backplane2Controller.class, "v2_get_reg_token", "v2_get_reg_tokens", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 }
