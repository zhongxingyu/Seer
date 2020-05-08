 package com.wideplay.warp.rendering;
 
 import com.wideplay.warp.module.WarpModuleAssembly;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Created with IntelliJ IDEA.
  * On: 17/03/2007
  *
  * @author Dhanji R. Prasanna
  * @since 1.0
  */
 public class TemplatingFilter {
     private final WarpModuleAssembly assembly;
     private final ServletContext context;
 
     private final Log log = LogFactory.getLog(getClass());
 
     public TemplatingFilter(WarpModuleAssembly assembly, ServletContext context) {
         this.assembly = assembly;
         this.context = context;
     }
 
     public boolean doFilter(HttpServletRequest request, HttpServletResponse response) {
         //first check if this is a forwardOrRedirect action (and use the flash-scoped "next" page then)
         Object page = request.getSession().getAttribute(PageRedirector.WARP_REDIRECTED_NEXT_PAGE_OBJECT);
 
         //if it is, clear it
         request.getSession().setAttribute(PageRedirector.WARP_REDIRECTED_NEXT_PAGE_OBJECT, null);
 
 
         //locate page handler from uri and handle page
        PageHandler handler = assembly.getUserFacingPage(request.getRequestURI().substring(request.getContextPath().length()));
 
         //render normally?, i.e. thru servlet
         if (null == handler)
             return false;
 
         log.trace("filter active for page: " + request.getRequestURI() + " ; handled by: " + handler);
 
 
         //locate template and render to response
         response.setContentType("text/html");
 
 
         //get event id and dispatch with the appropriate page object
         Object forward = handler.handleRequest(request, response, assembly.getInjector(), request.getParameter(RequestBinder.EVENT_PARAMETER_NAME), page);
 
         //do forward if necessary (really a client-side redirect)
         if (null != forward)
             PageRedirector.forwardOrRedirect(forward, response, request, assembly);
 
         return true;
     }
 
 
 
 
 }
