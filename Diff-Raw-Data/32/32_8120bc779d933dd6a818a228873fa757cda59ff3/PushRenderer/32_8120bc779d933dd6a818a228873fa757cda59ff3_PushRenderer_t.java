 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.application;
 
 import org.icefaces.impl.application.LazyPushManager;
 import org.icefaces.impl.event.BridgeSetup;
 import org.icefaces.impl.push.SessionViewManager;
 import org.icefaces.util.EnvUtils;
 import org.icepush.PushConfiguration;
 import org.icepush.PushContext;
 import org.icepush.PushNotification;
 
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletContext;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * <p>
  * The <code>PushRenderer</code>  allows an application to initiate
  * rendering asynchronously and independently of user interaction for a
  * group of sessions or views.  When a session is rendered, all windows or
  * views that a particular user has open in their session will be updated via
  * Ajax Push with the current application state.
  * </p>
  */
 public class PushRenderer {
     private static Logger log = Logger.getLogger(PushRenderer.class.getName());
     public static final String ALL_SESSIONS = "PushRenderer.ALL_SESSIONS";
     private static final String MissingICEpushMessage = "ICEpush library missing. Push notification disabled.";
     private static final PortableRenderer MissingICEpushPortableRenderer =
             new PortableRenderer() {
                 public void render(final String group) {
                     log.warning(MissingICEpushMessage);
                 }
 
                 public void render(final String group, final PushOptions options) {
                     log.warning(MissingICEpushMessage);
                 }
             };
 
     /**
      * Add the current view to the specified group. Groups
      * are automatically garbage collected when all members become
      * unable to receive push updates.
      *
      * @param groupName the name of the group to add the current view to
      */
     public static synchronized void addCurrentView(String groupName) {
         if (EnvUtils.isICEpushPresent()) {
             FacesContext context = FacesContext.getCurrentInstance();
             missingFacesContext(context);
             String viewID = lookupViewState(context);
             LazyPushManager.enablePushForView(context, viewID);
             PushContext pushContext = (PushContext) context.getExternalContext().getApplicationMap().get(PushContext.class.getName());
             pushContext.addGroupMember(groupName, viewID);
         } else {
             log.warning(MissingICEpushMessage);
         }
     }
 
     /**
      * Remove the current view from the specified group.
      *
      * @param groupName the name of the group to remove the current view from
      */
     public static synchronized void removeCurrentView(String groupName) {
         if (EnvUtils.isICEpushPresent()) {
             FacesContext context = FacesContext.getCurrentInstance();
             missingFacesContext(context);
             String viewID = lookupViewState(context);
             LazyPushManager.disablePushForView(context, viewID);
             PushContext pushContext = (PushContext) context.getExternalContext().getApplicationMap().get(PushContext.class.getName());
             pushContext.removeGroupMember(groupName, viewID);
         } else {
             log.warning(MissingICEpushMessage);
         }
     }
 
     /**
      * All subsequently created views in the current session will be added to the specified group.
      * Groups of sessions are automatically garbage collected when all member sessions have
      * become invalid.
      *
      * @param groupName the name of the group to add the current session to
      */
     public static synchronized void addCurrentSession(final String groupName) {
         if (EnvUtils.isICEpushPresent()) {
             FacesContext context = FacesContext.getCurrentInstance();
             missingFacesContext(context);
             LazyPushManager.enablePushForSessionViews(context);
             SessionViewManager.addCurrentSessionToGroup(context, groupName);
         } else {
             log.warning(MissingICEpushMessage);
         }
     }
 
     /**
      * Remove the current views from the specified group.  Use of this method is
      * optional as group membership is maintained automatically as clients leave.
      *
      * @param groupName the name of the group to remove the current view from
      */
     public static synchronized void removeCurrentSession(final String groupName) {
         if (EnvUtils.isICEpushPresent()) {
             FacesContext context = FacesContext.getCurrentInstance();
             missingFacesContext(context);
             LazyPushManager.disablePushForSessionViews(context);
             SessionViewManager.removeCurrentSessionFromGroup(context, groupName);
         } else {
             log.warning(MissingICEpushMessage);
         }
     }
 
     /**
      * Render the specified group of sessions by performing the JavaServer Faces
      * execute and render lifecycle phases.
      *
      * @param groupName the name of the group of sessions to render.
      */
     public static void render(String groupName) {
         if (EnvUtils.isICEpushPresent()) {
             FacesContext context = FacesContext.getCurrentInstance();
             missingFacesContext(context);
             PushContext pushContext = (PushContext) context.getExternalContext().getApplicationMap().get(PushContext.class.getName());
             pushContext.push(groupName);
         } else {
             log.warning(MissingICEpushMessage);
         }
     }
 
     /**
      * Render message to the specified group of sessions but only to the clients
      * that have their blocking connection paused.
      *
      * @param group   the name of the group of sessions to render.
      * @param options options for this push request
      */
     public static void render(String group, PushOptions options) {
         if (EnvUtils.isICEpushPresent()) {
             FacesContext context = FacesContext.getCurrentInstance();
             missingFacesContext(context);
             Boolean pushOthers = (Boolean)options.getAttributes().get(PushOthers.PUSH_OTHERS);
             if (pushOthers != null && pushOthers) {
                options.getAttributes().put("pushIDSet", SessionViewManager.getCurrentSessionViewSet(context));
             }
             Map<String, Object> applicationMap = context.getExternalContext().getApplicationMap();
             PushIsolator.render(applicationMap, group, options);
         } else {
             log.warning(MissingICEpushMessage);
         }
     }
 
     /**
      * Create a PortableRenderer instance. PortableRenderer can trigger renderings in the context of the application.
      * Once acquired it does not need a current FacesContext in order to function. The returned instances are not serializable.
      *
      * @return application wide PortableRenderer instance
      */
     public static PortableRenderer getPortableRenderer() {
         if (EnvUtils.isICEpushPresent()) {
             final Map<String, Object> applicationMap =
                     FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
             return new PortableRenderer() {
                 public void render(String group) {
                     //delay PushContext lookup until is needed
                     PushContext pushContext = (PushContext) applicationMap.get(PushContext.class.getName());
                     if (pushContext == null) {
                         log.fine("PushContext not initialized yet.");
                     } else {
                         pushContext.push(group);
                     }
                 }
 
                 public void render(String group, PushOptions options) {
                     //delay PushContext lookup until is needed
                     PushIsolator.render(applicationMap, group, options);
                 }
             };
         } else {
             log.warning(MissingICEpushMessage);
             return MissingICEpushPortableRenderer;
         }
     }
 
     public static PortableRenderer getPortableRenderer(final ServletContext servletContext) {
         if (EnvUtils.isICEpushPresent()) {
             return
                     new PortableRenderer() {
                         public void render(final String group) {
                             PushContext pushContext = (PushContext) servletContext.getAttribute(PushContext.class.getName());
                             if (pushContext == null) {
                                 log.fine("PushContext not initialized yet.");
                             } else {
                                 pushContext.push(group);
                             }
                         }
 
                         public void render(final String group, final PushOptions options) {
                             PushIsolator.render(servletContext, group, options);
                         }
                     };
         } else {
             log.warning(MissingICEpushMessage);
             return MissingICEpushPortableRenderer;
         }
     }
 
     private static String lookupViewState(FacesContext context) {
         return BridgeSetup.getViewID(context.getExternalContext());
     }
 
     private static void missingFacesContext(FacesContext context) {
         if (context == null) {
             throw new RuntimeException("FacesContext is not present for thread " + Thread.currentThread());
         }
     }
 }
 
 /*
 Classloading problems were observed with the following code inline.
 This avoids a runtime dependency on icepush.jar.
 */
 
 class PushIsolator {
     private static Logger log = Logger.getLogger(PushRenderer.class.getName());
 
     public static void render(Map<String, Object> applicationMap, String group,
                               PushOptions options) {
         PushContext pushContext =
                 (PushContext) applicationMap.get(PushContext.class.getName());
         if (pushContext == null) {
             log.fine("PushContext not initialized yet.");
         } else {
             if (options instanceof PushMessage) {
                 pushContext.push(group,
                         new PushNotification(options.getAttributes()));
             } else {
                 pushContext.push(group,
                         new PushConfiguration(options.getAttributes()));
             }
         }
     }
 
     public static void render(final ServletContext servletContext, final String group, final PushOptions options) {
         PushContext pushContext = (PushContext) servletContext.getAttribute(PushContext.class.getName());
         if (pushContext == null) {
             log.fine("PushContext not initialized yet.");
         } else {
             if (options instanceof PushMessage) {
                 pushContext.push(group, new PushNotification(options.getAttributes()));
             } else {
                 pushContext.push(group, new PushConfiguration(options.getAttributes()));
             }
         }
     }
 }
