 /*
  * Copyright (c) 2009, Jason Lee <jason@steeplesoft.com>
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice,
  *       this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice,
  *       this list of conditions and the following disclaimer in the documentation
  *       and/or other materials provided with the distribution.
  *     * Neither the name of the <ORGANIZATION> nor the names of its contributors
  *       may be used to endorse or promote products derived from this software
  *       without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.steeplesoft.jsf.facestester.context.mojarra;
 
 import com.steeplesoft.jsf.facestester.context.*;
 import com.steeplesoft.jsf.facestester.*;
 import com.steeplesoft.jsf.facestester.servlet.impl.FacesTesterServletContext;
 import com.steeplesoft.jsf.facestester.servlet.WebDeploymentDescriptor;
 import com.steeplesoft.jsf.facestester.servlet.impl.FacesTesterHttpServletRequest;
 import com.steeplesoft.jsf.facestester.servlet.impl.FacesTesterHttpServletResponse;
 import com.sun.faces.config.ConfigureListener;
 import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
 import java.util.EventListener;
 import java.util.List;
 import javax.faces.FacesException;
 
 import javax.faces.FactoryFinder;
 import static javax.faces.FactoryFinder.FACES_CONTEXT_FACTORY;
 import javax.faces.context.FacesContext;
 import javax.faces.context.FacesContextFactory;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.http.HttpSessionEvent;
 import java.util.Map;
 import javax.servlet.ServletContextListener;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionListener;
 
 public class MojarraFacesContextBuilder implements FacesContextBuilder {
 
 //    private static boolean initialized = false;
 //    private final ConfigureListener mojarraListener = new ConfigureListener();
     private FacesContextFactory facesContextFactory;
     private HttpSession session;
     private FacesTesterServletContext servletContext;
     private WebDeploymentDescriptor webDescriptor;
 
     public MojarraFacesContextBuilder(FacesTesterServletContext servletContext, HttpSession session, WebDeploymentDescriptor webDescriptor) {
         // TODO: Should not have to do this :(
        System.setProperty("com.sun.faces.InjectionProvider", "com.steeplesoft.jsf.facestester.injection.FacesTesterInjectionProvider");
         try {
             Class.forName("com.sun.faces.spi.AnnotationProvider");
             //Util.getLogger().info("This appears to be a Mojarra 2 environment.  Enabling AnnotationProvider.");
 //            System.setProperty("com.sun.faces.spi.annotationprovider", "com.steeplesoft.jsf.facestester.context.mojarra.FacesTesterAnnotationScanner");
         } catch (ClassNotFoundException ex) {
             //
         }
 
         this.servletContext = servletContext;
         this.session = session;
         this.webDescriptor = webDescriptor;
 
         try {
             if (Util.isMojarra()) {
                 webDescriptor.getListeners().add(0, new ConfigureListener());
             }
         } catch (Exception ex) {
             throw new FacesTesterException("Mojarra's ConfigureListener was found, but could not be instantiated: " + ex.getLocalizedMessage(), ex);
         }
 
         servletContext.addInitParameter(WebContextInitParameter.ExpressionFactory.getQualifiedName(),
                 WebContextInitParameter.ExpressionFactory.getDefaultValue());
 
         initializeFaces(servletContext);
         facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory(FACES_CONTEXT_FACTORY);
     }
 
     public FacesContext createFacesContext(String method, FacesLifecycle lifecycle) {
 
         return createFacesContext(null, method, lifecycle);
     }
 
     public FacesContext createFacesContext(String uri, String method, FacesLifecycle lifecycle) {
         return buildFacesContext(mockServletRequest(uri, method), lifecycle);
     }
 
     public FacesContext createFacesContext(FacesForm form, FacesLifecycle lifecycle) {
         FacesTesterHttpServletRequest request = mockServletRequest(form.getUri(), "POST");
 
         for (Map.Entry<String, String> each : form.getParameterMap().entrySet()) {
             request.addParameter(each.getKey(), each.getValue());
         }
 
         return buildFacesContext(request, lifecycle);
     }
 
     protected FacesContext buildFacesContext(FacesTesterHttpServletRequest request, FacesLifecycle lifecycle) throws FacesException {
         FacesContext context = facesContextFactory.getFacesContext(servletContext, request,
                 new FacesTesterHttpServletResponse(), lifecycle.getUnderlyingLifecycle());
         return context;
     }
 
     /*
      * This is a pretty simple solution that will likely need to be replaced,
      * but should get us going for now.
      */
     private void addQueryParameters(FacesTesterHttpServletRequest servletRequest, String uri) {
         int qmark = uri.indexOf("?");
 
         if (qmark > -1) {
             String queryString = uri.substring(qmark + 1);
             String[] params = queryString.split("&");
 
             for (String param : params) {
                 String[] parts = param.split("=");
 
                 if (parts.length == 1) {
                     servletRequest.addParameter(parts[0], "");
                 } else {
                     servletRequest.addParameter(parts[0], parts[1]);
                 }
             }
 
             servletRequest.setQueryString(queryString);
         }
     }
 
     private void initializeFaces(ServletContext servletContext) {
         ServletContextEvent sce = new ServletContextEvent(servletContext);
         HttpSessionEvent hse = new HttpSessionEvent(session);
         List<EventListener> listeners = webDescriptor.getListeners();
 
 //        synchronized (mojarraListener) {
 //            if (!initialized) {
 //                mojarraListener.contextInitialized(sce);
 //                mojarraListener.sessionCreated(hse);
 //                initialized = true;
 //            }
 //        }
 
         for (EventListener listener : listeners) {
             if (listener instanceof ServletContextListener) {
                 ((ServletContextListener) listener).contextInitialized(sce);
             }
         }
         // Do all SCLs need to be called first?  Probably can't hurt...
         for (EventListener listener : listeners) {
             if (listener instanceof HttpSessionListener) {
                 ((HttpSessionListener) listener).sessionCreated(hse);
             }
         }
     }
 
     private FacesTesterHttpServletRequest mockServletRequest(String uri, String method) {
         FacesTesterHttpServletRequest servletRequest;
         if (uri != null) {
             servletRequest = new FacesTesterHttpServletRequest(servletContext, method,uri);
             servletRequest.setServletPath(uri);
         } else {
             servletRequest = new FacesTesterHttpServletRequest(servletContext);
         }
         servletRequest.setSession(session);
 //        mojarraListener.requestInitialized(new ServletRequestEvent(servletContext, servletRequest));
 
         if (uri != null) {
             addQueryParameters(servletRequest, uri);
         }
 
         return servletRequest;
     }
 }
