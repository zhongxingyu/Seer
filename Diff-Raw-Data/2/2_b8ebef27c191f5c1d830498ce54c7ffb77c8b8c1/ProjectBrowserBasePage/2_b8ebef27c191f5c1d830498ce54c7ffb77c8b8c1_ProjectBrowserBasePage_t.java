 package org.hackystat.projectbrowser.page;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.PropertyModel;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.projectbrowser.imageurl.ImageUrl;
 import org.hackystat.projectbrowser.page.dailyprojectdata.DailyProjectDataPage;
 import org.hackystat.projectbrowser.page.projects.ProjectsPage;
 import org.hackystat.projectbrowser.page.sensordata.SensorDataPage;
 import org.hackystat.projectbrowser.page.telemetry.TelemetryPage;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * Provides a base class with associated markup that all ProjectBrowser pages (except for signin)
  * should inherit from. See http://wicket.apache.org/examplemarkupinheritance.html for an
  * explanation of how this works.
  * 
  * @author Philip Johnson
  */
 public class ProjectBrowserBasePage extends WebPage {
   
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
   
   /** The project name this user has selected in the ProjectDate form. */
   protected String projectName = "";
   
   /** The date this user has selected in the ProjectDate form. */
  protected Date date = getDateToday();
 
   /** The project name this user has selected. */
   private Project project = null;
   
   /** Displays text at the bottom of the screen for user information. */
   protected String footerFeedback = "";
 
   
   /**
    * Create the ProjectBrowserBasePage.
    */
   public ProjectBrowserBasePage() {
     ProjectBrowserApplication app = (ProjectBrowserApplication)getApplication();
     add(new ImageUrl("application-logo", app.getApplicationLogo()));
     add(new Label("application-name", app.getApplicationName()));
     // Provide a default value for the projectName field.
     List<String> projectNames = ProjectBrowserSession.get().getProjectNames();
     if ((projectNames != null) && !projectNames.isEmpty()) {
       this.projectName = projectNames.get(0);
     }
     add(new BookmarkablePageLink("SensorDataPageLink", SensorDataPage.class));
     add(new BookmarkablePageLink("ProjectsPageLink", ProjectsPage.class));
     add(new BookmarkablePageLink("DailyProjectDataPageLink", DailyProjectDataPage.class));
     add(new BookmarkablePageLink("TelemetryPageLink", TelemetryPage.class));
     add(new Link("LogoutLink") {
       /** Support serialization. */
       private static final long serialVersionUID = 1L;
       /** Go to the home page after invalidating the session. */
       @Override
       public void onClick() {
         getSession().invalidate();
         setResponsePage(getApplication().getHomePage());
       }
     });
     add(new Label("FooterFeedback", new PropertyModel(this, "footerFeedback")));
     add(new Label("UserEmail", new PropertyModel(ProjectBrowserSession.get(), "userEmail")));
   }
   
   /**
    * The action to be performed when the user has set the Project and Date fields. 
    */
   public void onProjectDateSubmit() {
     // do nothing in default case. 
   }
   
   /**
    * make a Date that represent today, at 0:00:00.
    * @return the Date object.
    */
   public final Date getDateToday() {
     XMLGregorianCalendar time = Tstamp.makeTimestamp();
     time.setTime(0, 0, 0);
     return time.toGregorianCalendar().getTime();
   }
 
   /**
    * @param project the project to set
    */
   public void setProject(Project project) {
     this.project = project;
   }
 
   /**
    * @return the project
    */
   public Project getProject() {
     return project;
   }
 
   /**
    * @param date the date to set
    */
   public void setDate(Date date) {
     this.date = date;
   }
 
   /**
    * @return the date
    */
   public Date getDate() {
     return date;
   }
 }
