 package app.managedBean;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 import app.util.LogUtils;
 import app.util.WinampUtils;
 
 @ManagedBean(name = "pageSelector")
 @RequestScoped
 public class PageSelector {
 
 	private static final Logger log = LogUtils.getLogger(PageSelector.class);
 	
 	private static final String IP_LOCALHOST = "127.0.0.1";
 	private static final String PAGE_USERS = "/indexUser.xhtml";
 	private static final String PAGE_DJ = "/indexDJ.xhtml";
 	
 	private String page;
 
 	public String getPage() {
 		HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();  
 		String remoteIP = httpServletRequest.getRemoteAddr();		
 		
 		log.debug("Entering IP="+remoteIP);
 		
//		if(IP_LOCALHOST.equals(remoteIP)) {
//			page = PAGE_DJ;
//		} else {
 			page = PAGE_USERS;
//		}
 		
 		log.debug("forwarding to "+page);
 		return page;
 	}
 }
