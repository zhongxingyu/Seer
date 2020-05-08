 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.servlets;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.authentication.JAMWikiAuthenticationConstants;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to handle requests or redirects to the login page, as well as requests to logout.
  */
 public class LoginServlet extends JAMWikiServlet {
 
 	/** Logger */
 	private static final WikiLogger logger = WikiLogger.getLogger(LoginServlet.class.getName());
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		// FIXME - hard coding
 		if (ServletUtil.isTopic(request, "Special:Logout")) {
 			// redirect to the Spring Security logout
 			viewLogout(request, response, pageInfo);
 			return null;
 		}
 		// retrieve the URL to redirect to after successful login (if one is defined)
 		String loginSuccessTarget = request.getParameter(PARAM_LOGIN_SUCCESS_TARGET);
 		next = ServletUtil.viewLogin(request, pageInfo, loginSuccessTarget, null);
 		// use a minimal page during upgrades since underlying data structures may
 		// not yet be in sync with the database
 		this.layout = !WikiUtil.isUpgrade();
 		return next;
 	}
 
 	/**
 	 * Redirect to the default Spring Security logout URL after adding a "successful logout"
 	 * URL to the request.  See the Spring Security LogoutFilter.determineTargetUrl() for
 	 * further details.
 	 */
 	private void viewLogout(HttpServletRequest request, HttpServletResponse response, WikiPageInfo pageInfo) throws IOException {
 		String virtualWikiName = pageInfo.getVirtualWikiName();
		String logoutSuccessUrl = request.getContextPath() + "/" + WikiUtil.findDefaultVirtualWikiUrl(virtualWikiName);
 		try {
 			logoutSuccessUrl = URLEncoder.encode(logoutSuccessUrl, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// this should never happen
 			throw new IllegalStateException("Unsupporting encoding UTF-8");
 		}
 		StringBuilder springSecurityLogoutUrl = new StringBuilder();
 		if (!StringUtils.equals(request.getContextPath(), "/")) {
 			springSecurityLogoutUrl.append(request.getContextPath());
 		}
 		springSecurityLogoutUrl.append(JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGOUT_URL);
 		springSecurityLogoutUrl.append('?').append(JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGOUT_REDIRECT_QUERY_PARAM);
 		springSecurityLogoutUrl.append('=').append(logoutSuccessUrl);
 		response.sendRedirect(response.encodeRedirectURL(springSecurityLogoutUrl.toString()));
 	}
 }
