 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: Mar 3, 2003 / 11:43:35 AM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.jforum.entities.User;
 import net.jforum.entities.UserSession;
 import net.jforum.model.DataAccessDriver;
 import net.jforum.model.UserSessionModel;
 import net.jforum.repository.SecurityRepository;
 import net.jforum.util.I18n;
 import net.jforum.util.MD5;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 import freemarker.template.Template;
 
 /**
  * Front Controller.
  * 
  * @author Rafael Steil
 * @version $Id: JForum.java,v 1.39 2004/10/06 15:15:18 rafaelsteil Exp $
  */
 public class JForum extends JForumCommonServlet 
 {
 	private static boolean isDatabaseUp; 
 	
 	private void startDatabase() throws Exception
 	{
 		DBConnection.getImplementation().init();
 		
 		// If init() fails, the above code will not
 		// be executed, soh is safe to have it this way
 		isDatabaseUp = true;
 	}
 	
 	public void init(ServletConfig config) throws ServletException
 	{
 		super.init(config);
 		
 		try {
 			this.startDatabase();
 		}
 		catch (Exception e) {
 			// we don't care about the exception here
 			// to not invalidate the context
 			isDatabaseUp = false;
 		}
 	}
 	
 	private void setAnonymousUserSession(UserSession userSession)
 	{
 		userSession.setStartTime(new Date(System.currentTimeMillis()));
 		userSession.setLastVisit(new Date(System.currentTimeMillis()));
 		userSession.setUserId(SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID));
 		userSession.setSessionId(JForum.getRequest().getSession().getId());
 		SessionFacade.setAttribute("logged", "0");
 	}
 	
 	private void checkCookies() throws Exception
 	{
 		if (SessionFacade.getUserSession() == null) {
 			UserSession userSession = new UserSession();
 			userSession.setSessionId(JForum.getRequest().getSession().getId());
 
 			String cookieName = SystemGlobals.getValue(ConfigKeys.COOKIE_NAME_DATA);
 			
 			Cookie cookie = JForum.getCookie(cookieName);
 			Cookie hashCookie = JForum.getCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_USER_HASH));
 			Cookie autoLoginCookie = JForum.getCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN));
 			
 			// If we don't have any cookie yet, then we should set it with the default values
 			if (hashCookie == null || cookie == null 
 					|| cookie.getValue().equals(SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID))
 					|| (autoLoginCookie == null || !"1".equals(autoLoginCookie.getValue()))) {
 				this.setAnonymousUserSession(userSession);
 			}
 			else {
 				String uid = cookie.getValue();
 				String uidHash = hashCookie.getValue();
 				
 				String securityHash = SystemGlobals.getValue(ConfigKeys.USER_HASH_SEQUENCE);
 				
 				if ((uid != null && !uid.equals(""))
 						&& (securityHash != null && !securityHash.equals(""))
 						&& (MD5.crypt(securityHash + uid).equals(uidHash))) {
 					int userId = Integer.parseInt(uid);
 					userSession.setUserId(userId);
 
 					User user = DataAccessDriver.getInstance().newUserModel().selectById(userId);
 					UserSessionModel sm = DataAccessDriver.getInstance().newUserSessionModel();
 
 					UserSession tmpUs = sm.selectById(userSession, JForum.getConnection());
 					if (tmpUs == null) {
 						userSession.setStartTime(new Date(System.currentTimeMillis()));
 						userSession.setLastVisit(new Date(System.currentTimeMillis()));
 						sm.add(userSession, JForum.getConnection());
 					}
 					else {
 						// Update last visit and session start time
 						userSession.setLastVisit(new Date(tmpUs.getStartTime().getTime() + tmpUs.getSessionTime()));
 						userSession.setStartTime(new Date(System.currentTimeMillis()));
 					}
 					
 					userSession.setPrivateMessages(user.getPrivateMessagesCount());
 					userSession.setUsername(user.getUsername());
 
 					// If the execution point is here, then the user
 					// has choosed "autoLogin"
 					userSession.setAutoLogin(true);
 					SessionFacade.setAttribute("logged", "1");
 					
 					userSession.setLang(user.getLang());
 					if (user.getLang() != null && !user.getLang().equals("") && !I18n.contains(user.getLang())) {
 						I18n.load(user.getLang());
 					}
 				}
 				else {
 					this.setAnonymousUserSession(userSession);
 				}
 			}
 			
 			SessionFacade.add(userSession);
 			SessionFacade.setAttribute("topics_tracking", new HashMap());
 		}
 		else {
 			SessionFacade.getUserSession().updateSessionTime();
 		}
 	}
 	
 	protected void setupTemplateContext()
 	{
 		JForum.getContext().put("contextPath", JForum.getRequest().getContextPath());
 		JForum.getContext().put("serverName", JForum.getRequest().getServerName());
 		JForum.getContext().put("templateName", SystemGlobals.getValue(ConfigKeys.TEMPLATE_NAME));
 		JForum.getContext().put("extension", SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 		JForum.getContext().put("serverPort", Integer.toString(JForum.getRequest().getServerPort()));
 		JForum.getContext().put("I18n", I18n.getInstance());
 		JForum.getContext().put("version", SystemGlobals.getValue(ConfigKeys.VERSION));
		JForum.getContext().put("forumTitle", SystemGlobals.getValue(ConfigKeys.FORUM_PAGE_TITLE));
 		JForum.getContext().put("pageTitle", SystemGlobals.getValue(ConfigKeys.FORUM_PAGE_TITLE));
 		JForum.getContext().put("metaKeywords", SystemGlobals.getValue(ConfigKeys.FORUM_PAGE_METATAG_KEYWORDS));
 		JForum.getContext().put("metaDescription", SystemGlobals.getValue(ConfigKeys.FORUM_PAGE_METATAG_DESCRIPTION));
 		JForum.getContext().put("forumLink", SystemGlobals.getValue(ConfigKeys.FORUM_LINK));
 		JForum.getContext().put("encoding", SystemGlobals.getValue(ConfigKeys.ENCODING));
 	}
 	
 	public void service(HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException
 	{
 		BufferedWriter out = null;
 		
 		try {
 			// Initializes thread local data
 			DataHolder dataHolder = new DataHolder();
 			localData.set(dataHolder);
 
 			String encoding = SystemGlobals.getValue(ConfigKeys.ENCODING);
 			req.setCharacterEncoding(encoding);
 			
 			// Request
 			ActionServletRequest request = new ActionServletRequest(req);
 			request.setCharacterEncoding(encoding);
 
 			dataHolder.setResponse(response);
 			dataHolder.setRequest(request);
 			
			if (!isDatabaseUp) {
				this.startDatabase();
			}
			
 			
 			if (isDatabaseUp) {
 				dataHolder.setConnection(DBConnection.getImplementation().getConnection());
 			}
 			
 			localData.set(dataHolder);
 			
			// Context
			this.setupTemplateContext();
 			
 			// Verify cookies
 			this.checkCookies();
 			
 			boolean logged = false;
 			if ("1".equals(SessionFacade.getAttribute("logged"))) {
 				logged = true;
 			}
 			
 			JForum.getContext().put("logged", logged);
 			
 			// Process security data
 			SecurityRepository.load(SessionFacade.getUserSession().getUserId());
 
 			String module = request.getModule();
 			
 			// Gets the module class name
 			String moduleClass = this.getModuleClass(module);
 			
 			JForum.getContext().put("moduleName", module);
 			JForum.getContext().put("action", JForum.getRequest().getAction());
 			
 			JForum.getContext().put("securityHash", MD5.crypt(JForum.getRequest().getSession().getId()));
 			JForum.getContext().put("session", SessionFacade.getUserSession());
 			
 			out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), encoding));
 			if (moduleClass != null) {
 				// Here we go, baby
 				Command c = (Command)Class.forName(moduleClass).newInstance();
 				Template template = c.process();
 
 				if (((DataHolder)localData.get()).getRedirectTo() == null) {
 					response.setContentType("text/html; charset=" + encoding);
 
 					template.process(JForum.getContext(), out);
 					out.flush();
 				}
 			}
 		}
 		catch (Exception e) {
 			response.setContentType("text/html");
 			if (out != null) {
 				new ForumException(e, out);
 				out.flush();
 			}
 			else {
 				new ForumException(e, new BufferedWriter(new OutputStreamWriter(response.getOutputStream())));
 			}
 		}
 		finally {
 			try {
 				if (JForum.getConnection() != null) {
 					DBConnection.getImplementation().releaseConnection(JForum.getConnection());
 				}
 			}
 			catch (Exception e) {
 				new ForumException(e);
 				e.printStackTrace();
 			}
 		}
 		
 		String redirectTo = ((DataHolder)localData.get()).getRedirectTo();
 		if (redirectTo != null) {
 			JForum.getResponse().sendRedirect(redirectTo);
 		}
 		
 		localData.set(null);
 	}
 	
 	/** 
 	 * @see javax.servlet.GenericServlet#destroy()
 	 */
 	public void destroy() {
 		super.destroy();
 		System.out.println("destroying...");
 		
 		try {
 			DBConnection.getImplementation().realReleaseAllConnections();
 		}
 		catch (Exception e) {}
 	}
 }
