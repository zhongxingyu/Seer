 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
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
 package org.seasar.teeda.core.application;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.application.StateManager;
 import javax.faces.application.ViewHandler;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.render.RenderKitFactory;
 import javax.portlet.PortletURL;
 import javax.portlet.RenderResponse;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.jstl.core.Config;
 
 import org.seasar.framework.util.AssertionUtil;
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.config.webapp.element.ServletMappingElement;
 import org.seasar.teeda.core.config.webapp.element.WebappConfig;
 import org.seasar.teeda.core.portlet.FacesPortlet;
 import org.seasar.teeda.core.util.PortletUtil;
 import org.seasar.teeda.core.util.ServletExternalContextUtil;
 import org.seasar.teeda.core.util.StateManagerUtil;
 import org.seasar.teeda.core.util.WebappConfigUtil;
 
 /**
  * @author higa
  * @author shot
  */
 public class ViewHandlerImpl extends ViewHandler {
 
     public ViewHandlerImpl() {
     }
 
     public Locale calculateLocale(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         Locale supportedLocale = getLocaleFromSupportedLocales(context);
         if (supportedLocale != null) {
             return supportedLocale;
         }
         Locale defaultLocale = getLocaleFromDefaultLocale(context);
         if (defaultLocale != null) {
             return defaultLocale;
         }
         return Locale.getDefault();
     }
 
     protected Locale getLocaleFromSupportedLocales(FacesContext context) {
         Application app = context.getApplication();
         for (Iterator locales = context.getExternalContext()
                 .getRequestLocales(); locales.hasNext();) {
             Locale locale = (Locale) locales.next();
             for (Iterator supportedLocales = app.getSupportedLocales(); supportedLocales
                     .hasNext();) {
                 Locale supportedLocale = (Locale) supportedLocales.next();
                 if (isMatchLocale(locale, supportedLocale)) {
                     return supportedLocale;
                 }
             }
         }
         return null;
     }
 
     protected Locale getLocaleFromDefaultLocale(FacesContext context) {
         Locale defaultLocale = context.getApplication().getDefaultLocale();
         for (Iterator locales = context.getExternalContext()
                 .getRequestLocales(); locales.hasNext();) {
             Locale reqLocale = (Locale) locales.next();
             if (isMatchLocale(reqLocale, defaultLocale)) {
                 return defaultLocale;
             }
         }
         return null;
     }
 
     protected boolean isMatchLocale(Locale reqLocale, Locale jsfLocale) {
         if (reqLocale == null && jsfLocale == null) {
             return true;
         } else if (reqLocale == null || jsfLocale == null) {
             return false;
         }
         if (reqLocale.equals(jsfLocale)) {
             return true;
         }
         return reqLocale.getLanguage().equals(jsfLocale.getLanguage())
                 && StringUtil.isEmpty(jsfLocale.getCountry());
     }
 
     public String calculateRenderKitId(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         String renderKitId = context.getApplication().getDefaultRenderKitId();
         if (renderKitId == null) {
             renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
         }
         return renderKitId;
     }
 
     public UIViewRoot createView(FacesContext context, String viewId) {
         AssertionUtil.assertNotNull("context", context);
         Locale locale = null;
         String renderKitId = null;
         UIViewRoot viewRoot = context.getViewRoot();
         if (viewRoot != null) {
             locale = viewRoot.getLocale();
             renderKitId = viewRoot.getRenderKitId();
         } else {
             locale = calculateLocale(context);
             renderKitId = calculateRenderKitId(context);
         }
         viewRoot = (UIViewRoot) context.getApplication().createComponent(
                 UIViewRoot.COMPONENT_TYPE);
         viewRoot.setViewId(viewId);
         viewRoot.setLocale(locale);
         viewRoot.setRenderKitId(renderKitId);
         return viewRoot;
     }
 
     public String getActionURL(FacesContext context, String viewId) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("viewId", viewId);
 
         // PortletSupport
         if (PortletUtil.isRender(context)) {
             RenderResponse response = (RenderResponse) context
                     .getExternalContext().getResponse();
             PortletURL url = response.createActionURL();
             url.setParameter(FacesPortlet.VIEW_ID, viewId);
             return url.toString();
         }
 
         String path = getViewIdPath(context, viewId);
         if (path != null && path.startsWith("/")) {
             return context.getExternalContext().getRequestContextPath() + path;
         } else {
             return path;
         }
     }
 
     public String getResourceURL(FacesContext context, String path) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("path", path);
         if (path.startsWith("/")) {
             return context.getExternalContext().getRequestContextPath() + path;
         } else {
             return path;
         }
     }
 
     public void renderView(FacesContext context, UIViewRoot viewRoot)
             throws IOException, FacesException {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("viewRoot", viewRoot);
         ExternalContext externalContext = context.getExternalContext();
 
         // really need this? 2.5.2.2 on JSF 1.1 and 7.5.2 on JSF(confused....)
         ensureResponseLocaleSet(externalContext, viewRoot);
         ensureRequestLocaleSet(externalContext, viewRoot);
         try {
             String viewId = convertViewIdIfNeed(context);
             context.getViewRoot().setViewId(viewId);
             externalContext.dispatch(viewId);
         } finally {
             storeResponseCharacterEncoding(externalContext);
         }
     }
 
     // TODO redirect need when viewId could not be identified?
     public UIViewRoot restoreView(FacesContext context, String viewId) {
         AssertionUtil.assertNotNull("context", context);
         Application app = context.getApplication();
         String renderKitId = calculateRenderKitId(context);
         StateManager stateManager = app.getStateManager();
         return stateManager.restoreView(context, viewId, renderKitId);
     }
 
     public void writeState(FacesContext context) throws IOException {
         AssertionUtil.assertNotNull("context", context);
         if (StateManagerUtil.isSavingStateInClient(context)) {
             context.getResponseWriter().writeText(JsfConstants.STATE_MARKER,
                     null);
         }
     }
 
     protected String getViewIdPath(FacesContext context, String viewId) {
         if (!viewId.startsWith("/")) {
             throw new IllegalArgumentException();
         }
 
         // PortletSupport
         if (PortletUtil.isPortlet(context)) {
             return viewId;
         }
 
         WebappConfig webappConfig = getWebappConfig(context);
         String urlPattern = getUrlPattern(webappConfig, context);
         if (urlPattern != null) {
             if (urlPattern.startsWith("*.")) {
                 urlPattern = urlPattern.substring(1, urlPattern.length());
                 if (viewId.endsWith(urlPattern)) {
                     return viewId;
                 } else {
                     int index = viewId.lastIndexOf(".");
                     if (index >= 0) {
                         return viewId.substring(0, index) + urlPattern;
                     } else {
                         return viewId + urlPattern;
                     }
                 }
             } else {
                 if (urlPattern.endsWith("/*")) {
                     urlPattern = urlPattern.substring(0,
                             urlPattern.length() - 2);
                 }
                 return urlPattern + viewId;
             }
         } else {
             return viewId;
         }
     }
 
     protected String getUrlPattern(WebappConfig webappConfig,
             FacesContext context) {
         String servletPath = context.getExternalContext()
                 .getRequestServletPath();
         String pathInfo = context.getExternalContext().getRequestPathInfo();
         List servletMappings = webappConfig.getServletMappingElement();
         for (Iterator itr = servletMappings.iterator(); itr.hasNext();) {
             ServletMappingElement servletMapping = (ServletMappingElement) itr
                     .next();
             String urlPattern = servletMapping.getUrlPattern();
             if ((isExtensionMapping(urlPattern) && pathInfo == null)
                     || (!isExtensionMapping(urlPattern) && pathInfo != null)) {
                 if (isExtensionMapping(urlPattern)) {
                     String extension = urlPattern.substring(1, urlPattern
                             .length());
                     if (servletPath.endsWith(extension)
                             || servletPath.equals(urlPattern)) {
                         return urlPattern;
                     }
                 } else {
                     urlPattern = urlPattern.substring(0,
                             urlPattern.length() - 2);
                     if (servletPath.equals(urlPattern)) {
                         return urlPattern;
                     }
                 }
             }
         }
         return null;
     }
 
     protected String convertViewIdIfNeed(FacesContext context) {
         WebappConfig webappConfig = getWebappConfig(context);
         ExternalContext externalContext = context.getExternalContext();
         String viewId = context.getViewRoot().getViewId();
         // PortletSupport
         if (PortletUtil.isPortlet(context)) {
             return viewId;
         }
        String urlPattern = getUrlPattern(webappConfig, context);
         if (urlPattern != null && isExtensionMapping(urlPattern)) {
             String defaultSuffix = externalContext
                     .getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
             String suffix = defaultSuffix != null ? defaultSuffix
                     : ViewHandler.DEFAULT_SUFFIX;
             if (!viewId.endsWith(suffix)) {
                 int dot = viewId.lastIndexOf('.');
                 if (dot == -1) {
                     viewId = viewId + suffix;
                 } else {
                     viewId = viewId.substring(0, dot) + suffix;
                 }
             }
         }
         return viewId;
     }
 
     protected WebappConfig getWebappConfig(FacesContext context) {
         return WebappConfigUtil.getWebappConfig(context);
     }
 
     protected void ensureResponseLocaleSet(ExternalContext externalContext,
             UIViewRoot viewRoot) {
         if (externalContext.getResponse() instanceof ServletResponse) {
             ServletResponse res = (ServletResponse) externalContext
                     .getResponse();
             res.setLocale(viewRoot.getLocale());
         }
     }
 
     protected void ensureRequestLocaleSet(ExternalContext externalContext,
             UIViewRoot viewRoot) {
         //TODO Config#set(...) seems not to support Portlet API
         if (externalContext.getRequest() instanceof ServletRequest) {
             Config.set((ServletRequest) externalContext.getRequest(),
                     Config.FMT_LOCALE, viewRoot.getLocale());
         }
     }
 
     protected void storeResponseCharacterEncoding(
             ExternalContext externalContext) {
         // Portlet: RenderRequest does not have getCharacterEncoding()
         if (externalContext.getResponse() instanceof ServletResponse) {
             ServletResponse res = (ServletResponse) externalContext
                     .getResponse();
             if (ServletExternalContextUtil.isHttpServletResponse(res)) {
                 HttpServletResponse httpRes = ServletExternalContextUtil
                         .getResponse(externalContext);
                 HttpSession session = (HttpSession) externalContext
                         .getSession(false);
                 if (session != null) {
                     session.setAttribute(ViewHandler.CHARACTER_ENCODING_KEY,
                             httpRes.getCharacterEncoding());
                 }
             }
         }
     }
 
     private static boolean isExtensionMapping(String urlPattern) {
         return (urlPattern != null) && (urlPattern.startsWith("*."));
     }
 }
