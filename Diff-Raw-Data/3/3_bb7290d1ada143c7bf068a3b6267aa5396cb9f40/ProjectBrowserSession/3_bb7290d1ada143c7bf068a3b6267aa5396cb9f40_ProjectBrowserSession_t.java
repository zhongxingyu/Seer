 package org.hackystat.projectbrowser;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.apache.wicket.Request;
 import org.apache.wicket.Session;
 import org.apache.wicket.protocol.http.WebSession;
 import org.hackystat.dailyprojectdata.client.DailyProjectDataClient;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.client.SensorBaseClientException;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectIndex;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectRef;
 import org.hackystat.telemetry.service.client.TelemetryClient;
 import org.hackystat.utilities.stacktrace.StackTrace;
 
 /**
  * Provides a session instance that holds authentication credentials.
  * @author Philip Johnson
  *
  */
 public class ProjectBrowserSession extends WebSession {
   /** Support serialization */
   private static final long serialVersionUID = 1L;
   /** The email used to connect to the SensorBase. */
   private String email = null;
   /** The password for the SensorBase. */
   private String password = null;
   /** The SensorBase client for this user. */
   // Need to make this class serializable if we want to keep it in the session and not
   // make a new one each request. 
   //private SensorBaseClient client = null; 
   /** The current signinFeedback message to display. */
   private String signinFeedback = "";
   /** The current registerFeedback message to display. */
   private String registerFeedback = "";
   /** If this user has been authenticated against the Sensorbase during this session. */
   private boolean isAuthenticated = false;
   /** The list of Projects that this user has. */
   private List<Project> projects = null;
   
   
   /**
    * Provide a constructor that initializes WebSession.
    * @param request The request object.
    */
   public ProjectBrowserSession(Request request) {
     super(request);
   }
 
   /**
    * Obtain the current session. 
    * @return The current ProjectBrowserSession.
    */
   public static ProjectBrowserSession get() {
     return (ProjectBrowserSession) Session.get();
   }
   
   /**
    * Returns true if the user has been authenticated in this session.
    * @return True if the user has supplied a valid email and password for this sensorbase.
    */
   public boolean isAuthenticated() {
     return this.isAuthenticated;
   }
   
   /**
    * Used by the Signin form to provide the SensorBase authentication credentials to this session. 
    * @param user The user.
    * @param password The password. 
    */
   public void setCredentials(String user, String password) {
     this.email = user;
     this.password = password;
   }
   
   /**
    * Returns the string to be displayed in the SigninFeedback label.
    * @return A signin feedback string. 
    */
   public String getSigninFeedback() {
     return this.signinFeedback;
   }
   
   /**
    * Allows other components to set the feedback string for the signin form.
    * @param signinFeedback The message to be displayed.
    */
   public void setSigninFeedback(String signinFeedback) {
     this.signinFeedback = signinFeedback;
   }
   
   /**
    * Allows other components to set the feedback string for the register form.
    * @param registerFeedback The message to be displayed.
    */
   public void setRegisterFeedback(String registerFeedback) {
     this.registerFeedback = registerFeedback;
   }
   
   /**
    * Returns the string to be displayed in the registerFeedback label.
    * @return A register feedback string. 
    */
   public String getRegisterFeedback() {
     return this.registerFeedback;
   }
   
   /**
    * Returns true if this email/password combination is valid for this sensorbase. 
    * @param email The email. 
    * @param password The password.
    * @return True if valid for this sensorbase. 
    */
   public boolean signin(String email, String password) {
     try {
       String host = ((ProjectBrowserApplication)getApplication()).getSensorBaseHost();
       SensorBaseClient client = new SensorBaseClient(host, email, password);
       client.authenticate();
       this.email = email;
       this.password = password;
       this.isAuthenticated = true;
       return true;
     }
     catch (Exception e) {
       this.isAuthenticated = false;
       return false;
     }
   }
   
   /**
    * Returns a SensorBaseClient instance for this user and session. 
    * @return The SensorBaseClient instance. 
    */
   public SensorBaseClient getSensorBaseClient() {
     String host = ((ProjectBrowserApplication)getApplication()).getSensorBaseHost();
     return new SensorBaseClient(host, this.email, this.password);
   }
   
   /**
    * Returns a TelemetryClient instance for this user and session.
    * @return The TelemetryClient instance. 
    */
   public TelemetryClient getTelemetryClient() {
     String host = ((ProjectBrowserApplication)getApplication()).getTelemetryHost();
     return new TelemetryClient(host, this.email, this.password);
   }
   
   /**
    * Returns a DailyProjectDataClient instance for this user and session.
    * @return The DailyProjectDataClient instance. 
    */
   public DailyProjectDataClient getDailyProjectDataClient() {
     String host = ((ProjectBrowserApplication)getApplication()).getDailyProjectDataHost();
     return new DailyProjectDataClient(host, this.email, this.password);
   }
   
   /**
    * Gets the user's email associated with this session. 
    * @return The user.
    */
   public String getUserEmail() {
     return this.email;
   }
   
   /**
    * Returns the list of project names associated with this user.
    * The project names are identified by '[name]:[owner]'. 
    * @return The list of project names. 
    */
   public List<String> getProjectNames() {
     List<String> projectNames = new ArrayList<String>();
     for (Project project : getProjects()) {
      // Add (the other) owner if project name is a duplicate. 
      projectNames.add(project.getName());
     }
     return projectNames;
   }
   
   /**
    * Return the list of projects associated with this user.  If the list has not yet been
    * built, get it from the SensorBase and cache it. 
    * @return The list of Project instances. 
    */
   public List<Project> getProjects() {
     if (this.projects == null) {
       this.projects= new ArrayList<Project>();
       try {
         SensorBaseClient sensorBaseClient = ProjectBrowserSession.get().getSensorBaseClient();
         ProjectIndex projectIndex = sensorBaseClient.getProjectIndex(this.email);
         for (ProjectRef projectRef : projectIndex.getProjectRef()) {
           Project project = sensorBaseClient.getProject(projectRef);
           projects.add(project);
         }
       }
       catch (SensorBaseClientException e) {
         Logger logger = ((ProjectBrowserApplication)getApplication()).getLogger();
         logger.warning("Error getting projects for " + this.email + StackTrace.toString(e));
       }
     }
     return this.projects;
   }
 }
