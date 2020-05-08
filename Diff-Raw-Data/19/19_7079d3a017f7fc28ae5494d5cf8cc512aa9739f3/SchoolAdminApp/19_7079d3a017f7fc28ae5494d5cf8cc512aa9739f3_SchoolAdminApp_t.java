 package org.sukrupa.app;
 
 import org.apache.log4j.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.sukrupa.platform.server.WebServer;
 
 import java.io.*;
import java.util.Map;
import java.util.Properties;
 
 import static java.lang.String.format;
 import static org.sukrupa.app.config.ApplicationContextLoader.configureApp;
 import static org.sukrupa.platform.logging.ConsoleLog4jLogging.configureLogging;
 
 @Component
 public class SchoolAdminApp {
 
     public static void main(String[] args) throws IOException {
         configureLogging();
         configureApp().getBean(SchoolAdminApp.class).start();
     }
 
     private final WebServer webServer;
 
     @Autowired
     public SchoolAdminApp(WebServer webServer) {
         this.webServer = webServer;
     }
 
     public void start() {
         webServer.start();
     }
 }
