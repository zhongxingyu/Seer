 package org.wattdepot.visualization;
 
 import org.apache.wicket.protocol.http.WebApplication;
 import org.wattdepot.visualization.page.BasePage;
 import org.wattdepot.visualization.page.home.HomePage;
 
 /**
  * Defines the Visualzation Wicket web application.
  * 
  * @author Bret K. Ikehara
  */
 public class VisualizationApplication extends WebApplication {

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
