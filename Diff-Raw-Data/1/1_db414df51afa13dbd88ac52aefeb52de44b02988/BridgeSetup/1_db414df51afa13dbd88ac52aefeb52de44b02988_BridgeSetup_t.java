 /*
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations under
  * the License.
  *
  * The Original Code is ICEfaces 1.5 open source software code, released
  * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
  * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
  * 2004-2011 ICEsoft Technologies Canada, Corp. All Rights Reserved.
  *
  * Contributor(s): _____________________.
  */
 
 package org.icefaces.impl.event;
 
 import org.icefaces.impl.application.LazyPushManager;
 import org.icefaces.impl.application.WindowScopeManager;
 import org.icefaces.impl.push.SessionViewManager;
 import org.icefaces.impl.push.servlet.ICEpushResourceHandler;
 import org.icefaces.impl.renderkit.DOMRenderKit;
 import org.icefaces.render.ExternalScript;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import javax.faces.application.ResourceHandler;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIForm;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.*;
 import javax.faces.render.RenderKit;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class BridgeSetup implements SystemEventListener {
     public final static String ViewState = BridgeSetup.class.getName() + "::ViewState";
     public final static String BRIDGE_SETUP = BridgeSetup.class.getName();
     private final static Logger log = Logger.getLogger(BridgeSetup.class.getName());
     private final static String JAVAX_FACES_RESOURCE_SCRIPT = "javax.faces.resource.Script";
     private int seed = 0;
     private boolean standardFormSerialization;
     private boolean deltaSubmit;
     private boolean disableDefaultErrorPopups;
 
     public BridgeSetup() {
         FacesContext fc = FacesContext.getCurrentInstance();
         deltaSubmit = EnvUtils.isDeltaSubmit(fc);
         standardFormSerialization = EnvUtils.isStandardFormSerialization(fc);
         disableDefaultErrorPopups = EnvUtils.disableDefaultErrorPopups(fc);
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
         ExternalContext externalContext = context.getExternalContext();
         ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
         Map collectedResourceComponents = new HashMap();
         String version = EnvUtils.isUniqueResourceURLs(context) ? String.valueOf(hashCode()) : null;
         //jsf.js might be added already by a page or component
         addOrCollectReplacingResource(context, "jsf.js", "javax.faces", "head", new JavascriptResourceOutput(resourceHandler, "jsf.js", "javax.faces", version), collectedResourceComponents);
         if (EnvUtils.isICEpushPresent()) {
             root.addComponentResource(context, new JavascriptResourceOutput(resourceHandler, "icepush.js", null, version), "head");
         }
         root.addComponentResource(context, new JavascriptResourceOutput(resourceHandler, "bridge.js", null, version), "head");
 
         RenderKit rk = context.getRenderKit();
         if (rk instanceof DOMRenderKit) {
             // If the context param is not null then make sure it's true
             DOMRenderKit drk = (DOMRenderKit) rk;
             List<ExternalScript> scriptRenderers = drk.getCustomRenderScripts();
             String contextParamName;
             String value = "";
             int i = 0;
             for (ExternalScript es : scriptRenderers) {
                 i++;
                 contextParamName = es.contextParam();
                 boolean insertHere = true;
                 // If present, the context param must be true for rendering
                 // but if not present, always insert the script. Annotation default is "Null"
                 if (!contextParamName.equals("Null")) {
                     value = externalContext.getInitParameter(contextParamName);
                     insertHere = (value != null && !value.equalsIgnoreCase(""));
                 }
                 if (insertHere) {
                     UIOutput externalScript = new ReferencedScriptWriter(es.scriptURL() + value);
                     externalScript.setTransient(true);
                     String externalScriptId = "external-script-" + i;
                     externalScript.setId(externalScriptId);
                     externalScript.getAttributes().put("name", externalScriptId);
                     root.addComponentResource(context, externalScript, "head");
                 }
             }
 
             Set<ResourceDependency> addedResDeps = new HashSet<ResourceDependency>();
             List<MandatoryResourceComponent> mandatoryResourceComponents = drk.getMandatoryResourceComponents();
             String resourceConfig = EnvUtils.getMandatoryResourceConfig(context);
             //pad with spaces to allow contains checking
             String resourceConfigPad = " " + resourceConfig + " ";
             for (MandatoryResourceComponent mrc : mandatoryResourceComponents) {
                 String compClassName = mrc.value();
                 if (!"all".equalsIgnoreCase(resourceConfig)) {
                     String tagName = mrc.tagName();
                     if (!resourceConfigPad.contains(" " + compClassName + " ") &&
                             (tagName != null && tagName.length() > 0 &&
                                     !resourceConfigPad.contains(" " + tagName + " "))) {
                         continue;
                     }
                 }
                 try {
                     Class<UIComponent> compClass = (Class<UIComponent>) Class.forName(compClassName);
                     // Iterate over ResourceDependencies, ResourceDependency 
                     // annotations, creating ResourceOutput components for 
                     // each unique one, so they'll add the mandatory
                     // resources.
                     ResourceDependencies resDeps = compClass.getAnnotation(ResourceDependencies.class);
                     if (resDeps != null) {
                         for (ResourceDependency resDep : resDeps.value()) {
                             addMandatoryResourceDependency(context, root, compClassName, addedResDeps, resDep, version, collectedResourceComponents);
                         }
                     }
                     ResourceDependency resDep = compClass.getAnnotation(ResourceDependency.class);
                     if (resDep != null) {
                         addMandatoryResourceDependency(context, root, compClassName, addedResDeps, resDep, version, collectedResourceComponents);
                     }
                 } catch (Exception e) {
                     if (log.isLoggable(Level.WARNING)) {
                         log.log(Level.WARNING, "When processing mandatory " +
                                 "resource components, could not create instance " +
                                 "of '" + compClassName + "'");
                     }
                 }
             }
 

             //replace collected resource mandatory components in on shot, otherwise MyFaces will keep re-adding
             //the components registered directly by it
             replaceCollectedResourceComponents(context, "head", collectedResourceComponents);
             replaceCollectedResourceComponents(context, "body", collectedResourceComponents);
         }
 
         List<UIComponent> bodyResources = getBodyResources(context);
         for (UIComponent bodyResource : bodyResources) {
             root.addComponentResource(context, bodyResource, "body");
         }
     }
 
     private void replaceCollectedResourceComponents(FacesContext context, String target, Map collectedResourceComponents) {
         UIViewRoot root = context.getViewRoot();
         List<UIComponent> components = new ArrayList<UIComponent>(root.getComponentResources(context, target));
         for (UIComponent next : components) {
             root.removeComponentResource(context, next, target);
         }
 
         for (UIComponent next : components) {
             String name = (String) next.getAttributes().get("name");
             String library = (String) next.getAttributes().get("library");
             UIComponent c = (UIComponent) collectedResourceComponents.get(calculateKey(name, library, target));
             if (c == null) {
                 root.addComponentResource(context, next, target);
             } else {
                 root.addComponentResource(context, c, target);
             }
         }
     }
 
     private static String stripHostInfo(String uriString) {
         try {
             URI uri = URI.create(uriString);
             return (new URI(null, null, uri.getPath(), uri.getQuery(), uri.getFragment())).toString();
         } catch (URISyntaxException e) {
             return uriString;
         }
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
      * Return the current BridgeSetup instance for use in non-body contexts.
      *
      * @return current BridgeSetup instance
      */
     public static BridgeSetup getBridgeSetup(FacesContext facesContext) {
         return (BridgeSetup) facesContext.getExternalContext().
                 getApplicationMap().get(BRIDGE_SETUP);
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
             final boolean sendDisposeWindow = !EnvUtils.isLazyWindowScope(context) ||
                     (windowScope != null && EnvUtils.containsBeans(windowScope)) || (viewScope != null && EnvUtils.containsBeans(viewScope));
             UIOutput icefacesSetup = new UIOutputWriter() {
                 public void encode(ResponseWriter writer, FacesContext context) throws IOException {
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
                     writer.write("deltaSubmit: ");
                     writer.write(Boolean.toString(deltaSubmit));
                     writer.write(",");
                     writer.write("disableDefaultErrorPopups: ");
                     writer.write(Boolean.toString(disableDefaultErrorPopups));
                     writer.write(",");
                     writer.write("standardFormSerialization: ");
                     writer.write(Boolean.toString(standardFormSerialization));
                     writer.write(",");
                     writer.write("sendDisposeWindow: ");
                     writer.write(Boolean.toString(sendDisposeWindow));
                     writer.write(",");
                     writer.write("blockUIOnSubmit: ");
                     writer.write(Boolean.toString(EnvUtils.isBlockUIOnSubmit(context)));
                     writer.write("});");
                     writer.endElement("script");
                     writer.endElement("span");
                 }
             };
             icefacesSetup.setTransient(true);
             icefacesSetup.setId(viewID + "_icefaces_config");
             bodyResources.add(icefacesSetup);
 
             //make sure there's always a form so that ice.singleSubmit and ice.retrieveUpdate can do their job
             UIForm retrieveUpdateSetup = new ShortIdForm();
             retrieveUpdateSetup.setTransient(true);
             //use viewID as element ID so that ice.singleSubmit and ice.receiveUpdate can easily lookup
             //the corresponding view state key (javax.faces.ViewState) 
             retrieveUpdateSetup.setId(viewID);
             bodyResources.add(retrieveUpdateSetup);
 
             if (EnvUtils.isICEpushPresent()) {
                 UIOutputWriter icepushSetup = new UIOutputWriter() {
                     public void encode(ResponseWriter writer, FacesContext context) throws IOException {
                         SessionViewManager.addView(context, viewID);
                         //need a span to make sure JSF bridge evaluates included script properly
                         writer.startElement("span", this);
                         writer.writeAttribute("id", this.getClientId(context), null);
                         writer.startElement("script", this);
                         writer.writeAttribute("type", "text/javascript", null);
                         writer.write(LazyPushManager.enablePush(context, viewID) ? "ice.setupPush('" + viewID + "');" : "");
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
 
     private static void addMandatoryResourceDependency(
             FacesContext facesContext, UIViewRoot root, String compClassName,
             Set<ResourceDependency> addedResDeps, ResourceDependency resDep, String version, Map collectedResourceComponents) {
         if (addedResDeps.contains(resDep)) {
             return;
         }
         addedResDeps.add(resDep);
         addMandatoryResource(facesContext, root, compClassName, resDep.name(),
                 resDep.library(), version, resDep.target(), collectedResourceComponents);
     }
 
     private static void addMandatoryResource(FacesContext facesContext,
                                              UIViewRoot root, String compClassName, String name,
                                              String library, String version, String target, Map collectedResourceComponents) {
         if (target == null || target.length() == 0) {
             target = "head";
         }
 
         ResourceHandler resourceHandler = facesContext.getApplication().getResourceHandler();
         String rendererType = resourceHandler.getRendererTypeForResourceName(name);
         if (rendererType == null || rendererType.length() == 0) {
             if (log.isLoggable(Level.WARNING)) {
                 log.log(Level.WARNING, "Could not determine renderer type " +
                         "for mandatory resource, for component: " + compClassName +
                         ". Resource name: " + name + ", library: " + library);
             }
         } else {
             UIComponent component = newResourceOutput(resourceHandler, rendererType, name, library, version);
             addOrCollectReplacingResource(facesContext, name, library, target, component, collectedResourceComponents);
         }
     }
 
     public static void addOrCollectReplacingResource(FacesContext context, String name, String library, String target, UIComponent component, Map collectedResourceComponents) {
         UIViewRoot viewRoot = context.getViewRoot();
         List<UIComponent> componentResources = viewRoot.getComponentResources(context, target);
         int position = -1;
         for (int i = 0; i < componentResources.size(); i++) {
             UIComponent c = componentResources.get(i);
             Map<String, Object> attributes = c.getAttributes();
             String resourceName = (String) attributes.get("name");
             String resourceLibrary = (String) attributes.get("library");
             if (name.equals(resourceName) && library.equals(resourceLibrary)) {
                 position = i;
                 break;
             }
         }
 
         if (position > -1) {
             //collect the component resource to replace it after all mandatory resources are read
             collectedResourceComponents.put(calculateKey(name, library, target), component);
         } else {
             viewRoot.addComponentResource(context, component, target);
         }
     }
 
     private static String calculateKey(String name, String library, String target) {
         return name + ":" + library + ":" + target;
     }
 
     private static UIComponent newResourceOutput(ResourceHandler resourceHandler, String rendererType, String name, String library, String version) {
         if (JAVAX_FACES_RESOURCE_SCRIPT.endsWith(rendererType)) {
             return new JavascriptResourceOutput(resourceHandler, name, library, version);
         } else {
             return new ResourceOutput(rendererType, name, library);
         }
     }
 
     public static class ResourceOutput extends UIOutput {
         public ResourceOutput(String rendererType, String name, String library) {
             setRendererType(rendererType);
             if (name != null && name.length() > 0) {
                 getAttributes().put("name", name);
             }
             if (library != null && library.length() > 0) {
                 getAttributes().put("library", library);
             }
             setTransient(true);
         }
     }
 
     private static class ReferencedScriptWriter extends UIOutputWriter {
         protected String script;
 
         public ReferencedScriptWriter(String script) {
             super();
             this.script = script;
             this.setTransient(true);
         }
 
         public void encode(ResponseWriter writer, FacesContext context) throws IOException {
             String clientID = getClientId(context);
             writer.startElement("script", this);
             writer.writeAttribute("id", clientID, null);
             //define potential script entries
             //encode URL, some portals are rewriting the URLs radically
             writer.writeAttribute("src", context.getExternalContext().encodeResourceURL(script), null);
             writer.writeAttribute("type", "text/javascript", null);
             writer.endElement("script");
         }
 
         //Convince PortletFaces Bridge that this is a valid script for
         //inserting into the Portal head
         public String getRendererType() {
             return JAVAX_FACES_RESOURCE_SCRIPT;
         }
     }
 
     private static class JavascriptResourceOutput extends ReferencedScriptWriter {
         public JavascriptResourceOutput(ResourceHandler resourceHandler, String name, String library, String version) {
             super("");
             String fixedLibraryName = library;
             if ("".equals(library)) {
                 fixedLibraryName = null;
             }
 
             Resource r = resourceHandler.createResource(name, fixedLibraryName);
             String path = r.getRequestPath();
             if (version == null) {
                 script = path;
             } else {
                 if (path.contains("?")) {
                     script = path + "&v=" + version;
                 } else {
                     script = path + "?v=" + version;
                 }
             }
             this.setTransient(true);
         }
     }
 
     public static class ShortIdForm extends UIForm {
         //ID is assigned uniquely by ICEpush so no need to prepend
         public String getClientId(FacesContext context) {
             return getId();
         }
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
 }
