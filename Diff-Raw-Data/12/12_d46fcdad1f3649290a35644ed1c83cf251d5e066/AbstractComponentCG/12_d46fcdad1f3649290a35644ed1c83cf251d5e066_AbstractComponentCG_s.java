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
 package org.wings.plaf.css;
 
 import org.wings.*;
 import org.wings.border.STitledBorder;
 import org.wings.io.Device;
 import org.wings.plaf.ComponentCG;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.io.Serializable;
 
 /**
  * Partial CG implementation that is common to all ComponentCGs.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public abstract class AbstractComponentCG
         implements ComponentCG, SConstants, Serializable {
 
     protected AbstractComponentCG() {    }
 
     /**
      * Install the appropriate CG for <code>component</code>.
      *
      * @param component the component
      */
     public void installCG(SComponent component) {
         Class clazz = component.getClass();
         while (!"org.wings".equals(clazz.getPackage().getName()))
             clazz = clazz.getSuperclass();
 
         String style = clazz.getName();
         style = style.substring(style.lastIndexOf('.') + 1);
         component.setStyle(style);
     }
 
     /**
      * Uninstall the CG from <code>component</code>.
      *
      * @param component the component
      */
     public void uninstallCG(SComponent component) {
     }
 
     public void write(Device device, SComponent component) throws IOException {
         if (!component.isVisible())
             return;
         writePrefix(device, component);
         writeContent(device, component);
         writePostfix(device, component);
     }
 
     public String mapSelector(String selector) {
         return selector;
     }
 
     protected void writePrefix(Device device, SComponent component) throws IOException {
         device
                 .print("<div id=\"")
                 .print(component.getName())
                 .print("\" class=\"")
                 .print(component.getStyle());
 
         if (component instanceof LowLevelEventListener) {
             LowLevelEventListener lowLevelEventListener = (LowLevelEventListener) component;
             device.print("\" event=\"")
                     .print(lowLevelEventListener.getEncodedLowLevelEventId());
         }
 
         final SDimension dim = component.getPreferredSize();
         if (dim != null) {
             device.print("\" style=\"");
             if (dim.width != null)
                 device
                         .print("width:")
                         .print(dim.width)
                        .print(";");
             if (dim.height != null)
                 device
                         .print(" height:")
                         .print(dim.height)
                        .print(";");
         }
 
         String toolTip = component.getToolTipText();
         if (toolTip != null)
             device.print("\" onmouseover=\"return makeTrue(domTT_activate(this, event, 'content', '")
                     .print(toolTip)
                     .print("', 'predefined', 'default'));");
 
         InputMap inputMap = component.getInputMap();
         if (inputMap != null && !(inputMap instanceof VersionedInputMap)) {
             System.out.println("inputMap = " + inputMap);
             inputMap = new VersionedInputMap(inputMap);
             component.setInputMap(inputMap);
         }
 
         if (inputMap != null) {
             VersionedInputMap versionedInputMap = (VersionedInputMap) inputMap;
             Integer inputMapVersion = (Integer) component.getClientProperty("inputMapVersion");
             if (inputMapVersion == null || versionedInputMap.getVersion() != inputMapVersion.intValue()) {
                 System.out.println("inputMapVersion = " + inputMapVersion);
                 InputMapScriptListener.install(component);
                 component.putClientProperty("inputMapVersion", new Integer(versionedInputMap.getVersion()));
             }
         }
 
         SPopupMenu menu = component.getComponentPopupMenu();
         if (menu != null) {
             String componentId = menu.getName();
             String popupId = componentId + "_pop";
             String hookId = component.getName();
 
             device.print("\" onclick=\"Menu.prototype.toggle(null,'");
             org.wings.plaf.Utils.write(device, hookId);
             device.print("','");
             org.wings.plaf.Utils.write(device, popupId);
             device.print("')");
         }
 
         device
                 .print("\">\n");
 
         // Special handling: Render title of STitledBorder
         if (component.getBorder() instanceof STitledBorder) {
             STitledBorder titledBorder = (STitledBorder) component.getBorder();
             device.print("<div class=\"legend\" style=\"");
             titledBorder.getTitleAttributes().write(device);
             device.print("\">");
             device.print(titledBorder.getTitle());
             device.print("</div>");
         }
 
         component.fireRenderEvent(SComponent.START_RENDERING);
     }
 
     protected void writeContent(Device device, SComponent component) throws IOException {
     }
 
     protected void writePostfix(Device device, SComponent component) throws IOException {
         component.fireRenderEvent(SComponent.DONE_RENDERING);
 
         boolean backup = component.getInheritsPopupMenu();
         component.setInheritsPopupMenu(false);
 
         if (component.getComponentPopupMenu() != null)
             component.getComponentPopupMenu().write(device);
 
         component.setInheritsPopupMenu(backup);
 
         device
                 .print("</div><!-- ")
                 .print(component.getName())
                 .print("-->\n");
     }
 }
