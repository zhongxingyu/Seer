 package ru.forketyfork.mtomsoap.server;
 
 import org.springframework.web.WebApplicationInitializer;
 import org.springframework.ws.transport.http.MessageDispatcherServlet;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletRegistration;
 
 /**
  * Spring web app initializer that replaces the web.xml
  *
  * @author Serge Petunin
  * @created 22.06.13 1:24
  */
 public class SampleServiceInitializer implements WebApplicationInitializer {
 
     @Override
     public void onStartup(ServletContext container) {
         ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher", new MessageDispatcherServlet());
         dispatcher.setLoadOnStartup(1);
         dispatcher.addMapping("/*");
     }
 
 }
