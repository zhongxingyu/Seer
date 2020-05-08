 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portlet.jsrimpl.RenderResponseImpl;
 import org.gridlab.gridsphere.services.core.tracker.TrackerService;
 
import javax.portlet.*;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import java.util.ArrayList;
 
 /**
  * The <code>ActionFormTag</code> provides a UI form tag that can also include <code>ActionParam</code> tags
  * nested within it.
  */
 public class ActionFormTag extends ActionTag {
 
     protected boolean isMultipart = false;
     // 'POST' replaced by 'post' for XHTML 1.0 Strict compliance
     protected String method = "post";
 
     /**
      * Sets the form method attribute e.g. POST
      *
      * @param method the form method
      */
     public void setMethod(String method) {
         this.method = method;
     }
 
     /**
      * Returns the form method attribute e.g. POST
      *
      * @return the form method
      */
     public String getMethod() {
         return method;
     }
 
     /**
      * Sets whether this form supports multi-part form data as in the case of file uploading
      *
      * @param isMultipart is true if this form supports multi-part form data, false otherwise
      */
     public void setMultiPartFormData(boolean isMultipart) {
         this.isMultipart = isMultipart;
     }
 
     /**
      * Returns whether this form supports multi-part form data as in the case of file uploading
      *
      * @return true if this form supports multi-part form data, false otherwise
      */
     public boolean getMultiPartFormData() {
         return isMultipart;
     }
 
     public int doStartTag() throws JspException {
         paramBeans = new ArrayList();
         return EVAL_BODY_BUFFERED;
     }
 
     public int doEndTag() throws JspException {
         try {
             JspWriter out = pageContext.getOut();
 
             out.print("<form ");
             if (onSubmit != null) out.print(" onsubmit=\"" + onSubmit + "\" ");
             out.print("action=\"");
 
             // if using JSR then create render link
             RenderResponse res = (RenderResponse) pageContext.getAttribute(SportletProperties.RENDER_RESPONSE, PageContext.REQUEST_SCOPE);
             String actionStr;
             if (res != null) {
                 if (res instanceof RenderResponseImpl) {
                     actionStr = createJSRActionURI(res.createRenderURL());
                 } else {
                     // if non-GS container this will have to be an ActionURL
                     actionStr = createJSRActionURI(res.createActionURL());
                 }
             } else {
                 actionStr = createActionURI();
             }
 
             out.print(actionStr);
 
 
             out.print("\" method=\"");
             out.print(method);
             out.print("\"");
             if (isMultipart) {
                 out.print(" enctype=\"multipart/form-data\"");
             }
             if (name == null) {
                 name = "form" + this.getUniqueId("gs_formNumber");
             }
 
            // 'name' attribute replaced by 'id' for XHTML 1.0 Strict compliance
             out.print(" id=\"" + name + "\"");
 
             out.println(">");
             // add JS info
             out.println("<p>"); // added for XHTML 1.0 Strict compliance
             // added the closing slash of tag for XHTML 1.0 Strict compliance
             out.println("<input name=\"JavaScript\" value=\"\" type=\"hidden\" />");
             out.println("</p>"); // added for XHTML 1.0 Strict compliance
             // 'language="JavaScript"' replaced by 'type="text/javascript"' for XHTML 1.0 Strict compliance
 
             // these 3 lines add javascript to detect if js is supported
             //out.println("<script type=\"text/javascript\">");
             // elements accessed by 'getElementById' method instead of 'name' attribute for XHTML 1.0 Strict compliance
             //out.println("document.getElementById(\"" + name + "\").JavaScript.value = \"enabled\";");
             //out.println("</script>");
             if (trackMe != null) {
                 out.println("<input name=\"" + TrackerService.TRACK_PARAM + "\" value=\"" + trackMe + "\" type=\"hidden\"/>");
             }
             if (extUrl != null) {
                 out.println("<input name=\"" + TrackerService.REDIRECT_URL + "\" value=\"" +extUrl + "\" type=\"hidden\"/>");
             }
 
             // write out rest of body
             bodyContent.writeOut(getPreviousOut());
             // end form
             out.print("</form>");
         } catch (Exception e) {
             throw new JspTagException(e.getMessage());
         }
        // nulling these out is ABSOLUTELY NECESSARY or weird
        // interactins happen with actionsubmit nested inside actionform!
         name = null;
        action = null;
         return EVAL_PAGE;
     }
 
    public void release() {
        super.release();
    }

 }
