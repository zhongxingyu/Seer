 package com.easytag.web.beans;
 
 import com.easytag.core.entity.jpa.User;
 import com.easytag.core.entity.jpa.UserProfile;
 import com.easytag.core.managers.Application;
 import com.easytag.core.managers.UserManagerLocal;
 import com.easytag.web.utils.JSFHelper;
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 @ManagedBean
 @ViewScoped
 public class UserBean implements Serializable {
     private Long userId;
     private User user;
     private UserProfile userProfile;
     
     @ManagedProperty("#{currentUserBean}")
     private CurrentUserBean currentUserBean;
     
     @EJB
     private UserManagerLocal userMan;
 
     public static final Logger log = LogManager.getLogger(UserBean.class.getName());
     
     @PostConstruct
     private void init() {
         //setUserId(new JSFHelper().getRequest().getParameter("id"));
     }
 
     public User getCurrentUser() {
         return currentUserBean.getUser();
     }
     
     public Long getCurrentUserId() {
         return currentUserBean.getUserId();
     }
     
     public String getUserId() {
         return userId==null ? null : userId.toString();
     }
 
     public User getUser() {
         return user;
     }
 
     public UserProfile getUserProfile() {
         return userProfile;
     }
     
    public Long getGuestUserId() {
        return Application.getGuestUserId();
    }
    
     public void hideEmail(User user){
         if (user == null || user.getId() != getCurrentUserId()){
                 user.setEmail("hidden");
         }
     }
     
     public void setUserId(String userId) {
         log.trace("setUserId = " + userId);
         if (userId == null || userId.isEmpty()) {
             // initialize with current user if authorized
             this.userId = currentUserBean.getUserId();
             log.trace("... resolved to " + this.userId);
             if (this.userId == null) {
                 // we are not authorized
                 // full stop...
                 return;
             }
             user = currentUserBean.getUser();            
             if (user != null) { // if user logged in
                 hideEmail(user);
                 userProfile = userMan.getUserProfile(user);
             }
             return;
         }
         try {
             // userId is not empty
             // try to load this user
             this.userId = Long.parseLong(userId);
             user = userMan.getUserById(this.userId);
             if (user != null) {
                 hideEmail(user);
                 userProfile = userMan.getUserProfile(user);
             }
         } catch (Exception ex) {
             this.userId = null;
         }
     }
     
     public boolean isCurrent() {
         if (currentUserBean.getUserId() == null)
             return false;
         if (this.userId == null)
             return true;
         return currentUserBean.getUserId().toString().equals(this.userId.toString());
     }
 
     public void setCurrentUserBean(CurrentUserBean currentUserBean) {
         this.currentUserBean = currentUserBean;
     }
     
     public String getDisabledClass(boolean disable){
         return disable ? "": "disabled";        
     }
     
     public String disableCreateButton(String userId){
         if (userId == "" || userId == null){
             return "";
         }
         Long id = Long.parseLong(userId);
         return (id == this.userId) ? "": "disabled";        
     }
     
     public User findById(String userId) {
         Long id = null;
         try {
             id = Long.parseLong(userId);
             log.trace("userid: " + id);
         } catch (Exception ex) {
         }
         if (id == null) { // || id == this.userId
             return user;
         }
         return userMan.getUserById(id);
     }
     
     public String redirectIfNotAuthorized() {
         log.trace("UserBean.redirectIfNotAuthorized()");
         if (new JSFHelper().getCurrentUserId() == null) {
             return "/index?faces-redirect=true";
         }
         return null;
     }
 }
