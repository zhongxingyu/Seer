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
 package org.wings.plaf.css;
 
 import java.awt.Adjustable;
 import java.io.IOException;
 
 import org.wings.*;
 import org.wings.style.CSSProperty;
 import org.wings.io.Device;
 import org.wings.script.JavaScriptDOMListener;
 
 public class ScrollPaneCG extends org.wings.plaf.css.AbstractComponentCG implements org.wings.plaf.ScrollPaneCG {
 
     private static final long serialVersionUID = 1L;
 
     public void writeInternal(Device device, SComponent component) throws IOException {
         SScrollPane scrollpane = (SScrollPane) component;
 
         if (scrollpane.getMode() == SScrollPane.MODE_COMPLETE) {
             SDimension preferredSize = scrollpane.getPreferredSize();
             if (preferredSize == null) {
                 scrollpane.setPreferredSize(new SDimension(200, 400));
             } else {
                 if (preferredSize.getWidthInt() < 0) Utils.setPreferredSize(component, "200", preferredSize.getHeight());
                 if (preferredSize.getHeightInt() < 0) Utils.setPreferredSize(component, preferredSize.getWidth(), "400");;
             }
 
             reLayout( component, RELAYOUT_SCROLLPANE);
             writeContent(device, component);
         } else {
             writeContent(device, component);
         }
         
         Adjustable sb = scrollpane.getVerticalScrollBar();
         SComponent viewport = (SComponent)scrollpane.getScrollable();
         if ( viewport != null && sb != null && sb instanceof SScrollBar) {
             final JavaScriptDOMListener handleMouseWheel = new JavaScriptDOMListener(
                     "DOMMouseScroll",
                     "wingS.scrollbar.handleMouseWheel", "'"+((SScrollBar)sb).getName()+"'", viewport);
             viewport.addScriptListener(handleMouseWheel);            
         }
 
     }
 
     public void writeContent(Device device, SComponent c) throws IOException {
         SScrollPane scrollPane = (SScrollPane) c;
 
         SDimension preferredSize = scrollPane.getPreferredSize();
         String height = preferredSize != null ? preferredSize.getHeight() : null;
         boolean clientLayout = Utils.isMSIE(scrollPane) && height != null && !"auto".equals(height)
             && scrollPane.getMode() != SScrollPane.MODE_COMPLETE;
         boolean clientFix = Utils.isMSIE(scrollPane) && (height == null || "auto".equals(height))
             && scrollPane.getMode() != SScrollPane.MODE_COMPLETE;
 
         device.print("<table");
 
         if (clientLayout) {
             Utils.optAttribute(device, "layoutHeight", height);
             Utils.setPreferredSize(scrollPane, preferredSize.getWidth(), null);
         }
 
         if (scrollPane.getMode() == SScrollPane.MODE_COMPLETE)
             scrollPane.setAttribute(CSSProperty.TABLE_LAYOUT, "fixed");
         else
             scrollPane.setAttribute(CSSProperty.TABLE_LAYOUT, null);
 
         Utils.writeAllAttributes(device, scrollPane);
 
         if (clientLayout) {
             Utils.setPreferredSize(scrollPane, preferredSize.getWidth(), height);
             reLayout(scrollPane, RELAYOUT_FILL);
         }
         else if (clientFix)
         	reLayout(scrollPane, RELAYOUT_FIX);
    
         device.print(">");
        
         Utils.renderContainer(device, scrollPane);
         device.print("</table>");
     }
 }
