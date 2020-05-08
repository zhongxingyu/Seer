 package com.qburst.openidlogin;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.expressme.openid.OpenIdManager;
 
 public class LoginFilter implements Filter {
 	public static final String OPENID_INDENTITY = "openIdIdentity";
 	public static final String LOGOUT_URL = "logoutURL";
 	Properties props = new Properties();
 	String logoutURL;
 	boolean logoutSessionOnly;
 	boolean isOpen;
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		InputStream input = null;
 		try {
			input = getClass().getClassLoader().getResourceAsStream("openid-urls.properties");
 			props.load(input);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (input != null) {
 				try {
 					input.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 
 		OpenIdManager manager = new OpenIdManager();
 		manager.setRealm(props.getProperty("realm"));
 		manager.setReturnTo(props.getProperty("returnToURL"));
 		logoutURL = props.getProperty("OpenIdLogoutURL");
 		isOpen = "none".equals(props.getProperty("security"));
 		logoutSessionOnly = "session".equals(props.getProperty("logoutType"));
 
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest httpRequest = (HttpServletRequest) request;
 		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (httpRequest.getRequestURI().matches(".*(css|jpg|png|gif|js)")) {
			chain.doFilter(request, response);
			return;
 		}

 		if (!isOpen) {
 			String action = request.getParameter("action");
 			if (action != null && "logout".equalsIgnoreCase(action)) {
 				HttpSession session = httpRequest.getSession(false);
 				session.removeAttribute(OPENID_INDENTITY);
 				session.invalidate();
 
 				if (logoutSessionOnly) {
 					// Redirect to the login page
 					request.getRequestDispatcher("/WEB-INF/view/login.jsp")
 							.forward(httpRequest, httpResponse);
 				} else {
 					// Redirect to the logout URL
 					httpResponse.sendRedirect(logoutURL);
 				}
 				return;
 			}
 
 			else if (!(httpRequest.getRequestURL().toString().equals(props
 					.getProperty("loginURL")))) {
 				HttpSession session = httpRequest.getSession(false);
 				if (session == null
 						|| session.getAttribute(OPENID_INDENTITY) == null) {
 					httpResponse.sendRedirect(props.getProperty("loginURL")
 							+ "?op=" + props.getProperty("openIdProvider"));
 					return;
 				}
 
 			}
		} else {
			request.setAttribute("noAuthentication", "Y");
 		}
 		chain.doFilter(request, response);
 
 	}
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
