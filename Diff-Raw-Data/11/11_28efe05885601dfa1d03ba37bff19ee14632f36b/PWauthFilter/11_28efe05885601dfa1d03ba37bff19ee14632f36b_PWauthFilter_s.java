 /**
  * 
  */
 package hudson.plugins.pwauth;
 
 import hudson.model.Hudson;
 import hudson.security.AuthorizationStrategy;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 class PWauthFilter implements Filter {
 	private static final String[] usernameParams = new String[] {"user", "username", "login"};
 	private static final String[] passwordParams = new String[] {"pass", "password", "passwd", "key"};
 	private final Filter superFilter;
 	private final PWauthSecurityRealm pwauth;
 	
 	public PWauthFilter(final Filter superFilter, final PWauthSecurityRealm pwauth) {
 		this.superFilter = superFilter;
 		this.pwauth = pwauth;
 	}
 
 	public void init(final FilterConfig filterConfig) throws ServletException {}
 
 	@Override
 	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
 		this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
 	}
 
 	public void doFilter(final HttpServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
 		if (this.pwauth.enableParamAuth && this.validateParams(request)) {
 			this.grantAccess(request, response, chain);
 			return;
 		}
 		for (String whitelistedId : this.getWhitelist())
 			if (whitelistedId.equals(request.getRemoteAddr())) {
 				grantAccess(request, response, chain);
 				return;
 			}
         this.superFilter.doFilter(request, response, chain);
 	}
 	
 	private boolean validateParams(HttpServletRequest request) {
 		try {
 			final String username = this.getParameter(usernameParams, request);
 			final String password = this.getParameter(passwordParams, request);
 			if (Hudson.getInstance().getSecurityRealm() instanceof PWauthSecurityRealm)
 				return ((PWauthSecurityRealm) Hudson.getInstance().getSecurityRealm()).authenticate(username, password) != null;
 		} catch (Exception e) {
 			// TODO Log exception to Hudson logs (after i figured out how)
 			return false;
 		}
 		return false;
 	}
 	
 	private String getParameter(String[] keyCandidates, HttpServletRequest request) {
 		for (String key : keyCandidates)
 			if (request.getParameter(key) != null)
 				return request.getParameter(key);
 		return null;
 	}
 
	private void grantAccess(final HttpServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException,
		ServletException {
 		AuthorizationStrategy strategy = Hudson.getInstance().getAuthorizationStrategy();
 		Hudson.getInstance().setAuthorizationStrategy(AuthorizationStrategy.UNSECURED);
		this.superFilter.doFilter(request, response, chain);
 		Hudson.getInstance().setAuthorizationStrategy(strategy);
 	}
 	
 	private List<String> getWhitelist() {
 		List<String> whitelist = new Vector<String>();
 		if (this.pwauth.whitelist != null)
 			for (String ip : this.pwauth.whitelist.split(PWauthValidation.listSperatorEx)) 
 	    		if (PWauthValidation.validateIP(ip))
 	    			whitelist.add(ip.trim());
 		return whitelist;
 	}
 	
 	public void destroy() {}
 }
