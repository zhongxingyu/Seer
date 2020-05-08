 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.html.impl;
 
 import java.io.IOException;
 
 import javax.faces.FactoryFinder;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.internal.PageContextUtil;
 import javax.faces.internal.RenderPreparableUtil;
 import javax.faces.render.RenderKitFactory;
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.PageContext;
 
 import org.seasar.framework.container.servlet.S2ContainerServlet;
 import org.seasar.framework.exception.IORuntimeException;
 import org.seasar.teeda.core.application.TeedaStateManager;
 import org.seasar.teeda.core.application.ViewHandlerImpl;
 import org.seasar.teeda.core.util.ExternalContextUtil;
 import org.seasar.teeda.core.util.PortletExternalContextUtil;
 import org.seasar.teeda.core.util.PortletUtil;
 import org.seasar.teeda.core.util.PostbackUtil;
 import org.seasar.teeda.core.util.ServletExternalContextUtil;
 import org.seasar.teeda.extension.exception.JspRuntimeException;
 import org.seasar.teeda.extension.html.HtmlSuffix;
 import org.seasar.teeda.extension.html.PageDesc;
 import org.seasar.teeda.extension.html.PageDescCache;
 import org.seasar.teeda.extension.html.PagePersistence;
 import org.seasar.teeda.extension.html.PageScopeHandler;
 import org.seasar.teeda.extension.html.SubApplicationScopeHandler;
 import org.seasar.teeda.extension.html.TagProcessor;
 import org.seasar.teeda.extension.html.TagProcessorCache;
 import org.seasar.teeda.extension.jsp.PageContextImpl;
 
 /**
  * @author higa
  * @author shot
  */
 public class HtmlViewHandler extends ViewHandlerImpl {
 
     private TagProcessorCache tagProcessorCache;
 
     private PagePersistence pagePersistence;
 
     private HtmlSuffix htmlSuffix;
 
     private TeedaStateManager stateManager;
 
     private PageDescCache pageDescCache;
 
     public static final String pageScopeHandler_BINDING = "bindingType=may";
 
     private PageScopeHandler pageScopeHandler = new PageScopeHandlerImpl();
 
     public static final String subApplicationScopeHandler_BINDING = "bindingType=may";
 
     private SubApplicationScopeHandler subApplicationScopeHandler = new SubApplicationScopeHandlerImpl();
 
     public void setTagProcessorCache(TagProcessorCache tagProcessorCache) {
         this.tagProcessorCache = tagProcessorCache;
     }
 
     public void setPagePersistence(PagePersistence pagePersistence) {
         this.pagePersistence = pagePersistence;
     }
 
     /**
      * @param htmlSuffix The htmlSuffix to set.
      */
     public void setHtmlSuffix(HtmlSuffix htmlSuffix) {
         this.htmlSuffix = htmlSuffix;
     }
 
     /**
      * @param stateManager The stateManager to set.
      */
     public void setStateManager(TeedaStateManager stateManager) {
         this.stateManager = stateManager;
     }
 
     public UIViewRoot restoreView(FacesContext context, String viewId) {
         htmlSuffix.setupSuffix(context, viewId);
         setUpRequestForExternalBinding(context, viewId);
         tagProcessorCache.updateTagProcessor(viewId);
         return super.restoreView(context, viewId);
     }
 
     protected void setUpRequestForExternalBinding(FacesContext context,
             String viewId) {
        if (viewId.equals(context.getExternalContext().getRequestServletPath())) {
             pagePersistence.restore(context, viewId);
         }
     }
 
     public UIViewRoot createView(FacesContext context, String viewId) {
         UIViewRoot viewRoot = super.createView(context, viewId);
         TagProcessor processor = tagProcessorCache.getTagProcessor(viewId);
         if (processor == null) {
             return viewRoot;
         }
         final HttpServletRequest request = prepareRequest(context);
         final HttpServletResponse response = prepareResponse(context);
         try {
             PageContext pageContext = createPageContext(request, response);
             PageContextUtil.setCurrentFacesContextAttribute(pageContext,
                     context);
             PageContextUtil.setCurrentViewRootAttribute(pageContext, viewRoot);
             processor.composeComponentTree(context, pageContext, null);
             stateManager.saveViewToServer(context, viewRoot);
         } catch (JspException e) {
             throw new JspRuntimeException(e);
         } catch (IOException e) {
             throw new IORuntimeException(e);
         }
         return viewRoot;
     }
 
     public void renderView(FacesContext context, UIViewRoot viewRoot)
             throws IOException {
         RenderPreparableUtil.encodeBeforeForComponent(context, viewRoot);
         ExternalContext externalContext = context.getExternalContext();
         String path = ExternalContextUtil.getViewId(externalContext);
         renderView(context, path);
         RenderPreparableUtil.encodeAfterForComponent(context, viewRoot);
     }
 
     protected void renderView(final FacesContext context, final String path)
             throws IOException {
         final HttpServletRequest request = prepareRequest(context);
         final HttpServletResponse response = prepareResponse(context);
         final PageContext pageContext = createPageContext(request, response);
         final TagProcessor tagProcessor = tagProcessorCache
                 .getTagProcessor(path);
         final boolean postback = PostbackUtil.isPostback(context
                 .getExternalContext().getRequestMap());
         final PageDesc pageDesc = pageDescCache.getPageDesc(path);
 
         boolean changed = false;
         if (postback) {
             changed = pageScopeHandler.toPage(pageDesc, context);
         }
         try {
             tagProcessor.process(pageContext, null);
         } catch (JspException ex) {
             throw new JspRuntimeException(ex);
         }
         subApplicationScopeHandler.toScope(pageDesc, context);
         if (!postback || changed) {
             pageScopeHandler.toScope(pageDesc, context);
         }
         pageContext.getOut().flush();
     }
 
     protected Servlet getServlet() {
         return S2ContainerServlet.getInstance();
     }
 
     protected ServletConfig getServletConfig() {
         return getServlet().getServletConfig();
     }
 
     protected ServletContext getServletContext() {
         return getServletConfig().getServletContext();
     }
 
     protected PageContext createPageContext(HttpServletRequest request,
             HttpServletResponse response) throws IOException {
         PageContextImpl pageContext = new PageContextImpl();
         pageContext.initialize(getServlet(), request, response, null);
         return pageContext;
     }
 
     protected RenderKitFactory getRenderKitFactory() {
         return (RenderKitFactory) FactoryFinder
                 .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
     }
 
     protected HttpServletRequest prepareRequest(FacesContext context) {
         final ExternalContext externalContext = context.getExternalContext();
         // PortletSupport
         if (!PortletUtil.isPortlet(context)) {
             return ServletExternalContextUtil.getRequest(externalContext);
         } else {
             return PortletExternalContextUtil
                     .wrapByHttpServletRequestWrapper(externalContext);
         }
     }
 
     protected HttpServletResponse prepareResponse(FacesContext context) {
         final ExternalContext externalContext = context.getExternalContext();
         // PortletSupport
         if (!PortletUtil.isPortlet(context)) {
             return ServletExternalContextUtil.getResponse(externalContext);
         } else {
             return PortletExternalContextUtil
                     .wrapByHttpServletResponseWrapper(externalContext);
         }
     }
 
     public PageDescCache getPageDescCache() {
         return pageDescCache;
     }
 
     public void setPageDescCache(PageDescCache pageDescCache) {
         this.pageDescCache = pageDescCache;
     }
 
     public PageScopeHandler getPageScopeHandler() {
         return pageScopeHandler;
     }
 
     public void setPageScopeHandler(PageScopeHandler pageScopeHandler) {
         this.pageScopeHandler = pageScopeHandler;
     }
 
     public SubApplicationScopeHandler getSubApplicationScopeHandler() {
         return subApplicationScopeHandler;
     }
 
     public void setSubApplicationScopeHandler(
             SubApplicationScopeHandler subApplicationScopeHandler) {
         this.subApplicationScopeHandler = subApplicationScopeHandler;
     }
 
 }
