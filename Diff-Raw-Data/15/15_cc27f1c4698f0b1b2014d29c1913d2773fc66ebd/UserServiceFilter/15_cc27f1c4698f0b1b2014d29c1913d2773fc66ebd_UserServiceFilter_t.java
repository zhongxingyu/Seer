 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package iddb.web.security.service;
 
 import iddb.web.security.SecurityConfig;
 import iddb.web.security.subject.AnonymousSubject;
 import iddb.web.security.subject.Subject;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ar.sgt.resolver.exception.ReverseException;
 import ar.sgt.resolver.exception.RuleNotFoundException;
 import ar.sgt.resolver.utils.UrlReverse;
 
 public class UserServiceFilter implements Filter {
 
 	private static final Logger log = LoggerFactory.getLogger(UserServiceFilter.class);
 	
 	private Map<String, Set<String>> urls;
 	private ServletContext context;
 	
 	/* (non-Javadoc)
 	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
 	 */
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		this.context = filterConfig.getServletContext();
 		Map<String, String> local = SecurityConfig.getInstance().getSection("urls");
 		if (local == null) {
 			urls = Collections.emptyMap();
 		} else {
 			urls = new LinkedHashMap<String, Set<String>>();
 			for (Entry<String, String> entry : local.entrySet()) {
 				Set<String> s = new LinkedHashSet<String>(Arrays.asList(entry.getValue().split(",")));
 				urls.put(entry.getKey(), s);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
 	 */
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		HttpSession session = ((HttpServletRequest) request).getSession(false);
 		CommonUserService service = (CommonUserService) UserServiceFactory.getUserService();
 		Subject s = null;
 		if (session != null) {
 			s = (Subject) session.getAttribute(UserService.SUBJECT);
 			if (s != null) {
 				service.saveLocal(s);
 			} else {
 				service.removeLocal();
 			}
 		} else {
 			service.removeLocal();
 		}
 		if (s == null) s = new AnonymousSubject();
 		if (haveAccess(s, (HttpServletRequest) request, (HttpServletResponse) response)) {
 			try {
 				chain.doFilter(request, response);
 			} finally {
 				service.removeLocal();
 			}
 		}
 	}
 
 	/**
 	 * @param s
 	 * @param request
 	 * @return
 	 * @throws IOException 
 	 */
 	private boolean haveAccess(Subject s, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String path = req.getRequestURI();
		log.debug("Check path {}", path);
		if (!req.getContextPath().isEmpty()) {
 			path = StringUtils.removeStartIgnoreCase(path, req.getContextPath());
 		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		} else if (path.startsWith("//")) {
			path = path.substring(1);
		}
 		for (String p : urls.keySet()) {
 			if (p.endsWith("**")) {
 				if (path.startsWith(p.substring(0, p.length()-2))) {
 					return checkRoles(s, req, resp, p);
 				}
 			} else {
 				if (path.equalsIgnoreCase(p)) {
 					return checkRoles(s, req, resp, p);
 				}
 			}
 		}
 		return true;
 	}
 
 	private boolean checkRoles(Subject s, HttpServletRequest req, HttpServletResponse resp, String p)
 			throws IOException {
 		if (s.isAuthenticated()) {
 			Set<String> r = urls.get(p);
 			for (String role : r) {
 				if ("*".equals(role) || s.hasRole(role)) {
					log.debug("Allowed");
 					return true;
 				}
				log.debug("Forbidden");
 				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
 			}
 		} else {
			log.debug("Redirect to Login");
 			UrlReverse reverse = new UrlReverse(this.context);
 			try {
 				resp.sendRedirect(req.getContextPath() + reverse.resolve("login") + "?next=" + URLEncoder.encode(req.getRequestURI(), "UTF-8"));
 			} catch (RuleNotFoundException e) {
 				log.error(e.getMessage());
 				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			} catch (ReverseException e) {
 				log.error(e.getMessage());
 				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			}
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.servlet.Filter#destroy()
 	 */
 	@Override
 	public void destroy() {
 		UserServiceFactory.destroy();
 	}
 
 }
