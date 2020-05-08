 package cz.admin24.myachievo.connector.http;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.admin24.myachievo.connector.http.cmd.CmdGetPhaseActivities;
 import cz.admin24.myachievo.connector.http.cmd.CmdGetProjectPhases;
 import cz.admin24.myachievo.connector.http.cmd.CmdGetProjects;
 import cz.admin24.myachievo.connector.http.cmd.CmdGetWorkReport;
 import cz.admin24.myachievo.connector.http.cmd.CmdRegisterHours;
 import cz.admin24.myachievo.connector.http.dto.PhaseActivity;
 import cz.admin24.myachievo.connector.http.dto.Project;
 import cz.admin24.myachievo.connector.http.dto.ProjectPhase;
 import cz.admin24.myachievo.connector.http.dto.WorkReport;
 
 public class AchievoConnectorImpl implements AchievoConnector {
     private static final Logger        LOG                  = LoggerFactory.getLogger(AchievoConnectorImpl.class);
 
     private final PersistentConnection persistentConnection = new PersistentConnection();
 
 
     public AchievoConnectorImpl() {
 
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see cz.admin24.myachievo.connector.http.AchievoConnectorI#setCredentials(java.lang.String,
      * java.lang.String)
      */
     public void setCredentials(String username, String password) {
         getConnection().setCredentials(username, password);
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see cz.admin24.myachievo.connector.http.AchievoConnectorI#authentize(java.lang.String,
      * java.lang.String)
      */
     public void authentize(String username, String password) throws AuthentizationException, IOException {
         setCredentials(username, password);
         getConnection().authentize();
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see cz.admin24.myachievo.connector.http.AchievoConnectorI#getFullUserName()
      */
     public String getFullUserName() {
         return getConnection().getUserName();
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see cz.admin24.myachievo.connector.http.AchievoConnectorI#getProjects()
      */
     public List<Project> getProjects() throws IOException {
         return new CmdGetProjects().execute(getConnection());
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see cz.admin24.myachievo.connector.http.AchievoConnectorI#getPhases(java.lang.String)
      */
     public List<ProjectPhase> getPhases(String projectId) throws IOException {
         return new CmdGetProjectPhases(projectId).execute(getConnection());
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see cz.admin24.myachievo.connector.http.AchievoConnectorI#getActivities(java.lang.String,
      * java.lang.String)
      */
     public List<PhaseActivity> getActivities(String projectId, String phaseId) throws IOException {
         return new CmdGetPhaseActivities(projectId, phaseId).execute(getConnection());
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see cz.admin24.myachievo.connector.http.AchievoConnectorI#registerHours(java.util.Date,
      * java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String,
      * java.lang.String)
      */
     public List<WorkReport> registerHours(Date day, Integer hours, Integer minutes, String projectId, String phaseId, String activityId, String remark) throws IOException {
        PersistentConnection connection = getConnection();
        new CmdRegisterHours(connection.getUserId(), day, hours, minutes, projectId, phaseId, activityId, remark).execute(connection);
         return getHours(day, day);
     }
 
 
     /*
      * (non-Javadoc)
      *
      * @see cz.admin24.myachievo.connector.http.AchievoConnectorI#getHours(java.util.Date,
      * java.util.Date)
      */
     public List<WorkReport> getHours(Date from, Date to) throws IOException {
         return new CmdGetWorkReport(from, to).execute(getConnection());
     }
 
 
     private PersistentConnection getConnection() {
         return persistentConnection;
     }
 
 }
