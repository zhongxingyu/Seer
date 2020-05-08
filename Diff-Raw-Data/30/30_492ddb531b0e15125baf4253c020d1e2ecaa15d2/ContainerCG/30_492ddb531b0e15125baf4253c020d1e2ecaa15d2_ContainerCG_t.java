 /*
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
 package org.wings.plaf.css;
 
 
 import org.wings.*;
 import org.wings.io.Device;
 
 public class ContainerCG extends AbstractComponentCG implements org.wings.plaf.PanelCG {
     private static final long serialVersionUID = 1L;
 
     public void writeInternal(final Device device, final SComponent component) throws java.io.IOException {
         final SContainer container = (SContainer) component;
         final SLayoutManager layout = container.getLayout();
 
         SDimension preferredSize = container.getPreferredSize();
         String height = preferredSize != null ? preferredSize.getHeight() : null;
         boolean clientLayout = isMSIE(container) && height != null && !"auto".equals(height)
             && (layout instanceof SBorderLayout || layout instanceof SGridBagLayout);
 
         device.print("<table");
 
         if (clientLayout) {
             Utils.optAttribute(device, "layoutHeight", height);
             preferredSize.setHeight(null);
         }
 
         writeAllAttributes(device, component);
         Utils.writeEvents(device, component, null);
 
         if (clientLayout) {
             preferredSize.setHeight(height);
             component.getSession().getScriptManager().addScriptListener(new LayoutFillScript(component.getName()));
         }
 
         device.print(">");
 
         // special case templateLayout and card layout. We open TABLE cell for them.
         final boolean writeTableData = layout instanceof STemplateLayout || layout instanceof SCardLayout;
         if (writeTableData) {
             device.print("<tr><td>");
         }
 
         Utils.renderContainer(device, container);
 
         if (writeTableData) {
             device.print("</td></tr>");
         }
 
         device.print("</table>");
     }
 }
