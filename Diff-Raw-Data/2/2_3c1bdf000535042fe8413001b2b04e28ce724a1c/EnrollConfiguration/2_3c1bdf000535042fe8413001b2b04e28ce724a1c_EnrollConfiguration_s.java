 package pl.agh.enrollme.webflow.model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import java.util.IdentityHashMap;
 
 /**
  * @author Michal Partyka
  */
@Entity
 public class EnrollConfiguration {
 
     @Id
     private Integer enroll_ID;
 
     private int pointsPerTerm;
 
     @Deprecated
     public EnrollConfiguration() {
         enroll_ID=1;
         pointsPerTerm=0;
     }
 
     public EnrollConfiguration(Integer enroll_id, int pointsPerTerm) {
         enroll_ID = enroll_id;
         this.pointsPerTerm = pointsPerTerm;
     }
 
     public Integer getEnroll_ID() {
         return enroll_ID;
     }
 
     public int getPointsPerTerm() {
         return pointsPerTerm;
     }
 
     public void setEnroll_ID(Integer enroll_ID) {
         this.enroll_ID = enroll_ID;
     }
 
     public void setPointsPerTerm(int pointsPerTerm) {
         this.pointsPerTerm = pointsPerTerm;
     }
 }
