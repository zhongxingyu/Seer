 package org.gridsphere.provider.portletui.beans;
 
 /**
 * An <code>RenderSubmitBean</code> is a visual bean that represents an HTML button and
  * has an associated <code>DefaultPortletAction</code>
  */
 public class RenderSubmitBean extends ActionBean implements TagBean {
 
     public static final String SUBMIT_STYLE = "portlet-form-button";
     public static final String NAME = "as";
 
     /**
      * Constructs a default action submit bean
      */
     public RenderSubmitBean() {
         super(NAME);
         this.cssClass = SUBMIT_STYLE;
     }
 
     /**
      * Constructs an action submit bean from a supplied portlet request and bean identifier
      *
      * @param beanId the bean identifier
      */
     public RenderSubmitBean(String beanId) {
         super(NAME);
         this.cssClass = SUBMIT_STYLE;
         this.beanId = beanId;
     }
 
     public String toStartString() {
         return "";
     }
 
     public String toEndString() {
         String sname = (name == null) ? "" : name;
         StringBuffer sb = new StringBuffer();
 
         String inputType = "submit";
         if (useAjax) inputType = "button";
         sb.append("<input " + getFormattedCss() + " type=\"" + inputType + "\" " + checkDisabled());
 
 
         if (action != null) sname = action;
         if (anchor != null) sname += "#" + anchor;
         if (onClick != null) {
             // 'onClick' replaced by 'onclick' for XHTML 1.0 Strict compliance
             sb.append(" onclick=\"" + onClick + "\" ");
         }
         sb.append("name=\"" + sname + "\" value=\"" + value + "\"/>");
         return sb.toString();
     }
 
 }
