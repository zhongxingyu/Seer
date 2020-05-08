 /*
  * Copyright (c) 2005 Your Corporation. All Rights Reserved.
  */
 package org.wings.plaf.css;
 
import org.wings.SComponent;
import org.wings.SContainer;
import org.wings.SLayoutManager;
import org.wings.SScrollPaneLayout;
 import org.wings.io.Device;
 import org.wings.plaf.LayoutCG;
 
 import java.io.IOException;
 import java.util.Map;
 
 public class ScrollPaneLayoutCG extends AbstractLayoutCG {
 
     public void write(Device d, SLayoutManager l)
             throws IOException {
         SScrollPaneLayout layout = (SScrollPaneLayout) l;
 
         if (layout.isPaging())
             writePaging(d, layout);
         else
             writeNonePaging(d, layout);
     }
 
     private void writeNonePaging(Device d, SScrollPaneLayout layout) throws IOException {
         Map components = layout.getComponents();
         SComponent center = (SComponent) components.get(SScrollPaneLayout.VIEWPORT);
         writeComponent(d, center);
     }
 
     private void writePaging(Device d, SScrollPaneLayout layout) throws IOException {
         SContainer container = layout.getContainer();
 
         Map components = layout.getComponents();
         SComponent north = (SComponent) components.get(SScrollPaneLayout.NORTH);
         SComponent east = (SComponent) components.get(SScrollPaneLayout.EAST);
         SComponent center = (SComponent) components.get(SScrollPaneLayout.VIEWPORT);
         SComponent west = (SComponent) components.get(SScrollPaneLayout.WEST);
         SComponent south = (SComponent) components.get(SScrollPaneLayout.SOUTH);
 
         d.print("\n<table class=\"SScrollPaneLayout\"");
         Utils.printCSSInlinePreferredSize(d, container.getPreferredSize());
         d.print("><tbody>");
 
         if (north != null) {
             d.print("<tr height=\"0%\">");
             if (west != null)
                 d.print("<td width=\"0%\"></td>");
 
             d.print("<td width=\"100%\">");
             writeComponent(d, north);
             d.print("</td>");
 
             if (east != null)
                 d.print("<td width=\"0%\"></td>");
             d.print("</tr>\n");
         }
 
         d.print("<tr height=\"100%\">");
         if (west != null) {
             d.print("<td width=\"0%\">");
             writeComponent(d, west);
             d.print("</td>");
         }
         if (center != null) {
             d.print("<td width=\"100%\">");
             writeComponent(d, center);
             d.print("</td>");
         }
         if (east != null) {
             d.print("<td width=\"0%\">");
             writeComponent(d, east);
             d.print("</td>");
         }
         d.print("</tr>\n");
 
         if (south != null) {
             d.print("<tr height=\"0%\">");
             if (west != null)
                 d.print("<td width=\"0%\"></td>");
 
             d.print("<td width=\"100%\">");
             writeComponent(d, south);
             d.print("</td>");
 
             if (east != null)
                 d.print("<td width=\"0%\"></td>");
             d.print("</tr>\n");
         }
 
         d.print("</tbody></table>\n");
     }
 
     protected void writeComponent(Device d, SComponent c)
             throws IOException {
         c.write(d);
     }
 }
 
 
