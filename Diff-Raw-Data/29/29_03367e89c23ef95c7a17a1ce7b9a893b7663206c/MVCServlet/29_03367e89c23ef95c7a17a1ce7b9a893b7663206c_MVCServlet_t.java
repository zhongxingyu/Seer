 package com.flaptor.util.web;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.flaptor.util.Execute;
 
 /**
  * Simple MVC pattern implementation
  * subclasses must implement the method doRequest and return a template (jsp, .vm, .etc) or a redirection (.do)
  * @author Martin Massera
  */
 abstract public class MVCServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(Execute.whoAmI());
 
     private static final long serialVersionUID = 1L;
 
     @Override
     public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
             String template = doRequest(request.getRequestURI(), request, response);
             if (null != template) {
                 if (template.contains(".do")) {
                     response.sendRedirect(request.getContextPath() + template);
                 } else {
                     request.getRequestDispatcher(template).forward(request, response);
                 }
             }
         } catch (Exception e) {
            logger.error("exception while processing request",e);
             throw new ServletException(e);
         }
     }
 
     abstract protected String doRequest(String uri, HttpServletRequest request, HttpServletResponse response) throws Exception;    
 }
