 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.provider.portletui.beans.TableCellBean;
 import org.gridlab.gridsphere.provider.portletui.beans.TableRowBean;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 
 /**
  * A <code>TableCellTag</code> represents a table cell element contained by a <code>TableRowTag</code>
  */
 public class TableCellTag extends BaseComponentTag {
 
     protected TableCellBean cellBean = null;
     protected String width = null;
     protected String cellSpacing = null;
 
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
      * Sets the table cell cell spacing
      *
      * @param cellSpacing the table cell cell spacing
      */
    public void setCellspacing(String cellSpacing) {
         this.cellSpacing = cellSpacing;
     }
 
     /**
      * Returns the table cell cell spacing
      *
      * @return the table cell cell spacing
      */
    public String getCellspacing() {
         return cellSpacing;
     }
 
     /**
      * Sets the table cell bean
      *
      * @param cellBean the table cell bean
      */
     public void setCellBean(TableCellBean cellBean) {
         this.cellBean = cellBean;
     }
 
     /**
      * Returns the table cell bean
      *
      * @return the table cell bean
      */
     public TableCellBean getCellBean() {
         return cellBean;
     }
 
     public int doStartTag() throws JspException {
         if (!beanId.equals("")) {
             cellBean = (TableCellBean)pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
             if (cellBean == null) cellBean = new TableCellBean();
         } else {
             cellBean = new TableCellBean();
             if (cellSpacing != null) cellBean.setCellSpacing(cellSpacing);
             if (width != null) cellBean.setWidth(width);
         }
 
         TableRowTag rowTag = (TableRowTag)getParent();
         if (rowTag.getHeader()) {
             cellBean.setCssStyle(TableRowBean.TABLE_HEADER_STYLE);
         }
         try {
             JspWriter out = pageContext.getOut();
             out.print(cellBean.toStartString());
         } catch (Exception e) {
             throw new JspException(e.getMessage());
         }
 
         return EVAL_BODY_INCLUDE;
     }
 
     public int doEndTag() throws JspException {
 
         try {
             JspWriter out = pageContext.getOut();
             out.print(cellBean.toEndString());
         } catch (Exception e) {
             throw new JspException(e.getMessage());
         }
         return EVAL_PAGE;
     }
 }
