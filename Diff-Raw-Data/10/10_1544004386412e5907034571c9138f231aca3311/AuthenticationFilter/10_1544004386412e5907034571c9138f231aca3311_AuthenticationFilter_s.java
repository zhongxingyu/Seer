 /**
  * 
  * $Id: AuthenticationFilter.java,v 1.7 2008-08-14 17:12:21 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.6  2006/11/09 17:33:19  pandyas
  * Commented out debug code
  *
  * Revision 1.5  2006/04/17 19:10:50  pandyas
  * Added $Id: AuthenticationFilter.java,v 1.7 2008-08-14 17:12:21 pandyas Exp $ and $log:$
  *
  * 
  */
 
 
 package gov.nih.nci.camod.util;
 
 import gov.nih.nci.camod.Constants;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class AuthenticationFilter implements Filter {
 
 
     private FilterConfig filterConfig;
 
     /**
      * Called by the web container to indicate to a filter that it is being
      * placed into service.
      */
     public void init(FilterConfig filterConfig) throws ServletException {
     	//System.out.println("AuthenticationFilter.init");
         this.filterConfig = filterConfig;
         
     }
 
     /**
      * The doFilter method of the Filter is called by the web container each time a
      * request/response pair is passed through the chain due to a client request
      * for a resource at the end of the chain.
      */
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
             ServletException {
     	//System.out.println("AuthenticationFilter.doFilter");
 
         boolean authorized = false;
         if (request instanceof HttpServletRequest) {
 	    	String isloginpage = ((HttpServletRequest) request).getRequestURI();
 	        boolean isRequestedSessionIdFromURL = ((HttpServletRequest) request).isRequestedSessionIdFromURL();  
         
        	if(isloginpage!=null && !isRequestedSessionIdFromURL &&( 
         			isloginpage.endsWith("/ReturnUserModels.do") ||
         			isloginpage.endsWith("/camod/")
        			))	{
         		//System.out.println("AuthenticationFilter.doFilter /ReturnUserModels.do loop ");
         		//just continue, so they can login
         		generateNewSession((HttpServletRequest) request);
         		chain.doFilter(request, response);
                 return;
         	}          	
         
         	//check login for caMOD
             HttpSession session = ((HttpServletRequest) request).getSession(false);
             if (session != null && !isRequestedSessionIdFromURL){
             	//System.out.println("AuthenticationFilter session != null && !isRequestedSessionIdFromURL loop: " );
 	            String theUsername = (String) session.getAttribute(Constants.CURRENTUSER);         
 	            if(theUsername != null && theUsername.length() >0 ){
 	            	//System.out.println("AuthenticationFilter set authorized = true: " );
 	                	authorized = true;
 	            }
             }
         }
         
         if (authorized) {
         	System.out.println("AuthenticationFilter.doFilter: user is authorized");
             chain.doFilter(request, response);
             return;
         } else if (filterConfig != null) {
         	// redirect to login.jsp from any unauthorized pages (external bookmarks to secure pages, ect)
             String unauthorizedPage = filterConfig.getInitParameter("unauthorizedPage");            
             if (unauthorizedPage != null && !"".equals(unauthorizedPage)) {
             	generateNewSession((HttpServletRequest) request);
             	chain.doFilter(request, response);             
                 return; 
             }             
         }
         throw new ServletException("Unauthorized access, unable to forward to login page");
     }
     
     private void generateNewSession(HttpServletRequest httpRequest){
     	//System.out.println("AuthenticationFilter generateNewSession enter");
    	 HttpSession session = httpRequest.getSession();
    	 	//System.out.println("AuthenticationFilter generateNewSession session_id  " + session.getId());
         HashMap<String, Object> old = new HashMap<String, Object>();
         Enumeration<String> keys = (Enumeration<String>) session.getAttributeNames();
         while (keys.hasMoreElements()) {
           String key = keys.nextElement();
           old.put(key, session.getAttribute(key));
         }
         //session invalidated 
         session.invalidate();
         
         // get new session
         session = httpRequest.getSession(true);
         for (Map.Entry<String, Object> entry : old.entrySet()) {
           session.setAttribute(entry.getKey(), entry.getValue());
         }
         //System.out.println("AuthenticationFilter generateNewSession new (reused) session_id  " + session.getId());
         System.out.println("AuthenticationFilter: generated new session. ");
         
    } 
     
     /**
      * Called by the web container to indicate to a filter that it is being
      * taken out of service.
      */
     public void destroy() {
         filterConfig = null;
     }
 }
