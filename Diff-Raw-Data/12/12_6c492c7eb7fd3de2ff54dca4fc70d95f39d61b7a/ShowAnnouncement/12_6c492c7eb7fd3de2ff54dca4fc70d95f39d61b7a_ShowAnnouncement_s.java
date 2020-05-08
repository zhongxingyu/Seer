 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.preppa.web.pages.announcements;
 
 import com.preppa.web.data.AnnouncementDAO;
 import com.preppa.web.entities.Announcement;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 /**
  *
  * @author newtonik
  */
 public class ShowAnnouncement {

     @Persist
     @Property
     private Announcement announce;
     @Inject
     private AnnouncementDAO announceDAO;
 
     Object onActivate(int id) {
         this.announce = announceDAO.findById(id);
 
         return this;
     }
 
     void setannounce(Announcement announce) {
         this.announce = announce;
     }
 }
