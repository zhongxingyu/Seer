 package edu.gatech.oad.rocket.findmythings.server.security;
 
 import edu.gatech.oad.rocket.findmythings.server.db.DatabaseService;
 import edu.gatech.oad.rocket.findmythings.server.model.MessageBean;
 import edu.gatech.oad.rocket.findmythings.server.util.HTTP;
 import edu.gatech.oad.rocket.findmythings.server.util.Messages;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.codec.Base64;
 import org.apache.shiro.subject.Subject;
 import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
 import org.apache.shiro.web.util.WebUtils;
 
import com.google.appengine.labs.repackaged.org.json.JSONObject;

 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 public final class BearerTokenAuthenticatingFilter extends AuthenticatingFilter {
 
 	private static final String AUTHORIZATION_HEADER = "X-Authorization";
 	private static final String AUTHORIZATION_PARAM = "fmthings_auth";
 	private static final String AUTHORIZATION_SCHEME = "FMTTOKEN";
 	private static final String AUTHORIZATION_SCHEME_ALT = "Basic";
 
 	@SuppressWarnings("unused")
 	private static final Logger LOGGER = Logger.getLogger(BearerTokenAuthenticatingFilter.class.getName());
 
 	private String usernameParam;
 	private String passwordParam;
 
 	public void setUsernameParam(String usernameParam) {
 		this.usernameParam = usernameParam;
 	}
 
 	public void setPasswordParam(String passwordParam) {
 		this.passwordParam = passwordParam;
 	}
 
 	String getUsernameParam() {
 		return usernameParam;
 	}
 
 	String getPasswordParam() {
 		return passwordParam;
 	}
 
 	@Override
 	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
 		if (isLoginRequest(request, response)) {
			String JSON = request.getReader().readLine();
			JSONObject contents = new JSONObject(JSON);
			String username = (String) contents.get(getUsernameParam());
			String password = (String) contents.get(getPasswordParam());
 			return createToken(username, password, request, response);
 		} else {
 			String authorizeHeader = getAuthorizationHeader(request);
 			String authorizeParameter = getAuthorizationParameter(request);
 			String[] principlesAndCredentials;
 
 			if (isHeaderLoginAttempt(authorizeHeader)) {
 				principlesAndCredentials = this.getHeaderPrincipalsAndCredentials(authorizeHeader);
 			} else if (isParameterLoginAttempt(authorizeParameter)) {
 				principlesAndCredentials = this.getParameterPrincipalsAndCredentials(authorizeParameter);
 			} else {
 				return null;
 			}
 
 			if (principlesAndCredentials == null || principlesAndCredentials.length != 2) {
 				return null;
 			}
 
 			String username = principlesAndCredentials[0];
 			String token = principlesAndCredentials[1];
 
 			return new BearerToken(username, token);
 		}
 	}
 
 	@Override
 	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
 		boolean authHasToken = hasAuthorizationToken(request);
 		boolean isLogin = isLoginRequest(request, response);
 		if (authHasToken || isLogin) {
 			return executeLogin(request, response);
 		} else {
 			HTTP.writeError(response, HTTP.Status.UNAUTHORIZED);
 			return false;
 		}
 	}
 
 	@Override
 	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
 		if (isLoginRequest(request, response)) {
 			String email = (String)subject.getPrincipal();
 			String newToken = DatabaseService.ofy().createAuthenticationToken(email);
 			HTTP.writeAsJSON(response,
 					MessageBean.STATUS, HTTP.Status.OK.toInt(),
 					MessageBean.MESSAGE, Messages.Status.OK.toString(),
 					MessageBean.TOKEN, newToken,
 					MessageBean.EMAIL, email);
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
 		if (isLoginRequest(request, response)) {
 			HTTP.writeAsJSON(response,
 					MessageBean.STATUS, HTTP.Status.UNAUTHORIZED.toInt(),
 					MessageBean.MESSAGE, Messages.Status.UNAUTHORIZED.toString(),
 					MessageBean.FAILURE_REASON, Messages.Login.getMessage(e));
 		} else {
 			HTTP.writeError(response, HTTP.Status.UNAUTHORIZED);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
 		return isLoginRequest(request, response) && hasAuthorizationToken(request) || super.onPreHandle(request, response, mappedValue);
 	}
 
 	boolean hasAuthorizationToken(ServletRequest request) {
 		String authorizeHeader = getAuthorizationHeader(request);
 		String authorizeParam = getAuthorizationParameter(request);
 		return isHeaderLoginAttempt(authorizeHeader) || isParameterLoginAttempt(authorizeParam);
 	}
 
 	String getAuthorizationHeader(ServletRequest request) {
 		HttpServletRequest httpRequest = WebUtils.toHttp(request);
 		return httpRequest.getHeader(AUTHORIZATION_HEADER);
 	}
 
 	String getAuthorizationParameter(ServletRequest request) {
 		HttpServletRequest httpRequest = WebUtils.toHttp(request);
 		return WebUtils.getCleanParam(httpRequest, AUTHORIZATION_PARAM);
 	}
 
 	boolean isHeaderLoginAttempt(String authorizeHeader) {
 		if (authorizeHeader == null) return false;
 		String authorizeScheme = AUTHORIZATION_SCHEME.toLowerCase(Locale.ENGLISH);
 		String authorizeSchemeAlt = AUTHORIZATION_SCHEME_ALT.toLowerCase(Locale.ENGLISH);
 		String test = authorizeHeader.toLowerCase(Locale.ENGLISH);
 		return test.startsWith(authorizeScheme) || test.startsWith(authorizeSchemeAlt);
 	}
 
 	boolean isParameterLoginAttempt(String authorizeParam) {
 		return (authorizeParam != null) && Base64.isBase64(authorizeParam.getBytes());
 	}
 
 	String[] getHeaderPrincipalsAndCredentials(String authorizeHeader) {
 		if (authorizeHeader == null) {
 			return null;
 		}
 		String[] authTokens = authorizeHeader.split(" ");
 		if (authTokens == null || authTokens.length < 2) {
 			return null;
 		}
 		return getPrincipalsAndCredentials(authTokens[1]);
 	}
 
 	String[] getParameterPrincipalsAndCredentials(String authorizeParam) {
 		if (authorizeParam == null) {
 			return null;
 		}
 		return getPrincipalsAndCredentials(authorizeParam);
 	}
 
 	String[] getPrincipalsAndCredentials(String encoded) {
 		String decoded = Base64.decodeToString(encoded);
 		return decoded.split(":", 2);
 	}
 
 	String getUsername(ServletRequest request) {
 		return WebUtils.getCleanParam(request, getUsernameParam());
 	}
 
 	String getPassword(ServletRequest request) {
 		return WebUtils.getCleanParam(request, getPasswordParam());
 	}
 
 	@Override
 	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
 		return !(!isLoginRequest(request, response) && isPermissive(mappedValue) && hasAuthorizationToken(request)) && (super.isAccessAllowed(request, response, mappedValue) || (!isLoginRequest(request, response) && isPermissive(mappedValue) && !hasAuthorizationToken(request)));
 	}
 
 }
