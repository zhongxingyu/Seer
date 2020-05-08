 package com.wideplay.warp.internal.pages;
 
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.wideplay.warp.module.ioc.SessionWide;
 import com.wideplay.warp.module.StateManager;
 import com.wideplay.warp.rendering.*;
 import com.wideplay.warp.annotations.event.PreRender;
 import com.wideplay.warp.annotations.event.PostRender;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * On: 17/03/2007
  *
  * @author Dhanji R. Prasanna
  * @since 1.0
  */
 class PageHandlerImpl implements PageHandler {
     private PageClassReflectionImpl reflection;
     private String uri;
     private final ComponentHandler rootComponentHandler;
 
     public PageHandlerImpl(String uri, PageClassReflectionImpl reflection, ComponentHandler root) {
         this.uri = uri;
         this.reflection = reflection;
         this.rootComponentHandler = root;
     }
 
 
     public Object handleRequest(HttpServletRequest request, HttpServletResponse response, Injector injector,
                                 String eventId, Object page) {
         //obtain the page object from guice (makes sure it is properly injected) ONLY if it is not provided
         if (null == page)
             page = injector.getInstance(reflection.getPageClass());
 
 
         //place persistent fields back into the page
         StateManager stateManager = injector.getInstance(Key.get(StateManager.class, SessionWide.class));
         stateManager.injectManaged(injector, reflection, page); //move out to InjectionSupport
 
         //map request parameters back into page object (only on events)
         if (null != request.getParameter(RequestBinder.EVENT_PARAMETER_NAME))
             injector.getInstance(RequestBinder.class).bindBean(page, request.getParameterMap());
 
         Object forwardPage = null;
         //fire lifecycle method pre-render
         forwardPage = reflection.fireEvent(page, PreRender.EVENT_ID);
         if (null != forwardPage)
             return forwardPage;
 
         //fire page event handlers as necessary
         if (null != eventId)
             forwardPage = reflection.fireEvent(page, eventId);
 
         //test if we need to forward to a different page
         if (null != forwardPage)
             return forwardPage;
 
         //render template with the page as its model
         //write response to a new HtmlWriter using the component handler tree
        HtmlWriter htmlWriter = new YUIFrameHtmlWriter();
         rootComponentHandler.handleRender(htmlWriter, injector, reflection, page);
 
         //write buffered output to the response stream
         try {
             response.getWriter().write(htmlWriter.getBuffer());
         } catch (IOException e) {
             throw new PageRenderException("Error obtaining the response writer while rendering page: ", e); //TODO page name
         }
 
         //fire lifecycle post-render
         forwardPage = reflection.fireEvent(page, PostRender.EVENT_ID);
 
         //ok now reabsorb managed properties into the statemanager
         stateManager.extractAndStore(reflection, page);
 
         //everything was ok, so return forwardpage (if null it stays on same)
         return forwardPage;
     }
 
 
     public Class<?> getPageClass() {
         return reflection.getPageClass();
     }
 
     public PageClassReflectionImpl getPageClassReflection() {
         return reflection;
     }
 
 
     public ComponentHandler getRootComponentHandler() {
         return rootComponentHandler;
     }
 }
