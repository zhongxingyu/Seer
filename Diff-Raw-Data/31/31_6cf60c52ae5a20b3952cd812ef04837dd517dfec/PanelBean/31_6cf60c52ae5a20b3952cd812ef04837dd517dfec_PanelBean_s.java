 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import org.gridlab.gridsphere.portlet.PortletRequest;
 
 /**
  * A <code>PanelBean</code> provides a stylized table that is generally used as a container for
  * <code>FrameBean</code>s
  */
 public class PanelBean extends BeanContainer implements TagBean {
 
     public static final String PANEL_STYLE = "portlet-pane";
     public static final String PANEL_WIDTH = "100%";
     public static final String PANEL_SPACING = "0";
 
     protected String cellSpacing = PANEL_SPACING;
     protected String width = PANEL_WIDTH;
 
     private String cols = "100%";
    private String[] colArray;
     private int numCols = 1;
 
     /**
      * Constructs a default panel bean
      */
     public PanelBean() {
         this.cssStyle = PANEL_STYLE;
     }
 
     /**
      * Constructs a panel bean from a supplied portlet request and bean identifier
      *
      * @param req the portlet request
      * @param beanId the bean identifier
      */
     public PanelBean(PortletRequest req, String beanId) {
         this.cssStyle = PANEL_STYLE;
         this.beanId = beanId;
         this.request = req;
     }
 
     /**
      * Sets the panel (table) width
      *
      * @param width the panel width
      */
     public void setWidth(String width) {
         this.width = width;
     }
 
     /**
      * Returns the panel (table) width
      *
      * @return the panel width
      */
     public String getWidth() {
         return width;
     }
 
     /**
      * Sets the number of columns in the panel
      *
      * @param cols the number of columns
      */
     public void setCols(String cols) {
         this.cols = cols;
     }
 
     /**
      * Returns the number of columns in the panel
      *
      * @return the number of columns in the panel
      */
     public String getCols() {
         return cols;
     }
 
     /**
      * Sets the number of columns in the panel
      *
      * @param numCols the number of columns
      */
     public void setNumCols(int numCols) {
         this.numCols = numCols;
     }
 
     /**
      * Returns the number of columns in the panel
      *
      * @return the number of columns in the panel
      */
     public int getNumCols() {
         return numCols;
     }
 
     /**
      * Sets the column array specifying column widths
      *
      * @param colArray the column array specifying column widths
      */
     public void setColArray(String[] colArray) {
         this.colArray = colArray;
     }
 
     /**
      * Returns the number of columns in the panel
      *
      * @return the number of columns in the panel
      */
     public String[] getColArray() {
         return colArray;
     }
 
     /**
      * Sets the panel (table) cell spacing
      *
      * @param cellSpacing the panel cell spacing
      */
     public void setCellSpacing(String cellSpacing) {
         this.cellSpacing = cellSpacing;
     }
 
     /**
      * Returns the panel (table) cell spacing
      *
      * @return  the panel cell spacing
      */
     public String getCellSpacing() {
         return cellSpacing;
     }
 
     public String toStartString() {
         StringBuffer sb = new StringBuffer();
         sb.append("<table class=\"" + cssStyle + "\" ");
         sb.append(" cellspacing=\"" + cellSpacing + "\" ");
         sb.append(" width=\"" + width + "\" ");
         sb.append(">");
         return sb.toString();
     }
 
     public String toEndString() {
         StringBuffer sb = new StringBuffer();

         int numRows = container.size();
         int i = 0;
         int j = 0;
 
         while (i < numRows) {
             sb.append("<tr>");
             while ((j < numCols) && (i < numRows)) {
                 TagBean tagBean = (TagBean)container.get(i);
                 //System.err.println("in panel bean colArray " + i + " " + colArray[j]);
                 sb.append("<td width=\"" + colArray[j] +"\">");
                 sb.append(tagBean.toStartString());
                 sb.append(tagBean.toEndString());
                 sb.append("</td>");
                 j++; i++;
             }
             j = 0;
             sb.append("</tr>");
         }
 
         sb.append("</table>");
         return sb.toString();
     }
 
 }
