 package org.wings.plaf.css.msie;
 
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SScrollPaneLayout;
 import org.wings.SDimension;
 import org.wings.io.Device;
 import org.wings.plaf.css.Utils;
 
 import java.io.IOException;
 import java.util.Map;
 
 public class ScrollPaneLayoutCG extends org.wings.plaf.css.ScrollPaneLayoutCG {
 
     private static final long serialVersionUID = 1L;
 
 
     protected void writePaging(Device d, SScrollPaneLayout layout) throws IOException {
         SDimension preferredSize = layout.getContainer().getPreferredSize();
        if (preferredSize == null) {
            super.write(d, layout);
            return;
        }
        String height = preferredSize.getHeight();
        if (height == null || "auto".equals(height)) {
            super.write(d, layout);
             return;
         }
 
         // special implementation with expressions is only required, if the center component
         // shall consume the remaining height
 
         Map components = layout.getComponents();
         SComponent north = (SComponent) components.get(SScrollPaneLayout.NORTH);
         SComponent east = (SComponent) components.get(SScrollPaneLayout.EAST);
         SComponent center = (SComponent) components.get(SScrollPaneLayout.VIEWPORT);
         SComponent west = (SComponent) components.get(SScrollPaneLayout.WEST);
         SComponent south = (SComponent) components.get(SScrollPaneLayout.SOUTH);
 
         openLayouterBody(d, layout);
 
         if (north != null) {
             d.print("<tr>");
             if (west != null) {
                 d.print("<td width=\"0%\"></td>");
             }
 
             d.print("<td width=\"100%\">");
             writeComponent(d, north);
             d.print("</td>");
 
             if (east != null) {
                 d.print("<td width=\"0%\"></td>");
             }
             d.print("</tr>\n");
         }
 
         d.print("<tr yweight=\"100\">");
         if (west != null) {
             d.print("<td width=\"0%\">");
             writeComponent(d, west);
             d.print("</td>");
         }
         if (center != null) {
             d.print("<td width=\"100%\"");
             Utils.printTableCellAlignment(d, center, SConstants.LEFT_ALIGN, SConstants.TOP_ALIGN);
             d.print(">");
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
             d.print("<tr>");
             if (west != null) {
                 d.print("<td width=\"0%\"></td>");
             }
 
             d.print("<td width=\"100%\">");
             writeComponent(d, south);
             d.print("</td>");
 
             if (east != null) {
                 d.print("<td width=\"0%\"></td>");
             }
             d.print("</tr>\n");
         }
 
         closeLayouterBody(d, layout);
     }
 }
