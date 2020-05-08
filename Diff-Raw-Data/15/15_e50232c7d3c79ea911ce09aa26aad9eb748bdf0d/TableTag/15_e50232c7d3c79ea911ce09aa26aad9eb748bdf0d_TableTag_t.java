 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.provider.portletui.beans.TableBean;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 
 /**
  * A <code>TableTag</code> represents a table element and is defined by a <code>DefaultTableModel</code>
  */
 public class TableTag extends BaseComponentTag {
 
     protected TableBean tableBean = null;
     protected String cellSpacing = null;
     protected String width = null;
 
     /**
      * Sets the table model associated with this table
      *
      * @param tableModel the table model associated with this table
      */
     public void setTableModel(DefaultTableModel tableModel) {
         this.tableBean.setTableModel(tableModel);
     }
 
     /**
      * Returns the table model associated with this table
      *
      * @return the table model associated with this table
      */
     public DefaultTableModel getTableModel() {
         return tableBean.getTableModel();
     }
 
     /**
      * Sets the table bean
      *
      * @param tableBean the table bean
      */
     public void setTableBean(TableBean tableBean) {
         this.tableBean = tableBean;
     }
 
     /**
      * Returns the table bean
      *
      * @return the table bean
      */
     public TableBean getTableBean() {
         return tableBean;
     }
 
     /**
      * Sets the table cell spacing
      *
      * @param cellSpacing the table cell spacing
      */
    public void setCellspacing(String cellSpacing) {
         this.cellSpacing = cellSpacing;
     }
 
     /**
      * Returns the table cell spacing
      *
      * @return the table cell spacing
      */
    public String getCellspacing() {
         return cellSpacing;
     }
 
     /**
      * Sets the table width
      *
      * @param width the table width
      */
     public void setWidth(String width) {
         this.width = width;
     }
 
     /**
      * Returns the table width
      *
      * @return the table width
      */
     public String getWidth() {
         return width;
     }
 
     public int doStartTag() throws JspException {
 
         boolean includeBody = true;
 
         if (!beanId.equals("")) {
             tableBean = (TableBean)pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
             if (tableBean == null) {
                 tableBean = new TableBean();
             } else {
                 includeBody = false;
             }
         } else {
             tableBean = new TableBean();
 
             if (width != null) tableBean.setWidth(width);
             if (cellSpacing != null) tableBean.setCellSpacing(cellSpacing);
         }
 
         try {
             JspWriter out = pageContext.getOut();
             out.print(tableBean.toStartString());
         } catch (Exception e) {
             throw new JspException(e.getMessage());
         }
         if (includeBody) {
             return EVAL_BODY_INCLUDE;
         } else {
             return SKIP_BODY;
         }
     }
 
     public int doEndTag() throws JspException {
 
         try {
             JspWriter out = pageContext.getOut();
             out.print(tableBean.toEndString());
         } catch (Exception e) {
             throw new JspException(e.getMessage());
         }
 
         return EVAL_PAGE;
     }
 }
