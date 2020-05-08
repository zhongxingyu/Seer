 package edu.wctc.bean.demo6;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ActionEvent;
 
 /**
  *
  * @author jlombardo
  */
 @ManagedBean(name="normal")
 @SessionScoped
public class NormalBean{
  
     public String buttonId; 
 
     public void printIt(ActionEvent event){
 
             //Get submit button id
             buttonId = event.getComponent().getClientId();
             buttonId = "From ActionListener event: " + buttonId;
     }
 
     public String outcome(){
             return "page2";
     }
 
     public String getButtonId() {
         return buttonId;
     }
 
     public void setButtonId(String buttonId) {
         this.buttonId = buttonId;
     }
     
     
 }
