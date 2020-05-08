 package org.gridlab.gridsphere.tags.web;
 
 import org.gridlab.gridsphere.portlet.PortletURI;
 import org.gridlab.gridsphere.portlet.DefaultPortletAction;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.tagext.TagSupport;
 
/**
 * Created by IntelliJ IDEA.
 * User: novotny
 * Date: Jan 10, 2003
 * Time: 11:11:04 AM
 * To change this template use Options | File Templates.
 */
 public class FormTag extends TagSupport {
 
     private boolean isMultipart = false;
    private String action;
     private String method = "POST";
 
     public void setAction(String action) {
         this.action =action;
     }
 
     public String getAction() {
         return action;
     }
 
     public void setMethod(String method) {
         this.method = method;
     }
 
     public String getMethod() {
         return method;
     }
 
     public void setMultiPartFormData(boolean isMultipart) {
         this.isMultipart = isMultipart;
     }
 
     public boolean getMultiPartFormData() {
         return isMultipart;
     }
 
     public String createActionURI() {
         PortletResponse res = (PortletResponse)pageContext.getAttribute("portletResponse");
         PortletURI someURI = res.createURI();
         DefaultPortletAction anAction = new DefaultPortletAction(action);
         someURI.addAction(anAction);
         pageContext.setAttribute("_uri", someURI);
         return someURI.toString();
     }
 
     public int doStartTag() throws JspException {
         createActionURI();
         try {
             JspWriter out = pageContext.getOut();
 
             //out.print("<script language=\"JavaScript\">");
             //out.print("function hello() {");
             //out.print("    alert(\"How's it going?\")");
             //out.print("}");
             //out.print("</script>");
 
             out.print("<form ");
             out.print("action=\"");
             out.print(createActionURI());
             out.print("\" method=\"");
             out.print(method);
             out.print("\"");
             if (isMultipart) {
                 out.print(" enctype=\"multipart/form-data\"");
             }
             //out.print(" onSubmit=\"hello()\" >");
         } catch (Exception e) {
             throw new JspTagException(e.getMessage());
         }
         return EVAL_BODY_INCLUDE;
     }
 
     public int doEndTag() throws JspTagException {
         try {
             JspWriter out = pageContext.getOut();
             out.print("</form>");
         } catch (Exception e) {
             throw new JspTagException(e.getMessage());
         }
         return EVAL_PAGE;
     }
 
 }
