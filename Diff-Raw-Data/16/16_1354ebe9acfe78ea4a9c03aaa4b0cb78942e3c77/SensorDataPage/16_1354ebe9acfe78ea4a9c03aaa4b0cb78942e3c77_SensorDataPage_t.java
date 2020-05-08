 package org.hackystat.projectbrowser.page.sensordata;
 
 import java.util.Date;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.projectbrowser.page.ProjectBrowserBasePage;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectSummary;
 import org.hackystat.utilities.stacktrace.StackTrace;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * Provides a page with information about SensorData. 
  * @author Philip Johnson
  */
 public class SensorDataPage extends ProjectBrowserBasePage {
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
   
   SdtSummaryModel sdtSummaryModel = null;
   
   /**
    * Creates this page. 
    */
   public SensorDataPage() {
     add(new SensorDataForm("sensorDataForm", this));
     add(new SdtSummaryPanel("sdtSummaryPanel", this));
     add(new SensorDataDetailsPanel("sensorDataDetailsPanel"));
   }
   
   /**
    * What to do when the user selects a project and date. 
    */
   @Override
   public void onProjectDateSubmit() {
     try {
      // Set the footerFeedback to null. 
      this.footerFeedback = "";
       // Start by getting the project summary.
       SensorDataSession session = ProjectBrowserSession.get().getSensorDataSession();
       //String projectName = session.getProjectName();
       Project project = session.getProject();
       SensorBaseClient client = ProjectBrowserSession.get().getSensorBaseClient();
       Date date = session.getDate();
       XMLGregorianCalendar startTime = Tstamp.makeTimestamp(date.getTime());
       XMLGregorianCalendar endTime = Tstamp.incrementDays(startTime, 1);
       ProjectSummary summary = 
         client.getProjectSummary(project.getOwner(), project.getName(), startTime, endTime);
       // Now create the summary model from the ProjectSummary and save it in this page's session.
       SdtSummaryModel model = session.getSdtSummaryModel();
       model.setModel(summary, date, project);
       
     }
     catch (Exception e) {
       this.footerFeedback = "Exception getting project summary: " + StackTrace.toString(e);
     }
 
   }
   
 }
