 package no.ebakke.studycaster.servlets;
 
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang3.StringEscapeUtils;
 
 @WebServlet(name = "JNLPServlet",
     urlPatterns = {JNLPServlet.JNLP_PATH})
 public class JNLPServlet extends HttpServlet {
   /* To change JNLP_DIR, would also have to update project properties
   (Build->Packaging) and build.xml. */
   public static final String JNLP_DIR  = "/client";
   public static final String JNLP_FILE = "sc_client.jnlp";
   public static final String JNLP_PATH =
       JNLPServlet.JNLP_DIR + "/" + JNLPServlet.JNLP_FILE;
   private static final long serialVersionUID = 1L;
 
   @Override
   protected void service(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException
   {
     resp.setHeader("Cache-Control", "no-cache");
     resp.setHeader("Pragma"       , "no-cache");
     resp.setCharacterEncoding("UTF-8");
 
    /* Note: The content type is set by the <jsp:directive.page/> element in
    master-application.jnlp (in the client project). */
    resp.setHeader("Content-Disposition", "attachment; filename=\"" +
        StringEscapeUtils.escapeJava(JNLPServlet.JNLP_FILE) + "\"");
     /* TODO: Consider whether the server URL should be a configuration
     parameter rather than being derived dynamically. */
     String serverURL = ServletUtil.getApplicationBase(req);
     req.setAttribute("codebaseURL", serverURL + JNLPServlet.JNLP_DIR);
     req.setAttribute("jnlpFile", JNLPServlet.JNLP_FILE);
     RequestDispatcher rd =
         getServletContext().getRequestDispatcher("/WEB-INF/generatedJNLP.jspx");
     rd.forward(req, resp);
   }
 }
