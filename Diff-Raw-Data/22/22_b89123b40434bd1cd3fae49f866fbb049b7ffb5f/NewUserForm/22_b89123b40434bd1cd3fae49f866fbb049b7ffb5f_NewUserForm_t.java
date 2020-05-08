 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.uniriotec.tracker.struts;
 
 import javax.servlet.http.HttpServletRequest;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 
 /**
  *
  * @author afonso
  */
 public class NewUserForm extends org.apache.struts.action.ActionForm {
     
     private String name;
     private String lastName;
     private String email;
     private String password;
     private String confirmedPassword;
     
     // error message
     private String error;
 
     public String getConfirmedPassword() {
         return confirmedPassword;
     }
 
     public void setConfirmedPassword(String confirmedPassword) {
         this.confirmedPassword = confirmedPassword;
     }
     
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getError() {
         return error;
     }
 
     public void setError() {
         this.error =
                "<span style='color:red'>You miss some fields, or email already exists on our DB</span>";
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * @return
      */
     public String getEmail() {
         return email;
     }
 
     /**
      * @param string
      */
     public void setEmail(String string) {
         email = string;
     }
 
     /**
      *
      */
     public NewUserForm() {
         super();
     }
 
     /**
      * This is the action called from the Struts framework.
      * @param mapping The ActionMapping used to select this instance.
      * @param request The HTTP Request we are processing.
      * @return
      */
     @Override
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
         ActionErrors errors = new ActionErrors();
         if (getEmail()== null || getName() == null || getConfirmedPassword() == null || getLastName() == null || getPassword().length() < 1) {
             errors.add("email", new ActionMessage("error.email.required"));
             // TODO: add 'error.email.required' key to your resources
         }
         if (!getEmail().equals(getConfirmedPassword())) {
             errors.add("email", new ActionMessage("error.email.required"));
         }
         return errors;
     }
 }
