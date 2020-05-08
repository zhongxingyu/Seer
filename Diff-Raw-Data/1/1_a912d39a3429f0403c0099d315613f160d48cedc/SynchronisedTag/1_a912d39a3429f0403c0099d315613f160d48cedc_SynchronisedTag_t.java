 package afc.jsp.tag;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.JspFragment;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 public final class SynchronisedTag extends SimpleTagSupport
 {
     private Object monitor;
     
     public void setMonitor(final Object monitor)
     {
         if (monitor == null) {
             throw new NullPointerException("monitor");
         }
        this.monitor = monitor;
     }
     
     @Override
     public void doTag() throws IOException, JspException
     {
         final JspFragment body = getJspBody();
         if (body == null) {
             throw new JspException("Tag body is undefined.");
         }
         final JspWriter out = getJspContext().getOut();
         synchronized (monitor) {
             body.invoke(out);
         }
     }
 }
