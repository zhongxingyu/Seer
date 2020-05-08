 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * An <code>ActionSubmitBean</code> is a visual bean that represents an HTML button and
  * has an associated <code>DefaultPortletAction</code>
  */
 public class ActionSubmitBean extends ActionBean implements TagBean {
 
     public static final String SUBMIT_STYLE = "portlet-form-button";
     public static final String NAME = "as";
 
     /**
      * Constructs a default action submit bean
      */
     public ActionSubmitBean() {
         super(NAME);
         this.cssClass = SUBMIT_STYLE;
     }
 
     /**
      * Constructs an action submit bean from a supplied portlet request and bean identifier
      *
      * @param beanId the bean identifier
      */
     public ActionSubmitBean(String beanId) {
         super(NAME);
         this.cssClass = SUBMIT_STYLE;
         this.beanId = beanId;
     }
 
     /**
      * Constructs an action submit bean from a supplied portlet request and bean identifier
      *
      * @param req the portlet request
      * @param beanId the bean identifier
      */
     public ActionSubmitBean(HttpServletRequest req, String beanId) {
         super(NAME);
         this.cssClass = SUBMIT_STYLE;
         this.request = req;
         this.beanId = beanId;
     }
 
     public String toStartString() {
         return "<input " + getFormattedCss() + " type=\"submit\" " + checkDisabled();
     }
 
     public String toEndString() {
         String pname = (name == null) ? "" : name;
         String sname = pname;

        sname = action;
         if (anchor != null) sname += "#" + anchor;

         return "name=\"" + sname + "\" value=\"" + value + "\"/>";
     }
 
 }
