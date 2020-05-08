 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.session;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.LowLevelEventListener;
 import org.wings.SComponent;
 import org.wings.SFrame;
 
 import java.util.*;
 
 /**
  * Registers session component instants which want to receive low level events.
  * The dispatcher holds a list of all known low level event listeners and is responsible
  * to dispatch the according part of an original HTTP request to the
  * {@link LowLevelEventListener#processLowLevelEvent(String, String[])} method of the registered
  * {@link LowLevelEventListener}s.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 public final class LowLevelEventDispatcher
         implements java.io.Serializable {
     private final transient static Log log = LogFactory.getLog(LowLevelEventDispatcher.class);
 
     /**
      * The name prefix is stored in the
      * HashMap as key. The value is a Set (ArrayList) of {@link LowLevelEventListener}s
      */
     private final HashMap listeners = new HashMap();
 
     private String eventEpoch;
 
     protected boolean namedEvents = true;
 
     public LowLevelEventDispatcher() {
     }
 
     public final void addLowLevelEventListener(LowLevelEventListener gl,
                                                String eventId) {
         List l = (List) listeners.get(eventId);
         if (l == null) {
             l = new ArrayList(2);
             l.add(gl);
             listeners.put(eventId, l);
         } else if (!l.contains(gl)) {
             l.add(gl);
         }
     }
 
     public final void removeLowLevelEventListener(LowLevelEventListener gl,
                                                   String eventId) {
         List l = (List) listeners.get(eventId);
         if (l != null) {
             l.remove(gl);
             if (l.isEmpty()) {
                 listeners.remove(eventId);
             }
         }
     }
 
     /**
      * Returns list of registered low level event listener for the given event id.
      *
      * @param eventId The id (HTTP request parameter name) under which the listeners are registered.
      * @return A list of registered low level event listener for the given event id.
      */
     public final List getLowLevelEventListener(String eventId) {
         final List list = (List) listeners.get(eventId);
         return list != null ? Collections.unmodifiableList(list) : Collections.EMPTY_LIST;
     }
 
     /**
      * Register low level event listeners additionally by their component name as event id.
      * Used for purposes where you use fixed ids vs. dnymaically applied ids.
      *
      * @param registerListenerAlsoUnderName if <code>true</code> then components will also receieve
      *                                      HTTP values under their {@link org.wings.SComponent#getName()}
      *                                      in addition to {@link org.wings.LowLevelEventListener#getLowLevelEventId()}
      */
     public final void setNamedEvents(boolean registerListenerAlsoUnderName) {
         namedEvents = registerListenerAlsoUnderName;
     }
 
     /**
      * Registers a low level event listeners (for HTTP request processing).
      * <p/>
      * The NamePrefix of the listeners id is used as HTTP requestr parameter name. .
      *
      * @param gl listeners
      */
     public void register(LowLevelEventListener gl) {
         if (gl != null) {
             final String id = gl.getLowLevelEventId();
 
             log.debug("register id '" + id + "' (type: " + gl.getClass() + ")");
             addLowLevelEventListener(gl, id);
         }
     }
 
     public void unregister(LowLevelEventListener gl) {
         if (gl != null) {
             final String id = gl.getLowLevelEventId();
 
             log.debug("unregister id '" + id + "' (type: " + gl.getClass() + ")");
             removeLowLevelEventListener(gl, id);
         }
     }
 
     /**
      * dispatch the events, encoded as [name/(multiple)values]
      * in the HTTP request. the part in front of the UID_DIVIDER ('-') is removed, first.
      * if the remainder contains an underscore ('_'), only the portion afore will be used
      * to identify the target component.
      *
      * @param name
      * @param values
      * @return if the event has been dispatched
      */
     public boolean dispatch(String name, String[] values) {
         boolean result = false;
        String id = null;
         String suffix = null;
        // Does name contain underscores?
         int dividerIndex = name.indexOf('_');
        if (dividerIndex > 0) {
            // Use the part before the first '_' as the
            // ID to detect the low level event listener
             id = name.substring(0, dividerIndex);
             suffix = name.substring(dividerIndex + 1);
         } else {
            id = name; // ID equals name in case of no '_'
         }
 
         final List l = (List) listeners.get(id);
         if (l != null && l.size() > 0) {
             for (int i = 0; i < l.size(); ++i) {
                 LowLevelEventListener gl = (LowLevelEventListener) l.get(i);
                 if (gl.isEnabled()) {
                     if (!gl.isEpochCheckEnabled() || isEventEpochValid(gl)) {
                         if (!"focus".equals(suffix) || gl instanceof SFrame) {
                         	if (log.isDebugEnabled()) {
                                 log.debug("processing event '" + name + "' by " +
                                 		gl.getClass() + "(" + gl.getLowLevelEventId() + ")");
                             }
                         	gl.processLowLevelEvent(name, values);
                             result = true;
                         }
                     }
                 }
             }
         }
         return result;
     }
 
     protected boolean isEventEpochValid(LowLevelEventListener gl) {
         if (eventEpoch != null) {
             SFrame frame = ((SComponent) gl).getParentFrame();
             if (frame == null) {
                 if (log.isDebugEnabled()) {
                     log.debug("request for dangling component '" + gl.getName() + "'");
                 }
                 unregister(gl);
                 return false;
             }
             if (!eventEpoch.equals(frame.getEventEpoch())) {
                 if (log.isDebugEnabled()) {
                     log.debug("### got outdated event '" + gl.getName() + "' from frame '" +
                     		frame.getName() + "' --> received epoch: " + eventEpoch +
                     		" | expected epoch: " + frame.getEventEpoch());
                 }
                 frame.fireInvalidLowLevelEventListener(gl);
                 frame.reload();
                 return false;
             }
         }
         return true;
     }
 
     void clear() {
         listeners.clear();
     }
 
     private final List<Runnable> runnables = new LinkedList<Runnable>();
 
     public void invokeLater(Runnable runnable) {
         synchronized (this.runnables) {
             runnables.add(runnable);
         }
     }
 
     void invokeRunnables() {
         synchronized (this.runnables) {
             for (Iterator iterator = runnables.iterator(); iterator.hasNext();) {
                 Runnable runnable = (Runnable)iterator.next();
                 try {
                     runnable.run();
                 }
                 catch (Throwable e) {
                     log.error(runnable, e);
                 }
                 iterator.remove();
             }
         }
     }
 
     protected void setEventEpoch(String epoch) {
 		this.eventEpoch = epoch;
 	}
 }
