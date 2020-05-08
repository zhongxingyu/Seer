 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portlet.tags.jsr;
 
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.provider.portletui.beans.ActionLinkBean;
 import org.gridlab.gridsphere.provider.portletui.beans.TextBean;
 import org.gridlab.gridsphere.provider.portletui.beans.MessageStyle;
 import org.gridlab.gridsphere.provider.portletui.tags.ActionTag;
 
 import javax.portlet.RenderResponse;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import java.util.ArrayList;
 
 /**
  * The <code>ActionLinkTag</code> provides a hyperlink element that includes a <code>DefaultPortletAction</code>
  * and can contain nested <code>ActionParamTag</code>s
  */
 public class ActionURLTagImpl extends ActionTag {
 
     protected ActionLinkBean actionlink = null;
     protected String style = MessageStyle.MSG_INFO;
 
     /**
      * Sets the style of the text: Available styles are
      * <ul>
      * <li>nostyle</li>
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
 
     /**
      * Returns the style of the text: Available styles are
      * <ul>
      * <li>nostyle</li>
      * <li>error</li>
      * <li>info</li>
      * <li>status</li>
      * <li>alert</li>
      * <li>success</li>
      *
      * @return the text style
      */
     public String getStyle() {
         return style;
     }
 
     public int doStartTag() throws JspException {
         if (!beanId.equals("")) {
             actionlink = (ActionLinkBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
             if (actionlink == null) {
                 actionlink = new ActionLinkBean();
                 actionlink.setStyle(style);
                 this.setBaseComponentBean(actionlink);
             }
         } else {
             actionlink = new ActionLinkBean();
             this.setBaseComponentBean(actionlink);
             actionlink.setStyle(style);
         }
 
         if (name != null) actionlink.setName(name);
         if (anchor != null) actionlink.setAnchor(anchor);
 
         paramBeans = new ArrayList();
 
         if (key != null) {
             actionlink.setKey(key);
             actionlink.setValue(getLocalizedText(key));
         }
 
        return EVAL_BODY_BUFFERED;
     }
 
     public int doEndTag() throws JspException {
         // set action to non-null
         if (action == null) action = "";
         RenderResponse res = (RenderResponse) pageContext.getAttribute(SportletProperties.RENDER_RESPONSE, PageContext.REQUEST_SCOPE);
         String actionString = createJSRActionURI(res.createActionURL());
         actionlink.setAction(actionString);
 
         if ((bodyContent != null) && (value == null)) {
             actionlink.setValue(bodyContent.getString());
         }
 
         if (imageBean != null) {
             String val = actionlink.getValue();
             if (val == null) val = "";
             actionlink.setValue(imageBean.toStartString() + val);
         }
         if (var == null) {
             try {
                 JspWriter out = pageContext.getOut();
                 out.print(actionURL.toString());
             } catch (Exception e) {
                 throw new JspException(e.getMessage());
             }
         } else {
             pageContext.setAttribute(var, actionURL.toString(), PageContext.PAGE_SCOPE);
         }
         return EVAL_PAGE;
     }
 
 }
