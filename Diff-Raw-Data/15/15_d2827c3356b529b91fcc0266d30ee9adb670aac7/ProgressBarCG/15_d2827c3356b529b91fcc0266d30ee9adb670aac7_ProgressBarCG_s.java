 // DO NOT EDIT! Your changes will be lost: generated from '/home/hengels/jdevel/wings/src/org/wings/plaf/css1/ProgressBar.plaf'
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
 import org.wings.io.Device;
 import org.wings.plaf.CGManager;
 
 import java.io.IOException;
 
 public class ProgressBarCG
         extends AbstractComponentCG
         implements SConstants, org.wings.plaf.ProgressBarCG {
 
 //--- byte array converted template snippets.
 
     public void installCG(final SComponent comp) {
         final SProgressBar component = (SProgressBar) comp;
         final CGManager manager = component.getSession().getCGManager();
         Object value;
         Object previous;
         value = manager.getObject("SProgressBar.borderColor", java.awt.Color.class);
         if (value != null) {
             component.setBorderColor((java.awt.Color) value);
         }
         value = manager.getObject("SProgressBar.filledColor", java.awt.Color.class);
         if (value != null) {
             component.setFilledColor((java.awt.Color) value);
         }
         value = manager.getObject("SProgressBar.foreground", java.awt.Color.class);
         if (value != null) {
             component.setForeground((java.awt.Color) value);
         }
         value = manager.getObject("SProgressBar.preferredSize", SDimension.class);
         if (value != null) {
             component.setPreferredSize((SDimension) value);
         }
         value = manager.getObject("SProgressBar.unfilledColor", java.awt.Color.class);
         if (value != null) {
             component.setUnfilledColor((java.awt.Color) value);
         }
     }
 
     public void uninstallCG(final SComponent comp) {
     }
 
 //--- code from common area in template.
     private static final SIcon BLIND_ICON = new SResourceIcon("org/wings/icons/blind.gif");
 
 
 //--- end code from common area in template.
 
 
     public void writeContent(final Device device,
                              final SComponent _c)
             throws IOException {
         final SProgressBar component = (SProgressBar) _c;
 
 //--- code from write-template.
         String style = component.getStyle();
 
         SDimension size = component.getPreferredSize();
         int width = size != null ? size.getIntWidth() : 200;
         int height = size != null ? size.getIntHeight() : 5;
 
         if (component.isStringPainted()) {
             device.print("<table><tr><td>");
         }
         if (component.isBorderPainted()) {
             device.print("<table cellpadding=\"1\"><tr><td");
             Utils.optAttribute(device, "bgcolor", component.getBorderColor());
             device.print(">");
         }
 
         device.print("<table>");
         device.print("<tr>");
         device.print("<td");
        Utils.optAttribute(device, "bgcolor", component.getFilledColor());
         device.print(">");
         device.print("<img");
         Utils.optAttribute(device, "src", BLIND_ICON.getURL());
         device.print(" width=\"");
         device.print(String.valueOf(width * component.getPercentComplete()));
         device.print("\"");
         device.print(" height=\"");
         device.print(String.valueOf(height));
         device.print("\"></td>");
         device.print("<td");
        Utils.optAttribute(device, "bgcolor", component.getUnfilledColor());
         device.print(">");
         device.print("<img");
         Utils.optAttribute(device, "src", BLIND_ICON.getURL());
         device.print(" width=\"");
         device.print(String.valueOf(width * (1 - component.getPercentComplete())));
         device.print("\"");
         device.print(" height=\"");
         device.print(String.valueOf(height));
         device.print("\">");
         device.print("</td>");
         device.print("</tr>");
         device.print("</table>");
         if (component.isBorderPainted()) {
             device.print("</td></tr></table>");
         }
 
         if (component.isStringPainted()) {
             device.print("</td></tr><tr><td align=\"center\">");
             if (style != null) {
                 device.print("<span class=\"");
                 Utils.write(device, style);
                 device.print("\">");
             }
             Utils.write(device, component.getString());
             if (style != null) {
                 device.print("</span>");
             }
             device.print("</td></tr></table>");
         }
 //--- end code from write-template.
     }
 }
