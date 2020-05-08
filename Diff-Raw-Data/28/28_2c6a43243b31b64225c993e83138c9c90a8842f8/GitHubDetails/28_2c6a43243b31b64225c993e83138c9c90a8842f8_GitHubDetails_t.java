 package com.worksnet.model.workdetails;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 
 /**
  * @author Max Levicky
  *         Date: 24.03.13
  *         Time: 13:02
  */
 @Entity
 public class GitHubDetails extends WorkDetail {
    @Column(name = "login", nullable = false)
     protected String login;
 
    @Column(name = "checked", nullable = false)
    protected boolean checked = false;
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     public boolean isChecked() {
         return checked;
     }
 
     public void setChecked(boolean checked) {
         this.checked = checked;
     }
 }
