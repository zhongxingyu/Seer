 package org.wattdepot.visualization;
 
 import org.apache.wicket.Session;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.Response;
 import org.wattdepot.visualization.data.sensor.SensorData;
 import org.wattdepot.visualization.data.server.ServerData;
 import org.wattdepot.visualization.page.BasePage;
 import org.wattdepot.visualization.page.home.HomePage;
 
 /**
  * Defines the Visualzation Wicket web application.
  * 
  * @author Bret K. Ikehara
  */
 public class VisualizationApplication extends WebApplication {
 
   @Override
   public Session newSession(Request request, Response response) {
     return new VisualizationSession(request);
   }
 
   @Override
   protected void init() {
     super.init();
     mountPage("/data/server", ServerData.class);
     mountPage("/data/server/sensors", SensorData.class);
   }
   /**
    * @see org.apache.wicket.Application#getHomePage()
    * 
    * @return {@link Class}&lt;? extends {@link BasePage}>
    */
   @Override
   public Class<? extends BasePage> getHomePage() {
     return HomePage.class;
   }
 }
