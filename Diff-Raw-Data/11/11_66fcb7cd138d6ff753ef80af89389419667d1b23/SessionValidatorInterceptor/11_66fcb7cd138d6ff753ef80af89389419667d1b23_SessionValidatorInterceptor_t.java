 package com.photon.phresco.service.admin.actions.interceptors;
 
 import com.opensymphony.xwork2.ActionInvocation;
 import com.opensymphony.xwork2.interceptor.Interceptor;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 
 public class SessionValidatorInterceptor extends ServiceBaseAction implements Interceptor  {
 	
 	private static final long serialVersionUID = 1L;
 
 	public void destroy() {  
     }  
   
     public void init() {  
     }
 	    
 	public String intercept(ActionInvocation invocation) throws Exception {
 		User user = (User) getHttpSession().getAttribute(SESSION_USER_INFO);
 		if (user == null) {
			setSessionAttribute(REQ_LOGIN_ERROR, getText(KEY_I18N_SESSION_EXPIRED));
 			return SUCCESS;
 		}
 		
 		return invocation.invoke();
 	}
 }
