 package org.jrecruiter.web.actions.registration;
 
 import org.apache.struts2.convention.annotation.Result;
 import org.jrecruiter.model.User;
 import org.jrecruiter.service.UserService;
 import org.jrecruiter.web.actions.BaseAction;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 
 /**
  * Retrieves all jobs and returns an XML document. The structure conforms to the layout
  * defined by Indeed.com
  *
  * @author Gunnar Hillert
  * @version $Id:UserService.java 128 2007-07-27 03:55:54Z ghillert $
  */
 @Controller
 @Result(name="success", location="index", type="redirectAction")
 public class AccountVerificationAction extends BaseAction {
 
     private @Autowired UserService userService;
 
     private String key;
 
     /** serialVersionUID. */
     private static final long serialVersionUID = -3422780336408883930L;
 
     public String execute() {
 
         if (this.key != null && this.key.length() != 0) {
             return process();
         }
 
         return INPUT;
 
     }
 
     public String process() {
 
         final User userFromDb = userService.getUserByVerificationKey(this.key);
 
         if (userFromDb == null) {
            super.addFieldError("key", super.getText("class.AccountValidationAction.field.required.key"));
             return INPUT;
         }
 
         if (userFromDb.isEnabled()) {
            super.addActionMessage(super.getText("class.AccountValidationAction.already.enabled"));
         } else {
             userFromDb.setEnabled(Boolean.TRUE);
             userService.updateUser(userFromDb);
            super.addActionMessage(super.getText("class.AccountValidationAction.success"));
         }
 
         return SUCCESS;
     }
 
     public void validate() {
         if (this.key == null || this.key.trim().length() == 0) {
            super.addFieldError("key", super.getText("class.AccountValidationAction.field.required.key"));
         }
     }
 
     public String getKey() {
         return key;
     }
 
     public void setKey(String key) {
         this.key = key;
     }
 
 }
