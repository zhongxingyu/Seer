 package org.hackystat.sensorbase.resource.projects;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Variant;
 
 /**
  * The resource for processing 
  * [host]/projects/[owner]/[project]/snapshot?sdt=[sdt]&startTime=[tstamp]&endTime=[tstamp]&tool=[t]
  * Returns a SensorDataIndex containing the "snapshot" sensor data for this project with the
  * given sdt and time interval.   The "snapshot" is the data set with the most recent runtime
  * value for the given time interval.  The tool parameter is optional.  
  * 
  * @author Philip Johnson
  */
 public class UserProjectSnapshotResource extends SensorBaseResource {
   
   /** The user corresponding to email, or null if not found. */
   private User user;
   /** To be retrieved from the URL. */
   private String projectName;
   /** An optional query parameter */
   private String startTime;
   /** An optional query string parameter. */
   private String endTime;
   /** An optional query parameter. */
   private String sdt;
   /** An optional query parameter. */
   private String tool;
 
   
   /**
    * Provides the following representational variants: TEXT_XML.
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public UserProjectSnapshotResource(Context context, Request request, Response response) {
     super(context, request, response);
     this.projectName = (String) request.getAttributes().get("projectname"); 
     this.startTime = (String) request.getAttributes().get("startTime");
     this.endTime = (String) request.getAttributes().get("endTime");
     this.sdt = (String) request.getAttributes().get("sdt");
     this.tool = (String) request.getAttributes().get("tool");
     this.user = super.userManager.getUser(super.uriUser);
   }
   
   /**
    * Returns a SensorDataIndex containing the "snapshot" for this sdt, time interval,
    * and tool (if supplied).  The "snapshot" is the data with the most recent runtime value.
    * 
    * Returns an error condition if:
    * <ul>
    * <li> The user does not exist.
    * <li> The authenticated user is not the uriUser or the Admin or a member of the project.
    * <li> The Project Resource named by the User and Project does not exist.
    * <li> startTime or endTime is not an XMLGregorianCalendar string.
    * <li> endTime is earlier than startTime.
    * </ul>
    * 
    * @param variant The representational variant requested.
    * @return The representation. 
    */
   @Override
   public Representation getRepresentation(Variant variant) {
     // The user (project owner) must be defined.
     if (this.user == null) {
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user");
       return null;
     } 
     // The project must be defined.
     Project project = super.projectManager.getProject(this.user, this.projectName);
     if (project == null) {
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown project");
       return null;
     }
     // The authorized user must be an admin, or the project owner, or a member, or invitee.
     if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser) &&
         !super.projectManager.isMember(this.user, this.projectName, this.authUser) &&
        !super.projectManager.isInvited(this.user, this.projectName, this.authUser) &&
        !super.projectManager.isSpectator(this.user, this.projectName, this.authUser)) {
       String msg = "User " + this.authUser + "is not authorized to obtain data from this Project.";
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
       return null;
     }
     // Both startTime and endTime must be XMLGregorianCalendars,
     // and startTime must be <= endTime.
     XMLGregorianCalendar startTimeXml = null;
     XMLGregorianCalendar endTimeXml = null;
     try {
       startTimeXml = Tstamp.makeTimestamp(this.startTime);
       endTimeXml = Tstamp.makeTimestamp(this.endTime);
     }
     catch (Exception e) {
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
           "startTime (or endTime) is not supplied and/or is not a timestamp");
       return null;
     }
     // We have a legal start and end time. Make sure startTime is not greater than endTime.
     if (Tstamp.greaterThan(startTimeXml, endTimeXml)) {
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
           "startTime cannot be greater than endTime.");
       return null;
     }
     // Make sure that startTime is not less than project.startTime.
     if (!ProjectUtils.isValidStartTime(project, startTimeXml)) {
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
       startTimeXml + " cannot be less than the project's start time: " + project.getStartTime());
       return null;
     }
     // And that endTime is not past the project endTime (if there is a project endTime).
     if ((project.getEndTime() != null) && 
         (!ProjectUtils.isValidEndTime(project, endTimeXml))) {
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
       "endTime cannot be greater than the project's end time.");
       return null;
     }
     if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
       try {
         // Return the snapshot.
         String data = super.projectManager.getProjectSensorDataSnapshot(project,
             startTimeXml, endTimeXml, sdt, tool);
         return SensorBaseResource.getStringRepresentation(data);
       }
       catch (Exception e) {
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Problem: " + e.getMessage());
         return null;
       }
     }
     return null;
   }
 }
