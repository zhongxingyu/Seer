 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.impl.event;
 
 import org.icefaces.impl.application.AuxUploadSetup;
 import org.icefaces.impl.application.LazyPushManager;
 import org.icefaces.impl.application.WindowScopeManager;
 import org.icefaces.impl.push.SessionViewManager;
 import org.icefaces.impl.push.servlet.ICEpushResourceHandler;
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.application.ProjectStage;
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceHandler;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.*;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class BridgeSetup implements SystemEventListener {
     public final static String ViewState = BridgeSetup.class.getName() + "::ViewState";
     public final static String BRIDGE_SETUP = BridgeSetup.class.getName();
     public final static String ICE_CORE_LIB = "ice.core";
     public final static String ICE_PUSH_LIB = "ice.push";
     private final static Logger log = Logger.getLogger(BridgeSetup.class.getName());
 
     private final boolean standardFormSerialization;
     private final boolean reloadOnUpdateFailure;
     private final boolean deltaSubmit;
     private final boolean focusManaged;
     private int seed = 0;
 
     public BridgeSetup() {
         FacesContext fc = FacesContext.getCurrentInstance();
         reloadOnUpdateFailure = EnvUtils.reloadOnUpdateFailure(fc);
         deltaSubmit = EnvUtils.isDeltaSubmit(fc);
         focusManaged = EnvUtils.isFocusManaged(fc);
         standardFormSerialization = EnvUtils.isStandardFormSerialization(fc);
         fc.getExternalContext().getApplicationMap().put(BRIDGE_SETUP, this);
     }
 
     public boolean isListenerForSource(final Object source) {
         if (!(source instanceof UIViewRoot)) {
             return false;
         }
         FacesContext facesContext = FacesContext.getCurrentInstance();
         if (!EnvUtils.isICEfacesView(facesContext)) {
             // If ICEfaces is not configured for this view, we don't need to process this event.
             return false;
         }
         if (!EnvUtils.hasHeadAndBodyComponents(facesContext)) {
             // If ICEfaces is configured for this view, but the h:head and/or h:body components
             // are not available, we cannot process it but we log the reason.
             if (log.isLoggable(Level.WARNING)) {
                 log.log(Level.WARNING, "ICEfaces configured for view " + facesContext.getViewRoot().getViewId() +
                         " but h:head and h:body components are required");
             }
             return false;
         }
         return true;
     }
 
     public void processEvent(SystemEvent event) throws AbortProcessingException {
         FacesContext context = FacesContext.getCurrentInstance();
         UIViewRoot root = context.getViewRoot();
 
         List<UIComponent> headResources = getHeadResources(context);
         for (UIComponent headResource : headResources) {
             root.addComponentResource(context, headResource, "head");
         }
 
         List<UIComponent> bodyResources = getBodyResources(context);
         for (UIComponent bodyResource : bodyResources) {
             root.addComponentResource(context, bodyResource, "body");
         }
     }
 
     /**
      * Return the current BridgeSetup instance for use in non-body contexts.
      *
      * @return current BridgeSetup instance
      */
     public static BridgeSetup getBridgeSetup(FacesContext facesContext) {
         return (BridgeSetup) facesContext.getExternalContext().
                 getApplicationMap().get(BRIDGE_SETUP);
     }
 
     private List<UIComponent> getHeadResources(FacesContext context) {
         ArrayList<UIComponent> resources = new ArrayList();
 
 //        //always add bridge support code
 //        resources.add(ResourceOutputUtil.createTransientScriptResourceComponent("bridge-support.js", ICE_CORE_LIB));
 //        //add ICEpush code only when needed
 //        if (EnvUtils.isICEpushPresent()) {
 //            resources.add(ResourceOutputUtil.createTransientScriptResourceComponent("icepush.js", ICE_PUSH_LIB));
 //        }
 //        //always add ICEfaces bridge code
 //        resources.add(ResourceOutputUtil.createTransientScriptResourceComponent("bridge.js", ICE_CORE_LIB));
         resources.add(new TestScript());
         resources.add(new CoreCSSOutput());
 
         return resources;
     }
 
     public List<UIComponent> getBodyResources(FacesContext context) {
         final ExternalContext externalContext = context.getExternalContext();
         UIViewRoot root = context.getViewRoot();
         List<UIComponent> bodyResources = new ArrayList();
         try {
             String tempWindowID = "unknownWindow";
             final WindowScopeManager.ScopeMap windowScope =
                     WindowScopeManager.lookupWindowScope(context);
             if (null != windowScope) {
                 tempWindowID = windowScope.getId();
             } else {
                 log.log(Level.WARNING, "Unable to find WindowScope for view " +
                         context.getViewRoot().getViewId());
             }
             final String windowID = tempWindowID;
             final String viewID = getViewID(externalContext);
 
             final Map viewScope = root.getViewMap();
             UIOutput icefacesSetup = new UIOutputWriter() {
                 public void encode(ResponseWriter writer, FacesContext context) throws IOException {
                     //determine if windowScope map contains any beans during render phase -- this is when is certain
                     //that the beans were already instantiated
                     boolean sendDisposeWindow = !EnvUtils.isLazyWindowScope(context) ||
                             (windowScope != null && EnvUtils.containsBeans(windowScope)) || (viewScope != null && EnvUtils.containsDisposedBeans(viewScope));
 
                     String clientID = getClientId(context);
                     writer.startElement("span", this);
                     writer.writeAttribute("id", clientID, null);
                     writer.startElement("script", this);
                     //define bridge configuration
                     writer.write("ice.setupBridge('");
                     writer.write(clientID);
                     writer.write("', '");
                     writer.write(viewID);
                     writer.write("', '");
                     writer.write(windowID);
                     writer.write("', {");
                     writer.write("reloadOnUpdateFailure: ");
                     writer.write(Boolean.toString(reloadOnUpdateFailure));
                     writer.write(",");
                     writer.write("deltaSubmit: ");
                     writer.write(Boolean.toString(deltaSubmit));
                     writer.write(",");
                     writer.write("focusManaged: ");
                     writer.write(Boolean.toString(focusManaged));
                     writer.write(",");
                     writer.write("disableDefaultErrorPopups: ");
                     writer.write(Boolean.toString(EnvUtils.disableDefaultErrorPopups(context)));
                     writer.write(",");
                     writer.write("standardFormSerialization: ");
                     writer.write(Boolean.toString(standardFormSerialization));
                     writer.write(",");
                     writer.write("sendDisposeWindow: ");
                     writer.write(Boolean.toString(sendDisposeWindow));
                     writer.write(",");
                     writer.write("blockUIOnSubmit: ");
                     writer.write(Boolean.toString(EnvUtils.isBlockUIOnSubmit(context)));
                     writer.write(",");
                     writer.write("clientSideElementUpdateDetermination: ");
                     writer.write(Boolean.toString(EnvUtils.isClientSideElementUpdateDetermination(context)));
                     writer.write("});");
                     writer.endElement("script");
                     writer.endElement("span");
                 }
             };
             Map attributes = icefacesSetup.getAttributes();
             attributes.put("name", "ICEfacesSetup");
             attributes.put("library", "ice.core");
             icefacesSetup.setTransient(true);
             icefacesSetup.setId(viewID + "_icefaces_config");
             bodyResources.add(icefacesSetup);
 
 
             if (EnvUtils.isICEpushPresent()) {
                 UIOutputWriter icepushSetup = new UIOutputWriter() {
                     public void encode(ResponseWriter writer,
                                        FacesContext context) throws
                             IOException {
                         SessionViewManager.addView(context, viewID);
                         //need a span to make sure JSF bridge evaluates included script properly
                         writer.startElement("span", this);
                         writer.writeAttribute("id", this.getClientId(context), null);
                         writer.startElement("script", this);
                         writer.writeAttribute("type", "text/javascript", null);
                         writer.write(LazyPushManager.enablePush(context, viewID) ? "ice.setupPush('" + viewID + "');" : "ice.unsetupPush('" + viewID + "');");
                         ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
                         Resource blockingConnectionResource = resourceHandler.createResource(ICEpushResourceHandler.BLOCKING_CONNECTION_RESOURCE_NAME, null, "text/xml");
                         Resource createPushIdResource = resourceHandler.createResource(ICEpushResourceHandler.CREATE_PUSH_ID_RESOURCE_NAME, null, "text/plain");
                         Resource notifyResource = resourceHandler.createResource(ICEpushResourceHandler.NOTIFY_RESOURCE_NAME, null, "text/plain");
                         Resource addGroupMemberResource = resourceHandler.createResource(ICEpushResourceHandler.ADD_GROUP_MEMBER_RESOURCE_NAME, null, "text/plain");
                         Resource removeGroupMemberResource = resourceHandler.createResource(ICEpushResourceHandler.REMOVE_GROUP_MEMBER_RESOURCE_NAME, null, "text/plain");
 
                         boolean isPortalEnvironment = EnvUtils.instanceofPortletRequest(externalContext.getRequest());
                         String contextPath = isPortalEnvironment ? "/" : externalContext.getRequestContextPath();
                         writer.write("ice.push.configuration.contextPath=\"" + contextPath + "\";");
                         writer.write("ice.push.configuration.blockingConnectionURI=\"" + blockingConnectionResource.getRequestPath() + "\";");
                         writer.write("ice.push.configuration.createPushIdURI=\"" + createPushIdResource.getRequestPath() + "\";");
                         writer.write("ice.push.configuration.notifyURI=\"" + notifyResource.getRequestPath() + "\";");
                         writer.write("ice.push.configuration.addGroupMemberURI=\"" + addGroupMemberResource.getRequestPath() + "\";");
                         writer.write("ice.push.configuration.removeGroupMemberURI=\"" + removeGroupMemberResource.getRequestPath() + "\";");
                         boolean isAuxUpload =
                                 EnvUtils.isAuxUploadBrowser(context);
                         if (isAuxUpload) {
                             AuxUploadSetup auxUpload =
                                     AuxUploadSetup.getInstance();
                             String cloudPushId = auxUpload.getCloudPushId();
                             if (null != cloudPushId) {
                                 writer.write(
                                         "window.addEventListener('load', function() { ice.push.parkInactivePushIds('"
                                                 + cloudPushId + "'); }, false);");
                             }
                         }
                         writer.endElement("script");
                         writer.endElement("span");
                     }
                 };
                 icepushSetup.setTransient(true);
                 icepushSetup.setId(viewID + "_icepush");
                 bodyResources.add(icepushSetup);
             }
         } catch (Exception e) {
             //could re-throw as a FacesException, but WindowScope failure should
             //not be fatal to the application
             log.log(Level.WARNING, "Failed to generate JS bridge setup.", e);
         }
         return bodyResources;
     }
 
     private static String assignViewID(ExternalContext externalContext) {
         final String viewIDParameter = externalContext.getRequestParameterMap().get("ice.view");
         //keep viewID sticky until page is unloaded
         BridgeSetup bridgeSetup = (BridgeSetup) externalContext.getApplicationMap().get(BRIDGE_SETUP);
         final String viewID = viewIDParameter == null ? bridgeSetup.generateViewID() : viewIDParameter;
         //save the calculated view state key so that other parts of the framework will use the same key
         externalContext.getRequestMap().put(ViewState, viewID);
         return viewID;
     }
 
     private String generateViewID() {
         return "v" + Integer.toString(hashCode(), 36) + Integer.toString(++seed, 36);
     }
 
     /**
      * This is only valid after a postback, or during or after rendering in
      * the initial page get.
      *
      * @return The view id
      */
     public static String getViewID(ExternalContext externalContext) {
         Map requestMap = externalContext.getRequestMap();
         return (String) requestMap.get(BridgeSetup.ViewState);
     }
 
     public static class AssignViewID implements PhaseListener {
         public void afterPhase(PhaseEvent event) {
         }
 
         //assign viewId as soon as possible
         public void beforePhase(PhaseEvent event) {
             assignViewID(event.getFacesContext().getExternalContext());
         }
 
         public PhaseId getPhaseId() {
             return PhaseId.RESTORE_VIEW;
         }
     }
 
     private static class TestScript extends UIOutput {
         private TestScript() {
             setTransient(true);
            getAttributes().put("name", "TestScript");
             getAttributes().put("ice.type", "dynamic");
         }
 
         public void encodeBegin(FacesContext context) throws IOException {
             ResponseWriter writer = context.getResponseWriter();
             writer.startElement("script", this);
             writer.writeAttribute("type", "text/javascript", null);
             writer.write("document.documentElement.isHeadUpdateSuccessful=true;");
             writer.endElement("script");
         }
     }
 
     private static class CoreCSSOutput extends UIOutputWriter {
         private CoreCSSOutput() {
             setTransient(true);
            getAttributes().put("name", "CoreCSSOutput");
             getAttributes().put("ice.type", "dynamic");
         }
 
         public void encode(ResponseWriter writer, FacesContext context) throws IOException {
             writer.startElement("style", this);
             writer.writeAttribute("type", "text/css", null);
             writer.writeText(".ice-blockui-overlay {", null);
             writer.writeText("position: absolute;", null);
             writer.writeText("background-color: white;", null);
             writer.writeText("z-index: 28000;", null);
             writer.writeText("opacity: 0.22;", null);
             writer.writeText("filter: alpha(opacity = 22);", null);
             writer.writeText("}", null);
             writer.writeText(".ice-status-indicator-overlay {", null);
             writer.writeText("position: absolute;", null);
             writer.writeText("background-color: white;", null);
             writer.writeText("z-index: 28000;", null);
             writer.writeText("opacity: 0.22;", null);
             writer.writeText("filter: alpha(opacity = 22);", null);
             writer.writeText("}", null);
             writer.endElement("style");
         }
     }
 }
