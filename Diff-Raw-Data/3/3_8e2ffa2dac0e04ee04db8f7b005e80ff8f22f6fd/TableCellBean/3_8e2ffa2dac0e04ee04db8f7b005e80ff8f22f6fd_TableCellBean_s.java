 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Iterator;
 
 /**
  * A <code>TableCellBean</code> represents a table cell and is contained by a <code>TableRowBean</code>
  */
 public class TableCellBean extends BeanContainer implements TagBean {
 
     protected String width = null;
     protected String height = null;
     protected String align = null;
     protected String valign = null;
     protected String colspan = null;
     protected String rowspan = null;
 
     protected String TABLE_CELL_STYLE = "portlet-section-body";
 
     /**
      * Constructs a default table cell bean
      */
     public TableCellBean() {
         super();
         this.cssClass = TABLE_CELL_STYLE;
     }
 
     public TableCellBean(BaseComponentBean compBean) {
         super();
         this.addBean(compBean);
         this.cssClass = TABLE_CELL_STYLE;
     }
 
     /**
      * Constructs a table cell bean from a supplied portlet request and bean identifier
      *
      * @param req    the portlet request
      * @param beanId the bean identifier
      */
     public TableCellBean(HttpServletRequest req, String beanId) {
         super();
         this.request = req;
         this.beanId = beanId;
         this.cssClass = TABLE_CELL_STYLE;
     }
 
     /**
      * Sets the table alignment e.g. "left", "center" or "right"
      *
      * @param align the table alignment
      */
     public void setAlign(String align) {
         this.align = align;
     }
 
     /**
      * Returns the table alignment e.g. "left", "center" or "right"
      *
      * @return the table alignment
      */
     public String getAlign() {
         return align;
     }
 
     /**
      * Sets the table vertical alignment e.g. "top", "middle", "bottom" or "baseline"
      *
      * @param valign the table vertical alignment
      */
     public void setValign(String valign) {
         this.valign = valign;
     }
 
     /**
      * Returns the table vertical alignment e.g. "top", "middle", "bottom" or "baseline"
      *
      * @return the table vertical alignment
      */
     public String getValign() {
         return valign;
     }
 
     /**
      * Sets the table cell width
      *
      * @param width the table cell width
      */
     public void setWidth(String width) {
         this.width = width;
     }
 
     /**
      * Returns the table cell width
      *
      * @return the table cell width
      */
     public String getWidth() {
         return width;
     }
 
     /**
      * Sets the table cell height
      *
      * @param height the table cell height
      */
     public void setHeight(String height) {
         this.height = height;
     }
 
     /**
      * Returns the table cell row span
      *
      * @return the table cell row span
      */
     public String getRowspan() {
         return rowspan;
     }
 
     /**
      * Sets the table cell row span
      *
      * @param rowspan the table cell row span
      */
     public void setRowspan(String rowspan) {
         this.rowspan = rowspan;
     }
 
     /**
      * Returns the table cell col span
      *
      * @return the table cell col span
      */
     public String getColspan() {
         return colspan;
     }
 
     /**
      * Sets the table cell col span
      *
      * @param colspan the table cell col span
      */
     public void setColspan(String colspan) {
         this.colspan = colspan;
     }
 
     /**
      * Returns the table cell height
      *
      * @return the table cell height
      */
     public String getHeight() {
         return height;
     }
 
     public String toStartString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<td ");
         sb.append(getFormattedCss());
         if (width != null) sb.append(" width=\"" + width + "\"");
         if (height != null) sb.append(" height=\"" + height + "\"");
        if (align != null) sb.append(" layout=\"" + align + "\"");
        if (valign != null) sb.append(" align=\"" + align + "\"");
         if (valign != null) sb.append(" valign=\"" + valign + "\"");
         if (rowspan != null) sb.append(" rowspan=\"" + rowspan + "\"");
         if (colspan != null) sb.append(" colspan=\"" + colspan + "\"");
         sb.append(">");
         Iterator it = container.iterator();
         while (it.hasNext()) {
             BaseComponentBean bean = (BaseComponentBean) it.next();
             sb.append(bean.toStartString());
             sb.append(bean.toEndString());
         }
         return sb.toString();
     }
 
     public String toEndString() {
         return "</td>";
     }
 
 }
