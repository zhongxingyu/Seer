 
 package myconcrete;
 
 /**
  *
  * @author Michael Grinker
  */
 public class Employee {
     private String firstName;
     private String lastName;
     private int hoursWorked;
     private String badgeID;
 
     public Employee(String firstName, String lastName, int hoursWorked, String badgeID) {
         this.firstName = firstName;
         this.lastName = lastName;
         this.hoursWorked = hoursWorked;
         this.badgeID = badgeID;
     }
     
     /**
      * @return the firstName
      */
     public String getFirstName() {
         return firstName;
     }
 
     /**
      * @param firstName the firstName to set
      */
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     /**
      * @return the lastName
      */
     public String getLastName() {
         return lastName;
     }
 
     /**
      * @param lastName the lastName to set
      */
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     /**
      * @return the hoursWorked
      */
     public int getHoursWorked() {
         return hoursWorked;
     }
 
     /**
      * @param hoursWorked the hoursWorked to set
      */
     public void setHoursWorked(int hoursWorked) {
         this.hoursWorked = hoursWorked;
     }
 
     /**
      * @return the badgeID
      */
     public String getBadgeID() {
         return badgeID;
     }
 
     /**
      * @param badgeID the badgeID to set
      */
     public void setBadgeID(String badgeID) {
         this.badgeID = badgeID;
     }
     
     public String toString(){
         String str;
         
         str = "Name: " + firstName + " " + lastName
                 + "\nID Number: " + badgeID
                + "Hours Worked: " + hoursWorked;
         
         return str;
     }
     
     
     
 }
