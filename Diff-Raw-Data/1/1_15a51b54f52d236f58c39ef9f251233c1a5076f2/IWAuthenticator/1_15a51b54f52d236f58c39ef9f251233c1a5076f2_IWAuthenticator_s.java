 /*
  * Created on 31.7.2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package com.idega.servlet.filter;
 
 import java.io.IOException;
import java.util.Enumeration;
 import java.util.logging.Logger;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import com.idega.core.accesscontrol.business.LoginBusinessBean;
 import com.idega.core.accesscontrol.jaas.IWJAASAuthenticationRequestWrapper;
 import com.idega.idegaweb.IWApplicationContext;
 import com.idega.idegaweb.IWException;
 import com.idega.idegaweb.IWMainApplicationSettings;
 import com.idega.presentation.IWContext;
 import com.idega.util.CypherText;
 
 /**
  * 
  * This class is responsible for authenticating users into the idegaWeb User
  * system. <br>
  * When the user has a "remember me" cookie set then this filter reads that and
  * logs the user into the system.
  * 
  * @author tryggvil
  * 
  *  
  */
 public class IWAuthenticator implements Filter {
 
 	private static Logger log = Logger.getLogger(IWAuthenticator.class
 			.getName());
 
 	private LoginBusinessBean loginBusiness = new LoginBusinessBean();
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
 	 */
 	public void init(FilterConfig arg0) throws ServletException {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
 	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
 	 */
 	public void doFilter(ServletRequest srequest, ServletResponse sresponse,
 			FilterChain chain) throws IOException, ServletException {
 
 		HttpServletRequest request = (HttpServletRequest)srequest;
 		HttpServletResponse response = (HttpServletResponse)sresponse;
 		//FacesContext fc = FacesContext.getCurrentInstance();
 		//IWContext iwc = IWContext.getIWContext(fc);
 		IWContext iwc = new IWContext(request,response, request.getSession().getServletContext());
 		
 //		Enumeration headerNames = request.getHeaderNames();
 //		System.out.println("------------HEADER BEGINS-------------");
 //		while (headerNames.hasMoreElements()) {
 //		String headerName = (String) headerNames.nextElement();
 //		System.out.println("\t["+headerName+"]: "+request.getHeader(headerName));
 //		}
 //		System.out.println("------------HEADER ENDS-------------");
 		
 //		Enumeration parameterNames = request.getParameterNames();
 //		System.out.println("------------PARAMETERS BEGINS-------------");
 //		while (parameterNames.hasMoreElements()) {
 //		String parameterName = (String) parameterNames.nextElement();
 //		System.out.println("\t["+parameterNames+"]: "+request.getParameter(parameterName));
 //		}
 //		System.out.println("------------PARAMETERS ENDS-------------");
 		
 		
 		if(useBasicAuthenticationMethod(iwc)){
 			if(!iwc.isLoggedOn()){
 				if (!getLoginBusiness(iwc).authenticateBasicAuthenticationRequest(iwc)) {	
 					getLoginBusiness(iwc).callForBasicAuthentication(iwc,null);
 					return;
 				}
 			}
 		} else {
 			if(!iwc.isLoggedOn()){
 				getLoginBusiness(iwc).authenticateBasicAuthenticationRequest(iwc);
 			}
 			setServletContextPath(iwc);
 			
 			tryRegularLogin(iwc);
 			
 			performCookieLogin(iwc);
 			addCookie(iwc);
 		}
 		
 		chain.doFilter(new IWJAASAuthenticationRequestWrapper(iwc), response);
 	}
 
 	/**
 	 * @param iwc
 	 * @return
 	 */
 	private boolean useBasicAuthenticationMethod(IWContext iwc) {
 		return iwc.isWebDavClient();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.Filter#destroy()
 	 */
 	public void destroy() {
 	}
 
 	public String userIDCookieName = "iwrbusid";
 	private static boolean CHECKED_CURRENT_APPCONTEXT=false;
 
 	//public String IW_BUNDLE_IDENTIFIER = "com.idega.block.login";
 	public static final String PARAMETER_ALLOWS_COOKIE_LOGIN = "icusallows";
 
 	
 	/**
 	 * This should propably move to another servlet filter.
 	 * @param iwc
 	 */
 	public void setServletContextPath(IWContext iwc ){
 		if (!hasCheckedCurrentAppContext()) {
 			String contextPath = iwc.getRequest().getContextPath();
 			iwc.getIWMainApplication().setApplicationContextURI(contextPath);
 			CHECKED_CURRENT_APPCONTEXT=true;
 		}
 	}
 	
 	private boolean hasCheckedCurrentAppContext(){
 		return CHECKED_CURRENT_APPCONTEXT;
 	}
 	
 	public void tryRegularLogin(IWContext iwc){
 		try {
 			getLoginBusiness(iwc).actionPerformed(iwc);
 		} catch (IWException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void addCookie(IWContext iwc) {
 		Cookie userIDCookie = getCookie(iwc);
 		//System.err.println("actionPerformed in LoginCookieListener");
 		if (getLoginBusiness(iwc).isLogOffAction(iwc) && userIDCookie != null) {
 			userIDCookie.setMaxAge(0);
 			iwc.addCookies(userIDCookie);
 		}
 
 		else if (iwc.isParameterSet(PARAMETER_ALLOWS_COOKIE_LOGIN)
 				&& LoginBusinessBean.isLoggedOn(iwc)) {
 			if (userIDCookie == null) {
 				//System.err.println("adding cookie");
 				String login = getLoginBusiness(iwc).getLoggedOnInfo(iwc)
 						.getLogin();
 				userIDCookie = new Cookie(userIDCookieName, cypherUserLogin(
 						iwc, login));
 				userIDCookie.setMaxAge(60 * 60 * 24 * 30);
 				iwc.addCookies(userIDCookie);
 			}
 		}
 	}
 
 	public void performCookieLogin(IWContext iwc) {
 		Cookie userIDCookie = getCookie(iwc);
 		if (!iwc.isLoggedOn()) {
 			//System.err.println("no user is logged on");
 			if (userIDCookie != null) {
 				//System.err.println("found the cookie");
 				String cypheredLoginName = userIDCookie.getValue();
 				String loginName = deCypherUserLogin(iwc, cypheredLoginName);
 				try {
 					getLoginBusiness(iwc).logInUnVerified(iwc, loginName);
 				} catch (Exception ex) {
 					//throw new IWException("Cookie login failed :
 					// "+ex.getMessage());
 					log.warning("Cookie login failed : :" + ex.getMessage());
 				}
 			} else {//System.err.println("no cookie found");
 			}
 		}
 	}
 
 	private Cookie getCookie(IWContext iwc) {
 		Cookie userIDCookie = iwc.getCookie(userIDCookieName);
 		return userIDCookie;
 	}
 
 	public String getCypherKey(IWApplicationContext iwc) {
 		IWMainApplicationSettings settings = iwc.getIWMainApplication()
 				.getSettings();
 		CypherText cyph = new CypherText();
 
 		String cypherKey = settings.getProperty("cypherKey");
 		if ((cypherKey == null) || (cypherKey.equalsIgnoreCase(""))) {
 			cypherKey = cyph.getKey(100);
 			settings.setProperty("cypherKey", cypherKey);
 		}
 
 		return (cypherKey);
 	}
 
 	protected String cypherUserLogin(IWApplicationContext iwc, String userLogin) {
 		String key = getCypherKey(iwc);
 		String cypheredId = new CypherText().doCyper(userLogin, key);
 		log.fine("Cyphered " + userLogin + "to " + cypheredId);
 		return cypheredId;
 	}
 
 	protected String deCypherUserLogin(IWApplicationContext iwc,
 			String cypheredLogin) {
 		String key = getCypherKey(iwc);
 		return new CypherText().doDeCypher(cypheredLogin, key);
 	}
 
 	protected LoginBusinessBean getLoginBusiness(IWContext iwc){
 		return loginBusiness;
 	}
 	
 }
