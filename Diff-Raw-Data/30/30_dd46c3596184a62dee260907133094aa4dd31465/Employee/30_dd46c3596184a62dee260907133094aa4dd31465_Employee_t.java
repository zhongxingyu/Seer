 package lab0;
 
 import java.util.Date;
 
 /**
  * In this challenge you need to address the probability that at some
  * point the arguments to method parameters may not be valid. 
  * <p>
  * For example, String arguments may be null or empty; other objects may be 
  * null; or primitive numbers may be out of acceptable range.
  * <p>
  * You need to validate ALL method parameters to make sure any and all 
  * arguments are valid. The only exception is when any argument is acceptable 
  * based on requirements. Fix the code below using if logic to validate
  * method arguments and throw IllegalArgumentException or a custom
  * exception if the validation fails.
  * 
  * @author  Jim Lombardo, jlombardo@wctc.edu
  * @version 1.00
  */
 public class Employee {
     public static final int MAX_VACATION_DAYS = 28;
     private String firstName;
     private String lastName;
     private String ssn;
     private Date hireDate;
     private int daysVacation;
 
     public Employee() {
         firstName = "Unknown";
         lastName = "Unknown";
         ssn = "Unknown";
         hireDate = new Date();
         daysVacation = 0;
     }
 
     public Employee(String firstName, String lastName, String ssn, Date hireDate, int daysVacation) {
         this.firstName = firstName;
         this.lastName = lastName;
         this.ssn = ssn;
         this.hireDate = hireDate;
         this.daysVacation = daysVacation;
     }
     
     public int getDaysVacation() {
         return daysVacation;
     }
 
     public void setDaysVacation(int daysVacation) {
         this.daysVacation = daysVacation;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public Date getHireDate() {
         return hireDate;
     }
 
     public void setHireDate(Date hireDate) {
        if(hireDate == null) {
            throw new IllegalArgumentException("hireDate cannot be null");
        }
         this.hireDate = hireDate;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public final String getSsn() {
         return ssn;
     }
 
     public void setSsn(String ssn) {
        validateSsn(ssn);
         this.ssn = ssn;
     }
     
    private void validateStringInput(String theString, String stringName) 
            throws IllegalArgumentException{
        if (theString == null || theString.length() == 0) {
            throw new IllegalArgumentException(stringName + " must not be null or " +
                    "blank.");
        }
    }
     
    private void validateSsn(String ssn) throws IllegalArgumentException {
        validateStringInput(ssn, "ssn");
        if (ssn.matches("[0-9]{3}\\-[0-9]{2}\\-[0-9]{4}")) {
            //ssn is correct format
        }
        else {
            throw new IllegalArgumentException("ssn format does not match template.");
        }
    }
 }
