 package org.alx.stripestemplate.stripes.ext;
 
 import net.sourceforge.stripes.action.ErrorResolution;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.config.Configuration;
 import net.sourceforge.stripes.controller.FileUploadLimitExceededException;
 import net.sourceforge.stripes.exception.ActionBeanNotFoundException;
 import net.sourceforge.stripes.exception.DefaultExceptionHandler;
 import net.sourceforge.stripes.exception.ExceptionHandler;
 import org.alx.stripestemplate.util.MLogger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * @author Alexis Boissonnat - alexis.boissonnat 'at' gmail.com
  */
 public class MyExceptionHandler extends DefaultExceptionHandler {
     private static final String VIEW = "/WEB-INF/jsp/exception.jsp";
     private static final MLogger logger = MLogger.getLogger(MyExceptionHandler.class);
 
     public Resolution catchActionBeanNotFound( ActionBeanNotFoundException exc, HttpServletRequest req, HttpServletResponse resp) {
         return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND);
     }
 


     public Resolution catchAll(Throwable exc, HttpServletRequest req, HttpServletResponse resp) {
         return new ForwardResolution(VIEW);
     }
 }
