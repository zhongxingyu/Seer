 package models.ma;
 
 import controllers.security.Secure;
 import models.main.Person;
 import models.rh.Department;
 import models.security.User;
 import play.data.binding.As;
 import play.db.jpa.Model;
 import utils.Utils;
 
 import javax.persistence.*;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 @Entity
 public class ResidentRequest extends Model {
 
     @ManyToOne
     public Person resident;
     @Temporal(TemporalType.DATE)
     public Date requestStart;
     @Temporal(TemporalType.DATE)
     public Date requestEnd;
     @Temporal(TemporalType.DATE)
     public Date rdvDay;
     @As("dd-MM-yyyy HH:mm")
     @Temporal(TemporalType.TIME)
     public Date rdvTime;
     public int status;
     public boolean enabled;
     public boolean medicalReport;
     @ManyToOne
     public Person contactPerson;
     public String applicant;
     @ManyToOne
     public Person personHome;
     @Lob
     public String comment;
     public boolean room;
     public boolean interview;
     @ManyToOne
     public User user;
     public int modalityHome;
     public int applicantHome;
     public int prevailingSituation;
     public int livingPlace;
     public int lastLivingPlace;
 
     public static List<ResidentRequest> getCurrents() {
         return ResidentRequest.find("byEnabled", true).fetch();
     }
     
     public static void saveRequest(ResidentRequest request, Person resident,
             String firstAction, Date creationDate, Date endDate) {
         resident.name = resident.name.toLowerCase();
         resident.firstname = resident.firstname.toLowerCase();
 
         // Reservation d'une chambre ou annulation de celle ci
         Hostel h = Hostel.find("byName", "Petits Riens").first();
         if (firstAction != null && firstAction.equals("room")) {
             if (!request.room) {
                 h.bookedUp++;
                 h.save();
             }
             request.room = true;
             request.interview = false;
         } else {
             if (request.room) {
                 h.bookedUp--;
                 h.save();
             }
             request.interview = true;
             request.room = false;
         }
 
         if (creationDate == null) {
             request.requestStart = new Date();
         }else{
             request.requestStart = creationDate;
         }
 
         if (endDate == null) {
             request.requestEnd = new Date();
         }else{
             request.requestEnd = endDate;
         }
 
         // la demande d'hebergement est acceptée
         if (request.status == 2) {
 
             if (resident.folderNumber == 0) {
                 resident.folderNumber = Person.getNewFolderNumber();
             }
             if (request.room) {
 
                 h.bookedUp--;
                 h.save();
             }
             Stay s = Stay.lastActif(resident.id);
             if (s != null) {
                 s.actif = false;
                 s.save();
                 Stay.closeStay(s);
             }
             
             Department dep = Department.getX();
 
             resident.department = dep;
             resident.enabled = true;
             resident.setToResident();
             resident.save();
 
             s = new Stay();
             s.actif = true;
             s.inDate = request.requestEnd;
             s.resident = resident;
             s.stayNumber = Stay.nbStays(resident) + 1;
             s.department = dep;
             s.save();
 
             InSummary in = new InSummary();
             in.actif = true;
             in.resident = resident;
             in.stay = s;
             in.save();
 
             Evaluation e = new Evaluation();
             e.actif = true;
             e.resident = resident;
             e.stay = s;
             GregorianCalendar d = new GregorianCalendar();
             d.add(Calendar.WEEK_OF_YEAR, 6);
             e.evaluationDate = d.getTime();
             e.save();
 
             Activation act = new Activation();
             act.activationID = s.id;
             act.activationType = 5;
             act.person = s.resident;
             act.personType = s.resident.getPersonType();
             act.startDate = new Date();
             act.save();
 
             Report report = Report.find("byReportDate", new GregorianCalendar()).first();
 
             if (report == null) {
                 report = new Report();
                 report.reportDate = new GregorianCalendar();
                 report.save();
             }
 
             ReportCategory rc = ReportCategory.getInOut();
             String link = Utils.residentFolderLink(resident);
 
             String rq = "Entrant : " + link + " - " + resident.folderNumber;
 
             ReportMessage rm = new ReportMessage();
             rm.createAt = new Date();
             rm.message = rq;
            rm.messageOrder = (int) ReportMessage.count("report = ?",report);
             rm.report = report;
             rm.reportCategory = rc;
             rm.user = User.find("username = ?", Secure.Security.connected()).first();
             rm.save();
 
             resident.nbStays = Stay.nbStays(resident);
 
         }
 
         // demande refusé, annule la reservation de la chambre s'il y avait une
         if (request.status > 2) {
             if (request.room && request.enabled) {
                 h.bookedUp--;
                 h.save();
             }
             resident.personStatus.remove("001");
             resident.personStatus.add("004");
         }
 
         User user = User.loadFromSession();
         request.contactPerson = user.person;
         
 
         if (ResidentRequest.count("resident = ?", resident) == 1) {
             resident.withPig = true;
             resident.driverLicence = -1;
         }
 
         resident.save();
 
         request.resident = resident;
 
         if (request.status != 1) {
             request.enabled = false;
         }
 
         request.save();
     }
 
     public static void saveOldRequest(ResidentRequest request, Person resident,
             String firstAction, Date creationDate, Date endDate){
         resident.name = resident.name.toLowerCase();
         resident.firstname = resident.firstname.toLowerCase();
 
         if (firstAction != null && firstAction.equals("room")) {
 
             request.room = true;
             request.interview = false;
         } else {
 
             request.interview = true;
             request.room = false;
         }
 
         if (creationDate != null) {
             request.requestStart = creationDate;
 
         }
 
         if (endDate != null) {
             request.requestEnd = endDate;
         }else{
             request.requestEnd = new Date();
         }
 
         resident.save();
 
         request.resident = resident;
         request.save();
 
     }
 
     public static long nbOldRequest() {
         return ResidentRequest.count("enabled = false and status != 2");
     }
 
     public static List<ResidentRequest> getOldRequests(int page,int perPage) {
         return ResidentRequest.find("enabled = false "
                 + "and status != 2 "
                 + "order by rdvDay desc,resident.name,resident.firstname ").fetch(page, perPage);
     }
 
     public static ResidentRequest lastDone(Person resident) {
         return ResidentRequest.find("requestEnd is not null "
                 + "and resident = ? "
                 + "order by requestEnd desc", resident).first();
     }
 
     public static List<ResidentRequest> byPeriod(Calendar from,Calendar to) {
         return ResidentRequest.find("requestStart >= ? " +
                 "and requestStart <= ?",from.getTime(),to.getTime()).fetch();
     }
 }
