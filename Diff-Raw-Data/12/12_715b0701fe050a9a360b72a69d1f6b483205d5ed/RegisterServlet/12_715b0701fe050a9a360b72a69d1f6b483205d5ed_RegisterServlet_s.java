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
 
 import java.util.Locale;
 import java.util.TreeMap;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.model.WikiUserInfo;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to process new user account setup.
  */
 public class RegisterServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(RegisterServlet.class.getName());
 	protected static final String JSP_REGISTER = "register.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (request.getParameter("function") == null) {
 			view(request, next, pageInfo);
 		} else {
 			register(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void loadDefaults(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, WikiUser user, WikiUserInfo userInfo) throws Exception {
 		if (!StringUtils.hasText(user.getDefaultLocale()) && request.getLocale() != null) {
 			user.setDefaultLocale(request.getLocale().toString());
 		}
 		TreeMap locales = new TreeMap();
 		Locale[] localeArray = Locale.getAvailableLocales();
 		for (int i=0; i < localeArray.length; i++) {
 			String key = localeArray[i].toString();
 			String value = key + " - " + localeArray[i].getDisplayName(localeArray[i]);
 			locales.put(value, key);
 		}
 		next.addObject("locales", locales);
 		next.addObject("newuser", user);
 		next.addObject("newuserinfo", userInfo);
 		pageInfo.setSpecial(true);
 		pageInfo.setContentJsp(JSP_REGISTER);
 		pageInfo.setPageTitle(new WikiMessage("register.title"));
 	}
 
 	/**
 	 *
 	 */
 	private void register(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWikiName = Utilities.getVirtualWikiFromURI(request);
 		WikiUser user = this.setWikiUser(request);
 		WikiUserInfo userInfo = this.setWikiUserInfo(request);
 		next.addObject("newuser", user);
 		next.addObject("newuserinfo", userInfo);
 		Vector errors = validate(request, user);
 		if (!errors.isEmpty()) {
 			next.addObject("errors", errors);
 			String oldPassword = request.getParameter("oldPassword");
 			String newPassword = request.getParameter("newPassword");
 			String confirmPassword = request.getParameter("confirmPassword");
 			if (oldPassword != null) {
 				next.addObject("oldPassword", oldPassword);
 			}
 			if (newPassword != null) {
 				next.addObject("newPassword", newPassword);
 			}
 			if (confirmPassword != null) {
 				next.addObject("confirmPassword", confirmPassword);
 			}
 			this.loadDefaults(request, next, pageInfo, user, userInfo);
 		} else {
 			WikiBase.getDataHandler().writeWikiUser(user, userInfo, null);
			// force logout to ensure current user will be re-validated.  this is
			// necessary because the install may have changed underlying data structures.
 			SecurityContextHolder.clearContext();
 			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
 			String topic = virtualWiki.getDefaultTopicName();
 			ServletUtil.redirect(next, virtualWikiName, topic);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private WikiUser setWikiUser(HttpServletRequest request) throws Exception {
 		String username = request.getParameter("login");
 		WikiUser user = new WikiUser(username);
 		String userIdString = request.getParameter("userId");
 		if (StringUtils.hasText(userIdString)) {
 			int userId = new Integer(userIdString).intValue();
 			if (userId > 0) {
 				user = WikiBase.getDataHandler().lookupWikiUser(userId, null);
 			}
 		}
 		user.setDisplayName(request.getParameter("displayName"));
 		String newPassword = request.getParameter("newPassword");
 		if (StringUtils.hasText(newPassword)) {
 			user.setPassword(Encryption.encrypt(newPassword));
 		}
 		user.setDefaultLocale(request.getParameter("defaultLocale"));
 		// FIXME - need to distinguish between add & update
 		user.setCreateIpAddress(request.getRemoteAddr());
 		user.setLastLoginIpAddress(request.getRemoteAddr());
 		return user;
 	}
 
 	/**
 	 *
 	 */
 	private WikiUserInfo setWikiUserInfo(HttpServletRequest request) throws Exception {
 		WikiUserInfo userInfo = new WikiUserInfo();
 		String username = request.getParameter("login");
 		String userIdString = request.getParameter("userId");
 		if (StringUtils.hasText(userIdString)) {
 			int userId = new Integer(userIdString).intValue();
 			if (userId > 0) {
 				userInfo = WikiBase.getUserHandler().lookupWikiUserInfo(username);
 			}
 		}
 		if (!WikiBase.getUserHandler().isWriteable()) {
 			return userInfo;
 		}
 		userInfo.setUsername(username);
 		userInfo.setEmail(request.getParameter("email"));
 		userInfo.setFirstName(request.getParameter("firstName"));
 		userInfo.setLastName(request.getParameter("lastName"));
 		String newPassword = request.getParameter("newPassword");
 		if (StringUtils.hasText(newPassword)) {
 			userInfo.setEncodedPassword(Encryption.encrypt(newPassword));
 		}
 		return userInfo;
 	}
 
 	/**
 	 *
 	 */
 	private Vector validate(HttpServletRequest request, WikiUser user) throws Exception {
 		Vector errors = new Vector();
 		try {
 			Utilities.validateUserName(user.getUsername());
 		} catch (WikiException e) {
 			errors.add(e.getWikiMessage());
 		}
 		String oldPassword = request.getParameter("oldPassword");
 		if (user.getUserId() > 0 && !WikiBase.getUserHandler().authenticate(user.getUsername(), oldPassword)) {
 			errors.add(new WikiMessage("register.error.oldpasswordinvalid"));
 		}
 		String newPassword = request.getParameter("newPassword");
 		String confirmPassword = request.getParameter("confirmPassword");
 		if (user.getUserId() < 1 && !StringUtils.hasText(newPassword)) {
 			errors.add(new WikiMessage("register.error.passwordempty"));
 		}
 		if (!WikiBase.getUserHandler().isWriteable() && !WikiBase.getUserHandler().authenticate(user.getUsername(), newPassword)) {
 			errors.add(new WikiMessage("register.error.oldpasswordinvalid"));
 		}
 		if (StringUtils.hasText(newPassword) || StringUtils.hasText(confirmPassword)) {
 			if (!StringUtils.hasText(newPassword)) {
 				errors.add(new WikiMessage("error.newpasswordempty"));
 			} else if (WikiBase.getUserHandler().isWriteable() && !StringUtils.hasText(confirmPassword)) {
 				errors.add(new WikiMessage("error.passwordconfirm"));
 			} else if (WikiBase.getUserHandler().isWriteable() && !newPassword.equals(confirmPassword)) {
 				errors.add(new WikiMessage("admin.message.passwordsnomatch"));
 			}
 		}
 		if (user.getUserId() < 1 && WikiBase.getDataHandler().lookupWikiUser(user.getUsername(), null) != null) {
 			errors.add(new WikiMessage("register.error.logininvalid", user.getUsername()));
 		}
 		return errors;
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		// FIXME - i suspect initializing with a null login is bad
 		WikiUser user = new WikiUser("");
 		WikiUserInfo userInfo = new WikiUserInfo();
 		if (Utilities.currentUser(request) != null) {
 			user = Utilities.currentUser(request);
 			userInfo = WikiBase.getUserHandler().lookupWikiUserInfo(user.getUsername());
 		}
 		this.loadDefaults(request, next, pageInfo, user, userInfo);
 	}
 }
