 package in.payb.api.mvc.filter;
 
 import in.payb.api.RealControlUserDetails;
 
 import java.beans.ConstructorProperties;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.AuthenticationDetailsSource;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.AuthenticationServiceException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
 import org.springframework.security.web.authentication.AuthenticationFailureHandler;
 import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
 import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
 import org.springframework.security.web.util.TextEscapeUtils;
 import org.springframework.stereotype.Component;
 
@Component
 public class PaybinAuthenticationFilter extends
 		AbstractAuthenticationProcessingFilter {
 
 	public static final String SPRING_SECURITY_FORM_COMPANY_KEY = "j_company";
 	public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
 	public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";
 
 	public static final String SPRING_SECURITY_LAST_COMPANY_KEY = "SPRING_SECURITY_LAST_COMPANY";
 	public static final String SPRING_SECURITY_LAST_USERNAME_KEY = "SPRING_SECURITY_LAST_USERNAME";
 
 	private boolean postOnly = true;
 
 	private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
 	@Autowired
 	private AuthenticationManager authenticationManager;
 
 	// We'll use this to populate some environment details into the
 	// authentication - ip, sessionid etc.
 	private AuthenticationDetailsSource<HttpServletRequest, ?> ads = new WebAuthenticationDetailsSource();
 
 	// This is needed so that the value can be passed in from the bean
 	// definition.
 	@ConstructorProperties(value = { "filterProcessesUrl" })
 	public PaybinAuthenticationFilter(String filterProcessesUrl) {
 		super(filterProcessesUrl);
 	}
 
 	public Authentication attemptAuthentication(HttpServletRequest request,
 			HttpServletResponse response) throws AuthenticationException,
 			IOException, ServletException {
 
 		if (postOnly && !request.getMethod().equals("POST")) {
 			throw new AuthenticationServiceException(
 					"Authentication method not supported: "
 							+ request.getMethod());
 		}
 
 		Authentication authentication = null;
 
 		String company = obtainCompany(request);
 		String username = obtainUsername(request);
 		String password = obtainPassword(request);
 
 		if (company == null) {
 			company = "";
 		}
 
 		if (username == null) {
 			username = "";
 		}
 
 		if (password == null) {
 			password = "";
 		}
 
 		company = company.trim();
 		username = username.trim();
 
 		// initial implementation - hard code the credentials - should get them
 		// from request.
 		RealControlUserDetails rcud = new RealControlUserDetails(company,
 				username, password);
 		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
 				rcud, password);
 
 		// Place the last username attempted into HttpSession for views
 		HttpSession session = request.getSession(false);
 
 		if (session != null || getAllowSessionCreation()) {
 			request.getSession().setAttribute(SPRING_SECURITY_LAST_COMPANY_KEY,
 					TextEscapeUtils.escapeEntities(company));
 			request.getSession().setAttribute(
 					SPRING_SECURITY_LAST_USERNAME_KEY,
 					TextEscapeUtils.escapeEntities(username));
 		}
 
 		// Allow subclasses to set the "details" property
 		setDetails(request, authRequest);
 
 		try {
 			// try to authenticate these credentials using the
 			// authenticationManager. This is a basic
 			// manager that will just iterate through a list of
 			// authenticationproviders as specified
 			// in security-config.xml.
 			// IMPORTANT - set erase-credentials to false on the manager in the
 			// security-config.xml
 			// or else the password will be removed by the call to authenticate.
 			authentication = authenticationManager.authenticate(authRequest);
 
 			// Set the authentication details in the ThreadLocal context so we
 			// can reuse the from now on.
 			SecurityContextHolder.getContext()
 					.setAuthentication(authentication);
 
 		} catch (AuthenticationException ae) {
 			failureHandler.onAuthenticationFailure(
 					(HttpServletRequest) request,
 					(HttpServletResponse) response, ae);
 			return null;
 		}
 
 		return authentication;
 
 	}
 
 	/**
 	 * Enables subclasses to override the composition of the password, such as
 	 * by including additional values and a separator.
 	 * <p>
 	 * This might be used for example if a postcode/zipcode was required in
 	 * addition to the password. A delimiter such as a pipe (|) should be used
 	 * to separate the password and extended value(s). The
 	 * <code>AuthenticationDao</code> will need to generate the expected
 	 * password in a corresponding manner.
 	 * </p>
 	 * 
 	 * @param request
 	 *            so that request attributes can be retrieved
 	 * 
 	 * @return the password that will be presented in the
 	 *         <code>Authentication</code> request token to the
 	 *         <code>AuthenticationManager</code>
 	 */
 	protected String obtainPassword(HttpServletRequest request) {
 		return request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY);
 	}
 
 	/**
 	 * Enables subclasses to override the composition of the company, such as by
 	 * including additional values and a separator.
 	 * 
 	 * @param request
 	 *            so that request attributes can be retrieved
 	 * 
 	 * @return the company that will be presented in the
 	 *         <code>Authentication</code> request token to the
 	 *         <code>AuthenticationManager</code>
 	 */
 	protected String obtainCompany(HttpServletRequest request) {
 		return request.getParameter(SPRING_SECURITY_FORM_COMPANY_KEY);
 	}
 
 	/**
 	 * Enables subclasses to override the composition of the username, such as
 	 * by including additional values and a separator.
 	 * 
 	 * @param request
 	 *            so that request attributes can be retrieved
 	 * 
 	 * @return the username that will be presented in the
 	 *         <code>Authentication</code> request token to the
 	 *         <code>AuthenticationManager</code>
 	 */
 	protected String obtainUsername(HttpServletRequest request) {
 		return request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
 	}
 
 	/**
 	 * Provided so that subclasses may configure what is put into the
 	 * authentication request's details property.
 	 * 
 	 * @param request
 	 *            that an authentication request is being created for
 	 * @param authRequest
 	 *            the authentication request object that should have its details
 	 *            set
 	 */
 	protected void setDetails(HttpServletRequest request,
 			UsernamePasswordAuthenticationToken authRequest) {
 		authRequest.setDetails(ads.buildDetails(request));
 	}
 }
