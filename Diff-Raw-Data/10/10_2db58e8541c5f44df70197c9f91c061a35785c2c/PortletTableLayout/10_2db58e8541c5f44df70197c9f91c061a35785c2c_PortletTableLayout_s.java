 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.ArrayList;
 
 /**
  * The <code>PortletTableLayout</code> is a concrete implementation of the <code>PortletFrameLayout</code>
  * that organizes portlets into a grid with a provided number of columns.
  * Portlets are arranged in column-wise order starting from the left most column.
  */
 public class PortletTableLayout extends PortletFrameLayout implements Cloneable {
 
     protected String style = null;
 
     /**
      * Constructs an instance of PortletTableLayout
      */
     public PortletTableLayout() {
     }
 
     /**
      * Returns the CSS style name for the grid-layout.
      *
      * @return css style name
      */
     public String getStyle() {
         return style;
     }
 
     /**
      * Sets the CSS style name for the grid-layout.
      * This needs to be set if you want to have transparent portlets, if there is
      * no background there can't be a real transparent portlet.
      * Most likely one sets just the background in that one.
      *
     * @param style  css style of the that layout
      */
     public void setStyle(String style) {
         this.style = style;
     }
 
     private PortletComponent getMaximizedComponent(List components) {
         PortletComponent p = null;
 
         for (int i=0;i<components.size();i++) {
             p = (PortletComponent)components.get(i);
             if (p instanceof PortletLayout) {
                PortletComponent layout = this.getMaximizedComponent(((PortletLayout)p).getPortletComponents());
                if (layout!=null) {
                    p = layout;
                }
             }
             if (p.getWidth().equals("100%")) {
                 return p;
             }
         }
         return null;
     }
 
     /**
      * Renders the portlet grid layout component
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
     * @throws IOException if an I/O error occurs during rendering
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         PortletResponse res = event.getPortletResponse();
         PrintWriter out = res.getWriter();
 
         PortletComponent p = null;
 
         // check if one window is maximized
 
         for (int i=0;i<components.size();i++) {
             p = (PortletComponent)components.get(i);
             if (p instanceof PortletLayout) {
                 PortletComponent maxi = getMaximizedComponent(components);
                 if (maxi!=null) {
                    p.doRender(event);
                     return;
                 }
             }
         }
 
         // starting of the gridtable
         out.println("<table ");
         if (this.style!=null) {
                    out.print("class=\""+this.style+"\" ");
         }
         out.println("border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tbody>");
         out.println("<tr>");
         for (int i=0;i<components.size();i++) {
             p = (PortletComponent) components.get(i);
             out.println("<td valign=\"top\" width=\""+p.getWidth()+"%\">");
             if (p.getVisible()) {
                 p.doRender(event);
                 //out.println("grid comp: "+i);
             }
             out.println("</td> ");
         }
         out.println("</tr></tbody></table>");
 
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletTableLayout g = (PortletTableLayout)super.clone();
         g.style = this.style;
         return g;
     }
 
 }
 
