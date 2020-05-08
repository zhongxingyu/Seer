 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
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
 import org.wings.SButton;
 import org.wings.SCheckBox;
 import org.wings.SClickable;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SFrame;
 import org.wings.SPageScroller;
 import org.wings.SRadioButton;
 import org.wings.SScrollBar;
 import org.wings.STabbedPane;
 import org.wings.STable;
 import org.wings.SToggleButton;
 import org.wings.STree;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public final class LowLevelEventDispatcher
         implements java.io.Serializable {
     private final transient static Log log = LogFactory.getLog(LowLevelEventDispatcher.class);
 
     private final HashMap listeners = new HashMap();
 
     protected boolean namedEvents = true;
     
     /** 
      * This array contains classes whose events should be filtered, since they
      * are caused by button tags, and IE's button tag support is buggy. 
      * 
      */
     private static final List ieButtonFixClasses = Arrays.asList(new Class[] {
             SButton.class, SToggleButton.class, SClickable.class,
            SPageScroller.class, SScrollBar.class,
             STabbedPane.class, STable.class, STree.class});
 
     public LowLevelEventDispatcher() {}
 
     public final void addLowLevelEventListener(LowLevelEventListener gl,
                                                String eventId) {
         List l = (List) listeners.get(eventId);
         if (l == null) {
             l = new ArrayList(2);
             l.add(gl);
             listeners.put(eventId, l);
         } else if (!l.contains(gl))
             l.add(gl);
     }
 
     public final void removeLowLevelEventListener(LowLevelEventListener gl,
                                                   String eventId) {
         List l = (List) listeners.get(eventId);
         if (l != null) {
             l.remove(gl);
             if (l.size() == 0)
                 listeners.remove(eventId);
         }
     }
 
     public final LowLevelEventListener getLowLevelEventListener(String eventId) {
         return (LowLevelEventListener) listeners.get(eventId);
     }
 
     public final void setNamedEvents(boolean b) {
         namedEvents = b;
     }
 
     /**
      * Registers a listeners. The NamePrefix of the listeners is stored in the
      * HashMap as key. The value is a Set (ArrayList) of {@link LowLevelEventListener}s.
      *
      * @param gl listeners
      */
     public void register(LowLevelEventListener gl) {
         if (gl == null)
             return;
 
         String key = gl.getLowLevelEventId();
 
         log.debug("dispatcher: register '" + key + "' type: " + gl.getClass());
         addLowLevelEventListener(gl, key);
 
         if (namedEvents) {
             key = gl.getName();
             if (key != null && key.trim().length() > 0) {
                 log.debug("dispatcher: register named '" + key + "'");
                 addLowLevelEventListener(gl, key);
             }
         }
     }
 
     /**
      * This should remove the GetListener from the HashMap, not the Names of
      * the GetListener (Names may change)
      */
     public void unregister(LowLevelEventListener gl) {
         if (gl == null)
             return;
 
         String key = gl.getLowLevelEventId();
 
         log.debug("unregister '" + key + "'");
         removeLowLevelEventListener(gl, key);
 
         key = gl.getName();
         if (key != null && key.trim().length() > 0) {
             log.debug("unregister named '" + key + "'");
             removeLowLevelEventListener(gl, key);
         }
 
     }
 
     /**
      * dispatch the events, encoded as [name/(multiple)values]
      * in the HTTP request.
      * @param name
      * @param values
      * @return if the event has been dispatched
      */
     public boolean dispatch(String name, String[] values) {
         if (log.isDebugEnabled())
             log.debug("dispatch " + name + " = " + Arrays.asList(values));
 
         boolean result = false;
         int dividerIndex = name.indexOf(SConstants.UID_DIVIDER);
         String epoch = null;
         boolean isIE = SessionManager.getSession().getUserAgent().getBrowserType() == BrowserType.IE;
 
         // no Alias
         if (dividerIndex > 0) {
             epoch = name.substring(0, dividerIndex);
             name = name.substring(dividerIndex + 1);
         }
 
         // make ImageButtons work in Forms .. browsers return
         // the click position as .x and .y suffix of the name
         if (name.endsWith(".x") || name.endsWith(".X")) {
             name = name.substring(0, name.length() - 2);
         } else if (name.endsWith(".y") || name.endsWith(".Y")) {
             // .. but don't process the same event twice.
             log.debug("discard '.y' part of image event");
             return false;
         }
         
         // is value encoded in name ? Then append it to the values we have.
         int p = name.indexOf(SConstants.UID_DIVIDER);
         if (p > -1) {
             String v = name.substring(p + 1);
             name = name.substring(0, p);
             String[] va = new String[values.length + 1];
             System.arraycopy(values, 0, va, 0, values.length);
             va[values.length] = v;
             values = va;
         }
 
         /*
          * This is a workaround for IE's buggy button tag support.
          * (Might change with IE7)
          * 
          * Normal behavior is to send only the pressed button's name value
          * pair. IE sends all buttons in a form, no matter which one was
          * pressed. Also, it doesn't send the values, but instead their
          * innerHtml property.
          * 
          * So what we do is when a button is pressed in IE, we attach a hidden
          * field to the form using javascript, containing info about the
          * pressed button. This is read here and the event processing is
          * triggered.
          * 
          * Below in the normal event dispatching, we discard all events
          * originating from Components which are represented by buttons.
          * These are right now:
          *   - SButton
          *   - SToggleButton
          */
         if (isIE && name.equals(SConstants.IEFIX_BUTTONACTION)) {
             String buttonId = values[0];
             String[] vals = new String[] {""};
             int pos = buttonId.indexOf(SConstants.UID_DIVIDER);
             if (pos > -1) {
                 String val = buttonId.substring(pos + 1);
                 buttonId = buttonId.substring(0, pos);
                 vals[0] = val;
             }
             List l = (List) listeners.get(buttonId);
             if (l != null && l.size() > 0) {
                 if (log.isDebugEnabled()) {
                     log.debug("process special IE workaround event '" + epoch + SConstants.UID_DIVIDER + name + "'");
                 }
                 for (int i = 0; i < l.size(); ++i) {
                     LowLevelEventListener gl = (LowLevelEventListener) l.get(i);
                     if (gl.isEnabled()) {
                         if (checkEpoch(epoch, buttonId, gl)) {
                             if (log.isDebugEnabled()) {
                                 log.debug("process event '" + buttonId + "' by " +
                                         gl.getClass() + "(" + gl.getLowLevelEventId() +
                                         ")");
                             }
                             gl.processLowLevelEvent(buttonId,vals);
                             result = true;
                         }
                     }
                 }
             }
         }
         List l = (List) listeners.get(name);
         if (l != null && l.size() > 0) {
             log.debug("process event '" + epoch + SConstants.UID_DIVIDER + name + "'");
             for (int i = 0; i < l.size(); ++i) {
                 LowLevelEventListener gl = (LowLevelEventListener) l.get(i);
                 if (gl.isEnabled()) {
                     if (checkEpoch(epoch, name, gl)) {
                         if (isIE) {
                             // see comment above, is this a form event?
                             boolean isFormEvent = ((SComponent)gl).getResidesInForm() && ((SComponent)gl).getShowAsFormComponent(); 
                             if (isFormEvent) {
                                 // was the button represented by a button html tag?
                                 boolean isButton = false;
                                 for (Iterator iter = ieButtonFixClasses.iterator(); iter
                                         .hasNext();) {
                                     Class bla = (Class)iter.next();
                                     isButton |= bla.isAssignableFrom(gl.getClass());
                                 }
                                 if (isButton) {
                                     log.debug("circumventing IE bug, not processing event '" + name + "' by " +
                                             gl.getClass() + "(" + gl.getLowLevelEventId() +
                                             ")");
                                 } else {
                                     log.debug("process form event not caused by a button tag'" + name + "' by " +
                                             gl.getClass() + "(" + gl.getLowLevelEventId() +
                                             ")");
                                     gl.processLowLevelEvent(name, values);
                                     result = true;
                                 }
                             } else {
                                 log.debug("process event '" + name + "' by " +
                                         gl.getClass() + "(" + gl.getLowLevelEventId() +
                                         ")");
                                 gl.processLowLevelEvent(name, values);
                                 result = true;
                             }
                         } else {
                             log.debug("process event '" + name + "' by " +
                                     gl.getClass() + "(" + gl.getLowLevelEventId() +
                                     ")");
                             gl.processLowLevelEvent(name, values);
                             result = true;
                         }
                     }
                 }
             }
         }
         return result;
     }
 
     protected boolean checkEpoch(String epoch, String name,
                                  LowLevelEventListener gl) {
         if (epoch != null) {
             SFrame frame = ((SComponent) gl).getParentFrame();
             if (frame == null) {
                 if (log.isDebugEnabled())
                     log.debug("request for dangling component '" + epoch + SConstants.UID_DIVIDER + name);
                 unregister(gl);
                 return false;
             }
             if (!epoch.equals(frame.getEventEpoch())) {
                 if (log.isDebugEnabled()) {
                     log.debug("### got outdated event '" + epoch + SConstants.UID_DIVIDER + name
                             + "' from frame '" + frame.getName() + "'; expected epoch: " + frame.getEventEpoch());
                 }
                 frame.fireInvalidLowLevelEventListener(gl);
                 return false;
             }
         }
         return true;
     }
 
     void clear() {
         listeners.clear();
     }
 }
 
 
