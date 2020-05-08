 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model;
 
 import roosterprogramma.Utils;
 
 /**
  *
  * @author Dark
  */
 public class WorkHours {
 
     private String date;
     private String shouldWork;
     private double worked;
     private double compensation150;
     private double compensation200;
     private double vacation;
     private double ADV;
     private double illness;
     private double leave;
     private double withdrawnCompensation;
     private int employeeNumber;
     private String comment;
 
     public WorkHours() {
         this(0, "");
     }
 
     public WorkHours(int employeeNumber, String date) {
         this(employeeNumber, date, "", 0, 0, 0, 0, 0, 0, 0, 0, "");
     }
 
     public WorkHours(int employeeNumber, String date, String shouldWork, double worked,
             double compensation150, double compensation200, double vacation, double ADV,
             double illness, double leave, double withdrawnCompensation, String comment) {
         this.employeeNumber = employeeNumber;
         this.date = date;
         this.shouldWork = shouldWork;
         this.worked = worked;
         this.compensation150 = compensation150;
         this.compensation200 = compensation200;
         this.vacation = vacation;
         this.ADV = ADV;
         this.illness = illness;
         this.leave = leave;
         this.withdrawnCompensation = withdrawnCompensation;
         this.comment = comment;
     }
 
     /**
      * @return the date
      */
     public String getDate() {
         return date;
     }
 
     /**
      * @param date the date to set
      */
     public void setDate(String date) {
         this.date = date;
     }
 
     /**
      * @return the worked
      */
     public double getWorked() {
         return worked;
     }
 
     /**
      * @param worked the worked to set
      */
     public void setWorked(double worked) {
         this.worked = worked;
     }
 
     /**
      * @return the compensation150
      */
     public double getCompensation150() {
         return compensation150;
     }
 
     /**
      * @param compensation150 the compensation150 to set
      */
     public void setCompensation150(double compensation150) {
         this.compensation150 = compensation150;
     }
 
     /**
      * @return the compensation200
      */
     public double getCompensation200() {
         return compensation200;
     }
 
     /**
      * @param compensation200 the compensation200 to set
      */
     public void setCompensation200(double compensation200) {
         this.compensation200 = compensation200;
     }
 
     /**
      * @return the vacation
      */
     public double getVacation() {
         return vacation;
     }
 
     /**
      * @param vacation the vacation to set
      */
     public void setVacation(double vacation) {
         this.vacation = vacation;
     }
 
     /**
      * @return the ADV
      */
     public double getADV() {
         return ADV;
     }
 
     /**
      * @param ADV the ADV to set
      */
     public void setADV(double ADV) {
         this.ADV = ADV;
     }
 
     /**
      * @return the illness
      */
     public double getIllness() {
         return illness;
     }
 
     /**
      * @param illness the illness to set
      */
     public void setIllness(double illness) {
         this.illness = illness;
     }
 
     /**
      * @return the verlof
      */
     public double getLeave() {
         return leave;
     }
 
     /**
      * @param leave the verlof to set
      */
     public void setLeave(double leave) {
         this.leave = leave;
     }
 
     /**
      * @return the project
      */
     public double getWithdrawnCompensation() {
         return withdrawnCompensation;
     }
 
     /**
      * @param withdrawnCompensation the project to set
      */
     public void setWithdrawnCompensation(double withdrawnCompensation) {
         this.withdrawnCompensation = withdrawnCompensation;
     }
 
     /**
      * @return the shouldwork
      */
     public String getShouldWork() {
         return shouldWork;
     }
 
     /**
      * @param shouldWork the shouldwork to set
      */
     public void setShouldWork(String shouldWork) {
         this.shouldWork = shouldWork;
     }
 
     public String getShouldWorkHours() {
        return Double.toString(Utils.intervalToDuration(shouldWork));
     }
 
     /**
      * @return the employeeNumber
      */
     public int getEmployeeNumber() {
         return employeeNumber;
     }
 
     /**
      * @param employeeNumber the employeeNumber to set
      */
     public void setEmployeeNumber(int employeeNumber) {
         this.employeeNumber = employeeNumber;
     }
 
     /**
      * @return the comment
      */
     public String getComment() {
         return comment;
     }
 
     /**
      * @param comment the comment to set
      */
     public void setComment(String comment) {
         this.comment = comment;
     }
 }
