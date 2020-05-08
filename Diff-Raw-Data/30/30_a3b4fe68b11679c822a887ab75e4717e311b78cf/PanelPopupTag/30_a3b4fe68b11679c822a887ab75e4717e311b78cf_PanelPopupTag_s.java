 package org.icemobile.jsp.tags;
 
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.tagext.TagSupport;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.logging.Logger;
 
 /**
  *
  */
 public class PanelPopupTag extends TagSupport {
 
     private static Logger LOG = Logger.getLogger(PanelPopupTag.class.getName());
     public static final String HIDDEN_CONTAINER_CLASS = "mobi-panelpopup-container-hide ";
     public static final String BLACKOUT_PNL_HIDDEN_CLASS = "mobi-panelpopup-bg-hide ";
     public static final String CONTAINER_CLASS = "mobi-panelpopup-container ";
     public static final String BLACKOUT_PNL_CLASS = "mobi-panelpopup-bg ";
     public static final String TITLE_CLASS = "mobi-date-title-container ";
 
     public int doStartTag() throws JspTagException {
 
         Writer out = pageContext.getOut();
 
         // popup background
         StringBuilder tag = new StringBuilder(TagUtil.DIV_TAG);
 //        tag.append(TagUtil.DIV_TAG);
         tag.append(" id=\"").append(getId()).append("_bg\"");
        tag.append(" class=\"").append(BLACKOUT_PNL_HIDDEN_CLASS).append("\"");
         tag.append(TagUtil.DIV_TAG_END);
 
         // popup container section
         tag.append(TagUtil.DIV_TAG);
         tag.append(" id=\"").append(getId()).append("_popup\"");
         tag.append(" class=\"").append(CONTAINER_CLASS).append("\"");
         if (styleClass != null && !"".equals(styleClass)) {
             tag.append(" ").append(getStyleClass());
         }
 
         if (style != null) {
             tag.append(" style=\"").append(getStyle()).append("\"");
         }
         tag.append(">");
 
         // popupPanel Title section
         if (title != null && !"".equals(title)) {
             tag.append(TagUtil.DIV_TAG);
             tag.append(" id=\"").append(getId()).append("_title\"");
             tag.append(" class=\"").append(TITLE_CLASS).append("\"");
             tag.append(">");
             tag.append(getTitle());
 
             //
             tag.append(TagUtil.INPUT_TAG);
             tag.append(" type=\"button\"");
             tag.append(" value=\"Close\"");
             tag.append(" onclick=\"ice.mobi.panelpopup.close('").append(getId()).append("');\"");
             tag.append("/>");
 
             tag.append(TagUtil.DIV_TAG_END); // Close title section
         }
 
         try {
             out.write(tag.toString());
         } catch (IOException ioe) {
             LOG.severe("IOException starting panelPopup tag: " + ioe);
         }
         // Allow children to render. Container DIV is open
         return EVAL_BODY_INCLUDE;
     }
 
     public int doEndTag() throws JspTagException {
 
         Writer out = pageContext.getOut();

         StringBuilder tag = new StringBuilder(TagUtil.INPUT_TAG);
 
        // Not sure of this.
         tag.append(" type=\"hidden\"");
        tag.append(" id=\"").append(getId()).append("\"");
         tag.append(" name=\"").append(getId()).append("\"");
         tag.append("/>");
 
         tag.append(TagUtil.DIV_TAG_END);
         tag.append(TagUtil.DIV_TAG_END);
         tag.append(encodeScript());
 
         try {
             out.write(tag.toString());
         } catch (IOException ioe) {
             LOG.severe("IOException starting panelPopup tag: " + ioe);
         }
 
         return EVAL_PAGE;
 
     }
 
     public String encodeScript() {
         StringBuilder retVal = new StringBuilder(TagUtil.SPAN_TAG);
         retVal.append(" id=\"").append(getId()).append("_scrSpan\">");
         retVal.append(TagUtil.SCRIPT_TAG);
         retVal.append(" ").append(TagUtil.JS_BOILER);
         retVal.append(" id=\"").append(getId()).append("_script\">");
 
         retVal.append("ice.mobi.panelpopup.init('").append(getId())
             .append("', {visible: ").append(isVisible())
             .append(",autocenter: ").append(isAutocenter())
             .append("});\n");
         retVal.append(TagUtil.SCRIPT_TAG_END);
         retVal.append(TagUtil.SPAN_TAG_END);
 
         return retVal.toString();
     }
 
 
     private String style;
     private String styleClass;
     private String title;
     private boolean visible;
     private boolean autocenter;
 
     public String getStyle() {
         return style;
     }
 
     public void setStyle(String style) {
         this.style = style;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getStyleClass() {
         return styleClass;
     }
 
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 
     public boolean isVisible() {
         return visible;
     }
 
     public void setVisible(boolean visible) {
         this.visible = visible;
     }
 
     public boolean isAutocenter() {
         return autocenter;
     }
 
     public void setAutocenter(boolean autocenter) {
         this.autocenter = autocenter;
     }
 }
