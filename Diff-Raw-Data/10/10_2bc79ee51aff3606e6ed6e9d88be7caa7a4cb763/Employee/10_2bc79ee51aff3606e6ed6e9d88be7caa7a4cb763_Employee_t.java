 package exabstract;
 
 /**
  *
  * @author Dan Smith
  */
 public abstract class Employee {
     // Fields for employees
     private String firstName;
     private String lastName;
     private String employeeNumber;
     private String department;
     
     // Constructor
     public Employee(String firstName, String lastName, String employeeNumber, 
             String department) {
         this.firstName = firstName;
         this.lastName = lastName;
         this.employeeNumber = employeeNumber;
         this.department = department;
     }
     
 
     // Getters and Setters
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
      * @return the employeeNumber
      */
     public String getEmployeeNumber() {
         return employeeNumber;
     }
 
     /**
      * @param employeeNumber the employeeNumber to set
      */
     public void setEmployeeNumber(String employeeNumber) {
         this.employeeNumber = employeeNumber;
     }
 
     /**
      * @return the department
      */
     public String getDepartment() {
         return department;
     }
 
     /**
      * @param department the department to set
      */
     public void setDepartment(String department) {
         this.department = department;
     }
 
     /**
      * Abstract method
      */
     public abstract double getHoursWorked();
 
     /**
      * An abstract method
      */
     public abstract void setHoursWorked(double hoursWorked);
     
    /*
     * FIXED! CANNOT instantiate an abstract class!
     * github is awesome!
     */
 }
