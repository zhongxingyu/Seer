 package id.web.martinusadyh.logbook.domain.trx;
 
 import id.web.martinusadyh.logbook.domain.BaseEntity;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 
 /**
  *
  * @author martinus
  */
 @Entity
 @Table(name="logbook_header")
 public class LogBookHeader extends BaseEntity {
     
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     @Column(name="log_date")
     private Date logDate;
     
    @OneToMany(mappedBy = "logBookHeader")
     private List<LogBookDetails> logBookDetails;
 
     public List<LogBookDetails> getLogBookDetails() {
         return logBookDetails;
     }
 
     public void setLogBookDetails(List<LogBookDetails> logBookDetails) {
         this.logBookDetails = logBookDetails;
     }
 
     public Date getLogDate() {
         return logDate;
     }
 
     public void setLogDate(Date logDate) {
         this.logDate = logDate;
     }
 }
