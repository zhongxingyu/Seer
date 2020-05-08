 package main.demo.gui;
 
 import gx.realtime.*;
 import org.apache.logging.log4j.Level;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.core.LoggerContext;
 import org.apache.logging.log4j.core.config.Configuration;
 import org.apache.logging.log4j.core.config.LoggerConfig;
 
 import javax.swing.*;
 import java.io.IOException;
 
 /**
  * Demo application for the Realtime Gx library. Showcases the use of a collaborative map.
  */
 public class DemoGuiApp
 {
     Document document;
 
     public static void main(String[] args)
     {
         // Set the Log4j2 logging levels
         LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
         Configuration config = ctx.getConfiguration();
         LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
         loggerConfig.setLevel(Level.ERROR);
         ctx.updateLoggers();
 
         // Initialize Gx before starting the app
 
         DemoGuiApp app = new DemoGuiApp();
         app.start();
     }
 
     public void start()
     {
         RealtimeOptions options = new RealtimeOptions();
         options.setClientId(getClientId());
         options.setDocId("0B6gFLa0mYRcOUHlpUlFTTm5EaDQ");
 
         options.setOnFileLoaded((doc) -> {
             document = doc;
 
             Model model = doc.getModel();
             CollaborativeMap root = model.getRoot();
 
             // Iterate over the keys and grab some key
             String key = root.keys().iterator().next();
             System.out.println(root.keys());
 
             if (key != null) {
                 // Try to get the key value
                 CollaborativeMap map = (CollaborativeMap) root.get(key);
                System.out.println("==" + root);
                 System.out.println("Found collaborativeMap: " + map.toString());
 
                 // Start the UI
                 SwingUtilities.invokeLater(() -> RealtimePanel.createUI(document, map));
             } else {
                 // TODO: v2 - create a new CollaborativeMap to play with
                 System.out.println("Found no collaborativeMap in the specified document - closing.");
                 document.close();
                 System.exit(1);
             }
         });
         options.setHandleErrors((doc) -> System.out.println("Received error, crap!"));
 
         RealtimeLoader loader = new RealtimeLoader(options);
         loader.start();
     }
 
     /**
      * Get the client ID from the default configuration file.
      *
      * @return
      */
     private String getClientId()
     {
         String clientId = null;
         try {
             clientId = IdentificationLoader.usingDefault().getClientId();
         } catch (IOException e) {
             System.out.println("Could not load client ID");
             e.printStackTrace();
             System.exit(1);
         }
         return clientId;
     }
 }
