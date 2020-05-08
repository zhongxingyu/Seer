 package burst.reader.web;
 
 import com.opensymphony.xwork2.ActionInvocation;
 import org.apache.struts2.ServletActionContext;
 import org.apache.struts2.dispatcher.StrutsResultSupport;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.io.PrintWriter;
 
 import static javax.servlet.http.HttpServletResponse.SC_FOUND;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Burst
  * Date: 13-4-4
  * Time: 下午9:43
  * To change this template use File | Settings | File Templates.
  */
 public class ExtendedRedirectResult extends StrutsResultSupport {
 
     private int statusCode = SC_FOUND;
 
     public void setStatusCode(int statusCode) {
         this.statusCode = statusCode;
     }
 
     @Override
     protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
 
         HttpServletRequest request = ServletActionContext.getRequest();
         HttpServletResponse response = ServletActionContext.getResponse();
 
         if(request.getHeader("User-Agent").toLowerCase().indexOf("untrusted/1.0") != -1) {
            response.sendRedirect(finalLocation);
             PrintWriter writer = response.getWriter();
            writer.print("<html><head>");
            writer.print("<meta http-equiv=\"refresh\" content=\"0; url=" + finalLocation + "\">");
            writer.print("</head></html>");
             writer.close();
         } else {
             if (SC_FOUND == statusCode) {
                 response.sendRedirect(finalLocation);
             } else {
                 response.setStatus(statusCode);
                 response.setHeader("Location", finalLocation);
                 response.getWriter().write(finalLocation);
                 response.getWriter().close();
             }
         }
     }
 }
