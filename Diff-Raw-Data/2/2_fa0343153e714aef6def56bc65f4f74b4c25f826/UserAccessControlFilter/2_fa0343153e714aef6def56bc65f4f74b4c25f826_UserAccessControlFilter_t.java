 package org.yogocodes.bikewars.web.filter;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yogocodes.bikewars.util.UserSessionUtil;
 
 /**
  * org.yogocodes.bikewars.web.filter.UserAccessControlFilter
  * 
  * @author joukojo
  * 
  */
 public class UserAccessControlFilter implements Filter {
 
 	private final Logger log = LoggerFactory.getLogger(UserAccessControlFilter.class);
 
 	@Override
 	public void destroy() {
 		log.trace("destroy");
 	}
 
 	@Override
 	public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
 
 		final HttpServletRequest request = (HttpServletRequest) req;
 		final HttpServletResponse response = (HttpServletResponse) resp;
 		final HttpSession httpSession = request.getSession();
 		final Long userId = UserSessionUtil.getUserId(httpSession);
 
 		if (userId == null) {
 			log.debug("the user is not logged in");
			final String url = request.getContextPath() + "/user-login.htm";
 			response.sendRedirect(url);
 		} else {
 			log.debug("the user is logged in");
 			chain.doFilter(request, response);
 		}
 	}
 
 	@Override
 	public void init(final FilterConfig arg0) throws ServletException {
 		log.trace("init");
 
 	}
 
 }
