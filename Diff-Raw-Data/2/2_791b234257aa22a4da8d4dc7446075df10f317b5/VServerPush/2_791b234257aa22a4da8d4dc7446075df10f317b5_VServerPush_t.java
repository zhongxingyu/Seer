 
 /*
  * Copyright (C) 2011 Elihu, LLC. All rights reserved.
  *
  * $Id$
  */
 
 package org.vaadin.addons.serverpush.client.ui;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.terminal.gwt.client.ApplicationConfiguration;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 import com.vaadin.terminal.gwt.client.VConsole;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.List;
 
 import org.atmosphere.gwt.client.AtmosphereClient;
 import org.atmosphere.gwt.client.AtmosphereListener;
 
 /**
  * Vaadin client widget class for ServerPush that listens for broadcasts
  */
 public class VServerPush extends Widget implements Paintable, AtmosphereListener {
 
     public static final String CONTEXT_PATH = "contextPath";
     public static final String COMET = "comet";
     public static final String CLASSNAME = "v-" + COMET;
 
     /** The client side widget identifier */
     protected String paintableId;
 
     /** Reference to the server connection object. */
     protected ApplicationConnection client;
 
     private AtmosphereClient atmosphereClient;
     private String contextPath;
 
     public VServerPush() {
         setElement(Document.get().createDivElement());
         getElement().getStyle().setDisplay(Style.Display.NONE);
         this.setStyleName(CLASSNAME);
     }
 
     public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
         // This call should be made first.
         // It handles sizes, captions, tooltips, etc. automatically.
         if (client.updateComponent(this, uidl, true)) {
             // If client.updateComponent returns true there has been no changes
             // and we
             // do not need to update anything.
             return;
         }
 
         // Save reference to server connection object to be able to send
         // user interaction later
         this.client = client;
 
         // Save the client side identifier (paintable id) for the widget
         this.paintableId = uidl.getId();
 
         if (this.contextPath == null) {
             final String ctxPath = uidl.getStringAttribute(CONTEXT_PATH);
            if (ctxPath != null)
                 this.contextPath = ctxPath;
         }
 
         if (this.atmosphereClient == null) {
             Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                 public void execute() {
                     if (getParent() != null) {
                         final String path = (contextPath == null ? "" : contextPath) + "/server-push";
                         atmosphereClient = new AtmosphereClient(path, VServerPush.this);
                         atmosphereClient.start();
                     } else {
                         Scheduler.get().scheduleDeferred(this);
                     }
                 }
             });
         }
     }
 
     private void update() {
         this.client.updateVariable(this.paintableId, "cometEvent", COMET, true);
     }
 
     public void onConnected(int heartbeat, int connectionID) {
         if (ApplicationConfiguration.isDebugMode())
             VConsole.log("on connected; heartbeat: " + heartbeat + "; con ID: " + connectionID);
     }
 
     public void onBeforeDisconnected() {
         if (ApplicationConfiguration.isDebugMode())
             VConsole.log("on before disconnected");
     }
 
     public void onDisconnected() {
         if (ApplicationConfiguration.isDebugMode())
             VConsole.log("on disconnected");
     }
 
     public void onError(Throwable exception, boolean connected) {
         if (ApplicationConfiguration.isDebugMode()) {
             VConsole.error("on error; connected: " + connected);
             VConsole.error(exception);
         }
     }
 
     public void onHeartbeat() {
         if (ApplicationConfiguration.isDebugMode())
             VConsole.log("on heartbeat");
     }
 
     public void onRefresh() {
         if (ApplicationConfiguration.isDebugMode())
             VConsole.log("on refresh");
     }
 
     public void onMessage(List<? extends Serializable> messages) {
         if (ApplicationConfiguration.isDebugMode())
             VConsole.log("on message; " + Arrays.toString(messages.toArray()));
         this.update();
     }
 
     @Override
     protected void onDetach() {
         super.onDetach();
         if (this.atmosphereClient != null && this.atmosphereClient.isRunning())
             this.atmosphereClient.stop();
     }
 }
