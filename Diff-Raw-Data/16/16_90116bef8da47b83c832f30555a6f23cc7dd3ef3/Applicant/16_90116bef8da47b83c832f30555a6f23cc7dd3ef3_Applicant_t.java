 package vaadinForm;
 
 /**
  * JavaBean class for the applicant.
  * @author Juho
  */
 public class Applicant {
     
     private String firstName;
     private String lastName;
     private String gender;
     private String arguments;
     
    public Applicant(){
        //Set default gender to fix problem with optiongroup default value.
        this.gender = "Male";
    }
    
     public void setFirstName(String firstName){
         this.firstName = firstName;
     }
     
     public String getFirstName(){
         return this.firstName;
     }
         
     public void setLastName(String lastName){
         this.lastName = lastName;
     }
     
     public String getLastName(){
         return this.lastName;
     }
     
     public void setGender(String gender){
         this.gender = gender;
     }
     
     public String getGender(){
         return this.gender;
     }
     
     public void setArguments(String arguments){
         this.arguments = arguments;
     }
     
     public String getArguments(){
         return this.arguments;
     }
     
 }
