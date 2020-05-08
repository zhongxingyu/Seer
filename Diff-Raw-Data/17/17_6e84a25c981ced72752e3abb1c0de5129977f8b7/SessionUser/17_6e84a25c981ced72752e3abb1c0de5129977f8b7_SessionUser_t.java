 package org.jboss.seam.examples.seamcrm.aaa;
 
 import java.io.Serializable;
import java.util.Date;
 
import javax.faces.event.ActionEvent;
 
import org.jboss.seam.examples.seamcrm.user.User;
 
 /**
  * 
  * @author Cody Lerum
  * 
  */
 public class SessionUser implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private User user;
     private String remoteAddr;
     private String imageURL = "";
    private Date lastCheckIn;
 
     public SessionUser(User user, String remoteAddr) {
         this.user = user;
         this.remoteAddr = remoteAddr;
     }
 
     public User getUser() {
         return user;
     }
 
     public String getRemoteAddr() {
         return remoteAddr;
     }
 
     public String getImageURL() {
         return imageURL;
     }
 
    public Date getLastCheckIn() {
        return lastCheckIn;
    }

    public void checkIn(ActionEvent event) {
        lastCheckIn = new Date();
    }

 }
