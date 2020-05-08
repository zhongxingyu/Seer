 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.preppa.web.pages.announcements;
 
 import com.preppa.web.data.AnnouncementDAO;
 import com.preppa.web.entities.Announcement;
import com.preppa.web.entities.User;
import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 /**
  *
  * @author newtonik
  */
 public class ShowAnnouncement {
    @ApplicationState
    private User user;
     @Persist
     @Property
     private Announcement announce;
     @Inject
     private AnnouncementDAO announceDAO;
 

    public int getUID() {
        return user.getId();
    }

     Object onActivate(int id) {
         this.announce = announceDAO.findById(id);
 
         return this;
     }
 
     void setannounce(Announcement announce) {
         this.announce = announce;
     }
 }
