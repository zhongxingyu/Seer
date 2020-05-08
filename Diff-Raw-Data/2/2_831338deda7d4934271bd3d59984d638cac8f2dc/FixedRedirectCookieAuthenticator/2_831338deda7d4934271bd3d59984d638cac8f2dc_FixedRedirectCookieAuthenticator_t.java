 /**
  * Copyright 2005-2012 Restlet S.A.S.
  * 
  * The contents of this file are subject to the terms of one of the following open source licenses:
  * Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the "Licenses"). You can select the
  * license that you prefer but you may not use this file except in compliance with one of these
  * Licenses.
  * 
  * You can obtain a copy of the Apache 2.0 license at http://www.opensource.org/licenses/apache-2.0
  * 
  * You can obtain a copy of the LGPL 3.0 license at http://www.opensource.org/licenses/lgpl-3.0
  * 
  * You can obtain a copy of the LGPL 2.1 license at http://www.opensource.org/licenses/lgpl-2.1
  * 
  * You can obtain a copy of the CDDL 1.0 license at http://www.opensource.org/licenses/cddl1
  * 
  * You can obtain a copy of the EPL 1.0 license at http://www.opensource.org/licenses/eclipse-1.0
  * 
  * See the Licenses for the specific language governing permissions and limitations under the
  * Licenses.
  * 
  * Alternatively, you can obtain a royalty free commercial license with less limitations,
  * transferable or non-transferable, directly at http://www.restlet.com/products/restlet-framework
  * 
  * Restlet is a registered trademark of Restlet S.A.S.
  */
 
 package com.github.ansell.restletutils;
 
 import java.security.GeneralSecurityException;
 
 import org.restlet.Context;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.data.ChallengeResponse;
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.Cookie;
 import org.restlet.data.CookieSetting;
 import org.restlet.data.Form;
 import org.restlet.data.Method;
 import org.restlet.data.Parameter;
 import org.restlet.data.Reference;
 import org.restlet.data.Status;
 import org.restlet.engine.util.Base64;
 import org.restlet.ext.crypto.internal.CryptoUtils;
 import org.restlet.routing.Filter;
 import org.restlet.security.ChallengeAuthenticator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Challenge authenticator based on browser cookies. This is useful when the web application
  * requires a finer grained control on the login and logout process and can't rely solely on
  * standard schemes such as {@link ChallengeScheme#HTTP_BASIC}.<br>
  * <br>
  * Login can be automatically handled by intercepting HTTP POST calls to the {@link #getLoginPath()}
  * URI. The request entity should contain an HTML form with two fields, the first one named
  * {@link #getIdentifierFormName()} and the second one named {@link #getSecretFormName()}.<br>
  * <br>
  * Logout can be automatically handled as well by intercepting HTTP GET or POST calls to the
  * {@link #getLogoutPath()} URI.<br>
  * <br>
  * After login or logout, the user's browser can be redirected to the URI provided in a query
  * parameter named by {@link #getRedirectQueryName()}.<br>
  * <br>
  * When the credentials are missing or stale, the {@link #challenge(Response, boolean)} method is
  * invoked by the parent class, and its default behavior is to redirect the user's browser to the
  * {@link #getLoginFormPath()} URI, adding the URI of the target resource as a query parameter of
  * name {@link #getRedirectQueryName()}.<br>
  * <br>
  * Note that credentials, both identifier and secret, are stored in a cookie in an encrypted manner.
  * The default encryption algorithm is AES but can be changed with
  * {@link #setEncryptAlgorithm(String)}. It is also strongly recommended to
  * 
  * @author Remi Dewitte
  * @author Jerome Louvel
  */
 public class FixedRedirectCookieAuthenticator extends ChallengeAuthenticator
 {
     
     public static final String DEFAULT_FIXED_REDIRECT_URI = "/";
     
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /**
      * A fixed URI to redirect to on login and logout
      */
     private volatile String fixedRedirectUri = FixedRedirectCookieAuthenticator.DEFAULT_FIXED_REDIRECT_URI;
     
     /** The name of the cookie that stores log info. */
     private volatile String cookieName;
     
     /** The name of the algorithm used to encrypt the log info cookie value. */
     private volatile String encryptAlgorithm;
     
     /**
      * The secret key for the algorithm used to encrypt the log info cookie value.
      */
     private volatile byte[] encryptSecretKey;
     
     /** The name of the HTML login form field containing the identifier. */
     private volatile String identifierFormName;
     
     /** Indicates if the login requests should be intercepted. */
     private volatile boolean interceptingLogin;
     
     /** Indicates if the logout requests should be intercepted. */
     private volatile boolean interceptingLogout;
     
     /** The URI path of the HTML login form to use to challenge the user. */
     private volatile String loginFormPath;
     
     /** The login URI path to intercept. */
     private volatile String loginPath;
     
     /** The logout URI path to intercept. */
     private volatile String logoutPath;
     
     /** The maximum age of the log info cookie. */
     private volatile int maxCookieAge;
     
     /**
      * The name of the query parameter containing the URI to redirect the browser to after login or
      * logout.
      */
     private volatile String redirectQueryName;
     
     /** The name of the HTML login form field containing the secret. */
     private volatile String secretFormName;
     
     /**
      * Constructor. Use the {@link ChallengeScheme#HTTP_COOKIE} pseudo-scheme.
      * 
      * @param context
      *            The parent context.
      * @param optional
      *            Indicates if this authenticator is optional so alternative authenticators down the
      *            chain can be attempted.
      * @param realm
      *            The name of the security realm.
      * @param encryptSecretKey
      *            The secret key used to encrypt the cookie value.
      */
     public FixedRedirectCookieAuthenticator(final Context context, final boolean optional, final String realm,
             final byte[] encryptSecretKey)
     {
         super(context, optional, ChallengeScheme.HTTP_COOKIE, realm);
         this.cookieName = "Credentials";
         this.interceptingLogin = true;
         this.interceptingLogout = true;
         this.identifierFormName = "login";
         this.loginPath = "/login";
         this.logoutPath = "/logout";
         this.secretFormName = "password";
         this.encryptAlgorithm = "AES";
         this.encryptSecretKey = encryptSecretKey;
         this.maxCookieAge = -1;
         this.redirectQueryName = "targetUri";
     }
     
     /**
      * Constructor for mandatory cookie authenticators.
      * 
      * @param context
      *            The parent context.
      * @param realm
      *            The name of the security realm.
      * @param encryptSecretKey
      *            The secret key used to encrypt the cookie value.
      */
     public FixedRedirectCookieAuthenticator(final Context context, final String realm, final byte[] encryptSecretKey)
     {
         this(context, false, realm, encryptSecretKey);
     }
     
     /**
      * Attempts to redirect the user's browser can be redirected to the URI provided in a query
      * parameter named by {@link #getRedirectQueryName()}.
      * 
      * Uses a configured fixed redirect URI or a default redirect URI if they are not setup.
      * 
      * @param request
      *            The current request.
      * @param response
      *            The current response.
      */
     protected void attemptRedirect(final Request request, final Response response)
     {
         final String targetUri = request.getResourceRef().getQueryAsForm().getFirstValue(this.getRedirectQueryName());
         
         if(targetUri != null)
         {
             response.redirectSeeOther(Reference.decode(targetUri));
         }
         
        if(this.getFixedRedirectUri() != null)
         {
             this.log.info("attemptRedirect: fixedRedirectUri={}", this.getFixedRedirectUri());
             response.redirectSeeOther(this.getFixedRedirectUri());
         }
         else
         {
             this.log.info("attemptRedirect: fixedRedirectUri={}",
                     FixedRedirectCookieAuthenticator.DEFAULT_FIXED_REDIRECT_URI);
             response.redirectSeeOther(FixedRedirectCookieAuthenticator.DEFAULT_FIXED_REDIRECT_URI);
         }
     }
     
     /**
      * Restores credentials from the cookie named {@link #getCookieName()} if available. The usual
      * processing is the followed.
      */
     @Override
     protected boolean authenticate(final Request request, final Response response)
     {
         // Restore credentials from the cookie
         final Cookie credentialsCookie = request.getCookies().getFirst(this.getCookieName());
         
         if(credentialsCookie != null)
         {
             request.setChallengeResponse(this.parseCredentials(credentialsCookie.getValue()));
         }
         
         this.log.info("Calling super.authenticate");
         return super.authenticate(request, response);
     }
     
     /**
      * Sets or update the credentials cookie.
      */
     @Override
     protected int authenticated(final Request request, final Response response)
     {
         try
         {
             final CookieSetting credentialsCookie = this.getCredentialsCookie(request, response);
             credentialsCookie.setValue(this.formatCredentials(request.getChallengeResponse()));
             credentialsCookie.setMaxAge(this.getMaxCookieAge());
         }
         catch(final GeneralSecurityException e)
         {
             this.log.error("Could not format credentials cookie", e);
         }
         
         // log.info("calling super.authenticated");
         // return super.authenticated(request, response);
         
         // Modified copy of Authenticator.authenticated
         if(this.log.isInfoEnabled())
         {
             this.log.info("The authentication succeeded for the identifer \"{}\" using the {} scheme.", request
                     .getChallengeResponse().getIdentifier(), request.getChallengeResponse().getScheme());
         }
         
         // Update the client info accordingly
         if(request.getClientInfo() != null)
         {
             request.getClientInfo().setAuthenticated(true);
         }
         
         // Clear previous challenge requests
         response.getChallengeRequests().clear();
         
         // Add the roles for the authenticated subject
         if(this.getEnroler() != null)
         {
             this.getEnroler().enrole(request.getClientInfo());
         }
         
         // Return STOP here works for login but not for subsequent requests that expect a response
         // return STOP;
         return Filter.CONTINUE;
     }
     
     /**
      * Optionally handles the login and logout actions by intercepting the HTTP calls to the
      * {@link #getLoginPath()} and {@link #getLogoutPath()} URIs.
      */
     @Override
     protected int beforeHandle(final Request request, final Response response)
     {
         this.log.info("Calling isLoggingIn");
         if(this.isLoggingIn(request, response))
         {
             this.log.info("Calling isLoggingIn");
             this.login(request, response);
             if(this.authenticate(request, response))
             {
                 this.log.info("Calling authenticated");
                 this.authenticated(request, response);
             }
             // we redirect after logging in for all cases, so stop here and return the 303 response
             return Filter.STOP;
         }
         else if(this.isLoggingOut(request, response))
         {
             this.log.info("Calling logout");
             return this.logout(request, response);
         }
         
         // log.info("calling super.beforeHandle");
         // return super.beforeHandle(request, response);
         
         this.log.info("Calling copy of Authenticator beforeHandle");
         
         if(
         // FIXME: How important is multiAuthenticating to this class?
         // isMultiAuthenticating() ||
         !request.getClientInfo().isAuthenticated())
         {
             this.log.info("Calling authenticate");
             if(this.authenticate(request, response))
             {
                 this.log.info("Calling authenticated");
                 return this.authenticated(request, response);
             }
             else if(this.isOptional())
             {
                 this.log.info("Authentication isOptional, returning CONTINUE with Status.SUCCESS_OK");
                 response.setStatus(Status.SUCCESS_OK);
                 return Filter.CONTINUE;
             }
             else
             {
                 this.log.info("Calling unauthenticated");
                 return this.unauthenticated(request, response);
             }
         }
         else
         {
             this.log.info("Returning CONTINUE");
             return Filter.CONTINUE;
         }
     }
     
     /**
      * This method should be overridden to return a login form representation.
      */
     @Override
     public void challenge(final Response response, final boolean stale)
     {
         this.log.info("Calling super.challenge");
         super.challenge(response, stale);
     }
     
     /**
      * Formats the raws credentials to store in the cookie.
      * 
      * @param challenge
      *            The challenge response to format.
      * @return The raw credentials.
      * @throws GeneralSecurityException
      */
     protected String formatCredentials(final ChallengeResponse challenge) throws GeneralSecurityException
     {
         // Data buffer
         final StringBuffer sb = new StringBuffer();
         
         // Indexes buffer
         final StringBuffer isb = new StringBuffer();
         final String timeIssued = Long.toString(System.currentTimeMillis());
         int i = timeIssued.length();
         sb.append(timeIssued);
         
         isb.append(i);
         
         final String identifier = challenge.getIdentifier();
         sb.append('/');
         sb.append(identifier);
         
         i += identifier.length() + 1;
         isb.append(',').append(i);
         
         sb.append('/');
         sb.append(challenge.getSecret());
         
         // Store indexes at the end of the string
         sb.append('/');
         sb.append(isb);
         
         return Base64.encode(
                 CryptoUtils.encrypt(this.getEncryptAlgorithm(), this.getEncryptSecretKey(), sb.toString()), false);
     }
     
     /**
      * Returns the cookie name to use for the authentication credentials. By default, it is is
      * "Credentials".
      * 
      * @return The cookie name to use for the authentication credentials.
      */
     public String getCookieName()
     {
         return this.cookieName;
     }
     
     /**
      * Returns the credentials cookie setting. It first try to find an existing cookie. If
      * necessary, it creates a new one.
      * 
      * @param request
      *            The current request.
      * @param response
      *            The current response.
      * @return The credentials cookie setting.
      */
     protected CookieSetting getCredentialsCookie(final Request request, final Response response)
     {
         CookieSetting credentialsCookie = response.getCookieSettings().getFirst(this.getCookieName());
         
         if(credentialsCookie == null)
         {
             credentialsCookie = new CookieSetting(this.getCookieName(), null);
             credentialsCookie.setAccessRestricted(true);
             // authCookie.setVersion(1);
             
             if(request.getRootRef() != null)
             {
                 final String p = request.getRootRef().getPath();
                 credentialsCookie.setPath(p == null ? "/" : p);
             }
             else
             {
                 // authCookie.setPath("/");
             }
             
             response.getCookieSettings().add(credentialsCookie);
         }
         
         return credentialsCookie;
     }
     
     /**
      * Returns the name of the algorithm used to encrypt the log info cookie value. By default, it
      * returns "AES".
      * 
      * @return The name of the algorithm used to encrypt the log info cookie value.
      */
     public String getEncryptAlgorithm()
     {
         return this.encryptAlgorithm;
     }
     
     /**
      * Returns the secret key for the algorithm used to encrypt the log info cookie value.
      * 
      * @return The secret key for the algorithm used to encrypt the log info cookie value.
      */
     public byte[] getEncryptSecretKey()
     {
         return this.encryptSecretKey;
     }
     
     /**
      * @return the fixedRedirectUri
      */
     public String getFixedRedirectUri()
     {
         return this.fixedRedirectUri;
     }
     
     /**
      * Returns the name of the HTML login form field containing the identifier. Returns "login" by
      * default.
      * 
      * @return The name of the HTML login form field containing the identifier.
      */
     public String getIdentifierFormName()
     {
         return this.identifierFormName;
     }
     
     /**
      * Returns the URI path of the HTML login form to use to challenge the user.
      * 
      * @return The URI path of the HTML login form to use to challenge the user.
      */
     public String getLoginFormPath()
     {
         return this.loginFormPath;
     }
     
     /**
      * Returns the login URI path to intercept.
      * 
      * @return The login URI path to intercept.
      */
     public String getLoginPath()
     {
         return this.loginPath;
     }
     
     /**
      * Returns the logout URI path to intercept.
      * 
      * @return The logout URI path to intercept.
      */
     public String getLogoutPath()
     {
         return this.logoutPath;
     }
     
     /**
      * Returns the maximum age of the log info cookie. By default, it uses -1 to make the cookie
      * only last until the end of the current browser session.
      * 
      * @return The maximum age of the log info cookie.
      * @see CookieSetting#getMaxAge()
      */
     public int getMaxCookieAge()
     {
         return this.maxCookieAge;
     }
     
     /**
      * Returns the name of the query parameter containing the URI to redirect the browser to after
      * login or logout. By default, it uses "targetUri".
      * 
      * @return The name of the query parameter containing the URI to redirect the browser to after
      *         login or logout.
      */
     public String getRedirectQueryName()
     {
         return this.redirectQueryName;
     }
     
     /**
      * Returns the name of the HTML login form field containing the secret. Returns "password" by
      * default.
      * 
      * @return The name of the HTML login form field containing the secret.
      */
     public String getSecretFormName()
     {
         return this.secretFormName;
     }
     
     /**
      * Indicates if the login requests should be intercepted.
      * 
      * @return True if the login requests should be intercepted.
      */
     public boolean isInterceptingLogin()
     {
         return this.interceptingLogin;
     }
     
     /**
      * Indicates if the logout requests should be intercepted.
      * 
      * @return True if the logout requests should be intercepted.
      */
     public boolean isInterceptingLogout()
     {
         return this.interceptingLogout;
     }
     
     /**
      * Indicates if the request is an attempt to log in and should be intercepted.
      * 
      * @param request
      *            The current request.
      * @param response
      *            The current response.
      * @return True if the request is an attempt to log in and should be intercepted.
      */
     protected boolean isLoggingIn(final Request request, final Response response)
     {
         return this.isInterceptingLogin()
                 && this.getLoginPath().equals(request.getResourceRef().getRemainingPart(false, false))
                 && Method.POST.equals(request.getMethod());
     }
     
     /**
      * Indicates if the request is an attempt to log out and should be intercepted.
      * 
      * @param request
      *            The current request.
      * @param response
      *            The current response.
      * @return True if the request is an attempt to log out and should be intercepted.
      */
     protected boolean isLoggingOut(final Request request, final Response response)
     {
         return this.isInterceptingLogout()
                 && this.getLogoutPath().equals(request.getResourceRef().getRemainingPart(false, false))
                 && (Method.GET.equals(request.getMethod()) || Method.POST.equals(request.getMethod()));
     }
     
     /**
      * Processes the login request.
      * 
      * @param request
      *            The current request.
      * @param response
      *            The current response.
      */
     protected void login(final Request request, final Response response)
     {
         // Login detected
         final Form form = new Form(request.getEntity());
         final Parameter identifier = form.getFirst(this.getIdentifierFormName());
         final Parameter secret = form.getFirst(this.getSecretFormName());
         
         // Set credentials
         final ChallengeResponse cr =
                 new ChallengeResponse(this.getScheme(), identifier != null ? identifier.getValue() : null,
                         secret != null ? secret.getValue() : null);
         request.setChallengeResponse(cr);
         
         this.log.info("calling attemptRedirect after login");
         // Attempt to redirect
         this.attemptRedirect(request, response);
     }
     
     /**
      * Processes the logout request.
      * 
      * @param request
      *            The current request.
      * @param response
      *            The current response.
      */
     protected int logout(final Request request, final Response response)
     {
         // Clears the credentials
         request.setChallengeResponse(null);
         final CookieSetting credentialsCookie = this.getCredentialsCookie(request, response);
         credentialsCookie.setMaxAge(0);
         
         this.log.info("calling attemptRedirect after logout");
         // Attempt to redirect
         this.attemptRedirect(request, response);
         
         return Filter.STOP;
     }
     
     /**
      * Decodes the credentials stored in a cookie into a proper {@link ChallengeResponse} object.
      * 
      * @param cookieValue
      *            The credentials to decode from cookie value.
      * @return The credentials as a proper challenge response.
      */
     protected ChallengeResponse parseCredentials(final String cookieValue)
     {
         // 1) Decode Base64 string
         final byte[] encrypted = Base64.decode(cookieValue);
         
         if(encrypted == null)
         {
             this.log.error("Cannot decode cookie credentials : {}", cookieValue);
         }
         
         // 2) Decrypt the credentials
         try
         {
             final String decrypted =
                     CryptoUtils.decrypt(this.getEncryptAlgorithm(), this.getEncryptSecretKey(), encrypted);
             
             // 3) Parse the decrypted cookie value
             final int lastSlash = decrypted.lastIndexOf('/');
             final String[] indexes = decrypted.substring(lastSlash + 1).split(",");
             final int identifierIndex = Integer.parseInt(indexes[0]);
             final int secretIndex = Integer.parseInt(indexes[1]);
             
             // 4) Create the challenge response
             final ChallengeResponse cr = new ChallengeResponse(this.getScheme());
             cr.setRawValue(cookieValue);
             cr.setTimeIssued(Long.parseLong(decrypted.substring(0, identifierIndex)));
             cr.setIdentifier(decrypted.substring(identifierIndex + 1, secretIndex));
             cr.setSecret(decrypted.substring(secretIndex + 1, lastSlash));
             return cr;
         }
         catch(final Exception e)
         {
             this.log.info("Unable to decrypt cookie credentials", e);
             return null;
         }
     }
     
     /**
      * Sets the cookie name to use for the authentication credentials.
      * 
      * @param cookieName
      *            The cookie name to use for the authentication credentials.
      */
     public void setCookieName(final String cookieName)
     {
         this.cookieName = cookieName;
     }
     
     /**
      * Sets the name of the algorithm used to encrypt the log info cookie value.
      * 
      * @param secretAlgorithm
      *            The name of the algorithm used to encrypt the log info cookie value.
      */
     public void setEncryptAlgorithm(final String secretAlgorithm)
     {
         this.encryptAlgorithm = secretAlgorithm;
     }
     
     /**
      * Sets the secret key for the algorithm used to encrypt the log info cookie value.
      * 
      * @param secretKey
      *            The secret key for the algorithm used to encrypt the log info cookie value.
      */
     public void setEncryptSecretKey(final byte[] secretKey)
     {
         this.encryptSecretKey = secretKey;
     }
     
     /**
      * @param fixedRedirectUri
      *            the fixedRedirectUri to set
      */
     public void setFixedRedirectUri(final String fixedRedirectUri)
     {
         this.fixedRedirectUri = fixedRedirectUri;
     }
     
     /**
      * Sets the name of the HTML login form field containing the identifier.
      * 
      * @param loginInputName
      *            The name of the HTML login form field containing the identifier.
      */
     public void setIdentifierFormName(final String loginInputName)
     {
         this.identifierFormName = loginInputName;
     }
     
     /**
      * Indicates if the login requests should be intercepted.
      * 
      * @param intercepting
      *            True if the login requests should be intercepted.
      */
     public void setInterceptingLogin(final boolean intercepting)
     {
         this.interceptingLogin = intercepting;
     }
     
     /**
      * Indicates if the logout requests should be intercepted.
      * 
      * @param intercepting
      *            True if the logout requests should be intercepted.
      */
     public void setInterceptingLogout(final boolean intercepting)
     {
         this.interceptingLogout = intercepting;
     }
     
     /**
      * Sets the URI path of the HTML login form to use to challenge the user.
      * 
      * @param loginFormPath
      *            The URI path of the HTML login form to use to challenge the user.
      */
     public void setLoginFormPath(final String loginFormPath)
     {
         this.loginFormPath = loginFormPath;
     }
     
     /**
      * Sets the login URI path to intercept.
      * 
      * @param loginPath
      *            The login URI path to intercept.
      */
     public void setLoginPath(final String loginPath)
     {
         this.loginPath = loginPath;
     }
     
     /**
      * Sets the logout URI path to intercept.
      * 
      * @param logoutPath
      *            The logout URI path to intercept.
      */
     public void setLogoutPath(final String logoutPath)
     {
         this.logoutPath = logoutPath;
     }
     
     /**
      * Sets the maximum age of the log info cookie.
      * 
      * @param timeout
      *            The maximum age of the log info cookie.
      * @see CookieSetting#setMaxAge(int)
      */
     public void setMaxCookieAge(final int timeout)
     {
         this.maxCookieAge = timeout;
     }
     
     /**
      * Sets the name of the query parameter containing the URI to redirect the browser to after
      * login or logout.
      * 
      * @param redirectQueryName
      *            The name of the query parameter containing the URI to redirect the browser to
      *            after login or logout.
      */
     public void setRedirectQueryName(final String redirectQueryName)
     {
         this.redirectQueryName = redirectQueryName;
     }
     
     /**
      * Sets the name of the HTML login form field containing the secret.
      * 
      * @param passwordInputName
      *            The name of the HTML login form field containing the secret.
      */
     public void setSecretFormName(final String passwordInputName)
     {
         this.secretFormName = passwordInputName;
     }
     
 }
