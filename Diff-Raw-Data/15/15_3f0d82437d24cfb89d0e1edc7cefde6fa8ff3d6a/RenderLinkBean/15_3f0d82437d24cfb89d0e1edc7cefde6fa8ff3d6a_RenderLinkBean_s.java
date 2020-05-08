 package org.gridsphere.provider.portletui.beans;
 
 /**
  * An <code>ActionLinkBean</code> is a visual bean that represents a hyperlink containing a portlet action
  */
 public class RenderLinkBean extends ActionBean implements TagBean {
 
     protected String style = "none";
 
     /**
      * Constructs a default action link bean
      */
     public RenderLinkBean() {
     }
 
     /**
      * Constructs an action link bean from a portlet request and supplied bean identifier
      *
      * @param beanId the bean id used to reference this ActionLinkBean
      */
     public RenderLinkBean(String beanId) {
         this.beanId = beanId;
     }
 
     /**
      * Returns the style of the text: Available styles are
      * <ul>
      * <li>nostyle - plain text</li>
      * <li>error - error text</li>
      * <li>info - default info text</li>
      * <li>status - status text</li>
      * <li>alert - alert text</li>
      * <li>success - success text</li>
      *
      * @return the text style
      */
     public String getStyle() {
         return style;
     }
 
     /**
      * Sets the style of the text: Available styles are
      * <ul>
      * <li>error</li>
      * <li>info</li>
      * <li>status</li>
      * <li>alert</li>
      * <li>success</li>
      *
      * @param style the text style
      */
     public void setStyle(String style) {
         this.style = style;
     }
 
 
     public String toStartString() {
         return "";
     }
 
     public String toEndString() {
         // now do the string rendering
         action = this.portletURI.toString();
 
         if (anchor != null) action += "#" + anchor;
 
         if (style.equalsIgnoreCase("error") || (style.equalsIgnoreCase("err"))) {
             this.cssClass = MessageStyle.MSG_ERROR;
         } else if (style.equalsIgnoreCase("status")) {
             this.cssClass = MessageStyle.MSG_STATUS;
         } else if (style.equalsIgnoreCase("info")) {
             this.cssClass = MessageStyle.MSG_INFO;
         } else if (style.equalsIgnoreCase("alert")) {
             this.cssClass = MessageStyle.MSG_ALERT;
         } else if (style.equalsIgnoreCase("success")) {
             this.cssClass = MessageStyle.MSG_SUCCESS;
         } else if (style.equalsIgnoreCase(MessageStyle.MSG_BOLD)) {
             this.addCssStyle("font-weight: bold;");
         } else if (style.equalsIgnoreCase(MessageStyle.MSG_ITALIC)) {
            this.addCssStyle("font-weight: italic;");
         } else if (style.equalsIgnoreCase(MessageStyle.MSG_UNDERLINE)) {
            this.addCssStyle("font-weight: underline;");
         }
         StringBuffer sb = new StringBuffer();
         sb.append("<a");
         if (name != null) sb.append(" name=\"").append(name).append("\"");
         if (id != null) sb.append(" id=\"").append(id).append("\" ");
         if (useAjax) action = "#";
         sb.append(" href=\"").append(action).append("\"");
 
         sb.append(getFormattedCss());
         if (onClick != null) sb.append(" onclick=\"").append(onClick).append("\"");
         if (onMouseOut != null) sb.append(" onMouseOut=\"").append(onMouseOut).append("\"");
         if (onMouseOver != null) sb.append(" onMouseOver=\"").append(onMouseOver).append("\"");
         sb.append(">").append(value).append("</a>");
         return sb.toString();
     }
 
 }
