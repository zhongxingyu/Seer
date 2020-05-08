 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.Portlet;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.jsrimpl.PortletURLImpl;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.provider.portletui.beans.ActionParamBean;
 import org.gridlab.gridsphere.provider.portletui.beans.ImageBean;
 
 import javax.portlet.*;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagExtraInfo;
 import javax.servlet.jsp.tagext.VariableInfo;
 import javax.servlet.jsp.tagext.TagData;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The abstract <code>ActionTag</code> is used by other Action tags to contain <code>DefaultPortletAction</code>s
  * and possibly <code>ActionParamTag</code>s
  */
 public abstract class ActionTag extends BaseComponentTag {
 
     protected String action = null;
     protected String anchor = null;
     protected String var = null;
     protected String key = null;
     protected boolean isSecure = false;
     protected PortletURI actionURI = null;
     protected PortletURLImpl actionURL = null;
     protected String windowState = null;
     protected String portletMode = null;
     protected DefaultPortletAction portletAction = null;
     protected List paramBeans = null;
     protected String label = null;
     protected ImageBean imageBean = null;
 
     public static class TEI extends TagExtraInfo {
 
         public VariableInfo[] getVariableInfo(TagData tagData) {
             VariableInfo vi[] = null;
             String var = tagData.getAttributeString("var");
             if (var != null) {
                 vi = new VariableInfo[1];
                 vi[0] = new VariableInfo(var, "java.lang.String", true, VariableInfo.AT_BEGIN);
             }
             return vi;
         }
     }
 
     /**
      * Sets the name of the variable to export as a RenderURL object
      *
      * @param var the name of the variable to export as a RenderURL object
      */
     public void setVar(String var) {
         this.var = var;
     }
 
     /**
      * Returns the name of the exported RenderURL object
      *
      * @return the exported variable
      */
     public String getVar() {
         return var;
     }
 
     /**
      * Sets the text that should be added at the end of generated URL
      *
      * @param anchor the action link key
      */
     public void setAnchor(String anchor) {
         this.anchor = anchor;
     }
 
     /**
      * Returns the anchor used to identify text that should be added at the end of generated URL
      *
      * @return the anchor
      */
     public String getAnchor() {
         return anchor;
     }
 
     /**
      * Sets the action link key used to locate localized text
      *
      * @param key the action link key
      */
     public void setKey(String key) {
         this.key = key;
     }
 
     /**
      * Returns the action link key used to locate localized text
      *
      * @return the action link key
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Sets the label identified with the portlet component to link to
      *
      * @param label the action link key
      */
     public void setLabel(String label) {
         this.label = label;
     }
 
     /**
      * Returns the label identified with the portlet component to link to
      *
      * @return the label
      */
     public String getLabel() {
         return label;
     }
 
     /**
      * If secure is true, then use https, otherwise use http
      *
      * @param isSecure
      */
     public void setSecure(boolean isSecure) {
         this.isSecure = isSecure;
     }
 
     /**
      * Returns true if this actiontag is secure e.g. https, flase otherwise
      *
      * @return true if this actiontag is secure, false otherwise
      */
     public boolean getSecure() {
         return isSecure;
     }
 
     public void setPortletMode(String portletMode) {
         this.portletMode = portletMode;
     }
 
     public String getPortletMode() {
         return portletMode;
     }
 
     public void setWindowState(String windowState) {
         this.windowState = windowState;
     }
 
     public String getWindowState() {
         return windowState;
     }
 
     public void setAction(String action) {
         this.action = action;
     }
 
     public String getAction() {
         return action;
     }
 
     public void setPortletAction(DefaultPortletAction portletAction) {
         this.portletAction = portletAction;
     }
 
     public DefaultPortletAction getPortletAction() {
         return portletAction;
     }
 
     public void addParamBean(ActionParamBean paramBean) {
         paramBeans.add(paramBean);
     }
 
     public void removeParamBean(ActionParamBean paramBean) {
         paramBeans.remove(paramBean);
     }
 
     public List getParamBeans() {
         return paramBeans;
     }
 
     protected String createJSRActionURI(PortletURL url) throws JspException {
         // Builds a URI containing the actin and associated params
         RenderResponse res = (RenderResponse) pageContext.getAttribute("renderResponse", PageContext.PAGE_SCOPE);
         this.actionURL = (PortletURLImpl)url;
         RenderRequest req = (RenderRequest) pageContext.getAttribute("renderRequest", PageContext.PAGE_SCOPE);
         // action is a required attribute except for FormTag
         if (windowState == null) {
             windowState = req.getWindowState().toString();
         }
         if (portletMode == null) {
             portletMode = req.getPortletMode().toString();
         }
 
         if (label != null) {
             //actionURL = res.createRenderURL();
             res.setProperty("label", label);
         }
         if (windowState != null) {
             WindowState state = new WindowState(windowState);
             try {
                 //actionURL = res.createRenderURL();
                 System.err.println("set state to:" + state);
                 actionURL.setWindowState(state);
             } catch (WindowStateException e) {
                 throw new JspException("Unknown window state in renderURL tag: " + windowState);
             }
         }
         if (portletMode != null) {
             PortletMode mode = new PortletMode(portletMode);
             try {
                 //actionURL = res.createRenderURL();
                 actionURL.setPortletMode(mode);
                 System.err.println("set mode to:" + mode);
             } catch (PortletModeException e) {
                 throw new JspException("Unknown portlet mode in renderURL tag: " + portletMode);
             }
         }
 
         if (action != null) {
             portletAction = new DefaultPortletAction(action);
         }
         if (!paramBeans.isEmpty()) {
             String id = createUniquePrefix(2);
             //portletAction.addParameter(SportletProperties.PREFIX, id);
             //actionURL.setParameter(SportletProperties.PREFIX, id);
             Iterator it = paramBeans.iterator();
             while (it.hasNext()) {
                ActionParamBean pbean = (ActionParamBean)it.next();
                actionURL.setParameter(pbean.getName(), pbean.getValue());
             }
         }
         if (action != null) {
             actionURL.setAction(action);
         }
         //if (!action.equals("render"))
         //}
         System.err.println("printing URL = " + actionURL.toString());
         return actionURL.toString();
     }
 
     public String createActionURI() throws JspException {
         if (isJSR()) {
             RenderResponse res = (RenderResponse) pageContext.getAttribute("renderResponse");
             return createJSRActionURI(res.createActionURL());
         }
         return createGSActionURI();
     }
 
     public String createGSActionURI() throws JspException {
         // Builds a URI containing the actin and associated params
         PortletResponse res = (PortletResponse) pageContext.getAttribute("portletResponse");
 
         // action is a required attribute except for FormTag
         if (label != null) {
             actionURI = res.createURI(label, isSecure);
         } else if (windowState != null) {
             PortletWindow.State state = PortletWindow.State.toState(windowState);
             actionURI = res.createURI(state);
         } else if (portletMode != null) {
             Portlet.Mode mode = Portlet.Mode.toMode(portletMode);
             actionURI = res.createURI(mode);
         } else {
             actionURI = res.createURI(isSecure);
         }
         if (action != null) {
             portletAction = new DefaultPortletAction(action);
             Iterator it = paramBeans.iterator();
 
             if (!paramBeans.isEmpty()) {
                 String id = createUniquePrefix(2);
 
                 portletAction.addParameter(SportletProperties.PREFIX, id);
 
                 while (it.hasNext()) {
                     ActionParamBean pbean = (ActionParamBean) it.next();
                     portletAction.addParameter(id + "_" + pbean.getName(), pbean.getValue());
                 }
             }
             actionURI.addAction(portletAction);
         }
         return actionURI.toString();
 
     }
 
     /**
      *  A string utility that produces a string composed of
      * <code>numChars</code> number of characters
      *
      * @param numChars the number of characters in the resulting <code>String</code>
      * @return the <code>String</code>
      */
     private String createUniquePrefix(int numChars) {
         StringBuffer s = new StringBuffer();
         for (int i = 0; i <= numChars; i++) {
             int nextChar = (int) (Math.random() * 62);
             if (nextChar < 10) //0-9
                 s.append(nextChar);
             else if (nextChar < 36) //a-z
                 s.append((char) (nextChar - 10 + 'a'));
             else
                 s.append((char) (nextChar - 36 + 'A'));
         }
         return s.toString();
     }
 
 }
