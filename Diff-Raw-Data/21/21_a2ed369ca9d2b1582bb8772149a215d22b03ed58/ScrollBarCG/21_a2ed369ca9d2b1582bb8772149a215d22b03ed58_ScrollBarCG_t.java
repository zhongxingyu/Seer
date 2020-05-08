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
 import org.wings.plaf.css.script.LayoutFillScript;
 
 import java.io.IOException;
 
 /**
  * CG for a scrollbar.
  *
  * @author holger
  */
 public final class ScrollBarCG extends org.wings.plaf.css.AbstractComponentCG implements org.wings.plaf.ScrollBarCG {
     private static final long serialVersionUID = 1L;
     public static final int FORWARD = 0;
     public static final int BACKWARD = 1;
     public static final int FORWARD_BLOCK = 2;
     public static final int BACKWARD_BLOCK = 3;
     public static final int FIRST = 4;
     public static final int LAST = 5;
     private final static SIcon[][][] DEFAULT_ICONS = new SIcon[2][6][2];
 
     static {
         String[] postfixes = new String[6];
         String[] prefixes = new String[6];
         for (int orientation = 0; orientation < 2; orientation++) {
             prefixes[BACKWARD] = "";
             prefixes[FORWARD] = "";
             prefixes[FIRST] = "Margin";
             prefixes[LAST] = "Margin";
             prefixes[FORWARD_BLOCK] = "Block";
             prefixes[BACKWARD_BLOCK] = "Block";
             if (orientation == SConstants.VERTICAL) {
                 postfixes[BACKWARD] = "Up";
                 postfixes[FORWARD] = "Down";
                 postfixes[FIRST] = "Up";
                 postfixes[LAST] = "Down";
                 postfixes[BACKWARD_BLOCK] = "Up";
                 postfixes[FORWARD_BLOCK] = "Down";
             } else {
                 postfixes[BACKWARD] = "Left";
                 postfixes[FORWARD] = "Right";
                 postfixes[FIRST] = "Left";
                 postfixes[LAST] = "Right";
                 postfixes[BACKWARD_BLOCK] = "Left";
                 postfixes[FORWARD_BLOCK] = "Right";
             }
 
             for (int direction = 0; direction < postfixes.length; direction++) {
                 DEFAULT_ICONS[orientation][direction][0] =
                         new SResourceIcon("org/wings/icons/"
                         + prefixes[direction]
                         + "Scroll"
                         + postfixes[direction] + ".gif");
                 DEFAULT_ICONS[orientation][direction][1] =
                         new SResourceIcon("org/wings/icons/Disabled"
                         + prefixes[direction]
                         + "Scroll"
                         + postfixes[direction] + ".gif");
             }
         }
     }
 
     public void writeInternal(Device d, SComponent c)
             throws IOException {
         SScrollBar sb = (SScrollBar) c;
 
         String style = sb.getStyle();
         if (sb.getOrientation() == SConstants.VERTICAL) {
             sb.setStyle(style + " SScrollBar_vertical");
             writeVerticalScrollbar(d, sb);
         } else {
             sb.setStyle(style + " SScrollBar_horizontal");
             writeHorizontalScrollbar(d, sb);
         }
         sb.setStyle(style);
     }
 
     private void writeVerticalScrollbar(Device device, SScrollBar sb) throws IOException {
         final int value = sb.getValue();
         final int blockIncrement = sb.getBlockIncrement();
         final int extent = sb.getExtent();
         final int minimum = sb.getMinimum();
         final int maximum = sb.getMaximum();
         final int last = maximum - extent;
 
         final boolean isMsIEBrowser = Utils.isMSIE(sb);
         final String rowHeightExpanded = isMsIEBrowser ? "" : " height=\"100%\"";
         final String rowHeightFlattened = isMsIEBrowser ? "" : " height=\"0%\"";
 
         SDimension preferredSize = sb.getPreferredSize();
         String height = preferredSize != null ? preferredSize.getHeight() : null;
         boolean clientLayout = isMSIE(sb) && height != null && !"auto".equals(height);
 
         Utils.printNewline(device, sb);
         device.print("<table");
 
         if (clientLayout) {
             Utils.optAttribute(device, "layoutHeight", height);
             preferredSize.setHeight(null);
         }
 
         writeAllAttributes(device, sb);
 
         if (clientLayout) {
             preferredSize.setHeight(height);
             sb.getSession().getScriptManager().addScriptListener(new LayoutFillScript(sb.getName()));
         }
 
         device.print("><tbody>")
            .print("<tr").print(rowHeightFlattened).print(">")
            .print("<td><table class=\"buttons\"><tbody>");
 
         device.print("<tr><td");
         Utils.printClickability(device, sb, "" + minimum, true, sb.getShowAsFormComponent());
         device.print(">");
 
         writeIcon(device, DEFAULT_ICONS[SConstants.VERTICAL][FIRST][0]);
         device.print("</td></tr>");
         device.print("<tr><td");
         Utils.printClickability(device, sb, "" + (Math.max(minimum, value - blockIncrement)), true, sb.getShowAsFormComponent());
         device.print(">");
 
         writeIcon(device, DEFAULT_ICONS[SConstants.VERTICAL][BACKWARD_BLOCK][0]);
         device.print("</td></tr>");
         device.print("<tr><td");
         Utils.printClickability(device, sb, "" + (value - 1), true, sb.getShowAsFormComponent());
         device.print(">");
         writeIcon(device, DEFAULT_ICONS[SConstants.VERTICAL][BACKWARD][0]);
         device.print("</td></tr>");
 
         device.print("</tbody></table></td>").print("</tr>");
         Utils.printNewline(device, sb);
         device.print("<tr yweight=\"100\"").print(rowHeightExpanded).print(">")
                 .print("<td><table class=\"slider\" height=\"100%\"><tbody>");
 
         int range = maximum - minimum;
         int iconWidth = DEFAULT_ICONS[SConstants.VERTICAL][FIRST][0].getIconWidth();
         verticalArea(device, "#eeeeff", value * 100 / range, iconWidth);
         verticalArea(device, "#cccccc", extent * 100 / range, iconWidth);
         verticalArea(device, "#eeeeff", (range - value - extent) * 100 / range, iconWidth);
 
         device.print("</tbody></table></td>")
                 .print("</tr>");
         Utils.printNewline(device, sb);
         device.print("<tr").print(rowHeightFlattened).print(">")
            .print("<td><table class=\"buttons\"><tbody>");
 
         device.print("<tr><td");
         Utils.printClickability(device, sb, "" + (value + 1), true, sb.getShowAsFormComponent());
         device.print(">");
 
         writeIcon(device, DEFAULT_ICONS[SConstants.VERTICAL][FORWARD][0]);
         device.print("</td></tr>");
         device.print("<tr><td");
         Utils.printClickability(device, sb, "" + (Math.min(last, value + blockIncrement)), true, sb.getShowAsFormComponent());
         device.print(">");
 
         writeIcon(device, DEFAULT_ICONS[SConstants.VERTICAL][FORWARD_BLOCK][0]);
         device.print("</td></tr>");
         device.print("<tr><td");
         Utils.printClickability(device, sb, "" + last, true, sb.getShowAsFormComponent());
         device.print(">");
         writeIcon(device, DEFAULT_ICONS[SConstants.VERTICAL][LAST][0]);
         device.print("</td></tr>");
 
         device.print("</tbody></table></td>")
                 .print("</tr>")
                 .print("</tbody></table>");
     }
 
     private void verticalArea(Device d, String s, int v, int iconWidth) throws IOException {
         d.print("<tr><td style=\"background-color: ");
         d.print(s);
         d.print("\" height=\"");
         d.print(v + "%");
         d.print("\" width=\"");
         d.print(iconWidth);
         d.print("\"></td></tr>");
     }
 
     private void writeHorizontalScrollbar(Device d, SScrollBar sb) throws IOException {
         final int value = sb.getValue();
         final int blockIncrement = sb.getBlockIncrement();
         final int extent = sb.getExtent();
         final int minimum = sb.getMinimum();
         final int maximum = sb.getMaximum();
         final int last = maximum - extent;
 
         d.print("<table");
         writeAllAttributes(d, sb);
         d.print("><tbody><tr>")
                 .print("<td width=\"1%\"><table class=\"buttons\"><tbody><tr>");
 
         d.print("<td");
         Utils.printClickability(d, sb, "" + minimum, true, sb.getShowAsFormComponent());
         d.print(">");
 
         writeIcon(d, DEFAULT_ICONS[SConstants.HORIZONTAL][FIRST][0]);
         d.print("</td>");
         d.print("<td");
         Utils.printClickability(d, sb, "" + (Math.max(minimum, value - blockIncrement)), true, sb.getShowAsFormComponent());
         d.print(">");
 
         writeIcon(d, DEFAULT_ICONS[SConstants.HORIZONTAL][BACKWARD_BLOCK][0]);
         d.print("</td>");
         d.print("<td");
         Utils.printClickability(d, sb, "" + (value - 1), true, sb.getShowAsFormComponent());
         d.print(">");
         writeIcon(d, DEFAULT_ICONS[SConstants.HORIZONTAL][BACKWARD][0]);
         d.print("</td>");
 
         d.print("</tr></tbody></table></td>")
                 .print("<td width=\"100%\"><table class=\"slider\" width=\"100%\"><tbody><tr>");
 
         int range = maximum - minimum;
         int iconHeight = DEFAULT_ICONS[SConstants.HORIZONTAL][FIRST][0].getIconHeight();
         horizontalArea(d, "#eeeeff", value * 100 / range, iconHeight);
         horizontalArea(d, "#cccccc", extent * 100 / range, iconHeight);
         horizontalArea(d, "#eeeeff", (range - value - extent) * 100 / range, iconHeight);
 
         d.print("</tr></tbody></table></td>")
                 .print("<td width=\"1%\"><table class=\"buttons\"><tbody><tr>");
 
         d.print("<td");
         Utils.printClickability(d, sb, "" + (value + 1), true, sb.getShowAsFormComponent());
         d.print(">");
 
         writeIcon(d, DEFAULT_ICONS[SConstants.HORIZONTAL][FORWARD][0]);
         d.print("</td>");
         d.print("<td");
         Utils.printClickability(d, sb, "" + (Math.min(last, value + blockIncrement)), true, sb.getShowAsFormComponent());
         d.print(">");
 
         writeIcon(d, DEFAULT_ICONS[SConstants.HORIZONTAL][FORWARD_BLOCK][0]);
         d.print("</td>");
         d.print("<td");
         Utils.printClickability(d, sb, "" + last, true, sb.getShowAsFormComponent());
         d.print(">");
         writeIcon(d, DEFAULT_ICONS[SConstants.HORIZONTAL][LAST][0]);
         d.print("</td>");
 
         d.print("</tr></tbody></table></td>")
                 .print("</tr></tbody></table>");
     }
 
     private void horizontalArea(Device d, String s, int v, int iconHeight) throws IOException {
         d.print("<td style=\"background-color: ");
         d.print(s);
         d.print("\" width=\"");
         d.print(v + "%");
         d.print("\" height=\"");
         d.print(iconHeight);
         d.print("\"></td>");
     }
 
     private void writeIcon(Device device, SIcon icon) throws IOException {
         device.print("<img class=\"nopad\"");
         Utils.optAttribute(device, "src", icon.getURL());
         Utils.optAttribute(device, "width", icon.getIconWidth());
         Utils.optAttribute(device, "height", icon.getIconHeight());
         Utils.attribute(device, "alt", icon.getIconTitle());
         device.print("/>");
     }
 }
