 package net.mademocratie.gae.server.entities;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.google.appengine.api.datastore.Email;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import net.mademocratie.gae.server.services.helper.DateHelper;
 import org.joda.time.Duration;
 
 import javax.persistence.Transient;
 import java.util.Date;
 
 @Entity
 public abstract class Contribution implements IContribution {
     @Transient
     public static final String SOMENONE = "someone";
 
     @Id
     protected Long itemIt;
 
     @Index
     protected Date date;
 
     @Index
     protected Email authorEmail;
 
     @Index
     private String authorPseudo = SOMENONE;
 
 
    public Contribution() {
        this.date = new Date();
    };

     protected Contribution(String authorEmail) {
         this.date = new Date();
         setAuthorEmailString(authorEmail);
     }
 
     public Contribution(Contribution c) {
         setItemIt(c.getItemIt());
         setDate(c.getDate());
         if (getDate() == null) {
             setDate(new Date());
         }
         this.setAuthorEmail(c.getAuthorEmail());
         this.setAuthorPseudo(c.getAuthorPseudo());
     }
 
     public Long getItemIt() {
         return itemIt;
     }
 
     public void setItemIt(Long itemIt) {
         this.itemIt = itemIt;
     }
 
     public Date getDate() {
         return date;
     }
 
     public String getDateFormat() {
         return DateHelper.getDateFormat(getDate());
     }
 
     @JsonProperty("age")
     public String getAge() {
         return DateHelper.getDateDuration(getDate());
     }
 
     public Email getAuthorEmail() {
         return authorEmail;
     }
 
     public String getAuthorEmailString() {
         return (authorEmail != null ? authorEmail.getEmail() : null);
     }
 
     public void setAuthorEmailString(String authorEmail) {
         this.authorEmail = (authorEmail != null ? new Email(authorEmail) : null);
     }
 
     public void setAuthorEmail(Email authorEmail) {
         this.authorEmail = authorEmail;
     }
 
     public String getAuthorPseudo() {
         if (authorPseudo != null) {
             return authorPseudo;
         }
         return SOMENONE;
     }
 
     public void setAuthorPseudo(String authorPseudo) {
         this.authorPseudo = authorPseudo;
     }
 
     public abstract String getContributionDetails();
 
     public abstract String getContributionType();
 
     public void setDate(Date date) {
         this.date = date;
     }
 }
