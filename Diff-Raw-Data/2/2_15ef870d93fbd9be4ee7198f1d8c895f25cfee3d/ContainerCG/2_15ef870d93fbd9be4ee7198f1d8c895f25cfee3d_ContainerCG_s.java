 package org.wings.plaf.css.msie;
 
 import org.wings.plaf.css.*;
 import org.wings.plaf.css.BorderLayoutCG;
 import org.wings.io.Device;
 import org.wings.*;
 
 public final class ContainerCG extends org.wings.plaf.css.ContainerCG {
     private static final long serialVersionUID = 1L;
 
     public void writeInternal(final Device device, final SComponent component) throws java.io.IOException {
         SContainer container = (SContainer) component;
         SLayoutManager layout = container.getLayout();
 
         boolean requiresFillBehaviour = false;
         SDimension preferredSize = null;
         String height = null;
        if (layout instanceof BorderLayoutCG) {
             preferredSize = container.getPreferredSize();
             if (preferredSize != null) {
                 height = preferredSize.getHeight();
                 if (height != null)
                     requiresFillBehaviour = true;
             }
         }
 
         if (requiresFillBehaviour) {
             device.print("<table style=\"behavior:url(../fill.htc)\"");
             Utils.optAttribute(device, "intendedHeight", height);
             preferredSize.setHeight(null);
         }
         else
             device.print("<table");
 
         writeAllAttributes(device, component);
         if (requiresFillBehaviour)
             preferredSize.setHeight(height);
 
         Utils.writeEvents(device, component, null);
         device.print(">");
 
         // special case templateLayout, open cell
         boolean writeTableData = container.getLayout() instanceof STemplateLayout
             || container.getLayout() instanceof SCardLayout;
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
