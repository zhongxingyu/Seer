 package com.easytag.web.beans;
 
 import com.easytag.core.entity.jpa.User;
import com.easytag.core.entity.jpa.UserProfile;
 import com.easytag.core.managers.UserManagerLocal;
 import com.easytag.web.utils.JSFHelper;
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 @ManagedBean
 @SessionScoped
 public class CurrentUserBean implements Serializable {
     @EJB
     UserManagerLocal userMan;
     
     private Long userId;
     private User user;
    private UserProfile userProfile;
 
     @PostConstruct
     private void init() {
         this.userId = new JSFHelper().getCurrentUserId();
         this.user = userMan.getUserById(userId);
        if (this.user != null)
            this.userProfile = userMan.getUserProfile(user);
    }
    
    public UserProfile getUserProfile() {
        return this.userProfile;
     }
 
     public Long getUserId() {
         return userId;
     }
     
     public User getUser() {
         return user;
     }
 }
