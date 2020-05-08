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
 package org.wings;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.wings.border.SBorder;
import org.wings.plaf.ComponentCG;
import org.wings.plaf.FormCG;
import org.wings.plaf.WindowCG;
 
 /**
  * The <code>SWindow</code> is currently just a placeholder for further implementations like
  * MDI windows or dialogs.
  * <p/>
  * User: rrd
  * Date: Oct 2, 2007
  * Time: 6:59:44 PM
  *
  * @author <a href="mailto:rrd@wilken.de">Roman R&auml;dle</a>
  * @version $Id
  */
 public class SWindow extends SForm implements LowLevelEventListener {
 
     private static final Log LOG = LogFactory.getLog(SWindow.class);
 
     /**
      * Action command if window window was closed
      */
     public static final String CLOSE_ACTION = "CLOSE";
 
     /**
      * Action command if user hit return
      */
     public static final String DEFAULT_ACTION = "DEFAULT";
 
     protected SRootContainer owner;
     
 	protected int x = -1;
 	protected int y = -1;
 
 	/**
      * Returns the root container in which this dialog is to be displayed.
      *
      * @return The root container in which this dialog is to be displayed.
      * @see this#getParent()
      */
     public SRootContainer getOwner() {
         return owner;
     }
 
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		int oldX = x;
 		this.x = x;
 		if (oldX != x)
 			reload();
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		int oldY = y;
 		this.y = y;
 		if (oldY != y)
 			reload();
 	}
 
     public void setVisible(boolean visible) {
         if (visible) {
             show(owner);
         } else {
             hide();
         }
         super.setVisible(visible);
     }
 
     /**
      * Removes all <code>SComponents</code> from the pane
      */
     public void dispose() {
         if (isVisible()) {
             hide();
         }
         removeAll();
     }
 
     /**
      * shows this window in the given SRootContainer. If the component is
      * not a root container, then the root container the component is in
      * is used.
      * If the component is null, the root frame of the session will be used.
      */
     public void show(SComponent c) {
         LOG.debug("show window");
 
         SComponent oldVal = this.owner;
 
         // If the owner is empty get the components root container.
         if (owner == null) {
             if (c != null) {
                 while (!(c instanceof SRootContainer)) {
                     c = c.getParent();
                 }
                 owner = (SRootContainer) c;
             }
         }
 
         if (owner == null) {
             owner = getSession().getRootFrame();
         }
         owner.pushWindow(this);
 
         propertyChangeSupport.firePropertyChange("owner", oldVal, c);
 
         /*
         if (isUpdatePossible() && SWindow.class.isAssignableFrom(getClass())) {
             update(((WindowCG) getCG()).getWindowAddedUpdate(this));
         }
         */
     }
 
     /**
      * Remove this window from its frame.
      */
     public void hide() {
         LOG.debug("hide window");
         if (owner != null) {
             owner.removeWindow(this);
         }
     }
 
     // LowLevelEventListener interface. Handle own events.
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
         if (action.endsWith("_keystroke"))
             return;
 
         if (action.indexOf("_") > -1) {
 			String[] actions = action.split("_");
 			if ("xy".equals(actions[1])) {
 				String[] coords = values[0].split(",");
 
 				x = Integer.parseInt(coords[0]);
 				y = Integer.parseInt(coords[1]);
 				return;
 			}
 			else if ("dispose".equals(actions[1])) {
 				dispose();
 			}
 		}
         
         // is this a window event?
         try {
             switch (new Integer(values[0]).intValue()) {
                 case org.wings.event.SInternalFrameEvent.INTERNAL_FRAME_CLOSED:
                 	dispose();
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
 }
