 /**
  * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.tags.portletui;
 
 import org.gridlab.gridsphere.provider.portletui.beans.ActionLinkBean;
 import org.gridlab.gridsphere.provider.portletui.beans.ActionParamBean;
 import org.gridlab.gridsphere.provider.portletui.beans.CheckBoxBean;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 
 import java.util.*;
 
 public class ActionLinkTag extends ActionTag {
 
     protected ActionLinkBean actionlink = null;
     protected String key = null;
 
     public void setKey(String key) {
         this.key = key;
     }
 
     /**
      * Returns the label of the beans.
      * @return label of the beans
      */
     public String getKey() {
         return key;
     }
 
     public int doStartTag() throws JspException {
        if (!beanId.equals("")) {
            actionlink = (ActionLinkBean)pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (actionlink == null) {
                actionlink = new ActionLinkBean();
                this.setBaseComponentBean(actionlink);
            }
        } else {
            actionlink = new ActionLinkBean();
            this.setBaseComponentBean(actionlink);
        }

         paramBeans = new ArrayList();
         createActionURI();
         if (key != null) {
             actionlink.setKey(key);
             Locale locale = pageContext.getRequest().getLocale();
             ResourceBundle bundle = ResourceBundle.getBundle("Portlet", locale);
             actionlink.setValue(bundle.getString(actionlink.getKey()));
         }
 
         return EVAL_BODY_INCLUDE;
     }
 
     public int doEndTag() throws JspException {
 
         Iterator it = paramBeans.iterator();
         while (it.hasNext()) {
             ActionParamBean pbean = (ActionParamBean)it.next();
             portletAction.addParameter(pbean.getName(), pbean.getValue());
         }
 
         actionlink.setAction(createActionURI());
 
         if ((bodyContent != null) && (value == null)) {
             actionlink.setValue(bodyContent.getString());
         }
 
         try {
             JspWriter out = pageContext.getOut();
             out.print(actionlink.toEndString());
         } catch (Exception e) {
             throw new JspException(e.getMessage());
         }
 
         return EVAL_PAGE;
     }
 }
