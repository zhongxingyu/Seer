 /*
  * Copyright (c) 2012, DataLite. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301  USA
  */
 package cz.datalite.zk.annotation.invoke;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.util.Clients;
 import org.zkoss.zul.Window;
 
 /**
 * BusyBoxHandler allows to show modal blocking window on client. This window is
 * considered to be used to inform user about blocking operation.
  *
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class BusyBoxHandler {
 
     /** message to be shown */
     private String message;
 
     /** allow Cancel button */
     private boolean cancelable;
 
     /** parent component */
     private String parentId;
 
     /** instance of busy window */
     private Window busybox;
 
     /** busy in progress */
     private boolean busy;
 
     /** listen for event name */
     private String eventName;
 
     /** event listener */
     private EventListener listener;
 
     public BusyBoxHandler(String message, boolean i18n, boolean cancellable, String parentId) {
         this.message = i18n ? Labels.getLabel(message, message) : message;
         this.cancelable = cancellable;
         this.parentId = parentId;
     }
 
     /**
      * Show modal window if not shown
      */
     public void show(Component master) {
         if (!busy) {
             if (cancelable) { // cancellable operation, button has to be shown
                 // show busy message
                 Map<String, Object> parameters = new HashMap<String, Object>();
                 parameters.put("cancellable", cancelable);
                 parameters.put("message", message);
                 // ignore parent component
                 busybox = (Window) Executions.createComponents("~./busybox.zul", null, parameters);
 
                 // register event listener
                 if (eventName != null && listener != null) {
                     busybox.addEventListener(eventName, listener);
                 }
             } else { // is not cancellable, simple busy window is fine
                Component parent = parentId == null ? master : master.getFellow(parentId);
                Clients.showBusy(parent, message);
             }
             busy = true;
         }
     }
 
     /**
      * close window if shown
      */
     public void close(Component master) {
         if (busy) {
             if (busybox == null) { // clients used
                Component parent = parentId == null ? master : master.getFellow(parentId);
                Clients.clearBusy(parent);
             } else { // modal window used
                 busybox.detach();
                 busybox = null;
             }
             busy = false; // not busy any more
         }
     }
 
     /**
      * Set event listener to be bound on busybox window
      *
      * @param event    event name
      * @param listener listener
      */
     public void setEventListener(String event, EventListener listener) {
         this.eventName = event;
         this.listener = listener;
     }
 }
