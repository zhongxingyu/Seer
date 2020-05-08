 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.provider.portletui.beans.ActionParamBean;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.tagext.TagSupport;
 import javax.servlet.jsp.tagext.Tag;
 
 /**
  * The <code>ActionParamTag</code> is used to specify action paramters (name value pairs) inside of an
  * <code>ActionLinkTag</code> or an <code>ActionSubmitTag</code>
  */
 public class ActionParamTag extends TagSupport {
 
     protected String name = "";
     protected String value = "";
     protected ActionParamBean paramBean = null;
 
     /**
      * Sets the action parameter name
      *
      * @param name the action parameter name
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Returns the action paramter name
      *
      * @return the action parameter name
      */
     public String getName() {
         return name;
     }
 
     /**
      * Sets the action parameter value
      *
      * @param value the action paramter value
      */
     public void setValue(String value) {
         this.value = value;
     }
 
     /**
      * Returns the action parameter value
      *
      * @return the action parameter value
      */
     public String getValue() {
         return value;
     }
 
     /**
      * Sets the action parameter bean
      *
      * @param paramBean the action parameter bean
      */
     public void setParamBean(ActionParamBean paramBean) {
         this.paramBean = paramBean;
     }
 
     /**
      * Returns the action parameter bean
      *
      * @return the action parameter bean
      */
     public ActionParamBean getParamBean() {
         return paramBean;
     }
 
     public int doStartTag() throws JspException {
         Tag tag =  getParent();
         if (tag instanceof ActionTag) {
             ActionTag actionTag = (ActionTag)tag;
             paramBean = new ActionParamBean(name, value);
             actionTag.addParamBean(paramBean);
         }
         return SKIP_BODY;
     }
 
     public int doEndTag() throws JspTagException {
         return EVAL_PAGE;
     }
 
 }
