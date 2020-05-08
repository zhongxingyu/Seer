 package id.web.martinusadyh.logbook.domain.trx;
 
 import id.web.martinusadyh.logbook.domain.Category;
 import id.web.martinusadyh.logbook.domain.BaseEntity;
 import id.web.martinusadyh.logbook.domain.Module;
import id.web.martinusadyh.logbook.domain.utility.UserProfile;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import org.hibernate.annotations.Index;
 
 /**
  *
  * @author martinus
  */
 @Entity
 @Table(name="logbook_details")
 @org.hibernate.annotations.Table(
         appliesTo="logbook_details",
         indexes={
             @Index(name="idx_time_reporting", columnNames={"time_reporting"}),
             @Index(name="idx_problem", columnNames={"problem"}), 
             @Index(name="idx_suspect", columnNames={"suspect"}),
             @Index(name="idx_solution", columnNames={"solution"})
         }
 )
 public class LogBookDetails extends BaseEntity {
     
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     @Column(name="time_reporting")
     private Date timeReporting;
     
     @ManyToOne
     private UserProfile receivedBy;
     
     @Column(name="from_user")
     private String fromUser;
     
     @Column(name="problem")
     private String problem;
     
     @Column(name="suspect")
     private String suspect;
     
     @Column(name="solution")
     private String solution;
     
     @ManyToOne
     private LogBookHeader logBookHeader;
     
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     @Column(name="time_solved")
     private Date timeSolved;
     
     @ManyToOne
     private UserProfile solvedBy;
     
     @ManyToOne
     private Category category;
     
     @ManyToOne
     private Module module;
 
     public Category getCategory() {
         return category;
     }
 
     public void setCategory(Category category) {
         this.category = category;
     }
 
     public String getFromUser() {
         return fromUser;
     }
 
     public void setFromUser(String fromUser) {
         this.fromUser = fromUser;
     }
 
     public LogBookHeader getLogBookHeader() {
         return logBookHeader;
     }
 
     public void setLogBookHeader(LogBookHeader logBookHeader) {
         this.logBookHeader = logBookHeader;
     }
 
     public Module getModule() {
         return module;
     }
 
     public void setModule(Module module) {
         this.module = module;
     }
 
     public String getProblem() {
         return problem;
     }
 
     public void setProblem(String problem) {
         this.problem = problem;
     }
 
     public UserProfile getReceivedBy() {
         return receivedBy;
     }
 
     public void setReceivedBy(UserProfile receivedBy) {
         this.receivedBy = receivedBy;
     }
 
     public String getSolution() {
         return solution;
     }
 
     public void setSolution(String solution) {
         this.solution = solution;
     }
 
     public UserProfile getSolvedBy() {
         return solvedBy;
     }
 
     public void setSolvedBy(UserProfile solvedBy) {
         this.solvedBy = solvedBy;
     }
 
     public String getSuspect() {
         return suspect;
     }
 
     public void setSuspect(String suspect) {
         this.suspect = suspect;
     }
 
     public Date getTimeReporting() {
         return timeReporting;
     }
 
     public void setTimeReporting(Date timeReporting) {
         this.timeReporting = timeReporting;
     }
 
     public Date getTimeSolved() {
         return timeSolved;
     }
 
     public void setTimeSolved(Date timeSolved) {
         this.timeSolved = timeSolved;
     }
 }
