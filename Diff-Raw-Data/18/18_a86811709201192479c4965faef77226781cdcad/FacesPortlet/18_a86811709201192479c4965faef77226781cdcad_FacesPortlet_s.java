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
 package org.seasar.teeda.core.portlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.FactoryFinder;
 import javax.faces.application.FacesMessage;
 import javax.faces.application.StateManager;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.FacesContextFactory;
 import javax.faces.internal.WebAppUtil;
 import javax.faces.internal.scope.RedirectScope;
 import javax.faces.lifecycle.Lifecycle;
 import javax.faces.lifecycle.LifecycleFactory;
 import javax.faces.webapp.FacesServlet;
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.GenericPortlet;
 import javax.portlet.PortletContext;
 import javax.portlet.PortletException;
 import javax.portlet.PortletRequest;
 import javax.portlet.PortletSession;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.servlet.ServletException;
 
 import org.seasar.framework.log.Logger;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.util.DIContainerUtil;
 import org.seasar.teeda.core.util.ErrorPageManager;
 import org.seasar.teeda.core.util.NullErrorPageManagerImpl;
 
 /**
  * @author shinsuke
  *
  */
 public class FacesPortlet extends GenericPortlet {
     /**
      * Logger for this class
      */
     private static final Logger logger = Logger.getLogger(FacesPortlet.class);
 
     public static final String PORTLET_CONFIG = "javax.portlet.PortletConfig";
 
     public static final String VIEW_ID = FacesPortlet.class.getName() +
             ".VIEW_ID";
 
     public static final String DEFAULT_PAGE = FacesPortlet.class.getName() +
             ".DEFAULT_PAGE";
 
     public static final String DEFAULT_VIEW_PAGE = "view-page";
 
     public static final String DEFAULT_EDIT_PAGE = "edit-page";
 
     public static final String DEFAULT_HELP_PAGE = "help-page";
 
     public static final String FACES_PORTLET_STATE_PREFIX = FacesPortlet.class
             .getName() +
             ".FACES_PORTLET_STATE" + "-";
 
     public static final String PREVIOUS_PORTLET_MODE = FacesPortlet.class
             .getName() +
             ".PREVIOUS_PORTLET_MODE";
 
     public static final String CURRENT_PORTLET_MODE = FacesPortlet.class
             .getName() +
             ".CURRENT_PORTLET_MODE";
 
     public static final String EXCLUDED_ATTRIBUTE_LIST = FacesPortlet.class
             .getName() +
             ".EXCLUDED_ATTRIBUTE_LIST";
 
     public static final String REDEPLOYED_PORTLET = FacesPortlet.class
             .getName() +
             ".REDEPLOYED_PORTLET";
 
     public static final String RENDER_PARAMETER = FacesPortlet.class.getName() +
             ".RENDER_PARAMETER";
 
     protected FacesContextFactory facesContextFactory;
 
     protected Lifecycle lifecycle;
 
     private String defaultViewPage = null;
 
     private String defaultEditPage = null;
 
     private String defaultHelpPage = null;
 
     public void init() throws PortletException {
         super.init();
 
         // Load default pages
         defaultViewPage = getPortletConfig()
                 .getInitParameter(DEFAULT_VIEW_PAGE);
         defaultEditPage = getPortletConfig()
                 .getInitParameter(DEFAULT_EDIT_PAGE);
         defaultHelpPage = getPortletConfig()
                 .getInitParameter(DEFAULT_HELP_PAGE);
         if (null == defaultViewPage) {
             // A Faces Portlet is required to have at least the default page
             throw new PortletException("Portlet " +
                     getPortletConfig().getPortletName() +
                     " is incorrectly configured. No default View page is defined.");
         }
         if (null == defaultEditPage) {
             defaultEditPage = defaultViewPage;
         }
         if (null == defaultHelpPage) {
             defaultHelpPage = defaultViewPage;
         }
 
         // Intialize Faces(from FacesServlet)
         LifecycleFactory lifecycleFactory;
         try {
             facesContextFactory = (FacesContextFactory) WebAppUtil
                     .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
             lifecycleFactory = (LifecycleFactory) WebAppUtil
                     .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
             String lifecycleId = getLifecycleId(getPortletContext());
             lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
         } catch (ServletException e) {
             throw new PortletException(e);
         }
     }
 
     protected String getLifecycleId(PortletContext portletContext) {
         String lifecycleId = portletContext
                 .getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
         if (lifecycleId == null) {
             lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
         }
         return lifecycleId;
     }
 
     public void destroy() {
         super.destroy();
         FactoryFinder.releaseFactories();
         facesContextFactory = null;
         lifecycle = null;
         defaultViewPage = null;
         defaultEditPage = null;
         defaultHelpPage = null;
     }
 
     //
     // Render Methods
     //
 
     protected void doEdit(RenderRequest request, RenderResponse response)
             throws PortletException, IOException {
         setContentType(request, response);
         setDefaultPage(request, defaultEditPage);
         renderFaces(request, response);
     }
 
     protected void doHelp(RenderRequest request, RenderResponse response)
             throws PortletException, IOException {
         setContentType(request, response);
         setDefaultPage(request, defaultHelpPage);
         renderFaces(request, response);
     }
 
     protected void doView(RenderRequest request, RenderResponse response)
             throws PortletException, IOException {
         setContentType(request, response);
         setDefaultPage(request, defaultViewPage);
         renderFaces(request, response);
     }
 
     protected void setContentType(RenderRequest request, RenderResponse response) {
 
         if (response.getContentType() == null) {
             String portalPreferredContentType = request
                     .getResponseContentType();
             if (portalPreferredContentType != null) {
                 response.setContentType(portalPreferredContentType);
             } else {
                 response.setContentType("text/html");
             }
         }
     }
 
     protected void setDefaultPage(PortletRequest request, String page) {
         if (request.getAttribute(DEFAULT_PAGE) == null) {
             request.setAttribute(DEFAULT_PAGE, page);
         }
     }
 
     private void storePortletConfig(PortletRequest request) {
         request.setAttribute(PORTLET_CONFIG, getPortletConfig());
     }
 
     private void setCurrentPortletMode(PortletRequest request) {
         request.setAttribute(CURRENT_PORTLET_MODE, request.getPortletMode()
                 .toString());
     }
 
     protected void renderFaces(RenderRequest request, RenderResponse response)
             throws PortletException, java.io.IOException {
         if (logger.isDebugEnabled()) {
             logger.debug("called renderFaces..");
         }
 
         // create excluded list
         createExcludedAttributeList(request);
 
         // restore error info
         restoreErrorInfo(request);
 
         storePortletConfig(request);
         setCurrentPortletMode(request);
 
         FacesContext facesContext = facesContextFactory.getFacesContext(
                 getPortletContext(), request, response, lifecycle);
         request.setAttribute(JsfConstants.FACES_CONTEXT, facesContext);
 
         String redirectPath = null;
         try {
             restoreFacesState(facesContext);
             lifecycle.render(facesContext);
             saveFacesState(facesContext);
         } catch (Throwable e) {
             handleException(facesContext, e);
         } finally {
             redirectPath = RedirectScope.getRedirectingPath(facesContext);
             if (redirectPath != null) {
                 //remove redirect scope
                 RedirectScope.removeContext(facesContext);
             }
             facesContext.release();
             request.getPortletSession().setAttribute(PREVIOUS_PORTLET_MODE,
                     request.getPortletMode().toString());
             request.getPortletSession().setAttribute(REDEPLOYED_PORTLET,
                     Boolean.FALSE);
         }
 
         // redirect
         if (redirectPath != null) {
             redirectRenderFaces(request, response, redirectPath);
         }
 
         // clear error info
         clearErrorInfo(request);
     }
 
     protected void redirectRenderFaces(RenderRequest request,
             RenderResponse response, String viewId) throws PortletException,
             java.io.IOException {
         if (logger.isDebugEnabled()) {
             logger.debug("called redirectRenderFaces.. viewId is " + viewId);
         }
 
         List excludedNameList = (List) request
                 .getAttribute(EXCLUDED_ATTRIBUTE_LIST);
         // clear attribute
         for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();) {
             String key = (String) e.nextElement();
             if (!excludedNameList.contains(key)) {
                 request.removeAttribute(key);
             }
         }
         // call renderFaces again
         request.getPortletSession().removeAttribute(
                 FacesPortlet.PREVIOUS_PORTLET_MODE);
         request.setAttribute(FacesPortlet.DEFAULT_PAGE, viewId);
         renderFaces(request, response);
 
     }
 
     protected void restoreFacesState(FacesContext facesContext) {
 
         Map sessionMap = facesContext.getExternalContext().getSessionMap();
         Map requestMap = facesContext.getExternalContext().getRequestMap();
 
         String currentPortletMode = (String) requestMap
                 .get(CURRENT_PORTLET_MODE);
         String previousPortletMode = (String) sessionMap
                 .get(PREVIOUS_PORTLET_MODE);
 
         FacesPortletState state = null;
         if (previousPortletMode != null &&
                 previousPortletMode.equals(currentPortletMode)) {
             state = (FacesPortletState) sessionMap
                     .get(FACES_PORTLET_STATE_PREFIX + currentPortletMode);
             if (state == null) {
                 // from request parameter
                 Map requestParameterMap = facesContext.getExternalContext()
                         .getRequestParameterMap();
                 String viewId = (String) requestParameterMap.get(VIEW_ID);
                 if (viewId != null && !checkSessionState(facesContext)) {
                     requestMap.put(VIEW_ID, viewId);
                 }
 
                 restoreView(facesContext);
                 return;
             } else {
                 // get viewId
                 String viewId = state.getViewId();
                 requestMap.put(VIEW_ID, viewId);
 
                 // restore view
                 restoreView(facesContext);
 
                 // restore message information
                 Iterator clientIds = state.getClientIds();
                 while (clientIds.hasNext()) {
                     String clientId = (String) clientIds.next();
                     Iterator msgs = state.getMessages(clientId);
                     while (msgs.hasNext()) {
                         facesContext.addMessage(clientId, (FacesMessage) msgs
                                 .next());
                     }
                 }
 
                 // restore request attributes
                 copyMap(state.getRequestMap(), facesContext
                         .getExternalContext().getRequestMap(), new ArrayList());
 
                 // restore state to UIViewRoot
                 Object s = state.getState();
                 if (s != null) {
                     facesContext.getViewRoot().processRestoreState(
                             facesContext, s);
                 }
 
             }
         } else {
             // display new page
             restoreDefaultFacesState(facesContext);
         }
 
     }
 
     protected void restoreDefaultFacesState(FacesContext facesContext) {
         ExternalContext externalContext = facesContext.getExternalContext();
         Map requestMap = externalContext.getRequestMap();
         // get default page
         String viewId = (String) externalContext.getRequestMap().get(
                 DEFAULT_PAGE);
         requestMap.put(VIEW_ID, viewId);
 
         // remove all states in session
         for (Iterator i = externalContext.getSessionMap().entrySet().iterator(); i
                 .hasNext();) {
             Map.Entry e = (Map.Entry) i.next();
             if (e.getKey() instanceof String) {
                 String key = (String) e.getKey();
                 if (key.startsWith(FACES_PORTLET_STATE_PREFIX)) {
                     externalContext.getSessionMap().remove(key);
                 }
             }
         }
 
         // remove redirect scope
         if (RedirectScope.isRedirecting(facesContext)) {
             RedirectScope.removeContext(facesContext);
         }
 
         restoreView(facesContext);
     }
 
     private void restoreView(FacesContext facesContext) {
         facesContext.renderResponse();
         // call restore phase only
         lifecycle.execute(facesContext);
     }
 
     //
     // Action Methods
     //
 
     public void processAction(ActionRequest request, ActionResponse response)
             throws PortletException, IOException {
         if (logger.isDebugEnabled()) {
             logger.debug("called processAction..");
         }
 
         // check session timeout
         if (checkSessionState(request)) {
             return;
         }
 
         String viewId = request.getParameter(VIEW_ID);
         if (viewId != null) {
             request.setAttribute(VIEW_ID, viewId);
             executeFaces(request, response);
         } else {
             if (logger.isDebugEnabled()) {
                 logger.debug("skip processAction because viewId is null.");
             }
         }
     }
 
     protected void executeFaces(ActionRequest request, ActionResponse response)
             throws PortletException, IOException {
 
         storePortletConfig(request);
         setCurrentPortletMode(request);
 
         // create excluded list
         createExcludedAttributeList(request);
 
         FacesContext facesContext = facesContextFactory.getFacesContext(
                 getPortletContext(), request, response, lifecycle);
 
         try {
             lifecycle.execute(facesContext);
         } catch (Throwable e) {
             handleException(facesContext, e);
         } finally {
             if (!facesContext.getResponseComplete()) {
                 request.setAttribute(VIEW_ID, facesContext.getViewRoot()
                         .getViewId());
                 saveFacesState(facesContext);
                 if (request.getParameter(RENDER_PARAMETER) != null) {
                     setRenderParameters(request, response);
                 }
             } else if (RedirectScope.isRedirecting(facesContext)) {
                 saveFacesState(facesContext);
             }
             // release the FacesContext instance for this request
             facesContext.release();
         }
     }
 
     protected void saveFacesState(FacesContext facesContext) {
         Map sessionMap = facesContext.getExternalContext().getSessionMap();
         Map requestMap = facesContext.getExternalContext().getRequestMap();
 
         // save portlet mode
         String currentPortletMode = (String) requestMap
                 .get(CURRENT_PORTLET_MODE);
         sessionMap.put(PREVIOUS_PORTLET_MODE, currentPortletMode);
 
         FacesPortletState state = new FacesPortletState();
 
        // save message information from this FacesContext
        Iterator clientIds = facesContext.getClientIdsWithMessages();
        while (clientIds.hasNext()) {
            String clientId = (String) clientIds.next();
            Iterator msgs = facesContext.getMessages(clientId);
            while (msgs.hasNext()) {
                state.addMessage(clientId, (FacesMessage) msgs.next());
             }
         }
 
         String redirectPath = RedirectScope.getRedirectingPath(facesContext);
         if (redirectPath == null) {
             state.setViewId(facesContext.getViewRoot().getViewId());
 
             List excludedNameList = (List) requestMap
                     .get(EXCLUDED_ATTRIBUTE_LIST);
             // save request attributes
             Map map = new HashMap();
             copyMap(requestMap, map, excludedNameList);
             state.setRequestMap(map);
 
             // save state
             StateManager stateManager = facesContext.getApplication()
                     .getStateManager();
             stateManager.saveSerializedView(facesContext);
 
             // store state to FacesPortletState
             state.setState(facesContext.getViewRoot().processSaveState(
                     facesContext));
         } else {
             // replace viewId
             state.setViewId(redirectPath);
 
             // save request attributes
             Map map = new HashMap();
             state.setRequestMap(map);
 
             // set state to null
             state.setState(null);
         }
 
         // store state to session
         sessionMap.put(FACES_PORTLET_STATE_PREFIX + currentPortletMode, state);
 
     }
 
     //
     // Others
     //
 
     protected void handleException(FacesContext context, Throwable e)
             throws PortletException, IOException {
         ErrorPageManager errorPageManager = null;
         try {
             errorPageManager = (ErrorPageManager) DIContainerUtil
                     .getComponent(ErrorPageManager.class);
         } catch (Exception e1) {
         }
         if (errorPageManager == null) {
             errorPageManager = new NullErrorPageManagerImpl();
         }
 
         ExternalContext extContext = context.getExternalContext();
         if (errorPageManager.handleException(e, context, extContext)) {
             context.responseComplete();
         } else {
             if (e instanceof IOException) {
                 throw (IOException) e;
             } else if (e instanceof PortletException) {
                 throw (PortletException) e;
             } else if (e.getMessage() != null) {
                 throw new PortletException(e.getMessage(), e);
             }
             throw new PortletException(e);
         }
     }
 
     protected boolean checkSessionState(PortletRequest request) {
         return request.getPortletSession(false) == null ||
                 (request.getPortletSession().getAttribute(REDEPLOYED_PORTLET) == null);
     }
 
     protected boolean checkSessionState(FacesContext context) {
         return context.getExternalContext().getSession(false) == null ||
                 (context.getExternalContext().getSessionMap().get(
                         REDEPLOYED_PORTLET) == null);
     }
 
     protected void createExcludedAttributeList(PortletRequest request) {
         // create excluded list
         ArrayList excludedNameList = new ArrayList();
         Enumeration enu = request.getAttributeNames();
         while (enu.hasMoreElements()) {
             excludedNameList.add(enu.nextElement());
         }
         // reserved names
         excludedNameList.add(EXCLUDED_ATTRIBUTE_LIST);
         excludedNameList.add(JsfConstants.FACES_CONTEXT);
         request.setAttribute(EXCLUDED_ATTRIBUTE_LIST, excludedNameList);
     }
 
     private void copyMap(Map fromMap, Map toMap, List excludedNameList) {
         for (Iterator i = fromMap.entrySet().iterator(); i.hasNext();) {
             Map.Entry e = (Map.Entry) i.next();
             if (!toMap.containsKey((String) e.getKey()) &&
                     !excludedNameList.contains((String) e.getKey())) {
                 toMap.put(e.getKey(), e.getValue());
             }
         }
     }
 
     protected void setRenderParameters(ActionRequest request,
             ActionResponse response) {
         Enumeration enu = request.getParameterNames();
         while (enu.hasMoreElements()) {
             String key = (String) enu.nextElement();
             response.setRenderParameter(key, request.getParameter(key));
         }
     }
 
     protected void restoreErrorInfo(PortletRequest request) {
         PortletSession portletSession = request.getPortletSession();
         Throwable exception = (Throwable) portletSession
                 .getAttribute(JsfConstants.ERROR_EXCEPTION);
         if (exception != null) {
             request.setAttribute(JsfConstants.ERROR_EXCEPTION, exception);
             request.setAttribute(JsfConstants.ERROR_EXCEPTION_TYPE, exception
                     .getClass());
             request.setAttribute(JsfConstants.ERROR_MESSAGE, exception
                     .getMessage());
         }
     }
 
     protected void clearErrorInfo(PortletRequest request) {
         request.getPortletSession().removeAttribute(
                 JsfConstants.ERROR_EXCEPTION);
     }
 }
