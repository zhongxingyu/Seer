 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import org.wings.plaf.FormCG;
 
 import javax.swing.event.EventListenerList;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 /*
  * Ein Form Container. In HTML ist die einzige Moeglichkeit
  * Benutzerinteraktion zu haben durch Forms gegeben. Alle
  * HTML Komponenten, die auf Benutzerinteraktion angewiesen sind
  * muessen in einen Form Container eingebettet sein.
  */
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SForm
         extends SContainer
         implements LowLevelEventListener {
     private final static Logger logger = Logger.getLogger("org.wings");
 
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "FormCG";
 
     public static final String MULTIPART_ENCODING = "multipart/form-data";
     public static final String URL_ENCODING = "application/x-www-form-urlencoded";
 
     /**
      * Use method POST for submission of the data.
      */
     private boolean postMethod = true;
 
     /**
      * EncondingType for submission of the data.
      */
     private String encType;
 
     /**
      * URL to which data
      * should be sent to
      */
     private URL action;
 
     /**
      * TODO: documentation
      */
     protected final EventListenerList listenerList = new EventListenerList();
 
     /**
      * TODO: documentation
      */
     protected String actionCommand;
 
     /**
      * the WingS event thread is the servlet doGet()/doPost() context
      * thread. Within this thread, we collect all armed components. A
      * 'armed' component is a component, that will 'fire' an event after the
      * first processRequest() stage is completed.
      */
     private static ThreadLocal threadArmedComponents = new ThreadLocal() {
         protected synchronized Object initialValue() {
             return new ArrayList(2);
         }
     };
 
     /**
      * TODO: documentation
      *
      * @param action
      */
     public SForm(URL action) {
         setAction(action);
     }
 
     /**
      * TODO: documentation
      */
     public SForm() {
     }
 
     /**
      * TODO: documentation
      *
      * @param layout
      */
     public SForm(SLayoutManager layout) {
         super(layout);
     }
 
     /**
      * TODO: documentation
      *
      * @param ac
      */
     public void setActionCommand(String ac) {
         actionCommand = ac;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public String getActionCommand() {
         return actionCommand;
     }
 
     /**
      * Add a listener for Form events. A Form event is always triggered, when
      * a form has been submitted. Usually, this happens, whenever a submit
      * button is pressed or some other mechanism triggered the posting of the
      * form. Other mechanisms are
      * <ul>
      * <li> Java Script submit() event</li>
      * <li> If a form contains a single text input, then many browsers
      * submit the form, if the user presses RETURN in that field. In that
      * case, the submit button will <em>not</em> receive any event but
      * only the form.
      * <li> The {@link SFileChooser} will trigger a form event, if the file
      * size exceeded the allowed size. In that case, even if the submit
      * button has been pressed, no submit-button event will be triggered.
      * (For details, see {@link SFileChooser}).
      * </ul>
      * Form events are guaranteed to be triggered <em>after</em> all
      * Selection-Changes and Button ActionListeners.
      *
      * @param listener
      */
     public void addActionListener(ActionListener listener) {
         listenerList.add(ActionListener.class, listener);
     }
 
     /**
      * Remove a form action listener, that has been added in
      * {@link #addActionListener(ActionListener)}
      *
      * @param listener
      */
     public void removeActionListener(ActionListener listener) {
         listenerList.remove(ActionListener.class, listener);
     }
 
     /**
      * Fire a ActionEvent at each registered listener.
      */
    protected void fireActionPerformed() {
         ActionEvent e = null;
         // Guaranteed to return a non-null array
         Object[] listeners = listenerList.getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ActionListener.class) {
                 // lazy create ActionEvent
                 if (e == null) {
                     e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                        actionCommand);
                 }
                 ((ActionListener) listeners[i + 1]).actionPerformed(e);
             }
         }
     }
 
     /*
      * fixme: the following static function should go in some global
      * class.
      */
 
     public final static void addArmedComponent(LowLevelEventListener component) {
         List armedComponents = (List) threadArmedComponents.get();
         armedComponents.add(component);
     }
 
     /**
      * clear armed components. This is usually not necessary, since sessions
      * clear clear their armed components. But if there was some Exception, it
      * might well be, that this does not happen.
      */
     public static void clearArmedComponents() {
         List armedComponents = (List) threadArmedComponents.get();
         armedComponents.clear();
     }
 
     /*
      * Die Sache muss natuerlich Thread Save sein, d.h. es duerfen nur
      * die Events gefeuert werden, die auch aus dem feuernden Thread
      * stammen (eben dem Dispatcher Thread). Sichergestellt wird das
      * dadurch das beim abfeuern der Event in eine Queue (ArrayList)
      * gestellt wird, die zu dem feuernden Event gehoert. Diese Queues
      * der verschiedenen Threads werden in einer Map verwaltet.
      * Beim feuern wird dann die Queue, die dem aktuellen Thread
      * entspricht gefeuert und aus der Map entfernt.
      */
     /**
      * This method fires the low level events for all "armed" components of
      * this thread (http session) in an ordered manner:
      * <ul><li>forms
      * <li>buttons / clickables
      * <li>"regular" components</ul>
      * This order derives out of the assumption, that a user first modifies
      * regular components before he presses the button submitting his changes.
      * Otherwise button actions would get fired before the edit components
      * fired their events.
      */
     public static void fireEvents() {
         List armedComponents = (List) threadArmedComponents.get();
         try {
             LowLevelEventListener component;
             // handle form special, form event should be fired last
             // hopefully there is only one form ;-)
             Iterator iterator = armedComponents.iterator();
             LinkedList formEvents = null;
             LinkedList buttonEvents = null;
             while (iterator.hasNext()) {
                 component = (LowLevelEventListener) iterator.next();
                 // fire form events at last
                 // there could be more than one form event (e.g. mozilla posts a
                 // hidden element even if it is in a form outside the posted
                 // form (if the form are nested
                 if (component instanceof SForm) {
                     if (formEvents == null) {
                         formEvents = new LinkedList();
                     } // end of if ()
                     formEvents.add(component);
                 } else if (component instanceof SAbstractIconTextCompound) {
                     if (buttonEvents == null) {
                         buttonEvents = new LinkedList();
                     }
                     buttonEvents.add(component);
                 } else {
                     component.fireIntermediateEvents();
                 }
             }
 
             if (buttonEvents != null) {
                 iterator = buttonEvents.iterator();
                 while (iterator.hasNext()) {
                     ((SAbstractIconTextCompound) iterator.next()).fireIntermediateEvents();
                 }
             }
 
             if (formEvents != null) {
                 iterator = formEvents.iterator();
                 while (iterator.hasNext()) {
                     ((SForm) iterator.next()).fireIntermediateEvents();
                 }
             }
 
             iterator = armedComponents.iterator();
             while (iterator.hasNext()) {
                 component = (LowLevelEventListener) iterator.next();
                 // fire form events at last
                 if (!(component instanceof SForm || component instanceof SAbstractIconTextCompound)) {
                     component.fireFinalEvents();
                 }
             }
 
             if (buttonEvents != null) {
                 iterator = buttonEvents.iterator();
                 while (iterator.hasNext()) {
                     ((SAbstractIconTextCompound) iterator.next()).fireFinalEvents();
                 }
                 buttonEvents.clear();
             }
 
 
             if (formEvents != null) {
                 iterator = formEvents.iterator();
                 while (iterator.hasNext()) {
                     ((SForm) iterator.next()).fireFinalEvents();
                 }
                 formEvents.clear();
             }
         } finally {
             armedComponents.clear();
         }
     }
 
 
     /**
      * Set, whether this form is to be transmitted via <code>POST</code> (true)
      * or <code>GET</code> (false). The default, and this is what you
      * usually want, is <code>POST</code>.
      *
      * @param postMethod
      */
     public void setPostMethod(boolean postMethod) {
         this.postMethod = postMethod;
     }
 
     /**
      * Returns, whether this form is transmitted via <code>POST</code> (true)
      * or <code>GET</code> (false).
      *
      * @return
      */
     public boolean isPostMethod() {
         return postMethod;
     }
 
     /**
      * Set the encoding of this form. This actually is an HTML interna
      * that bubbles up here. By default, the encoding type of any HTML-form
      * is <code>application/x-www-form-urlencoded</code>, and as such, needn't
      * be explicitly set with this setter. However, if you've included a
      * file upload element (as represented by {@link SFileChooser}) in your
      * form, this must be set to <code>multipart/form-data</code>, since only
      * then, files are transmitted correctly. In 'normal' forms without
      * file upload, it is not necessary to set it to
      * <code>multipart/form-data</code>; actually it enlarges the data to
      * be transmitted, so you probably don't want to do this, then.
      *
      * @param type the encoding type; one of <code>multipart/form-data</code>,
      *             <code>application/x-www-form-urlencoded</code> or null to detect encoding.
      */
     public void setEncodingType(String type) {
         encType = type;
     }
 
     /**
      * Get the current encoding type, as set with
      * {@link #setEncodingType(String)}
      *
      * @return string containting the encoding type. This is something like
      *         <code>multipart/form-data</code>,
      *         <code>application/x-www-form-urlencoded</code> .. or 'null'
      *         by default.
      */
     public String getEncodingType() {
         if (encType == null) {
             return detectEncodingType(this);
         } else {
             return encType;
         }
     }
 
     protected String detectEncodingType(SContainer pContainer) {
         for (int i = 0; i < pContainer.getComponentCount(); i++) {
             SComponent tComponent = pContainer.getComponent(i);
 
             if (tComponent instanceof SFileChooser) {
                 return MULTIPART_ENCODING;
             } else if (tComponent instanceof SContainer) {
                 String tContainerEncoding = detectEncodingType((SContainer) tComponent);
 
                 if (tContainerEncoding != null) {
                     return tContainerEncoding;
                 }
             }
         }
 
         return null;
     }
 
     /**
      * TODO: documentation
      *
      * @param action
      */
     public void setAction(URL action) {
         this.action = action;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public URL getAction() {
         return action;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public RequestURL getRequestURL() {
         RequestURL addr = super.getRequestURL();
         if (getAction() != null) {
             addr.addParameter(getAction().toString()); // ??
         }
         return addr;
     }
 
     public void processLowLevelEvent(String name, String[] values) {
         // we have to wait, until all changed states of our form have
         // changed, before we anything can happen.
         SForm.addArmedComponent(this);
     }
 
     public void fireIntermediateEvents() {
     }
 
     public void fireFinalEvents() {
        fireActionPerformed();
     }
 
     public boolean checkEpoch() {
         return true;
     }
 
     public SComponent addComponent(SComponent c, Object constraint, int index) {
         if (c instanceof SForm)
             logger.warning("WARNING: attempt to nest forms; won't work.");
         return super.addComponent(c, constraint, index);
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(FormCG cg) {
         super.setCG(cg);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
