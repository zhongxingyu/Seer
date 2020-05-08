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
 
 import org.wings.plaf.DialogCG;
 import org.wings.session.SessionManager;
 
 import java.util.logging.Logger;
 
 /**
  * As opposed to Swing, wingS dialogs are non modal. However, the dismission of
  * the dialog is propagated by means of ActionEvents. The action command of the
  * event tells, what kind of user activity caused the dialog to dismiss.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public class SDialog extends SForm {
     private final static Logger logger = Logger.getLogger("org.wings");
 
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "DialogCG";
 
     /**
      * Action command if dialog window was closed
      */
     public static final String CLOSE_ACTION = "CLOSE";
 
     /**
      * Action command if user hit return
      */
     public static final String DEFAULT_ACTION = "DEFAULT";
 
     /**
      * The Title of the Dialog Frame
      */
     protected String title;
 
     protected String titleStyle;
 
     protected String contentStyle;
 
     /**
      * TODO: documentation
      */
     protected SIcon icon = null;
 
     private boolean closable = true;
     private boolean closed = false;
 
     /**
      * The parent of the Dialog
      */
     protected SRootContainer owner = null;
 
 
     /**
      * Creates a Dialog without parent <code>SFrame</code> or <code>SDialog</code>
      * and without Title
      */
     public SDialog() {
     }
 
 
     /**
      * Creates a dialog with the specifed parent <code>SFrame</code> as its owner.
      *
      * @param owner the parent <code>SFrame</code>
      */
     public SDialog(SFrame owner) {
         this.owner = owner;
     }
 
     /**
      * Creates a dialog with the specified title and the specified owner frame.
      *
      * @param owner the parent <code>SFrame</code>
     * @param title  the <code>String</code> to display as titke
      */
     public SDialog(SFrame owner, String title) {
         this.owner = owner;
         this.title = title;
     }
 
     public void setTitle(String t) {
         String oldTitle = title;
         title = t;
         if ((title == null && oldTitle != null) ||
             (title != null && !title.equals(oldTitle)))
             reload(ReloadManager.RELOAD_CODE);
     }
 
     public String getTitle() {
         return title;
     }
 
     /**
      * TODO: documentation
      *
      * @param i
      */
     public void setIcon(SIcon i) {
         if (i != icon || i != null && !i.equals(icon)) {
             icon = i;
             reload(ReloadManager.RELOAD_CODE);
         }
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public SIcon getIcon() {
         return icon;
     }
 
     public void setClosable(boolean v) {
         boolean old = closable;
         closable = v;
         if (old != closable)
             reload(ReloadManager.RELOAD_CODE);
     }
 
     public boolean isClosable() {
         return closable;
     }
 
     public void setClosed(boolean v) {
         v &= isClosable();
         boolean old = closed;
         closed = v;
         if (old != closed)
             reload(ReloadManager.RELOAD_CODE);
     }
 
     public boolean isClosed() {
         return closed;
     }
 
     public String getTitleStyle() {
         return titleStyle;
     }
 
     public void setTitleStyle(String pTitleStyle) {
         titleStyle = pTitleStyle;
     }
 
     public String getContentStyle() {
         return contentStyle;
     }
 
     public void setContentStyle(String pContentStyle) {
         contentStyle = pContentStyle;
     }
 
    // fireFinalEvents() somewhere up the stack
    protected void fireActionPerformed(String state) {
        setActionCommand(state);
        fireActionPerformed();
    }

     /**
      * Removes all <code>SComponents</code> from the pane
     *
      */
     public void dispose() {
         removeAll();
     }
 
     /**
      * Remove this dialog from its frame.
      */
     public void hide() {
         logger.fine("hide dialog");
         if (owner != null) {
             owner.popDialog();
         }
     }
 
     public void setVisible(boolean visible) {
         if (visible) {
             if (owner != null) show(owner);
         } else {
             if (isVisible()) hide();
         }
         super.setVisible(visible);
     }
 
     /**
      * sets the root container in which this dialog is to be displayed.
      */
     protected void setFrame(SRootContainer f) {
         owner = f;
     }
 
     /**
      * shows this dialog in the given SRootContainer. If the component is
      * not a root container, then the root container the component is in
      * is used.
      * If the component is null, the root frame of the session is used. If there
      * is no root frame in the session, a NullPointerException is thrown (this
      * should not happen ;-).
      *
      * @param c
      */
     public void show(SComponent c) {
         logger.fine("show dialog");
         if (c == null)
             c = SessionManager.getSession().getRootFrame();
 
         SContainer frame = null;
         if (c instanceof SContainer)
             frame = (SContainer) c;
         else
             frame = c.getParent();
 
         // find RootContainer
         while (frame != null && !(frame instanceof SRootContainer)) {
             frame = frame.getParent();
         }
 
         if (frame == null) {
             frame = SessionManager.getSession().getRootFrame();
         }
 
         if (frame == null) {
             throw new IllegalArgumentException("Component has no root container");
         }
         owner = (SRootContainer) frame;
         owner.pushDialog(this);
     }
 
     // LowLevelEventListener interface. Handle own events.
     public void processLowLevelEvent(String name, String[] values) {
         // is this a window event?
         try {
             switch (new Integer(values[0]).intValue()) {
                 case org.wings.event.SInternalFrameEvent.INTERNAL_FRAME_CLOSED:
                     setClosed(true);
                     actionCommand = CLOSE_ACTION;
                     break;
 
                 default:
                     // form event
                     actionCommand = DEFAULT_ACTION;
             }
         } catch (NumberFormatException ex) {
             // no window event...
         }
         SForm.addArmedComponent(this); // trigger later invocation of fire*()
     }
 
     public void fireFinalEvents() {
         super.fireFinalEvents();
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(DialogCG cg) {
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
