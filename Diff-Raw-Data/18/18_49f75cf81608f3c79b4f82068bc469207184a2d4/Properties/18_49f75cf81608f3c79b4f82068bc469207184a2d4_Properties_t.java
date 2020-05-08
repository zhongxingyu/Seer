 package org.browsexml.ecampus.domain;
 
 import java.util.Enumeration;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.MessageSource;
 import org.springframework.context.MessageSourceAware;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.stereotype.Component;
 import org.springframework.web.util.UrlPathHelper;
 
 @Component
 public class Properties implements MessageSourceAware {
 	private static Log log = LogFactory.getLog(Properties.class);
 	private static MessageSource  messageSource = null;
 	private HttpServletRequest request;
 	private HttpSession session;
 	
 	public Boolean getAuthenticated() {
 		 try {
 			return (LdapUserDetails)SecurityContextHolder.
 				getContext().getAuthentication().getPrincipal() != null;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	public String getBreadCrumbs() {
 		return getStaticBreadCrumbs(request, session);
 	}
 	
 	public Boolean getTermsNotNull() {
 		return getStaticTermsNotNull(session);
 	}
 	
	public Boolean getSuper() {
		return isSuper();
	}
	
 	public HttpServletRequest getRequest() {
 		return request;
 	}
 
 	public void setRequest(HttpServletRequest request) {
 		this.request = request;
 	}
 
 	public HttpSession getSession() {
 		return session;
 	}
 
 	public void setSession(HttpSession session) {
 		this.session = session;
 	}
 
 	public static String getStaticBreadCrumbs(HttpServletRequest request, HttpSession session) {
 		if (messageSource == null)
 			return "";
 		String uri = new UrlPathHelper().getOriginatingRequestUri(request);
 		String key = new UrlPathHelper().getOriginatingRequestUri(request);
 		log.debug(" original key/uri = " + key);
 		key = key.replaceAll("/ecampus/(?:app/)?([^/?]*).*\\Z", "$1");
 		uri = uri.replaceAll("/ecampus/(?:app/)?", "");
 		for (Enumeration e = request.getHeaderNames();e.hasMoreElements();) {
 			String name = (String) e.nextElement();
 			log.debug("HEADER: " + name + " = " + request.getHeader(name));
 		}
 		
 		Path path = (Path) session.getAttribute("path");
 		if (path == null) {
 			path = new Path();
 			session.setAttribute("path", path);
 		}
 		if (key.equals("resourceNotFound"))
 			return path.out(path.peek().key);
 		
 		String title = messageSource.getMessage(key, 
 				null, key, Locale.getDefault());
 		log.debug(" key = " + key);
 		log.debug(" title = " + title);
 		path.push(key, uri, request.getQueryString(), request.getParameterMap(), title,
 					request.getContextPath());
 		return path.out(key);
 	}
 	
 	public static Boolean getStaticTermsNotNull(HttpSession session) {
 		return session.getAttribute("term") != null;
 	}
 
 	@Override
 	public void setMessageSource(MessageSource messageSource) {
 //		new Exception("MS").printStackTrace();
 		this.messageSource = messageSource;
 	}
 	
 	public static String getEmployeeId() {
     	try {
 			LdapUserDetails user = (LdapUserDetails)SecurityContextHolder.
 				getContext().getAuthentication().getPrincipal();
 
 			return user.getEmployeeId();
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 		return null;
 		//return "000181845";
 	}
 	
 	public static Boolean isSuper() {
 		LdapUserDetails user = (LdapUserDetails)SecurityContextHolder.
 			getContext().getAuthentication().getPrincipal();
 
 		for  (GrantedAuthority auth:user.getAuthorities()) {
 			log.debug("ROLE = " + auth.getAuthority());
 			if (auth.getAuthority().equals("ROLE_DEPT - BOOKSTORE"))
 				return true;
 		}
 
 		return false;
 	}
 	
 	public static int calcPageStart(int pageNo, int sizeNo) {
 		return Math.max(((pageNo - 1) * sizeNo) -1, 0);
 	}
 }
