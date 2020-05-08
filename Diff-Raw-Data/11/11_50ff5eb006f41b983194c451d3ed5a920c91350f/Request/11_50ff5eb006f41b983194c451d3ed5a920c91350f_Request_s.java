 package models;
 
 import java.util.List;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 import org.joda.time.DateTime;
 
 import play.Play;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 import controllers.Application;
 
 /**
  * Represents a request for a tutoring session
  */
 public class Request extends Model {
 
   private static final long serialVersionUID = -8504063147212754838L;
 
   @Id
   @GeneratedValue
   public Long id;
 
   // The student requesting this session
   private Student requestingStudent;
 
   // The tutor this request is being made of
   private Tutor requestedTutor;
 
   // The requested start time
   @Required
   private long requestedStartTime;
 
   // The requested end time
   @Required
   private long requestedEndTime;
 
   // True if this request has already been approved, false otherwise
   private boolean approved;
 
   public Request(Student requestingStudent, Tutor requestedTutor,
       long requestedStartTime, long requestedEndTime) {
     this(requestingStudent, requestedTutor, requestedStartTime,
         requestedEndTime, false);
   }
 
   public Request(Student requestingStudent, Tutor requestedTutor,
       long requestedStartTime, long requestedEndTime,
       boolean approved) {
     this.requestingStudent = requestingStudent;
     this.requestedTutor = requestedTutor;
     this.requestedStartTime = requestedStartTime;
     this.requestedEndTime = requestedEndTime;
     this.approved = approved;
     Request.create(this);
   }
 
   public static Finder<Long, Request> find = new Finder<Long, Request>(
       Long.class, Request.class);
 
   /**
    * @return the list of all requests
    */
   public static List<Request> all() {
     return find.all();
   }
 
   public static void create(Request request) {
     request.save();
   }
 
   public static void delete(Long id) {
     find.ref(id).delete();
   }
 
   /**
    * @return the requestinStudent
    */
   public Student getRequestingStudent() {
     return requestingStudent;
   }
 
   /**
    * @param requestinStudent the requestinStudent to set
    */
   public void setRequestingStudent(Student requestingStudent) {
     this.requestingStudent = requestingStudent;
   }
 
   /**
    * @return the requestedTutor
    */
   public Tutor getRequestedTutor() {
     return requestedTutor;
   }
 
   /**
    * @param requestedTutor the requestedTutor to set
    */
   public void setRequestedTutor(Tutor requestedTutor) {
     this.requestedTutor = requestedTutor;
   }
 
   /**
    * @return the startTime
    */
   public long getStartTime() {
     return requestedStartTime;
   }
 
   /**
    * @param startTime the startTime to set
    */
   public void setStartTime(long startTime) {
     this.requestedStartTime = startTime;
   }
 
   /**
    * @return the endTime
    */
   public long getEndTime() {
     return requestedEndTime;
   }
 
   /**
    * @param endTime the endTime to set
    */
   public void setEndTime(long endTime) {
     this.requestedEndTime = endTime;
   }
 
   /**
    * @return the approved
    */
   public boolean isApproved() {
     return approved;
   }
 
   /**
    * @param approved the approved to set
    */
   public void setApproved(boolean approved) {
     this.approved = approved;
   }
 
   /**
    * Notifies a tutor that a request has been created or canceled
    * 
    * @param created: True if this is a new request, false otherwise
    */
   public void sendRequestNotification() {
     String emailSubject = "New Tutor Request";
     String emailRecipient = requestedTutor.getName() + "<"
    + requestedTutor.getEmail() + ">";
     String emailHtml = "Hi, "
      + requestedTutor.getName()
      + "<br /><br />You have a new tutoring request. To review it, please log in to your account at <a href=\""
      + Play.application().path() + "\">" + Play.application().path()
      + "</a>.";
     Application.sendEmail(emailSubject, emailRecipient, emailHtml);
   }
 
   /**
    * Notifies a student or tutor of a canceled request
    * 
    * @param student: True if the recipient is a student, false otherwise
    */
   public void sendCancellationNotification(boolean student) {
     String emailSubject = "Cancelled Tutor Request";
     String name;
     String email;
     if (student) {
       name = requestingStudent.getName();
       email = requestingStudent.getEmail();
     } else {
       name = requestedTutor.getName();
       email = requestedTutor.getEmail();
     }
     String emailRecipient = name + "<" + email + ">";
     DateTime startTimeDate = new DateTime(requestedStartTime);
     String startTimeString = startTimeDate.toGregorianCalendar().toString();
     DateTime endTimeDate = new DateTime(requestedEndTime);
     String endTimeString = endTimeDate.toGregorianCalendar().toString();
     String emailHtml = "Hi, "
       + name
       + "<br /><br />Your tutoring request from "
       + startTimeString
       + " to "
       + endTimeString
       + ".  To review outstanding requests, please log in to your account at <a href=\""
       + Play.application().path() + "\">" + Play.application().path()
       + "</a>.";
     Application.sendEmail(emailSubject, emailRecipient, emailHtml);
   }
 
   /**
    * Notifies a student and tutor of an upcoming sesssion
    * 
    * @param session: The upcoming session
    */
   public void sendSessionNotifications(Session session) {
     String sessionNotificationSubject = "New Tutor.me Session";
     Tutor sessionTutor = session.getTutor();
     String tutorRecipient = sessionTutor.getName() + " <"
     + sessionTutor.getEmail() + ">";
     Student sessionStudent = session.getStudent();
     String studentRecipient = sessionStudent.getName() + " <"
     + sessionStudent.getEmail() + ">";
     DateTime startTimeDate = new DateTime(session.getStartTime());
     String startTimeString = startTimeDate.toGregorianCalendar().toString();
     DateTime endTimeDate = new DateTime(session.getEndTime());
     String endTimeString = endTimeDate.toGregorianCalendar().toString();
     String emailText =
       "  You have been signed up for a Tutor.Me session! <br/>"
       + "  Your session is from"
       + startTimeString
       + " to "
       + endTimeString
       + "<br/>"
       + "  Please use the following link to access your session at the specified time: ";
     String tutorLink = Play.application().path() + "/"
     + session.getScribblarId() + "/" + sessionTutor.getScribblarId();
     String studentLink = Play.application().path() + "/"
     + session.getScribblarId() + "/" + sessionStudent.getScribblarId();
     String tutorEmailHtml = "<hmtl>" + emailText + "<br/>" + tutorLink
     + "</hmtl>";
     String studentEmailHtml = "<hmtl>" + emailText + "<br/>" + studentLink
     + "</hmtl>";
     Application.sendEmail(sessionNotificationSubject, tutorRecipient,
         tutorEmailHtml);
     Application.sendEmail(sessionNotificationSubject, studentRecipient,
         studentEmailHtml);
   }
 
   public Session generateSession() {
     Session session = new Session(requestingStudent, requestedTutor,
         requestedStartTime, requestedEndTime);
     return session;
   }
 }
