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
     	System.out.println("AuthenticationFilter.doFilter");
 
         HttpServletRequest req = (HttpServletRequest) request;
         HttpServletResponse res = (HttpServletResponse) response;
         System.out.println("req.getServletPath()= " + req.getServletPath());
         System.out.println("Enter doFilter req.getSession().getId()= " + req.getSession().getId());
         System.out.println("Enter doFilter notloggedin= " + (String)req.getSession().getAttribute("notloggedin"));
 
         boolean authorized = false;
     	String isloginpage = ((HttpServletRequest) request).getRequestURI();
     	System.out.println("AuthenticationFilter.doFilter isloginpage= " + isloginpage);
         boolean isRequestedSessionIdFromURL = ((HttpServletRequest) request).isRequestedSessionIdFromURL();
         System.out.println("AuthenticationFilter.doFilter isRequestedSessionIdFromURL= " + isRequestedSessionIdFromURL);        
         
         if (request instanceof HttpServletRequest) {
 
         	if(isloginpage!=null && !isRequestedSessionIdFromURL &&( 
        			isloginpage.endsWith("login.do") ||
        			isloginpage.endsWith("LoginAction.do")
         			))	{
         		System.out.println("AuthenticationFilter.doFilter login.do loop ");
         		//just continue, so they can login
         		generateNewSession((HttpServletRequest) request);
         		chain.doFilter(request, response);
                 return;
         	}  
         	System.out.println("AuthenticationFilter.doFilter NOT login.do or loginMain.do ");        	
         
         	//check login for caMOD
             HttpSession session = ((HttpServletRequest) request).getSession(false);
             System.out.println("AuthenticationFilter.doFilter session= " + session);
             if (session != null && !isRequestedSessionIdFromURL){
 	            String loggedin = (String)session.getAttribute("loggedin");
 	            System.out.println("AuthenticationFilter loggedin= " + loggedin);
 	            // reverse this property in application when this code works
 	            if(loggedin != null && loggedin.equals("true")){
 	            	System.out.println("AuthenticationFilter set authorized = true: " );
 	                	authorized = true;
 	            }
             }
         }
         
         if (authorized) {
         	System.out.println("AuthenticationFilter.doFilter authorized loop");
        	generateNewSession((HttpServletRequest) request);
             chain.doFilter(request, response);
             return;
         } else if (filterConfig != null) {
         	// redirect to login.jsp from any unauthorized pages (external bookmarks to secure pages, ect)
             String unauthorizedPage = filterConfig.getInitParameter("unauthorizedPage");
             System.out.println("AuthenticationFilter.doFilter not authorized loop unauthorizedPage= " + unauthorizedPage);
             
             if (unauthorizedPage != null && !"".equals(unauthorizedPage)) {
             	//System.out.println("unauthorizedPage != null && !.equals(unauthorizedPage) loop: ");
             	generateNewSession((HttpServletRequest) request);
             	System.out.println("Generated new session for request ");
             	//chain.doFilter(request, response); 
             	chain.doFilter(request, response);             
                 return; 
             } 
             
         }
     }
     
     private void generateNewSession(HttpServletRequest httpRequest){
     	System.out.println("AuthenticationFilter generateNewSession enter");
    	 HttpSession session = httpRequest.getSession();
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
         System.out.println("AuthenticationFilter generateNewSession exit");
         
    } 
     
     /**
      * Called by the web container to indicate to a filter that it is being
      * taken out of service.
      */
     public void destroy() {
         filterConfig = null;
     }
 }
