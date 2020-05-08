 package org.hackystat.projectbrowser.authentication;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.behavior.HeaderContributor;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.hackystat.dailyprojectdata.client.DailyProjectDataClient;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.imageurl.ImageUrl;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.telemetry.service.client.TelemetryClient;
 
 /**
  * Provides a signin page for either logging in with a previous username and password, or 
  * else registering with the system. 
  * 
  * @author Philip Johnson
  */
 public class SigninPage extends WebPage {
   
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
 
   /**
    * Create the SigninPage. 
    */
   public SigninPage() {
     ProjectBrowserApplication app = (ProjectBrowserApplication)getApplication();
     add(HeaderContributor.forCss(org.hackystat.projectbrowser.Start.class, 
         "style/boilerplate/screen.css", "screen"));
     add(HeaderContributor.forCss(org.hackystat.projectbrowser.Start.class, 
         "style/boilerplate/print.css", "print"));
     add(new Label("title", app.getApplicationName()));
     add(new ImageUrl("application-logo", app.getApplicationLogo()));
     add(new Label("application-name", (app.hasApplicationLogo() ? "" : app.getApplicationName())));
     add(new SigninForm("signinForm"));
     add(new RegisterForm("registerForm"));
     List<String> serviceInfo = getServiceInfo();
     add(new Label("available", serviceInfo.get(0)));
     add(new Label("notAvailable", serviceInfo.get(1)));
   }
   
   /**
    * Returns a list containing two strings.  The first string indicates the services that 
    * were contacted successfully.  The second string indicates the services that were not
    * contacted successfully. 
    * @return A list of two strings indicating service availability. 
    */
   private List<String> getServiceInfo() {
     List<String> serviceInfo = new ArrayList<String>();
     StringBuffer available = new StringBuffer(20);
    StringBuffer notAvailable = new StringBuffer(22);
     available.append("Available services: ");
     notAvailable.append("Unavailable services: ");
     
     ProjectBrowserApplication app = (ProjectBrowserApplication)getApplication();
     String sensorbase = app.getSensorBaseHost();
     if (SensorBaseClient.isHost(sensorbase)) {
       available.append(sensorbase);
     }
     else {
       notAvailable.append(sensorbase);
     }
     String dpd = app.getDailyProjectDataHost();
     if (DailyProjectDataClient.isHost(dpd)) {
       available.append(' ').append(dpd);
     }
     else {
       notAvailable.append(' ').append(dpd);
     }
     String telemetry = app.getTelemetryHost();
     if (TelemetryClient.isHost(telemetry)) {
       available.append(' ').append(telemetry);
     }
     else {
       notAvailable.append(' ').append(telemetry);
     }
     String availableString = ((available.length() > 20) ?
         available.toString() : "");
     String notAvailableString = ((notAvailable.length() > 22) ?
         notAvailable.toString() : "");
         
     serviceInfo.add(availableString);
     serviceInfo.add(notAvailableString);
     return serviceInfo;
   }
 }
