 package pl.agh.enrollme.model;
 
 import pl.agh.enrollme.utils.EnrollmentMode;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Entity
 public class Enroll implements Serializable {
 
     @Transient
     private static final long serialVersionUID = -577123547860923047L;
 
     @Id
     @GeneratedValue
     private Integer enrollID;
 
     private String name;
     private EnrollmentMode enrollmentMode;
 
     @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private EnrollConfiguration enrollConfiguration;
 
     @OneToMany(mappedBy = "enroll", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Subject> subjects = new ArrayList<>();
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private List<Person> persons;
 
     public EnrollConfiguration getEnrollConfiguration() {
         return enrollConfiguration;
     }
 
    public Enroll(String name, EnrollmentMode enrollmentMode, EnrollConfiguration enrollConfiguration, List<Subject> subjects) {
         this.enrollmentMode = enrollmentMode;
         this.enrollConfiguration = enrollConfiguration;
         this.subjects = subjects;
         enrollID = 0;
         this.name = name;
     }
 
     public void addSubject(Subject subject) {
         subjects.add(subject);
     }
 
     /**
      * @param enrollmentMode defines either enroll is completed, closed or currently open
      */
     public void setEnrollmentMode(EnrollmentMode enrollmentMode) {
         this.enrollmentMode = enrollmentMode;
     }
 
     public EnrollmentMode getEnrollmentMode() {
         return enrollmentMode;
     }
 
     /**
      * @param subjects subjects available for this enrollment
      */
    public void setSubjects(List<Subject> subjects) {
         this.subjects = subjects;
     }
 
    public List<Subject> getSubjects() {
         return subjects;
     }
 
     /**
      * @param enrollID primary key
      */
     public void setEnrollID(Integer enrollID) {
         this.enrollID = enrollID;
     }
 
     public List<Person> getPersons() {
         return persons;
     }
 
     /**
      * @param persons list of people who are allowed for this enrollment
      */
     public void setPersons(List<Person> persons) {
         this.persons = persons;
     }
 
     /**
      * @param enrollConfiguration object enrollConfiguration - configuration of this enrollment
      */
     public void setEnrollConfiguration(EnrollConfiguration enrollConfiguration) {
         this.enrollConfiguration = enrollConfiguration;
     }
 
     public Enroll() {
         enrollID = 0;
         name = "";
     }
 
     /**
      * @param name name of the enrollment
      */
     public void setName(String name) {
         this.name = name;
     }
 
     public Integer getEnrollID() {
 
         return enrollID;
     }
 
     public String getName() {
         return name;
     }
 }
