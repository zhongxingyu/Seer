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
 
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class RegisterServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(RegisterServlet.class.getName());
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (request.getParameter("function") != null) {
 			register(request, next, pageInfo);
 		} else {
 			view(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (Utilities.currentUser(request) != null) {
 			next.addObject("newuser", Utilities.currentUser(request));
 		}
 		pageInfo.setSpecial(true);
 		pageInfo.setAction(WikiPageInfo.ACTION_REGISTER);
 		pageInfo.setPageTitle(new WikiMessage("register.title"));
 	}
 
 	/**
 	 *
 	 */
 	private void register(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		pageInfo.setSpecial(true);
 		pageInfo.setAction(WikiPageInfo.ACTION_REGISTER);
 		pageInfo.setPageTitle(new WikiMessage("register.title"));
 		String virtualWikiName = ServletUtil.getVirtualWikiFromURI(request);
 		WikiUser user = new WikiUser();
 		String userIdString = request.getParameter("userId");
 		if (StringUtils.hasText(userIdString)) {
 			int userId = new Integer(userIdString).intValue();
 			if (userId > 0) user = WikiBase.getHandler().lookupWikiUser(userId);
 		}
 		user.setLogin(request.getParameter("login"));
 		user.setDisplayName(request.getParameter("displayName"));
 		user.setEmail(request.getParameter("email"));
 		String newPassword = request.getParameter("newPassword");
 		if (StringUtils.hasText(newPassword)) {
 			user.setEncodedPassword(Encryption.encrypt(newPassword));
 		}
 		// FIXME - need to distinguish between add & update
 		user.setCreateIpAddress(request.getRemoteAddr());
 		user.setLastLoginIpAddress(request.getRemoteAddr());
 		next.addObject("newuser", user);
 		Vector errors = validate(request, user);
 		if (errors.size() > 0) {
 			next.addObject("errors", errors);
 			String oldPassword = request.getParameter("oldPassword");
 			String confirmPassword = request.getParameter("confirmPassword");
 			if (oldPassword != null) next.addObject("oldPassword", oldPassword);
 			if (newPassword != null) next.addObject("newPassword", newPassword);
 			if (confirmPassword != null) next.addObject("confirmPassword", confirmPassword);
 		} else {
 			WikiBase.getHandler().writeWikiUser(user);
 			Utilities.login(request, null, user, false);
 			VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
 			String topic = virtualWiki.getDefaultTopicName();
 			ServletUtil.redirect(next, virtualWikiName, topic);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private Vector validate(HttpServletRequest request, WikiUser user) throws Exception {
 		Vector errors = new Vector();
 		try {
 			Utilities.validateUserName(user.getLogin());
 		} catch (WikiException e) {
 			errors.add(e.getWikiMessage());
 		}
 		String oldPassword = request.getParameter("oldPassword");
 		if (user.getUserId() > 0 && WikiBase.getHandler().lookupWikiUser(user.getLogin(), oldPassword, false) == null) {
 			errors.add(new WikiMessage("register.error.oldpasswordinvalid"));
 		}
 		String newPassword = request.getParameter("newPassword");
 		String confirmPassword = request.getParameter("confirmPassword");
 		if (user.getUserId() < 1 && !StringUtils.hasText(newPassword)) {
 			errors.add(new WikiMessage("register.error.passwordempty"));
 		}
 		if (StringUtils.hasText(newPassword) || StringUtils.hasText(confirmPassword)) {
 			if (!StringUtils.hasText(newPassword)) {
 				errors.add(new WikiMessage("error.newpasswordempty"));
 			} else if (!StringUtils.hasText(confirmPassword)) {
 				errors.add(new WikiMessage("error.passwordconfirm"));
 			} else if (!newPassword.equals(confirmPassword)) {
 				errors.add(new WikiMessage("admin.message.passwordsnomatch"));
 			}
 		}
		if (WikiBase.getHandler().lookupWikiUser(user.getLogin()) != null) {
			errors.add(new WikiMessage("register.error.logininvalid", user.getLogin()));
		}
 		return errors;
 	}
 }
