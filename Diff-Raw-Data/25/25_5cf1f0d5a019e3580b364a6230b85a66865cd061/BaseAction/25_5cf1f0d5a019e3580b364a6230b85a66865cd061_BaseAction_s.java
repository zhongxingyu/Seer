 /**
  * 
  */
 package com.chinarewards.qqgbvpn.mgmtui.struts;
 
 import org.apache.struts2.ServletActionContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.logic.journal.JournalLogic;
 import com.google.inject.Injector;
 import com.opensymphony.xwork2.ActionSupport;
 
 /**
  * 
  * 
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public abstract class BaseAction extends ActionSupport {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4043054562328091468L;
 
 	protected Logger log = LoggerFactory.getLogger(getClass());
 
 	public <T> T getInstance(Class<T> type) {
 		return ((Injector) ServletActionContext.getServletContext()
 				.getAttribute(Injector.class.getName())).getInstance(type);
 	}
 	
 	protected void logEvent(String event, String entity, String entityId,
 			String eventDetail) {
 		this.getInstance(JournalLogic.class).logEvent(event, entity, entityId,
 				eventDetail);
 	}	
 	
 	/**
 	 * description：获取当前URI
 	 * @time 2011-6-10   下午01:29:54
 	 * @author Seek
 	 */
 	protected String getCurrentPath() {
 		StringBuffer urlBuff = new StringBuffer(ServletActionContext.getRequest().getRequestURI());
 		if (ServletActionContext.getRequest().getQueryString() != null) {
 			urlBuff.append("?");
 			urlBuff.append(ServletActionContext.getRequest().getQueryString());
 		}
 		log.debug("getCurrentPath()   currentURL:"+urlBuff);
 		return urlBuff.toString();
 	} 
 	
 }
