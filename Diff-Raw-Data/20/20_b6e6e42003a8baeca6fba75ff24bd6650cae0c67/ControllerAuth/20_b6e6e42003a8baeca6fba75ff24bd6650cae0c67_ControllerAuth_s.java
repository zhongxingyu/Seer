 package elw.webauth;
 
 import com.google.common.base.Strings;
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.io.CharStreams;
 import elw.web.core.W;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.openid4java.association.AssociationException;
 import org.openid4java.consumer.ConsumerException;
 import org.openid4java.consumer.ConsumerManager;
 import org.openid4java.consumer.VerificationResult;
 import org.openid4java.discovery.DiscoveryException;
 import org.openid4java.discovery.DiscoveryInformation;
 import org.openid4java.discovery.Identifier;
 import org.openid4java.discovery.yadis.YadisResolver;
 import org.openid4java.message.AuthRequest;
 import org.openid4java.message.AuthSuccess;
 import org.openid4java.message.MessageException;
 import org.openid4java.message.ParameterList;
 import org.openid4java.message.ax.AxMessage;
 import org.openid4java.message.ax.FetchRequest;
 import org.openid4java.message.ax.FetchResponse;
 import org.openid4java.server.RealmVerifier;
 import org.openid4java.server.RealmVerifierFactory;
 import org.openid4java.util.HttpFetcherFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpMethod;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.mail.*;
 import javax.mail.Message;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.security.*;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 public abstract class ControllerAuth {
     private static final Logger log = LoggerFactory.getLogger(ControllerAuth.class);
 
     public static final String SESSION_SUCCESS_REDIRECT = "SUCCESS_REDIRECT";
 
     protected static final String SESSION_OID_DISCOVERY = "OID_DISCOVERY";
     protected static final String SESSION_OID_CONSUMER = "OID_CONSUMER";
 
     protected static final String OID_ENDPOINT_YAHOO =
             "https://me.yahoo.com";
     protected static final String OID_ENDPOINT_GOOGLE =
             "https://www.google.com/accounts/o8/id";
     protected static final String OID_ENDPOINT_GOOGLE_PROFILES =
             "http://www.google.com/profiles/";
     protected static final String OID_ENDPOINT_YANDEX =
             "http://openid.yandex.ru";
     protected static final String OID_ENDPOINT_MAILRU =
             "http://openid.mail.ru/mail/";
 
     protected static final String OID_REDIR_BODY_TARGETURL = "${targetUrl}";
     protected static final String OID_REDIR_BODY_PARAMS = "${paramsGoHere}";
     protected static final String OID_REDIR_PARAMKEY = "${parameter.key}";
     protected static final String OID_REDIR_PARAMVAL = "${parameter.value}";
 
     /**
      * Not all endpoints are reporting confirmed emails,
      * so we have to white-list them.
      */
     protected static final List<String> OID_ENDPOINT_TRUSTED =
             Collections.unmodifiableList(
                     Arrays.asList(
                             OID_ENDPOINT_YAHOO,
                             OID_ENDPOINT_GOOGLE,
                             OID_ENDPOINT_GOOGLE_PROFILES,
                             OID_ENDPOINT_YANDEX,
                             OID_ENDPOINT_MAILRU
                     )
             );
 
     protected static RealmVerifierFactory realmVerifierFactory =
             //  what the heck, I did not see anything like that in the docs?..
             new RealmVerifierFactory(
                     new YadisResolver(
                             new HttpFetcherFactory()
                     )
             );
 
     //  the most simple way to get base64 of binary hash (no additional libs)
     private final ObjectMapper mapper = new ObjectMapper();
 
     private final ServerConfigAuth serverConfig;
     private final Cache<String, Long> srcAddrToStamp;
 
     public ControllerAuth(ServerConfigAuth serverConfig) {
         this.serverConfig = serverConfig;
 
         Security.addProvider(new BouncyCastleProvider());
 
         final CacheBuilder<Object, Object> caches =
             CacheBuilder.newBuilder()
                 .concurrencyLevel(3)
                 .expireAfterWrite(
                     2 * serverConfig.getMailSourceDelayMillis(),
                     TimeUnit.MILLISECONDS
                 );
 
         this.srcAddrToStamp = caches.build();
     }
 
     public static void storeSuccessRedirect(
             HttpServletRequest req
     ) {
 
         final String reqUri = req.getRequestURI();
 
         if (!Strings.isNullOrEmpty(req.getQueryString())) {
             req.getSession(true).setAttribute(
                     SESSION_SUCCESS_REDIRECT,
                     reqUri + "?" + req.getQueryString()
             );
         } else {
             req.getSession(true).setAttribute(
                     SESSION_SUCCESS_REDIRECT,
                     reqUri
             );
         }
     }
 
     @RequestMapping(
            value = "smtpchallenge",
            method = RequestMethod.GET
     )
     public ModelAndView do_smtpchallengeGet(
            final HttpServletRequest req,
            final HttpServletResponse resp
     ) throws IOException {
         final String emailParam = req.getParameter("email");
         if (emailParam == null || emailParam.trim().length() == 0) {
             resp.sendError(
                     HttpServletResponse.SC_BAD_REQUEST,
                     "email parameter not set"
             );
             return null;
         }
 
         final String remoteAddress = W.resolveRemoteAddress(req);
         final long forcedDelayMillis =
             serverConfig.getMailSourceDelayMillis();
         final Long lastStamp = srcAddrToStamp.getIfPresent(remoteAddress);
         final long currentMillis = System.currentTimeMillis();
         if (lastStamp != null &&
             lastStamp + forcedDelayMillis > currentMillis) {
             resp.sendError(
                 HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                 "your IP is requesting smtp auth too often"
             );
             return null;
         }
         srcAddrToStamp.put(remoteAddress, currentMillis);
 
         final String email = emailParam.trim();
         final String smtpChallengeToken;
         try {
             smtpChallengeToken = registerChallengeToken(req, email);
         } catch (AuthException e) {
             log.warn("failed on smtpchallenge", e);
             resp.sendError(
                     HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     e.getMessage()
             );
             return null;
         }
 
         final Session session = setupSmtpSession();
         try {
 
             Message message = createMessage(
                 session, req, email, smtpChallengeToken
             );
 
             final Transport transport =
                     session.getTransport(serverConfig.getMailProtocol());
 
             transport.connect(
                     serverConfig.getSmtpHost(),
                     serverConfig.getSmtpPort(),
                     serverConfig.getSmtpUser(),
                     serverConfig.getSmtpPass()
             );
 
             transport.sendMessage(
                     message,
                     message.getRecipients(Message.RecipientType.TO)
             );
         } catch (MessagingException e) {
             log.warn("failed on smtpchallenge", e);
             resp.sendError(
                     HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     e.getMessage()
             );
             return null;
         }
 
         final StringBuilder responseBuilder =
             generateSmtpResponseBody(email);
 
         sendBody(resp, responseBuilder);
 
         return null;
     }
 
     protected StringBuilder generateSmtpResponseBody(final String email) {
         final StringBuilder responseBuilder =
             new StringBuilder(serverConfig.getMailResponseForm());
 
         replace(
             responseBuilder,
             "${targetUrl}",
             serverConfig.getBaseUrl() + "auth/smtpresponse"
         );
 
         replace(
             responseBuilder,
             "${email}",
             email
         );
         return responseBuilder;
     }
 
     protected Message createMessage(
         final Session session, final HttpServletRequest req,
         final String emailAddress,
         final String smtpChallengeToken
     ) throws MessagingException, UnsupportedEncodingException {
         final String mailBody =
             generateMailBody(req, emailAddress, smtpChallengeToken);
 
         final Message message = new MimeMessage(session);
 
         message.setSentDate(new Date());
         message.setFrom(new InternetAddress(serverConfig.getSmtpFrom()));
         message.setRecipients(
                 Message.RecipientType.TO,
                 InternetAddress.parse(emailAddress)
         );
         message.setSubject(serverConfig.getSmtpSubject());
         message.setText( mailBody);
 
         return message;
     }
 
     protected String generateMailBody(
         final HttpServletRequest req,
         final String emailAddress,
         final String smtpChallengeToken
     ) throws UnsupportedEncodingException {
         final StringBuilder mailBodyBuilder =
             new StringBuilder(serverConfig.getMailBody());
 
         replace(
             mailBodyBuilder,
             "${smtpAuth.sourceAddr}",
             W.resolveRemoteAddress(req)
         );
 
         replace(
             mailBodyBuilder,
             "${smtpAuth.token}",
             smtpChallengeToken
         );
 
         replace(
             mailBodyBuilder,
             "${smtpAuth.responseUrl}",
             serverConfig.getBaseUrl() +
                 "auth/smtpresponse?" +
                 "email=" + URLEncoder.encode(emailAddress, "UTF-8") + "&" +
                 "token=" + URLEncoder.encode(smtpChallengeToken, "UTF-8")
         );
 
         return mailBodyBuilder.toString();
     }
 
     protected abstract String registerChallengeToken(
         final HttpServletRequest req, String emailAddress
     ) throws AuthException;
 
     protected String challengeToken(
         final String email,
         final Long stamp,
         final String sessionId
     ) throws AuthException {
 
         final String superSecretSalt = serverConfig.getMailTokenSalt();
 
         try {
             final MessageDigest mda = MessageDigest.getInstance(
                 "SHA-1",
                 "BC"
             );
 
             final String challengeData =
                 email + " " +
                     stamp + " " +
                     sessionId + " " +
                     superSecretSalt;
 
             final byte[] digest =
                 mda.digest(challengeData.getBytes());
             
             //  drop some bytes to avoid padding char
             final byte[] digestPadded =
                 new byte[digest.length - digest.length % 3];
             
             System.arraycopy(digest, 0, digestPadded, 0, digestPadded.length);
 
             final String base64Quoted =
                 mapper.writeValueAsString(digestPadded);
 
             final String base64 =
                 base64Quoted.substring(1, base64Quoted.length() - 1);
 
             //  switch to URL-safe variant
             return base64.replace("+", "-").replace("/", "_");
         } catch (NoSuchAlgorithmException e) {
             throw new AuthException("BouncyCastle/SHA-1'd been abducted", e);
         } catch (java.security.NoSuchProviderException e) {
             throw new AuthException("BouncyCastle took off and flew away", e);
         } catch (JsonMappingException e) {
             throw new AuthException("Jackson doesn't", e);
         } catch (JsonGenerationException e) {
             throw new AuthException("Jackson didn't", e);
         } catch (IOException e) {
             throw new AuthException("Jackson would rather not", e);
         }
     }
 
     protected Session setupSmtpSession() {
         final Properties props = new Properties();
 
         props.put(
                 "mail.smtp.auth",
                 serverConfig.getSmtpAuth()
         );
         props.put(
                 "mail.smtp.starttls.enable",
                 String.valueOf(serverConfig.getSmtpStartTls())
         );
         props.put(
                 "mail.smtp.host",
                 serverConfig.getSmtpHost()
         );
         props.put(
                 "mail.smtp.port",
                 serverConfig.getSmtpPort()
         );
         props.put(
                 "mail.smtp.socketFactory.port",
                 serverConfig.getSmtpPort()
         );
         props.put(
                 "mail.smtp.socketFactory.class",
                 serverConfig.getSmtpSocketFactory()
         );
         props.put(
                 "mail.smtp.socketFactory.fallback",
                 "false"
         );
 
         final Session session = Session.getInstance(
             props,
             new Authenticator() {
                 protected PasswordAuthentication getPasswordAuthentication() {
                     return new PasswordAuthentication(
                             serverConfig.getSmtpUser(),
                             serverConfig.getSmtpPass()
                     );
                 }
             }
         );
 
         session.setDebug(serverConfig.isSmtpDebug());
 
         return session;
     }
 
     @RequestMapping(
         value = "smtpresponse",
         method = RequestMethod.GET
     )
     public ModelAndView do_smtpresponseGet(
         final HttpServletRequest req,
         final HttpServletResponse resp
     ) throws IOException {
         return do_smtpresponsePost(req, resp);
     }
 
     @RequestMapping(
         value = "smtpresponse",
         method = RequestMethod.POST
     )
     public ModelAndView do_smtpresponsePost(
         final HttpServletRequest req,
         final HttpServletResponse resp
     ) throws IOException {
         final String emailParam = req.getParameter("email");
         if (emailParam == null || emailParam.trim().length() == 0) {
             resp.sendError(
                 HttpServletResponse.SC_BAD_REQUEST,
                 "email parameter not set"
             );
             return null;
         }
 
         final String email = emailParam.trim();
 
         final String tokenParam = req.getParameter("token");
         if (tokenParam == null || tokenParam.trim().length() == 0) {
             resp.sendError(
                 HttpServletResponse.SC_BAD_REQUEST,
                 "tokenParam parameter not set"
             );
             return null;
         }
 
         final String token = tokenParam.trim();
 
         try {
             if (!activateAuth(req, email, token)) {
                 resp.sendError(
                     HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     "failed to activate token"
                 );
                 return null;
             }
         } catch (AuthException e) {
             resp.sendError(
                 HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                 "failed to activate token: " + e.getMessage()
             );
             return null;
         }
 
         processAuth(
             req,
             resp,
             req.getSession(true),
             Collections.singletonList(email),
             Collections.<String>emptyList()
         );
 
         return null;
     }
 
     protected abstract boolean activateAuth(
         final HttpServletRequest req,
         final String email,
         final String token
     ) throws AuthException;
 
     @RequestMapping(
             value = "oidchallenge",
             method = RequestMethod.GET
     )
     public ModelAndView do_oidchallengeGet(
             final HttpServletRequest req,
             final HttpServletResponse resp
     ) throws IOException {
         final String oidIdent = req.getParameter("openid_identifier");
         if (oidIdent == null || oidIdent.trim().length() == 0) {
             resp.sendError(
                     HttpServletResponse.SC_BAD_REQUEST,
                     "openid_identifier not set"
             );
             return null;
         }
 
         final OpenIdChallengeResult challengeResult;
         try {
             challengeResult =
                     oidchallenge(
                             req,
                             oidIdent,
                             serverConfig.getBaseUrl() + "auth/oidresponse"
                     );
         } catch (AuthException e) {
             log.warn("failed on oidchallenge", e);
             resp.sendError(
                     HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     e.getMessage()
             );
             return null;
         }
 
         if (challengeResult.method == HttpMethod.GET) {
             resp.sendRedirect(challengeResult.targetUrl);
             return null;
         }
 
         final StringBuilder responseBody =
             generateFormRedirectBody(challengeResult);
 
         sendBody(resp, responseBody);
 
         return null;
     }
 
     protected void sendBody(
         final HttpServletResponse resp,
         final StringBuilder responseBody
     ) throws IOException {
         resp.setContentType("text/html; charset=UTF-8");
         resp.setCharacterEncoding("UTF-8");
 
         resp.setHeader("Pragma", "no-cache");
         resp.setHeader("Cache-Control", "no-cache");
         resp.setDateHeader("Expires", System.currentTimeMillis());
 
         CharStreams.copy(
             new StringReader(responseBody.toString()),
             resp.getWriter()
         );
     }
 
     protected StringBuilder generateFormRedirectBody(
             OpenIdChallengeResult challengeResult
     ) {
         final StringBuilder params = new StringBuilder();
         for (Object key : challengeResult.parameterMap.keySet()) {
             params.append(serverConfig.getFormRedirectParam());
             replace(
                     params,
                     OID_REDIR_PARAMKEY,
                     String.valueOf(key)
             );
             replace(
                     params,
                     OID_REDIR_PARAMVAL,
                     String.valueOf(challengeResult.parameterMap.get(key))
             );
         }
 
         final StringBuilder responseBody =
             new StringBuilder(serverConfig.getFormRedirect());
 
         replace(
                 responseBody,
                 OID_REDIR_BODY_TARGETURL,
                 challengeResult.targetUrl
         );
         replace(
                 responseBody,
                 OID_REDIR_BODY_PARAMS,
                 params.toString()
         );
         return responseBody;
     }
 
     protected static StringBuilder replace(
             final StringBuilder content,
             final String search,
             final String replace
     ) {
         int replaces = 16;
         int targetIndex;
         while (replaces-- > 0 && (targetIndex = content.indexOf(search)) >= 0) {
             content.replace(
                     targetIndex,
                     targetIndex + search.length(),
                     replace
             );
         }
         
         return content;
     }
 
     @RequestMapping(
             value = "oidresponse",
             method = RequestMethod.GET
     )
     public ModelAndView do_oidresponseGet(
             final HttpServletRequest req,
             final HttpServletResponse resp
     ) throws IOException {
         return do_oidresponsePost(req, resp);
     }
 
     @RequestMapping(
             value = "oidresponse",
             method = RequestMethod.POST
     )
     public ModelAndView do_oidresponsePost(
             final HttpServletRequest req,
             final HttpServletResponse resp
     ) throws IOException {
         try {
             final HttpSession session = req.getSession(true);
 
             final OpenIdResponseResult openIdResponseResult =
                     oidresponse(req, session);
 
             final List<String> emails = openIdResponseResult.emails;
             final List<String> openIds = openIdResponseResult.openIds;
 
             return processAuth(req, resp, session, emails, openIds);
         } catch (AuthException e) {
             log.warn("failed on oidresponse", e);
             resp.sendError(
                     HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     e.getMessage()
             );
             return null;
         }
     }
 
     protected ModelAndView processAuth(
         final HttpServletRequest req,
         final HttpServletResponse resp,
         final HttpSession session,
         final List<String> emails,
         final List<String> openIds
     ) throws IOException {
         processAuthInfo(req, session, emails, openIds);
 
         return processAuthSuccess(req, resp);
     }
 
     protected ModelAndView processAuthSuccess(
             HttpServletRequest req,
             HttpServletResponse resp
     ) throws IOException {
         final HttpSession session = req.getSession(true);
         final Object successRedirSession =
                 session.getAttribute(SESSION_SUCCESS_REDIRECT);
         final String successRedir;
         if (successRedirSession == null) {
             successRedir = serverConfig.getBaseUrl();
         } else {
             successRedir = String.valueOf(successRedirSession);
         }
 
         resp.sendRedirect(successRedir);
         session.removeAttribute(SESSION_SUCCESS_REDIRECT);
         return null;
     }
 
     protected abstract void processAuthInfo(
         HttpServletRequest req,
         HttpSession session,
         List<String> emails,
         List<String> openIds
     );
 
     public OpenIdChallengeResult oidchallenge(
             final HttpServletRequest request,
             final String oidIdent,
             final String responseUrl
     ) throws AuthException {
         //  adding some extra code for exception handling
         try {
 
             return oidChallenge_unsafe(request, oidIdent, responseUrl);
 
         } catch (DiscoveryException e) {
             throw new AuthException("OpenID auth failed", e);
         } catch (MessageException e) {
             throw new AuthException("OpenID auth failed", e);
         } catch (ConsumerException e) {
             throw new AuthException("OpenID auth failed", e);
         }
     }
 
     protected OpenIdChallengeResult oidChallenge_unsafe(
             final HttpServletRequest request,
             final String oidIdent,
             final String responseUrl
     ) throws DiscoveryException, MessageException, ConsumerException {
         final HttpSession session = request.getSession(true);
 
         //  coding this up this just like the QuickStart sample says, see:
         //      http://code.google.com/p/openid4java/wiki/QuickStart
 
         // perform discovery on the user-supplied identifier
         final ConsumerManager oidConsumer = oidConsumer(session, true);
         final List discoveries = oidConsumer.discover(oidIdent);
 
         // attempt to associate with the OpenID provider
         // and retrieve one service endpoint for authentication
         final DiscoveryInformation discovered =
                 oidConsumer.associate(discoveries);
 
         // store the discovery information in the user's session for later use
         // leave out for stateless operation / if there is no session
         session.setAttribute(SESSION_OID_DISCOVERY, discovered);
 
         // obtain a AuthRequest message to be sent to the OpenID provider
         final AuthRequest authReq =
                 oidConsumer.authenticate(
                         discovered,
                         responseUrl
                 );
 
         FetchRequest fetch = FetchRequest.createFetchRequest();
         if (OID_ENDPOINT_GOOGLE.equals(oidIdent)) {
             fetch.addAttribute(
                     "email",
                     "http://axschema.org/contact/email",
                     true
             );
         } else if (OID_ENDPOINT_YAHOO.equals(oidIdent)) {
             fetch.addAttribute(
                     "email",
                     "http://axschema.org/contact/email",
                     true
             );
         } else {
             //works for myOpenID
             fetch.addAttribute(
                     "email",
                     "http://schema.openid.net/contact/email",
                     true
             );
         }
 
         if (!fetch.getAttributes().isEmpty()) {
             authReq.addExtension(fetch);
         }
 
         if (discovered.isVersion2()) {
             return new OpenIdChallengeResult(
                     HttpMethod.POST,
                     authReq.getDestinationUrl(false),
                     authReq.getParameterMap()
             );
         }
 
         return new OpenIdChallengeResult(
                 HttpMethod.GET,
                 authReq.getDestinationUrl(true),
                 Collections.EMPTY_MAP
         );
     }
 
     public OpenIdResponseResult oidresponse(
             final HttpServletRequest request,
             final HttpSession session
     ) throws AuthException {
         final ConsumerManager oidConsumer = oidConsumer(session, false);
         if (oidConsumer == null) {
             throw new AuthException(
                     "no ConsumerManager in session", new IllegalStateException()
             );
         }
 
         try {
 
             return oidresponse_unsafe(request, session, oidConsumer);
 
 
         } catch (MessageException e) {
             throw new AuthException("OpenID auth failed", e);
         } catch (DiscoveryException e) {
             throw new AuthException("OpenID auth failed", e);
         } catch (AssociationException e) {
             throw new AuthException("OpenID auth failed", e);
         } finally {
             session.removeAttribute(SESSION_OID_DISCOVERY);
             session.removeAttribute(SESSION_OID_CONSUMER);
         }
     }
 
     protected OpenIdResponseResult oidresponse_unsafe(
             final HttpServletRequest request,
             final HttpSession session,
             final ConsumerManager oidConsumer
     ) throws MessageException, DiscoveryException,
             AssociationException, AuthException {
 
         // extract the parameters from the authentication response
         // (which comes in as a HTTP request from the OpenID provider)
         final ParameterList openidResp =
                 new ParameterList(request.getParameterMap());
 
         // retrieve the previously stored discovery information
         final DiscoveryInformation discovered =
                 (DiscoveryInformation) session.getAttribute(SESSION_OID_DISCOVERY);
 
         // extract the receiving URL from the HTTP request
         final StringBuffer receivingURL = request.getRequestURL();
         final String queryString = request.getQueryString();
         if (queryString != null && queryString.length() > 0)
             receivingURL.append("?").append(request.getQueryString());
 
 
         // verify the response
         final VerificationResult verification = oidConsumer.verify(
                 receivingURL.toString(),
                 openidResp,
                 discovered
         );
 
 
         // examine the verification result and extract the verified identifier
         final Identifier verified = verification.getVerifiedId();
         if (verified == null) {
             throw new AuthException(
                     "OpenID authentication failed", new IllegalStateException()
             );
         }
 
         final List<String> emails = new ArrayList<String>();
 
         final AuthSuccess authSuccess =
                 (AuthSuccess) verification.getAuthResponse();
 
         if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
             final FetchResponse fetchResp =
                     (FetchResponse) authSuccess.getExtension(
                             AxMessage.OPENID_NS_AX
                     );
 
             List emailsRaw = fetchResp.getAttributeValues("email");
             for (Object emailRaw : emailsRaw) {
                 emails.add(String.valueOf(emailRaw));
             }
         }
         
         boolean trustedEmails = false;
         for (String trustedEndpoint : OID_ENDPOINT_TRUSTED) {
             if (authSuccess.getOpEndpoint().startsWith(trustedEndpoint)) {
                 trustedEmails = true;
             }
         }
 
         // success
         final List<String> emptyEmails = Collections.emptyList();
         final List<String> emailsEffective =
                 trustedEmails ? emails : emptyEmails;
 
         return new OpenIdResponseResult(
                 emailsEffective,
                 Collections.singletonList(verified.getIdentifier())
         );
     }
 
     protected ConsumerManager oidConsumer(
             final HttpSession session,
             final boolean create
     ) {
         final Object existing = session.getAttribute(SESSION_OID_CONSUMER);
 
         if (existing != null || !create) {
             return (ConsumerManager) existing;
         }
 
         final ConsumerManager created = new ConsumerManager();
 
         if (created.getRealmVerifier() == null) {
             RealmVerifier rv = realmVerifierFactory.getRealmVerifierForConsumer();
             rv.setEnforceRpId(serverConfig.isRelyingPartyIdent());
             created.setRealmVerifier(rv);
         } else {
             created.getRealmVerifier().setEnforceRpId(
                     serverConfig.isRelyingPartyIdent()
             );
         }
 
         session.setAttribute(SESSION_OID_CONSUMER, created);
 
         return created;
     }
 }
