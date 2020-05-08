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
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.authentication.JAMWikiAuthenticationConfiguration;
 import org.jamwiki.db.DatabaseUpgrades;
 import org.jamwiki.model.VirtualWiki;
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
 	/** The name of the JSP file used to render the servlet output. */
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
 		pageInfo.setPageTitle(new WikiMessage("upgrade.title", Environment.getValue(Environment.PROP_BASE_WIKI_VERSION), WikiVersion.CURRENT_WIKI_VERSION));
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
 	private boolean login(HttpServletRequest request) throws WikiException {
 		String password = request.getParameter("password");
 		String username = request.getParameter("username");
 		return DatabaseUpgrades.login(username, password);
 	}
 
 	/**
 	 *
 	 */
 	private void upgrade(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
 		List<WikiMessage> errors = new ArrayList<WikiMessage>();
 		List<WikiMessage> messages = new ArrayList<WikiMessage>();
 		try {
 			if (!this.login(request)) {
 				throw new WikiException(new WikiMessage("error.login"));
 			}
 			WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
 			if (oldVersion.before(0, 7, 0)) {
 				throw new WikiException(new WikiMessage("upgrade.error.oldversion", WikiVersion.CURRENT_WIKI_VERSION, "0.7.0"));
 			}
 			// first perform database upgrades
 			this.upgradeDatabase(true, messages);
 			// perform any additional upgrades required
 			if (oldVersion.before(0, 8, 0)) {
 				try {
 					WikiBase.getDataHandler().reloadLogItems();
 				} catch (DataAccessException e) {
 					logger.warning("Failure during upgrade while reloading log items.  Please use the Special:Maintenance page to complete this step.", e);
 					messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
 				}
 				try {
 					WikiBase.getDataHandler().reloadRecentChanges();
 				} catch (DataAccessException e) {
 					logger.warning("Failure during upgrade while reloading recent changes.  Please use the Special:Maintenance page to complete this step.", e);
 					messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
 				}
 			}
 			// upgrade stylesheet
 			if (this.upgradeStyleSheetRequired()) {
 				this.upgradeStyleSheet(request, messages);
 			}
 			errors = ServletUtil.validateSystemSettings(Environment.getInstance());
 			try {
 				Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiVersion.CURRENT_WIKI_VERSION);
 				Environment.saveProperties();
 				// reset data handler and other instances.  this probably hides a bug
 				// elsewhere since no reset should be needed, but it's anyone's guess
 				// where that might be...
 				WikiBase.reload();
 			} catch (Exception e) {
 				logger.severe("Failure during upgrade while saving properties and executing WikiBase.reload()", e);
 				throw new WikiException(new WikiMessage("upgrade.error.nonfatal", e.toString()));
 			}
 		} catch (WikiException e) {
 			errors.add(e.getWikiMessage());
 		}
 		if (!errors.isEmpty()) {
 			errors.add(new WikiMessage("upgrade.caption.upgradefailed"));
 			next.addObject("errors", errors);
 			next.addObject("failure", "true");
 		} else {
 			handleUpgradeSuccess(request, next, pageInfo);
 		}
 		next.addObject("messages", messages);
 		this.view(request, next, pageInfo);
 	}
 		
 	/**
 	 *
 	 */
 	private void handleUpgradeSuccess(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
 		WikiMessage wm = new WikiMessage("upgrade.caption.upgradecomplete");
 		try {
 			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(WikiBase.DEFAULT_VWIKI);
 			WikiLink wikiLink = new WikiLink();
 			wikiLink.setDestination(virtualWiki.getDefaultTopicName());
 			String htmlLink = LinkUtil.buildInternalLinkHtml(request.getContextPath(), virtualWiki.getName(), wikiLink, virtualWiki.getDefaultTopicName(), null, null, true);
 			// do not escape the HTML link
 			wm.setParamsWithoutEscaping(new String[]{htmlLink});
 		} catch (DataAccessException e) {
 			// building a link to the start page shouldn't fail, but if it does display a message
 			wm = new WikiMessage("upgrade.error.nonfatal", e.toString());
 			logger.warning("Upgrade complete, but unable to build redirect link to the start page.", e);
 		}
 		next.addObject("successMessage", wm);
 		// force logout to ensure current user will be re-validated.  this is
 		// necessary because the upgrade may have changed underlying data structures.
 		SecurityContextHolder.clearContext();
 		// force group permissions to reset
 		JAMWikiAuthenticationConfiguration.resetDefaultGroupRoles();
 		JAMWikiAuthenticationConfiguration.resetJamwikiAnonymousAuthorities();
 	}
 
 	/**
 	 *
 	 */
 	private boolean upgradeDatabase(boolean performUpgrade, List<WikiMessage> messages) throws WikiException {
 		boolean upgradeRequired = false;
 		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
 		if (oldVersion.before(0, 8, 0)) {
 			upgradeRequired = true;
 			if (performUpgrade) {
 				messages = DatabaseUpgrades.upgrade080(messages);
 			}
 		}
 		return upgradeRequired;
 	}
 
 	/**
 	 *
 	 */
 	private boolean upgradeStyleSheet(HttpServletRequest request, List<WikiMessage> messages) {
 		try {
 			List<VirtualWiki> virtualWikis = WikiBase.getDataHandler().getVirtualWikiList();
 			for (VirtualWiki virtualWiki : virtualWikis) {
 				WikiBase.getDataHandler().updateSpecialPage(request.getLocale(), virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, ServletUtil.getIpAddress(request));
 				messages.add(new WikiMessage("upgrade.message.stylesheet.success", virtualWiki.getName()));
 			}
 			return true;
 		} catch (WikiException e) {
 			logger.warning("Failure while updating JAMWiki stylesheet", e);
 			messages.add(e.getWikiMessage());
 			messages.add(new WikiMessage("upgrade.message.stylesheet.failure",  e.getMessage()));
 			return false;
 		} catch (DataAccessException e) {
 			logger.warning("Failure while updating JAMWiki stylesheet", e);
 			messages.add(new WikiMessage("upgrade.message.stylesheet.failure",  e.getMessage()));
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private boolean upgradeStyleSheetRequired() {
 		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (oldVersion.before(0, 9, 0)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
 		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
 		if (oldVersion.before(0, 7, 0)) {
 			List<WikiMessage> errors = new ArrayList<WikiMessage>();
 			errors.add(new WikiMessage("upgrade.error.oldversion", WikiVersion.CURRENT_WIKI_VERSION, "0.7.0"));
 			next.addObject("errors", errors);
 		}
 		List<WikiMessage> upgradeDetails = new ArrayList<WikiMessage>();
 		try {
 			if (this.upgradeDatabase(false, null)) {
 				upgradeDetails.add(new WikiMessage("upgrade.caption.database"));
 			}
 		} catch (Exception e) {
 			// never thrown when the first parameter is false
 		}
 		if (this.upgradeStyleSheetRequired()) {
 			upgradeDetails.add(new WikiMessage("upgrade.caption.stylesheet"));
 		}
 		upgradeDetails.add(new WikiMessage("upgrade.caption.releasenotes"));
 		upgradeDetails.add(new WikiMessage("upgrade.caption.manual"));
 		next.addObject("upgradeDetails", upgradeDetails);
 		pageInfo.setContentJsp(JSP_UPGRADE);
 		pageInfo.setSpecial(true);
 	}
 }
