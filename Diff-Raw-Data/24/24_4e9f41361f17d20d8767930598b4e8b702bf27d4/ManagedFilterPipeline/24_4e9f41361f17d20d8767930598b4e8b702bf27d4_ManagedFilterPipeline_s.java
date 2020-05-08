 package com.wideplay.warp.servlet;
 
 import com.google.inject.Injector;
 import com.google.inject.Singleton;
 import net.jcip.annotations.Immutable;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dhanji
  * Date: Dec 19, 2007
  * Time: 5:42:41 PM
  *
  * <p>
  *
  * Central routing/dispatch class handles lifecycle of managed filters.
  * </p>
  *
  * @author Dhanji R. Prasanna (dhanji gmail com)
  */
@Immutable
@Singleton
 class ManagedFilterPipeline {
     private final List<FilterDefinition> filterDefinitions;
 
     public ManagedFilterPipeline(List<FilterDefinition> filterDefinitions) {
         this.filterDefinitions = Collections.unmodifiableList(filterDefinitions);
     }
 
     public void initPipeline(ServletContext servletContext, Injector injector) throws ServletException {
         //go down chain and initialize all our filters
         for (FilterDefinition filterDefinition : filterDefinitions) {
             filterDefinition.init(servletContext, injector);
         }
 
         //initialize servlets...
         injector.getInstance(ManagedServletPipeline.class)
                 .init(servletContext, injector);
     }
 
     public void dispatch(Injector injector, ServletRequest request, ServletResponse response, FilterChain proceedingFilterChain)
             throws IOException, ServletException {
 
         //invoke over the filter/servlet pipeline with the given request/response
         final ManagedServletPipeline servletPipeline = injector.getInstance(ManagedServletPipeline.class);
         new FilterChainInvocation(filterDefinitions, servletPipeline, proceedingFilterChain, injector)
                 .doFilter(withDispatcher(request, servletPipeline, injector), response);
 
     }
 
     /**
      * Used to create an proxy that dispatches either to the warp-servlet pipeline or the regular pipeline based on
      * uri-path match. 
      */
     @SuppressWarnings("JavaDoc")
     private ServletRequest withDispatcher(ServletRequest request, final ManagedServletPipeline servletPipeline,
                                           final Injector injector) {
         return new HttpServletRequestWrapper((HttpServletRequest) request) {
 
            @Override
             public RequestDispatcher getRequestDispatcher(String path) {
                 final RequestDispatcher dispatcher = servletPipeline.getRequestDispatcher(path, injector);
 
                 return (null != dispatcher) ? dispatcher : super.getRequestDispatcher(path);
             }
 
             
         };
     }
 
     public void destroyPipeline(Injector injector) {
         //destroy servlets first
         injector.getInstance(ManagedServletPipeline.class)
                 .destroy(injector);
 
         //go down chain and destroy all our filters
         for (FilterDefinition filterDefinition : filterDefinitions) {
             filterDefinition.destroy(injector);
         }
     }
 }
