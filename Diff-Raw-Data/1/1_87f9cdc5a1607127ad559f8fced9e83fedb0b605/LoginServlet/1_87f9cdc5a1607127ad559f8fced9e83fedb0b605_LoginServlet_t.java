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
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class LoginServlet extends JAMWikiServlet {
 
 	/** Logger */
 	private static final Logger logger = Logger.getLogger(LoginServlet.class);
 
 	/**
 	 *
 	 */
 	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
 		ModelAndView next = new ModelAndView("wiki");
 		try {
 			if (isTopic(request, "Special:Logout")) {
 				// FIXME - response is non-standard here
 				logout(request, response, next);
 				return null;
 			}
 			if (request.getParameter("function") != null) {
 				// FIXME - response is non-standard here
 				if (login(request, response, next)) {
 					// FIXME - use Spring
 					// login successful, non-Spring redirect
 					return null;
 				}
 			} else {
 				viewLogin(request, next, null);
 			}
 		} catch (Exception e) {
 			viewError(request, next, e);
 		}
 		loadDefaults(request, next, this.pageInfo);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void logout(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
 		String virtualWikiName = JAMWikiServlet.getVirtualWikiFromURI(request);
 		request.getSession().invalidate();
 		Utilities.removeCookie(response, JAMWikiServlet.USER_COOKIE);
 		String redirect = request.getParameter("redirect");
 		if (!StringUtils.hasText(redirect)) {
 			VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
 			redirect = virtualWiki.getDefaultTopicName();
 		}
 		redirect = LinkUtil.buildInternalLinkUrl(request.getContextPath(), virtualWikiName, redirect);
 		// FIXME - can a redirect be done with Spring?
 		redirect(redirect, response);
 	}
 
 	/**
 	 *
 	 */
 	private boolean login(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
 		String virtualWikiName = JAMWikiServlet.getVirtualWikiFromURI(request);
 		String password = request.getParameter("password");
 		String username = request.getParameter("username");
 		String redirect = request.getParameter("redirect");
 		if (!StringUtils.hasText(redirect)) {
 			VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
 			String topic = virtualWiki.getDefaultTopicName();
 			redirect = LinkUtil.buildInternalLinkUrl(request.getContextPath(), virtualWikiName, topic);
 		}
 		WikiUser user = WikiBase.getHandler().lookupWikiUser(username, password, false);
 		if (user == null) {
 			next.addObject("errorMessage", new WikiMessage("error.login"));
 			next.addObject("redirect", redirect);
			this.pageInfo.setPageTitle(new WikiMessage("login.title"));
 			this.pageInfo.setSpecial(true);
 			this.pageInfo.setPageAction(JAMWikiServlet.ACTION_LOGIN);
 			return false;
 		}
 		request.getSession().setAttribute(JAMWikiServlet.PARAMETER_USER, user);
 		if (request.getParameter("remember") != null) {
 			String cookieValue = user.getLogin() + JAMWikiServlet.USER_COOKIE_DELIMITER + user.getEncodedPassword();
 			Utilities.addCookie(response, JAMWikiServlet.USER_COOKIE, cookieValue, JAMWikiServlet.USER_COOKIE_EXPIRES);
 		}
 		// FIXME - can a redirect be done with Spring?
 		redirect(redirect, response);
 		return true;
 	}
 }
