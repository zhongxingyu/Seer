 package pl.agh.enrollme.webflow.model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import java.io.Serializable;
 
 @Entity(name="enrollment")
 public class Enrollment implements Serializable {
 
     @Id
    private int enrollmentID;
 
     private String name;
 
     public Enrollment() {
 
     }
 
    public int getEnrollmentID() {
        return enrollmentID;
     }
 
    public void setEnrollmentID(int enrollmentID) {
        this.enrollmentID = enrollmentID;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 }
