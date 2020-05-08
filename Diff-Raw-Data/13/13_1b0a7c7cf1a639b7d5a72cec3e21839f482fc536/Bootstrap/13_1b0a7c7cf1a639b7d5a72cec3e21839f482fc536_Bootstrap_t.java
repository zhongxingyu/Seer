 package wandledi.config;
 
 import javax.servlet.ServletContext;
 import wandledi.example.controllers.Jsp;
 import wandledi.example.controllers.Home;
 import wandledi.java.WandlediBootstrap;
 
 /**
  *
  * @author Markus Kahl
  */
 public class Bootstrap extends WandlediBootstrap {
 
     public void init(ServletContext servletContext) {
 
        System.out.println("Dir: " + new java.io.File(".").getAbsolutePath());
        wandledi.jetty.Application.setWebDirectory("application/src/main/webapp");
         setPersistenceUnit("MyPU");
         addControllers(Jsp.class, Home.class);
     }
 
     public void destroy() {
     }
 }
