 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.cms.security;
 
 import java.net.URLEncoder;
 import java.security.Principal;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 import edu.yale.its.tp.cas.client.ProxyTicketValidator;
 import edu.yale.its.tp.cas.client.Util;
 
 /**
  * @author Mattias Bogeblad
  *
  * This authentication module authenticates an user against CAS (edu.yale.its.tp.cas)
  * which is a singe sign on service.
  */
 
 public class CASBasicAuthenticationModule extends AuthenticationModule//, AuthorizationModule
 {
     private final static Logger logger = Logger.getLogger(CASBasicAuthenticationModule.class.getName());
 
 	private String loginUrl 			= null;
 	private String logoutUrl 			= null;
 	private String invalidLoginUrl 		= null;
 	private String authenticatorClass 	= null;
 	private String authorizerClass 		= null;
 	private String successLoginUrl		= null;
 	private String serverName			= null;
 	private String casValidateUrl		= null;
 	private String casServiceUrl		= null;
 	private String casLogoutUrl			= null;
 	private String casAuthorizedProxy 	= null;
 	private String casRenew				= null;
 	private Properties extraProperties	= null;
 	
 	/**
 	 * This method handles all of the logic for checking how to handle a login.
 	 */
 	
 	public String authenticateUser(HttpServletRequest request, HttpServletResponse response, FilterChain fc) throws Exception
 	{
 		String authenticatedUserName = null;
 
 		HttpSession session = ((HttpServletRequest)request).getSession();
 
 		String ticket = request.getParameter("ticket");
 		logger.info("ticket:" + ticket);
 		
 		// no ticket?  abort request processing and redirect
 		if (ticket == null || ticket.equals("")) 
 		{
 			if (loginUrl == null) 
 			{
 				throw new ServletException(
 						"When InfoGlueFilter protects pages that do not receive a 'userName' " +
 						"parameter, it needs a org.infoglue.cms.security.loginUrl " +
 						"filter parameter");
 			}
   
 			String requestURI = request.getRequestURI();
 			logger.info("requestURI:" + requestURI);
 
 			String redirectUrl = "";
 
			if(requestURI.indexOf("ViewCMSTool.action") > -1 ||
 			   requestURI.indexOf("standalone") > -1 ||
 			   requestURI.indexOf("workflows") > -1 ||
 			   requestURI.indexOf("ViewDigitalAsset") > -1 ||
 			   requestURI.indexOf("Editor") > -1 ||
 			   requestURI.indexOf("binding") > -1)
 			{
 				if(requestURI.indexOf("?") > 0)
 					redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 				else
 					redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 	
 				logger.info("redirectUrl 1:" + redirectUrl);
 				response.sendRedirect(redirectUrl);
 			}
 			else
 			{
 				response.sendRedirect("index-cms.html");
 			}
 				
 			return null;
 		} 
 	   	
 		authenticatedUserName = authenticate(ticket);
 		logger.info("authenticatedUserName:" + authenticatedUserName);
 		if(authenticatedUserName == null)
 		{
 			String requestURI = request.getRequestURI();
 			logger.info("requestURI:" + requestURI);
 	
 			String redirectUrl = "";
 	
 			if(requestURI.indexOf("?") > 0)
 				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 			else
 				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 		
 			logger.info("redirectUrl 2:" + redirectUrl);
 			response.sendRedirect(redirectUrl);
 	
 			return null;
 		}
 
 		//request.getSession().setAttribute("ticket", ticket);
 	
 		//fc.doFilter(request, response);
 		return authenticatedUserName;
 	}
 	
 	
 	/**
 	 * This method handles all of the logic for checking how to handle a login.
 	 */
 	
 	public String authenticateUser(Map request) throws Exception
 	{
 		String authenticatedUserName = null;
 
 		if(request.containsKey("j_username"))
 		    return (String)request.get("j_username");
 		
 		String ticket = (String)request.get("ticket");
 		logger.info("ticket:" + ticket);
 		
 		// no ticket?  abort request processing and redirect
 		if (ticket == null || ticket.equals("")) 
 		{
 			return null;
 		} 
 		
 		authenticatedUserName = authenticate(ticket);
 		logger.info("authenticatedUserName:" + authenticatedUserName);
 
 		return authenticatedUserName;
 	}
 	
 	/**
 	 * This method handles all of the logic for checking how to handle a login.
 	 */
 	
 	public String getLoginDialogUrl(HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		String url = null;
 
 		HttpSession session = ((HttpServletRequest)request).getSession();
 
 		String ticket = request.getParameter("ticket");
 		logger.info("ticket:" + ticket);
 		
 		// no ticket?  abort request processing and redirect
 		if (ticket == null || ticket.equals("")) 
 		{
 			if (loginUrl == null) 
 			{
 				throw new ServletException(
 						"When InfoGlueFilter protects pages that do not receive a 'userName' " +
 						"parameter, it needs a org.infoglue.cms.security.loginUrl " +
 						"filter parameter");
 			}
   
 			String requestURI = request.getRequestURI();
 			logger.info("requestURI:" + requestURI);
 			
 			String redirectUrl = "";
 
 			if(requestURI.indexOf("?") > 0)
 				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 			else
 				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 	
 			logger.info("redirectUrl 3:" + redirectUrl);
 
 			return redirectUrl;
 		} 
 	   	
 		String authenticatedUserName = authenticate(ticket);
 		logger.info("authenticatedUserName:" + authenticatedUserName);
 		if(authenticatedUserName == null)
 		{
 			String requestURI = request.getRequestURI();
 			logger.info("requestURI:" + requestURI);
 	
 			String redirectUrl = "";
 	
 			if(requestURI.indexOf("?") > 0)
 				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 			else
 				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 		
 			logger.info("redirectUrl 4:" + redirectUrl);
 			response.sendRedirect(redirectUrl);
 	
 			return redirectUrl;
 		}
 
 		return url;
 	}
 	
 	
 	
 
 	/**
 	 * This method authenticates against the infoglue extranet user database.
 	 */
 	
 	private String authenticate(String ticket) throws Exception
 	{
 		boolean isAuthenticated = false;
 		
 	    logger.info("ticket:" + ticket);
 	    TrustManager[] trustAllCerts = new TrustManager[]
 		{
 			new X509TrustManager() 
 			{
 				public java.security.cert.X509Certificate[] getAcceptedIssuers() 
 				{
 					return null;
 				}
 				
 				public void checkClientTrusted
 				(
 					java.security.cert.X509Certificate[] certs, String authType) {
 				    logger.info("Checking if client is trusted...");
 				}
 				/*
 				public void checkServerTrusted(X509Certificate[] certs, String authType)
 				{
 					logger.info("Checking if server is trusted...");				
 				}
 				*/
 				
                 public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
                 {
                     // TODO Auto-generated method stub
                     
                 }
 			}
 		};
 
 		HostnameVerifier hv = new HostnameVerifier() {
 		    public boolean verify(String urlHostName, SSLSession session) {
 		        System.out.println("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
 		        return true;
 		    }
 		};
 		 
 		HttpsURLConnection.setDefaultHostnameVerifier(hv);
 
 		SSLContext sc = SSLContext.getInstance("SSL");
 		sc.init(null, trustAllCerts, new java.security.SecureRandom());
 		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		
 		String authenticatedUserName = null;
 		
 		/* instantiate a new ProxyTicketValidator */
 		ProxyTicketValidator pv = new ProxyTicketValidator();
 		
 		/* set its parameters */
 		pv.setCasValidateUrl(casValidateUrl);
 				
 		System.out.println("validating: " + casServiceUrl);
 		pv.setService(URLEncoder.encode(casServiceUrl, "UTF-8"));
 
 		pv.setServiceTicket(ticket);
 
 		/* 
 		 * If we want to be able to acquire proxy tickets (requires callback servlet to be set up  
 		 * in web.xml - see below)
 		 */
 		//pv.setProxyCallbackUrl("https://gavin.adm.gu.se:9070/uPortal/CasProxyServlet");
 		//pv.setProxyCallbackUrl("http://localhost:8080/infoglueCMSAuthDev/CasProxyServlet");
 
 		/* contact CAS and validate */
 		pv.validate(); 
 
 		/* if we want to look at the raw response, we can use getResponse() */
 		String xmlResponse = pv.getResponse();
 		logger.info("xmlResponse:" + xmlResponse);
 		System.out.println("xmlResponse:" + xmlResponse);
 
 		/* read the response */
 		if(pv.isAuthenticationSuccesful()) 
 		{
 			String user = pv.getUser();
 			List proxyList = pv.getProxyList();
 			authenticatedUserName = pv.getUser();
 		} 
 		else 
 		{
 			String errorCode = pv.getErrorCode();
 			String errorMessage = pv.getErrorMessage();
 			/* handle the error */
 		}
 
 		/* The user is now authenticated. */
 		/* If we did set the proxy callback url, we can get proxy tickets with: */
 
 		//String proxyTicket = edu.yale.its.tp.cas.proxy.ProxyTicketReceptor.getProxyTicket(pv.getPgtIou(), casServiceUrl);
 		logger.info("proxies:\n " + pv.getProxyList()); 
 		
 		return authenticatedUserName;
 	} 
 
 	public Principal loginUser(HttpServletRequest request, HttpServletResponse response, Map status) throws SystemException, Exception 
 	{
 		Principal principal = null;
 		
 		String authenticatedUserName = getAuthenticatedUserName(request, response, status);
 		if(authenticatedUserName != null)
 		{
 			principal = UserControllerProxy.getController().getUser(authenticatedUserName);
 			if(principal == null)
 				throw new SystemException("The CAS-authenticated user " + authenticatedUserName + " was not located in the authorization system's user database.");
 		}
 		
 		return principal;
 	}
 
 	
 	public boolean logoutUser(HttpServletRequest request, HttpServletResponse response) throws Exception 
 	{
 	    String referer = request.getHeader("Referer");
 	    String service = getService(request);
 		if(referer != null && !referer.equals(""))
 		{
 			if(referer.lastIndexOf("/") > 0)
 				referer = referer.substring(0, referer.lastIndexOf("/"));
 			
 			service = referer;
 		}
 		
 		/*
 		if(this.casServiceUrl.equals("$currentUrl"))
 		{
 		  	String originalFullURL = getCurrentURL(request);
 		  	System.out.println("originalFullURL:" + originalFullURL);
 		  	this.casServiceUrl = originalFullURL;
 		}
 		*/
 	  	
 		response.sendRedirect(this.getCasLogoutUrl() + "?service=" + service);
 		
 		return true;
 	}
 
 	/**
 	 * Returns either the configured service or figures it out for the current
 	 * request.  The returned service is URL-encoded.
 	 */
 	private String getService(HttpServletRequest request) throws ServletException, Exception 
 	{
 		// ensure we have a server name or service name
 	  	if (serverName == null && casServiceUrl == null)
 			throw new ServletException("need one of the following configuration "
 		  	+ "parameters: edu.yale.its.tp.cas.client.filter.serviceUrl or "
 		  	+ "edu.yale.its.tp.cas.client.filter.serverName");
 
 		if(this.casServiceUrl.equals("$currentUrl"))
 		{
 		  	String originalFullURL = getCurrentURL(request);
 		  	
 		  	String referer = request.getHeader("Referer");
 		    //System.out.println("referer:" + referer);
 			/*
 		    String service = getService(request);
 			if(referer != null && !referer.equals(""))
 			{
 				if(referer.lastIndexOf("/") > 0)
 					referer = referer.substring(0, referer.lastIndexOf("/"));
 				
 				service = referer;
 			}
 			*/
 		  	
 		  	System.out.println("originalFullURL:" + originalFullURL);
 		  	this.casServiceUrl = originalFullURL;
 		}
 
 	  	if (casServiceUrl != null && casServiceUrl.length() > 0)
 			return URLEncoder.encode(casServiceUrl, "UTF-8");
 	  	else
 			return Util.getService(request, serverName);
 	} 
 
 	public String getCurrentURL(HttpServletRequest request)
 	{
 		return request.getRequestURL() + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
 	}
 
 	public String getAuthenticatorClass()
 	{
 		return authenticatorClass;
 	}
 
 	public void setAuthenticatorClass(String authenticatorClass)
 	{
 		this.authenticatorClass = authenticatorClass;
 	}
 
 	public String getAuthorizerClass()
 	{
 		return authorizerClass;
 	}
 
 	public void setAuthorizerClass(String authorizerClass)
 	{
 		this.authorizerClass = authorizerClass;
 	}
 
 	public String getInvalidLoginUrl()
 	{
 		return invalidLoginUrl;
 	}
 
 	public void setInvalidLoginUrl(String invalidLoginUrl)
 	{
 		this.invalidLoginUrl = invalidLoginUrl;
 	}
 
 	public String getLoginUrl()
 	{
 		return loginUrl;
 	}
 
 	public void setLoginUrl(String loginUrl)
 	{
 		this.loginUrl = loginUrl;
 	}
 
 	public String getLogoutUrl()
 	{
 		return logoutUrl;
 	}
 
 	public void setLogoutUrl(String logoutUrl)
 	{
 		this.logoutUrl = logoutUrl;
 	}
 
 	public String getServerName()
 	{
 		return serverName;
 	}
 
 	public void setServerName(String string)
 	{
 		serverName = string;
 	}
 
 	public Properties getExtraProperties()
 	{
 		return this.extraProperties;
 	}
 
 	public void setExtraProperties(Properties properties)
 	{
 		this.extraProperties = properties;
 	}
 	
 	public String getCasRenew()
 	{
 		return casRenew;
 	}
 
 	public void setCasRenew(String casRenew)
 	{
 		this.casRenew = casRenew;
 	}
 
 	public String getCasServiceUrl()
 	{
 		return casServiceUrl;
 	}
 
 	public void setCasServiceUrl(String casServiceUrl)
 	{
 		this.casServiceUrl = casServiceUrl;
 	}
 
 	public String getCasValidateUrl()
 	{
 		return casValidateUrl;
 	}
 
 	public void setCasValidateUrl(String casValidateUrl)
 	{
 		this.casValidateUrl = casValidateUrl;
 	}
 
 	public String getCasLogoutUrl()
 	{
 		return casLogoutUrl;
 	}
 
 	public void setCasLogoutUrl(String casLogoutUrl)
 	{
 		this.casLogoutUrl = casLogoutUrl;
 	}
 
 	public String getCasAuthorizedProxy()
 	{
 		return casAuthorizedProxy;
 	}
 
 	public void setCasAuthorizedProxy(String casAuthorizedProxy)
 	{
 		this.casAuthorizedProxy = casAuthorizedProxy;
 	}
 
     public Object getTransactionObject()
     {
         return null;
     }
 
     public void setTransactionObject(Object transactionObject)
     {
     }
 
 
 	/**
 	 * This method handles all of the logic for checking how to handle a login.
 	 */
 	
 	private String getAuthenticatedUserName(HttpServletRequest request, HttpServletResponse response, Map status) throws Exception
 	{
 		String authenticatedUserName = null;
 
 		HttpSession session = ((HttpServletRequest)request).getSession();
 
 		String ticket = request.getParameter("ticket");
 		logger.info("ticket:" + ticket);
 		
 		// no ticket?  abort request processing and redirect
 		if (ticket == null || ticket.equals("")) 
 		{
 			if (loginUrl == null) 
 			{
 				throw new ServletException(
 						"When InfoGlueFilter protects pages that do not receive a 'userName' " +
 						"parameter, it needs a org.infoglue.cms.security.loginUrl " +
 						"filter parameter");
 			}
   
 			String requestURI = request.getRequestURI();
 			logger.info("requestURI:" + requestURI);
 			
 			String redirectUrl = "";
 
 			if(requestURI.indexOf("?") > 0)
 				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 			else
 				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 	
 			logger.info("redirectUrl 6:" + redirectUrl);
 			
 			response.sendRedirect(redirectUrl);
 			status.put("redirected", new Boolean(true));
 			return null;
 		} 
 	   	
 		authenticatedUserName = authenticate(ticket);
 		logger.info("authenticatedUserName:" + authenticatedUserName);
 		if(authenticatedUserName == null)
 		{
 			String requestURI = request.getRequestURI();
 			logger.info("requestURI:" + requestURI);
 	
 			String redirectUrl = "";
 	
 			if(requestURI.indexOf("?") > 0)
 				redirectUrl = loginUrl + "&service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 			else
 				redirectUrl = loginUrl + "?service=" + getService(request) + ((casRenew != null && !casRenew.equals("")) ? "&renew="+ casRenew : "");
 		
 			logger.info("redirectUrl 7:" + redirectUrl);
 			response.sendRedirect(redirectUrl);
 	
 			status.put("redirected", new Boolean(true));
 
 			return null;
 		}
 
 		return authenticatedUserName;
 	}
 
 
 	public String getSuccessLoginUrl()
 	{
 		return successLoginUrl;
 	}
 
 
 	public void setSuccessLoginUrl(String successLoginUrl)
 	{
 		this.successLoginUrl = successLoginUrl;
 	}
 
 	public boolean enforceJ2EEContainerPrincipal() 
 	{
 		return false;
 	}
 
 }
