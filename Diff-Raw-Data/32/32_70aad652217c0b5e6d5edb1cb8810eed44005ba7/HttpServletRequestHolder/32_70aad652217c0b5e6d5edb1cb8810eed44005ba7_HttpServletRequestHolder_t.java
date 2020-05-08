 /**
  * 
  */
 package org.cotrix.web.common.server.util;
 import javax.inject.Singleton;
 import javax.servlet.ServletRequestEvent;
 import javax.servlet.ServletRequestListener;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class HttpServletRequestHolder implements ServletRequestListener {
 
 	private static ThreadLocal<HttpServletRequest> REQUESTS = new ThreadLocal<>();
 
 	@Override
 	public void requestDestroyed(ServletRequestEvent sre) {
 		REQUESTS.remove();
 	}
 
 	@Override
 	public void requestInitialized(ServletRequestEvent sre) {
 		REQUESTS.set(HttpServletRequest.class.cast(sre.getServletRequest()));
 	}
 
 	public HttpServletRequest getRequest() {
 		return REQUESTS.get();
 	}
 }
