 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 
 /**
  * A <code>TableBean</code> provides a table element
  */
 public class TableBean extends BaseComponentBean implements TagBean {
 
     public static final String CURRENT_PAGE = "tablepage";
     public static final String SHOW_ALL = "showall";
     public static final String SHOW_PAGES = "showpages";
 
     protected DefaultTableModel defaultModel = null;
     protected String cellSpacing = null;
     protected String cellPadding = null;
     protected String border = null;
     protected String background = null;
     protected String width = null;
     protected String align = null;
     protected String valign = null;
     protected int currentPage = 0;
     protected boolean isSortable = false;
     protected boolean isZebra = false;
     protected String sortableId = "t1";
     private int rowCount = 0;
     private int maxRows = -1;
     private boolean showall = false;
     private boolean isJSR = true;
     protected PortletResponse res = null;
     protected String uris = "";
     protected String uriString = "";
     protected String title = null;
     protected int numEntries = 0;
 
 
     /**
      * Constructs a default table bean
      */
     public TableBean() {
         super();
     }
 
     /**
      * Constructs a table bean with a supplied CSS style
      *
      * @param cssStyle the CSS style
      */
     public TableBean(String cssStyle) {
         this.cssClass = cssStyle;
     }
 
     /**
      * Sets the default table model for this table
      *
      * @param defaultModel the table model
      */
     public void setTableModel(DefaultTableModel defaultModel) {
         this.defaultModel = defaultModel;
     }
 
     /**
      * Returns the default table model
      *
      * @return the default table model
      */
     public DefaultTableModel getTableModel() {
         return defaultModel;
     }
 
     public int getNumEntries() {
         return numEntries;
     }
 
     public void setNumEntries(int numEntries) {
         this.numEntries = numEntries;
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
      * Returns the table vertical alignment e.g. 'top', 'bottom'
      *
      * @return the table vertical alignment
      */
     public String getValign() {
         return valign;
     }
 
     /**
      * Sets the table horizontal alignment, e.g. 'top', 'bottom'
      *
      * @param valign the tables horizontal alignment
      */
     public void setValign(String valign) {
         this.valign = valign;
     }
 
     /**
      * Sets the table cell spacing
      *
      * @param cellSpacing the table cell spacing
      */
     public void setCellSpacing(String cellSpacing) {
         this.cellSpacing = cellSpacing;
     }
 
     /**
      * Returns the table cell spacing
      *
      * @return the table cell spacing
      */
     public String getCellSpacing() {
         return cellSpacing;
     }
 
     /**
      * Sets the table cell spacing
      *
      * @param cellPadding the table cell padding
      */
     public void setCellPadding(String cellPadding) {
         this.cellPadding = cellPadding;
     }
 
     /**
      * Returns the table cell padding
      *
      * @return the table cell padding
      */
     public String getCellPadding() {
         return cellPadding;
     }
 
     /**
      * Sets the table background
      *
      * @param background the table background
      */
     public void setBackground(String background) {
         this.background = background;
     }
 
     /**
      * Returns the table background
      *
      * @return the table background
      */
     public String getBackground() {
         return background;
     }
 
     /**
      * Sets the table border
      *
      * @param border the table border
      */
     public void setBorder(String border) {
         this.border = border;
     }
 
     /**
      * Returns the tableborder
      *
      * @return the table border
      */
     public String getBorder() {
         return border;
     }
 
     public void setZebra(boolean isZebra) {
         this.isZebra = isZebra;
     }
 
     public boolean getZebra() {
         return isZebra;
     }
 
     public void setSortable(boolean isSortable) {
         this.isSortable = isSortable;
     }
 
     public boolean getSortable() {
         return isSortable;
     }
 
     public void setSortableID(String sortableId) {
         this.sortableId = sortableId;
     }
 
     public String getSortableID() {
         return sortableId;
     }
 
     public void setRowCount(int rowCount) {
         this.rowCount = rowCount;
     }
 
     public int getRowCount() {
         return rowCount;
     }
 
     public void setCurrentPage(int currentPage) {
         this.currentPage = currentPage;
     }
 
     public int getCurrentPage() {
         return currentPage;
     }
 
     public void setMaxRows(int maxRows) {
         this.maxRows = maxRows;
     }
 
     public int getMaxRows() {
         return maxRows;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public boolean isShowall() {
         return showall;
     }
 
     public void setShowall(boolean showall) {
         this.showall = showall;
     }
 
     public void setURIString(String uriString) {
         this.uriString = uriString;
     }
 
     public void setJSR(boolean isJSR) {
         this.isJSR = isJSR;
     }
 
     public String toStartString() {
         StringBuffer sb = new StringBuffer();
         if (isSortable) {
             sb.append("<table class=\"sortable\" id=\"" + sortableId + "\" ");
         } else {
             sb.append("<table" + getFormattedCss());
         }
         if (cellSpacing != null) sb.append(" cellspacing=\"" + cellSpacing + "\" ");
         if (cellPadding != null) sb.append(" cellpadding=\"" + cellPadding + "\" ");
         if (border != null) sb.append(" border=\"" + border + "\" ");
         if (width != null) sb.append(" width=\"" + width + "\" ");
         if (align != null) sb.append(" align=\"" + align + "\" ");
         /// Removed for XHTML 1.0 Strict compliance
         /*
         if (valign != null) {
             sb.append(" valign=\"" + valign + "\" ");
         }
         */
         sb.append(">");
         if (title != null) sb.append("<caption>" + title + "</caption>");
         if (defaultModel != null) sb.append(defaultModel.toStartString());
         return sb.toString();
     }
 
     public String toEndString() {
         StringBuffer sb = new StringBuffer();
         sb.append("</table>");
         String uri = "";
         if (showall) {
             uri = uriString;
             sb.append("<p>"); // added for XHTML 1.0 Strict compliance
             String showPages = TableBean.SHOW_PAGES;
             if (isJSR) showPages = "rp_" + showPages;
             sb.append("<a href=\"" + uri + "&amp;" + showPages + "\">" + this.getLocalizedText("SHOW_PAGES") + "</a>");
             sb.append("</p>"); // added for XHTML 1.0 Strict compliance
         }
         if (maxRows > 0) {
            int numpages = (numEntries != 0) ? (numEntries / maxRows + numEntries % maxRows) : rowCount / maxRows +  rowCount % maxRows;
            System.err.println("numpages = " + numpages);
             int dispPage = currentPage + 1;
             if ((dispPage == numpages) && (numpages == 1)) return sb.toString();
             int c = 0;
             //System.err.println("maxrows=" + maxRows + " numEntries=" + numEntries + " rowCount=" + rowCount + " numpages=" + numpages);
 
             sb.append("<p>"); // added for XHTML 1.0 Strict compliance  
             sb.append(this.getLocalizedText("PAGE") + dispPage + this.getLocalizedText("OUT_OF_PAGES") + numpages);
 
             for (int i = 0; i < numpages; i++) {
                 c = i + 1;
                 if (c == dispPage) {
                     sb.append(" | <b>" + c + "</b>");
                 } else {
                     // create an actionlink
                     uris = uriString;
                     //System.err.println("uri = " + uris);
                     String curPage = TableBean.CURRENT_PAGE;
                     if (isJSR) curPage = "rp_" + curPage;
                     uri = uris + "&amp;" + curPage + "=" + i;
                     sb.append(" | " + "<a href=\"" + uri + "\">" + c + "</a>");
                 }
             }
             uri = uriString;
             sb.append(" | ");
             String showall = TableBean.SHOW_ALL;
             if (isJSR) showall = "rp_" + TableBean.SHOW_ALL;
             sb.append("<a href=\"" + uri + "&amp;" + showall + "\">" + this.getLocalizedText("SHOW_ALL") + "</a>");
             sb.append("</p>"); // added for XHTML 1.0 Strict compliance
             rowCount = 0;
         }
 
         return sb.toString();
     }
 
 
 }
