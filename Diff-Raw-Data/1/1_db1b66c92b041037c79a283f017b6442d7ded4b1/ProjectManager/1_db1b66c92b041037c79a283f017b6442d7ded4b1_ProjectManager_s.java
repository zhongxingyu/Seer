 package org.hackystat.sensorbase.resource.projects;
 
 import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;
 
 import java.io.File;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.hackystat.sensorbase.db.DbManager;
 import org.hackystat.utilities.stacktrace.StackTrace;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.sensorbase.resource.projects.jaxb.Invitations;
 import org.hackystat.sensorbase.resource.projects.jaxb.Members;
 import org.hackystat.sensorbase.resource.projects.jaxb.MultiDayProjectSummary;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectIndex;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectRef;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectSummary;
 import org.hackystat.sensorbase.resource.projects.jaxb.Projects;
 import org.hackystat.sensorbase.resource.projects.jaxb.Properties;
 import org.hackystat.sensorbase.resource.projects.jaxb.Spectators;
 import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
 import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
 import org.hackystat.sensorbase.resource.users.UserManager;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.server.Server;
 import org.w3c.dom.Document;
 
 /**
  * Provides a manager for the Project resource. 
  * @author Philip Johnson
  */
 public class ProjectManager {
 
   /** The String naming the Default project. */
   public static final String DEFAULT_PROJECT_NAME = "Default";
   
   /** Holds the class-wide JAXBContext, which is thread-safe. */
   private JAXBContext jaxbContext;
   
   /** The Server associated with this SdtManager. */
   Server server; 
   
   /** The DbManager associated with this server. */
   DbManager dbManager;
   
   /** The UserManager. */
   UserManager userManager;
   
   /** The ProjectIndex open tag. */
   public static final String projectIndexOpenTag = "<ProjectIndex>";
   
   /** The ProjectIndex close tag. */
   public static final String projectIndexCloseTag = "</ProjectIndex>";
   
   /** The initial size for Collection instances that hold the Projects. */
   private static final int projectSetSize = 127;
 
   /** The in-memory repository of Projects, keyed by Owner and Project name. */
   private Map<User, Map<String, Project>> owner2name2project = 
     new HashMap<User, Map<String, Project>>(projectSetSize);
   
   /** The in-memory repository of Project XML strings, keyed by Project. */
   private ProjectStringMap project2xml = new ProjectStringMap();
   
   /** The in-memory repository of ProjectRef XML strings, keyed by Project. */
   private ProjectStringMap project2ref = new ProjectStringMap();  
   
   /** The http string identifier. */
   private static final String http = "http";
   
   /** 
    * The constructor for ProjectManagers. 
    * There is one ProjectManager per Server. 
    * @param server The Server instance associated with this ProjectManager. 
    */
   public ProjectManager(Server server) {
     this.server = server;
     // Note: cannot get a SensorDataManager at this point; has not yet been instantiated.
     this.userManager = 
       (UserManager)this.server.getContext().getAttributes().get("UserManager");    
     this.dbManager = (DbManager)this.server.getContext().getAttributes().get("DbManager");
     try {
       this.jaxbContext = 
         JAXBContext.newInstance("org.hackystat.sensorbase.resource.projects.jaxb");
       loadDefaultProjects(); //NOPMD it's throwing a false warning. 
       initializeCache();  //NOPMD 
       initializeDefaultProjects(); //NOPMD
     }
     catch (Exception e) {
       String msg = "Exception during ProjectManager initialization processing";
       server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
       throw new RuntimeException(msg, e);
     }
   }
   
   /**
    * Loads the default Projects from the defaults file and adds them to the database. 
    * @throws Exception If problems occur. 
    */
   private final void loadDefaultProjects() throws Exception {
     // Get the default User definitions from the XML defaults file. 
     File defaultsFile = findDefaultsFile();
     // Add these users to the database if we've found a default file. 
     if (defaultsFile.exists()) {
       server.getLogger().info("Loading Project defaults from " + defaultsFile.getPath()); 
       Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
       Projects projects = (Projects) unmarshaller.unmarshal(defaultsFile);
       for (Project project : projects.getProject()) {
         provideDefaults(project);
         this.dbManager.storeProject(project, this.makeProject(project), 
             this.makeProjectRefString(project));
       }
     }
   } 
   
   /** Read in all Projects from the database and initialize the in-memory cache. */
   private final void initializeCache() {
     try {
       ProjectIndex index = makeProjectIndex(this.dbManager.getProjectIndex());
 
       for (ProjectRef ref : index.getProjectRef()) {
         String owner = ref.getOwner();
         User user = this.userManager.getUser(owner);
         // Check to make sure user exists.  DB is not normalized! 
         if (user == null) {
           String msg = "Project with undefined user '" + owner + "' found while initializing " 
           + " project cache from database. Project will be ignored.";
           server.getLogger().fine(msg);   
         }
         else {
           String projectName = ref.getName();
           if (this.hasProject(user, projectName)) {
             String msg = "Duplicate project for " + user + " with name " + projectName + 
             " found in database. Ignoring.";
             server.getLogger().warning(msg);
           }
           else {
             String projectString = this.dbManager.getProject(user, projectName);
             Project project = makeProject(projectString); 
             provideDefaults(project);
             this.updateCache(project);
           }
         }
       }
     }
     catch (Exception e) {
       server.getLogger().warning("Failed to initialize users " + StackTrace.toString(e));
     }
   }
   
   /**
    * Updates the in-memory cache with information about this Project. 
    * @param project The project to be added to the cache.
    * @throws Exception If problems occur updating the cache. 
    */
   private final void updateCache(Project project) throws Exception {
     provideDefaults(project);
     // Fix bogus default project start/end dates. 
     if (project.getName().equals("Default") && Tstamp.isBogusStartTime(project.getStartTime())) {
         project.setStartTime(Tstamp.getDefaultProjectStartTime());
         project.setEndTime(Tstamp.getDefaultProjectEndTime());
     }
     updateCache(project, this.makeProject(project), this.makeProjectRefString(project));
   }
   
   /**
    * Updates the cache given all the Project representations.
    * Throws unchecked exceptions if the Owner is not defined as a User.
    * @param project The Project.
    * @param projectXml The Project as an XML string. 
    * @param projectRef The Project as an XML reference. 
    */
   private final void updateCache(Project project, String projectXml, String projectRef) {
     // First, put the [owner, project] mapping
     String email = project.getOwner();
     User user = userManager.getUser(email);
     if (user == null) {
       throw new IllegalArgumentException("Project with undefined User " + email + " " + project);
     }
     if (!owner2name2project.containsKey(user)) {
       owner2name2project.put(user, new HashMap<String, Project>());
     }
     owner2name2project.get(user).put(project.getName(), project);
     this.project2xml.put(project, projectXml);
     this.project2ref.put(project, projectRef);
   }
   
   
   /** Make sure that all Users have a "Default" project defined for them. */ 
   private void initializeDefaultProjects() {
     for (User user : userManager.getUsers()) {
       if (!hasProject(user, "Default")) {
         addDefaultProject(user);
       }
     }
   }
   
   /**
    * Checks the ServerProperties for the XML_DIR property.
    * If this property is null, returns the File for ./xml/defaults/sensordatatypes.defaults.xml.
    * @return The File instance (which might not point to an existing file.)
    */
   private File findDefaultsFile() {
     String defaultsPath = "/defaults/projects.defaults.xml";
     String xmlDir = this.server.getServerProperties().get(XML_DIR_KEY);
     return (xmlDir == null) ?
         new File (System.getProperty("user.dir") + "/xml" + defaultsPath) :
           new File (xmlDir + defaultsPath);
   }
   
   /**
    * Converts an "Owner" string to an email address.
    * The owner string might be a URI (starting with http) or an email address. 
    * @param owner The owner string. 
    * @return The email address corresponding to the owner string. 
    */
   public synchronized String convertOwnerToEmail(String owner) {
     if (owner.startsWith(http)) {
       int lastSlash = owner.lastIndexOf('/');
       if (lastSlash < 0) {
         throw new IllegalArgumentException("Could not convert owner to URI");
       }
       return owner.substring(lastSlash + 1); 
     }
     // Otherwise owner is already the email. 
     return owner;
   }
   
   /**
    * Returns the owner string as a URI.
    * The owner string could either be an email address or the URI. 
    * @param owner The owner string. 
    * @return The URI corresponding to the owner string. 
    */
   public synchronized String convertOwnerToUri(String owner) {
     return (owner.startsWith(http)) ? owner :
       this.server.getServerProperties().getFullHost() + "users/" + owner;
   }
   
   /**
    * Returns the XML string containing the ProjectIndex with all defined Projects.
    * Uses the in-memory cache of ProjectRef strings.  
    * @return The XML string providing an index to all current Projects.
    */
   public synchronized String getProjectIndex() {
     StringBuilder builder = new StringBuilder(512);
     builder.append(projectIndexOpenTag);
     for (String ref : this.project2ref.values()) {
       builder.append(ref);
     }
     builder.append(projectIndexCloseTag);
     return builder.toString();
   }
   
   /**
    * Returns the XML string containing the ProjectIndex with all Projects associated with 
    * this user.
    * Uses the in-memory cache of ProjectRef strings.  
    * @param user The user whose associated Projects are to be retrieved. All projects for
    * which this user is an owner, member, spectator, or invitee are returned.
    * @return The XML string providing an index to all Projects associated with this user.
    */
   public synchronized String getProjectIndex(User user) {
     String email = user.getEmail();
     StringBuilder builder = new StringBuilder(512);
     builder.append(projectIndexOpenTag);
     for (Map<String, Project> name2project : this.owner2name2project.values()) {
       for (Project project : name2project.values()) {
         Members members = project.getMembers();
         Invitations invitations = project.getInvitations();
         Spectators spectators = project.getSpectators();
         if (project.getOwner().equals(email) ||
             (members != null) && (members.getMember().contains(email)) ||
             (spectators != null) && (spectators.getSpectator().contains(email)) ||
             (invitations != null) && (invitations.getInvitation().contains(email))) {
           builder.append(this.project2ref.get(project));   
         }
       }
     }
     builder.append(projectIndexCloseTag);
     return builder.toString();
   }  
   
   /**
    * Ensures that project has default values for Invitations, Members, Properties, 
    * Spectators, and UriPatterns.
    * @param project The project to check.
    * @return The project representation with initialized fields as needed.
    */
   private Project provideDefaults(Project project) {
     if (project.getInvitations() == null) {
       project.setInvitations(new Invitations());
     }
     if (project.getSpectators() == null) {
       project.setSpectators(new Spectators());
     }
     if (project.getMembers() == null) {
       project.setMembers(new Members());
     }
     if (project.getProperties() == null) {
       project.setProperties(new Properties());
     }
     if (project.getUriPatterns() == null) {
       // If missing, default to matching everything. 
       UriPatterns uriPatterns = new UriPatterns();
       uriPatterns.getUriPattern().add("*");
       project.setUriPatterns(uriPatterns);
     }
     if (project.getLastMod() == null) {
       project.setLastMod(Tstamp.makeTimestamp());
     }
     return project;
   }
   
   /**
    * Updates the Manager with this Project. Any old definition is overwritten.
    * Provide default values for UriPatterns, Properties, Members, and Invitations if not provided.
    * @param project The Project.
    */
   public synchronized void putProject(Project project) {
     try {
       provideDefaults(project);
       project.setLastMod(Tstamp.makeTimestamp());
       String xmlProject =  this.makeProject(project);
       String xmlRef =  this.makeProjectRefString(project);
       this.updateCache(project, xmlProject, xmlRef);
       this.dbManager.storeProject(project, xmlProject, xmlRef);
     }
     catch (Exception e) {
       server.getLogger().warning("Failed to put Project" + StackTrace.toString(e));
     }
   }
   
   /**
    * Renames the project.
    * @param owner The owner of the project to be renamed. 
    * @param projectName The project to be renamed.
    * @param newProjectName The new project name. 
    * @throws Exception If projectName could not be found, or if newProjectName names an 
    * existing project. 
    */
   public synchronized void renameProject(User owner, String projectName, String newProjectName)
   throws Exception {
     if (hasProject(owner, newProjectName)) {
       throw new Exception("Project " + newProjectName + " is already defined.");
     }
     Project project = getProject(owner, projectName);
     if (project == null) {
       throw new Exception("Project " + projectName + " not found.");
     }
     project.setName(newProjectName);
     deleteProject(owner, projectName);
     putProject(project);
   }
   
   /**
    * Returns true if the passed Project name is defined for this User (who must be the owner).
    * @param  owner The project owner (can be null). 
    * @param  projectName A project name (can be null).
    * @return True if a Project with that name is owned by that User.  False if the User or
    * Project is not defined. 
    */
   public synchronized boolean hasProject(User owner, String projectName) {
     return 
     (owner != null) &&
     (projectName != null) &&
     this.owner2name2project.containsKey(owner) &&
     this.owner2name2project.get(owner).containsKey(projectName);
   }
   
   /**
    * Returns true if member is a member of the project owned by owner. 
    * @param owner The owner of projectName.
    * @param projectName The name of the project owned by owner.
    * @param member The user whose membership is being checked.
    * @return True if member is a member of project, false otherwise. 
    */
   public synchronized boolean isMember(User owner, String projectName, String member) {
     // Return false if owner, project, member are invalid.
     if ((owner == null) || (member == null) || (projectName == null) ||
         !this.owner2name2project.containsKey(owner) ||
         !this.owner2name2project.get(owner).containsKey(projectName)) {
       return false;
     }
     // Now we can get the project.
     Project project = this.owner2name2project.get(owner).get(projectName);
     // Return false if the <Members> field is null.
     if (!project.isSetMembers()) {
       return false;
     }
     // Look for the member in the list.
     List<String> members = project.getMembers().getMember();
     for (String currMember : members) {
       if (currMember.equals(member)) {
         return true;
       }
     }
     // Got here, which means that we never found the member.
     return false;
   }
   
   /**
    * Returns true if member is invited to be a member of the project owned by owner. 
    * @param owner The owner of projectName.
    * @param projectName The name of the project owned by owner.
    * @param invitee The user whose invitation status is being checked.
    * @return True if member is invited to be a member of project, false otherwise. 
    */
   public synchronized boolean isInvited(User owner, String projectName, String invitee) {
     // Return false if owner, project, member are invalid.
     if ((owner == null) || (invitee == null) || (projectName == null) ||
         !this.owner2name2project.containsKey(owner) ||
         !this.owner2name2project.get(owner).containsKey(projectName)) {
       return false;
     }
     // Now we can get the project.
     Project project = this.owner2name2project.get(owner).get(projectName);
     // Return false if the <Members> field is null.
     if (!project.isSetInvitations()) {
       return false;
     }
     // Look for the member in the list.
     List<String> invitees = project.getInvitations().getInvitation();
     for (String currInvitee : invitees) {
       if (currInvitee.equals(invitee)) {
         return true;
       }
     }
     // Got here, which means that we never found the invitee.
     return false;
   }
   
   /**
    * Returns true if member is a spectator of the project owned by owner. 
    * @param owner The owner of projectName.
    * @param projectName The name of the project owned by owner.
    * @param spectator The user whose spectator status is being checked.
    * @return True if spectator is a spectator. 
    */
   public synchronized boolean isSpectator(User owner, String projectName, String spectator) {
     // Return false if owner, project, member are invalid.
     if ((owner == null) || (spectator == null) || (projectName == null) ||
         !this.owner2name2project.containsKey(owner) ||
         !this.owner2name2project.get(owner).containsKey(projectName)) {
       return false;
     }
     // Now we can get the project.
     Project project = this.owner2name2project.get(owner).get(projectName);
     // Return false if the <Spectators> field is null.
     if (!project.isSetSpectators()) {
       return false;
     }
     // Look for the member in the list.
     List<String> spectators = project.getSpectators().getSpectator();
     for (String currSpectator : spectators) {
       if (currSpectator.equals(spectator)) {
         return true;
       }
     }
     // Got here, which means that we never found the invitee.
     return false;
   }
   
   
   /**
    * Returns true if user1 and user2 are members of the same Project and 
    * that project encompasses the given day.
    * @param userEmail1 The first user.
    * @param userEmail2 The second user.
    * @param tstampString The date in question, which could be null.
    * @return True if the two users are in the same project that encompasses the given day.
    */
   public synchronized boolean inProject(String userEmail1, String userEmail2, String tstampString) {
     // If any params are null, return false.
     if ((tstampString == null) || (userEmail1 == null) || (userEmail2 == null)) {
       return false;
     }
     // If either email cannot be converted to a user, return false.
     User user1 = this.userManager.getUser(userEmail1);
     if (user1 == null) {
       return false;
     }
     User user2 = this.userManager.getUser(userEmail2);
     if (user2 == null) {
       return false;
     }
     // Return false if timestamp is null or cannot be converted to a real timestamp.
     XMLGregorianCalendar timestamp = null;
     try {
       timestamp = Tstamp.makeTimestamp(tstampString);
     }
     catch (Exception e) {
       return false;
     }
     // Now look through all projects and see if there is a project with both users that encompasses
     // the given timestamp.
     for (Entry<User, Map<String, Project>> entry : this.owner2name2project.entrySet()) {
       for (Project project : entry.getValue().values()) {
         if (belongs(project, user1) && belongs(project, user2) && 
             Tstamp.inBetween(project.getStartTime(), timestamp, project.getEndTime())) {
           return true;
         }
       }
     }
     return false;
   }
 
   /**
    * Returns true if user is the owner or a member or a spectator of Project.
    * @param project The project. 
    * @param user The user who's belonging is being assessed. 
    * @return True if user is the owner or a member of project.
    */
   private boolean belongs(Project project, User user) {
     return 
     (project.getOwner().equals(user.getEmail()) ||
      project.getMembers().getMember().contains(user.getEmail()) ||
      project.getSpectators().getSpectator().contains(user.getEmail())
     );
   }
   
   
   /**
    * Ensures that the passed Project is no longer present in this Manager. 
    * @param owner The user who owns this Project.
    * @param projectName The name of the project.
    */
   public synchronized void deleteProject(User owner, String projectName) {
     if (this.owner2name2project.containsKey(owner)) {
       Project project = this.owner2name2project.get(owner).get(projectName);
       if (project != null) {
         this.project2ref.remove(project);
         this.project2xml.remove(project);
         this.owner2name2project.get(owner).remove(projectName);
       }
     }
     this.dbManager.deleteProject(owner, projectName);
   }
   
   /**
    * Returns the Project Xml String associated with this User and project name.
    * @param owner The user that owns this project.
    * @param projectName The name of the project.
    * @return The Project XML string, or null if not found.
    */
   public synchronized String getProjectString(User owner, String projectName) {
     if (hasProject(owner, projectName)) {
       Project project = this.owner2name2project.get(owner).get(projectName);
       return this.project2xml.get(project);
     }
     return null;
   }  
   
   /**
    * Returns a set containing the current Project instances. 
    * For thread safety, a fresh Set of Projects is built each time this is called. 
    * @return A Set containing the current Projects. 
    */
   public synchronized Set<Project> getProjects() {
     Set<Project> projectSet = new HashSet<Project>(projectSetSize);
     for (User user : this.owner2name2project.keySet()) {
       for (String projectName : this.owner2name2project.get(user).keySet()) {
         projectSet.add(this.owner2name2project.get(user).get(projectName));
       }
     }
     return projectSet;
   }
  
  
   /**
    * Returns an XML SensorDataIndex String for all data associated with the Project
    * owned by this user.
    * Assumes that the owner and projectName define an existing Project.
    * @param owner The User that owns this Project.
    * @param project the Project instance.
    * @return The XML SensorDataIndex string providing an index to all data for this project.
    * @throws Exception If things go wrong. 
    */
   public synchronized String getProjectSensorDataIndex(User owner, Project project) 
   throws Exception {
     SensorDataManager sensorDataManager = this.getSensorDataManager(); 
     XMLGregorianCalendar startTime = project.getStartTime();
     XMLGregorianCalendar endTime = project.getEndTime();
     List<String> patterns = project.getUriPatterns().getUriPattern();
     List<User> users = getProjectUsers(project);
     return sensorDataManager.getSensorDataIndex(users, startTime, endTime, patterns, null);
   }
   
 
   /**
    * Returns the XML SensorDataIndex string for the data associated with this Project within the 
    * specified start and end times.
    * Assumes that owner, project, startTime, and endTime are non-null, and that startTime and
    * endTime are within the Project start and end time interval.
    * @param owner The User who owns this Project. 
    * @param project the Project.
    * @param startTime The startTime.
    * @param endTime The endTime.
    * @param sdt The SensorDataType of interest, or null if all sensordatatypes are to be retrieved.
    * @return The XML String providing a SensorDataIndex to the sensor data in this project
    * starting at startTime and ending at endTime. 
    * @throws Exception if problems occur.
    */  
   public synchronized String getProjectSensorDataIndex(User owner, 
       Project project, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, String sdt) 
   throws Exception {
     SensorDataManager sensorDataManager = this.getSensorDataManager(); 
     List<String> patterns = project.getUriPatterns().getUriPattern();
     List<User> users = getProjectUsers(project);
     return sensorDataManager.getSensorDataIndex(users, startTime, endTime, patterns, sdt);
   }
   
   /**
    * Returns the XML SensorDataIndex string for the data associated with this Project within the 
    * specified start and end times.
    * Assumes that owner, project, startTime, and endTime are non-null, and that startTime and
    * endTime are within the Project start and end time interval.
    * @param owner The User who owns this Project. 
    * @param project the Project.
    * @param startTime The startTime.
    * @param endTime The endTime.
    * @param sdt The SensorDataType of interest, or null if all sensordatatypes are to be retrieved.
    * @param tool The tool of interest.
    * @return The XML String providing a SensorDataIndex to the sensor data in this project
    * starting at startTime and ending at endTime. 
    * @throws Exception if problems occur.
    */  
   public synchronized String getProjectSensorDataIndex(User owner, 
       Project project, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, String sdt, 
       String tool) 
   throws Exception {
     SensorDataManager sensorDataManager = this.getSensorDataManager(); 
     List<String> patterns = project.getUriPatterns().getUriPattern();
     List<User> users = getProjectUsers(project);
     return sensorDataManager.getSensorDataIndex(users, startTime, endTime, patterns, sdt, tool);
   }
   
   /**
    * Returns the XML SensorDataIndex string for the data associated with this Project within the 
    * specified start and end times and startIndex and maxInstances.
    * Assumes that owner, project, startTime, and endTime are non-null, and that startTime and
    * endTime are within the Project start and end time interval, and that startIndex and 
    * maxInstances are non-negative.
    * @param owner The User who owns this Project. 
    * @param project the Project.
    * @param startTime The startTime.
    * @param endTime The endTime.
    * @param startIndex The starting index within the timestamp-ordered list of all sensor data
    * instances associated with this project at the time of this call.
    * @param maxInstances The maximum number of instances to return in the index.
    * @return The XML String providing a SensorDataIndex to the sensor data in this project
    * starting at startTime and ending at endTime with the specified startIndex and maxInstances. 
    * @throws Exception if problems occur.
    */  
   public synchronized String getProjectSensorDataIndex(User owner, 
       Project project, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, int startIndex,
       int maxInstances) 
   throws Exception {
     SensorDataManager sensorDataManager = this.getSensorDataManager(); 
     List<String> patterns = project.getUriPatterns().getUriPattern();
     List<User> users = getProjectUsers(project);
     return sensorDataManager.getSensorDataIndex(users, startTime, endTime, patterns, startIndex,
         maxInstances);
   }
   
   /**
    * Returns a string containing a SensorDataIndex representing the "snapshot" of the sensor data
    * for the given project in the given interval for the given sdt.  Tool is optional and can be 
    * null.
    * @param project The project.
    * @param startTime The start time.
    * @param endTime The end time.
    * @param sdt The sensor data type of interest. 
    * @param tool The tool of interest, or null if any tool is acceptable.
    * @return The SensorDataIndex containing the snapshot of sensor data. 
    * @throws Exception If problems occur.
    */
   public synchronized String getProjectSensorDataSnapshot(
       Project project, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, String sdt,
       String tool)  throws Exception {
     List<String> patterns = project.getUriPatterns().getUriPattern();
     List<User> users = getProjectUsers(project);
     return dbManager.getProjectSensorDataSnapshot(users, startTime, endTime, patterns, sdt, tool);
   }
   
   /**
    * Creates and returns the list of User instances associated with project.
    * The users are the owner plus all members.
    * If the owner email or member emails cannot be resolved to User instances, they are silently
    * ignored.
    * @param project The project whose users are to be found and returned.
    * @return The list of Users associated with this project.
    */
   private List<User> getProjectUsers(Project project) {
     List<User> users = new ArrayList<User>();
     User owner = userManager.getUser(project.getOwner());
     if (owner != null) {
       users.add(owner);
     }
     for (String member : project.getMembers().getMember()) {
       User user = userManager.getUser(member);
       if (user != null) {
         users.add(user);
       }
     }
     return users; 
   }
   
 
   /**
    * Returns the XML ProjectSummary string for the data associated with this Project within the 
    * specified start and end times.
    * Note that the Project start and end times may further constrain the returned set of data. 
    * This method chooses the greater of startString and the Project startTime, and the lesser of
    * endString and the Project endTime. 
    * Assumes that User and Project are valid.
    * @param project the Project.
    * @param startTime The startTimeXml.
    * @param endTime The endTime.
    * @return The XML String providing a ProjectSummary of this project
    * starting at startTime and ending at endTime. 
    * @throws Exception if startString or endString are not XMLGregorianCalendars.
    */  
   public synchronized String getProjectSummaryString(Project project, 
       XMLGregorianCalendar startTime, XMLGregorianCalendar endTime) throws Exception {
     List<String> patterns = project.getUriPatterns().getUriPattern();
     List<User> users = getProjectUsers(project);
     String href = this.server.getHostName() + "projects/" + project.getOwner() + "/" +
     project.getName() + "/summary?startTime=" + startTime + "&endTime=" + endTime;
     ProjectSummary summary = dbManager.getProjectSummary(users, startTime, endTime, patterns, href);
     return makeProjectSummaryString(summary);
   }
   
   /**
    * Returns a MultiDayProjectSummary instance for the given project, startTime, and number of days.
    * @param project The Project.
    * @param startTime The startTime. 
    * @param numDays The number of days. 
    * @return The MultiDayProjectSummary instance for the given set of days. 
    * @throws Exception If problems occur. 
    */
   public synchronized String getMultiDayProjectSummaryString(Project project, 
       XMLGregorianCalendar startTime, Integer numDays) throws Exception {
     List<String> patterns = project.getUriPatterns().getUriPattern();
     List<User> users = getProjectUsers(project);
     MultiDayProjectSummary multiSummary = new MultiDayProjectSummary();
     for (int i = 0; i < numDays; i++) {
       XMLGregorianCalendar start = Tstamp.incrementDays(startTime, i);
       XMLGregorianCalendar end = Tstamp.incrementDays(startTime, i + 1);
       String href = this.server.getHostName() + "projects/" + project.getOwner() + "/" +
       project.getName() + "/summary?startTime=" + start + "&endTime=" + end;
       ProjectSummary summary = dbManager.getProjectSummary(users, start, end, patterns, href);
       multiSummary.getProjectSummary().add(summary);
     }
     return makeMultiDayProjectSummaryString(multiSummary);
   }
   
   
   /**
    * Creates and stores the "Default" project for the specified user. 
    * @param owner The user who will own this Project.
    */
   public final synchronized void addDefaultProject(User owner) {
     Project project = new Project();
     provideDefaults(project);
     project.setDescription("The default Project");
     project.setStartTime(Tstamp.getDefaultProjectStartTime());
     project.setEndTime(Tstamp.getDefaultProjectEndTime());
     project.setName(DEFAULT_PROJECT_NAME);
     project.setOwner(owner.getEmail());
    project.getUriPatterns().getUriPattern().add("**");
     putProject(project);
   }
   
   /**
    * Returns true if the passed user has any defined Projects.
    * @param  owner The user who is the owner of the Projects.
    * @return True if that User is defined and has at least one Project.
    */
   public synchronized boolean hasProjects(User owner) {
     return this.owner2name2project.containsKey(owner);
   }
   
   /**
    * Returns the Project associated with user and projectName, or null if not found.
    * @param  owner The user. 
    * @param  projectName A project name
    * @return The project, or null if not found.
    */
   public synchronized Project getProject(User owner, String projectName) {
     if ((owner == null) || (projectName == null)) {
       return null;
     }
     Project project = ((hasProject(owner, projectName)) ? 
         owner2name2project.get(owner).get(projectName) : null);
     if (project != null) {
       project = provideDefaults(project);
     }
     return project;
 
   }
   
   /**
    * Takes a String encoding of a Project in XML format and converts it to an instance. 
    * 
    * @param xmlString The XML string representing a Project
    * @return The corresponding Project instance. 
    * @throws Exception If problems occur during unmarshalling.
    */
   public final synchronized Project makeProject(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
     return (Project)unmarshaller.unmarshal(new StringReader(xmlString));
   }
   
   /**
    * Takes a String encoding of a ProjectIndex in XML format and converts it to an instance. 
    * 
    * @param xmlString The XML string representing a ProjectIndex.
    * @return The corresponding ProjectIndex instance. 
    * @throws Exception If problems occur during unmarshalling.
    */
   public final synchronized ProjectIndex makeProjectIndex(String xmlString) 
   throws Exception {
     Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
     return (ProjectIndex)unmarshaller.unmarshal(new StringReader(xmlString));
   }
   
   /**
    * Returns the passed Project instance as a String encoding of its XML representation.
    * Final because it's called in constructor.
    * @param project The Project instance. 
    * @return The XML String representation.
    * @throws Exception If problems occur during translation. 
    */
   public final synchronized String makeProject (Project project) throws Exception {
     Marshaller marshaller = jaxbContext.createMarshaller(); 
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(project, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction.  This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
 
   /**
    * Returns the passed Project instance as a String encoding of its XML representation 
    * as a ProjectRef object.
    * Final because it's called in constructor.
    * @param project The Project instance. 
    * @return The XML String representation of it as a ProjectRef
    * @throws Exception If problems occur during translation. 
    */
   public final synchronized String makeProjectRefString (Project project) 
   throws Exception {
     ProjectRef ref = makeProjectRef(project);
     Marshaller marshaller = jaxbContext.createMarshaller(); 
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(ref, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction.  This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
   
   /**
    * Returns the passed ProjectSummary instance as a String encoding of its XML representation. 
    * @param summary The ProjectSummary instance. 
    * @return The XML String representation of it.
    * @throws Exception If problems occur during translation. 
    */
   public final synchronized String makeProjectSummaryString (ProjectSummary summary) 
   throws Exception {
     Marshaller marshaller = jaxbContext.createMarshaller(); 
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(summary, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction.  This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
   
   /**
    * Returns the passed MultiDayProjectSummary as a String encoding of its XML representation. 
    * @param summary The MultiDayProjectSummary instance. 
    * @return The XML String representation of it.
    * @throws Exception If problems occur during translation. 
    */
   public final synchronized String makeMultiDayProjectSummaryString (MultiDayProjectSummary summary)
   throws Exception {
     Marshaller marshaller = jaxbContext.createMarshaller(); 
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(summary, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction.  This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
   /**
    * Returns a ProjectRef instance constructed from a Project instance.
    * @param project The Project instance. 
    * @return A ProjectRef instance. 
    */
   public synchronized ProjectRef makeProjectRef(Project project) {
     ProjectRef ref = new ProjectRef();
     String ownerEmail = convertOwnerToEmail(project.getOwner());
     ref.setName(project.getName());
     ref.setOwner(ownerEmail);
     ref.setLastMod(project.getLastMod());
     ref.setHref(this.server.getHostName() + "projects/" + ownerEmail + "/" + project.getName()); 
     return ref;
   }
   
   /**
    * Returns the SensorDataManager. 
    * @return The SensorDataManager. 
    */
   private SensorDataManager getSensorDataManager() {
     return (SensorDataManager)this.server.getContext().getAttributes().get("SensorDataManager");
   }
   
 }
