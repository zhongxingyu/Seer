 package com.sras.client.action;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.UUID;
 
 import javax.security.auth.login.LoginException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Category;
 import org.apache.velocity.context.Context;
 
 import com.sras.client.utils.AuthenticationUtils;
 import com.sras.client.utils.ClientConstants;
 import com.sras.client.utils.SessionHelper;
 import com.sras.client.utils.Utilities;
 import com.sras.dao.ModelFactory;
 import com.sras.dao.UserDao;
 import com.sras.datamodel.UserData;
 
 public class LoginCommand extends Command {
 	protected static Category log = Category.getInstance(LoginCommand.class);
 
 	public LoginCommand(HttpServletRequest request,
 			HttpServletResponse response, Context ctx) {
 		super(request, response, ctx);
 	}
 
 	public String doPost() throws Exception {
 		TEMPLATE_NAME = "login.vm";
 		try {
 			TEMPLATE_NAME = handleLogin();
 		} catch (Exception e) {
 			log.debug(e);
 			HttpSession session = request.getSession();
 			int loginFailureCount = getLoginFailureCount(session);
 			session.setAttribute(ClientConstants.loginFailureCount,
 					loginFailureCount + 1);
 			if (loginFailureCount > 2) {
 				ctx.put("showCaptcha", "yes");
 			}
 			ctx.put(ClientConstants.errorTextVariableName + "-signin",
 					e.getMessage());
 			TEMPLATE_NAME = "login.vm";
 		}
 		return TEMPLATE_NAME;
 	}
 
 	public String doGet() throws Exception {
 		TEMPLATE_NAME = "login.vm";
 		HttpSession session = request.getSession();
 		int loginFailureCount = getLoginFailureCount(session);
 
 		if (loginFailureCount > 2) {
 			ctx.put("showCaptcha", "yes");
 		}
 
 		if (session.getAttribute("userName") != null) {
 			try {
 				redirectToURL(request, response,
 						ClientConstants.servletPageWithDefaultAction);
 			} catch (IOException e) {
 				log.debug(e);
 			}
 		}
 		return TEMPLATE_NAME;
 	}
 
 	private int getLoginFailureCount(HttpSession session) {
 		if (session == null) {
 			return 0;
 		}
 		int loginFailureCount = 0;
 		Object obj = session.getAttribute(ClientConstants.loginFailureCount);
 		if (obj != null) {
 			loginFailureCount = (Integer) obj;
 		}
 		return loginFailureCount;
 	}
 
 	private String handleLogin() throws Exception {
 		String userName = addToContext("lusername", false);
 		String password = request.getParameter("password");
 		String rememberUserNameString = request
 				.getParameter("rememberUserName");
 		boolean rememberUserName = rememberUserNameString != null;
 
 		HttpSession session = request.getSession();
 		int loginFailureCount = getLoginFailureCount(session);
 
 		if (loginFailureCount > 3) {
 			if (verifyReCaptcha()) {
 				log.debug("Answer was entered correctly!");
 			} else {
 				throw new Exception("ReCaptcha answer is incorrect!");
 			}
 		}
 
 		// This is to signin user after signup
 		userName = (String) ((userName == null) ? request
 				.getParameter("suserName") : userName);
 		password = (String) ((password == null) ? request
 				.getParameter("spassword") : password);
 
 		log.debug("User " + userName + " rememberUserName " + rememberUserName);
 		UserData user = new UserData();
 		try {
 			log.debug("logging in: handleLogin");
 			// TODO: Verify User Credentials
 			user.setUserName(userName);
 			if (userName == null || userName.trim().length() == 0
 					|| password == null || password.trim().length() == 0) {
 				throw new LoginException("Invalid Credentials!");
 			}
 			user.setPassword(AuthenticationUtils.createPassword(password));
 
 			UserDao userDao = (UserDao) ModelFactory.getImplementation(user);
 			user = (UserData) userDao.read();
 
 			if (user == null) {
 				throw new LoginException("Invalid Credentials!");
 			}
 
 			setLoginAttributes(request.getSession(), request,
 					user.getUserName(), user.getPassword());
 			log.debug("login attributes set");
 		} catch (Exception e) {
 			// request.getSession().invalidate();
 			System.setProperty("loginName", "");
 			log.info("Login failed. Username=" + userName);
 			log.info("Invalid username or password");
 			log.debug(e);
 			throw e;
 		}
 
 		if (rememberUserName) {
 			String uuid = UUID.randomUUID().toString();
 			// String encryptedUuid = Encryption.encrypt(uuid);
 			Utilities.addCookie(response, ClientConstants.COOKIE_NAME, uuid,
 					ClientConstants.COOKIE_AGE);
 			// ClientConstants.sessions.put(uuid, user);
 			SessionHelper.createUserSession(request, uuid, user.getId(),
 					ClientConstants.COOKIE_AGE);
 			log.debug("After Login UUID ::" + uuid);
 		}
 
 		redirectToPrevUrl();
		return ClientConstants.servletPage;
 	}
 
 	private void redirectToPrevUrl() throws UnsupportedEncodingException,
 			IOException {
 		String prevURLobject = (String) request.getSession().getAttribute(
 				ClientConstants.loginRequest);
 		String prevURL = null;
 
 		// if you are in an application and your session expired, when you click
 		// there the next time the login
 		// page will appear and then you will be redirected to the page you HAD
 		// OPEN
 		//
 		// but if you log out , the prev page is logOut so you are sent back
 		// || !page.equalsIgnoreCase("logout"))
 		if (prevURLobject == null || prevURLobject.indexOf("logout") != -1) {
 			prevURL = ClientConstants.servletPage;
 			log.info("prevURL was null");
 		} else {
 			prevURL = prevURLobject;
 			log.info("prevURL " + prevURL);
 		}
 
 		if (prevURL.length() > 0) {
 			log.info("About to redirect to " + prevURL);
 			Command.redirectToURL(request, response, prevURL);
 		}
 	}
 
 	public static void setLoginAttributes(HttpSession session,
 			HttpServletRequest request, String userName, String password)
 			throws Exception
 
 	{
 		session.setAttribute("userName", userName);
 		// session.setAttribute("password", password);
 
 		log.info("User " + userName + " logged in from "
 				+ Utilities.getRemoteHostName(request));
 		System.setProperty("adminDefaultLoginName", userName);
 	}
 }
