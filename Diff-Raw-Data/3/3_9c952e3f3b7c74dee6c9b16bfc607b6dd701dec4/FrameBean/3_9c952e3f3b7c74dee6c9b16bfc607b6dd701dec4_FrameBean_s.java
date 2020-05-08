 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 
 /**
  *  The <code>FrameBean</code> extends <code>TableBean</code> to provide a stylized table that can also
  * be used to render text messages.
  */
 public class FrameBean extends TableBean implements TagBean {
 
     public static final String TABLE_FRAME_STYLE = "portlet-frame";
     public static final String TABLE_FRAME_WIDTH = "100%";
     public static final String TABLE_FRAME_SPACING = "1";
     public static final String TABLE_FRAME_PADDING = "1";
     public static final String TABLE_FRAME_BORDER = "1";
 
     public static final String ERROR_TYPE = "error";
     public static final String MESSAGE_TYPE = "message";
 
     protected String textStyle = TextBean.MSG_INFO;
 
     /**
      * Constructs a default frame bean
      */
     public FrameBean() {
         super(TABLE_FRAME_STYLE);
         this.width = TABLE_FRAME_WIDTH;
         this.cellSpacing = TABLE_FRAME_SPACING;
         this.cellPadding = TABLE_FRAME_PADDING;
         this.border = TABLE_FRAME_BORDER;
     }
 
     /**
      * Constructs a frame bean from a bean identifier
      *
      * @param beanId the frame bean identifier
      */
     public FrameBean(String beanId) {
         super(TABLE_FRAME_STYLE);
         this.beanId = beanId;
         this.width = TABLE_FRAME_WIDTH;
         this.cellSpacing = TABLE_FRAME_SPACING;
         this.cellPadding = TABLE_FRAME_PADDING;
         this.border = TABLE_FRAME_BORDER;
     }
 
     /**
      * Constructs a frame bean from a portlet request and bena identifier
      *
      * @param req the portlet request
      * @param beanId the bean identifier
      */
     public FrameBean(PortletRequest req, String beanId) {
         super(TABLE_FRAME_STYLE);
         this.width = TABLE_FRAME_WIDTH;
         this.cellSpacing = TABLE_FRAME_SPACING;
         this.cellPadding = TABLE_FRAME_PADDING;
         this.border = TABLE_FRAME_BORDER;
         this.beanId = beanId;
         this.request = req;
     }
 
     /**
      * Sets the text style
      *
      * @param style the text style
      */
     public void setStyle(String style) {
         this.textStyle = style;
     }
 
     /**
      * Returns the text style
      *
      * @return the text style
      */
     public String getStyle() {
         return textStyle;
     }
 
     /**
      * Creates a frame to display a text message
      */
     protected void createMessage() {
         defaultModel = new DefaultTableModel();
         TableRowBean tr = new TableRowBean();
         TableCellBean tc = new TableCellBean();
         TextBean text = new TextBean();
         text.setCssClass(textStyle);
         if (key != null) {
             text.setKey(key);
         }
         if (value != null) {
             text.setValue(value);
         }
         tc.addBean(text);
         tc.setCssClass(textStyle);
         tr.addBean(tc);
         defaultModel.addTableRowBean(tr);
     }
 
     public String toStartString() {
         StringBuffer sb = new StringBuffer();
         if ((key != null) || (value != null)) createMessage();
         sb.append("<table " + getFormattedCss() + " ");
         if (cellSpacing != null) sb.append(" cellspacing=\"" + cellSpacing + "\" ");
         if (cellPadding != null) sb.append(" cellpadding=\"" + cellPadding + "\" ");
         if (border != null) sb.append(" border=\"" + border + "\" ");
        if (width != null) sb.append(" width=\"" + width + "\" >");
         if (defaultModel != null) sb.append(defaultModel.toStartString());
         return sb.toString();
     }
 
     public String toEndString() {
         return ("</table>");
     }
 }
