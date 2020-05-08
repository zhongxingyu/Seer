 /**
  * 
  */
 package com.alertscape.web.ui;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.context.ContextLoaderServlet;
 
 /**
  * @author josh
  * 
  */
 public class AlertscapeContextLoaderServlet extends ContextLoaderServlet {
 
   private static final long serialVersionUID = 1L;
 
   @Override
   public void init() throws ServletException {
     if (alreadyInstalled()) {
       super.init();
     }
   }
 
   protected boolean alreadyInstalled() {
     URL installed = getClass().getResource("/alertscape.installed");
 
     return installed != null;
   }
 
   @Override
   public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
     if (alreadyInstalled()) {
      return;
     } else {
       response.sendRedirect("com.alertscape.wizard.InstallWizard/InstallWizard.html");
     }
   }
 }
