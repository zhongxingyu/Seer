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
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.authentication.JAMWikiAnonymousProcessingFilter;
 import org.jamwiki.db.DatabaseUpgrades;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to automatically handle JAMWiki upgrades, including configuration and
  * data modifications.
  *
  * @see org.jamwiki.servlets.SetupServlet
  */
 public class UpgradeServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(UpgradeServlet.class.getName());
 	protected static final String JSP_UPGRADE = "upgrade.jsp";
 
 	/**
 	 * This method handles the request after its parent class receives control.
 	 *
 	 * @param request - Standard HttpServletRequest object.
 	 * @param response - Standard HttpServletResponse object.
 	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (!WikiUtil.isUpgrade()) {
 			throw new WikiException(new WikiMessage("upgrade.error.notrequired"));
 		}
 		String function = request.getParameter("function");
 		if (!StringUtils.isBlank(function) && function.equals("upgrade")) {
 			upgrade(request, next, pageInfo);
 		} else {
 			view(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	protected void initParams() {
 		this.layout = false;
 		this.displayJSP = "upgrade";
 	}
 
 	/**
 	 * Special login method - it cannot be assumed that the database schema
 	 * is unchanged, so do not use standard methods.
 	 */
 	private boolean login(HttpServletRequest request) throws Exception {
 		String password = request.getParameter("password");
 		String username = request.getParameter("username");
 		WikiUser user = null;
 		if (!WikiBase.getUserHandler().authenticate(username, password)) {
 			return false;
 		}
 		user = DatabaseUpgrades.getWikiUser(username);
 		return (user != null);
 	}
 
 	/**
 	 *
 	 */
 	private void upgrade(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (!this.login(request)) {
 			next.addObject("error", new WikiMessage("error.login"));
 			pageInfo.setContentJsp(JSP_UPGRADE);
 			pageInfo.setSpecial(true);
 			pageInfo.setPageTitle(new WikiMessage("upgrade.title"));
 			return;
 		}
 		Vector messages = new Vector();
 		boolean success = true;
 		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
 		if (oldVersion.before(0, 4, 0)) {
 			messages.add(new WikiMessage("upgrade.error.oldversion", WikiVersion.CURRENT_WIKI_VERSION, "0.4.0"));
 			success = false;
 		} else {
 			// first perform database upgrades
 			if (!Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals("FILE")) {
 				try {
 					if (oldVersion.before(0, 4, 2)) {
 						messages = DatabaseUpgrades.upgrade042(messages);
 					}
 					if (oldVersion.before(0, 5, 0)) {
 						messages = DatabaseUpgrades.upgrade050(messages);
 					}
 					if (oldVersion.before(0, 6, 0)) {
 						messages = DatabaseUpgrades.upgrade060(messages);
 					}
 					if (oldVersion.before(0, 6, 1)) {
 						messages = DatabaseUpgrades.upgrade061(messages);
 					}
 				} catch (Exception e) {
 					// FIXME - hard coding
 					String msg = "Unable to complete upgrade to new JAMWiki version.";
 					logger.severe(msg, e);
 					messages.add(msg + ": " + e.getMessage());
 					success = false;
 				}
 			}
 			// then perform other needed upgrades
 			boolean stylesheet = false;
 			if (oldVersion.before(0, 4, 2)) {
 				stylesheet = true;
 			}
 			if (oldVersion.before(0, 5, 1)) {
 				stylesheet = true;
 			}
 			if (oldVersion.before(0, 6, 0)) {
 				stylesheet = true;
 			}
 			if (oldVersion.before(0, 6, 1)) {
 				stylesheet = true;
 			}
			if (oldVersion.before(0, 6, 3)) {
				stylesheet = true;
			}
 			if (stylesheet) {
 				// upgrade stylesheet
 				if (!upgradeStyleSheet(request, messages)) {
 					success = false;
 				}
 			}
 			Vector errors = WikiUtil.validateSystemSettings(Environment.getInstance());
 			if (!errors.isEmpty()) {
 				next.addObject("errors", errors);
 				success = false;
 			}
 		}
 		if (success) {
 			Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiVersion.CURRENT_WIKI_VERSION);
 			Environment.saveProperties();
 			// reset data handler and other instances.  this probably hides a bug
 			// elsewhere since no reset should be needed, but it's anyone's guess
 			// where that might be...
 			WikiBase.reload();
 			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(WikiBase.DEFAULT_VWIKI);
 			WikiLink wikiLink = new WikiLink();
 			wikiLink.setDestination(virtualWiki.getDefaultTopicName());
 			String htmlLink = LinkUtil.buildInternalLinkHtml(request.getContextPath(), virtualWiki.getName(), wikiLink, virtualWiki.getDefaultTopicName(), null, null, true);
 			WikiMessage wm = new WikiMessage("upgrade.caption.upgradecomplete");
 			// do not escape the HTML link
 			wm.setParamsWithoutEscaping(new String[]{htmlLink});
 			next.addObject("message", wm);
 			// force logout to ensure current user will be re-validated.  this is
 			// necessary because the upgrade may have changed underlying data structures.
 			SecurityContextHolder.clearContext();
 			// force group permissions to reset
 			WikiUser.resetDefaultGroupRoles();
 			JAMWikiAnonymousProcessingFilter.reset();
 		} else {
 			next.addObject("error", new WikiMessage("upgrade.caption.upgradefailed"));
 			next.addObject("failure", "true");
 		}
 		next.addObject("messages", messages);
 		pageInfo.setContentJsp(JSP_UPGRADE);
 		pageInfo.setSpecial(true);
 		pageInfo.setPageTitle(new WikiMessage("upgrade.title"));
 	}
 
 	/**
 	 *
 	 */
 	private boolean upgradeStyleSheet(HttpServletRequest request, Vector messages) throws Exception {
 		try {
 			WikiUser user = WikiUtil.currentUser();
 			Collection virtualWikis = WikiBase.getDataHandler().getVirtualWikiList(null);
 			for (Iterator iterator = virtualWikis.iterator(); iterator.hasNext();) {
 				VirtualWiki virtualWiki = (VirtualWiki)iterator.next();
 				WikiBase.getDataHandler().updateSpecialPage(request.getLocale(), virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user, request.getRemoteAddr(), null);
 				messages.add("Updated stylesheet for virtual wiki " + virtualWiki.getName());
 			}
 			return true;
 		} catch (Exception e) {
 			// FIXME - hard coding
 			String msg = "Unable to complete upgrade to new JAMWiki version.";
 			logger.severe(msg, e);
 			messages.add(msg + ": " + e.getMessage());
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
 		if (oldVersion.before(0, 4, 0)) {
 			Vector errors = new Vector();
 			errors.add(new WikiMessage("upgrade.error.oldversion", WikiVersion.CURRENT_WIKI_VERSION, "0.4.0"));
 			next.addObject("errors", errors);
 		}
 		pageInfo.setContentJsp(JSP_UPGRADE);
 		pageInfo.setSpecial(true);
 		pageInfo.setPageTitle(new WikiMessage("upgrade.title"));
 	}
 }
