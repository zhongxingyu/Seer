 package org.microsauce.gravy.runtime;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.microsauce.gravy.context.EnterpriseService;
 import org.microsauce.gravy.context.ServletFacade;
 import org.microsauce.gravy.context.Handler;
 
 /**
  *
  */
 class RouteChain implements FilterChain {
 
     List<Handler> route;
     Integer currentPosition = 0;
     FilterChain serverChain;
     ServletFacade servletFacade;
 
     RouteChain(ServletRequest req, ServletResponse res, FilterChain serverChain, List<Handler> route, Map<String, EnterpriseService> paramPreconditions) {
         this.serverChain = serverChain;
         this.route = route;
 
        // TODO build a uri parameter map for each handler - how?
         EnterpriseService endPoint = endPoint();
         if ( endPoint != null )
             servletFacade = new ServletFacade((HttpServletRequest) req, (HttpServletResponse) res, this, endPoint.getUriPattern(), endPoint.getUriParamNames());
         else
             servletFacade = new ServletFacade((HttpServletRequest) req, (HttpServletResponse) res, this, null, null);
 
         List<Handler> paramHandlers= new ArrayList<Handler>();
         for( String uriParam : servletFacade.getUriParamMap().keySet() ) {
             EnterpriseService paramService = paramPreconditions.get(uriParam);
             if ( paramService != null ) {
                 paramHandlers.add(paramService.getHandlers().get(EnterpriseService.MIDDLEWARE));
             }
         }
         if ( paramHandlers.size() > 0 )
             route.addAll(0, paramHandlers);
     }
 
     public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
         if (currentPosition >= route.size())
             // finish up with the 'native' filter
             serverChain.doFilter(req, res);
         else {
             Handler handler = route.get(currentPosition++);
 
             if ( handler == null ) doFilter(req, res); // there may not be a 'default' handler for this route
             else {
                 try {
                     req.setAttribute("_module", handler.getModule());
                     ((HttpServletRequest) req).getSession().setAttribute("_module", handler.getModule());    // TODO is session scope necessary ???
 
                     GravyThreadLocal.SCRIPT_CONTEXT.set(handler.getModule().getScriptContext());
                     handler.execute(servletFacade);
                 } catch (Throwable t) {
                     t.printStackTrace();
                 } finally {
                     if (!res.isCommitted())
                         res.getOutputStream().flush();  // TODO review the flush here - this will preclude any other service on the chain from writing response headers
                 }
             }
         }
     }
 
     private EnterpriseService endPoint() {
         EnterpriseService endPoint = route.get(route.size()-1).getService();
 
         if ( endPoint != null && endPoint.isEndPoint() ) return endPoint;
         else return null;
     }
 
 }
