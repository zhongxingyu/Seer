 package com.WebQ.interceptors;
 
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.WebQ.beans.User;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionInvocation;
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.interceptor.Interceptor;
 
 public class AuthenticationInterceptor implements Interceptor {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     @Override
     public void destroy() {
 	// TODO Auto-generated method stub
 
     }
 
     @Override
     public void init() {
 	// TODO Auto-generated method stub
 
     }
 
     @Override
     public String intercept(ActionInvocation actionInvocation) {
 	try {
 	    ActionContext context = actionInvocation.getInvocationContext();
 	    Map<String, Object> session = context.getSession();
 
 	    User user = (User) session.get("user");
 	    if (user == null) {
 		return ActionSupport.LOGIN;
 	    }
 
	    // HttpServletResponse response = (HttpServletResponse) context
	    // .get(StrutsStatics.HTTP_REQUEST);
	    //
	    // if (response != null) {
	    // response.setHeader("Pragma", "no-cache");
	    // response.setHeader("Cache-Control", "no-cache");
	    // response.setHeader("Expires", "0");
	    // }

 	    return actionInvocation.invoke();
 	} catch (Exception e) {
 	    Logger.getLogger(AuthenticationInterceptor.class).error(
 		    e.toString());
 	}
 	return null;
     }
 
 }
