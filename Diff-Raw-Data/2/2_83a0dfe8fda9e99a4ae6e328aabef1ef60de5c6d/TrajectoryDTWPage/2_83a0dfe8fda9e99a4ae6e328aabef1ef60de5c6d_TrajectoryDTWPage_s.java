 package org.hackystat.projectbrowser.page.trajectory.dtwpanel;
 
 import java.util.logging.Logger;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.basic.MultiLineLabel;
 import org.apache.wicket.model.PropertyModel;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.projectbrowser.page.ProjectBrowserBasePage;
 import org.hackystat.projectbrowser.page.loadingprocesspanel.LoadingProcessPanel;
 import org.hackystat.projectbrowser.page.trajectory.TrajectorySession;
 import org.hackystat.projectbrowser.page.trajectory.dtwpanel.datapanel.TrajectoryDTWDataPanel;
 import org.hackystat.projectbrowser.page.trajectory.dtwpanel.inputpanel.TrajectoryDTWInputPanel;
 
 /**
  * Provides a page with Trajectory analyzes.
  *
  * @author Pavel Senin, Philip Johnson, Shaoxuan Zhang
  *
  *
  */
 public class TrajectoryDTWPage extends ProjectBrowserBasePage {
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
   /** Trajectory session to hold up the state. */
   private TrajectorySession session = ProjectBrowserSession.get().getTrajectorySession();
   /** the TelemetryInputPanel in this page. */
   // private TrajectoryDTWInputPanel inputPanel;
   /** the TelemetryDataPanel in this page. */
   // private TrajectoryDTWDataPanel dataPanel;
   /** the LoadingProcessPanel in this page. */
   private LoadingProcessPanel loadingProcessPanel;
 
   // Some string constants used while logging
   //
   private static final String MARK = "[DEBUG] ";
 
   /**
    * Constructs the telemetry page without URL parameters.
    */
   public TrajectoryDTWPage() {
     getLogger().info(MARK + "Trajectory page constructor invoked, hash: " + this.hashCode());
 
     TrajectoryDTWInputPanel inputPanel = new TrajectoryDTWInputPanel("inputPanel", this);
     inputPanel.setOutputMarkupId(true);
     add(inputPanel);
 
     TrajectoryDTWDataPanel dataPanel = new TrajectoryDTWDataPanel("dataPanel", this);
     dataPanel.setOutputMarkupId(true);
     add(dataPanel);
 
     loadingProcessPanel = new LoadingProcessPanel("loadingProcessPanel", session.getDataModel()) {
       /** Support serialization. */
       private static final long serialVersionUID = 1L;
 
       @Override
       protected void onFinished(AjaxRequestTarget target) {
         setResponsePage(TrajectoryDTWPage.class);
       }
     };
     loadingProcessPanel.setOutputMarkupId(true);
     add(loadingProcessPanel);
 
     this.get("FooterFeedback").setModel(new PropertyModel(session, "feedback"));
     this.get("FooterFeedback").setOutputMarkupId(true);
 
    add(new MultiLineLabel("paramErrorMessage", new PropertyModel(session, "paramErrorMessage")));
   }
 
   /**
    * Constructs the telemetry page.
    *
    * @param parameters the parameters from URL request.
    */
   public TrajectoryDTWPage(PageParameters parameters) {
     this();
 
     boolean isLoadSucceed = session.loadPageParameters(parameters);
 
     if (isLoadSucceed) {
       session.updateDataModel();
       loadingProcessPanel.start();
     }
   }
 
   /**
    * The action to be performed when the user has set the Project and Date fields.
    */
   @Override
   public void onProjectDateSubmit() {
     loadingProcessPanel.start();
     // setResponsePage(TelemetryPage.class, session.getPageParameters());
   }
 
   /**
    * @return the logger that associated to this web application.
    */
   private Logger getLogger() {
     return ((ProjectBrowserApplication) Application.get()).getLogger();
   }
 
 }
