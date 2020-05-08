 package org.otherobjects.cms.model;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Transient;
 
 import org.springframework.util.Assert;
 
 @Entity
 @SequenceGenerator(name = "PasswordChangeRequestSequence", sequenceName = "password_change_request_seq")
 public class PasswordChangeRequest
 {
     private Long id;
     private String token;
     private Date requestDate;
     private User user;
 
     @Transient
     public String getChangeRequestCode()
     {
         Assert.notNull(id, "You can't get the change request code before it has been persisted");
         Assert.notNull(id, "No token has been set yet");
 
         return id.toString() + "-" + token;
     }
 
     public static Object[] splitPasswordChangeRequestIdentifier(String changeRequestCode)
     {
         Assert.hasText(changeRequestCode, "changeRequestCode can't be empty string or null");
         Object[] result = new Object[2];
 
         int dashPos = changeRequestCode.indexOf('-');
         Assert.isTrue(dashPos > -1, "changeRequestCode is of the wrong format; needs to have a least one dash");
         result[0] = new Long(changeRequestCode.substring(0, dashPos));
 
         result[1] = new String(changeRequestCode.substring(dashPos + 1));
 
         return result;
     }
 
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PasswordChangeRequestSequence")
     public Long getId()
     {
         return this.id;
     }
 
     public void setId(Long id)
     {
         this.id = id;
     }
 
     @Column(name = "request_date", nullable = false)
     public Date getRequestDate()
     {
         return requestDate;
     }
 
     public void setRequestDate(Date requestDate)
     {
         this.requestDate = requestDate;
     }
 
     @Column(nullable = false)
     public String getToken()
     {
         return token;
     }
 
     public void setToken(String token)
     {
         this.token = token;
     }
 
     @ManyToOne()
    @JoinColumn(name="user_id")
     public User getUser()
     {
         return user;
     }
 
     public void setUser(User user)
     {
         this.user = user;
     }
 
 }
