 package com.bpodgursky.hubris.account;
 
 import com.bpodgursky.hubris.common.HubrisConstants;
 import com.gistlabs.mechanize.MechanizeAgent;
 import com.gistlabs.mechanize.cookie.Cookie;
 import com.gistlabs.mechanize.document.Document;
 import com.gistlabs.mechanize.document.html.form.Form;
 import com.gistlabs.mechanize.document.html.query.HtmlQueryBuilder;
 import com.gistlabs.mechanize.document.link.Link;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.common.net.HttpHeaders;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolException;
 import org.apache.http.client.RedirectHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HttpContext;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.Set;
 
 public class LoginClient {
   public enum LoginResponseType {
     SUCCESS, INVALID_LOGIN, INVALID_TWO_FACTOR_AUTH_TOKEN;
   }
 
   public static class LoginResponse {
     private final LoginResponseType responseType;
     private final String cookies;
 
     public LoginResponse(LoginResponseType responseType, String cookies) {
       this.responseType = responseType;
       this.cookies = cookies;
     }
 
     public LoginResponseType getResponseType() {
       return responseType;
     }
 
     public String getCookies() {
       return cookies;
     }
   }
 
   protected static class LoginConstants {
     // First login page stuff
     public final static String LOGIN_URL_SUBSTR = "google.com/accounts/ServiceLogin";
     public final static String LOGIN_FORM_ID = "gaia_loginform";
     public final static String USERNAME_FIELD_NAME = "Email";
     public final static String PASSWORD_FIELD_NAME = "Passwd";
 
     // Two-factor auth stuff
     public static final String TWO_FACTOR_AUTH_FORM = "verify-form";
     public static final String TWO_FACTOR_AUTH_TOKEN_NAME = "smsUserPin";
     public static final String TWO_FACTOR_AUTH_HIDDEN_POST_FORM_NAME = "hiddenpost";
 
     // Confirm access stuff
     public final static String CONFIRM_ACCESS_TITLE = "Google Accounts";
     public final static String CONFIRM_ACCESS_FORM_ACTION = "https://appengine.google.com/_ah/conflogin";
     public final static String CONFIRM_ACCESS_PERSIST_CHECKBOX_NAME = "persist";
     public final static String CONFIRM_ACCESS_SUBMIT_BUTTON = "submit_true";
 
     // Cookies and response handling
     public final static String COOKIE_DOMAIN = "np.ironhelmet.com";
     public final static Set<Integer> REDIRECT_STATUS_CODES = Sets.newHashSet(302, 303);
     public final static RedirectHandler REDIRECT_HANDLER = new RedirectHandler() {
       @Override
       public boolean isRedirectRequested(HttpResponse httpResponse, HttpContext httpContext) {
         return LoginConstants.REDIRECT_STATUS_CODES.contains(httpResponse.getStatusLine().getStatusCode());
       }
 
       @Override
       public URI getLocationURI(HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
         try {
           return new URI(httpResponse.getFirstHeader(HttpHeaders.LOCATION).getValue());
         }
         catch (URISyntaxException e) {
           throw new RuntimeException(e);
         }
       }
     };
   }
 
   public LoginResponse login(String username, String password) {
     return login(username, password, new ExceptionThrowingTwoFactorCallback("Two-factor authentication needed, but no callback provided."));
   }
 
   public LoginResponse login(String username, String password, TwoFactorAuthCallback twoFactorAuthCallback) {
     final MechanizeAgent agent = buildAgent();
 
     // Request homepage and find "login" link.
     Document homepage = agent.get(HubrisConstants.homepageUrl);
     Link loginLink = null;
     for (Link link : homepage.links()) {
       if (link.href().contains(LoginConstants.LOGIN_URL_SUBSTR)) {
         loginLink = link;
         break;
       }
     }
     if (loginLink == null) {
       throw new RuntimeException("Unable to find login link on NP homepage");
     }
 
     // Find and fill out form
     Document loginPage = loginLink.click();
     Form loginForm = loginPage.form(LoginConstants.LOGIN_FORM_ID);
     if (loginForm == null) {
       throw new RuntimeException("Couldn't find login form on login page");
     }
 
     loginForm.getEmail(HtmlQueryBuilder.byName(LoginConstants.USERNAME_FIELD_NAME)).set(username);
     loginForm.getPassword(HtmlQueryBuilder.byName(LoginConstants.PASSWORD_FIELD_NAME)).set(password);
     Document postLoginPage = loginForm.submit();
 
     return handlePostLoginResponse(agent, postLoginPage, twoFactorAuthCallback);
   }
 
   protected LoginResponse handlePostLoginResponse(MechanizeAgent agent, Document postLoginPage, TwoFactorAuthCallback twoFactorAuthCallback) {
     // A few possibilities at this point:
     //  1. Login failed -- indicated by the login form being present in the response
     //  2. Login succeeded, but authorization needed. Indicated by a different form being present in the response.
     //  3. Login succeeded, no authorization needed (already authorized). Probably doesn't happen unless the agent
     //     is initialized with some cookies.
     //  4. Weird Google Accounts state -- "enter phone number for verification", etc. Could be two-factor auth,
     //     all of which could theoretically be handled if need be.
     if (postLoginPage.form(LoginConstants.LOGIN_FORM_ID) != null) {
       return new LoginResponse(LoginResponseType.INVALID_LOGIN, null);
     }
     else if (postLoginPage.form(LoginConstants.TWO_FACTOR_AUTH_FORM) != null) {
       return handleTwoFactorAuth(agent, postLoginPage, twoFactorAuthCallback);
     }
     else if (postLoginPage.getUri().contains(HubrisConstants.homepageUrl)) {
       return handleLoginPassed(agent);
     }
     else if (postLoginPage.getTitle().contains(LoginConstants.CONFIRM_ACCESS_TITLE)) {
       return handleConfirmAccess(agent, postLoginPage);
     }
     else {
       throw new RuntimeException("Unknown state");
     }
   }
 
   protected LoginResponse handleTwoFactorAuth(MechanizeAgent agent, Document postLoginPage, TwoFactorAuthCallback twoFactorAuthCallback) {
     String authToken = twoFactorAuthCallback.requestAuthToken();
     Form authForm = postLoginPage.form(LoginConstants.TWO_FACTOR_AUTH_FORM);
     authForm.get(LoginConstants.TWO_FACTOR_AUTH_TOKEN_NAME).set(authToken);
     Document postTwoFactorAuthResponse = authForm.submit();
 
     // There's a hidden form that automatically submits via JavaScript. Submit it.
     Form hiddenPostForm = postTwoFactorAuthResponse.form(LoginConstants.TWO_FACTOR_AUTH_HIDDEN_POST_FORM_NAME);
     if (hiddenPostForm == null) {
       return new LoginResponse(LoginResponseType.INVALID_TWO_FACTOR_AUTH_TOKEN, null);
     }
     Document postHiddenPostResponse = hiddenPostForm.submit();
     return handlePostLoginResponse(agent,
       postHiddenPostResponse,
       new ExceptionThrowingTwoFactorCallback("Two-factor authentication already passed, but detected Google asking for it again."));
   }
 
   protected LoginResponse handleConfirmAccess(MechanizeAgent agent, Document confirmAccessPage) {
     Form confirmAccessForm = null;
     for (Form form : confirmAccessPage.forms()) {
       if (form.getUri().equals(LoginConstants.CONFIRM_ACCESS_FORM_ACTION)) {
         confirmAccessForm = form;
         break;
       }
     }
     if (confirmAccessForm == null) {
       throw new RuntimeException("Couldn't find confirm access form in confirm access page");
     }
     confirmAccessForm.getCheckbox(HtmlQueryBuilder.byName(LoginConstants.CONFIRM_ACCESS_PERSIST_CHECKBOX_NAME)).check();
     Document confirmAccessResponse = confirmAccessForm
       .getSubmitButton(HtmlQueryBuilder.byName(LoginConstants.CONFIRM_ACCESS_SUBMIT_BUTTON))
       .submit();
 
    String landingUrl = confirmAccessResponse.getResponse().toString();
     if (!landingUrl.contains(HubrisConstants.homepageUrl)) {
       throw new RuntimeException("Didn't get redirected to homepage after access confirmation. Instead, got: " + landingUrl);
     }
 
     return handleLoginPassed(agent);
   }
 
   protected LoginResponse handleLoginPassed(MechanizeAgent agent) {
     List<String> mergedCookies = Lists.newArrayList();
     for (Cookie cookie : agent.cookies().getAll()) {
       if (cookie.getDomain().equals(LoginConstants.COOKIE_DOMAIN)) {
         mergedCookies.add(cookie.getName().concat("=").concat(cookie.getValue()));
       }
     }
 
     return new LoginResponse(LoginResponseType.SUCCESS, Joiner.on("; ").join(mergedCookies));
   }
 
   protected static MechanizeAgent buildAgent() {
     DefaultHttpClient client = new DefaultHttpClient();
     client.setRedirectHandler(LoginConstants.REDIRECT_HANDLER);
     return new MechanizeAgent(client)
       .setUserAgent(HubrisConstants.userAgent);
   }
 }
