 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portlet.tags.jsr;
 
 import javax.portlet.RenderResponse;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.TagSupport;
 import java.io.IOException;
 
 /**
  * This tag produces a unique value for the current portlet.
  * <p/>
  * Supporting class for the <CODE>namespace</CODE> tag.
  * writes a unique value for the current portlet
  * <BR>This tag has no attributes
  */
 public class NamespaceTag extends TagSupport {
 
     public int doStartTag() throws JspException {
         RenderResponse renderResponse = (RenderResponse) pageContext.getAttribute("renderResponse", PageContext.PAGE_SCOPE);
         String namespace = renderResponse.getNamespace();
         JspWriter writer = pageContext.getOut();
         try {
             writer.print(namespace);
            writer.flush();
         } catch (IOException e) {
             throw new JspException("namespace Tag Exception: cannot write to the output writer.", e);
         }
         return SKIP_BODY;
     }
 }
