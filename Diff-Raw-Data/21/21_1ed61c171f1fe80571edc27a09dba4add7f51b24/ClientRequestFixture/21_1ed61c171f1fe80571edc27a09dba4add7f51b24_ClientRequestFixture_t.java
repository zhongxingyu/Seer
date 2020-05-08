 package org.fiz;
 import org.apache.log4j.*;
 
 /**
  * This class provides a dummy implementation of the ClientRequest class
  * for use in tests.
  */
 public class ClientRequestFixture extends ClientRequest {
     public ClientRequestFixture() {
         super(new ServletFixture(new ServletConfigFixture(
                 new ServletContextFixture())),
                 new ServletRequestFixture(),
                 new ServletResponseFixture());
        Config.init("test/testData/WEB-INF/app/config", "web/WEB-INF/fiz/config");
         Css.init("web/WEB-INF/fiz/css");
 
         // Create the Html object specially so it will find Javascript files.
         html = new Html(null);
 
         // Provide some initial query values in the main dataset.
         Dataset main = getMainDataset();
         main.set("name", "Alice");
         main.set("age", "36");
         main.set("height", "66");
         main.set("state", "California");
     }
 
     public void clearData() {
         mainDataset = null;
         reminders = null;
         requestDataProcessed = false;
     }
 }
